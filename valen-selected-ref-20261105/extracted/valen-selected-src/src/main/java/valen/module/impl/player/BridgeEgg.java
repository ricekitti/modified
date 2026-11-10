/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Items
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.play.server.S08PacketPlayerPosLook
 *  net.minecraft.network.play.server.S22PacketMultiBlockChange
 *  net.minecraft.network.play.server.S22PacketMultiBlockChange$BlockUpdateData
 *  net.minecraft.network.play.server.S23PacketBlockChange
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.MathHelper
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.World
 *  valen.module.impl.player.BridgeEgg$SupportUpdate
 *  valen.utility.VoidProximity
 */
package valen.module.impl.player;

import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import valen.Valen;
import valen.event.ClientRotationEvent;
import valen.event.PlayerMoveEvent;
import valen.event.PostMotionEvent;
import valen.event.ReceivePacketEvent;
import valen.helper.RotationHelper;
import valen.lunar.forge.fml.common.eventhandler.EventPriority;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;
import valen.lunar.forge.fml.common.gameevent.TickEvent;
import valen.module.Module;
import valen.module.ModuleManager;
import valen.module.impl.player.BridgeEgg;
import valen.module.impl.render.Trajectories;
import valen.module.setting.impl.ButtonSetting;
import valen.module.setting.impl.SliderSetting;
import valen.utility.ContinuousCondition;
import valen.utility.RescueAttempt;
import valen.utility.RescueFall;
import valen.utility.RescueItemUse;
import valen.utility.RotationUtils;
import valen.utility.TimerController;
import valen.utility.Utils;
import valen.utility.VoidProximity;

public final class BridgeEgg
extends Module {
    private static final int EXPERIMENTAL_BRIDGE_DEPTH = 5;
    private static final long ATTEMPT_TIMEOUT_MS = 1800L;
    private final SliderSetting lowestY = new SliderSetting("Lowest Y", 40.0, 0.0, 255.0, 1.0);
    private final ButtonSetting inVoidFor = new ButtonSetting("In void for", false);
    private final SliderSetting voidTime = new SliderSetting("Time", "s", 0.5, 0.5, 5.0, 0.1);
    private final ButtonSetting waitForClutch = new ButtonSetting("Wait for Clutch", true);
    private final SliderSetting preTimer = new SliderSetting("Pre timer speed", "x", 1.0, 0.0, 1.0, 0.01);
    private final SliderSetting afterTimer = new SliderSetting("After timer speed", "x", 0.03, 0.0, 1.0, 0.01);
    private final ButtonSetting useStasis = new ButtonSetting("Use Stasis instead", false);
    private final ContinuousCondition voidDuration = new ContinuousCondition();
    private Vec3 suspendedMotion;
    private Vec3 suspendedPosition;
    private final RescueAttempt attempt = new RescueAttempt();
    private final ConcurrentLinkedQueue<SupportUpdate> updates = new ConcurrentLinkedQueue();
    private EntityPlayer player;
    private Object world;
    private BlockPos bridge;
    private float yaw;
    private float aimPitch;
    private Vec3 catchPosition;
    private int slot = -1;
    private int previousSlot = -1;
    private int restoreTick = -1;
    private volatile boolean corrected;

    public BridgeEgg() {
        super("Bridge Egg Clutch", Module.category.player);
        this.registerSetting(this.lowestY);
        this.registerSetting(this.inVoidFor);
        this.registerSetting(this.voidTime);
        this.registerSetting(this.waitForClutch);
        this.registerSetting(this.preTimer);
        this.registerSetting(this.afterTimer);
        this.registerSetting(this.useStasis);
        this.voidTime.visible = false;
    }

    @Override
    public void guiUpdate() {
        this.voidTime.setVisible(this.inVoidFor.isToggled(), this);
        this.preTimer.setVisible(!this.useStasis.isToggled(), this);
        this.afterTimer.setVisible(!this.useStasis.isToggled(), this);
    }

    public static boolean isBridgeEgg(ItemStack itemStack) {
        return itemStack != null && itemStack.stackSize > 0 && itemStack.getItem() == Items.egg;
    }

    public static int generatedY(double d) {
        return MathHelper.floor_double((double)d) - 5;
    }

    private int findEgg() {
        for (int i = 0; i < 9; ++i) {
            if (!BridgeEgg.isBridgeEgg(BridgeEgg.mc.thePlayer.inventory.mainInventory[i])) continue;
            return i;
        }
        return -1;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean eligible() {
        if (!Utils.nullCheck()) return false;
        if (BridgeEgg.mc.playerController == null) return false;
        if (BridgeEgg.mc.currentScreen != null) return false;
        if (BridgeEgg.mc.thePlayer.isDead) return false;
        if (BridgeEgg.mc.thePlayer.onGround) return false;
        double d = this.suspendedMotion == null ? BridgeEgg.mc.thePlayer.motionY : this.suspendedMotion.yCoord;
        if (!(d < 0.0)) return false;
        if (BridgeEgg.mc.thePlayer.isInWater()) return false;
        if (BridgeEgg.mc.thePlayer.isInLava()) return false;
        if (BridgeEgg.mc.thePlayer.capabilities.isFlying) return false;
        if (ModuleManager.isBedMining()) return false;
        if (ModuleManager.autoPearl != null) {
            if (ModuleManager.autoPearl.isRescueActive()) return false;
        }
        if (ModuleManager.fireballPearlClutch != null) {
            if (ModuleManager.fireballPearlClutch.isRescueActive()) return false;
        }
        if (!this.waitForClutch.isToggled()) return true;
        if (ModuleManager.clutch == null) return true;
        if (ModuleManager.clutch.canSave()) return false;
        return true;
    }

    public boolean isAttemptActive() {
        this.refresh();
        return this.isEnabled() && this.attempt.active();
    }

    public boolean canSave() {
        if (!this.isEnabled()) {
            return false;
        }
        this.refresh();
        if (this.attempt.active()) {
            return true;
        }
        if (this.attempt.state() != RescueAttempt.State.IDLE || !this.eligible() || !this.voidReady() || this.findEgg() < 0) {
            return false;
        }
        RescueFall rescueFall = this.predictedFall();
        return rescueFall != null && Trajectories.getThrowRotations((EntityPlayer)BridgeEgg.mc.thePlayer, Items.egg, rescueFall.catchPosition, false) != null;
    }

    @Override
    public void onEnable() {
        this.reset();
    }

    @Override
    public void onDisable() {
        this.reset();
    }

    private void reset() {
        this.releaseStasis();
        this.voidDuration.reset();
        if (this.player == BridgeEgg.mc.thePlayer) {
            RescueItemUse.restore(this.previousSlot);
        }
        this.previousSlot = -1;
        this.restoreTick = -1;
        this.player = BridgeEgg.mc.thePlayer;
        this.world = BridgeEgg.mc.theWorld;
        this.attempt.reset();
        this.updates.clear();
        this.corrected = false;
        this.bridge = null;
        this.aimPitch = 90.0f;
        this.catchPosition = null;
        this.slot = -1;
        Valen.timerController.release(TimerController.Owner.BRIDGE_EGG);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onRotation(ClientRotationEvent clientRotationEvent) {
        this.refresh();
        if (this.attempt.state() == RescueAttempt.State.IDLE && this.canSave()) {
            this.player = BridgeEgg.mc.thePlayer;
            this.world = BridgeEgg.mc.theWorld;
            this.slot = this.findEgg();
            if (!this.updateAim()) {
                this.attempt.fail();
                this.updateControl();
                return;
            }
            this.attempt.begin(System.currentTimeMillis(), 1800L);
        }
        if (this.attempt.state() == RescueAttempt.State.AIMING) {
            if (!(this.eligible() && this.slot >= 0 && BridgeEgg.isBridgeEgg(this.player.inventory.mainInventory[this.slot]) && this.updateAim())) {
                this.attempt.fail();
                this.updateControl();
                return;
            }
            clientRotationEvent.setYaw(Float.valueOf(this.yaw));
            clientRotationEvent.setPitch(Float.valueOf(this.aimPitch));
            RotationHelper.get().forceMovementFix = true;
        }
        this.updateControl();
    }

    @SubscribeEvent
    public void onPostMotion(PostMotionEvent postMotionEvent) {
        this.refresh();
        if (this.attempt.state() != RescueAttempt.State.AIMING) {
            return;
        }
        if (!this.eligible() || !BridgeEgg.isBridgeEgg(this.player.inventory.mainInventory[this.slot])) {
            this.attempt.fail();
            return;
        }
        if ((double)Math.abs(RotationUtils.serverRotations[1] - this.aimPitch) > 0.01 || (double)Math.abs(MathHelper.wrapAngleTo180_float((float)(RotationUtils.serverRotations[0] - this.yaw))) > 0.01) {
            return;
        }
        if (this.bridge == null || this.catchPosition == null) {
            this.attempt.fail();
            return;
        }
        this.previousSlot = this.player.inventory.currentItem;
        boolean bl = false;
        try {
            if (RescueItemUse.use(this.slot)) {
                this.attempt.thrown(this.player.ticksExisted);
                this.restoreTick = this.player.ticksExisted + 1;
                bl = true;
            } else {
                this.attempt.fail();
            }
        }
        finally {
            if (!bl) {
                this.restoreSlot();
            }
            this.updateControl();
        }
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent renderTickEvent) {
        this.refresh();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent clientTickEvent) {
        this.refresh();
    }

    @SubscribeEvent
    public void onPacket(ReceivePacketEvent receivePacketEvent) {
        if (receivePacketEvent.getPacket() instanceof S08PacketPlayerPosLook) {
            this.corrected = true;
        } else if (receivePacketEvent.getPacket() instanceof S23PacketBlockChange) {
            S23PacketBlockChange s23PacketBlockChange = (S23PacketBlockChange)receivePacketEvent.getPacket();
            this.updates.add(new SupportUpdate(s23PacketBlockChange.getBlockPosition(), s23PacketBlockChange.getBlockState()));
        } else if (receivePacketEvent.getPacket() instanceof S22PacketMultiBlockChange) {
            for (S22PacketMultiBlockChange.BlockUpdateData blockUpdateData : ((S22PacketMultiBlockChange)receivePacketEvent.getPacket()).getChangedBlocks()) {
                this.updates.add(new SupportUpdate(blockUpdateData.getPos(), blockUpdateData.getBlockState()));
            }
        }
    }

    private void refresh() {
        SupportUpdate supportUpdate;
        if (this.player != BridgeEgg.mc.thePlayer || this.world != BridgeEgg.mc.theWorld || !Utils.nullCheck()) {
            this.reset();
            return;
        }
        if (this.previousSlot >= 0 && this.player.ticksExisted >= this.restoreTick) {
            this.restoreSlot();
        }
        if (BridgeEgg.mc.thePlayer.onGround && !this.attempt.active()) {
            this.attempt.reset();
            this.corrected = false;
        }
        if (this.attempt.state() == RescueAttempt.State.IDLE) {
            this.voidReady();
        }
        boolean bl = false;
        while ((supportUpdate = this.updates.poll()) != null) {
            AxisAlignedBB axisAlignedBB;
            if (this.bridge == null || this.attempt.state() == RescueAttempt.State.AIMING || !this.attempt.active() || supportUpdate.pos.getY() != this.bridge.getY() || Math.abs(supportUpdate.pos.getX() - this.bridge.getX()) > 1 || Math.abs(supportUpdate.pos.getZ() - this.bridge.getZ()) > 1 || (axisAlignedBB = supportUpdate.state.getBlock().getCollisionBoundingBox((World)BridgeEgg.mc.theWorld, supportUpdate.pos, supportUpdate.state)) == null || axisAlignedBB.maxY > this.player.posY + 0.01) continue;
            RescueFall rescueFall = RescueFall.predict(this.player, axisAlignedBB.maxY, this.suspendedMotion);
            Vec3 vec3 = rescueFall.catchPosition;
            bl |= rescueFall.lands || vec3 != null && vec3.xCoord + 0.3 > axisAlignedBB.minX && vec3.xCoord - 0.3 < axisAlignedBB.maxX && vec3.zCoord + 0.3 > axisAlignedBB.minZ && vec3.zCoord - 0.3 < axisAlignedBB.maxZ;
        }
        if (BridgeEgg.mc.currentScreen != null || this.player.isDead || this.player.onGround) {
            this.voidDuration.reset();
        }
        boolean bl2 = this.suspendedPosition != null && this.player.getPositionVector().squareDistanceTo(this.suspendedPosition) > 1.0E-6 || this.corrected || BridgeEgg.mc.currentScreen != null || this.player.isDead || ModuleManager.isBedMining() || ModuleManager.fireballPearlClutch != null && ModuleManager.fireballPearlClutch.isRescueActive() || ModuleManager.autoPearl != null && ModuleManager.autoPearl.isRescueActive();
        this.attempt.advance(this.player.ticksExisted, System.currentTimeMillis(), bl || this.player.onGround, bl2);
        this.updateControl();
    }

    private void restoreSlot() {
        if (this.player == BridgeEgg.mc.thePlayer) {
            RescueItemUse.restore(this.previousSlot);
        }
        this.previousSlot = -1;
        this.restoreTick = -1;
    }

    private boolean voidReady() {
        boolean bl;
        if (!this.inVoidFor.isToggled()) {
            this.voidDuration.reset();
            return true;
        }
        boolean bl2 = bl = Utils.nullCheck() && BridgeEgg.mc.currentScreen == null && !BridgeEgg.mc.thePlayer.isDead && !BridgeEgg.mc.thePlayer.onGround;
        if (bl) {
            for (double d : new double[]{-0.3, 0.3}) {
                for (double d2 : new double[]{-0.3, 0.3}) {
                    bl &= VoidProximity.isUnsupported((World)BridgeEgg.mc.theWorld, (double)(BridgeEgg.mc.thePlayer.posX + d), (double)BridgeEgg.mc.thePlayer.posY, (double)(BridgeEgg.mc.thePlayer.posZ + d2));
                }
            }
        }
        return this.voidDuration.update(bl, System.nanoTime(), (long)(this.voidTime.getInput() * 1.0E9));
    }

    private RescueFall predictedFall() {
        double d;
        double d2 = d = this.suspendedMotion == null ? BridgeEgg.mc.thePlayer.motionY : this.suspendedMotion.yCoord;
        if (!Utils.nullCheck() || BridgeEgg.mc.currentScreen != null || BridgeEgg.mc.thePlayer.isDead || BridgeEgg.mc.thePlayer.onGround || d >= 0.0 || BridgeEgg.mc.thePlayer.isInWater() || BridgeEgg.mc.thePlayer.isInLava() || BridgeEgg.mc.thePlayer.capabilities.isFlying) {
            return null;
        }
        int n = BridgeEgg.generatedY(BridgeEgg.mc.thePlayer.posY);
        if ((double)n < this.lowestY.getInput()) {
            return null;
        }
        RescueFall rescueFall = RescueFall.predict((EntityPlayer)BridgeEgg.mc.thePlayer, (double)n + 1.0, this.suspendedMotion);
        return rescueFall.lands || rescueFall.catchPosition == null ? null : rescueFall;
    }

    private boolean updateAim() {
        RescueFall rescueFall = this.predictedFall();
        if (rescueFall == null) {
            return false;
        }
        float[] fArray = Trajectories.getThrowRotations((EntityPlayer)BridgeEgg.mc.thePlayer, Items.egg, rescueFall.catchPosition, false);
        if (fArray == null) {
            return false;
        }
        this.catchPosition = rescueFall.catchPosition;
        this.bridge = new BlockPos(MathHelper.floor_double((double)this.catchPosition.xCoord), BridgeEgg.generatedY(BridgeEgg.mc.thePlayer.posY), MathHelper.floor_double((double)this.catchPosition.zCoord));
        this.yaw = fArray[0];
        this.aimPitch = fArray[1];
        return true;
    }

    private void updateControl() {
        boolean bl;
        boolean bl2 = bl = this.attempt.state() == RescueAttempt.State.AIMING || this.attempt.slowing();
        if (!bl || !Utils.nullCheck() || this.player != BridgeEgg.mc.thePlayer || this.corrected || BridgeEgg.mc.currentScreen != null || this.player.isDead || this.player.onGround) {
            Valen.timerController.release(TimerController.Owner.BRIDGE_EGG);
            this.releaseStasis();
            return;
        }
        if (this.useStasis.isToggled()) {
            Valen.timerController.release(TimerController.Owner.BRIDGE_EGG);
            if (this.suspendedMotion == null) {
                this.suspendedMotion = new Vec3(this.player.motionX, this.player.motionY, this.player.motionZ);
                this.suspendedPosition = this.player.getPositionVector();
            }
            this.player.motionZ = 0.0;
            this.player.motionY = 0.0;
            this.player.motionX = 0.0;
        } else {
            this.releaseStasis();
            Valen.timerController.request(TimerController.Owner.BRIDGE_EGG, this.attempt.slowing() ? this.afterTimer.getInput() : this.preTimer.getInput());
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        this.refresh();
        if (this.suspendedMotion != null) {
            this.player.motionZ = 0.0;
            this.player.motionY = 0.0;
            this.player.motionX = 0.0;
            playerMoveEvent.setCanceled(true);
        }
    }

    private void releaseStasis() {
        if (this.suspendedMotion != null && this.player == BridgeEgg.mc.thePlayer && this.world == BridgeEgg.mc.theWorld && this.player != null && !this.player.isDead && !this.corrected && this.player.getPositionVector().squareDistanceTo(this.suspendedPosition) < 1.0E-6) {
            this.player.motionX = this.suspendedMotion.xCoord;
            this.player.motionY = this.suspendedMotion.yCoord;
            this.player.motionZ = this.suspendedMotion.zCoord;
        }
        this.suspendedMotion = null;
        this.suspendedPosition = null;
    }
}


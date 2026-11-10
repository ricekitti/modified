package dev.rice.module.impl.pearl;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.NativeFeatureBridge;
import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.NumberSetting;
import dev.rice.module.impl.fireball.TimerController;
import net.minecraft.client.Minecraft;

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

public final class BridgeEgg
extends Module implements RescueServices.BridgeEggQuery {
    private static final Minecraft mc=Minecraft.getMinecraft();
    private static final int EXPERIMENTAL_BRIDGE_DEPTH = 5;
    private static final long ATTEMPT_TIMEOUT_MS = 1800L;
    private final NumberSetting lowestY = new NumberSetting("lowest_y", "Lowest Y", 40.0, 0.0, 255.0, 1.0);
    private final BooleanSetting inVoidFor = new BooleanSetting("in_void_for", "In void for", false);
    private final NumberSetting voidTime = new NumberSetting("time", "Time", 0.5, 0.5, 5.0, 0.1);
    private final BooleanSetting waitForFireballPearl = new BooleanSetting("wait_for_fireball_pearl_clutch", "Wait for Fireball Pearl Clutch", true);
    private final BooleanSetting waitForClutch = new BooleanSetting("wait_for_clutch", "Wait for Clutch", true);
    private final NumberSetting preTimer = new NumberSetting("pre_timer_speed", "Pre timer speed", 1.0, 0.0, 1.0, 0.01);
    private final NumberSetting afterTimer = new NumberSetting("after_timer_speed", "After timer speed", 0.03, 0.0, 1.0, 0.01);
    private final BooleanSetting useStasis = new BooleanSetting("use_stasis_instead", "Use Stasis instead", false);
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
        super("bridge_egg_clutch","Bridge Egg Clutch","Recovered bridge egg rescue",ModuleCategory.UTILITY);
        this.add(this.lowestY);
        this.add(this.inVoidFor);
        this.add(this.voidTime);
        this.add(this.waitForClutch);
        this.add(this.waitForFireballPearl);
        this.add(this.preTimer);
        this.add(this.afterTimer);
        this.add(this.useStasis);

    }

    @Override public boolean settingVisible(String key){
        if(key.equals("time"))return inVoidFor.isEnabled();
        if(key.equals("pre_timer_speed")||key.equals("after_timer_speed"))return !useStasis.isEnabled();
        return true;
    }
    public boolean listens(int type){return type==1||type==2||type==13||type==18;}
    public void onEvent(ClientContext c,SemanticEvent e){
        if(NativeFeatureBridge.guiOpen()||NativeFeatureBridge.timerBlocked()){reset();return;}
        if(e.type==13)onRotation(e);else if(e.type==2)onPacket(e);else refresh();
    }
    public void postMotion(){if(isEnabled()&&!NativeFeatureBridge.guiOpen())onPostMotion();}
    public float[] motionRotation(){if(!isEnabled()||NativeFeatureBridge.guiOpen()||NativeFeatureBridge.timerBlocked())return null;refresh();return attempt.state()==RescueAttempt.State.AIMING&&eligible()?new float[]{yaw,aimPitch}:null;}
    public void onWorldUnload(ClientContext c){reset();}
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
        if (!RescueServices.ready()) return false;
        if (BridgeEgg.mc.playerController == null) return false;
        if (BridgeEgg.mc.currentScreen != null) return false;
        if (BridgeEgg.mc.thePlayer.isDead) return false;
        if (BridgeEgg.mc.thePlayer.onGround) return false;
        double d = this.suspendedMotion == null ? BridgeEgg.mc.thePlayer.motionY : this.suspendedMotion.yCoord;
        if (!(d < 0.0)) return false;
        if (BridgeEgg.mc.thePlayer.isInWater()) return false;
        if (BridgeEgg.mc.thePlayer.isInLava()) return false;
        if (BridgeEgg.mc.thePlayer.capabilities.isFlying) return false;
        if (RescueServices.bedMining()) return false;
        if(RescueServices.pearlActive(false)||RescueServices.pearlActive(true)||waitForFireballPearl.isEnabled()&&RescueServices.fireballPearlHasPriority())return false;
        if (!this.waitForClutch.isEnabled()) return true;
        if (RescueServices.clutchCanSave()) return false;
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
        if(waitForFireballPearl.isEnabled()&&RescueServices.fireballPearlHasPriority())return false;
        if (this.attempt.active()) {
            return true;
        }
        if (this.attempt.state() != RescueAttempt.State.IDLE || !this.eligible() || !this.voidReady() || this.findEgg() < 0) {
            return false;
        }
        RescueFall rescueFall = this.predictedFall();
        return rescueFall != null && TrajectoryAim.getThrowRotations((EntityPlayer)BridgeEgg.mc.thePlayer, Items.egg, rescueFall.catchPosition, false) != null;
    }

    @Override
    protected void onEnable(ClientContext c) {
        this.reset();
    }

    @Override
    protected void onDisable(ClientContext c) {
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
        RescueServices.timer().release(TimerController.Owner.BRIDGE_EGG);
    }

    public void onRotation(SemanticEvent clientRotationEvent) {
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
            clientRotationEvent.yaw=this.yaw;
            clientRotationEvent.pitch=this.aimPitch;
            clientRotationEvent.remapMovement=true;clientRotationEvent.overrideRotation=true;
        }
        this.updateControl();
    }

    public void onPostMotion() {
        this.refresh();
        if (this.attempt.state() != RescueAttempt.State.AIMING) {
            return;
        }
        if (!this.eligible() || !BridgeEgg.isBridgeEgg(this.player.inventory.mainInventory[this.slot])) {
            this.attempt.fail();
            return;
        }
        if ((double)Math.abs(RescueServices.serverRotations[1] - this.aimPitch) > 0.01 || (double)Math.abs(MathHelper.wrapAngleTo180_float((float)(RescueServices.serverRotations[0] - this.yaw))) > 0.01) {
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

    public void onRender() {
        this.refresh();
    }

    public void onTick() {
        this.refresh();
    }

    public void onPacket(SemanticEvent receivePacketEvent) {
        if (receivePacketEvent.packet instanceof S08PacketPlayerPosLook) {
            this.corrected = true;
        } else if (receivePacketEvent.packet instanceof S23PacketBlockChange) {
            S23PacketBlockChange s23PacketBlockChange = (S23PacketBlockChange)receivePacketEvent.packet;
            this.updates.add(new SupportUpdate(s23PacketBlockChange.getBlockPosition(), s23PacketBlockChange.getBlockState()));
        } else if (receivePacketEvent.packet instanceof S22PacketMultiBlockChange) {
            for (S22PacketMultiBlockChange.BlockUpdateData blockUpdateData : ((S22PacketMultiBlockChange)receivePacketEvent.packet).getChangedBlocks()) {
                this.updates.add(new SupportUpdate(blockUpdateData.getPos(), blockUpdateData.getBlockState()));
            }
        }
    }

    private void refresh() {
        SupportUpdate supportUpdate;
        if (this.player != BridgeEgg.mc.thePlayer || this.world != BridgeEgg.mc.theWorld || !RescueServices.ready()) {
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
        boolean bl2 = this.suspendedPosition != null && this.player.getPositionVector().squareDistanceTo(this.suspendedPosition) > 1.0E-6 || this.corrected || BridgeEgg.mc.currentScreen != null || this.player.isDead || RescueServices.bedMining() || RescueServices.pearlActive(true) || RescueServices.pearlActive(false);
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
        if (!this.inVoidFor.isEnabled()) {
            this.voidDuration.reset();
            return true;
        }
        boolean bl2 = bl = RescueServices.ready() && BridgeEgg.mc.currentScreen == null && !BridgeEgg.mc.thePlayer.isDead && !BridgeEgg.mc.thePlayer.onGround;
        if (bl) {
            for (double d : new double[]{-0.3, 0.3}) {
                for (double d2 : new double[]{-0.3, 0.3}) {
                    bl &= SupportScan.isUnsupported((World)BridgeEgg.mc.theWorld, (double)(BridgeEgg.mc.thePlayer.posX + d), (double)BridgeEgg.mc.thePlayer.posY, (double)(BridgeEgg.mc.thePlayer.posZ + d2));
                }
            }
        }
        return this.voidDuration.update(bl, System.nanoTime(), (long)(this.voidTime.doubleValue() * 1.0E9));
    }

    private RescueFall predictedFall() {
        double d;
        double d2 = d = this.suspendedMotion == null ? BridgeEgg.mc.thePlayer.motionY : this.suspendedMotion.yCoord;
        if (!RescueServices.ready() || BridgeEgg.mc.currentScreen != null || BridgeEgg.mc.thePlayer.isDead || BridgeEgg.mc.thePlayer.onGround || d >= 0.0 || BridgeEgg.mc.thePlayer.isInWater() || BridgeEgg.mc.thePlayer.isInLava() || BridgeEgg.mc.thePlayer.capabilities.isFlying) {
            return null;
        }
        int n = BridgeEgg.generatedY(BridgeEgg.mc.thePlayer.posY);
        if ((double)n < this.lowestY.doubleValue()) {
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
        float[] fArray = TrajectoryAim.getThrowRotations((EntityPlayer)BridgeEgg.mc.thePlayer, Items.egg, rescueFall.catchPosition, false);
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
        if (!bl || !RescueServices.ready() || this.player != BridgeEgg.mc.thePlayer || this.corrected || BridgeEgg.mc.currentScreen != null || this.player.isDead || this.player.onGround) {
            RescueServices.timer().release(TimerController.Owner.BRIDGE_EGG);
            this.releaseStasis();
            return;
        }
        if (this.useStasis.isEnabled()) {
            RescueServices.timer().release(TimerController.Owner.BRIDGE_EGG);
            if (this.suspendedMotion == null) {
                this.suspendedMotion = new Vec3(this.player.motionX, this.player.motionY, this.player.motionZ);
                this.suspendedPosition = this.player.getPositionVector();
            }
            this.player.motionZ = 0.0;
            this.player.motionY = 0.0;
            this.player.motionX = 0.0;
        } else {
            this.releaseStasis();
            RescueServices.timer().request(TimerController.Owner.BRIDGE_EGG, this.attempt.slowing() ? this.afterTimer.doubleValue() : this.preTimer.doubleValue());
        }
    }

    public boolean cancelMove() {
        this.refresh();
        if (this.suspendedMotion != null) {
            this.player.motionZ = 0.0;
            this.player.motionY = 0.0;
            this.player.motionX = 0.0;
            return true;
        }
        return false;
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
    private static final class SupportUpdate {
        final BlockPos pos;final net.minecraft.block.state.IBlockState state;
        SupportUpdate(BlockPos p,net.minecraft.block.state.IBlockState s){pos=p;state=s;}
    }
}

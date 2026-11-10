package dev.rice.module.impl.pearl;
import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.MinecraftFields;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.NativeFeatureBridge;
import dev.rice.module.impl.fireball.TimerController;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.NumberSetting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class PearlRescueModule
extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final boolean fireballMode;
    private final TimerController.Owner timerOwner;
    private static final double GRAVITY = 0.03;
    private static final double DRAG = 0.99;
    private static final double THROW_VELOCITY = 1.5;
    private static final double SPAWN_Y_DROP = (double)0.1f;
    private static final double SPAWN_XZ_OFFSET = 0.16;
    private static final int MAX_SIM_TICKS = 160;
    private static final int SEARCH_RADIUS = 6;
    private static final int SEARCH_DOWN = 5;
    private static final int SEARCH_UP = 12;
    private static final int SWEEP_RADIUS = 20;
    private static final int SWEEP_STEP = 2;
    private static final int SWEEP_DOWN = 8;
    private static final int SWEEP_UP = 16;
    private static final double MIN_TARGET_DISTANCE = 2.0;
    private static final float COARSE_STEP = 1.0f;
    private static final float FINE_SPAN = 1.5f;
    private static final float FINE_STEP = 0.05f;
    private static final int VERIFY_CANDIDATES = 4;
    private static final int YAW_FIXPOINT_STEPS = 4;
    private static final long TICK_MS = 50L;
    private static final long LANDING_MARGIN_MS = 100L;
    private static final int MAX_CANDIDATES = 24;
    private static final int SOLVE_ATTEMPTS = 5;
    private static final int EDGE_SCAN_MAX = 4;
    private static final int RECENT_DAMAGE_TICKS = 20;
    private static final double FLAT_RANGE = 53.0;
    private static final double CLIMB_COST = 1.05;
    private static final double CENTRALITY_WEIGHT = 6.0;
    private static final double ENEMY_WEIGHT = 4.0;
    private static final double HALF_WIDTH = 0.3;
    private static final double[][] CORNERS = new double[][]{{-0.3, -0.3}, {0.3, -0.3}, {-0.3, 0.3}, {0.3, 0.3}};
    private static final int EDGE_SEARCH_RADIUS = 12;
    private static final int EDGE_SEARCH_DOWN = 8;
    private static final int EDGE_SEARCH_UP = 24;
    private static final double PREDICTIVE_DROP = 10.0;
    private final NumberSetting range;
    private final NumberSetting avoidEnemies;
    private final NumberSetting edgeMargin;
    private final NumberSetting prePearlTimer;
    private final NumberSetting afterPearlTimer;
    private final BooleanSetting swapBack;
    private final BooleanSetting disableAfterwards;
    private final BooleanSetting requireDamaged;
    private final BooleanSetting requireAboveVoid;
    private final BooleanSetting predictSafeLanding;
    private final BooleanSetting inVoidFor;
    private final NumberSetting voidTime;
    private final BooleanSetting waitForClutch;
    private final BooleanSetting waitForBridgeEgg;
    private final PearlFireballSequence comboSequence = new PearlFireballSequence();
    private static final long COMBO_FLIGHT_TIMEOUT_MS = 3500L;
    private int comboPhase;
    private int fireballSlot = -1;
    private long comboDeadline;
    private boolean comboAttempted;
    private EntityPlayer activePlayer;
    private final Map<Long, Boolean> solidCache = new HashMap<Long, Boolean>();
    private BlockPos targetBlock;
    private float aimYaw;
    private float aimPitch;
    private boolean hasSolution;
    private int pearlSlot = -1;
    private int previousSlot = -1;
    private int simFlightTicks;
    private boolean timering;
    private long timerDeadlineMs;
    private volatile boolean landed;
    private int prevHurtTime = -1;
    private int lastDamageTick = Integer.MIN_VALUE;
    private boolean solving;
    private boolean manualTimerPre;
    private final ContinuousCondition voidDuration = new ContinuousCondition();
    private boolean voidReady;
    private boolean damageDuringVoid;
    private double predictiveEdgeTopY = Double.NaN;
    private Object activeWorld;
    private boolean forced;

    protected PearlRescueModule(String string, boolean bl) {
        super(bl ? "fireball_pearl_clutch" : "auto_pearl", string, "Recovered Valen pearl rescue", ModuleCategory.UTILITY);
        this.fireballMode = bl;
        this.timerOwner = bl ? TimerController.Owner.FIREBALL_PEARL_CLUTCH : TimerController.Owner.AUTO_PEARL;
        this.range = new NumberSetting("range", "Range", 40.0, 8.0, 64.0, 1.0);
        this.add(this.range);
        this.avoidEnemies = new NumberSetting("avoid_enemies", "Avoid enemies", 10.0, 0.0, 32.0, 1.0);
        this.add(this.avoidEnemies);
        this.edgeMargin = new NumberSetting("edge_margin", "Edge margin", 2.0, 0.0, 4.0, 1.0);
        this.add(this.edgeMargin);
        this.prePearlTimer = new NumberSetting("pre_pearl_timer", "Pre pearl timer", 0.02, 0.0, 1.0, 0.01);
        if (!bl) {
            this.add(this.prePearlTimer);
        }
        this.afterPearlTimer = new NumberSetting("after_pearl_timer", "After pearl timer", 0.1, 0.0, 1.0, 0.01);
        this.add(this.afterPearlTimer);
        this.swapBack = new BooleanSetting("swap_back", "Swap back", true);
        if (!bl) {
            this.add(this.swapBack);
        }
        this.disableAfterwards = new BooleanSetting("disable_afterwards", "Disable afterwards", true);
        this.add(this.disableAfterwards);
        this.requireDamaged = new BooleanSetting("damaged", "Damaged", false);
        this.add(this.requireDamaged);
        this.requireAboveVoid = new BooleanSetting("above_void", "Above void", false);
        this.add(this.requireAboveVoid);
        this.predictSafeLanding = new BooleanSetting("predict_safe_landing", "Predict safe landing", false);
        this.add(this.predictSafeLanding);
        this.inVoidFor = new BooleanSetting("in_void_for", "In void for", false);
        this.add(this.inVoidFor);

        this.voidTime = new NumberSetting("time", "Time", 1.0, 0.5, 10.0, 0.1);
        this.add(this.voidTime);

        this.waitForClutch = new BooleanSetting("wait_for_clutch", "Wait for Clutch", true);
        this.add(this.waitForClutch);
        this.waitForBridgeEgg = new BooleanSetting("wait_for_bridge_egg", "Wait for Bridge Egg", true);
        this.add(this.waitForBridgeEgg);
    }

    @Override public boolean settingVisible(String key) {
        if (key.equals("pre_pearl_timer") || key.equals("swap_back")) return !fireballMode;
        if (key.equals("in_void_for")) return requireAboveVoid.isEnabled() && !predictSafeLanding.isEnabled();
        if (key.equals("time")) return requireAboveVoid.isEnabled() && !predictSafeLanding.isEnabled() && inVoidFor.isEnabled();
        return true;
    }
    public boolean listens(int type) { return type==1 || type==2 || type==13; }
    public void onEvent(ClientContext context, SemanticEvent e) {
        if (typePaused()) { abortRescue(); return; }
        if (e.type==1) onRenderTick();
        else if(e.type==2) onReceivePacket(e);
        else if(e.type==13) onClientRotation(e);
    }
    private boolean typePaused() { return NativeFeatureBridge.guiOpen() || NativeFeatureBridge.timerBlocked(); }
    public void postMotion() { if(isEnabled() && !typePaused()) onPostMotion(); }
    public float[] motionRotation() {
        if(!isEnabled()||typePaused()||!RescueServices.ready()||activePlayer!=mc.thePlayer||activeWorld!=mc.theWorld||mc.currentScreen!=null||mc.thePlayer.isDead)return null;
        if(fireballMode&&(comboPhase==1||comboPhase==2))return conditionsMet()&&earlierRescuesYielded()?new float[]{aimYaw,aimPitch}:null;
        return !fireballMode&&!timering&&hasSolution&&conditionsMet()?new float[]{aimYaw,aimPitch}:null;
    }
    public void onWorldUnload(ClientContext context) { abortRescue(); resetSession(); }
    private void disable() { NativeFeatureBridge.disableFeature(this); }
    @Override
    protected void onEnable(ClientContext context) {
        this.resetSession();
    }

    @Override
    protected void onDisable(ClientContext context) {
        this.abortRescue();
        this.resetSession();
    }

    private void resetSession() {
        this.clearSolution();
        this.previousSlot = -1;
        this.timering = false;
        this.landed = false;
        this.solving = false;
        this.prevHurtTime = -1;
        this.lastDamageTick = Integer.MIN_VALUE;
        this.manualTimerPre = false;
        this.activeWorld = null;
        this.activePlayer = null;
        this.comboPhase = 0;
        this.comboSequence.reset();
        this.comboAttempted = false;
        this.fireballSlot = -1;
        this.resetVoidDuration();
        RescueServices.timer().release(this.timerOwner);
    }

    private void resetVoidDuration() {
        this.voidDuration.reset();
        this.voidReady = false;
        this.damageDuringVoid = false;
        this.predictiveEdgeTopY = Double.NaN;
    }

    public void onRenderTick() {
        if (!this.isEnabled()) {
            return;
        }
        if (!RescueServices.ready() || PearlRescueModule.mc.thePlayer.isDead) {
            this.abortRescue();
            this.resetSession();
            return;
        }
        if (this.activeWorld != PearlRescueModule.mc.theWorld || this.activePlayer != PearlRescueModule.mc.thePlayer) {
            this.resetSession();
            this.activeWorld = PearlRescueModule.mc.theWorld;
            this.activePlayer = PearlRescueModule.mc.thePlayer;
        }
        if (PearlRescueModule.mc.currentScreen != null || RescueServices.bedMining()) {
            this.abortRescue();
            return;
        }
        if (PearlRescueModule.mc.thePlayer.onGround) {
            this.comboAttempted = false;
        }
        if (this.comboPhase > 0 && (this.landed || this.comboPhase < 3 && PearlRescueModule.mc.thePlayer.onGround || System.currentTimeMillis() >= this.comboDeadline)) {
            boolean bl = this.comboPhase == 3 && this.landed;
            this.abortRescue();
            if (bl && this.disableAfterwards.isEnabled()) {
                this.disable();
            }
            return;
        }
        if (this.timering && (this.landed || System.currentTimeMillis() >= this.timerDeadlineMs)) {
            boolean completed = this.landed;
            this.stopTimering();
            if (completed && this.disableAfterwards.isEnabled()) {
                this.disable();
            }
            return;
        }
        if (this.timering) {
            RescueServices.timer().request(this.timerOwner, this.comboPhase == 3 ? 0.03 : this.afterPearlTimer.doubleValue());
        } else if (!this.fireballMode && this.solving && this.comboPhase == 0 && PearlRescueModule.mc.currentScreen == null) {
            if (this.manualTimerPre || this.manualTimerActive()) {
                RescueServices.timer().release(this.timerOwner);
            } else {
                RescueServices.timer().request(this.timerOwner, this.prePearlTimer.doubleValue());
            }
        } else {
            RescueServices.timer().release(this.timerOwner);
            if (PearlRescueModule.mc.currentScreen != null) {
                this.solving = false;
                this.manualTimerPre = false;
                this.clearSolution();
                this.resetVoidDuration();
            }
        }
    }

    public void onReceivePacket(SemanticEvent receivePacketEvent) {
        if ((this.timering || this.comboPhase > 0) && receivePacketEvent.packet instanceof S08PacketPlayerPosLook) {
            this.landed = true;
        }
    }

    private void startTimering() {
        this.landed = false;
        this.timering = true;
        this.timerDeadlineMs = System.currentTimeMillis() + Math.max(10000L, (long)this.simFlightTicks * 50L + this.pingMs() + 1000L);
        RescueServices.timer().request(this.timerOwner, this.afterPearlTimer.doubleValue());
    }

    private void stopTimering() {
        if (!this.timering) {
            return;
        }
        this.timering = false;
        this.landed = false;
        this.resetVoidDuration();
        RescueServices.timer().release(this.timerOwner);
    }

    private long pingMs() {
        if (mc.getNetHandler() == null || PearlRescueModule.mc.thePlayer == null) {
            return 0L;
        }
        NetworkPlayerInfo networkPlayerInfo = mc.getNetHandler().getPlayerInfo(PearlRescueModule.mc.thePlayer.getUniqueID());
        return networkPlayerInfo == null ? 0L : (long)Math.max(0, Math.min(1000, networkPlayerInfo.getResponseTime()));
    }

    private void clearSolution() {
        this.hasSolution = false;
        this.targetBlock = null;
        this.pearlSlot = -1;
        this.forced = false;
    }

    public void onClientRotation(SemanticEvent clientRotationEvent) {
        if (this.timering) {
            return;
        }
        if (!RescueServices.ready() || PearlRescueModule.mc.currentScreen != null || PearlRescueModule.mc.thePlayer.isDead || RescueServices.bedMining()) {
            this.abortRescue();
            this.solving = false;
            this.manualTimerPre = false;
            this.clearSolution();
            this.resetVoidDuration();
            RescueServices.timer().release(this.timerOwner);
            return;
        }
        if (this.activeWorld != PearlRescueModule.mc.theWorld || this.activePlayer != PearlRescueModule.mc.thePlayer) {
            this.resetSession();
            this.activeWorld = PearlRescueModule.mc.theWorld;
            this.activePlayer = PearlRescueModule.mc.thePlayer;
        }
        if (this.comboPhase > 0) {
            if (this.landed || PearlRescueModule.mc.thePlayer.onGround || System.currentTimeMillis() >= this.comboDeadline || !this.fireballMode || !this.conditionsMet() || !this.earlierRescuesYielded()) {
                this.abortRescue();
                return;
            }
            clientRotationEvent.remapMovement = true; clientRotationEvent.overrideRotation = true;
            clientRotationEvent.yaw = this.aimYaw;
            clientRotationEvent.pitch = this.aimPitch;
            return;
        }
        this.refreshTriggerConditions();
        if (this.pearlSlot == -1 || !this.conditionsMet()) {
            this.solving = false;
            this.manualTimerPre = false;
            this.clearSolution();
            RescueServices.timer().release(this.timerOwner);
            return;
        }
        if (!this.solving) {
            this.manualTimerPre = this.manualTimerActive();
        }
        this.solving = true;
        this.solve();
        if (this.fireballMode ? this.hasSolution || !this.beginCombo() : !this.hasSolution) {
            return;
        }
        clientRotationEvent.remapMovement = true; clientRotationEvent.overrideRotation = true;
        clientRotationEvent.yaw = this.aimYaw;
        clientRotationEvent.pitch = this.aimPitch;
    }

    private void trackDamage() {
        int n = PearlRescueModule.mc.thePlayer.hurtTime;
        if (n > 0 && n > this.prevHurtTime) {
            this.lastDamageTick = PearlRescueModule.mc.thePlayer.ticksExisted;
        }
        this.prevHurtTime = n;
    }

    private boolean hasRecentDamage() {
        return this.lastDamageTick != Integer.MIN_VALUE && PearlRescueModule.mc.thePlayer.ticksExisted - this.lastDamageTick <= 20;
    }

    private boolean manualTimerActive() {
        if(NativeFeatureBridge.timerBlocked()) return true;
        if(RescueServices.timer().isRequested(TimerController.Owner.TIMER)) return true;
        if(RescueServices.timer().hasRequests() || !RescueServices.ready()) return false;
        return Math.abs(MinecraftFields.timer(mc).timerSpeed - 1f) > .001f;
    }

    private boolean conditionsMet() { return conditionsMet(true); }
    private boolean conditionsMet(boolean checkBridgeEgg) {
        if (this.fireballMode && RescueServices.pearlActive(false) || !this.fireballMode && RescueServices.pearlActive(true)) {
            return false;
        }
        if (!(!this.requireDamaged.isEnabled() || this.hasRecentDamage() || this.requireAboveVoid.isEnabled() && this.damageDuringVoid)) {
            return false;
        }
        if (this.predictSafeLanding.isEnabled() && !this.voidReady) {
            return false;
        }
        if (!this.predictSafeLanding.isEnabled() && this.requireAboveVoid.isEnabled() && !this.voidReady) {
            return false;
        }
        if (this.waitForClutch.isEnabled() && RescueServices.clutchCanSave()) {
            return false;
        }
        return !checkBridgeEgg || !this.waitForBridgeEgg.isEnabled() || !RescueServices.bridgeEggCanSave();
    }

    private boolean predictiveRescueReady() {
        if (!RescueServices.ready() || PearlRescueModule.mc.currentScreen != null || PearlRescueModule.mc.thePlayer.isDead || PearlRescueModule.mc.thePlayer.onGround || PearlRescueModule.mc.thePlayer.motionY >= 0.0 || PearlRescueModule.mc.thePlayer.isInWater() || PearlRescueModule.mc.thePlayer.isInLava() || PearlRescueModule.mc.thePlayer.isOnLadder() || PearlRescueModule.mc.thePlayer.capabilities.isFlying) {
            this.predictiveEdgeTopY = Double.NaN;
            return false;
        }
        if (!this.aboveVoid()) {
            this.predictiveEdgeTopY = Double.NaN;
            return false;
        }
        RescueFall rescueFall = RescueFall.predict((EntityPlayer)PearlRescueModule.mc.thePlayer, -8.0);
        if (rescueFall.lands) {
            return false;
        }
        if (RescueServices.clutchCanSave()) {
            return false;
        }
        if (!Double.isFinite(this.predictiveEdgeTopY)) {
            double d = this.closestEdgeTopY();
            this.predictiveEdgeTopY = Double.isFinite(d) ? d : Math.ceil(PearlRescueModule.mc.thePlayer.posY);
        }
        return PearlRescueModule.isBelowClosestEdge(PearlRescueModule.mc.thePlayer.posY, this.predictiveEdgeTopY);
    }

    private double closestEdgeTopY() {
        int n;
        int n2 = MathHelper.floor_double((double)PearlRescueModule.mc.thePlayer.posX);
        int n3 = MathHelper.floor_double((double)PearlRescueModule.mc.thePlayer.posY);
        int n4 = MathHelper.floor_double((double)PearlRescueModule.mc.thePlayer.posZ);
        double d = Double.MAX_VALUE;
        double d2 = Double.MAX_VALUE;
        double d3 = Double.NaN;
        int n5 = Math.max(0, n3 - 8);
        for (int i = n = Math.min(255, n3 + 24); i >= n5; --i) {
            double d4 = (double)i + 1.0;
            if (d4 < PearlRescueModule.mc.thePlayer.posY - 0.25) continue;
            for (int j = n2 - 12; j <= n2 + 12; ++j) {
                for (int k = n4 - 12; k <= n4 + 12; ++k) {
                    BlockPos blockPos = new BlockPos(j, i, k);
                    if (!this.isSolid(blockPos) || this.isSolid(blockPos.up())) continue;
                    boolean bl = false;
                    for (EnumFacing enumFacing : new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST}) {
                        if (this.isSolid(blockPos.offset(enumFacing))) continue;
                        bl = true;
                        break;
                    }
                    if (!bl) continue;
                    double d5 = (double)j + 0.5 - PearlRescueModule.mc.thePlayer.posX;
                    double d6 = (double)k + 0.5 - PearlRescueModule.mc.thePlayer.posZ;
                    double d7 = d5 * d5 + d6 * d6;
                    double d8 = Math.abs(d4 - PearlRescueModule.mc.thePlayer.posY);
                    if (!(d7 < d - 1.0E-6) && (!(Math.abs(d7 - d) <= 1.0E-6) || !(d8 < d2))) continue;
                    d = d7;
                    d2 = d8;
                    d3 = d4;
                }
            }
        }
        return d3;
    }

    public static boolean isBelowClosestEdge(double d, double d2) {
        return Double.isFinite(d2) && d <= d2 - 10.0;
    }

    private boolean aboveVoid() {
        int n = MathHelper.floor_double((double)PearlRescueModule.mc.thePlayer.posY) - 1;
        long[] lArray = new long[CORNERS.length];
        int n2 = 0;
        for (double[] dArray : CORNERS) {
            int n3;
            int n4 = MathHelper.floor_double((double)(PearlRescueModule.mc.thePlayer.posX + dArray[0]));
            int n5 = MathHelper.floor_double((double)(PearlRescueModule.mc.thePlayer.posZ + dArray[1]));
            long l = (long)n4 << 32 ^ (long)n5 & 0xFFFFFFFFL;
            boolean bl = false;
            for (n3 = 0; n3 < n2; ++n3) {
                if (lArray[n3] != l) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            lArray[n2++] = l;
            for (n3 = n; n3 >= 0; --n3) {
                if (!this.isSolid(new BlockPos(n4, n3, n5))) continue;
                return false;
            }
        }
        return true;
    }

    public void onPostMotion() {
        if (this.comboPhase == 1 || this.comboPhase == 2) {
            this.throwCombo();
            return;
        }
        if (this.fireballMode || this.timering || !this.hasSolution || !RescueServices.ready() || PearlRescueModule.mc.currentScreen != null) {
            return;
        }
        if (PearlRescueModule.mc.thePlayer.isDead || RescueServices.bedMining() || !this.conditionsMet() || this.predictSafeLanding.isEnabled() && !this.predictiveRescueReady() || this.pearlSlot < 0 || !PearlRescueModule.isPearl(PearlRescueModule.mc.thePlayer.inventory.mainInventory[this.pearlSlot])) {
            this.clearSolution();
            return;
        }
        float f = RescueServices.serverRotations[0];
        float f2 = RescueServices.serverRotations[1];
        if (!this.landsOnTarget(this.simulate(f, f2, true), this.targetBlock)) {
            return;
        }
        this.throwPearl();
    }

    public boolean isRescueActive() {
        return this.isEnabled() && (this.timering || this.comboPhase > 0);
    }

    private boolean earlierRescuesYielded() {
        return !(RescueServices.clutchCanSave() || RescueServices.bridgeEggCanSave());
    }

    private int findFireballSlot() {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = PearlRescueModule.mc.thePlayer.inventory.mainInventory[i];
            if (itemStack == null || itemStack.stackSize <= 0 || itemStack.getItem() != Items.fire_charge) continue;
            return i;
        }
        return -1;
    }

    private boolean beginCombo() {
        if (!this.fireballMode || this.comboAttempted || PearlRescueModule.mc.thePlayer.onGround || PearlRescueModule.mc.thePlayer.motionY >= 0.0 || PearlRescueModule.mc.thePlayer.isInWater() || PearlRescueModule.mc.thePlayer.isInLava() || PearlRescueModule.mc.thePlayer.capabilities.isFlying || !this.earlierRescuesYielded() || RescueFall.predict((EntityPlayer)PearlRescueModule.mc.thePlayer, (double)(PearlRescueModule.mc.thePlayer.posY - 5.0)).lands) {
            return false;
        }
        this.fireballSlot = this.findFireballSlot();
        if (this.pearlSlot < 0 || this.fireballSlot < 0) {
            return false;
        }
        this.comboAttempted = true;
        this.comboPhase = 1;
        this.previousSlot = PearlRescueModule.mc.thePlayer.inventory.currentItem;
        this.aimYaw = RescueServices.serverRotations[0];
        this.aimPitch = -90.0f;
        this.comboDeadline = System.currentTimeMillis() + 1500L;
        this.comboSequence.begin(this.pearlSlot, this.fireballSlot, this.aimYaw, System.currentTimeMillis());
        this.solving = false;
        RescueServices.timer().release(this.timerOwner);
        return true;
    }

    private void throwCombo() {
        ItemStack itemStack;
        if (!RescueServices.ready() || PearlRescueModule.mc.thePlayer != this.activePlayer || PearlRescueModule.mc.theWorld != this.activeWorld || PearlRescueModule.mc.currentScreen != null || PearlRescueModule.mc.thePlayer.isDead || PearlRescueModule.mc.thePlayer.onGround || RescueServices.bedMining() || this.landed || !this.fireballMode || !this.conditionsMet() || !this.earlierRescuesYielded() || System.currentTimeMillis() >= this.comboDeadline) {
            this.abortRescue();
            return;
        }
        if ((double)Math.abs(MathHelper.wrapAngleTo180_float((float)(RescueServices.serverRotations[0] - this.aimYaw))) > 0.01 || (double)Math.abs(RescueServices.serverRotations[1] - this.aimPitch) > 0.01) {
            return;
        }
        ItemStack itemStack2 = itemStack = this.fireballSlot < 0 ? null : PearlRescueModule.mc.thePlayer.inventory.mainInventory[this.fireballSlot];
        if (itemStack == null || itemStack.stackSize <= 0 || itemStack.getItem() != Items.fire_charge) {
            this.abortRescue();
            return;
        }
        if (!(this.comboPhase != 1 || this.pearlSlot >= 0 && PearlRescueModule.isPearl(PearlRescueModule.mc.thePlayer.inventory.mainInventory[this.pearlSlot]))) {
            this.abortRescue();
            return;
        }
        boolean bl = this.comboSequence.send(PearlRescueModule.mc.thePlayer.ticksExisted, System.currentTimeMillis(), RescueServices.serverRotations[0], RescueServices.serverRotations[1], true, RescueItemUse::use);
        if (this.comboSequence.state() == PearlFireballSequence.State.FAILED) {
            this.abortRescue();
            return;
        }
        if (this.comboSequence.state() == PearlFireballSequence.State.FIREBALL) {
            this.comboPhase = 2;
            if (this.manualTimerPre || this.manualTimerActive()) {
                this.stopManualTimer();
                RescueServices.timer().request(this.timerOwner, this.afterPearlTimer.doubleValue());
                this.manualTimerPre = false;
            }
        }
        if (bl) {
            RescueServices.recordLaunch(this.aimYaw, this.aimPitch);
            RescueItemUse.restore(this.previousSlot);
            this.previousSlot = -1;
            this.clearSolution();
            this.comboPhase = 3;
            this.timering = true;
            this.landed = false;
            this.timerDeadlineMs = this.comboDeadline = System.currentTimeMillis() + 12000L + this.pingMs();
            RescueServices.timer().request(this.timerOwner, 0.03);
        }
    }

    private void abortRescue() {
        if (this.comboPhase > 0 && this.activePlayer == PearlRescueModule.mc.thePlayer) {
            RescueItemUse.restore(this.previousSlot);
            this.previousSlot = -1;
        } else {
            this.restoreSlot();
        }
        this.comboPhase = 0;
        this.comboSequence.reset();
        this.timering = false;
        this.landed = false;
        this.solving = false;
        this.manualTimerPre = false;
        this.clearSolution();
        this.resetVoidDuration();
        RescueServices.timer().release(this.timerOwner);
    }

    private void throwPearl() {
        this.previousSlot = PearlRescueModule.mc.thePlayer.inventory.currentItem;
        boolean bl = RescueItemUse.use(this.pearlSlot);
        this.restoreSlot();
        this.clearSolution();
        this.solving = false;
        this.manualTimerPre = false;
        if (bl) {
            this.stopManualTimer();
            this.startTimering();
        } else {
            RescueServices.timer().release(this.timerOwner);
        }
    }

    private void stopManualTimer() {
        RescueServices.timer().release(TimerController.Owner.TIMER);
    }

    private void restoreSlot() {
        if (!this.swapBack.isEnabled() || this.previousSlot < 0 || PearlRescueModule.mc.thePlayer == null) {
            this.previousSlot = -1;
            return;
        }
        if (this.activePlayer == PearlRescueModule.mc.thePlayer) {
            RescueItemUse.restore(this.previousSlot);
        }
        this.previousSlot = -1;
    }

    private int findPearlSlot() {
        int n = PearlRescueModule.mc.thePlayer.inventory.currentItem;
        if (PearlRescueModule.isPearl(PearlRescueModule.mc.thePlayer.inventory.mainInventory[n])) {
            return n;
        }
        for (int i = 0; i <= 8; ++i) {
            if (!PearlRescueModule.isPearl(PearlRescueModule.mc.thePlayer.inventory.mainInventory[i])) continue;
            return i;
        }
        return -1;
    }

    private static boolean isPearl(ItemStack itemStack) {
        return itemStack != null && itemStack.stackSize > 0 && itemStack.getItem() == Items.ender_pearl;
    }

    private void solve() {
        this.hasSolution = false;
        this.targetBlock = null;
        this.forced = false;
        List<Candidate> list = this.gatherCandidates();
        if (list.isEmpty()) {
            return;
        }
        if (this.attemptSolve(list, true)) {
            return;
        }
        this.attemptSolve(list, false);
    }

    private boolean attemptSolve(List<Candidate> list, boolean bl) {
        int n = 0;
        for (Candidate candidate : list) {
            if (candidate.tried || bl && !candidate.safe) continue;
            if (n >= 5) break;
            ++n;
            candidate.tried = true;
            if (!this.solveFor(candidate.pos)) continue;
            this.targetBlock = candidate.pos;
            this.hasSolution = true;
            this.forced = !candidate.safe;
            return true;
        }
        return false;
    }

    private List<Candidate> gatherCandidates() {
        Vec3 vec3 = PearlRescueModule.mc.thePlayer.getPositionEyes(1.0f);
        Vec3 vec32 = PearlRescueModule.mc.thePlayer.getLook(1.0f);
        double d = this.range.doubleValue();
        Vec3 vec33 = vec3.addVector(vec32.xCoord * d, vec32.yCoord * d, vec32.zCoord * d);
        MovingObjectPosition movingObjectPosition = PearlRescueModule.mc.theWorld.rayTraceBlocks(vec3, vec33, false, true, false);
        Vec3 vec34 = movingObjectPosition != null && movingObjectPosition.hitVec != null ? movingObjectPosition.hitVec : vec33;
        List<Candidate> list = this.collectColumns(vec34, 6, 1, 5, 12, vec3, vec34, d);
        if (list.isEmpty()) {
            Vec3 vec35 = new Vec3(PearlRescueModule.mc.thePlayer.posX, PearlRescueModule.mc.thePlayer.posY, PearlRescueModule.mc.thePlayer.posZ);
            list = this.collectColumns(vec35, 20, 2, 8, 16, vec3, vec34, d);
        }
        if (list.isEmpty()) {
            return list;
        }
        Collections.sort(list, new Comparator<Candidate>(){

            @Override
            public int compare(Candidate candidate, Candidate candidate2) {
                return Double.compare(candidate.focusDistance, candidate2.focusDistance);
            }
        });
        if (list.size() > 24) {
            list = new ArrayList<Candidate>(list.subList(0, 24));
        }
        double d2 = this.avoidEnemies.doubleValue();
        int n = (int)this.edgeMargin.doubleValue();
        for (Candidate candidate : list) {
            int n2 = this.edgeDistance(candidate.pos);
            double d3 = this.enemyDistance(candidate.pos);
            double d4 = d2 > 0.0 && d3 < d2 ? (d2 - d3) * 4.0 : 0.0;
            candidate.safe = n2 >= n && (d2 <= 0.0 || d3 >= d2);
            candidate.score = candidate.focusDistance + (double)(4 - n2) * 6.0 + d4;
        }
        Collections.sort(list, new Comparator<Candidate>(){

            @Override
            public int compare(Candidate candidate, Candidate candidate2) {
                return Double.compare(candidate.score, candidate2.score);
            }
        });
        return list;
    }

    private List<Candidate> collectColumns(Vec3 vec3, int n, int n2, int n3, int n4, Vec3 vec32, Vec3 vec33, double d) {
        ArrayList<Candidate> arrayList = new ArrayList<Candidate>();
        int n5 = MathHelper.floor_double((double)vec3.xCoord);
        int n6 = MathHelper.floor_double((double)vec3.yCoord);
        int n7 = MathHelper.floor_double((double)vec3.zCoord);
        for (int i = -n; i <= n; i += n2) {
            block1: for (int j = -n; j <= n; j += n2) {
                int n8 = n5 + i;
                double d2 = (double)n8 + 0.5 - PearlRescueModule.mc.thePlayer.posX;
                int n9 = n7 + j;
                double d3 = (double)n9 + 0.5 - PearlRescueModule.mc.thePlayer.posZ;
                double d4 = Math.sqrt(d2 * d2 + d3 * d3);
                if (d4 > d || d4 > 53.0) continue;
                for (int k = n6 + n4; k >= n6 - n3; --k) {
                    Vec3 vec34;
                    double d5;
                    BlockPos blockPos = new BlockPos(n8, k, n9);
                    if (!this.isSolid(blockPos)) continue;
                    if (this.isSolid(blockPos.up()) || this.isSolid(blockPos.up(2)) || !((d5 = vec32.distanceTo(vec34 = PearlRescueModule.topCentre(blockPos))) >= 2.0) || !(d5 <= d) || !this.reachable(blockPos)) continue block1;
                    arrayList.add(new Candidate(blockPos, vec34.distanceTo(vec33)));
                    continue block1;
                }
            }
        }
        return arrayList;
    }

    private boolean reachable(BlockPos blockPos) {
        double d = (double)blockPos.getX() + 0.5 - PearlRescueModule.mc.thePlayer.posX;
        double d2 = (double)blockPos.getZ() + 0.5 - PearlRescueModule.mc.thePlayer.posZ;
        double d3 = Math.sqrt(d * d + d2 * d2);
        double d4 = (double)blockPos.getY() + 1.0 - (PearlRescueModule.mc.thePlayer.posY + (double)PearlRescueModule.mc.thePlayer.getEyeHeight());
        double d5 = d4 > 0.0 ? 53.0 - d4 * 1.05 : 53.0;
        return d3 <= d5;
    }

    private int edgeDistance(BlockPos blockPos) {
        for (int i = 1; i <= 4; ++i) {
            for (int j = -i; j <= i; ++j) {
                for (int k = -i; k <= i; ++k) {
                    BlockPos blockPos2;
                    if (Math.max(Math.abs(j), Math.abs(k)) != i || this.isSolid(blockPos2 = blockPos.add(j, 0, k)) || this.isSolid(blockPos2.down())) continue;
                    return i - 1;
                }
            }
        }
        return 4;
    }

    private double enemyDistance(BlockPos blockPos) {
        double d = Double.MAX_VALUE;
        double d2 = (double)blockPos.getX() + 0.5;
        double d3 = (double)blockPos.getY() + 1.0;
        double d4 = (double)blockPos.getZ() + 0.5;
        for (EntityPlayer entityPlayer : PearlRescueModule.mc.theWorld.playerEntities) {
            if (entityPlayer == PearlRescueModule.mc.thePlayer || RescueServices.friendly(entityPlayer) || mc.getNetHandler() == null || mc.getNetHandler().getPlayerInfo(entityPlayer.getUniqueID()) == null || RescueServices.bot(entityPlayer)) continue;
            double d5 = entityPlayer.posX - d2;
            double d6 = entityPlayer.posY - d3;
            double d7 = entityPlayer.posZ - d4;
            d = Math.min(d, Math.sqrt(d5 * d5 + d6 * d6 + d7 * d7));
        }
        return d;
    }

    private boolean solveFor(BlockPos blockPos) {
        Vec3 vec3 = PearlRescueModule.topCentre(blockPos);
        float f = this.yawTo(vec3);
        float[] fArray = new float[4];
        double[] dArray = new double[4];
        for (int i = 0; i < 4; ++i) {
            dArray[i] = Double.MAX_VALUE;
        }
        for (float f2 = -89.0f; f2 <= 89.0f; f2 += 1.0f) {
            double d = this.closestApproachSq(f, f2, vec3);
            PearlRescueModule.insertCandidate(fArray, dArray, f2, d);
        }
        for (int i = 0; i < 4; ++i) {
            float f3;
            if (dArray[i] == Double.MAX_VALUE || !this.landsOnTarget(this.simulate(f, f3 = this.refinePitch(f, fArray[i], vec3), true), blockPos)) continue;
            this.aimYaw = f;
            this.aimPitch = f3;
            return true;
        }
        return false;
    }

    private static void insertCandidate(float[] fArray, double[] dArray, float f, double d) {
        for (int i = 0; i < dArray.length; ++i) {
            if (d >= dArray[i]) continue;
            for (int j = dArray.length - 1; j > i; --j) {
                dArray[j] = dArray[j - 1];
                fArray[j] = fArray[j - 1];
            }
            dArray[i] = d;
            fArray[i] = f;
            return;
        }
    }

    private float refinePitch(float f, float f2, Vec3 vec3) {
        float f3 = f2;
        double d = Double.MAX_VALUE;
        for (float f4 = f2 - 1.5f; f4 <= f2 + 1.5f; f4 += 0.01f) {
            double d2;
            if (f4 < -90.0f || f4 > 90.0f || !((d2 = this.closestApproachSq(f, f4, vec3)) < d)) continue;
            d = d2;
            f3 = f4;
        }
        return f3;
    }

    private float yawTo(Vec3 vec3) {
        double d = Math.toDegrees(Math.atan2(vec3.zCoord - PearlRescueModule.mc.thePlayer.posZ, vec3.xCoord - PearlRescueModule.mc.thePlayer.posX)) - 90.0;
        for (int i = 0; i < 4; ++i) {
            double d2 = Math.toRadians(d);
            double d3 = PearlRescueModule.mc.thePlayer.posX - Math.cos(d2) * 0.16;
            double d4 = PearlRescueModule.mc.thePlayer.posZ - Math.sin(d2) * 0.16;
            d = Math.toDegrees(Math.atan2(vec3.zCoord - d4, vec3.xCoord - d3)) - 90.0;
        }
        return (float)d;
    }

    private static Vec3 topCentre(BlockPos blockPos) {
        return new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.0, (double)blockPos.getZ() + 0.5);
    }

    private double closestApproachSq(float f, float f2, Vec3 vec3) {
        double d = Math.toRadians(f);
        double d2 = Math.toRadians(f2);
        double d3 = PearlRescueModule.mc.thePlayer.posX - Math.cos(d) * 0.16;
        double d4 = PearlRescueModule.mc.thePlayer.posY + (double)PearlRescueModule.mc.thePlayer.getEyeHeight() - (double)0.1f;
        double d5 = PearlRescueModule.mc.thePlayer.posZ - Math.sin(d) * 0.16;
        double d6 = -Math.sin(d) * Math.cos(d2) * 1.5;
        double d7 = -Math.sin(d2) * 1.5;
        double d8 = Math.cos(d) * Math.cos(d2) * 1.5;
        double d9 = Double.MAX_VALUE;
        for (int i = 0; i < 160; ++i) {
            double d10 = d3 + d6;
            double d11 = d4 + d7;
            double d12 = d5 + d8;
            d9 = Math.min(d9, PearlRescueModule.segmentDistanceSq(d3, d4, d5, d10, d11, d12, vec3));
            d3 = d10;
            d4 = d11;
            d5 = d12;
            d6 *= 0.99;
            d7 *= 0.99;
            d8 *= 0.99;
            if ((d7 -= 0.03) < 0.0 && d4 < vec3.yCoord - 8.0) break;
        }
        return d9;
    }

    private static double segmentDistanceSq(double d, double d2, double d3, double d4, double d5, double d6, Vec3 vec3) {
        double d7 = d4 - d;
        double d8 = d5 - d2;
        double d9 = d6 - d3;
        double d10 = d7 * d7 + d8 * d8 + d9 * d9;
        double d11 = 0.0;
        if (d10 > 1.0E-9) {
            d11 = ((vec3.xCoord - d) * d7 + (vec3.yCoord - d2) * d8 + (vec3.zCoord - d3) * d9) / d10;
            d11 = Math.max(0.0, Math.min(1.0, d11));
        }
        double d12 = d + d7 * d11 - vec3.xCoord;
        double d13 = d2 + d8 * d11 - vec3.yCoord;
        double d14 = d3 + d9 * d11 - vec3.zCoord;
        return d12 * d12 + d13 * d13 + d14 * d14;
    }

    private MovingObjectPosition simulate(float f, float f2, boolean bl) {
        double d = Math.toRadians(f);
        double d2 = Math.toRadians(f2);
        double d3 = PearlRescueModule.mc.thePlayer.posX - Math.cos(d) * 0.16;
        double d4 = PearlRescueModule.mc.thePlayer.posY + (double)PearlRescueModule.mc.thePlayer.getEyeHeight() - (double)0.1f;
        double d5 = PearlRescueModule.mc.thePlayer.posZ - Math.sin(d) * 0.16;
        double d6 = -Math.sin(d) * Math.cos(d2) * 1.5;
        double d7 = -Math.sin(d2) * 1.5;
        double d8 = Math.cos(d) * Math.cos(d2) * 1.5;
        this.simFlightTicks = 160;
        for (int i = 0; i < 160; ++i) {
            MovingObjectPosition movingObjectPosition;
            Vec3 vec3 = new Vec3(d3, d4, d5);
            Vec3 vec32 = new Vec3(d3 + d6, d4 + d7, d5 + d8);
            if (bl && (movingObjectPosition = PearlRescueModule.mc.theWorld.rayTraceBlocks(vec3, vec32)) != null) {
                this.simFlightTicks = i + 1;
                return movingObjectPosition;
            }
            d3 += d6;
            d4 += d7;
            d5 += d8;
            d6 *= 0.99;
            d7 *= 0.99;
            d8 *= 0.99;
            d7 -= 0.03;
            if (d4 < -8.0) break;
        }
        return null;
    }

    private boolean landsOnTarget(MovingObjectPosition movingObjectPosition, BlockPos blockPos) {
        if (movingObjectPosition == null || blockPos == null) {
            return false;
        }
        if (movingObjectPosition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || movingObjectPosition.sideHit != EnumFacing.UP) {
            return false;
        }
        BlockPos blockPos2 = movingObjectPosition.getBlockPos();
        if (blockPos2.getY() != blockPos.getY()) {
            return false;
        }
        if(!blockPos2.equals(blockPos)||movingObjectPosition.hitVec==null)return false;
        double x=movingObjectPosition.hitVec.xCoord-blockPos.getX(),z=movingObjectPosition.hitVec.zCoord-blockPos.getZ();
        return x>=0.1&&x<=0.9&&z>=0.1&&z<=0.9;
    }

    private boolean isSolid(BlockPos blockPos) {
        long l = (long)(blockPos.getX() & 0x3FFFFFF) << 38 | (long)(blockPos.getY() & 0xFFF) << 26 | (long)(blockPos.getZ() & 0x3FFFFFF);
        Boolean bl = this.solidCache.get(l);
        if (bl != null) {
            return bl;
        }
        IBlockState iBlockState = PearlRescueModule.mc.theWorld.getBlockState(blockPos);
        Block block = iBlockState.getBlock();
        boolean bl2 = false;
        if (block != null) {
            AxisAlignedBB axisAlignedBB = block.getCollisionBoundingBox((World)PearlRescueModule.mc.theWorld, blockPos, iBlockState);
            bl2 = axisAlignedBB != null;
        }
        this.solidCache.put(l, bl2);
        return bl2;
    }

    @Override
    public String statusText() {
        if (this.targetBlock == null || PearlRescueModule.mc.thePlayer == null) {
            return "";
        }
        double d = PearlRescueModule.mc.thePlayer.getDistance((double)this.targetBlock.getX() + 0.5, (double)this.targetBlock.getY() + 1.0, (double)this.targetBlock.getZ() + 0.5);
        return MathHelper.floor_double((double)d) + "m" + (this.forced ? " !" : "");
    }

    private static final class Candidate {
        private final BlockPos pos;
        private final double focusDistance;
        private double score;
        private boolean safe;
        private boolean tried;

        private Candidate(BlockPos blockPos, double d) {
            this.pos = blockPos;
            this.focusDistance = d;
        }
    }
    private void refreshTriggerConditions() {
        this.solidCache.clear();
        this.trackDamage();
        boolean bl = (this.requireAboveVoid.isEnabled() || this.predictSafeLanding.isEnabled()) && !PearlRescueModule.mc.thePlayer.onGround && this.aboveVoid();
        boolean bl2 = this.damageDuringVoid = bl && (this.damageDuringVoid || this.hasRecentDamage());
        if (this.predictSafeLanding.isEnabled()) {
            this.voidDuration.reset();
            this.voidReady = this.predictiveRescueReady();
        } else if (this.inVoidFor.isEnabled()) {
            this.predictiveEdgeTopY = Double.NaN;
            this.voidReady = this.voidDuration.update(bl, System.nanoTime(), (long)(this.voidTime.doubleValue() * 1.0E9));
        } else {
            this.predictiveEdgeTopY = Double.NaN;
            this.voidDuration.reset();
            this.voidReady = bl;
        }
        this.pearlSlot = this.findPearlSlot();
    }
    public boolean shouldPrecedeBridgeEgg() {
        if(!fireballMode||!isEnabled()||!RescueServices.ready())return false;
        if(activeWorld!=mc.theWorld||activePlayer!=mc.thePlayer){resetSession();activeWorld=mc.theWorld;activePlayer=mc.thePlayer;}
        if(mc.thePlayer.onGround&&!isRescueActive())comboAttempted=false;
        // This is a sequence gate, not a prediction of whether Fireball Pearl can
        // trigger this frame. Missing items, delayed conditions and a reachable
        // normal-pearl target must not silently let Egg go first.
        return isRescueActive()||!comboAttempted;
    }
}

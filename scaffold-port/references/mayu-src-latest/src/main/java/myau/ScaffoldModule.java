package myau;

import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.WorldSettings;

public class ScaffoldModule
extends Module {
    private static final Minecraft minecraft = Minecraft.getMinecraft();
    private static final double[] FACE_SAMPLE_OFFSETS = new double[]{0.03125, 0.09375, 0.15625, 0.21875, 0.28125, 0.34375, 0.40625, 0.46875, 0.53125, 0.59375, 0.65625, 0.71875, 0.78125, 0.84375, 0.90625, 0.96875};
    private int placementDelayTicks = 0;
    private int previousHotbarSlot = -1;
    private int remainingStackBlocks = -1;
    private float placementYaw = -180.0f;
    private float placementPitch = 0.0f;
    private boolean hasPlacementRotation = false;
    private float tickPlacementYaw;
    private float tickPlacementPitch;
    private int towerPhase = 0;
    private int towerCycleCount = 0;
    private int keepYState = 0;
    private int keepYLevel = 256;
    private boolean keepYRecoveryPlacement = false;
    private boolean tellyRotationActive = false;
    private EnumFacing pendingPlacementFace = null;
    public final ModeSetting rotations = new ModeSetting("rotations", 2, new String[]{"NONE", "DEFAULT", "BACKWARDS", "SIDEWAYS"});
    public final ModeSetting moveFix = new ModeSetting("move-fix", 1, new String[]{"NONE", "SILENT"});
    public final ModeSetting sprint = new ModeSetting("sprint", 0, new String[]{"NONE", "VANILLA"});
    public final PercentSetting groundMotion = new PercentSetting("ground-motion", 100);
    public final PercentSetting airMotion = new PercentSetting("air-motion", 100);
    public final PercentSetting speedMotion = new PercentSetting("speed-motion", 100);
    public final ModeSetting tower = new ModeSetting("tower", 0, new String[]{"NONE", "VANILLA", "EXTRA", "TELLY"});
    public final ModeSetting keepY = new ModeSetting("keep-y", 0, new String[]{"NONE", "VANILLA", "EXTRA", "TELLY"});
    public final BooleanSetting keepYOnPress = new BooleanSetting("keep-y-on-press", false, this::isKeepYOnPressVisible);
    public final BooleanSetting multiPlace = new BooleanSetting("multi-place", true);
    public final BooleanSetting safeWalk = new BooleanSetting("safe-walk", true);
    public final BooleanSetting swing = new BooleanSetting("swing", true);
    public final BooleanSetting itemSpoof = new BooleanSetting("item-spoof", false);
    public final BooleanSetting blockCounter = new BooleanSetting("block-counter", true);

    private boolean shouldDisableSprint() {
        boolean standardKeepY;
        if (this.isTellyTakeoff()) {
            return false;
        }
        boolean bl = standardKeepY = (Integer)this.keepY.getValue() == 1 || (Integer)this.keepY.getValue() == 2;
        if (standardKeepY && this.keepYState > 0) {
            return false;
        }
        return (Integer)this.sprint.getValue() == 0;
    }

    private boolean canPlaceWithActiveModules() {
        BedNukerModule bedNuker = (BedNukerModule)MyauClient.moduleManager.modules.get(BedNukerModule.class);
        if (bedNuker.isEnabled() && bedNuker.isControllingInteractions()) {
            return false;
        }
        LongJumpModule longJump = (LongJumpModule)MyauClient.moduleManager.modules.get(LongJumpModule.class);
        return !longJump.isEnabled() || !longJump.isAutomaticFireballMode() || longJump.hasJumpStarted();
    }

    private EnumFacing findConnectingFace(BlockPos supportBlock, BlockPos desiredPlacementBlock) {
        EnumFacing bestFace = null;
        double bestDistanceSquared = 0.0;
        for (EnumFacing candidateFace : EnumFacing.VALUES) {
            BlockPos neighborBlock;
            if (candidateFace == EnumFacing.DOWN || (neighborBlock = supportBlock.offset(candidateFace)).getY() > desiredPlacementBlock.getY() || !BlockUtils.isReplaceableAt(neighborBlock)) continue;
            double distanceSquared = neighborBlock.distanceSqToCenter((double)desiredPlacementBlock.getX() + 0.5, (double)desiredPlacementBlock.getY() + 0.5, (double)desiredPlacementBlock.getZ() + 0.5);
            if (bestFace != null && !(distanceSquared < bestDistanceSquared) && (distanceSquared != bestDistanceSquared || candidateFace != EnumFacing.UP)) continue;
            bestDistanceSquared = distanceSquared;
            bestFace = candidateFace;
        }
        return bestFace;
    }

    private BlockPlacementTarget findPlacementTarget() {
        int playerBlockY = MathHelper.floor_double((double)ScaffoldModule.minecraft.thePlayer.posY);
        int placementY = this.keepYState != 0 && !this.keepYRecoveryPlacement ? Math.min(playerBlockY, this.keepYLevel) : playerBlockY;
        BlockPos desiredPlacementBlock = new BlockPos(MathHelper.floor_double((double)ScaffoldModule.minecraft.thePlayer.posX), placementY - 1, MathHelper.floor_double((double)ScaffoldModule.minecraft.thePlayer.posZ));
        if (!BlockUtils.isReplaceableAt(desiredPlacementBlock)) {
            return null;
        }
        ArrayList<BlockPos> supportCandidates = new ArrayList<BlockPos>();
        double blockReach = ScaffoldModule.minecraft.playerController.getBlockReachDistance();
        for (int offsetX = -4; offsetX <= 4; ++offsetX) {
            for (int offsetY = -4; offsetY <= 0; ++offsetY) {
                for (int offsetZ = -4; offsetZ <= 4; ++offsetZ) {
                    double distanceToCandidate;
                    BlockPos candidateBlock = desiredPlacementBlock.add(offsetX, offsetY, offsetZ);
                    if (BlockUtils.isReplaceableAt(candidateBlock) || BlockUtils.isInteractableAt(candidateBlock) || (distanceToCandidate = ScaffoldModule.minecraft.thePlayer.getDistance((double)candidateBlock.getX() + 0.5, (double)candidateBlock.getY() + 0.5, (double)candidateBlock.getZ() + 0.5)) > blockReach || this.keepYState != 0 && !this.keepYRecoveryPlacement && candidateBlock.getY() >= this.keepYLevel) continue;
                    for (EnumFacing face : EnumFacing.VALUES) {
                        if (face == EnumFacing.DOWN || !BlockUtils.isReplaceableAt(candidateBlock.offset(face))) continue;
                        supportCandidates.add(candidateBlock);
                    }
                }
            }
        }
        if (supportCandidates.isEmpty()) {
            return null;
        }
        supportCandidates.sort(Comparator.comparingDouble(supportCandidate -> ScaffoldModule.targetDistanceSquared(desiredPlacementBlock, supportCandidate)));
        BlockPos selectedSupportBlock = (BlockPos)supportCandidates.get(0);
        EnumFacing connectingFace = this.findConnectingFace(selectedSupportBlock, desiredPlacementBlock);
        return connectingFace == null ? null : new BlockPlacementTarget(selectedSupportBlock, connectingFace);
    }

    private MovingObjectPosition tracePlacement(BlockPos position, EnumFacing facing, float yaw, float pitch) {
        MovingObjectPosition rayHit = RotationUtils.traceBlocks(yaw, pitch, ScaffoldModule.minecraft.playerController.getBlockReachDistance(), 1.0f);
        if (rayHit == null || rayHit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !rayHit.getBlockPos().equals((Object)position) || rayHit.sideHit != facing) {
            return null;
        }
        return rayHit;
    }

    private void placeBlock(BlockPos position, EnumFacing facing) {
        if (!ItemUtils.isHoldingPlaceableBlock() || this.remainingStackBlocks <= 0) {
            return;
        }
        MovingObjectPosition rayHit = this.tracePlacement(position, facing, this.tickPlacementYaw, this.tickPlacementPitch);
        if (rayHit == null) {
            return;
        }
        boolean placed = ScaffoldModule.minecraft.playerController.onPlayerRightClick(ScaffoldModule.minecraft.thePlayer, ScaffoldModule.minecraft.theWorld, ScaffoldModule.minecraft.thePlayer.inventory.getCurrentItem(), position, facing, rayHit.hitVec);
        if (!placed) {
            return;
        }
        if (ScaffoldModule.minecraft.playerController.getCurrentGameType() != WorldSettings.GameType.CREATIVE) {
            --this.remainingStackBlocks;
        }
        if (((Boolean)this.swing.getValue()).booleanValue()) {
            ScaffoldModule.minecraft.thePlayer.swingItem();
        } else {
            PacketUtils.send(new C0APacketAnimation());
        }
    }

    private EnumFacing facingFromYaw(float yaw) {
        if (yaw < -135.0f || yaw > 135.0f) {
            return EnumFacing.NORTH;
        }
        if (yaw < -45.0f) {
            return EnumFacing.EAST;
        }
        if (yaw < 45.0f) {
            return EnumFacing.SOUTH;
        }
        return EnumFacing.WEST;
    }

    private double distanceToBlockEdge(EnumFacing facing) {
        switch (facing) {
            case NORTH: {
                return ScaffoldModule.minecraft.thePlayer.posZ - Math.floor(ScaffoldModule.minecraft.thePlayer.posZ);
            }
            case EAST: {
                return Math.ceil(ScaffoldModule.minecraft.thePlayer.posX) - ScaffoldModule.minecraft.thePlayer.posX;
            }
            case SOUTH: {
                return Math.ceil(ScaffoldModule.minecraft.thePlayer.posZ) - ScaffoldModule.minecraft.thePlayer.posZ;
            }
        }
        return ScaffoldModule.minecraft.thePlayer.posX - Math.floor(ScaffoldModule.minecraft.thePlayer.posX);
    }

    private float getMotionMultiplier() {
        int percent = !ScaffoldModule.minecraft.thePlayer.onGround ? (Integer)this.airMotion.getValue() : (MovementUtils.speedPotionLevel() > 0 ? ((Integer)this.speedMotion.getValue()).intValue() : ((Integer)this.groundMotion.getValue()).intValue());
        return (float)percent / 100.0f;
    }

    private double randomEdgeApproachDistance() {
        return 0.2155 - RandomUtils.randomDouble(1.0E-4, 9.0E-4);
    }

    private float getMovementYaw() {
        return MovementUtils.movementYawForInput(ScaffoldModule.minecraft.thePlayer.rotationYaw, MovementUtils.forwardInput(), MovementUtils.strafeInput());
    }

    private boolean isDiagonalYaw(float angle) {
        float remainder = Math.abs(angle % 90.0f);
        return remainder > 20.0f && remainder < 70.0f;
    }

    private boolean isTellyTakeoff() {
        if (!ScaffoldModule.minecraft.thePlayer.onGround || !MovementUtils.hasMovementInput()) {
            return false;
        }
        if (PlayerUtils.hasCollisionAbove()) {
            return false;
        }
        if ((Integer)this.keepY.getValue() == 3 && this.keepYState > 0) {
            return true;
        }
        return (Integer)this.tower.getValue() == 3 && ScaffoldModule.minecraft.gameSettings.keyBindJump.isKeyDown();
    }

    public ScaffoldModule() {
        super("Scaffold", false, 0);
    }

    public int getPreviousHotbarSlot() {
        return this.previousHotbarSlot;
    }

    private void selectPlacementHotbarSlot() {
        ItemStack heldStack = ScaffoldModule.minecraft.thePlayer.getHeldItem();
        int heldStackSize = ItemUtils.isPlaceableBlockStack(heldStack) ? heldStack.stackSize : 0;
        this.remainingStackBlocks = Math.min(this.remainingStackBlocks, heldStackSize);
        if (this.remainingStackBlocks > 0) {
            return;
        }
        int searchStartSlot = ScaffoldModule.minecraft.thePlayer.inventory.currentItem;
        if (this.remainingStackBlocks == 0) {
            --searchStartSlot;
        }
        for (int slotCursor = searchStartSlot; slotCursor > searchStartSlot - 9; --slotCursor) {
            int hotbarSlot = (slotCursor % 9 + 9) % 9;
            ItemStack candidateStack = ScaffoldModule.minecraft.thePlayer.inventory.getStackInSlot(hotbarSlot);
            if (!ItemUtils.isPlaceableBlockStack(candidateStack)) continue;
            ScaffoldModule.minecraft.thePlayer.inventory.currentItem = hotbarSlot;
            this.remainingStackBlocks = candidateStack.stackSize;
            return;
        }
    }

    private Vec3 findBestPlacementHit(BlockPlacementTarget placementTarget, float previousYaw, float previousPitch) {
        if (placementTarget == null) {
            return null;
        }
        double[] hitOffsetsX = FACE_SAMPLE_OFFSETS;
        double[] hitOffsetsY = FACE_SAMPLE_OFFSETS;
        double[] hitOffsetsZ = FACE_SAMPLE_OFFSETS;
        switch (placementTarget.face()) {
            case NORTH: {
                hitOffsetsZ = new double[]{0.0};
                break;
            }
            case EAST: {
                hitOffsetsX = new double[]{1.0};
                break;
            }
            case SOUTH: {
                hitOffsetsZ = new double[]{1.0};
                break;
            }
            case WEST: {
                hitOffsetsX = new double[]{0.0};
                break;
            }
            case DOWN: {
                hitOffsetsY = new double[]{0.0};
                break;
            }
            case UP: {
                hitOffsetsY = new double[]{1.0};
                break;
            }
        }
        float bestYaw = -180.0f;
        float bestPitch = 0.0f;
        float bestRotationCost = 0.0f;
        Vec3 bestHit = null;
        for (double hitOffsetX : hitOffsetsX) {
            for (double hitOffsetY : hitOffsetsY) {
                for (double hitOffsetZ : hitOffsetsZ) {
                    double deltaX = (double)placementTarget.position().getX() + hitOffsetX - ScaffoldModule.minecraft.thePlayer.posX;
                    double deltaY = (double)placementTarget.position().getY() + hitOffsetY - ScaffoldModule.minecraft.thePlayer.posY - (double)ScaffoldModule.minecraft.thePlayer.getEyeHeight();
                    double deltaZ = (double)placementTarget.position().getZ() + hitOffsetZ - ScaffoldModule.minecraft.thePlayer.posZ;
                    float[] candidateRotations = RotationUtils.rotationsFromDelta(deltaX, deltaY, deltaZ);
                    candidateRotations[0] = RotationUtils.snapToMouseSensitivity(candidateRotations[0], previousYaw);
                    candidateRotations[1] = RotationUtils.snapToMouseSensitivity(candidateRotations[1], previousPitch);
                    candidateRotations[1] = MathHelper.clamp_float((float)candidateRotations[1], (float)-90.0f, (float)90.0f);
                    MovingObjectPosition candidateHit = RotationUtils.traceBlocks(candidateRotations[0], candidateRotations[1], ScaffoldModule.minecraft.playerController.getBlockReachDistance(), 1.0f);
                    if (candidateHit == null || candidateHit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !candidateHit.getBlockPos().equals((Object)placementTarget.position()) || candidateHit.sideHit != placementTarget.face()) continue;
                    float rotationCost = Math.abs(MathHelper.wrapAngleTo180_float((float)(candidateRotations[0] - this.placementYaw))) + Math.abs(candidateRotations[1] - this.placementPitch);
                    if ((bestYaw != -180.0f || bestPitch != 0.0f) && !(rotationCost < bestRotationCost)) continue;
                    bestYaw = candidateRotations[0];
                    bestPitch = candidateRotations[1];
                    bestRotationCost = rotationCost;
                    bestHit = candidateHit.hitVec;
                }
            }
        }
        if (bestYaw != -180.0f || bestPitch != 0.0f) {
            this.placementYaw = bestYaw;
            this.placementPitch = bestPitch;
            this.hasPlacementRotation = true;
        }
        return bestHit;
    }

    private void applyPlacementRotation(RotationUpdateEvent event, BlockPlacementTarget placementTarget, float previousYaw, float previousPitch, float backwardYaw, float sidewaysYaw) {
        float backwardYawDifference;
        if (this.hasPlacementRotation && MovementUtils.hasMovementInput() && (backwardYawDifference = Math.abs(MathHelper.wrapAngleTo180_float((float)(backwardYaw - this.placementYaw)))) < 90.0f) {
            switch ((Integer)this.rotations.getValue()) {
                case 2: {
                    float snappedBackwardYaw = RotationUtils.snapToMouseSensitivity(backwardYaw, previousYaw);
                    if (placementTarget == null || this.tracePlacement(placementTarget.position(), placementTarget.face(), snappedBackwardYaw, this.placementPitch) == null) break;
                    this.placementYaw = snappedBackwardYaw;
                    break;
                }
                case 3: {
                    float snappedSidewaysYaw = RotationUtils.snapToMouseSensitivity(sidewaysYaw, previousYaw);
                    if (placementTarget == null || this.tracePlacement(placementTarget.position(), placementTarget.face(), snappedSidewaysYaw, this.placementPitch) == null) break;
                    this.placementYaw = snappedSidewaysYaw;
                    break;
                }
            }
        }
        if ((Integer)this.rotations.getValue() == 0) {
            return;
        }
        float requestedYaw = this.placementYaw;
        float requestedPitch = this.placementPitch;
        if (this.isTellyTakeoff()) {
            float playerYawDifference = MathHelper.wrapAngleTo180_float((float)(ScaffoldModule.minecraft.thePlayer.rotationYaw - event.getPreviousYaw()));
            requestedYaw = RotationUtils.snapToMouseSensitivity(previousYaw + playerYawDifference * RandomUtils.randomFloat(0.98f, 0.99f), previousYaw);
            requestedPitch = RotationUtils.snapToMouseSensitivity(RandomUtils.randomFloat(30.0f, 80.0f), previousPitch);
            this.placementDelayTicks = 3;
            this.tellyRotationActive = true;
        } else {
            float placementYawDifference = MathHelper.wrapAngleTo180_float((float)(this.placementYaw - event.getPreviousYaw()));
            float maxYawChange = RandomUtils.randomFloat(80.0f, 90.0f);
            if (this.tellyRotationActive && ScaffoldModule.minecraft.thePlayer.motionY > 0.0 && this.placementDelayTicks <= 1) {
                maxYawChange = RandomUtils.randomFloat(30.0f, 35.0f);
            }
            if (Math.abs(placementYawDifference) > maxYawChange) {
                float clampedYawChange = RotationUtils.clampAngleChange(placementYawDifference, maxYawChange);
                requestedYaw = RotationUtils.snapToMouseSensitivity(previousYaw + clampedYawChange, previousYaw);
                this.placementDelayTicks = Math.max(this.placementDelayTicks, 1);
            }
        }
        event.setRotation(requestedYaw, requestedPitch, 3);
        if ((Integer)this.moveFix.getValue() == 1) {
            event.setMovementYaw(requestedYaw, 3);
        }
    }

    private void placeCurrentAndAdditionalBlocks(BlockPlacementTarget placementTarget, Vec3 placementHit) {
        if (placementTarget == null || placementHit == null || this.placementDelayTicks > 0) {
            return;
        }
        this.placeBlock(placementTarget.position(), placementTarget.face());
        if (!((Boolean)this.multiPlace.getValue()).booleanValue()) {
            return;
        }
        for (int extraPlacementIndex = 0; extraPlacementIndex < 3 && (placementTarget = this.findPlacementTarget()) != null; ++extraPlacementIndex) {
            double fallbackDeltaZ;
            MovingObjectPosition extraPlacementHit = RotationUtils.traceBlocks(this.placementYaw, this.placementPitch, ScaffoldModule.minecraft.playerController.getBlockReachDistance(), 1.0f);
            if (extraPlacementHit != null && extraPlacementHit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && extraPlacementHit.getBlockPos().equals((Object)placementTarget.position()) && extraPlacementHit.sideHit == placementTarget.face()) {
                this.placeBlock(placementTarget.position(), placementTarget.face());
                continue;
            }
            Vec3 fallbackHit = BlockUtils.randomFacePoint(placementTarget.position(), placementTarget.face());
            double fallbackDeltaX = fallbackHit.xCoord - ScaffoldModule.minecraft.thePlayer.posX;
            double fallbackDeltaY = fallbackHit.yCoord - ScaffoldModule.minecraft.thePlayer.posY - (double)ScaffoldModule.minecraft.thePlayer.getEyeHeight();
            float[] fallbackRotations = RotationUtils.rotationsFromDelta(fallbackDeltaX, fallbackDeltaY, fallbackDeltaZ = fallbackHit.zCoord - ScaffoldModule.minecraft.thePlayer.posZ);
            if (Math.abs(MathHelper.wrapAngleTo180_float((float)(fallbackRotations[0] - this.placementYaw))) >= 120.0f || Math.abs(fallbackRotations[1] - this.placementPitch) >= 60.0f || (extraPlacementHit = RotationUtils.traceBlocks(fallbackRotations[0], fallbackRotations[1], ScaffoldModule.minecraft.playerController.getBlockReachDistance(), 1.0f)) == null || extraPlacementHit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !extraPlacementHit.getBlockPos().equals((Object)placementTarget.position()) || extraPlacementHit.sideHit != placementTarget.face()) break;
            this.placeBlock(placementTarget.position(), placementTarget.face());
        }
    }

    private boolean handlePendingTowerPlacement() {
        if (this.pendingPlacementFace == null) {
            return false;
        }
        if (this.placementDelayTicks <= 0) {
            BlockPos beneathPlayer = new BlockPos(MathHelper.floor_double((double)ScaffoldModule.minecraft.thePlayer.posX), MathHelper.floor_double((double)ScaffoldModule.minecraft.thePlayer.posY) - 1, MathHelper.floor_double((double)ScaffoldModule.minecraft.thePlayer.posZ));
            this.placeBlock(beneathPlayer, this.pendingPlacementFace);
        }
        this.pendingPlacementFace = null;
        return true;
    }

    private void handleExtraKeepYRecovery() {
        if ((Integer)this.keepY.getValue() != 2 || this.keepYState <= 0 || ScaffoldModule.minecraft.thePlayer.onGround) {
            return;
        }
        int predictedBlockY = MathHelper.floor_double((double)(ScaffoldModule.minecraft.thePlayer.posY + ScaffoldModule.minecraft.thePlayer.motionY));
        if (predictedBlockY > this.keepYLevel || ScaffoldModule.minecraft.thePlayer.posY <= (double)(this.keepYLevel + 1)) {
            return;
        }
        this.keepYRecoveryPlacement = true;
        BlockPlacementTarget placementTarget = this.findPlacementTarget();
        if (placementTarget == null || this.placementDelayTicks > 0) {
            return;
        }
        this.placeBlock(placementTarget.position(), placementTarget.face());
    }

    @EventHandler(priority=50)
    public void onRotationUpdate(RotationUpdateEvent event) {
        float sidewaysYaw;
        if (!this.isEnabled()) {
            return;
        }
        if (this.placementDelayTicks > 0) {
            --this.placementDelayTicks;
        }
        if (ScaffoldModule.minecraft.thePlayer.onGround) {
            if (this.keepYState > 0) {
                --this.keepYState;
            }
            if (this.keepYState < 0) {
                ++this.keepYState;
            }
            if (!(this.keepYState != 0 || (Integer)this.keepY.getValue() == 0 || ((Boolean)this.keepYOnPress.getValue()).booleanValue() && !PlayerUtils.isUseItemKeyDown() || ScaffoldModule.minecraft.gameSettings.keyBindJump.isKeyDown() || ScaffoldModule.minecraft.thePlayer.isPotionActive(Potion.jump))) {
                this.keepYState = 1;
            }
            if (!this.keepYRecoveryPlacement) {
                this.keepYLevel = MathHelper.floor_double((double)ScaffoldModule.minecraft.thePlayer.posY);
            }
            this.keepYRecoveryPlacement = false;
            this.tellyRotationActive = false;
        }
        if (!this.canPlaceWithActiveModules()) {
            return;
        }
        this.selectPlacementHotbarSlot();
        float previousYaw = event.getPreviousYaw();
        float previousPitch = event.getPreviousPitch();
        float movementYaw = this.getMovementYaw();
        float backwardYaw = movementYaw - 180.0f;
        if (this.isDiagonalYaw(movementYaw)) {
            sidewaysYaw = backwardYaw;
        } else {
            float sidewaysDirectionFactor = (movementYaw + 180.0f) % 90.0f < 45.0f ? 1.0f : -1.0f;
            sidewaysYaw = movementYaw - 135.0f * sidewaysDirectionFactor;
        }
        if (!this.hasPlacementRotation) {
            if (this.placementYaw == -180.0f && this.placementPitch == 0.0f) {
                this.placementPitch = RotationUtils.snapToMouseSensitivity(RandomUtils.randomFloat(65.0f, 85.0f), previousPitch);
            }
            switch ((Integer)this.rotations.getValue()) {
                case 2: {
                    this.placementYaw = RotationUtils.snapToMouseSensitivity(backwardYaw, previousYaw);
                    break;
                }
                default: {
                    this.placementYaw = RotationUtils.snapToMouseSensitivity(sidewaysYaw, previousYaw);
                }
            }
        }
        BlockPlacementTarget placementTarget = this.findPlacementTarget();
        Vec3 placementHit = this.findBestPlacementHit(placementTarget, previousYaw, previousPitch);
        this.applyPlacementRotation(event, placementTarget, previousYaw, previousPitch, backwardYaw, sidewaysYaw);
        this.tickPlacementYaw = event.getYaw();
        this.tickPlacementPitch = event.getPitch();
        this.placeCurrentAndAdditionalBlocks(placementTarget, placementHit);
        if (this.handlePendingTowerPlacement()) {
            return;
        }
        this.handleExtraKeepYRecovery();
    }

    @EventHandler
    public void onMoveFlying(MoveFlyingEvent event) {
        if (!this.isEnabled() || ScaffoldModule.minecraft.thePlayer.isCollidedHorizontally || ScaffoldModule.minecraft.thePlayer.hurtTime > 5 || ScaffoldModule.minecraft.thePlayer.isPotionActive(Potion.jump) || !ScaffoldModule.minecraft.gameSettings.keyBindJump.isKeyDown() || !ItemUtils.isHoldingPlaceableBlock()) {
            this.towerPhase = 0;
            this.towerCycleCount = 0;
            return;
        }
        int heightHundredths = (int)(ScaffoldModule.minecraft.thePlayer.posY % 1.0 * 100.0);
        switch ((Integer)this.tower.getValue()) {
            case 1: {
                this.applyVanillaTowerMotion(heightHundredths, event);
                return;
            }
            case 2: {
                this.applyExtraTowerMotion(heightHundredths, event);
                return;
            }
        }
        this.towerPhase = 0;
        this.towerCycleCount = 0;
    }

    @EventHandler
    public void onMovementInput(MovementInputEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if ((Integer)this.moveFix.getValue() == 1 && RotationState.isActive() && RotationState.movementPriority() == 3.0f && MovementUtils.hasMovementInput()) {
            MovementUtils.correctMovementForYaw(RotationState.movementYaw());
        }
        if (!ScaffoldModule.minecraft.thePlayer.onGround || this.keepYState <= 0 || !MovementUtils.hasMovementInput()) {
            return;
        }
        ScaffoldModule.minecraft.thePlayer.movementInput.jump = true;
    }

    @EventHandler
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        float motionMultiplier = this.getMotionMultiplier();
        if (motionMultiplier != 1.0f) {
            if (ScaffoldModule.minecraft.thePlayer.movementInput.moveForward != 0.0f && ScaffoldModule.minecraft.thePlayer.movementInput.moveStrafe != 0.0f) {
                float diagonalScale = 1.0f / (float)Math.sqrt(2.0);
                ScaffoldModule.minecraft.thePlayer.movementInput.moveForward *= diagonalScale;
                ScaffoldModule.minecraft.thePlayer.movementInput.moveStrafe *= diagonalScale;
            }
            ScaffoldModule.minecraft.thePlayer.movementInput.moveForward *= motionMultiplier;
            ScaffoldModule.minecraft.thePlayer.movementInput.moveStrafe *= motionMultiplier;
        }
        if (this.shouldDisableSprint()) {
            ScaffoldModule.minecraft.thePlayer.setSprinting(false);
        }
    }

    @EventHandler
    public void onSafeWalk(SafeWalkEvent event) {
        if (!this.isEnabled() || !((Boolean)this.safeWalk.getValue()).booleanValue() || !ScaffoldModule.minecraft.thePlayer.onGround || ScaffoldModule.minecraft.thePlayer.motionY > 0.0) {
            return;
        }
        if (PlayerUtils.isOffsetCollisionFree(ScaffoldModule.minecraft.thePlayer.motionX, -1.0, ScaffoldModule.minecraft.thePlayer.motionZ)) {
            event.setSafeWalk(true);
        }
    }

    @EventHandler
    public void onRenderOverlay(RenderOverlayEvent event) {
        if (!this.isEnabled() || !((Boolean)this.blockCounter.getValue()).booleanValue()) {
            return;
        }
        int blockCount = 0;
        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
            Block block;
            ItemStack stack = ScaffoldModule.minecraft.thePlayer.inventory.getStackInSlot(hotbarSlot);
            if (stack == null || stack.stackSize <= 0 || !(stack.getItem() instanceof ItemBlock) || BlockUtils.isInteractable(block = ((ItemBlock)stack.getItem()).getBlock()) || !BlockUtils.isSuitableFullBlock(block)) continue;
            blockCount += stack.stackSize;
        }
        this.drawBlockCounter(blockCount);
    }

    @EventHandler
    public void onLeftClick(LeftClickEvent event) {
        if (this.isEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRightClick(RightClickEvent event) {
        if (this.isEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockClick(BlockClickEvent event) {
        if (this.isEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHotbarChange(HotbarChangeEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        this.previousHotbarSlot = event.getTargetSlot(this.previousHotbarSlot);
        event.setCancelled(true);
    }

    @Override
    public void onEnable() {
        this.previousHotbarSlot = ScaffoldModule.minecraft.thePlayer != null ? ScaffoldModule.minecraft.thePlayer.inventory.currentItem : -1;
        this.remainingStackBlocks = -1;
        this.placementDelayTicks = 0;
        this.placementYaw = -180.0f;
        this.placementPitch = 0.0f;
        this.hasPlacementRotation = false;
        this.towerPhase = 0;
        this.towerCycleCount = 0;
        this.tellyRotationActive = false;
    }

    @Override
    public void onDisable() {
        if (ScaffoldModule.minecraft.thePlayer != null && this.previousHotbarSlot != -1) {
            ScaffoldModule.minecraft.thePlayer.inventory.currentItem = this.previousHotbarSlot;
        }
    }

    private boolean isKeepYOnPressVisible() {
        return (Integer)this.keepY.getValue() != 0;
    }

    private static double targetDistanceSquared(BlockPos desiredPlacementBlock, BlockPos supportCandidate) {
        return supportCandidate.distanceSqToCenter((double)desiredPlacementBlock.getX() + 0.5, (double)desiredPlacementBlock.getY() + 0.5, (double)desiredPlacementBlock.getZ() + 0.5);
    }

    private void applyVanillaTowerMotion(int heightHundredths, MoveFlyingEvent event) {
        switch (this.towerPhase) {
            case 0: {
                if (ScaffoldModule.minecraft.thePlayer.onGround) {
                    this.towerPhase = 1;
                    ScaffoldModule.minecraft.thePlayer.motionY = -0.0784000015258789;
                }
                return;
            }
            case 1: {
                if (heightHundredths == 0 && PlayerUtils.hasCollisionBelow()) {
                    this.keepYLevel = MathHelper.floor_double((double)ScaffoldModule.minecraft.thePlayer.posY);
                    this.towerPhase = 2;
                    ScaffoldModule.minecraft.thePlayer.motionY = 0.42f;
                    if (MovementUtils.hasMovementInput()) {
                        MovementUtils.setHorizontalMotion(MovementUtils.horizontalSpeed(), MovementUtils.movementYaw());
                    } else {
                        MovementUtils.setSpeedAlongMotion(0.0);
                        event.setForward(0.0f);
                        event.setStrafe(0.0f);
                    }
                    return;
                }
                this.towerPhase = 0;
                return;
            }
            case 2: {
                this.towerPhase = 3;
                ScaffoldModule.minecraft.thePlayer.motionY = 0.75 - ScaffoldModule.minecraft.thePlayer.posY % 1.0;
                return;
            }
            case 3: {
                this.towerPhase = 1;
                ScaffoldModule.minecraft.thePlayer.motionY = 1.0 - ScaffoldModule.minecraft.thePlayer.posY % 1.0;
                return;
            }
        }
        this.towerPhase = 0;
    }

    private void applyExtraTowerMotion(int heightHundredths, MoveFlyingEvent event) {
        switch (this.towerPhase) {
            case 0: {
                if (ScaffoldModule.minecraft.thePlayer.onGround) {
                    this.towerPhase = 1;
                    ScaffoldModule.minecraft.thePlayer.motionY = -0.0784000015258789;
                }
                return;
            }
            case 1: {
                if (heightHundredths == 0 && PlayerUtils.hasCollisionBelow()) {
                    this.keepYLevel = MathHelper.floor_double((double)ScaffoldModule.minecraft.thePlayer.posY);
                    if (!MovementUtils.hasMovementInput()) {
                        this.towerCycleCount = 2;
                        MovementUtils.setSpeedAlongMotion(0.0);
                        event.setForward(0.0f);
                        event.setStrafe(0.0f);
                        EnumFacing towerFacing = this.facingFromYaw(MathHelper.wrapAngleTo180_float((float)(this.placementYaw - 180.0f)));
                        double edgeDistance = this.distanceToBlockEdge(towerFacing);
                        if (edgeDistance > 0.1) {
                            if (ScaffoldModule.minecraft.thePlayer.onGround) {
                                Vec3i edgeDirection = towerFacing.getDirectionVec();
                                double approachDistance = Math.min(this.randomEdgeApproachDistance(), edgeDistance - 0.05);
                                double edgePadding = RandomUtils.randomDouble(0.02, 0.03);
                                AxisAlignedBB shiftedBounds = ScaffoldModule.minecraft.thePlayer.getEntityBoundingBox().offset((double)edgeDirection.getX() * (approachDistance - edgePadding), 0.0, (double)edgeDirection.getZ() * (approachDistance - edgePadding));
                                if (ScaffoldModule.minecraft.theWorld.getCollidingBoundingBoxes((Entity)ScaffoldModule.minecraft.thePlayer, shiftedBounds).isEmpty()) {
                                    ScaffoldModule.minecraft.thePlayer.motionY = -0.0784000015258789;
                                    ScaffoldModule.minecraft.thePlayer.setPosition(shiftedBounds.minX + (shiftedBounds.maxX - shiftedBounds.minX) / 2.0, shiftedBounds.minY, shiftedBounds.minZ + (shiftedBounds.maxZ - shiftedBounds.minZ) / 2.0);
                                }
                                return;
                            }
                        } else {
                            this.towerPhase = 2;
                            this.pendingPlacementFace = towerFacing;
                            ScaffoldModule.minecraft.thePlayer.motionY = 0.42f;
                        }
                        return;
                    }
                    this.towerPhase = 2;
                    ++this.towerCycleCount;
                    ScaffoldModule.minecraft.thePlayer.motionY = 0.42f;
                    MovementUtils.setHorizontalMotion(MovementUtils.horizontalSpeed(), MovementUtils.movementYaw());
                    return;
                }
                this.towerPhase = 0;
                this.towerCycleCount = 0;
                return;
            }
            case 2: {
                this.towerPhase = 3;
                ScaffoldModule.minecraft.thePlayer.motionY -= RandomUtils.randomDouble(0.00101, 0.00109);
                return;
            }
            case 3: {
                if (this.towerCycleCount >= 4) {
                    this.towerPhase = 4;
                    this.towerCycleCount = 0;
                } else {
                    this.towerPhase = 1;
                    ScaffoldModule.minecraft.thePlayer.motionY = 1.0 - ScaffoldModule.minecraft.thePlayer.posY % 1.0;
                }
                return;
            }
            case 4: {
                this.towerPhase = 5;
                return;
            }
            case 5: {
                if (!PlayerUtils.hasCollisionBelow()) {
                    this.towerPhase = 0;
                } else {
                    this.towerPhase = 1;
                    ScaffoldModule.minecraft.thePlayer.motionY -= 0.08;
                    ScaffoldModule.minecraft.thePlayer.motionY *= (double)0.98f;
                    ScaffoldModule.minecraft.thePlayer.motionY -= 0.08;
                    ScaffoldModule.minecraft.thePlayer.motionY *= (double)0.98f;
                }
                return;
            }
        }
        this.towerPhase = 0;
        this.towerCycleCount = 0;
    }

    private void drawBlockCounter(int blockCount) {
        HUDModule hud = (HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class);
        float hudScale = ((Float)hud.scale.getValue()).floatValue();
        GlStateManager.pushMatrix();
        GlStateManager.scale((float)hudScale, (float)hudScale, (float)0.0f);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc((int)770, (int)771);
        FontRenderer fontRenderer = ScaffoldModule.minecraft.fontRendererObj;
        String counterText = String.format("%d block%s left", blockCount, blockCount != 1 ? "s" : "");
        ScaledResolution resolution = new ScaledResolution(minecraft);
        float textX = ((float)resolution.getScaledWidth() / 2.0f + (float)fontRenderer.FONT_HEIGHT * 1.5f) / hudScale;
        float textY = (float)resolution.getScaledHeight() / 2.0f / hudScale - (float)fontRenderer.FONT_HEIGHT / 2.0f + 1.0f;
        int textColor = blockCount > 0 ? 0xFFFFFF : 0xFF5555;
        fontRenderer.drawString(counterText, textX, textY, textColor | 0xBF000000, ((Boolean)hud.shadow.getValue()).booleanValue());
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }
}


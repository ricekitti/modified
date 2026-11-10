/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockContainer
 *  net.minecraft.block.BlockFalling
 *  net.minecraft.block.BlockTNT
 *  net.minecraft.block.BlockWorkbench
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.ItemBlock
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.util.EnumFacing$Axis
 *  net.minecraft.util.MathHelper
 *  net.minecraft.util.MovementInput
 *  net.minecraft.util.MovingObjectPosition
 *  net.minecraft.util.MovingObjectPosition$MovingObjectType
 *  net.minecraft.util.Vec3
 *  net.minecraft.util.Vec3i
 *  net.minecraft.world.World
 */
package com.rice.blockin;

import com.rice.Rice;
import com.rice.antipearl.core.RotationManager;
import com.rice.config.Configuration;
import com.rice.config.Property;
import com.rice.core.event.RenderGameOverlayEvent;
import com.rice.core.event.SubscribeEvent;
import com.rice.core.event.TickEvent;
import com.rice.displace.WireRotation;
import com.rice.gui.ProgressBar;
import com.rice.util.Claims;
import com.rice.util.GameAccess;
import com.rice.util.PlaceWatch;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;

public final class BlockIn {
    public static final String CAT = "blockin";
    private static final double REACH = 4.5;
    private static final double CENTRE = 0.2;
    private static final int MAX_WAY = 8;
    private int wayPlaced;
    private static final EnumFacing[] AROUND = new EnumFacing[]{EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST};
    private static BlockIn instance;
    private final Property enabled;
    private final Property speed;
    private final Property requireGround;
    private final Property progressBar;
    private boolean wasOn;
    private BlockPos feet;
    private int startSide;
    private int previousSlot = -1;
    private boolean centring;
    private BlockPos clickPos;
    private EnumFacing clickFace;
    private Vec3 clickVec;

    public BlockIn(Configuration configuration) {
        instance = this;
        Claims.register(this, 85, new Claims.Holder(){

            @Override
            public boolean holds(Claims.Resource resource) {
                return BlockIn.this.enabled.getBoolean() && BlockIn.this.feet != null;
            }
        });
        this.enabled = configuration.get(CAT, "Enabled", false, "Closes you in with blocks while its bind is held; let go and it stops at once.");
        this.speed = configuration.get(CAT, "Speed", 1.0, "How much of the way to each block it turns a tick (1 = at once).", 0.1, 1.0);
        this.requireGround = configuration.get(CAT, "Require ground", true, "Only standing on the ground.");
        this.progressBar = configuration.get(CAT, "Show progress bar", true, "While it works: how many of the blocks round you are in place.");
    }

    public Property progressBarProp() {
        return this.progressBar;
    }

    private int[] progress(Minecraft mc) {
        if (this.feet == null || mc.thePlayer == null || mc.theWorld == null) {
            return null;
        }
        AxisAlignedBB box = mc.thePlayer.getEntityBoundingBox();
        int filled = 0;
        int all = 0;
        ArrayList<BlockPos> cells = new ArrayList<BlockPos>();
        for (int level = 0; level < 2; ++level) {
            for (EnumFacing side : AROUND) {
                cells.add(this.feet.offset(side).up(level));
            }
        }
        cells.add(this.feet.up(2));
        for (BlockPos cell : cells) {
            boolean filledNow;
            boolean bl = filledNow = !BlockIn.open(mc, cell);
            if (!filledNow && box.intersectsWith(new AxisAlignedBB(cell, cell.add(1, 1, 1)))) continue;
            ++all;
            filled += filledNow ? 1 : 0;
        }
        return new int[]{filled, all};
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || !this.enabled.getBoolean() || !this.progressBar.getBoolean() || mc.gameSettings.showDebugInfo) {
            return;
        }
        int[] p = this.progress(mc);
        if (p == null || p[1] == 0) {
            return;
        }
        ProgressBar.draw(event.resolution, "Block In " + p[0] + "/" + p[1] + (this.centring ? " centring" : ""), (float)p[0] / (float)p[1]);
    }

    public Property enabledProp() {
        return this.enabled;
    }

    public Property speedProp() {
        return this.speed;
    }

    public Property requireGroundProp() {
        return this.requireGround;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase != TickEvent.Phase.START || event.player != mc.thePlayer || mc.theWorld == null) {
            return;
        }
        EntityPlayerSP player = mc.thePlayer;
        boolean on = this.enabled.getBoolean();
        if (on != this.wasOn) {
            this.wasOn = on;
            if (on) {
                this.begin(player);
            } else {
                this.end(player);
            }
        }
        if (!on) {
            return;
        }
        if (this.requireGround.getBoolean() && !player.onGround && this.feet == null) {
            this.enabled.set(false);
            return;
        }
        if (mc.currentScreen != null || Claims.blocked(this, Claims.Resource.LOOK)) {
            return;
        }
        int slot = BlockIn.blockSlot(player);
        if (slot < 0) {
            this.enabled.set(false);
            return;
        }
        if (player.inventory.currentItem != slot) {
            player.inventory.currentItem = slot;
            GameAccess.syncHeldItem(mc.playerController);
        }
        if (player.onGround || this.feet == null) {
            this.feet = new BlockPos(player.posX, player.posY + 0.01, player.posZ);
        }
        this.centring = !BlockIn.centred(player);
        List<BlockPos[]> todo = this.plan(mc);
        if (todo.isEmpty()) {
            if (!this.centring) {
                this.enabled.set(false);
            }
            return;
        }
        for (BlockPos[] step : todo) {
            float[] look = this.aim(mc, step[0]);
            if (look == null) continue;
            float sentYaw = RotationManager.get().getSentYaw();
            float sentPitch = RotationManager.get().getSentPitch();
            float share = (float)this.speed.getDouble(1.0);
            float yaw = sentYaw + MathHelper.wrapAngleTo180_float((float)(look[0] - sentYaw)) * share;
            float pitch = sentPitch + (look[1] - sentPitch) * share;
            if (!RotationManager.get().aim(this, 85, yaw, pitch, true)) {
                return;
            }
            WireRotation.ensurePlaced();
            MovingObjectPosition over = BlockIn.trace(mc, player.getPositionEyes(1.0f), yaw, pitch);
            if (over != null && over.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && over.getBlockPos().equals((Object)this.clickPos) && over.sideHit == this.clickFace && BlockIn.buildingBlock(player.getHeldItem())) {
                RotationManager.get().notePlacing();
                if (mc.playerController.onPlayerRightClick(player, mc.theWorld, player.getHeldItem(), this.clickPos, this.clickFace, over.hitVec)) {
                    PlaceWatch.placed("Block In", this.clickPos.offset(this.clickFace));
                    if (this.clickPos.offset(this.clickFace).getY() >= this.feet.getY() + 2 && !this.clickPos.offset(this.clickFace).equals((Object)this.feet.up(2))) {
                        ++this.wayPlaced;
                    }
                    player.swingItem();
                }
            }
            return;
        }
        if (!this.centring && this.onlyTopLeft(todo) && !Rice.instance.getBinds().heldDown(Rice.instance.getBinds().blockIn())) {
            this.enabled.set(false);
        }
    }

    private boolean onlyTopLeft(List<BlockPos[]> todo) {
        for (BlockPos[] step : todo) {
            if (step[0].getY() >= this.feet.getY() + 2) continue;
            return false;
        }
        return true;
    }

    private void begin(EntityPlayerSP player) {
        this.wayPlaced = 0;
        this.previousSlot = player.inventory.currentItem;
        this.startSide = MathHelper.floor_double((double)((double)(player.rotationYaw / 90.0f) + 0.5)) & 3;
        this.feet = null;
        this.clickPos = null;
    }

    private void end(EntityPlayerSP player) {
        this.feet = null;
        this.clickPos = null;
        this.centring = false;
        if (player != null && this.previousSlot >= 0 && this.previousSlot != player.inventory.currentItem) {
            player.inventory.currentItem = this.previousSlot;
        }
        this.previousSlot = -1;
    }

    private List<BlockPos[]> plan(Minecraft mc) {
        ArrayList<BlockPos[]> out = new ArrayList<BlockPos[]>();
        AxisAlignedBB box = mc.thePlayer.getEntityBoundingBox();
        for (int level = 0; level < 2; ++level) {
            for (int i = 0; i < 4; ++i) {
                BlockPos spot = this.feet.offset(AROUND[this.startSide + i & 3]).up(level);
                if (!BlockIn.open(mc, spot) || box.intersectsWith(new AxisAlignedBB(spot, spot.add(1, 1, 1)))) continue;
                out.add(new BlockPos[]{spot});
            }
        }
        BlockPos top = this.feet.up(2);
        if (BlockIn.open(mc, top)) {
            out.add(new BlockPos[]{top});
            boolean beside = false;
            for (EnumFacing side : EnumFacing.values()) {
                if (side == EnumFacing.DOWN || BlockIn.open(mc, top.offset(side))) continue;
                beside = true;
            }
            if (!beside) {
                for (BlockPos cell : this.wayToTop(mc, top, box)) {
                    out.add(new BlockPos[]{cell});
                }
            }
        }
        return out;
    }

    private List<BlockPos> wayToTop(Minecraft mc, BlockPos top, AxisAlignedBB box) {
        ArrayList<BlockPos> found = new ArrayList<BlockPos>();
        if (this.wayPlaced >= 8) {
            return found;
        }
        Vec3 eye = mc.thePlayer.getPositionEyes(1.0f);
        HashSet<BlockPos> seen = new HashSet<BlockPos>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<BlockPos>();
        seen.add(top);
        queue.add(top);
        while (!queue.isEmpty() && found.size() < 300) {
            BlockPos at = (BlockPos)queue.poll();
            for (EnumFacing side : EnumFacing.values()) {
                boolean ownColumn;
                BlockPos next = at.offset(side);
                if (seen.contains(next) || Math.abs(next.getX() - this.feet.getX()) > 4 || Math.abs(next.getZ() - this.feet.getZ()) > 4 || next.getY() < top.getY() || next.getY() > top.getY() + 2) continue;
                if (eye.squareDistanceTo(new Vec3((Vec3i)next).addVector(0.5, 0.5, 0.5)) > 29.160000000000004) continue;
                seen.add(next);
                boolean bl = ownColumn = next.getX() == this.feet.getX() && next.getZ() == this.feet.getZ() && next.getY() < top.getY();
                if (ownColumn || !BlockIn.open(mc, next) || box.intersectsWith(new AxisAlignedBB(next, next.add(1, 1, 1)))) continue;
                found.add(next);
                queue.add(next);
            }
        }
        return found;
    }

    private float[] aim(Minecraft mc, BlockPos spot) {
        EntityPlayerSP player = mc.thePlayer;
        Vec3 eye = player.getPositionEyes(1.0f);
        float sentYaw = RotationManager.get().getSentYaw();
        float sentPitch = RotationManager.get().getSentPitch();
        float[] best = null;
        double bestCost = Double.MAX_VALUE;
        MovingObjectPosition bestHit = null;
        for (EnumFacing face : EnumFacing.values()) {
            BlockPos support = spot.offset(face.getOpposite());
            if (BlockIn.open(mc, support) || !((ItemBlock)player.getHeldItem().getItem()).canPlaceBlockOnSide((World)mc.theWorld, support, face, (EntityPlayer)player, player.getHeldItem())) continue;
            for (double a = 0.15; a <= 0.86; a += 0.175) {
                for (double b = 0.15; b <= 0.86; b += 0.175) {
                    MovingObjectPosition hit;
                    Vec3 point = BlockIn.facePoint(support, face, a, b);
                    if (eye.squareDistanceTo(point) > 20.25) continue;
                    double dx = point.xCoord - eye.xCoord;
                    double dy = point.yCoord - eye.yCoord;
                    double dz = point.zCoord - eye.zCoord;
                    float yaw = sentYaw + MathHelper.wrapAngleTo180_float((float)((float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f - sentYaw));
                    float pitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
                    double cost = Math.abs(yaw - sentYaw) + Math.abs(pitch - sentPitch);
                    if (cost >= bestCost || (hit = BlockIn.trace(mc, eye, yaw, pitch)) == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !hit.getBlockPos().equals((Object)support) || hit.sideHit != face) continue;
                    best = new float[]{yaw, pitch};
                    bestCost = cost;
                    bestHit = hit;
                }
            }
        }
        if (best != null) {
            this.clickPos = bestHit.getBlockPos();
            this.clickFace = bestHit.sideHit;
            this.clickVec = bestHit.hitVec;
        }
        return best;
    }

    public static void adjustInput(MovementInput input) {
        double dz;
        BlockIn self = instance;
        Minecraft mc = Minecraft.getMinecraft();
        if (self == null || !self.enabled.getBoolean() || mc.thePlayer == null) {
            return;
        }
        if (!self.centring) {
            return;
        }
        EntityPlayerSP player = mc.thePlayer;
        double dx = Math.floor(player.posX) + 0.5 - player.posX;
        double length = Math.sqrt(dx * dx + (dz = Math.floor(player.posZ) + 0.5 - player.posZ) * dz);
        if (length < 0.001) {
            return;
        }
        input.jump = false;
        input.sneak = false;
        double speed = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
        if (length <= speed * 0.546 / 0.454 + 0.02) {
            input.moveForward = 0.0f;
            input.moveStrafe = 0.0f;
            return;
        }
        RotationManager rotations = RotationManager.get();
        float moveYaw = rotations.shouldFixMovement() ? rotations.getYaw() : player.rotationYaw;
        float towards = (float)Math.toDegrees(Math.atan2(-dx, dz));
        float[] keys = RotationManager.correctedInput(towards, moveYaw, 1.0f, 0.0f, false);
        if (keys == null) {
            return;
        }
        input.moveForward = keys[0];
        input.moveStrafe = keys[1];
    }

    private static boolean centred(EntityPlayerSP player) {
        double fx = player.posX - Math.floor(player.posX);
        double fz = player.posZ - Math.floor(player.posZ);
        return Math.abs(fx - 0.5) <= 0.2 && Math.abs(fz - 0.5) <= 0.2;
    }

    private static Vec3 facePoint(BlockPos pos, EnumFacing face, double a, double b) {
        double x = (double)pos.getX() + 0.5 + (double)face.getFrontOffsetX() * 0.5;
        double y = (double)pos.getY() + 0.5 + (double)face.getFrontOffsetY() * 0.5;
        double z = (double)pos.getZ() + 0.5 + (double)face.getFrontOffsetZ() * 0.5;
        if (face.getAxis() == EnumFacing.Axis.Y) {
            x = (double)pos.getX() + a;
            z = (double)pos.getZ() + b;
        } else if (face.getAxis() == EnumFacing.Axis.X) {
            y = (double)pos.getY() + a;
            z = (double)pos.getZ() + b;
        } else {
            x = (double)pos.getX() + a;
            y = (double)pos.getY() + b;
        }
        return new Vec3(x, y, z);
    }

    private static MovingObjectPosition trace(Minecraft mc, Vec3 eye, float yaw, float pitch) {
        float cosYaw = MathHelper.cos((float)(-yaw * ((float)Math.PI / 180) - (float)Math.PI));
        float sinYaw = MathHelper.sin((float)(-yaw * ((float)Math.PI / 180) - (float)Math.PI));
        float cosPitch = -MathHelper.cos((float)(-pitch * ((float)Math.PI / 180)));
        float sinPitch = MathHelper.sin((float)(-pitch * ((float)Math.PI / 180)));
        Vec3 end = eye.addVector((double)(sinYaw * cosPitch) * 4.5, (double)sinPitch * 4.5, (double)(cosYaw * cosPitch) * 4.5);
        return mc.theWorld.rayTraceBlocks(eye, end, false, false, false);
    }

    private static boolean open(Minecraft mc, BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock().isReplaceable((World)mc.theWorld, pos);
    }

    private static int blockSlot(EntityPlayerSP player) {
        if (BlockIn.buildingBlock(player.inventory.getCurrentItem())) {
            return player.inventory.currentItem;
        }
        int best = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (!BlockIn.buildingBlock(stack) || best >= 0 && stack.stackSize <= player.inventory.mainInventory[best].stackSize) continue;
            best = i;
        }
        return best;
    }

    private static boolean buildingBlock(ItemStack stack) {
        if (stack == null || stack.stackSize <= 0 || !(stack.getItem() instanceof ItemBlock)) {
            return false;
        }
        Block block = ((ItemBlock)stack.getItem()).getBlock();
        return block.isFullCube() && block.getMaterial().isSolid() && !(block instanceof BlockTNT) && !(block instanceof BlockFalling) && !(block instanceof BlockContainer) && !(block instanceof BlockWorkbench);
    }
}

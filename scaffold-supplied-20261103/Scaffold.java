package com.snowy.module.player.scaffold;

import com.snowy.config.Configuration;
import com.snowy.config.Property;
import com.snowy.core.event.RenderGameOverlayEvent;
import com.snowy.core.event.RenderWorldLastEvent;
import com.snowy.core.event.SubscribeEvent;
import com.snowy.core.event.TickEvent;
import com.snowy.module.Mod;
import com.snowy.rotation.AngleUtils;
import com.snowy.rotation.RotationManager;
import com.snowy.util.Claims;
import com.snowy.util.GameAccess;
import com.snowy.util.WorldDraw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Scaffold, as Myau and Jade build it: blocks put under you as you walk.
 *
 * <p>The look, as Myau's rotations: Default looks straight at where each block goes - of the points on the face
 * of the block a placement goes against, the one nearest the look it already has, so it turns as little as it can -
 * its yaw held to whole 45-degree steps off the way you walk (Jade's snap), so the keys under it walk you straight
 * and the line never drifts aside.
 * Backwards and Sideways hold it where a bridging player looks - straight back along the way you walk, or back and
 * to the side - and work out only the pitch, so the yaw never wanders. Three ticks of turning come first when it
 * starts.
 *
 * <p>The packet order (as Clutch Assist's): the server judges a click by the look of the last movement packet it
 * has, and a tick's click goes out before that tick's movement packet. So a block goes down only where the look
 * sent last tick already reaches the face, and is clicked with what that look reaches; the look itself is worked
 * out from where your eyes will be after the tick's move, for the spot you will stand over then - so it is there
 * the tick that spot opens, and the block goes down at once.
 *
 * <p>The block it goes against is the nearest one (within reach) beside or under an open spot that leads to the
 * spot under your feet - so over a gap it builds its way back to you.
 *
 * <ul>
 * <li><b>Keep Y</b> (Myau's keep-Y telly): it jumps for you as you walk, looking forward on the ground and
 *     turning back in the air to place, always at the level you set off from - sprint-jump bridging. With
 *     <b>Keep Y on right click</b>, only while you hold right click.</li>
 * <li><b>Tower</b> (Myau's tower telly): holding jump as you walk, the same - each jump builds you a step up.</li>
 * <li><b>Safe walk</b>: the game's crouch edge-stop without crouching, so you never walk off before the block
 *     is down.</li>
 * </ul>
 */
public final class Scaffold extends Mod {

    public static final int PRIORITY = 80;
    public static final String DEFAULT = "Default";
    public static final String BACKWARDS = "Backwards";
    public static final String SIDEWAYS = "Sideways";
    /**
     * Of Myau's sixteen points across a face (0.03125 to 0.96875), the ten not near its edges - so the least turn
     * lands well inside the face. The least turn to one of all sixteen is mostly right at an edge, and a look there
     * only grazes the face: a server working the ray out a hair differently (where it has your eyes, its sine)
     * sees it miss. Clutch Assist, which places without a flag, clicks near the middle too.
     */
    private static final double[] INNER = {0.21875D, 0.28125D, 0.34375D, 0.40625D, 0.46875D, 0.53125D, 0.59375D,
            0.65625D, 0.71875D, 0.78125D};
    /**
     * How far inside the face's edges a look must land: one worked out lands this far in, and a click goes ahead
     * once the look the server has, from where it has you, lands CLICK_MARGIN in - a little less, for where your
     * eyes turned out to be after the move it was worked out for.
     */
    private static final double MARGIN = 0.15D;
    private static final double CLICK_MARGIN = 0.1D;

    private final Property rotation;
    private final Property keepY;
    private final Property keepYOnUse;
    private final Property tower;
    private final Property multiPlace;
    private final Property swing;
    private final Property autoSwitch;
    private final Property sprint;
    private final Property safeWalk;
    private final Property counter;
    private final Property showTarget;
    private final Property glow;

    private final BlockCounter blocks = new BlockCounter();
    private final Random random = new Random();

    /** The look it keeps (the pitch worked out, the yaw held back); unset until the first tick. */
    private float yaw;
    private float pitch;
    private boolean looked;
    private boolean found;
    /** Ticks before it may place: three when it starts, and after a telly turn forward. */
    private int rotationDelay;
    private int startY;
    /** Where you stood at the last tick's start: how far a tick moves you, to work the next look out from. */
    private double lastX;
    private double lastZ;
    /** Myau's keep-Y stage: 1 while Keep Y builds at startY (set on the ground, kept through the jump). */
    private int stage;
    private boolean tellying;
    private float tellyPitch;
    private boolean aiming;
    private int returnLeft;
    private int previousSlot = -1;
    /** The slot it builds from, held there while it builds; -1 when not building. */
    private int heldSlot = -1;
    private boolean sneakForced;
    /** The spot it is building to now, for drawing. */
    private BlockPos spot;

    /** Where a block goes: against this block, on this face, and the point there it clicks. */
    private static final class Target {
        final BlockPos against;
        final EnumFacing face;
        Vec3 hit;
        float yaw;
        float pitch;

        Target(BlockPos against, EnumFacing face) {
            this.against = against;
            this.face = face;
        }

        BlockPos spot() {
            return against.offset(face);
        }
    }

    public Scaffold(Configuration configuration) {
        super(configuration, "Scaffold", "Player", "Places blocks under you as you walk.");
        Claims.register(this, PRIORITY, new Claims.Holder() {
            @Override
            public boolean holds(Claims.Resource resource) {
                return aiming && resource != Claims.Resource.ATTACK;
            }
        });
        configuration.getCategory(cat).remove("Turn speed"); // the first version's
        configuration.getCategory(cat).remove("Delay");
        configuration.getCategory(cat).remove("Telly"); // a toggle of its own before: Keep Y and Tower are telly now
        rotation = mode("Rotation", "Mode", BACKWARDS, new String[] {DEFAULT, BACKWARDS, SIDEWAYS},
                "Which way you face. Default: toward each placement, in 45\u00b0 steps. Backwards: straight back "
                + "and down. Sideways: back and to the side.");
        keepY = bool("Building", "Keep Y", false, "Jumps for you and places at your starting height (telly).");
        keepYOnUse = bool("Building", "Keep Y on right click", false, "Keep Y only while holding right click.");
        shownWhen(keepYOnUse, whenOn(keepY));
        tower = bool("Building", "Tower", false, "Builds up while you hold jump.");
        multiPlace = bool("Building", "Multi place", true, "Places two blocks in one tick when it can.");
        swing = bool("Placing", "Swing", true, "Swings your arm (off: server only).");
        autoSwitch = bool("Placing", "Auto switch", true, "Switches to blocks from your hotbar, then back.");
        sprint = bool("Movement", "Sprint", false, "Lets you sprint (strict anticheats flag it).");
        safeWalk = bool("Movement", "Safe walk", true, "Stops you walking off edges.");
        counter = bool("Display", "Block count", true, "Shows your block count under the crosshair.");
        showTarget = bool("Display", "Show target", true, "A box where the next block goes.");
        glow = bool("Display", "Glow", true, "The box and block count glow.");
    }

    @Override
    public String suffix() {
        return rotation.getString();
    }

    @Override
    protected void onEnable() {
        Minecraft mc = Minecraft.getMinecraft();
        startY = mc.thePlayer != null ? MathHelper.floor_double(mc.thePlayer.posY) : 256;
        lastX = mc.thePlayer != null ? mc.thePlayer.posX : 0.0D;
        lastZ = mc.thePlayer != null ? mc.thePlayer.posZ : 0.0D;
        previousSlot = -1;
        heldSlot = -1;
        returnLeft = 0;
        rotationDelay = 3;
        stage = 0;
        looked = false;
        found = false;
        tellying = false;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        if (event.phase != TickEvent.Phase.START || event.player != player || player == null || mc.theWorld == null) {
            return;
        }
        boolean wasAiming = aiming;
        aiming = false;
        spot = null;
        RotationManager rotations = RotationManager.get();
        if (!on() || mc.currentScreen != null || Claims.blocked(this, Claims.Resource.LOOK)
                || player.capabilities.isFlying || Blocks.count(player) <= 0 || !holdBlocks(mc, player)) {
            turnBack(player, rotations, wasAiming);
            return;
        }
        returnLeft = 0;
        if (rotationDelay > 0) {
            rotationDelay--;
        }
        float sentYaw = rotations.getSentYaw();
        float sentPitch = rotations.getSentPitch();
        boolean moveKeys = moveKeys(mc);
        boolean jumpKey = mc.gameSettings.keyBindJump.isKeyDown();
        if (player.onGround) {
            // Myau's keep-Y stage: set again on each landing while Keep Y holds, so it lasts through the jump
            if (stage > 0) {
                stage--;
            }
            if (stage == 0 && keepY.getBoolean() && !jumpKey && (!keepYOnUse.getBoolean() || Mouse.isButtonDown(1)
                    || mc.gameSettings.keyBindUseItem.isKeyDown())) {
                stage = 1;
            }
            startY = MathHelper.floor_double(player.posY);
        }
        int floorY = MathHelper.floor_double(player.posY);
        int targetY = (stage > 0 ? Math.min(startY, floorY) : floorY) - 1;

        // telly: on the ground (about to jump) it faces forward and places nothing; in the air it places in Myau's
        // order (below)
        boolean tellyGround = player.onGround && moveKeys && hasHeadroom(mc, player)
                && (stage > 0 || tower.getBoolean() && jumpKey);
        boolean airTelly = tellying && !player.onGround;
        double nextX = player.posX + (player.posX - lastX);
        double nextZ = player.posZ + (player.posZ - lastZ);
        double nextY = player.posY + (player.onGround ? 0.0D : player.motionY);
        lastX = player.posX;
        lastZ = player.posZ;

        // The server judges a click by the look of the last movement packet it has - this tick's goes out after the
        // click. So a block goes down only where that look (sent last tick) already reaches one of the faces Myau's
        // search picks for the spot under you, from where you stand now, and is clicked with what it reaches there;
        // a look still on its way only turns this tick. A click is never judged against a look it was not made
        // with (the flag) - as Clutch Assist does it.
        double reach = mc.playerController.getBlockReachDistance();
        boolean placed = false;
        Target clicked = null;
        BlockPos spotNow = openSpot(mc, player.posX, player.posZ, targetY);
        if (spotNow != null && rotationDelay <= 0 && !tellyGround && !airTelly && wasAiming) { // the look last sent was ours
            MovingObjectPosition seen = Blocks.trace(mc, player.getPositionEyes(1.0F), sentYaw, sentPitch, reach);
            for (Target where : locate(mc, spotNow, player.posX, player.posY, player.posZ)) {
                if (landsInside(seen, where, CLICK_MARGIN)) {
                    where.hit = seen.hitVec;
                    placed = place(mc, player, where);
                    clicked = placed ? where : null;
                    break;
                }
            }
        }
        if (placed && multiPlace.getBoolean()) {
            // the spot you are heading for, the same tick, when the look the server has reaches it too
            BlockPos ahead = new BlockPos(MathHelper.floor_double(nextX), targetY, MathHelper.floor_double(nextZ));
            if (Blocks.open(mc, ahead)) {
                MovingObjectPosition hit = Blocks.trace(mc, player.getPositionEyes(1.0F), sentYaw, sentPitch, reach);
                if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                        && ahead.equals(hit.getBlockPos().offset(hit.sideHit)) && hit.sideHit != EnumFacing.DOWN) {
                    Target extra = new Target(hit.getBlockPos(), hit.sideHit);
                    extra.hit = hit.hitVec;
                    place(mc, player, extra);
                }
            }
        }

        // The look for this tick's packet: worked out from the eyes you will have once this tick's move is done
        // (where the server judges the next click from), for the spot you will stand over then - so it is there
        // before that spot opens, and the block goes down the tick it does - or, with nothing open there, the spot
        // under you if it is still open. On the ground a tick that clicked turns only to a look that, from where the
        // click was made, still runs onto the face clicked: the movement packets either side of a click both carry
        // a look on its face, whichever of the two a server judges it by; else it keeps the look, and turns the
        // tick after. In a telly jump's air, as Myau does it: the look for the spot under you now, from where you
        // are, and the click with it this same tick - before the movement packet that carries that look.
        Vec3 nextEye = airTelly ? player.getPositionEyes(1.0F) : new Vec3(nextX, nextY + player.getEyeHeight(), nextZ);
        BlockPos spotNext = airTelly ? spotNow : openSpot(mc, nextX, nextZ, targetY);
        if (spotNext == null && !airTelly) {
            spotNext = openSpot(mc, player.posX, player.posZ, targetY);
        }

        // the look: back along the way you walk (or back and to the side); Default starts from the side one, as Myau's
        String look = rotation.getString();
        float moveYaw = movementYaw(mc, player);
        float backYaw = near(moveYaw - 180.0F, sentYaw);
        float sideYaw = diagonal(moveYaw) ? backYaw
                : near(moveYaw - 135.0F * ((moveYaw + 180.0F) % 90.0F < 45.0F ? 1.0F : -1.0F), sentYaw);
        float baseYaw = BACKWARDS.equals(look) ? backYaw : sideYaw;
        if (!looked) {
            yaw = baseYaw;
            pitch = 85.0F;
            looked = true;
        } else if (!found) {
            yaw = baseYaw;
        }
        Target aimed = null;
        if (spotNext != null) {
            // Myau's: the nearest block's face; when no look reaches that one (a sliver of it from right above), the
            // next nearest, up to four
            for (Target where : airTelly ? locate(mc, spotNext, player.posX, player.posY, player.posZ)
                    : locate(mc, spotNext, nextX, nextY, nextZ)) {
                aimed = aim(mc, where, nextEye, near(yaw, sentYaw), pitch, DEFAULT.equals(look) ? moveYaw : Float.NaN);
                if (aimed != null) {
                    break;
                }
            }
        }
        boolean strict = clicked != null && !tellying;
        if (aimed != null && strict && !onFace(player.getPositionEyes(1.0F), aimed.yaw, aimed.pitch, reach, clicked)) {
            aimed = null; // would turn off the face just clicked: kept for this tick
        }
        if (aimed != null) {
            yaw = aimed.yaw;
            pitch = aimed.pitch;
            found = true;
            boolean moving = player.movementInput != null
                    && (player.movementInput.moveForward != 0.0F || player.movementInput.moveStrafe != 0.0F);
            if (moving && !DEFAULT.equals(look) && Math.abs(MathHelper.wrapAngleTo180_float(baseYaw - yaw)) < 90.0F
                    && landsInside(Blocks.trace(mc, nextEye, baseYaw, pitch, reach), aimed, MARGIN)) {
                yaw = baseYaw; // the yaw held exactly back; only the pitch follows the block (Myau)
            }
        } else if (placed) {
            yaw = sentYaw; // the look that clicked goes out again after the click
            pitch = sentPitch;
        }
        spot = aimed != null ? aimed.spot() : spotNow;
        float wantYaw = near(yaw, sentYaw);
        float wantPitch = pitch;

        // telly: on the ground, facing forward; in the air, turning back to place
        if (tellyGround) {
            if (!tellying) {
                tellyPitch = 30.0F + random.nextFloat() * 50.0F;
            }
            wantYaw = sentYaw + MathHelper.wrapAngleTo180_float(player.rotationYaw - sentYaw) * (0.98F + random.nextFloat() * 0.01F);
            wantPitch = tellyPitch;
            rotationDelay = 3;
            tellying = true;
        } else if (tellying && (player.motionY > 0.0D || player.posY > startY + 1)) {
            float turn = MathHelper.wrapAngleTo180_float(wantYaw - sentYaw);
            float most = rotationDelay >= 2 ? 90.0F + random.nextFloat() * 5.0F : 30.0F + random.nextFloat() * 5.0F;
            if (Math.abs(turn) > most) {
                wantYaw = sentYaw + MathHelper.clamp_float(turn, -most, most); // round in steps, not one snap
                rotationDelay = Math.max(rotationDelay, 1);
            }
        } else if (player.onGround) {
            tellying = false;
        }

        float sens = mc.gameSettings.mouseSensitivity;
        float sendYaw = AngleUtils.quantize(wantYaw, sentYaw, sens, true);
        float sendPitch = AngleUtils.clampPitch(AngleUtils.quantize(wantPitch, sentPitch, sens, false));
        if (strict && !onFace(player.getPositionEyes(1.0F), sendYaw, sendPitch, reach, clicked)) {
            sendYaw = sentYaw; // as it goes out (telly's step, the mouse grid) it would leave the face: kept
            sendPitch = sentPitch;
        }
        if (!rotations.aim(this, PRIORITY, sendYaw, sendPitch, true)) {
            return;
        }
        aiming = true;
        com.snowy.module.combat.displace.WireRotation.ensurePlaced();
        if (airTelly && spotNow != null && rotationDelay <= 0) {
            // Myau's order: the click with the look this tick's movement packet carries (it goes out after the click),
            // on the point that look meets from where you are; none while the turn back is still held to its step
            MovingObjectPosition seen = Blocks.trace(mc, player.getPositionEyes(1.0F), sendYaw, sendPitch, reach);
            for (Target where : locate(mc, spotNow, player.posX, player.posY, player.posZ)) {
                if (landsInside(seen, where, CLICK_MARGIN)) {
                    where.hit = seen.hitVec;
                    place(mc, player, where);
                    break;
                }
            }
        }
    }

    /** Blocks in hand: switched to from the hotbar (Auto switch) - false when there are none to hold. */
    private boolean holdBlocks(Minecraft mc, EntityPlayerSP player) {
        if (heldSlot >= 0 && player.inventory.currentItem != heldSlot) {
            // a slot you picked yourself (scroll, number key) while it builds: put back before the game tells the
            // server (that goes out at the next tick's start), and yours when Scaffold stops - as Myau holds it
            previousSlot = player.inventory.currentItem;
            player.inventory.currentItem = heldSlot;
        }
        if (Blocks.building(player.getHeldItem())) {
            heldSlot = player.inventory.currentItem;
            return true;
        }
        if (!autoSwitch.getBoolean()) {
            return false;
        }
        int slot = Blocks.biggestSlot(player);
        if (slot < 0) {
            return false;
        }
        if (previousSlot < 0) {
            previousSlot = player.inventory.currentItem;
        }
        player.inventory.currentItem = slot;
        GameAccess.syncHeldItem(mc.playerController);
        heldSlot = slot;
        return true;
    }

    /** Whether a look from these eyes runs onto this face of the block clicked (the block alone, as a server checks it). */
    private static boolean onFace(Vec3 eye, float yaw, float pitch, double reach, Target clicked) {
        float cosYaw = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float sinYaw = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float cosPitch = -MathHelper.cos(-pitch * 0.017453292F);
        float sinPitch = MathHelper.sin(-pitch * 0.017453292F);
        Vec3 end = eye.addVector(sinYaw * cosPitch * reach, sinPitch * reach, cosYaw * cosPitch * reach);
        MovingObjectPosition hit = new AxisAlignedBB(clicked.against, clicked.against.add(1, 1, 1)).calculateIntercept(eye, end);
        return hit != null && hit.sideHit == clicked.face && inside(hit.hitVec, clicked.against, clicked.face, CLICK_MARGIN);
    }

    /** Whether a trace met this target's face, this far or more inside its edges. */
    private static boolean landsInside(MovingObjectPosition hit, Target where, double margin) {
        return Blocks.hits(hit, where.against, where.face) && inside(hit.hitVec, where.against, where.face, margin);
    }

    /** Whether a point on a block's face is this far or more inside its edges. */
    private static boolean inside(Vec3 point, BlockPos block, EnumFacing face, double margin) {
        double x = point.xCoord - block.getX();
        double y = point.yCoord - block.getY();
        double z = point.zCoord - block.getZ();
        double a;
        double b;
        switch (face.getAxis()) {
            case X: a = y; b = z; break;
            case Y: a = x; b = z; break;
            default: a = x; b = y; break;
        }
        return a >= margin && a <= 1.0D - margin && b >= margin && b <= 1.0D - margin;
    }

    /** The way your keys walk you (your yaw turned by them), as Jade and Myau work it out. */
    private static float movementYaw(Minecraft mc, EntityPlayerSP player) {
        float yaw = player.rotationYaw;
        float forward = (mc.gameSettings.keyBindForward.isKeyDown() ? 1 : 0) - (mc.gameSettings.keyBindBack.isKeyDown() ? 1 : 0);
        float strafe = (mc.gameSettings.keyBindLeft.isKeyDown() ? 1 : 0) - (mc.gameSettings.keyBindRight.isKeyDown() ? 1 : 0);
        if (forward < 0.0F) {
            yaw += 180.0F;
        }
        float share = forward < 0.0F ? -0.5F : forward > 0.0F ? 0.5F : 1.0F;
        if (strafe > 0.0F) {
            yaw -= 90.0F * share;
        } else if (strafe < 0.0F) {
            yaw += 90.0F * share;
        }
        return MathHelper.wrapAngleTo180_float(yaw);
    }

    /** A walking key held that moves you (Myau's: forward against back, left against right). */
    private static boolean moveKeys(Minecraft mc) {
        return mc.gameSettings.keyBindForward.isKeyDown() != mc.gameSettings.keyBindBack.isKeyDown()
                || mc.gameSettings.keyBindLeft.isKeyDown() != mc.gameSettings.keyBindRight.isKeyDown();
    }

    private static boolean diagonal(float yaw) {
        float off = Math.abs(yaw % 90.0F);
        return off > 20.0F && off < 70.0F;
    }

    private static float near(float angle, float to) {
        return to + MathHelper.wrapAngleTo180_float(angle - to);
    }

    private static boolean hasHeadroom(Minecraft mc, EntityPlayerSP player) {
        return mc.theWorld.getCollidingBoundingBoxes(player, player.getEntityBoundingBox().offset(0.0D, 1.0D, 0.0D)).isEmpty();
    }

    /** The spot under these feet at this height, when it is open for a block; else null. */
    private static BlockPos openSpot(Minecraft mc, double feetX, double feetZ, int y) {
        BlockPos spot = new BlockPos(MathHelper.floor_double(feetX), y, MathHelper.floor_double(feetZ));
        return Blocks.open(mc, spot) ? spot : null;
    }

    /**
     * Where a block for the spot can go, as Myau's search has it, nearest first: the solid blocks within reach of
     * these feet - within four blocks of the spot, no higher - with an open side, each with the face (not the
     * bottom, none above the spot) whose side is nearest the spot. Myau takes only the first; the next three are
     * for when no look reaches that one's face.
     */
    private static List<Target> locate(Minecraft mc, final BlockPos spot, double feetX, double feetY, double feetZ) {
        double reach = mc.playerController.getBlockReachDistance();
        List<BlockPos> blocks = new ArrayList<BlockPos>();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 0; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos pos = spot.add(dx, dy, dz);
                    double cx = pos.getX() + 0.5D - feetX;
                    double cy = pos.getY() + 0.5D - feetY;
                    double cz = pos.getZ() + 0.5D - feetZ;
                    if (!Blocks.support(mc, pos) || Math.sqrt(cx * cx + cy * cy + cz * cz) > reach) {
                        continue;
                    }
                    for (EnumFacing face : EnumFacing.values()) {
                        if (face != EnumFacing.DOWN && Blocks.open(mc, pos.offset(face))) {
                            blocks.add(pos);
                            break;
                        }
                    }
                }
            }
        }
        Collections.sort(blocks, new Comparator<BlockPos>() { // a stable sort: on a tie, Myau's order
            @Override
            public int compare(BlockPos a, BlockPos b) {
                return Double.compare(a.distanceSqToCenter(spot.getX() + 0.5D, spot.getY() + 0.5D, spot.getZ() + 0.5D),
                        b.distanceSqToCenter(spot.getX() + 0.5D, spot.getY() + 0.5D, spot.getZ() + 0.5D));
            }
        });
        List<Target> found = new ArrayList<Target>();
        for (BlockPos against : blocks) {
            EnumFacing face = bestFace(against, spot);
            if (face != null) {
                found.add(new Target(against, face));
            }
            if (found.size() >= 4) {
                break;
            }
        }
        return found;
    }

    /** Of a block's faces (not its bottom, none above the spot), the one whose side is nearest the spot - its top on a tie. */
    private static EnumFacing bestFace(BlockPos against, BlockPos target) {
        EnumFacing best = null;
        double bestDist = 0.0D;
        for (EnumFacing face : EnumFacing.values()) {
            if (face == EnumFacing.DOWN) {
                continue;
            }
            BlockPos into = against.offset(face);
            if (into.getY() > target.getY()) {
                continue;
            }
            double dist = into.distanceSqToCenter(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);
            if (best == null || dist < bestDist || dist == bestDist && face == EnumFacing.UP) {
                best = face;
                bestDist = dist;
            }
        }
        return best;
    }

    /**
     * Of the ten by ten points well inside the face (INNER), the one reached from these eyes with the least turning
     * from the look it keeps - each point turned toward as Myau turns ({@link #turn}), so the look found lands a
     * little short of it now and then and differs from one block to the next; null if no turn reaches the face.
     * With steps (Default: the way you walk), the yaw is first held to whole 45-degree steps off it.
     */
    private Target aim(Minecraft mc, Target where, Vec3 eye, float fromYaw, float fromPitch, float steps) {
        if (!Float.isNaN(steps)) {
            // Default: only yaws a whole number of 45-degree steps off the way you walk (Jade's snap) - the keys
            // under that look then walk you exactly that way, so the line never drifts aside; the pitch follows the
            // block. Only when none of those reaches the face, any yaw.
            Target stepped = aimAt(mc, where, eye, fromYaw, fromPitch, steps);
            if (stepped != null) {
                return stepped;
            }
        }
        return aimAt(mc, where, eye, fromYaw, fromPitch, Float.NaN);
    }

    private Target aimAt(Minecraft mc, Target where, Vec3 eye, float fromYaw, float fromPitch, float steps) {
        BlockPos against = where.against;
        EnumFacing face = where.face;
        double[] xs = INNER;
        double[] ys = INNER;
        double[] zs = INNER;
        switch (face) {
            case NORTH: zs = new double[] {0.0D}; break;
            case SOUTH: zs = new double[] {1.0D}; break;
            case WEST: xs = new double[] {0.0D}; break;
            case EAST: xs = new double[] {1.0D}; break;
            case UP: ys = new double[] {1.0D}; break;
            default: ys = new double[] {0.0D}; break;
        }
        double reach = mc.playerController.getBlockReachDistance();
        Target best = null;
        float bestTurn = 0.0F;
        for (double x : xs) {
            for (double y : ys) {
                for (double z : zs) {
                    float[] look = turn(against.getX() + x - eye.xCoord, against.getY() + y - eye.yCoord,
                            against.getZ() + z - eye.zCoord, fromYaw, fromPitch);
                    if (!Float.isNaN(steps)) {
                        look[0] = near(steps + Math.round(MathHelper.wrapAngleTo180_float(look[0] - steps) / 45.0F) * 45.0F, fromYaw);
                    }
                    MovingObjectPosition hit = Blocks.trace(mc, eye, look[0], look[1], reach);
                    if (!landsInside(hit, where, MARGIN)) {
                        continue;
                    }
                    float turned = Math.abs(look[0] - fromYaw) + Math.abs(look[1] - fromPitch);
                    if (best == null || turned < bestTurn) {
                        bestTurn = turned;
                        best = new Target(against, face);
                        best.hit = hit.hitVec;
                        best.yaw = look[0];
                        best.pitch = look[1];
                    }
                }
            }
        }
        return best;
    }

    /**
     * Myau's turn toward a point (from your eyes, this far off): within a degree, none; else 95 to 100% of the way
     * (all the way half the time), on its 0.0096-degree grid.
     */
    private float[] turn(double dx, double dy, double dz, float fromYaw, float fromPitch) {
        double flat = Math.sqrt(dx * dx + dz * dz);
        float yawTurn = MathHelper.wrapAngleTo180_float((float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F - fromYaw);
        float pitchTurn = MathHelper.wrapAngleTo180_float((float) (-Math.atan2(dy, flat) * 180.0D / Math.PI) - fromPitch);
        yawTurn = Math.abs(yawTurn) <= 1.0F ? 0.0F : yawTurn * share();
        pitchTurn = Math.abs(pitchTurn) <= 1.0F ? 0.0F : pitchTurn * share();
        return new float[] {grid(fromYaw + yawTurn), grid(fromPitch + pitchTurn)};
    }

    /** How much of a turn Myau makes: 0.5 + 0.5 x (1 - a random 0 to 0.1, half the time 0). */
    private float share() {
        return 0.5F + 0.5F * (1.0F - MathHelper.clamp_float(random.nextFloat() * 0.2F - 0.1F, 0.0F, 1.0F));
    }

    private static float grid(float angle) {
        return (float) ((double) angle - (double) angle % (double) 0.0096F);
    }

    /** The click, on the point found. */
    private boolean place(Minecraft mc, EntityPlayerSP player, Target t) {
        if (!Blocks.building(player.getHeldItem()) || !Blocks.open(mc, t.against.offset(t.face))) {
            return false;
        }
        if (GameAccess.hittingBlock(mc.playerController)) {
            mc.playerController.resetBlockRemoving();
        }
        // Not notePlacing(): that has the wire force a look into this tick's movement packet - the same look again
        // when it has not turned (a packet the game never sends) - and then write your own view into the next one
        // that carries none, so the server had your camera's look between our blocks and judged the next click by
        // it: the flag. Our look reaches the packet the game's own way; only the wire's count of it is kept - the
        // look the click is made with: the last one aimed (on the ground, the one already sent; in a telly jump's
        // air, this tick's, aimed just before)
        com.snowy.module.combat.displace.WireRotation.notePlacementLook(RotationManager.get().getYaw(),
                RotationManager.get().getPitch());
        if (!mc.playerController.onPlayerRightClick(player, mc.theWorld, player.getHeldItem(), t.against, t.face, t.hit)) {
            return false;
        }
        if (swing.getBoolean()) {
            player.swingItem();
        } else {
            mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
        }
        com.snowy.util.PlaceWatch.placed("Scaffold", t.against.offset(t.face));
        com.snowy.module.combat.buffer.AutoBuffer.notePlacedBlock();
        return true;
    }

    /** Done: back to your own look over a few ticks, and the slot you held back. */
    private void turnBack(EntityPlayerSP player, RotationManager rotations, boolean justEnded) {
        if (justEnded) {
            returnLeft = 3;
            restoreSlot(Minecraft.getMinecraft());
        }
        if (returnLeft <= 0 || rotations.takenOver(this)) {
            returnLeft = 0;
            return;
        }
        float fromYaw = rotations.getSentYaw();
        float fromPitch = rotations.getSentPitch();
        float share = 1.0F / returnLeft--;
        float toYaw = fromYaw + MathHelper.wrapAngleTo180_float(player.rotationYaw - fromYaw) * share;
        float toPitch = fromPitch + (player.rotationPitch - fromPitch) * share;
        if (returnLeft > 0) {
            rotations.aim(this, RotationManager.PRIORITY_TURN_BACK, toYaw, toPitch, true);
        }
    }

    private void restoreSlot(Minecraft mc) {
        if (previousSlot >= 0 && mc.thePlayer != null) {
            mc.thePlayer.inventory.currentItem = previousSlot;
            GameAccess.syncHeldItem(mc.playerController);
        }
        previousSlot = -1;
        heldSlot = -1;
    }

    /** Keep Y's telly: the jump made for you as you walk, while its stage holds (Myau's). */
    @Override
    protected void onInput(MovementInput input) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player != null && aiming && stage > 0 && player.onGround && (input.moveForward != 0.0F || input.moveStrafe != 0.0F)) {
            input.jump = true;
        }
    }

    /** No sprint while building (unless Sprint is on, or a telly jump wants it); Safe walk's edge-stop. */
    @Override
    protected void onPreMove(EntityPlayerSP player) {
        if (aiming && !sprint.getBoolean() && !tellying && player.isSprinting()) {
            player.setSprinting(false);
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (aiming && safeWalk.getBoolean() && player.onGround && player.motionY <= 0.0D && !player.isOnLadder()
                && player.movementInput != null && !player.movementInput.sneak && !mc.gameSettings.keyBindJump.isKeyDown()) {
            player.movementInput.sneak = true; // the game stops a crouching player at an edge; only for this move
            sneakForced = true;
        }
    }

    @Override
    protected void onPostMove(EntityPlayerSP player) {
        if (sneakForced) {
            sneakForced = false;
            if (player.movementInput != null) {
                player.movementInput.sneak = false; // not crouching: the server never hears of it
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        BlockPos at = spot;
        if (!on() || !showTarget.getBoolean() || at == null) {
            return;
        }
        AxisAlignedBB box = WorldDraw.relative(new AxisAlignedBB(at, at.add(1, 1, 1)));
        int top = WorldDraw.themeTop();
        int bottom = WorldDraw.themeBottom();
        WorldDraw.begin(false);
        WorldDraw.fill(box, WorldDraw.withAlpha(top, 0x30), WorldDraw.withAlpha(bottom, 0x18));
        WorldDraw.box(box, top, bottom, 0.9F, glow.getBoolean());
        WorldDraw.end();
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.gameSettings.hideGUI) {
            return;
        }
        blocks.draw(mc, event.resolution, on() && counter.getBoolean(), glow.getBoolean());
    }

    @Override
    protected void onDisable() {
        // aiming is left for the next tick to see, so the look turns back smoothly from where it was
        spot = null;
        restoreSlot(Minecraft.getMinecraft());
    }
}

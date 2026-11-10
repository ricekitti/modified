package dev.rice.client.combat.openb4;

import dev.rice.client.ClientContext;
import dev.rice.client.RelationshipService;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.scoreboard.*;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.util.IChatComponent;
import dev.rice.inject.ReflectionHelper;
import dev.rice.module.impl.WeaponsModule;
import dev.rice.setting.*;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/** Host APIs used only by the imported OpenB4 aura. */
public final class B4AuraHost {
    public static ClientContext context;
    public static WeaponsModule weapons;
    public static SliderSetting aimClosestPoint, jitterYaw;
    private static float inputScale = 1.0F;
    public static void beginInput() { inputScale = 1.0F; }
    public static float inputScale() {
        // Rice runs before vanilla's item-use multiplier; OpenB4 redirects that multiplier.
        // Compensate only while packet AutoBlock owns input, so the final factor matches OpenB4.
        boolean vanillaSlow = mc().thePlayer.isUsingItem() && !mc().thePlayer.isRiding();
        return OpenB4KillAuraBackend.autoBlock.packet && OpenB4KillAuraBackend.serverBlocking && vanillaSlow ? inputScale / 0.2F : inputScale;
    }
    private static final ThreadLocal<Boolean> replay = new ThreadLocal<Boolean>();
    private static boolean animationOwned;
    private B4AuraHost() {}
    private static Minecraft mc() { return context.minecraft(); }
    public static boolean replaying() { return Boolean.TRUE.equals(replay.get()); }
    public static dev.rice.module.ModuleManager modules;
    public static boolean holdAuraAttack(Entity entity, boolean forced) { return false; }
    public static boolean skyWars() { ServerState.State state = ServerState.detect(); return state == ServerState.State.SKYWARS_GAME_SOLO || state == ServerState.State.SKYWARS_GAME_TEAM; }
    public static void reset() {
        PacketUtils.serverSlot = PacketUtils.previousServerSlot = PacketUtils.slotPost = PacketUtils.previousSlotPost = mc().thePlayer.inventory.currentItem;
        RotationManager.lastYaw = RotationManager.serverYaw = mc().thePlayer.rotationYaw;
        RotationManager.lastPitch = RotationManager.serverPitch = mc().thePlayer.rotationPitch;
    }
    public static void beginTick() {
        PacketUtils.slotChangesPost = 0; PacketUtils.lastUse = -1;
        if (OpenB4KillAuraBackend.resyncSlot) { PlayerUtils.sendSlot(mc().thePlayer.inventory.currentItem); OpenB4KillAuraBackend.resyncSlot = false; }
    }
    public static boolean packet(Packet<?> p) {
        // OpenB4 tracks attempts before module filters and tracks accepted packets afterward.
        PacketUtils.track(p);
        boolean allowed = OpenB4KillAuraBackend.instance.onSendPacket(p);
        if (allowed) {
            PacketUtils.trackPost(p);
            if (p instanceof C09PacketHeldItemChange) allowed = OpenB4KillAuraBackend.instance.allowHeldItemPacket();
        }
        return allowed;
    }
    public static void restore() {
        if (PacketUtils.serverSlot != mc().thePlayer.inventory.currentItem) PlayerUtils.sendSlot(mc().thePlayer.inventory.currentItem);
        if (animationOwned) { mc().thePlayer.clearItemInUse(); animationOwned = false; }
        KeyBinding.setKeyBindState(mc().gameSettings.keyBindUseItem.getKeyCode(), Mouse.isButtonDown(1));
    }
    public static void discard() {
        OpenB4KillAuraBackend.resetWorld(); PacketUtils.reset(); animationOwned = false;
    }
    public static void animate() {
        boolean fake = OpenB4KillAuraBackend.fakeBlocking && PlayerUtils.holdingSword();
        if (fake && !mc().thePlayer.isUsingItem()) { mc().thePlayer.setItemInUse(mc().thePlayer.getHeldItem(), 72000); animationOwned = true; }
        else if (!fake && animationOwned) { mc().thePlayer.clearItemInUse(); animationOwned = false; }
    }
    public static boolean placementBusy() {


        // Mayu KillAura stands down while Mayu BedNuker controls interactions; Rice's KillAura does the same.
        if(dev.rice.client.mayu.MayuHost.bedNukerControlling())return true;
        dev.rice.module.Module blockin=modules==null?null:modules.find("blockin");
        if(blockin instanceof dev.rice.module.impl.BlockInModule&&blockin.isEnabled()&&((dev.rice.module.impl.BlockInModule)blockin).active)return true;
        return mc().thePlayer.getHeldItem()!=null&&mc().thePlayer.getHeldItem().getItem() instanceof ItemBlock&&Mouse.isButtonDown(1);
    }
    public static boolean isMob(Entity e) { return e instanceof EntityMob || e instanceof EntityGolem || e instanceof EntitySlime; }
    public static boolean sameTeam(Entity e) { return (RelationshipService.flags(e) & 1) != 0 || e instanceof EntityLivingBase && ServerState.sameTeam((EntityLivingBase)e); }
    public static void slowdown() { inputScale = 0.2F; }

    public static final class SliderSetting {
        private final Setting<?> value;
        private final Object initial;
        public SliderSetting(Setting<?> value) { this.value = value; this.initial = value.get(); }
        public double getInput() { return value instanceof ChoiceSetting ? ((ChoiceSetting)value).index() : ((NumberSetting)value).doubleValue(); }
        public boolean isAtMin() { return getInput() == (value instanceof NumberSetting ? ((NumberSetting)value).getMinimum() : 0); }
        public boolean isNotDefault() { return !value.get().equals(initial); }
        public static void order(SliderSetting a, SliderSetting b) { if (b.getInput() < a.getInput()) ((NumberSetting)b.value).set(a.getInput()); }
    }
    public static final class ButtonSetting {
        private final BooleanSetting value;
        public ButtonSetting(BooleanSetting value) { this.value = value; }
        public boolean isToggled() { return value.isEnabled(); }
    }
    public static final class MouseEvent {
        public final int button; public final boolean buttonstate; private boolean cancelled;
        public MouseEvent(int button,boolean state){this.button=button;this.buttonstate=state;}
        public void setCanceled(boolean value){this.cancelled=value;}
        public boolean isCanceled(){return this.cancelled;}
    }
    public static final class RotationManager { public static float lastYaw, lastPitch, serverYaw, serverPitch; }
    public static final class MathUtils { public static int randomInt(int min, int max) { return ThreadLocalRandom.current().nextInt(min, max + 1); } }
    public static final class Overlay {
        public static boolean screenOpen() { return mc().currentScreen != null; }
        public static boolean ownScreen() { return !context.inputReady(); }
    }
    public static final class KeyUtils {
        public static int heldHotbarKey() { for (int i=0;i<9;i++) { int k=mc().gameSettings.keyBindsHotbar[i].getKeyCode(); if(k>0 && Keyboard.isKeyDown(k)) return i; } return -1; }
    }
    public static final class BlockUtils {
        public static Block getBlock(BlockPos pos) { return mc().theWorld.getBlockState(pos).getBlock(); }
    public static boolean isContainer(Block block) {
        return block instanceof BlockContainer || block == Blocks.crafting_table || block == Blocks.anvil;
    }
    public static boolean isInteractable(Block block) {
        if (isContainer(block) || block == Blocks.bed || block == Blocks.lever) {
            return true;
        }
        if (block instanceof BlockTrapDoor || block instanceof BlockFence || block instanceof BlockFenceGate) {
            return true;
        }
        if (!(block instanceof BlockDoor)) {
            return false;
        }
        return block != Blocks.iron_door;
    }
    }
    public static final class ItemUtils {
        public static boolean isSword(ItemStack s) { return s != null && s.getItem() instanceof ItemSword; }
    public static boolean isThrowable(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        Item item = itemStack.getItem();
        return item == Items.fire_charge || item == Items.ender_pearl || item == Items.ender_eye || item == Items.snowball || item == Items.egg || item == Items.experience_bottle || item == Items.nether_star || item == Items.potionitem && ItemPotion.isSplash(item.getMetadata(itemStack));
    }
    public static double attackDamage(ItemStack itemStack) {
        double d = 0.0;
        for (Map.Entry<String, AttributeModifier> entry : itemStack.getAttributeModifiers().entries()) {
            if (!entry.getKey().equals("generic.attackDamage")) continue;
            d = entry.getValue().getAmount();
            break;
        }
        int n = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack);
        int n2 = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, itemStack);
        return d + (double) n * 1.25 + (double) (n2 > 0 ? n2 * 4 - 1 : 0);
    }
    public static boolean sameItem(ItemStack a, ItemStack b) {
        Item item = a == null ? null : a.getItem();
        Item item2 = b == null ? null : b.getItem();
        return item == item2;
    }
    }
    public static final class PacketUtils {
        public static int serverSlot, previousServerSlot, slotPost, previousSlotPost, slotChangesPost, lastUse = -1;
        public static boolean sentAttack, sentCloseWindow, sentAnyPlacement, sentAnyDigging;
        public static void reset() {
            sentAttack = sentCloseWindow = sentAnyPlacement = sentAnyDigging = false;
            slotChangesPost = 0; lastUse = -1; lastVelocity = 0; sending = null;
        }
        public static void track(Packet<?> p) {
            if (p instanceof C03PacketPlayer) {
                sentAttack = sentCloseWindow = sentAnyPlacement = sentAnyDigging = false;
            } else if (p instanceof C02PacketUseEntity) {
                if (((C02PacketUseEntity)p).getAction() == C02PacketUseEntity.Action.ATTACK) sentAttack = true;
            } else if (p instanceof C08PacketPlayerBlockPlacement) {
                sentAnyPlacement = true;
                ItemStack stack = ((C08PacketPlayerBlockPlacement)p).getStack();
                if (stack != null && stack.getItem() == Items.fire_charge) PlayerUtils.lastFireball = System.currentTimeMillis();
                if (((C08PacketPlayerBlockPlacement)p).getPlacedBlockDirection() == 255) lastUse = 1;
            } else if (p instanceof C07PacketPlayerDigging) {
                sentAnyDigging = true;
                if (((C07PacketPlayerDigging)p).getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) lastUse = 2;
            } else if (p instanceof C09PacketHeldItemChange) {
                previousServerSlot = serverSlot; serverSlot = ((C09PacketHeldItemChange)p).getSlotId(); lastUse = 3;
            } else if (p instanceof C0DPacketCloseWindow) sentCloseWindow = true;
        }
        public static void trackPost(Packet<?> p) {
            if (p instanceof C09PacketHeldItemChange) {
                previousSlotPost = slotPost; slotPost = ((C09PacketHeldItemChange)p).getSlotId(); ++slotChangesPost;
            }
        }
        public static long lastVelocity;
        public static Packet<?> sending;
        public static void send(Packet<?> p) { Packet<?> old=sending; sending=p; try { mc().thePlayer.sendQueue.addToSendQueue(p); } finally { sending=old; } }
        public static void sendThroughLag(Packet<?> p) { trackPost(p); if (!(p instanceof C09PacketHeldItemChange) || OpenB4KillAuraBackend.instance.allowHeldItemPacket()) dev.rice.client.packet.NetworkSendAdapter.sendDirect(mc().thePlayer.sendQueue.getNetworkManager(), p); }
        public static boolean noAttackPlaceOrDig() { return !(sentAttack || sentAnyPlacement || sentAnyDigging); }
    }
    public static final class ClickUtils { public static void onAttackClick() {} }
    public static final class PlayerUtils {
        public static boolean sendingSlot;
        public static long lastFireball;
        public static boolean holdingSword() { return ItemUtils.isSword(mc().thePlayer.getHeldItem()); }
        public static boolean holdingWeapon() { return weapons.isWeapon(mc().thePlayer); }
        public static boolean isBlocking() { return OpenB4KillAuraBackend.fakeBlocking && holdingSword() || mc().thePlayer.isBlocking(); }
        public static boolean isInvisibleUnarmoured(EntityLivingBase e) { if (!e.isInvisible()) return false; for (int i=0;i<4;i++) if(e.getCurrentArmor(i)!=null) return false; return true; }
        public static boolean isFriend(EntityPlayer e) { return (RelationshipService.flags(e) & 4) != 0; }
        public static boolean isEnemy(EntityPlayer e) { return RelationshipService.hostile(e) && !sameTeam(e); }
        public static int currentSlot() { return mc().thePlayer.inventory.currentItem; }
        public static void setSlot(int slot, boolean send) { mc().thePlayer.inventory.currentItem = slot; if (send) sendSlot(slot); }
        public static void sendSlot(int slot) { sendingSlot=true; try { PacketUtils.send(new C09PacketHeldItemChange(slot)); } finally { sendingSlot=false; } }
        public static void swing(boolean silent, boolean unused) { if (silent) PacketUtils.send(new C0APacketAnimation()); else mc().thePlayer.swingItem(); }
        public static void attack(Entity e, boolean silent) { swing(silent,false); mc().playerController.attackEntity(mc().thePlayer,e); }
            public static boolean inFov(float fov, double x, double z) {
        fov = (float) ((double) fov * 0.5);
        double d = MathHelper.wrapAngleTo180_double((double) ((mc().thePlayer.rotationYaw - RotationUtils.yawTo(x, z)) % 360.0f));
        if (d > 0.0) {
            return d < (double) fov;
        }
        return d > (double) (-fov);
    }
    public static boolean inFov(float fov, Entity entity) {
        if (fov >= 360.0f) {
            return true;
        }
        Vec3 eyes = mc().thePlayer.getPositionEyes(1.0f);
        float f = entity.getCollisionBorderSize();
        AxisAlignedBB box = entity.getEntityBoundingBox().expand(f, f, f);
        if (box.isVecInside(eyes)) {
            return true;
        }
        return inFov(fov, entity.posX, entity.posZ);
    }
            public static boolean rayHits(Entity entity, AxisAlignedBB box, float yaw, float pitch, double range) {
        if (entity == null || !entity.canBeCollidedWith()) {
            return false;
        }
        Vec3 from = mc().thePlayer.getPositionEyes(1.0f);
        Vec3 dir = RotationUtils.getVectorForRotation(yaw, pitch);
        Vec3 to = from.addVector(dir.xCoord * range, dir.yCoord * range, dir.zCoord * range);
        float f = entity.getCollisionBorderSize();
        box = box.expand(f, f, f);
        return box.calculateIntercept(from, to) != null;
    }
    public static boolean rayHits(Entity entity, float yaw, float pitch, double range) {
        if (entity == null) {
            return false;
        }
        return rayHits(entity, entity.getEntityBoundingBox(), yaw, pitch, range);
    }
        public static boolean rayHits(Entity e,double range) { float[] r=RotationUtils.toEntityBox(e); return r!=null && rayHits(e,r[0],r[1],range); }
    }
    public static final class BotCheck {
        public static boolean isBot(Entity e) { return (RelationshipService.flags(e) & 2)!=0; }
        public static MovingObjectPosition rayTrace(Entity e,double range,double x,double y,double z,float yaw,float pitch) {
            if(e==null || !e.canBeCollidedWith()) return null;
            Vec3 from = new Vec3(x,y+mc().thePlayer.getEyeHeight(),z), dir = RotationUtils.getVectorForRotation(yaw,pitch);
            float border=e.getCollisionBorderSize(); AxisAlignedBB box=e.getEntityBoundingBox().expand(border,border,border);
            if (box.isVecInside(from)) return new MovingObjectPosition(e,from);
            return box.calculateIntercept(from,from.addVector(dir.xCoord*range,dir.yCoord*range,dir.zCoord*range));
        }
    }
    /** OpenB4 box aim, visible points and mouse-sensitivity quantization. */
    public static final class RotationUtils {
        public static float lastYawStep, lastPitchStep;
        private static float clampPitch(float pitch) { return MathHelper.clamp_float(pitch, -90.0F, 90.0F); }
        private static float[] playerRotation() { return new float[]{mc().thePlayer.rotationYaw, mc().thePlayer.rotationPitch}; }
        private static float[] currentRotation() { return new float[]{RotationManager.lastYaw, RotationManager.lastPitch}; }
    public static float[] toVec(Vec3 vec3) {
        double x = mc().thePlayer.posX;
        double y = mc().thePlayer.posY + (double) mc().thePlayer.getEyeHeight();
        double z = mc().thePlayer.posZ;
        if (vec3.xCoord == x && vec3.yCoord == y && vec3.zCoord == z) {
            return playerRotation();
        }
        return fromDelta(vec3.xCoord - x, vec3.yCoord - y, vec3.zCoord - z);
    }
    public static float[] fromDelta(double dx, double dy, double dz) {
        double d4 = Math.sqrt(dx * dx + dz * dz);
        float f = (float) (Math.atan2(dz, dx) * (double) 57.29578f) - 90.0f;
        float f2 = (float) (-(Math.atan2(dy, d4) * (double) 57.29578f));
        float[] current = currentRotation();
        f = current[0] + MathHelper.wrapAngleTo180_float(f - current[0]);
        f2 = clampPitch(current[1] + MathHelper.wrapAngleTo180_float(f2 - current[1]));
        return new float[]{f, f2};
    }
    public static float[] fixSensitivity(float yaw, float pitch, float lastYaw, float lastPitch, boolean jitter) {
        int n;
        float f5 = yaw - lastYaw;
        float f6 = Math.abs(f5);
        float f7 = pitch - lastPitch;
        float f8 = mc().gameSettings.mouseSensitivity * 0.6f + 0.2f;
        double d = (double) (f8 * f8 * f8) * 1.2;
        float f9 = lastYawStep = (float) ((double) Math.round((double) f5 / d) * d);
        float f10 = lastPitchStep = (float) ((double) Math.round((double) f7 / d) * d);
        yaw = lastYaw + f9;
        pitch = lastPitch + f10;
        if (jitter && (double) f6 >= d && (n = (int) B4AuraHost.jitterYaw.getInput()) > 0) {
            int n2 = MathUtils.randomInt(-n, n);
            yaw += (float) ((double) n2 * d);
        }
        return new float[]{yaw, clampPitch(pitch)};
    }
    public static float[] fixSensitivity(float yaw, float pitch, float lastYaw, float lastPitch) {
        return fixSensitivity(yaw, pitch, lastYaw, lastPitch, true);
    }
    public static float yawTo(double x, double z) {
        return yawBetween(mc().thePlayer.posX, mc().thePlayer.posZ, x, z);
    }
    public static float yawBetween(double fromX, double fromZ, double toX, double toZ) {
        double d7 = Math.atan2(toX - fromX, toZ - fromZ) * (double) 57.29578f;
        return (float) (-d7);
    }
    public static double yawDifference(Entity entity, boolean server) {
        float f2 = yawTo(entity.posX, entity.posZ);
        float f = server ? RotationManager.serverYaw : mc().thePlayer.rotationYaw;
        return MathHelper.wrapAngleTo180_double((double) (f2 - f));
    }
    public static double absYawDifference(Entity entity, boolean server) {
        return Math.abs(yawDifference(entity, server));
    }
    public static Vec3 getVectorForRotation(float yaw, float pitch) {
        float f3 = MathHelper.cos((yaw *= -((float) Math.PI / 180)) - (float) Math.PI);
        float f4 = MathHelper.sin(yaw - (float) Math.PI);
        float f5 = -MathHelper.cos(pitch *= -((float) Math.PI / 180));
        float f6 = MathHelper.sin(pitch);
        return new Vec3(f4 * f5, f6, f3 * f5);
    }
    public static float[] toEntityBox(Entity entity) {
        if (entity == null) {
            return null;
        }
        return toBox(entity, entity.getEntityBoundingBox());
    }
    public static float[] toBox(Entity entity, AxisAlignedBB box) {
        Vec3 eyes;
        if (entity == null) {
            return null;
        }
        float f = entity.getCollisionBorderSize();
        if ((box = box.expand(f, f, f)).isVecInside(eyes = mc().thePlayer.getPositionEyes(1.0f))) {
            return new float[]{mc().thePlayer.rotationYaw, mc().thePlayer.rotationPitch};
        }
        Vec3 p = aimPoint(eyes, box);
        return fromDelta(p.xCoord - eyes.xCoord, p.yCoord - eyes.yCoord, p.zCoord - eyes.zCoord);
    }
    public static Vec3 aimPoint(Vec3 eyes, AxisAlignedBB box) {
        double d = B4AuraHost.aimClosestPoint.getInput();
        double cx = (box.minX + box.maxX) / 2.0;
        double cy = box.minY + (box.maxY - box.minY) * 0.825;
        double cz = (box.minZ + box.maxZ) / 2.0;
        if (d <= 0.0) {
            return new Vec3(cx, cy, cz);
        }
        if (B4AuraHost.aimClosestPoint.isAtMin()) {
            return new Vec3(cx, cy, cz);
        }
        double d5 = 0.03;
        double px = MathHelper.clamp_double(eyes.xCoord, box.minX + d5, box.maxX - d5);
        double py = MathHelper.clamp_double(eyes.yCoord, box.minY + d5, box.maxY - d5);
        double pz = MathHelper.clamp_double(eyes.zCoord, box.minZ + d5, box.maxZ - d5);
        if (d >= 100.0) {
            return new Vec3(px, py, pz);
        }
        double t = d / 100.0;
        return new Vec3(cx + (px - cx) * t, cy + (py - cy) * t, cz + (pz - cz) * t);
    }
    public static Vec3 visiblePoint(Entity entity) {
        Vec3 eyes = mc().thePlayer.getPositionEyes(1.0f);
        float f = entity.getCollisionBorderSize();
        AxisAlignedBB box = entity.getEntityBoundingBox().expand(f, f, f);
        if (box.isVecInside(eyes)) {
            return eyes;
        }
        Vec3 p = aimPoint(eyes, box);
        if (WorldUtils.clearLine(eyes, p)) {
            return p;
        }
        return fallbackPoint(entity, eyes);
    }
    public static Vec3 fallbackPoint(Entity entity, Vec3 eyes) {
        double x = entity.posX;
        double y = entity.posY;
        double z = entity.posZ;
        double eye = entity.getEyeHeight();
        double h1 = eye - 0.2;
        double h2 = eye - 0.9;
        double h3 = eye - 1.5;
        double o = 0.28;
        Vec3[] points = {
                new Vec3(x, y + (eye + 0.2), z),
                new Vec3(x, y + (eye - 0.6), z),
                new Vec3(x, y + h2, z),
                new Vec3(x, y + (eye - 1.2), z),
                new Vec3(x, y + h3, z),
                new Vec3(x + o, y + h1, z + o),
                new Vec3(x + o, y + h1, z - o),
                new Vec3(x - o, y + h1, z + o),
                new Vec3(x - o, y + h1, z - o),
                new Vec3(x + o, y + h2, z + o),
                new Vec3(x + o, y + h2, z - o),
                new Vec3(x - o, y + h2, z + o),
                new Vec3(x - o, y + h2, z - o),
                new Vec3(x + o, y + h3, z + o),
                new Vec3(x + o, y + h3, z - o),
                new Vec3(x - o, y + h3, z + o),
                new Vec3(x - o, y + h3, z - o),
        };
        for (Vec3 p : points) {
            if (WorldUtils.clearLine(eyes, p)) {
                return p;
            }
        }
        return null;
    }
    public static boolean canSee(Entity entity) {
        Vec3 eyes = mc().thePlayer.getPositionEyes(1.0f);
        double x = entity.posX;
        double y = entity.posY;
        double z = entity.posZ;
        double h = (double) entity.getEyeHeight() - 0.2;
        if (WorldUtils.clearLine(eyes, new Vec3(x + 0.3, y + h, z))) {
            return true;
        }
        if (WorldUtils.clearLine(eyes, new Vec3(x - 0.3, y + h, z))) {
            return true;
        }
        if (WorldUtils.clearLine(eyes, new Vec3(x, y + h, z + 0.3))) {
            return true;
        }
        if (WorldUtils.clearLine(eyes, new Vec3(x, y + h, z - 0.3))) {
            return true;
        }
        for (double d = (double) entity.getEyeHeight() + 0.2; d > 0.0; d -= 0.2) {
            if (!WorldUtils.clearLine(eyes, new Vec3(x, y + d, z))) continue;
            return true;
        }
        return false;
    }
    public static void setRotation(float yaw, float pitch) {
        mc().thePlayer.rotationYaw = yaw;
        mc().thePlayer.rotationPitch = pitch;
    }
    }
    public static final class WorldUtils {
        private static final BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
    public static boolean clearLine(Vec3 from, Vec3 to) {
        MovingObjectPosition hit = rayTrace(from, to);
        return hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK;
    }
    public static MovingObjectPosition rayTrace(Vec3 from, Vec3 to) {
        MovingObjectPosition hit;
        int x = MathHelper.floor_double(from.xCoord);
        int y = MathHelper.floor_double(from.yCoord);
        int z = MathHelper.floor_double(from.zCoord);
        scratch.set(x, y, z);
        IBlockState state = mc().theWorld.getBlockState(scratch);
        Block block = state.getBlock();
        if (block.canCollideCheck(state, false) && (hit = block.collisionRayTrace(mc().theWorld, scratch, from, to)) != null) {
            return hit;
        }
        int ex = MathHelper.floor_double(to.xCoord);
        int ey = MathHelper.floor_double(to.yCoord);
        int ez = MathHelper.floor_double(to.zCoord);
        double d = from.xCoord;
        double d2 = from.yCoord;
        double d3 = from.zCoord;
        double d4 = to.xCoord - d;
        double d5 = to.yCoord - d2;
        double d6 = to.zCoord - d3;
        int n7 = 200;
        while (n7-- >= 0) {
            MovingObjectPosition hit2;
            EnumFacing facing;
            if (x == ex && y == ey && z == ez) {
                return null;
            }
            boolean bx = true;
            boolean by = true;
            boolean bz = true;
            double d7 = 999.0;
            double d8 = 999.0;
            double d9 = 999.0;
            if (ex > x) {
                d7 = x + 1;
            } else if (ex < x) {
                d7 = x;
            } else {
                bx = false;
            }
            if (ey > y) {
                d8 = y + 1;
            } else if (ey < y) {
                d8 = y;
            } else {
                by = false;
            }
            if (ez > z) {
                d9 = z + 1;
            } else if (ez < z) {
                d9 = z;
            } else {
                bz = false;
            }
            double d10 = 999.0;
            double d11 = 999.0;
            double d12 = 999.0;
            if (bx) {
                d10 = (d7 - d) / d4;
            }
            if (by) {
                d11 = (d8 - d2) / d5;
            }
            if (bz) {
                d12 = (d9 - d3) / d6;
            }
            if (d10 == -0.0) {
                d10 = -1.0E-4;
            }
            if (d11 == -0.0) {
                d11 = -1.0E-4;
            }
            if (d12 == -0.0) {
                d12 = -1.0E-4;
            }
            if (d10 < d11 && d10 < d12) {
                facing = ex > x ? EnumFacing.WEST : EnumFacing.EAST;
                d = d7;
                d2 += d5 * d10;
                d3 += d6 * d10;
            } else if (d11 < d12) {
                facing = ey > y ? EnumFacing.DOWN : EnumFacing.UP;
                d += d4 * d11;
                d2 = d8;
                d3 += d6 * d11;
            } else {
                facing = ez > z ? EnumFacing.NORTH : EnumFacing.SOUTH;
                d += d4 * d12;
                d2 += d5 * d12;
                d3 = d9;
            }
            x = MathHelper.floor_double(d) - (facing == EnumFacing.EAST ? 1 : 0);
            y = MathHelper.floor_double(d2) - (facing == EnumFacing.UP ? 1 : 0);
            z = MathHelper.floor_double(d3) - (facing == EnumFacing.SOUTH ? 1 : 0);
            scratch.set(x, y, z);
            IBlockState state2 = mc().theWorld.getBlockState(scratch);
            Block block2 = state2.getBlock();
            if (!block2.canCollideCheck(state2, false) || (hit2 = block2.collisionRayTrace(mc().theWorld, scratch, new Vec3(d, d2, d3), to)) == null) continue;
            return hit2;
        }
        return null;
    }
    }
    /** OpenB4 server detection needed by inventory suppression and team filtering. */
    public static final class ServerState {
        public enum State {
            NOT_HYPIXEL, UNKNOWN, LIMBO, LOBBY, REPLAY, BRIDGE_PRACTICE, PIT, MURDER_MYSTERY_GAME, BEDWARS_PREGAME,
            BEDWARS_GAME_BED, BEDWARS_GAME_NO_BED, SKYWARS_QUEUE, SKYWARS_GAME_SOLO, SKYWARS_GAME_TEAM, DUELS_QUEUE, DUELS_GAME
        }
        private static final String HOST = "hypixel.net";
        private static final String LOBBY_MARK = "  \u00a78L";
        private static final char BED_MARK = '\u2713';
        private static String tabHeader() {
            if (mc().ingameGUI == null) return "";
            IChatComponent header = ReflectionHelper.getPrivateValue(GuiPlayerTabOverlay.class, mc().ingameGUI.getTabList(), "header", "field_175256_i");
            return header == null ? "" : header.getUnformattedText();
        }
        private static ScorePlayerTeam ownTeam() { return (ScorePlayerTeam)mc().thePlayer.getTeam(); }
    public static boolean isHypixelServer() {
        if (mc().isSingleplayer() || mc().getCurrentServerData() == null) {
            return false;
        }
        if (mc().getCurrentServerData().serverIP.toLowerCase().endsWith(HOST)) {
            return true;
        }
        return tabHeader().toLowerCase().contains(HOST);
    }
    public static List<String> sidebar() {
        if (mc().theWorld == null) return null;
        Scoreboard sb = mc().theWorld.getScoreboard();
        if (sb == null) return null;
        ScoreObjective obj = sb.getObjectiveInDisplaySlot(1);
        if (obj == null) return null;
        ArrayList<Score> scores = new ArrayList<Score>(sb.getSortedScores(obj));
        ArrayList<String> out = new ArrayList<String>(10);
        out.add(obj.getDisplayName());
        int n = 0;
        for (int i = scores.size() - 1; i >= 0 && n < 15; --i) {
            Score s = scores.get(i);
            String name;
            if (s == null || (name = s.getPlayerName()) == null || name.startsWith("#")) continue;
            ScorePlayerTeam team = sb.getPlayersTeam(name);
            out.add(ScorePlayerTeam.formatPlayerName(team, name));
            ++n;
        }
        return out;
    }
    public static State detect() {
        if (!isHypixelServer()) return State.NOT_HYPIXEL;
        List<String> lines = sidebar();
        if (lines == null) {
            if (mc().theWorld.provider != null && mc().theWorld.provider.getDimensionId() == 1) return State.LIMBO;
            return State.UNKNOWN;
        }
        boolean bedwars = false, skywars = false, skyPlayers = false, duels = false, murder = false;
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            if (i == 0) {
                String t = net.minecraft.util.StringUtils.stripControlCodes(line);
                if (t.equals("REPLAY")) return State.REPLAY;
                if (t.equals("BED WARS PRACTICE")) return State.BRIDGE_PRACTICE;
                if (t.equals("THE HYPIXEL PIT")) return State.PIT;
                if (t.equals("BED WARS")) bedwars = true;
                else if (t.equals("SKYWARS")) skywars = true;
                else if (t.equals("DUELS")) duels = true;
                else if (t.equals("MURDER MYSTERY")) murder = true;
                continue;
            }
            if (i == 1 && line.contains(LOBBY_MARK)) return State.LOBBY;
            if (bedwars) {
                if (i >= 5) {
                    if (i == 8 && line.startsWith("Mode")) return State.BEDWARS_PREGAME;
                    if (line.endsWith(" YOU")) {
                        return line.indexOf(BED_MARK) != -1 ? State.BEDWARS_GAME_BED : State.BEDWARS_GAME_NO_BED;
                    }
                }
            } else if (skywars) {
                if (i == 3) {
                    if (line.startsWith("Player")) return State.SKYWARS_QUEUE;
                } else if (i == 6) {
                    if (line.startsWith("Player")) skyPlayers = true;
                } else if (i == 7 && skyPlayers) {
                    return !line.startsWith("Team") ? State.SKYWARS_GAME_SOLO : State.SKYWARS_GAME_TEAM;
                }
            } else if (murder) {
                if (i == 3 && line.startsWith("Role")) return State.MURDER_MYSTERY_GAME;
            } else if (duels) {
                if (i == 3) {
                    if (line.startsWith("Time")) return State.DUELS_GAME;
                } else if (i == 6) {
                    if (line.startsWith("Waiting.")) return State.DUELS_QUEUE;
                    if (line.startsWith("Starting ")) return State.DUELS_QUEUE;
                }
            }
        }
        return State.UNKNOWN;
    }
    public static boolean sameTeam(net.minecraft.entity.EntityLivingBase e) {
        State state = detect();
        if (state == State.NOT_HYPIXEL) {
            return false;
        }
        if (state == State.PIT) {
            return false;
        }
        if (e instanceof net.minecraft.entity.monster.EntitySilverfish) {
            String own;
            if (e.getName().length() > 5 && (own = mc().thePlayer.getDisplayName().getUnformattedText()).length() > 5
                    && e.getName().substring(0, 5).equals(own.substring(0, 5))) {
                return true;
            }
        } else if (e instanceof net.minecraft.entity.monster.EntityIronGolem) {
            Entity tag = mc().theWorld.getEntityByID(e.getEntityId() + 1);
            String own;
            if (tag != null && tag.getName().length() > 2 && (own = mc().thePlayer.getDisplayName().getUnformattedText()).length() > 2
                    && tag.getName().substring(0, 2).equals(own.substring(0, 2))) {
                return true;
            }
        } else if (!(e instanceof net.minecraft.entity.player.EntityPlayer)) {
            return !(e instanceof net.minecraft.entity.passive.EntityHorse) && (state == State.BEDWARS_GAME_BED || state == State.BEDWARS_GAME_NO_BED);
        }
        net.minecraft.scoreboard.ScorePlayerTeam team = (net.minecraft.scoreboard.ScorePlayerTeam) e.getTeam();
        if (team == null) {
            return false;
        }
        net.minecraft.scoreboard.ScorePlayerTeam own = ownTeam();
        if (own == null) {
            return false;
        }
        return team.getColorPrefix().equals(own.getColorPrefix());
    }
    }
}

package dev.rice.client.clutch;

import dev.rice.module.ModuleManager;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ChoiceSetting;
import dev.rice.setting.NumberSetting;
import dev.rice.setting.Setting;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 * Native engine surface for the OpenB4 AutoClutch port (Sam's uploaded script). This is the OpenB4 script API
 * ({@code client}, {@code world}, {@code inventory}, {@code keybinds}, {@code render}, {@code gl}, {@code util},
 * {@code modules} and the {@link Vec3}/{@link Block}/{@link ItemStack}/{@link Entity}/packet value types) rebound to
 * Rice's real engine, with the script-runtime machinery removed. {@link AutoClutchBackend} extends this and carries the
 * script body verbatim, exactly as OpenB4's compiled script extended its ScriptDefaults. Verbs the shim never needed
 * for the old Scaffold-toggling script ({@code client.placeBlock}, {@code client.raycastBlock}, {@code client.swing},
 * {@code client.getScreen}, {@code client.time}, {@code world.getEntities}, {@code inventory.getSlot} and the entity
 * eye/last-position accessors) are implemented here against named Minecraft 1.8.9 (the build remaps all three payloads).
 */
public abstract class ClutchApi {
   /** Bound once by {@link dev.rice.module.impl.AutoClutchModule}; a single AutoClutch instance means static is safe. */
   protected static Minecraft mc;
   static ModuleManager manager;
   private static Consumer<Setting<?>> sink;
   /** Registered settings keyed by their original OpenB4 label (the key the script reads back with). */
   private static final Map<String, Setting<?>> BY_LABEL = new LinkedHashMap<>();
   private static final Map<String, KeyBinding> KEY_MAP = new LinkedHashMap<>();
   private static String scriptName = "AutoClutch";

   /** Module hands over its engine handles + settings sink before it runs the script's onLoad. */
   public static void bind(Minecraft minecraft, ModuleManager moduleManager, Consumer<Setting<?>> settingSink) {
      mc = minecraft;
      manager = moduleManager;
      sink = settingSink;
   }

   static String displayName() {
      return scriptName;
   }

   private static KeyBinding key(String name) {
      if (KEY_MAP.isEmpty() && mc != null && mc.gameSettings != null) {
         for (KeyBinding binding : mc.gameSettings.keyBindings) {
            KEY_MAP.put(binding.getKeyDescription().replaceFirst("key\\.", ""), binding);
         }
      }
      return KEY_MAP.get(name);
   }

   // ----- value types (OpenB4 wrappers over Minecraft) -----

   public static final class Vec3 {
      public double x;
      public double y;
      public double z;

      public Vec3() {
      }

      public Vec3(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }

      public Vec3 offset(double dx, double dy, double dz) {
         return new Vec3(this.x + dx, this.y + dy, this.z + dz);
      }

      public Vec3 offset(Vec3 other) {
         return this.offset(other.x, other.y, other.z);
      }

      @Override
      public String toString() {
         return "Vec3(" + this.x + "," + this.y + "," + this.z + ")";
      }
   }

   public static final class Block {
      public String name;
      public String type;

      Block(int x, int y, int z) {
         net.minecraft.block.state.IBlockState state = mc.theWorld.getBlockState(new BlockPos(x, y, z));
         net.minecraft.block.Block block = state.getBlock();
         this.type = block.getClass().getSimpleName();
         this.name = stripNamespace(net.minecraft.block.Block.blockRegistry.getNameForObject(block));
      }

      @Override
      public String toString() {
         return "Block(" + this.type + "," + this.name + ")";
      }
   }

   public static final class ItemStack {
      net.minecraft.item.ItemStack handle;
      public String name;
      public String displayName;
      public String type;
      public int stackSize;
      public boolean isBlock;

      private ItemStack(net.minecraft.item.ItemStack stack) {
         this.handle = stack;
         net.minecraft.item.Item item = stack.getItem();
         this.type = item.getClass().getSimpleName();
         this.name = stripNamespace(net.minecraft.item.Item.itemRegistry.getNameForObject(item));
         this.displayName = stack.getDisplayName();
         this.stackSize = stack.stackSize;
         this.isBlock = item instanceof ItemBlock;
      }

      static ItemStack of(net.minecraft.item.ItemStack stack) {
         return stack == null ? null : new ItemStack(stack);
      }

      @Override
      public String toString() {
         return "ItemStack(" + this.type + "," + this.name + ")";
      }
   }

   public static final class Entity {
      net.minecraft.entity.Entity handle;
      public int entityId;
      public boolean isLiving;

      private Entity(net.minecraft.entity.Entity entity) {
         this.handle = entity;
         this.entityId = entity.getEntityId();
         this.isLiving = entity instanceof EntityLivingBase;
      }

      static Entity of(net.minecraft.entity.Entity entity) {
         return entity == null ? null : new Entity(entity);
      }

      public Vec3 getPosition() {
         return new Vec3(this.handle.posX, this.handle.posY, this.handle.posZ);
      }

      public Vec3 getLastPosition() {
         return new Vec3(this.handle.lastTickPosX, this.handle.lastTickPosY, this.handle.lastTickPosZ);
      }

      public float getYaw() {
         return this.handle.rotationYaw;
      }

      public void setYaw(float yaw) {
         this.handle.rotationYaw = yaw;
      }

      public float getPitch() {
         return this.handle.rotationPitch;
      }

      public boolean onGround() {
         return this.handle.onGround;
      }

      public int getHurtTime() {
         return this.isLiving ? ((EntityLivingBase) this.handle).hurtTime : -1;
      }

      public float getFallDistance() {
         return this.handle.fallDistance;
      }

      public int getTicksExisted() {
         return this.handle.ticksExisted;
      }

      public double getEyeHeight() {
         return this.handle.getEyeHeight();
      }

      public boolean isSprinting() {
         return this.handle.isSprinting();
      }

      public String getName() {
         return this.handle.getName();
      }

      public ItemStack getHeldItem() {
         return this.isLiving ? ItemStack.of(((EntityLivingBase) this.handle).getHeldItem()) : null;
      }
   }

   // ----- packet value types (constructed at the hook boundary by the module) -----

   public static class SPacket {
      public String name;

      SPacket(net.minecraft.network.Packet<?> packet) {
         this.name = packet.getClass().getSimpleName();
      }
   }

   public static final class S12 extends SPacket {
      public int entityId;

      S12(net.minecraft.network.play.server.S12PacketEntityVelocity packet) {
         super(packet);
         this.entityId = packet.getEntityID();
      }
   }

   public static class CPacket {
      public String name;

      CPacket(net.minecraft.network.Packet<?> packet) {
         this.name = packet.getClass().getSimpleName();
      }
   }

   public static final class C03 extends CPacket {
      public float yaw;
      public float pitch;

      C03(net.minecraft.network.play.client.C03PacketPlayer packet) {
         super(packet);
         this.yaw = packet.getYaw();
         this.pitch = packet.getPitch();
      }
   }

   /** Wrap an inbound packet for onPacketReceived; returns null for packets the script ignores. */
   public static SPacket wrapInbound(net.minecraft.network.Packet<?> packet) {
      if (packet instanceof net.minecraft.network.play.server.S12PacketEntityVelocity) {
         return new S12((net.minecraft.network.play.server.S12PacketEntityVelocity) packet);
      }
      return new SPacket(packet);
   }

   /** Wrap an outbound packet for onPacketSent. */
   public static CPacket wrapOutbound(net.minecraft.network.Packet<?> packet) {
      if (packet instanceof net.minecraft.network.play.client.C03PacketPlayer) {
         return new C03((net.minecraft.network.play.client.C03PacketPlayer) packet);
      }
      return new CPacket(packet);
   }

   // ----- engine verb namespaces (inherited by the backend, OpenB4-style) -----

   public static final class client {
      public static Entity getPlayer() {
         return mc.thePlayer == null ? null : Entity.of(mc.thePlayer);
      }

      public static Vec3 getMotion() {
         return mc.thePlayer == null ? null : new Vec3(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
      }

      public static String getScreen() {
         return mc.currentScreen == null ? "" : mc.currentScreen.getClass().getSimpleName();
      }

      public static long time() {
         return System.currentTimeMillis();
      }

      public static boolean isFlying() {
         return mc.thePlayer != null && mc.thePlayer.capabilities.isFlying;
      }

      public static float getForward() {
         return mc.thePlayer.movementInput.moveForward;
      }

      public static float getStrafe() {
         return mc.thePlayer.movementInput.moveStrafe;
      }

      public static boolean isMoving() {
         return mc.thePlayer != null && (getForward() != 0.0F || getStrafe() != 0.0F);
      }

      public static void swing() {
         if (mc.thePlayer != null) {
            mc.thePlayer.swingItem();
         }
      }

      public static void print(String message) {
         if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(message));
         }
      }

      /** Raytrace a block from the eye at the given rotations; returns {blockPos, hitFraction, sideName} or null. */
      public static Object[] raycastBlock(double reach, float yaw, float pitch) {
         EntityPlayer player = mc.thePlayer;
         if (player == null || mc.theWorld == null) {
            return null;
         }
         net.minecraft.util.Vec3 eye = player.getPositionEyes(1.0F);
         net.minecraft.util.Vec3 look = lookVector(yaw, pitch);
         net.minecraft.util.Vec3 end = eye.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
         MovingObjectPosition hit = mc.theWorld.rayTraceBlocks(eye, end, false, false, true);
         if (hit == null || hit.typeOfHit != MovingObjectType.BLOCK) {
            return null;
         }
         BlockPos pos = hit.getBlockPos();
         Vec3 blockPos = new Vec3(pos.getX(), pos.getY(), pos.getZ());
         Vec3 fraction = new Vec3(hit.hitVec.xCoord - pos.getX(), hit.hitVec.yCoord - pos.getY(), hit.hitVec.zCoord - pos.getZ());
         return new Object[]{blockPos, fraction, hit.sideHit.name()};
      }

      /** Place a block against {hit} on {side} at the world-space {hitVec}; true when the interaction was accepted. */
      public static boolean placeBlock(Vec3 hit, String side, Vec3 hitVec) {
         net.minecraft.client.entity.EntityPlayerSP player = mc.thePlayer;
         if (player == null || mc.theWorld == null || mc.playerController == null) {
            return false;
         }
         BlockPos pos = new BlockPos(MathHelper.floor_double(hit.x), MathHelper.floor_double(hit.y), MathHelper.floor_double(hit.z));
         EnumFacing facing = EnumFacing.valueOf(side);
         net.minecraft.util.Vec3 vec = new net.minecraft.util.Vec3(hitVec.x, hitVec.y, hitVec.z);
         return mc.playerController.onPlayerRightClick(player, mc.theWorld, player.getHeldItem(), pos, facing, vec);
      }
   }

   public static final class world {
      public static Block getBlockAt(int x, int y, int z) {
         return new Block(x, y, z);
      }

      public static Block getBlockAt(Vec3 pos) {
         return new Block(MathHelper.floor_double(pos.x), MathHelper.floor_double(pos.y), MathHelper.floor_double(pos.z));
      }

      public static List<Entity> getEntities() {
         List<Entity> result = new ArrayList<>();
         if (mc.theWorld != null) {
            for (Object entity : new ArrayList<>(mc.theWorld.loadedEntityList)) {
               result.add(Entity.of((net.minecraft.entity.Entity) entity));
            }
         }
         return result;
      }
   }

   public static final class inventory {
      public static int getSlot() {
         return mc.thePlayer.inventory.currentItem;
      }

      public static void setSlot(int slot) {
         mc.thePlayer.inventory.currentItem = slot;
      }

      public static ItemStack getStackInSlot(int slot) {
         return ItemStack.of(mc.thePlayer.inventory.getStackInSlot(slot));
      }
   }

   public static final class keybinds {
      public static boolean isPressed(String name) {
         KeyBinding binding = key(name);
         return binding != null && binding.isKeyDown();
      }

      public static void setPressed(String name, boolean pressed) {
         KeyBinding binding = key(name);
         if (binding != null) {
            KeyBinding.setKeyBindState(binding.getKeyCode(), pressed);
            if (pressed) {
               KeyBinding.onTick(binding.getKeyCode());
            }
         }
      }

      public static boolean isMouseDown(int button) {
         return Mouse.isButtonDown(button);
      }

      public static boolean isKeyDown(int code) {
         return Keyboard.isKeyDown(code);
      }

      public static int getKeycode(String name) {
         KeyBinding binding = key(name);
         return binding != null ? binding.getKeyCode() : -1;
      }
   }

   public static final class render {
      public static Vec3 getPosition() {
         return new Vec3(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY, mc.getRenderManager().viewerPosZ);
      }
   }

   public static final class gl {
      public static void push() {
         GlStateManager.pushMatrix();
      }

      public static void pop() {
         GlStateManager.popMatrix();
      }

      public static void translate(double x, double y, double z) {
         GlStateManager.translate(x, y, z);
      }

      public static void blend(boolean on) {
         if (on) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         } else {
            GlStateManager.disableBlend();
         }
      }

      public static void texture2d(boolean on) {
         if (on) {
            GlStateManager.enableTexture2D();
         } else {
            GlStateManager.disableTexture2D();
         }
      }

      public static void depth(boolean on) {
         if (on) {
            GlStateManager.enableDepth();
         } else {
            GlStateManager.disableDepth();
         }
      }

      public static void depthMask(boolean on) {
         GlStateManager.depthMask(on);
      }

      public static void lighting(boolean on) {
         if (on) {
            GlStateManager.enableLighting();
         } else {
            GlStateManager.disableLighting();
         }
      }

      public static void cull(boolean on) {
         if (on) {
            GlStateManager.enableCull();
         } else {
            GlStateManager.disableCull();
         }
      }

      public static void alpha(boolean on) {
         if (on) {
            GlStateManager.enableAlpha();
         } else {
            GlStateManager.disableAlpha();
         }
      }

      public static void disableItemLighting() {
         RenderHelper.disableStandardItemLighting();
      }

      public static void lineSmooth(boolean on) {
         if (on) {
            GL11.glEnable(2848);
         } else {
            GL11.glDisable(2848);
         }
      }

      public static void lineWidth(double width) {
         GL11.glLineWidth((float) width);
      }

      public static void lineWidth(float width) {
         GL11.glLineWidth(width);
      }

      public static void color(float r, float g, float b, float a) {
         GlStateManager.color(r, g, b, a);
         GL11.glColor4f(r, g, b, a);
      }

      public static void color(int packed) {
         float a = (packed >> 24 & 0xFF) / 255.0F;
         float r = (packed >> 16 & 0xFF) / 255.0F;
         float g = (packed >> 8 & 0xFF) / 255.0F;
         float b = (packed & 0xFF) / 255.0F;
         GlStateManager.color(r, g, b, a);
         GL11.glColor4f(r, g, b, a);
      }

      public static void resetColor() {
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      }

      public static void begin(int mode) {
         GL11.glBegin(mode);
      }

      public static void end() {
         GL11.glEnd();
      }

      public static void vertex3(double x, double y, double z) {
         GL11.glVertex3d(x, y, z);
      }

      public static void vertex3(float x, float y, float z) {
         GL11.glVertex3f(x, y, z);
      }
   }

   public static final class util {
      public static String color(String text) {
         if (text == null) {
            return "";
         }
         char[] chars = text.toCharArray();
         for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&' && "0123456789abcdefklmnorABCDEFKLMNOR".indexOf(chars[i + 1]) > -1) {
               chars[i] = '§';
               chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
         }
         return new String(chars);
      }
   }

   public static final class modules {
      public static void registerDescription(String text) {
         // OpenB4 DescriptionSetting: section headings/credit have no Rice ClickGUI row type. Dropped, as the shim did.
      }

      public static void registerButton(String label, boolean enabled) {
         put(label, new BooleanSetting(keyFor(label), label, enabled));
      }

      /** Mode slider: options make it a ChoiceSetting; {@code value} is the default index. */
      public static void registerSlider(String label, String suffix, int value, String[] options) {
         put(label, new ChoiceSetting(keyFor(label), label, value, options));
      }

      /** Numeric slider. */
      public static void registerSlider(String label, String suffix, double value, double min, double max, double interval) {
         String name = label + (suffix == null || suffix.trim().isEmpty() ? "" : " (" + suffix.trim() + ")");
         put(label, new NumberSetting(keyFor(label), name, value, min, max, interval));
      }

      public static double getSlider(String moduleName, String label) {
         Setting<?> setting = BY_LABEL.get(label);
         if (setting instanceof ChoiceSetting) {
            return ((ChoiceSetting) setting).index();
         } else if (setting instanceof NumberSetting) {
            return ((NumberSetting) setting).doubleValue();
         }
         return 0.0;
      }

      public static boolean getButton(String moduleName, String label) {
         Setting<?> setting = BY_LABEL.get(label);
         return setting instanceof BooleanSetting && ((BooleanSetting) setting).isEnabled();
      }

      public static boolean isEnabled(String moduleName) {
         dev.rice.module.Module module = find(moduleName);
         return module != null && module.isEnabled();
      }

      public static void enable(String moduleName) {
         dev.rice.module.Module module = find(moduleName);
         if (module != null && !module.isEnabled() && manager != null) {
            manager.applyNow(module, true);
         }
      }

      public static void disable(String moduleName) {
         dev.rice.module.Module module = find(moduleName);
         if (module != null && module.isEnabled() && manager != null) {
            manager.applyNow(module, false);
         }
      }

      private static void put(String label, Setting<?> setting) {
         BY_LABEL.put(label, setting);
         if (sink != null) {
            sink.accept(setting);
         }
      }

      private static dev.rice.module.Module find(String name) {
         if (manager == null) {
            return null;
         }
         for (dev.rice.module.Module module : manager.all()) {
            if (module.getName().equalsIgnoreCase(name) || module.getId().equalsIgnoreCase(name)) {
               return module;
            }
         }
         return null;
      }
   }

   static String getScriptName() {
      return scriptName;
   }

   private static String keyFor(String label) {
      return label.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
   }

   private static String stripNamespace(ResourceLocation location) {
      if (location == null) {
         return "air";
      }
      return location.getResourcePath();
   }

   private static net.minecraft.util.Vec3 lookVector(float yaw, float pitch) {
      float yr = (float) Math.toRadians(yaw);
      float pr = (float) Math.toRadians(pitch);
      float cy = MathHelper.cos(-yr - (float) Math.PI);
      float sy = MathHelper.sin(-yr - (float) Math.PI);
      float cp = -MathHelper.cos(-pr);
      float sp = MathHelper.sin(-pr);
      return new net.minecraft.util.Vec3(sy * cp, sp, cy * cp);
   }
}

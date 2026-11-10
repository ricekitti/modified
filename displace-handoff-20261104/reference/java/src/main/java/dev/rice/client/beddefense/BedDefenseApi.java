package dev.rice.client.beddefense;

import dev.rice.module.Module;
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
import org.lwjgl.opengl.GL11;

/**
 * Native engine surface for Sam's OpenB4 Bed Defender script ("v1.0 by winnie"). This is the OpenB4 script API
 * ({@code client}, {@code world}, {@code inventory}, {@code keybinds}, {@code render}, {@code util}, {@code modules},
 * the {@link Vec3}/{@link Block}/{@link ItemStack}/{@link Entity}/{@link CPacket} value types and a Gson-backed
 * {@link Json}) rebound to Rice's real engine, with the script-runtime machinery removed. {@link BedDefenseBackend}
 * extends this and carries the imported script body with session cleanup, as the compiled OpenB4 script extended its
 * ScriptDefaults. Nothing here touches OpenJade: the server-synced raycast ghost-block fix lives entirely in the
 * script (it tracks serverYaw/serverPitch from its own outbound C03 observation and ray-traces with them), and the
 * rotation output is published through Rice's own {@link dev.rice.client.rotation.RotationService}, mirroring AutoClutch.
 */
public abstract class BedDefenseApi {
   /** Bound once by {@link dev.rice.module.impl.BedDefenderModule}; a single Bed Defender instance means static is safe. */
   protected static Minecraft mc;
   static ModuleManager manager;
   static Module owner;
   private static Consumer<Setting<?>> sink;
   /** Registered settings keyed by their original OpenB4 label (the key the script reads back with). */
   private static final Map<String, Setting<?>> BY_LABEL = new LinkedHashMap<>();
   private static final Map<String, KeyBinding> KEY_MAP = new LinkedHashMap<>();
   private static final String SCRIPT_NAME = "Bed Defender";

   /** Module hands over its engine handles + settings sink before it runs the script's onLoad. */
   public static void bind(Minecraft minecraft, ModuleManager moduleManager, Consumer<Setting<?>> settingSink, Module module) {
      BY_LABEL.clear();
      KEY_MAP.clear();
      mc = minecraft;
      manager = moduleManager;
      sink = settingSink;
      owner = module;
   }

   private static KeyBinding key(String name) {
      if (mc == null || mc.gameSettings == null) return null;
      if ("sneak".equals(name)) return mc.gameSettings.keyBindSneak;
      if (KEY_MAP.isEmpty() && mc.gameSettings.keyBindings != null) {
         for (KeyBinding binding : mc.gameSettings.keyBindings) {
            if (binding == null) continue;
            KEY_MAP.put(binding.getKeyDescription().replaceFirst("key\\.", ""), binding);
         }
      }
      return KEY_MAP.get(name);
   }

   static String getScriptName() {
      return SCRIPT_NAME;
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

      public double distanceToSq(Vec3 other) {
         double dx = this.x - other.x;
         double dy = this.y - other.y;
         double dz = this.z - other.z;
         return dx * dx + dy * dy + dz * dz;
      }

      // The script compares placement targets by value (e.g. _place.equals(target)); Minecraft block coordinates
      // are whole numbers here, so exact double equality is the intended block-cell identity.
      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         }
         if (!(o instanceof Vec3)) {
            return false;
         }
         Vec3 v = (Vec3) o;
         return Double.compare(this.x, v.x) == 0 && Double.compare(this.y, v.y) == 0 && Double.compare(this.z, v.z) == 0;
      }

      @Override
      public int hashCode() {
         return Double.hashCode(this.x) * 31 * 31 + Double.hashCode(this.y) * 31 + Double.hashCode(this.z);
      }

      @Override
      public String toString() {
         return "Vec3(" + this.x + "," + this.y + "," + this.z + ")";
      }
   }

   public static final class Block {
      public String name;
      public String type;
      /** Raw block metadata (bed facing/occupied/head bits); the script switches on it as {@code variant}. */
      public int variant;

      Block(int x, int y, int z) {
         if (mc == null || mc.theWorld == null) { this.type = "BlockAir"; this.name = "air"; return; }
         net.minecraft.block.state.IBlockState state = mc.theWorld.getBlockState(new BlockPos(x, y, z));
         net.minecraft.block.Block block = state.getBlock();
         this.type = block.getClass().getSimpleName();
         this.name = stripNamespace(net.minecraft.block.Block.blockRegistry.getNameForObject(block));
         this.variant = block.getMetaFromState(state);
      }

      @Override
      public String toString() {
         return "Block(" + this.type + "," + this.name + "," + this.variant + ")";
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

      public Vec3 getBlockPosition() {
         BlockPos pos = this.handle.getPosition();
         return new Vec3(pos.getX(), pos.getY(), pos.getZ());
      }

      public float getYaw() {
         return this.handle.rotationYaw;
      }

      public float getPitch() {
         return this.handle.rotationPitch;
      }

      public double getEyeHeight() {
         return this.handle.getEyeHeight();
      }

      public boolean isDead() {
         return this.handle.isDead;
      }

      public String getName() {
         return this.handle.getName();
      }

      public ItemStack getHeldItem() {
         return this.isLiving ? ItemStack.of(((EntityLivingBase) this.handle).getHeldItem()) : null;
      }
   }

   // ----- packet value types (constructed at the hook boundary by the module) -----

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
         this.name = packet.getRotating() ? "C05PacketPlayerLook" : "C03PacketPlayer";
         this.yaw = packet.getYaw();
         this.pitch = packet.getPitch();
      }
   }

   /** Wrap an outbound packet for onPacketSent (C04/C05/C06 all extend C03PacketPlayer and keep their own class name). */
   public static CPacket wrapOutbound(net.minecraft.network.Packet<?> packet) {
      if (packet instanceof net.minecraft.network.play.client.C03PacketPlayer) {
         return new C03((net.minecraft.network.play.client.C03PacketPlayer) packet);
      }
      return new CPacket(packet);
   }

   // ----- engine verb namespaces (inherited by the backend, OpenB4-style) -----

   public static final class client {
      public static Entity getPlayer() {
         return mc == null || mc.thePlayer == null ? null : Entity.of(mc.thePlayer);
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

      /** Raytrace a block from the eye at the GIVEN rotations (the script passes serverYaw/serverPitch); returns
       * {blockPos, hitFraction, sideName} or null. This is the heart of the server-synced ghost-block fix. */
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
         if (player == null || mc.theWorld == null || mc.playerController == null || player.getHeldItem() == null || hit == null || side == null || hitVec == null) {
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
   }

   public static final class inventory {
      public static int getSlot() {
         return mc.thePlayer == null ? -1 : mc.thePlayer.inventory.currentItem;
      }

      public static void setSlot(int slot) {
         if (mc.thePlayer != null && slot >= 0 && slot < 9) mc.thePlayer.inventory.currentItem = slot;
      }

      public static ItemStack getStackInSlot(int slot) {
         return mc.thePlayer == null || slot < 0 || slot >= 9 ? null : ItemStack.of(mc.thePlayer.inventory.getStackInSlot(slot));
      }
   }

   public static final class keybinds {
      public static void restore(String name) {
         KeyBinding binding = key(name);
         if (binding != null) dev.rice.client.KeyState.restore(binding);
      }

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
   }

   public static final class render {
      /** Isolated preview pass. Raw GL state is restored without changing Minecraft's cached state. */
      public static void block(Vec3 pos, int color, boolean filled, boolean outline) {
         if (pos == null || mc == null || mc.theWorld == null || mc.getRenderManager() == null) return;
         double padding = 0.002;
         double x1 = pos.x - mc.getRenderManager().viewerPosX - padding;
         double y1 = pos.y - mc.getRenderManager().viewerPosY - padding;
         double z1 = pos.z - mc.getRenderManager().viewerPosZ - padding;
         double x2 = x1 + 1.0 + padding * 2.0;
         double y2 = y1 + 1.0 + padding * 2.0;
         double z2 = z1 + 1.0 + padding * 2.0;
         float a = (color >>> 24 & 255) / 255.0F;
         float r = (color >> 16 & 255) / 255.0F;
         float g = (color >> 8 & 255) / 255.0F;
         float b = (color & 255) / 255.0F;
         if (a <= 0F) a = 1F; // OpenB4's RGB-only script colors are opaque.
         GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
         try {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDepthMask(false);
            if (filled) {
               GL11.glColor4f(r, g, b, a * 0.25F);
               box(x1, y1, z1, x2, y2, z2, true);
            }
            if (outline) {
               GL11.glEnable(GL11.GL_LINE_SMOOTH);
               GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
               GL11.glLineWidth(1.5F);
               GL11.glColor4f(r, g, b, a);
               box(x1, y1, z1, x2, y2, z2, false);
            }
         } finally {
            GL11.glPopAttrib();
         }
      }

      private static void vertex(int corner, double x1, double y1, double z1, double x2, double y2, double z2) {
         GL11.glVertex3d((corner & 1) == 0 ? x1 : x2, (corner & 2) == 0 ? y1 : y2, (corner & 4) == 0 ? z1 : z2);
      }

      private static void box(double x1, double y1, double z1, double x2, double y2, double z2, boolean quads) {
         GL11.glBegin(quads ? GL11.GL_QUADS : GL11.GL_LINES);
         try {
            if (quads) {
               int[] faces = {0, 1, 5, 4, 2, 6, 7, 3, 0, 2, 3, 1, 4, 5, 7, 6, 0, 4, 6, 2, 1, 3, 7, 5};
               for (int corner : faces) vertex(corner, x1, y1, z1, x2, y2, z2);
            } else {
               // Twelve independent edges; a line strip creates unwanted face diagonals.
               for (int corner = 0; corner < 8; corner++) {
                  for (int axis = 0; axis < 3; axis++) {
                     int bit = 1 << axis;
                     if ((corner & bit) == 0) {
                        vertex(corner, x1, y1, z1, x2, y2, z2);
                        vertex(corner | bit, x1, y1, z1, x2, y2, z2);
                     }
                  }
               }
            }
         } finally {
            GL11.glEnd();
         }
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

      public static double randomDouble(double min, double max) {
         return min + Math.random() * (max - min);
      }
   }

   public static final class modules {
      public static void registerDescription(String text) {
         // OpenB4 DescriptionSetting: credit/section headings have no Rice ClickGUI row type. Dropped, as the shim did.
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
         return owner != null && owner.isEnabled();
      }

      public static void disable(String moduleName) {
         if (owner != null && owner.isEnabled() && manager != null) {
            manager.applyNow(owner, false);
         }
      }

      /** The player's own bed, reused from Mayu BedNuker's respawn whitelist scan (no OpenJade). Null if unavailable. */
      public static Vec3 getWhitelistedBedPos() {
         try {
            myau.BedNukerModule nuker = dev.rice.client.mayu.MayuHost.bedNuker();
            if (nuker == null) {
               return null;
            }
            java.lang.reflect.Field field = myau.BedNukerModule.class.getDeclaredField("whitelistedBed");
            field.setAccessible(true);
            Object value = field.get(nuker);
            if (!(value instanceof BlockPos)) {
               return null;
            }
            BlockPos pos = (BlockPos) value;
            return new Vec3(pos.getX(), pos.getY(), pos.getZ());
         } catch (Throwable ignored) {
            return null;
         }
      }

      private static void put(String label, Setting<?> setting) {
         BY_LABEL.put(label, setting);
         if (sink != null) {
            sink.accept(setting);
         }
      }
   }

   /** Minimal Gson-backed view of the OpenB4 Json helper (object/map/array/get), order-preserving. */
   public static final class Json {
      private final com.google.gson.JsonElement element;

      public Json(String text) {
         this.element = new com.google.gson.JsonParser().parse(text);
      }

      private Json(com.google.gson.JsonElement node) {
         this.element = node;
      }

      public Json object() {
         return this;
      }

      public Map<String, Json> map() {
         Map<String, Json> result = new LinkedHashMap<>();
         if (this.element != null && this.element.isJsonObject()) {
            for (Map.Entry<String, com.google.gson.JsonElement> entry : this.element.getAsJsonObject().entrySet()) {
               result.put(entry.getKey(), new Json(entry.getValue()));
            }
         }
         return result;
      }

      public List<Json> array(String name) {
         List<Json> result = new ArrayList<>();
         if (this.element != null && this.element.isJsonObject()) {
            com.google.gson.JsonElement node = this.element.getAsJsonObject().get(name);
            if (node != null && node.isJsonArray()) {
               for (com.google.gson.JsonElement child : node.getAsJsonArray()) {
                  result.add(new Json(child));
               }
            }
         }
         return result;
      }

      public String get(String key, String fallback) {
         try {
            com.google.gson.JsonElement node = this.element.getAsJsonObject().get(key);
            return node == null ? fallback : node.getAsString();
         } catch (Exception ignored) {
            return fallback;
         }
      }
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

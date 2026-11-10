package dev.rice.client.render;

import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ChoiceSetting;
import dev.rice.setting.Setting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.opengl.GL11;

/**
 * The Raven script API subset that converted Raven scripts call: {@code client}, {@code render}, {@code gl},
 * {@code world}, {@code modules}, and the script value types Vec3 / Entity / ItemStack / Block.
 *
 * <p>Each body is ported from the OpenB4-u20 reference implementation (script/ScriptDefaults.java,
 * script/ScriptUtil.lookVec, script/ScriptProtected.renderBlock -> util/Render3D.blockBox/filledBox,
 * util/RenderUtils.resetColor). Host substitutions:
 * <ul>
 *   <li>{@code Defaults.mc} is {@code Minecraft.getMinecraft()}.</li>
 *   <li>Forge's {@code getRegistryName()} is the vanilla registry name, which gives the same
 *       "minecraft:..." string.</li>
 *   <li>Forge's {@code canRiderInteract()} is false for every vanilla entity, so the call is dropped.</li>
 *   <li>The Entity wrapper cache, an allocation optimisation, is not kept.</li>
 * </ul>
 */
public final class RavenScriptApi {
   private RavenScriptApi() {
   }

   private static Minecraft mc() {
      return Minecraft.getMinecraft();
   }

   /** ScriptUtil.stripNamespace. */
   static String stripNamespace(String string) {
      return string.replaceFirst("minecraft:", "");
   }

   /** ScriptUtil.lookVec. */
   static net.minecraft.util.Vec3 lookVec(float f, float f2) {
      double yaw = Math.toRadians(f), pitch = Math.toRadians(f2);
      return new net.minecraft.util.Vec3(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
   }

   public static class Vec3 {
      public double z;
      public double y;
      public double x;

      public Vec3() {
      }

      public Vec3(double d, double d2, double d3) {
         this.x = d;
         this.y = d2;
         this.z = d3;
      }

      protected Vec3(net.minecraft.util.Vec3 vec3) {
         this(vec3.xCoord, vec3.yCoord, vec3.zCoord);
      }

      protected Vec3(BlockPos blockPos) {
         this(blockPos.getX(), blockPos.getY(), blockPos.getZ());
      }

      public Vec3 floor() {
         return new Vec3(MathHelper.floor_double((double)this.x), MathHelper.floor_double((double)this.y), MathHelper.floor_double((double)this.z));
      }
   }

   public static class Entity {
      public boolean isLiving;
      public boolean isPlayer;
      public boolean isUser;
      public int entityId;
      private net.minecraft.entity.Entity entity;
      public String type;

      protected Entity(net.minecraft.entity.Entity entity) {
         this.entity = entity;
         this.type = entity.getClass().getSimpleName();
         this.entityId = entity.getEntityId();
         this.isLiving = entity instanceof EntityLivingBase;
         if (this.isLiving && (this.isPlayer = entity instanceof EntityPlayer)) {
            this.isUser = entity.getUniqueID().equals(mc().thePlayer.getUniqueID());
         }
      }

      protected static Entity of(net.minecraft.entity.Entity entity) {
         if (entity == null) {
            return null;
         }
         return new Entity(entity);
      }

      public Vec3 getPosition() {
         return new Vec3(this.entity.posX, this.entity.posY, this.entity.posZ);
      }

      public Vec3 getLastPosition() {
         return new Vec3(this.entity.lastTickPosX, this.entity.lastTickPosY, this.entity.lastTickPosZ);
      }

      public float getYaw() {
         return this.entity.rotationYaw;
      }

      public float getPitch() {
         return this.entity.rotationPitch;
      }

      public float getWidth() {
         return this.entity.width;
      }

      public float getHeight() {
         return this.entity.height;
      }

      public float getEyeHeight() {
         return this.entity.getEyeHeight();
      }

      public ItemStack getHeldItem() {
         return this.isLiving ? ItemStack.of(((EntityLivingBase)this.entity).getHeldItem()) : null;
      }
   }

   public static class ItemStack {
      public String type;
      public String name;
      public String displayName;
      public int meta;
      public int stackSize;
      public int maxStackSize;
      public int maxDurability;
      public int durability;
      net.minecraft.item.ItemStack itemStack;

      protected ItemStack(byte by, net.minecraft.item.ItemStack itemStack) {
         if (itemStack == null) {
            return;
         }
         this.itemStack = itemStack;
         Item item = itemStack.getItem();
         this.type = item.getClass().getSimpleName();
         this.name = stripNamespace(String.valueOf(Item.itemRegistry.getNameForObject(item)));
         this.meta = itemStack.getMetadata();
         this.displayName = itemStack.getDisplayName();
         this.stackSize = itemStack.stackSize;
         this.maxStackSize = itemStack.getMaxStackSize();
         this.maxDurability = itemStack.getMaxDamage();
         this.durability = this.maxDurability - itemStack.getItemDamage();
      }

      protected static ItemStack of(net.minecraft.item.ItemStack itemStack) {
         if (itemStack == null) {
            return null;
         }
         return new ItemStack((byte) 0, itemStack);
      }
   }

   public static class Block {
      public String type;
      public String name;
      public int variant;
      public double height;
      public double width;
      public double length;

      public Block(IBlockState iBlockState) {
         net.minecraft.block.Block block = iBlockState.getBlock();
         this.type = block.getClass().getSimpleName();
         this.name = stripNamespace(String.valueOf(net.minecraft.block.Block.blockRegistry.getNameForObject(block)));
         this.variant = block.getMetaFromState(iBlockState);
         this.height = block.getBlockBoundsMaxY();
         this.width = block.getBlockBoundsMaxX();
         this.length = block.getBlockBoundsMaxZ();
      }

      public Block(int n, int n2, int n3) {
         this(mc().theWorld.getBlockState(new BlockPos(n, n2, n3)));
      }
   }

   public static class client {
      public static Entity getPlayer() {
         return Entity.of((net.minecraft.entity.Entity)mc().thePlayer);
      }

      public static long time() {
         return System.currentTimeMillis();
      }

      public static Vec3 eyePosition(float partial) {
         return new Vec3(mc().thePlayer.getPositionEyes(partial));
      }

      public static Object[] raycastBlock(double range, float yaw, float pitch) {
         return raycastBlock(range, yaw, pitch, 1.0F);
      }

      public static Object[] raycastBlock(double range, float yaw, float pitch, float partial) {
         net.minecraft.util.Vec3 eye = mc().thePlayer.getPositionEyes(partial);
         net.minecraft.util.Vec3 direction = lookVec(yaw, pitch);
         MovingObjectPosition hit = mc().theWorld.rayTraceBlocks(eye, eye.addVector(direction.xCoord * range, direction.yCoord * range, direction.zCoord * range), false, false, false);
         if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return null;
         Vec3 block = new Vec3(hit.getBlockPos());
         return new Object[]{block, new Vec3(hit.hitVec.xCoord - block.x, hit.hitVec.yCoord - block.y, hit.hitVec.zCoord - block.z), hit.sideHit.name()};
      }

      public static Object[] raycastEntity(double range, float yaw, float pitch) {
         return raycastEntity(range, yaw, pitch, 1.0F);
      }

      public static Object[] raycastEntity(double range, float yaw, float pitch, float partial) {
         net.minecraft.util.Vec3 eye = mc().thePlayer.getPositionEyes(partial);
         net.minecraft.util.Vec3 direction = lookVec(yaw, pitch);
         net.minecraft.util.Vec3 end = eye.addVector(direction.xCoord * range, direction.yCoord * range, direction.zCoord * range);
         net.minecraft.entity.Entity nearest = null;
         net.minecraft.util.Vec3 point = null;
         double distance = range;
         for (net.minecraft.entity.Entity entity : mc().theWorld.loadedEntityList) {
            if (entity == mc().thePlayer || entity == mc().thePlayer.ridingEntity || !(entity instanceof EntityLivingBase)
               || entity.isDead || !entity.canBeCollidedWith() || ((EntityLivingBase) entity).deathTime != 0) continue;
            // Use the same interpolated frame as the line and final entity highlight, with no tick-position broadphase miss.
            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partial;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial;
            AxisAlignedBB box = entity.getEntityBoundingBox().offset(x - entity.posX, y - entity.posY, z - entity.posZ);
            MovingObjectPosition intercept = box.calculateIntercept(eye, end);
            net.minecraft.util.Vec3 hit = box.isVecInside(eye) ? eye : intercept == null ? null : intercept.hitVec;
            if (hit != null && eye.distanceTo(hit) < distance) {
               nearest = entity; point = hit; distance = eye.distanceTo(hit);
            }
         }
         if (nearest == null) return null;
         return new Object[]{new Entity(nearest), new Vec3(point.xCoord - nearest.posX, point.yCoord - nearest.posY, point.zCoord - nearest.posZ), eye.squareDistanceTo(point)};
      }
   }

   public static class world {
      public static Block getBlockAt(int n, int n2, int n3) {
         return new Block(n, n2, n3);
      }
   }

   public static class render {
      public static Vec3 getPosition() {
         return new Vec3(mc().getRenderManager().viewerPosX, mc().getRenderManager().viewerPosY, mc().getRenderManager().viewerPosZ);
      }

      public static void block(Vec3 vec3, int n, boolean bl, boolean bl2) {
         render.block(MathHelper.floor_double((double)vec3.x), MathHelper.floor_double((double)vec3.y), MathHelper.floor_double((double)vec3.z), n, bl, bl2);
      }

      /** ScriptBridge.renderBlock -> ScriptProtected.renderBlock -> Render3D.blockBox(n, n2, n3, 0, 0, 0, 1, 1, 1, ...). */
      public static void block(int n, int n2, int n3, int n4, boolean bl, boolean bl2) {
         blockBox(n, n2, n3, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, n4, bl, bl2);
      }
   }

   /** util/Render3D.blockBox. */
   static void blockBox(int n, int n2, int n3, float f, float f2, float f3, float f4, float f5, float f6, int n4, boolean outline, boolean shade) {
      RenderManager rm = mc().getRenderManager();
      double d = (double) n - rm.viewerPosX;
      double d2 = (double) n2 - rm.viewerPosY;
      double d3 = (double) n3 - rm.viewerPosZ;
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      float a = (float) (n4 >> 24 & 0xFF) / 255.0f;
      float r = (float) (n4 >> 16 & 0xFF) / 255.0f;
      float g = (float) (n4 >> 8 & 0xFF) / 255.0f;
      float b = (float) (n4 & 0xFF) / 255.0f;
      AxisAlignedBB bb = new AxisAlignedBB(d + (double) f, d2 + (double) f2, d3 + (double) f3, d + (double) f4, d2 + (double) f5, d3 + (double) f6);
      if (outline) {
         GlStateManager.color(r, g, b, a);
         GL11.glColor4f(r, g, b, a);
         GL11.glEnable(2848);
         GL11.glLineWidth(2.0f);
         RenderGlobal.drawSelectionBoundingBox(bb);
         GL11.glDisable(2848);
      }
      if (shade) {
         filledBox(bb, r, g, b, 0.42f);
      }
      resetColor();
      GL11.glEnable(3553);
      GL11.glEnable(2929);
      GL11.glDepthMask(true);
      GL11.glDisable(3042);
   }

   /** util/Render3D.filledBox. */
   static void filledBox(AxisAlignedBB a, float r, float g, float b, float alpha) {
      Tessellator t = Tessellator.getInstance();
      WorldRenderer w = t.getWorldRenderer();
      w.begin(7, DefaultVertexFormats.POSITION_COLOR);
      double[][] v = {
            {a.minX, a.minY, a.minZ}, {a.minX, a.maxY, a.minZ}, {a.maxX, a.maxY, a.minZ}, {a.maxX, a.minY, a.minZ},
            {a.minX, a.minY, a.maxZ}, {a.maxX, a.minY, a.maxZ}, {a.maxX, a.maxY, a.maxZ}, {a.minX, a.maxY, a.maxZ},
            {a.minX, a.minY, a.minZ}, {a.minX, a.minY, a.maxZ}, {a.minX, a.maxY, a.maxZ}, {a.minX, a.maxY, a.minZ},
            {a.maxX, a.minY, a.minZ}, {a.maxX, a.maxY, a.minZ}, {a.maxX, a.maxY, a.maxZ}, {a.maxX, a.minY, a.maxZ},
            {a.minX, a.minY, a.minZ}, {a.maxX, a.minY, a.minZ}, {a.maxX, a.minY, a.maxZ}, {a.minX, a.minY, a.maxZ},
            {a.minX, a.maxY, a.minZ}, {a.minX, a.maxY, a.maxZ}, {a.maxX, a.maxY, a.maxZ}, {a.maxX, a.maxY, a.minZ}};
      for (double[] p : v) {
         w.pos(p[0], p[1], p[2]).color(r, g, b, alpha).endVertex();
      }
      t.draw();
   }

   /** util/RenderUtils.resetColor. */
   static void resetColor() {
      GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
      GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
   }

   public static class gl {
      public static void push() {
         GlStateManager.pushMatrix();
      }

      public static void pop() {
         GlStateManager.popMatrix();
      }

      public static void enable(int n) {
         GL11.glEnable((int)n);
      }

      public static void disable(int n) {
         GL11.glDisable((int)n);
      }

      public static void blend(boolean bl) {
         if (bl) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
         } else {
            GlStateManager.disableBlend();
         }
      }

      public static void alpha(boolean bl) {
         if (bl) {
            GlStateManager.enableAlpha();
         } else {
            GlStateManager.disableAlpha();
         }
      }

      public static void texture2d(boolean bl) {
         if (bl) {
            GlStateManager.enableTexture2D();
         } else {
            GlStateManager.disableTexture2D();
         }
      }

      public static void lighting(boolean bl) {
         if (bl) {
            GlStateManager.enableLighting();
         } else {
            GlStateManager.disableLighting();
         }
      }

      public static void cull(boolean bl) {
         if (bl) {
            GlStateManager.enableCull();
         } else {
            GlStateManager.disableCull();
         }
      }

      public static void depth(boolean bl) {
         if (bl) {
            GlStateManager.enableDepth();
         } else {
            GlStateManager.disableDepth();
         }
      }

      public static void depthMask(boolean bl) {
         GlStateManager.depthMask((boolean)bl);
      }

      public static void lineSmooth(boolean bl) {
         if (bl) {
            gl.enable(2848);
         } else {
            gl.disable(2848);
         }
      }

      public static void lineWidth(float f) {
         GL11.glLineWidth((float)f);
      }

      public static void lineWidth(double d) {
         GL11.glLineWidth((float)((float)d));
      }

      public static void color(float f, float f2, float f3, float f4) {
         GlStateManager.color((float)f, (float)f2, (float)f3, (float)f4);
         GL11.glColor4f((float)f, (float)f2, (float)f3, (float)f4);
      }

      public static void color(double d, double d2, double d3, double d4) {
         GlStateManager.color((float)((float)d), (float)((float)d2), (float)((float)d3), (float)((float)d4));
         GL11.glColor4f((float)((float)d), (float)((float)d2), (float)((float)d3), (float)((float)d4));
      }

      public static void color(int n, int n2, int n3, int n4) {
         GlStateManager.color((float)((float)n / 255.0f), (float)((float)n2 / 255.0f), (float)((float)n3 / 255.0f), (float)((float)n4 / 255.0f));
         GL11.glColor4f((float)((float)n / 255.0f), (float)((float)n2 / 255.0f), (float)((float)n3 / 255.0f), (float)((float)n4 / 255.0f));
      }

      public static void color(int n) {
         float f = (float)(n >> 24 & 0xFF) / 255.0f;
         float f2 = (float)(n >> 16 & 0xFF) / 255.0f;
         float f3 = (float)(n >> 8 & 0xFF) / 255.0f;
         float f4 = (float)(n & 0xFF) / 255.0f;
         gl.color(f2, f3, f4, f);
      }

      public static void resetColor() {
         gl.color(1.0f, 1.0f, 1.0f, 1.0f);
      }

      public static void begin(int n) {
         GL11.glBegin((int)n);
      }

      public static void end() {
         GL11.glEnd();
      }

      public static void vertex3(double d, double d2, double d3) {
         GL11.glVertex3d((double)d, (double)d2, (double)d3);
      }

      public static void vertex3(float f, float f2, float f3) {
         GL11.glVertex3f((float)f, (float)f2, (float)f3);
      }
   }

   /**
    * Raven {@code modules} for one converted script. Raven creates the script's settings from onLoad's
    * register* calls; here they become the Rice module's settings, with labels exactly as registered.
    * getSlider/getButton follow OpenB4's ScriptDefaults.modules / ScriptProtected lookups by module name.
    * Only this script is a module the host knows; any other name ("Settings") finds nothing, as it would
    * for a module that does not exist in OpenB4.
    */
   public static final class Modules {
      private final String scriptName;
      private final Consumer<Setting<?>> sink;
      private final List<Setting<?>> settings = new ArrayList<>();
      private String description;

      public Modules(String scriptName, Consumer<Setting<?>> sink) {
         this.scriptName = scriptName;
         this.sink = sink;
      }

      private static String key(String name) {
         return name.toLowerCase(java.util.Locale.ROOT).trim().replace(' ', '_');
      }

      private void register(Setting<?> setting) {
         this.settings.add(setting);
         this.sink.accept(setting);
      }

      public void registerDescription(String string) {
         this.description = string;
      }

      public String description() {
         return this.description;
      }

      public void registerSlider(String string, String string2, int n, String[] stringArray) {
         this.register(new ChoiceSetting(key(string), string, n, stringArray));
      }

      public void registerButton(String string, boolean bl) {
         this.register(new BooleanSetting(key(string), string, bl));
      }

      /** ScriptProtected.getSlider: {input, display, min, max, interval} of a mode slider, or null. */
      private Object[] slider(String string, String string2) {
         if (this.scriptName.equals(string)) {
            for (Setting<?> setting : this.settings) {
               if (!(setting instanceof ChoiceSetting) || !setting.getLabel().equals(string2)) continue;
               ChoiceSetting slider = (ChoiceSetting) setting;
               return new Object[]{(double) slider.index(), slider.selectedLabel(), 0.0, (double) (slider.getOptions().size() - 1), 1.0};
            }
         }
         return null;
      }

      /** ScriptDefaults.modules.getSliderData. */
      private Object getSliderData(String string, String string2, int n, Object object) {
         Object[] objectArray = this.slider(string, string2);
         return objectArray == null ? object : objectArray[n];
      }

      public double getSlider(String string, String string2) {
         return (Double)this.getSliderData(string, string2, 0, 0);
      }

      public boolean getButton(String string, String string2) {
         if (this.scriptName.equals(string)) {
            for (Setting<?> setting : this.settings) {
               if (setting instanceof BooleanSetting && setting.getLabel().equals(string2)) {
                  return ((BooleanSetting) setting).isEnabled();
               }
            }
         }
         return false;
      }
   }
}

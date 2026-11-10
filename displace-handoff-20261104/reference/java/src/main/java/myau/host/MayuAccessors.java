package myau.host;

import dev.rice.inject.ReflectionHelper;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.util.Timer;
import net.minecraft.util.Vec3;
import org.apache.logging.log4j.Logger;

/**
 * What Mixin generates for the Mayu {@code myau.mixin.Accessor*} interfaces ({@code @Accessor} = field get/set,
 * {@code @Invoker} = method call on the target). Mixin adds the interface to the target class at load time; Rice patches
 * classes that are already loaded, where the JVM forbids adding interfaces, so tools/MayuBytecode rewrites each
 * {@code invokeinterface myau/mixin/AccessorX.m} to {@code invokestatic MayuAccessors.AccessorX_m(target, ...)} (and the
 * matching checkcast to the target class). Names resolve through Rice's mapping-aware reflection.
 */
public final class MayuAccessors {
   private static final java.util.Map<String, Field> FIELDS = new java.util.HashMap<>();
   private static final java.util.Map<String, Method> METHODS = new java.util.HashMap<>();

   private MayuAccessors() {
   }

   private static Field field(Class<?> owner, String named, String srg) {
      return FIELDS.computeIfAbsent(owner.getName() + '.' + named, key -> ReflectionHelper.findField(owner, named, srg));
   }

   private static Method method(Class<?> owner, String named, String srg, Class<?>... parameters) {
      return METHODS.computeIfAbsent(owner.getName() + '.' + named, key -> ReflectionHelper.findMethod((Class<Object>)owner, null, new String[]{named, srg}, parameters));
   }

   private static Object get(Field field, Object target) {
      try {
         return field.get(target);
      } catch (IllegalAccessException error) {
         throw new IllegalStateException(error);
      }
   }

   private static void set(Field field, Object target, Object value) {
      try {
         field.set(target, value);
      } catch (IllegalAccessException error) {
         throw new IllegalStateException(error);
      }
   }

   private static Object invoke(Method method, Object target, Object... arguments) {
      try {
         return method.invoke(target, arguments);
      } catch (ReflectiveOperationException error) {
         throw new IllegalStateException(error);
      }
   }

   // AccessorEntity (@Mixin Entity)
   public static boolean AccessorEntity_getIsInWeb(Entity self) {
      return (Boolean)get(field(Entity.class, "isInWeb", "field_70134_J"), self);
   }

   public static Vec3 AccessorEntity_callGetVectorForRotation(Entity self, float pitch, float yaw) {
      return (Vec3)invoke(method(Entity.class, "getVectorForRotation", "func_174806_f", float.class, float.class), self, pitch, yaw);
   }

   // AccessorEntityPlayer (@Mixin EntityPlayer)
   public static ItemStack AccessorEntityPlayer_getItemInUse(EntityPlayer self) {
      return (ItemStack)get(field(EntityPlayer.class, "itemInUse", "field_71074_e"), self);
   }

   public static void AccessorEntityPlayer_setItemInUse(EntityPlayer self, ItemStack value) {
      set(field(EntityPlayer.class, "itemInUse", "field_71074_e"), self, value);
   }

   public static int AccessorEntityPlayer_getItemInUseCount(EntityPlayer self) {
      return (Integer)get(field(EntityPlayer.class, "itemInUseCount", "field_71072_f"), self);
   }

   public static void AccessorEntityPlayer_setItemInUseCount(EntityPlayer self, int value) {
      set(field(EntityPlayer.class, "itemInUseCount", "field_71072_f"), self, value);
   }

   // AccessorEntityRenderer (@Mixin EntityRenderer)
   public static void AccessorEntityRenderer_callSetupCameraTransform(EntityRenderer self, float partialTicks, int pass) {
      invoke(method(EntityRenderer.class, "setupCameraTransform", "func_78479_a", float.class, int.class), self, partialTicks, pass);
   }

   // AccessorGuiChat (@Mixin GuiChat)
   public static GuiTextField AccessorGuiChat_getInputField(GuiChat self) {
      return (GuiTextField)get(field(GuiChat.class, "inputField", "field_146415_a"), self);
   }

   // AccessorMinecraft (@Mixin Minecraft)
   public static Logger AccessorMinecraft_getLogger(Minecraft self) {
      return (Logger)get(field(Minecraft.class, "logger", "field_147123_G"), null);
   }

   public static Timer AccessorMinecraft_getTimer(Minecraft self) {
      return (Timer)get(field(Minecraft.class, "timer", "field_71428_T"), self);
   }

   public static int AccessorMinecraft_getRightClickDelayTimer(Minecraft self) {
      return (Integer)get(field(Minecraft.class, "rightClickDelayTimer", "field_71467_ac"), self);
   }

   public static void AccessorMinecraft_setRightClickDelayTimer(Minecraft self, int value) {
      set(field(Minecraft.class, "rightClickDelayTimer", "field_71467_ac"), self, value);
   }

   // AccessorPlayerControllerMP (@Mixin PlayerControllerMP)
   public static float AccessorPlayerControllerMP_getCurBlockDamageMP(PlayerControllerMP self) {
      return (Float)get(field(PlayerControllerMP.class, "curBlockDamageMP", "field_78770_f"), self);
   }

   public static void AccessorPlayerControllerMP_setCurBlockDamageMP(PlayerControllerMP self, float value) {
      set(field(PlayerControllerMP.class, "curBlockDamageMP", "field_78770_f"), self, value);
   }

   public static int AccessorPlayerControllerMP_getBlockHitDelay(PlayerControllerMP self) {
      return (Integer)get(field(PlayerControllerMP.class, "blockHitDelay", "field_78781_i"), self);
   }

   public static void AccessorPlayerControllerMP_setBlockHitDelay(PlayerControllerMP self, int value) {
      set(field(PlayerControllerMP.class, "blockHitDelay", "field_78781_i"), self, value);
   }

   public static boolean AccessorPlayerControllerMP_getIsHittingBlock(PlayerControllerMP self) {
      return (Boolean)get(field(PlayerControllerMP.class, "isHittingBlock", "field_78778_j"), self);
   }

   public static int AccessorPlayerControllerMP_getCurrentPlayerItem(PlayerControllerMP self) {
      return (Integer)get(field(PlayerControllerMP.class, "currentPlayerItem", "field_78777_l"), self);
   }

   public static void AccessorPlayerControllerMP_setCurrentPlayerItem(PlayerControllerMP self, int value) {
      set(field(PlayerControllerMP.class, "currentPlayerItem", "field_78777_l"), self, value);
   }

   public static void AccessorPlayerControllerMP_callSyncCurrentPlayItem(PlayerControllerMP self) {
      invoke(method(PlayerControllerMP.class, "syncCurrentPlayItem", "func_78750_j"), self);
   }

   // AccessorRenderManager (@Mixin RenderManager)
   public static double AccessorRenderManager_getRenderPosX(RenderManager self) {
      return (Double)get(field(RenderManager.class, "renderPosX", "field_78725_b"), self);
   }

   public static double AccessorRenderManager_getRenderPosY(RenderManager self) {
      return (Double)get(field(RenderManager.class, "renderPosY", "field_78726_c"), self);
   }

   public static double AccessorRenderManager_getRenderPosZ(RenderManager self) {
      return (Double)get(field(RenderManager.class, "renderPosZ", "field_78723_d"), self);
   }

   // AccessorC14PacketTabComplete (@Mixin C14PacketTabComplete)
   public static void AccessorC14PacketTabComplete_setMessage(C14PacketTabComplete self, String value) {
      set(field(C14PacketTabComplete.class, "message", "field_149420_a"), self, value);
   }
}

package myau.host;

import dev.rice.inject.ReflectionHelper;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

/**
 * Targets of the post-compile adapters (tools/MayuBytecode, tools/mayu-adapters.json) for the Forge-only members the
 * unmodified Mayu sources use. Upstream is a Forge mod; Rice also runs on vanilla 1.8.9. Each method calls the real
 * Forge member when Forge is loaded, and otherwise returns exactly what Forge returns for vanilla content.
 */
public final class MayuForge {
   private static final Object MISSING = new Object();
   private static EnumFacing[] values;
   private static Object digSpeed;
   private static Object toolClasses;
   private static Object metadata;
   private static Object attackTarget;

   private MayuForge() {
   }

   /** {@code EnumFacing.VALUES}: Forge's access transformer makes the private field public; this returns that same array. */
   public static EnumFacing[] enumFacingValues() {
      if (values == null) {
         try {
            Field field = ReflectionHelper.findField(EnumFacing.class, "VALUES", "field_82609_l");
            values = (EnumFacing[])field.get(null);
         } catch (IllegalAccessException error) {
            throw new IllegalStateException(error);
         }
      }

      return values;
   }

   private static Object forge(Class<?> owner, String name, Class<?>... parameters) {
      try {
         Method method = owner.getMethod(name, parameters);
         method.setAccessible(true);
         return method;
      } catch (NoSuchMethodException absent) {
         return MISSING;
      }
   }

   private static Object call(Object method, Object receiver, Object... arguments) {
      try {
         return ((Method)method).invoke(receiver, arguments);
      } catch (ReflectiveOperationException error) {
         throw new IllegalStateException(error);
      }
   }

   /**
    * Forge {@code Item.getDigSpeed(ItemStack, IBlockState)}. Forge: an ItemTool returns its efficiency when one of its
    * tool classes is effective on the state, otherwise (and for every other item) {@code getStrVsBlock(stack, block)}.
    * Forge's vanilla tool/harvest tables never make a tool effective where vanilla getStrVsBlock is not, so without
    * Forge the result is {@code getStrVsBlock}.
    */
   public static float getDigSpeed(Item item, ItemStack stack, IBlockState state) {
      if (digSpeed == null) digSpeed = forge(Item.class, "getDigSpeed", ItemStack.class, IBlockState.class);
      return digSpeed != MISSING ? (Float)call(digSpeed, item, stack, state) : item.getStrVsBlock(stack, state.getBlock());
   }

   /** Forge {@code Item.getToolClasses(ItemStack)}: Forge's ItemTool constructor registers pickaxe/axe/shovel by type. */
   @SuppressWarnings("unchecked")
   public static Set<String> getToolClasses(Item item, ItemStack stack) {
      if (toolClasses == null) toolClasses = forge(Item.class, "getToolClasses", ItemStack.class);
      if (toolClasses != MISSING) {
         return (Set<String>)call(toolClasses, item, stack);
      }

      if (item instanceof ItemPickaxe) {
         return Collections.singleton("pickaxe");
      } else if (item instanceof ItemAxe) {
         return Collections.singleton("axe");
      } else {
         return item instanceof ItemSpade ? Collections.singleton("shovel") : Collections.<String>emptySet();
      }
   }

   /** Forge {@code Item.getMetadata(ItemStack)}: the stack's damage value, as vanilla {@code ItemStack.getMetadata()}. */
   public static int getMetadata(Item item, ItemStack stack) {
      if (metadata == null) metadata = forge(Item.class, "getMetadata", ItemStack.class);
      return metadata != MISSING ? (Integer)call(metadata, item, stack) : stack.getMetadata();
   }

   /** Forge {@code ForgeHooks.onPlayerAttackTarget}: true unless a Forge listener cancels the AttackEntityEvent. */
   public static boolean onPlayerAttackTarget(EntityPlayer player, Entity target) {
      if (attackTarget == null) {
         try {
            Class<?> hooks = Class.forName("net.minecraftforge.common.ForgeHooks", false, Minecraft.class.getClassLoader());
            attackTarget = forge(hooks, "onPlayerAttackTarget", EntityPlayer.class, Entity.class);
         } catch (ClassNotFoundException absent) {
            attackTarget = MISSING;
         }
      }

      return attackTarget == MISSING || (Boolean)call(attackTarget, null, player, target);
   }
}

package dev.rice.jade.mixin.impl.accessor;

import dev.rice.inject.ReflectionHelper;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.multiplayer.PlayerControllerMP;

/**
 * Rice host replacement for Jade's PlayerControllerMP mixin accessor; {@link #of(Object)} replaces upstream's
 * cast and every member is backed by Rice's mapped reflection.
 */
public interface IAccessorPlayerControllerMP {
   void callSyncCurrentPlayItem();

   void setIsHittingBlock(boolean hitting);

   boolean getIsHittingBlock();

   void setBlockHitDelay(int ticks);

   void setCurBlockDamageMP(float damage);

   float getCurBlockDamageMP();

   static IAccessorPlayerControllerMP of(Object controller) {
      final PlayerControllerMP target = (PlayerControllerMP)controller;
      return new IAccessorPlayerControllerMP() {
         @Override
         public void callSyncCurrentPlayItem() {
            try {
               Members.SYNC.invoke(target);
            } catch (ReflectiveOperationException e) {
               throw new IllegalStateException(e);
            }
         }

         @Override
         public void setIsHittingBlock(boolean hitting) {
            Members.set(Members.HITTING, target, hitting);
         }

         @Override
         public boolean getIsHittingBlock() {
            return (Boolean)Members.get(Members.HITTING, target);
         }

         @Override
         public void setBlockHitDelay(int ticks) {
            Members.set(Members.DELAY, target, ticks);
         }

         @Override
         public void setCurBlockDamageMP(float damage) {
            Members.set(Members.DAMAGE, target, damage);
         }

         @Override
         public float getCurBlockDamageMP() {
            return ((Number)Members.get(Members.DAMAGE, target)).floatValue();
         }
      };
   }

   final class Members {
      static final Field HITTING = ReflectionHelper.findField(PlayerControllerMP.class, "isHittingBlock", "field_78778_j");
      static final Field DAMAGE = ReflectionHelper.findField(PlayerControllerMP.class, "curBlockDamageMP", "field_78770_f");
      static final Field DELAY = ReflectionHelper.findField(PlayerControllerMP.class, "blockHitDelay", "field_78781_i");
      static final Method SYNC = ReflectionHelper.findMethod(PlayerControllerMP.class, (PlayerControllerMP)null, new String[]{"syncCurrentPlayItem", "func_78750_j"});

      private Members() {
      }

      static Object get(Field field, Object owner) {
         try {
            return field.get(owner);
         } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
         }
      }

      static void set(Field field, Object owner, Object value) {
         try {
            field.set(owner, value);
         } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
         }
      }
   }
}

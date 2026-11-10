// Jade recovery: original class: jade.deps.eLz.PsUd8OATua
package dev.rice.jade.client.common;

import java.lang.reflect.Method;
import net.minecraft.entity.Entity;

public final class AttackHelper {
   private static final Method method = findCanRiderInteractMethod();

   private static Method findCanRiderInteractMethod() {
      try {
         return Entity.class.getMethod("canRiderInteract");
      } catch (ReflectiveOperationException var1) {
         return null;
      }
   }

   public static boolean invokeCanRiderInteract(Entity var0) {
      Method var1 = method;
      if (var0 != null && var1 != null) {
         try {
            Object var2 = var1.invoke(var0);
            return var2 instanceof Boolean && (Boolean)var2;
         } catch (ReflectiveOperationException var3) {
            return false;
         }
      } else {
         return false;
      }
   }
}

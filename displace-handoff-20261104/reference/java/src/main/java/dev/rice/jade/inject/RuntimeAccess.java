package dev.rice.jade.inject;

import dev.rice.inject.ReflectionHelper;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rice host shim for Jade's mapped reflection.
 *
 * <p>Upstream jade.inject.RuntimeAccess resolveMappedField/resolveMappedMethod:
 * <ul>
 *   <li>walks the class and every superclass;</li>
 *   <li>tries the mapped name, then the given {MCP, SRG} names, on each declaring class;</li>
 *   <li>caches hits by (type, first name[, parameters]).</li>
 * </ul>
 * Here each class in the chain is resolved through Rice's ReflectionHelper, so the same lookups work in named,
 * SRG and official runtimes, including fields and methods inherited from Entity / EntityLivingBase.
 */
public final class RuntimeAccess {
   private static final Map<String, Field> FIELDS = new ConcurrentHashMap<>();
   private static final Map<String, Method> METHODS = new ConcurrentHashMap<>();

   private RuntimeAccess() {
   }

   public static Field resolveMappedField(Class<?> type, String... sourceNames) {
      if (type == null || sourceNames == null || sourceNames.length == 0) {
         return null;
      }

      String cacheKey = type.getName() + "#mapped-field#" + sourceNames[0];
      Field cached = FIELDS.get(cacheKey);
      if (cached != null) {
         return cached;
      }

      for (Class<?> current = type; current != null; current = current.getSuperclass()) {
         try {
            Field field = ReflectionHelper.findField(current, sourceNames);
            field.setAccessible(true);
            FIELDS.put(cacheKey, field);
            return field;
         } catch (Throwable notDeclaredHere) {
         }
      }

      return null;
   }

   public static Method resolveMappedMethod(Class<?> type, Class<?>[] parameterTypes, String... sourceNames) {
      if (type == null || sourceNames == null || sourceNames.length == 0) {
         return null;
      }

      Class<?>[] parameters = parameterTypes == null ? new Class<?>[0] : parameterTypes;
      String cacheKey = type.getName() + "#mapped-method#" + sourceNames[0] + Arrays.toString(parameters);
      Method cached = METHODS.get(cacheKey);
      if (cached != null) {
         return cached;
      }

      for (Class<?> current = type; current != null; current = current.getSuperclass()) {
         try {
            Method method = findDeclared(current, sourceNames, parameters);
            method.setAccessible(true);
            METHODS.put(cacheKey, method);
            return method;
         } catch (Throwable notDeclaredHere) {
         }
      }

      return null;
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   private static Method findDeclared(Class<?> type, String[] names, Class<?>[] parameters) {
      return ReflectionHelper.findMethod((Class)type, null, names, parameters);
   }
}

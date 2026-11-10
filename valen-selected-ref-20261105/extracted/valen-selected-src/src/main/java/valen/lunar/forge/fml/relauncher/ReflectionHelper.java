/*
 * Decompiled with CFR 0.152.
 */
package valen.lunar.forge.fml.relauncher;

import java.lang.reflect.Field;

public final class ReflectionHelper {
    public static Field findField(Class<?> clazz, String ... stringArray) {
        for (String string : stringArray) {
            try {
                Field field = clazz.getDeclaredField(string);
                field.setAccessible(true);
                return field;
            }
            catch (NoSuchFieldException noSuchFieldException) {
            }
        }
        throw new IllegalArgumentException("Field not found in " + clazz.getName());
    }
}


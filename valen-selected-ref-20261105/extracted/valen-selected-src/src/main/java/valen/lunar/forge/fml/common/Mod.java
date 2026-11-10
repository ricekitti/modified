/*
 * Decompiled with CFR 0.152.
 */
package valen.lunar.forge.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface Mod {
    public String modid();

    public String name() default "";

    public String version() default "";

    public String acceptedMinecraftVersions() default "";

    public String acceptableRemoteVersions() default "";

    public boolean clientSideOnly() default true;

    @Retention(value=RetentionPolicy.RUNTIME)
    @Target(value={ElementType.METHOD})
    public static @interface EventHandler {
    }
}


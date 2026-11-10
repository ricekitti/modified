/*
 * Decompiled with CFR 0.152.
 */
package valen.lunar.forge.fml.common.eventhandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import valen.lunar.forge.fml.common.eventhandler.EventPriority;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD})
public @interface SubscribeEvent {
    public EventPriority priority() default EventPriority.NORMAL;

    public boolean receiveCanceled() default false;
}


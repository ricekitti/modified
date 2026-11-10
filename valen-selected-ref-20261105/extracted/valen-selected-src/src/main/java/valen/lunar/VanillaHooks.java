/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.RenderEntityItem
 *  net.minecraft.entity.Entity
 */
package valen.lunar;

import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.entity.Entity;

public final class VanillaHooks {
    public static boolean canRiderInteract(Entity entity) {
        return false;
    }

    public static boolean shouldSpreadItems(RenderEntityItem renderEntityItem) {
        return true;
    }
}


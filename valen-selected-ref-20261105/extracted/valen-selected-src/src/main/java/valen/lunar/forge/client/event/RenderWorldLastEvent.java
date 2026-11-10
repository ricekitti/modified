/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.RenderGlobal
 */
package valen.lunar.forge.client.event;

import net.minecraft.client.renderer.RenderGlobal;
import valen.lunar.forge.fml.common.eventhandler.Event;

public class RenderWorldLastEvent
extends Event {
    public final RenderGlobal context;
    public final float partialTicks;

    public RenderWorldLastEvent(RenderGlobal renderGlobal, float f) {
        this.context = renderGlobal;
        this.partialTicks = f;
    }
}


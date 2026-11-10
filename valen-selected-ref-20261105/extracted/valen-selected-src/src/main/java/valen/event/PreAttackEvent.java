/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.MovingObjectPosition
 */
package valen.event;

import net.minecraft.util.MovingObjectPosition;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;
import valen.lunar.forge.fml.common.eventhandler.Event;

@Cancelable
public class PreAttackEvent
extends Event {
    public final MovingObjectPosition objectMouseOver;

    public PreAttackEvent(MovingObjectPosition movingObjectPosition) {
        this.objectMouseOver = movingObjectPosition;
    }
}


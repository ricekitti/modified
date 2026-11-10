/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.component.impl.player.RotationComponent
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.motion.PreMotionEvent
 *  com.alan.clients.value.Mode
 *  net.minecraft.util.MathHelper
 */
package com.alan.clients.module.impl.player.scaffold.sprint;

import com.alan.clients.component.impl.player.RotationComponent;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.value.Mode;
import net.minecraft.util.MathHelper;

public class LegitSprint
extends Mode<Scaffold> {
    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = var0 -> {
        if (Math.abs(MathHelper.wrapAngleTo180_float((float)LegitSprint.aEg.thePlayer.pl) - MathHelper.wrapAngleTo180_float((float)RotationComponent.fk.x)) > 90.0f) {
            LegitSprint.aEg.gameSettings.cgG.setPressed(false);
            LegitSprint.aEg.thePlayer.setSprinting(false);
        }
    };

    public LegitSprint(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }
}

/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.motion.StrafeEvent
 *  com.alan.clients.value.Mode
 */
package com.alan.clients.module.impl.player.scaffold.sprint;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.value.Mode;

public class DisabledSprint
extends Mode<Scaffold> {
    @EventLink(value=0)
    public final Listener<StrafeEvent> onPreMotionEvent = var0 -> {
        DisabledSprint.aEg.gameSettings.cgG.setPressed(false);
        DisabledSprint.aEg.thePlayer.setSprinting(false);
    };

    public DisabledSprint(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }
}

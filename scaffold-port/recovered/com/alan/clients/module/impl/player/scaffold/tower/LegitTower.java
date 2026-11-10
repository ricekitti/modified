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
package com.alan.clients.module.impl.player.scaffold.tower;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.value.Mode;

public class LegitTower
extends Mode<Scaffold> {
    @EventLink
    public final Listener<StrafeEvent> onStrafe = var0 -> {
        if (LegitTower.aEg.gameSettings.keyBindJump.isKeyDown() && LegitTower.aEg.thePlayer.onGround) {
            LegitTower.aEg.thePlayer.jump();
        }
    };

    public LegitTower(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }
}

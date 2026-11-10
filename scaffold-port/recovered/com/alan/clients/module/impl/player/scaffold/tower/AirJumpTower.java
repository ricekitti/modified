/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.motion.PreMotionEvent
 *  com.alan.clients.util.player.PlayerUtil
 *  com.alan.clients.value.Mode
 */
package com.alan.clients.module.impl.player.scaffold.tower;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.player.PlayerUtil;
import com.alan.clients.value.Mode;

public class AirJumpTower
extends Mode<Scaffold> {
    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = var0 -> {
        if (AirJumpTower.aEg.gameSettings.keyBindJump.isKeyDown() && AirJumpTower.aEg.thePlayer.ticksExisted % 2 == 0 && PlayerUtil.ay((int)2)) {
            AirJumpTower.aEg.thePlayer.motionY = 0.42f;
            var0.setOnGround(true);
        }
    };

    public AirJumpTower(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }
}

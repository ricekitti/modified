/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.motion.PreMotionEvent
 *  com.alan.clients.newevent.impl.motion.StrafeEvent
 *  com.alan.clients.util.player.PlayerUtil
 *  com.alan.clients.value.Mode
 *  net.minecraft.potion.Potion
 */
package com.alan.clients.module.impl.player.scaffold.tower;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.util.player.PlayerUtil;
import com.alan.clients.value.Mode;
import net.minecraft.potion.Potion;

public class VerusTower
extends Mode<Scaffold> {
    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = var0 -> {
        if (VerusTower.aEg.gameSettings.keyBindJump.isKeyDown() && PlayerUtil.ay((int)1)) {
            VerusTower.aEg.thePlayer.motionY = 0.8f;
        }
    };
    @EventLink
    public final Listener<StrafeEvent> onStrafe = var0 -> {
        if (VerusTower.aEg.thePlayer.motionY < 0.77) {
            VerusTower.aEg.thePlayer.isPotionActive(Potion.moveSpeed);
        }
    };

    public VerusTower(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }
}

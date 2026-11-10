/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.motion.PreUpdateEvent
 *  com.alan.clients.value.Mode
 *  org.lwjgl.input.Keyboard
 */
package com.alan.clients.module.impl.player.scaffold.downwards;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.value.Mode;
import org.lwjgl.input.Keyboard;

public class NormalDownwards
extends Mode<Scaffold> {
    @EventLink(value=1)
    public final Listener<PreUpdateEvent> onPreUpdate = var1x -> {
        if (Keyboard.isKeyDown((int)NormalDownwards.aEg.gameSettings.keyBindSneak.getKeyCode())) {
            ((Scaffold)this.getParent()).placeOffset = ((Scaffold)this.getParent()).placeOffset.v(0.0, -1.0, 0.0);
        }
    };

    public NormalDownwards(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }
}

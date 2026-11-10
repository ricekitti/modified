/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.motion.PreMotionEvent
 *  com.alan.clients.util.player.MoveUtil
 *  com.alan.clients.value.Mode
 *  com.viaversion.viaversion.api.protocol.version.ProtocolVersion
 *  de.florianmichael.vialoadingbase.ViaLoadingBase
 */
package com.alan.clients.module.impl.player.scaffold.tower;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.value.Mode;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;

public class WatchdogPrediction18Tower
extends Mode<Scaffold> {
    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = var0 -> {
        if (WatchdogPrediction18Tower.aEg.gameSettings.keyBindJump.isKeyDown()) {
            if (WatchdogPrediction18Tower.aEg.thePlayer.onGround && !MoveUtil.enoughMovementForSprinting()) {
                WatchdogPrediction18Tower.aEg.thePlayer.jump();
            }
            if (ViaLoadingBase.getInstance().getTargetVersion().olderThan(ProtocolVersion.v1_18)) {
                if (WatchdogPrediction18Tower.aEg.thePlayer.tR == 4 && MoveUtil.speed() == 0.0) {
                    WatchdogPrediction18Tower.aEg.thePlayer.motionY -= 0.03;
                }
                if (WatchdogPrediction18Tower.aEg.thePlayer.tR == 5 && MoveUtil.speed() == 0.0) {
                    WatchdogPrediction18Tower.aEg.thePlayer.motionY -= 0.5;
                }
            }
        }
    };

    public WatchdogPrediction18Tower(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }
}

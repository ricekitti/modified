/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.Client
 *  com.alan.clients.module.impl.exploit.Disabler
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.motion.JumpEvent
 *  com.alan.clients.newevent.impl.motion.PreMotionEvent
 *  com.alan.clients.util.interfaces.InstanceAccess
 *  com.alan.clients.util.packet.PacketUtil
 *  com.alan.clients.util.player.MoveUtil
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C03PacketPlayer
 */
package com.alan.clients.module.impl.player.scaffold.sprint;

import com.alan.clients.Client;
import com.alan.clients.module.impl.exploit.Disabler;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.JumpEvent;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.player.MoveUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

public final class WatchdogJumpSprintHelper
implements InstanceAccess {
    private Scaffold scaffold;
    private static final double Y_OFFSET = 0.001;
    private boolean active;
    @EventLink(value=3)
    public final Listener<PreMotionEvent> onPreMotion = var1x -> {
        if (!this.shouldRun()) {
            this.unregister();
        } else {
            if (!var1x.isOnGround() && !WatchdogJumpSprintHelper.aEg.thePlayer.onGround) {
                if (!this.scaffold.isEnabled()) {
                    this.unregister();
                }
            } else {
                if (!((Boolean)((Disabler)this.e(Disabler.class)).watchdogFly.wo()).booleanValue() || !((Disabler)this.e(Disabler.class)).isEnabled()) {
                    if (WatchdogJumpSprintHelper.aEg.thePlayer.ticksExisted % 3 == 0 && WatchdogJumpSprintHelper.aEg.thePlayer.cqL > 2 && !WatchdogJumpSprintHelper.aEg.gameSettings.keyBindJump.isKeyDown()) {
                        if (((Scaffold)this.e(Scaffold.class)).isEnabled() && !WatchdogJumpSprintHelper.aEg.gameSettings.cgI.isKeyDown() && !WatchdogJumpSprintHelper.aEg.gameSettings.keyBindSneak.isKeyDown()) {
                            var1x.setPosY(var1x.getPosY() + 0.001);
                        }
                    } else if (((Scaffold)this.e(Scaffold.class)).isEnabled()) {
                        // empty if block
                    }
                }
                this.active = true;
            }
            if (((Scaffold)this.e(Scaffold.class)).isEnabled() && WatchdogJumpSprintHelper.aEg.gameSettings.keyBindSneak.isPressed() && WatchdogJumpSprintHelper.aEg.thePlayer.ticksExisted % 3 == 1) {
                WatchdogJumpSprintHelper.aEg.gameSettings.keyBindSneak.setPressed(false);
            }
            if (((Scaffold)this.e(Scaffold.class)).isEnabled() && WatchdogJumpSprintHelper.aEg.gameSettings.keyBindSneak.isPressed()) {
                MoveUtil.stop();
                WatchdogJumpSprintHelper.aEg.timer.dzD = 0.5f;
                PacketUtil.send((Packet)new C03PacketPlayer(WatchdogJumpSprintHelper.aEg.thePlayer.onGround));
            }
        }
    };
    @EventLink(value=4)
    public final Listener<JumpEvent> onJump = var1x -> {
        if (WatchdogJumpSprintHelper.aEg.thePlayer.ticksExisted % 3 == 1 && WatchdogJumpSprintHelper.aEg.thePlayer.cqL > 2 && ((Scaffold)this.e(Scaffold.class)).isEnabled()) {
            var1x.setCancelled();
        }
    };

    public WatchdogJumpSprintHelper(Scaffold scaffold) {
        this.scaffold = scaffold;
    }

    private void unregister() {
        Client.a.e().c((Object)this);
        this.active = false;
    }

    private boolean shouldRun() {
        return this.scaffold.isEnabled() || this.active;
    }

    public void onEnable() {
        Client.a.e().b((Object)this);
    }
}

package hackclient.rise;

import hackclient.rise.aka;
import hackclient.rise.am;
import net.minecraft.entity.c;
import net.minecraft.network.aj;
import net.minecraft.network.play.client.k;
import net.minecraft.network.play.server.i;
import net.minecraft.network.play.server.t;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.AttackEvent;
import rise.event.impl.PacketReceiveEvent;
import rise.event.impl.PacketSendEvent;

public class ao
extends am {
    @EventLink(priority=0)
    final public Listener<PacketSendEvent> bC = packetSendEvent -> {
        if (mc == null || ao.mc.bfW == null || packetSendEvent.isCancelled()) {
            return;
        }
        aj<?> aj2 = packetSendEvent.dq();
        if (aj2 instanceof k && !((k)aj2).xX().h(new aka(-1.0, -1.0, -1.0))) {
            ao.mc.thePlayer.crL = 0;
        }
    };
    @EventLink(priority=0)
    final public Listener<AttackEvent> bD = attackEvent -> {
        ao.mc.thePlayer.aY = 0;
    };
    @EventLink(priority=0)
    final public Listener<PacketReceiveEvent> bE = packetReceiveEvent -> {
        if (mc == null || ao.mc.bfW == null || packetReceiveEvent.isCancelled()) {
            return;
        }
        aj<?> aj2 = packetReceiveEvent.dq();
        if (aj2 instanceof t) {
            t t2 = (t)aj2;
            c c2 = ao.mc.bfW.cn(t2.getEntityID());
            if (c2 == null) {
                return;
            }
            c2.crM = (double)t2.dcj / 8000.0;
            c2.crN = (double)t2.dck / 8000.0;
            c2.crO = (double)t2.dcl / 8000.0;
            c2.ae = 0;
            if ((double)t2.dck / 8000.0 > 0.1 && Math.hypot((double)t2.dcl / 8000.0, (double)t2.dcj / 8000.0) > 0.2) {
                c2.crK = 0;
            }
        } else if (aj2 instanceof i) {
            ao.mc.thePlayer.Zl = 0;
        }
    };
}



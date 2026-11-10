package rise.module.impl.movement.noslow;

import hackclient.rise.afi;
import hackclient.rise.ahj;
import hackclient.rise.bm;
import hackclient.rise.bu;
import net.minecraft.item.bg;
import net.minecraft.network.aj;
import net.minecraft.network.play.client.i;
import net.minecraft.network.play.client.k;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PacketSendEvent;
import rise.event.impl.PreUpdateEvent;
import rise.event.impl.SlowDownEvent;
import rise.module.Mode;
import rise.module.impl.movement.NoSlow;

public class PdMode
extends Mode<NoSlow> {
    private boolean enabled;
    @EventLink
    final public Listener<PreUpdateEvent> Nb = preUpdateEvent -> {
        block5: {
            block4: {
                if (!PdMode.mc.thePlayer.isUsingItem()) break block4;
                this.d(bu.class);
                if (bu.bO().bP() instanceof bg) break block5;
            }
            return;
        }
        bm.blink();
        if (PdMode.mc.thePlayer.cHR < -1) {
            PdMode.mc.aIp.cgM.ay(false);
        }
    };
    @EventLink
    final public Listener<PacketSendEvent> Nc = packetSendEvent -> {
        aj<?> aj2 = packetSendEvent.dq();
        if (aj2 instanceof i) {
            i i2 = (i)aj2;
            afi.b("digging " + String.valueOf((Object)i2.afI()) + " " + packetSendEvent.isCancelled(), new Object[0]);
            this.enabled = false;
        } else if (!this.enabled && aj2 instanceof k) {
            this.d(bu.class);
            if (bu.bO() != null) {
                this.d(bu.class);
                if (bu.bO().bP() instanceof bg) {
                    this.enabled = true;
                    bm.blink();
                    bm.enabled = true;
                    packetSendEvent.cF();
                    ahj.m(aj2);
                    afi.b("Started", new Object[0]);
                }
            }
        }
    };
    @EventLink
    final public Listener<SlowDownEvent> Nd = slowDownEvent -> {
        this.d(bu.class);
        if (bu.bO() != null) {
            this.d(bu.class);
            if (bu.bO().bP() instanceof bg) {
                slowDownEvent.cF();
            }
        }
    };

    public PdMode(String string, NoSlow noSlow) {
        super(string, noSlow);
    }
}



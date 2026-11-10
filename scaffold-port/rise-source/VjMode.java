package rise.module.impl.player.scaffold;

import hackclient.rise.ahj;
import hackclient.rise.aif;
import net.minecraft.network.aj;
import net.minecraft.network.play.client.k;
import net.minecraft.network.play.client.n;
import net.minecraft.network.play.client.o;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PacketSendEvent;
import rise.event.impl.PreMotionEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;
import rise.value.NumberValue;

public class VjMode
extends Mode<Scaffold> {
    final private NumberValue ajq = new NumberValue("Speed", this, (Number)1.3, (Number)0.9, (Number)1.3, (Number)0.05);
    private int dm;
    private int ajr;
    @EventLink
    final public Listener<PreMotionEvent> ajs = preMotionEvent -> {
        VjMode.mc.thePlayer.setSprinting(false);
        double d2 = ((Number)this.ajq.getValue()).doubleValue();
        if (!VjMode.mc.aIp.cgI.QQ() && d2 > 0.9 && VjMode.mc.thePlayer.cqP >= 2 && this.ajr <= 10) {
            aif.strafe(aif.getAllowedHorizontalDistance() * d2);
            VjMode.mc.thePlayer.jump();
            VjMode.mc.thePlayer.ue = 0.012500047683714;
        }
        if (VjMode.mc.thePlayer.aO) {
            aif.strafe();
        }
        ++this.dm;
        ++this.ajr;
        switch (this.dm) {
            case 1: {
                ahj.l(new n(VjMode.mc.thePlayer, o.daH));
                break;
            }
            case 10: {
                this.dm = 0;
            }
        }
    };
    @EventLink
    final public Listener<PacketSendEvent> ajt = packetSendEvent -> {
        k k2;
        aj<?> aj2 = packetSendEvent.dq();
        if (aj2 instanceof k && (k2 = (k)aj2).getPlacedBlockDirection() != 255) {
            this.ajr = 0;
        }
    };

    public VjMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }

    @Override
    public void onDisable() {
        if (this.dm == 1) {
            ahj.l(new n(VjMode.mc.thePlayer, o.daI));
        }
    }
}



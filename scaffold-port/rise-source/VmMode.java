package rise.module.impl.player.scaffold;

import hackclient.rise.afi;
import hackclient.rise.ahj;
import hackclient.rise.aif;
import hackclient.rise.bs;
import hackclient.rise.bu;
import hackclient.rise.cb;
import net.minecraft.network.aj;
import net.minecraft.network.play.client.e;
import net.minecraft.network.play.client.g;
import net.minecraft.network.play.client.h;
import net.minecraft.network.play.client.k;
import net.minecraft.network.play.server.an;
import net.minecraft.util.b;
import net.minecraft.util.bt;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.MoveInputEvent;
import rise.event.impl.PacketReceiveEvent;
import rise.event.impl.PacketSendEvent;
import rise.event.impl.PreUpdateEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;
import rise.util.math.Vector2f;

public class VmMode
extends Mode<Scaffold> {
    private float gZ;
    private boolean dk;
    private boolean El;
    private b Em;
    private int En;
    @EventLink
    final public Listener<PreUpdateEvent> ajL = preUpdateEvent -> {
        float f2 = bt.bN(this.gZ);
        bs.d(false);
        bs.a(new Vector2f(this.gZ, 94.0f), 10.0, cb.gH);
    };
    @EventLink
    final public Listener<PacketSendEvent> ajM = packetSendEvent -> {
        e e2;
        aj<?> aj2 = packetSendEvent.dq();
        if (aj2 instanceof e && ((e2 = (e)aj2) instanceof g || e2 instanceof h) && Math.abs(e2.getPitch() - 90.0f) < 0.001f) {
            e2.setPitch(94.0f);
        }
    };
    @EventLink
    final public Listener<PacketReceiveEvent> ajN = packetReceiveEvent -> {
        aj<?> aj2 = packetReceiveEvent.dq();
        if (VmMode.mc.thePlayer.aQx >= this.En) {
            this.dk = false;
        }
        if (aj2 instanceof an) {
            an an2 = (an)aj2;
            b b2 = an2.agW();
            if (!b2.equals(this.Em)) {
                this.Em = b2;
                this.En = VmMode.mc.thePlayer.aQx + 10;
            }
            if (this.El) {
                this.dk = true;
                this.El = false;
            }
        }
    };
    @EventLink
    final public Listener<MoveInputEvent> ajO = moveInputEvent -> {
        if (this.dk) {
            moveInputEvent.setSneak(true);
        }
        if (this.dk) {
            moveInputEvent.setForward(0.0f);
        }
    };

    public VmMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }

    @Override
    public void onEnable() {
        this.En = VmMode.mc.thePlayer.aQx + 10;
        this.El = true;
        this.dk = true;
        this.d(bu.class);
        ahj.l(new k(bu.bO()));
        this.d(bu.class);
        ahj.l(new k(bu.bO()));
        this.d(bu.class);
        ahj.l(new k(bu.bO()));
        float f2 = bt.bN(this.gZ);
        bs.d(false);
        double d2 = bt.aJ(Math.toDegrees(aif.direction()));
        double d3 = bt.aJ(Math.toDegrees(Math.atan2(VmMode.mc.thePlayer.uf, VmMode.mc.thePlayer.ud)) - 90.0);
        boolean bl2 = Math.abs(d2 % 90.0) <= 10.0 || Math.abs(d2 % 90.0) >= 80.0;
        double d4 = (double)Math.round(d2 / 45.0) * 45.0;
        VmMode.mc.thePlayer.pl = (float)d4;
        afi.c(d2, new Object[0]);
        this.gZ = bl2 && d2 >= -10.0 && d2 <= 100.0 && VmMode.mc.thePlayer.aO ? VmMode.mc.thePlayer.pl + 43.0f : (bl2 && VmMode.mc.thePlayer.aO ? VmMode.mc.thePlayer.pl - 43.0f : VmMode.mc.thePlayer.pl);
    }
}



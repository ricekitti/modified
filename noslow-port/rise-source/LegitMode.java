package rise.module.impl.movement.noslow;

import hackclient.rise.bc;
import net.minecraft.client.settings.a;
import net.minecraft.item.ai;
import net.minecraft.item.bg;
import net.minecraft.item.by;
import net.minecraft.item.co;
import net.minecraft.item.cp;
import net.minecraft.item.d;
import net.minecraft.network.play.server.at;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PacketReceiveEvent;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.SlowDownEvent;
import rise.module.Mode;
import rise.module.impl.movement.NoSlow;

public class LegitMode
extends Mode<NoSlow> {
    final private static int Ng = 32;
    final private static int Nh = 12;
    final private static int Ni = 0;
    private int Nj;
    private boolean Nk;
    private boolean Nl;
    private boolean Nm;
    private int Nn;
    @EventLink
    final public Listener<PreMotionEvent> No = preMotionEvent -> {
        if (LegitMode.mc.thePlayer == null || LegitMode.mc.thePlayer.UW() == null) {
            return;
        }
        if (bc.cR) {
            ++this.Nj;
        }
        if (this.hF()) {
            if (!bc.cR) {
                bc.cR = true;
                this.Nj = LegitMode.mc.thePlayer.abt();
            }
            LegitMode.mc.thePlayer.abu();
            LegitMode.mc.aIp.cgM.ay(false);
        }
        if (bc.cR && (this.Nj >= 32 || this.Nk)) {
            this.gx();
        }
    };
    @EventLink
    final public Listener<SlowDownEvent> Np = slowDownEvent -> {
        boolean bl2;
        if (!this.hG()) {
            this.hH();
            return;
        }
        boolean bl3 = bl2 = LegitMode.mc.thePlayer.abt() % 3 != 0;
        if (bl2) {
            if (!this.Nl) {
                this.Nm = true;
                this.Nn = 0;
                this.Nl = true;
                return;
            }
            if (this.Nm) {
                --this.Nn;
                if (this.Nn <= 0) {
                    this.Nm = false;
                    LegitMode.mc.thePlayer.setSprinting(true);
                }
                return;
            }
            LegitMode.mc.thePlayer.setSprinting(true);
            return;
        }
        this.hH();
    };
    @EventLink
    final public Listener<PacketReceiveEvent> Nq = packetReceiveEvent -> {
        at at2;
        if (packetReceiveEvent.dq() instanceof at && (at2 = (at)packetReceiveEvent.dq()).getSoundName() != null && at2.getSoundName().contains("random.burp")) {
            this.Nk = true;
        }
    };

    public LegitMode(String string, NoSlow noSlow) {
        super(string, noSlow);
    }

    @Override
    public void onDisable() {
        this.gx();
        this.hH();
        LegitMode.mc.aIp.cgM.ay(a.a(LegitMode.mc.aIp.cgM));
    }

    private boolean hF() {
        if (!LegitMode.mc.thePlayer.isUsingItem()) {
            return false;
        }
        co co2 = LegitMode.mc.thePlayer.UW();
        if (co2 == null) {
            return false;
        }
        d d2 = co2.bP();
        return LegitMode.mc.thePlayer.abt() > 12 && !(d2 instanceof ai) && this.b(co2, false);
    }

    private boolean hG() {
        if (!LegitMode.mc.thePlayer.isUsingItem()) {
            return false;
        }
        co co2 = LegitMode.mc.thePlayer.UW();
        return co2 != null && this.b(co2, true);
    }

    private boolean b(co co2, boolean bl2) {
        d d2 = co2.bP();
        if (d2 instanceof bg) {
            return (Boolean)((NoSlow)this.getParent()).DO.getValue();
        }
        if (d2 instanceof by) {
            return (Boolean)((NoSlow)this.getParent()).DP.getValue() != false && !by.gb(co2.xO());
        }
        if (d2 instanceof cp) {
            return (Boolean)((NoSlow)this.getParent()).DQ.getValue();
        }
        return bl2 && d2 instanceof ai && (Boolean)((NoSlow)this.getParent()).DR.getValue() != false;
    }

    private void gx() {
        if (bc.cR) {
            bc.dispatch();
            bc.cR = false;
        }
        this.Nk = false;
        this.Nj = 0;
    }

    private void hH() {
        this.Nl = false;
        this.Nm = false;
        this.Nn = 0;
    }
}



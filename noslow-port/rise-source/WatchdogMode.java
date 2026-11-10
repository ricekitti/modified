package rise.module.impl.movement.noslow;

import hackclient.rise.afi;
import hackclient.rise.ahj;
import hackclient.rise.aih;
import hackclient.rise.bu;
import hackclient.rise.ea;
import net.minecraft.init.Blocks;
import net.minecraft.item.ai;
import net.minecraft.item.bg;
import net.minecraft.item.by;
import net.minecraft.item.cp;
import net.minecraft.network.aj;
import net.minecraft.network.play.client.l;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PacketSendEvent;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.SlowDownEvent;
import rise.module.Mode;
import rise.module.impl.combat.KillAura;
import rise.module.impl.movement.NoSlow;
import rise.value.BooleanValue;

public class WatchdogMode
extends Mode<NoSlow> {
    private int tR;
    private boolean dk;
    private boolean vh;
    private aj<?> NI;
    private KillAura NJ = null;
    final public BooleanValue NK = new BooleanValue("Slow down on Slabs", (Mode<?>)this, (Boolean)true);
    @EventLink
    final public Listener<PreMotionEvent> NL = preMotionEvent -> {
        double d2;
        if (aih.p(0.0, WatchdogMode.mc.thePlayer.ue, 0.0) != Blocks.cKu && !WatchdogMode.mc.thePlayer.isUsingItem() && ((Boolean)this.NK.getValue()).booleanValue()) {
            this.vh = false;
        }
        if (Math.abs((d2 = preMotionEvent.getPosY()) - (double)Math.round(d2)) > 0.03 && WatchdogMode.mc.thePlayer.aO) {
            this.vh = true;
        }
        if (WatchdogMode.mc.thePlayer.isUsingItem() && !(WatchdogMode.mc.thePlayer.UW().bP() instanceof cp)) {
            this.tR = WatchdogMode.mc.thePlayer.aO ? 0 : ++this.tR;
            if (this.tR >= 2) {
                this.dk = false;
                this.NI = null;
            } else if (WatchdogMode.mc.thePlayer.aO && !this.vh) {
                preMotionEvent.setPosY(preMotionEvent.getPosY() + 0.001);
            }
        }
        if (this.vh && !WatchdogMode.mc.thePlayer.aO && WatchdogMode.mc.thePlayer.isUsingItem() && !(WatchdogMode.mc.thePlayer.UW().bP() instanceof cp)) {
            WatchdogMode.mc.thePlayer.ud *= 0.1;
            WatchdogMode.mc.thePlayer.uf *= 0.1;
        }
    };
    @EventLink
    final public Listener<ea> NM = ea2 -> {
        if (WatchdogMode.mc.thePlayer.UW() == null) {
            return;
        }
        if (WatchdogMode.mc.thePlayer.isUsingItem() || WatchdogMode.mc.thePlayer.UW().bP() instanceof by && !by.gb(WatchdogMode.mc.thePlayer.UW().xO()) || WatchdogMode.mc.thePlayer.UW().bP() instanceof bg || WatchdogMode.mc.thePlayer.UW().bP() instanceof ai) {
            if (WatchdogMode.mc.thePlayer.tR < 2 && WatchdogMode.mc.thePlayer.tR != 0 && !this.vh) {
                afi.b("You must start eating while in the air even with potions", new Object[0]);
                ea2.cF();
            } else if (WatchdogMode.mc.thePlayer.aO) {
                WatchdogMode.mc.thePlayer.jump();
                ea2.cF();
            }
        }
    };
    @EventLink
    final private Listener<PacketSendEvent> NN = packetSendEvent -> {
        if (this.NJ == null) {
            this.NJ = this.e(KillAura.class);
        }
    };
    @EventLink
    final public Listener<SlowDownEvent> NO = slowDownEvent -> {
        if (!this.vh || WatchdogMode.mc.thePlayer.aO) {
            if (((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() && WatchdogMode.mc.thePlayer.isUsingItem() && WatchdogMode.mc.thePlayer.UW().bP() instanceof bg) {
                slowDownEvent.cF();
            }
            if (((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && WatchdogMode.mc.thePlayer.isUsingItem() && WatchdogMode.mc.thePlayer.UW().bP() instanceof by && !by.gb(WatchdogMode.mc.thePlayer.UW().xO())) {
                slowDownEvent.cF();
            }
            if (((Boolean)((NoSlow)this.getParent()).DR.getValue()).booleanValue() && WatchdogMode.mc.thePlayer.isUsingItem() && WatchdogMode.mc.thePlayer.UW().bP() instanceof ai) {
                slowDownEvent.cF();
            }
        }
        if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && WatchdogMode.mc.thePlayer.isUsingItem() && WatchdogMode.mc.thePlayer.UW().bP() instanceof cp) {
            this.d(bu.class);
            ahj.l(new l(bu.bQ() % 7 + (int)(Math.random() * 2.0) + 1));
            this.d(bu.class);
            ahj.l(new l(bu.bQ()));
            slowDownEvent.cF();
        }
    };

    public WatchdogMode(String string, NoSlow noSlow) {
        super(string, noSlow);
    }
}



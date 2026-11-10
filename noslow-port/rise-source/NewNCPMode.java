package rise.module.impl.movement.noslow;

import hackclient.rise.ahj;
import hackclient.rise.bb;
import hackclient.rise.fd;
import net.minecraft.item.ai;
import net.minecraft.item.bg;
import net.minecraft.item.by;
import net.minecraft.item.cp;
import net.minecraft.network.play.client.k;
import net.minecraft.util.b;
import rise.Client;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.SlowDownEvent;
import rise.module.Mode;
import rise.module.impl.combat.KillAura;
import rise.module.impl.movement.NoSlow;

public class NewNCPMode
extends Mode<NoSlow> {
    private int zn;
    @EventLink
    final public Listener<PreMotionEvent> Nu = preMotionEvent -> {
        ++this.zn;
        this.hA();
        this.hB();
        this.hC();
    };
    @EventLink
    final public Listener<SlowDownEvent> Nv = slowDownEvent -> {
        if (Client.INSTANCE.g().c(KillAura.class).jE != null) {
            return;
        }
        if (((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() && NewNCPMode.mc.thePlayer.isUsingItem() && NewNCPMode.mc.thePlayer.UW().bP() instanceof bg) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && NewNCPMode.mc.thePlayer.isUsingItem() && NewNCPMode.mc.thePlayer.UW().bP() instanceof by) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && NewNCPMode.mc.thePlayer.isUsingItem() && NewNCPMode.mc.thePlayer.UW().bP() instanceof cp) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DR.getValue()).booleanValue() && NewNCPMode.mc.thePlayer.isUsingItem() && NewNCPMode.mc.thePlayer.UW().bP() instanceof ai) {
            slowDownEvent.cF();
        }
    };
    @EventLink
    final public Listener<fd> Nw = fd2 -> {
        this.zn = 0;
    };

    public NewNCPMode(String string, NoSlow noSlow) {
        super(string, noSlow);
    }

    @Override
    public void onEnable() {
    }

    private void hA() {
        if (((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() && NewNCPMode.mc.thePlayer.isUsingItem() && NewNCPMode.mc.thePlayer.UW().bP() instanceof bg) {
            this.hE();
        }
    }

    private void hB() {
        if (((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && NewNCPMode.mc.thePlayer.isUsingItem() && NewNCPMode.mc.thePlayer.UW().bP() instanceof by) {
            this.hE();
        }
    }

    private void hC() {
        if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && NewNCPMode.mc.thePlayer.isUsingItem() && NewNCPMode.mc.thePlayer.UW().bP() instanceof cp) {
            this.hE();
        }
    }

    private void hE() {
        if (this.zn > 10 && !bb.a(false, true, true, false, false) && Client.INSTANCE.g().c(KillAura.class).jE == null) {
            ahj.l(new k(new b(-1, -1, -1), 5, null, 0.0f, 0.0f, 0.0f));
        }
    }
}



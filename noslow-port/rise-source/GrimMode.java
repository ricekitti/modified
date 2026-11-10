package rise.module.impl.movement.noslow;

import hackclient.rise.ahj;
import hackclient.rise.bu;
import net.minecraft.item.ai;
import net.minecraft.item.bg;
import net.minecraft.item.by;
import net.minecraft.item.cp;
import net.minecraft.network.play.client.l;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.SlowDownEvent;
import rise.module.Mode;
import rise.module.impl.movement.NoSlow;

public class GrimMode
extends Mode<NoSlow> {
    @EventLink
    final public Listener<PreMotionEvent> MX = preMotionEvent -> {
        this.hA();
        this.hB();
        this.hC();
        this.hD();
    };
    @EventLink
    final public Listener<SlowDownEvent> MY = slowDownEvent -> {
        if (((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() && GrimMode.mc.thePlayer.isUsingItem() && GrimMode.mc.thePlayer.UW().bP() instanceof bg) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && GrimMode.mc.thePlayer.isUsingItem() && GrimMode.mc.thePlayer.UW().bP() instanceof by) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && GrimMode.mc.thePlayer.isUsingItem() && GrimMode.mc.thePlayer.UW().bP() instanceof cp) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DR.getValue()).booleanValue() && GrimMode.mc.thePlayer.isUsingItem() && GrimMode.mc.thePlayer.UW().bP() instanceof ai) {
            slowDownEvent.cF();
        }
    };

    public GrimMode(String string, NoSlow noSlow) {
        super(string, noSlow);
    }

    private void hA() {
        if (((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() && GrimMode.mc.thePlayer.isUsingItem() && GrimMode.mc.thePlayer.UW().bP() instanceof bg) {
            this.hE();
        }
    }

    private void hB() {
        if (((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && GrimMode.mc.thePlayer.isUsingItem() && GrimMode.mc.thePlayer.UW().bP() instanceof by) {
            this.hE();
        }
    }

    private void hC() {
        if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && GrimMode.mc.thePlayer.isUsingItem() && GrimMode.mc.thePlayer.UW().bP() instanceof cp) {
            this.hE();
        }
    }

    private void hD() {
        if (((Boolean)((NoSlow)this.getParent()).DR.getValue()).booleanValue() && GrimMode.mc.thePlayer.isUsingItem() && GrimMode.mc.thePlayer.UW().bP() instanceof ai) {
            this.hE();
        }
    }

    private void hE() {
        this.d(bu.class);
        ahj.l(new l(bu.bQ() % 8 + 1));
        this.d(bu.class);
        ahj.l(new l(bu.bQ()));
    }
}



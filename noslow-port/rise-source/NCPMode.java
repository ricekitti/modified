package rise.module.impl.movement.noslow;

import hackclient.rise.ahj;
import hackclient.rise.bu;
import net.minecraft.item.ai;
import net.minecraft.item.bg;
import net.minecraft.item.by;
import net.minecraft.item.cp;
import net.minecraft.network.play.client.i;
import net.minecraft.network.play.client.j;
import net.minecraft.network.play.client.k;
import net.minecraft.util.ao;
import net.minecraft.util.b;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PostMotionEvent;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.SlowDownEvent;
import rise.module.Mode;
import rise.module.impl.movement.NoSlow;

public class NCPMode
extends Mode<NoSlow> {
    @EventLink
    final public Listener<PreMotionEvent> Nr = preMotionEvent -> {
        if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && NCPMode.mc.thePlayer.abw()) {
            ahj.l(new i(j.daw, b.dtf, ao.dvI));
        }
    };
    @EventLink
    final public Listener<PostMotionEvent> Ns = postMotionEvent -> {
        if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && NCPMode.mc.thePlayer.abw()) {
            this.d(bu.class);
            ahj.l(new k(bu.bO()));
        }
    };
    @EventLink
    final public Listener<SlowDownEvent> Nt = slowDownEvent -> {
        if (((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() && NCPMode.mc.thePlayer.isUsingItem() && NCPMode.mc.thePlayer.UW().bP() instanceof bg) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && NCPMode.mc.thePlayer.isUsingItem() && NCPMode.mc.thePlayer.UW().bP() instanceof by) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && NCPMode.mc.thePlayer.isUsingItem() && NCPMode.mc.thePlayer.UW().bP() instanceof cp) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DR.getValue()).booleanValue() && NCPMode.mc.thePlayer.isUsingItem() && NCPMode.mc.thePlayer.UW().bP() instanceof ai) {
            slowDownEvent.cF();
        }
    };

    public NCPMode(String string, NoSlow noSlow) {
        super(string, noSlow);
    }
}



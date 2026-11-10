package rise.module.impl.movement.noslow;

import hackclient.rise.ahj;
import hackclient.rise.bc;
import net.minecraft.item.ai;
import net.minecraft.item.bg;
import net.minecraft.item.by;
import net.minecraft.item.cp;
import net.minecraft.item.d;
import net.minecraft.network.play.client.i;
import net.minecraft.network.play.client.j;
import net.minecraft.network.play.client.k;
import net.minecraft.util.ao;
import net.minecraft.util.b;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.SlowDownEvent;
import rise.module.Mode;
import rise.module.impl.movement.NoSlow;

public class IntaveMode
extends Mode<NoSlow> {
    boolean bi;
    @EventLink
    final public Listener<PreMotionEvent> MZ = preMotionEvent -> {
        if (IntaveMode.mc.thePlayer.abA() == null) {
            return;
        }
        d d2 = IntaveMode.mc.thePlayer.abA().bP();
        if (IntaveMode.mc.thePlayer.isUsingItem()) {
            if (d2 instanceof cp && ((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue()) {
                bc.cR = true;
                if (IntaveMode.mc.thePlayer.aQx % 5 == 0) {
                    ahj.l(new i(j.daw, b.dtf, ao.dvI));
                    bc.dispatch();
                    mc.AG().t(new k(IntaveMode.mc.thePlayer.abA()));
                }
            } else if (d2 instanceof bg && ((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() || d2 instanceof ai && ((Boolean)((NoSlow)this.getParent()).DR.getValue()).booleanValue()) {
                bc.cR = true;
            }
            this.bi = true;
        } else if (this.bi) {
            this.bi = false;
            bc.cR = false;
        }
    };
    @EventLink
    final public Listener<SlowDownEvent> Na = slowDownEvent -> {
        if (((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() && IntaveMode.mc.thePlayer.isUsingItem() && IntaveMode.mc.thePlayer.UW().bP() instanceof bg) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && IntaveMode.mc.thePlayer.isUsingItem() && IntaveMode.mc.thePlayer.UW().bP() instanceof by) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && IntaveMode.mc.thePlayer.isUsingItem() && IntaveMode.mc.thePlayer.UW().bP() instanceof cp) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DR.getValue()).booleanValue() && IntaveMode.mc.thePlayer.isUsingItem() && IntaveMode.mc.thePlayer.UW().bP() instanceof ai) {
            slowDownEvent.cF();
        }
    };

    public IntaveMode(String string, NoSlow noSlow) {
        super(string, noSlow);
    }
}



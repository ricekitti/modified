package rise.module.impl.movement.noslow;

import hackclient.rise.aif;
import net.minecraft.item.ai;
import net.minecraft.item.bg;
import net.minecraft.item.by;
import net.minecraft.item.cp;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.SlowDownEvent;
import rise.event.impl.StrafeEvent;
import rise.module.Mode;
import rise.module.impl.movement.NoSlow;
import rise.module.impl.movement.Speed;

public class MatrixMode
extends Mode<NoSlow> {
    @EventLink
    final public Listener<SlowDownEvent> Ne = slowDownEvent -> {
        if (((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() && MatrixMode.mc.thePlayer.isUsingItem() && MatrixMode.mc.thePlayer.UW().bP() instanceof bg) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && MatrixMode.mc.thePlayer.isUsingItem() && MatrixMode.mc.thePlayer.UW().bP() instanceof by) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && MatrixMode.mc.thePlayer.isUsingItem() && MatrixMode.mc.thePlayer.UW().bP() instanceof cp) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DR.getValue()).booleanValue() && MatrixMode.mc.thePlayer.isUsingItem() && MatrixMode.mc.thePlayer.UW().bP() instanceof ai) {
            slowDownEvent.cF();
        }
    };
    @EventLink
    final public Listener<StrafeEvent> Nf = strafeEvent -> {
        if (((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() && MatrixMode.mc.thePlayer.isUsingItem() && MatrixMode.mc.thePlayer.UW().bP() instanceof bg) {
            if (MatrixMode.mc.thePlayer.cqP > 1) {
                aif.strafe(0.0265);
            } else if (!this.e(Speed.class).isEnabled()) {
                MatrixMode.mc.thePlayer.ud *= 0.992;
                MatrixMode.mc.thePlayer.uf *= 0.992;
            } else {
                MatrixMode.mc.thePlayer.ud *= 0.99;
                MatrixMode.mc.thePlayer.uf *= 0.99;
            }
        }
        if (((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && MatrixMode.mc.thePlayer.isUsingItem() && MatrixMode.mc.thePlayer.UW().bP() instanceof by) {
            if (MatrixMode.mc.thePlayer.cqP > 1) {
                aif.strafe(0.0265);
            } else if (!this.e(Speed.class).isEnabled()) {
                MatrixMode.mc.thePlayer.ud *= 0.992;
                MatrixMode.mc.thePlayer.uf *= 0.992;
            } else {
                MatrixMode.mc.thePlayer.ud *= 0.99;
                MatrixMode.mc.thePlayer.uf *= 0.99;
            }
        }
        if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && MatrixMode.mc.thePlayer.isUsingItem() && MatrixMode.mc.thePlayer.UW().bP() instanceof cp) {
            if (MatrixMode.mc.thePlayer.cqP > 1) {
                aif.strafe(0.0265);
            } else if (!this.e(Speed.class).isEnabled()) {
                MatrixMode.mc.thePlayer.ud *= 0.992;
                MatrixMode.mc.thePlayer.uf *= 0.992;
            } else {
                MatrixMode.mc.thePlayer.ud *= 0.99;
                MatrixMode.mc.thePlayer.uf *= 0.99;
            }
        }
        if (((Boolean)((NoSlow)this.getParent()).DR.getValue()).booleanValue() && MatrixMode.mc.thePlayer.isUsingItem() && MatrixMode.mc.thePlayer.UW().bP() instanceof ai) {
            if (MatrixMode.mc.thePlayer.cqP > 1) {
                aif.strafe(0.0265);
            } else if (!this.e(Speed.class).isEnabled()) {
                MatrixMode.mc.thePlayer.ud *= 0.992;
                MatrixMode.mc.thePlayer.uf *= 0.992;
            } else {
                MatrixMode.mc.thePlayer.ud *= 0.99;
                MatrixMode.mc.thePlayer.uf *= 0.99;
            }
        }
    };

    public MatrixMode(String string, NoSlow noSlow) {
        super(string, noSlow);
    }
}



package rise.module.impl.movement.noslow;

import net.minecraft.item.ai;
import net.minecraft.item.bg;
import net.minecraft.item.by;
import net.minecraft.item.cp;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.SlowDownEvent;
import rise.module.Mode;
import rise.module.impl.movement.NoSlow;
import rise.value.NumberValue;

public class PredictionMode
extends Mode<NoSlow> {
    final private NumberValue ND = new NumberValue("Amount", this, (Number)2, (Number)2, (Number)5, (Number)1);
    @EventLink
    final public Listener<SlowDownEvent> NE = slowDownEvent -> {
        if (PredictionMode.mc.thePlayer.cqP % ((Number)this.ND.getValue()).intValue() != 0 && PredictionMode.mc.thePlayer.aO) {
            if (((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() && PredictionMode.mc.thePlayer.isUsingItem() && PredictionMode.mc.thePlayer.UW().bP() instanceof bg) {
                slowDownEvent.cF();
            }
            if (((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && PredictionMode.mc.thePlayer.isUsingItem() && PredictionMode.mc.thePlayer.UW().bP() instanceof by) {
                slowDownEvent.cF();
            }
            if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && PredictionMode.mc.thePlayer.isUsingItem() && PredictionMode.mc.thePlayer.UW().bP() instanceof cp) {
                slowDownEvent.cF();
            }
            if (((Boolean)((NoSlow)this.getParent()).DR.getValue()).booleanValue() && PredictionMode.mc.thePlayer.isUsingItem() && PredictionMode.mc.thePlayer.UW().bP() instanceof ai) {
                slowDownEvent.cF();
            }
        }
    };

    public PredictionMode(String string, NoSlow noSlow) {
        super(string, noSlow);
    }
}



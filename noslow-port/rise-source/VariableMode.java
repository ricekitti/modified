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

public class VariableMode
extends Mode<NoSlow> {
    final private NumberValue NG = new NumberValue("Multiplier", this, (Number)0.8, (Number)0.2, (Number)1, (Number)0.05);
    @EventLink
    final public Listener<SlowDownEvent> NH = slowDownEvent -> {
        if (((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() && VariableMode.mc.thePlayer.isUsingItem() && VariableMode.mc.thePlayer.UW().bP() instanceof bg) {
            slowDownEvent.setForwardMultiplier(((Number)this.NG.getValue()).floatValue());
            slowDownEvent.setStrafeMultiplier(((Number)this.NG.getValue()).floatValue());
        }
        if (((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && VariableMode.mc.thePlayer.isUsingItem() && VariableMode.mc.thePlayer.UW().bP() instanceof by) {
            slowDownEvent.setForwardMultiplier(((Number)this.NG.getValue()).floatValue());
            slowDownEvent.setStrafeMultiplier(((Number)this.NG.getValue()).floatValue());
        }
        if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && VariableMode.mc.thePlayer.isUsingItem() && VariableMode.mc.thePlayer.UW().bP() instanceof cp) {
            slowDownEvent.setForwardMultiplier(((Number)this.NG.getValue()).floatValue());
            slowDownEvent.setStrafeMultiplier(((Number)this.NG.getValue()).floatValue());
        }
        if (((Boolean)((NoSlow)this.getParent()).DR.getValue()).booleanValue() && VariableMode.mc.thePlayer.isUsingItem() && VariableMode.mc.thePlayer.UW().bP() instanceof ai) {
            slowDownEvent.setForwardMultiplier(((Number)this.NG.getValue()).floatValue());
            slowDownEvent.setStrafeMultiplier(((Number)this.NG.getValue()).floatValue());
        }
    };

    public VariableMode(String string, NoSlow noSlow) {
        super(string, noSlow);
    }
}



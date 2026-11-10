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

public class VanillaMode
extends Mode<NoSlow> {
    @EventLink
    final public Listener<SlowDownEvent> NF = slowDownEvent -> {
        if (((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() && VanillaMode.mc.thePlayer.isUsingItem() && VanillaMode.mc.thePlayer.UW().bP() instanceof bg) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && VanillaMode.mc.thePlayer.isUsingItem() && VanillaMode.mc.thePlayer.UW().bP() instanceof by) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && VanillaMode.mc.thePlayer.isUsingItem() && VanillaMode.mc.thePlayer.UW().bP() instanceof cp) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DR.getValue()).booleanValue() && VanillaMode.mc.thePlayer.isUsingItem() && VanillaMode.mc.thePlayer.UW().bP() instanceof ai) {
            slowDownEvent.cF();
        }
    };

    public VanillaMode(String string, NoSlow noSlow) {
        super(string, noSlow);
    }
}



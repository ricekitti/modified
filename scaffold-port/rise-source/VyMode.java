package rise.module.impl.player.scaffold;

import hackclient.rise.aif;
import hackclient.rise.aih;
import net.minecraft.item.co;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VyMode
extends Mode<Scaffold> {
    @EventLink
    final public Listener<PreMotionEvent> akj = preMotionEvent -> {
        if (VyMode.mc.aIp.cgI.QQ() && aih.ay(2) && VyMode.mc.thePlayer.tR > 3) {
            co co2 = VyMode.mc.thePlayer.cHn.cIU[VyMode.mc.thePlayer.cHn.cIW];
            if (co2 == null || co2.cWr > 2) {
                // empty if block
            }
            if (!aif.isMoving()) {
                preMotionEvent.setOnGround(true);
                VyMode.mc.thePlayer.aO = true;
                VyMode.mc.thePlayer.ue = 0.5;
                if (VyMode.mc.thePlayer.aQx % 2 == 0) {
                    preMotionEvent.setPosX(preMotionEvent.getPosX() + 0.1);
                }
            }
        }
    };

    public VyMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



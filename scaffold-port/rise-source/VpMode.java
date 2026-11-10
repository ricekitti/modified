package rise.module.impl.player.scaffold;

import hackclient.rise.aih;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VpMode
extends Mode<Scaffold> {
    @EventLink
    final public Listener<PreMotionEvent> ajW = preMotionEvent -> {
        if (VpMode.mc.aIp.cgI.QQ() && VpMode.mc.thePlayer.aQx % 2 == 0 && aih.ay(2)) {
            VpMode.mc.thePlayer.ue = 0.42f;
            preMotionEvent.setOnGround(true);
        }
    };

    public VpMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



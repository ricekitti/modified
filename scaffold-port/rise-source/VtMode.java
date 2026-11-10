package rise.module.impl.player.scaffold;

import hackclient.rise.aif;
import hackclient.rise.aih;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VtMode
extends Mode<Scaffold> {
    @EventLink
    final public Listener<PreMotionEvent> akc = preMotionEvent -> {
        if (VtMode.mc.aIp.cgI.QQ() && aih.a(2.0, false) && VtMode.mc.thePlayer.ue < 0.2 && !aif.isMoving()) {
            VtMode.mc.thePlayer.ue = 0.42f;
            preMotionEvent.setOnGround(true);
        }
    };

    public VtMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



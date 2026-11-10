package rise.module.impl.player.scaffold;

import hackclient.rise.aih;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VxMode
extends Mode<Scaffold> {
    @EventLink
    final public Listener<PreMotionEvent> aki = preMotionEvent -> {
        if (VxMode.mc.aIp.cgI.QQ() && aih.ay(1)) {
            VxMode.mc.thePlayer.ue = 0.42f;
        }
    };

    public VxMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



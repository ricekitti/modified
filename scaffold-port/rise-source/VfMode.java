package rise.module.impl.player.scaffold;

import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.StrafeEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VfMode
extends Mode<Scaffold> {
    @EventLink(priority=0)
    final public Listener<StrafeEvent> ajl = strafeEvent -> {
        VfMode.mc.aIp.cgK.ay(false);
        VfMode.mc.thePlayer.setSprinting(false);
    };

    public VfMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



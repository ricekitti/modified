package rise.module.impl.player.scaffold;

import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.StrafeEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VrMode
extends Mode<Scaffold> {
    @EventLink
    final public Listener<StrafeEvent> aka = strafeEvent -> {
        if (!VrMode.mc.aIp.cgI.QQ()) {
            return;
        }
        if (VrMode.mc.thePlayer.aO) {
            VrMode.mc.thePlayer.jump();
        }
    };

    public VrMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



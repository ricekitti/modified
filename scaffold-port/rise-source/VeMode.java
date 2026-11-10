package rise.module.impl.player.scaffold;

import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VeMode
extends Mode<Scaffold> {
    @EventLink
    final public Listener<PreMotionEvent> ajk = preMotionEvent -> VeMode.mc.thePlayer.setSprinting(false);

    public VeMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



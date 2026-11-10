package rise.module.impl.player.scaffold;

import org.lwjgl.input.Keyboard;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreUpdateEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VaMode
extends Mode<Scaffold> {
    @EventLink(priority=1)
    final public Listener<PreUpdateEvent> ajb = preUpdateEvent -> {
        if (!Keyboard.isKeyDown(VaMode.mc.aIp.cgJ.cO())) {
            return;
        }
        ((Scaffold)this.getParent()).agy = ((Scaffold)this.getParent()).agy.v(0.0, -1.0, 0.0);
    };

    public VaMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



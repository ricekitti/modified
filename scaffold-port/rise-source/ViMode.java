package rise.module.impl.player.scaffold;

import hackclient.rise.aif;
import net.minecraft.potion.a;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.StrafeEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class ViMode
extends Mode<Scaffold> {
    @EventLink
    final public Listener<PreMotionEvent> ajo = preMotionEvent -> {
        if (ViMode.mc.thePlayer.aO) {
            ViMode.mc.thePlayer.ue = 0.0;
            aif.strafe(0.5);
            if (ViMode.mc.thePlayer.a(a.dfO)) {
                aif.strafe(0.06 * (double)(1 + ViMode.mc.thePlayer.b(a.dfO).aiR()) + 0.1);
            }
        }
    };
    @EventLink
    final public Listener<StrafeEvent> ajp = strafeEvent -> {
        if (ViMode.mc.thePlayer.aO) {
            strafeEvent.setSpeed(0.61);
            if (ViMode.mc.thePlayer.a(a.dfO)) {
                aif.strafe(0.06 * (double)(1 + ViMode.mc.thePlayer.b(a.dfO).aiR()) + 0.1);
            }
        } else if (ViMode.mc.thePlayer.ue < 0.77) {
            strafeEvent.setSpeed(0.4);
            if (ViMode.mc.thePlayer.a(a.dfO)) {
                aif.strafe(0.01 * (double)(1 + ViMode.mc.thePlayer.b(a.dfO).aiR()) + 0.1);
            }
        }
    };

    public ViMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



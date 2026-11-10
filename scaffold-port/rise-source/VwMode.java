package rise.module.impl.player.scaffold;

import hackclient.rise.aih;
import net.minecraft.potion.a;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.StrafeEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VwMode
extends Mode<Scaffold> {
    @EventLink
    final public Listener<PreMotionEvent> akg = preMotionEvent -> {
        if (VwMode.mc.aIp.cgI.QQ() && aih.ay(1)) {
            VwMode.mc.thePlayer.ue = 0.8f;
        }
    };
    @EventLink
    final public Listener<StrafeEvent> akh = strafeEvent -> {
        if (!(VwMode.mc.thePlayer.ue < 0.77) || VwMode.mc.thePlayer.a(a.dfO)) {
            // empty if block
        }
    };

    public VwMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



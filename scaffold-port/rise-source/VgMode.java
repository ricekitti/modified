package rise.module.impl.player.scaffold;

import hackclient.rise.bs;
import net.minecraft.util.bt;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VgMode
extends Mode<Scaffold> {
    @EventLink
    final public Listener<PreMotionEvent> ajm = preMotionEvent -> {
        if (Math.abs(bt.bN(VgMode.mc.thePlayer.pl) - bt.bN(bs.fk.x)) > 90.0f) {
            VgMode.mc.aIp.cgK.ay(false);
            VgMode.mc.thePlayer.setSprinting(false);
        }
    };

    public VgMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



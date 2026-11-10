package rise.module.impl.player.scaffold;

import hackclient.rise.ahj;
import hackclient.rise.aih;
import net.minecraft.network.play.client.k;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VuMode
extends Mode<Scaffold> {
    @EventLink
    final public Listener<PreMotionEvent> akd = preMotionEvent -> {
        if (VuMode.mc.aIp.cgI.QQ() && aih.ay(2)) {
            ahj.m(new k(null));
            if (VuMode.mc.thePlayer.gX % 1.0 <= 0.00153598) {
                VuMode.mc.thePlayer.setPosition(VuMode.mc.thePlayer.distance, Math.floor(VuMode.mc.thePlayer.gX), VuMode.mc.thePlayer.gY);
                VuMode.mc.thePlayer.ue = 0.42f;
            } else if (VuMode.mc.thePlayer.gX % 1.0 < 0.1 && VuMode.mc.thePlayer.tR != 0) {
                VuMode.mc.thePlayer.ue = 0.0;
                VuMode.mc.thePlayer.setPosition(VuMode.mc.thePlayer.distance, Math.floor(VuMode.mc.thePlayer.gX), VuMode.mc.thePlayer.gY);
            }
        }
    };

    public VuMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



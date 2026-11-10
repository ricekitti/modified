package rise.module.impl.player.scaffold;

import hackclient.rise.aif;
import hackclient.rise.aih;
import net.minecraft.network.aj;
import net.minecraft.network.play.client.k;
import net.minecraft.util.b;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PacketSendEvent;
import rise.event.impl.PreMotionEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VqMode
extends Mode<Scaffold> {
    private int ajX = 5;
    @EventLink
    final public Listener<PreMotionEvent> ajY = preMotionEvent -> {
        if (VqMode.mc.aIp.cgI.QQ() && aih.ay(2) && !aif.isMoving()) {
            if (VqMode.mc.thePlayer.gX % 1.0 <= 0.00153598) {
                VqMode.mc.thePlayer.setPosition(VqMode.mc.thePlayer.distance, Math.floor(VqMode.mc.thePlayer.gX), VqMode.mc.thePlayer.gY);
                VqMode.mc.thePlayer.ue = 0.42f;
            } else if (VqMode.mc.thePlayer.gX % 1.0 < 0.1 && VqMode.mc.thePlayer.tR != 0) {
                VqMode.mc.thePlayer.ue = 0.0;
                VqMode.mc.thePlayer.setPosition(VqMode.mc.thePlayer.distance, Math.floor(VqMode.mc.thePlayer.gX), VqMode.mc.thePlayer.gY);
            }
        }
        if (aif.isMoving() && VqMode.mc.aIp.cgI.QQ() && VqMode.mc.thePlayer.aO) {
            VqMode.mc.thePlayer.jump();
        }
    };
    @EventLink
    final public Listener<PacketSendEvent> ajZ = packetSendEvent -> {
        k k2;
        aj<?> aj2 = packetSendEvent.dq();
        if (aif.isMoving() && VqMode.mc.thePlayer.ue > -0.09800000190734864 && aj2 instanceof k && (k2 = (k)aj2).xX().equals(new b(VqMode.mc.thePlayer.distance, VqMode.mc.thePlayer.gX - 1.4, VqMode.mc.thePlayer.gY))) {
            VqMode.mc.thePlayer.ue = -0.09800000190734864;
        }
    };

    public VqMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



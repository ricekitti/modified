package rise.module.impl.player.scaffold;

import net.minecraft.network.aj;
import net.minecraft.network.play.client.k;
import net.minecraft.util.b;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PacketSendEvent;
import rise.event.impl.PreMotionEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VvMode
extends Mode<Scaffold> {
    @EventLink
    final public Listener<PreMotionEvent> ake = preMotionEvent -> {
        if (VvMode.mc.aIp.cgI.QQ() && VvMode.mc.thePlayer.aO) {
            VvMode.mc.thePlayer.ue = 0.42f;
        }
    };
    @EventLink
    final public Listener<PacketSendEvent> akf = packetSendEvent -> {
        k k2;
        aj<?> aj2 = packetSendEvent.dq();
        if (VvMode.mc.thePlayer.ue > -0.0784000015258789 && aj2 instanceof k && (k2 = (k)aj2).xX().equals(new b(VvMode.mc.thePlayer.distance, Math.floor(VvMode.mc.thePlayer.gX) - 2.0, VvMode.mc.thePlayer.gY))) {
            VvMode.mc.thePlayer.ue = -0.0784000015258789;
        }
    };

    public VvMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



package rise.module.impl.player.scaffold;

import net.minecraft.network.aj;
import net.minecraft.network.play.client.k;
import net.minecraft.util.b;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PacketSendEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VsMode
extends Mode<Scaffold> {
    @EventLink
    final public Listener<PacketSendEvent> akb = packetSendEvent -> {
        k k2;
        aj<?> aj2 = packetSendEvent.dq();
        if (VsMode.mc.aIp.cgI.QQ() && aj2 instanceof k && (k2 = (k)aj2).xX().equals(new b(VsMode.mc.thePlayer.distance, VsMode.mc.thePlayer.gX - 1.4, VsMode.mc.thePlayer.gY))) {
            VsMode.mc.aIp.cgK.ay(false);
            VsMode.mc.thePlayer.setSprinting(false);
            VsMode.mc.thePlayer.ue = 0.42f;
        }
    };

    public VsMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



package rise.module.impl.player.scaffold;

import net.minecraft.network.aj;
import net.minecraft.network.play.client.k;
import org.lwjgl.input.Keyboard;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PacketSendEvent;
import rise.event.impl.PreUpdateEvent;
import rise.event.impl.StrafeEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VcMode
extends Mode<Scaffold> {
    public static int bj;
    @EventLink(priority=0)
    final public Listener<StrafeEvent> ajf = strafeEvent -> {
        if (!Keyboard.isKeyDown(VcMode.mc.aIp.cgJ.cO())) {
            return;
        }
    };
    @EventLink(priority=3)
    final public Listener<PreUpdateEvent> ajg = preUpdateEvent -> {
        if (!Keyboard.isKeyDown(VcMode.mc.aIp.cgJ.cO()) || VcMode.mc.thePlayer.crL > 20) {
            return;
        }
    };
    @EventLink
    final public Listener<PacketSendEvent> ajh = packetSendEvent -> {
        aj<?> aj2 = packetSendEvent.dq();
        if (aj2 instanceof k) {
            k k2 = (k)aj2;
        }
    };

    public VcMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }

    @Override
    public void onEnable() {
        bj = 0;
    }
}



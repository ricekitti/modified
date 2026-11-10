package rise.module.impl.player.scaffold;

import hackclient.rise.aka;
import net.minecraft.network.aj;
import net.minecraft.network.play.client.k;
import net.minecraft.util.ao;
import org.lwjgl.input.Keyboard;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PacketSendEvent;
import rise.event.impl.PreUpdateEvent;
import rise.event.impl.StrafeEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VbMode
extends Mode<Scaffold> {
    int bj;
    @EventLink(priority=0)
    final public Listener<StrafeEvent> ajc = strafeEvent -> {
        if (!Keyboard.isKeyDown(VbMode.mc.aIp.cgJ.cO())) {
            return;
        }
        VbMode.mc.aIp.cgK.ay(false);
        VbMode.mc.thePlayer.setSprinting(false);
    };
    @EventLink(priority=3)
    final public Listener<PreUpdateEvent> ajd = preUpdateEvent -> {
        if (!Keyboard.isKeyDown(VbMode.mc.aIp.cgJ.cO()) || VbMode.mc.thePlayer.crL > 20) {
            this.bj = 1000;
            return;
        }
        ++this.bj;
        if (VbMode.mc.thePlayer.gX % 1.0 != 0.0) {
            this.bj = 0;
        }
        if (VbMode.mc.thePlayer.ue <= 0.0 && this.bj >= 15) {
            ((Scaffold)this.getParent()).agy = ((Scaffold)this.getParent()).agy.v(0.0, -1.0, 0.0);
        }
    };
    @EventLink
    final public Listener<PacketSendEvent> aje = packetSendEvent -> {
        k k2;
        aj<?> aj2 = packetSendEvent.dq();
        if (!(aj2 instanceof k) || (k2 = (k)aj2).xX().h(new aka(-1.0, -1.0, -1.0)) || k2.getPlacedBlockDirection() == ao.dvI.OK()) {
            // empty if block
        }
    };

    public VbMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }

    @Override
    public void onEnable() {
        this.bj = 1000;
    }
}



package rise.module.impl.player.scaffold;

import hackclient.rise.aif;
import net.minecraft.potion.a;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PacketSendEvent;
import rise.event.impl.StrafeEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VnMode
extends Mode<Scaffold> {
    private int hV;
    @EventLink(priority=0)
    final public Listener<StrafeEvent> ajP = strafeEvent -> {
        double d2;
        ++this.hV;
        VnMode.mc.aIp.cgK.ay(true);
        VnMode.mc.thePlayer.setSprinting(true);
        double d3 = d2 = VnMode.mc.thePlayer.a(a.dfO) ? 0.118 : 0.083;
        if (VnMode.mc.thePlayer.aO) {
            aif.strafe(d2 - (VnMode.mc.thePlayer.Zl <= 40 ? 0.1 : 0.0) - Math.random() * 1.0E-4);
        }
        if (aif.speed() >= d2 && !VnMode.mc.aIp.cgI.QQ()) {
            aif.moveFlying((aif.speed() - d2) * -1.0);
        }
    };
    @EventLink
    final public Listener<PacketSendEvent> ajQ = packetSendEvent -> {};

    public VnMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }

    @Override
    public void onDisable() {
        VnMode.mc.thePlayer.cHL.cJc = false;
        VnMode.mc.timer.timerSpeed = 1.0f;
    }
}



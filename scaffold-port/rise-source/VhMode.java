package rise.module.impl.player.scaffold;

import hackclient.rise.bs;
import net.minecraft.util.bt;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.StrafeEvent;
import rise.module.Mode;
import rise.module.impl.movement.Speed;
import rise.module.impl.player.Scaffold;

public class VhMode
extends Mode<Scaffold> {
    private int dm;
    private boolean LH;
    @EventLink
    final public Listener<StrafeEvent> ajn = strafeEvent -> {
        boolean bl2;
        float f2 = VhMode.mc.thePlayer.pl;
        float f3 = bt.bN(f2);
        bs.d(false);
        this.e(Speed.class).setEnabled(false);
        boolean bl3 = bl2 = Math.abs(f3 % 80.0f) <= 15.0f || Math.abs(f3 % 90.0f) >= 75.0f;
        if (!bl2) {
            VhMode.mc.thePlayer.ud *= 0.95;
            VhMode.mc.thePlayer.uf *= 0.95;
        }
    };

    public VhMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



package rise.module.impl.player.scaffold;

import hackclient.rise.aif;
import hackclient.rise.aih;
import hackclient.rise.aik;
import hackclient.rise.bu;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.PreUpdateEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VdMode
extends Mode<Scaffold> {
    @EventLink
    final public Listener<PreMotionEvent> aji = preMotionEvent -> {
        if (VdMode.mc.thePlayer.aO) {
            // empty if block
        }
    };
    @EventLink(priority=1)
    final public Listener<PreUpdateEvent> ajj = preUpdateEvent -> {
        boolean bl2 = VdMode.mc.thePlayer.aI == ((Scaffold)this.getParent()).Me;
        this.d(bu.class);
        if (bu.bO() != null && VdMode.mc.thePlayer.gX > ((Scaffold)this.getParent()).Me && VdMode.mc.thePlayer.gX + aif.predictedMotion(VdMode.mc.thePlayer.ue, 3) < ((Scaffold)this.getParent()).Me + 1.0) {
            this.d(bu.class);
            bu.setSlot(aik.vy());
            this.d(bu.class);
            if (bu.bO().cWs > 0) {
                mc.AA();
            }
        }
        if (!bl2 && VdMode.mc.thePlayer.aO) {
            VdMode.mc.thePlayer.jump();
            aif.strafe();
        }
        if (VdMode.mc.thePlayer.tR == 4 && !VdMode.mc.aIp.cgI.QQ()) {
            VdMode.mc.thePlayer.ue -= -0.09800000190735147;
        }
        if (VdMode.mc.thePlayer.tR == 1 && !VdMode.mc.aIp.cgI.QQ()) {
            aif.strafe();
        }
        if (aih.p(0.0, VdMode.mc.thePlayer.ue, 0.0) != Blocks.cKu && VdMode.mc.thePlayer.tR > 2) {
            aif.strafe();
        }
        VdMode.mc.thePlayer.bjU = aif.isMoving();
    };

    public VdMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }

    @Override
    public void onEnable() {
        if (!(aih.o(VdMode.mc.thePlayer.distance, VdMode.mc.thePlayer.aI - 2.0, VdMode.mc.thePlayer.gX) instanceof BlockAir)) {
            ((Scaffold)this.getParent()).Me = VdMode.mc.thePlayer.aI - 1.0;
        }
    }

    @Override
    public void onDisable() {
        if (!(aih.o(VdMode.mc.thePlayer.distance, VdMode.mc.thePlayer.aI - 2.0, VdMode.mc.thePlayer.gX) instanceof BlockAir)) {
            ((Scaffold)this.getParent()).Me = VdMode.mc.thePlayer.aI - 1.0;
        }
    }
}



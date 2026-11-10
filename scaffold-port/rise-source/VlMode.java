package rise.module.impl.player.scaffold;

import hackclient.rise.aif;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.JumpEvent;
import rise.event.impl.KeyboardInputEvent;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.PreUpdateEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VlMode
extends Mode<Scaffold> {
    private boolean El;
    @EventLink(priority=3)
    final private Listener<PreUpdateEvent> ajH = preUpdateEvent -> {
        if (VlMode.mc.thePlayer.aO && !VlMode.mc.aIp.cgI.QQ()) {
            VlMode.mc.timer.timerSpeed = 1.0029f;
            aif.preventDiagonalSpeed();
            VlMode.mc.thePlayer.uf *= 0.998;
            VlMode.mc.thePlayer.ud *= 0.998;
        }
        if (VlMode.mc.aIp.cgI.QS() && VlMode.mc.thePlayer.aO) {
            aif.stop();
            this.El = false;
        }
    };
    @EventLink(priority=3)
    final private Listener<PreMotionEvent> ajI = preMotionEvent -> {
        if (VlMode.mc.thePlayer.aO) {
            preMotionEvent.setPosY(preMotionEvent.getPosY() + 1.0E-13);
        }
        if (VlMode.mc.thePlayer.aO && !this.El && !VlMode.mc.aIp.cgI.QQ()) {
            aif.stop();
            preMotionEvent.setPosY(preMotionEvent.getPosY() + 1.0E-13);
            this.El = true;
        }
    };
    @EventLink
    final public Listener<JumpEvent> ajJ = jumpEvent -> jumpEvent.setJumpMotion(0.42f);
    @EventLink(priority=4)
    final public Listener<KeyboardInputEvent> ajK = keyboardInputEvent -> {
        if (keyboardInputEvent.cQ() != null || keyboardInputEvent.isCancelled()) {
            return;
        }
        if (keyboardInputEvent.cO() == 57) {
            keyboardInputEvent.cF();
        }
    };

    public VlMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }

    @Override
    public void onEnable() {
        this.El = true;
        if (!VlMode.mc.aIp.cgI.QQ()) {
            aif.stop();
        }
        if (VlMode.mc.thePlayer.cqP > 10 && !VlMode.mc.aIp.cgI.QQ()) {
            VlMode.mc.thePlayer.ue = 0.42;
        }
    }

    @Override
    public void onDisable() {
        aif.stop();
    }
}



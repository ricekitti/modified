package rise.module.impl.player.scaffold;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import hackclient.rise.aif;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.module.Mode;
import rise.module.impl.player.Scaffold;

public class VzMode
extends Mode<Scaffold> {
    @EventLink
    final public Listener<PreMotionEvent> akk = preMotionEvent -> {
        if (!VzMode.mc.aIp.cgI.QQ()) {
            return;
        }
        if (VzMode.mc.thePlayer.aO && !aif.enoughMovementForSprinting()) {
            VzMode.mc.thePlayer.jump();
        }
        if (ViaLoadingBase.getInstance().getTargetVersion().olderThan(ProtocolVersion.v1_18)) {
            if (VzMode.mc.thePlayer.tR == 4 && aif.speed() == 0.0) {
                VzMode.mc.thePlayer.ue -= 0.03;
            }
            if (VzMode.mc.thePlayer.tR == 5 && aif.speed() == 0.0) {
                VzMode.mc.thePlayer.ue -= 0.5;
            }
        }
    };

    public VzMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }
}



package rise.module.impl.movement.noslow;

import com.viaversion.viabackwards.protocol.v1_19to1_18_2.Protocol1_19To1_18_2;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ServerboundPackets1_19;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import hackclient.rise.ahj;
import hackclient.rise.bc;
import hackclient.rise.bm;
import hackclient.rise.bs;
import hackclient.rise.bu;
import hackclient.rise.cb;
import hackclient.rise.en;
import net.minecraft.item.ai;
import net.minecraft.item.bg;
import net.minecraft.item.by;
import net.minecraft.item.cp;
import net.minecraft.item.d;
import net.minecraft.network.play.client.i;
import net.minecraft.network.play.client.j;
import net.minecraft.network.play.client.k;
import net.minecraft.util.ao;
import net.minecraft.util.b;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.PreUpdateEvent;
import rise.event.impl.SlowDownEvent;
import rise.module.Mode;
import rise.module.impl.combat.KillAura;
import rise.module.impl.movement.NoSlow;
import rise.util.math.Vector2f;
import rise.value.BooleanValue;
import rise.value.NumberValue;

public class WatchdogPredictionMode
extends Mode<NoSlow> {
    boolean bi;
    int MM;
    public NumberValue pR = new NumberValue("Max Ping Spoof", this, (Number)8, (Number)0, (Number)30, (Number)1);
    public NumberValue Nx = new NumberValue("When to finish eating", this, (Number)30, (Number)20, (Number)36, (Number)1);
    final public BooleanValue Ny = new BooleanValue("Non-Blink Speed Bypass", (Mode<?>)this, (Boolean)true);
    @EventLink
    final public Listener<PreMotionEvent> Nz = preMotionEvent -> {
        if (WatchdogPredictionMode.mc.thePlayer.abA() == null) {
            return;
        }
        d d2 = WatchdogPredictionMode.mc.thePlayer.abA().bP();
        if (WatchdogPredictionMode.mc.thePlayer.isUsingItem()) {
            if ((!(d2 instanceof cp) || !((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue()) && (d2 instanceof bg && (Boolean)((NoSlow)this.getParent()).DO.getValue() != false && WatchdogPredictionMode.mc.thePlayer.isEating() || d2 instanceof ai && (Boolean)((NoSlow)this.getParent()).DR.getValue() != false || WatchdogPredictionMode.mc.thePlayer.UW().bP() instanceof by && !by.gb(WatchdogPredictionMode.mc.thePlayer.UW().xO()) && ((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && WatchdogPredictionMode.mc.thePlayer.isEating())) {
                ++this.MM;
                if (this.MM > ((Number)this.pR.getValue()).intValue()) {
                    bm.a(30000, true, false, false, false, true);
                }
            }
            this.bi = true;
        } else if (this.bi) {
            this.MM = 0;
            this.bi = false;
            bm.dispatch();
        }
        if (this.MM > ((Number)this.Nx.getValue()).intValue()) {
            WatchdogPredictionMode.mc.aIp.cgM.ay(false);
        }
    };
    @EventLink
    final public Listener<en> NA = en2 -> {
        if (WatchdogPredictionMode.mc.thePlayer.isUsingItem() && WatchdogPredictionMode.mc.thePlayer.cuo > 0.0f && ((Boolean)this.Ny.getValue()).booleanValue() && this.MM <= ((Number)this.pR.getValue()).intValue()) {
            WatchdogPredictionMode.mc.thePlayer.setSprinting(true);
        }
    };
    @EventLink(priority=1)
    final public Listener<PreUpdateEvent> NB = preUpdateEvent -> {
        if (!(KillAura.mB && this.e(KillAura.class).isEnabled() || WatchdogPredictionMode.mc.thePlayer.aO && WatchdogPredictionMode.mc.thePlayer.cqP <= 2 || WatchdogPredictionMode.mc.aIp.cgH.QQ() || WatchdogPredictionMode.mc.aIp.cgF.QQ() || WatchdogPredictionMode.mc.thePlayer.Zl <= 5 || !WatchdogPredictionMode.mc.thePlayer.isUsingItem() || WatchdogPredictionMode.mc.thePlayer.UW() != null && WatchdogPredictionMode.mc.thePlayer.UW().bP() instanceof cp || WatchdogPredictionMode.mc.thePlayer.UW() != null && WatchdogPredictionMode.mc.thePlayer.UW().bP() instanceof ai)) {
            bs.a(new Vector2f(WatchdogPredictionMode.mc.thePlayer.pl + 45.0f, WatchdogPredictionMode.mc.thePlayer.pm), 10.0, cb.gH);
        }
    };
    @EventLink
    final public Listener<SlowDownEvent> NC = slowDownEvent -> {
        if (((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() && WatchdogPredictionMode.mc.thePlayer.isUsingItem() && WatchdogPredictionMode.mc.thePlayer.UW().bP() instanceof bg && this.MM > ((Number)this.pR.getValue()).intValue()) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && WatchdogPredictionMode.mc.thePlayer.isUsingItem() && WatchdogPredictionMode.mc.thePlayer.UW().bP() instanceof by && this.MM > ((Number)this.pR.getValue()).intValue()) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && WatchdogPredictionMode.mc.thePlayer.isUsingItem() && WatchdogPredictionMode.mc.thePlayer.UW().bP() instanceof cp) {
            ahj.l(new i(j.daw, b.dtf, ao.dvI));
            bc.dispatch();
            if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_19)) {
                UserConnection userConnection = Via.getManager().getConnectionManager().getConnections().iterator().next();
                PacketWrapper packetWrapper = PacketWrapper.create(ServerboundPackets1_19.USE_ITEM, userConnection);
                packetWrapper.write(Types.VAR_INT, 0);
                packetWrapper.write(Types.VAR_INT, WatchdogPredictionMode.mc.bfL.Ha());
                packetWrapper.sendToServer(Protocol1_19To1_18_2.class);
            } else {
                this.d(bu.class);
                ahj.l(new k(bu.bO()));
            }
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DR.getValue()).booleanValue() && WatchdogPredictionMode.mc.thePlayer.isUsingItem() && WatchdogPredictionMode.mc.thePlayer.UW().bP() instanceof ai && this.MM > ((Number)this.pR.getValue()).intValue()) {
            slowDownEvent.cF();
        }
    };

    public WatchdogPredictionMode(String string, NoSlow noSlow) {
        super(string, noSlow);
    }

    @Override
    public void onEnable() {
    }
}



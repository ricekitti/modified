package rise.module.impl.player.scaffold;

import hackclient.rise.ahj;
import hackclient.rise.aif;
import hackclient.rise.aih;
import hackclient.rise.aik;
import hackclient.rise.bb;
import hackclient.rise.bm;
import hackclient.rise.bs;
import hackclient.rise.bu;
import hackclient.rise.cb;
import hackclient.rise.vo;
import java.util.Random;
import lombok.Generated;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.item.cp;
import net.minecraft.network.play.client.e;
import net.minecraft.network.play.server.an;
import net.minecraft.potion.a;
import net.minecraft.util.bt;
import rise.Client;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PacketReceiveEvent;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.PreUpdateEvent;
import rise.event.impl.StrafeEvent;
import rise.module.Mode;
import rise.module.impl.movement.Flight;
import rise.module.impl.movement.LongJump;
import rise.module.impl.movement.Speed;
import rise.module.impl.player.Scaffold;
import rise.module.impl.player.scaffold.WaMode;
import rise.util.math.Vector2f;
import rise.value.BooleanValue;

public class VkMode
extends Mode<Scaffold> {
    private boolean HJ = false;
    private boolean gD = false;
    private boolean aju = false;
    private int ajv = -1;
    public static boolean ajw = false;
    public static boolean ajx = false;
    public static boolean ajy = false;
    public static boolean ajz = false;
    private int ajA = 0;
    private int ajr;
    final public BooleanValue ajB = new BooleanValue("Place Extra blocks", (Mode<?>)this, (Boolean)false);
    final private vo ajC;
    @EventLink(priority=3)
    final public Listener<PreMotionEvent> ajD = preMotionEvent -> {
        double d2;
        float f2;
        float f3;
        ((Scaffold)this.getParent()).agF = 1;
        if (!(Math.abs(VkMode.mc.thePlayer.ue) < 0.1) || VkMode.mc.thePlayer.tR < 2) {
            // empty if block
        }
        boolean bl2 = Math.abs((f3 = bt.bN(f2 = VkMode.mc.thePlayer.pl)) % 90.0f) <= 10.0f || Math.abs(f3 % 90.0f) >= 80.0f;
        double d3 = bt.aJ(Math.toDegrees(aif.direction()));
        if (Math.abs(d3 - (d2 = bt.aJ(Math.toDegrees(Math.atan2(VkMode.mc.thePlayer.uf, VkMode.mc.thePlayer.ud)) - 90.0))) < 5.0 && VkMode.mc.thePlayer.Zl > 7 && !VkMode.mc.aIp.cgM.QQ() && !VkMode.mc.aIp.cgJ.QQ()) {
            if (!(VkMode.mc.thePlayer.cqP <= 2 || VkMode.mc.aIp.cgI.QQ() || Client.INSTANCE.g().c(Speed.class).isEnabled() || VkMode.mc.thePlayer.a(a.dfO) || VkMode.mc.thePlayer.Zl <= 7)) {
                aif.strafe();
                if (bl2) {
                    VkMode.mc.thePlayer.uf *= 1.1225;
                    VkMode.mc.thePlayer.ud *= 1.1225;
                } else {
                    VkMode.mc.thePlayer.uf *= 1.129;
                    VkMode.mc.thePlayer.ud *= 1.129;
                }
            } else if (VkMode.mc.thePlayer.cqP > 2 && !VkMode.mc.aIp.cgI.QQ() && !Client.INSTANCE.g().c(Speed.class).isEnabled() && VkMode.mc.thePlayer.a(a.dfO) && VkMode.mc.thePlayer.b((a)a.dfO).dgC + 1 >= 2 && VkMode.mc.thePlayer.Zl > 7) {
                aif.strafe();
                VkMode.mc.thePlayer.uf *= 1.1055;
                VkMode.mc.thePlayer.ud *= 1.1055;
            } else if (VkMode.mc.thePlayer.cqP > 2 && !VkMode.mc.aIp.cgI.QQ() && !Client.INSTANCE.g().c(Speed.class).isEnabled() && VkMode.mc.thePlayer.a(a.dfO) && VkMode.mc.thePlayer.b((a)a.dfO).dgC + 1 == 1 && VkMode.mc.thePlayer.Zl > 7) {
                aif.strafe();
                VkMode.mc.thePlayer.uf *= 1.098;
                VkMode.mc.thePlayer.ud *= 1.098;
            }
        }
        if (VkMode.mc.aIp.cgJ.QQ()) {
            return;
        }
        int n2 = VkMode.mc.thePlayer.aQx;
        int n3 = this.ajr;
        if (!this.e(Flight.class).isEnabled() && !this.e(LongJump.class).isEnabled() && !bb.a(true, true, false, true, false) && Math.random() > 0.5 && ajy && aif.isMoving() && !(VkMode.mc.thePlayer.UW().bP() instanceof cp)) {
            Random random = new Random();
            float f4 = random.nextFloat();
            float f5 = random.nextFloat();
            ajy = false;
        }
        if ((this.ajv == -1 || n2 - this.ajv < 4) && this.ajv == -1) {
            this.ajv = n2;
        }
        if (VkMode.mc.thePlayer.aO) {
            aif.strafe();
        }
        if (this.gD && !VkMode.mc.aIp.cgI.QQ()) {
            WaMode.akr = VkMode.mc.thePlayer.pl;
            WaMode.qH = 17;
            WaMode.hV = 0;
            aif.stop();
            this.gD = false;
            bs.a(new Vector2f((float)((double)VkMode.mc.thePlayer.pl + (Math.random() - 0.5) * 3.0), 90.0f), 10.0, cb.gH);
            if (!(aih.o(VkMode.mc.thePlayer.distance, VkMode.mc.thePlayer.aI - 2.0, VkMode.mc.thePlayer.gX) instanceof BlockAir)) {
                ((Scaffold)this.getParent()).Me = VkMode.mc.thePlayer.aI - 1.0;
            }
        }
        if (VkMode.mc.aIp.cgI.QQ()) {
            this.gD = true;
        }
    };
    @EventLink(priority=2)
    final public Listener<PreUpdateEvent> ajE = preUpdateEvent -> {
        boolean bl2;
        block17: {
            block16: {
                boolean bl3 = bl2 = VkMode.mc.thePlayer.aI == ((Scaffold)this.getParent()).Me;
                if (!((Boolean)this.ajB.getValue()).booleanValue()) {
                    bl2 = true;
                }
                this.d(bu.class);
                if (bu.bO() != null && VkMode.mc.thePlayer.gX > ((Scaffold)this.getParent()).Me && VkMode.mc.thePlayer.gX + aif.predictedMotion(VkMode.mc.thePlayer.ue, 2) < ((Scaffold)this.getParent()).Me + 1.0 && (!this.aju || ajw) && Client.INSTANCE.g().c(Speed.class).isEnabled() && !((Mode)this.e(Scaffold.class).afO.getValue()).getName().equals("Auto Jump")) break block16;
                this.d(bu.class);
                if (bu.bO() == null || !(VkMode.mc.thePlayer.gX > ((Scaffold)this.getParent()).Me) || !(VkMode.mc.thePlayer.gX + aif.predictedMotion(VkMode.mc.thePlayer.ue, 2) < ((Scaffold)this.getParent()).Me + 1.0) || !((Mode)this.e(Scaffold.class).afO.getValue()).getName().equals("Auto Jump")) break block17;
            }
            this.d(bu.class);
            bu.setSlot(aik.vy());
        }
        if (Client.INSTANCE.g().c(Speed.class).isEnabled()) {
            bl2 = false;
        }
        if (this.aju) {
            bl2 = true;
        }
        if (VkMode.mc.thePlayer.tR > 8) {
            this.aju = false;
        }
        if ((aih.ad(2.0) || aih.ad(1.0) || aih.ad(0.0) || aih.ad(3.0) || VkMode.mc.aIp.cgI.QS() || VkMode.mc.thePlayer.a(a.dfO) || this.gD) && VkMode.mc.thePlayer.cqP < 1) {
            VkMode.mc.thePlayer.crh = false;
        }
        if (VkMode.mc.aIp.cgI.QS() && VkMode.mc.thePlayer.aO && !this.gD && (ajx || ((Mode)this.e(Scaffold.class).afO.getValue()).getName().equals("Auto Jump")) && !Client.INSTANCE.g().c(Speed.class).isEnabled()) {
            ajx = false;
            aif.strafe(aif.ve() * 0.9);
            if (((Mode)this.e(Scaffold.class).afO.getValue()).getName().equals("Auto Jump")) {
                aif.strafe(aif.getAllowedHorizontalDistance() - 0.001);
            }
        }
        if (VkMode.mc.thePlayer.tR == 4 && !VkMode.mc.aIp.cgI.QQ() && !Client.INSTANCE.g().c(Speed.class).isEnabled() && !bl2 && ((Mode)this.e(Scaffold.class).afO.getValue()).getName().equals("Auto Jump")) {
            VkMode.mc.thePlayer.ue -= 0.19;
        }
        if (VkMode.mc.thePlayer.tR == 1 && !VkMode.mc.aIp.cgI.QQ()) {
            aif.strafe();
        }
        if (aih.p(0.0, VkMode.mc.thePlayer.ue, 0.0) != Blocks.cKu && VkMode.mc.thePlayer.tR > 2) {
            aif.strafe();
        }
        if (bl2 && !VkMode.mc.aIp.cgI.QQ() && !VkMode.mc.thePlayer.aO && !this.e(Speed.class).isEnabled() && ((Boolean)this.ajB.getValue()).booleanValue() || !bl2 || VkMode.mc.aIp.cgI.QQ() || VkMode.mc.thePlayer.aO || ((Boolean)this.ajB.getValue()).booleanValue()) {
            // empty if block
        }
        if (bl2 && VkMode.mc.thePlayer.aO && !VkMode.mc.aIp.cgI.QQ()) {
            ++this.ajA;
        }
        if (!bl2) {
            this.ajA = 0;
        }
    };
    @EventLink
    final public Listener<PacketReceiveEvent> ajF = packetReceiveEvent -> {
        if (packetReceiveEvent.dq() instanceof an) {
            ajy = true;
            ++this.ajr;
        }
    };
    @EventLink
    final public Listener<StrafeEvent> ajG = strafeEvent -> {
        if (!((Mode)this.e(Scaffold.class).afO.getValue()).getName().equals("Auto Jump") && VkMode.mc.thePlayer.aO && this.aju && ajw && !Client.INSTANCE.g().c(Speed.class).isEnabled() && !VkMode.mc.aIp.cgI.QQ()) {
            ajw = false;
        }
    };

    public VkMode(String string, Scaffold scaffold) {
        super(string, scaffold);
        this.ajC = new vo(scaffold);
    }

    @Override
    public void onEnable() {
        ajy = false;
        ajz = false;
        if (!((Mode)this.e(Scaffold.class).afO.getValue()).getName().equals("Auto Jump")) {
            if (VkMode.mc.thePlayer.cqP > 9) {
                ajw = true;
                this.aju = true;
            }
        } else {
            VkMode.mc.thePlayer.crh = false;
        }
        ajx = true;
        this.gD = false;
        if (!VkMode.mc.thePlayer.aO && !VkMode.mc.aIp.cgI.QQ()) {
            VkMode.mc.thePlayer.ud *= 0.0;
            VkMode.mc.thePlayer.uf *= 0.0;
        }
        this.ajv = -1;
        this.ajr = 0;
        ajx = true;
    }

    @Override
    public void onDisable() {
        float f2;
        float f3;
        boolean bl2;
        this.HJ = false;
        ajy = false;
        VkMode.mc.thePlayer.crh = false;
        ajw = false;
        this.aju = false;
        if (VkMode.mc.thePlayer.Zl < 20) {
            // empty if block
        }
        boolean bl3 = bl2 = Math.abs((f3 = bt.bN(f2 = VkMode.mc.thePlayer.pl)) % 90.0f) <= 10.0f || Math.abs(f3 % 90.0f) >= 80.0f;
        if (bl2) {
            if (VkMode.mc.thePlayer.cqP > 2 && VkMode.mc.thePlayer.aQx % 3 == 1) {
                VkMode.mc.timer.timerSpeed = 0.5f;
                ahj.l(new e(VkMode.mc.thePlayer.aO));
            } else if (VkMode.mc.thePlayer.cqP > 2 && VkMode.mc.thePlayer.aQx % 3 == 2) {
                VkMode.mc.timer.timerSpeed = 0.33f;
                ahj.l(new e(VkMode.mc.thePlayer.aO));
                ahj.l(new e(VkMode.mc.thePlayer.aO));
            } else if (VkMode.mc.thePlayer.cqP > 2) {
                VkMode.mc.timer.timerSpeed = 0.5f;
                ahj.l(new e(VkMode.mc.thePlayer.aO));
            }
        } else if (VkMode.mc.thePlayer.cqP > 2) {
            VkMode.mc.timer.timerSpeed = 0.33f;
            ahj.l(new e(VkMode.mc.thePlayer.aO));
            ahj.l(new e(VkMode.mc.thePlayer.aO));
        }
        ajx = false;
        if (this.gD) {
            // empty if block
        }
        bm.dispatch();
    }

    @Generated
    public vo kD() {
        return this.ajC;
    }
}



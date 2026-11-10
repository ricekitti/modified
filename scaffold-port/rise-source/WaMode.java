package rise.module.impl.player.scaffold;

import hackclient.rise.aif;
import hackclient.rise.aih;
import hackclient.rise.aik;
import hackclient.rise.aiu;
import hackclient.rise.aka;
import hackclient.rise.bs;
import hackclient.rise.bu;
import hackclient.rise.cb;
import hackclient.rise.eh;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.item.d;
import net.minecraft.network.aj;
import net.minecraft.network.play.client.k;
import net.minecraft.potion.a;
import net.minecraft.util.bt;
import rise.Client;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.JumpEvent;
import rise.event.impl.KeyboardInputEvent;
import rise.event.impl.MoveInputEvent;
import rise.event.impl.PacketSendEvent;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.StrafeEvent;
import rise.event.impl.TickEvent;
import rise.module.Mode;
import rise.module.impl.exploit.Disabler;
import rise.module.impl.movement.Speed;
import rise.module.impl.player.Scaffold;
import rise.util.math.Vector2f;

public class WaMode
extends Mode<Scaffold> {
    public static int qH;
    int dm;
    public static int hV;
    private boolean akl;
    private boolean HJ;
    private int akm = 0;
    private int akn = 0;
    float ako;
    boolean akp = true;
    boolean akq = false;
    private int rW;
    public static float akr;
    double aks = Double.NaN;
    double akt = Double.NaN;
    private int aku;
    private boolean gD = false;
    @EventLink
    final public Listener<JumpEvent> akv = jumpEvent -> {};
    @EventLink(priority=1)
    public Listener<PreMotionEvent> Fr = preMotionEvent -> {
        float f2;
        boolean bl2;
        double d2 = WaMode.mc.thePlayer.pl;
        double d3 = Math.toRadians(d2);
        double d4 = -Math.sin(d3);
        double d5 = Math.cos(d3);
        double d6 = d5 * 1.0;
        boolean bl3 = bl2 = d6 < 0.0;
        if (WaMode.mc.thePlayer.aQx % 3 == 0 && WaMode.mc.thePlayer.aO && WaMode.mc.thePlayer.cqP > 2) {
            return;
        }
        if (!WaMode.mc.aIp.cgI.QQ() && aif.isMoving()) {
            akr = WaMode.mc.thePlayer.pl;
            hV = 0;
            qH = 0;
        } else if (!WaMode.mc.aIp.cgI.QQ()) {
            hV = 100;
            return;
        }
        if (aif.isMoving()) {
            ++qH;
        } else if (qH > 20) {
            --qH;
        }
        if (WaMode.mc.aIp.cgI.QQ()) {
            ++hV;
        }
        if (qH >= 23) {
            qH = 1;
            akr = WaMode.mc.thePlayer.pl;
            hV = 99;
        }
        if (WaMode.mc.thePlayer.aO) {
            hV = 0;
            this.rW = 0;
            akr = WaMode.mc.thePlayer.pl;
        } else if (hV == 100) {
            aif.strafe(0.0);
        }
        if (!aif.isMoving()) {
            double d7 = WaMode.mc.thePlayer.pl;
            double d8 = Math.toRadians(d7);
            double d9 = -Math.sin(d8);
            double d10 = Math.cos(d8);
            boolean bl4 = d10 * 1.0 < 0.0;
            ((Scaffold)this.getParent()).agF = 1;
            if (!this.akq) {
                if (!bl4) {
                    this.aks = Math.floor(WaMode.mc.thePlayer.gY) + 0.999999999999;
                    this.akt = Double.NaN;
                } else {
                    this.akt = Math.floor(WaMode.mc.thePlayer.distance) + 0.999999999999;
                    this.aks = Double.NaN;
                }
                this.akq = true;
            }
            ++this.rW;
            if (Math.abs((double)this.aku - WaMode.mc.thePlayer.gX) >= 1.0) {
                if (this.rW == 1) {
                    aif.stop();
                    if (!this.kG()) {
                        double d11;
                        double d12;
                        double d13;
                        if (!Double.isNaN(this.akt)) {
                            double d14 = WaMode.mc.thePlayer.distance + (this.akt - WaMode.mc.thePlayer.distance) / 3.0;
                            double d15 = WaMode.mc.thePlayer.gX;
                            double d16 = WaMode.mc.thePlayer.gY;
                            if (this.e(d14, d15, d16)) {
                                WaMode.mc.thePlayer.setPosition(d14, d15, d16);
                            }
                        } else if (!Double.isNaN(this.aks) && this.e(d13 = WaMode.mc.thePlayer.distance, d12 = WaMode.mc.thePlayer.gX, d11 = WaMode.mc.thePlayer.gY + (this.aks - WaMode.mc.thePlayer.gY) / 3.0)) {
                            WaMode.mc.thePlayer.setPosition(d13, d12, d11);
                        }
                    }
                } else if (this.rW == 2) {
                    aif.stop();
                    if (!this.kG()) {
                        double d17;
                        double d18;
                        double d19;
                        if (!Double.isNaN(this.akt)) {
                            double d20 = WaMode.mc.thePlayer.distance + 2.0 * (this.akt - WaMode.mc.thePlayer.distance) / 3.0;
                            double d21 = WaMode.mc.thePlayer.gX;
                            double d22 = WaMode.mc.thePlayer.gY;
                            if (this.e(d20, d21, d22)) {
                                WaMode.mc.thePlayer.setPosition(d20, d21, d22);
                            }
                        } else if (!Double.isNaN(this.aks) && this.e(d19 = WaMode.mc.thePlayer.distance, d18 = WaMode.mc.thePlayer.gX, d17 = WaMode.mc.thePlayer.gY + 2.0 * (this.aks - WaMode.mc.thePlayer.gY) / 3.0)) {
                            WaMode.mc.thePlayer.setPosition(d19, d18, d17);
                        }
                        this.kF();
                    } else {
                        this.kF();
                    }
                } else if (this.rW == 3) {
                    aif.stop();
                    if (!this.kG()) {
                        double d23;
                        double d24;
                        double d25;
                        if (!Double.isNaN(this.akt)) {
                            d25 = this.akt;
                            d24 = WaMode.mc.thePlayer.gX;
                            d23 = WaMode.mc.thePlayer.gY;
                            if (this.e(d25, d24, d23)) {
                                WaMode.mc.thePlayer.setPosition(d25, d24, d23);
                                this.kF();
                            }
                        } else if (!Double.isNaN(this.aks) && this.e(d25 = WaMode.mc.thePlayer.distance, d24 = WaMode.mc.thePlayer.gX, d23 = this.aks)) {
                            WaMode.mc.thePlayer.setPosition(d25, d24, d23);
                        }
                        d25 = WaMode.mc.thePlayer.distance;
                        d24 = WaMode.mc.thePlayer.gX;
                        d23 = this.aks;
                        if (this.e(d25, d24, d23)) {
                            // empty if block
                        }
                        this.kF();
                        this.rW = 0;
                        this.akq = false;
                    } else {
                        this.kF();
                    }
                }
            } else {
                this.kF();
                this.rW = 0;
                this.akq = false;
            }
        } else {
            this.akq = false;
        }
        if (this.HJ && !aif.isMoving()) {
            WaMode.mc.aIp.cgI.ay(true);
        }
        if (WaMode.mc.thePlayer.Zl < 1) {
            this.akq = false;
        }
        if (WaMode.mc.aIp.cgI.QQ()) {
            this.gD = true;
        }
        qH = 0;
        if (WaMode.mc.thePlayer.ue < 0.3 && this.HJ && WaMode.mc.thePlayer.ue > 0.17) {
            ((Scaffold)this.getParent()).toggle();
            if (WaMode.mc.thePlayer.aO) {
                WaMode.mc.thePlayer.jump();
            }
            this.HJ = false;
        }
        if (this.gD && !WaMode.mc.aIp.cgI.QQ()) {
            if (!(WaMode.mc.thePlayer.ue < 0.3 && this.HJ && WaMode.mc.thePlayer.ue > 0.17)) {
                // empty if block
            }
            this.gD = false;
        }
        if (WaMode.mc.aIp.cgI.QS() || !WaMode.mc.aIp.cgI.QQ()) {
            // empty if block
        }
        float f3 = f2 = hV == 1 ? 90.0f : 0.0f;
        if (bt.bN(WaMode.mc.thePlayer.pl - akr) < f2) {
            akr = WaMode.mc.thePlayer.pl;
        } else if (bt.bN(WaMode.mc.thePlayer.pl - akr) < 0.0f) {
            akr -= f2;
        } else if (bt.bN(WaMode.mc.thePlayer.pl - akr) > 0.0f) {
            akr += f2;
        }
        if (qH < 20 && !Client.INSTANCE.g().c(Speed.class).isEnabled()) {
            if (WaMode.mc.thePlayer.a(a.dfO)) {
                // empty if block
            }
            if (WaMode.mc.aIp.cgI.QQ()) {
                if (WaMode.mc.thePlayer.a(a.dfO)) {
                    // empty if block
                }
                switch (hV) {
                    case 0: {
                        aif.strafe();
                        if (WaMode.mc.thePlayer.a(a.dfO) && WaMode.mc.thePlayer.b((a)a.dfO).dgC + 1 >= 2) {
                            WaMode.mc.thePlayer.uf *= 1.045;
                            WaMode.mc.thePlayer.ud *= 1.045;
                        } else if (WaMode.mc.thePlayer.a(a.dfO)) {
                            WaMode.mc.thePlayer.uf *= 1.035;
                            WaMode.mc.thePlayer.ud *= 1.035;
                        }
                        if (((Mode)Client.INSTANCE.g().c(Scaffold.class).afO.getValue()).getName().equals("Off")) {
                            // empty if block
                        }
                        if (aif.speed() < 0.24) {
                            // empty if block
                        }
                        WaMode.mc.thePlayer.ue = 0.42;
                        break;
                    }
                    case 1: {
                        aif.strafe();
                        if (aif.speed() < 0.24) {
                            // empty if block
                        }
                        if (!WaMode.mc.thePlayer.a(a.dfO) || WaMode.mc.thePlayer.b((a)a.dfO).dgC + 1 >= 2) {
                            // empty if block
                        }
                        WaMode.mc.thePlayer.ue = 0.33;
                        if (WaMode.mc.thePlayer.a(a.dfO) && WaMode.mc.thePlayer.b((a)a.dfO).dgC + 1 >= 2) {
                            WaMode.mc.thePlayer.uf *= 1.015;
                            WaMode.mc.thePlayer.ud *= 1.015;
                        } else if (WaMode.mc.thePlayer.a(a.dfO)) {
                            WaMode.mc.thePlayer.uf *= 1.005;
                            WaMode.mc.thePlayer.ud *= 1.005;
                        }
                        if (!((Mode)Client.INSTANCE.g().c(Scaffold.class).afO.getValue()).getName().equals("Off")) break;
                        break;
                    }
                    case 2: {
                        if (WaMode.mc.thePlayer.a(a.dfO)) {
                            // empty if block
                        }
                        if (((Boolean)this.e(Disabler.class).wu.getValue()).booleanValue() && this.e(Disabler.class).isEnabled()) {
                            aif.strafe();
                        }
                        WaMode.mc.thePlayer.ue = 1.0 - WaMode.mc.thePlayer.gX % 1.0;
                    }
                }
            }
        } else if (!WaMode.mc.thePlayer.aO) {
            // empty if block
        }
        if (!(WaMode.mc.thePlayer.ue > 0.0) || !WaMode.mc.aIp.cgI.QQ()) {
            // empty if block
        }
        if (qH == 20 || qH == 21) {
            this.ako = akr;
        } else if (qH > 20) {
            // empty if block
        }
        if (qH == 22 && !Client.INSTANCE.g().c(Speed.class).isEnabled()) {
            if (WaMode.mc.thePlayer.cqP != 0 || !WaMode.mc.thePlayer.a(a.dfO)) {
                // empty if block
            }
            if (WaMode.mc.thePlayer.cqP == 1) {
                // empty if block
            }
            WaMode.mc.thePlayer.ue = WaMode.mc.thePlayer.ue < 0.2 && WaMode.mc.thePlayer.ue > 0.1 ? (WaMode.mc.thePlayer.ue -= 0.42) : -0.0784000015258789;
            if (WaMode.mc.thePlayer.a(a.dfO)) {
                // empty if block
            }
        }
        if (WaMode.mc.thePlayer.Zl == 1) {
            // empty if block
        }
        if (hV == 2) {
            hV = -1;
        }
        ((Scaffold)this.getParent()).agF = 1;
    };
    @EventLink
    final public Listener<eh> akw = eh2 -> {
        if (hV == 2) {
            // empty if block
        }
    };
    @EventLink
    final public Listener<MoveInputEvent> akx = moveInputEvent -> {
        if (aif.isMoving() && WaMode.mc.aIp.cgI.QQ() && !this.e(Speed.class).isEnabled()) {
            moveInputEvent.setForward(moveInputEvent.getForward() * 5.0f);
        }
    };
    @EventLink
    final public Listener<StrafeEvent> aky = strafeEvent -> {
        if (WaMode.mc.aIp.cgI.QQ() && (WaMode.mc.aIp.cgH.QQ() || WaMode.mc.aIp.cgF.QQ())) {
            aif.strafe(0.25);
        }
        if (aif.speed() < 0.19 && WaMode.mc.aIp.cgI.QQ() && WaMode.mc.thePlayer.Zl > 9) {
            aif.strafe(0.19);
        }
        if (qH < 16 || WaMode.mc.thePlayer.a(a.dfO)) {
            // empty if block
        }
    };
    @EventLink
    final public Listener<KeyboardInputEvent> akz = keyboardInputEvent -> {
        if (keyboardInputEvent.cO() == ((Scaffold)this.getParent()).getKey() && !this.HJ && WaMode.mc.aIp.cgI.QQ() && !Client.INSTANCE.g().c(Speed.class).isEnabled()) {
            keyboardInputEvent.cF();
            this.HJ = true;
        }
    };
    @EventLink
    final public Listener<TickEvent> akA = tickEvent -> {};
    @EventLink
    final public Listener<PacketSendEvent> akB = packetSendEvent -> {
        k k2;
        double d2 = WaMode.mc.thePlayer.pl;
        double d3 = Math.toRadians(d2);
        double d4 = -Math.sin(d3);
        double d5 = Math.cos(d3);
        double d6 = d5 * 1.0;
        boolean bl2 = d6 < 0.0;
        aj<?> aj2 = packetSendEvent.dq();
        if (aj2 instanceof k && (k2 = (k)aj2).getPlacedBlockDirection() == 1 && (k2.ld() == null || k2.ld().bP() != d.I(Blocks.cMc))) {
            this.akl = true;
            ++this.akm;
            if (this.akm >= 2) {
                // empty if block
            }
        } else {
            k k3;
            aj2 = packetSendEvent.dq();
            if (aj2 instanceof k && ((k3 = (k)aj2).ld() == null || k3.ld().bP() != d.I(Blocks.cMc))) {
                this.akm = 0;
                this.akl = false;
            }
        }
        if (!this.akl || this.e(Speed.class).isEnabled() || this.akm < 2 || hV == 1) {
            // empty if block
        }
    };
    @EventLink
    final public Listener<JumpEvent> akC = jumpEvent -> {
        if (!this.e(Speed.class).isEnabled() && WaMode.mc.aIp.cgI.QQ() && !WaMode.mc.thePlayer.a(a.dfO)) {
            aif.strafe(0.15);
        }
        if (this.HJ) {
            aif.strafe(0.0);
        }
    };

    public WaMode(String string, Scaffold scaffold) {
        super(string, scaffold);
    }

    @Override
    public void onEnable() {
        this.akl = false;
        this.d(bu.class);
        bu.setSlot(aik.vy());
        this.akn = 0;
        this.HJ = false;
        this.aks = Double.NaN;
        qH = 0;
        this.rW = 0;
        akr = WaMode.mc.thePlayer.pl;
        this.akq = false;
        hV = WaMode.mc.thePlayer.aO ? 0 : 100;
        if (((Mode)Client.INSTANCE.g().c(Scaffold.class).afO.getValue()).getName().equals("Off") && !WaMode.mc.thePlayer.aO) {
            this.akp = true;
        }
        this.gD = false;
    }

    @Override
    public void onDisable() {
        this.akq = false;
        this.akp = false;
        this.HJ = false;
        akr = WaMode.mc.thePlayer.pl;
        hV = 100;
        this.rW = 0;
        if (WaMode.mc.aIp.cgI.QQ()) {
            aif.strafe(0.23);
        }
    }

    public void kF() {
        aka aka2;
        boolean bl2;
        this.aku = (int)Math.floor(WaMode.mc.thePlayer.gX);
        double d2 = WaMode.mc.thePlayer.pl;
        double d3 = Math.toRadians(d2);
        double d4 = -Math.sin(d3);
        double d5 = Math.cos(d3);
        double d6 = d5 * 1.0;
        boolean bl3 = bl2 = d6 < 0.0;
        if (bl2) {
            bs.d(false);
            bs.a(new Vector2f((float)((double)(WaMode.mc.thePlayer.pl - 164.0f) + (Math.random() - 0.5) * 3.0), 86.0f), 10.0, cb.gG);
            aka2 = new aka(1.0, 0.0, 0.0);
        } else {
            aka2 = new aka(0.0, 0.0, 1.0);
        }
        ((Scaffold)this.getParent()).agy = aka2;
        Vector2f vector2f = aiu.d(aka2);
        if (!aif.isMoving() && !bl2) {
            bs.d(false);
            float f2 = (float)(d2 + 164.0 + (Math.random() - 0.5) * 3.0);
            float f3 = 86.0f;
            bs.d(false);
            bs.a(new Vector2f(f2, f3), 10.0, cb.gG);
        }
    }

    private boolean kG() {
        Block[] blockArray;
        int n2 = (int)Math.floor(WaMode.mc.thePlayer.distance);
        int n3 = (int)Math.floor(WaMode.mc.thePlayer.gX);
        int n4 = (int)Math.floor(WaMode.mc.thePlayer.gY);
        for (Block block : blockArray = new Block[]{aih.o(n2 + 1, n3, n4), aih.o(n2 - 1, n3, n4), aih.o(n2, n3, n4 + 1), aih.o(n2, n3, n4 - 1)}) {
            if (!(block instanceof BlockAir)) continue;
            return false;
        }
        return true;
    }

    private boolean e(double d2, double d3, double d4) {
        float f2 = WaMode.mc.thePlayer.width / 2.0f;
        double d5 = d2 - (double)f2;
        double d6 = d3;
        double d7 = d4 - (double)f2;
        double d8 = d2 + (double)f2;
        double d9 = d3 + (double)WaMode.mc.thePlayer.eyeHeight;
        double d10 = d4 + (double)f2;
        for (int i2 = bt.aF(d5); i2 <= bt.aF(d8); ++i2) {
            for (int i3 = bt.aF(d6); i3 <= bt.aF(d9); ++i3) {
                for (int i4 = bt.aF(d7); i4 <= bt.aF(d10); ++i4) {
                    Block block = aih.o(i2, i3, i4);
                    if (block instanceof BlockAir) continue;
                    return false;
                }
            }
        }
        return true;
    }
}



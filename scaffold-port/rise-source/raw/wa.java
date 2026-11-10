/*
 * Decompiled with CFR 0.152.
 */
package hackclient.rise;

import hackclient.rise.a;
import hackclient.rise.aif;
import hackclient.rise.aih;
import hackclient.rise.aik;
import hackclient.rise.aiu;
import hackclient.rise.ajy;
import hackclient.rise.aka;
import hackclient.rise.akd;
import hackclient.rise.bs;
import hackclient.rise.bu;
import hackclient.rise.cb;
import hackclient.rise.dk;
import hackclient.rise.dm;
import hackclient.rise.dx;
import hackclient.rise.dz;
import hackclient.rise.ed;
import hackclient.rise.eh;
import hackclient.rise.ei;
import hackclient.rise.eo;
import hackclient.rise.fe;
import hackclient.rise.fi;
import hackclient.rise.jg;
import hackclient.rise.mh;
import hackclient.rise.tn;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.item.d;
import net.minecraft.network.aj;
import net.minecraft.network.play.client.k;
import net.minecraft.util.bt;

public class wa
extends akd<tn> {
    static public int qH;
    int dm;
    static public int hV;
    private boolean akl;
    private boolean HJ;
    private int akm = 0;
    private int akn = 0;
    float ako;
    boolean akp = true;
    boolean akq = false;
    private int rW;
    static public float akr;
    double aks = Double.NaN;
    double akt = Double.NaN;
    private int aku;
    private boolean gD = false;
    @dm
    public final dk<ed> akv = ed2 -> {};
    @dm(cH=1)
    public dk<ei> Fr = ei2 -> {
        float f2;
        boolean bl2;
        double d2 = wa.aEi.bgb.pl;
        double d3 = Math.toRadians(d2);
        double d4 = -Math.sin(d3);
        double d5 = Math.cos(d3);
        double d6 = d5 * 1.0;
        boolean bl3 = bl2 = d6 < 0.0;
        if (wa.aEi.bgb.aQx % 3 == 0 && wa.aEi.bgb.aO && wa.aEi.bgb.cqP > 2) {
            return;
        }
        if (!wa.aEi.aIp.cgI.QQ() && aif.isMoving()) {
            akr = wa.aEi.bgb.pl;
            hV = 0;
            qH = 0;
        } else if (!wa.aEi.aIp.cgI.QQ()) {
            hV = 100;
            return;
        }
        if (aif.isMoving()) {
            ++qH;
        } else if (qH > 20) {
            --qH;
        }
        if (wa.aEi.aIp.cgI.QQ()) {
            ++hV;
        }
        if (qH >= 23) {
            qH = 1;
            akr = wa.aEi.bgb.pl;
            hV = 99;
        }
        if (wa.aEi.bgb.aO) {
            hV = 0;
            this.rW = 0;
            akr = wa.aEi.bgb.pl;
        } else if (hV == 100) {
            aif.strafe(0.0);
        }
        if (!aif.isMoving()) {
            double d7 = wa.aEi.bgb.pl;
            double d8 = Math.toRadians(d7);
            double d9 = -Math.sin(d8);
            double d10 = Math.cos(d8);
            boolean bl4 = d10 * 1.0 < 0.0;
            ((tn)this.wk()).agF = 1;
            if (!this.akq) {
                if (!bl4) {
                    this.aks = Math.floor(wa.aEi.bgb.gY) + 0.999999999999;
                    this.akt = Double.NaN;
                } else {
                    this.akt = Math.floor(wa.aEi.bgb.gW) + 0.999999999999;
                    this.aks = Double.NaN;
                }
                this.akq = true;
            }
            ++this.rW;
            if (Math.abs((double)this.aku - wa.aEi.bgb.gX) >= 1.0) {
                if (this.rW == 1) {
                    aif.stop();
                    if (!this.kG()) {
                        double d11;
                        double d12;
                        double d13;
                        if (!Double.isNaN(this.akt)) {
                            double d14 = wa.aEi.bgb.gW + (this.akt - wa.aEi.bgb.gW) / 3.0;
                            double d15 = wa.aEi.bgb.gX;
                            double d16 = wa.aEi.bgb.gY;
                            if (this.e(d14, d15, d16)) {
                                wa.aEi.bgb.setPosition(d14, d15, d16);
                            }
                        } else if (!Double.isNaN(this.aks) && this.e(d13 = wa.aEi.bgb.gW, d12 = wa.aEi.bgb.gX, d11 = wa.aEi.bgb.gY + (this.aks - wa.aEi.bgb.gY) / 3.0)) {
                            wa.aEi.bgb.setPosition(d13, d12, d11);
                        }
                    }
                } else if (this.rW == 2) {
                    aif.stop();
                    if (!this.kG()) {
                        double d17;
                        double d18;
                        double d19;
                        if (!Double.isNaN(this.akt)) {
                            double d20 = wa.aEi.bgb.gW + 2.0 * (this.akt - wa.aEi.bgb.gW) / 3.0;
                            double d21 = wa.aEi.bgb.gX;
                            double d22 = wa.aEi.bgb.gY;
                            if (this.e(d20, d21, d22)) {
                                wa.aEi.bgb.setPosition(d20, d21, d22);
                            }
                        } else if (!Double.isNaN(this.aks) && this.e(d19 = wa.aEi.bgb.gW, d18 = wa.aEi.bgb.gX, d17 = wa.aEi.bgb.gY + 2.0 * (this.aks - wa.aEi.bgb.gY) / 3.0)) {
                            wa.aEi.bgb.setPosition(d19, d18, d17);
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
                            d24 = wa.aEi.bgb.gX;
                            d23 = wa.aEi.bgb.gY;
                            if (this.e(d25, d24, d23)) {
                                wa.aEi.bgb.setPosition(d25, d24, d23);
                                this.kF();
                            }
                        } else if (!Double.isNaN(this.aks) && this.e(d25 = wa.aEi.bgb.gW, d24 = wa.aEi.bgb.gX, d23 = this.aks)) {
                            wa.aEi.bgb.setPosition(d25, d24, d23);
                        }
                        d25 = wa.aEi.bgb.gW;
                        d24 = wa.aEi.bgb.gX;
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
            wa.aEi.aIp.cgI.ay(true);
        }
        if (wa.aEi.bgb.Zl < 1) {
            this.akq = false;
        }
        if (wa.aEi.aIp.cgI.QQ()) {
            this.gD = true;
        }
        qH = 0;
        if (wa.aEi.bgb.ue < 0.3 && this.HJ && wa.aEi.bgb.ue > 0.17) {
            ((tn)this.wk()).toggle();
            if (wa.aEi.bgb.aO) {
                wa.aEi.bgb.jump();
            }
            this.HJ = false;
        }
        if (this.gD && !wa.aEi.aIp.cgI.QQ()) {
            if (!(wa.aEi.bgb.ue < 0.3 && this.HJ && wa.aEi.bgb.ue > 0.17)) {
                // empty if block
            }
            this.gD = false;
        }
        if (wa.aEi.aIp.cgI.QS() || !wa.aEi.aIp.cgI.QQ()) {
            // empty if block
        }
        float f3 = f2 = hV == 1 ? 90.0f : 0.0f;
        if (bt.bN(wa.aEi.bgb.pl - akr) < f2) {
            akr = wa.aEi.bgb.pl;
        } else if (bt.bN(wa.aEi.bgb.pl - akr) < 0.0f) {
            akr -= f2;
        } else if (bt.bN(wa.aEi.bgb.pl - akr) > 0.0f) {
            akr += f2;
        }
        if (qH < 20 && !a.a.g().c(mh.class).isEnabled()) {
            if (wa.aEi.bgb.a(net.minecraft.potion.a.dfO)) {
                // empty if block
            }
            if (wa.aEi.aIp.cgI.QQ()) {
                if (wa.aEi.bgb.a(net.minecraft.potion.a.dfO)) {
                    // empty if block
                }
                switch (hV) {
                    case 0: {
                        aif.strafe();
                        if (wa.aEi.bgb.a(net.minecraft.potion.a.dfO) && wa.aEi.bgb.b((net.minecraft.potion.a)net.minecraft.potion.a.dfO).dgC + 1 >= 2) {
                            wa.aEi.bgb.uf *= 1.045;
                            wa.aEi.bgb.ud *= 1.045;
                        } else if (wa.aEi.bgb.a(net.minecraft.potion.a.dfO)) {
                            wa.aEi.bgb.uf *= 1.035;
                            wa.aEi.bgb.ud *= 1.035;
                        }
                        if (((akd)a.a.g().c(tn.class).afO.wp()).getName().equals("Off")) {
                            // empty if block
                        }
                        if (aif.speed() < 0.24) {
                            // empty if block
                        }
                        wa.aEi.bgb.ue = 0.42;
                        break;
                    }
                    case 1: {
                        aif.strafe();
                        if (aif.speed() < 0.24) {
                            // empty if block
                        }
                        if (!wa.aEi.bgb.a(net.minecraft.potion.a.dfO) || wa.aEi.bgb.b((net.minecraft.potion.a)net.minecraft.potion.a.dfO).dgC + 1 >= 2) {
                            // empty if block
                        }
                        wa.aEi.bgb.ue = 0.33;
                        if (wa.aEi.bgb.a(net.minecraft.potion.a.dfO) && wa.aEi.bgb.b((net.minecraft.potion.a)net.minecraft.potion.a.dfO).dgC + 1 >= 2) {
                            wa.aEi.bgb.uf *= 1.015;
                            wa.aEi.bgb.ud *= 1.015;
                        } else if (wa.aEi.bgb.a(net.minecraft.potion.a.dfO)) {
                            wa.aEi.bgb.uf *= 1.005;
                            wa.aEi.bgb.ud *= 1.005;
                        }
                        if (!((akd)a.a.g().c(tn.class).afO.wp()).getName().equals("Off")) break;
                        break;
                    }
                    case 2: {
                        if (wa.aEi.bgb.a(net.minecraft.potion.a.dfO)) {
                            // empty if block
                        }
                        if (((Boolean)this.e(jg.class).wu.wp()).booleanValue() && this.e(jg.class).isEnabled()) {
                            aif.strafe();
                        }
                        wa.aEi.bgb.ue = 1.0 - wa.aEi.bgb.gX % 1.0;
                    }
                }
            }
        } else if (!wa.aEi.bgb.aO) {
            // empty if block
        }
        if (!(wa.aEi.bgb.ue > 0.0) || !wa.aEi.aIp.cgI.QQ()) {
            // empty if block
        }
        if (qH == 20 || qH == 21) {
            this.ako = akr;
        } else if (qH > 20) {
            // empty if block
        }
        if (qH == 22 && !a.a.g().c(mh.class).isEnabled()) {
            if (wa.aEi.bgb.cqP != 0 || !wa.aEi.bgb.a(net.minecraft.potion.a.dfO)) {
                // empty if block
            }
            if (wa.aEi.bgb.cqP == 1) {
                // empty if block
            }
            wa.aEi.bgb.ue = wa.aEi.bgb.ue < 0.2 && wa.aEi.bgb.ue > 0.1 ? (wa.aEi.bgb.ue -= 0.42) : -0.0784000015258789;
            if (wa.aEi.bgb.a(net.minecraft.potion.a.dfO)) {
                // empty if block
            }
        }
        if (wa.aEi.bgb.Zl == 1) {
            // empty if block
        }
        if (hV == 2) {
            hV = -1;
        }
        ((tn)this.wk()).agF = 1;
    };
    @dm
    public final dk<eh> akw = eh2 -> {
        if (hV == 2) {
            // empty if block
        }
    };
    @dm
    public final dk<dz> akx = dz2 -> {
        if (aif.isMoving() && wa.aEi.aIp.cgI.QQ() && !this.e(mh.class).isEnabled()) {
            dz2.setForward(dz2.getForward() * 5.0f);
        }
    };
    @dm
    public final dk<eo> aky = eo2 -> {
        if (wa.aEi.aIp.cgI.QQ() && (wa.aEi.aIp.cgH.QQ() || wa.aEi.aIp.cgF.QQ())) {
            aif.strafe(0.25);
        }
        if (aif.speed() < 0.19 && wa.aEi.aIp.cgI.QQ() && wa.aEi.bgb.Zl > 9) {
            aif.strafe(0.19);
        }
        if (qH < 16 || wa.aEi.bgb.a(net.minecraft.potion.a.dfO)) {
            // empty if block
        }
    };
    @dm
    public final dk<dx> akz = dx2 -> {
        if (dx2.cO() == ((tn)this.wk()).getKey() && !this.HJ && wa.aEi.aIp.cgI.QQ() && !a.a.g().c(mh.class).isEnabled()) {
            dx2.cF();
            this.HJ = true;
        }
    };
    @dm
    public final dk<fe> akA = fe2 -> {};
    @dm
    public final dk<fi> akB = fi2 -> {
        k k2;
        double d2 = wa.aEi.bgb.pl;
        double d3 = Math.toRadians(d2);
        double d4 = -Math.sin(d3);
        double d5 = Math.cos(d3);
        double d6 = d5 * 1.0;
        boolean bl2 = d6 < 0.0;
        aj<?> aj2 = fi2.dq();
        if (aj2 instanceof k && (k2 = (k)aj2).getPlacedBlockDirection() == 1 && (k2.ld() == null || k2.ld().bP() != d.I(Blocks.cMc))) {
            this.akl = true;
            ++this.akm;
            if (this.akm >= 2) {
                // empty if block
            }
        } else {
            k k3;
            aj2 = fi2.dq();
            if (aj2 instanceof k && ((k3 = (k)aj2).ld() == null || k3.ld().bP() != d.I(Blocks.cMc))) {
                this.akm = 0;
                this.akl = false;
            }
        }
        if (!this.akl || this.e(mh.class).isEnabled() || this.akm < 2 || hV == 1) {
            // empty if block
        }
    };
    @dm
    public final dk<ed> akC = ed2 -> {
        if (!this.e(mh.class).isEnabled() && wa.aEi.aIp.cgI.QQ() && !wa.aEi.bgb.a(net.minecraft.potion.a.dfO)) {
            aif.strafe(0.15);
        }
        if (this.HJ) {
            aif.strafe(0.0);
        }
    };

    public wa(String string, tn tn2) {
        super(string, tn2);
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
        akr = wa.aEi.bgb.pl;
        this.akq = false;
        hV = wa.aEi.bgb.aO ? 0 : 100;
        if (((akd)a.a.g().c(tn.class).afO.wp()).getName().equals("Off") && !wa.aEi.bgb.aO) {
            this.akp = true;
        }
        this.gD = false;
    }

    @Override
    public void onDisable() {
        this.akq = false;
        this.akp = false;
        this.HJ = false;
        akr = wa.aEi.bgb.pl;
        hV = 100;
        this.rW = 0;
        if (wa.aEi.aIp.cgI.QQ()) {
            aif.strafe(0.23);
        }
    }

    public void kF() {
        aka aka2;
        boolean bl2;
        this.aku = (int)Math.floor(wa.aEi.bgb.gX);
        double d2 = wa.aEi.bgb.pl;
        double d3 = Math.toRadians(d2);
        double d4 = -Math.sin(d3);
        double d5 = Math.cos(d3);
        double d6 = d5 * 1.0;
        boolean bl3 = bl2 = d6 < 0.0;
        if (bl2) {
            bs.d(false);
            bs.a(new ajy((float)((double)(wa.aEi.bgb.pl - 164.0f) + (Math.random() - 0.5) * 3.0), 86.0f), 10.0, cb.gG);
            aka2 = new aka(1.0, 0.0, 0.0);
        } else {
            aka2 = new aka(0.0, 0.0, 1.0);
        }
        ((tn)this.wk()).agy = aka2;
        ajy ajy2 = aiu.d(aka2);
        if (!aif.isMoving() && !bl2) {
            bs.d(false);
            float f2 = (float)(d2 + 164.0 + (Math.random() - 0.5) * 3.0);
            float f3 = 86.0f;
            bs.d(false);
            bs.a(new ajy(f2, f3), 10.0, cb.gG);
        }
    }

    private boolean kG() {
        Block[] blockArray;
        int n2 = (int)Math.floor(wa.aEi.bgb.gW);
        int n3 = (int)Math.floor(wa.aEi.bgb.gX);
        int n4 = (int)Math.floor(wa.aEi.bgb.gY);
        for (Block block : blockArray = new Block[]{aih.o(n2 + 1, n3, n4), aih.o(n2 - 1, n3, n4), aih.o(n2, n3, n4 + 1), aih.o(n2, n3, n4 - 1)}) {
            if (!(block instanceof BlockAir)) continue;
            return false;
        }
        return true;
    }

    private boolean e(double d2, double d3, double d4) {
        float f2 = wa.aEi.bgb.width / 2.0f;
        double d5 = d2 - (double)f2;
        double d6 = d3;
        double d7 = d4 - (double)f2;
        double d8 = d2 + (double)f2;
        double d9 = d3 + (double)wa.aEi.bgb.height;
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



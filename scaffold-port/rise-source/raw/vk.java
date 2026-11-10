/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package hackclient.rise;

import hackclient.rise.a;
import hackclient.rise.ahj;
import hackclient.rise.aif;
import hackclient.rise.aih;
import hackclient.rise.aik;
import hackclient.rise.ajy;
import hackclient.rise.akd;
import hackclient.rise.akf;
import hackclient.rise.bb;
import hackclient.rise.bm;
import hackclient.rise.bs;
import hackclient.rise.bu;
import hackclient.rise.cb;
import hackclient.rise.dk;
import hackclient.rise.dm;
import hackclient.rise.ei;
import hackclient.rise.ej;
import hackclient.rise.eo;
import hackclient.rise.fh;
import hackclient.rise.lw;
import hackclient.rise.lz;
import hackclient.rise.mh;
import hackclient.rise.tn;
import hackclient.rise.vo;
import hackclient.rise.wa;
import java.util.Random;
import lombok.Generated;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.item.cp;
import net.minecraft.network.play.client.e;
import net.minecraft.network.play.server.an;
import net.minecraft.util.bt;

public class vk
extends akd<tn> {
    private boolean HJ = false;
    private boolean gD = false;
    private boolean aju = false;
    private int ajv = -1;
    static public boolean ajw = false;
    static public boolean ajx = false;
    static public boolean ajy = false;
    static public boolean ajz = false;
    private int ajA = 0;
    private int ajr;
    public final akf ajB = new akf("Place Extra blocks", (akd<?>)this, (Boolean)false);
    final private vo ajC;
    @dm(cH=3)
    public final dk<ei> ajD = ei2 -> {
        double d2;
        float f2;
        float f3;
        ((tn)this.wk()).agF = 1;
        if (!(Math.abs(vk.aEi.bgb.ue) < 0.1) || vk.aEi.bgb.tR < 2) {
            // empty if block
        }
        boolean bl2 = Math.abs((f3 = bt.bN(f2 = vk.aEi.bgb.pl)) % 90.0f) <= 10.0f || Math.abs(f3 % 90.0f) >= 80.0f;
        double d3 = bt.aJ(Math.toDegrees(aif.direction()));
        if (Math.abs(d3 - (d2 = bt.aJ(Math.toDegrees(Math.atan2(vk.aEi.bgb.uf, vk.aEi.bgb.ud)) - 90.0))) < 5.0 && vk.aEi.bgb.Zl > 7 && !vk.aEi.aIp.cgM.QQ() && !vk.aEi.aIp.cgJ.QQ()) {
            if (!(vk.aEi.bgb.cqP <= 2 || vk.aEi.aIp.cgI.QQ() || a.a.g().c(mh.class).isEnabled() || vk.aEi.bgb.a(net.minecraft.potion.a.dfO) || vk.aEi.bgb.Zl <= 7)) {
                aif.strafe();
                if (bl2) {
                    vk.aEi.bgb.uf *= 1.1225;
                    vk.aEi.bgb.ud *= 1.1225;
                } else {
                    vk.aEi.bgb.uf *= 1.129;
                    vk.aEi.bgb.ud *= 1.129;
                }
            } else if (vk.aEi.bgb.cqP > 2 && !vk.aEi.aIp.cgI.QQ() && !a.a.g().c(mh.class).isEnabled() && vk.aEi.bgb.a(net.minecraft.potion.a.dfO) && vk.aEi.bgb.b((net.minecraft.potion.a)net.minecraft.potion.a.dfO).dgC + 1 >= 2 && vk.aEi.bgb.Zl > 7) {
                aif.strafe();
                vk.aEi.bgb.uf *= 1.1055;
                vk.aEi.bgb.ud *= 1.1055;
            } else if (vk.aEi.bgb.cqP > 2 && !vk.aEi.aIp.cgI.QQ() && !a.a.g().c(mh.class).isEnabled() && vk.aEi.bgb.a(net.minecraft.potion.a.dfO) && vk.aEi.bgb.b((net.minecraft.potion.a)net.minecraft.potion.a.dfO).dgC + 1 == 1 && vk.aEi.bgb.Zl > 7) {
                aif.strafe();
                vk.aEi.bgb.uf *= 1.098;
                vk.aEi.bgb.ud *= 1.098;
            }
        }
        if (vk.aEi.aIp.cgJ.QQ()) {
            return;
        }
        int n2 = vk.aEi.bgb.aQx;
        int n3 = this.ajr;
        if (!this.e(lw.class).isEnabled() && !this.e(lz.class).isEnabled() && !bb.a(true, true, false, true, false) && Math.random() > 0.5 && ajy && aif.isMoving() && !(vk.aEi.bgb.UW().bP() instanceof cp)) {
            Random random = new Random();
            float f4 = random.nextFloat();
            float f5 = random.nextFloat();
            ajy = false;
        }
        if ((this.ajv == -1 || n2 - this.ajv < 4) && this.ajv == -1) {
            this.ajv = n2;
        }
        if (vk.aEi.bgb.aO) {
            aif.strafe();
        }
        if (this.gD && !vk.aEi.aIp.cgI.QQ()) {
            wa.akr = vk.aEi.bgb.pl;
            wa.qH = 17;
            wa.hV = 0;
            aif.stop();
            this.gD = false;
            bs.a(new ajy((float)((double)vk.aEi.bgb.pl + (Math.random() - 0.5) * 3.0), 90.0f), 10.0, cb.gH);
            if (!(aih.o(vk.aEi.bgb.gW, vk.aEi.bgb.aI - 2.0, vk.aEi.bgb.gX) instanceof BlockAir)) {
                ((tn)this.wk()).Me = vk.aEi.bgb.aI - 1.0;
            }
        }
        if (vk.aEi.aIp.cgI.QQ()) {
            this.gD = true;
        }
    };
    @dm(cH=2)
    public final dk<ej> ajE = ej2 -> {
        boolean bl2;
        block17: {
            block16: {
                boolean bl3 = bl2 = vk.aEi.bgb.aI == ((tn)this.wk()).Me;
                if (!((Boolean)this.ajB.wp()).booleanValue()) {
                    bl2 = true;
                }
                this.d(bu.class);
                if (bu.bO() != null && vk.aEi.bgb.gX > ((tn)this.wk()).Me && vk.aEi.bgb.gX + aif.predictedMotion(vk.aEi.bgb.ue, 2) < ((tn)this.wk()).Me + 1.0 && (!this.aju || ajw) && a.a.g().c(mh.class).isEnabled() && !((akd)this.e(tn.class).afO.wp()).getName().equals("Auto Jump")) break block16;
                this.d(bu.class);
                if (bu.bO() == null || !(vk.aEi.bgb.gX > ((tn)this.wk()).Me) || !(vk.aEi.bgb.gX + aif.predictedMotion(vk.aEi.bgb.ue, 2) < ((tn)this.wk()).Me + 1.0) || !((akd)this.e(tn.class).afO.wp()).getName().equals("Auto Jump")) break block17;
            }
            this.d(bu.class);
            bu.setSlot(aik.vy());
        }
        if (a.a.g().c(mh.class).isEnabled()) {
            bl2 = false;
        }
        if (this.aju) {
            bl2 = true;
        }
        if (vk.aEi.bgb.tR > 8) {
            this.aju = false;
        }
        if ((aih.ad(2.0) || aih.ad(1.0) || aih.ad(0.0) || aih.ad(3.0) || vk.aEi.aIp.cgI.QS() || vk.aEi.bgb.a(net.minecraft.potion.a.dfO) || this.gD) && vk.aEi.bgb.cqP < 1) {
            vk.aEi.bgb.crh = false;
        }
        if (vk.aEi.aIp.cgI.QS() && vk.aEi.bgb.aO && !this.gD && (ajx || ((akd)this.e(tn.class).afO.wp()).getName().equals("Auto Jump")) && !a.a.g().c(mh.class).isEnabled()) {
            ajx = false;
            aif.strafe(aif.ve() * 0.9);
            if (((akd)this.e(tn.class).afO.wp()).getName().equals("Auto Jump")) {
                aif.strafe(aif.getAllowedHorizontalDistance() - 0.001);
            }
        }
        if (vk.aEi.bgb.tR == 4 && !vk.aEi.aIp.cgI.QQ() && !a.a.g().c(mh.class).isEnabled() && !bl2 && ((akd)this.e(tn.class).afO.wp()).getName().equals("Auto Jump")) {
            vk.aEi.bgb.ue -= 0.19;
        }
        if (vk.aEi.bgb.tR == 1 && !vk.aEi.aIp.cgI.QQ()) {
            aif.strafe();
        }
        if (aih.p(0.0, vk.aEi.bgb.ue, 0.0) != Blocks.cKu && vk.aEi.bgb.tR > 2) {
            aif.strafe();
        }
        if (bl2 && !vk.aEi.aIp.cgI.QQ() && !vk.aEi.bgb.aO && !this.e(mh.class).isEnabled() && ((Boolean)this.ajB.wp()).booleanValue() || !bl2 || vk.aEi.aIp.cgI.QQ() || vk.aEi.bgb.aO || ((Boolean)this.ajB.wp()).booleanValue()) {
            // empty if block
        }
        if (bl2 && vk.aEi.bgb.aO && !vk.aEi.aIp.cgI.QQ()) {
            ++this.ajA;
        }
        if (!bl2) {
            this.ajA = 0;
        }
    };
    @dm
    public final dk<fh> ajF = fh2 -> {
        if (fh2.dq() instanceof an) {
            ajy = true;
            ++this.ajr;
        }
    };
    @dm
    public final dk<eo> ajG = eo2 -> {
        if (!((akd)this.e(tn.class).afO.wp()).getName().equals("Auto Jump") && vk.aEi.bgb.aO && this.aju && ajw && !a.a.g().c(mh.class).isEnabled() && !vk.aEi.aIp.cgI.QQ()) {
            ajw = false;
        }
    };

    public vk(String string, tn tn2) {
        super(string, tn2);
        this.ajC = new vo(tn2);
    }

    @Override
    public void onEnable() {
        ajy = false;
        ajz = false;
        if (!((akd)this.e(tn.class).afO.wp()).getName().equals("Auto Jump")) {
            if (vk.aEi.bgb.cqP > 9) {
                ajw = true;
                this.aju = true;
            }
        } else {
            vk.aEi.bgb.crh = false;
        }
        ajx = true;
        this.gD = false;
        if (!vk.aEi.bgb.aO && !vk.aEi.aIp.cgI.QQ()) {
            vk.aEi.bgb.ud *= 0.0;
            vk.aEi.bgb.uf *= 0.0;
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
        vk.aEi.bgb.crh = false;
        ajw = false;
        this.aju = false;
        if (vk.aEi.bgb.Zl < 20) {
            // empty if block
        }
        boolean bl3 = bl2 = Math.abs((f3 = bt.bN(f2 = vk.aEi.bgb.pl)) % 90.0f) <= 10.0f || Math.abs(f3 % 90.0f) >= 80.0f;
        if (bl2) {
            if (vk.aEi.bgb.cqP > 2 && vk.aEi.bgb.aQx % 3 == 1) {
                vk.aEi.bfT.dzH = 0.5f;
                ahj.l(new e(vk.aEi.bgb.aO));
            } else if (vk.aEi.bgb.cqP > 2 && vk.aEi.bgb.aQx % 3 == 2) {
                vk.aEi.bfT.dzH = 0.33f;
                ahj.l(new e(vk.aEi.bgb.aO));
                ahj.l(new e(vk.aEi.bgb.aO));
            } else if (vk.aEi.bgb.cqP > 2) {
                vk.aEi.bfT.dzH = 0.5f;
                ahj.l(new e(vk.aEi.bgb.aO));
            }
        } else if (vk.aEi.bgb.cqP > 2) {
            vk.aEi.bfT.dzH = 0.33f;
            ahj.l(new e(vk.aEi.bgb.aO));
            ahj.l(new e(vk.aEi.bgb.aO));
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



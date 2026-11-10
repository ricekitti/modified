package net.minecraft.entity;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import hackclient.rise.adz;
import hackclient.rise.aka;
import hackclient.rise.ef;
import hackclient.rise.eh;
import hackclient.rise.el;
import hackclient.rise.fc;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.Block$SoundType;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern$PatternHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.command.ab;
import net.minecraft.command.ad;
import net.minecraft.command.aw;
import net.minecraft.enchantment.l;
import net.minecraft.enchantment.u;
import net.minecraft.entity.d;
import net.minecraft.entity.effect.a;
import net.minecraft.entity.f;
import net.minecraft.entity.g;
import net.minecraft.entity.item.i;
import net.minecraft.entity.n;
import net.minecraft.entity.player.e;
import net.minecraft.entity.r;
import net.minecraft.init.Blocks;
import net.minecraft.item.co;
import net.minecraft.nbt.q;
import net.minecraft.nbt.s;
import net.minecraft.nbt.v;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ao;
import net.minecraft.util.ap;
import net.minecraft.util.aq;
import net.minecraft.util.as;
import net.minecraft.util.b;
import net.minecraft.util.ba;
import net.minecraft.util.bt;
import net.minecraft.util.ce;
import net.minecraft.util.cl;
import net.minecraft.util.cr;
import net.minecraft.util.da;
import net.minecraft.util.db;
import net.minecraft.world.ai;
import net.minecraft.world.h;
import net.minecraft.world.w;
import rise.Client;
import rise.event.impl.StrafeEvent;
import rise.event.impl.WaterEvent;
import rise.util.InstanceAccess;

public abstract class c
implements aw {
    private final static net.minecraft.util.a cqM = new net.minecraft.util.a(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    private static int cqN;
    private int entityId;
    public int tR;
    public int cqP;
    public double cqQ = 1.0;
    public boolean cqR;
    public transient c cqS;
    public boolean cqT;
    public transient c riding;
    public boolean cqV;
    public List<aka> cqW = new adz<aka>(100);
    public transient w burning;
    public double cqX;
    public double cqY;
    public double cqZ;
    public transient adz<Double> cra = new adz(20);
    public double distance;
    public double gX;
    public double gY;
    public double aH;
    public double aI;
    public double aJ;
    public double crb;
    public double crc;
    public double crd;
    public double ud;
    public double He;
    public double ue;
    public double Hf;
    public double uf;
    public double Hg;
    public float pl;
    public float pm;
    public float cre;
    public float crf;
    public float crg;
    public boolean crh = false;
    public transient net.minecraft.util.a jJ;
    public boolean aO;
    public boolean im;
    public boolean fX = true;
    public boolean cri = true;
    public boolean crj;
    public boolean crk = false;
    public boolean crl;
    public int crm;
    final public rip.vantage.commons.util.time.a crn = new rip.vantage.commons.util.time.a();
    public boolean cro;
    public boolean crp;
    public boolean crq;
    public boolean crr;
    private boolean crs;
    public boolean crt;
    public float width = 0.6f;
    public float eyeHeight = 1.8f;
    public float cru;
    public float crv;
    public float crw;
    public float crx;
    private int cry = 1;
    public double crz;
    public double crA;
    public double crB;
    public double crC;
    public double crD;
    public double crE;
    public double crF;
    public float crG;
    public boolean crH;
    public float crI;
    protected transient Random ta;
    public int aQx;
    public int crJ;
    public int ae;
    public int crK;
    public int crL;
    public int aY;
    public double crM;
    public double crN;
    public double crO;
    public int Zl;
    public int crP = 1;
    private int crQ;
    public boolean crR;
    public int crS;
    protected boolean crT = true;
    protected boolean crU;
    protected transient net.minecraft.entity.a crV;
    private double crW;
    private double crX;
    public boolean crY;
    public int crZ;
    public int csa;
    public int csb;
    public int ao;
    public int ap;
    public int aq;
    public boolean csc;
    public boolean csd;
    public int cse;
    protected boolean csf;
    protected int csg;
    public int csh;
    public int csi;
    protected transient b csj;
    protected transient da csk;
    protected transient ao csl;
    private boolean csm;
    protected transient UUID csn;
    public float pp;
    public float pq;
    public float pr;
    public boolean adr = false;
    public int cso;
    private final transient ab csp;
    public Render<?> csq;
    private Optional<b> csr = Optional.empty();
    private boolean css;
    public rip.vantage.commons.util.time.a aie = new rip.vantage.commons.util.time.a();
    public rip.vantage.commons.util.time.a cst = new rip.vantage.commons.util.time.a();
    rip.vantage.commons.util.time.a bN = new rip.vantage.commons.util.time.a();
    public Double csu;
    public Double csv;

    protected boolean l(da da2) {
        return false;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public void eu(int n2) {
        this.entityId = n2;
    }

    public void Ta() {
        this.Tf();
    }

    public c(w w2) {
        this.entityId = cqN++;
        this.jJ = cqM;
        this.ta = new Random();
        this.csn = bt.h(this.ta);
        this.csp = new ab();
        this.burning = w2;
        this.setPosition(0.0, 0.0, 0.0);
        if (w2 != null) {
            this.csh = w2.dCD.getDimensionId();
        }
        this.crV = new net.minecraft.entity.a(this);
        this.crV.a(0, 0);
        this.crV.a(1, 300);
        this.crV.a(3, 0);
        this.crV.a(2, "");
        this.crV.a(4, 0);
        this.Im();
    }

    protected abstract void Im();

    public net.minecraft.entity.a Tb() {
        return this.crV;
    }

    public boolean equals(Object object) {
        return object instanceof c ? ((c)object).entityId == this.entityId : false;
    }

    public void Tc() {
        this.aie.aX();
        this.fX = false;
    }

    public void Td() {
        this.cst.aX();
        this.cri = false;
    }

    public int hashCode() {
        return this.entityId;
    }

    protected void Te() {
        if (this.burning != null) {
            while (this.gX > 0.0 && this.gX < 256.0) {
                this.setPosition(this.distance, this.gX, this.gY);
                if (this.burning.a(this, this.Ue()).isEmpty()) break;
                this.gX += 1.0;
            }
            this.uf = 0.0;
            this.ue = 0.0;
            this.ud = 0.0;
            this.pm = 0.0f;
        }
    }

    public void Tf() {
        this.crt = true;
    }

    public void o(float f2, float f3) {
        if (f2 != this.width || f3 != this.eyeHeight) {
            float f4 = this.width;
            this.width = f2;
            this.eyeHeight = f3;
            this.n(new net.minecraft.util.a(this.Ue().dsZ, this.Ue().dta, this.Ue().dtb, this.Ue().dsZ + (double)this.width, this.Ue().dta + (double)this.eyeHeight, this.Ue().dtb + (double)this.width));
            if (this.width > f4 && !this.crT && !this.burning.dCO) {
                this.y(f4 - this.width, 0.0, f4 - this.width);
            }
        }
    }

    protected void setRotation(float f2, float f3) {
        this.pl = f2 % 360.0f;
        this.pm = f3 % 360.0f;
    }

    public void setPosition(double d2, double d3, double d4) {
        this.distance = d2;
        this.gX = d3;
        this.gY = d4;
        float f2 = this.width / 2.0f;
        float f3 = this.eyeHeight;
        this.n(new net.minecraft.util.a(d2 - (double)f2, d3, d4 - (double)f2, d2 + (double)f2, d3 + (double)f3, d4 + (double)f2));
    }

    public void p(float f2, float f3) {
        float f4 = this.pm;
        float f5 = this.pl;
        this.pl = (float)((double)this.pl + (double)f2 * 0.15);
        this.pm = (float)((double)this.pm - (double)f3 * 0.15);
        this.pm = bt.I(this.pm, -90.0f, 90.0f);
        this.crg += this.pm - f4;
        this.cre += this.pl - f5;
    }

    public void CY() {
        if (this.isInvisible() || !this.crl) {
            this.crJ = 0;
        }
        if (this.aO) {
            this.tR = 0;
            ++this.cqP;
        } else {
            this.cqP = 0;
            ++this.tR;
        }
        da da2 = new da(this.crz, this.crA, this.crB);
        if (this.RP().x(da2) > 0.1) {
            this.adr = true;
        }
        this.csi = this.isSprinting() ? ++this.csi : 0;
        this.Tg();
    }

    public void Tg() {
        this.cqW.add(new aka(this.distance, this.gX, this.gY));
        this.burning.dCL.eY("entityBaseTick");
        if (this.riding != null && this.riding.crt) {
            this.riding = null;
        }
        if (this.aQx > 10 && (Math.abs(this.cqX - this.distance) > 0.2 || Math.abs(this.cqY - this.gX) > 0.2 || Math.abs(this.cqZ - this.gY) > 0.2)) {
            this.adr = true;
        }
        this.cru = this.crv;
        this.cqX = this.distance;
        this.cqY = this.gX;
        this.cqZ = this.gY;
        this.crg = this.pm;
        this.crf = this.cre;
        this.cre = this.pl;
        if (!this.burning.dCO && this.burning instanceof ai) {
            this.burning.dCL.eY("portal");
            MinecraftServer minecraftServer = ((ai)this.burning).awf();
            int n2 = this.Th();
            if (this.csf) {
                if (minecraftServer.akN()) {
                    if (this.riding == null && this.csg++ >= n2) {
                        this.csg = n2;
                        this.cse = this.TL();
                        int n3 = this.burning.dCD.getDimensionId() == -1 ? 0 : -1;
                        this.ez(n3);
                    }
                    this.csf = false;
                }
            } else {
                if (this.csg > 0) {
                    this.csg -= 4;
                }
                if (this.csg < 0) {
                    this.csg = 0;
                }
            }
            if (this.cse > 0) {
                --this.cse;
            }
            this.burning.dCL.ajh();
        }
        this.Tv();
        this.csu = null;
        this.csv = null;
        this.Tt();
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_13)) {
            this.burning.a(this.Ue().af(0.001, 0.001, 0.001), Material.bdN, this, this.burning.dCD.avQ() ? 0.007 : 0.0023333333333333335);
        }
        if (this.burning.dCO) {
            this.crQ = 0;
        } else if (this.crQ > 0) {
            if (this.crU) {
                this.crQ -= 4;
                if (this.crQ < 0) {
                    this.crQ = 0;
                }
            } else {
                if (this.crQ % 20 == 0) {
                    this.a(net.minecraft.util.ad.duv, 1.0f);
                }
                --this.crQ;
            }
        }
        if (this.isInLava()) {
            this.Ti();
            this.crx *= 0.5f;
        }
        if (this.gX < -64.0) {
            this.Tk();
        }
        if (!this.burning.dCO) {
            this.l(0, this.crQ > 0);
        }
        this.crT = false;
        this.burning.dCL.ajh();
    }

    public int Th() {
        return 0;
    }

    protected void Ti() {
        if (!this.crU) {
            this.a(net.minecraft.util.ad.duw, 4.0f);
            this.ev(15);
        }
    }

    public void ev(int n2) {
        int n3 = n2 * 20;
        if (this.crQ < (n3 = u.a(this, n3))) {
            this.crQ = n3;
        }
    }

    public void Tj() {
        this.crQ = 0;
    }

    protected void Tk() {
        this.Tf();
    }

    public boolean L(double d2, double d3, double d4) {
        net.minecraft.util.a a2 = this.Ue().ae(d2, d3, d4);
        return this.l(a2);
    }

    private boolean l(net.minecraft.util.a a2) {
        return this.burning.a(this, a2).isEmpty() && !this.burning.u(a2);
    }

    public b Tl() {
        return this.aP(0.2f);
    }

    public b Tm() {
        return this.aP(0.500001f);
    }

    protected b aP(float f2) {
        if (ViaLoadingBase.getInstance().getTargetVersion().olderThan(ProtocolVersion.v1_20) || this.csr.isEmpty()) {
            return new b(this.distance, this.gX - (double)f2, this.gY);
        }
        b b2 = this.csr.get();
        if (f2 <= 1.0E-5f) {
            return b2;
        }
        Block block = this.burning.H(b2).df();
        return f2 <= 0.5f && block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockFenceGate ? b2 : new b(b2.wf(), bt.aF(this.gX - (double)f2), b2.wj());
    }

    private Optional<b> m(net.minecraft.util.a a2) {
        db db2 = null;
        double d2 = Double.MAX_VALUE;
        for (int i2 = bt.aF(a2.dsZ - 1.0E-7) - 1; i2 < bt.aF(a2.dtc + 1.0E-7) + 1; ++i2) {
            for (int i3 = bt.aF(a2.dta - 1.0E-7) - 1; i3 < bt.aF(a2.dtd + 1.0E-7) + 1; ++i3) {
                for (int i4 = bt.aF(a2.dtb - 1.0E-7) - 1; i4 < bt.aF(a2.dte + 1.0E-7) + 1; ++i4) {
                    double d3;
                    b b2 = new b(i2, i3, i4);
                    IBlockState iBlockState = this.burning.H(b2);
                    net.minecraft.util.a a3 = iBlockState.df().a(this.burning, b2, iBlockState);
                    if (a3 == null || !((d3 = b2.ao(this.distance, this.gX, this.gY)) < d2) && (d3 != d2 || db2 != null && db2.h(b2) >= 0)) continue;
                    db2 = new net.minecraft.util.g(b2.wf(), b2.wg(), b2.wj());
                    d2 = d3;
                }
            }
        }
        return Optional.ofNullable(db2);
    }

    protected void a(boolean bl2, da da2) {
        if (!bl2) {
            this.css = false;
            if (this.csr.isPresent()) {
                this.csr = Optional.empty();
            }
            return;
        }
        net.minecraft.util.a a2 = new net.minecraft.util.a(this.jJ.dsZ, this.jJ.dta - 1.0E-6, this.jJ.dtb, this.jJ.dtc, this.jJ.dta, this.jJ.dte);
        Optional<b> optional = this.m(a2);
        if (optional.isEmpty() && !this.css) {
            if (da2 != null) {
                optional = this.m(a2.ae(-da2.dzY, 0.0, -da2.dAa));
                this.csr = optional;
            }
        } else {
            this.csr = optional;
        }
        this.css = optional.isEmpty();
    }

    public void y(double d2, double d3, double d4) {
        ef ef2 = new ef(new da(this.ud, this.ue, this.uf));
        Client.INSTANCE.e().d(ef2);
        if (!ef2.isCancelled()) {
            da da2 = ef2.cZ();
            this.ud = da2.dzY;
            this.ue = da2.dzZ;
            this.uf = da2.dAa;
        }
        if (this.crH) {
            this.n(this.Ue().ae(d2, d3, d4));
            this.Tn();
        } else {
            double d5;
            double d6;
            boolean bl2;
            boolean bl3;
            this.burning.dCL.eY("move");
            double d7 = this.distance;
            double d8 = this.gX;
            double d9 = this.gY;
            if (this.crr) {
                this.crr = false;
                d2 *= 0.25;
                d3 *= (double)0.05f;
                d4 *= 0.25;
                this.ud = 0.0;
                this.ue = 0.0;
                this.uf = 0.0;
            }
            double d10 = d2;
            double d11 = d3;
            double d12 = d4;
            el el2 = new el(-1.0);
            Client.INSTANCE.e().d(el2);
            double d13 = el2.da();
            boolean bl4 = bl3 = this.aO && this.isSneaking() && this instanceof net.minecraft.entity.player.a;
            if (bl3 || this.crh) {
                double d14 = 0.05;
                while (d2 != 0.0 && this.burning.a(this, this.Ue().ae(d2, d13, 0.0)).isEmpty()) {
                    d2 = d2 < d14 && d2 >= -d14 ? 0.0 : (d2 > 0.0 ? (d2 -= d14) : (d2 += d14));
                    d10 = d2;
                }
                while (d4 != 0.0 && this.burning.a(this, this.Ue().ae(0.0, d13, d4)).isEmpty()) {
                    d4 = d4 < d14 && d4 >= -d14 ? 0.0 : (d4 > 0.0 ? (d4 -= d14) : (d4 += d14));
                    d12 = d4;
                }
                while (d2 != 0.0 && d4 != 0.0 && this.burning.a(this, this.Ue().ae(d2, d13, d4)).isEmpty()) {
                    d2 = d2 < d14 && d2 >= -d14 ? 0.0 : (d2 > 0.0 ? (d2 -= d14) : (d2 += d14));
                    d10 = d2;
                    d4 = d4 < d14 && d4 >= -d14 ? 0.0 : (d4 > 0.0 ? (d4 -= d14) : (d4 += d14));
                    d12 = d4;
                }
            }
            this.crh = false;
            List<net.minecraft.util.a> list = this.burning.a(this, this.Ue().ac(d2, d3, d4));
            net.minecraft.util.a a2 = this.Ue();
            for (net.minecraft.util.a iterator : list) {
                d3 = iterator.b(this.Ue(), d3);
            }
            this.n(this.Ue().ae(0.0, d3, 0.0));
            boolean bl5 = bl2 = this.aO || d11 != d3 && d11 < 0.0;
            if (ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_13_2) && Math.abs(d2) < Math.abs(d4)) {
                for (net.minecraft.util.a a3 : list) {
                    d4 = a3.c(this.Ue(), d4);
                }
                this.n(this.Ue().ae(0.0, 0.0, d4));
                for (net.minecraft.util.a a4 : list) {
                    d2 = a4.a(this.Ue(), d2);
                }
                this.n(this.Ue().ae(d2, 0.0, 0.0));
            } else {
                for (net.minecraft.util.a a5 : list) {
                    d2 = a5.a(this.Ue(), d2);
                }
                this.n(this.Ue().ae(d2, 0.0, 0.0));
                for (net.minecraft.util.a a6 : list) {
                    d4 = a6.c(this.Ue(), d4);
                }
                this.n(this.Ue().ae(0.0, 0.0, d4));
            }
            if (this.crG > 0.0f && bl2 && (d10 != d2 || d12 != d4)) {
                double d14 = d2;
                d6 = d3;
                d5 = d4;
                net.minecraft.util.a a7 = this.Ue();
                this.n(a2);
                d3 = this.crG;
                List<net.minecraft.util.a> list2 = this.burning.a(this, this.Ue().ac(d10, d3, d12));
                net.minecraft.util.a a8 = this.Ue();
                net.minecraft.util.a a9 = a8.ac(d10, 0.0, d12);
                double d15 = d3;
                for (net.minecraft.util.a a10 : list2) {
                    d15 = a10.b(a9, d15);
                }
                a8 = a8.ae(0.0, d15, 0.0);
                double d16 = d10;
                double d17 = d12;
                if (ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_13_2) && Math.abs(d16) < Math.abs(d17)) {
                    for (net.minecraft.util.a a11 : list2) {
                        d17 = a11.c(a8, d17);
                    }
                    a8 = a8.ae(0.0, 0.0, d17);
                    for (net.minecraft.util.a a11 : list2) {
                        d16 = a11.a(a8, d16);
                    }
                    a8 = a8.ae(d16, 0.0, 0.0);
                } else {
                    for (net.minecraft.util.a a11 : list2) {
                        d16 = a11.a(a8, d16);
                    }
                    a8 = a8.ae(d16, 0.0, 0.0);
                    for (net.minecraft.util.a a11 : list2) {
                        d17 = a11.c(a8, d17);
                    }
                    a8 = a8.ae(0.0, 0.0, d17);
                }
                Object object = this.Ue();
                double d18 = d3;
                for (net.minecraft.util.a a12 : list2) {
                    d18 = a12.b((net.minecraft.util.a)object, d18);
                }
                object = ((net.minecraft.util.a)object).ae(0.0, d18, 0.0);
                double d19 = d10;
                double d20 = d12;
                if (ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_13_2) && Math.abs(d19) < Math.abs(d20)) {
                    for (net.minecraft.util.a a13 : list2) {
                        d20 = a13.c((net.minecraft.util.a)object, d20);
                    }
                    object = ((net.minecraft.util.a)object).ae(0.0, 0.0, d20);
                    for (net.minecraft.util.a a13 : list2) {
                        d19 = a13.a((net.minecraft.util.a)object, d19);
                    }
                    object = ((net.minecraft.util.a)object).ae(d19, 0.0, 0.0);
                } else {
                    for (net.minecraft.util.a a13 : list2) {
                        d19 = a13.a((net.minecraft.util.a)object, d19);
                    }
                    object = ((net.minecraft.util.a)object).ae(d19, 0.0, 0.0);
                    for (net.minecraft.util.a a13 : list2) {
                        d20 = a13.c((net.minecraft.util.a)object, d20);
                    }
                    object = ((net.minecraft.util.a)object).ae(0.0, 0.0, d20);
                }
                double d21 = d16 * d16 + d17 * d17;
                double d22 = d19 * d19 + d20 * d20;
                if (d21 > d22) {
                    d2 = d16;
                    d4 = d17;
                    d3 = ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_13_2) ? -d15 + d3 : -d15;
                    this.n(a8);
                } else {
                    d2 = d19;
                    d4 = d20;
                    d3 = -d18;
                    this.n((net.minecraft.util.a)object);
                }
                for (net.minecraft.util.a a14 : list2) {
                    d3 = a14.b(this.Ue(), d3);
                }
                this.n(this.Ue().ae(0.0, d3, 0.0));
                if (d14 * d14 + d5 * d5 >= d2 * d2 + d4 * d4) {
                    d2 = d14;
                    d3 = d6;
                    d4 = d5;
                    this.n(a7);
                }
                if (this instanceof net.minecraft.client.entity.c) {
                    this.cso = 0;
                    Client.INSTANCE.e().d(new fc(this.Ue().dta - this.gX));
                }
            }
            ++this.cso;
            this.burning.dCL.ajh();
            this.burning.dCL.eY("rest");
            this.Tn();
            this.crj = d10 != d2 || d12 != d4;
            this.cro = d11 != d3;
            this.aO = this.cro && d11 < 0.0;
            this.a(this.aO, new da(d2, d3, d4));
            boolean bl6 = this.cqT = this.crj && this.l(new da(d2, d3, d4));
            if (this.aO) {
                this.crb = this.aH;
                this.crc = this.aI;
                this.crd = this.aJ;
                this.aH = this.distance;
                this.aI = this.gX;
                this.aJ = this.gY;
            }
            this.crp = this.crj || this.cro;
            b b2 = this.Tl();
            Block block = this.burning.H(b2).df();
            this.a(d3, this.aO, block, b2);
            if (d10 != d2) {
                this.ud = 0.0;
            }
            if (d12 != d4) {
                this.uf = 0.0;
            }
            if (d11 != d3) {
                block.a(this.burning, this);
            }
            if (this.Il() && !bl3 && this.riding == null) {
                d6 = this.distance - d7;
                d5 = this.gX - d8;
                double d23 = this.gY - d9;
                if (block != Blocks.cLO) {
                    d5 = 0.0;
                }
                if (block != null && this.aO) {
                    block.a(this.burning, b2, this);
                }
                this.crv = (float)((double)this.crv + (double)bt.aD(d6 * d6 + d23 * d23) * 0.6);
                this.crw = (float)((double)this.crw + (double)bt.aD(d6 * d6 + d5 * d5 + d23 * d23) * 0.6);
                if (this.crw > (float)this.cry && block.wN() != Material.bdF) {
                    this.cry = (int)this.crw + 1;
                    if (this.isInWater()) {
                        float f2 = bt.aD(this.ud * this.ud * (double)0.2f + this.ue * this.ue + this.uf * this.uf * (double)0.2f) * 0.35f;
                        if (f2 > 1.0f) {
                            f2 = 1.0f;
                        }
                        this.a(this.To(), f2, 1.0f + (this.ta.nextFloat() - this.ta.nextFloat()) * 0.4f);
                    }
                    this.a(b2, block);
                }
            }
            try {
                this.Tp();
            }
            catch (Throwable throwable) {
                net.minecraft.crash.a a15 = net.minecraft.crash.a.a(throwable, "Checking entity block collision");
                net.minecraft.crash.i i2 = a15.dY("Entity being checked for collision");
                this.a(i2);
                throw new cl(a15);
            }
            boolean bl7 = this.Ts();
            if (this.burning.v(this.Ue().af(0.001, 0.001, 0.001))) {
                this.ew(1);
                if (!bl7) {
                    ++this.crQ;
                    if (this.crQ == 0) {
                        this.ev(8);
                    }
                }
            } else if (this.crQ <= 0) {
                this.crQ = -this.crP;
            }
            if (bl7 && this.crQ > 0) {
                this.a("random.fizz", 0.7f, 1.6f + (this.ta.nextFloat() - this.ta.nextFloat()) * 0.4f);
                this.crQ = -this.crP;
            }
            this.burning.dCL.ajh();
        }
    }

    private void Tn() {
        this.distance = (this.Ue().dsZ + this.Ue().dtc) / 2.0;
        this.gX = this.Ue().dta;
        this.gY = (this.Ue().dtb + this.Ue().dte) / 2.0;
    }

    protected String To() {
        return "game.neutral.swim";
    }

    protected void Tp() {
        b b2 = new b(this.Ue().dsZ + 0.001, this.Ue().dta + 0.001, this.Ue().dtb + 0.001);
        b b3 = new b(this.Ue().dtc - 0.001, this.Ue().dtd - 0.001, this.Ue().dte - 0.001);
        if (this.burning.e(b2, b3)) {
            for (int i2 = b2.wf(); i2 <= b3.wf(); ++i2) {
                for (int i3 = b2.wg(); i3 <= b3.wg(); ++i3) {
                    for (int i4 = b2.wj(); i4 <= b3.wj(); ++i4) {
                        b b4 = new b(i2, i3, i4);
                        IBlockState iBlockState = this.burning.H(b4);
                        try {
                            iBlockState.df().a(this.burning, b4, iBlockState, this);
                            continue;
                        }
                        catch (Throwable throwable) {
                            net.minecraft.crash.a a2 = net.minecraft.crash.a.a(throwable, "Colliding entity with block");
                            net.minecraft.crash.i i5 = a2.dY("Block being collided with");
                            net.minecraft.crash.i.a(i5, b4, iBlockState);
                            throw new cl(a2);
                        }
                    }
                }
            }
        }
    }

    protected void a(b b2, Block block) {
        Block$SoundType block$SoundType = block.aRS;
        if (this.burning.H(b2.aqJ()).df() == Blocks.cMb) {
            block$SoundType = Blocks.cMb.aRS;
            this.a(block$SoundType.xD(), block$SoundType.getVolume() * 0.15f, block$SoundType.xF());
        } else if (!block.wN().zh()) {
            this.a(block$SoundType.xD(), block$SoundType.getVolume() * 0.15f, block$SoundType.xF());
        }
    }

    public void a(String string, float f2, float f3) {
        if (!this.Tq()) {
            this.burning.a(this, string, f2, f3);
        }
    }

    public boolean Tq() {
        return this.crV.el(4) == 1;
    }

    public void aD(boolean bl2) {
        this.crV.b(4, (byte)(bl2 ? 1 : 0));
    }

    protected boolean Il() {
        return true;
    }

    protected void a(double d2, boolean bl2, Block block, b b2) {
        if (bl2) {
            if (this.crx > 0.0f) {
                if (block != null) {
                    block.a(this.burning, b2, this, this.crx);
                } else {
                    this.q(this.crx, 1.0f);
                }
                this.crx = 0.0f;
            }
        } else if (d2 < 0.0) {
            this.crx = (float)((double)this.crx - d2);
        }
    }

    public net.minecraft.util.a Ip() {
        return null;
    }

    protected void ew(int n2) {
        if (!this.crU) {
            this.a(net.minecraft.util.ad.dut, (float)n2);
        }
    }

    public final boolean Tr() {
        return this.crU;
    }

    public void q(float f2, float f3) {
        if (this.cqS != null) {
            this.cqS.q(f2, f3);
        }
    }

    public boolean Ts() {
        return this.crR || this.burning.ba(new b(this.distance, this.gX, this.gY)) || this.burning.ba(new b(this.distance, this.gX + (double)this.eyeHeight, this.gY));
    }

    public boolean isInWater() {
        if (this == Minecraft.getMinecraft().thePlayer && this.bN.T(100L)) {
            WaterEvent waterEvent = new WaterEvent(this.crR);
            Client.INSTANCE.e().d(waterEvent);
            this.crR = waterEvent.isWater();
            this.bN.aX();
        }
        return this.crR;
    }

    public boolean Tt() {
        if (this.burning.a(this.Ue().ad(0.0, ViaLoadingBase.getInstance().getTargetVersion().olderThan(ProtocolVersion.v1_13) ? (double)-0.4f : 0.0, 0.0).af(0.001, 0.001, 0.001), Material.bdM, this, 0.014)) {
            if (!this.crR && !this.crT) {
                this.Tu();
            }
            this.crx = 0.0f;
            this.crR = true;
            this.crQ = 0;
        } else {
            this.crR = false;
        }
        return this.crR;
    }

    protected void Tu() {
        float f2;
        float f3;
        float f4 = bt.aD(this.ud * this.ud * (double)0.2f + this.ue * this.ue + this.uf * this.uf * (double)0.2f) * 0.2f;
        if (f4 > 1.0f) {
            f4 = 1.0f;
        }
        this.a(this.Tx(), f4, 1.0f + (this.ta.nextFloat() - this.ta.nextFloat()) * 0.4f);
        float f5 = bt.aF(this.Ue().dta);
        int n2 = 0;
        while ((float)n2 < 1.0f + this.width * 20.0f) {
            f3 = (this.ta.nextFloat() * 2.0f - 1.0f) * this.width;
            f2 = (this.ta.nextFloat() * 2.0f - 1.0f) * this.width;
            this.burning.a(as.dws, this.distance + (double)f3, (double)(f5 + 1.0f), this.gY + (double)f2, this.ud, this.ue - (double)(this.ta.nextFloat() * 0.2f), this.uf, new int[0]);
            ++n2;
        }
        n2 = 0;
        while ((float)n2 < 1.0f + this.width * 20.0f) {
            f3 = (this.ta.nextFloat() * 2.0f - 1.0f) * this.width;
            f2 = (this.ta.nextFloat() * 2.0f - 1.0f) * this.width;
            this.burning.a(as.dwt, this.distance + (double)f3, (double)(f5 + 1.0f), this.gY + (double)f2, this.ud, this.ue, this.uf, new int[0]);
            ++n2;
        }
    }

    public void Tv() {
        if (this.isSprinting() && !this.isInWater()) {
            this.Tw();
        }
    }

    protected void Tw() {
        int n2;
        int n3;
        int n4 = bt.aF(this.distance);
        b b2 = new b(n4, n3 = bt.aF(this.gX - (double)0.2f), n2 = bt.aF(this.gY));
        IBlockState iBlockState = this.burning.H(b2);
        Block block = iBlockState.df();
        if (block.wS() != -1) {
            this.burning.a(as.dwZ, this.distance + ((double)this.ta.nextFloat() - 0.5) * (double)this.width, this.Ue().dta + 0.1, this.gY + ((double)this.ta.nextFloat() - 0.5) * (double)this.width, -this.ud * 4.0, 1.5, -this.uf * 4.0, Block.a(iBlockState));
        }
    }

    protected String Tx() {
        return "game.neutral.swim.splash";
    }

    public boolean c(Material material) {
        double d2 = this.gX + (double)this.getEyeHeight();
        b b2 = new b(this.distance, d2, this.gY);
        IBlockState iBlockState = this.burning.H(b2);
        Block block = iBlockState.df();
        if (block.wN() == material) {
            float f2 = BlockLiquid.bb(iBlockState.df().c(iBlockState)) - 0.11111111f;
            float f3 = (float)(b2.wg() + 1) - f2;
            return d2 < (double)f3;
        }
        return false;
    }

    public boolean isInLava() {
        return ViaLoadingBase.getInstance().getTargetVersion().olderThan(ProtocolVersion.v1_13) ? this.burning.b(this.Ue().ad(-0.1f, -0.4f, -0.1f), Material.bdN) : !this.crT && this.csv != null && this.csv > 0.0;
    }

    public da a(double d2, double d3, float f2) {
        double d4 = d2 * d2 + d3 * d3;
        if (d4 < 1.0E-7) {
            return new da(0.0, 0.0, 0.0);
        }
        if (d4 > 1.0) {
            if ((d4 = Math.sqrt(d4)) < 1.0E-4) {
                d2 = 0.0;
                d3 = 0.0;
            } else {
                d2 /= d4;
                d3 /= d4;
            }
        }
        float f3 = bt.bx(this.pl * ((float)Math.PI / 180));
        float f4 = bt.bB(this.pl * ((float)Math.PI / 180));
        return new da((d2 *= (double)f2) * (double)f4 - (d3 *= (double)f2) * (double)f3, 0.0, d3 * (double)f4 + d2 * (double)f3);
    }

    public void a(double d2, double d3, float f2, float f3) {
        double d4 = d2 * d2 + d3 * d3;
        if (d4 < 1.0E-7) {
            return;
        }
        if (d4 > 1.0) {
            if ((d4 = Math.sqrt(d4)) < 1.0E-4) {
                d2 = 0.0;
                d3 = 0.0;
            } else {
                d2 /= d4;
                d3 /= d4;
            }
        }
        float f4 = bt.bx(f3 * ((float)Math.PI / 180));
        float f5 = bt.bB(f3 * ((float)Math.PI / 180));
        this.ud += (d2 *= (double)f2) * (double)f5 - (d3 *= (double)f2) * (double)f4;
        this.uf += d3 * (double)f5 + d2 * (double)f4;
    }

    public void l(float f2, float f3, float f4, float f5) {
        float f6 = f2 * f2 + f3 * f3;
        if (f6 < 1.0E-4f) {
            return;
        }
        if ((f6 = bt.bK(f6)) < 1.0f) {
            f6 = 1.0f;
        }
        f6 = f4 / f6;
        float f7 = bt.bx(f5 * ((float)Math.PI / 180));
        float f8 = bt.bB(f5 * ((float)Math.PI / 180));
        this.ud += (double)((f2 *= f6) * f8 - (f3 *= f6) * f7);
        this.uf += (double)(f3 * f8 + f2 * f7);
    }

    public void s(float f2, float f3, float f4) {
        boolean bl2 = this == Minecraft.getMinecraft().thePlayer;
        boolean bl3 = ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_14);
        float f5 = this.pl;
        if (bl2) {
            if (bl3 && !this.aO && ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_19_4) && !this.isInWater() && !this.isInLava()) {
                f4 = InstanceAccess.mc.thePlayer.cHL.cJc ? (this.isSprinting() ? 0.1f : 0.05f) : (this.isSprinting() ? 0.025999999f : 0.02f);
            }
            StrafeEvent strafeEvent = new StrafeEvent(f3, f2, f4, this.pp);
            Client.INSTANCE.e().d(strafeEvent);
            f2 = strafeEvent.getStrafe();
            f3 = strafeEvent.getForward();
            f4 = strafeEvent.getFriction();
            f5 = strafeEvent.getYaw();
            if (strafeEvent.isCancelled()) {
                return;
            }
        }
        if (bl2 && bl3) {
            this.a((double)f2, f3, f4, f5);
        } else {
            this.l(f2, f3, f4, f5);
        }
        if (bl2) {
            Client.INSTANCE.e().d(new eh());
        }
    }

    public double[] t(float f2, float f3, float f4) {
        float f5 = this.pl;
        float f6 = f2 * f2 + f3 * f3;
        if (f6 >= 1.0E-4f) {
            if ((f6 = bt.bK(f6)) < 1.0f) {
                f6 = 1.0f;
            }
            f6 = f4 / f6;
            float f7 = bt.bx(f5 * (float)Math.PI / 180.0f);
            float f8 = bt.bB(f5 * (float)Math.PI / 180.0f);
            return new double[]{(f2 *= f6) * f8 - (f3 *= f6) * f7, f3 * f8 + f2 * f7};
        }
        return new double[]{0.0, 0.0};
    }

    public int af(float f2) {
        b b2 = new b(this.distance, this.gX + (double)this.getEyeHeight(), this.gY);
        return this.burning.aF(b2) ? this.burning.b(b2, 0) : 0;
    }

    public float ag(float f2) {
        b b2 = new b(this.distance, this.gX + (double)this.getEyeHeight(), this.gY);
        return this.burning.aF(b2) ? this.burning.aN(b2) : 0.0f;
    }

    public void b(w w2) {
        this.burning = w2;
    }

    public void a(double d2, double d3, double d4, float f2, float f3) {
        this.cqX = this.distance = d2;
        this.cqY = this.gX = d3;
        this.cqZ = this.gY = d4;
        this.cre = this.pl = f2;
        this.crg = this.pm = f3;
        double d5 = this.cre - f2;
        if (d5 < -180.0) {
            this.cre += 360.0f;
        }
        if (d5 >= 180.0) {
            this.cre -= 360.0f;
        }
        this.setPosition(this.distance, this.gX, this.gY);
        this.setRotation(f2, f3);
    }

    public void a(b b2, float f2, float f3) {
        this.b((double)b2.wf() + 0.5, (double)b2.wg(), (double)b2.wj() + 0.5, f2, f3);
    }

    public void b(double d2, double d3, double d4, float f2, float f3) {
        this.cqX = this.distance = d2;
        this.crz = this.distance;
        this.cqY = this.gX = d3;
        this.crA = this.gX;
        this.cqZ = this.gY = d4;
        this.crB = this.gY;
        this.pl = f2;
        this.pm = f3;
        this.setPosition(this.distance, this.gX, this.gY);
    }

    public float S(c c2) {
        float f2 = (float)(this.distance - c2.distance);
        float f3 = (float)(this.gX - c2.gX);
        float f4 = (float)(this.gY - c2.gY);
        return bt.bK(f2 * f2 + f3 * f3 + f4 * f4);
    }

    public double M(double d2, double d3, double d4) {
        double d5 = this.distance - d2;
        double d6 = this.gX - d3;
        double d7 = this.gY - d4;
        return d5 * d5 + d6 * d6 + d7 * d7;
    }

    public double W(b b2) {
        return b2.am(this.distance, this.gX, this.gY);
    }

    public double X(b b2) {
        return b2.ao(this.distance, this.gX, this.gY);
    }

    public double getDistance(double d2, double d3, double d4) {
        double d5 = this.distance - d2;
        double d6 = this.gX - d3;
        double d7 = this.gY - d4;
        return bt.aD(d5 * d5 + d6 * d6 + d7 * d7);
    }

    public double T(c c2) {
        double d2 = this.distance - c2.distance;
        double d3 = this.gX - c2.gX;
        double d4 = this.gY - c2.gY;
        return d2 * d2 + d3 * d3 + d4 * d4;
    }

    public void n(net.minecraft.entity.player.a a2) {
    }

    public void U(c c2) {
        double d2;
        double d3;
        double d4;
        if (c2.cqS != this && c2.riding != this && !c2.crH && !this.crH && (d4 = bt.x(d3 = c2.distance - this.distance, d2 = c2.gY - this.gY)) >= (double)0.01f) {
            d4 = bt.aD(d4);
            d3 /= d4;
            d2 /= d4;
            double d5 = 1.0 / d4;
            if (d5 > 1.0) {
                d5 = 1.0;
            }
            d3 *= d5;
            d2 *= d5;
            d3 *= (double)0.05f;
            d2 *= (double)0.05f;
            d3 *= (double)(1.0f - this.crI);
            d2 *= (double)(1.0f - this.crI);
            if (this.cqS == null) {
                this.N(-d3, 0.0, -d2);
            }
            if (c2.cqS == null) {
                c2.N(d3, 0.0, d2);
            }
        }
    }

    public void N(double d2, double d3, double d4) {
        this.ud += d2;
        this.ue += d3;
        this.uf += d4;
        this.csd = true;
    }

    protected void Ty() {
        this.crq = true;
    }

    public boolean a(net.minecraft.util.ad ad2, float f2) {
        if (this.a(ad2)) {
            return false;
        }
        this.Ty();
        return false;
    }

    public da O(float f2) {
        if (f2 == 1.0f) {
            return this.s(this.pm, this.pl);
        }
        float f3 = this.crg + (this.pm - this.crg) * f2;
        float f4 = this.cre + (this.pl - this.cre) * f2;
        return this.s(f3, f4);
    }

    public da r(float f2, float f3) {
        return this.s(f3, f2);
    }

    public final da s(float f2, float f3) {
        float f4 = bt.bB(-f3 * ((float)Math.PI / 180) - (float)Math.PI);
        float f5 = bt.bx(-f3 * ((float)Math.PI / 180) - (float)Math.PI);
        float f6 = -bt.bB(-f2 * ((float)Math.PI / 180));
        float f7 = bt.bx(-f2 * ((float)Math.PI / 180));
        return new da(f5 * f6, f7, f4 * f6);
    }

    public aka Tz() {
        return new aka(this.distance, this.gX, this.gY);
    }

    public da aQ(float f2) {
        return new da(this.distance, this.gX + (double)this.getEyeHeight(), this.gY);
    }

    public ce b(double d2, float f2) {
        da da2 = this.aQ(f2);
        da da3 = this.O(f2);
        da da4 = da2.al(da3.dzY * d2, da3.dzZ * d2, da3.dAa * d2);
        return this.burning.a(da2, da4, false, false, true);
    }

    public ce a(double d2, float f2, float f3) {
        da da2 = this.aQ(1.0f);
        da da3 = this.r(f2, f3);
        da da4 = da2.al(da3.dzY * d2, da3.dzZ * d2, da3.dAa * d2);
        return this.burning.a(da2, da4, false, false, true);
    }

    public boolean TA() {
        return false;
    }

    public boolean Iq() {
        return false;
    }

    public void b(c c2, int n2) {
    }

    public boolean O(double d2, double d3, double d4) {
        double d5 = this.distance - d2;
        double d6 = this.gX - d3;
        double d7 = this.gY - d4;
        double d8 = d5 * d5 + d6 * d6 + d7 * d7;
        return this.an(d8);
    }

    public boolean an(double d2) {
        double d3 = this.Ue().aqH();
        if (Double.isNaN(d3)) {
            d3 = 1.0;
        }
        return d2 < (d3 = d3 * 64.0 * this.cqQ) * d3;
    }

    public boolean j(net.minecraft.nbt.n n2) {
        String string = this.TC();
        if (!this.crt && string != null) {
            n2.R("id", string);
            this.b(n2);
            return true;
        }
        return false;
    }

    public boolean k(net.minecraft.nbt.n n2) {
        String string = this.TC();
        if (!this.crt && string != null && this.cqS == null) {
            n2.R("id", string);
            this.b(n2);
            return true;
        }
        return false;
    }

    public void b(net.minecraft.nbt.n n2) {
        try {
            net.minecraft.nbt.n n3;
            n2.b("Pos", this.b(this.distance, this.gX, this.gY));
            n2.b("Motion", this.b(this.ud, this.ue, this.uf));
            n2.b("Rotation", this.e(this.pl, this.pm));
            n2.a("FallDistance", this.crx);
            n2.a("Fire", (short)this.crQ);
            n2.a("Air", (short)this.TO());
            n2.o("OnGround", this.aO);
            n2.B("Dimension", this.csh);
            n2.o("Invulnerable", this.csm);
            n2.B("PortalCooldown", this.cse);
            n2.d("UUIDMost", this.TX().getMostSignificantBits());
            n2.d("UUIDLeast", this.TX().getLeastSignificantBits());
            if (this.TZ() != null && this.TZ().length() > 0) {
                n2.R("CustomName", this.TZ());
                n2.o("CustomNameVisible", this.Ua());
            }
            this.csp.g(n2);
            if (this.Tq()) {
                n2.o("Silent", this.Tq());
            }
            this.d(n2);
            if (this.riding != null && this.riding.j(n3 = new net.minecraft.nbt.n())) {
                n2.b("Riding", n3);
            }
        }
        catch (Throwable throwable) {
            net.minecraft.crash.a a2 = net.minecraft.crash.a.a(throwable, "Saving entity NBT");
            net.minecraft.crash.i i2 = a2.dY("Entity being saved");
            this.a(i2);
            throw new cl(a2);
        }
    }

    public void a(net.minecraft.nbt.n n2) {
        try {
            v v2 = n2.D("Pos", 6);
            v v3 = n2.D("Motion", 6);
            v v4 = n2.D("Rotation", 5);
            this.ud = v3.gk(0);
            this.ue = v3.gk(1);
            this.uf = v3.gk(2);
            if (Math.abs(this.ud) > 10.0) {
                this.ud = 0.0;
            }
            if (Math.abs(this.ue) > 10.0) {
                this.ue = 0.0;
            }
            if (Math.abs(this.uf) > 10.0) {
                this.uf = 0.0;
            }
            this.crz = this.distance = v2.gk(0);
            this.cqX = this.distance;
            this.crA = this.gX = v2.gk(1);
            this.cqY = this.gX;
            this.crB = this.gY = v2.gk(2);
            this.cqZ = this.gY;
            this.cre = this.pl = v4.gl(0);
            this.crg = this.pm = v4.gl(1);
            this.aR(this.pl);
            this.aS(this.pl);
            this.crx = n2.eI("FallDistance");
            this.crQ = n2.eF("Fire");
            this.ey(n2.eF("Air"));
            this.aO = n2.eO("OnGround");
            this.csh = n2.eG("Dimension");
            this.csm = n2.eO("Invulnerable");
            this.cse = n2.eG("PortalCooldown");
            if (n2.C("UUIDMost", 4) && n2.C("UUIDLeast", 4)) {
                this.csn = new UUID(n2.eH("UUIDMost"), n2.eH("UUIDLeast"));
            } else if (n2.C("UUID", 8)) {
                this.csn = UUID.fromString(n2.eK("UUID"));
            }
            this.setPosition(this.distance, this.gX, this.gY);
            this.setRotation(this.pl, this.pm);
            if (n2.C("CustomName", 8) && n2.eK("CustomName").length() > 0) {
                this.ec(n2.eK("CustomName"));
            }
            this.aF(n2.eO("CustomNameVisible"));
            this.csp.f(n2);
            this.aD(n2.eO("Silent"));
            this.e(n2);
            if (this.TB()) {
                this.setPosition(this.distance, this.gX, this.gY);
            }
        }
        catch (Throwable throwable) {
            net.minecraft.crash.a a2 = net.minecraft.crash.a.a(throwable, "Loading entity NBT");
            net.minecraft.crash.i i2 = a2.dY("Entity being loaded");
            this.a(i2);
            throw new cl(a2);
        }
    }

    protected boolean TB() {
        return true;
    }

    protected final String TC() {
        return n.ad(this);
    }

    protected abstract void e(net.minecraft.nbt.n var1);

    protected abstract void d(net.minecraft.nbt.n var1);

    public void TD() {
    }

    protected v b(double ... dArray) {
        v v2 = new v();
        for (double d2 : dArray) {
            v2.a(new q(d2));
        }
        return v2;
    }

    protected v e(float ... fArray) {
        v v2 = new v();
        for (float f2 : fArray) {
            v2.a(new s(f2));
        }
        return v2;
    }

    public i d(net.minecraft.item.d d2, int n2) {
        return this.a(d2, n2, 0.0f);
    }

    public i a(net.minecraft.item.d d2, int n2, float f2) {
        return this.a(new co(d2, n2, 0), f2);
    }

    public i a(co co2, float f2) {
        if (co2.cWr != 0 && co2.bP() != null) {
            i i2 = new i(this.burning, this.distance, this.gX + (double)f2, this.gY, co2);
            i2.XZ();
            this.burning.G(i2);
            return i2;
        }
        return null;
    }

    public boolean TE() {
        return !this.crt;
    }

    public boolean TF() {
        if (this.crH) {
            return false;
        }
        net.minecraft.util.g g2 = new net.minecraft.util.g(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        for (int i2 = 0; i2 < 8; ++i2) {
            int n2 = bt.aF(this.gX + (double)(((float)((i2 >> 0) % 2) - 0.5f) * 0.1f) + (double)this.getEyeHeight());
            int n3 = bt.aF(this.distance + (double)(((float)((i2 >> 1) % 2) - 0.5f) * this.width * 0.8f));
            int n4 = bt.aF(this.gY + (double)(((float)((i2 >> 2) % 2) - 0.5f) * this.width * 0.8f));
            if (g2.wf() == n3 && g2.wg() == n2 && g2.wj() == n4) continue;
            g2.E(n3, n2, n4);
            if (!this.burning.H(g2).df().wQ()) continue;
            return true;
        }
        return false;
    }

    public boolean o(net.minecraft.entity.player.a a2) {
        return false;
    }

    public net.minecraft.util.a V(c c2) {
        return null;
    }

    public void TG() {
        if (this.riding.crt) {
            this.riding = null;
        } else {
            this.ud = 0.0;
            this.ue = 0.0;
            this.uf = 0.0;
            this.CY();
            if (this.riding != null) {
                this.riding.TH();
                this.crX += (double)(this.riding.pl - this.riding.cre);
                this.crW += (double)(this.riding.pm - this.riding.crg);
                while (this.crX >= 180.0) {
                    this.crX -= 360.0;
                }
                while (this.crX < -180.0) {
                    this.crX += 360.0;
                }
                while (this.crW >= 180.0) {
                    this.crW -= 360.0;
                }
                while (this.crW < -180.0) {
                    this.crW += 360.0;
                }
                double d2 = this.crX * 0.5;
                double d3 = this.crW * 0.5;
                float f2 = 10.0f;
                if (d2 > (double)f2) {
                    d2 = f2;
                }
                if (d2 < (double)(-f2)) {
                    d2 = -f2;
                }
                if (d3 > (double)f2) {
                    d3 = f2;
                }
                if (d3 < (double)(-f2)) {
                    d3 = -f2;
                }
                this.crX -= d2;
                this.crW -= d3;
            }
        }
    }

    public void TH() {
        if (this.cqS != null) {
            this.cqS.setPosition(this.distance, this.gX + this.TI() + this.cqS.Lv(), this.gY);
        }
    }

    public double Lv() {
        return 0.0;
    }

    public double TI() {
        return (double)this.eyeHeight * 0.75;
    }

    public void D(c c2) {
        this.crW = 0.0;
        this.crX = 0.0;
        if (c2 == null) {
            if (this.riding != null) {
                this.b(this.riding.distance, this.riding.Ue().dta + (double)this.riding.eyeHeight, this.riding.gY, this.pl, this.pm);
                this.riding.cqS = null;
            }
            this.riding = null;
        } else {
            if (this.riding != null) {
                this.riding.cqS = null;
            }
            if (c2 != null) {
                c c3 = c2.riding;
                while (c3 != null) {
                    if (c3 == this) {
                        return;
                    }
                    c3 = c3.riding;
                }
            }
            this.riding = c2;
            c2.cqS = this;
        }
    }

    public void a(double d2, double d3, double d4, float f2, float f3, int n2, boolean bl2) {
        this.setPosition(d2, d3, d4);
        this.setRotation(f2, f3);
        List<net.minecraft.util.a> list = this.burning.a(this, this.Ue().af(0.03125, 0.0, 0.03125));
        if (!list.isEmpty()) {
            double d5 = 0.0;
            for (net.minecraft.util.a a2 : list) {
                if (!(a2.dtd > d5)) continue;
                d5 = a2.dtd;
            }
            this.setPosition(d2, d3 += d5 - this.Ue().dta, d4);
        }
    }

    public float TJ() {
        return ViaLoadingBase.getInstance().getTargetVersion().getVersion() <= 47 ? 0.1f : 0.0f;
    }

    public da TK() {
        return null;
    }

    public void Y(b b2) {
        if (this.cse > 0) {
            this.cse = this.TL();
        } else {
            if (!this.burning.dCO && !b2.equals(this.csj)) {
                this.csj = b2;
                BlockPattern$PatternHelper blockPattern$PatternHelper = Blocks.cMs.M(this.burning, b2);
                double d2 = blockPattern$PatternHelper.zR().asx() == net.minecraft.util.ap.dvZ ? (double)blockPattern$PatternHelper.zQ().wj() : (double)blockPattern$PatternHelper.zQ().wf();
                double d3 = blockPattern$PatternHelper.zR().asx() == net.minecraft.util.ap.dvZ ? this.gY : this.distance;
                d3 = Math.abs(bt.aj(d3 - (double)(blockPattern$PatternHelper.zR().asp().asn() == net.minecraft.util.aq.dwh ? 1 : 0), d2, d2 - (double)blockPattern$PatternHelper.zT()));
                double d4 = bt.aj(this.gX - 1.0, blockPattern$PatternHelper.zQ().wg(), blockPattern$PatternHelper.zQ().wg() - blockPattern$PatternHelper.zU());
                this.csk = new da(d3, d4, 0.0);
                this.csl = blockPattern$PatternHelper.zR();
            }
            this.csf = true;
        }
    }

    public int TL() {
        return 300;
    }

    public void P(double d2, double d3, double d4) {
        this.ud = d2;
        this.ue = d3;
        this.uf = d4;
    }

    public void a(byte by2) {
    }

    public void TM() {
    }

    public co[] TN() {
        return null;
    }

    public void c(int n2, co co2) {
    }

    public boolean isBurning() {
        boolean bl2 = this.burning != null && this.burning.dCO;
        return !this.crU && (this.crQ > 0 || bl2 && this.ex(0));
    }

    public boolean isRiding() {
        return this.riding != null;
    }

    public boolean isSneaking() {
        return this.ex(1);
    }

    public void setSneaking(boolean bl2) {
        this.l(1, bl2);
    }

    public boolean isSprinting() {
        return this.ex(3);
    }

    public void setSprinting(boolean bl2) {
        this.l(3, bl2);
    }

    public boolean isInvisible() {
        return this.ex(5);
    }

    public boolean p(net.minecraft.entity.player.a a2) {
        return a2.CL() ? false : this.isInvisible();
    }

    public void ak(boolean bl2) {
        this.l(5, bl2);
    }

    public boolean isEating() {
        return this.ex(4);
    }

    public void aE(boolean bl2) {
        this.l(4, bl2);
    }

    protected boolean ex(int n2) {
        return (this.crV.el(0) & 1 << n2) != 0;
    }

    public void l(int n2, boolean bl2) {
        byte by2 = this.crV.el(0);
        if (bl2) {
            this.crV.b(0, (byte)(by2 | 1 << n2));
        } else {
            this.crV.b(0, (byte)(by2 & ~(1 << n2)));
        }
    }

    public int TO() {
        return this.crV.em(1);
    }

    public void ey(int n2) {
        this.crV.b(1, (short)n2);
    }

    public void b(a a2) {
        this.a(net.minecraft.util.ad.duu, 5.0f);
        ++this.crQ;
        if (this.crQ == 0) {
            this.ev(8);
        }
    }

    public void R(r r2) {
    }

    protected boolean x(double d2, double d3, double d4) {
        b b2 = new b(d2, d3, d4);
        double d5 = d2 - (double)b2.wf();
        double d6 = d3 - (double)b2.wg();
        double d7 = d4 - (double)b2.wj();
        List<net.minecraft.util.a> list = this.burning.r(this.Ue());
        if (list.isEmpty() && !this.burning.aR(b2)) {
            return false;
        }
        int n2 = 3;
        double d8 = 9999.0;
        if (!this.burning.aR(b2.aqN()) && d5 < d8) {
            d8 = d5;
            n2 = 0;
        }
        if (!this.burning.aR(b2.aqO()) && 1.0 - d5 < d8) {
            d8 = 1.0 - d5;
            n2 = 1;
        }
        if (!this.burning.aR(b2.aqJ()) && 1.0 - d6 < d8) {
            d8 = 1.0 - d6;
            n2 = 3;
        }
        if (!this.burning.aR(b2.aqL()) && d7 < d8) {
            d8 = d7;
            n2 = 4;
        }
        if (!this.burning.aR(b2.aqM()) && 1.0 - d7 < d8) {
            d8 = 1.0 - d7;
            n2 = 5;
        }
        float f2 = this.ta.nextFloat() * 0.2f + 0.1f;
        if (n2 == 0) {
            this.ud = -f2;
        }
        if (n2 == 1) {
            this.ud = f2;
        }
        if (n2 == 3) {
            this.ue = f2;
        }
        if (n2 == 4) {
            this.uf = -f2;
        }
        if (n2 == 5) {
            this.uf = f2;
        }
        return true;
    }

    public void TP() {
        this.crr = true;
        this.crx = 0.0f;
    }

    @Override
    public String xG() {
        if (this.xH()) {
            return this.TZ();
        }
        String string = n.ad(this);
        if (string == null) {
            string = "generic";
        }
        return cr.gm("entity." + string + ".name");
    }

    public c[] TQ() {
        return null;
    }

    public boolean W(c c2) {
        return this == c2;
    }

    public float TR() {
        return 0.0f;
    }

    public void aR(float f2) {
    }

    public void aS(float f2) {
    }

    public boolean Io() {
        return true;
    }

    public boolean X(c c2) {
        return false;
    }

    public String toString() {
        return String.format("%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]", this.getClass().getSimpleName(), this.xG(), this.entityId, this.burning == null ? "~NULL~" : this.burning.getTotalWorldTime().akZ(), this.distance, this.gX, this.gY);
    }

    public boolean a(net.minecraft.util.ad ad2) {
        return this.csm && ad2 != net.minecraft.util.ad.duC && !ad2.asc();
    }

    public void Y(c c2) {
        this.b(c2.distance, c2.gX, c2.gY, c2.pl, c2.pm);
    }

    public void Z(c c2) {
        net.minecraft.nbt.n n2 = new net.minecraft.nbt.n();
        c2.b(n2);
        this.a(n2);
        this.cse = c2.cse;
        this.csj = c2.csj;
        this.csk = c2.csk;
        this.csl = c2.csl;
    }

    public void ez(int n2) {
        if (!this.burning.dCO && !this.crt) {
            this.burning.dCL.eY("changeDimension");
            MinecraftServer minecraftServer = MinecraftServer.afe();
            int n3 = this.csh;
            ai ai2 = minecraftServer.ha(n3);
            ai ai3 = minecraftServer.ha(n2);
            this.csh = n2;
            if (n3 == 1 && n2 == 1) {
                ai3 = minecraftServer.ha(0);
                this.csh = 0;
            }
            this.burning.H(this);
            this.crt = false;
            this.burning.dCL.eY("reposition");
            minecraftServer.getMaxPlayers().a(this, n3, ai2, ai3);
            this.burning.dCL.fa("reloading");
            c c2 = n.a(n.ad(this), (w)ai3);
            if (c2 != null) {
                c2.Z(this);
                if (n3 == 1 && n2 == 1) {
                    b b2 = this.burning.aP(ai3.avq());
                    c2.a(b2, c2.pl, c2.pm);
                }
                ai3.G(c2);
            }
            this.crt = true;
            this.burning.dCL.ajh();
            ai2.avX();
            ai3.avX();
            this.burning.dCL.ajh();
        }
    }

    public float a(h h2, w w2, b b2, IBlockState iBlockState) {
        return iBlockState.df().A(this);
    }

    public boolean a(h h2, w w2, b b2, IBlockState iBlockState, float f2) {
        return true;
    }

    public int TS() {
        return 3;
    }

    public da TT() {
        return this.csk;
    }

    public ao TU() {
        return this.csl;
    }

    public boolean TV() {
        return false;
    }

    public void a(net.minecraft.crash.i i2) {
        i2.a("Entity Type", new d(this));
        i2.b("Entity ID", this.entityId);
        i2.a("Entity Name", new net.minecraft.entity.e(this));
        i2.b("Entity's Exact location", String.format("%.2f, %.2f, %.2f", this.distance, this.gX, this.gY));
        i2.b("Entity's Block location", net.minecraft.crash.i.K(bt.aF(this.distance), bt.aF(this.gX), bt.aF(this.gY)));
        i2.b("Entity's Momentum", String.format("%.2f, %.2f, %.2f", this.ud, this.ue, this.uf));
        i2.a("Entity's Rider", new f(this));
        i2.a("Entity's Vehicle", new g(this));
    }

    public boolean TW() {
        return this.isBurning();
    }

    public UUID TX() {
        return this.csn;
    }

    public boolean TY() {
        return true;
    }

    @Override
    public ba xI() {
        net.minecraft.util.s s2 = new net.minecraft.util.s(this.xG());
        s2.arb().a(this.Ud());
        s2.arb().gc(this.TX().toString());
        return s2;
    }

    public void ec(String string) {
        this.crV.b(2, string);
    }

    public String TZ() {
        return this.crV.ep(2);
    }

    public boolean xH() {
        return this.crV.ep(2).length() > 0;
    }

    public void aF(boolean bl2) {
        this.crV.b(3, (byte)(bl2 ? 1 : 0));
    }

    public boolean Ua() {
        return this.crV.el(3) == 1;
    }

    public void Q(double d2, double d3, double d4) {
        this.b(d2, d3, d4, this.pl, this.pm);
    }

    public boolean Ub() {
        return this.Ua();
    }

    public void eA(int n2) {
    }

    public ao aT(float f2) {
        return net.minecraft.util.ao.hz(bt.aF((double)(f2 * 4.0f / 360.0f) + 0.5) & 3);
    }

    public ao Uc() {
        return this.aT(this.pl);
    }

    protected net.minecraft.event.c Ud() {
        net.minecraft.nbt.n n2 = new net.minecraft.nbt.n();
        String string = n.ad(this);
        n2.R("id", this.TX().toString());
        if (string != null) {
            n2.R("type", string);
        }
        n2.R("name", this.xG());
        return new net.minecraft.event.c(net.minecraft.event.d.cKp, new net.minecraft.util.s(n2.toString()));
    }

    public boolean a(e e2) {
        return true;
    }

    public net.minecraft.util.a Ue() {
        return this.jJ;
    }

    public void n(net.minecraft.util.a a2) {
        this.jJ = a2;
    }

    public float getEyeHeight() {
        return this.eyeHeight * 0.85f;
    }

    public boolean Uf() {
        return this.crs;
    }

    public void aG(boolean bl2) {
        this.crs = bl2;
    }

    public boolean f(int n2, co co2) {
        return false;
    }

    @Override
    public void a(ba ba2) {
    }

    @Override
    public boolean c(int n2, String string) {
        return true;
    }

    @Override
    public b xX() {
        return new b(this.distance, this.gX + 0.5, this.gY);
    }

    @Override
    public da RP() {
        return new da(this.distance, this.gX, this.gY);
    }

    public w getBurning() {
        return this.burning;
    }

    @Override
    public c RR() {
        return this;
    }

    @Override
    public boolean RS() {
        return false;
    }

    @Override
    public void a(ad ad2, int n2) {
        this.csp.a(this, ad2, n2);
    }

    public ab Ug() {
        return this.csp;
    }

    public void aa(c c2) {
        this.csp.a(c2.Ug());
    }

    public net.minecraft.nbt.n Uh() {
        return null;
    }

    public void l(net.minecraft.nbt.n n2) {
    }

    public boolean a(net.minecraft.entity.player.a a2, da da2) {
        return false;
    }

    public boolean Ui() {
        return false;
    }

    protected void c(r r2, c c2) {
        if (c2 instanceof r) {
            l.a((r)c2, (c)r2);
        }
        l.b(r2, c2);
    }
}



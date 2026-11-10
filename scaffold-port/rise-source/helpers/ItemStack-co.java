package net.minecraft.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.enchantment.i;
import net.minecraft.enchantment.l;
import net.minecraft.entity.aj;
import net.minecraft.entity.item.j;
import net.minecraft.entity.r;
import net.minecraft.entity.z;
import net.minecraft.init.Items;
import net.minecraft.item.a;
import net.minecraft.item.ai;
import net.minecraft.item.c;
import net.minecraft.item.d;
import net.minecraft.nbt.g;
import net.minecraft.nbt.n;
import net.minecraft.nbt.v;
import net.minecraft.util.an;
import net.minecraft.util.ao;
import net.minecraft.util.b;
import net.minecraft.util.ba;
import net.minecraft.util.cm;
import net.minecraft.util.cr;
import net.minecraft.util.s;
import net.minecraft.world.w;

public final class co {
    final public static DecimalFormat cWq = new DecimalFormat("#.###");
    public int cWr;
    public int cWs;
    public int cWt = 9999999;
    public int cWu;
    private d cWv;
    private n cWw;
    private int cWx;
    private j cWy;
    private Block cWz = null;
    private boolean cWA = false;
    private Block cWB = null;
    private boolean cWC = false;

    public co(Block block) {
        this(block, 1);
    }

    public co(Block block, int n2) {
        this(block, n2, 0);
    }

    public co(Block block, int n2, int n3) {
        this(d.I(block), n2, n3);
    }

    public co(d d2) {
        this(d2, 1);
    }

    public co(d d2, int n2) {
        this(d2, n2, 0);
    }

    public co(d d2, int n2, int n3) {
        this.cWv = d2;
        this.cWr = n2;
        this.cWx = n3;
        if (this.cWx < 0) {
            this.cWx = 0;
        }
    }

    public static co s(n n2) {
        co co2 = new co();
        co2.a(n2);
        return co2.bP() != null ? co2 : null;
    }

    public int jI() {
        return this.cWr;
    }

    public co() {
    }

    public co ge(int n2) {
        co co2 = new co(this.cWv, n2, this.cWx);
        if (this.cWw != null) {
            co2.cWw = (n)this.cWw.aes();
        }
        this.cWr -= n2;
        return co2;
    }

    public d bP() {
        return this.cWv;
    }

    public boolean a(net.minecraft.entity.player.a a2, w w2, b b2, ao ao2, float f2, float f3, float f4) {
        boolean bl2 = this.bP().a(this, a2, w2, b2, ao2, f2, f3, f4);
        if (bl2) {
            a2.a(net.minecraft.stats.n.dpW[d.p(this.cWv)]);
        }
        return bl2;
    }

    public float G(Block block) {
        return this.bP().a(this, block);
    }

    public co a(w w2, net.minecraft.entity.player.a a2) {
        return this.bP().a(this, w2, a2);
    }

    public co b(w w2, net.minecraft.entity.player.a a2) {
        return this.bP().b(this, w2, a2);
    }

    public n t(n n2) {
        cm cm2 = d.cUA.y(this.cWv);
        n2.R("id", cm2 == null ? "minecraft:air" : cm2.toString());
        n2.a("Count", (byte)this.cWr);
        n2.a("Damage", (short)this.cWx);
        if (this.cWw != null) {
            n2.b("tag", this.cWw);
        }
        return n2;
    }

    public void a(n n2) {
        this.cWv = n2.C("id", 8) ? d.et(n2.eK("id")) : d.fT(n2.eF("id"));
        this.cWr = n2.eE("Count");
        this.cWx = n2.eF("Damage");
        if (this.cWx < 0) {
            this.cWx = 0;
        }
        if (n2.C("tag", 10)) {
            this.cWw = n2.eN("tag");
            if (this.cWv != null) {
                this.cWv.q(this.cWw);
            }
        }
    }

    public int adN() {
        return this.bP().add();
    }

    public boolean adO() {
        return this.adN() > 1 && (!this.adP() || !this.adQ());
    }

    public boolean adP() {
        return this.cWv != null && this.cWv.adf() > 0 && (!this.adV() || !this.adW().eO("Unbreakable"));
    }

    public boolean ade() {
        return this.cWv.ade();
    }

    public boolean adQ() {
        return this.adP() && this.cWx > 0;
    }

    public int adR() {
        return this.cWx;
    }

    public int xO() {
        return this.cWx;
    }

    public void gf(int n2) {
        this.cWx = n2;
        if (this.cWx < 0) {
            this.cWx = 0;
        }
    }

    public int adf() {
        return this.cWv.adf();
    }

    public boolean d(int n2, Random random) {
        if (!this.adP()) {
            return false;
        }
        if (n2 > 0) {
            int n3 = l.d(net.minecraft.enchantment.a.cpG.cpO, this);
            int n4 = 0;
            for (int i2 = 0; n3 > 0 && i2 < n2; ++i2) {
                if (!i.a(this, n3, random)) continue;
                ++n4;
            }
            if ((n2 -= n4) <= 0) {
                return false;
            }
        }
        this.cWx += n2;
        return this.cWx > this.adf();
    }

    public void b(int n2, r r2) {
        if ((!(r2 instanceof net.minecraft.entity.player.a) || !((net.minecraft.entity.player.a)r2).cHL.cJe) && this.adP() && this.d(n2, r2.Vn())) {
            r2.W(this);
            --this.cWr;
            if (r2 instanceof net.minecraft.entity.player.a) {
                net.minecraft.entity.player.a a2 = (net.minecraft.entity.player.a)r2;
                a2.a(net.minecraft.stats.n.dpX[d.p(this.cWv)]);
                if (this.cWr == 0 && this.bP() instanceof ai) {
                    a2.abB();
                }
            }
            if (this.cWr < 0) {
                this.cWr = 0;
            }
            this.cWx = 0;
        }
    }

    public void a(r r2, net.minecraft.entity.player.a a2) {
        boolean bl2 = this.cWv.a(this, r2, (r)a2);
        if (bl2) {
            a2.a(net.minecraft.stats.n.dpW[d.p(this.cWv)]);
        }
    }

    public void a(w w2, Block block, b b2, net.minecraft.entity.player.a a2) {
        boolean bl2 = this.cWv.a(this, w2, block, b2, a2);
        if (bl2) {
            a2.a(net.minecraft.stats.n.dpW[d.p(this.cWv)]);
        }
    }

    public boolean F(Block block) {
        return this.cWv.F(block);
    }

    public boolean a(net.minecraft.entity.player.a a2, r r2) {
        return this.cWv.a(this, a2, r2);
    }

    public co adS() {
        co co2 = new co(this.cWv, this.cWr, this.cWx);
        if (this.cWw != null) {
            co2.cWw = (n)this.cWw.aes();
        }
        return co2;
    }

    public static boolean c(co co2, co co3) {
        return co2 == null && co3 == null || co2 != null && co3 != null && (co2.cWw != null || co3.cWw == null) && (co2.cWw == null || co2.cWw.equals(co3.cWw));
    }

    public static boolean d(co co2, co co3) {
        return co2 == null && co3 == null || co2 != null && co3 != null && co2.aH(co3);
    }

    private boolean aH(co co2) {
        return !(this.cWr != co2.cWr || this.cWv != co2.cWv || this.cWx != co2.cWx || this.cWw == null && co2.cWw != null || this.cWw != null && !this.cWw.equals(co2.cWw));
    }

    public static boolean e(co co2, co co3) {
        return co2 == null && co3 == null || co2 != null && co3 != null && co2.aI(co3);
    }

    public boolean aI(co co2) {
        return co2 != null && this.cWv == co2.cWv && this.cWx == co2.cWx;
    }

    public String xn() {
        return this.cWv.an(this);
    }

    public static co aJ(co co2) {
        return co2 == null ? null : co2.adS();
    }

    public String toString() {
        return this.cWr + "x" + this.cWv.xn() + "@" + this.cWx;
    }

    public void a(w w2, net.minecraft.entity.c c2, int n2, boolean bl2) {
        if (this.cWu > 0) {
            --this.cWu;
        }
        ++this.cWt;
        if (this.cWt >= 20) {
            this.cWs = this.cWr;
        }
        if (this.cWv != null) {
            this.cWv.a(this, w2, c2, n2, bl2);
        }
    }

    public void a(w w2, net.minecraft.entity.player.a a2, int n2) {
        a2.a(net.minecraft.stats.n.dpV[d.p(this.cWv)], n2);
        this.cWv.c(this, w2, a2);
    }

    public boolean aK(co co2) {
        return this.aH(co2);
    }

    public int adT() {
        return this.bP().ap(this);
    }

    public a adU() {
        return this.bP().ao(this);
    }

    public void b(w w2, net.minecraft.entity.player.a a2, int n2) {
        this.bP().a(this, w2, a2, n2);
    }

    public boolean adV() {
        return this.cWw != null;
    }

    public n adW() {
        return this.cWw;
    }

    public n j(String string, boolean bl2) {
        if (this.cWw != null && this.cWw.C(string, 10)) {
            return this.cWw.eN(string);
        }
        if (bl2) {
            n n2 = new n();
            this.a(string, n2);
            return n2;
        }
        return null;
    }

    public v adX() {
        return this.cWw == null ? null : this.cWw.D("ench", 10);
    }

    public void u(n n2) {
        this.cWw = n2;
    }

    public String getDisplayName() {
        n n2;
        String string;
        String string2 = string = this.bP() != null ? this.bP().as(this) : "";
        if (this.cWw != null && this.cWw.C("display", 10) && (n2 = this.cWw.eN("display")).C("Name", 8)) {
            string = n2.eK("Name");
        }
        return string;
    }

    public co ey(String string) {
        if (this.cWw == null) {
            this.cWw = new n();
        }
        if (!this.cWw.C("display", 10)) {
            this.cWw.b("display", new n());
        }
        this.cWw.eN("display").R("Name", string);
        return this;
    }

    public void adY() {
        if (this.cWw != null && this.cWw.C("display", 10)) {
            n n2 = this.cWw.eN("display");
            n2.eP("Name");
            if (n2.aet()) {
                this.cWw.eP("display");
                if (this.cWw.aet()) {
                    this.u(null);
                }
            }
        }
    }

    public boolean adZ() {
        return this.cWw != null && this.cWw.C("display", 10) && this.cWw.eN("display").C("Name", 8);
    }

    public List<String> d(net.minecraft.entity.player.a a2, boolean bl2) {
        v v2;
        v v3;
        Multimap<String, net.minecraft.entity.ai.attributes.a> multimap;
        ArrayList<String> arrayList = Lists.newArrayList();
        Object object = this.getDisplayName();
        if (this.adZ()) {
            object = String.valueOf((Object)an.dvy) + (String)object;
        }
        object = (String)object + String.valueOf((Object)an.dvz);
        if (bl2) {
            String string = "";
            if (((String)object).length() > 0) {
                object = (String)object + " (";
                string = ")";
            }
            int n2 = d.p(this.cWv);
            object = this.ade() ? (String)object + String.format("#%04d/%d%s", n2, this.cWx, string) : (String)object + String.format("#%04d%s", n2, string);
        } else if (!this.adZ() && this.cWv == Items.cQz) {
            object = (String)object + " #" + this.cWx;
        }
        arrayList.add((String)object);
        int n3 = 0;
        if (this.adV() && this.cWw.C("HideFlags", 99)) {
            n3 = this.cWw.eG("HideFlags");
        }
        if ((n3 & 0x20) == 0) {
            this.cWv.a(this, a2, arrayList, bl2);
        }
        if (this.adV()) {
            int n4;
            v v4;
            if ((n3 & 1) == 0 && (v4 = this.adX()) != null) {
                for (int i2 = 0; i2 < v4.aeE(); ++i2) {
                    n4 = v4.gi(i2).eF("id");
                    short s2 = v4.gi(i2).eF("lvl");
                    if (net.minecraft.enchantment.a.eh(n4) == null) continue;
                    arrayList.add(net.minecraft.enchantment.a.eh(n4).ek(s2));
                }
            }
            if (this.cWw.C("display", 10)) {
                v v5;
                n n5 = this.cWw.eN("display");
                if (n5.C("color", 3)) {
                    if (bl2) {
                        arrayList.add("Color: #" + Integer.toHexString(n5.eG("color")).toUpperCase());
                    } else {
                        arrayList.add(String.valueOf((Object)an.dvy) + cr.gm("item.dyed"));
                    }
                }
                if (n5.eC("Lore") == 9 && (v5 = n5.D("Lore", 8)).aeE() > 0) {
                    for (n4 = 0; n4 < v5.aeE(); ++n4) {
                        arrayList.add(String.valueOf((Object)an.dvi) + String.valueOf((Object)an.dvy) + v5.gm(n4));
                    }
                }
            }
        }
        if (!(multimap = this.aei()).isEmpty() && (n3 & 2) == 0) {
            arrayList.add("");
            for (Map.Entry<String, net.minecraft.entity.ai.attributes.a> entry : multimap.entries()) {
                net.minecraft.entity.ai.attributes.a a3 = entry.getValue();
                double d2 = a3.Xc();
                if (a3.Xa() == d.cUC) {
                    d2 += (double)l.b(this, z.cvl);
                }
                double d3 = a3.Xb() != 1 && a3.Xb() != 2 ? d2 : d2 * 100.0;
                if (d2 > 0.0) {
                    arrayList.add(String.valueOf((Object)an.dvm) + cr.c("attribute.modifier.plus." + a3.Xb(), cWq.format(d3), cr.gm("attribute.name." + entry.getKey())));
                    continue;
                }
                if (!(d2 < 0.0)) continue;
                arrayList.add(String.valueOf((Object)an.dvp) + cr.c("attribute.modifier.take." + a3.Xb(), cWq.format(d3 *= -1.0), cr.gm("attribute.name." + entry.getKey())));
            }
        }
        if (this.adV() && this.adW().eO("Unbreakable") && (n3 & 4) == 0) {
            arrayList.add(String.valueOf((Object)an.dvm) + cr.gm("item.unbreakable"));
        }
        if (this.adV() && this.cWw.C("CanDestroy", 9) && (n3 & 8) == 0 && (v3 = this.cWw.D("CanDestroy", 8)).aeE() > 0) {
            arrayList.add("");
            arrayList.add(String.valueOf((Object)an.dvk) + cr.gm("item.canBreak"));
            for (int i3 = 0; i3 < v3.aeE(); ++i3) {
                Block block = Block.cq(v3.gm(i3));
                if (block != null) {
                    arrayList.add(String.valueOf((Object)an.dvl) + block.xl());
                    continue;
                }
                arrayList.add(String.valueOf((Object)an.dvl) + "missingno");
            }
        }
        if (this.adV() && this.cWw.C("CanPlaceOn", 9) && (n3 & 0x10) == 0 && (v2 = this.cWw.D("CanPlaceOn", 8)).aeE() > 0) {
            arrayList.add("");
            arrayList.add(String.valueOf((Object)an.dvk) + cr.gm("item.canPlace"));
            for (int i4 = 0; i4 < v2.aeE(); ++i4) {
                Block block = Block.cq(v2.gm(i4));
                if (block != null) {
                    arrayList.add(String.valueOf((Object)an.dvl) + block.xl());
                    continue;
                }
                arrayList.add(String.valueOf((Object)an.dvl) + "missingno");
            }
        }
        if (bl2) {
            if (this.adQ()) {
                arrayList.add("Durability: " + (this.adf() - this.adR()) + " / " + this.adf());
            }
            arrayList.add(String.valueOf((Object)an.dvl) + d.cUA.y(this.cWv).toString());
            if (this.adV()) {
                arrayList.add(String.valueOf((Object)an.dvl) + "NBT: " + this.adW().aeC().size() + " tag(s)");
            }
        }
        return arrayList;
    }

    public boolean aea() {
        return this.bP() != null && this.bP().at(this);
    }

    public c aeb() {
        return this.bP().au(this);
    }

    public boolean aec() {
        return this.bP().av(this) && !this.aed();
    }

    public void b(net.minecraft.enchantment.a a2, int n2) {
        if (this.cWw == null) {
            this.u(new n());
        }
        if (!this.cWw.C("ench", 9)) {
            this.cWw.b("ench", new v());
        }
        v v2 = this.cWw.D("ench", 10);
        n n3 = new n();
        n3.a("id", (short)a2.cpO);
        n3.a("lvl", (short)((byte)n2));
        v2.a(n3);
    }

    public boolean aed() {
        return this.cWw != null && this.cWw.C("ench", 9);
    }

    public void a(String string, g g2) {
        if (this.cWw == null) {
            this.u(new n());
        }
        this.cWw.b(string, g2);
    }

    public boolean aee() {
        return this.bP().adq();
    }

    public boolean aef() {
        return this.cWy != null;
    }

    public void d(j j2) {
        this.cWy = j2;
    }

    public j aeg() {
        return this.cWy;
    }

    public int aeh() {
        return this.adV() && this.cWw.C("RepairCost", 3) ? this.cWw.eG("RepairCost") : 0;
    }

    public void gg(int n2) {
        if (!this.adV()) {
            this.cWw = new n();
        }
        this.cWw.B("RepairCost", n2);
    }

    public Multimap<String, net.minecraft.entity.ai.attributes.a> aei() {
        Multimap<String, net.minecraft.entity.ai.attributes.a> multimap;
        if (this.adV() && this.cWw.C("AttributeModifiers", 9)) {
            multimap = HashMultimap.create();
            v v2 = this.cWw.D("AttributeModifiers", 10);
            for (int i2 = 0; i2 < v2.aeE(); ++i2) {
                n n2 = v2.gi(i2);
                net.minecraft.entity.ai.attributes.a a2 = aj.m(n2);
                if (a2 == null || a2.Xa().getLeastSignificantBits() == 0L || a2.Xa().getMostSignificantBits() == 0L) continue;
                multimap.put(n2.eK("AttributeName"), a2);
            }
        } else {
            multimap = this.bP().adr();
        }
        return multimap;
    }

    public void r(d d2) {
        this.cWv = d2;
    }

    public ba Dy() {
        s s2 = new s(this.getDisplayName());
        if (this.adZ()) {
            s2.arb().g(true);
        }
        ba ba2 = new s("[").n(s2).ga("]");
        if (this.cWv != null) {
            n n2 = new n();
            this.t(n2);
            ba2.arb().a(new net.minecraft.event.c(net.minecraft.event.d.cKo, new s(n2.toString())));
            ba2.arb().b(this.aeb().cUx);
        }
        return ba2;
    }

    public boolean K(Block block) {
        if (block == this.cWz) {
            return this.cWA;
        }
        this.cWz = block;
        if (this.adV() && this.cWw.C("CanDestroy", 9)) {
            v v2 = this.cWw.D("CanDestroy", 8);
            for (int i2 = 0; i2 < v2.aeE(); ++i2) {
                Block block2 = Block.cq(v2.gm(i2));
                if (block2 != block) continue;
                this.cWA = true;
                return true;
            }
        }
        this.cWA = false;
        return false;
    }

    public boolean w(Block block) {
        if (block == this.cWB) {
            return this.cWC;
        }
        this.cWB = block;
        if (this.adV() && this.cWw.C("CanPlaceOn", 9)) {
            v v2 = this.cWw.D("CanPlaceOn", 8);
            for (int i2 = 0; i2 < v2.aeE(); ++i2) {
                Block block2 = Block.cq(v2.gm(i2));
                if (block2 != block) continue;
                this.cWC = true;
                return true;
            }
        }
        this.cWC = false;
        return false;
    }
}



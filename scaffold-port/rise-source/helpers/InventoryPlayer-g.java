package net.minecraft.entity.player;

import hackclient.rise.bu;
import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.i;
import net.minecraft.entity.c;
import net.minecraft.entity.player.a;
import net.minecraft.entity.player.h;
import net.minecraft.entity.r;
import net.minecraft.inventory.ab;
import net.minecraft.item.co;
import net.minecraft.item.d;
import net.minecraft.item.y;
import net.minecraft.nbt.aa;
import net.minecraft.nbt.n;
import net.minecraft.nbt.v;
import net.minecraft.util.ba;
import net.minecraft.util.cl;
import net.minecraft.util.s;
import net.minecraft.util.t;
import rise.util.InstanceAccess;

public class g
implements InstanceAccess,
ab {
    public co[] cIU = new co[36];
    public co[] cIV = new co[4];
    public int cIW;
    public int cIX;
    public boolean cIY;
    public a bhA;
    private co jV;
    public boolean cIZ;

    public g(a a2) {
        this.bhA = a2;
    }
    public co acq() {
        if (this.bhA == Minecraft.getMinecraft().thePlayer) {
            this.d(bu.class);
            if (bu.bQ() >= 9) return null;
            this.d(bu.class);
            if (bu.bQ() < 0) return null;
            this.d(bu.class);
            co co2 = this.cIU[bu.bQ()];
            return co2;
        }
        if (this.cIW >= 9) return null;
        if (this.cIW < 0) return null;
        co co3 = this.cIU[this.cIW];
        return co3;
    }

    public co acr() {
        return this.cIW < 9 && this.cIW >= 0 ? this.cIU[this.cIW] : null;
    }

    public static int acs() {
        return 9;
    }

    private int m(d d2) {
        for (int i2 = 0; i2 < this.cIU.length; ++i2) {
            if (this.cIU[i2] == null || this.cIU[i2].bP() != d2) continue;
            return i2;
        }
        return -1;
    }

    private int e(d d2, int n2) {
        for (int i2 = 0; i2 < this.cIU.length; ++i2) {
            if (this.cIU[i2] == null || this.cIU[i2].bP() != d2 || this.cIU[i2].xO() != n2) continue;
            return i2;
        }
        return -1;
    }

    private int ae(co co2) {
        for (int i2 = 0; i2 < this.cIU.length; ++i2) {
            if (this.cIU[i2] == null || this.cIU[i2].bP() != co2.bP() || !this.cIU[i2].adO() || this.cIU[i2].cWr >= this.cIU[i2].adN() || this.cIU[i2].cWr >= this.Yv() || this.cIU[i2].ade() && this.cIU[i2].xO() != co2.xO() || !co.c(this.cIU[i2], co2)) continue;
            return i2;
        }
        return -1;
    }

    public int act() {
        for (int i2 = 0; i2 < this.cIU.length; ++i2) {
            if (this.cIU[i2] != null) continue;
            return i2;
        }
        return -1;
    }

    public void a(d d2, int n2, boolean bl2, boolean bl3) {
        int n3;
        co co2 = this.acr();
        int n4 = n3 = bl2 ? this.e(d2, n2) : this.m(d2);
        if (n3 >= 0 && n3 < 9) {
            this.cIW = n3;
        } else if (bl3 && d2 != null) {
            int n5 = this.act();
            if (n5 >= 0 && n5 < 9) {
                this.cIW = n5;
            }
            if (co2 == null || !co2.aec() || this.e(co2.bP(), co2.adR()) != this.cIW) {
                int n6;
                int n7 = this.e(d2, n2);
                if (n7 >= 0) {
                    n6 = this.cIU[n7].cWr;
                    this.d(bu.class);
                    this.cIU[n7] = this.cIU[bu.bQ()];
                } else {
                    n6 = 1;
                }
                this.d(bu.class);
                this.cIU[bu.bQ()] = new co(d2, n6, n2);
            }
        }
    }

    public void fI(int n2) {
        if (n2 > 0) {
            n2 = 1;
        }
        if (n2 < 0) {
            n2 = -1;
        }
        this.cIW -= n2;
        while (this.cIW < 0) {
            this.cIW += 9;
        }
        while (this.cIW >= 9) {
            this.cIW -= 9;
        }
    }

    public int a(d d2, int n2, int n3, n n4) {
        int n5;
        co co2;
        int n6;
        int n7 = 0;
        for (n6 = 0; n6 < this.cIU.length; ++n6) {
            co2 = this.cIU[n6];
            if (co2 == null || d2 != null && co2.bP() != d2 || n2 > -1 && co2.xO() != n2 || n4 != null && !aa.a(n4, co2.adW(), true)) continue;
            n5 = n3 <= 0 ? co2.cWr : Math.min(n3 - n7, co2.cWr);
            n7 += n5;
            if (n3 == 0) continue;
            this.cIU[n6].cWr -= n5;
            if (this.cIU[n6].cWr == 0) {
                this.cIU[n6] = null;
            }
            if (n3 <= 0 || n7 < n3) continue;
            return n7;
        }
        for (n6 = 0; n6 < this.cIV.length; ++n6) {
            co2 = this.cIV[n6];
            if (co2 == null || d2 != null && co2.bP() != d2 || n2 > -1 && co2.xO() != n2 || n4 != null && !aa.a(n4, co2.adW(), false)) continue;
            n5 = n3 <= 0 ? co2.cWr : Math.min(n3 - n7, co2.cWr);
            n7 += n5;
            if (n3 == 0) continue;
            this.cIV[n6].cWr -= n5;
            if (this.cIV[n6].cWr == 0) {
                this.cIV[n6] = null;
            }
            if (n3 <= 0 || n7 < n3) continue;
            return n7;
        }
        if (this.jV != null) {
            if (d2 != null && this.jV.bP() != d2) {
                return n7;
            }
            if (n2 > -1 && this.jV.xO() != n2) {
                return n7;
            }
            if (n4 != null && !aa.a(n4, this.jV.adW(), false)) {
                return n7;
            }
            n6 = n3 <= 0 ? this.jV.cWr : Math.min(n3 - n7, this.jV.cWr);
            n7 += n6;
            if (n3 != 0) {
                this.jV.cWr -= n6;
                if (this.jV.cWr == 0) {
                    this.jV = null;
                }
                if (n3 > 0 && n7 >= n3) {
                    return n7;
                }
            }
        }
        return n7;
    }

    private int af(co co2) {
        d d2 = co2.bP();
        int n2 = co2.cWr;
        int n3 = this.ae(co2);
        if (n3 < 0) {
            n3 = this.act();
        }
        if (n3 < 0) {
            return n2;
        }
        if (this.cIU[n3] == null) {
            this.cIU[n3] = new co(d2, 0, co2.xO());
            if (co2.adV()) {
                this.cIU[n3].u((n)co2.adW().aes());
            }
        }
        int n4 = n2;
        if (n2 > this.cIU[n3].adN() - this.cIU[n3].cWr) {
            n4 = this.cIU[n3].adN() - this.cIU[n3].cWr;
        }
        if (n4 > this.Yv() - this.cIU[n3].cWr) {
            n4 = this.Yv() - this.cIU[n3].cWr;
        }
        if (n4 == 0) {
            return n2;
        }
        this.cIU[n3].cWr += n4;
        this.cIU[n3].cWu = 5;
        return n2 -= n4;
    }

    public void acu() {
        for (int i2 = 0; i2 < this.cIU.length; ++i2) {
            if (this.cIU[i2] == null) continue;
            this.cIU[i2].a(this.bhA.burning, this.bhA, i2, this.cIW == i2);
        }
    }

    public boolean n(d d2) {
        int n2 = this.m(d2);
        if (n2 < 0) {
            return false;
        }
        if (--this.cIU[n2].cWr <= 0) {
            this.cIU[n2] = null;
        }
        return true;
    }

    public boolean o(d d2) {
        int n2 = this.m(d2);
        return n2 >= 0;
    }

    public boolean ag(co co2) {
        if (co2 != null && co2.cWr != 0 && co2.bP() != null) {
            try {
                int n2;
                if (co2.adQ()) {
                    int n3 = this.act();
                    if (n3 >= 0) {
                        this.cIU[n3] = co.aJ(co2);
                        this.cIU[n3].cWu = 5;
                        co2.cWr = 0;
                        return true;
                    }
                    if (this.bhA.cHL.cJe) {
                        co2.cWr = 0;
                        return true;
                    }
                    return false;
                }
                do {
                    n2 = co2.cWr;
                    co2.cWr = this.af(co2);
                } while (co2.cWr > 0 && co2.cWr < n2);
                if (co2.cWr == n2 && this.bhA.cHL.cJe) {
                    co2.cWr = 0;
                    return true;
                }
                return co2.cWr < n2;
            }
            catch (Throwable throwable) {
                net.minecraft.crash.a a2 = net.minecraft.crash.a.a(throwable, "Adding item to inventory");
                i i2 = a2.dY("Item being added");
                i2.b("Item ID", d.p(co2.bP()));
                i2.b("Item data", co2.xO());
                i2.a("Item name", new h(this, co2));
                throw new cl(a2);
            }
        }
        return false;
    }

    @Override
    public co ap(int n2, int n3) {
        co[] coArray = this.cIU;
        if (n2 >= this.cIU.length) {
            coArray = this.cIV;
            n2 -= this.cIU.length;
        }
        if (coArray[n2] != null) {
            if (coArray[n2].cWr <= n3) {
                co co2 = coArray[n2];
                coArray[n2] = null;
                return co2;
            }
            co co3 = coArray[n2].ge(n3);
            if (coArray[n2].cWr == 0) {
                coArray[n2] = null;
            }
            return co3;
        }
        return null;
    }

    @Override
    public co fj(int n2) {
        co[] coArray = this.cIU;
        if (n2 >= this.cIU.length) {
            coArray = this.cIV;
            n2 -= this.cIU.length;
        }
        if (coArray[n2] != null) {
            co co2 = coArray[n2];
            coArray[n2] = null;
            return co2;
        }
        return null;
    }

    @Override
    public void g(int n2, co co2) {
        co[] coArray = this.cIU;
        if (n2 >= coArray.length) {
            n2 -= coArray.length;
            coArray = this.cIV;
        }
        coArray[n2] = co2;
    }

    public float G(Block block) {
        float f2 = 1.0f;
        this.d(bu.class);
        if (this.cIU[bu.bQ()] != null) {
            this.d(bu.class);
            f2 *= this.cIU[bu.bQ()].G(block);
        }
        return f2;
    }

    public v a(v v2) {
        n n2;
        int n3;
        for (n3 = 0; n3 < this.cIU.length; ++n3) {
            if (this.cIU[n3] == null) continue;
            n2 = new n();
            n2.a("Slot", (byte)n3);
            this.cIU[n3].t(n2);
            v2.a(n2);
        }
        for (n3 = 0; n3 < this.cIV.length; ++n3) {
            if (this.cIV[n3] == null) continue;
            n2 = new n();
            n2.a("Slot", (byte)(n3 + 100));
            this.cIV[n3].t(n2);
            v2.a(n2);
        }
        return v2;
    }

    public void b(v v2) {
        this.cIU = new co[36];
        this.cIV = new co[4];
        for (int i2 = 0; i2 < v2.aeE(); ++i2) {
            n n2 = v2.gi(i2);
            int n3 = n2.eE("Slot") & 0xFF;
            co co2 = co.s(n2);
            if (co2 == null) continue;
            if (n3 >= 0 && n3 < this.cIU.length) {
                this.cIU[n3] = co2;
            }
            if (n3 < 100 || n3 >= this.cIV.length + 100) continue;
            this.cIV[n3 - 100] = co2;
        }
    }

    @Override
    public int Yu() {
        return this.cIU.length + 4;
    }

    @Override
    public co fi(int n2) {
        co[] coArray = this.cIU;
        if (n2 >= coArray.length) {
            n2 -= coArray.length;
            coArray = this.cIV;
        }
        return coArray[n2];
    }

    @Override
    public String xG() {
        return "container.inventory";
    }

    @Override
    public boolean xH() {
        return false;
    }

    @Override
    public ba xI() {
        return this.xH() ? new s(this.xG()) : new t(this.xG(), new Object[0]);
    }

    @Override
    public int Yv() {
        return 64;
    }

    public boolean H(Block block) {
        if (block.wN().zr()) {
            return true;
        }
        this.d(bu.class);
        co co2 = bu.bO();
        return co2 != null && co2.F(block);
    }

    public co fJ(int n2) {
        return this.cIV[n2];
    }

    public int VC() {
        int n2 = 0;
        for (co co2 : this.cIV) {
            if (co2 == null || !(co2.bP() instanceof y)) continue;
            int n3 = ((y)co2.bP()).cVa;
            n2 += n3;
        }
        return n2;
    }

    public void aZ(float f2) {
        if ((f2 /= 4.0f) < 1.0f) {
            f2 = 1.0f;
        }
        for (int i2 = 0; i2 < this.cIV.length; ++i2) {
            if (this.cIV[i2] == null || !(this.cIV[i2].bP() instanceof y)) continue;
            this.cIV[i2].b((int)f2, (r)this.bhA);
            if (this.cIV[i2].cWr != 0) continue;
            this.cIV[i2] = null;
        }
    }

    public void acv() {
        int n2;
        for (n2 = 0; n2 < this.cIU.length; ++n2) {
            if (this.cIU[n2] == null) continue;
            this.bhA.a(this.cIU[n2], true, false);
            this.cIU[n2] = null;
        }
        for (n2 = 0; n2 < this.cIV.length; ++n2) {
            if (this.cIV[n2] == null) continue;
            this.bhA.a(this.cIV[n2], true, false);
            this.cIV[n2] = null;
        }
    }

    @Override
    public void Li() {
        this.cIZ = true;
    }

    public void e(co co2) {
        this.jV = co2;
    }

    public co bO() {
        return this.jV;
    }

    @Override
    public boolean v(a a2) {
        return !this.bhA.crt && a2.T((c)this.bhA) <= 64.0;
    }

    public boolean ah(co co2) {
        int n2;
        for (n2 = 0; n2 < this.cIV.length; ++n2) {
            if (this.cIV[n2] == null || !this.cIV[n2].aI(co2)) continue;
            return true;
        }
        for (n2 = 0; n2 < this.cIU.length; ++n2) {
            if (this.cIU[n2] == null || !this.cIU[n2].aI(co2)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void w(a a2) {
    }

    @Override
    public void x(a a2) {
    }

    @Override
    public boolean h(int n2, co co2) {
        return true;
    }

    public void a(g g2) {
        int n2;
        for (n2 = 0; n2 < this.cIU.length; ++n2) {
            this.cIU[n2] = co.aJ(g2.cIU[n2]);
        }
        for (n2 = 0; n2 < this.cIV.length; ++n2) {
            this.cIV[n2] = co.aJ(g2.cIV[n2]);
        }
        this.cIW = g2.cIW;
    }

    @Override
    public int cx(int n2) {
        return 0;
    }

    @Override
    public void K(int n2, int n3) {
    }

    @Override
    public int Is() {
        return 0;
    }

    @Override
    public void clear() {
        Arrays.fill(this.cIU, null);
        Arrays.fill(this.cIV, null);
    }
}



package net.minecraft.world;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.viamcp.fixes.FixedSoundEngine;
import hackclient.rise.aef;
import hackclient.rise.afi;
import hackclient.rise.aha;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab$EnumBlockHalf;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStairs$EnumHalf;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.p;
import net.minecraft.init.Blocks;
import net.minecraft.item.co;
import net.minecraft.nbt.n;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ao;
import net.minecraft.util.ar;
import net.minecraft.util.as;
import net.minecraft.util.bi;
import net.minecraft.util.bj;
import net.minecraft.util.bt;
import net.minecraft.util.ce;
import net.minecraft.util.cf;
import net.minecraft.util.cl;
import net.minecraft.util.da;
import net.minecraft.world.aa;
import net.minecraft.world.ac;
import net.minecraft.world.ah;
import net.minecraft.world.an;
import net.minecraft.world.ap;
import net.minecraft.world.b;
import net.minecraft.world.biome.aj;
import net.minecraft.world.biome.j;
import net.minecraft.world.border.c;
import net.minecraft.world.chunk.a;
import net.minecraft.world.e;
import net.minecraft.world.f;
import net.minecraft.world.g;
import net.minecraft.world.i;
import net.minecraft.world.l;
import net.minecraft.world.s;
import net.minecraft.world.storage.d;
import net.minecraft.world.storage.h;
import net.minecraft.world.storage.o;
import net.minecraft.world.x;
import net.minecraft.world.y;
import net.minecraft.world.z;
import rise.module.impl.render.FullBright;
import rise.util.InstanceAccess;

/*
 * Duplicate member names - consider using --renamedupmembers true
 */
public abstract class w
implements InstanceAccess,
aha,
Serializable,
l {
    private int dCi = 63;
    protected boolean dCj;
    final public ConcurrentLinkedQueue<net.minecraft.entity.c> dCk = new ConcurrentLinkedQueue();
    protected final List<net.minecraft.entity.c> dCl = Lists.newArrayList();
    final public List<net.minecraft.tileentity.d> dCm = Lists.newArrayList();
    final public List<net.minecraft.tileentity.d> dCn = Lists.newArrayList();
    private final List<net.minecraft.tileentity.d> dCo = Lists.newArrayList();
    private final List<net.minecraft.tileentity.d> dCp = Lists.newArrayList();
    final public List<net.minecraft.entity.player.a> dCq = Lists.newArrayList();
    final public List<net.minecraft.entity.c> dCr = Lists.newArrayList();
    protected final bj<net.minecraft.entity.c> dCs = new bj();
    private final long dCt = 0xFFFFFFL;
    private int dCu;
    protected int dCv = new Random().nextInt();
    protected final int dCw = 1013904223;
    protected float dCx;
    protected float dCy;
    protected float dCz;
    protected float dCA;
    private int dCB;
    final public Random dCC = new Random();
    final public ac dCD;
    protected List<net.minecraft.world.o> dCE = Lists.newArrayList();
    protected net.minecraft.world.chunk.h dCF;
    protected final d dCG;
    protected o totalWorldTime;
    protected boolean dCI;
    protected h dCJ;
    protected net.minecraft.village.e dCK;
    final public net.minecraft.profiler.d dCL;
    private final Calendar dCM = Calendar.getInstance();
    protected net.minecraft.scoreboard.i dCN = new net.minecraft.scoreboard.i();
    final public boolean dCO;
    protected Set<b> dCP = Sets.newHashSet();
    private int dCQ = this.dCC.nextInt(12000);
    protected boolean dCR = true;
    protected boolean dCS = true;
    private boolean dCT;
    private final c dCU;
    int[] dCV = new int[32768];

    protected w(d d2, o o2, ac ac2, net.minecraft.profiler.d d3, boolean bl2) {
        this.dCG = d2;
        this.dCL = d3;
        this.totalWorldTime = o2;
        this.dCD = ac2;
        this.dCO = bl2;
        this.dCU = ac2.Sh();
    }

    public w ava() {
        return this;
    }

    @Override
    public j aB(net.minecraft.util.b b2) {
        if (this.aF(b2)) {
            a a2 = this.aG(b2);
            try {
                return a2.a(b2, this.dCD.avb());
            }
            catch (Throwable throwable) {
                net.minecraft.crash.a a3 = net.minecraft.crash.a.a(throwable, "Getting biome");
                net.minecraft.crash.i i2 = a3.dY("Coordinates of biome request");
                i2.a("Location", new x(this, b2));
                throw new cl(a3);
            }
        }
        return this.dCD.avb().b(b2, j.dFy);
    }

    public aj avb() {
        return this.dCD.avb();
    }

    protected abstract net.minecraft.world.chunk.h Hz();

    public void a(an an2) {
        this.totalWorldTime.cI(true);
    }

    public void avc() {
        this.aZ(new net.minecraft.util.b(8, 64, 8));
    }

    public Block aD(net.minecraft.util.b b2) {
        net.minecraft.util.b b3 = new net.minecraft.util.b(b2.wf(), this.avn(), b2.wj());
        while (!this.aC(b3.aqJ())) {
            b3 = b3.aqJ();
        }
        return this.H(b3).df();
    }

    private boolean aE(net.minecraft.util.b b2) {
        if (b2 == null) {
            return false;
        }
        return b2.wf() >= -30000000 && b2.wj() >= -30000000 && b2.wf() < 30000000 && b2.wj() < 30000000 && b2.wg() >= 0 && b2.wg() < 256;
    }

    @Override
    public boolean aC(net.minecraft.util.b b2) {
        return this.H(b2).df().wN() == Material.bdF;
    }

    public boolean aF(net.minecraft.util.b b2) {
        return this.b(b2, true);
    }

    public boolean b(net.minecraft.util.b b2, boolean bl2) {
        return this.aE(b2) && this.e(b2.wf() >> 4, b2.wj() >> 4, bl2);
    }

    public boolean f(net.minecraft.util.b b2, int n2) {
        return this.a(b2, n2, true);
    }

    public boolean a(net.minecraft.util.b b2, int n2, boolean bl2) {
        return this.a(b2.wf() - n2, b2.wg() - n2, b2.wj() - n2, b2.wf() + n2, b2.wg() + n2, b2.wj() + n2, bl2);
    }

    public boolean e(net.minecraft.util.b b2, net.minecraft.util.b b3) {
        return this.a(b2, b3, true);
    }

    public boolean a(net.minecraft.util.b b2, net.minecraft.util.b b3, boolean bl2) {
        return this.a(b2.wf(), b2.wg(), b2.wj(), b3.wf(), b3.wg(), b3.wj(), bl2);
    }

    public boolean a(net.minecraft.world.gen.structure.w w2) {
        return this.a(w2, true);
    }

    public boolean a(net.minecraft.world.gen.structure.w w2, boolean bl2) {
        return this.a(w2.dPT, w2.dPU, w2.dPV, w2.dPW, w2.dPX, w2.dPY, bl2);
    }

    private boolean a(int n2, int n3, int n4, int n5, int n6, int n7, boolean bl2) {
        if (n6 >= 0 && n3 < 256) {
            n4 >>= 4;
            n5 >>= 4;
            n7 >>= 4;
            for (int i2 = n2 >>= 4; i2 <= n5; ++i2) {
                for (int i3 = n4; i3 <= n7; ++i3) {
                    if (this.e(i2, i3, bl2)) continue;
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean e(int n2, int n3, boolean bl2) {
        return this.dCF.E(n2, n3) && (bl2 || !this.dCF.H(n2, n3).isEmpty());
    }

    public a aG(net.minecraft.util.b b2) {
        return this.aL(b2.wf() >> 4, b2.wj() >> 4);
    }

    public a aL(int n2, int n3) {
        return this.dCF.H(n2, n3);
    }

    public boolean a(net.minecraft.util.b b2, IBlockState iBlockState, int n2) {
        if (!this.aE(b2)) {
            return false;
        }
        if (!this.dCO && this.totalWorldTime.awo() == ap.dEa) {
            return false;
        }
        a a2 = this.aG(b2);
        Block block = iBlockState.df();
        IBlockState iBlockState2 = a2.e(b2, iBlockState);
        if (iBlockState2 == null) {
            return false;
        }
        Block block2 = iBlockState2.df();
        if (block.wJ() != block2.wJ() || block.wL() != block2.wL()) {
            this.dCL.eY("checkLight");
            this.aV(b2);
            this.dCL.ajh();
        }
        if ((n2 & 2) != 0 && (!this.dCO || (n2 & 4) == 0) && a2.axn()) {
            this.L(b2);
        }
        if (!this.dCO && (n2 & 1) != 0) {
            this.b(b2, iBlockState2.df());
            if (block.xv()) {
                this.f(b2, block);
            }
        }
        return true;
    }

    public boolean aH(net.minecraft.util.b b2) {
        return this.a(b2, Blocks.cKu.xy(), 3);
    }

    public boolean c(net.minecraft.util.b b2, boolean bl2) {
        return FixedSoundEngine.destroyBlock(this, b2, bl2);
    }

    public boolean d(net.minecraft.util.b b2, IBlockState iBlockState) {
        return this.a(b2, iBlockState, 3);
    }

    public void L(net.minecraft.util.b b2) {
        for (int i2 = 0; i2 < this.dCE.size(); ++i2) {
            this.dCE.get(i2).L(b2);
        }
    }

    public void b(net.minecraft.util.b b2, Block block) {
        if (this.totalWorldTime.awo() != ap.dEa) {
            this.c(b2, block);
        }
    }

    public void x(int n2, int n3, int n4, int n5) {
        int n6;
        if (n4 > n5) {
            n6 = n5;
            n5 = n4;
            n4 = n6;
        }
        if (!this.dCD.avR()) {
            for (n6 = n4; n6 <= n5; ++n6) {
                this.d(g.dBr, new net.minecraft.util.b(n2, n6, n3));
            }
        }
        this.f(n2, n4, n3, n2, n5, n3);
    }

    public void f(net.minecraft.util.b b2, net.minecraft.util.b b3) {
        this.f(b2.wf(), b2.wg(), b2.wj(), b3.wf(), b3.wg(), b3.wj());
    }

    public void f(int n2, int n3, int n4, int n5, int n6, int n7) {
        for (int i2 = 0; i2 < this.dCE.size(); ++i2) {
            this.dCE.get(i2).f(n2, n3, n4, n5, n6, n7);
        }
    }

    public void c(net.minecraft.util.b b2, Block block) {
        this.d(b2.aqN(), block);
        this.d(b2.aqO(), block);
        this.d(b2.aqK(), block);
        this.d(b2.aqJ(), block);
        this.d(b2.aqL(), block);
        this.d(b2.aqM(), block);
    }

    public void a(net.minecraft.util.b b2, Block block, ao ao2) {
        if (ao2 != ao.dvM) {
            this.d(b2.aqN(), block);
        }
        if (ao2 != ao.dvN) {
            this.d(b2.aqO(), block);
        }
        if (ao2 != ao.dvI) {
            this.d(b2.aqK(), block);
        }
        if (ao2 != ao.dvJ) {
            this.d(b2.aqJ(), block);
        }
        if (ao2 != ao.dvK) {
            this.d(b2.aqL(), block);
        }
        if (ao2 != ao.dvL) {
            this.d(b2.aqM(), block);
        }
    }

    public void d(net.minecraft.util.b b2, Block block) {
        if (!this.dCO) {
            IBlockState iBlockState = this.H(b2);
            try {
                iBlockState.df().a(this, b2, iBlockState, block);
            }
            catch (Throwable throwable) {
                net.minecraft.crash.a a2 = net.minecraft.crash.a.a(throwable, "Exception while updating neighbours");
                net.minecraft.crash.i i2 = a2.dY("Block being updated");
                i2.a("Source block type", new y(this, block));
                net.minecraft.crash.i.a(i2, b2, iBlockState);
                throw new cl(a2);
            }
        }
    }

    public boolean e(net.minecraft.util.b b2, Block block) {
        return false;
    }

    public boolean aI(net.minecraft.util.b b2) {
        return this.aG(b2).aI(b2);
    }

    public boolean aJ(net.minecraft.util.b b2) {
        if (b2.wg() >= this.avn()) {
            return this.aI(b2);
        }
        net.minecraft.util.b b3 = new net.minecraft.util.b(b2.wf(), this.avn(), b2.wj());
        if (!this.aI(b3)) {
            return false;
        }
        b3 = b3.aqK();
        while (b3.wg() > b2.wg()) {
            Block block = this.H(b3).df();
            if (block.wJ() > 0 && !block.wN().zh()) {
                return false;
            }
            b3 = b3.aqK();
        }
        return true;
    }

    public int aK(net.minecraft.util.b b2) {
        if (b2.wg() < 0) {
            return 0;
        }
        if (b2.wg() >= 256) {
            b2 = new net.minecraft.util.b(b2.wf(), 255, b2.wj());
        }
        return this.aG(b2).g(b2, 0);
    }

    public int aL(net.minecraft.util.b b2) {
        return this.d(b2, true);
    }

    public int d(net.minecraft.util.b b2, boolean bl2) {
        if (b2.wf() >= -30000000 && b2.wj() >= -30000000 && b2.wf() < 30000000 && b2.wj() < 30000000) {
            if (bl2 && this.H(b2).df().wM()) {
                int n2 = this.d(b2.aqJ(), false);
                int n3 = this.d(b2.aqO(), false);
                int n4 = this.d(b2.aqN(), false);
                int n5 = this.d(b2.aqM(), false);
                int n6 = this.d(b2.aqL(), false);
                if (n3 > n2) {
                    n2 = n3;
                }
                if (n4 > n2) {
                    n2 = n4;
                }
                if (n5 > n2) {
                    n2 = n5;
                }
                if (n6 > n2) {
                    n2 = n6;
                }
                return n2;
            }
            if (b2.wg() < 0) {
                return 0;
            }
            if (b2.wg() >= 256) {
                b2 = new net.minecraft.util.b(b2.wf(), 255, b2.wj());
            }
            a a2 = this.aG(b2);
            return a2.g(b2, this.dCu);
        }
        return 15;
    }

    public net.minecraft.util.b aM(net.minecraft.util.b b2) {
        int n2 = b2.wf() >= -30000000 && b2.wj() >= -30000000 && b2.wf() < 30000000 && b2.wj() < 30000000 ? (this.e(b2.wf() >> 4, b2.wj() >> 4, true) ? this.aL(b2.wf() >> 4, b2.wj() >> 4).aU(b2.wf() & 0xF, b2.wj() & 0xF) : 0) : this.avn() + 1;
        return new net.minecraft.util.b(b2.wf(), n2, b2.wj());
    }

    public int aM(int n2, int n3) {
        if (n2 >= -30000000 && n3 >= -30000000 && n2 < 30000000 && n3 < 30000000) {
            if (!this.e(n2 >> 4, n3 >> 4, true)) {
                return 0;
            }
            a a2 = this.aL(n2 >> 4, n3 >> 4);
            return a2.axz();
        }
        return this.avn() + 1;
    }

    public int c(g g2, net.minecraft.util.b b2) {
        if (this.dCD.avR() && g2 == g.dBr) {
            return 0;
        }
        if (b2.wg() < 0) {
            b2 = new net.minecraft.util.b(b2.wf(), 0, b2.wj());
        }
        if (!this.aE(b2)) {
            return g2.dBt;
        }
        if (!this.aF(b2)) {
            return g2.dBt;
        }
        if (this.H(b2).df().wM()) {
            int n2 = this.b(g2, b2.aqJ());
            int n3 = this.b(g2, b2.aqO());
            int n4 = this.b(g2, b2.aqN());
            int n5 = this.b(g2, b2.aqM());
            int n6 = this.b(g2, b2.aqL());
            if (n3 > n2) {
                n2 = n3;
            }
            if (n4 > n2) {
                n2 = n4;
            }
            if (n5 > n2) {
                n2 = n5;
            }
            if (n6 > n2) {
                n2 = n6;
            }
            return n2;
        }
        a a2 = this.aG(b2);
        return a2.b(g2, b2);
    }

    public int b(g g2, net.minecraft.util.b b2) {
        if (b2.wg() < 0) {
            b2 = new net.minecraft.util.b(b2.wf(), 0, b2.wj());
        }
        if (!this.aE(b2)) {
            return g2.dBt;
        }
        if (!this.aF(b2)) {
            return g2.dBt;
        }
        a a2 = this.aG(b2);
        return a2.b(g2, b2);
    }

    public void a(g g2, net.minecraft.util.b b2, int n2) {
        if (this.aE(b2) && this.aF(b2)) {
            a a2 = this.aG(b2);
            a2.a(g2, b2, n2);
            this.M(b2);
        }
    }

    public void M(net.minecraft.util.b b2) {
        for (int i2 = 0; i2 < this.dCE.size(); ++i2) {
            this.dCE.get(i2).M(b2);
        }
    }

    @Override
    public int b(net.minecraft.util.b b2, int n2) {
        int n3 = this.c(g.dBr, b2);
        int n4 = this.c(g.dBs, b2);
        if (n4 < n2) {
            n4 = n2;
        }
        return n3 << 20 | n4 << 4;
    }

    public float aN(net.minecraft.util.b b2) {
        return this.dCD.avS()[this.aL(b2)];
    }

    @Override
    public IBlockState H(net.minecraft.util.b b2) {
        if (!this.aE(b2)) {
            return Blocks.cKu.xy();
        }
        a a2 = this.aG(b2);
        return a2.H(b2);
    }

    public boolean avd() {
        return this.dCu < 4;
    }

    public ce d(da da2, da da3) {
        return this.a(da2, da3, false, false, false);
    }

    public ce a(da da2, da da3, boolean bl2) {
        return this.a(da2, da3, bl2, false, false);
    }

    public ce a(da da2, da da3, boolean bl2, boolean bl3, boolean bl4) {
        if (!(Double.isNaN(da2.dzY) || Double.isNaN(da2.dzZ) || Double.isNaN(da2.dAa))) {
            if (!(Double.isNaN(da3.dzY) || Double.isNaN(da3.dzZ) || Double.isNaN(da3.dAa))) {
                ce ce2;
                int n2 = bt.aF(da3.dzY);
                int n3 = bt.aF(da3.dzZ);
                int n4 = bt.aF(da3.dAa);
                int n5 = bt.aF(da2.dzY);
                int n6 = bt.aF(da2.dzZ);
                int n7 = bt.aF(da2.dAa);
                net.minecraft.util.b b2 = new net.minecraft.util.b(n5, n6, n7);
                IBlockState iBlockState = this.H(b2);
                Block block = iBlockState.df();
                if ((!bl3 || block.a(this, b2, iBlockState) != null) && block.a(iBlockState, bl2) && (ce2 = block.a(this, b2, da2, da3)) != null) {
                    return ce2;
                }
                ce2 = null;
                int n8 = 200;
                while (n8-- >= 0) {
                    ao ao2;
                    if (Double.isNaN(da2.dzY) || Double.isNaN(da2.dzZ) || Double.isNaN(da2.dAa)) {
                        return null;
                    }
                    if (n5 == n2 && n6 == n3 && n7 == n4) {
                        return bl4 ? ce2 : null;
                    }
                    boolean bl5 = true;
                    boolean bl6 = true;
                    boolean bl7 = true;
                    double d2 = 999.0;
                    double d3 = 999.0;
                    double d4 = 999.0;
                    if (n2 > n5) {
                        d2 = (double)n5 + 1.0;
                    } else if (n2 < n5) {
                        d2 = (double)n5 + 0.0;
                    } else {
                        bl5 = false;
                    }
                    if (n3 > n6) {
                        d3 = (double)n6 + 1.0;
                    } else if (n3 < n6) {
                        d3 = (double)n6 + 0.0;
                    } else {
                        bl6 = false;
                    }
                    if (n4 > n7) {
                        d4 = (double)n7 + 1.0;
                    } else if (n4 < n7) {
                        d4 = (double)n7 + 0.0;
                    } else {
                        bl7 = false;
                    }
                    double d5 = 999.0;
                    double d6 = 999.0;
                    double d7 = 999.0;
                    double d8 = da3.dzY - da2.dzY;
                    double d9 = da3.dzZ - da2.dzZ;
                    double d10 = da3.dAa - da2.dAa;
                    if (bl5) {
                        d5 = (d2 - da2.dzY) / d8;
                    }
                    if (bl6) {
                        d6 = (d3 - da2.dzZ) / d9;
                    }
                    if (bl7) {
                        d7 = (d4 - da2.dAa) / d10;
                    }
                    if (d5 == -0.0) {
                        d5 = -1.0E-4;
                    }
                    if (d6 == -0.0) {
                        d6 = -1.0E-4;
                    }
                    if (d7 == -0.0) {
                        d7 = -1.0E-4;
                    }
                    if (d5 < d6 && d5 < d7) {
                        ao2 = n2 > n5 ? ao.dvM : ao.dvN;
                        da2 = new da(d2, da2.dzZ + d9 * d5, da2.dAa + d10 * d5);
                    } else if (d6 < d7) {
                        ao2 = n3 > n6 ? ao.dvI : ao.dvJ;
                        da2 = new da(da2.dzY + d8 * d6, d3, da2.dAa + d10 * d6);
                    } else {
                        ao2 = n4 > n7 ? ao.dvK : ao.dvL;
                        da2 = new da(da2.dzY + d8 * d7, da2.dzZ + d9 * d7, d4);
                    }
                    n5 = bt.aF(da2.dzY) - (ao2 == ao.dvN ? 1 : 0);
                    n6 = bt.aF(da2.dzZ) - (ao2 == ao.dvJ ? 1 : 0);
                    n7 = bt.aF(da2.dAa) - (ao2 == ao.dvL ? 1 : 0);
                    b2 = new net.minecraft.util.b(n5, n6, n7);
                    IBlockState iBlockState2 = this.H(b2);
                    Block block2 = iBlockState2.df();
                    if (bl3 && block2.a(this, b2, iBlockState2) == null) continue;
                    if (block2.a(iBlockState2, bl2)) {
                        ce ce3 = block2.a(this, b2, da2, da3);
                        if (ce3 == null) continue;
                        return ce3;
                    }
                    ce2 = new ce(cf.dyP, da2, ao2, b2);
                }
                return bl4 ? ce2 : null;
            }
            return null;
        }
        return null;
    }

    public void a(net.minecraft.entity.c c2, String string, float f2, float f3) {
        for (int i2 = 0; i2 < this.dCE.size(); ++i2) {
            this.dCE.get(i2).a(string, c2.distance, c2.gX, c2.gY, f2, f3);
        }
    }

    public void a(net.minecraft.entity.player.a a2, String string, float f2, float f3) {
        for (int i2 = 0; i2 < this.dCE.size(); ++i2) {
            this.dCE.get(i2).a(a2, string, a2.distance, a2.gX, a2.gY, f2, f3);
        }
    }

    public void a(double d2, double d3, double d4, String string, float f2, float f3) {
        for (int i2 = 0; i2 < this.dCE.size(); ++i2) {
            this.dCE.get(i2).a(string, d2, d3, d4, f2, f3);
        }
    }

    public void a(double d2, double d3, double d4, String string, float f2, float f3, boolean bl2) {
    }

    public void a(net.minecraft.util.b b2, String string) {
        for (int i2 = 0; i2 < this.dCE.size(); ++i2) {
            this.dCE.get(i2).a(string, b2);
        }
    }

    public void a(as as2, double d2, double d3, double d4, double d5, double d6, double d7, int ... nArray) {
        this.a(as2.asK(), as2.asM(), d2, d3, d4, d5, d6, d7, nArray);
    }

    public void a(as as2, boolean bl2, double d2, double d3, double d4, double d5, double d6, double d7, int ... nArray) {
        this.a(as2.asK(), as2.asM() | bl2, d2, d3, d4, d5, d6, d7, nArray);
    }

    private void a(int n2, boolean bl2, double d2, double d3, double d4, double d5, double d6, double d7, int ... nArray) {
        for (int i2 = 0; i2 < this.dCE.size(); ++i2) {
            this.dCE.get(i2).a(n2, bl2, d2, d3, d4, d5, d6, d7, nArray);
        }
    }

    public boolean ay(net.minecraft.entity.c c2) {
        this.dCr.add(c2);
        return true;
    }

    public boolean G(net.minecraft.entity.c c2) {
        int n2 = bt.aF(c2.distance / 16.0);
        int n3 = bt.aF(c2.gY / 16.0);
        boolean bl2 = c2.cqV;
        if (c2 instanceof net.minecraft.entity.player.a) {
            bl2 = true;
        }
        if (!bl2 && !this.e(n2, n3, true)) {
            return false;
        }
        if (c2 instanceof net.minecraft.entity.player.a) {
            net.minecraft.entity.player.a a2 = (net.minecraft.entity.player.a)c2;
            this.dCq.add(a2);
            this.avu();
        }
        this.aL(n2, n3).aE(c2);
        this.dCk.add(c2);
        this.I(c2);
        return true;
    }

    protected void I(net.minecraft.entity.c c2) {
        for (int i2 = 0; i2 < this.dCE.size(); ++i2) {
            this.dCE.get(i2).I(c2);
        }
    }

    protected void J(net.minecraft.entity.c c2) {
        for (int i2 = 0; i2 < this.dCE.size(); ++i2) {
            this.dCE.get(i2).J(c2);
        }
    }

    public void H(net.minecraft.entity.c c2) {
        if (c2.cqS != null) {
            c2.cqS.D(null);
        }
        if (c2.riding != null) {
            c2.D(null);
        }
        c2.Tf();
        if (c2 instanceof net.minecraft.entity.player.a) {
            this.dCq.remove(c2);
            this.avu();
            this.J(c2);
        }
    }

    public void az(net.minecraft.entity.c c2) {
        c2.Tf();
        if (c2 instanceof net.minecraft.entity.player.a) {
            this.dCq.remove(c2);
            this.avu();
        }
        int n2 = c2.crZ;
        int n3 = c2.csb;
        if (c2.crY && this.e(n2, n3, true)) {
            this.aL(n2, n3).H(c2);
        }
        this.dCk.remove(c2);
        this.J(c2);
    }

    public void a(net.minecraft.world.o o2) {
        this.dCE.add(o2);
    }

    public void b(net.minecraft.world.o o2) {
        this.dCE.remove(o2);
    }

    public List<net.minecraft.util.a> a(net.minecraft.entity.c c2, net.minecraft.util.a a2) {
        ArrayList<net.minecraft.util.a> arrayList = Lists.newArrayList();
        int n2 = bt.aF(a2.dsZ);
        int n3 = bt.aF(a2.dtc + 1.0);
        int n4 = bt.aF(a2.dta);
        int n5 = bt.aF(a2.dtd + 1.0);
        int n6 = bt.aF(a2.dtb);
        int n7 = bt.aF(a2.dte + 1.0);
        c c3 = this.Sh();
        boolean bl2 = c2.Uf();
        boolean bl3 = this.a(c3, c2);
        IBlockState iBlockState = Blocks.cKv.xy();
        net.minecraft.util.g g2 = new net.minecraft.util.g();
        for (int i2 = n2; i2 < n3; ++i2) {
            for (int i3 = n6; i3 < n7; ++i3) {
                if (!this.aF(g2.E(i2, 64, i3))) continue;
                for (int i4 = n4 - 1; i4 < n5; ++i4) {
                    g2.E(i2, i4, i3);
                    if (bl2 && bl3) {
                        c2.aG(false);
                    } else if (!bl2 && !bl3) {
                        c2.aG(true);
                    }
                    IBlockState iBlockState2 = iBlockState;
                    if (c3.bi(g2) || !bl3) {
                        iBlockState2 = this.H(g2);
                    }
                    iBlockState2.df().a(this, g2, iBlockState2, a2, arrayList, c2);
                }
            }
        }
        double d2 = 0.25;
        if (c2 == w.mc.thePlayer) {
            List<net.minecraft.entity.c> list = this.b(c2, a2.ad(0.25, 0.25, 0.25));
            for (int i5 = 0; i5 < list.size(); ++i5) {
                if (c2.cqS == list || c2.riding == list) continue;
                net.minecraft.util.a a3 = list.get(i5).Ip();
                if (a3 != null && a3.q(a2)) {
                    arrayList.add(a3);
                }
                if ((a3 = c2.V(list.get(i5))) == null || !a3.q(a2)) continue;
                arrayList.add(a3);
            }
        }
        return arrayList;
    }

    public boolean a(c c2, net.minecraft.entity.c c3) {
        double d2 = c2.awY();
        double d3 = c2.awZ();
        double d4 = c2.axa();
        double d5 = c2.axb();
        if (c3.Uf()) {
            d2 += 1.0;
            d3 += 1.0;
            d4 -= 1.0;
            d5 -= 1.0;
        } else {
            d2 -= 1.0;
            d3 -= 1.0;
            d4 += 1.0;
            d5 += 1.0;
        }
        return c3.distance > d2 && c3.distance < d4 && c3.gY > d3 && c3.gY < d5;
    }

    public List<net.minecraft.util.a> r(net.minecraft.util.a a2) {
        ArrayList<net.minecraft.util.a> arrayList = Lists.newArrayList();
        int n2 = bt.aF(a2.dsZ);
        int n3 = bt.aF(a2.dtc + 1.0);
        int n4 = bt.aF(a2.dta);
        int n5 = bt.aF(a2.dtd + 1.0);
        int n6 = bt.aF(a2.dtb);
        int n7 = bt.aF(a2.dte + 1.0);
        net.minecraft.util.g g2 = new net.minecraft.util.g();
        for (int i2 = n2; i2 < n3; ++i2) {
            for (int i3 = n6; i3 < n7; ++i3) {
                if (!this.aF(g2.E(i2, 64, i3))) continue;
                for (int i4 = n4 - 1; i4 < n5; ++i4) {
                    g2.E(i2, i4, i3);
                    IBlockState iBlockState = i2 >= -30000000 && i2 < 30000000 && i3 >= -30000000 && i3 < 30000000 ? this.H(g2) : Blocks.cKB.xy();
                    iBlockState.df().a(this, g2, iBlockState, a2, arrayList, null);
                }
            }
        }
        return arrayList;
    }

    public int bR(float f2) {
        float f3 = this.bT(f2);
        float f4 = 1.0f - (bt.bB(f3 * (float)Math.PI * 2.0f) * 2.0f + 0.5f);
        f4 = bt.I(f4, 0.0f, 1.0f);
        f4 = 1.0f - f4;
        f4 = (float)((double)f4 * (1.0 - (double)(this.ca(f2) * 5.0f) / 16.0));
        f4 = (float)((double)f4 * (1.0 - (double)(this.bY(f2) * 5.0f) / 16.0));
        f4 = 1.0f - f4;
        return (int)(f4 * 11.0f);
    }

    public float bS(float f2) {
        float f3 = this.bT(f2);
        float f4 = 1.0f - (bt.bB(f3 * (float)Math.PI * 2.0f) * 2.0f + 0.2f);
        f4 = bt.I(f4, 0.0f, 1.0f);
        f4 = 1.0f - f4;
        f4 = (float)((double)f4 * (1.0 - (double)(this.ca(f2) * 5.0f) / 16.0));
        f4 = (float)((double)f4 * (1.0 - (double)(this.bY(f2) * 5.0f) / 16.0));
        return f4 * 0.8f + 0.2f;
    }

    public da i(net.minecraft.entity.c c2, float f2) {
        float f3;
        float f4;
        float f5 = this.bT(f2);
        float f6 = bt.bB(f5 * (float)Math.PI * 2.0f) * 2.0f + 0.5f;
        f6 = bt.I(f6, 0.0f, 1.0f);
        int n2 = bt.aF(c2.distance);
        int n3 = bt.aF(c2.gX + (double)c2.getEyeHeight());
        int n4 = bt.aF(c2.gY);
        net.minecraft.util.b b2 = new net.minecraft.util.b(n2, n3, n4);
        j j2 = this.aB(b2);
        float f7 = j2.be(b2);
        int n5 = j2.cc(f7);
        float f8 = (float)(n5 >> 16 & 0xFF) / 255.0f;
        float f9 = (float)(n5 >> 8 & 0xFF) / 255.0f;
        float f10 = (float)(n5 & 0xFF) / 255.0f;
        f8 *= f6;
        f9 *= f6;
        f10 *= f6;
        float f11 = this.ca(f2);
        if (f11 > 0.0f) {
            f4 = (f8 * 0.3f + f9 * 0.59f + f10 * 0.11f) * 0.6f;
            f3 = 1.0f - f11 * 0.75f;
            f8 = f8 * f3 + f4 * (1.0f - f3);
            f9 = f9 * f3 + f4 * (1.0f - f3);
            f10 = f10 * f3 + f4 * (1.0f - f3);
        }
        if ((f4 = this.bY(f2)) > 0.0f) {
            f3 = (f8 * 0.3f + f9 * 0.59f + f10 * 0.11f) * 0.2f;
            float f12 = 1.0f - f4 * 0.75f;
            f8 = f8 * f12 + f3 * (1.0f - f12);
            f9 = f9 * f12 + f3 * (1.0f - f12);
            f10 = f10 * f12 + f3 * (1.0f - f12);
        }
        if (this.dCB > 0) {
            f3 = (float)this.dCB - f2;
            if (f3 > 1.0f) {
                f3 = 1.0f;
            }
            f8 = f8 * (1.0f - (f3 *= 0.45f)) + 0.8f * f3;
            f9 = f9 * (1.0f - f3) + 0.8f * f3;
            f10 = f10 * (1.0f - f3) + 1.0f * f3;
        }
        return new da(f8, f9, f10);
    }

    public float bT(float f2) {
        return this.dCD.a(this.totalWorldTime.getWorldTime(), f2);
    }

    public int ave() {
        return this.dCD.J(this.totalWorldTime.getWorldTime());
    }

    public float avf() {
        return ac.dDc[this.dCD.J(this.totalWorldTime.getWorldTime())];
    }

    public float bU(float f2) {
        float f3 = this.bT(f2);
        return f3 * (float)Math.PI * 2.0f;
    }

    public da bV(float f2) {
        float f3;
        float f4;
        float f5 = this.bT(f2);
        float f6 = bt.bB(f5 * (float)Math.PI * 2.0f) * 2.0f + 0.5f;
        f6 = bt.I(f6, 0.0f, 1.0f);
        float f7 = (float)(this.dCt >> 16 & 0xFFL) / 255.0f;
        float f8 = (float)(this.dCt >> 8 & 0xFFL) / 255.0f;
        float f9 = (float)(this.dCt & 0xFFL) / 255.0f;
        float f10 = this.ca(f2);
        if (f10 > 0.0f) {
            f4 = (f7 * 0.3f + f8 * 0.59f + f9 * 0.11f) * 0.6f;
            f3 = 1.0f - f10 * 0.95f;
            f7 = f7 * f3 + f4 * (1.0f - f3);
            f8 = f8 * f3 + f4 * (1.0f - f3);
            f9 = f9 * f3 + f4 * (1.0f - f3);
        }
        f7 *= f6 * 0.9f + 0.1f;
        f8 *= f6 * 0.9f + 0.1f;
        f9 *= f6 * 0.85f + 0.15f;
        f4 = this.bY(f2);
        if (f4 > 0.0f) {
            f3 = (f7 * 0.3f + f8 * 0.59f + f9 * 0.11f) * 0.2f;
            float f11 = 1.0f - f4 * 0.95f;
            f7 = f7 * f11 + f3 * (1.0f - f11);
            f8 = f8 * f11 + f3 * (1.0f - f11);
            f9 = f9 * f11 + f3 * (1.0f - f11);
        }
        return new da(f7, f8, f9);
    }

    public da bW(float f2) {
        float f3 = this.bT(f2);
        return this.dCD.A(f3, f2);
    }

    public net.minecraft.util.b aO(net.minecraft.util.b b2) {
        return this.aG(b2).aO(b2);
    }

    public net.minecraft.util.b aP(net.minecraft.util.b b2) {
        net.minecraft.util.b b3;
        Material material;
        a a2 = this.aG(b2);
        net.minecraft.util.b b4 = new net.minecraft.util.b(b2.wf(), a2.axh() + 16, b2.wj());
        while (!(b4.wg() < 0 || (material = a2.bl(b3 = b4.aqK()).wN()).zk() && material != Material.bdO)) {
            b4 = b3;
        }
        return b4;
    }

    public float bX(float f2) {
        float f3 = this.bT(f2);
        float f4 = 1.0f - (bt.bB(f3 * (float)Math.PI * 2.0f) * 2.0f + 0.25f);
        f4 = bt.I(f4, 0.0f, 1.0f);
        return f4 * f4 * 0.5f;
    }

    public void a(net.minecraft.util.b b2, Block block, int n2) {
    }

    public void a(net.minecraft.util.b b2, Block block, int n2, int n3) {
    }

    public void b(net.minecraft.util.b b2, Block block, int n2, int n3) {
    }

    public void avg() {
        this.dCL.eY("entities");
        FullBright fullBright = this.e(FullBright.class);
        if (fullBright == null || !fullBright.isEnabled()) {
            this.dCB = Math.max(0, this.dCB - 1);
        }
        this.dCL.eY("global");
        for (int i2 = 0; i2 < this.dCr.size(); ++i2) {
            net.minecraft.entity.c object2 = this.dCr.get(i2);
            try {
                ++object2.aQx;
                ++object2.ae;
                ++object2.aY;
                ++object2.crK;
                ++object2.Zl;
                object2.CY();
            }
            catch (Throwable throwable) {
                net.minecraft.crash.a a2 = net.minecraft.crash.a.a(throwable, "Ticking entity");
                net.minecraft.crash.i i3 = a2.dY("Entity being ticked");
                if (object2 == null) {
                    i3.b("Entity", "~~NULL~~");
                } else {
                    object2.a(i3);
                }
                throw new cl(a2);
            }
            if (!object2.crt) continue;
            this.dCr.remove(i2--);
        }
        this.dCL.fa("remove");
        this.dCk.removeAll(this.dCl);
        for (net.minecraft.entity.c c2 : this.dCl) {
            if (c2 == null) continue;
            int n2 = c2.crZ;
            int n3 = c2.csb;
            if (!c2.crY || !this.e(n2, n3, true)) continue;
            this.aL(n2, n3).H(c2);
        }
        for (net.minecraft.entity.c c3 : this.dCl) {
            this.J(c3);
        }
        this.dCl.clear();
        this.dCL.fa("regular");
        for (net.minecraft.entity.c c4 : this.dCk) {
            if (c4 == null || w.mc.thePlayer == null) continue;
            this.aC(c4);
        }
        this.dCL.fa("blockEntities");
        this.dCT = true;
        Iterator<Object> iterator = this.dCn.iterator();
        for (net.minecraft.entity.c c5 : this.dCk) {
            if (c5 == null || w.mc.thePlayer == null) continue;
            if (c5 instanceof net.minecraft.entity.item.i) {
                c5.crl = true;
                continue;
            }
            if (c5 == w.mc.thePlayer || c5.isInvisible()) {
                c5.crl = false;
                continue;
            }
            c5.crl = aef.t(c5);
            c5.crF = w.mc.thePlayer.T(c5);
            if (c5.crl) {
                c5.crm = 0;
                continue;
            }
            ++c5.crm;
        }
        while (iterator.hasNext()) {
            net.minecraft.util.b b2;
            net.minecraft.tileentity.d d2 = (net.minecraft.tileentity.d)iterator.next();
            if (!d2.apD() && d2.apA() && this.aF(b2 = d2.zN()) && this.dCU.bi(b2)) {
                try {
                    ((bi)((Object)d2)).ju();
                }
                catch (Throwable throwable) {
                    net.minecraft.crash.a a3 = net.minecraft.crash.a.a(throwable, "Ticking block entity");
                    net.minecraft.crash.i i4 = a3.dY("Block entity being ticked");
                    d2.b(i4);
                    throw new cl(a3);
                }
            }
            if (!d2.apD()) continue;
            iterator.remove();
            this.dCm.remove(d2);
            if (!this.aF(d2.zN())) continue;
            this.aG(d2.zN()).aQ(d2.zN());
        }
        this.dCT = false;
        if (!this.dCp.isEmpty()) {
            this.dCn.removeAll(this.dCp);
            this.dCm.removeAll(this.dCp);
            this.dCp.clear();
        }
        this.dCL.fa("pendingBlockEntities");
        if (!this.dCo.isEmpty()) {
            for (net.minecraft.tileentity.d d3 : this.dCo) {
                if (d3.apD()) continue;
                if (!this.dCm.contains(d3)) {
                    this.f(d3);
                }
                if (this.aF(d3.zN())) {
                    this.aG(d3.zN()).c(d3.zN(), d3);
                }
                this.L(d3.zN());
            }
            this.dCo.clear();
        }
        this.dCL.ajh();
        this.dCL.ajh();
    }

    public boolean f(net.minecraft.tileentity.d d2) {
        boolean bl2 = this.dCm.add(d2);
        if (bl2 && d2 instanceof bi) {
            this.dCn.add(d2);
        }
        return bl2;
    }

    public void g(Collection<net.minecraft.tileentity.d> collection) {
        if (this.dCT) {
            this.dCo.addAll(collection);
        } else {
            for (net.minecraft.tileentity.d d2 : collection) {
                this.dCm.add(d2);
                if (!(d2 instanceof bi)) continue;
                this.dCn.add(d2);
            }
        }
    }

    public void aA(net.minecraft.entity.c c2) {
        this.d(c2, true);
    }

    public void d(net.minecraft.entity.c c2, boolean bl2) {
        int n2 = bt.aF(c2.distance);
        int n3 = bt.aF(c2.gY);
        int n4 = 32;
        if (!bl2 || this.a(n2 - 32, 0, n3 - 32, n2 + 32, 0, n3 + 32, true)) {
            c2.crC = c2.crz;
            c2.crD = c2.crA;
            c2.crE = c2.crB;
            c2.crz = c2.distance;
            c2.crA = c2.gX;
            c2.crB = c2.gY;
            c2.cre = c2.pl;
            c2.crg = c2.pm;
            if (bl2 && c2.crY) {
                ++c2.ae;
                ++c2.aQx;
                ++c2.crJ;
                ++c2.crK;
                ++c2.Zl;
                ++c2.aY;
                ++c2.crL;
                if (c2.aie.T(60L)) {
                    c2.fX = true;
                    c2.aie.aX();
                }
                if (c2.cst.T(60L)) {
                    c2.cri = true;
                    c2.cst.aX();
                }
                if (c2.riding != null) {
                    c2.TG();
                } else {
                    c2.CY();
                }
            }
            this.dCL.eY("chunkCheck");
            if (Double.isNaN(c2.distance) || Double.isInfinite(c2.distance)) {
                c2.distance = c2.crz;
            }
            if (Double.isNaN(c2.gX) || Double.isInfinite(c2.gX)) {
                c2.gX = c2.crA;
            }
            if (Double.isNaN(c2.gY) || Double.isInfinite(c2.gY)) {
                c2.gY = c2.crB;
            }
            if (Double.isNaN(c2.pm) || Double.isInfinite(c2.pm)) {
                c2.pm = c2.crg;
            }
            if (Double.isNaN(c2.pl) || Double.isInfinite(c2.pl)) {
                c2.pl = c2.cre;
            }
            int n5 = bt.aF(c2.distance / 16.0);
            int n6 = bt.aF(c2.gX / 16.0);
            int n7 = bt.aF(c2.gY / 16.0);
            if (!c2.crY || c2.crZ != n5 || c2.csa != n6 || c2.csb != n7) {
                if (c2.crY && this.e(c2.crZ, c2.csb, true)) {
                    this.aL(c2.crZ, c2.csb).e(c2, c2.csa);
                }
                if (this.e(n5, n7, true)) {
                    c2.crY = true;
                    this.aL(n5, n7).aE(c2);
                } else {
                    c2.crY = false;
                }
            }
            this.dCL.ajh();
            if (bl2 && c2.crY && c2.cqS != null) {
                if (!c2.cqS.crt && c2.cqS.riding == c2) {
                    this.aA(c2.cqS);
                } else {
                    c2.cqS.riding = null;
                    c2.cqS = null;
                }
            }
        }
    }

    public boolean s(net.minecraft.util.a a2) {
        return this.a(a2, null);
    }

    public boolean a(net.minecraft.util.a a2, net.minecraft.entity.c c2) {
        List<net.minecraft.entity.c> list = this.b((net.minecraft.entity.c)null, a2);
        for (int i2 = 0; i2 < list.size(); ++i2) {
            net.minecraft.entity.c c3 = list.get(i2);
            if (c3.crt || !c3.cqR || c3 == c2 || c2 != null && (c2.riding == c3 || c2.cqS == c3)) continue;
            return false;
        }
        return true;
    }

    public boolean t(net.minecraft.util.a a2) {
        int n2 = bt.aF(a2.dsZ);
        int n3 = bt.aF(a2.dtc);
        int n4 = bt.aF(a2.dta);
        int n5 = bt.aF(a2.dtd);
        int n6 = bt.aF(a2.dtb);
        int n7 = bt.aF(a2.dte);
        net.minecraft.util.g g2 = new net.minecraft.util.g();
        for (int i2 = n2; i2 <= n3; ++i2) {
            for (int i3 = n4; i3 <= n5; ++i3) {
                for (int i4 = n6; i4 <= n7; ++i4) {
                    Block block = this.H(g2.E(i2, i3, i4)).df();
                    if (block.wN() == Material.bdF) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean u(net.minecraft.util.a a2) {
        int n2 = bt.aF(a2.dsZ);
        int n3 = bt.aF(a2.dtc);
        int n4 = bt.aF(a2.dta);
        int n5 = bt.aF(a2.dtd);
        int n6 = bt.aF(a2.dtb);
        int n7 = bt.aF(a2.dte);
        net.minecraft.util.g g2 = new net.minecraft.util.g();
        for (int i2 = n2; i2 <= n3; ++i2) {
            for (int i3 = n4; i3 <= n5; ++i3) {
                for (int i4 = n6; i4 <= n7; ++i4) {
                    Block block = this.H(g2.E(i2, i3, i4)).df();
                    if (!block.wN().zh()) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean v(net.minecraft.util.a a2) {
        int n2;
        int n3 = bt.aF(a2.dsZ);
        int n4 = bt.aF(a2.dtc + 1.0);
        int n5 = bt.aF(a2.dta);
        int n6 = bt.aF(a2.dtd + 1.0);
        int n7 = bt.aF(a2.dtb);
        if (this.a(n3, n5, n7, n4, n6, n2 = bt.aF(a2.dte + 1.0), true)) {
            net.minecraft.util.g g2 = new net.minecraft.util.g();
            for (int i2 = n3; i2 < n4; ++i2) {
                for (int i3 = n5; i3 < n6; ++i3) {
                    for (int i4 = n7; i4 < n2; ++i4) {
                        Block block = this.H(g2.E(i2, i3, i4)).df();
                        if (block != Blocks.cLv && block != Blocks.cKE && block != Blocks.cKF) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static int a(boolean bl2, double d2) {
        return bl2 ? bt.aI(d2) : bt.aF(d2 + 1.0);
    }

    private static da d(da da2, double d2) {
        return new da(da2.dzY * d2, da2.dzZ * d2, da2.dAa * d2);
    }

    public boolean a(net.minecraft.util.a a2, Material material, net.minecraft.entity.c c2, double d2) {
        int n2;
        boolean bl2 = ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_13);
        int n3 = bt.aF(a2.dsZ);
        int n4 = w.a(bl2, a2.dtc);
        int n5 = bt.aF(a2.dta);
        int n6 = w.a(bl2, a2.dtd);
        int n7 = bt.aF(a2.dtb);
        if (!this.a(n3, n5, n7, n4, n6, n2 = w.a(bl2, a2.dte), true)) {
            return false;
        }
        boolean bl3 = false;
        double d3 = 0.0;
        int n8 = 0;
        da da2 = new da(0.0, 0.0, 0.0);
        net.minecraft.util.g g2 = new net.minecraft.util.g();
        for (int i2 = n3; i2 < n4; ++i2) {
            for (int i3 = n5; i3 < n6; ++i3) {
                for (int i4 = n7; i4 < n2; ++i4) {
                    g2.E(i2, i3, i4);
                    IBlockState iBlockState = this.H(g2);
                    Block block = iBlockState.df();
                    if (block.wN() != material) continue;
                    double d4 = (float)(i3 + 1) - BlockLiquid.bb(iBlockState.b(BlockLiquid.aWm));
                    if (!(bl2 ? d4 >= a2.dta : (double)n6 >= d4)) continue;
                    bl3 = true;
                    d3 = Math.max(d4 - a2.dta, d3);
                    da da3 = ((BlockLiquid)block).n(this, g2);
                    if (bl2 && d3 < 0.4) {
                        da3 = w.d(da3, d3);
                    }
                    da2 = da2.w(da3);
                    ++n8;
                }
            }
        }
        double d5 = bl2 ? Math.sqrt(da2.t(da2)) : da2.atE();
        if (d5 > 0.0 && c2.TY()) {
            if (bl2 && n8 > 0) {
                double d6 = 1.0 / (double)n8;
                da2 = w.d(da2, d6);
            }
            if (!bl2) {
                da2 = da2.atD();
            }
            da2 = w.d(da2, d2);
            if (bl2) {
                double d7 = 0.003;
                double d8 = d7 * 1.5;
                if (Math.abs(c2.ud) < d7 && Math.abs(c2.uf) < d7 && Math.sqrt(da2.t(da2)) < d8) {
                    da2 = w.d(da2.atD(), d8);
                }
            }
            c2.ud += da2.dzY;
            c2.ue += da2.dzZ;
            c2.uf += da2.dAa;
        }
        if (material == Material.bdM) {
            c2.csu = d3;
        }
        if (material == Material.bdN) {
            c2.csv = d3;
        }
        return bl3;
    }

    public boolean b(net.minecraft.util.a a2, Material material) {
        int n2 = bt.aF(a2.dsZ);
        int n3 = bt.aF(a2.dtc + 1.0);
        int n4 = bt.aF(a2.dta);
        int n5 = bt.aF(a2.dtd + 1.0);
        int n6 = bt.aF(a2.dtb);
        int n7 = bt.aF(a2.dte + 1.0);
        net.minecraft.util.g g2 = new net.minecraft.util.g();
        for (int i2 = n2; i2 < n3; ++i2) {
            for (int i3 = n4; i3 < n5; ++i3) {
                for (int i4 = n6; i4 < n7; ++i4) {
                    if (this.H(g2.E(i2, i3, i4)).df().wN() != material) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean c(net.minecraft.util.a a2, Material material) {
        int n2 = bt.aF(a2.dsZ);
        int n3 = bt.aF(a2.dtc + 1.0);
        int n4 = bt.aF(a2.dta);
        int n5 = bt.aF(a2.dtd + 1.0);
        int n6 = bt.aF(a2.dtb);
        int n7 = bt.aF(a2.dte + 1.0);
        net.minecraft.util.g g2 = new net.minecraft.util.g();
        for (int i2 = n2; i2 < n3; ++i2) {
            for (int i3 = n4; i3 < n5; ++i3) {
                for (int i4 = n6; i4 < n7; ++i4) {
                    IBlockState iBlockState = this.H(g2.E(i2, i3, i4));
                    Block block = iBlockState.df();
                    if (block.wN() != material) continue;
                    int n8 = iBlockState.b(BlockLiquid.aWm);
                    double d2 = i3 + 1;
                    if (n8 < 8) {
                        d2 = (double)(i3 + 1) - (double)n8 / 8.0;
                    }
                    if (!(d2 >= a2.dta)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public net.minecraft.world.h a(net.minecraft.entity.c c2, double d2, double d3, double d4, float f2, boolean bl2) {
        return this.a(c2, d2, d3, d4, f2, false, bl2);
    }

    public net.minecraft.world.h a(net.minecraft.entity.c c2, double d2, double d3, double d4, float f2, boolean bl2, boolean bl3) {
        net.minecraft.world.h h2 = new net.minecraft.world.h(this, c2, d2, d3, d4, f2, bl2, bl3);
        h2.auP();
        h2.cs(true);
        return h2;
    }

    public float b(da da2, net.minecraft.util.a a2) {
        double d2 = 1.0 / ((a2.dtc - a2.dsZ) * 2.0 + 1.0);
        double d3 = 1.0 / ((a2.dtd - a2.dta) * 2.0 + 1.0);
        double d4 = 1.0 / ((a2.dte - a2.dtb) * 2.0 + 1.0);
        double d5 = (1.0 - Math.floor(1.0 / d2) * d2) / 2.0;
        double d6 = (1.0 - Math.floor(1.0 / d4) * d4) / 2.0;
        if (d2 >= 0.0 && d3 >= 0.0 && d4 >= 0.0) {
            int n2 = 0;
            int n3 = 0;
            float f2 = 0.0f;
            while (f2 <= 1.0f) {
                float f3 = 0.0f;
                while (f3 <= 1.0f) {
                    float f4 = 0.0f;
                    while (f4 <= 1.0f) {
                        double d7 = a2.dsZ + (a2.dtc - a2.dsZ) * (double)f2;
                        double d8 = a2.dta + (a2.dtd - a2.dta) * (double)f3;
                        double d9 = a2.dtb + (a2.dte - a2.dtb) * (double)f4;
                        if (this.d(new da(d7 + d5, d8, d9 + d6), da2) == null) {
                            ++n2;
                        }
                        ++n3;
                        f4 = (float)((double)f4 + d4);
                    }
                    f3 = (float)((double)f3 + d3);
                }
                f2 = (float)((double)f2 + d2);
            }
            return (float)n2 / (float)n3;
        }
        return 0.0f;
    }

    public boolean a(net.minecraft.entity.player.a a2, net.minecraft.util.b b2, ao ao2) {
        if (this.H(b2 = b2.x(ao2)).df() == Blocks.cLv) {
            this.b(a2, 1004, b2, 0);
            this.aH(b2);
            return true;
        }
        return false;
    }

    public String avh() {
        return "All: " + this.dCk.size();
    }

    public String avi() {
        return this.dCF.GY();
    }

    @Override
    public net.minecraft.tileentity.d G(net.minecraft.util.b b2) {
        net.minecraft.tileentity.d d2;
        int n2;
        if (!this.aE(b2)) {
            return null;
        }
        net.minecraft.tileentity.d d3 = null;
        if (this.dCT) {
            for (n2 = 0; n2 < this.dCo.size(); ++n2) {
                d2 = this.dCo.get(n2);
                if (d2.apD() || !d2.zN().equals(b2)) continue;
                d3 = d2;
                break;
            }
        }
        if (d3 == null) {
            d3 = this.aG(b2).a(b2, net.minecraft.world.chunk.e.dIx);
        }
        if (d3 == null) {
            for (n2 = 0; n2 < this.dCo.size(); ++n2) {
                d2 = this.dCo.get(n2);
                if (d2.apD() || !d2.zN().equals(b2)) continue;
                d3 = d2;
                break;
            }
        }
        return d3;
    }

    public void a(net.minecraft.util.b b2, net.minecraft.tileentity.d d2) {
        if (d2 != null && !d2.apD()) {
            if (this.dCT) {
                d2.al(b2);
                Iterator<net.minecraft.tileentity.d> iterator = this.dCo.iterator();
                while (iterator.hasNext()) {
                    try {
                        net.minecraft.tileentity.d d3 = iterator.next();
                        if (!d3.zN().equals(b2)) continue;
                        d3.apE();
                        iterator.remove();
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                this.dCo.add(d2);
            } else {
                this.f(d2);
                this.aG(b2).c(b2, d2);
            }
        }
    }

    public void aQ(net.minecraft.util.b b2) {
        net.minecraft.tileentity.d d2 = this.G(b2);
        if (d2 != null && this.dCT) {
            d2.apE();
            this.dCo.remove(d2);
        } else {
            if (d2 != null) {
                this.dCo.remove(d2);
                this.dCm.remove(d2);
                this.dCn.remove(d2);
            }
            this.aG(b2).aQ(b2);
        }
    }

    public void g(net.minecraft.tileentity.d d2) {
        this.dCp.add(d2);
    }

    public boolean aR(net.minecraft.util.b b2) {
        IBlockState iBlockState = this.H(b2);
        net.minecraft.util.a a2 = iBlockState.df().a(this, b2, iBlockState);
        return a2 != null && a2.aqH() >= 1.0;
    }

    public static boolean v(l l2, net.minecraft.util.b b2) {
        IBlockState iBlockState = l2.H(b2);
        Block block = iBlockState.df();
        return block.wN().isOpaque() && block.wR() || (block instanceof BlockStairs ? iBlockState.b(BlockStairs.bar) == BlockStairs$EnumHalf.bay : (block instanceof BlockSlab ? iBlockState.b(BlockSlab.baf) == BlockSlab$EnumBlockHalf.bag : block instanceof BlockHopper || block instanceof BlockSnow && iBlockState.b(BlockSnow.bak) == 7));
    }

    public boolean e(net.minecraft.util.b b2, boolean bl2) {
        if (!this.aE(b2)) {
            return bl2;
        }
        a a2 = this.dCF.D(b2);
        if (a2.isEmpty()) {
            return bl2;
        }
        Block block = this.H(b2).df();
        return block.wN().isOpaque() && block.wR();
    }

    public void avj() {
        int n2 = this.bR(1.0f);
        if (n2 != this.dCu) {
            this.dCu = n2;
        }
    }

    public void j(boolean bl2, boolean bl3) {
        this.dCR = bl2;
        this.dCS = bl3;
    }

    public void tick() {
        this.HC();
    }

    protected void avk() {
        if (this.totalWorldTime.avw()) {
            this.dCy = 1.0f;
            if (this.totalWorldTime.avv()) {
                this.dCA = 1.0f;
            }
        }
    }

    protected void HC() {
        if (!this.dCD.avR() && !this.dCO) {
            int n2;
            int n3 = this.totalWorldTime.azx();
            if (n3 > 0) {
                this.totalWorldTime.iS(--n3);
                this.totalWorldTime.iQ(this.totalWorldTime.avv() ? 1 : 2);
                this.totalWorldTime.iR(this.totalWorldTime.avw() ? 1 : 2);
            }
            if ((n2 = this.totalWorldTime.azf()) <= 0) {
                if (this.totalWorldTime.avv()) {
                    this.totalWorldTime.iQ(this.dCC.nextInt(12000) + 3600);
                } else {
                    this.totalWorldTime.iQ(this.dCC.nextInt(168000) + 12000);
                }
            } else {
                this.totalWorldTime.iQ(--n2);
                if (n2 <= 0) {
                    this.totalWorldTime.cF(!this.totalWorldTime.avv());
                }
            }
            this.dCz = this.dCA;
            this.dCA = this.totalWorldTime.avv() ? (float)((double)this.dCA + 0.01) : (float)((double)this.dCA - 0.01);
            this.dCA = bt.I(this.dCA, 0.0f, 1.0f);
            int n4 = this.totalWorldTime.azg();
            if (n4 <= 0) {
                if (this.totalWorldTime.avw()) {
                    this.totalWorldTime.iR(this.dCC.nextInt(12000) + 12000);
                } else {
                    this.totalWorldTime.iR(this.dCC.nextInt(168000) + 12000);
                }
            } else {
                this.totalWorldTime.iR(--n4);
                if (n4 <= 0) {
                    this.totalWorldTime.cG(!this.totalWorldTime.avw());
                }
            }
            this.dCx = this.dCy;
            this.dCy = this.totalWorldTime.avw() ? (float)((double)this.dCy + 0.01) : (float)((double)this.dCy - 0.01);
            this.dCy = bt.I(this.dCy, 0.0f, 1.0f);
        }
    }

    protected void avl() {
        int n2;
        int n3;
        int n4;
        net.minecraft.entity.player.a a2;
        int n5;
        this.dCP.clear();
        this.dCL.eY("buildList");
        for (n5 = 0; n5 < this.dCq.size(); ++n5) {
            a2 = this.dCq.get(n5);
            n4 = bt.aF(a2.distance / 16.0);
            n3 = bt.aF(a2.gY / 16.0);
            n2 = this.HD();
            for (int i2 = -n2; i2 <= n2; ++i2) {
                for (int i3 = -n2; i3 <= n2; ++i3) {
                    this.dCP.add(new b(i2 + n4, i3 + n3));
                }
            }
        }
        this.dCL.ajh();
        if (this.dCQ > 0) {
            --this.dCQ;
        }
        this.dCL.eY("playerCheckLight");
        if (!this.dCq.isEmpty()) {
            n5 = this.dCC.nextInt(this.dCq.size());
            a2 = this.dCq.get(n5);
            n4 = bt.aF(a2.distance) + this.dCC.nextInt(11) - 5;
            n3 = bt.aF(a2.gX) + this.dCC.nextInt(11) - 5;
            n2 = bt.aF(a2.gY) + this.dCC.nextInt(11) - 5;
            this.aV(new net.minecraft.util.b(n4, n3, n2));
        }
        this.dCL.ajh();
    }

    protected abstract int HD();

    protected void a(int n2, int n3, a a2) {
        this.dCL.fa("moodSound");
        if (this.dCQ == 0 && !this.dCO) {
            net.minecraft.entity.player.a a3;
            this.dCv = this.dCv * 3 + 1013904223;
            int n4 = this.dCv >> 2;
            int n5 = n4 & 0xF;
            int n6 = n4 >> 8 & 0xF;
            int n7 = n4 >> 16 & 0xFF;
            net.minecraft.util.b b2 = new net.minecraft.util.b(n5, n7, n6);
            Block block = a2.bl(b2);
            if (block.wN() == Material.bdF && this.aK(b2) <= this.dCC.nextInt(8) && this.b(g.dBr, b2) <= 0 && (a3 = this.o((double)(n5 += n2) + 0.5, (double)n7 + 0.5, (double)(n6 += n3) + 0.5, 8.0)) != null && a3.M((double)n5 + 0.5, (double)n7 + 0.5, (double)n6 + 0.5) > 4.0) {
                this.a((double)n5 + 0.5, (double)n7 + 0.5, (double)n6 + 0.5, "ambient.cave.cave", 0.7f, 0.8f + this.dCC.nextFloat() * 0.2f);
                this.dCQ = this.dCC.nextInt(12000) + 6000;
            }
        }
        this.dCL.fa("checkLight");
        a2.axr();
    }

    protected void HA() {
        this.avl();
    }

    public void a(Block block, net.minecraft.util.b b2, Random random) {
        this.dCj = true;
        block.b(this, b2, this.H(b2), random);
        this.dCj = false;
    }

    public boolean aS(net.minecraft.util.b b2) {
        return this.f(b2, false);
    }

    public boolean aT(net.minecraft.util.b b2) {
        return this.f(b2, true);
    }

    public boolean f(net.minecraft.util.b b2, boolean bl2) {
        IBlockState iBlockState;
        Block block;
        j j2 = this.aB(b2);
        float f2 = j2.be(b2);
        if (f2 > 0.15f) {
            return false;
        }
        if (b2.wg() >= 0 && b2.wg() < 256 && this.b(g.dBs, b2) < 10 && ((block = (iBlockState = this.H(b2)).df()) == Blocks.cKD || block == Blocks.cKC) && iBlockState.b(BlockLiquid.aWm) == 0) {
            if (!bl2) {
                return true;
            }
            boolean bl3 = this.aU(b2.aqN()) && this.aU(b2.aqO()) && this.aU(b2.aqL()) && this.aU(b2.aqM());
            return !bl3;
        }
        return false;
    }

    private boolean aU(net.minecraft.util.b b2) {
        return this.H(b2).df().wN() == Material.bdM;
    }

    public boolean g(net.minecraft.util.b b2, boolean bl2) {
        j j2 = this.aB(b2);
        float f2 = j2.be(b2);
        if (f2 > 0.15f) {
            return false;
        }
        if (!bl2) {
            return true;
        }
        if (b2.wg() >= 0 && b2.wg() < 256 && this.b(g.dBs, b2) < 10) {
            Block block = this.H(b2).df();
            return block.wN() == Material.bdF && Blocks.cMb.d(this, b2);
        }
        return false;
    }

    public boolean aV(net.minecraft.util.b b2) {
        boolean bl2 = false;
        if (!this.dCD.avR()) {
            bl2 |= this.d(g.dBr, b2);
        }
        return bl2 |= this.d(g.dBs, b2);
    }

    private int a(net.minecraft.util.b b2, g g2) {
        if (g2 == g.dBr && this.aI(b2)) {
            return 15;
        }
        Block block = this.H(b2).df();
        int n2 = g2 == g.dBr ? 0 : block.wL();
        int n3 = block.wJ();
        if (n3 >= 15 && block.wL() > 0) {
            n3 = 1;
        }
        if (n3 < 1) {
            n3 = 1;
        }
        if (n3 >= 15) {
            return 0;
        }
        if (n2 >= 14) {
            return n2;
        }
        for (ao ao2 : ao.values()) {
            net.minecraft.util.b b3 = b2.x(ao2);
            int n4 = this.b(g2, b3) - n3;
            if (n4 > n2) {
                n2 = n4;
            }
            if (n2 < 14) continue;
            return n2;
        }
        return n2;
    }

    public boolean d(g g2, net.minecraft.util.b b2) {
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        int n9;
        if (!this.a(b2, 17, false)) {
            return false;
        }
        int n10 = 0;
        int n11 = 0;
        this.dCL.eY("getBrightness");
        int n12 = this.b(g2, b2);
        int n13 = this.a(b2, g2);
        int n14 = b2.wf();
        int n15 = b2.wg();
        int n16 = b2.wj();
        if (n13 > n12) {
            this.dCV[n11++] = 133152;
        } else if (n13 < n12) {
            this.dCV[n11++] = 0x20820 | n12 << 18;
            while (n10 < n11) {
                n9 = this.dCV[n10++];
                n8 = (n9 & 0x3F) - 32 + n14;
                n7 = (n9 >> 6 & 0x3F) - 32 + n15;
                n6 = (n9 >> 12 & 0x3F) - 32 + n16;
                int n17 = n9 >> 18 & 0xF;
                net.minecraft.util.b b3 = new net.minecraft.util.b(n8, n7, n6);
                n5 = this.b(g2, b3);
                if (n5 != n17) continue;
                this.a(g2, b3, 0);
                if (n17 <= 0 || (n4 = bt.hN(n8 - n14)) + (n3 = bt.hN(n7 - n15)) + (n2 = bt.hN(n6 - n16)) >= 17) continue;
                net.minecraft.util.g g3 = new net.minecraft.util.g();
                for (ao ao2 : ao.values()) {
                    int n18 = n8 + ao2.ast();
                    int n19 = n7 + ao2.asu();
                    int n20 = n6 + ao2.asv();
                    g3.E(n18, n19, n20);
                    int n21 = Math.max(1, this.H(g3).df().wJ());
                    n5 = this.b(g2, (net.minecraft.util.b)g3);
                    if (n5 != n17 - n21 || n11 >= this.dCV.length) continue;
                    this.dCV[n11++] = n18 - n14 + 32 | n19 - n15 + 32 << 6 | n20 - n16 + 32 << 12 | n17 - n21 << 18;
                }
            }
            n10 = 0;
        }
        this.dCL.ajh();
        this.dCL.eY("checkedPosition < toCheckCount");
        while (n10 < n11) {
            boolean bl2;
            n9 = this.dCV[n10++];
            n8 = (n9 & 0x3F) - 32 + n14;
            n7 = (n9 >> 6 & 0x3F) - 32 + n15;
            n6 = (n9 >> 12 & 0x3F) - 32 + n16;
            net.minecraft.util.b b4 = new net.minecraft.util.b(n8, n7, n6);
            int n22 = this.b(g2, b4);
            n5 = this.a(b4, g2);
            if (n5 == n22) continue;
            this.a(g2, b4, n5);
            if (n5 <= n22) continue;
            n4 = Math.abs(n8 - n14);
            n3 = Math.abs(n7 - n15);
            n2 = Math.abs(n6 - n16);
            boolean bl3 = bl2 = n11 < this.dCV.length - 6;
            if (n4 + n3 + n2 >= 17 || !bl2) continue;
            if (this.b(g2, b4.aqN()) < n5) {
                this.dCV[n11++] = n8 - 1 - n14 + 32 + (n7 - n15 + 32 << 6) + (n6 - n16 + 32 << 12);
            }
            if (this.b(g2, b4.aqO()) < n5) {
                this.dCV[n11++] = n8 + 1 - n14 + 32 + (n7 - n15 + 32 << 6) + (n6 - n16 + 32 << 12);
            }
            if (this.b(g2, b4.aqK()) < n5) {
                this.dCV[n11++] = n8 - n14 + 32 + (n7 - 1 - n15 + 32 << 6) + (n6 - n16 + 32 << 12);
            }
            if (this.b(g2, b4.aqJ()) < n5) {
                this.dCV[n11++] = n8 - n14 + 32 + (n7 + 1 - n15 + 32 << 6) + (n6 - n16 + 32 << 12);
            }
            if (this.b(g2, b4.aqL()) < n5) {
                this.dCV[n11++] = n8 - n14 + 32 + (n7 - n15 + 32 << 6) + (n6 - 1 - n16 + 32 << 12);
            }
            if (this.b(g2, b4.aqM()) >= n5) continue;
            this.dCV[n11++] = n8 - n14 + 32 + (n7 - n15 + 32 << 6) + (n6 + 1 - n16 + 32 << 12);
        }
        this.dCL.ajh();
        return true;
    }

    public boolean ct(boolean bl2) {
        return false;
    }

    public List<s> a(a a2, boolean bl2) {
        return null;
    }

    public List<s> b(net.minecraft.world.gen.structure.w w2, boolean bl2) {
        return null;
    }

    public List<net.minecraft.entity.c> b(net.minecraft.entity.c c2, net.minecraft.util.a a2) {
        return this.a(c2, a2, net.minecraft.util.ah.dvb);
    }

    public List<net.minecraft.entity.c> a(net.minecraft.entity.c c2, net.minecraft.util.a a2, Predicate<? super net.minecraft.entity.c> predicate) {
        ArrayList<net.minecraft.entity.c> arrayList = Lists.newArrayList();
        int n2 = bt.aF((a2.dsZ - 2.0) / 16.0);
        int n3 = bt.aF((a2.dtc + 2.0) / 16.0);
        int n4 = bt.aF((a2.dtb - 2.0) / 16.0);
        int n5 = bt.aF((a2.dte + 2.0) / 16.0);
        try {
            for (int i2 = n2; i2 <= n3; ++i2) {
                for (int i3 = n4; i3 <= n5; ++i3) {
                    if (!this.e(i2, i3, true)) continue;
                    this.aL(i2, i3).a(c2, a2, arrayList, predicate);
                }
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return arrayList;
        }
        return arrayList;
    }

    public <T extends net.minecraft.entity.c> List<T> a(Class<? extends T> clazz, Predicate<? super T> predicate) {
        ArrayList<net.minecraft.entity.c> arrayList = Lists.newArrayList();
        for (net.minecraft.entity.c c2 : this.dCk) {
            if (!clazz.isAssignableFrom(c2.getClass()) || !predicate.apply(c2)) continue;
            arrayList.add(c2);
        }
        return arrayList;
    }

    public <T extends net.minecraft.entity.c> List<T> b(Class<? extends T> clazz, Predicate<? super T> predicate) {
        ArrayList<net.minecraft.entity.c> arrayList = Lists.newArrayList();
        for (net.minecraft.entity.c c2 : this.dCq) {
            if (!clazz.isAssignableFrom(c2.getClass()) || !predicate.apply(c2)) continue;
            arrayList.add(c2);
        }
        return arrayList;
    }

    public <T extends net.minecraft.entity.c> List<T> a(Class<? extends T> clazz, net.minecraft.util.a a2) {
        return this.a(clazz, a2, (T)((Object)net.minecraft.util.ah.dvb));
    }

    public <T extends net.minecraft.entity.c> List<T> a(Class<? extends T> clazz, net.minecraft.util.a a2, Predicate<? super T> predicate) {
        int n2 = bt.aF((a2.dsZ - 2.0) / 16.0);
        int n3 = bt.aF((a2.dtc + 2.0) / 16.0);
        int n4 = bt.aF((a2.dtb - 2.0) / 16.0);
        int n5 = bt.aF((a2.dte + 2.0) / 16.0);
        ArrayList arrayList = Lists.newArrayList();
        for (int i2 = n2; i2 <= n3; ++i2) {
            for (int i3 = n4; i3 <= n5; ++i3) {
                if (!this.e(i2, i3, true)) continue;
                this.aL(i2, i3).a(clazz, a2, arrayList, predicate);
            }
        }
        return arrayList;
    }

    public <T extends net.minecraft.entity.c> T a(Class<? extends T> clazz, net.minecraft.util.a a2, T t2) {
        List<T> list = this.a(clazz, a2);
        net.minecraft.entity.c c2 = null;
        double d2 = Double.MAX_VALUE;
        for (int i2 = 0; i2 < list.size(); ++i2) {
            double d3;
            net.minecraft.entity.c c3 = (net.minecraft.entity.c)list.get(i2);
            if (c3 == t2 || !net.minecraft.util.ah.dvb.apply(c3) || !((d3 = t2.T(c3)) <= d2)) continue;
            c2 = c3;
            d2 = d3;
        }
        return (T)c2;
    }

    public net.minecraft.entity.c cn(int n2) {
        return this.dCs.hE(n2);
    }

    public ConcurrentLinkedQueue<net.minecraft.entity.c> avm() {
        return this.dCk;
    }

    public void b(net.minecraft.util.b b2, net.minecraft.tileentity.d d2) {
        if (this.aF(b2)) {
            this.aG(b2).axm();
        }
    }

    public int r(Class<?> clazz) {
        int n2 = 0;
        for (net.minecraft.entity.c c2 : this.dCk) {
            if (c2 instanceof p && ((p)c2).Va() || !clazz.isAssignableFrom(c2.getClass())) continue;
            ++n2;
        }
        return n2;
    }

    public void h(Collection<net.minecraft.entity.c> collection) {
        this.dCk.addAll(collection);
        for (net.minecraft.entity.c c2 : collection) {
            this.I(c2);
        }
    }

    public void i(Collection<net.minecraft.entity.c> collection) {
        this.dCl.addAll(collection);
    }

    public boolean a(Block block, net.minecraft.util.b b2, boolean bl2, ao ao2, net.minecraft.entity.c c2, co co2) {
        Block block2 = this.H(b2).df();
        net.minecraft.util.a a2 = bl2 ? null : block.a(this, b2, block.xy());
        return (a2 == null || Minecraft.getMinecraft().thePlayer.crH || this.a(a2, c2)) && (block2.wN() == Material.bdV && block == Blocks.cNz || block2.wN().zq() && block.a(this, b2, ao2, co2));
    }

    public int avn() {
        return this.dCi;
    }

    public void id(int n2) {
        this.dCi = n2;
    }

    @Override
    public int i(net.minecraft.util.b b2, ao ao2) {
        IBlockState iBlockState = this.H(b2);
        return iBlockState.df().b((l)this, b2, iBlockState, ao2);
    }

    @Override
    public ap afZ() {
        return this.totalWorldTime.awo();
    }

    public int aW(net.minecraft.util.b b2) {
        int n2 = 0;
        if ((n2 = Math.max(n2, this.i(b2.aqK(), ao.dvI))) >= 15) {
            return n2;
        }
        if ((n2 = Math.max(n2, this.i(b2.aqJ(), ao.dvJ))) >= 15) {
            return n2;
        }
        if ((n2 = Math.max(n2, this.i(b2.aqL(), ao.dvK))) >= 15) {
            return n2;
        }
        if ((n2 = Math.max(n2, this.i(b2.aqM(), ao.dvL))) >= 15) {
            return n2;
        }
        if ((n2 = Math.max(n2, this.i(b2.aqN(), ao.dvM))) >= 15) {
            return n2;
        }
        n2 = Math.max(n2, this.i(b2.aqO(), ao.dvN));
        return n2;
    }

    public boolean j(net.minecraft.util.b b2, ao ao2) {
        return this.k(b2, ao2) > 0;
    }

    public int k(net.minecraft.util.b b2, ao ao2) {
        IBlockState iBlockState = this.H(b2);
        Block block = iBlockState.df();
        return block.wP() ? this.aW(b2) : block.a((l)this, b2, iBlockState, ao2);
    }

    public boolean aX(net.minecraft.util.b b2) {
        return this.k(b2.aqK(), ao.dvI) > 0 || this.k(b2.aqJ(), ao.dvJ) > 0 || this.k(b2.aqL(), ao.dvK) > 0 || this.k(b2.aqM(), ao.dvL) > 0 || this.k(b2.aqN(), ao.dvM) > 0 || this.k(b2.aqO(), ao.dvN) > 0;
    }

    public int aY(net.minecraft.util.b b2) {
        int n2 = 0;
        for (ao ao2 : ao.values()) {
            int n3 = this.k(b2.x(ao2), ao2);
            if (n3 >= 15) {
                return 15;
            }
            if (n3 <= n2) continue;
            n2 = n3;
        }
        return n2;
    }

    public net.minecraft.entity.player.a f(net.minecraft.entity.c c2, double d2) {
        return this.o(c2.distance, c2.gX, c2.gY, d2);
    }

    public net.minecraft.entity.player.a o(double d2, double d3, double d4, double d5) {
        double d6 = -1.0;
        net.minecraft.entity.player.a a2 = null;
        for (int i2 = 0; i2 < this.dCq.size(); ++i2) {
            net.minecraft.entity.player.a a3 = this.dCq.get(i2);
            if (!net.minecraft.util.ah.dvb.apply(a3)) continue;
            double d7 = a3.M(d2, d3, d4);
            if (!(d5 < 0.0) && !(d7 < d5 * d5) || d6 != -1.0 && !(d7 < d6)) continue;
            d6 = d7;
            a2 = a3;
        }
        return a2;
    }

    public boolean p(double d2, double d3, double d4, double d5) {
        for (int i2 = 0; i2 < this.dCq.size(); ++i2) {
            net.minecraft.entity.player.a a2 = this.dCq.get(i2);
            if (!net.minecraft.util.ah.dvb.apply(a2)) continue;
            double d6 = a2.M(d2, d3, d4);
            if (!(d5 < 0.0) && !(d6 < d5 * d5)) continue;
            return true;
        }
        return false;
    }

    public net.minecraft.entity.player.a gz(String string) {
        for (int i2 = 0; i2 < this.dCq.size(); ++i2) {
            net.minecraft.entity.player.a a2 = this.dCq.get(i2);
            if (!string.equals(a2.xG())) continue;
            return a2;
        }
        return null;
    }

    public net.minecraft.entity.player.a n(UUID uUID) {
        for (int i2 = 0; i2 < this.dCq.size(); ++i2) {
            net.minecraft.entity.player.a a2 = this.dCq.get(i2);
            if (!uUID.equals(a2.TX())) continue;
            return a2;
        }
        return null;
    }

    public void HB() {
    }

    public void avo() {
        this.dCG.avo();
    }

    public void I(long l2) {
        this.totalWorldTime.P(l2);
    }

    public long avp() {
        return this.totalWorldTime.avp();
    }

    public long getTotalWorldTime() {
        return this.totalWorldTime.azc();
    }

    public long getWorldTime() {
        return this.totalWorldTime.getWorldTime();
    }

    public void o(long l2) {
        this.totalWorldTime.o(l2);
    }

    public net.minecraft.util.b avq() {
        net.minecraft.util.b b2 = new net.minecraft.util.b(this.totalWorldTime.ayZ(), this.totalWorldTime.aza(), this.totalWorldTime.azb());
        if (!this.Sh().bi(b2)) {
            b2 = this.aM(new net.minecraft.util.b(this.Sh().getCenterX(), 0.0, this.Sh().getCenterZ()));
        }
        return b2;
    }

    public void aZ(net.minecraft.util.b b2) {
        this.totalWorldTime.bt(b2);
    }

    public void aB(net.minecraft.entity.c c2) {
        int n2 = bt.aF(c2.distance / 16.0);
        int n3 = bt.aF(c2.gY / 16.0);
        int n4 = 2;
        for (int i2 = n2 - 2; i2 <= n2 + 2; ++i2) {
            for (int i3 = n3 - 2; i3 <= n3 + 2; ++i3) {
                this.aL(i2, i3);
            }
        }
        if (!this.dCk.contains(c2)) {
            this.dCk.add(c2);
        }
    }

    public boolean a(net.minecraft.entity.player.a a2, net.minecraft.util.b b2) {
        return true;
    }

    public void a(net.minecraft.entity.c c2, byte by2) {
    }

    public net.minecraft.world.chunk.h avr() {
        return this.dCF;
    }

    public void c(net.minecraft.util.b b2, Block block, int n2, int n3) {
        block.a(this, b2, this.H(b2), n2, n3);
    }

    public d avs() {
        return this.dCG;
    }

    public o getTotalWorldTime() {
        return this.totalWorldTime;
    }

    public i RU() {
        return this.totalWorldTime.azi();
    }

    public void avu() {
    }

    public float bY(float f2) {
        return (this.dCz + (this.dCA - this.dCz) * f2) * this.ca(f2);
    }

    public void bZ(float f2) {
        this.dCz = f2;
        this.dCA = f2;
    }

    public float ca(float f2) {
        return this.dCx + (this.dCy - this.dCx) * f2;
    }

    public void cb(float f2) {
        this.dCx = f2;
        this.dCy = f2;
    }

    public boolean avv() {
        return (double)this.bY(1.0f) > 0.9;
    }

    public boolean avw() {
        return (double)this.ca(1.0f) > 0.2;
    }

    public boolean ba(net.minecraft.util.b b2) {
        if (!this.avw()) {
            return false;
        }
        if (!this.aI(b2)) {
            return false;
        }
        if (this.aO(b2).wg() > b2.wg()) {
            return false;
        }
        j j2 = this.aB(b2);
        return !j2.awI() && !this.g(b2, false) && j2.awJ();
    }

    public boolean bb(net.minecraft.util.b b2) {
        j j2 = this.aB(b2);
        return j2.awK();
    }

    public h avx() {
        return this.dCJ;
    }

    public void a(String string, ah ah2) {
        this.dCJ.b(string, ah2);
    }

    public ah b(Class<? extends ah> clazz, String string) {
        return this.dCJ.e(clazz, string);
    }

    public int gA(String string) {
        return this.dCJ.gA(string);
    }

    public void d(int n2, net.minecraft.util.b b2, int n3) {
        for (int i2 = 0; i2 < this.dCE.size(); ++i2) {
            this.dCE.get(i2).a(n2, b2, n3);
        }
    }

    public void e(int n2, net.minecraft.util.b b2, int n3) {
        this.b(null, n2, b2, n3);
    }

    public void b(net.minecraft.entity.player.a a2, int n2, net.minecraft.util.b b2, int n3) {
        try {
            for (int i2 = 0; i2 < this.dCE.size(); ++i2) {
                this.dCE.get(i2).a(a2, n2, b2, n3);
            }
        }
        catch (Throwable throwable) {
            net.minecraft.crash.a a3 = net.minecraft.crash.a.a(throwable, "Playing level event");
            net.minecraft.crash.i i3 = a3.dY("Level event being played");
            i3.b("Block coordinates", net.minecraft.crash.i.V(b2));
            i3.b("Event source", a2);
            i3.b("Event type", n2);
            i3.b("Event data", n3);
            throw new cl(a3);
        }
    }

    public int getHeight() {
        return 256;
    }

    public int avy() {
        return this.dCD.avR() ? 128 : 256;
    }

    public Random K(int n2, int n3, int n4) {
        long l2 = (long)n2 * 341873128712L + (long)n3 * 132897987541L + this.getTotalWorldTime().avp() + (long)n4;
        this.dCC.setSeed(l2);
        return this.dCC;
    }

    public net.minecraft.util.b b(String string, net.minecraft.util.b b2) {
        return this.avr().a(this, string, b2);
    }

    @Override
    public boolean auz() {
        return false;
    }

    public double avz() {
        return 0.0;
    }

    public net.minecraft.crash.i d(net.minecraft.crash.a a2) {
        net.minecraft.crash.i i2 = a2.y("Affected level", 1);
        i2.b("Level name", this.totalWorldTime == null ? "????" : this.totalWorldTime.akZ());
        i2.a("All players", new z(this));
        i2.a("Chunk stats", new aa(this));
        try {
            this.totalWorldTime.c(i2);
        }
        catch (Throwable throwable) {
            i2.a("Level Data Unobtainable", throwable);
        }
        return i2;
    }

    public void b(int n2, net.minecraft.util.b b2, int n3) {
        for (int i2 = 0; i2 < this.dCE.size(); ++i2) {
            net.minecraft.world.o o2 = this.dCE.get(i2);
            o2.b(n2, b2, n3);
        }
    }

    public void c(int n2, net.minecraft.util.b b2, int n3) {
        for (int i2 = 0; i2 < this.dCE.size(); ++i2) {
            net.minecraft.world.o o2 = this.dCE.get(i2);
            o2.c(n2, b2, n3);
        }
    }

    public Calendar avA() {
        if (this.getTotalWorldTime() % 600L == 0L) {
            this.dCM.setTimeInMillis(MinecraftServer.alz());
        }
        return this.dCM;
    }

    public void a(double d2, double d3, double d4, double d5, double d6, double d7, n n2) {
    }

    public net.minecraft.scoreboard.i Sp() {
        return this.dCN;
    }

    public void f(net.minecraft.util.b b2, Block block) {
        for (ao ao2 : ar.dwl) {
            net.minecraft.util.b b3 = b2.x(ao2);
            if (!this.aF(b3)) continue;
            IBlockState iBlockState = this.H(b3);
            if (Blocks.cND.s(iBlockState.df())) {
                iBlockState.df().a(this, b3, iBlockState, block);
                continue;
            }
            if (!iBlockState.df().wP() || !Blocks.cND.s((iBlockState = this.H(b3 = b3.x(ao2))).df())) continue;
            iBlockState.df().a(this, b3, iBlockState, block);
        }
    }

    public e bc(net.minecraft.util.b b2) {
        long l2 = 0L;
        float f2 = 0.0f;
        if (this.aF(b2)) {
            f2 = this.avf();
            l2 = this.aG(b2).axA();
        }
        return new e(this.afY(), this.getWorldTime(), l2, f2);
    }

    public f afY() {
        return this.getTotalWorldTime().afY();
    }

    public int avB() {
        return this.dCu;
    }

    public void ie(int n2) {
        this.dCu = n2;
    }

    public int avC() {
        return this.dCB;
    }

    public void cfr_renamed_0(int n2) {
        this.dCB = n2;
    }

    public boolean avD() {
        return this.dCI;
    }

    public net.minecraft.village.e avE() {
        return this.dCK;
    }

    public c Sh() {
        return this.dCU;
    }

    public boolean aN(int n2, int n3) {
        net.minecraft.util.b b2 = this.avq();
        int n4 = n2 * 16 + 8 - b2.wf();
        int n5 = n3 * 16 + 8 - b2.wj();
        int n6 = 128;
        return n4 >= -128 && n4 <= 128 && n5 >= -128 && n5 <= 128;
    }

    public void aC(net.minecraft.entity.c c2) {
        if (c2.riding != null) {
            if (!c2.riding.crt && c2.riding.cqS == c2) {
                return;
            }
            c2.riding.cqS = null;
            c2.riding = null;
        }
        this.dCL.eY("tick");
        if (!c2.crt) {
            try {
                this.aA(c2);
            }
            catch (Throwable throwable) {
                throwable.printStackTrace();
                afi.c("Exception when updating entity", new Object[0]);
            }
        }
        this.dCL.ajh();
        this.dCL.eY("remove");
        if (c2.crt) {
            int n2 = c2.crZ;
            int n3 = c2.csb;
            if (c2.crY && this.e(n2, n3, true)) {
                this.aL(n2, n3).H(c2);
            }
            this.dCk.remove(c2);
            this.J(c2);
        }
    }
}



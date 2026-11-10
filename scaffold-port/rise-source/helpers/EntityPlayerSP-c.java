package net.minecraft.client.entity;

import com.viaversion.viabackwards.protocol.v1_21_2to1_21.Protocol1_21_2To1_21;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import hackclient.rise.ek;
import hackclient.rise.en;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSoundMinecartRiding;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.a;
import net.minecraft.client.entity.d;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.bl;
import net.minecraft.client.gui.bs;
import net.minecraft.client.gui.inventory.b;
import net.minecraft.client.gui.inventory.g;
import net.minecraft.client.gui.inventory.m;
import net.minecraft.client.gui.inventory.n;
import net.minecraft.client.gui.inventory.o;
import net.minecraft.client.gui.inventory.p;
import net.minecraft.client.gui.inventory.r;
import net.minecraft.client.gui.s;
import net.minecraft.client.gui.y;
import net.minecraft.client.network.h;
import net.minecraft.command.server.e;
import net.minecraft.entity.ae;
import net.minecraft.entity.item.k;
import net.minecraft.init.Items;
import net.minecraft.inventory.ab;
import net.minecraft.item.co;
import net.minecraft.network.play.client.aa;
import net.minecraft.network.play.client.i;
import net.minecraft.network.play.client.j;
import net.minecraft.network.play.client.q;
import net.minecraft.network.play.client.w;
import net.minecraft.network.play.client.z;
import net.minecraft.stats.f;
import net.minecraft.tileentity.ag;
import net.minecraft.util.ad;
import net.minecraft.util.ao;
import net.minecraft.util.as;
import net.minecraft.util.ba;
import net.minecraft.util.bt;
import net.minecraft.util.cc;
import net.minecraft.util.cm;
import net.minecraft.util.da;
import rise.Client;
import rise.event.impl.ChatInputEvent;
import rise.event.impl.MoveEvent;
import rise.event.impl.PostMotionEvent;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.PreUpdateEvent;
import rise.event.impl.SlowDownEvent;
import rise.util.InstanceAccess;
import rise.util.math.Vector2f;

public class c
extends a
implements InstanceAccess {
    public h bju;
    private final net.minecraft.stats.m bjv;
    public UserConnection user;
    private d bjw = new d(false, false, false, false, false, false, false);
    public double bjx;
    public double bjy;
    public double bjz;
    public float bjA;
    public float bjB;
    public boolean bjC;
    public boolean bjD;
    public boolean bjE;
    public int bjF;
    private boolean bjG;
    private String bjH;
    public cc sneaking;
    protected Minecraft P;
    protected int bjJ;
    public int bjK;
    public float bjL;
    public float bjM;
    public float po;
    public float bjN;
    public float bjO;
    public float bjP;
    private int bjQ;
    private float bjR;
    public float bjS;
    public float bjT;
    public boolean bjU;

    public c(Minecraft minecraft, net.minecraft.world.w w2, h h2, net.minecraft.stats.m m2) {
        super(w2, h2.HQ());
        this.bju = h2;
        this.bjv = m2;
        this.P = minecraft;
        this.csh = 0;
    }

    @Override
    public boolean a(ad ad2, float f2) {
        return false;
    }

    @Override
    public void P(float f2) {
    }

    @Override
    public void D(net.minecraft.entity.c c2) {
        super.D(c2);
        if (c2 instanceof k) {
            this.P.Bj().a(new MovingSoundMinecartRiding(this, (k)c2));
        }
    }

    @Override
    public void CY() {
        if (this.burning.aF(new net.minecraft.util.b(this.distance, 0.0, this.gY))) {
            boolean bl2;
            this.bjN = this.po;
            this.po = this.pm;
            boolean bl3 = bl2 = this == Minecraft.getMinecraft().thePlayer;
            if (bl2) {
                Client.INSTANCE.e().d(new PreUpdateEvent());
            }
            super.CY();
            d d2 = new d(this.sneaking.cuo > 0.0f, this.sneaking.cuo < 0.0f, this.sneaking.dyK > 0.0f, this.sneaking.dyK < 0.0f, this.sneaking.gD, this.sneaking.jr, this.P.aIp.cgK.QQ());
            if (!this.bjw.equals(d2) && ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_2)) {
                UserConnection userConnection = Via.getManager().getConnectionManager().getConnections().iterator().next();
                PacketWrapper packetWrapper = PacketWrapper.create(ServerboundPackets1_21_2.PLAYER_INPUT, userConnection);
                packetWrapper.write(Types.BYTE, d2.Dq());
                packetWrapper.sendToServer(Protocol1_21_2To1_21.class);
                this.bjw = d2;
            }
            if (this.isRiding()) {
                this.bju.t(new net.minecraft.network.play.client.g(this.pl, this.pm, this.aO));
                this.bju.t(new net.minecraft.network.play.client.p(this.cun, this.cuo, this.sneaking.gD, this.sneaking.jr));
            } else {
                this.Da();
            }
            if (bl2) {
                Client.INSTANCE.e().d(new PostMotionEvent());
            }
        }
    }

    public void Da() {
        boolean bl2;
        boolean bl3;
        PreMotionEvent preMotionEvent = new PreMotionEvent(this.distance, this.gX, this.gY, this.pl, this.pm, this.aO, this.isSprinting());
        if (this == this.P.thePlayer) {
            Client.INSTANCE.e().d(preMotionEvent);
        }
        if ((bl3 = preMotionEvent.isSprinting()) != this.bjD) {
            if (bl3) {
                this.bju.t(new net.minecraft.network.play.client.n(this, net.minecraft.network.play.client.o.daK));
            } else {
                this.bju.t(new net.minecraft.network.play.client.n(this, net.minecraft.network.play.client.o.daL));
            }
            this.bjD = bl3;
        }
        if ((bl2 = this.isSneaking()) != this.bjC) {
            if (bl2) {
                this.bju.t(new net.minecraft.network.play.client.n(this, net.minecraft.network.play.client.o.daH));
            } else {
                this.bju.t(new net.minecraft.network.play.client.n(this, net.minecraft.network.play.client.o.daI));
            }
            this.bjC = bl2;
        }
        if (preMotionEvent.isCancelled()) {
            return;
        }
        if (this.Do()) {
            boolean bl4;
            double d2 = preMotionEvent.getPosX() - this.bjx;
            double d3 = preMotionEvent.getPosY() - this.bjy;
            double d4 = preMotionEvent.getPosZ() - this.bjz;
            double d5 = preMotionEvent.getYaw() - this.bjA;
            double d6 = preMotionEvent.getPitch() - this.bjB;
            if (ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8)) {
                ++this.bjF;
            }
            boolean bl5 = d2 * d2 + d3 * d3 + d4 * d4 > (ViaLoadingBase.getInstance().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_18) ? 9.0E-4 : 4.0E-8) || this.bjF >= 20;
            boolean bl6 = d5 != 0.0 || d6 != 0.0;
            boolean bl7 = bl4 = ViaLoadingBase.getInstance().getTargetVersion().getVersion() <= 47;
            if (this.riding == null) {
                if (bl5 && bl6) {
                    this.bju.t(new net.minecraft.network.play.client.h(preMotionEvent.getPosX(), preMotionEvent.getPosY(), preMotionEvent.getPosZ(), preMotionEvent.getYaw(), preMotionEvent.getPitch(), preMotionEvent.isOnGround()));
                } else if (bl5) {
                    this.bju.t(new net.minecraft.network.play.client.f(preMotionEvent.getPosX(), preMotionEvent.getPosY(), preMotionEvent.getPosZ(), preMotionEvent.isOnGround()));
                } else if (bl6) {
                    this.bju.t(new net.minecraft.network.play.client.g(preMotionEvent.getYaw(), preMotionEvent.getPitch(), preMotionEvent.isOnGround()));
                } else if (this.bjE != this.aO || bl4) {
                    this.bju.t(new net.minecraft.network.play.client.e(preMotionEvent.isOnGround()));
                }
            } else {
                this.bju.t(new net.minecraft.network.play.client.h(this.ud, -999.0, this.uf, this.pl, this.pm, this.aO));
                bl5 = false;
            }
            if (!ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8)) {
                ++this.bjF;
            }
            if (bl5) {
                this.bjx = preMotionEvent.getPosX();
                this.bjy = preMotionEvent.getPosY();
                this.bjz = preMotionEvent.getPosZ();
                this.bjF = 0;
            }
            if (bl6) {
                this.bjA = preMotionEvent.getYaw();
                this.bjB = preMotionEvent.getPitch();
            }
            this.bjE = preMotionEvent.isOnGround();
        }
    }

    @Override
    public net.minecraft.entity.item.i V(boolean bl2) {
        j j2 = bl2 ? j.dau : j.dav;
        this.bju.t(new i(j2, net.minecraft.util.b.dtf, net.minecraft.util.ao.dvI));
        return null;
    }

    @Override
    protected void a(net.minecraft.entity.item.i i2) {
    }

    public void sendChatMessage(String string) {
        ChatInputEvent chatInputEvent = new ChatInputEvent(string);
        Client.INSTANCE.e().d(chatInputEvent);
        if (chatInputEvent.isCancelled()) {
            return;
        }
        this.bju.t(new net.minecraft.network.play.client.b(chatInputEvent.getMessage()));
    }

    @Override
    public void swingItem() {
        super.swingItem();
        this.bju.t(new net.minecraft.network.play.client.m());
    }

    @Override
    public void Db() {
        this.bju.t(new z(aa.dbh));
    }

    @Override
    protected void b(ad ad2, float f2) {
        if (!this.a(ad2)) {
            this.aY(this.getHealth() - f2);
        }
    }

    @Override
    public void Dc() {
        this.bju.t(new q(this.cHq.cSd));
        this.Dd();
    }

    public void Dd() {
        this.cHn.e((co)null);
        super.Dc();
        this.P.a((GuiScreen)null);
    }

    public void Q(float f2) {
        if (this.bjG) {
            float f3 = this.getHealth() - f2;
            if (f3 <= 0.0f) {
                this.aY(f2);
                if (f3 < 0.0f) {
                    this.crS = this.ctS / 2;
                }
            } else {
                this.cul = f3;
                this.aY(this.getHealth());
                this.crS = this.ctS;
                this.b(ad.duD, f3);
                this.ctL = 10;
                this.ctK = 10;
            }
        } else {
            this.aY(f2);
            this.bjG = true;
        }
    }

    @Override
    public void a(f f2, int n2) {
        if (f2 != null && f2.doH) {
            super.a(f2, n2);
        }
    }

    @Override
    public void De() {
        this.bju.t(new w(this.cHL));
    }

    @Override
    public boolean Df() {
        return true;
    }

    protected void Dg() {
        this.bju.t(new net.minecraft.network.play.client.n(this, net.minecraft.network.play.client.o.daM, (int)(this.Dm() * 100.0f)));
    }

    public void Dh() {
        this.bju.t(new net.minecraft.network.play.client.n(this, net.minecraft.network.play.client.o.daN));
    }

    public void cF(String string) {
        this.bjH = string;
    }

    public String Di() {
        return this.bjH;
    }

    public net.minecraft.stats.m Dj() {
        return this.bjv;
    }

    @Override
    public void b(ba ba2) {
        this.P.bgr.Ek().c(ba2);
    }

    @Override
    protected boolean x(double d2, double d3, double d4) {
        if (this.crH) {
            return false;
        }
        net.minecraft.util.b b2 = new net.minecraft.util.b(d2, d3, d4);
        double d5 = d2 - (double)b2.wf();
        double d6 = d4 - (double)b2.wj();
        if (!this.C(b2)) {
            ek ek2 = new ek();
            Client.INSTANCE.e().d(ek2);
            if (ek2.isCancelled()) {
                return false;
            }
            int n2 = -1;
            double d7 = 9999.0;
            if (this.C(b2.aqN()) && d5 < d7) {
                d7 = d5;
                n2 = 0;
            }
            if (this.C(b2.aqO()) && 1.0 - d5 < d7) {
                d7 = 1.0 - d5;
                n2 = 1;
            }
            if (this.C(b2.aqL()) && d6 < d7) {
                d7 = d6;
                n2 = 4;
            }
            if (this.C(b2.aqM()) && 1.0 - d6 < d7) {
                d7 = 1.0 - d6;
                n2 = 5;
            }
            float f2 = 0.1f;
            if (n2 == 0) {
                this.ud = -0.1f;
            }
            if (n2 == 1) {
                this.ud = 0.1f;
            }
            if (n2 == 4) {
                this.uf = -0.1f;
            }
            if (n2 == 5) {
                this.uf = 0.1f;
            }
        }
        return false;
    }

    private boolean C(net.minecraft.util.b b2) {
        return !this.burning.H(b2).df().wP() && !this.burning.H(b2.aqJ()).df().wP();
    }

    @Override
    public void setSprinting(boolean bl2) {
        super.setSprinting(bl2);
        this.bjK = bl2 ? 600 : 0;
    }

    public void a(float f2, int n2, int n3) {
        this.cHO = f2;
        this.cHN = n2;
        this.cHM = n3;
    }

    @Override
    public void a(ba ba2) {
        this.P.bgr.Ek().c(ba2);
    }

    @Override
    public boolean c(int n2, String string) {
        return n2 <= 0;
    }

    @Override
    public net.minecraft.util.b xX() {
        return new net.minecraft.util.b(this.distance + 0.5, this.gX + 0.5, this.gY + 0.5);
    }

    @Override
    public void a(String string, float f2, float f3) {
        this.burning.a(this.distance, this.gX, this.gY, string, f2, f3, false);
    }

    @Override
    public boolean Dk() {
        return true;
    }

    public boolean Dl() {
        return this.riding != null && this.riding instanceof net.minecraft.entity.passive.f && ((net.minecraft.entity.passive.f)this.riding).aae();
    }

    public float Dm() {
        return this.bjR;
    }

    @Override
    public void a(ag ag2) {
        this.P.a(new o(ag2));
    }

    @Override
    public void a(e e2) {
        this.P.a(new net.minecraft.client.gui.h(e2));
    }

    @Override
    protected boolean l(da da2) {
        float f2 = this.pl * ((float)Math.PI / 180);
        double d2 = bt.bx(f2);
        double d3 = bt.bB(f2);
        double d4 = (double)this.cun * d3 - (double)this.cuo * d2;
        double d5 = (double)this.cuo * d3 + (double)this.cun * d2;
        double d6 = d4 * d4 + d5 * d5;
        double d7 = da2.dzY * da2.dzY + da2.dAa * da2.dAa;
        if (d6 < (double)1.0E-5f || d7 < (double)1.0E-5f) {
            return false;
        }
        return Math.acos((d4 * da2.dzY + d5 * da2.dAa) / Math.sqrt(d6 * d7)) < 0.13962633907794952;
    }

    @Override
    public void F(co co2) {
        net.minecraft.item.d d2 = co2.bP();
        if (d2 == Items.cRi) {
            this.P.a(new bs(this, co2, true));
        }
    }

    @Override
    public void a(ab ab2) {
        String string;
        String string2 = string = ab2 instanceof net.minecraft.world.m ? ((net.minecraft.world.m)((Object)ab2)).xJ() : "minecraft:container";
        if ("minecraft:chest".equals(string)) {
            this.P.a(new net.minecraft.client.gui.inventory.h(this.cHn, ab2));
        } else if ("minecraft:hopper".equals(string)) {
            this.P.a(new y(this.cHn, ab2));
        } else if ("minecraft:furnace".equals(string)) {
            this.P.a(new p(this.cHn, ab2));
        } else if ("minecraft:brewing_stand".equals(string)) {
            this.P.a(new g(this.cHn, ab2));
        } else if ("minecraft:beacon".equals(string)) {
            this.P.a(new b(this.cHn, ab2));
        } else if (!"minecraft:dispenser".equals(string) && !"minecraft:dropper".equals(string)) {
            this.P.a(new net.minecraft.client.gui.inventory.h(this.cHn, ab2));
        } else {
            this.P.a(new n(this.cHn, ab2));
        }
    }

    @Override
    public void a(net.minecraft.entity.passive.f f2, ab ab2) {
        this.P.a(new r(this.cHn, ab2, f2));
    }

    @Override
    public void a(net.minecraft.world.m m2) {
        String string = m2.xJ();
        if ("minecraft:crafting_table".equals(string)) {
            this.P.a(new m(this.cHn, this.burning));
        } else if ("minecraft:enchanting_table".equals(string)) {
            this.P.a(new s(this.cHn, this.burning, m2));
        } else if ("minecraft:anvil".equals(string)) {
            this.P.a(new bl(this.cHn, this.burning));
        }
    }

    @Override
    public void a(ae ae2) {
        this.P.a(new net.minecraft.client.gui.ao(this.cHn, ae2, this.burning));
    }

    @Override
    public void E(net.minecraft.entity.c c2) {
        this.P.bge.a(c2, as.dwx);
    }

    @Override
    public void F(net.minecraft.entity.c c2) {
        this.P.bge.a(c2, as.dwy);
    }

    @Override
    public boolean isSneaking() {
        boolean bl2 = this.sneaking != null && this.sneaking.jr;
        return bl2 && !this.cHC;
    }

    @Override
    public void Dn() {
        super.Dn();
        if (this.Do()) {
            this.cun = this.sneaking.dyK;
            this.cuo = this.sneaking.cuo;
            this.cum = this.sneaking.gD;
            this.bjO = this.bjL;
            this.bjP = this.bjM;
            this.bjM = (float)((double)this.bjM + (double)(this.pm - this.bjM) * 0.5);
            this.bjL = (float)((double)this.bjL + (double)(this.pl - this.bjL) * 0.5);
        }
    }

    protected boolean Do() {
        return this.P.Bo() == this;
    }

    @Override
    public void CZ() {
        boolean bl2;
        if (this.bjK > 0) {
            --this.bjK;
            if (this.bjK == 0) {
                this.setSprinting(false);
            }
        }
        if (this.bjJ > 0) {
            --this.bjJ;
        }
        this.bjT = this.bjS;
        if (this.csf) {
            if (this.P.bgi != null && !this.P.bgi.doesGuiPauseGame()) {
                this.P.a((GuiScreen)null);
            }
            if (this.bjS == 0.0f) {
                this.P.Bj().a(PositionedSoundRecord.a(new cm("portal.trigger"), this.ta.nextFloat() * 0.4f + 0.8f));
            }
            this.bjS += 0.0125f;
            if (this.bjS >= 1.0f) {
                this.bjS = 1.0f;
            }
            this.csf = false;
        } else if (this.a(net.minecraft.potion.a.dfW) && this.b(net.minecraft.potion.a.dfW).getDuration() > 60) {
            this.bjS += 0.006666667f;
            if (this.bjS > 1.0f) {
                this.bjS = 1.0f;
            }
        } else {
            if (this.bjS > 0.0f) {
                this.bjS -= 0.05f;
            }
            if (this.bjS < 0.0f) {
                this.bjS = 0.0f;
            }
        }
        if (this.cse > 0) {
            --this.cse;
        }
        boolean bl3 = this.sneaking.gD;
        boolean bl4 = this.sneaking.jr;
        float f2 = 0.8f;
        boolean bl5 = this.sneaking.cuo >= 0.8f;
        this.sneaking.ath();
        SlowDownEvent slowDownEvent = new SlowDownEvent(0.2f, 0.2f, this.isUsingItem());
        Client.INSTANCE.e().d(slowDownEvent);
        if (!slowDownEvent.isCancelled() && slowDownEvent.db() && !this.isRiding()) {
            this.sneaking.dyK *= slowDownEvent.getStrafeMultiplier();
            this.sneaking.cuo *= slowDownEvent.getForwardMultiplier();
            this.bjJ = 0;
        }
        this.x(this.distance - (double)this.width * 0.35, this.Ue().dta + 0.5, this.gY + (double)this.width * 0.35);
        this.x(this.distance - (double)this.width * 0.35, this.Ue().dta + 0.5, this.gY - (double)this.width * 0.35);
        this.x(this.distance + (double)this.width * 0.35, this.Ue().dta + 0.5, this.gY - (double)this.width * 0.35);
        this.x(this.distance + (double)this.width * 0.35, this.Ue().dta + 0.5, this.gY + (double)this.width * 0.35);
        boolean bl6 = bl2 = (float)this.abK().getFoodLevel() > 6.0f || this.cHL.cJd;
        if (!(!this.aO || bl4 || bl5 || !this.bjU && !(this.sneaking.cuo >= 0.8f) || this.isSprinting() || !bl2 || slowDownEvent.db() || this.a(net.minecraft.potion.a.dgc))) {
            if (this.bjJ <= 0 && !this.P.aIp.cgK.QQ()) {
                this.bjJ = 7;
            } else {
                this.setSprinting(true);
            }
        }
        if (!this.isSprinting() && (this.bjU || this.sneaking.cuo >= 0.8f) && bl2 && (!slowDownEvent.db() || slowDownEvent.isCancelled()) && !this.a(net.minecraft.potion.a.dgc) && this.P.aIp.cgK.QQ()) {
            this.setSprinting(true);
        }
        if (this.isSprinting() && !this.bjU && (this.sneaking.cuo < 0.8f || this.crj && (ViaLoadingBase.getInstance().getTargetVersion().olderThan(ProtocolVersion.v1_14) || !this.cqT) || !bl2 || this.isInWater())) {
            this.setSprinting(false);
        }
        if (this.cHL.cJd) {
            if (this.P.bfL.Hl()) {
                if (!this.cHL.cJc) {
                    this.cHL.cJc = true;
                    this.De();
                }
            } else if (!bl3 && this.sneaking.gD) {
                if (this.cHs == 0) {
                    this.cHs = 7;
                } else {
                    this.cHL.cJc = !this.cHL.cJc;
                    this.De();
                    this.cHs = 0;
                }
            }
        }
        if (this.cHL.cJc && this.Do()) {
            if (this.sneaking.jr) {
                this.ue -= (double)(this.cHL.getFlySpeed() * 3.0f);
            }
            if (this.sneaking.gD) {
                this.ue += (double)(this.cHL.getFlySpeed() * 3.0f);
            }
        }
        if (this.Dl()) {
            if (this.bjQ < 0) {
                ++this.bjQ;
                if (this.bjQ == 0) {
                    this.bjR = 0.0f;
                }
            }
            if (bl3 && !this.sneaking.gD) {
                this.bjQ = -10;
                this.Dg();
            } else if (!bl3 && this.sneaking.gD) {
                this.bjQ = 0;
                this.bjR = 0.0f;
            } else if (bl3) {
                ++this.bjQ;
                this.bjR = this.bjQ < 10 ? (float)this.bjQ * 0.1f : 0.8f + 2.0f / (float)(this.bjQ - 9) * 0.1f;
            }
        } else {
            this.bjR = 0.0f;
        }
        Client.INSTANCE.e().d(new en());
        super.CZ();
        if (this.aO && this.cHL.cJc && !this.P.bfL.Hl()) {
            this.cHL.cJc = false;
            this.De();
        }
    }

    public Vector2f Dp() {
        return new Vector2f(this.bjA, this.bjB);
    }

    @Override
    public void y(double d2, double d3, double d4) {
        MoveEvent moveEvent = new MoveEvent(d2, d3, d4);
        if (this == Minecraft.getMinecraft().thePlayer) {
            Client.INSTANCE.e().d(moveEvent);
            if (moveEvent.isCancelled()) {
                return;
            }
        }
        super.y(moveEvent.getPosX(), moveEvent.getPosY(), moveEvent.getPosZ());
    }

    public void z(double d2, double d3, double d4) {
        super.y(d2, d3, d4);
    }
}



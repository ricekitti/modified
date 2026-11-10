package net.minecraft.client.multiplayer;

import com.viaversion.viabackwards.protocol.v1_19_1to1_19.Protocol1_19_1To1_19;
import com.viaversion.viabackwards.protocol.v1_19_3to1_19_1.Protocol1_19_3To1_19_1;
import com.viaversion.viabackwards.protocol.v1_19_4to1_19_3.Protocol1_19_4To1_19_3;
import com.viaversion.viabackwards.protocol.v1_19to1_18_2.Protocol1_19To1_18_2;
import com.viaversion.viabackwards.protocol.v1_20to1_19_4.Protocol1_20To1_19_4;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ServerboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ServerboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ServerboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.packet.ServerboundPackets1_19_1;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.vialoadingbase.netty.handler.VLBViaDecodeHandler;
import hackclient.rise.bu;
import hackclient.rise.eb;
import hackclient.rise.et;
import hackclient.rise.eu;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.c;
import net.minecraft.client.gui.inventory.m;
import net.minecraft.client.gui.inventory.p;
import net.minecraft.client.gui.inventory.q;
import net.minecraft.client.multiplayer.j;
import net.minecraft.client.network.h;
import net.minecraft.entity.passive.f;
import net.minecraft.entity.player.a;
import net.minecraft.entity.player.g;
import net.minecraft.item.af;
import net.minecraft.item.co;
import net.minecraft.item.cp;
import net.minecraft.network.play.client.i;
import net.minecraft.network.play.client.k;
import net.minecraft.network.play.client.l;
import net.minecraft.network.play.client.r;
import net.minecraft.network.play.client.t;
import net.minecraft.network.play.client.u;
import net.minecraft.util.b;
import net.minecraft.util.ce;
import net.minecraft.util.cf;
import net.minecraft.util.cm;
import net.minecraft.util.da;
import net.minecraft.world.ao;
import net.minecraft.world.w;
import rise.Client;
import rise.module.impl.player.ChestStealer;
import rise.module.impl.player.InventoryManager;
import rise.util.InstanceAccess;

public class d
implements InstanceAccess {
    private final Minecraft bCJ;
    private final h bCK;
    private b bCL = new b(-1, -1, -1);
    private co bCM;
    private int bCN;
    public float bCO;
    private float bCP;
    public int bCQ;
    public boolean bCR;
    private ao bCS = ao.dDP;
    public int bCT;
    public int bCU;

    public d(Minecraft minecraft, h h2) {
        this.bCJ = minecraft;
        this.bCK = h2;
    }

    public static void a(Minecraft minecraft, d d2, b b2, net.minecraft.util.ao ao2) {
        if (!minecraft.bfW.a(minecraft.thePlayer, b2, ao2)) {
            d2.d(b2, ao2);
        }
    }

    public void j(a a2) {
        this.bCS.a(a2.cHL);
    }

    public int Ha() {
        return ++this.bCN;
    }

    public boolean CL() {
        return this.bCS == ao.dDS;
    }

    public void a(ao ao2) {
        this.bCS = ao2;
        this.bCS.a(this.bCJ.thePlayer.cHL);
    }

    public void k(a a2) {
        a2.pl = -180.0f;
    }

    public boolean Hb() {
        return this.bCS.aws();
    }

    public boolean d(b b2, net.minecraft.util.ao ao2) {
        Object object;
        Object object2;
        et et2 = new et(b2);
        Client.INSTANCE.e().d(et2);
        if (et2.isCancelled()) {
            return false;
        }
        if (this.bCS.awr()) {
            if (this.bCS == ao.dDS) {
                return false;
            }
            if (!this.bCJ.thePlayer.abM()) {
                object2 = this.bCJ.bfW.H(b2).df();
                this.d(bu.class);
                object = bu.bO();
                if (object == null) {
                    return false;
                }
                if (!((co)object).K((Block)object2)) {
                    return false;
                }
            }
        }
        if (this.bCS.alT() && this.bCJ.thePlayer.UW() != null && this.bCJ.thePlayer.UW().bP() instanceof cp) {
            return false;
        }
        object2 = this.bCJ.bfW;
        object = ((w)object2).H(b2);
        Block block = object.df();
        if (block.wN() == Material.bdF) {
            return false;
        }
        ((w)object2).e(2001, b2, Block.a((IBlockState)object));
        boolean bl2 = ((w)object2).aH(b2);
        if (bl2) {
            block.b((w)object2, b2, (IBlockState)object);
        }
        this.bCL = new b(this.bCL.wf(), -1, this.bCL.wj());
        if (!this.bCS.alT()) {
            co co2;
            if (this.bCJ.thePlayer.cHn.cIY) {
                this.d(bu.class);
                v0 = bu.bO();
            } else {
                v0 = co2 = this.bCJ.thePlayer.UW();
            }
            if (co2 != null) {
                co2.a((w)object2, block, b2, this.bCJ.thePlayer);
                if (co2.cWr == 0) {
                    this.bCJ.thePlayer.abB();
                }
            }
        }
        return bl2;
    }

    public boolean e(b b2, net.minecraft.util.ao ao2) {
        Object object;
        Object object2;
        if (this.bCS.awr()) {
            if (this.bCS == ao.dDS) {
                return false;
            }
            if (!this.bCJ.thePlayer.abM()) {
                object2 = this.bCJ.bfW.H(b2).df();
                if (this.bCJ.thePlayer.cHn.cIY) {
                    this.d(bu.class);
                    v0 = bu.bO();
                } else {
                    v0 = object = this.bCJ.thePlayer.UW();
                }
                if (object == null) {
                    return false;
                }
                if (!((co)object).K((Block)object2)) {
                    return false;
                }
            }
        }
        if (!this.bCJ.bfW.Sh().bi(b2)) {
            return false;
        }
        if (this.bCS.alT()) {
            if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_19)) {
                object2 = Via.getManager().getConnectionManager().getConnections().iterator().next();
                object = PacketWrapper.create(ServerboundPackets1_19.PLAYER_ACTION, (UserConnection)object2);
                object.write(Types.VAR_INT, net.minecraft.network.play.client.j.dar.ordinal());
                object.write(Types.BLOCK_POSITION1_14, new BlockPosition(b2.wf(), b2.wg(), b2.wj()));
                object.write(Types.BYTE, (byte)ao2.OK());
                object.write(Types.VAR_INT, this.Ha());
                object.sendToServer(Protocol1_19To1_18_2.class);
            } else {
                this.bCK.t(new i(net.minecraft.network.play.client.j.dar, b2, ao2));
            }
            d.a(this.bCJ, this, b2, ao2);
            this.bCQ = 5;
        } else if (!this.bCR || !this.E(b2)) {
            boolean bl2;
            if (this.bCR) {
                this.bCK.t(new i(net.minecraft.network.play.client.j.das, this.bCL, ao2));
            }
            if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_19)) {
                object2 = Via.getManager().getConnectionManager().getConnections().iterator().next();
                object = PacketWrapper.create(ServerboundPackets1_19.PLAYER_ACTION, (UserConnection)object2);
                object.write(Types.VAR_INT, net.minecraft.network.play.client.j.dar.ordinal());
                object.write(Types.BLOCK_POSITION1_14, new BlockPosition(b2.wf(), b2.wg(), b2.wj()));
                object.write(Types.BYTE, (byte)ao2.OK());
                object.write(Types.VAR_INT, this.Ha());
                object.sendToServer(Protocol1_19To1_18_2.class);
            } else {
                this.bCK.t(new i(net.minecraft.network.play.client.j.dar, b2, ao2));
            }
            object2 = this.bCJ.bfW.H(b2).df();
            boolean bl3 = bl2 = ((Block)object2).wN() != Material.bdF;
            if (bl2 && this.bCO == 0.0f) {
                ((Block)object2).a((w)this.bCJ.bfW, b2, this.bCJ.thePlayer);
            }
            if (this.bCJ.bgt != null && this.bCJ.bgt.dyM.equals((Object)cf.dyQ) && this.bCJ.bgt.dg().equals(b2)) {
                eu eu2 = new eu(this.bCJ.thePlayer, this.bCJ.thePlayer.burning, b2);
                Client.INSTANCE.e().d(eu2);
            }
            if (bl2 && ((Block)object2).a(this.bCJ.thePlayer, this.bCJ.thePlayer.burning, b2) >= 1.0f) {
                this.d(b2, ao2);
            } else {
                co co2;
                this.bCR = true;
                this.bCL = b2;
                if (this.bCJ.thePlayer.cHn.cIY) {
                    this.d(bu.class);
                    co2 = bu.bO();
                } else {
                    co2 = this.bCJ.thePlayer.UW();
                }
                this.bCM = co2;
                this.bCO = 0.0f;
                this.bCP = 0.0f;
                this.bCJ.bfW.b(this.bCJ.thePlayer.getEntityId(), this.bCL, (int)(this.bCO * 10.0f) - 1);
            }
        }
        return true;
    }

    public void Hc() {
        if (this.bCR) {
            this.bCK.t(new i(net.minecraft.network.play.client.j.das, this.bCL, net.minecraft.util.ao.dvI));
            this.bCR = false;
            this.bCO = 0.0f;
            this.bCJ.bfW.b(this.bCJ.thePlayer.getEntityId(), this.bCL, -1);
        }
    }

    public boolean f(b b2, net.minecraft.util.ao ao2) {
        this.Hf();
        if (this.bCQ > 0) {
            --this.bCQ;
            return true;
        }
        if (this.bCS.alT() && this.bCJ.bfW.Sh().bi(b2)) {
            this.bCQ = 5;
            if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_19)) {
                UserConnection userConnection = Via.getManager().getConnectionManager().getConnections().iterator().next();
                PacketWrapper packetWrapper = PacketWrapper.create(ServerboundPackets1_19.PLAYER_ACTION, userConnection);
                packetWrapper.write(Types.VAR_INT, net.minecraft.network.play.client.j.dar.ordinal());
                packetWrapper.write(Types.BLOCK_POSITION1_14, new BlockPosition(b2.wf(), b2.wg(), b2.wj()));
                packetWrapper.write(Types.BYTE, (byte)ao2.OK());
                packetWrapper.write(Types.VAR_INT, this.Ha());
                packetWrapper.sendToServer(Protocol1_19To1_18_2.class);
            } else {
                this.bCK.t(new i(net.minecraft.network.play.client.j.dar, b2, ao2));
            }
            d.a(this.bCJ, this, b2, ao2);
            return true;
        }
        if (this.E(b2)) {
            Block block = this.bCJ.bfW.H(b2).df();
            if (block.wN() == Material.bdF) {
                this.bCR = false;
                return false;
            }
            this.bCO += block.a(this.bCJ.thePlayer, this.bCJ.thePlayer.burning, b2);
            if (this.bCP % 4.0f == 0.0f) {
                this.bCJ.Bj().a(new PositionedSoundRecord(new cm(block.aRS.xD()), (block.aRS.getVolume() + 1.0f) / 8.0f, block.aRS.xF() * 0.5f, (float)b2.wf() + 0.5f, (float)b2.wg() + 0.5f, (float)b2.wj() + 0.5f));
            }
            this.bCP += 1.0f;
            if (this.bCO >= 1.0f) {
                this.bCR = false;
                if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_19)) {
                    UserConnection userConnection = Via.getManager().getConnectionManager().getConnections().iterator().next();
                    PacketWrapper packetWrapper = PacketWrapper.create(ServerboundPackets1_19.PLAYER_ACTION, userConnection);
                    packetWrapper.write(Types.VAR_INT, net.minecraft.network.play.client.j.dat.ordinal());
                    packetWrapper.write(Types.BLOCK_POSITION1_14, new BlockPosition(b2.wf(), b2.wg(), b2.wj()));
                    packetWrapper.write(Types.BYTE, (byte)ao2.OK());
                    packetWrapper.write(Types.VAR_INT, this.Ha());
                    packetWrapper.sendToServer(Protocol1_19To1_18_2.class);
                } else {
                    this.bCK.t(new i(net.minecraft.network.play.client.j.dat, b2, ao2));
                }
                this.d(b2, ao2);
                this.bCO = 0.0f;
                this.bCP = 0.0f;
                this.bCQ = 5;
            }
            this.bCJ.bfW.b(this.bCJ.thePlayer.getEntityId(), this.bCL, (int)(this.bCO * 10.0f) - 1);
            return true;
        }
        return this.e(b2, ao2);
    }

    public float Hd() {
        return this.bCS.alT() ? 5.0f : 4.5f;
    }

    public void He() {
        this.Hf();
        if (this.bCK.dr().aeQ()) {
            this.bCK.dr().aeM();
        } else {
            this.bCK.dr().aeV();
        }
    }

    private boolean E(b b2) {
        boolean bl2;
        co co2;
        if (this.bCJ.thePlayer.cHn.cIY) {
            this.d(bu.class);
            co2 = bu.bO();
        } else {
            co2 = this.bCJ.thePlayer.UW();
        }
        co co3 = co2;
        boolean bl3 = bl2 = this.bCM == null && co3 == null;
        if (this.bCM != null && co3 != null) {
            bl2 = co3.bP() == this.bCM.bP() && co.c(co3, this.bCM) && (co3.adP() || co3.xO() == this.bCM.xO());
        }
        return b2.equals(this.bCL) && bl2;
    }

    public void Hf() {
        g g2 = this.bCJ.thePlayer.cHn;
        int n2 = g2.cIW;
        eb eb2 = new eb(n2);
        Client.INSTANCE.e().d(eb2);
        n2 = eb2.cX();
        if (n2 != this.bCT) {
            this.bCU = this.bCT;
            this.bCT = n2;
            this.bCK.t(new l(this.bCT));
        }
    }

    public boolean a(c c2, j j2, co co2, b b2, net.minecraft.util.ao ao2, da da2) {
        Object object;
        Object object2;
        this.Hf();
        float f2 = (float)(da2.dzY - (double)b2.wf());
        float f3 = (float)(da2.dzZ - (double)b2.wg());
        float f4 = (float)(da2.dAa - (double)b2.wj());
        boolean bl2 = false;
        if (!this.bCJ.bfW.Sh().bi(b2)) {
            return false;
        }
        if (this.bCS != ao.dDS) {
            object2 = j2.H(b2);
            if ((!c2.isSneaking() || c2.UW() == null) && object2.df().a((w)j2, b2, (IBlockState)object2, c2, ao2, f2, f3, f4)) {
                bl2 = true;
            }
            if (!bl2 && co2 != null && co2.bP() instanceof af) {
                object = (af)co2.bP();
                if (!((af)object).a(j2, b2, ao2, c2, co2)) {
                    return false;
                }
                co2.cWt = 0;
                --co2.cWs;
            }
        }
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_19)) {
            object2 = Via.getManager().getConnectionManager().getConnections().iterator().next();
            object = PacketWrapper.create(ServerboundPackets1_19.USE_ITEM_ON, (UserConnection)object2);
            object.write(Types.VAR_INT, 0);
            object.write(Types.BLOCK_POSITION1_14, new BlockPosition(b2.wf(), b2.wg(), b2.wj()));
            object.write(Types.VAR_INT, ao2.ordinal());
            object.write(Types.FLOAT, Float.valueOf(f2));
            object.write(Types.FLOAT, Float.valueOf(f3));
            object.write(Types.FLOAT, Float.valueOf(f4));
            object.write(Types.BOOLEAN, false);
            object.write(Types.VAR_INT, this.Ha());
            object.sendToServer(Protocol1_19To1_18_2.class);
        } else {
            this.bCK.t(new k(b2, ao2.OK(), c2.cHn.acq(), f2, f3, f4));
        }
        if (!bl2 && this.bCS != ao.dDS) {
            if (co2 == null) {
                return false;
            }
            if (this.bCS.alT()) {
                int n2 = co2.xO();
                int n3 = co2.cWr;
                boolean bl3 = co2.a(c2, j2, b2, ao2, f2, f3, f4);
                co2.gf(n2);
                co2.cWr = n3;
                return bl3;
            }
            return co2.a(c2, j2, b2, ao2, f2, f3, f4);
        }
        return true;
    }

    public float Hg() {
        return this.bCO;
    }

    public void ae(float f2) {
        this.bCO = f2;
    }

    public boolean a(a a2, w w2, co co2) {
        Object object;
        if (this.bCS == ao.dDS) {
            return false;
        }
        this.Hf();
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_19)) {
            UserConnection userConnection = Via.getManager().getConnectionManager().getConnections().iterator().next();
            object = PacketWrapper.create(ServerboundPackets1_19.USE_ITEM, userConnection);
            object.write(Types.VAR_INT, 0);
            object.write(Types.VAR_INT, this.Ha());
            object.sendToServer(Protocol1_19To1_18_2.class);
        } else {
            this.bCK.t(new k(a2.cHn.acq()));
        }
        int n2 = co2.cWr;
        object = co2.a(w2, a2);
        if (object != co2 || object != null && ((co)object).cWr != n2) {
            co[] coArray = a2.cHn.cIU;
            this.d(bu.class);
            coArray[bu.bQ()] = object;
            if (((co)object).cWr == 0) {
                co[] coArray2 = a2.cHn.cIU;
                this.d(bu.class);
                coArray2[bu.bQ()] = null;
            }
            return true;
        }
        return false;
    }

    public c a(w w2, net.minecraft.stats.m m2) {
        return new c(this.bCJ, w2, this.bCK, m2);
    }

    public void a(a a2, net.minecraft.entity.c c2) {
        this.Hf();
        this.bCK.t(new net.minecraft.network.play.client.c(c2, net.minecraft.network.play.client.d.dam));
        if (this.bCS != ao.dDS) {
            a2.ar(c2);
        }
    }

    public boolean b(a a2, net.minecraft.entity.c c2) {
        this.Hf();
        this.bCK.t(new net.minecraft.network.play.client.c(c2, net.minecraft.network.play.client.d.dal));
        return this.bCS != ao.dDS && a2.aq(c2);
    }

    public boolean a(a a2, net.minecraft.entity.c c2, ce ce2) {
        this.Hf();
        da da2 = new da(ce2.dak.dzY - c2.distance, ce2.dak.dzZ - c2.gX, ce2.dak.dAa - c2.gY);
        this.bCK.t(new net.minecraft.network.play.client.c(c2, da2));
        return this.bCS != ao.dDS && c2.a(a2, da2);
    }

    public co a(int n2, int n3, int n4, int n5, a a2) {
        short s2 = a2.cHq.b(a2.cHn);
        co co2 = a2.cHq.a(n3, n4, n5, a2);
        co co3 = ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_12) ? this.bCJ.thePlayer.cHn.bO() : co2;
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_19) && ViaLoadingBase.getInstance().getTargetVersion().olderThan(ProtocolVersion.v1_20_2) & (!this.e(InventoryManager.class).jJ() || this.e(InventoryManager.class).kb() || this.e(ChestStealer.class).ky() > 0 || n5 < 5 && n5 > 0 || !(this.bCJ.bgi instanceof m) && !(this.bCJ.bgi instanceof q) && !(this.bCJ.bgi instanceof net.minecraft.client.gui.inventory.h) && !(this.bCJ.bgi instanceof p))) {
            UserConnection userConnection = Via.getManager().getConnectionManager().getConnections().iterator().next();
            ProtocolInfo protocolInfo = userConnection.getProtocolInfo();
            Class clazz = null;
            ProtocolVersion protocolVersion = ViaLoadingBase.getInstance().getTargetVersion();
            PacketWrapperImpl packetWrapperImpl = null;
            if (protocolVersion == ProtocolVersion.v1_20) {
                packetWrapperImpl = (PacketWrapperImpl)PacketWrapper.create(ServerboundPackets1_19_4.CONTAINER_CLICK, userConnection);
                clazz = Protocol1_20To1_19_4.class;
            } else if (protocolVersion == ProtocolVersion.v1_19_4) {
                packetWrapperImpl = (PacketWrapperImpl)PacketWrapper.create(ServerboundPackets1_19_4.CONTAINER_CLICK, userConnection);
                clazz = Protocol1_19_4To1_19_3.class;
            } else if (protocolVersion == ProtocolVersion.v1_19_3) {
                packetWrapperImpl = (PacketWrapperImpl)PacketWrapper.create(ServerboundPackets1_19_3.CONTAINER_CLICK, userConnection);
                clazz = Protocol1_19_3To1_19_1.class;
            } else if (protocolVersion == ProtocolVersion.v1_19_1) {
                packetWrapperImpl = (PacketWrapperImpl)PacketWrapper.create(ServerboundPackets1_19_1.CONTAINER_CLICK, userConnection);
                clazz = Protocol1_19_1To1_19.class;
            } else if (protocolVersion == ProtocolVersion.v1_19) {
                packetWrapperImpl = (PacketWrapperImpl)PacketWrapper.create(ServerboundPackets1_19.CONTAINER_CLICK, userConnection);
                clazz = Protocol1_19To1_18_2.class;
            }
            int n6 = VLBViaDecodeHandler.stateId;
            assert (packetWrapperImpl != null);
            packetWrapperImpl.write(Types.BYTE, (byte)n2);
            packetWrapperImpl.write(Types.VAR_INT, n6);
            packetWrapperImpl.write(Types.SHORT, (short)n3);
            packetWrapperImpl.write(Types.BYTE, (byte)n4);
            packetWrapperImpl.write(Types.VAR_INT, n5);
            packetWrapperImpl.write(Types.VAR_INT, 0);
            packetWrapperImpl.write(Types.BYTE, 0);
            if (protocolInfo.getPipeline().contains(clazz)) {
                packetWrapperImpl.sendToServer(clazz);
            }
        } else {
            this.bCK.t(new r(n2, n3, n4, n5, co3, s2));
        }
        return co2;
    }

    public void I(int n2, int n3) {
        this.bCK.t(new u(n2, n3));
    }

    public void a(co co2, int n2) {
        if (this.bCS.alT()) {
            this.bCK.t(new t(n2, co2));
        }
    }

    public void J(co co2) {
        if (this.bCS.alT() && co2 != null) {
            this.bCK.t(new t(-1, co2));
        }
    }

    public void l(a a2) {
        this.Hf();
        this.bCK.t(new i(net.minecraft.network.play.client.j.daw, b.dtf, net.minecraft.util.ao.dvI));
        a2.abu();
    }

    public boolean Hh() {
        return this.bCS.aws();
    }

    public boolean Hi() {
        return !this.bCS.alT();
    }

    public boolean Hj() {
        return this.bCS.alT();
    }

    public boolean Hk() {
        return this.bCS.alT();
    }

    public boolean Dl() {
        return this.bCJ.thePlayer.isRiding() && this.bCJ.thePlayer.riding instanceof f;
    }

    public boolean Hl() {
        return this.bCS == ao.dDS;
    }

    public ao Hm() {
        return this.bCS;
    }

    public boolean Hn() {
        return this.bCR;
    }
}



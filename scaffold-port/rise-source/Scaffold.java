package rise.module.impl.player;

import hackclient.rise.aef;
import hackclient.rise.afi;
import hackclient.rise.ahj;
import hackclient.rise.aib;
import hackclient.rise.aif;
import hackclient.rise.aih;
import hackclient.rise.aiu;
import hackclient.rise.aka;
import hackclient.rise.bs;
import hackclient.rise.bu;
import hackclient.rise.cb;
import hackclient.rise.ea;
import hackclient.rise.en;
import hackclient.rise.fd;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.Generated;
import net.minecraft.block.BlockAir;
import net.minecraft.client.entity.c;
import net.minecraft.client.multiplayer.d;
import net.minecraft.client.multiplayer.j;
import net.minecraft.item.af;
import net.minecraft.network.aj;
import net.minecraft.network.play.client.h;
import net.minecraft.network.play.client.k;
import net.minecraft.network.play.client.m;
import net.minecraft.util.ao;
import net.minecraft.util.b;
import net.minecraft.util.bt;
import net.minecraft.util.ce;
import net.minecraft.util.da;
import net.minecraft.util.db;
import net.minecraft.world.w;
import org.lwjgl.input.Keyboard;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.MoveInputEvent;
import rise.event.impl.PacketReceiveEvent;
import rise.event.impl.PacketSendEvent;
import rise.event.impl.PostMotionEvent;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.PreUpdateEvent;
import rise.event.impl.StrafeEvent;
import rise.module.Category;
import rise.module.Mode;
import rise.module.Module;
import rise.module.ModuleInfo;
import rise.module.impl.movement.Flight;
import rise.module.impl.movement.Speed;
import rise.module.impl.player.scaffold.VaMode;
import rise.module.impl.player.scaffold.VbMode;
import rise.module.impl.player.scaffold.VcMode;
import rise.module.impl.player.scaffold.VeMode;
import rise.module.impl.player.scaffold.VfMode;
import rise.module.impl.player.scaffold.VgMode;
import rise.module.impl.player.scaffold.VhMode;
import rise.module.impl.player.scaffold.ViMode;
import rise.module.impl.player.scaffold.VjMode;
import rise.module.impl.player.scaffold.VkMode;
import rise.module.impl.player.scaffold.VmMode;
import rise.module.impl.player.scaffold.VnMode;
import rise.module.impl.player.scaffold.VpMode;
import rise.module.impl.player.scaffold.VrMode;
import rise.module.impl.player.scaffold.VsMode;
import rise.module.impl.player.scaffold.VtMode;
import rise.module.impl.player.scaffold.VuMode;
import rise.module.impl.player.scaffold.VvMode;
import rise.module.impl.player.scaffold.VwMode;
import rise.module.impl.player.scaffold.VxMode;
import rise.module.impl.player.scaffold.VyMode;
import rise.module.impl.player.scaffold.VzMode;
import rise.module.impl.player.scaffold.WaMode;
import rise.util.math.Vector2f;
import rise.value.BooleanValue;
import rise.value.ModeValue;
import rise.value.NamedMode;
import rise.value.NumberValue;
import rise.value.RangeValue;

@ModuleInfo(aliases={"module.player.scaffold.name"}, description="module.player.scaffold.description", category=Category.PLAYER)
public class Scaffold
extends Module {
    public float agS;
    @EventLink
    public Listener<ea> agY;
    public BooleanValue agr;
    public static boolean ahf;
    @EventLink
    public Listener<fd> agX;
    public BooleanValue agb;
    public int agL;
    public boolean agH;
    @EventLink(priority=2)
    public Listener<PreUpdateEvent> agZ;
    public BooleanValue afW;
    public static Object[] oO00O0OO0ooO;
    public BooleanValue agj;
    public aka agy;
    @EventLink
    public Listener<PreMotionEvent> agV;
    public int agO;
    public RangeValue age;
    public ModeValue afP;
    @EventLink
    public Listener<StrafeEvent> ahc;
    public int agI;
    public float acs;
    public int acu;
    public RangeValue agg;
    public static Object[] cfr_renamed_1;
    public aib agw;
    public BooleanValue agk;
    public BooleanValue agt;
    public ModeValue afJ;
    public boolean agN;
    public b Yx;
    public BooleanValue afU;
    public RangeValue agl;
    public boolean agx;
    public static Object[] o0Oo000O0oO;
    public RangeValue agn;
    public BooleanValue agd;
    public ModeValue afO;
    public RangeValue afQ;
    public ModeValue afK;
    public int agB;
    public float agT;
    public float jq;
    public int agD;
    public boolean agP;
    public int agF;
    public int agG;
    @EventLink
    public Listener<PacketSendEvent> ahd;
    public NumberValue afS;
    public aib acr;
    @EventLink
    public Listener<MoveInputEvent> aha;
    public double Me;
    public float agz;
    public BooleanValue agp;
    public ModeValue afL;
    public float agQ;
    public RangeValue agm;
    public NumberValue afT;
    public int agJ;
    public BooleanValue agu;
    @EventLink
    public Listener<PostMotionEvent> agW;
    @EventLink
    public Listener<MoveInputEvent> ahb;
    public float agR;
    @EventLink
    public Listener<en> ahe;
    public BooleanValue afZ;
    public BooleanValue agh;
    public int agM;
    public static Object[] cfr_renamed_0;
    public NumberValue ago;
    public int agE;
    public static Object Oo0o00000O00;
    public float agA;
    public int agC;
    public da Yw;
    public float act;
    public ModeValue afM;
    public ModeValue yawOffset;
    public BooleanValue afV;
    public BooleanValue afY;
    public RangeValue agi;
    public RangeValue afR;
    public static int[] O0OoOO0OOOOO;
    @EventLink
    public Listener<PacketReceiveEvent> agU;
    public ModeValue afN;
    public BooleanValue agf;
    public BooleanValue afX;
    public int agK;
    public BooleanValue aga;
    public float jp;
    public BooleanValue agv;
    public BooleanValue agq;
    public BooleanValue agc;
    public static int ooo00oo0Ooo0;
    public static boolean o0o000o0O0o0;
    public strictfp void kk() {
        int n2 = 917607971;
        ooo00oo0Ooo0 = n2;
        if (989274759 < ooo00oo0Ooo0) {
        }
        Object[] objectArray = new Object[52];
        Integer n3 = null;
        Integer n4 = null;
        Integer n5 = null;
        Integer n6 = null;
        Integer n7 = null;
        Integer n8 = null;
        Integer n9 = null;
        Integer n10 = null;
        Long l2 = null;
        Integer n11 = null;
        Integer n12 = null;
        Integer n13 = null;
        Integer n14 = null;
        Integer n15 = null;
        Long l3 = null;
        Integer n16 = null;
        Integer n17 = null;
        Integer n18 = null;
        Integer n19 = null;
        Long.valueOf(3636714682925411568L);
        Long.valueOf(7389712544539007766L);
        Long.valueOf(7100756557467902600L);
        l2 = -4562075659081197047L;
        l3 = 5740918573583401345L;
        n14 = 69;
        n14 = n14 + 54;
        n14 = n14 - 107;
        if (!((Mode)this.afP.getValue()).getName().equals((String)o0Oo000O0oO[n14]) && this.acu == 0) {
            n17 = -17;
            n17 = n17 ^ 0x45;
            n17 = n17 + 86;
            Scaffold.mc.aIp.cgJ.ay(n17 != 0);
        }
        n9 = 64;
        n9 = n9 + -85;
        n9 = n9 + 22;
        this.agB -= n9.intValue();
        if (!((Boolean)this.agk.getValue()).booleanValue() && this.agE <= 0) {
            return;
        }
        n4 = 127;
        n4 = n4 + -5;
        n4 = n4 - 90;
        long l4 = l2;
        n16 = -104;
        n16 = n16 + 2;
        n16 = n16 ^ 0xFFFFFFBA;
        l2 = l4 ^ ((long)this.agl.ww().intValue() << n4 ^ l4) & -1L << n16;
        long l5 = l2;
        n8 = 255;
        n8 = n8 + -100;
        n8 = n8 - 123;
        l2 = l5 ^ ((long)this.afR.ww().intValue() ^ l5) & -1L >>> n8;
        n6 = -67;
        n6 = n6 ^ 0xFFFFFFB6;
        n6 = n6 ^ 0x2B;
        long l6 = l3;
        n7 = -49;
        n7 = n7 ^ 0x1D;
        n7 = n7 ^ 0xFFFFFFF2;
        l3 = l6 ^ ((long)this.agm.ww().intValue() << n6 ^ l6) & -1L << n7;
        if (this.agE > 0) {
            n5 = 2;
            n5 = n5 ^ 0xFFFFFFC7;
            n5 = n5 + 60;
            this.agE -= n5.intValue();
            n15 = -44;
            n15 = n15 + 88;
            n15 = n15 + -44;
            this.agB = n15;
            n13 = -35;
            n13 = n13 ^ 0xFFFFFFC7;
            n13 = 0;
            this.agC = 0;
        }
        if (this.agB >= 0) {
            n3 = -54;
            n3 = n3 ^ 9;
            n3 = n3 ^ 0xFFFFFFC2;
            Scaffold.mc.aIp.cgJ.ay(n3 != 0);
            return;
        }
        if (this.acu > 0) {
            n19 = 58;
            n19 = n19 - 87;
            n19 = n19 + 61;
            this.agB = (int)((double)((int)(l3 >>> n19)));
        }
        if (this.acu <= 0) {
            n18 = 0;
            n18 = n18 + 28;
            n18 = n18 ^ 0x3C;
            n11 = -93;
            n11 = n11 - -82;
            n11 = n11 ^ 0xFFFFFFD5;
            if (!(aih.p(Scaffold.mc.thePlayer.ud * (double)((int)(l2 >>> n18)), Double.longBitsToDouble(-4633058300709086167L), Scaffold.mc.thePlayer.uf * (double)((int)(l2 >>> n11))) instanceof BlockAir)) return;
        }
        if (this.agC > 0) return;
        n12 = -10;
        n12 = n12 ^ 0xFFFFFFDC;
        n12 = n12 ^ 0xA;
        n10 = 102;
        n10 = n10 - 0;
        n10 = n10 + -70;
        this.agB = (int)((double)((int)(l2 >>> n12) + (int)l2.longValue() + (int)(l3 >>> n10)));
        this.agC = this.agn.ww().intValue();
    }
    public static strictfp Object o0Oo000O0oO(Object[] object) {
        Object object2;
        int n2 = 707760329;
        ooo00oo0Ooo0 = n2;
        if (1703909794 <= ooo00oo0Ooo0) {
            n2 = 667145289;
        }
        if (123056492 < (ooo00oo0Ooo0 = 51747435)) {
            ooo00oo0Ooo0 = -1472012378;
        }
        n2 = 1087705845;
        ooo00oo0Ooo0 = 1087705845;
        if (1664815063 < ooo00oo0Ooo0) {
            throw null;
        }
        Object[] objectArray = new Object[45];
        byte[] byArray = null;
        byte[] byArray2 = null;
        char[] cArray = null;
        byte[] byArray3 = null;
        Integer n3 = null;
        PBEKeySpec pBEKeySpec = null;
        Integer n4 = null;
        char[] cArray2 = null;
        SecretKeyFactory secretKeyFactory = null;
        Integer n5 = null;
        Cipher cipher = null;
        byte[] byArray4 = null;
        byte[] byArray5 = null;
        byte[] byArray6 = null;
        byte[] byArray7 = null;
        byte[] byArray8 = null;
        byte[] byArray9 = null;
        Integer n6 = null;
        byte[] byArray10 = null;
        char[] cArray3 = null;
        String string = null;
        Integer n7 = null;
        Integer n8 = null;
        Integer n9 = null;
        n8 = (int)((Integer)object[1]);
        string = (String)object[2];
        Object object3 = object[0];
        Object[] objectArray2 = oO00O0OO0ooO;
        if (oO00O0OO0ooO == null) {
            objectArray2 = oO00O0OO0ooO = new Object[1];
        }
        if ((object2 = objectArray2[n8]) == null) {
            Object[] objectArray3 = (Object[])object3;
            if (objectArray3 == null) {
                Object[] objectArray4 = new Object[1];
                cfr_renamed_1 = objectArray4;
                objectArray3 = objectArray4;
                byArray3 = new byte[16];
                byArray3[7] = -113;
                byArray3[5] = 15;
                byArray3[11] = -113;
                byArray3[6] = -47;
                byArray3[0] = -80;
                byArray3[12] = 83;
                byArray3[13] = -119;
                byArray3[15] = -71;
                byArray3[14] = -32;
                byArray3[3] = 101;
                byArray3[4] = 40;
                byArray3[8] = -85;
                byArray3[10] = 43;
                byArray3[9] = -105;
                byArray3[1] = 94;
                byArray3[2] = -65;
                objectArray4[0] = byArray3;
            }
            byArray10 = (byte[])objectArray3[0];
            if (Oo0o00000O00 == null) {
                byArray4 = new byte[32];
                byArray4[28] = 102;
                byArray4[24] = -70;
                byArray4[21] = 77;
                byArray4[22] = 50;
                byArray4[26] = 104;
                byArray4[16] = -73;
                byArray4[31] = 55;
                byArray4[3] = 76;
                byArray4[5] = -19;
                byArray4[30] = -59;
                byArray4[4] = -37;
                byArray4[8] = -7;
                byArray4[0] = 79;
                byArray4[17] = 21;
                byArray4[29] = 10;
                byArray4[25] = -4;
                byArray4[23] = -121;
                byArray4[18] = -51;
                byArray4[6] = -48;
                byArray4[11] = 109;
                byArray4[12] = -9;
                byArray4[2] = 119;
                byArray4[13] = -109;
                byArray4[1] = 20;
                byArray4[14] = -54;
                byArray4[9] = -86;
                byArray4[27] = 19;
                byArray4[15] = -27;
                byArray4[20] = -96;
                byArray4[19] = 120;
                byArray4[7] = 52;
                byArray4[10] = 76;
                byArray8 = new byte[byArray10.length + byArray4.length];
                System.arraycopy(byArray10, 0, byArray8, 0, byArray10.length);
                System.arraycopy(byArray4, 0, byArray8, byArray10.length, byArray4.length);
                Object object4 = Scaffold.cfr_renamed_1()[1];
                if (object4 == null) {
                    cArray = "\ub710\uc9a6\ub76d\uc9a4\uc99a\ub7d6\ub779\ub647\uc994\ub648\uc9a8\uc98b\uc9bf\ub645\ub775\uc9a8\uc99f\ub7cf".toCharArray();
                    n9 = 0;
                    while (n9 < 18) {
                        n4 = cArray[n9];
                        n4 = n4 ^ 0x7650;
                        n4 = n4 + 17749;
                        n4 = n4 - 41958;
                        n4 = n4 + 50503;
                        n4 = n4 - 1112;
                        n4 = n4 ^ 0x22E8;
                        n4 = n4 - 40089;
                        n4 = n4 ^ 0xA25A;
                        n4 = n4 + 19372;
                        n4 = n4 + 51390;
                        n4 = n4 ^ 0x397E;
                        n4 = n4 ^ 0xE3DF;
                        cArray[n9.intValue()] = (char)n4.intValue();
                        Object[] objectArray5 = objectArray;
                        n9 = n9 + 1;
                    }
                    object4 = Scaffold.cfr_renamed_1()[1] = new String(cArray);
                }
                secretKeyFactory = SecretKeyFactory.getInstance((String)object4);
                byArray7 = new byte[16];
                byArray7[13] = -2;
                byArray7[11] = 69;
                byArray7[10] = -50;
                byArray7[2] = -18;
                byArray7[6] = 1;
                byArray7[5] = -71;
                byArray7[7] = 32;
                byArray7[14] = -28;
                byArray7[9] = 63;
                byArray7[12] = -82;
                byArray7[15] = 40;
                byArray7[8] = -113;
                byArray7[0] = 113;
                byArray7[1] = 17;
                byArray7[4] = 30;
                byArray7[3] = 110;
                pBEKeySpec = new PBEKeySpec(new String(byArray8, StandardCharsets.UTF_8).toCharArray(), byArray7, 10, 256);
                byArray5 = secretKeyFactory.generateSecret(pBEKeySpec).getEncoded();
                Object object5 = Scaffold.cfr_renamed_1()[2];
                if (object5 == null) {
                    cArray2 = "\u1aae\u1aa2\u1ab4".toCharArray();
                    n5 = 0;
                    while (n5 < 3) {
                        n3 = cArray2[n5];
                        n3 = n3 ^ 0xC21;
                        n3 = n3 - 53091;
                        n3 = n3 ^ 0xA908;
                        n3 = n3 ^ 0xE82E;
                        n3 = n3 - 26608;
                        n3 = n3 - 53299;
                        n3 = n3 ^ 0xEAD3;
                        n3 = n3 - 28532;
                        n3 = n3 + 37428;
                        n3 = n3 - 20662;
                        n3 = n3 - 63932;
                        n3 = n3 + 25788;
                        n3 = n3 ^ 0x647F;
                        cArray2[n5.intValue()] = (char)n3.intValue();
                        Object[] objectArray6 = objectArray;
                        n5 = n5 + 1;
                    }
                    String string2 = new String(cArray2);
                    object5 = string2;
                    Scaffold.cfr_renamed_1()[2] = string2;
                }
                Oo0o00000O00 = new SecretKeySpec(byArray5, (String)object5);
            }
            byArray6 = Base64.getDecoder().decode(string);
            byArray = Arrays.copyOfRange(byArray6, 0, 16);
            byArray2 = Arrays.copyOfRange(byArray6, 16, byArray6.length);
            Object object6 = Scaffold.cfr_renamed_1()[3];
            if (object6 == null) {
                cArray3 = "\ua041\ua04d\ua07b\ua35f\ua04b\ua04e\ua04b\ua35f\ua07c\ua073\ua04b\ua07b\ua35d\ua07c\ua061\ua068\ua068\ua069\ua012\ua017".toCharArray();
                n6 = 0;
                while (n6 < 20) {
                    n7 = cArray3[n6];
                    n7 = n7 ^ 0x2CA4;
                    n7 = n7 - 31686;
                    n7 = n7 + 15270;
                    n7 = n7 ^ 0x676C;
                    n7 = n7 ^ 0x4BCE;
                    n7 = n7 ^ 0x6E2F;
                    n7 = n7 + 46383;
                    n7 = n7 ^ 0xB1D2;
                    n7 = n7 ^ 0x71B2;
                    n7 = n7 - 42549;
                    n7 = n7 + 52957;
                    n7 = n7 ^ 0xF1D;
                    n7 = n7 + 56223;
                    cArray3[n6.intValue()] = (char)n7.intValue();
                    Object[] objectArray7 = objectArray;
                    n6 = n6 + 1;
                }
                object6 = Scaffold.cfr_renamed_1()[3] = new String(cArray3);
            }
            cipher = Cipher.getInstance((String)object6);
            cipher.init(2, (Key)((SecretKey)Oo0o00000O00), new IvParameterSpec(byArray));
            byArray9 = cipher.doFinal(byArray2);
            object2 = Scaffold.oO00O0OO0ooO[n8.intValue()] = new String(byArray9, StandardCharsets.UTF_8);
        }
        return object2;
    }

    /*
     * Unable to fully structure code
     */
    public strictfp float c(float var1_1, float var2_2, float var3_3) {
        block10: {
            if (Scaffold.o0o000o0O0o0) {
                throw null;
lbl3:
                // 1 sources

                while (true) {
                    var24_4[13] = 140;
                    var24_4[13] = (Integer)var24_4[13] - 83;
                    var24_4[13] = (Integer)var24_4[13] ^ 25;
                    var24_4[16] = v0 ^ ((long)v1 << (Integer)var24_4[15] ^ v0) & -1L << (Integer)var24_4[13];
                    var24_4[8] = Float.valueOf(bt.I(((Float)var24_4[8]).floatValue(), -var3_3, var3_3));
                    return var1_1 + ((Float)var24_4[8]).floatValue();
                }
            }
            var24_4 = new Object[20];
            var24_4[16] = -6466820149392686991L;
            var24_4[8] = Float.valueOf(bt.bN(var2_2 - var1_1));
            var24_4[4] = bt.aJ(var2_2);
            var24_4[11] = bt.aJ(Math.toDegrees(Math.atan2(Scaffold.mc.thePlayer.uf, Scaffold.mc.thePlayer.ud)) - Double.longBitsToDouble(440541444290559937L ^ 5065316049134993345L));
            var24_4[7] = Float.valueOf(Scaffold.mc.thePlayer.pl);
            var24_4[3] = Float.valueOf(bt.bN(((Float)var24_4[7]).floatValue()));
            var22_5 = -795940084;
            block6: while (true) {
                switch (var22_5) {
                    default: {
                        if (!(Math.abs(((Float)var24_4[3]).floatValue() % 90.0f) <= 15.0f)) {
                            var22_5 = 750219498;
                            continue block6;
                        }
                        ** GOTO lbl27
                    }
                    case 750219498: {
                        if (!(Math.abs(((Float)var24_4[3]).floatValue() % 90.0f) >= 75.0f)) ** GOTO lbl32
lbl27:
                        // 2 sources

                        var24_4[12] = 36;
                        var24_4[12] = (Integer)var24_4[12] ^ 100;
                        var24_4[12] = (Integer)var24_4[12] - 63;
                        v1 = (Integer)var24_4[12];
                        break block10;
lbl32:
                        // 1 sources

                        var22_5 = -840032042;
                        continue block6;
                    }
                    case -840032042: 
                }
                break;
            }
            var17_6 = -1;
            var17_6 ^= 24;
            var23_7 = 1859358547;
            v1 = var17_6 ^= -25;
        }
        var24_4[15] = 3;
        var24_4[15] = (Integer)var24_4[15] - -59;
        var24_4[15] = (Integer)var24_4[15] + -30;
        v0 = (Long)var24_4[16];
        ** while (true)
    }

    /*
     * Exception decompiling
     */
    public strictfp void jF() {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Extractable last case doesn't follow previous, and can't clone.
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.SwitchReplacer.examineSwitchContiguity(SwitchReplacer.java:611)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.SwitchReplacer.replaceRawSwitches(SwitchReplacer.java:94)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:517)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    /*
     * Recovered potentially malformed switches.  Disable with '--allowmalformedswitch false'
     * Unable to fully structure code
     * Enabled aggressive block sorting
     */
    static {
        block20: {
            block21: {
                var94 = 1370460231;
                if (574778647 >= var94) {
                    var94 = -1385510498;
                }
                if (Scaffold.o0o000o0O0o0) {
                    63 & 1;
                }
                if (1105882466 >= (var94 = 1967817378)) {
                }
                var92_6 = new Object[92];
                var88_2 = 17755345;
                break block21;
lbl13:
                // 1 sources

                while (true) {
                    var92_6[83] = 69;
                    var92_6[83] = (Integer)var92_6[83] - -83;
                    var92_6[83] = (Integer)var92_6[83] + -120;
                    if ((int)((Long)var92_6[5] >>> (Integer)var92_6[83]) < (int)((Long)var92_6[87]).longValue()) {
                        var92_6[88] = 81;
                        var92_6[88] = (Integer)var92_6[88] + -49;
                        var92_6[88] = (Integer)var92_6[88] - 0;
                        var92_6[28] = -160;
                        var92_6[28] = (Integer)var92_6[28] ^ -17;
                        var92_6[28] = (Integer)var92_6[28] - 111;
                        ((char[])var92_6[45])[(int)(((Long)var92_6[5]).longValue() >>> ((Integer)var92_6[88]).intValue())] = ((char[])var92_6[61])[(int)((Long)var92_6[34]).longValue() + (int)((Long)var92_6[5] >>> (Integer)var92_6[28])];
                        var92_6[5] = (Long)var92_6[5] + 0x100000000L;
                        continue;
                    }
                    var92_6[23] = 5;
                    var92_6[23] = (Integer)var92_6[23] - 37;
                    var92_6[23] = (Integer)var92_6[23] - -64;
                    var92_6[9] = (Long)var92_6[9] + 0x100000000L;
                    Scaffold.o0Oo000O0oO[(int)(((Long)var92_6[9]).longValue() >>> ((Integer)var92_6[23]).intValue())] = new String((char[])var92_6[45]);
                    v0 = (Long)var92_6[34];
                    var92_6[25] = -77;
                    var92_6[25] = (Integer)var92_6[25] + 108;
                    var92_6[25] = (Integer)var92_6[25] ^ 63;
                    var92_6[34] = v0 ^ ((long)((int)((Long)var92_6[34]).longValue() + (int)((Long)var92_6[87]).longValue()) ^ v0) & -1L >>> (Integer)var92_6[25];
                    break block20;
                    break;
                }
lbl38:
                // 1 sources

                while (true) {
                    if (!Scaffold.class.desiredAssertionStatus()) {
                        var92_6[24] = 86;
                        var92_6[24] = (Integer)var92_6[24] ^ -124;
                        var92_6[24] = (Integer)var92_6[24] - -47;
                        v1 = (Integer)var92_6[24];
                    } else {
                        var82_1 = -229;
                        var82_1 -= -121;
                        v1 = var82_1 ^= -108;
                    }
                    Scaffold.ahf = v1;
                    var90_4 = 2012363090;
lbl50:
                    // 2 sources

                    while (true) {
                        switch (var90_4) {
                            default: {
                                v2 = (Long)var92_6[34];
                                var92_6[79] = 73;
                                var92_6[79] = (Integer)var92_6[79] - 2;
                                var92_6[79] = (Integer)var92_6[79] - 70;
                                var92_6[43] = 78;
                                var92_6[43] = (Integer)var92_6[43] + -88;
                                var92_6[43] = (Integer)var92_6[43] - -42;
                                var92_6[34] = v2 ^ (v2 ^ v2 + (long)((Integer)var92_6[79]).intValue()) & -1L >>> (Integer)var92_6[43];
                                v3 = (Long)var92_6[69];
                                var92_6[39] = 107;
                                var92_6[39] = (Integer)var92_6[39] - 91;
                                var92_6[39] = (Integer)var92_6[39] - -16;
                                var92_6[69] = v3 ^ ((long)((char[])var92_6[61])[(int)((Long)var92_6[34]).longValue()] ^ v3) & -1L >>> (Integer)var92_6[39];
                                v4 = (Long)var92_6[34];
                                var92_6[44] = -84;
                                var92_6[44] = (Integer)var92_6[44] + -1;
                                var92_6[44] = (Integer)var92_6[44] - -86;
                                var92_6[52] = 169;
                                var92_6[52] = (Integer)var92_6[52] - 66;
                                var92_6[52] = (Integer)var92_6[52] - 71;
                                var92_6[34] = v4 ^ (v4 ^ v4 + (long)((Integer)var92_6[44]).intValue()) & -1L >>> (Integer)var92_6[52];
                                var92_6[3] = 7;
                                var92_6[3] = (Integer)var92_6[3] ^ 104;
                                var92_6[3] = (Integer)var92_6[3] + -79;
                                v5 = (Long)var92_6[53];
                                var92_6[78] = -196;
                                var92_6[78] = (Integer)var92_6[78] - -69;
                                var92_6[78] = (Integer)var92_6[78] ^ -95;
                                var92_6[53] = v5 ^ ((long)((char[])var92_6[61])[(int)((Long)var92_6[34]).longValue()] << (Integer)var92_6[3] ^ v5) & -1L << (Integer)var92_6[78];
                                var92_6[36] = -40;
                                var92_6[36] = (Integer)var92_6[36] - -87;
                                var92_6[36] = (Integer)var92_6[36] + -31;
                                var92_6[30] = -75;
                                var92_6[30] = (Integer)var92_6[30] ^ -2;
                                var92_6[30] = (Integer)var92_6[30] ^ 107;
                                v6 = (Long)var92_6[87];
                                var92_6[46] = 102;
                                var92_6[46] = (Integer)var92_6[46] ^ 117;
                                var92_6[46] = (Integer)var92_6[46] + 13;
                                var92_6[87] = v6 ^ ((long)((int)((Long)var92_6[69]).longValue() << (Integer)var92_6[36] | (int)((Long)var92_6[53] >>> (Integer)var92_6[30])) ^ v6) & -1L >>> (Integer)var92_6[46];
                                var92_6[45] = new char[(int)((Long)var92_6[87]).longValue()];
                                var91_5 = 459799409;
                                v7 = (Long)var92_6[5];
                                var92_6[63] = 217;
                                var92_6[63] = (Integer)var92_6[63] ^ 82;
                                var92_6[63] = (Integer)var92_6[63] - 107;
                                var92_6[5] = v7 ^ (0L ^ v7) & -1L << (Integer)var92_6[63];
                                ** continue;
                            }
                            case 2012363090: 
                        }
                        return;
                    }
                    break;
                }
            }
            block11: while (true) {
                switch (var88_2) {
                    default: {
                        Scaffold.Oo0o00000O00();
                        var88_2 = 487991867;
                        continue block11;
                    }
                    case 487991867: 
                }
                break;
            }
            var92_6[85] = 8817512699821374345L;
            var92_6[22] = -4973603622344038451L;
            var92_6[8] = -4217526177548428533L;
            var92_6[67] = -1798928794125959508L;
            var92_6[59] = -3144965346520726645L;
            var92_6[20] = 8867819083219041528L;
            var92_6[47] = -319130966545160858L;
            var92_6[69] = -5364835736178497868L;
            var92_6[53] = -6126539696220154312L;
            var92_6[57] = -739033697011632914L;
            var92_6[87] = -2442406316076401025L;
            var92_6[34] = 3156525353879008766L;
            var92_6[5] = -4250559617276363390L;
            var92_6[9] = 759584902887108111L;
            var92_6[71] = 11;
            var92_6[71] = (Integer)var92_6[71] ^ 55;
            var92_6[71] = (Integer)var92_6[71] - -109;
            var89_3 = -224993746;
            Scaffold.o0Oo000O0oO = new Object[((Integer)var92_6[71]).intValue()];
            v8 = (Long)var92_6[9];
            var92_6[72] = 59;
            var92_6[72] = (Integer)var92_6[72] + 67;
            var92_6[72] = (Integer)var92_6[72] + -94;
            var92_6[9] = v8 ^ (0L ^ v8) & -1L << (Integer)var92_6[72];
            v9 = new Object[3];
            v9[0] = Scaffold.cfr_renamed_1;
            v9[1] = 0;
            v10 = Scaffold.cfr_renamed_1()[0];
            if (v10 == null) {
                var92_6[51] = "\u3802\u3825\u338f\u3809\u3809\u3804\ud7c9\u3815\u383c\u380a\u3809\u3306\u383d\u3807\ud7c9\u3869\u383c\u33b3\u3809\u3869\u3817\u3241\u382a\u33be\u3802\u382a\u3241\u3826\u3303\u338f\u3818\u38ef\u3825\u3817\u33b0\u38ef\ud7c2\u3821\u383a\u383c\u3867\u3802\u3826\u3819\u383a\u3820\u3826\u33b6\u3807\u3819\u3820\u3810\u3300\u3823\u3812\u3804\u3809\u3241\u3814\u3826\u383a\u380a\u3812\u3825\u3303\u381b\ud7c2\u33b6\u33bb\u3245\ud7c2\u3818\u33b8\u3807\u3821\u331f\u33b6\u33bb\u3813\u33b4\u3812\u3813\u3300\u3241\ud7c2\u3826\u33b5\u3245\u33b3\u3812\u3246\u3864\u33b0\u3303\u331f\u3804\u3240\u33b4\u331c\u325a\u331f\u383d\u325c\u382a\u33b6\u383c\u381b\u3245\u3814\u33b3\u3815\u383d\u33b9\u3814\u3814\u383d\u3802\u3813\u325f\u3825\u383c\u3306\u3816\u3867\u33b4\u3303\u33b2\u325a\u3807\u33b5\ud7c9\u33b8\u3825\u3862\u33b4\u33b8\u33b2\u3303\u381b\u3378\u3804\u3818\u3869\u3826\u3816\u3804\u380a\u3810\u33b0\u382a\u380a\u33b9\u383d\u3305\u3819\u383f\u33bb\u33b3\u3867\u325a\u3300\ud7c9\u383a\u3240\u3300\ud7c9\u3869\u3810\u381b\u3862\u3241\u383a\u3240\u382a\u3303\u3813\u383f\u3867\u3867\u383d\u325c\ud7c4\u383a\ud7c2\u33be\ud7c2\u33b4\u382a\u3241\u3812\u33b7\u3378\u3804\u338f\u383d\u3867\u3869\u3869\u33b9\u33b0\u3815\u3809\u3819\u3820\u33b9\u3818\u325c\u33b3\u3245\u325a\u3869\u3816\u3867\u33b7\ud7c9\u325d\u380a\u3240\ud7c4\u3804\u3816\u380a\u33b5\u325d\ud7c4\u382a\u3809\u3804\u325f\u3303\ud7ea\u383a\u3306\u33be\u33bb\u3241\u33bb\u3246\u3816\u383d\u3821\u382a\u381e\ud7ea\u3246\u383a\u3820\u33b2\u3815\u383d\u3867\u325c\ud7c4\u331c\u380a\u3867\u33b7\u325f\u33b5\u383d\u3378\u381b\u331f\u33b7\u3807\u338f\u3823\u3300\u33b5\u33b0\u3303\u3814\u3303\u380a\u33b4\u33b0\u325d\u3807\u33b4\u383f\u3813\u3816\u3305\u33b9\u383c\u3823\u381e\u3300\u380a\u3819\u3378\u325c\u383f\u3823\u3809\u3820\u3243\u38ef\u38ef\ud7c2\u338f\u33b3\u3813\u3802\u33be\u325a\u3862\u38ef\u3305\u3864\u3814\u3825\u33b0\u3812\u3378\u3807\u331f\u3802\u3241\u3864\u33b8\u33b8\u3823\u33b8\u3818\u325c\u3807\u33b3\u3804\u3816\u3802\u3807\u33bb\ud7ea\ud7c4\u383c\u325f\u3823\u38ef\u381e\u331f\u3378\u3869\u3300\u3809\u33b4\u33b7\u382a\ud7ea\u3246\u325a\u3816\u3867\u381e\u3821\u3305\u383a\u33bb\u33b5\u3809\u38ef\u33b9\ud7ea\u33b3\u3823\u382a\u3243\u3240\u3823\u3802\u3306\u3816\u3245\u3814\u3814\u325c\u38ef\ud7c2\u3864\u331f\u3823\u3816\u33b7\u3819\u383f\u3306\u331f\u3243\u3241\u3305\u325f\u33bb\u3378\u3300\u3802\u3815\u3245\u3819\u3810\u325f\u3823\u325a\u3821\ud7ea\u325d\u338f\u383d\u3816\u331f\u3378\u3378\u383d\u3810\u3826\u3306\u3826\u3241\u325f\u325c\u3869\u3818\u3305\u3378\u3243\u33b7\u3817\u3816\u383d\u3812\u325d\u3821\u3246\u3243\u3300\ud7c4\u33b6\u383d\u3306\u3862\u33b0\u3246\u3826\u382a\u33b7\ud7c4\u33be\u33b8\u383d\u383a\u3817\u3815\u3869\u3802\u38ef\u3825\u3869\u3826\u325d\u3243\u3245\u3869\u3864\u3821\u3802\u33be\u3246\u3817\u3819\u3867\u3300\u3246\u3241\u381e\u3804\u331c\u381e\u33b5\u3821\u3804\u325f\u3819\u380a\u3819\u3809\u3303\u3867\u3825\u325c\u383a\u383d\u380a\u331c\u3867\u33b2\u3809\u3818\u380a\u3378\u33b6\u33b3\u33b5\u3306\u3814\u3303\u3867\u33b6\u325c\u3869\u33b7\u325f\u3812\u33b0\u33b3\u3378\u33bb\ud7ea\u3825\u3804\u3810\u3821\u33b6\u383d\u3823\u33b7\u3240\u3821\u3306\u383f\u380a\ud7c2\u3814\u3821\u382a\u383a\u3864\u3301\u33b6\u3804\u3819\u3825\u3305\u3306\u3807\u3240\u33b8\u3816\u381b\u3243\u3815\u3245\u3821\ud7c9\u331f\u38ef\u3818\u33b5\u3303\u3821\u3378\u3817\u338f\u383d\u3240\ud7c4\u3820\u33b4\u3243\u3807\u325a\u3240\u331c\u3306\u3241\u3820\u3804\u3306\u33b8\u3243\u3812\u3243\ud7c4\u3807\u381b\u325f\u383c\u383f\u383c\ud7c4\u3305\u33b2\u3825\u3814\u3821\u3241\u3378\u3243\u3817\u3300\u3241\u33b2\u3300\ud7ea\u33b9\u3306\u38ef\u38ef\u33b2\u3303\u3378\u3819\u381e\u3301\u3814\u3378\u33b9\u3869\u33b6\u33b2\u381e\u3819\u3818\u33be\u338f\u380a\u3809\u3820\u33b4\u331c\u3823\ud7c4\u3826\u3821\ud7ea\u325c\u33b7\u33b7\u33b9\u3809\u381b\ud7c2\u3813\u381e\ud7ea\u3241\u3819\u33b3\u3813\u331f\u33b7\u3240\u3306\u3818\u3246\u33b9\u3826\u3867\u33be\u3816\u3245\u33bb\u325d\u3300\u3306\ud7ea\u33b6\ud7c9\ud7c2\u3802\u3240\u3809\u325f\u33b7\u3826\u325c\u33b6\u3814\u3813\u33b5\ud7c2\u3246\u3814\u3809\u3812\u3867\ud7ea\u33b5\u380a\u3810\u3821\u33bb\u3810\u33b2\u3804\u3804\u3241\u33b4\u3818\ud7c9\u3826\u38ef\u381b\u3813\u3306\u331c\u338f\u3300\u3813\u3817\u3815\u33b8\u33b0\u383f\u3826\u3809\u3869\u3802\u3378\u33bb\u338f\u383c\u3819\u3807\u3823\u3818\u3820\u33bb\u33b0\u325a\u3241\u33b3\u3816\u380a\u3246\u3300\u33b6\u33b8\ud7ea\u331f\u3869\u325a\u33b3\u3820\u3814\u325c\u3819\u3802\u3802\u33b7\u3802\u3810\u33b4\u33bb\u3241\u331c\u33b5\u380a\ud7c2\u383a\u33b0\u325a\u38ef\u33b2\u3812\u3812\ud7c2\u383a\u3814\u325a\u3819\u33b6\u33b3\u383c\u33bb\u3241\u3378\u3826\u3306\u3240\u3300\u33be\u3825\u3245\u33b6\u338f\u325c\u383c\ud7c2\u3306\u3862\u38ef\u3802\u383d\u383c\u383c\u3305\u33b2\u38ef\u383f\u3246\u3815\u33b4\u3867\u3303\u3241\u3241\u3826\u3862\u3300\u3301\u33b7\u381e\u3864\u3813\u3862\u3819\u383c\u3243\ud7c2\u33b3\u33b4\u3864\u3807\u325f\u3240\u33b4\ud7ea\u33b9\u3306\u325c\u33b4\u325f\u33b8\u3245\u3815\u38ef\u3814\u33be\u33b6\u3823\u383d\u331f\u33b0\u3818\u3825\u33b9\u3816\u38ef\u3810\u331f\u3300\u3306\u3300\u3819\u3818\u382a\u3825\u381b\u3825\u3826\u325a\u325d\u3305\u3867\u33b0\ud7c2\u3817\u33bb\u33b2\u381e\u338f\u3818\u325d\u33b6\u33b4\u3819\u381e\u33b3\u3823\u33b9\u3245\u3301\u38ef\u3818\u3864\u3303\u3817\u3826\u33be\u3867\u33b3\u3303\u3820\u33b6\u3821\u381e\u3814\u381b\u338f\ud7ea\ud7c9\u3869\u3817\u381e\u3809\u3245\ud7c9\u331c\u3814\u3826\u381b\u331c\u3240\u3240\u3821\u33b6\u33b2\u325c\u3825\u3864\u325a\u383a\u3809\u3814\u3246\u3378\u3816\u33b3\u331f\u3815\u33b9\u3378\u3305\u3814\u338f\u3243\u3867\u33b8\u383c\ud7ea\u3809\ud7c4\u3241\u3821\u338f\u3825\u3814\ud7ea\u33b4\u38ef\u383f\u33b8\u383f\u3869\u338f\u3821\u3305\u3809\u33b4\u33be\u383f\u382a\u33b9\u3241\ud7c4\u331f\u3820\u3826\u325f\u3810\u33b7\u33b7\u38ef\u3810\u3305\u381b\u3862\u325a\u33b2\u33bb\u33b2\u3804\u33b3\u325d\u3817\u383d\u3823\u3804\u331c\u3245\u3823\u33b5\ud7ea\u3240\u3306\u3804\u3807\u3241\u3245\u3826\u3802\u3246\u3817\u3303\u3301\u3245\ud7c4\u3810\u331c\u3821\ud7c2\u3815\ud7c4\u3245\u331c\u3821\u3246\u325c\u3802\u3809\u331f\u3303\u33b3\u325f\u3305\u3241\u3241\u3301\u33be\ud7c2\u33be\u33b5\u33be\u33b9\u3862\u3867\u33be\u3823\u325d\u381b\u3819\u3818\u380a\u381b\u3305\u3817\u3802\u3826\u3814\u3817\u3815\u3826\u3810\u383f\u3802\u33b7\u3818\u3305\u3817\u3378\u33b4\u3303\u3814\u380a\u383c\u3306\u3816\u3819\ud7c4\u3821\u383a\u3243\u3378\u33bb\u3820\u381b\u3826\u331f\u3300\u3804\ud7ea\u3301\u3818\ud7ea\u3300\u3802\u3826\u3864\u3305\u3241\u3816\u3826\u3814\u3809\u331f\ud7c4\u33bb\u3818\u33b4\u33b3\u3240\u325d\u331f\u3815\u3305\u38ef\u33b6\u3867\u33b9\u33bb\u3303\u383a\u331c\u3815\u3814\u3823\u33b9\u3243\u33b0\u3810\u3821\u3802\u33b7\u3826\u3240\u3240\u3305\u383f\u3869\u33b8\u383f\u3825\u3245\u3816\u3821\u338f\u3814\ud7ea\u383f\u3867\u3823\u38ef\ud7c9\u3807\u3815\u33b4\u33b0\u3815\u3241\u3817\u3826\u3378\u383a\u3823\u3246\u3300\u331c\u3245\u3867\u382a\u33bb\u38ef\u325c\u3826\u3246\u325f\u33bb\u33b2\u325a\u3300\u3825\u325d\u3300\u3240\u3818\u325f\u33b2\u3816\u33b7\u3306\u33b9\u3818\ud7c4\u3813\u3869\u3804\u3804\u3864\u3306\u33b3\u325d\u3810\u3241\u3804\u383a\u383f\u3869\u33b6\u3826\u3804\u381e\u33b4\u331c\u3245\u3804\u3804\u3246\u3819\u3243\u3306\u3816\ud7c4\u38ef\u3804\u383c\u3378\u3862\u33b2\u3818\u3804\u3819\u33b0\u3246\u33b8\u33bb\u33b7\u3301\u3815\u3306\u3241\u38ef\u3804\u3862\u3821\u3819\u3306\u3240\u33b9\u33bb\u3862\u33bb\u3246\u382a\u338f\u3864\u3241\u3869\u3817\u33b4\u3812\u383d\u3818\ud7ea\u33be\u3818\u3804\u3306\u33b6\u3814\u331f\u382a\u3816\u338f\u3821\u33b8\u3807\u325c\u3300\u3240\u331f\u3246\u3810\u3862\u33b6\u3819\u331c\u3812\u3246\u3826\u331c\u33b9\u3300\u381e\u3814\u3814\u3301\u3862\u33b8\u33b9\u3809\u338f\u3306\u33b7\u3246\u325f\u33b3\u3814\ud7c9\u3303\u33b8\u3813\u3812\u33b8\u3813\u3303\u3807\u3306\u3807\ud7ea\u3243\u383c\u33b4\u38ef\u3300\u381e\u3819\u3241\u33b8\u3306\u33b0\u3245\u380a\u3300\u382a\u33b3\u3246\u325d\u33be\u3300\u3378\u3819\u3818\ud7ea\u3867\u380a\u3303\ud7c2\u33b4\u33b3\ud7ea\u381e\u3804\u33b7\u33b2\u3816\u3802\u3823\u33b9\u325c\u3305\u381e\u33b7\u3305\u3820\u33b2\u3821\u380a\u3306\u381e\u33b3\u33b0\u3306\u3817\u3821\u381e\u3245\ud7c2\u3823\u33b0\ud7c9\u33b5\ud7ea\u33b0\u338f\u380a\u38ef\u3867\u3867\u3869\u3802\u383d\u380a\u3818\u3245\u3301\u331c\u325f\u325a\u331c\u3817\u381b\ud7c4\u3246\u33be\u33be\u3243\u3240\u3243\u380a\ud7c2\ud7ea\u3864\u3821\u383f\u3301\u331c\ud7c2\u33b5\u3240\u33b4\u325c\u3245\u3810\u3241\u33b2\u33bb\u3804\u3809\u3243\u3823\u3813\u3818\u3814\u3821\u3809\u3826\u381b\u382a\u3814\u33b7\u3815\u3814\u3378\ud7c2\u38ef\u381e\u33b7\u3246\ud7c4\u3378\u3825\u3809\u3245\u33b8\ud7c2\u3306\u3818\u381b\u3817\u3812\u331c\u3825\u38ef\u3810\u338f\u325c\u3867\u33b8\u381b\u325d\u3816\u3817\u3825\u3813\u33be\u3243\u3826\u383a\u3816\u383d\u325c\u380a\u3807\u383f\u3243\u3867\u33b4\u33b4\u3823\u33b9\u3864\u383f\u3820\u3812\u383a\u3303\u3240\u383a\u33b7\u3815\u381e\ud7c2\u325d\u331c\u3241\u3826\u33b8\u38ef\u3809\u331c\u3819\u3243\u381e\u3240\u3306\u3826\u33b9\u3245\u381b\u3807\u3810\u382a\u3300\u381b\u383c\u3815\u33b9\u3245\u3303\u338f\u380a\u3241\u3807\u380a\u3300\u3819\u325c\u33be\u383c\u33b9\u3378\u383c\u325a\u3810\u33b2\ud7ea\u380a\u33be\u3809\u325a\u3804\u3867\u3804\u33b3\u338f\u3815\u383c\u380a\u3820\u33b7\u3814\u33b0\u3823\u3810\u325c\u3809\u381b\ud7c2\u33b5\u3819\u331c\u383a\u33be\u3867\u325f\u33b0\u3818\u3826\u383c\u3245\u381b\u325c\u382a\u3826\u381e\u3869\u3821\u3306\u33b8\u3245\u3245\u3864\u331c\u3243\u33b0\u3300\u3819\u3807\u325d\u3812\u3869\u3817\u3306\u3306\u33bb\u3823\u3303\ud7c2\u3823\u382a\u331f\u3804\u3812\u33b9\u3812\u383a\u33b5\u3862\u33b0\u33b9\u3306\u381e\u383c\u338f\u325c\u33be\u331f\u3817\u3864\u3241\u3378\u3305\u3807\u33b9\u325a\u33b9\ud7c4\ud7ea\u383a\ud7ea\u331c\u383d\u3300\u3817\u33b2\ud7ea\u33b9\u3240\u33b7\u33b7\u3817\u33b7\u3818\u3819\u383d\u381b\u382a\u3869\u3825\u3862\u338f\u3807\u3819\u383f\u381b\u3815\u3814\u3818\u3802\u3819\u3240\u3378\u3809\u33b6\u3813\u3826\ud7c4\u3826\u33b0\u325f\u3821\u33b5\u33b2\u331c\u3819\u3300\u381e\u3243\u381b\u381e\u325a\u3812\u38ef\u33be\u3812\ud7c4\u383a\u381b\u331c\u325a\ud7c2\u3817\u3246\u3306\u3816\u381b\u338f\ud7ea\u3818\u3815\u33b2\u383f\u3869\u33bb\u33b8\u325c\u325a\u3826\u3305\u3814\u338f\u382a\u383f\u381e\u3821\u3825\u3804\u383c\u38ef\u38ef\u3813\u3240\u382a\u3802\u325c\u3243\u3807\ud7ea\u3819\u3825\u3300\u383f\u3378\u3301\u3807\u325c\u383d\u381e\u3821\u3815\u33b2\u3862\u3862\u3818\u38ef\u3807\u3819\u3306\u3819\u382a\u383c\ud7c4\u3819\u381e\u383d\u381e\u3825\u3802\u325c\u3815\u383a\u33b3\u383c\u3305\ud7c4\ud7c4\u3303\u380a\u33b2\u33bb\u3817\u325c\u382a\ud7c2\u3809\ud7c4\ud7c2\u33b7\u3809\ud7ea\u33b7\u3243\u3818\u3819\u33bb\ud7c4\u33b8\u3810\u3812\u331c\u3819\u33be\u3826\u33b8\ud7c9\u325a\u383d\u3301\u3378\u3378\u3815\u325f\u33bb\u3825\ud7c9\u383c\ud7c2\u33b8\u331c\u33b4\u331f\u331f\u3867\u331f\u3817\u3869\u383f\u331c\u3826\u33b0\u3825\u3815\u3821\u3245\u33b0\u381b\u381e\u3301\u3378\u325c\u33b9\u33b9\u380a\u38ef\u331c\u3869\u3819\u3305\u33b0\u33b0\ud7c4\u3243\u3240\u380a\u3810\u3240\u3245\u3303\u33b3\u383d\u3825\u381e\u33b5\ud7c2\u3864\u3809\u381e\u33b6\u3303\u3821\u33b5\u3825\u3241\u33b5\u33b9\u33b4\u3864\u33b5\u3823\u3814\u38ef\u3814\u3802\u33b7\u325c\u383a\u383c\u3813\u3862\u33b4\u325c\u3862\u381b\u381b\u3804\u381b\u3246\u381e\u33b6\u3812\u3807\u33b7\u338f\u33b5\u3241\u383a\u3812\ud7c9\u33b8\u3817\u381e\u3815\u331c\u383d\u3303\u3818\u3301\ud7c9\u3864\u383f\u3816\u3301\u325f\u3867\u3867\u3812\u3816\u3821\u33be\u383d\u38ef\u3301\u3817\u383a\u33b3\u3378\u3300\u3305\u3300\u33b6\u3305\u381b\u3812\u3825\u381e\u325d\u3814\u3867\u33b6\u3862\u338f\ud7c2\u33b6\u33b0\u382a\u33bb\u3864\u33b2\u38ef\u382a\u381e\u3809\u3804\u3814\u3825\u33b7\u3802\u33b9\u382a\u3246\u33b2\u3810\ud7ea\u325a\u3820\u3823\u325f\u3378\u382a\u3802\u3243\u3815\u33bb\u33b2\u3243\u3246\u3240\u33b2\u380a\u325f\u381e\u3305\ud7c4\u3810\u3300\u3246\ud7c2\u3869\u3816\ud7c4\u3820\u381e\u3813\u3809\u33b9\u33b8\u33be\u3815\u3810\u3867\u3812\u3867\u3802\u3862\u3303\u383f\u3243\u3243\u331f\u3826\u3864\u3300\u325c\u3826\u3862\u383a\u382a\u3862\u33b4\u3825\u331c\u325f\u33bb\ud7c9\u3820\u3821\u3812\u3306\u3818\u33b0\u383d\u33b2\u338f\u3241\u3240\u3807\u3825\u382a\u3862\u383f\ud7c9\u3301\u383f\u3809\u3826\u380a\u381b\u33b3\u3862\u381e\u33b6\u33b6\u3240\u3821\u3869\u383f\u3245\u3867\u3869\u3817\u3817\u3869\u3867\u3245\u325d\u3804\u325c\u3867\u3378\u3867\u383d\u381b\u3820\u38ef\u3864\u3243\u383d\u325d\u3300\u3867\u3378\u3825\u3246\u3245\u3301\u3241\u3245\u331f\u325d\u33b5\u33b9\u3864\u3821\u3240\u3812\u3867\u33b4\u383d\u3810\u3816\u33b5\u3243\u3303\u331f\u3802\u383d\u33b0\u3818\u325c\u33b7\u3816\u383a\u3816\u3300\u381b\u331c\u33b6\u3820\u331f\u325d\u3820\u383a\u325d\u3812\u3823\u3300\u3869\u382a\u383d\u3804\u33b4\u3809\u38ef\u3378\u381b\u383a\u3813\u3867\u3802\u3814\u382a\u3814\u3826\u3817\u33b4\u3807\u331c\u33b9\u325a\u3245\u3804\u3869\u33b2\u3818\ud7c9\u3810\u3819\u3821\u3869\u33b8\u3807\u33b4\u3306\ud7c2\u3378\u338f\u3243\u3812\u383d\u3813\u383c\u325c\u3306\u33b6\u3825\u325a\u33bb\u3303\u325d\u325a\u3813\u3241\u383f\ud7c2\u325d\u3802\ud7ea\u331c\u3303\u3817\ud7c2\u383d\u33b3\u382a\u3807\u3815\u3802\u3817\u325f\u33b2\u3816\u3823\u33b3\ud7c9\u3862\u33be\u3303\u338f\u33b6\u3804\u382a\u338f\u325f\u3240\u3826\u380a\u381b\u33b3\u3815\u3301\u3378\u3825\u33be\u3867\u33bb\u3807\u325f\u383c\u380a\u3802\u33b7\u3813\u325c\u383d\u3813\u3809\u3301\u3867\u325d\u3821\u3862\u3300\u3867\u3814\u3802\u3820\u33b7\u3817\u3378\u3301\u383a\u33bb\u3378\u3826\ud7c4\u3810\u331c\u3814\u3243\u33b9\u3867\u3807\u382a\u33be\u33b0\u3300\u38ef\u3378\u381e\u3864\u325d\u33b8\u3245\u3241\u3378\u3809\u33b2\u325f\u3810\u3802\u325d\u3378\u3862\u33b8\u33b4\u325f\u3814\u33be\ud7c4\u3812\u3305\u3815\u3826\u3819\u3867\u3810\u3862\u33b0\u3815\u380a\u331f\u3864\u338f\u33bb\u33b0\u331c\u3809\ud7c9\u3812\ud7c4\u3804\u3301\u33b7\u383a\u3802\u3300\u3246\u3300\u381b\u3246\u383f\u3820\u3819\u383a\u3807\u381b\u3817\ud7c4\u3819\u33b2\u3809\u3804\u33b7\u383f\u331c\u33b5\u33b9\ud7c4\u3864\u338f\u3240\u338f\u325c\u331c\u3301\u3823\u381e\u3814\u3814\u3303\u3802\u33be\u3825\u3301\ud7c9\u383c\u33bb\u338f\u33b9\u33b3\u33b9\u3823\u3246\u3303\u33b5\u3812\u33b4\ud7c9\u3814\u3867\u3378\u33be\u3821\ud7ea\u3819\u33b3\u325f\u325c\u3301\u383a\u3867\u3240\u325c\u3817\u383a\u3305\u3825\u381e\u3246\u3825\u3862\ud7ce\ud7ce".toCharArray();
                var92_6[84] = 0;
                while ((Integer)var92_6[84] < 2392) {
                    var92_6[81] = (int)((char[])var92_6[51])[(Integer)var92_6[84]];
                    var92_6[81] = (Integer)var92_6[81] ^ 51858;
                    var92_6[81] = (Integer)var92_6[81] + 46595;
                    var92_6[81] = (Integer)var92_6[81] ^ 60452;
                    var92_6[81] = (Integer)var92_6[81] - 45606;
                    var92_6[81] = (Integer)var92_6[81] + 62775;
                    var92_6[81] = (Integer)var92_6[81] ^ 34056;
                    var92_6[81] = (Integer)var92_6[81] + 2649;
                    var92_6[81] = (Integer)var92_6[81] ^ 57817;
                    var92_6[81] = (Integer)var92_6[81] - 24681;
                    var92_6[81] = (Integer)var92_6[81] ^ 15723;
                    var92_6[81] = (Integer)var92_6[81] ^ 13070;
                    var92_6[81] = (Integer)var92_6[81] + 32319;
                    ((char[])var92_6[51])[((Integer)var92_6[84]).intValue()] = (char)((Integer)var92_6[81]).intValue();
                    var92_6[84] = (Integer)var92_6[84] + 1;
                }
                v10 = Scaffold.cfr_renamed_1()[0] = new String((char[])var92_6[51]);
            }
            v9[2] = (String)v10;
            var92_6[61] = ((String)Scaffold.o0Oo000O0oO(v9)).toCharArray();
            v11 = (Long)var92_6[59];
            var92_6[11] = -73;
            var92_6[11] = (Integer)var92_6[11] - -113;
            var92_6[11] = (Integer)var92_6[11] - 8;
            var92_6[59] = v11 ^ (0x6E600000000L ^ v11) & -1L << (Integer)var92_6[11];
            v12 = (Long)var92_6[34];
            var92_6[14] = 19;
            var92_6[14] = (Integer)var92_6[14] + -18;
            var92_6[14] = (Integer)var92_6[14] + 31;
            var92_6[34] = v12 ^ (0L ^ v12) & -1L >>> (Integer)var92_6[14];
        }
        var92_6[82] = 85;
        var92_6[82] = (Integer)var92_6[82] ^ 80;
        var92_6[82] = (Integer)var92_6[82] - -27;
        ** while ((int)((Long)var92_6[34]).longValue() >= (int)(((Long)var92_6[59]).longValue() >>> ((Integer)var92_6[82]).intValue()))
lbl177:
        // 1 sources

        var90_4 = -1253402459;
        ** while (true)
    }
    public strictfp aka b(c c2) {
        aka aka2;
        aka2(c2.distance, c2.gX + (double)c2.getEyeHeight(), c2.gY);
        return aka2;
    }
    public Scaffold() {
        BooleanValue booleanValue;
        int n2 = 19175914;
        ooo00oo0Ooo0 = n2;
        if (33515418 < ooo00oo0Ooo0) {
            n2 = 75;
        }
        int n3 = -8;
        n3 ^= 1;
        n3 ^= 0xFFFFFFE5;
        int n4 = -74;
        n4 ^= 0xFFFFFFCC;
        Mode[] modeArray = new Mode[n4 ^= 0x7B];
        int n5 = 8;
        n5 ^= 0x14;
        int n6 = -5;
        n6 += -74;
        modeArray[n5 -= 28] = new NamedMode((String)o0Oo000O0oO[n6 -= -110]);
        int n7 = 88;
        n7 += -65;
        Mode[] modeArray2 = new Mode[n7 -= 22];
        int n8 = -206;
        n8 += 112;
        int n9 = -6;
        n9 += 98;
        modeArray2[n8 ^= 0xFFFFFFA2] = new NamedMode((String)o0Oo000O0oO[n9 -= 41]);
        int n10 = -153;
        n10 -= -28;
        Mode[] modeArray3 = new Mode[n10 ^= 0xFFFFFF82];
        int n11 = 13;
        n11 += 19;
        int n12 = -131;
        n12 -= 18;
        modeArray3[n11 += -32] = new NamedMode((String)o0Oo000O0oO[n12 ^= 0xFFFFFFF2]);
        int n13 = -42;
        n13 ^= 0;
        Mode[] modeArray4 = new Mode[n13 += 43];
        int n14 = -87;
        n14 ^= 0xFFFFFFA5;
        int n15 = -127;
        n15 ^= 0xFFFFFFD5;
        modeArray4[n14 -= 12] = new NamedMode((String)o0Oo000O0oO[n15 += 60]);
        int n16 = -109;
        n16 += 19;
        Mode[] modeArray5 = new Mode[n16 ^= 0xFFFFFFA7];
        int n17 = -18;
        n17 ^= 0x20;
        int n18 = 17;
        n18 += 85;
        modeArray5[n17 += 50] = new NamedMode((String)o0Oo000O0oO[n18 -= 5]);
        int n19 = -63;
        n19 += 89;
        Mode[] modeArray6 = new Mode[n19 -= 25];
        int n20 = -34;
        n20 += 9;
        int n21 = 201;
        n21 ^= 0x4A;
        modeArray6[n20 += 25] = new NamedMode((String)o0Oo000O0oO[n21 -= -16]);
        int n22 = -8;
        n22 += 86;
        this.afJ = new ModeValue((String)o0Oo000O0oO[n3], this).a(modeArray).a(modeArray2).a(modeArray3).a(modeArray4).a(modeArray5).a(modeArray6).co((String)o0Oo000O0oO[n22 += 30]);
        int n23 = 282;
        n23 += -39;
        n23 -= 87;
        int n24 = 48;
        n24 -= 77;
        Mode[] modeArray7 = new Mode[n24 -= -30];
        int n25 = -36;
        n25 += 112;
        int n26 = -11;
        n26 ^= 0xFFFFFF9B;
        modeArray7[n25 += -76] = new NamedMode((String)o0Oo000O0oO[n26 += -45]);
        int n27 = 103;
        n27 ^= 0x46;
        Mode[] modeArray8 = new Mode[n27 += -32];
        int n28 = -33;
        n28 -= 56;
        int n29 = 13;
        n29 -= -36;
        modeArray8[n28 += 89] = new NamedMode((String)o0Oo000O0oO[n29 += 85]);
        int n30 = -278;
        n30 += 64;
        this.afK = new ModeValue((String)o0Oo000O0oO[n23], this).a(modeArray7).a(modeArray8).co((String)o0Oo000O0oO[n30 ^= 0xFFFFFF8B]);
        int n31 = 2;
        n31 += 22;
        n31 ^= 0x1D;
        int n32 = -171;
        n32 -= -50;
        Mode[] modeArray9 = new Mode[n32 += 122];
        int n33 = -42;
        n33 ^= 0xFFFFFF9D;
        int n34 = 167;
        n34 += -35;
        modeArray9[n33 -= 75] = new NamedMode((String)o0Oo000O0oO[n34 += 4]);
        int n35 = -11;
        n35 += 53;
        Mode[] modeArray10 = new Mode[n35 += -41];
        int n36 = -128;
        n36 += 103;
        int n37 = -93;
        n37 ^= 0xFFFFFFBE;
        modeArray10[n36 -= -25] = new NamedMode((String)o0Oo000O0oO[n37 -= -120]);
        int n38 = -46;
        n38 += 118;
        Mode[] modeArray11 = new Mode[n38 -= 71];
        int n39 = 27;
        n39 += -6;
        int n40 = 144;
        n40 -= 114;
        modeArray11[n39 -= 21] = new NamedMode((String)o0Oo000O0oO[n40 ^= 0x4F]);
        int n41 = 157;
        n41 -= 69;
        this.afL = new ModeValue((String)o0Oo000O0oO[n31], this).a(modeArray9).a(modeArray10).a(modeArray11).co((String)o0Oo000O0oO[n41 ^= 0x65]);
        int n42 = 127;
        n42 += -38;
        n42 += 75;
        int n43 = 83;
        n43 ^= 0xFFFFFF96;
        Mode[] modeArray12 = new Mode[n43 ^= 0xFFFFFFC4];
        int n44 = -83;
        n44 += -11;
        int n45 = -112;
        n45 += 56;
        modeArray12[n44 ^= 0xFFFFFFA2] = new NamedMode((String)o0Oo000O0oO[n45 ^= 0xFFFFFFA0]);
        int n46 = 52;
        n46 += -21;
        Mode[] modeArray13 = new Mode[n46 -= 30];
        int n47 = -218;
        n47 -= -107;
        int n48 = -183;
        n48 -= -93;
        modeArray13[n47 -= -111] = new VfMode((String)o0Oo000O0oO[n48 ^= 0xFFFFFFE2], this);
        int n49 = -38;
        n49 ^= 0xFFFFFF9E;
        Mode[] modeArray14 = new Mode[n49 -= 67];
        int n50 = 91;
        n50 += -76;
        int n51 = -11;
        n51 += 80;
        modeArray14[n50 ^= 0xF] = new VgMode((String)o0Oo000O0oO[n51 -= -72], this);
        int n52 = -45;
        n52 -= -17;
        Mode[] modeArray15 = new Mode[n52 += 29];
        int n53 = -147;
        n53 += 113;
        int n54 = 22;
        n54 += 68;
        modeArray15[n53 -= -34] = new VeMode((String)o0Oo000O0oO[n54 += -76], this);
        int n55 = -161;
        n55 -= -50;
        Mode[] modeArray16 = new Mode[n55 ^= 0xFFFFFF90];
        int n56 = -53;
        n56 -= -51;
        int n57 = -61;
        n57 -= -115;
        modeArray16[n56 += 2] = new VjMode((String)o0Oo000O0oO[n57 += 94], this);
        int n58 = -17;
        n58 ^= 0x20;
        Mode[] modeArray17 = new Mode[n58 += 50];
        int n59 = -10;
        n59 -= -65;
        int n60 = 19;
        ++n60;
        modeArray17[n59 ^= 0x37] = new ViMode((String)o0Oo000O0oO[n60 += 54], this);
        int n61 = 53;
        n61 ^= 0x74;
        Mode[] modeArray18 = new Mode[n61 += -64];
        int n62 = -41;
        n62 -= -86;
        int n63 = 5;
        n63 ^= 0xFFFFFFE5;
        modeArray18[n62 -= 45] = new VhMode((String)o0Oo000O0oO[n63 += 111], this);
        int n64 = 2;
        n64 += -32;
        Mode[] modeArray19 = new Mode[n64 += 31];
        int n65 = -41;
        n65 ^= 0x62;
        int n66 = -9;
        n66 += 64;
        int n67 = -163;
        n67 ^= 0xFFFFFFD7;
        modeArray19[n65 ^= 0xFFFFFFB5] = new VmMode((String)o0Oo000O0oO[n66 ^= 0x31] + (String)o0Oo000O0oO[n67 += -91], this);
        int n68 = 43;
        n68 -= 65;
        Mode[] modeArray20 = new Mode[n68 ^= 0xFFFFFFEB];
        int n69 = -51;
        n69 += -23;
        int n70 = 24;
        n70 -= 20;
        modeArray20[n69 += 74] = new VkMode((String)o0Oo000O0oO[n70 += 111], this);
        int n71 = -180;
        n71 -= -65;
        Mode[] modeArray21 = new Mode[n71 += 116];
        int n72 = 40;
        n72 -= -17;
        int n73 = 181;
        n73 ^= 0x31;
        modeArray21[n72 += -57] = new VnMode((String)o0Oo000O0oO[n73 -= 78], this);
        int n74 = -5;
        n74 += 69;
        this.afM = new ModeValue((String)o0Oo000O0oO[n42], this).a(modeArray12).a(modeArray13).a(modeArray14).a(modeArray15).a(modeArray16).a(modeArray17).a(modeArray18).a(modeArray19).a(modeArray20).a(modeArray21).co((String)o0Oo000O0oO[n74 += 8]);
        int n75 = 133;
        n75 -= -102;
        n75 -= 105;
        int n76 = -19;
        n76 -= 97;
        Mode[] modeArray22 = new Mode[n76 ^= 0xFFFFFF8D];
        int n77 = -189;
        n77 -= -109;
        int n78 = -232;
        n78 ^= 0xFFFFFF9E;
        modeArray22[n77 -= -80] = new NamedMode((String)o0Oo000O0oO[n78 += -21]);
        int n79 = -31;
        n79 -= -91;
        Mode[] modeArray23 = new Mode[n79 -= 59];
        int n80 = -43;
        n80 ^= 0xFFFFFF8A;
        int n81 = -109;
        n81 -= -36;
        modeArray23[n80 ^= 0x5F] = new VyMode((String)o0Oo000O0oO[n81 += 119], this);
        int n82 = -37;
        n82 += 26;
        Mode[] modeArray24 = new Mode[n82 += 12];
        int n83 = 92;
        n83 += -77;
        int n84 = 58;
        n84 ^= 0xFFFFFFA9;
        modeArray24[n83 ^= 0xF] = new VxMode((String)o0Oo000O0oO[n84 ^= 0xFFFFFFB6], this);
        int n85 = -13;
        n85 -= -5;
        Mode[] modeArray25 = new Mode[n85 ^= 0xFFFFFFF9];
        int n86 = 157;
        n86 -= 54;
        int n87 = -175;
        n87 ^= 0xFFFFFF9D;
        modeArray25[n86 -= 103] = new VvMode((String)o0Oo000O0oO[n87 -= 52], this);
        int n88 = -172;
        n88 -= -82;
        Mode[] modeArray26 = new Mode[n88 -= -91];
        int n89 = 95;
        n89 ^= 0xFFFFFFBE;
        int n90 = 161;
        n90 += -43;
        modeArray26[n89 += 31] = new VpMode((String)o0Oo000O0oO[n90 ^= 0x7D], this);
        int n91 = 106;
        n91 -= 89;
        Mode[] modeArray27 = new Mode[n91 += -16];
        int n92 = -18;
        n92 += -19;
        int n93 = -114;
        n93 ^= 0xFFFFFFF8;
        modeArray27[n92 ^= 0xFFFFFFDB] = new WaMode((String)o0Oo000O0oO[n93 -= 78], this);
        int n94 = -9;
        n94 ^= 0x46;
        Mode[] modeArray28 = new Mode[n94 -= -80];
        int n95 = 173;
        n95 -= 48;
        int n96 = 46;
        n96 ^= 0x33;
        modeArray28[n95 += -125] = new VsMode((String)o0Oo000O0oO[n96 ^= 0x5B], this);
        int n97 = -115;
        n97 -= 4;
        Mode[] modeArray29 = new Mode[n97 ^= 0xFFFFFF88];
        int n98 = -29;
        n98 += 20;
        int n99 = -109;
        n99 -= -39;
        modeArray29[n98 += 9] = new VuMode((String)o0Oo000O0oO[n99 += 91], this);
        int n100 = -152;
        n100 += 33;
        Mode[] modeArray30 = new Mode[n100 += 120];
        int n101 = 70;
        n101 ^= 0x59;
        int n102 = -181;
        n102 += 87;
        modeArray30[n101 ^= 0x1F] = new VtMode((String)o0Oo000O0oO[n102 ^= 0xFFFFFF83], this);
        int n103 = -21;
        n103 -= 38;
        Mode[] modeArray31 = new Mode[n103 -= -60];
        int n104 = 89;
        n104 += -53;
        int n105 = -172;
        n105 += 59;
        modeArray31[n104 ^= 0x24] = new VrMode((String)o0Oo000O0oO[n105 ^= 0xFFFFFF92], this);
        int n106 = 127;
        n106 ^= 0x63;
        Mode[] modeArray32 = new Mode[n106 += -27];
        int n107 = 134;
        n107 += -115;
        int n108 = 206;
        n108 += -17;
        modeArray32[n107 -= 19] = new VwMode((String)o0Oo000O0oO[n108 += -122], this);
        int n109 = -98;
        n109 ^= 0x79;
        Mode[] modeArray33 = new Mode[n109 -= -26];
        int n110 = -24;
        n110 += 50;
        int n111 = 103;
        n111 -= -10;
        int n112 = 70;
        n112 += -11;
        modeArray33[n110 ^= 0x1A] = new VzMode((String)o0Oo000O0oO[n111 += 45] + (String)o0Oo000O0oO[n112 += 10], this);
        int n113 = 345;
        n113 -= 113;
        this.afN = new ModeValue((String)o0Oo000O0oO[n75], this).a(modeArray22).a(modeArray23).a(modeArray24).a(modeArray25).a(modeArray26).a(modeArray27).a(modeArray28).a(modeArray29).a(modeArray30).a(modeArray31).a(modeArray32).a(modeArray33).co((String)o0Oo000O0oO[n113 += -105]);
        int n114 = -24;
        n114 ^= 0xFFFFFFAC;
        n114 += 94;
        int n115 = -157;
        n115 -= -86;
        Mode[] modeArray34 = new Mode[n115 += 72];
        int n116 = -35;
        n116 += -68;
        int n117 = 51;
        n117 ^= 0xFFFFFF8C;
        modeArray34[n116 ^= 0xFFFFFF99] = new NamedMode((String)o0Oo000O0oO[n117 += 68]);
        int n118 = -39;
        n118 ^= 0x60;
        Mode[] modeArray35 = new Mode[n118 -= -72];
        int n119 = 97;
        n119 ^= 0x38;
        int n120 = 256;
        n120 += -38;
        modeArray35[n119 ^= 0x59] = new NamedMode((String)o0Oo000O0oO[n120 ^= 0x55]);
        int n121 = -6;
        n121 ^= 0xFFFFFFA7;
        Mode[] modeArray36 = new Mode[n121 += -92];
        int n122 = -151;
        n122 -= -89;
        int n123 = -224;
        n123 ^= 0xFFFFFFBA;
        modeArray36[n122 += 62] = new NamedMode((String)o0Oo000O0oO[n123 -= 14]);
        int n124 = 273;
        n124 += -121;
        this.afO = new ModeValue((String)o0Oo000O0oO[n114], this).a(modeArray34).a(modeArray35).a(modeArray36).co((String)o0Oo000O0oO[n124 += 14]);
        int n125 = 51;
        n125 -= -24;
        n125 ^= 0x6C;
        int n126 = -49;
        n126 -= -115;
        n126 -= 14;
        int n127 = 142;
        n127 += -68;
        Mode[] modeArray37 = new Mode[n127 += -73];
        int n128 = 34;
        n128 ^= 0x49;
        int n129 = 309;
        n129 -= 99;
        modeArray37[n128 -= 107] = new NamedMode((String)o0Oo000O0oO[n129 += -107]);
        int n130 = 19;
        n130 ^= 0x64;
        Mode[] modeArray38 = new Mode[n130 -= 118];
        int n131 = 186;
        n131 -= 60;
        int n132 = 193;
        n132 -= 10;
        modeArray38[n131 += -126] = new VaMode((String)o0Oo000O0oO[n132 += -123], this);
        int n133 = -1;
        n133 ^= 0x49;
        Mode[] modeArray39 = new Mode[n133 += 75];
        int n134 = 51;
        n134 += 11;
        int n135 = 29;
        n135 -= -17;
        modeArray39[n134 += -62] = new VcMode((String)o0Oo000O0oO[n135 ^= 0x3D], this);
        int n136 = 97;
        n136 ^= 0x68;
        Mode[] modeArray40 = new Mode[n136 ^= 8];
        int n137 = 73;
        n137 -= 52;
        int n138 = 260;
        n138 += -98;
        modeArray40[n137 += -21] = new VbMode((String)o0Oo000O0oO[n138 ^= 0x2B], this);
        int n139 = 116;
        n139 -= -124;
        this.afP = new ModeValue((String)o0Oo000O0oO[n125] + (String)o0Oo000O0oO[n126], this).a(modeArray37).a(modeArray38).a(modeArray39).a(modeArray40).co((String)o0Oo000O0oO[n139 ^= 0x6D]);
        int n140 = 107;
        n140 += -5;
        n140 -= 43;
        int n141 = 200;
        n141 -= 69;
        n141 += -126;
        int n142 = 19;
        n142 += 80;
        n142 ^= 0x69;
        int n143 = 0;
        n143 += -30;
        int n144 = -48;
        n144 -= -28;
        int n145 = 127;
        n145 ^= 0x50;
        this.afQ = new RangeValue((String)o0Oo000O0oO[n140], this, (Number)n141, (Number)n142, (Number)(n143 += 30), (Number)(n144 += 30), (Number)(n145 ^= 0x2E));
        int n146 = 16;
        n146 ^= 0xFFFFFFFF;
        n146 ^= 0xFFFFFF9F;
        int n147 = 103;
        n147 ^= 0x58;
        n147 += -63;
        int n148 = 27;
        n148 -= 38;
        n148 += 11;
        int n149 = 17;
        n149 ^= 0x4A;
        int n150 = -126;
        n150 += 113;
        int n151 = -16;
        n151 ^= 0xFFFFFFFC;
        this.afR = new RangeValue((String)o0Oo000O0oO[n146], this, (Number)n147, (Number)n148, (Number)(n149 ^= 0x5B), (Number)(n150 -= -18), (Number)(n151 += -11));
        int n152 = 35;
        n152 += -25;
        int n153 = -183;
        n153 -= -66;
        int n154 = -79;
        n154 -= -100;
        this.afS = new NumberValue((String)o0Oo000O0oO[n152 -= -123], this, (Number)(n153 += 118), (Number)Double.longBitsToDouble(4591870180066957722L), (Number)(n154 -= 11), (Number)Double.longBitsToDouble(4591870180066957722L));
        int n155 = 66;
        n155 += -102;
        n155 -= -125;
        int n156 = 87;
        n156 -= -4;
        n156 ^= 0x5B;
        int n157 = 5;
        n157 ^= 0xFFFFFFCE;
        int n158 = -49;
        n158 -= 26;
        int n159 = 64;
        n159 ^= 0xFFFFFFBF;
        this.afT = new NumberValue((String)o0Oo000O0oO[n155], this, (Number)n156, (Number)(n157 += 53), (Number)(n158 += 79), (Number)(n159 ^= 0xFFFFFFFE));
        int n160 = 111;
        n160 += -104;
        n160 -= -57;
        int n161 = 79;
        n161 ^= 0xFFFFFFCC;
        n161 ^= 0xFFFFFFFB;
        int n162 = 126;
        n162 += 36;
        int n164 = 26;
        n164 = n164 ^ 0xFFFFFFAB;
        boolean bl2 = n164 + 79;
        this.afU = new BooleanValue((String)o0Oo000O0oO[n160] + (String)o0Oo000O0oO[n161] + (String)o0Oo000O0oO[n162 ^= 0x34], (Module)this, (Boolean)bl2);
        int n165 = -51;
        n165 += -122;
        int n167 = -3;
        n167 = n167 ^ 5;
        boolean bl3 = n167 - -9;
        this.afV = new BooleanValue((String)o0Oo000O0oO[n165 ^= 0xFFFFFFF3], (Module)this, (Boolean)bl3);
        int n168 = -196;
        n168 -= 6;
        n168 ^= 0xFFFFFF93;
        int n169 = -187;
        n169 ^= 0xFFFFFF9D;
        int n171 = -72;
        n171 = n171 + 97;
        boolean bl4 = n171 + -25;
        this.afW = new BooleanValue((String)o0Oo000O0oO[n168] + (String)o0Oo000O0oO[n169 ^= 0x52], (Module)this, (Boolean)bl4);
        int n172 = 4;
        n172 += -36;
        int n174 = -147;
        n174 = n174 + 86;
        boolean bl5 = n174 - -62;
        this.afX = new BooleanValue((String)o0Oo000O0oO[n172 += 123], (Module)this, (Boolean)bl5);
        int n175 = -87;
        n175 ^= 0x48;
        n175 -= -93;
        int n176 = 61;
        n176 += 37;
        int n178 = 139;
        n178 = n178 - 44;
        boolean bl6 = n178 + -95;
        this.afY = new BooleanValue((String)o0Oo000O0oO[n175] + (String)o0Oo000O0oO[n176 ^= 0xC], (Module)this, (Boolean)bl6);
        int n179 = 65;
        n179 += -86;
        int n181 = -4;
        n181 = n181 + -116;
        boolean bl7 = n181 ^ 0xFFFFFF88;
        this.afZ = new BooleanValue((String)o0Oo000O0oO[n179 += 31], (Module)this, (Boolean)bl7);
        int n182 = -241;
        n182 -= -113;
        int n183 = -27;
        n183 -= 16;
        int n184 = 69;
        boolean bl8 = n184 ^ 0x44;
        this.aga = new BooleanValue((String)o0Oo000O0oO[n182 ^= 0xFFFFFFE5] + (String)o0Oo000O0oO[n183 += 98], (Module)this, (Boolean)bl8, () -> {
            int n2 = 63540975;
            ooo00oo0Ooo0 = n2;
            if (826546338 < ooo00oo0Ooo0) {
                n2 = 1216211337;
            }
            Object[] objectArray = new Object[6];
            Integer n3 = null;
            if (((Boolean)this.afZ.getValue()).booleanValue()) {
                int n4 = 74;
                n4 ^= 0xFFFFFF99;
                int n5 = n4 ^= 0xFFFFFFD3;
                return n5 != 0;
            }
            n3 = -20;
            n3 = n3 ^ 0x10;
            n3 = 1;
            return 1 != 0;
        });
        int n185 = -176;
        n185 += 80;
        n185 ^= 0xFFFFFF98;
        int n186 = 219;
        n186 += -5;
        n186 -= 126;
        int n187 = -49;
        n187 += 91;
        int n189 = 105;
        n189 = n189 ^ 0xFFFFFFAD;
        boolean bl9 = n189 ^ 0xFFFFFFC5;
        this.agb = new BooleanValue((String)o0Oo000O0oO[n185] + (String)o0Oo000O0oO[n186] + (String)o0Oo000O0oO[n187 ^= 0x3D], (Module)this, (Boolean)bl9, () -> {
            int n2 = 535299867;
            if (185182173 >= n2) {
            }
            Object[] objectArray = new Object[6];
            Integer n3 = null;
            if ((Boolean)this.afZ.getValue() != false) return 0 != 0;
            n3 = 222;
            n3 = n3 + -100;
            n3 = 1;
            return 1 != 0;
        });
        int n190 = 32;
        n190 -= 51;
        n190 += 45;
        int n191 = 62;
        n191 -= 11;
        int n193 = 121;
        n193 = n193 - 3;
        boolean bl10 = n193 - 118;
        this.agc = new BooleanValue((String)o0Oo000O0oO[n190] + (String)o0Oo000O0oO[n191 ^= 0x6D], (Module)this, (Boolean)bl10, () -> {
            Object[] objectArray = new Object[4];
            Integer n2 = null;
            if (((Boolean)this.afZ.getValue()).booleanValue()) {
                return 0 != 0;
            }
            n2 = -152;
            n2 = n2 + 58;
            n2 = 1;
            return 1 != 0;
        });
        int n194 = -3;
        n194 += -82;
        n194 ^= 0xFFFFFFA7;
        int n195 = 78;
        n195 ^= 0xFFFFFFA9;
        int n197 = -7;
        n197 = n197 - -54;
        boolean bl11 = n197 - 46;
        this.agd = new BooleanValue((String)o0Oo000O0oO[n194] + (String)o0Oo000O0oO[n195 -= -63], (Module)this, (Boolean)bl11, () -> {
            if (false) {
            }
            Object[] objectArray = new Object[5];
            Integer n2 = null;
            if ((Boolean)this.afZ.getValue() != false) return 0 != 0;
            n2 = -7;
            n2 = n2 ^ 0xFFFFFFE5;
            n2 = 1;
            return 1 != 0;
        });
        int n198 = 59;
        n198 -= -35;
        n198 -= 62;
        int n199 = 238;
        n199 += -62;
        n199 += -126;
        int n200 = -46;
        n200 += 16;
        n200 ^= 0xFFFFFFE2;
        int n201 = -40;
        n201 -= -9;
        n201 ^= 0xFFFFFFE1;
        int n202 = -61;
        n202 ^= 0xFFFFFF93;
        int n203 = 143;
        n203 += -122;
        int n204 = -115;
        n204 += -4;
        this.age = new RangeValue((String)o0Oo000O0oO[n198] + (String)o0Oo000O0oO[n199], this, (Number)n200, (Number)n201, (Number)(n202 += -80), (Number)(n203 += -16), (Number)(n204 ^= 0xFFFFFF88), () -> {
            Object[] objectArray = new Object[5];
            Integer n2 = null;
            if ((Boolean)this.afZ.getValue() != false) return 0 != 0;
            n2 = -54;
            n2 = n2 - 32;
            n2 = 1;
            return 1 != 0;
        });
        int n205 = -54;
        n205 -= -36;
        int n207 = 5;
        n207 = n207 + 20;
        boolean bl12 = n207 ^ 0x18;
        this.agf = new BooleanValue((String)o0Oo000O0oO[n205 -= -75], (Module)this, (Boolean)bl12, () -> {
            int n2;
            Object[] objectArray = new Object[6];
            Integer n3 = null;
            if (((Boolean)this.afZ.getValue()).booleanValue()) {
                int n4 = -107;
                n4 ^= 0xFFFFFF8D;
                n2 = n4 += -24;
                return n2 != 0;
            }
            n3 = -7;
            n3 = -125;
            n3 = n3 + 126;
            n2 = n3;
            return n2 != 0;
        });
        int n208 = 20;
        n208 ^= 0x34;
        n208 ^= 0x55;
        int n209 = -131;
        n209 += 23;
        n209 ^= 0xFFFFFFC3;
        int n210 = -25;
        n210 += 114;
        n210 += -54;
        int n211 = 133;
        n211 -= 105;
        n211 ^= 0x3A;
        int n212 = 139;
        n212 += -75;
        int n213 = 397;
        n213 += -99;
        int n214 = -7;
        n214 ^= 0x68;
        this.agg = new RangeValue((String)o0Oo000O0oO[n208] + (String)o0Oo000O0oO[n209], this, (Number)n210, (Number)n211, (Number)(n212 += -64), (Number)(n213 += -118), (Number)(n214 -= -112), () -> {
            int n2 = 23415185;
            ooo00oo0Ooo0 = n2;
            if (31486791 <= ooo00oo0Ooo0) {
            }
            Object[] objectArray = new Object[6];
            Integer n3 = null;
            if ((Boolean)this.afZ.getValue() != false) return 0 != 0;
            n3 = -28;
            n3 = n3 - -50;
            n3 = 1;
            return 1 != 0;
        });
        int n215 = 227;
        n215 -= 114;
        n215 += 9;
        int n216 = -36;
        n216 -= 33;
        int n218 = 66;
        n218 = n218 ^ 0x11;
        boolean bl13 = n218 + -83;
        this.agh = new BooleanValue((String)o0Oo000O0oO[n215] + (String)o0Oo000O0oO[n216 ^= 0xFFFFFFD4], (Module)this, (Boolean)bl13, () -> {
            int n2;
            Object[] objectArray = new Object[6];
            Integer n3 = null;
            if (((Boolean)this.afZ.getValue()).booleanValue()) {
                int n4 = -119;
                n4 -= 7;
                n2 = n4 += 126;
                return n2 != 0;
            }
            n3 = 134;
            n3 = 28;
            n3 = n3 + -27;
            n2 = n3;
            return n2 != 0;
        });
        int n219 = 117;
        n219 -= 67;
        n219 += 59;
        int n220 = -36;
        n220 += -49;
        n220 ^= 0xFFFFFFA4;
        int n221 = 282;
        n221 -= 113;
        n221 -= 76;
        int n222 = 211;
        n222 += 17;
        n222 += -106;
        int n223 = 214;
        n223 -= 80;
        n223 -= 6;
        int n224 = 88;
        n224 -= -33;
        int n225 = 371;
        n225 += -84;
        int n226 = 231;
        n226 += -120;
        this.agi = new RangeValue((String)o0Oo000O0oO[n219] + (String)o0Oo000O0oO[n220] + (String)o0Oo000O0oO[n221], this, (Number)n222, (Number)n223, (Number)(n224 ^= 0x79), (Number)(n225 += -107), (Number)(n226 -= 110), () -> {
            int n2 = 1115359911;
            ooo00oo0Ooo0 = n2;
            if (1360214207 <= ooo00oo0Ooo0) {
            }
            Object[] objectArray = new Object[6];
            Integer n3 = null;
            if (((Boolean)this.afZ.getValue()).booleanValue() && ((Boolean)this.agh.getValue()).booleanValue()) {
                int n4 = -81;
                n4 ^= 0xFFFFFF8D;
                int n5 = n4 -= 34;
                return n5 != 0;
            }
            n3 = -162;
            n3 = n3 - -66;
            n3 = 1;
            return 1 != 0;
        });
        int n227 = 43;
        n227 += -6;
        n227 ^= 0x3B;
        int n228 = 325;
        n228 -= 75;
        int n230 = -10;
        n230 = n230 - -23;
        boolean bl14 = n230 - 13;
        this.agj = new BooleanValue((String)o0Oo000O0oO[n227] + (String)o0Oo000O0oO[n228 += -96], (Module)this, (Boolean)bl14, () -> {
            Object[] objectArray = new Object[7];
            Integer n2 = null;
            if (((Boolean)this.afZ.getValue()).booleanValue() && ((Boolean)this.agh.getValue()).booleanValue()) {
                return 0 != 0;
            }
            n2 = -153;
            n2 = n2 - -113;
            n2 = 1;
            return 1 != 0;
        });
        int n231 = 15;
        n231 += 6;
        int n233 = 22;
        n233 = n233 - 50;
        boolean bl15 = n233 - -28;
        this.agk = new BooleanValue((String)o0Oo000O0oO[n231 += 61], (Module)this, (Boolean)bl15);
        int n234 = -90;
        n234 ^= 0x59;
        n234 ^= 0xFFFFFFB2;
        int n235 = 56;
        n235 -= -19;
        n235 ^= 0x4B;
        int n236 = -148;
        n236 += 39;
        n236 ^= 0xFFFFFF93;
        int n237 = 149;
        n237 += -54;
        int n238 = 197;
        n238 += -82;
        int n239 = -80;
        n239 += -3;
        this.agl = new RangeValue((String)o0Oo000O0oO[n234], this, (Number)n235, (Number)n236, (Number)(n237 -= 95), (Number)(n238 += -110), (Number)(n239 ^= 0xFFFFFFAC), () -> {
            int n2 = 514306295;
            if (248808581 < 1709510943 || -380030891 == 0) {
                // empty if block
            }
            Object[] objectArray = new Object[7];
            Integer n3 = null;
            if ((Boolean)this.agk.getValue() != false) return 0 != 0;
            n3 = -70;
            n3 = n3 ^ 0x41;
            n3 = 1;
            return 1 != 0;
        });
        int n240 = -71;
        n240 ^= 0xFFFFFFDB;
        n240 -= 8;
        int n241 = -133;
        n241 -= -66;
        n241 ^= 0xFFFFFFBD;
        int n242 = -123;
        n242 ^= 0xFFFFFFDA;
        n242 -= 95;
        int n243 = 222;
        n243 -= 102;
        int n244 = 23;
        n244 += 29;
        int n245 = 75;
        n245 ^= 0x75;
        this.agm = new RangeValue((String)o0Oo000O0oO[n240], this, (Number)n241, (Number)n242, (Number)(n243 -= 120), (Number)(n244 -= 47), (Number)(n245 -= 61), () -> {
            Object[] objectArray = new Object[7];
            Integer n2 = null;
            if (!((Boolean)this.agk.getValue()).booleanValue()) {
                n2 = -14;
                n2 = n2 ^ 0xFFFFFF98;
                n2 = 1;
                return 1 != 0;
            }
            int n3 = -14;
            n3 += 104;
            int n4 = n3 ^= 0x5A;
            return n4 != 0;
        });
        int n246 = 96;
        n246 += 73;
        n246 ^= 0xA;
        int n247 = 12;
        n247 ^= 0xFFFFFFEE;
        n247 += 39;
        int n248 = -99;
        n248 ^= 0xFFFFFF97;
        n248 += -9;
        int n249 = -88;
        n249 -= -60;
        n249 += 29;
        int n250 = 30;
        n250 += -84;
        int n251 = -95;
        n251 ^= 0x32;
        int n252 = -32;
        n252 += 21;
        this.agn = new RangeValue((String)o0Oo000O0oO[n246] + (String)o0Oo000O0oO[n247], this, (Number)n248, (Number)n249, (Number)(n250 -= -55), (Number)(n251 += 119), (Number)(n252 -= -12), () -> {
            Object[] objectArray = new Object[6];
            Integer n2 = null;
            if ((Boolean)this.agk.getValue() != false) return 0 != 0;
            n2 = -27;
            n2 = 42975271;
            n2 = 1;
            return 1 != 0;
        });
        int n253 = 113;
        n253 += 81;
        int n254 = 128;
        n254 += -34;
        this.ago = new NumberValue((String)o0Oo000O0oO[n253 ^= 0x42], this, (Number)Double.longBitsToDouble(4596373779694328218L), (Number)Double.longBitsToDouble(4596373779694328218L), (Number)(n254 += -93), (Number)Double.longBitsToDouble(4587366580439587226L), () -> {
            int n2 = 515291396;
            ooo00oo0Ooo0 = 1180720852;
            if (799893299 < ooo00oo0Ooo0) {
            }
            Object[] objectArray = new Object[7];
            Integer n3 = null;
            if (!((Boolean)this.agk.getValue()).booleanValue()) {
                n3 = -25;
                n3 = n3 ^ 0x70;
                n3 = 1;
                return 1 != 0;
            }
            int n4 = -68;
            n4 = -46;
            int n5 = n4 += 46;
            return n5 != 0;
        });
        int n255 = 84;
        n255 += 64;
        n255 -= 46;
        int n256 = -155;
        n256 ^= 0xFFFFFFD3;
        int n258 = 70;
        n258 = n258 - 1;
        boolean bl16 = n258 + -69;
        this.agp = new BooleanValue((String)o0Oo000O0oO[n255] + (String)o0Oo000O0oO[n256 ^= 0x3D], (Module)this, (Boolean)bl16, () -> {
            int n2;
            int n3 = 400989994;
            ooo00oo0Ooo0 = 755756487;
            if (1057050803 < ooo00oo0Ooo0) {
            }
            Object[] objectArray = new Object[9];
            Integer n4 = null;
            Integer n5 = null;
            n4 = 24;
            n4 = n4 + -70;
            n4 = 53;
            if (((Mode)this.afJ.getValue()).getName().equals((String)o0Oo000O0oO[53])) {
                int n6 = 11;
                n6 += 84;
                n2 = n6 -= 95;
                return n2 != 0;
            }
            n5 = -102;
            n5 = n5 ^ 0xFFFFFFDA;
            n5 = n5 + -63;
            n2 = n5;
            return n2 != 0;
        });
        int n259 = -54;
        n259 += 34;
        int n261 = 131;
        n261 = n261 - 16;
        boolean bl17 = n261 ^ 0x72;
        this.agq = new BooleanValue((String)o0Oo000O0oO[n259 -= -118], (Module)this, (Boolean)bl17);
        int n262 = 69;
        n262 ^= 0xFFFFFF99;
        int n263 = -144;
        n263 += 68;
        n263 ^= 0xFFFFFFB4;
        this.agr = new BooleanValue((String)o0Oo000O0oO[n262 += 54], (Module)this, (Boolean)((Object)Integer.valueOf(0)));
        this.yawOffset = new ModeValue("Yaw Offset", (Module)this, () -> {
            int n2 = 561332192;
            if (92092097 >= n2) {
                throw null;
            }
            Object[] objectArray = new Object[6];
            Integer n3 = null;
            if (!((Boolean)this.agr.getValue()).booleanValue()) {
                n3 = 57;
                n3 = n3 + 49;
                n3 = 1;
                return 1 != 0;
            }
            int n4 = 40;
            n4 = 120;
            int n5 = n4 -= 120;
            return n5 != 0;
        }).a(new Mode[]{new NamedMode("0")}).a(new Mode[]{new NamedMode("45")}).a(new Mode[]{new NamedMode("-45")}).co("0");
        int n264 = 53;
        n264 -= 106;
        n264 += 78;
        int n265 = -67;
        n265 ^= 0xFFFFFFFD;
        int n267 = 9;
        n267 = n267 - 40;
        boolean bl18 = n267 + 31;
        booleanValue("Ignore Sp" + (String)o0Oo000O0oO[n264] + (String)o0Oo000O0oO[n265 ^= 0x6C], (Module)this, (Boolean)bl18, () -> {
            Object[] objectArray = new Object[7];
            Integer n2 = null;
            if (((Boolean)this.agr.getValue()).booleanValue()) {
                int n3 = 42;
                n3 ^= 0x50;
                int n4 = n3 += -122;
                return n4 != 0;
            }
            n2 = 72;
            n2 = n2 ^ 0x66;
            n2 = 1;
            return 1 != 0;
        });
        this.agt = booleanValue;
        int n268 = 115;
        n268 -= 114;
        int n270 = 196;
        n270 = n270 - 80;
        boolean bl19 = n270 + -116;
        this.agu = new BooleanValue((String)o0Oo000O0oO[n268 -= -70], (Module)this, (Boolean)bl19, () -> {
            int n2 = 1617313651;
            if (950250811 >= 1709510943) {
            }
            Object[] objectArray = new Object[7];
            Integer n3 = null;
            if ((Boolean)this.agr.getValue() != false) return 0 != 0;
            n3 = -119;
            n3 = n3 ^ 0x23;
            n3 = 1;
            return 1 != 0;
        });
        int n271 = 366;
        n271 += -119;
        n271 ^= 0x76;
        int n272 = 37;
        n272 += 83;
        int n274 = 26;
        n274 = n274 + -99;
        boolean bl20 = n274 + 73;
        this.agv = new BooleanValue((String)o0Oo000O0oO[n271] + (String)o0Oo000O0oO[n272 -= 78], (Module)this, (Boolean)bl20, () -> {
            int n2 = 1724459944;
            if (148238284 >= n2) {
            }
            Object[] objectArray = new Object[6];
            Integer n3 = null;
            if (!((Boolean)this.agr.getValue()).booleanValue()) {
                n3 = -43;
                n3 = n3 ^ 0xFFFFFFDE;
                n3 = 1;
                return 1 != 0;
            }
            int n4 = -73;
            n4 = -93;
            int n5 = n4 ^= 0xFFFFFFA3;
            return n5 != 0;
        });
        this.agy = new aka(0.0, 0.0, 0.0);
        this.agU = ahj::j;
        this.agV = preMotionEvent -> {
            ooo00oo0Ooo0 = 1373363553;
            if (1722607584 < ooo00oo0Ooo0) {
            }
            if (this.e(Flight.class).isEnabled()) {
                // empty if block
            }
            this.agy = new aka(0.0, 0.0, 0.0);
            if (this.Yw != null && this.acr != null && this.Yx != null) {
                Scaffold.mc.thePlayer.cHX.aX();
                if (((Number)this.afS.getValue()).floatValue() != 1.0f) {
                    Scaffold.mc.timer.timerSpeed = ((Number)this.afS.getValue()).floatValue();
                }
                return;
            }
        };
        this.agW = var1_1 -> {
            /*
             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
             * 
             * org.benf.cfr.reader.util.ConfusedCFRException: First case is not immediately after switch.
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.SwitchReplacer.examineSwitchContiguity(SwitchReplacer.java:442)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.SwitchReplacer.replaceRawSwitches(SwitchReplacer.java:94)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:517)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
             *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
             *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
             *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
             *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
             *     at org.benf.cfr.reader.Main.main(Main.java:54)
             */
            throw new IllegalStateException("Decompilation failed");
        };
        this.agX = fd2 -> {
            ooo00oo0Ooo0 = 523843386;
            if (597709360 > ooo00oo0Ooo0) {
                // empty if block
            }
            if (fd2.getPosY() < Scaffold.mc.thePlayer.gX - Double.longBitsToDouble(0x4000000000000000L)) {
                // empty if block
            }
        };
        this.agY = ea2 -> {
            int n2 = 65319179;
            ooo00oo0Ooo0 = n2;
            if (513527551 < ooo00oo0Ooo0) {
                throw null;
            }
            Object[] objectArray = new Object[2];
            Integer n3 = null;
            if ((Boolean)this.agp.getValue() == false) return;
            n3 = 112;
            n3 = n3 + 16;
            n3 = 132;
            if (!((Mode)this.afL.getValue()).getName().equals((String)o0Oo000O0oO[132])) {
                ea2.cF();
            }
        };
        this.agZ = var1_1 -> {
            /*
             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
             * 
             * org.benf.cfr.reader.util.ConfusedCFRException: First case is not immediately after switch.
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.SwitchReplacer.examineSwitchContiguity(SwitchReplacer.java:442)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.SwitchReplacer.replaceRawSwitches(SwitchReplacer.java:94)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:517)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
             *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
             *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
             *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
             *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
             *     at org.benf.cfr.reader.Main.main(Main.java:54)
             */
            throw new IllegalStateException("Decompilation failed");
        };
        this.aha = this::l;
        this.ahb = moveInputEvent -> {
            int n2 = 1756167063;
            if (490367594 >= n2) {
                throw new IllegalStateException();
            }
            Object[] objectArray = new Object[6];
            Double d2 = null;
            Integer n3 = null;
            this.jp = moveInputEvent.getForward();
            this.jq = moveInputEvent.getStrafe();
            int n4 = this.agD;
            n3 = -66;
            n3 = n3 - 59;
            n3 = 1;
            this.agD = n4 - 1;
            if (n4 > 0) {
                moveInputEvent.setForward(0.0f);
                moveInputEvent.setStrafe(0.0f);
            }
            if (!((Boolean)this.agk.getValue()).booleanValue()) {
                return;
            }
            d2 = ((Number)this.ago.getValue()).doubleValue();
            if (d2 <= Double.longBitsToDouble(4596373779694328218L)) {
                return;
            }
            moveInputEvent.setSneakSlowDownMultiplier(d2);
        };
        this.ahc = strafeEvent -> {
            int n2 = 2136551847;
            if (1728376341 >= 42975370) {
                n2 = 44;
            }
            if (2122346160 < (ooo00oo0Ooo0 = 42975370)) {
            }
            Object[] objectArray = new Object[7];
            Integer n3 = null;
            Integer n4 = null;
            Integer n5 = null;
            this.kp();
            n5 = 219;
            n5 = n5 - -7;
            n5 = n5 - 95;
            if (!Objects.equals(((Mode)this.yawOffset.getValue()).getName(), (String)o0Oo000O0oO[n5]) && !((Boolean)this.afU.getValue()).booleanValue()) {
                n3 = -79;
                n3 = 9;
                n3 = 73;
                if (!((Mode)this.afM.getValue()).getName().equals((String)o0Oo000O0oO[n3])) {
                    aif.useDiagonalSpeed();
                }
            }
            n4 = -9;
            n4 = n4 - -99;
            n4 = n4 - -52;
            if (((Mode)this.afO.getValue()).getName().equals((String)o0Oo000O0oO[n4]) && Scaffold.mc.thePlayer.aO && aif.isMoving() && Scaffold.mc.thePlayer.gX == this.Me) {
                Scaffold.mc.thePlayer.jump();
            }
        };
        this.ahd = packetSendEvent -> {
            int n2 = 2021726091;
            if (302645498 >= n2) {
                n2 = 1546414770;
            }
            Object[] objectArray = new Object[9];
            Integer n3 = null;
            Integer n4 = null;
            aj<?> aj2 = null;
            k k2 = null;
            aj2 = packetSendEvent.dq();
            if (aj2 instanceof k) {
                k2 = (k)aj2;
                if (!k2.xX().h(new aka(Double.longBitsToDouble(-4616189618054758400L), Double.longBitsToDouble(-4616189618054758400L), Double.longBitsToDouble(-4616189618054758400L)))) {
                    n3 = -12;
                    n3 = n3 ^ 0x42;
                    n3 = n3 ^ 0xFFFFFFB7;
                    this.agC -= n3.intValue();
                }
                if (this.ko() && this.a(k2)) {
                    n4 = 94;
                    n4 = n4 - 85;
                    n4 = 1;
                    --this.agL;
                }
            }
        };
        this.ahe = en2 -> {
            int n2;
            int n3;
            if (o0o000o0O0o0) {
            }
            if (1763557347 >= 1941418856) {
                throw new ConnectException();
            }
            Object[] objectArray = new Object[22];
            int n4 = 1899247868;
            objectArray[11] = 3684131739033133104L;
            if (Scaffold.mc.thePlayer.aO) return;
            objectArray[13] = Float.valueOf(Scaffold.mc.thePlayer.pl);
            int n5 = 205252878;
            objectArray[16] = Float.valueOf(bt.bN(((Float)objectArray[13]).floatValue()));
            if (Math.abs(((Float)objectArray[16]).floatValue() % 90.0f) <= 10.0f || Math.abs(((Float)objectArray[16]).floatValue() % 90.0f) >= 80.0f) {
                int n6 = -1009071197;
                objectArray[0] = 55;
                objectArray[0] = (Integer)objectArray[0] ^ 0x22;
                objectArray[0] = (Integer)objectArray[0] - 20;
                n2 = (Integer)objectArray[0];
            } else {
                int n7 = 123;
                n7 ^= 0x45;
                n2 = n7 ^= 0x3E;
            }
            objectArray[7] = -128;
            objectArray[7] = (Integer)objectArray[7] ^ 0xFFFFFF94;
            objectArray[7] = (Integer)objectArray[7] - -12;
            long l2 = (Long)objectArray[11];
            objectArray[2] = 59;
            objectArray[2] = (Integer)objectArray[2] ^ 0x75;
            objectArray[2] = (Integer)objectArray[2] - 46;
            objectArray[11] = l2 ^ ((long)n2 << (Integer)objectArray[7] ^ l2) & -1L << (Integer)objectArray[2];
            objectArray[3] = 91;
            objectArray[3] = (Integer)objectArray[3] ^ 0xFFFFFFBE;
            objectArray[3] = (Integer)objectArray[3] ^ 0xFFFFFFC5;
            if ((int)((Long)objectArray[11] >>> (Integer)objectArray[3]) == 0) return;
            if (!Scaffold.mc.aIp.cgI.QQ()) return;
            if ((Boolean)this.afZ.getValue() == false) return;
            if (Scaffold.mc.thePlayer.aO) return;
            int n8 = -330211241;
            objectArray[19] = 61;
            objectArray[19] = (Integer)objectArray[19] ^ 0x61;
            objectArray[19] = (Integer)objectArray[19] ^ 0x5C;
            Scaffold.mc.thePlayer.setSprinting((Integer)objectArray[19] != 0);
        };
    }
    public strictfp String km() {
        String string;
        int n2 = 729150226;
        if (302820632 >= n2) {
        }
        Object[] objectArray = new Object[11];
        Integer n3 = null;
        Integer n4 = null;
        String string2 = null;
        Integer n5 = null;
        Integer n6 = null;
        this.kn();
        string2 = ((Mode)this.afJ.getValue()).getName();
        n6 = 29;
        n6 = n6 + 5;
        n6 = 66;
        if (!string2.equals((String)o0Oo000O0oO[66])) return string2;
        if (this.ko()) {
            n3 = -69;
            n3 = n3 ^ 0x53;
            n3 = n3 - -119;
            return (String)o0Oo000O0oO[n3];
        }
        if ((Boolean)this.agp.getValue() == false) return string2;
        if (Scaffold.mc.aIp.cgM.QQ()) {
            n4 = 22;
            n4 = n4 ^ 0xFFFFFF8E;
            n4 = n4 ^ 0xFFFFFF99;
            string = (String)o0Oo000O0oO[n4];
            return string;
        }
        n5 = 20;
        n5 = n5 + 70;
        n5 = n5 - -24;
        string = (String)o0Oo000O0oO[n5];
        return string;
    }
    public static void Oo0o00000O00() {
        O0OoOO0OOOOO = new int[400];
        Scaffold.O0OoOO0OOOOO[139] = -98;
        Scaffold.O0OoOO0OOOOO[222] = -189;
        Scaffold.O0OoOO0OOOOO[279] = 46;
        Scaffold.O0OoOO0OOOOO[181] = -27;
        Scaffold.O0OoOO0OOOOO[156] = -161;
        Scaffold.O0OoOO0OOOOO[170] = 55;
        Scaffold.O0OoOO0OOOOO[293] = 120;
        Scaffold.O0OoOO0OOOOO[186] = -41;
        Scaffold.O0OoOO0OOOOO[223] = -109;
        Scaffold.O0OoOO0OOOOO[221] = -115;
        Scaffold.O0OoOO0OOOOO[147] = -45;
        Scaffold.O0OoOO0OOOOO[320] = -26;
        Scaffold.O0OoOO0OOOOO[0] = -8;
        Scaffold.O0OoOO0OOOOO[370] = -115;
        Scaffold.O0OoOO0OOOOO[385] = 60;
        Scaffold.O0OoOO0OOOOO[242] = 15;
        Scaffold.O0OoOO0OOOOO[99] = -128;
        Scaffold.O0OoOO0OOOOO[334] = -84;
        Scaffold.O0OoOO0OOOOO[304] = -53;
        Scaffold.O0OoOO0OOOOO[116] = 101;
        Scaffold.O0OoOO0OOOOO[169] = -65;
        Scaffold.O0OoOO0OOOOO[238] = 26;
        Scaffold.O0OoOO0OOOOO[185] = 31;
        Scaffold.O0OoOO0OOOOO[359] = 62;
        Scaffold.O0OoOO0OOOOO[166] = 32;
        Scaffold.O0OoOO0OOOOO[73] = 70;
        Scaffold.O0OoOO0OOOOO[151] = 113;
        Scaffold.O0OoOO0OOOOO[271] = -8;
        Scaffold.O0OoOO0OOOOO[98] = -41;
        Scaffold.O0OoOO0OOOOO[126] = -112;
        Scaffold.O0OoOO0OOOOO[363] = 273;
        Scaffold.O0OoOO0OOOOO[358] = -89;
        Scaffold.O0OoOO0OOOOO[184] = -32;
        Scaffold.O0OoOO0OOOOO[196] = 65;
        Scaffold.O0OoOO0OOOOO[258] = 95;
        Scaffold.O0OoOO0OOOOO[206] = 116;
        Scaffold.O0OoOO0OOOOO[118] = -38;
        Scaffold.O0OoOO0OOOOO[225] = -232;
        Scaffold.O0OoOO0OOOOO[343] = -116;
        Scaffold.O0OoOO0OOOOO[313] = -115;
        Scaffold.O0OoOO0OOOOO[309] = 127;
        Scaffold.O0OoOO0OOOOO[160] = -51;
        Scaffold.O0OoOO0OOOOO[239] = 12;
        Scaffold.O0OoOO0OOOOO[1] = 1;
        Scaffold.O0OoOO0OOOOO[192] = -163;
        Scaffold.O0OoOO0OOOOO[26] = -32;
        Scaffold.O0OoOO0OOOOO[159] = -53;
        Scaffold.O0OoOO0OOOOO[355] = -89;
        Scaffold.O0OoOO0OOOOO[330] = 345;
        Scaffold.O0OoOO0OOOOO[257] = -91;
        Scaffold.O0OoOO0OOOOO[384] = 186;
        Scaffold.O0OoOO0OOOOO[141] = 91;
        Scaffold.O0OoOO0OOOOO[135] = -183;
        Scaffold.O0OoOO0OOOOO[47] = 5;
        Scaffold.O0OoOO0OOOOO[66] = -36;
        Scaffold.O0OoOO0OOOOO[338] = 72;
        Scaffold.O0OoOO0OOOOO[55] = 74;
        Scaffold.O0OoOO0OOOOO[213] = -5;
        Scaffold.O0OoOO0OOOOO[188] = -75;
        Scaffold.O0OoOO0OOOOO[91] = -99;
        Scaffold.O0OoOO0OOOOO[53] = 25;
        Scaffold.O0OoOO0OOOOO[331] = 113;
        Scaffold.O0OoOO0OOOOO[194] = -91;
        Scaffold.O0OoOO0OOOOO[228] = -31;
        Scaffold.O0OoOO0OOOOO[114] = 157;
        Scaffold.O0OoOO0OOOOO[376] = 73;
        Scaffold.O0OoOO0OOOOO[327] = 70;
        Scaffold.O0OoOO0OOOOO[395] = -62;
        Scaffold.O0OoOO0OOOOO[3] = -74;
        Scaffold.O0OoOO0OOOOO[110] = 21;
        Scaffold.O0OoOO0OOOOO[365] = 14;
        Scaffold.O0OoOO0OOOOO[22] = -28;
        Scaffold.O0OoOO0OOOOO[96] = -11;
        Scaffold.O0OoOO0OOOOO[216] = 133;
        Scaffold.O0OoOO0OOOOO[168] = -10;
        Scaffold.O0OoOO0OOOOO[368] = 108;
        Scaffold.O0OoOO0OOOOO[340] = -68;
        Scaffold.O0OoOO0OOOOO[41] = -89;
        Scaffold.O0OoOO0OOOOO[180] = 5;
        Scaffold.O0OoOO0OOOOO[217] = -102;
        Scaffold.O0OoOO0OOOOO[199] = -23;
        Scaffold.O0OoOO0OOOOO[152] = -34;
        Scaffold.O0OoOO0OOOOO[399] = 97;
        Scaffold.O0OoOO0OOOOO[36] = -127;
        Scaffold.O0OoOO0OOOOO[280] = 51;
        Scaffold.O0OoOO0OOOOO[256] = -82;
        Scaffold.O0OoOO0OOOOO[39] = -109;
        Scaffold.O0OoOO0OOOOO[356] = -92;
        Scaffold.O0OoOO0OOOOO[145] = 80;
        Scaffold.O0OoOO0OOOOO[310] = 99;
        Scaffold.O0OoOO0OOOOO[388] = 10;
        Scaffold.O0OoOO0OOOOO[328] = -11;
        Scaffold.O0OoOO0OOOOO[289] = -39;
        Scaffold.O0OoOO0OOOOO[249] = 157;
        Scaffold.O0OoOO0OOOOO[172] = 1;
        Scaffold.O0OoOO0OOOOO[297] = -181;
        Scaffold.O0OoOO0OOOOO[50] = 25;
        Scaffold.O0OoOO0OOOOO[229] = -91;
        Scaffold.O0OoOO0OOOOO[81] = -278;
        Scaffold.O0OoOO0OOOOO[291] = -152;
        Scaffold.O0OoOO0OOOOO[34] = -91;
        Scaffold.O0OoOO0OOOOO[276] = 173;
        Scaffold.O0OoOO0OOOOO[155] = -76;
        Scaffold.O0OoOO0OOOOO[382] = 100;
        Scaffold.O0OoOO0OOOOO[162] = -61;
        Scaffold.O0OoOO0OOOOO[46] = 85;
        Scaffold.O0OoOO0OOOOO[18] = -6;
        Scaffold.O0OoOO0OOOOO[269] = -37;
        Scaffold.O0OoOO0OOOOO[339] = -35;
        Scaffold.O0OoOO0OOOOO[86] = 29;
        Scaffold.O0OoOO0OOOOO[171] = 19;
        Scaffold.O0OoOO0OOOOO[90] = -42;
        Scaffold.O0OoOO0OOOOO[72] = 103;
        Scaffold.O0OoOO0OOOOO[311] = -27;
        Scaffold.O0OoOO0OOOOO[187] = 98;
        Scaffold.O0OoOO0OOOOO[318] = -98;
        Scaffold.O0OoOO0OOOOO[97] = 53;
        Scaffold.O0OoOO0OOOOO[56] = -16;
        Scaffold.O0OoOO0OOOOO[40] = 19;
        Scaffold.O0OoOO0OOOOO[397] = -17;
        Scaffold.O0OoOO0OOOOO[115] = 69;
        Scaffold.O0OoOO0OOOOO[277] = 48;
        Scaffold.O0OoOO0OOOOO[158] = -112;
        Scaffold.O0OoOO0OOOOO[224] = -80;
        Scaffold.O0OoOO0OOOOO[20] = 41;
        Scaffold.O0OoOO0OOOOO[161] = 2;
        Scaffold.O0OoOO0OOOOO[102] = -93;
        Scaffold.O0OoOO0OOOOO[342] = 51;
        Scaffold.O0OoOO0OOOOO[324] = 103;
        Scaffold.O0OoOO0OOOOO[164] = 94;
        Scaffold.O0OoOO0OOOOO[29] = -14;
        Scaffold.O0OoOO0OOOOO[59] = 30;
        Scaffold.O0OoOO0OOOOO[284] = -120;
        Scaffold.O0OoOO0OOOOO[109] = -6;
        Scaffold.O0OoOO0OOOOO[241] = -77;
        Scaffold.O0OoOO0OOOOO[287] = 9;
        Scaffold.O0OoOO0OOOOO[149] = 29;
        Scaffold.O0OoOO0OOOOO[176] = -64;
        Scaffold.O0OoOO0OOOOO[8] = 28;
        Scaffold.O0OoOO0OOOOO[389] = -123;
        Scaffold.O0OoOO0OOOOO[68] = -76;
        Scaffold.O0OoOO0OOOOO[33] = -87;
        Scaffold.O0OoOO0OOOOO[231] = -43;
        Scaffold.O0OoOO0OOOOO[137] = -30;
        Scaffold.O0OoOO0OOOOO[15] = -206;
        Scaffold.O0OoOO0OOOOO[136] = -93;
        Scaffold.O0OoOO0OOOOO[122] = -60;
        Scaffold.O0OoOO0OOOOO[333] = -24;
        Scaffold.O0OoOO0OOOOO[24] = 13;
        Scaffold.O0OoOO0OOOOO[268] = -19;
        Scaffold.O0OoOO0OOOOO[106] = 118;
        Scaffold.O0OoOO0OOOOO[119] = 75;
        Scaffold.O0OoOO0OOOOO[380] = -107;
        Scaffold.O0OoOO0OOOOO[393] = 51;
        Scaffold.O0OoOO0OOOOO[142] = -76;
        Scaffold.O0OoOO0OOOOO[254] = 52;
        Scaffold.O0OoOO0OOOOO[298] = 87;
        Scaffold.O0OoOO0OOOOO[326] = 45;
        Scaffold.O0OoOO0OOOOO[117] = 127;
        Scaffold.O0OoOO0OOOOO[360] = -224;
        Scaffold.O0OoOO0OOOOO[263] = 125;
        Scaffold.O0OoOO0OOOOO[250] = 54;
        Scaffold.O0OoOO0OOOOO[183] = 2;
        Scaffold.O0OoOO0OOOOO[218] = 105;
        Scaffold.O0OoOO0OOOOO[173] = 54;
        Scaffold.O0OoOO0OOOOO[134] = -111;
        Scaffold.O0OoOO0OOOOO[270] = -114;
        Scaffold.O0OoOO0OOOOO[85] = 22;
        Scaffold.O0OoOO0OOOOO[203] = 111;
        Scaffold.O0OoOO0OOOOO[378] = 309;
        Scaffold.O0OoOO0OOOOO[259] = -66;
        Scaffold.O0OoOO0OOOOO[77] = 89;
        Scaffold.O0OoOO0OOOOO[335] = 94;
        Scaffold.O0OoOO0OOOOO[107] = 71;
        Scaffold.O0OoOO0OOOOO[178] = -86;
        Scaffold.O0OoOO0OOOOO[377] = 107;
        Scaffold.O0OoOO0OOOOO[21] = -153;
        Scaffold.O0OoOO0OOOOO[332] = -105;
        Scaffold.O0OoOO0OOOOO[288] = -109;
        Scaffold.O0OoOO0OOOOO[354] = -6;
        Scaffold.O0OoOO0OOOOO[372] = 142;
        Scaffold.O0OoOO0OOOOO[260] = 31;
        Scaffold.O0OoOO0OOOOO[65] = -30;
        Scaffold.O0OoOO0OOOOO[182] = 111;
        Scaffold.O0OoOO0OOOOO[386] = -126;
        Scaffold.O0OoOO0OOOOO[124] = -11;
        Scaffold.O0OoOO0OOOOO[362] = 14;
        Scaffold.O0OoOO0OOOOO[150] = -147;
        Scaffold.O0OoOO0OOOOO[312] = 134;
        Scaffold.O0OoOO0OOOOO[262] = -43;
        Scaffold.O0OoOO0OOOOO[87] = -171;
        Scaffold.O0OoOO0OOOOO[373] = -68;
        Scaffold.O0OoOO0OOOOO[58] = 86;
        Scaffold.O0OoOO0OOOOO[261] = 161;
        Scaffold.O0OoOO0OOOOO[197] = -21;
        Scaffold.O0OoOO0OOOOO[352] = -38;
        Scaffold.O0OoOO0OOOOO[148] = -17;
        Scaffold.O0OoOO0OOOOO[391] = 73;
        Scaffold.O0OoOO0OOOOO[35] = 12;
        Scaffold.O0OoOO0OOOOO[144] = -11;
        Scaffold.O0OoOO0OOOOO[138] = -38;
        Scaffold.O0OoOO0OOOOO[49] = 89;
        Scaffold.O0OoOO0OOOOO[129] = 52;
        Scaffold.O0OoOO0OOOOO[140] = 67;
        Scaffold.O0OoOO0OOOOO[143] = 15;
        Scaffold.O0OoOO0OOOOO[128] = -96;
        Scaffold.O0OoOO0OOOOO[146] = -72;
        Scaffold.O0OoOO0OOOOO[61] = -39;
        Scaffold.O0OoOO0OOOOO[57] = -8;
        Scaffold.O0OoOO0OOOOO[132] = -218;
        Scaffold.O0OoOO0OOOOO[5] = 123;
        Scaffold.O0OoOO0OOOOO[2] = -27;
        Scaffold.O0OoOO0OOOOO[42] = -18;
        Scaffold.O0OoOO0OOOOO[253] = -99;
        Scaffold.O0OoOO0OOOOO[235] = -36;
        Scaffold.O0OoOO0OOOOO[43] = 32;
        Scaffold.O0OoOO0OOOOO[79] = -36;
        Scaffold.O0OoOO0OOOOO[303] = 89;
        Scaffold.O0OoOO0OOOOO[204] = -180;
        Scaffold.O0OoOO0OOOOO[63] = 48;
        Scaffold.O0OoOO0OOOOO[11] = -110;
        Scaffold.O0OoOO0OOOOO[208] = -17;
        Scaffold.O0OoOO0OOOOO[201] = 24;
        Scaffold.O0OoOO0OOOOO[290] = 91;
        Scaffold.O0OoOO0OOOOO[205] = -65;
        Scaffold.O0OoOO0OOOOO[30] = -42;
        Scaffold.O0OoOO0OOOOO[387] = 193;
        Scaffold.O0OoOO0OOOOO[247] = -5;
        Scaffold.O0OoOO0OOOOO[319] = 121;
        Scaffold.O0OoOO0OOOOO[198] = -51;
        Scaffold.O0OoOO0OOOOO[193] = -41;
        Scaffold.O0OoOO0OOOOO[300] = -21;
        Scaffold.O0OoOO0OOOOO[264] = 106;
        Scaffold.O0OoOO0OOOOO[163] = -115;
        Scaffold.O0OoOO0OOOOO[344] = 68;
        Scaffold.O0OoOO0OOOOO[267] = -18;
        Scaffold.O0OoOO0OOOOO[153] = 22;
        Scaffold.O0OoOO0OOOOO[19] = 98;
        Scaffold.O0OoOO0OOOOO[317] = -122;
        Scaffold.O0OoOO0OOOOO[285] = -29;
        Scaffold.O0OoOO0OOOOO[112] = 114;
        Scaffold.O0OoOO0OOOOO[347] = -72;
        Scaffold.O0OoOO0OOOOO[210] = 181;
        Scaffold.O0OoOO0OOOOO[74] = -32;
        Scaffold.O0OoOO0OOOOO[230] = 59;
        Scaffold.O0OoOO0OOOOO[273] = -9;
        Scaffold.O0OoOO0OOOOO[374] = -73;
        Scaffold.O0OoOO0OOOOO[179] = 45;
        Scaffold.O0OoOO0OOOOO[54] = 201;
        Scaffold.O0OoOO0OOOOO[248] = -7;
        Scaffold.O0OoOO0OOOOO[64] = 77;
        Scaffold.O0OoOO0OOOOO[345] = -39;
        Scaffold.O0OoOO0OOOOO[60] = 282;
        Scaffold.O0OoOO0OOOOO[82] = 64;
        Scaffold.O0OoOO0OOOOO[195] = 43;
        Scaffold.O0OoOO0OOOOO[322] = 50;
        Scaffold.O0OoOO0OOOOO[211] = 49;
        Scaffold.O0OoOO0OOOOO[315] = 206;
        Scaffold.O0OoOO0OOOOO[353] = 85;
        Scaffold.O0OoOO0OOOOO[349] = 56;
        Scaffold.O0OoOO0OOOOO[337] = -86;
        Scaffold.O0OoOO0OOOOO[255] = -172;
        Scaffold.O0OoOO0OOOOO[191] = 49;
        Scaffold.O0OoOO0OOOOO[296] = 31;
        Scaffold.O0OoOO0OOOOO[292] = 33;
        Scaffold.O0OoOO0OOOOO[44] = 50;
        Scaffold.O0OoOO0OOOOO[75] = -33;
        Scaffold.O0OoOO0OOOOO[281] = 91;
        Scaffold.O0OoOO0OOOOO[207] = 40;
        Scaffold.O0OoOO0OOOOO[52] = 9;
        Scaffold.O0OoOO0OOOOO[105] = -46;
        Scaffold.O0OoOO0OOOOO[396] = 29;
        Scaffold.O0OoOO0OOOOO[32] = 43;
        Scaffold.O0OoOO0OOOOO[100] = 103;
        Scaffold.O0OoOO0OOOOO[274] = 70;
        Scaffold.O0OoOO0OOOOO[48] = -63;
        Scaffold.O0OoOO0OOOOO[12] = 88;
        Scaffold.O0OoOO0OOOOO[394] = 11;
        Scaffold.O0OoOO0OOOOO[9] = -5;
        Scaffold.O0OoOO0OOOOO[232] = -118;
        Scaffold.O0OoOO0OOOOO[67] = 112;
        Scaffold.O0OoOO0OOOOO[108] = 27;
        Scaffold.O0OoOO0OOOOO[282] = -115;
        Scaffold.O0OoOO0OOOOO[361] = -70;
        Scaffold.O0OoOO0OOOOO[266] = -16;
        Scaffold.O0OoOO0OOOOO[4] = -52;
        Scaffold.O0OoOO0OOOOO[329] = 10;
        Scaffold.O0OoOO0OOOOO[245] = -74;
        Scaffold.O0OoOO0OOOOO[83] = -117;
        Scaffold.O0OoOO0OOOOO[251] = 103;
        Scaffold.O0OoOO0OOOOO[62] = 87;
        Scaffold.O0OoOO0OOOOO[212] = 78;
        Scaffold.O0OoOO0OOOOO[240] = 92;
        Scaffold.O0OoOO0OOOOO[350] = 89;
        Scaffold.O0OoOO0OOOOO[246] = -13;
        Scaffold.O0OoOO0OOOOO[219] = -19;
        Scaffold.O0OoOO0OOOOO[272] = 78;
        Scaffold.O0OoOO0OOOOO[131] = 30;
        Scaffold.O0OoOO0OOOOO[154] = 68;
        Scaffold.O0OoOO0OOOOO[113] = 79;
        Scaffold.O0OoOO0OOOOO[14] = 22;
        Scaffold.O0OoOO0OOOOO[390] = -1;
        Scaffold.O0OoOO0OOOOO[27] = -131;
        Scaffold.O0OoOO0OOOOO[10] = -74;
        Scaffold.O0OoOO0OOOOO[325] = -10;
        Scaffold.O0OoOO0OOOOO[95] = 4;
        Scaffold.O0OoOO0OOOOO[398] = 61;
        Scaffold.O0OoOO0OOOOO[104] = -120;
        Scaffold.O0OoOO0OOOOO[278] = -125;
        Scaffold.O0OoOO0OOOOO[252] = -175;
        Scaffold.O0OoOO0OOOOO[227] = -21;
        Scaffold.O0OoOO0OOOOO[348] = 97;
        Scaffold.O0OoOO0OOOOO[120] = 83;
        Scaffold.O0OoOO0OOOOO[392] = 75;
        Scaffold.O0OoOO0OOOOO[234] = -109;
        Scaffold.O0OoOO0OOOOO[92] = 75;
        Scaffold.O0OoOO0OOOOO[130] = -21;
        Scaffold.O0OoOO0OOOOO[174] = 53;
        Scaffold.O0OoOO0OOOOO[175] = 116;
        Scaffold.O0OoOO0OOOOO[321] = -24;
        Scaffold.O0OoOO0OOOOO[323] = 26;
        Scaffold.O0OoOO0OOOOO[379] = 99;
        Scaffold.O0OoOO0OOOOO[190] = 64;
        Scaffold.O0OoOO0OOOOO[215] = 8;
        Scaffold.O0OoOO0OOOOO[314] = 19;
        Scaffold.O0OoOO0OOOOO[127] = 56;
        Scaffold.O0OoOO0OOOOO[299] = -125;
        Scaffold.O0OoOO0OOOOO[165] = -17;
        Scaffold.O0OoOO0OOOOO[295] = 89;
        Scaffold.O0OoOO0OOOOO[84] = 2;
        Scaffold.O0OoOO0OOOOO[214] = 69;
        Scaffold.O0OoOO0OOOOO[302] = -60;
        Scaffold.O0OoOO0OOOOO[341] = -103;
        Scaffold.O0OoOO0OOOOO[367] = -24;
        Scaffold.O0OoOO0OOOOO[51] = -34;
        Scaffold.O0OoOO0OOOOO[177] = -41;
        Scaffold.O0OoOO0OOOOO[71] = -45;
        Scaffold.O0OoOO0OOOOO[88] = -50;
        Scaffold.O0OoOO0OOOOO[275] = -80;
        Scaffold.O0OoOO0OOOOO[364] = -121;
        Scaffold.O0OoOO0OOOOO[89] = 122;
        Scaffold.O0OoOO0OOOOO[78] = 13;
        Scaffold.O0OoOO0OOOOO[17] = -94;
        Scaffold.O0OoOO0OOOOO[70] = -101;
        Scaffold.O0OoOO0OOOOO[305] = 36;
        Scaffold.O0OoOO0OOOOO[45] = 17;
        Scaffold.O0OoOO0OOOOO[202] = 20;
        Scaffold.O0OoOO0OOOOO[383] = 118;
        Scaffold.O0OoOO0OOOOO[80] = 85;
        Scaffold.O0OoOO0OOOOO[133] = -107;
        Scaffold.O0OoOO0OOOOO[69] = -11;
        Scaffold.O0OoOO0OOOOO[226] = -98;
        Scaffold.O0OoOO0OOOOO[351] = 256;
        Scaffold.O0OoOO0OOOOO[103] = -66;
        Scaffold.O0OoOO0OOOOO[38] = 60;
        Scaffold.O0OoOO0OOOOO[167] = 50;
        Scaffold.O0OoOO0OOOOO[220] = 97;
        Scaffold.O0OoOO0OOOOO[357] = -151;
        Scaffold.O0OoOO0OOOOO[6] = 8;
        Scaffold.O0OoOO0OOOOO[369] = -49;
        Scaffold.O0OoOO0OOOOO[16] = 112;
        Scaffold.O0OoOO0OOOOO[28] = 18;
        Scaffold.O0OoOO0OOOOO[283] = 4;
        Scaffold.O0OoOO0OOOOO[306] = -172;
        Scaffold.O0OoOO0OOOOO[294] = 70;
        Scaffold.O0OoOO0OOOOO[23] = -126;
        Scaffold.O0OoOO0OOOOO[200] = 74;
        Scaffold.O0OoOO0OOOOO[381] = 19;
        Scaffold.O0OoOO0OOOOO[307] = 59;
        Scaffold.O0OoOO0OOOOO[94] = -35;
        Scaffold.O0OoOO0OOOOO[244] = -87;
        Scaffold.O0OoOO0OOOOO[111] = 144;
        Scaffold.O0OoOO0OOOOO[121] = -106;
        Scaffold.O0OoOO0OOOOO[237] = -37;
        Scaffold.O0OoOO0OOOOO[93] = 167;
        Scaffold.O0OoOO0OOOOO[125] = -94;
        Scaffold.O0OoOO0OOOOO[336] = -157;
        Scaffold.O0OoOO0OOOOO[123] = -83;
        Scaffold.O0OoOO0OOOOO[157] = -50;
        Scaffold.O0OoOO0OOOOO[301] = 38;
        Scaffold.O0OoOO0OOOOO[25] = 19;
        Scaffold.O0OoOO0OOOOO[13] = -65;
        Scaffold.O0OoOO0OOOOO[371] = 14;
        Scaffold.O0OoOO0OOOOO[101] = -25;
        Scaffold.O0OoOO0OOOOO[209] = -57;
        Scaffold.O0OoOO0OOOOO[7] = 20;
        Scaffold.O0OoOO0OOOOO[233] = 95;
        Scaffold.O0OoOO0OOOOO[316] = -17;
        Scaffold.O0OoOO0OOOOO[243] = 58;
        Scaffold.O0OoOO0OOOOO[189] = -9;
        Scaffold.O0OoOO0OOOOO[37] = -43;
        Scaffold.O0OoOO0OOOOO[366] = 51;
        Scaffold.O0OoOO0OOOOO[308] = -110;
        Scaffold.O0OoOO0OOOOO[76] = 56;
        Scaffold.O0OoOO0OOOOO[31] = 0;
        Scaffold.O0OoOO0OOOOO[346] = 96;
        Scaffold.O0OoOO0OOOOO[286] = 20;
        Scaffold.O0OoOO0OOOOO[265] = 89;
        Scaffold.O0OoOO0OOOOO[375] = 34;
        Scaffold.O0OoOO0OOOOO[236] = 119;
    }

    /*
     * Unable to fully structure code
     */
    public strictfp void l(MoveInputEvent var1_1) {
        block41: {
            block38: {
                block34: {
                    block33: {
                        block29: {
                            block32: {
                                block37: {
                                    block31: {
                                        block36: {
                                            block30: {
                                                block35: {
                                                    var40_2 = 86078380;
                                                    Scaffold.ooo00oo0Ooo0 = var40_2;
                                                    if (1225747363 >= Scaffold.ooo00oo0Ooo0) break block35;
                                                    var40_2 = -1150721182;
                                                    break block35;
lbl6:
                                                    // 1 sources

                                                    while (true) {
                                                        var38_3[3] = Float.valueOf(Scaffold.mc.thePlayer.pl);
                                                        var38_3[18] = Float.valueOf(bt.bN(((Float)var38_3[3]).floatValue()));
                                                        var34_4 = -446042251;
                                                        break block29;
lbl11:
                                                        // 1 sources

                                                        while (true) {
                                                            if (Math.abs(((Float)var38_3[18]).floatValue() % 90.0f) <= 10.0f) break block30;
                                                            var34_4 = -720157399;
                                                            break block29;
                                                            break;
                                                        }
lbl15:
                                                        // 1 sources

                                                        while (true) {
                                                            if (!(Math.abs(((Float)var38_3[18]).floatValue() % 90.0f) >= 80.0f)) break block31;
                                                            break block30;
                                                            break;
                                                        }
                                                        break;
                                                    }
                                                }
                                                if (!Scaffold.o0o000o0O0o0) break block36;
                                                90 + 60;
                                                break block36;
                                            }
                                            var38_3[4] = -71;
                                            var38_3[4] = (Integer)var38_3[4] + 85;
                                            var38_3[4] = (Integer)var38_3[4] ^ 15;
                                            v0 = (Integer)var38_3[4];
                                            break block37;
                                        }
                                        if (Scaffold.o0o000o0O0o0) {
                                            throw new IndexOutOfBoundsException();
                                        }
                                        break block38;
                                    }
                                    var16_5 = -94;
                                    var16_5 -= -114;
                                    var35_6 = -1478407947;
                                    v0 = var16_5 += -20;
                                }
                                var38_3[10] = 88;
                                var38_3[10] = (Integer)var38_3[10] ^ -55;
                                var38_3[10] = (Integer)var38_3[10] ^ -79;
                                v1 = (Long)var38_3[16];
                                var38_3[24] = 181;
                                var38_3[24] = (Integer)var38_3[24] - 120;
                                var38_3[24] = (Integer)var38_3[24] ^ 29;
                                var38_3[16] = v1 ^ ((long)v0 << (Integer)var38_3[10] ^ v1) & -1L << (Integer)var38_3[24];
                                var36_7 = 1542916122;
                                break block33;
lbl49:
                                // 1 sources

                                while (true) {
                                    if (!Scaffold.mc.thePlayer.aO || !aif.enoughMovementForSprinting()) ** GOTO lbl74
                                    if (!((Boolean)this.afZ.getValue()).booleanValue()) break block32;
                                    var36_7 = -1529878495;
                                    break block33;
                                    break;
                                }
lbl54:
                                // 1 sources

                                while (true) {
                                    if (Scaffold.mc.thePlayer.cqP >= this.agO) {
                                        var38_3[26] = 128;
                                        var38_3[26] = (Integer)var38_3[26] + -66;
                                        var38_3[26] = (Integer)var38_3[26] - 61;
                                        var1_1.setJump((boolean)((Integer)var38_3[26]).intValue());
                                    }
                                    ** GOTO lbl74
                                    break;
                                }
                            }
                            var36_7 = 1861565043;
                            break block33;
lbl64:
                            // 2 sources

                            while (true) {
                                if (!Scaffold.o0o000o0O0o0) break block34;
                                break block34;
                                break;
                            }
lbl69:
                            // 1 sources

                            while (true) {
                                var38_3[35] = -74;
                                var38_3[35] = (Integer)var38_3[35] - -115;
                                var38_3[35] = (Integer)var38_3[35] ^ 40;
                                var1_1.setJump((boolean)((Integer)var38_3[35]).intValue());
lbl74:
                                // 4 sources

                                while (true) {
                                    block40: {
                                        block39: {
                                            var38_3[21] = -8;
                                            var38_3[21] = (Integer)var38_3[21] - 68;
                                            var38_3[21] = (Integer)var38_3[21] ^ -74;
                                            if (Scaffold.mc.thePlayer.tR == (Integer)var38_3[21]) break block39;
                                            var37_8 = -1677655356;
                                            var38_3[7] = -26;
                                            var38_3[7] = (Integer)var38_3[7] - 68;
                                            var38_3[7] = (Integer)var38_3[7] ^ -95;
                                            if (Scaffold.mc.thePlayer.tR == (Integer)var38_3[7]) break block39;
                                            var38_3[14] = 245;
                                            var38_3[14] = (Integer)var38_3[14] ^ 116;
                                            var38_3[14] = (Integer)var38_3[14] + -125;
                                            if (Scaffold.mc.thePlayer.tR == (Integer)var38_3[14]) break block39;
                                            var38_3[20] = -163;
                                            var38_3[20] = (Integer)var38_3[20] - -74;
                                            var38_3[20] = (Integer)var38_3[20] + 94;
                                            if (Scaffold.mc.thePlayer.tR != (Integer)var38_3[20]) break block40;
                                        }
                                        if (Scaffold.mc.aIp.cgI.QQ()) {
                                            // empty if block
                                        }
                                    }
lbl96:
                                    // 2 sources

                                    return;
                                }
                                break;
                            }
                        }
                        switch (var34_4) {
                            default: {
                                ** continue;
                            }
                            ** case -720157399:
lbl103:
                            // 1 sources

                            ** continue;
                        }
                    }
                    switch (var36_7) {
                        default: {
                            ** continue;
                        }
                        case -1529878495: {
                            ** continue;
                        }
                        case 1861565043: {
                            ** continue;
                        }
                        ** case 332963950:
lbl113:
                        // 1 sources

                        ** continue;
                    }
                }
                var38_3 = new Object[36];
                var38_3[16] = 7071921249845620351L;
                if (!((Boolean)this.agk.getValue()).booleanValue()) {
                    var38_3[0] = 2;
                    var38_3[0] = (Integer)var38_3[0] - -49;
                    var38_3[0] = (Integer)var38_3[0] - 16;
                    if (!((Mode)this.afM.getValue()).getName().equals((String)Scaffold.o0Oo000O0oO[(Integer)var38_3[0]])) {
                        var38_3[19] = 11;
                        var38_3[19] = (Integer)var38_3[19] + 99;
                        var38_3[19] = (Integer)var38_3[19] - 110;
                        var1_1.setSneak((boolean)((Integer)var38_3[19]).intValue());
                    }
                }
                break block41;
            }
            var40_2 = 1563686768;
            if (344250087 < var40_2) ** GOTO lbl64
            99 & 1;
            ** while (true)
        }
        var38_3[30] = 9;
        var38_3[30] = (Integer)var38_3[30] + 90;
        var38_3[30] = (Integer)var38_3[30] - -47;
        ** while (!this.km().equals((Object)((String)Scaffold.o0Oo000O0oO[((Integer)var38_3[30]).intValue()])))
lbl138:
        // 1 sources

        ** while (true)
    }
    public strictfp boolean ki() {
        int n2 = 89174431;
        ooo00oo0Ooo0 = n2;
        if (388044798 <= ooo00oo0Ooo0) {
        }
        Object[] objectArray = new Object[9];
        Integer n3 = null;
        if (Scaffold.mc.thePlayer != null && this.isEnabled() && ((Boolean)this.afZ.getValue()).booleanValue()) {
            n3 = 84;
            n3 = n3 - 4;
            n3 = n3 ^ 0x5D;
            if (((String)o0Oo000O0oO[n3]).equals(this.km()) && aif.enoughMovementForSprinting()) {
                Integer.valueOf(-49);
                Integer.valueOf(-121);
                Integer.valueOf(1);
                return 1 != 0;
            }
        }
        int n4 = -71;
        n4 = 29;
        int n5 = n4 ^= 0x1D;
        return n5 != 0;
    }

    public strictfp aka a(c c2, float f2) {
        Object[] objectArray = new Object[6];
        Double d2 = null;
        Double d3 = null;
        Double d4 = null;
        d3 = c2.cqX + (c2.distance - c2.cqX) * (double)f2;
        d2 = c2.cqY + (c2.gX - c2.cqY) * (double)f2 + (double)c2.getEyeHeight();
        d4 = c2.cqZ + (c2.gY - c2.cqZ) * (double)f2;
        return new aka(d3, d2, d4);
    }

    public strictfp Boolean n(b b2) {
        ooo00oo0Ooo0 = 184746120;
        if (208683669 > ooo00oo0Ooo0) {
            // empty if block
        }
        return b2.equals(this.Yx);
    }
    public strictfp Vector2f a(b b2, aib aib2, float f2, float f3, float f4, int n2) {
        ooo00oo0Ooo0 = 805086078;
        if (916140317 <= ooo00oo0Ooo0) {
            // empty if block
        }
        Object[] objectArray = new Object[3];
        Float f5 = null;
        Float f6 = null;
        f6 = Float.valueOf(bt.bN(Scaffold.mc.thePlayer.pl));
        f5 = Float.valueOf(bt.I((float)(Double.longBitsToDouble(0x4055400000000000L) + Math.random() * Double.longBitsToDouble(4591870180066957722L)), f3, f4));
        return aiu.m(new Vector2f(f6.floatValue(), f5.floatValue()));
    }

    @Generated
    public strictfp int kv() {
        return this.acu;
    }
    public strictfp float l(float f2) {
        ooo00oo0Ooo0 = 1599426505;
        if (2044623641 <= ooo00oo0Ooo0) {
            // empty if block
        }
        Object[] objectArray = new Object[1];
        Float f3 = null;
        f3 = Float.valueOf(bt.bN(f2));
        return (float)Math.round(f3.floatValue() / 45.0f) * 45.0f;
    }
    @Override
    public strictfp void onDisable() {
        Vector2f vector2f;
        int n2 = 1197237115;
        ooo00oo0Ooo0 = n2;
        if (1894936233 < ooo00oo0Ooo0) {
            n2 = 96;
        }
        Object[] objectArray = new Object[8];
        Integer n3 = null;
        Integer n4 = null;
        Integer n5 = null;
        n3 = 30;
        n3 = n3 + -91;
        n3 = n3 + 63;
        if (Scaffold.mc.thePlayer.aQx % n3 == 0) {
            n5 = -13;
            n5 = n5 - -4;
            n5 = 0;
            this.agx = 0;
        }
        vector2f(Scaffold.mc.thePlayer.pl, Scaffold.mc.thePlayer.pm);
        this.h(vector2f);
        this.kj();
        n4 = 65;
        n4 = n4 ^ 0x5A;
        n4 = n4 + -27;
        Scaffold.mc.thePlayer.crh = n4.intValue();
    }
    public strictfp void kn() {
        Object[] objectArray = new Object[10];
        Integer n2 = null;
        Integer n3 = null;
        Integer n4 = null;
        Integer n5 = null;
        if (Scaffold.mc.thePlayer != null && ((Boolean)this.afZ.getValue()).booleanValue() && ((Boolean)this.agf.getValue()).booleanValue()) {
            n3 = 129;
            n3 = n3 + -37;
            n3 = n3 - 91;
            if (Scaffold.mc.thePlayer.Zl != n3) return;
            if (this.agM == Scaffold.mc.thePlayer.aQx) return;
            n5 = 8;
            n5 = n5 + -68;
            n5 = n5 - -70;
            this.agL = n5;
            this.agM = Scaffold.mc.thePlayer.aQx;
            return;
        }
        n2 = 92;
        n2 = n2 + 16;
        n2 = n2 - 108;
        this.agL = n2;
        n4 = 0;
        n4 = n4 ^ 5;
        n4 = -1;
        this.agM = -1;
    }
    public strictfp boolean a(k k2) {
        int n2 = 93252543;
        ooo00oo0Ooo0 = n2;
        if (124336764 < ooo00oo0Ooo0) {
        }
        Object[] objectArray = new Object[5];
        Integer n3 = null;
        if (!k2.xX().h(new aka(Double.longBitsToDouble(-4616189618054758400L), Double.longBitsToDouble(-4616189618054758400L), Double.longBitsToDouble(-4616189618054758400L))) && k2.ld() != null && k2.ld().bP() instanceof af) {
            n3 = 37;
            n3 = n3 ^ 0x40;
            n3 = 1;
            return 1 != 0;
        }
        int n4 = 60;
        n4 = 92;
        int n5 = n4 += -92;
        return n5 != 0;
    }
    public static strictfp Object[] cfr_renamed_1() {
        Object[] objectArray = cfr_renamed_0;
        if (cfr_renamed_0 != null) return objectArray;
        cfr_renamed_0 = new Object[4];
        objectArray = cfr_renamed_0;
        return objectArray;
    }

    public strictfp Vector2f a(b b2, ao ao2, float f2, boolean bl2) {
        if (!false) {
            // empty if block
        }
        return this.a(b2, ao2, f2, bl2, 82.0f);
    }
    public strictfp void h(Vector2f vector2f) {
        int n2 = 614086487;
        ooo00oo0Ooo0 = n2;
        if (1489560446 <= ooo00oo0Ooo0) {
        }
        Object[] objectArray = new Object[2];
        Integer n3 = null;
        n3 = -40;
        n3 = n3 - 21;
        n3 = 0;
        this.agP = 0;
        this.agS = vector2f.x;
        this.agT = vector2f.y;
        this.agQ = vector2f.x;
        this.agR = vector2f.y;
    }

    public strictfp float[] a(aka aka2, aka aka3) {
        Object[] objectArray = new Object[20];
        Integer n2 = null;
        Float f2 = null;
        Double d2 = null;
        Float f3 = null;
        Double d3 = null;
        Integer n3 = null;
        Integer n4 = null;
        Double d4 = null;
        Double d5 = null;
        d3 = aka3.x - aka2.x;
        d2 = aka3.y - aka2.y;
        d5 = aka3.z - aka2.z;
        d4 = Math.sqrt(d3 * d3 + d5 * d5);
        f2 = Float.valueOf((float)Math.toDegrees(Math.atan2(d5, d3)) - 90.0f);
        f3 = Float.valueOf((float)(-Math.toDegrees(Math.atan2(d2, d4))));
        f2 = Float.valueOf(bt.bN(f2.floatValue()));
        f3 = Float.valueOf(bt.I(f3.floatValue(), -90.0f, 90.0f));
        n2 = 95;
        n2 = n2 ^ 0x32;
        n2 = n2 ^ 0x6F;
        float[] fArray = new float[n2.intValue()];
        n4 = -194;
        n4 = n4 + 115;
        n4 = n4 ^ 0xFFFFFFB1;
        fArray[n4.intValue()] = f2.floatValue();
        n3 = -81;
        n3 = n3 - 4;
        n3 = 1;
        fArray[1] = f3.floatValue();
        return fArray;
    }

    /*
     * Exception decompiling
     */
    public strictfp void D(int var1_1) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Extractable last case doesn't follow previous, and can't clone.
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.SwitchReplacer.examineSwitchContiguity(SwitchReplacer.java:611)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.SwitchReplacer.replaceRawSwitches(SwitchReplacer.java:94)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:517)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    @Generated
    public strictfp void M(int n2) {
        if (!false) {
            // empty if block
        }
        this.acu = n2;
    }
    public strictfp boolean kh() {
        int n2 = 1087683062;
        if (1083765470 >= n2) {
            n2 = 10;
        }
        Object[] objectArray = new Object[5];
        if (((Boolean)this.afZ.getValue()).booleanValue() && this.age.wB().intValue() > 0) {
            Integer.valueOf(29);
            Integer.valueOf(1);
            return 1 != 0;
        }
        int n3 = 92;
        n3 = 12;
        int n4 = n3 += -12;
        return n4 != 0;
    }
    public strictfp da kt() {
        int n2 = 434887295;
        ooo00oo0Ooo0 = n2;
        if (1426109913 < ooo00oo0Ooo0) {
        }
        if (997345769 >= 1420734718) {
        }
        if (351967125 >= 1373607930) {
        }
        Object[] objectArray = new Object[9];
        ce ce2 = null;
        da da2 = null;
        Integer n3 = null;
        Integer n4 = null;
        Integer n5 = null;
        da2 = new da((double)this.Yx.wf() + Math.random(), (double)this.Yx.wg() + Math.random(), (double)this.Yx.wj() + Math.random());
        ce2 = aef.c(bs.fk, Scaffold.mc.bfL.Hd());
        if (this.acr == null) {
            return da2;
        }
        switch (this.acr.vb()) {
            case dvI: {
                da2.dzZ = this.Yx.wg();
                break;
            }
            case dvJ: {
                n3 = -24;
                n3 = n3 + 96;
                n3 = n3 + -71;
                da2.dzZ = this.Yx.wg() + n3;
                break;
            }
            case dvK: {
                da2.dAa = this.Yx.wj();
                break;
            }
            case dvN: {
                n4 = 77;
                n4 = n4 + -49;
                n4 = n4 + -27;
                da2.dzY = this.Yx.wf() + n4;
                break;
            }
            case dvL: {
                n5 = -21;
                n5 = n5 + 68;
                n5 = 1;
                da2.dAa = this.Yx.wj() + 1;
                break;
            }
            case dvM: {
                da2.dzY = this.Yx.wf();
                break;
            }
        }
        if (ce2 == null) return da2;
        if (ce2.dg() == null) return da2;
        if (ce2.dak == null) return da2;
        if (!ce2.dg().equals(this.Yx)) return da2;
        if (ce2.dyN != this.acr.vb()) return da2;
        return ce2.dak;
    }
    public strictfp boolean ks() {
        b b2;
        b2(Scaffold.mc.thePlayer.distance, this.Me - 1.0, Scaffold.mc.thePlayer.gY);
        return aih.o(Scaffold.mc.thePlayer.distance, this.Me - 1.0, Scaffold.mc.thePlayer.gY).a(Scaffold.mc.thePlayer.burning, b2);
    }

    public strictfp boolean kl() {
        Object[] objectArray = new Object[2];
        Integer n2 = null;
        n2 = -188;
        n2 = n2 - -85;
        n2 = 24;
        return ((Mode)this.afL.getValue()).getName().equals((String)o0Oo000O0oO[24]);
    }

    /*
     * Unable to fully structure code
     */
    public strictfp void kp() {
        block19: {
            block20: {
                var23_1 = 590599629;
                Scaffold.ooo00oo0Ooo0 = var23_1;
                if (1026608665 < Scaffold.ooo00oo0Ooo0) {
                    throw null;
lbl5:
                    // 1 sources

                    while (true) {
                        var19_5 = -2143854197;
                        v0 = var11_8 += 126;
lbl8:
                        // 2 sources

                        while (true) {
                            continue;
                            break;
                        }
                        break;
                    }
                }
                var23_1 = 624106104;
                Scaffold.ooo00oo0Ooo0 = var23_1;
                if (1215695303 > Scaffold.ooo00oo0Ooo0) break block20;
                break block20;
                var21_7[9] = -80;
                var21_7[9] = (Integer)var21_7[9] ^ 100;
                var21_7[9] = (Integer)var21_7[9] ^ -12;
                v1 = (Long)var21_7[14];
                var21_7[8] = -19;
                var21_7[8] = (Integer)var21_7[8] - 20;
                var21_7[8] = (Integer)var21_7[8] ^ -7;
                var21_7[14] = v1 ^ ((long)v0 << (Integer)var21_7[9] ^ v1) & -1L << (Integer)var21_7[8];
                var20_6 = 1622218970;
                if (!Scaffold.mc.thePlayer.aO || aif.enoughMovementForSprinting()) {
                    // empty if block
                }
lbl27:
                // 4 sources

                while (true) {
                    block21: {
                        var17_3 = -539051328;
                        break block21;
lbl30:
                        // 1 sources

                        return;
                    }
lbl33:
                    // 2 sources

                    while (true) {
                        switch (var17_3) {
                            case -539051328: {
                                ** continue;
                            }
                            default: {
                                break block19;
                            }
                        }
                        break;
                    }
                    break;
                }
            }
            var21_7 = new Object[20];
            var16_2 = 1096901299;
            var21_7[14] = -4929980300125532866L;
            var21_7[13] = -92;
            var21_7[13] = (Integer)var21_7[13] - -65;
            var21_7[13] = (Integer)var21_7[13] + 112;
            ** while (!this.km().equals((Object)((String)Scaffold.o0Oo000O0oO[((Integer)var21_7[13]).intValue()])))
lbl47:
            // 1 sources

            var17_3 = 2126044813;
            ** while (true)
        }
        var21_7[17] = Float.valueOf(Scaffold.mc.thePlayer.pl);
        var21_7[16] = Float.valueOf(bt.bN(((Float)var21_7[17]).floatValue()));
        var18_4 = -188868161;
        block15: while (true) {
            switch (var18_4) {
                default: {
                    if (!(Math.abs(((Float)var21_7[16]).floatValue() % 90.0f) <= 10.0f)) {
                        var18_4 = 1054108513;
                        continue block15;
                    }
                    ** GOTO lbl62
                }
                case 1054108513: {
                    if (!(Math.abs(((Float)var21_7[16]).floatValue() % 90.0f) >= 80.0f)) break block15;
                }
lbl62:
                // 3 sources

                case -908051744: {
                    var21_7[2] = -38;
                    var21_7[2] = (Integer)var21_7[2] ^ -10;
                    var21_7[2] = (Integer)var21_7[2] + -43;
                    v0 = (Integer)var21_7[2];
                    ** continue;
                }
            }
            break;
        }
        var11_8 = -8;
        var11_8 += -118;
        ** while (true)
    }

    /*
     * Recovered potentially malformed switches.  Disable with '--allowmalformedswitch false'
     * Unable to fully structure code
     * Enabled aggressive block sorting
     */
    public strictfp Vector2f a(b var1_1, aib var2_2, boolean var3_3, float var4_4, float var5_5, float var6_6) {
        if (Scaffold.o0o000o0O0o0) {
            48 & 1;
        }
        if (Scaffold.o0o000o0O0o0) {
        }
        if (1942025177 < (Scaffold.ooo00oo0Ooo0 = (var48_18 = 2890462))) {
        }
        if (1314317721 < (Scaffold.ooo00oo0Ooo0 = (var48_18 = 173357569))) {
            var48_18 = -1446781167;
        }
        if (1735972329 < (Scaffold.ooo00oo0Ooo0 = (var48_18 = 1687300755))) {
            var48_18 = 12;
        }
        var46_17 = new Object[39];
        var32_7 = 180088889;
        var46_17[25] = -7868409899082659037L;
        var46_17[27] = -100621615793956434L;
        if (var2_2 == null) {
            return new Vector2f(var4_4, var5_5);
        }
        var33_8 = 469682884;
        var46_17[15] = var2_2.vb();
        var34_9 = 2121246203;
        var46_17[29] = Float.valueOf(Scaffold.mc.thePlayer.pl);
        var46_17[20] = Float.valueOf((float)Math.toDegrees(aif.direction()));
        var46_17[26] = new ArrayList<E>();
        ((ArrayList)var46_17[26]).add(Float.valueOf(this.l(((Float)var46_17[29]).floatValue() + var6_6)));
        ((ArrayList)var46_17[26]).add(Float.valueOf(this.l(((Float)var46_17[29]).floatValue() + 45.0f + var6_6)));
        var35_10 = -626211818;
        ** GOTO lbl123
lbl31:
        // 3 sources

        while (true) {
            var46_17[21] = 65;
            var46_17[21] = (Integer)var46_17[21] - 7;
            var46_17[21] = (Integer)var46_17[21] + -26;
            if ((int)((Long)var46_17[27] >>> (Integer)var46_17[21]) == 0) {
                ((ArrayList)var46_17[5]).add(Float.valueOf(((Float)var46_17[0]).floatValue()));
            }
            block32: while (true) {
                var40_15 = -1855233298;
                switch (var40_15) {
                    default: {
                        continue block32;
                    }
                    case -1855233298: 
                }
                break;
            }
lbl44:
            // 2 sources

            while (!var12_16.hasNext()) {
                var12_16 = null;
                var42_19 = -2145501359;
                var46_17[0] = Float.valueOf(3.4028235E38f);
                var46_17[2] = ((ArrayList)var46_17[5]).iterator();
lbl49:
                // 4 sources

                while (true) {
                    var43_20 = -592803901;
                    ** GOTO lbl79
lbl52:
                    // 2 sources

                    while (true) {
                        var44_21 = -1340856279;
                        ** GOTO lbl94
                        break;
                    }
lbl55:
                    // 2 sources

                    while (true) {
                        var44_21 = -330720434;
                        ** GOTO lbl94
                        break;
                    }
                    while (true) {
                        if (var12_16 != null) {
                            return var12_16;
                        }
                        var43_20 = -192708483;
                        ** GOTO lbl79
                        break;
                    }
                    break;
                }
            }
            ** GOTO lbl136
            break;
        }
lbl64:
        // 1 sources

        block38: while (true) {
            switch (var37_12) {
                case 582543280: {
                    ** GOTO lbl31
                }
                case 294018447: {
                    var46_17[6] = Float.valueOf(((Float)((Iterator)var46_17[38]).next()).floatValue());
                    if (!(Math.abs(bt.bN(((Float)var46_17[0]).floatValue() - ((Float)var46_17[6]).floatValue())) < 0.1f)) ** GOTO lbl144
                    var38_13 = -1763606233;
                    v0 = (Long)var46_17[27];
                    var46_17[11] = -119;
                    var46_17[11] = (Integer)var46_17[11] ^ 26;
                    var46_17[11] = (Integer)var46_17[11] ^ -77;
                    var46_17[27] = v0 ^ (0x100000000L ^ v0) & -1L << (Integer)var46_17[11];
                    var39_14 = -1574743411;
                    ** GOTO lbl31
                }
lbl79:
                // 2 sources

                block39: while (true) {
                    switch (var43_20) {
                        default: {
                            ** GOTO lbl49
                        }
                        case -592803901: {
                            if (!((Iterator)var46_17[2]).hasNext()) ** continue;
                            var43_20 = -2086158142;
                            continue block39;
                        }
                        case -2086158142: {
                            var46_17[38] = Float.valueOf(((Float)((Iterator)var46_17[2]).next()).floatValue());
                            var46_17[6] = this.a(var1_1, (ao)var46_17[15], ((Float)var46_17[38]).floatValue(), var3_3);
                            if ((Vector2f)var46_17[6] != null) ** GOTO lbl55
                            ** GOTO lbl52
                        }
                        case -192708483: 
                    }
                    break;
                }
                return aiu.a(new aka(var1_1.wf(), var1_1.wg(), var1_1.wj()), (ao)var46_17[15]);
lbl94:
                // 2 sources

                block40: while (true) {
                    switch (var44_21) {
                        default: {
                            ** continue;
                        }
                        case -1340856279: {
                            ** GOTO lbl49
                        }
                        case -728052901: {
                            ** continue;
                        }
                        case -330720434: {
                            var44_21 = 1317713947;
                            continue block40;
                        }
                        case 1317713947: 
                    }
                    break;
                }
                var46_17[7] = Float.valueOf(Math.abs(bt.bN(((Vector2f)var46_17[6]).x - var4_4)));
                var46_17[14] = Float.valueOf(Math.abs(((Vector2f)var46_17[6]).y - var5_5));
                var46_17[8] = Float.valueOf(((Float)var46_17[7]).floatValue() * ((Float)var46_17[7]).floatValue() + ((Float)var46_17[14]).floatValue() * ((Float)var46_17[14]).floatValue());
                var46_17[1] = Float.valueOf(Math.abs(bt.bN(((Vector2f)var46_17[6]).x - this.l(((Vector2f)var46_17[6]).x))));
                var46_17[33] = Float.valueOf(Math.abs(bt.bN(((Vector2f)var46_17[6]).x - ((Float)var46_17[29]).floatValue())));
                var45_22 = -160569857;
                block41: while (true) {
                    switch (var45_22) {
                        default: {
                            var46_17[8] = Float.valueOf(((Float)var46_17[8]).floatValue() + (((Float)var46_17[1]).floatValue() * 0.001f + ((Float)var46_17[33]).floatValue() * 5.0E-4f));
                            if (((Float)var46_17[8]).floatValue() < ((Float)var46_17[0]).floatValue()) {
                                var46_17[0] = Float.valueOf(((Float)var46_17[8]).floatValue());
                                var12_16 = (Vector2f)var46_17[6];
                            }
                            var45_22 = 538555015;
                            continue block41;
                        }
                        case 538555015: 
                    }
                    break;
                }
                ** continue;
lbl123:
                // 1 sources

                block42: while (true) {
                    switch (var35_10) {
                        default: {
                            ((ArrayList)var46_17[26]).add(Float.valueOf(this.l(((Float)var46_17[29]).floatValue() - 45.0f + var6_6)));
                            ((ArrayList)var46_17[26]).add(Float.valueOf(this.l(((Float)var46_17[20]).floatValue() + var6_6)));
                            var35_10 = -1704277894;
                            continue block42;
                        }
                        case -1704277894: 
                    }
                    break;
                }
                var46_17[5] = new ArrayList<E>();
                var12_16 = ((ArrayList)var46_17[26]).iterator();
                ** GOTO lbl44
lbl136:
                // 1 sources

                var46_17[0] = Float.valueOf(((Float)var12_16.next()).floatValue());
                var36_11 = -1615162699;
                v1 = (Long)var46_17[27];
                var46_17[13] = 56;
                var46_17[13] = (Integer)var46_17[13] ^ -49;
                var46_17[13] = (Integer)var46_17[13] + 41;
                var46_17[27] = v1 ^ (0L ^ v1) & -1L << (Integer)var46_17[13];
                var46_17[38] = ((ArrayList)var46_17[5]).iterator();
lbl144:
                // 2 sources

                if (((Iterator)var46_17[38]).hasNext()) break block38;
                ** continue;
            }
            break;
        }
        var37_12 = 294018447;
        ** while (true)
    }
    public strictfp void c(boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, boolean bl7) {
        if (bl2) {
            Scaffold.mc.aIp.cgJ.ay(Keyboard.isKeyDown(Scaffold.mc.aIp.cgJ.cO()));
        }
        if (bl3) {
            Scaffold.mc.aIp.cgI.ay(Keyboard.isKeyDown(Scaffold.mc.aIp.cgI.cO()));
        }
        if (bl4) {
            Scaffold.mc.aIp.cgH.ay(Keyboard.isKeyDown(Scaffold.mc.aIp.cgH.cO()));
        }
        if (bl5) {
            Scaffold.mc.aIp.cgF.ay(Keyboard.isKeyDown(Scaffold.mc.aIp.cgF.cO()));
        }
        if (bl6) {
            Scaffold.mc.aIp.cgE.ay(Keyboard.isKeyDown(Scaffold.mc.aIp.cgE.cO()));
        }
        if (bl7) {
            Scaffold.mc.aIp.cgG.ay(Keyboard.isKeyDown(Scaffold.mc.aIp.cgG.cO()));
        }
    }

    /*
     * Exception decompiling
     */
    public strictfp Vector2f a(b var1_1, ao var2_2, float var3_3, boolean var4_4, float var5_5) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: First case is not immediately after switch.
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.SwitchReplacer.examineSwitchContiguity(SwitchReplacer.java:442)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.SwitchReplacer.replaceRawSwitches(SwitchReplacer.java:94)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:517)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    /*
     * Exception decompiling
     */
    public strictfp double kr() {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Extractable last case doesn't follow previous, and can't clone.
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.SwitchReplacer.examineSwitchContiguity(SwitchReplacer.java:611)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.SwitchReplacer.replaceRawSwitches(SwitchReplacer.java:94)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:517)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }
    public strictfp void kq() {
        int n2 = 1865344710;
        ooo00oo0Ooo0 = 1709510943;
        if (1903851136 < ooo00oo0Ooo0) {
        }
        if (213902627 <= (ooo00oo0Ooo0 = 69891570)) {
            n2 = 1079823098;
        }
        if (1056363059 >= 1180720852) {
        }
        Object[] objectArray = new Object[7];
        Double d2 = null;
        Integer n3 = null;
        if (Scaffold.mc.aIp.cgI.QQ()) {
            d2 = this.kr();
            if (!Double.isNaN(d2)) {
                this.Me = d2;
            }
        }
        if ((Scaffold.mc.thePlayer.gX < this.Me || Scaffold.mc.thePlayer.aO) && !aif.isMoving()) {
            this.Me = Math.floor(Scaffold.mc.thePlayer.gX);
        }
        n3 = 72;
        n3 = 106;
        n3 = 2;
        if (Scaffold.mc.thePlayer.cqP > n3 && Math.floor(Scaffold.mc.thePlayer.gX) != this.Me && !(aih.p(0.0, Double.longBitsToDouble(-4616189618054758400L), 0.0) instanceof BlockAir)) {
            this.Me = Math.floor(Scaffold.mc.thePlayer.gX);
        }
    }
    public strictfp boolean a(ao ao2) {
        da da2;
        Object[] objectArray = new Object[15];
        Integer n2 = null;
        Integer n3 = null;
        da da3 = null;
        db db2 = null;
        da da4 = null;
        Double d2 = null;
        da2(Scaffold.mc.thePlayer.ud, 0.0, Scaffold.mc.thePlayer.uf);
        da4 = da2;
        if (da4.atE() < Double.longBitsToDouble(4547007122018943789L)) {
            n2 = 81;
            n2 = n2 ^ 0xFFFFFF88;
            n2 = n2 + 39;
            return n2 != 0;
        }
        da4 = da4.atD();
        db2 = ao2.asy();
        da3 = new da(db2.wf(), 0.0, db2.wj()).atD();
        d2 = da4.t(da3);
        if (!(d2 < 0.0)) return 0 != 0;
        n3 = 90;
        n3 = n3 ^ 0x62;
        n3 = 1;
        return 1 != 0;
    }

    /*
     * Unable to fully structure code
     */
    public strictfp void a(b var1_1, float var2_2) {
        block62: {
            block69: {
                block64: {
                    block67: {
                        block68: {
                            block66: {
                                block61: {
                                    block60: {
                                        block59: {
                                            block63: {
                                                var72_3 = 242592800;
                                                Scaffold.ooo00oo0Ooo0 = var72_3;
                                                if (1900220845 >= Scaffold.ooo00oo0Ooo0) break block63;
                                                break block63;
lbl7:
                                                // 1 sources

                                                while (true) {
                                                    var70_9[23] = -33;
                                                    var70_9[23] = (Integer)var70_9[23] + 36;
                                                    var70_9[23] = (Integer)var70_9[23] - -29;
                                                    var70_9[54] = v0 ^ ((long)v1 << (Integer)var70_9[41] ^ v0) & -1L << (Integer)var70_9[23];
                                                    if (!Scaffold.mc.aIp.cgF.QQ() && !Scaffold.mc.aIp.cgH.QQ()) {
                                                        break block59;
                                                    }
                                                    ** GOTO lbl72
                                                    break;
                                                }
                                            }
                                            if (!Scaffold.o0o000o0O0o0) break block64;
                                            7 & 1;
                                            break block64;
                                        }
lbl21:
                                        // 2 sources

                                        while (true) {
                                            if (!aif.enoughMovementForSprinting()) ** GOTO lbl39
                                            var69_16 = -2137099172;
                                            break block60;
                                            break;
                                        }
lbl25:
                                        // 1 sources

                                        while (true) {
                                            var69_16 = 1675875599;
                                            break block60;
                                            break;
                                        }
lbl28:
                                        // 2 sources

                                        while (true) {
                                            if (!Scaffold.o0o000o0O0o0) break block61;
                                            51 & 1;
                                            break block61;
                                            break;
                                        }
lbl33:
                                        // 1 sources

                                        while (true) {
                                            block65: {
                                                if (aif.speed() < Double.longBitsToDouble(8990290933606730628L ^ 4860764734450343054L)) break block65;
                                                var70_9[18] = -21;
                                                var70_9[18] = (Integer)var70_9[18] + -41;
                                                var70_9[18] = (Integer)var70_9[18] ^ -30;
                                                if ((int)((Long)var70_9[54] >>> (Integer)var70_9[18]) == 0) ** GOTO lbl72
                                            }
lbl40:
                                            // 2 sources

                                            while (true) {
                                                var69_16 = 612587837;
                                                break block60;
                                                break;
                                            }
                                            break;
                                        }
lbl43:
                                        // 1 sources

                                        while (true) {
                                            if (!(aif.speed() < Double.longBitsToDouble(4808354887668155705L ^ 9038816865994751960L)) && Scaffold.mc.thePlayer.aO) ** GOTO lbl53
                                            var69_16 = -511490201;
                                            break block60;
                                            break;
                                        }
lbl47:
                                        // 1 sources

                                        while (true) {
                                            if (!Scaffold.mc.aIp.cgI.QQ()) lbl-1000:
                                            // 2 sources

                                            {
                                                while (true) {
                                                    bs.a(new Vector2f(this.acs - 30.0f, this.act), Double.longBitsToDouble(4556142355098279105L ^ 9159947074177768641L), (Boolean)this.afU.getValue() != false ? cb.gH : cb.gG);
                                                    var69_16 = -219966864;
                                                    break block60;
                                                    break;
                                                }
                                            }
lbl53:
                                            // 3 sources

                                            if (!(aif.speed() < Double.longBitsToDouble(-7546891630513116663L ^ -6298498212393739974L))) ** GOTO lbl60
lbl54:
                                            // 2 sources

                                            while (true) {
                                                var69_16 = 497564885;
                                                break block60;
                                                break;
                                            }
                                            break;
                                        }
lbl57:
                                        // 1 sources

                                        while (true) {
                                            if (!Scaffold.mc.aIp.cgI.QQ()) {
                                                bs.a(new Vector2f(this.acs - 45.0f, this.act), Double.longBitsToDouble(-4739976425562917176L ^ -136171706483427640L), (Boolean)this.afU.getValue() != false ? cb.gH : cb.gG);
                                            }
lbl60:
                                            // 5 sources

                                            while (true) {
                                                var69_16 = -1114394801;
                                                break block60;
                                                break;
                                            }
                                            break;
                                        }
lbl63:
                                        // 1 sources

                                        while (true) {
                                            if (!Scaffold.mc.aIp.cgI.QQ()) ** GOTO lbl-1000
lbl65:
                                            // 2 sources

                                            while (true) {
                                                if (this.e(Speed.class).isEnabled()) lbl-1000:
                                                // 2 sources

                                                {
                                                    while (true) {
                                                        bs.a(new Vector2f(this.acs - 10.0f, this.act), Double.longBitsToDouble(-5588978367069799627L ^ -987425447803995339L), (Boolean)this.afU.getValue() != false ? cb.gH : cb.gG);
                                                        break;
                                                    }
                                                } else if (Scaffold.mc.aIp.cgI.QQ()) {
                                                    bs.a(new Vector2f(this.acs - 10.0f, this.act), Double.longBitsToDouble(-755733887536824115L ^ -5357286806802628403L), (Boolean)this.afU.getValue() != false ? cb.gH : cb.gG);
                                                }
lbl72:
                                                // 6 sources

                                                return;
                                            }
                                            break;
                                        }
                                    }
                                    switch (var69_16) {
                                        default: {
                                            ** continue;
                                        }
                                        case -2137099172: {
                                            ** continue;
                                        }
                                        case 1675875599: {
                                            ** continue;
                                        }
                                        case -237940030: {
                                            ** continue;
                                        }
                                        case 612587837: {
                                            ** continue;
                                        }
                                        case -511490201: {
                                            ** continue;
                                        }
                                        case 1517928011: {
                                            ** continue;
                                        }
                                        case -219966864: {
                                            ** continue;
                                        }
                                        case -657042402: {
                                            ** continue;
                                        }
                                        case 497564885: {
                                            ** continue;
                                        }
                                        case -1114394801: {
                                            ** continue;
                                        }
                                        case -634593699: {
                                            ** continue;
                                        }
                                        ** case -1204941712:
lbl100:
                                        // 1 sources

                                        ** continue;
                                    }
                                }
                                var70_9 = new Object[67];
                                var60_12 = -323294650;
                                var70_9[50] = 2385033817242211221L;
                                var70_9[54] = 9082541415915227660L;
                                var70_9[62] = this.a(Scaffold.mc.thePlayer, Scaffold.mc.timer.bWq);
                                var70_9[30] = new aka((double)var1_1.wf() + Double.longBitsToDouble(1575604834300601048L ^ 3043778312823382744L), (double)var1_1.wg() + Double.longBitsToDouble(-5666507520295144846L ^ -8161501713858399630L), (double)var1_1.wj() + Double.longBitsToDouble(-8842472957891522803L ^ -4996398876117119219L));
                                var70_9[10] = Float.valueOf(Scaffold.mc.thePlayer.pl);
                                var70_9[45] = Math.toRadians(((Float)var70_9[10]).floatValue());
                                var70_9[65] = new aka(-Math.sin((Double)var70_9[45]), 0.0, Math.cos((Double)var70_9[45]));
                                var70_9[66] = ((aka)var70_9[65]).wh();
                                var61_13 = -1937411924;
                                block46: while (true) {
                                    switch (var61_13) {
                                        default: {
                                            if ((Double)var70_9[66] != 0.0) {
                                                var70_9[65] = new aka(((aka)var70_9[65]).x / (Double)var70_9[66], 0.0, ((aka)var70_9[65]).z / (Double)var70_9[66]);
                                            }
                                            var61_13 = 1015337682;
                                            continue block46;
                                        }
                                        case 1015337682: 
                                    }
                                    break;
                                }
                                var70_9[19] = Math.toRadians(((Float)var70_9[10]).floatValue());
                                var70_9[61] = Math.cos((Double)var70_9[19]);
                                var70_9[7] = (Double)var70_9[61] * 1.0;
                                if (!((Double)var70_9[7] < 0.0)) break block66;
                                var70_9[13] = 74;
                                var70_9[13] = (Integer)var70_9[13] + -114;
                                var70_9[13] = (Integer)var70_9[13] + 41;
                                v2 = (Integer)var70_9[13];
                                break block67;
                            }
                            var39_10 = 61;
                            var39_10 ^= -120;
                            break block68;
lbl134:
                            // 1 sources

                            while (true) {
                                var72_3 = 73871384;
                                Scaffold.ooo00oo0Ooo0 = var72_3;
                                if (309779753 > Scaffold.ooo00oo0Ooo0) ** GOTO lbl28
                                Scaffold.ooo00oo0Ooo0 = var72_3 = 1187171543;
                                ** continue;
                                break;
                            }
                        }
                        v2 = var39_10 ^= -75;
                    }
                    var70_9[5] = 58;
                    var70_9[5] = (Integer)var70_9[5] - 36;
                    var70_9[5] = (Integer)var70_9[5] + 10;
                    v3 = (Long)var70_9[50];
                    var70_9[25] = 111;
                    var70_9[25] = (Integer)var70_9[25] - 97;
                    var70_9[25] = (Integer)var70_9[25] - -18;
                    var70_9[50] = v3 ^ ((long)v2 << (Integer)var70_9[5] ^ v3) & -1L << (Integer)var70_9[25];
                    var62_14 = -1380209348;
                    var70_9[8] = new aka(((aka)var70_9[65]).z, 0.0, -((aka)var70_9[65]).x);
                    var70_9[0] = ((aka)var70_9[8]).wh();
                    if ((Double)var70_9[0] != 0.0) {
                        var70_9[8] = new aka(((aka)var70_9[8]).x / (Double)var70_9[0], 0.0, ((aka)var70_9[8]).z / (Double)var70_9[0]);
                    }
                    break block69;
                }
                ** while (1916233288 < (var72_3 = 2051029620))
lbl159:
                // 1 sources

                throw new UnsupportedOperationException();
            }
            var70_9[36] = Double.longBitsToDouble(-5728459179330403311L ^ 1107765961490742430L);
            var70_9[14] = ((aka)var70_9[30]).e(((aka)var70_9[8]).ag(-((Double)var70_9[36]).doubleValue()));
            var63_15 = 1577465867;
            var70_9[14] = ((aka)var70_9[14]).v(0.0, (Math.random() - Double.longBitsToDouble(1723032180005901991L ^ 2884960883867489959L)) * Double.longBitsToDouble(329629404254572533L ^ 4267879365779887727L), 0.0);
            var70_9[37] = this.a((aka)var70_9[62], (aka)var70_9[14]);
            var70_9[11] = Float.valueOf(this.acs);
            var64_4 = -2049979623;
            var70_9[33] = Float.valueOf(this.act);
            var65_5 = -266985993;
            var70_9[60] = -75;
            var70_9[60] = (Integer)var70_9[60] + 99;
            var70_9[60] = (Integer)var70_9[60] - 24;
            var70_9[1] = Float.valueOf(this.c(((Float)var70_9[11]).floatValue(), ((float[])var70_9[37])[(Integer)var70_9[60]], var2_2));
            var70_9[53] = -25;
            var70_9[53] = (Integer)var70_9[53] - -119;
            var70_9[53] = (Integer)var70_9[53] + -93;
            var70_9[38] = Float.valueOf(this.c(((Float)var70_9[33]).floatValue(), ((float[])var70_9[37])[(Integer)var70_9[53]], var2_2));
            this.acs = ((Float)var70_9[1]).floatValue();
            this.act = ((Float)var70_9[38]).floatValue();
            var70_9[47] = 151;
            var70_9[47] = (Integer)var70_9[47] - 41;
            var66_6 = -5084025;
            block48: while (true) {
                switch (var66_6) {
                    default: {
                        var70_9[47] = (Integer)var70_9[47] - 110;
                        var66_6 = -876993905;
                        continue block48;
                    }
                    case -876993905: 
                }
                break;
            }
            bs.d((boolean)((Integer)var70_9[47]).intValue());
            var70_9[34] = Float.valueOf(Scaffold.mc.thePlayer.pl);
            var70_9[51] = Float.valueOf(bt.bN(((Float)var70_9[34]).floatValue()));
            var67_7 = 1956738156;
            block49: while (true) {
                block70: {
                    switch (var67_7) {
                        default: {
                            if (!(Math.abs(((Float)var70_9[51]).floatValue() % 90.0f) <= 10.0f)) {
                                var67_7 = 2059205457;
                                continue block49;
                            }
                            ** GOTO lbl203
                        }
                        case 2059205457: {
                            if (!(Math.abs(((Float)var70_9[51]).floatValue() % 90.0f) >= 80.0f)) break block70;
lbl203:
                            // 2 sources

                            var70_9[46] = 47;
                            var68_8 = 1757655631;
                            break;
                        }
                    }
                    var70_9[46] = (Integer)var70_9[46] - -29;
                    var70_9[46] = (Integer)var70_9[46] ^ 77;
                    v1 = (Integer)var70_9[46];
                    break block62;
                }
                var67_7 = 1183164273;
                continue;
                {
                    case 1183164273: {
                        var67_7 = 491418153;
                        continue block49;
                    }
                    ** case 491418153:
                }
                break;
            }
lbl217:
            // 1 sources

            var53_11 = 59;
            var53_11 += -120;
            v1 = var53_11 -= -61;
        }
        var70_9[41] = 88;
        var70_9[41] = (Integer)var70_9[41] - 43;
        var70_9[41] = (Integer)var70_9[41] ^ 13;
        v0 = (Long)var70_9[54];
        ** while (true)
    }
    public strictfp boolean ko() {
        Object[] objectArray = new Object[7];
        Integer n2 = null;
        if ((Boolean)this.afZ.getValue() == false) return 0 != 0;
        if ((Boolean)this.agf.getValue() == false) return 0 != 0;
        if (this.agL <= 0) return 0 != 0;
        n2 = 64;
        n2 = n2 - 53;
        n2 = 1;
        return 1 != 0;
    }
    @Override
    public strictfp void onEnable() {
        Object[] objectArray = new Object[19];
        Integer n2 = null;
        Integer n3 = null;
        Integer n4 = null;
        Integer n5 = null;
        Integer n6 = null;
        Integer n7 = null;
        Integer n8 = null;
        VkMode vkMode = null;
        this.acs = Scaffold.mc.thePlayer.pl - 180.0f + (float)Integer.parseInt(((Mode)this.yawOffset.getValue()).getName());
        this.act = 90.0f;
        this.agA = (float)((Math.random() - Double.longBitsToDouble(4602678819172646912L)) * (Math.random() - Double.longBitsToDouble(4602678819172646912L)) * Double.longBitsToDouble(0x4024000000000000L));
        this.agz = (float)((Math.random() - Double.longBitsToDouble(4602678819172646912L)) * (Math.random() - Double.longBitsToDouble(4602678819172646912L)) * Double.longBitsToDouble(0x4024000000000000L));
        n7 = 8;
        n7 = n7 ^ 0x28;
        n7 = n7 + -32;
        this.agK = n7;
        this.Me = Math.floor(Scaffold.mc.thePlayer.gX);
        this.Yw = null;
        this.agw = null;
        if (((Boolean)this.agh.getValue()).booleanValue()) {
            n8 = -40;
            n8 = n8 + 99;
            n8 = n8 - 58;
            this.agN = n8.intValue();
        } else {
            n3 = -120;
            n3 = n3 + 59;
            n3 = n3 + 61;
            this.agN = n3.intValue();
        }
        this.agO = this.age.ww().intValue();
        this.h(new Vector2f(this.acs, this.act));
        n4 = -14;
        n4 = n4 - -108;
        n4 = n4 + -95;
        this.agB = n4;
        n5 = -26;
        n5 = n5 + 52;
        n5 = n5 + -26;
        this.agF = n5;
        n2 = 43;
        n2 = n2 + -37;
        n2 = n2 ^ 6;
        this.agC = n2;
        n6 = -85;
        n6 = n6 + 81;
        n6 = 0;
        Scaffold.mc.thePlayer.crh = 0;
        Object t2 = this.afM.getValue();
        if (t2 instanceof VkMode) {
            vkMode = (VkMode)t2;
            vkMode.kD().onEnable();
        }
    }
    public strictfp float b(float f2, float f3, float f4) {
        Object[] objectArray = new Object[2];
        Float f5 = null;
        f5 = Float.valueOf(bt.bN(f3 - f2));
        if (f5.floatValue() > f4) {
            f5 = Float.valueOf(f4);
        }
        if (!(f5.floatValue() < -f4)) return f2 + f5.floatValue();
        f5 = Float.valueOf(-f4);
        return f2 + f5.floatValue();
    }
    public strictfp boolean L(int n2) {
        b b2;
        b2(Scaffold.mc.thePlayer);
        return aih.p(this.agy.getX(), (double)(-n2) + this.agy.getY(), this.agy.getZ()).a((w)Scaffold.mc.bfW, b2.hr(n2));
    }
    public strictfp boolean K(int n2) {
        b b2;
        b2(bt.aF(Scaffold.mc.thePlayer.distance), bt.aF(Scaffold.mc.thePlayer.gX) - n2, bt.aF(Scaffold.mc.thePlayer.gY));
        return Scaffold.mc.thePlayer.burning.H(new b(bt.aF(Scaffold.mc.thePlayer.distance), bt.aF(Scaffold.mc.thePlayer.gX) - n2, bt.aF(Scaffold.mc.thePlayer.gY))).df().a(Scaffold.mc.thePlayer.burning, b2);
    }

    @Generated
    public strictfp boolean kw() {
        return this.agH;
    }
    public strictfp void ku() {
        int n2 = 1881739924;
        if (458864917 >= n2) {
            throw null;
        }
        Object[] objectArray = new Object[28];
        Long l2 = null;
        Integer n3 = null;
        Integer n4 = null;
        Float f2 = null;
        Integer n5 = null;
        Integer n6 = null;
        Integer n7 = null;
        Integer n8 = null;
        Integer n9 = null;
        da da2 = null;
        Integer n10 = null;
        Integer n11 = null;
        l2 = -900152777313574570L;
        n3 = 27;
        n3 = n3 ^ 0x55;
        n3 = n3 - 75;
        if (this.agE > n3) {
            n9 = 118;
            n9 = n9 ^ 0x50;
            n9 = n9 + 113;
            n5 = 68;
            n5 = n5 - -127;
            n5 = n5 + -111;
            n10 = -128;
            n10 = n10 ^ 0xFFFFFFCA;
            n10 = n10 + -74;
            afi.c((String)o0Oo000O0oO[n9] + (String)o0Oo000O0oO[n5], new Object[n10.intValue()]);
            return;
        }
        if (this.acr == null) {
            return;
        }
        n8 = 79;
        n8 = n8 + 66;
        n8 = n8 - -22;
        n6 = 129;
        n6 = n6 - 40;
        n6 = n6 + -57;
        long l3 = l2;
        n4 = 46;
        n4 = n4 - 68;
        n4 = n4 + 54;
        l2 = l3 ^ ((long)((Mode)this.afK.getValue()).getName().equals((String)o0Oo000O0oO[n8]) << n6 ^ l3) & -1L << n4;
        da2 = this.kt();
        n7 = 10;
        n7 = n7 + 16;
        n7 = 32;
        if ((int)(l2 >>> 32) != 0) {
            h h2;
            f2 = Float.valueOf(Scaffold.mc.thePlayer.pl + bt.bN(this.acs - Scaffold.mc.thePlayer.pl));
            ahj.l(new h(Scaffold.mc.thePlayer.distance, Scaffold.mc.thePlayer.gX, Scaffold.mc.thePlayer.gY, f2.floatValue(), this.act, Scaffold.mc.thePlayer.aO));
            d d2 = Scaffold.mc.bfL;
            c c2 = Scaffold.mc.thePlayer;
            j j2 = Scaffold.mc.bfW;
            this.d(bu.class);
            if (d2.a(c2, j2, bu.bO(), this.Yx, this.acr.vb(), da2)) {
                ahj.l(new m());
            }
            h2(Scaffold.mc.thePlayer.distance, Scaffold.mc.thePlayer.gX, Scaffold.mc.thePlayer.gY, (float)((double)Scaffold.mc.thePlayer.pl + Math.random() * Double.longBitsToDouble(4584304132692975288L)), (float)Math.clamp((double)((double)Scaffold.mc.thePlayer.pm - Math.random()), (double)Double.longBitsToDouble(-4587338432941916160L), (double)Double.longBitsToDouble(4636033603912859648L)), Scaffold.mc.thePlayer.aO);
            ahj.l(h2);
            return;
        }
        n11 = 188;
        n11 = n11 - 85;
        n11 = n11 + 56;
        if (((Mode)this.afL.getValue()).getName().equals((String)o0Oo000O0oO[n11])) {
            mc.AA();
            return;
        }
        d d3 = Scaffold.mc.bfL;
        c c3 = Scaffold.mc.thePlayer;
        j j3 = Scaffold.mc.bfW;
        this.d(bu.class);
        if (!d3.a(c3, j3, bu.bO(), this.Yx, this.acr.vb(), da2)) return;
        ahj.l(new m());
    }

    public strictfp void kj() {
        Object[] objectArray = new Object[12];
        Integer n2 = null;
        Integer n3 = null;
        Integer n4 = null;
        Integer n5 = null;
        Integer n6 = null;
        Integer n7 = null;
        n2 = 16;
        n2 = -813312694;
        n2 = 813312731;
        n6 = 91;
        n6 = n6 - 58;
        n6 = n6 + -32;
        n3 = 75;
        n3 = n3 + -4;
        n3 = n3 + -70;
        n5 = -96;
        n5 = n5 - -7;
        n5 = n5 ^ 0xFFFFFFA6;
        n4 = -103;
        n4 = n4 + -2;
        n4 = n4 - -106;
        n7 = -16;
        n7 = n7 ^ 0xFFFFFFBD;
        n7 = 1;
        this.c(n2 != 0, n6 != 0, n3 != 0, n5 != 0, n4 != 0, true);
    }
    public static strictfp float m(float f2) {
        ooo00oo0Ooo0 = 94353833;
        if (1040293449 <= ooo00oo0Ooo0) {
            // empty if block
        }
        if (f2 > 89.9f) {
            return 89.9f;
        }
        switch (1888496139) {
            default: {
                if (!(f2 < -89.9f)) return f2;
                return -89.9f;
            }
            case 1099576420: 
        }
        return f2;
    }
}



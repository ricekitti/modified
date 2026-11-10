/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package hackclient.rise;

import hackclient.rise.aef;
import hackclient.rise.ahj;
import hackclient.rise.aib;
import hackclient.rise.aif;
import hackclient.rise.aih;
import hackclient.rise.aiu;
import hackclient.rise.ajy;
import hackclient.rise.aka;
import hackclient.rise.akd;
import hackclient.rise.akf;
import hackclient.rise.akg;
import hackclient.rise.akm;
import hackclient.rise.akn;
import hackclient.rise.akp;
import hackclient.rise.bs;
import hackclient.rise.cb;
import hackclient.rise.dk;
import hackclient.rise.dm;
import hackclient.rise.dz;
import hackclient.rise.ea;
import hackclient.rise.eg;
import hackclient.rise.ei;
import hackclient.rise.ej;
import hackclient.rise.en;
import hackclient.rise.eo;
import hackclient.rise.fd;
import hackclient.rise.fh;
import hackclient.rise.fi;
import hackclient.rise.gi;
import hackclient.rise.gj;
import hackclient.rise.gl;
import hackclient.rise.lw;
import hackclient.rise.mh;
import hackclient.rise.ub;
import hackclient.rise.va;
import hackclient.rise.vb;
import hackclient.rise.vc;
import hackclient.rise.ve;
import hackclient.rise.vf;
import hackclient.rise.vg;
import hackclient.rise.vh;
import hackclient.rise.vi;
import hackclient.rise.vj;
import hackclient.rise.vk;
import hackclient.rise.vm;
import hackclient.rise.vn;
import hackclient.rise.vp;
import hackclient.rise.vr;
import hackclient.rise.vs;
import hackclient.rise.vt;
import hackclient.rise.vu;
import hackclient.rise.vv;
import hackclient.rise.vw;
import hackclient.rise.vx;
import hackclient.rise.vy;
import hackclient.rise.vz;
import hackclient.rise.wa;
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
import net.minecraft.item.af;
import net.minecraft.network.play.client.k;
import net.minecraft.util.ao;
import net.minecraft.util.b;
import net.minecraft.util.bt;
import net.minecraft.util.ce;
import net.minecraft.util.da;
import net.minecraft.util.db;
import net.minecraft.world.w;
import org.lwjgl.input.Keyboard;

@gl(aliases={"module.player.scaffold.name"}, description="module.player.scaffold.description", category=gj.PLAYER)
public class tn
extends gi {
    public float agS;
    @dm
    public dk<ea> agY;
    public akf agr;
    static public boolean ahf;
    @dm
    public dk<fd> agX;
    public akf agb;
    public int agL;
    public boolean agH;
    @dm(cH=2)
    public dk<ej> agZ;
    public akf afW;
    static public Object[] oO00O0OO0ooO;
    public akf agj;
    public aka agy;
    @dm
    public dk<ei> agV;
    public int agO;
    public akg age;
    public akm afP;
    @dm
    public dk<eo> ahc;
    public int agI;
    public float acs;
    public int acu;
    public akg agg;
    static public Object[] cfr_renamed_1;
    public aib agw;
    public akf agk;
    public akf agt;
    public akm afJ;
    public boolean agN;
    public b Yx;
    public akf afU;
    public akg agl;
    public boolean agx;
    static public Object[] o0Oo000O0oO;
    public akg agn;
    public akf agd;
    public akm afO;
    public akg afQ;
    public akm afK;
    public int agB;
    public float agT;
    public float jq;
    public int agD;
    public boolean agP;
    public int agF;
    public int agG;
    @dm
    public dk<fi> ahd;
    public akn afS;
    public aib acr;
    @dm
    public dk<dz> aha;
    public double Me;
    public float agz;
    public akf agp;
    public akm afL;
    public float agQ;
    public akg agm;
    public akn afT;
    public int agJ;
    public akf agu;
    @dm
    public dk<eg> agW;
    @dm
    public dk<dz> ahb;
    public float agR;
    @dm
    public dk<en> ahe;
    public akf afZ;
    public akf agh;
    public int agM;
    static public Object[] cfr_renamed_0;
    public akn ago;
    public int agE;
    static public Object Oo0o00000O00;
    public float agA;
    public int agC;
    public da Yw;
    public float act;
    public akm afM;
    public akm ags;
    public akf afV;
    public akf afY;
    public akg agi;
    public akg afR;
    static public int[] O0OoOO0OOOOO;
    @dm
    public dk<fh> agU;
    public akm afN;
    public akf agf;
    public akf afX;
    public int agK;
    public akf aga;
    public float jp;
    public akf agv;
    public akf agq;
    public akf agc;
    static public int ooo00oo0Ooo0;
    static public boolean o0o000o0O0o0;

    /*
     * Exception decompiling
     */
    public strictfp void kk() {
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
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public static strictfp Object o0Oo000O0oO(Object[] var0) {
        block74: {
            block75: {
                block73: {
                    block69: {
                        block70: {
                            block68: {
                                block63: {
                                    block64: {
                                        block66: {
                                            block72: {
                                                block65: {
                                                    block71: {
                                                        if (!tn.o0o000o0O0o0) break block71;
                                                        94 + 84;
                                                        break block71;
lbl5:
                                                        // 1 sources

                                                        while (true) {
                                                            ((byte[])var46_8[25])[7] = 32;
                                                            var41_9 = 676320658;
                                                            break block63;
lbl9:
                                                            // 1 sources

                                                            while (true) {
                                                                ((byte[])var46_8[25])[14] = -28;
                                                                ((byte[])var46_8[25])[9] = 63;
                                                                ((byte[])var46_8[25])[12] = -82;
                                                                var41_9 = -810408077;
                                                                break block63;
                                                                break;
                                                            }
lbl15:
                                                            // 1 sources

                                                            while (true) {
                                                                ((byte[])var46_8[25])[15] = 40;
                                                                ((byte[])var46_8[25])[8] = -113;
                                                                ((byte[])var46_8[25])[0] = 113;
                                                                ((byte[])var46_8[25])[1] = 17;
                                                                ((byte[])var46_8[25])[4] = 30;
                                                                ((byte[])var46_8[25])[3] = 110;
                                                                var46_8[12] = new PBEKeySpec(new String((byte[])var46_8[28], StandardCharsets.UTF_8).toCharArray(), (byte[])var46_8[25], 10, 256);
                                                                var46_8[20] = ((SecretKeyFactory)var46_8[16]).generateSecret((PBEKeySpec)var46_8[12]).getEncoded();
                                                                v0 = (byte[])var46_8[20];
                                                                v1 = tn.cfr_renamed_1()[2];
                                                                if (v1 != null) break block64;
                                                                var46_8[15] = "\u1aae\u1aa2\u1ab4".toCharArray();
                                                                var46_8[17] = 0;
lbl29:
                                                                // 2 sources

                                                                while ((Integer)var46_8[17] < 3) {
                                                                    break block65;
                                                                }
                                                                break block66;
                                                                break;
                                                            }
                                                            break;
                                                        }
                                                    }
                                                    if (1703909794 > (tn.ooo00oo0Ooo0 = (var48_1 = 707760329))) break block72;
                                                    var48_1 = 667145289;
                                                    break block72;
                                                }
                                                var46_8[6] = (int)((char[])var46_8[15])[(Integer)var46_8[17]];
                                                var46_8[6] = (Integer)var46_8[6] ^ 3105;
                                                var46_8[6] = (Integer)var46_8[6] - 53091;
                                                var46_8[6] = (Integer)var46_8[6] ^ 43272;
                                                var46_8[6] = (Integer)var46_8[6] ^ 59438;
                                                var46_8[6] = (Integer)var46_8[6] - 26608;
                                                var46_8[6] = (Integer)var46_8[6] - 53299;
                                                var46_8[6] = (Integer)var46_8[6] ^ 60115;
                                                var46_8[6] = (Integer)var46_8[6] - 28532;
                                                var46_8[6] = (Integer)var46_8[6] + 37428;
                                                var46_8[6] = (Integer)var46_8[6] - 20662;
                                                var46_8[6] = (Integer)var46_8[6] - 63932;
                                                var46_8[6] = (Integer)var46_8[6] + 25788;
                                                var46_8[6] = (Integer)var46_8[6] ^ 25727;
                                                ((char[])var46_8[15])[((Integer)var46_8[17]).intValue()] = (char)((Integer)var46_8[6]).intValue();
                                                var46_8[17] = (Integer)var46_8[17] + 1;
                                                ** GOTO lbl29
                                            }
                                            if (123056492 >= (tn.ooo00oo0Ooo0 = (var48_1 = 51747435))) break block73;
                                            tn.ooo00oo0Ooo0 = var48_1 = -1472012378;
                                            break block73;
                                        }
                                        v2 = new String((char[])var46_8[15]);
                                        v1 = v2;
                                        tn.cfr_renamed_1()[2] = v2;
                                    }
                                    tn.Oo0o00000O00 = new SecretKeySpec(v0, (String)v1);
lbl64:
                                    // 2 sources

                                    while (true) {
                                        block67: {
                                            var46_8[23] = Base64.getDecoder().decode((String)var46_8[35]);
                                            var46_8[1] = Arrays.copyOfRange((byte[])var46_8[23], 0, 16);
                                            var42_10 = -1493307077;
                                            var46_8[2] = Arrays.copyOfRange((byte[])var46_8[23], 16, ((byte[])var46_8[23]).length);
                                            v3 = tn.cfr_renamed_1()[3];
                                            if (v3 != null) ** GOTO lbl119
                                            var46_8[34] = "\ua041\ua04d\ua07b\ua35f\ua04b\ua04e\ua04b\ua35f\ua07c\ua073\ua04b\ua07b\ua35d\ua07c\ua061\ua068\ua068\ua069\ua012\ua017".toCharArray();
                                            var43_11 = 1958600111;
                                            var46_8[31] = 0;
lbl74:
                                            // 3 sources

                                            while (true) {
                                                if ((Integer)var46_8[31] >= 20) break block67;
                                                var46_8[37] = (int)((char[])var46_8[34])[(Integer)var46_8[31]];
                                                var46_8[37] = (Integer)var46_8[37] ^ 11428;
                                                var45_13 = -1335530345;
                                                break block68;
                                                break;
                                            }
lbl80:
                                            // 1 sources

                                            while (true) {
                                                var46_8[37] = (Integer)var46_8[37] - 31686;
                                                var45_13 = 468700930;
                                                break block68;
                                                break;
                                            }
lbl84:
                                            // 1 sources

                                            while (true) {
                                                var46_8[37] = (Integer)var46_8[37] + 15270;
                                                var45_13 = -246374860;
                                                break block68;
                                                break;
                                            }
lbl88:
                                            // 1 sources

                                            while (true) {
                                                var46_8[37] = (Integer)var46_8[37] ^ 26476;
                                                var45_13 = -285199609;
                                                break block68;
                                                break;
                                            }
lbl92:
                                            // 1 sources

                                            while (true) {
                                                var46_8[37] = (Integer)var46_8[37] ^ 19406;
                                                var46_8[37] = (Integer)var46_8[37] ^ 28207;
                                                var45_13 = 0x2B22B88B;
                                                break block68;
                                                break;
                                            }
lbl97:
                                            // 1 sources

                                            while (true) {
                                                var46_8[37] = (Integer)var46_8[37] + 46383;
                                                var46_8[37] = (Integer)var46_8[37] ^ 45522;
                                                var46_8[37] = (Integer)var46_8[37] ^ 29106;
                                                var45_13 = 203841477;
                                                break block68;
                                                break;
                                            }
lbl103:
                                            // 1 sources

                                            while (true) {
                                                var46_8[37] = (Integer)var46_8[37] - 42549;
                                                var46_8[37] = (Integer)var46_8[37] + 52957;
                                                var45_13 = 409511575;
                                                break block68;
                                                break;
                                            }
lbl108:
                                            // 1 sources

                                            while (true) {
                                                var46_8[37] = (Integer)var46_8[37] ^ 3869;
                                                var46_8[37] = (Integer)var46_8[37] + 56223;
                                                ((char[])var46_8[34])[((Integer)var46_8[31]).intValue()] = (char)((Integer)var46_8[37]).intValue();
                                                var46_8[31] = (Integer)var46_8[31] + 1;
                                                ** GOTO lbl74
                                                break;
                                            }
                                        }
lbl115:
                                        // 2 sources

                                        while (true) {
                                            continue;
                                            break;
                                        }
lbl117:
                                        // 1 sources

                                        while (true) {
                                            v3 = tn.cfr_renamed_1()[3] = new String((char[])var46_8[34]);
lbl119:
                                            // 2 sources

                                            var46_8[18] = Cipher.getInstance((String)v3);
                                            ((Cipher)var46_8[18]).init(2, (Key)((SecretKey)tn.Oo0o00000O00), new IvParameterSpec((byte[])var46_8[1]));
                                            var46_8[30] = ((Cipher)var46_8[18]).doFinal((byte[])var46_8[2]);
                                            v4 = tn.oO00O0OO0ooO[((Integer)var46_8[40]).intValue()] = new String((byte[])var46_8[30], StandardCharsets.UTF_8);
                                            return v4;
                                        }
                                        break;
                                    }
                                }
                                switch (var41_9) {
                                    default: {
                                        ** continue;
                                    }
                                    ** case -810408077:
lbl130:
                                    // 1 sources

                                    ** continue;
                                }
                                var44_12 = -2128501903;
                                switch (var44_12) {
                                    default: {
                                        ** continue;
                                    }
                                    case 1439000547: {
                                        ** continue;
                                    }
                                    ** case -2128501903:
lbl138:
                                    // 1 sources

                                    ** continue;
                                }
                            }
                            switch (var45_13) {
                                default: {
                                    ** continue;
                                }
                                case 468700930: {
                                    ** continue;
                                }
                                case -246374860: {
                                    ** continue;
                                }
                                case -285199609: {
                                    ** continue;
                                }
                                case 0x2B22B88B: {
                                    ** continue;
                                }
                                case 203841477: {
                                    ** continue;
                                }
                                ** case 409511575:
lbl154:
                                // 1 sources

                                ** continue;
                            }
lbl155:
                            // 2 sources

                            while (true) {
                                var46_8 = new Object[45];
                                var46_8[40] = (int)((Integer)var0 /* !! */ [1]);
                                var35_2 = -1520091026;
                                var46_8[35] = (String)var0 /* !! */ [2];
                                var36_3 = -1043107862;
                                var0 /* !! */  = var0 /* !! */ [0];
                                var37_4 = 602135151;
                                block56: while (true) {
                                    switch (var37_4) {
                                        default: {
                                            v5 = tn.oO00O0OO0ooO;
                                            if (tn.oO00O0OO0ooO == null) {
                                                v5 = new Object[1];
                                            }
                                            if ((v4 = v5[(Integer)var46_8[40]]) != null) ** continue;
                                            v6 = var0 /* !! */ ;
                                            if (var0 /* !! */  == null) {
                                                var37_4 = 715842064;
                                                continue block56;
                                            }
                                            break block69;
                                        }
                                        case 715842064: {
                                            v7 = new Object[1];
                                            tn.cfr_renamed_1 = v7;
                                            v6 = v7;
                                            var46_8[5] = new byte[43448 ^ 43432];
                                            ((byte[])var46_8[5])[61551 ^ 61544] = -61465 ^ 61544;
                                            ((byte[])var46_8[5])[56395 ^ 56398] = 56385 ^ 56398;
                                            ((byte[])var46_8[5])[27250 ^ 27257] = -27146 ^ 27257;
                                            ((byte[])var46_8[5])[9326 ^ 9320] = -9287 ^ 9320;
                                            ((byte[])var46_8[5])[24386 ^ 24386] = -24334 ^ 24386;
                                            ((byte[])var46_8[5])[40360 ^ 40356] = 40439 ^ 40356;
                                            ((byte[])var46_8[5])[66999 ^ 67002] = -67021 ^ 67002;
                                            ((byte[])var46_8[5])[9715 ^ 9724] = -9659 ^ 9724;
                                            ((byte[])var46_8[5])[28957 ^ 28947] = -28941 ^ 28947;
                                            ((byte[])var46_8[5])[1889 ^ 1890] = 1799 ^ 1890;
                                            ((byte[])var46_8[5])[42017 ^ 42021] = 41997 ^ 42021;
                                            ((byte[])var46_8[5])[30858 ^ 30850] = -30935 ^ 30850;
                                            ((byte[])var46_8[5])[1873 ^ 1883] = 1904 ^ 1883;
                                            ((byte[])var46_8[5])[20809 ^ 20800] = -20777 ^ 20800;
                                            break block56;
                                        }
                                    }
                                    break;
                                }
                                break block70;
                                break;
                            }
lbl196:
                            // 1 sources

                            while (true) {
                                if (!tn.o0o000o0O0o0) ** GOTO lbl155
                                var48_1 = -65819580;
                                ** continue;
                                break;
                            }
                        }
                        ((byte[])var46_8[5])[19506 ^ 19507] = 19565 ^ 19507;
                        ((byte[])var46_8[5])[6821 ^ 6823] = -6888 ^ 6823;
                        v7[0] = (byte[])var46_8[5];
                    }
                    var46_8[33] = (byte[])v6[0];
                    var38_5 = -1786848174;
                    ** while (tn.Oo0o00000O00 != null)
lbl208:
                    // 1 sources

                    var46_8[19] = new byte[46472 ^ 46504];
                    ((byte[])var46_8[19])[33998 ^ 34002] = 33972 ^ 34002;
                    ((byte[])var46_8[19])[65892 ^ 65916] = -65850 ^ 65916;
                    var39_6 = -1343575797;
                    block58: while (true) {
                        switch (var39_6) {
                            default: {
                                ((byte[])var46_8[19])[29634 ^ 29655] = 29594 ^ 29655;
                                ((byte[])var46_8[19])[68704 ^ 68726] = 68676 ^ 68726;
                                ((byte[])var46_8[19])[5074 ^ 5064] = 5024 ^ 5064;
                                ((byte[])var46_8[19])[24218 ^ 24202] = -24259 ^ 24202;
                                ((byte[])var46_8[19])[42039 ^ 42024] = 42015 ^ 42024;
                                ((byte[])var46_8[19])[33599 ^ 33596] = 33648 ^ 33596;
                                ((byte[])var46_8[19])[63087 ^ 63082] = -63097 ^ 63082;
                                var39_6 = -1594381748;
                                continue block58;
                            }
                            case -1594381748: {
                                ((byte[])var46_8[19])[15400 ^ 15414] = -15373 ^ 15414;
                                ((byte[])var46_8[19])[54555 ^ 54559] = -54588 ^ 54559;
                                ((byte[])var46_8[19])[40789 ^ 40797] = -40796 ^ 40797;
                                var39_6 = 1850617782;
                                continue block58;
                            }
                            case 1850617782: {
                                ((byte[])var46_8[19])[41869 ^ 41869] = 41922 ^ 41869;
                                ((byte[])var46_8[19])[10222 ^ 10239] = 10218 ^ 10239;
                                ((byte[])var46_8[19])[27475 ^ 27470] = 27460 ^ 27470;
                                ((byte[])var46_8[19])[18671 ^ 18678] = -18678 ^ 18678;
                                ((byte[])var46_8[19])[53149 ^ 53130] = -53235 ^ 53130;
                                var39_6 = -1143963011;
                                continue block58;
                            }
                            case -1143963011: {
                                ((byte[])var46_8[19])[8452 ^ 8470] = -8485 ^ 8470;
                                ((byte[])var46_8[19])[66519 ^ 66513] = -66559 ^ 66513;
                                var39_6 = -1188743763;
                                continue block58;
                            }
                            case -1188743763: {
                                ((byte[])var46_8[19])[21832 ^ 21827] = 21806 ^ 21827;
                                var39_6 = 630858157;
                                continue block58;
                            }
                            case 630858157: {
                                ((byte[])var46_8[19])[47514 ^ 47510] = -47519 ^ 47510;
                                ((byte[])var46_8[19])[27077 ^ 27079] = 27056 ^ 27079;
                                ((byte[])var46_8[19])[10125 ^ 10112] = -10221 ^ 10112;
                                var39_6 = -393005779;
                                continue block58;
                            }
                            case -393005779: {
                                ((byte[])var46_8[19])[36375 ^ 36374] = 36354 ^ 36374;
                                ((byte[])var46_8[19])[51540 ^ 51546] = -51568 ^ 51546;
                                ((byte[])var46_8[19])[62987 ^ 62978] = -63064 ^ 62978;
                                var39_6 = 1498308352;
                                continue block58;
                            }
                            case 1498308352: 
                        }
                        break;
                    }
                    ((byte[])var46_8[19])[29117 ^ 29094] = 29109 ^ 29094;
                    ((byte[])var46_8[19])[15664 ^ 15679] = -15654 ^ 15679;
                    ((byte[])var46_8[19])[23485 ^ 23465] = -23543 ^ 23465;
                    ((byte[])var46_8[19])[34956 ^ 34975] = 35047 ^ 34975;
                    ((byte[])var46_8[19])[39078 ^ 39073] = 39061 ^ 39073;
                    ((byte[])var46_8[19])[41265 ^ 41275] = 41335 ^ 41275;
                    var46_8[28] = new byte[((byte[])var46_8[33]).length + ((byte[])var46_8[19]).length];
                    System.arraycopy((byte[])var46_8[33], 0, (byte[])var46_8[28], 0, ((byte[])var46_8[33]).length);
                    System.arraycopy((byte[])var46_8[19], 0, (byte[])var46_8[28], ((byte[])var46_8[33]).length, ((byte[])var46_8[19]).length);
                    v8 = tn.cfr_renamed_1()[1];
                    if (v8 != null) break block74;
                    var46_8[4] = "\ub710\uc9a6\ub76d\uc9a4\uc99a\ub7d6\ub779\ub647\uc994\ub648\uc9a8\uc98b\uc9bf\ub645\ub775\uc9a8\uc99f\ub7cf".toCharArray();
                    var46_8[44] = 0;
                    while ((Integer)var46_8[44] < 18) {
                        var46_8[14] = (int)((char[])var46_8[4])[(Integer)var46_8[44]];
                        var40_7 = 1264773210;
                        block60: while (true) {
                            switch (var40_7) {
                                default: {
                                    var46_8[14] = (Integer)var46_8[14] ^ 30288;
                                    var40_7 = -271109720;
                                    continue block60;
                                }
                                case -271109720: {
                                    var46_8[14] = (Integer)var46_8[14] + 17749;
                                    var46_8[14] = (Integer)var46_8[14] - 41958;
                                    var40_7 = 49617943;
                                    continue block60;
                                }
                                case 49617943: {
                                    var46_8[14] = (Integer)var46_8[14] + 50503;
                                    var46_8[14] = (Integer)var46_8[14] - 1112;
                                    var46_8[14] = (Integer)var46_8[14] ^ 8936;
                                    var46_8[14] = (Integer)var46_8[14] - 40089;
                                    var40_7 = -655886600;
                                    continue block60;
                                }
                                case -655886600: {
                                    var46_8[14] = (Integer)var46_8[14] ^ 41562;
                                    var46_8[14] = (Integer)var46_8[14] + 19372;
                                    var46_8[14] = (Integer)var46_8[14] + 51390;
                                    var46_8[14] = (Integer)var46_8[14] ^ 14718;
                                    var40_7 = 1230752001;
                                    continue block60;
                                }
                                case 1230752001: {
                                    var46_8[14] = (Integer)var46_8[14] ^ 58335;
                                    ((char[])var46_8[4])[((Integer)var46_8[44]).intValue()] = (char)((Integer)var46_8[14]).intValue();
                                    var46_8[44] = (Integer)var46_8[44] + 1;
                                    var40_7 = -2058517322;
                                    continue block60;
                                }
                                case -2058517322: 
                            }
                            break;
                        }
                    }
                    break block75;
                }
                ** while (1664815063 >= (tn.ooo00oo0Ooo0 = (var48_1 = 1087705845)))
lbl312:
                // 1 sources

                throw null;
            }
            v8 = tn.cfr_renamed_1()[1] = new String((char[])var46_8[4]);
        }
        var46_8[16] = SecretKeyFactory.getInstance((String)v8);
        var46_8[25] = new byte[16];
        ((byte[])var46_8[25])[13] = -2;
        ((byte[])var46_8[25])[11] = 69;
        ((byte[])var46_8[25])[10] = -50;
        ((byte[])var46_8[25])[2] = -18;
        ((byte[])var46_8[25])[6] = 1;
        ((byte[])var46_8[25])[5] = -71;
        ** while (true)
    }

    /*
     * Unable to fully structure code
     */
    public strictfp float c(float var1_1, float var2_2, float var3_3) {
        block10: {
            if (tn.o0o000o0O0o0) {
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
            var24_4[11] = bt.aJ(Math.toDegrees(Math.atan2(tn.aEi.bgb.uf, tn.aEi.bgb.ud)) - Double.longBitsToDouble(440541444290559937L ^ 5065316049134993345L));
            var24_4[7] = Float.valueOf(tn.aEi.bgb.pl);
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
                if (tn.o0o000o0O0o0) {
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
                    tn.o0Oo000O0oO[(int)(((Long)var92_6[9]).longValue() >>> ((Integer)var92_6[23]).intValue())] = new String((char[])var92_6[45]);
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
                    if (!tn.class.desiredAssertionStatus()) {
                        var92_6[24] = 86;
                        var92_6[24] = (Integer)var92_6[24] ^ -124;
                        var92_6[24] = (Integer)var92_6[24] - -47;
                        v1 = (Integer)var92_6[24];
                    } else {
                        var82_1 = -229;
                        var82_1 -= -121;
                        v1 = var82_1 ^= -108;
                    }
                    tn.ahf = v1;
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
                        tn.Oo0o00000O00();
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
            tn.o0Oo000O0oO = new Object[((Integer)var92_6[71]).intValue()];
            v8 = (Long)var92_6[9];
            var92_6[72] = 59;
            var92_6[72] = (Integer)var92_6[72] + 67;
            var92_6[72] = (Integer)var92_6[72] + -94;
            var92_6[9] = v8 ^ (0L ^ v8) & -1L << (Integer)var92_6[72];
            v9 = new Object[3];
            v9[0] = tn.cfr_renamed_1;
            v9[1] = 0;
            v10 = tn.cfr_renamed_1()[0];
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
                v10 = tn.cfr_renamed_1()[0] = new String((char[])var92_6[51]);
            }
            v9[2] = (String)v10;
            var92_6[61] = ((String)tn.o0Oo000O0oO(v9)).toCharArray();
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

    /*
     * Enabled aggressive block sorting
     */
    public strictfp aka b(c c2) {
        aka aka2;
        if (o0o000o0O0o0) {
            int n2 = 33;
        }
        int n3 = -1676571003;
        aka2(c2.gW, c2.gX + (double)c2.getEyeHeight(), c2.gY);
        return aka2;
    }

    /*
     * Enabled aggressive block sorting
     */
    public tn() {
        akf akf2;
        int n2 = 19175914;
        ooo00oo0Ooo0 = n2;
        if (33515418 < ooo00oo0Ooo0) {
            n2 = 75;
        }
        int n3 = O0OoOO0OOOOO[0];
        n3 ^= O0OoOO0OOOOO[1];
        n3 ^= O0OoOO0OOOOO[2];
        int n4 = O0OoOO0OOOOO[3];
        n4 ^= O0OoOO0OOOOO[4];
        akd[] akdArray = new akd[n4 ^= O0OoOO0OOOOO[5]];
        int n5 = O0OoOO0OOOOO[6];
        n5 ^= O0OoOO0OOOOO[7];
        int n6 = O0OoOO0OOOOO[9];
        n6 += O0OoOO0OOOOO[10];
        akdArray[n5 -= tn.O0OoOO0OOOOO[8]] = new akp((String)o0Oo000O0oO[n6 -= O0OoOO0OOOOO[11]]);
        int n7 = O0OoOO0OOOOO[12];
        n7 += O0OoOO0OOOOO[13];
        akd[] akdArray2 = new akd[n7 -= O0OoOO0OOOOO[14]];
        int n8 = O0OoOO0OOOOO[15];
        n8 += O0OoOO0OOOOO[16];
        int n9 = O0OoOO0OOOOO[18];
        n9 += O0OoOO0OOOOO[19];
        akdArray2[n8 ^= tn.O0OoOO0OOOOO[17]] = new akp((String)o0Oo000O0oO[n9 -= O0OoOO0OOOOO[20]]);
        int n10 = O0OoOO0OOOOO[21];
        n10 -= O0OoOO0OOOOO[22];
        akd[] akdArray3 = new akd[n10 ^= O0OoOO0OOOOO[23]];
        int n11 = O0OoOO0OOOOO[24];
        n11 += O0OoOO0OOOOO[25];
        int n12 = O0OoOO0OOOOO[27];
        n12 -= O0OoOO0OOOOO[28];
        akdArray3[n11 += tn.O0OoOO0OOOOO[26]] = new akp((String)o0Oo000O0oO[n12 ^= O0OoOO0OOOOO[29]]);
        int n13 = O0OoOO0OOOOO[30];
        n13 ^= O0OoOO0OOOOO[31];
        akd[] akdArray4 = new akd[n13 += O0OoOO0OOOOO[32]];
        int n14 = O0OoOO0OOOOO[33];
        n14 ^= O0OoOO0OOOOO[34];
        int n15 = O0OoOO0OOOOO[36];
        n15 ^= O0OoOO0OOOOO[37];
        akdArray4[n14 -= tn.O0OoOO0OOOOO[35]] = new akp((String)o0Oo000O0oO[n15 += O0OoOO0OOOOO[38]]);
        int n16 = O0OoOO0OOOOO[39];
        n16 += O0OoOO0OOOOO[40];
        akd[] akdArray5 = new akd[n16 ^= O0OoOO0OOOOO[41]];
        int n17 = O0OoOO0OOOOO[42];
        n17 ^= O0OoOO0OOOOO[43];
        int n18 = O0OoOO0OOOOO[45];
        n18 += O0OoOO0OOOOO[46];
        akdArray5[n17 += tn.O0OoOO0OOOOO[44]] = new akp((String)o0Oo000O0oO[n18 -= O0OoOO0OOOOO[47]]);
        int n19 = O0OoOO0OOOOO[48];
        n19 += O0OoOO0OOOOO[49];
        akd[] akdArray6 = new akd[n19 -= O0OoOO0OOOOO[50]];
        int n20 = O0OoOO0OOOOO[51];
        n20 += O0OoOO0OOOOO[52];
        int n21 = O0OoOO0OOOOO[54];
        n21 ^= O0OoOO0OOOOO[55];
        akdArray6[n20 += tn.O0OoOO0OOOOO[53]] = new akp((String)o0Oo000O0oO[n21 -= O0OoOO0OOOOO[56]]);
        int n22 = O0OoOO0OOOOO[57];
        n22 += O0OoOO0OOOOO[58];
        this.afJ = new akm((String)o0Oo000O0oO[n3], this).a(akdArray).a(akdArray2).a(akdArray3).a(akdArray4).a(akdArray5).a(akdArray6).co((String)o0Oo000O0oO[n22 += O0OoOO0OOOOO[59]]);
        int n23 = O0OoOO0OOOOO[60];
        n23 += O0OoOO0OOOOO[61];
        n23 -= O0OoOO0OOOOO[62];
        int n24 = O0OoOO0OOOOO[63];
        n24 -= O0OoOO0OOOOO[64];
        akd[] akdArray7 = new akd[n24 -= O0OoOO0OOOOO[65]];
        int n25 = O0OoOO0OOOOO[66];
        n25 += O0OoOO0OOOOO[67];
        int n26 = O0OoOO0OOOOO[69];
        n26 ^= O0OoOO0OOOOO[70];
        akdArray7[n25 += tn.O0OoOO0OOOOO[68]] = new akp((String)o0Oo000O0oO[n26 += O0OoOO0OOOOO[71]]);
        int n27 = O0OoOO0OOOOO[72];
        n27 ^= O0OoOO0OOOOO[73];
        akd[] akdArray8 = new akd[n27 += O0OoOO0OOOOO[74]];
        int n28 = O0OoOO0OOOOO[75];
        n28 -= O0OoOO0OOOOO[76];
        int n29 = O0OoOO0OOOOO[78];
        n29 -= O0OoOO0OOOOO[79];
        akdArray8[n28 += tn.O0OoOO0OOOOO[77]] = new akp((String)o0Oo000O0oO[n29 += O0OoOO0OOOOO[80]]);
        int n30 = O0OoOO0OOOOO[81];
        n30 += O0OoOO0OOOOO[82];
        this.afK = new akm((String)o0Oo000O0oO[n23], this).a(akdArray7).a(akdArray8).co((String)o0Oo000O0oO[n30 ^= O0OoOO0OOOOO[83]]);
        int n31 = O0OoOO0OOOOO[84];
        n31 += O0OoOO0OOOOO[85];
        n31 ^= O0OoOO0OOOOO[86];
        int n32 = O0OoOO0OOOOO[87];
        n32 -= O0OoOO0OOOOO[88];
        akd[] akdArray9 = new akd[n32 += O0OoOO0OOOOO[89]];
        int n33 = O0OoOO0OOOOO[90];
        n33 ^= O0OoOO0OOOOO[91];
        int n34 = O0OoOO0OOOOO[93];
        n34 += O0OoOO0OOOOO[94];
        akdArray9[n33 -= tn.O0OoOO0OOOOO[92]] = new akp((String)o0Oo000O0oO[n34 += O0OoOO0OOOOO[95]]);
        int n35 = O0OoOO0OOOOO[96];
        n35 += O0OoOO0OOOOO[97];
        akd[] akdArray10 = new akd[n35 += O0OoOO0OOOOO[98]];
        int n36 = O0OoOO0OOOOO[99];
        n36 += O0OoOO0OOOOO[100];
        int n37 = O0OoOO0OOOOO[102];
        n37 ^= O0OoOO0OOOOO[103];
        akdArray10[n36 -= tn.O0OoOO0OOOOO[101]] = new akp((String)o0Oo000O0oO[n37 -= O0OoOO0OOOOO[104]]);
        int n38 = O0OoOO0OOOOO[105];
        n38 += O0OoOO0OOOOO[106];
        akd[] akdArray11 = new akd[n38 -= O0OoOO0OOOOO[107]];
        int n39 = O0OoOO0OOOOO[108];
        n39 += O0OoOO0OOOOO[109];
        int n40 = O0OoOO0OOOOO[111];
        n40 -= O0OoOO0OOOOO[112];
        akdArray11[n39 -= tn.O0OoOO0OOOOO[110]] = new akp((String)o0Oo000O0oO[n40 ^= O0OoOO0OOOOO[113]]);
        int n41 = O0OoOO0OOOOO[114];
        n41 -= O0OoOO0OOOOO[115];
        this.afL = new akm((String)o0Oo000O0oO[n31], this).a(akdArray9).a(akdArray10).a(akdArray11).co((String)o0Oo000O0oO[n41 ^= O0OoOO0OOOOO[116]]);
        int n42 = O0OoOO0OOOOO[117];
        n42 += O0OoOO0OOOOO[118];
        n42 += O0OoOO0OOOOO[119];
        int n43 = O0OoOO0OOOOO[120];
        n43 ^= O0OoOO0OOOOO[121];
        akd[] akdArray12 = new akd[n43 ^= O0OoOO0OOOOO[122]];
        int n44 = O0OoOO0OOOOO[123];
        n44 += O0OoOO0OOOOO[124];
        int n45 = O0OoOO0OOOOO[126];
        n45 += O0OoOO0OOOOO[127];
        akdArray12[n44 ^= tn.O0OoOO0OOOOO[125]] = new akp((String)o0Oo000O0oO[n45 ^= O0OoOO0OOOOO[128]]);
        int n46 = O0OoOO0OOOOO[129];
        n46 += O0OoOO0OOOOO[130];
        akd[] akdArray13 = new akd[n46 -= O0OoOO0OOOOO[131]];
        int n47 = O0OoOO0OOOOO[132];
        n47 -= O0OoOO0OOOOO[133];
        int n48 = O0OoOO0OOOOO[135];
        n48 -= O0OoOO0OOOOO[136];
        akdArray13[n47 -= tn.O0OoOO0OOOOO[134]] = new vf((String)o0Oo000O0oO[n48 ^= O0OoOO0OOOOO[137]], this);
        int n49 = O0OoOO0OOOOO[138];
        n49 ^= O0OoOO0OOOOO[139];
        akd[] akdArray14 = new akd[n49 -= O0OoOO0OOOOO[140]];
        int n50 = O0OoOO0OOOOO[141];
        n50 += O0OoOO0OOOOO[142];
        int n51 = O0OoOO0OOOOO[144];
        n51 += O0OoOO0OOOOO[145];
        akdArray14[n50 ^= tn.O0OoOO0OOOOO[143]] = new vg((String)o0Oo000O0oO[n51 -= O0OoOO0OOOOO[146]], this);
        int n52 = O0OoOO0OOOOO[147];
        n52 -= O0OoOO0OOOOO[148];
        akd[] akdArray15 = new akd[n52 += O0OoOO0OOOOO[149]];
        int n53 = O0OoOO0OOOOO[150];
        n53 += O0OoOO0OOOOO[151];
        int n54 = O0OoOO0OOOOO[153];
        n54 += O0OoOO0OOOOO[154];
        akdArray15[n53 -= tn.O0OoOO0OOOOO[152]] = new ve((String)o0Oo000O0oO[n54 += O0OoOO0OOOOO[155]], this);
        int n55 = O0OoOO0OOOOO[156];
        n55 -= O0OoOO0OOOOO[157];
        akd[] akdArray16 = new akd[n55 ^= O0OoOO0OOOOO[158]];
        int n56 = O0OoOO0OOOOO[159];
        n56 -= O0OoOO0OOOOO[160];
        int n57 = O0OoOO0OOOOO[162];
        n57 -= O0OoOO0OOOOO[163];
        akdArray16[n56 += tn.O0OoOO0OOOOO[161]] = new vj((String)o0Oo000O0oO[n57 += O0OoOO0OOOOO[164]], this);
        int n58 = O0OoOO0OOOOO[165];
        n58 ^= O0OoOO0OOOOO[166];
        akd[] akdArray17 = new akd[n58 += O0OoOO0OOOOO[167]];
        int n59 = O0OoOO0OOOOO[168];
        n59 -= O0OoOO0OOOOO[169];
        int n60 = O0OoOO0OOOOO[171];
        n60 += O0OoOO0OOOOO[172];
        akdArray17[n59 ^= tn.O0OoOO0OOOOO[170]] = new vi((String)o0Oo000O0oO[n60 += O0OoOO0OOOOO[173]], this);
        int n61 = O0OoOO0OOOOO[174];
        n61 ^= O0OoOO0OOOOO[175];
        akd[] akdArray18 = new akd[n61 += O0OoOO0OOOOO[176]];
        int n62 = O0OoOO0OOOOO[177];
        n62 -= O0OoOO0OOOOO[178];
        int n63 = O0OoOO0OOOOO[180];
        n63 ^= O0OoOO0OOOOO[181];
        akdArray18[n62 -= tn.O0OoOO0OOOOO[179]] = new vh((String)o0Oo000O0oO[n63 += O0OoOO0OOOOO[182]], this);
        int n64 = O0OoOO0OOOOO[183];
        n64 += O0OoOO0OOOOO[184];
        akd[] akdArray19 = new akd[n64 += O0OoOO0OOOOO[185]];
        int n65 = O0OoOO0OOOOO[186];
        n65 ^= O0OoOO0OOOOO[187];
        int n66 = O0OoOO0OOOOO[189];
        n66 += O0OoOO0OOOOO[190];
        int n67 = O0OoOO0OOOOO[192];
        n67 ^= O0OoOO0OOOOO[193];
        akdArray19[n65 ^= tn.O0OoOO0OOOOO[188]] = new vm((String)o0Oo000O0oO[n66 ^= O0OoOO0OOOOO[191]] + (String)o0Oo000O0oO[n67 += O0OoOO0OOOOO[194]], this);
        int n68 = O0OoOO0OOOOO[195];
        n68 -= O0OoOO0OOOOO[196];
        akd[] akdArray20 = new akd[n68 ^= O0OoOO0OOOOO[197]];
        int n69 = O0OoOO0OOOOO[198];
        n69 += O0OoOO0OOOOO[199];
        int n70 = O0OoOO0OOOOO[201];
        n70 -= O0OoOO0OOOOO[202];
        akdArray20[n69 += tn.O0OoOO0OOOOO[200]] = new vk((String)o0Oo000O0oO[n70 += O0OoOO0OOOOO[203]], this);
        int n71 = O0OoOO0OOOOO[204];
        n71 -= O0OoOO0OOOOO[205];
        akd[] akdArray21 = new akd[n71 += O0OoOO0OOOOO[206]];
        int n72 = O0OoOO0OOOOO[207];
        n72 -= O0OoOO0OOOOO[208];
        int n73 = O0OoOO0OOOOO[210];
        n73 ^= O0OoOO0OOOOO[211];
        akdArray21[n72 += tn.O0OoOO0OOOOO[209]] = new vn((String)o0Oo000O0oO[n73 -= O0OoOO0OOOOO[212]], this);
        int n74 = O0OoOO0OOOOO[213];
        n74 += O0OoOO0OOOOO[214];
        this.afM = new akm((String)o0Oo000O0oO[n42], this).a(akdArray12).a(akdArray13).a(akdArray14).a(akdArray15).a(akdArray16).a(akdArray17).a(akdArray18).a(akdArray19).a(akdArray20).a(akdArray21).co((String)o0Oo000O0oO[n74 += O0OoOO0OOOOO[215]]);
        int n75 = O0OoOO0OOOOO[216];
        n75 -= O0OoOO0OOOOO[217];
        n75 -= O0OoOO0OOOOO[218];
        int n76 = O0OoOO0OOOOO[219];
        n76 -= O0OoOO0OOOOO[220];
        akd[] akdArray22 = new akd[n76 ^= O0OoOO0OOOOO[221]];
        int n77 = O0OoOO0OOOOO[222];
        n77 -= O0OoOO0OOOOO[223];
        int n78 = O0OoOO0OOOOO[225];
        n78 ^= O0OoOO0OOOOO[226];
        akdArray22[n77 -= tn.O0OoOO0OOOOO[224]] = new akp((String)o0Oo000O0oO[n78 += O0OoOO0OOOOO[227]]);
        int n79 = O0OoOO0OOOOO[228];
        n79 -= O0OoOO0OOOOO[229];
        akd[] akdArray23 = new akd[n79 -= O0OoOO0OOOOO[230]];
        int n80 = O0OoOO0OOOOO[231];
        n80 ^= O0OoOO0OOOOO[232];
        int n81 = O0OoOO0OOOOO[234];
        n81 -= O0OoOO0OOOOO[235];
        akdArray23[n80 ^= tn.O0OoOO0OOOOO[233]] = new vy((String)o0Oo000O0oO[n81 += O0OoOO0OOOOO[236]], this);
        int n82 = O0OoOO0OOOOO[237];
        n82 += O0OoOO0OOOOO[238];
        akd[] akdArray24 = new akd[n82 += O0OoOO0OOOOO[239]];
        int n83 = O0OoOO0OOOOO[240];
        n83 += O0OoOO0OOOOO[241];
        int n84 = O0OoOO0OOOOO[243];
        n84 ^= O0OoOO0OOOOO[244];
        akdArray24[n83 ^= tn.O0OoOO0OOOOO[242]] = new vx((String)o0Oo000O0oO[n84 ^= O0OoOO0OOOOO[245]], this);
        int n85 = O0OoOO0OOOOO[246];
        n85 -= O0OoOO0OOOOO[247];
        akd[] akdArray25 = new akd[n85 ^= O0OoOO0OOOOO[248]];
        int n86 = O0OoOO0OOOOO[249];
        n86 -= O0OoOO0OOOOO[250];
        int n87 = O0OoOO0OOOOO[252];
        n87 ^= O0OoOO0OOOOO[253];
        akdArray25[n86 -= tn.O0OoOO0OOOOO[251]] = new vv((String)o0Oo000O0oO[n87 -= O0OoOO0OOOOO[254]], this);
        int n88 = O0OoOO0OOOOO[255];
        n88 -= O0OoOO0OOOOO[256];
        akd[] akdArray26 = new akd[n88 -= O0OoOO0OOOOO[257]];
        int n89 = O0OoOO0OOOOO[258];
        n89 ^= O0OoOO0OOOOO[259];
        int n90 = O0OoOO0OOOOO[261];
        n90 += O0OoOO0OOOOO[262];
        akdArray26[n89 += tn.O0OoOO0OOOOO[260]] = new vp((String)o0Oo000O0oO[n90 ^= O0OoOO0OOOOO[263]], this);
        int n91 = O0OoOO0OOOOO[264];
        n91 -= O0OoOO0OOOOO[265];
        akd[] akdArray27 = new akd[n91 += O0OoOO0OOOOO[266]];
        int n92 = O0OoOO0OOOOO[267];
        n92 += O0OoOO0OOOOO[268];
        int n93 = O0OoOO0OOOOO[270];
        n93 ^= O0OoOO0OOOOO[271];
        akdArray27[n92 ^= tn.O0OoOO0OOOOO[269]] = new wa((String)o0Oo000O0oO[n93 -= O0OoOO0OOOOO[272]], this);
        int n94 = O0OoOO0OOOOO[273];
        n94 ^= O0OoOO0OOOOO[274];
        akd[] akdArray28 = new akd[n94 -= O0OoOO0OOOOO[275]];
        int n95 = O0OoOO0OOOOO[276];
        n95 -= O0OoOO0OOOOO[277];
        int n96 = O0OoOO0OOOOO[279];
        n96 ^= O0OoOO0OOOOO[280];
        akdArray28[n95 += tn.O0OoOO0OOOOO[278]] = new vs((String)o0Oo000O0oO[n96 ^= O0OoOO0OOOOO[281]], this);
        int n97 = O0OoOO0OOOOO[282];
        n97 -= O0OoOO0OOOOO[283];
        akd[] akdArray29 = new akd[n97 ^= O0OoOO0OOOOO[284]];
        int n98 = O0OoOO0OOOOO[285];
        n98 += O0OoOO0OOOOO[286];
        int n99 = O0OoOO0OOOOO[288];
        n99 -= O0OoOO0OOOOO[289];
        akdArray29[n98 += tn.O0OoOO0OOOOO[287]] = new vu((String)o0Oo000O0oO[n99 += O0OoOO0OOOOO[290]], this);
        int n100 = O0OoOO0OOOOO[291];
        n100 += O0OoOO0OOOOO[292];
        akd[] akdArray30 = new akd[n100 += O0OoOO0OOOOO[293]];
        int n101 = O0OoOO0OOOOO[294];
        n101 ^= O0OoOO0OOOOO[295];
        int n102 = O0OoOO0OOOOO[297];
        n102 += O0OoOO0OOOOO[298];
        akdArray30[n101 ^= tn.O0OoOO0OOOOO[296]] = new vt((String)o0Oo000O0oO[n102 ^= O0OoOO0OOOOO[299]], this);
        int n103 = O0OoOO0OOOOO[300];
        n103 -= O0OoOO0OOOOO[301];
        akd[] akdArray31 = new akd[n103 -= O0OoOO0OOOOO[302]];
        int n104 = O0OoOO0OOOOO[303];
        n104 += O0OoOO0OOOOO[304];
        int n105 = O0OoOO0OOOOO[306];
        n105 += O0OoOO0OOOOO[307];
        akdArray31[n104 ^= tn.O0OoOO0OOOOO[305]] = new vr((String)o0Oo000O0oO[n105 ^= O0OoOO0OOOOO[308]], this);
        int n106 = O0OoOO0OOOOO[309];
        n106 ^= O0OoOO0OOOOO[310];
        akd[] akdArray32 = new akd[n106 += O0OoOO0OOOOO[311]];
        int n107 = O0OoOO0OOOOO[312];
        n107 += O0OoOO0OOOOO[313];
        int n108 = O0OoOO0OOOOO[315];
        n108 += O0OoOO0OOOOO[316];
        akdArray32[n107 -= tn.O0OoOO0OOOOO[314]] = new vw((String)o0Oo000O0oO[n108 += O0OoOO0OOOOO[317]], this);
        int n109 = O0OoOO0OOOOO[318];
        n109 ^= O0OoOO0OOOOO[319];
        akd[] akdArray33 = new akd[n109 -= O0OoOO0OOOOO[320]];
        int n110 = O0OoOO0OOOOO[321];
        n110 += O0OoOO0OOOOO[322];
        int n111 = O0OoOO0OOOOO[324];
        n111 -= O0OoOO0OOOOO[325];
        int n112 = O0OoOO0OOOOO[327];
        n112 += O0OoOO0OOOOO[328];
        akdArray33[n110 ^= tn.O0OoOO0OOOOO[323]] = new vz((String)o0Oo000O0oO[n111 += O0OoOO0OOOOO[326]] + (String)o0Oo000O0oO[n112 += O0OoOO0OOOOO[329]], this);
        int n113 = O0OoOO0OOOOO[330];
        n113 -= O0OoOO0OOOOO[331];
        this.afN = new akm((String)o0Oo000O0oO[n75], this).a(akdArray22).a(akdArray23).a(akdArray24).a(akdArray25).a(akdArray26).a(akdArray27).a(akdArray28).a(akdArray29).a(akdArray30).a(akdArray31).a(akdArray32).a(akdArray33).co((String)o0Oo000O0oO[n113 += O0OoOO0OOOOO[332]]);
        int n114 = O0OoOO0OOOOO[333];
        n114 ^= O0OoOO0OOOOO[334];
        n114 += O0OoOO0OOOOO[335];
        int n115 = O0OoOO0OOOOO[336];
        n115 -= O0OoOO0OOOOO[337];
        akd[] akdArray34 = new akd[n115 += O0OoOO0OOOOO[338]];
        int n116 = O0OoOO0OOOOO[339];
        n116 += O0OoOO0OOOOO[340];
        int n117 = O0OoOO0OOOOO[342];
        n117 ^= O0OoOO0OOOOO[343];
        akdArray34[n116 ^= tn.O0OoOO0OOOOO[341]] = new akp((String)o0Oo000O0oO[n117 += O0OoOO0OOOOO[344]]);
        int n118 = O0OoOO0OOOOO[345];
        n118 ^= O0OoOO0OOOOO[346];
        akd[] akdArray35 = new akd[n118 -= O0OoOO0OOOOO[347]];
        int n119 = O0OoOO0OOOOO[348];
        n119 ^= O0OoOO0OOOOO[349];
        int n120 = O0OoOO0OOOOO[351];
        n120 += O0OoOO0OOOOO[352];
        akdArray35[n119 ^= tn.O0OoOO0OOOOO[350]] = new akp((String)o0Oo000O0oO[n120 ^= O0OoOO0OOOOO[353]]);
        int n121 = O0OoOO0OOOOO[354];
        n121 ^= O0OoOO0OOOOO[355];
        akd[] akdArray36 = new akd[n121 += O0OoOO0OOOOO[356]];
        int n122 = O0OoOO0OOOOO[357];
        n122 -= O0OoOO0OOOOO[358];
        int n123 = O0OoOO0OOOOO[360];
        n123 ^= O0OoOO0OOOOO[361];
        akdArray36[n122 += tn.O0OoOO0OOOOO[359]] = new akp((String)o0Oo000O0oO[n123 -= O0OoOO0OOOOO[362]]);
        int n124 = O0OoOO0OOOOO[363];
        n124 += O0OoOO0OOOOO[364];
        this.afO = new akm((String)o0Oo000O0oO[n114], this).a(akdArray34).a(akdArray35).a(akdArray36).co((String)o0Oo000O0oO[n124 += O0OoOO0OOOOO[365]]);
        int n125 = O0OoOO0OOOOO[366];
        n125 -= O0OoOO0OOOOO[367];
        n125 ^= O0OoOO0OOOOO[368];
        int n126 = O0OoOO0OOOOO[369];
        n126 -= O0OoOO0OOOOO[370];
        n126 -= O0OoOO0OOOOO[371];
        int n127 = O0OoOO0OOOOO[372];
        n127 += O0OoOO0OOOOO[373];
        akd[] akdArray37 = new akd[n127 += O0OoOO0OOOOO[374]];
        int n128 = O0OoOO0OOOOO[375];
        n128 ^= O0OoOO0OOOOO[376];
        int n129 = O0OoOO0OOOOO[378];
        n129 -= O0OoOO0OOOOO[379];
        akdArray37[n128 -= tn.O0OoOO0OOOOO[377]] = new akp((String)o0Oo000O0oO[n129 += O0OoOO0OOOOO[380]]);
        int n130 = O0OoOO0OOOOO[381];
        n130 ^= O0OoOO0OOOOO[382];
        akd[] akdArray38 = new akd[n130 -= O0OoOO0OOOOO[383]];
        int n131 = O0OoOO0OOOOO[384];
        n131 -= O0OoOO0OOOOO[385];
        int n132 = O0OoOO0OOOOO[387];
        n132 -= O0OoOO0OOOOO[388];
        akdArray38[n131 += tn.O0OoOO0OOOOO[386]] = new va((String)o0Oo000O0oO[n132 += O0OoOO0OOOOO[389]], this);
        int n133 = O0OoOO0OOOOO[390];
        n133 ^= O0OoOO0OOOOO[391];
        akd[] akdArray39 = new akd[n133 += O0OoOO0OOOOO[392]];
        int n134 = O0OoOO0OOOOO[393];
        n134 += O0OoOO0OOOOO[394];
        int n135 = O0OoOO0OOOOO[396];
        n135 -= O0OoOO0OOOOO[397];
        akdArray39[n134 += tn.O0OoOO0OOOOO[395]] = new vc((String)o0Oo000O0oO[n135 ^= O0OoOO0OOOOO[398]], this);
        int n136 = O0OoOO0OOOOO[399];
        n136 ^= 0x68;
        akd[] akdArray40 = new akd[n136 ^= 8];
        int n137 = 73;
        n137 -= 52;
        int n138 = 260;
        n138 += -98;
        akdArray40[n137 += -21] = new vb((String)o0Oo000O0oO[n138 ^= 0x2B], this);
        int n139 = 116;
        n139 -= -124;
        this.afP = new akm((String)o0Oo000O0oO[n125] + (String)o0Oo000O0oO[n126], this).a(akdArray37).a(akdArray38).a(akdArray39).a(akdArray40).co((String)o0Oo000O0oO[n139 ^= 0x6D]);
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
        this.afQ = new akg((String)o0Oo000O0oO[n140], this, (Number)n141, (Number)n142, (Number)(n143 += 30), (Number)(n144 += 30), (Number)(n145 ^= 0x2E));
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
        this.afR = new akg((String)o0Oo000O0oO[n146], this, (Number)n147, (Number)n148, (Number)(n149 ^= 0x5B), (Number)(n150 -= -18), (Number)(n151 += -11));
        int n152 = 35;
        n152 += -25;
        int n153 = -183;
        n153 -= -66;
        int n154 = -79;
        n154 -= -100;
        this.afS = new akn((String)o0Oo000O0oO[n152 -= -123], this, (Number)(n153 += 118), (Number)Double.longBitsToDouble(4591870180066957722L), (Number)(n154 -= 11), (Number)Double.longBitsToDouble(4591870180066957722L));
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
        this.afT = new akn((String)o0Oo000O0oO[n155], this, (Number)n156, (Number)(n157 += 53), (Number)(n158 += 79), (Number)(n159 ^= 0xFFFFFFFE));
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
        this.afU = new akf((String)o0Oo000O0oO[n160] + (String)o0Oo000O0oO[n161] + (String)o0Oo000O0oO[n162 ^= 0x34], (gi)this, (Boolean)bl2);
        int n165 = -51;
        n165 += -122;
        int n167 = -3;
        n167 = n167 ^ 5;
        boolean bl3 = n167 - -9;
        this.afV = new akf((String)o0Oo000O0oO[n165 ^= 0xFFFFFFF3], (gi)this, (Boolean)bl3);
        int n168 = -196;
        n168 -= 6;
        n168 ^= 0xFFFFFF93;
        int n169 = -187;
        n169 ^= 0xFFFFFF9D;
        int n171 = -72;
        n171 = n171 + 97;
        boolean bl4 = n171 + -25;
        this.afW = new akf((String)o0Oo000O0oO[n168] + (String)o0Oo000O0oO[n169 ^= 0x52], (gi)this, (Boolean)bl4);
        int n172 = 4;
        n172 += -36;
        int n174 = -147;
        n174 = n174 + 86;
        boolean bl5 = n174 - -62;
        this.afX = new akf((String)o0Oo000O0oO[n172 += 123], (gi)this, (Boolean)bl5);
        int n175 = -87;
        n175 ^= 0x48;
        n175 -= -93;
        int n176 = 61;
        n176 += 37;
        int n178 = 139;
        n178 = n178 - 44;
        boolean bl6 = n178 + -95;
        this.afY = new akf((String)o0Oo000O0oO[n175] + (String)o0Oo000O0oO[n176 ^= 0xC], (gi)this, (Boolean)bl6);
        int n179 = 65;
        n179 += -86;
        int n181 = -4;
        n181 = n181 + -116;
        boolean bl7 = n181 ^ 0xFFFFFF88;
        this.afZ = new akf((String)o0Oo000O0oO[n179 += 31], (gi)this, (Boolean)bl7);
        int n182 = -241;
        n182 -= -113;
        int n183 = -27;
        n183 -= 16;
        int n184 = 69;
        boolean bl8 = n184 ^ 0x44;
        this.aga = new akf((String)o0Oo000O0oO[n182 ^= 0xFFFFFFE5] + (String)o0Oo000O0oO[n183 += 98], (gi)this, (Boolean)bl8, () -> {
            int n2;
            int n3 = 63540975;
            ooo00oo0Ooo0 = -121772600;
            if (826546338 < ooo00oo0Ooo0) {
                n3 = 1216211337;
            }
            Object[] objectArray = new Object[6];
            int n4 = (Boolean)this.afZ.wp() == false ? 368164142 : 588418017;
            switch (n4) {
                default: {
                    objectArray[5] = -20;
                    objectArray[5] = (Integer)objectArray[5] ^ 0x10;
                    objectArray[5] = (Integer)objectArray[5] - -5;
                    n2 = (Integer)objectArray[5];
                    return n2 != 0;
                }
                case 588418017: 
            }
            int n5 = 74;
            int n6 = -270769190;
            n5 ^= 0xFFFFFF99;
            n2 = n5 ^= 0xFFFFFFD3;
            return n2 != 0;
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
        this.agb = new akf((String)o0Oo000O0oO[n185] + (String)o0Oo000O0oO[n186] + (String)o0Oo000O0oO[n187 ^= 0x3D], (gi)this, (Boolean)bl9, () -> {
            int n2;
            int n3 = 535299867;
            if (185182173 >= -121772600) {
            }
            Object[] objectArray = new Object[6];
            if (!((Boolean)this.afZ.wp()).booleanValue()) {
                objectArray[0] = 222;
                objectArray[0] = (Integer)objectArray[0] + -100;
                objectArray[0] = (Integer)objectArray[0] ^ 0x7B;
                n2 = (Integer)objectArray[0];
                return n2 != 0;
            }
            int n4 = 30;
            n4 -= -65;
            int n5 = -1130227532;
            n2 = n4 += -95;
            return n2 != 0;
        });
        int n190 = 32;
        n190 -= 51;
        n190 += 45;
        int n191 = 62;
        n191 -= 11;
        int n193 = 121;
        n193 = n193 - 3;
        boolean bl10 = n193 - 118;
        this.agc = new akf((String)o0Oo000O0oO[n190] + (String)o0Oo000O0oO[n191 ^= 0x6D], (gi)this, (Boolean)bl10, () -> {
            int n2;
            if (o0o000o0O0o0) {
                int n3 = 28;
            }
            Object[] objectArray = new Object[4];
            if (((Boolean)this.afZ.wp()).booleanValue()) {
                int n4 = 49;
                n4 ^= 0x68;
                n2 = n4 ^= 0x59;
                return n2 != 0;
            }
            objectArray[1] = -152;
            objectArray[1] = (Integer)objectArray[1] + 58;
            objectArray[1] = (Integer)objectArray[1] ^ 0xFFFFFFA3;
            n2 = (Integer)objectArray[1];
            return n2 != 0;
        });
        int n194 = -3;
        n194 += -82;
        n194 ^= 0xFFFFFFA7;
        int n195 = 78;
        n195 ^= 0xFFFFFFA9;
        int n197 = -7;
        n197 = n197 - -54;
        boolean bl11 = n197 - 46;
        this.agd = new akf((String)o0Oo000O0oO[n194] + (String)o0Oo000O0oO[n195 -= -63], (gi)this, (Boolean)bl11, () -> {
            int n2;
            if (o0o000o0O0o0) {
            }
            Object[] objectArray = new Object[5];
            if (!((Boolean)this.afZ.wp()).booleanValue()) {
                objectArray[3] = -7;
                objectArray[3] = (Integer)objectArray[3] ^ 0xFFFFFFE5;
                objectArray[3] = (Integer)objectArray[3] - 27;
                int n3 = -2067770628;
                n2 = (Integer)objectArray[3];
                return n2 != 0;
            }
            int n4 = -194;
            n4 -= -88;
            n2 = n4 += 106;
            return n2 != 0;
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
        this.age = new akg((String)o0Oo000O0oO[n198] + (String)o0Oo000O0oO[n199], this, (Number)n200, (Number)n201, (Number)(n202 += -80), (Number)(n203 += -16), (Number)(n204 ^= 0xFFFFFF88), () -> {
            int n2;
            if (o0o000o0O0o0) {
                throw null;
            }
            Object[] objectArray = new Object[5];
            int n3 = 176796345;
            block4: while (true) {
                switch (n3) {
                    case 1797832140: {
                        break block4;
                    }
                    case 1711423296: {
                        int n4 = -4;
                        n4 -= 57;
                        n2 = n4 ^= 0xFFFFFFC3;
                        return n2 != 0;
                    }
                    default: {
                        if (!((Boolean)this.afZ.wp()).booleanValue()) break block4;
                        n3 = 1711423296;
                        continue block4;
                    }
                }
                break;
            }
            objectArray[1] = -54;
            objectArray[1] = (Integer)objectArray[1] - 32;
            objectArray[1] = (Integer)objectArray[1] + 87;
            n2 = (Integer)objectArray[1];
            return n2 != 0;
        });
        int n205 = -54;
        n205 -= -36;
        int n207 = 5;
        n207 = n207 + 20;
        boolean bl12 = n207 ^ 0x18;
        this.agf = new akf((String)o0Oo000O0oO[n205 -= -75], (gi)this, (Boolean)bl12, () -> {
            int n2;
            if (o0o000o0O0o0) {
            }
            Object[] objectArray = new Object[6];
            int n3 = (Boolean)this.afZ.wp() == false ? -947958275 : -449718949;
            switch (n3) {
                case -449718949: {
                    int n4 = -107;
                    n4 ^= 0xFFFFFF8D;
                    n2 = n4 += -24;
                    return n2 != 0;
                }
            }
            objectArray[4] = -7;
            int n5 = 711428258;
            objectArray[4] = (Integer)objectArray[4] ^ 0x7A;
            objectArray[4] = (Integer)objectArray[4] + 126;
            n2 = (Integer)objectArray[4];
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
        this.agg = new akg((String)o0Oo000O0oO[n208] + (String)o0Oo000O0oO[n209], this, (Number)n210, (Number)n211, (Number)(n212 += -64), (Number)(n213 += -118), (Number)(n214 -= -112), () -> {
            int n2;
            int n3 = 23415185;
            ooo00oo0Ooo0 = -121772600;
            if (31486791 <= ooo00oo0Ooo0) {
            }
            Object[] objectArray = new Object[6];
            if (((Boolean)this.afZ.wp()).booleanValue()) {
                int n4 = 103;
                n4 += 5;
                int n5 = 1883236867;
                n2 = n4 -= 108;
                return n2 != 0;
            }
            objectArray[0] = -28;
            int n6 = 2132973460;
            block4: while (true) {
                switch (n6) {
                    default: {
                        objectArray[0] = (Integer)objectArray[0] - -50;
                        n6 = 457797233;
                        continue block4;
                    }
                    case 457797233: 
                }
                break;
            }
            objectArray[0] = (Integer)objectArray[0] - 21;
            n2 = (Integer)objectArray[0];
            return n2 != 0;
        });
        int n215 = 227;
        n215 -= 114;
        n215 += 9;
        int n216 = -36;
        n216 -= 33;
        int n218 = 66;
        n218 = n218 ^ 0x11;
        boolean bl13 = n218 + -83;
        this.agh = new akf((String)o0Oo000O0oO[n215] + (String)o0Oo000O0oO[n216 ^= 0xFFFFFFD4], (gi)this, (Boolean)bl13, () -> {
            int n2;
            if (o0o000o0O0o0) {
                throw new ClassNotFoundException();
            }
            Object[] objectArray = new Object[6];
            if (!((Boolean)this.afZ.wp()).booleanValue()) {
                int n3 = 2040772697;
                block5: while (true) {
                    switch (n3) {
                        case -944100060: {
                            break block5;
                        }
                        default: {
                            n3 = -1799670639;
                            continue block5;
                        }
                        case -1799670639: {
                            objectArray[4] = 134;
                            objectArray[4] = (Integer)objectArray[4] - 106;
                            objectArray[4] = (Integer)objectArray[4] + -27;
                            int n4 = -153308895;
                            n2 = (Integer)objectArray[4];
                            return n2 != 0;
                        }
                    }
                    break;
                }
            }
            int n5 = -119;
            n5 -= 7;
            n2 = n5 += 126;
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
        this.agi = new akg((String)o0Oo000O0oO[n219] + (String)o0Oo000O0oO[n220] + (String)o0Oo000O0oO[n221], this, (Number)n222, (Number)n223, (Number)(n224 ^= 0x79), (Number)(n225 += -107), (Number)(n226 -= 110), () -> {
            int n2;
            int n3 = 1115359911;
            ooo00oo0Ooo0 = -121772600;
            if (1360214207 <= ooo00oo0Ooo0) {
            }
            Object[] objectArray = new Object[6];
            if (((Boolean)this.afZ.wp()).booleanValue() && ((Boolean)this.agh.wp()).booleanValue()) {
                int n4 = -403160539;
                int n5 = -81;
                n5 ^= 0xFFFFFF8D;
                int n6 = 685442087;
                n2 = n5 -= 34;
                return n2 != 0;
            }
            objectArray[0] = -162;
            objectArray[0] = (Integer)objectArray[0] - -66;
            objectArray[0] = (Integer)objectArray[0] ^ 0xFFFFFFA1;
            n2 = (Integer)objectArray[0];
            return n2 != 0;
        });
        int n227 = 43;
        n227 += -6;
        n227 ^= 0x3B;
        int n228 = 325;
        n228 -= 75;
        int n230 = -10;
        n230 = n230 - -23;
        boolean bl14 = n230 - 13;
        this.agj = new akf((String)o0Oo000O0oO[n227] + (String)o0Oo000O0oO[n228 += -96], (gi)this, (Boolean)bl14, () -> {
            int n2;
            if (o0o000o0O0o0) {
            }
            Object[] objectArray = new Object[7];
            if (((Boolean)this.afZ.wp()).booleanValue() && ((Boolean)this.agh.wp()).booleanValue()) {
                int n3 = -319566260;
                int n4 = -93;
                int n5 = -1130266407;
                n4 ^= 0xFFFFFFC8;
                n2 = n4 ^= 0x6B;
                return n2 != 0;
            }
            objectArray[3] = -153;
            int n6 = -1868942284;
            block5: while (true) {
                switch (n6) {
                    default: {
                        objectArray[3] = (Integer)objectArray[3] - -113;
                        objectArray[3] = (Integer)objectArray[3] + 41;
                        n6 = -2040625769;
                        continue block5;
                    }
                    case -2040625769: 
                }
                break;
            }
            n2 = (Integer)objectArray[3];
            return n2 != 0;
        });
        int n231 = 15;
        n231 += 6;
        int n233 = 22;
        n233 = n233 - 50;
        boolean bl15 = n233 - -28;
        this.agk = new akf((String)o0Oo000O0oO[n231 += 61], (gi)this, (Boolean)bl15);
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
        this.agl = new akg((String)o0Oo000O0oO[n234], this, (Number)n235, (Number)n236, (Number)(n237 -= 95), (Number)(n238 += -110), (Number)(n239 ^= 0xFFFFFFAC), () -> {
            int n2;
            int n3 = 514306295;
            if (248808581 >= 7898831) {
            }
            Object[] objectArray = new Object[7];
            int n4 = -750294020;
            if (!((Boolean)this.agk.wp()).booleanValue()) {
                objectArray[0] = -70;
                int n5 = -420994601;
                objectArray[0] = (Integer)objectArray[0] ^ 0x41;
                objectArray[0] = (Integer)objectArray[0] ^ 0xFFFFFFFA;
                n2 = (Integer)objectArray[0];
                return n2 != 0;
            }
            int n6 = -53;
            n6 ^= 0x20;
            int n7 = 308254529;
            n2 = n6 += 21;
            return n2 != 0;
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
        this.agm = new akg((String)o0Oo000O0oO[n240], this, (Number)n241, (Number)n242, (Number)(n243 -= 120), (Number)(n244 -= 47), (Number)(n245 -= 61), () -> {
            int n2;
            if (o0o000o0O0o0) {
            }
            Object[] objectArray = new Object[7];
            if (!((Boolean)this.agk.wp()).booleanValue()) {
                int n3 = 1498328749;
                objectArray[3] = -14;
                objectArray[3] = (Integer)objectArray[3] ^ 0xFFFFFF98;
                objectArray[3] = (Integer)objectArray[3] ^ 0x6B;
                int n4 = -915763585;
                n2 = (Integer)objectArray[3];
                return n2 != 0;
            }
            int n5 = -14;
            int n6 = 1921955357;
            n5 += 104;
            n2 = n5 ^= 0x5A;
            return n2 != 0;
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
        this.agn = new akg((String)o0Oo000O0oO[n246] + (String)o0Oo000O0oO[n247], this, (Number)n248, (Number)n249, (Number)(n250 -= -55), (Number)(n251 += 119), (Number)(n252 -= -12), this::fV);
        int n253 = 113;
        n253 += 81;
        int n254 = 128;
        n254 += -34;
        this.ago = new akn((String)o0Oo000O0oO[n253 ^= 0x42], this, (Number)Double.longBitsToDouble(4596373779694328218L), (Number)Double.longBitsToDouble(4596373779694328218L), (Number)(n254 += -93), (Number)Double.longBitsToDouble(4587366580439587226L), () -> {
            int n2;
            int n3 = 515291396;
            ooo00oo0Ooo0 = 7898831;
            if (799893299 < ooo00oo0Ooo0) {
            }
            Object[] objectArray = new Object[7];
            if (!((Boolean)this.agk.wp()).booleanValue()) {
                objectArray[2] = -25;
                objectArray[2] = (Integer)objectArray[2] ^ 0x70;
                int n4 = 779408368;
                objectArray[2] = (Integer)objectArray[2] - -106;
                n2 = (Integer)objectArray[2];
                return n2 != 0;
            }
            int n5 = -68;
            n5 -= -22;
            int n6 = 1251436273;
            n2 = n5 += 46;
            return n2 != 0;
        });
        int n255 = 84;
        n255 += 64;
        n255 -= 46;
        int n256 = -155;
        n256 ^= 0xFFFFFFD3;
        int n258 = 70;
        n258 = n258 - 1;
        boolean bl16 = n258 + -69;
        this.agp = new akf((String)o0Oo000O0oO[n255] + (String)o0Oo000O0oO[n256 ^= 0x3D], (gi)this, (Boolean)bl16, () -> {
            int n2;
            int n3 = 400989994;
            ooo00oo0Ooo0 = 755756487;
            if (1057050803 < ooo00oo0Ooo0) {
            }
            Object[] objectArray = new Object[9];
            objectArray[2] = 24;
            objectArray[2] = (Integer)objectArray[2] + -70;
            objectArray[2] = (Integer)objectArray[2] ^ 0xFFFFFFE7;
            if (((akd)this.afJ.wp()).getName().equals((String)o0Oo000O0oO[(Integer)objectArray[2]])) {
                int n4 = 11;
                n4 += 84;
                int n5 = 1237215978;
                n2 = n4 -= 95;
                return n2 != 0;
            }
            int n6 = 1268932901;
            block5: while (true) {
                switch (n6) {
                    default: {
                        n6 = 299552185;
                        continue block5;
                    }
                    case 299552185: 
                }
                break;
            }
            objectArray[5] = -102;
            objectArray[5] = (Integer)objectArray[5] ^ 0xFFFFFFDA;
            int n7 = 1583443563;
            objectArray[5] = (Integer)objectArray[5] + -63;
            n2 = (Integer)objectArray[5];
            return n2 != 0;
        });
        int n259 = -54;
        n259 += 34;
        int n261 = 131;
        n261 = n261 - 16;
        boolean bl17 = n261 ^ 0x72;
        this.agq = new akf((String)o0Oo000O0oO[n259 -= -118], (gi)this, (Boolean)bl17);
        int n262 = 69;
        n262 ^= 0xFFFFFF99;
        int n264 = -144;
        n264 = n264 + 68;
        boolean bl18 = n264 ^ 0xFFFFFFB4;
        this.agr = new akf((String)o0Oo000O0oO[n262 += 54], (gi)this, (Boolean)bl18);
        int n265 = 166;
        n265 -= 51;
        n265 -= 29;
        int n266 = 140;
        n266 -= 29;
        akd[] akdArray41 = new akd[n266 ^= 0x6E];
        int n267 = -13;
        n267 ^= 0xFFFFFF8D;
        int n268 = 184;
        n268 += -65;
        akdArray41[n267 += -126] = new akp((String)o0Oo000O0oO[n268 -= 61]);
        int n269 = 113;
        n269 -= 102;
        akd[] akdArray42 = new akd[n269 -= 10];
        int n270 = 34;
        n270 += 80;
        int n271 = -77;
        n271 += -2;
        akdArray42[n270 ^= 0x72] = new akp((String)o0Oo000O0oO[n271 += 99]);
        int n272 = 30;
        n272 ^= 0x72;
        akd[] akdArray43 = new akd[n272 ^= 0x6D];
        int n273 = 23;
        n273 -= 33;
        int n274 = 10;
        n274 ^= 0x31;
        akdArray43[n273 ^= 0xFFFFFFF6] = new akp((String)o0Oo000O0oO[n274 ^= 0x3C]);
        int n275 = 220;
        n275 ^= 0x56;
        this.ags = new akm((String)o0Oo000O0oO[n265], (gi)this, () -> {
            int n2;
            int n3 = 561332192;
            if (92092097 >= -121772600) {
                throw null;
            }
            Object[] objectArray = new Object[6];
            if (!((Boolean)this.agr.wp()).booleanValue()) {
                objectArray[5] = 57;
                objectArray[5] = (Integer)objectArray[5] + 49;
                objectArray[5] = (Integer)objectArray[5] ^ 0x6B;
                n2 = (Integer)objectArray[5];
                return n2 != 0;
            }
            int n4 = 40;
            n4 += 80;
            int n5 = 1631141842;
            n2 = n4 -= 120;
            return n2 != 0;
        }).a(akdArray41).a(akdArray42).a(akdArray43).co((String)o0Oo000O0oO[n275 -= 121]);
        int n276 = 21;
        n276 -= -41;
        n276 -= -61;
        int n277 = 53;
        n277 -= 106;
        n277 += 78;
        int n278 = -67;
        n278 ^= 0xFFFFFFFD;
        int n280 = 9;
        n280 = n280 - 40;
        boolean bl19 = n280 + 31;
        akf2((String)o0Oo000O0oO[n276] + (String)o0Oo000O0oO[n277] + (String)o0Oo000O0oO[n278 ^= 0x6C], (gi)this, (Boolean)bl19, () -> {
            int n2;
            if (o0o000o0O0o0) {
            }
            Object[] objectArray = new Object[7];
            if (((Boolean)this.agr.wp()).booleanValue()) {
                int n3 = 42;
                int n4 = -1959627777;
                n3 ^= 0x50;
                n2 = n3 += -122;
                return n2 != 0;
            }
            int n5 = 582819054;
            objectArray[0] = 72;
            int n6 = 417817694;
            objectArray[0] = (Integer)objectArray[0] ^ 0x66;
            objectArray[0] = (Integer)objectArray[0] + -45;
            n2 = (Integer)objectArray[0];
            return n2 != 0;
        });
        this.agt = akf2;
        int n281 = 115;
        n281 -= 114;
        int n283 = 196;
        n283 = n283 - 80;
        boolean bl20 = n283 + -116;
        this.agu = new akf((String)o0Oo000O0oO[n281 -= -70], (gi)this, (Boolean)bl20, () -> {
            int n2 = 1617313651;
            if (950250811 >= n2) {
            }
            Object[] objectArray = new Object[7];
            int n3 = -1633290138;
            while (true) {
                int n4;
                switch (n3) {
                    case 1881689282: {
                        objectArray[2] = -119;
                        int n5 = -2075036367;
                        objectArray[2] = (Integer)objectArray[2] ^ 0x23;
                        objectArray[2] = (Integer)objectArray[2] + 87;
                        n4 = (Integer)objectArray[2];
                        return n4 != 0;
                    }
                }
                if (((Boolean)this.agr.wp()).booleanValue()) {
                    int n6 = -215;
                    n6 += 114;
                    int n7 = 979994757;
                    n4 = n6 -= -101;
                    return n4 != 0;
                }
                n3 = 1881689282;
            }
        });
        int n284 = 366;
        n284 += -119;
        n284 ^= 0x76;
        int n285 = 37;
        n285 += 83;
        int n287 = 26;
        n287 = n287 + -99;
        boolean bl21 = n287 + 73;
        this.agv = new akf((String)o0Oo000O0oO[n284] + (String)o0Oo000O0oO[n285 -= 78], (gi)this, (Boolean)bl21, () -> {
            int n2;
            int n3 = 1724459944;
            if (148238284 >= -121772600) {
            }
            Object[] objectArray = new Object[6];
            int n4 = (Boolean)this.agr.wp() == false ? 1078976875 : 760575531;
            switch (n4) {
                default: {
                    objectArray[5] = -43;
                    objectArray[5] = (Integer)objectArray[5] ^ 0xFFFFFFDE;
                    objectArray[5] = (Integer)objectArray[5] - 10;
                    n2 = (Integer)objectArray[5];
                    return n2 != 0;
                }
                case 760575531: 
            }
            int n5 = -73;
            n5 ^= 0x14;
            int n6 = 102491907;
            block6: while (true) {
                switch (n6) {
                    default: {
                        n5 ^= 0xFFFFFFA3;
                        n6 = 1416848476;
                        continue block6;
                    }
                    case 1416848476: 
                }
                break;
            }
            n2 = n5;
            return n2 != 0;
        });
        this.agy = new aka(0.0, 0.0, 0.0);
        this.agU = ahj::j;
        this.agV = this::j;
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
            int n2 = 523843386;
            ooo00oo0Ooo0 = 310325002;
            if (597709360 <= ooo00oo0Ooo0) {
                n2 = -1003588944;
            }
            if (fd2.getPosY() < tn.aEi.bgb.gX - Double.longBitsToDouble(0x4000000000000000L)) {
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
            if (((Boolean)this.agp.wp()).booleanValue()) {
                objectArray[1] = 112;
                objectArray[1] = (Integer)objectArray[1] + 16;
                objectArray[1] = (Integer)objectArray[1] + 4;
                if (!((akd)this.afL.wp()).getName().equals((String)o0Oo000O0oO[(Integer)objectArray[1]])) {
                    ea2.cF();
                }
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
        this.ahb = this::k;
        this.ahc = eo2 -> {
            int n2 = 2136551847;
            if (1728376341 >= 42975370) {
                n2 = 44;
            }
            if (2122346160 < (ooo00oo0Ooo0 = 42975370)) {
            }
            Object[] objectArray = new Object[7];
            this.kp();
            objectArray[5] = 219;
            objectArray[5] = (Integer)objectArray[5] - -7;
            objectArray[5] = (Integer)objectArray[5] - 95;
            if (!Objects.equals(((akd)this.ags.wp()).getName(), (String)o0Oo000O0oO[(Integer)objectArray[5]]) && !((Boolean)this.afU.wp()).booleanValue()) {
                int n3 = -533186734;
                objectArray[0] = -79;
                objectArray[0] = (Integer)objectArray[0] ^ 0xFFFFFFB8;
                objectArray[0] = (Integer)objectArray[0] - -64;
                if (!((akd)this.afM.wp()).getName().equals((String)o0Oo000O0oO[(Integer)objectArray[0]])) {
                    aif.useDiagonalSpeed();
                }
            }
            objectArray[2] = -9;
            objectArray[2] = (Integer)objectArray[2] - -99;
            objectArray[2] = (Integer)objectArray[2] - -52;
            if (((akd)this.afO.wp()).getName().equals((String)o0Oo000O0oO[(Integer)objectArray[2]]) && tn.aEi.bgb.aO && aif.isMoving() && tn.aEi.bgb.gX == this.Me) {
                tn.aEi.bgb.jump();
            }
        };
        this.ahd = var1_1 -> {
            /*
             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
             * 
             * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [11[UNCONDITIONALDOLOOP]], but top level block is 12[UNCONDITIONALDOLOOP]
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
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
            if (tn.aEi.bgb.aO) return;
            objectArray[13] = Float.valueOf(tn.aEi.bgb.pl);
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
            if (!tn.aEi.aIp.cgI.QQ()) return;
            if ((Boolean)this.afZ.wp() == false) return;
            if (tn.aEi.bgb.aO) return;
            int n8 = -330211241;
            objectArray[19] = 61;
            objectArray[19] = (Integer)objectArray[19] ^ 0x61;
            objectArray[19] = (Integer)objectArray[19] ^ 0x5C;
            tn.aEi.bgb.setSprinting((Integer)objectArray[19] != 0);
        };
    }

    /*
     * Enabled aggressive block sorting
     */
    public strictfp String km() {
        String string;
        int n2;
        if (o0o000o0O0o0) {
        }
        if (302820632 >= 729150226) {
        }
        Object[] objectArray = new Object[11];
        int n3 = -199150100;
        this.kn();
        objectArray[5] = ((akd)this.afJ.wp()).getName();
        objectArray[10] = 29;
        objectArray[10] = (Integer)objectArray[10] + 5;
        objectArray[10] = (Integer)objectArray[10] - -32;
        if (!((String)objectArray[5]).equals((String)o0Oo000O0oO[(Integer)objectArray[10]])) return (String)objectArray[5];
        if (this.ko()) {
            int n4 = 1011478959;
            objectArray[0] = -69;
            objectArray[0] = (Integer)objectArray[0] ^ 0x53;
            objectArray[0] = (Integer)objectArray[0] - -119;
            return (String)o0Oo000O0oO[(Integer)objectArray[0]];
        }
        if ((Boolean)this.agp.wp() == false) return (String)objectArray[5];
        if (tn.aEi.aIp.cgM.QQ()) {
            objectArray[1] = 22;
            objectArray[1] = (Integer)objectArray[1] ^ 0xFFFFFF8E;
            objectArray[1] = (Integer)objectArray[1] ^ 0xFFFFFF99;
            string = (String)o0Oo000O0oO[(Integer)objectArray[1]];
            return string;
        }
        objectArray[6] = 20;
        objectArray[6] = (Integer)objectArray[6] + 70;
        objectArray[6] = (Integer)objectArray[6] - -24;
        string = (String)o0Oo000O0oO[(Integer)objectArray[6]];
        return string;
    }

    /*
     * Enabled aggressive block sorting
     */
    public static void Oo0o00000O00() {
        if (o0o000o0O0o0) {
        }
        O0OoOO0OOOOO = new int[400];
        tn.O0OoOO0OOOOO[139] = -98;
        tn.O0OoOO0OOOOO[222] = -189;
        tn.O0OoOO0OOOOO[279] = 46;
        tn.O0OoOO0OOOOO[181] = -27;
        tn.O0OoOO0OOOOO[156] = -161;
        tn.O0OoOO0OOOOO[170] = 55;
        tn.O0OoOO0OOOOO[293] = 120;
        tn.O0OoOO0OOOOO[186] = -41;
        tn.O0OoOO0OOOOO[223] = -109;
        tn.O0OoOO0OOOOO[221] = -115;
        tn.O0OoOO0OOOOO[147] = -45;
        tn.O0OoOO0OOOOO[320] = -26;
        tn.O0OoOO0OOOOO[0] = -8;
        int n2 = 848960636;
        block84: while (true) {
            switch (n2) {
                case 630375925: {
                    tn.O0OoOO0OOOOO[134] = -111;
                    tn.O0OoOO0OOOOO[270] = -114;
                    tn.O0OoOO0OOOOO[85] = 22;
                    tn.O0OoOO0OOOOO[203] = 111;
                    tn.O0OoOO0OOOOO[378] = 309;
                    tn.O0OoOO0OOOOO[259] = -66;
                    tn.O0OoOO0OOOOO[77] = 89;
                    n2 = 1858084091;
                    continue block84;
                }
                case 1858084091: {
                    tn.O0OoOO0OOOOO[335] = 94;
                    tn.O0OoOO0OOOOO[107] = 71;
                    tn.O0OoOO0OOOOO[178] = -86;
                    tn.O0OoOO0OOOOO[377] = 107;
                    tn.O0OoOO0OOOOO[21] = -153;
                    tn.O0OoOO0OOOOO[332] = -105;
                    tn.O0OoOO0OOOOO[288] = -109;
                    n2 = 1070417466;
                    continue block84;
                }
                case 1070417466: {
                    tn.O0OoOO0OOOOO[354] = -6;
                    tn.O0OoOO0OOOOO[372] = 142;
                    tn.O0OoOO0OOOOO[260] = 31;
                    tn.O0OoOO0OOOOO[65] = -30;
                    n2 = 817076982;
                    continue block84;
                }
                case 817076982: {
                    tn.O0OoOO0OOOOO[182] = 111;
                    tn.O0OoOO0OOOOO[386] = -126;
                    tn.O0OoOO0OOOOO[124] = -11;
                    tn.O0OoOO0OOOOO[362] = 14;
                    n2 = 675525774;
                    continue block84;
                }
                case 675525774: {
                    tn.O0OoOO0OOOOO[150] = -147;
                    tn.O0OoOO0OOOOO[312] = 134;
                    n2 = -640613813;
                    continue block84;
                }
                case -640613813: {
                    tn.O0OoOO0OOOOO[262] = -43;
                    tn.O0OoOO0OOOOO[87] = -171;
                    tn.O0OoOO0OOOOO[373] = -68;
                    tn.O0OoOO0OOOOO[58] = 86;
                    tn.O0OoOO0OOOOO[261] = 161;
                    n2 = 1850166689;
                    continue block84;
                }
                case 1850166689: {
                    tn.O0OoOO0OOOOO[197] = -21;
                    tn.O0OoOO0OOOOO[352] = -38;
                    tn.O0OoOO0OOOOO[148] = -17;
                    tn.O0OoOO0OOOOO[391] = 73;
                    tn.O0OoOO0OOOOO[35] = 12;
                    n2 = -1839000851;
                    continue block84;
                }
                case -1839000851: {
                    tn.O0OoOO0OOOOO[144] = -11;
                    tn.O0OoOO0OOOOO[138] = -38;
                    tn.O0OoOO0OOOOO[49] = 89;
                    tn.O0OoOO0OOOOO[129] = 52;
                    tn.O0OoOO0OOOOO[140] = 67;
                    tn.O0OoOO0OOOOO[143] = 15;
                    n2 = 2133916460;
                    continue block84;
                }
                case 2133916460: {
                    tn.O0OoOO0OOOOO[128] = -96;
                    tn.O0OoOO0OOOOO[146] = -72;
                    tn.O0OoOO0OOOOO[61] = -39;
                    n2 = 570389564;
                    continue block84;
                }
                case 570389564: {
                    tn.O0OoOO0OOOOO[57] = -8;
                    tn.O0OoOO0OOOOO[132] = -218;
                    n2 = 536904876;
                    continue block84;
                }
                case 536904876: {
                    tn.O0OoOO0OOOOO[5] = 123;
                    tn.O0OoOO0OOOOO[2] = -27;
                    tn.O0OoOO0OOOOO[42] = -18;
                    tn.O0OoOO0OOOOO[253] = -99;
                    tn.O0OoOO0OOOOO[235] = -36;
                    tn.O0OoOO0OOOOO[43] = 32;
                    tn.O0OoOO0OOOOO[79] = -36;
                    tn.O0OoOO0OOOOO[303] = 89;
                    n2 = -416325401;
                    continue block84;
                }
                case -416325401: {
                    tn.O0OoOO0OOOOO[204] = -180;
                    n2 = -583953695;
                    continue block84;
                }
                case -583953695: {
                    tn.O0OoOO0OOOOO[63] = 48;
                    tn.O0OoOO0OOOOO[11] = -110;
                    tn.O0OoOO0OOOOO[208] = -17;
                    tn.O0OoOO0OOOOO[201] = 24;
                    tn.O0OoOO0OOOOO[290] = 91;
                    tn.O0OoOO0OOOOO[205] = -65;
                    tn.O0OoOO0OOOOO[30] = -42;
                    tn.O0OoOO0OOOOO[387] = 193;
                    tn.O0OoOO0OOOOO[247] = -5;
                    tn.O0OoOO0OOOOO[319] = 121;
                    tn.O0OoOO0OOOOO[198] = -51;
                    tn.O0OoOO0OOOOO[193] = -41;
                    tn.O0OoOO0OOOOO[300] = -21;
                    tn.O0OoOO0OOOOO[264] = 106;
                    tn.O0OoOO0OOOOO[163] = -115;
                    tn.O0OoOO0OOOOO[344] = 68;
                    tn.O0OoOO0OOOOO[267] = -18;
                    tn.O0OoOO0OOOOO[153] = 22;
                    n2 = -833246175;
                    continue block84;
                }
                case -833246175: {
                    tn.O0OoOO0OOOOO[19] = 98;
                    tn.O0OoOO0OOOOO[317] = -122;
                    tn.O0OoOO0OOOOO[285] = -29;
                    tn.O0OoOO0OOOOO[112] = 114;
                    tn.O0OoOO0OOOOO[347] = -72;
                    tn.O0OoOO0OOOOO[210] = 181;
                    tn.O0OoOO0OOOOO[74] = -32;
                    tn.O0OoOO0OOOOO[230] = 59;
                    tn.O0OoOO0OOOOO[273] = -9;
                    tn.O0OoOO0OOOOO[374] = -73;
                    tn.O0OoOO0OOOOO[179] = 45;
                    tn.O0OoOO0OOOOO[54] = 201;
                    n2 = -78929327;
                    continue block84;
                }
                case -78929327: {
                    tn.O0OoOO0OOOOO[248] = -7;
                    tn.O0OoOO0OOOOO[64] = 77;
                    n2 = -435368735;
                    continue block84;
                }
                case -435368735: {
                    tn.O0OoOO0OOOOO[345] = -39;
                    tn.O0OoOO0OOOOO[60] = 282;
                    tn.O0OoOO0OOOOO[82] = 64;
                    tn.O0OoOO0OOOOO[195] = 43;
                    tn.O0OoOO0OOOOO[322] = 50;
                    tn.O0OoOO0OOOOO[211] = 49;
                    tn.O0OoOO0OOOOO[315] = 206;
                    tn.O0OoOO0OOOOO[353] = 85;
                    tn.O0OoOO0OOOOO[349] = 56;
                    tn.O0OoOO0OOOOO[337] = -86;
                    tn.O0OoOO0OOOOO[255] = -172;
                    tn.O0OoOO0OOOOO[191] = 49;
                    tn.O0OoOO0OOOOO[296] = 31;
                    tn.O0OoOO0OOOOO[292] = 33;
                    tn.O0OoOO0OOOOO[44] = 50;
                    tn.O0OoOO0OOOOO[75] = -33;
                    tn.O0OoOO0OOOOO[281] = 91;
                    tn.O0OoOO0OOOOO[207] = 40;
                    tn.O0OoOO0OOOOO[52] = 9;
                    n2 = 1897582711;
                    continue block84;
                }
                case 1897582711: {
                    tn.O0OoOO0OOOOO[105] = -46;
                    tn.O0OoOO0OOOOO[396] = 29;
                    tn.O0OoOO0OOOOO[32] = 43;
                    n2 = 1105437453;
                    continue block84;
                }
                case 1105437453: {
                    tn.O0OoOO0OOOOO[100] = 103;
                    tn.O0OoOO0OOOOO[274] = 70;
                    n2 = 242385104;
                    continue block84;
                }
                case 242385104: {
                    tn.O0OoOO0OOOOO[48] = -63;
                    n2 = -1090726124;
                    continue block84;
                }
                case -1090726124: {
                    tn.O0OoOO0OOOOO[12] = 88;
                    tn.O0OoOO0OOOOO[394] = 11;
                    tn.O0OoOO0OOOOO[9] = -5;
                    n2 = -1260323484;
                    continue block84;
                }
                case -1260323484: {
                    tn.O0OoOO0OOOOO[232] = -118;
                    tn.O0OoOO0OOOOO[67] = 112;
                    tn.O0OoOO0OOOOO[108] = 27;
                    tn.O0OoOO0OOOOO[282] = -115;
                    n2 = -1628585326;
                    continue block84;
                }
                case -1628585326: {
                    tn.O0OoOO0OOOOO[361] = -70;
                    tn.O0OoOO0OOOOO[266] = -16;
                    tn.O0OoOO0OOOOO[4] = -52;
                    tn.O0OoOO0OOOOO[329] = 10;
                    n2 = 1087240279;
                    continue block84;
                }
                case 1087240279: {
                    tn.O0OoOO0OOOOO[245] = -74;
                    tn.O0OoOO0OOOOO[83] = -117;
                    tn.O0OoOO0OOOOO[251] = 103;
                    n2 = -1549603011;
                    continue block84;
                }
                case -1549603011: {
                    tn.O0OoOO0OOOOO[62] = 87;
                    tn.O0OoOO0OOOOO[212] = 78;
                    tn.O0OoOO0OOOOO[240] = 92;
                    tn.O0OoOO0OOOOO[350] = 89;
                    tn.O0OoOO0OOOOO[246] = -13;
                    tn.O0OoOO0OOOOO[219] = -19;
                    tn.O0OoOO0OOOOO[272] = 78;
                    tn.O0OoOO0OOOOO[131] = 30;
                    tn.O0OoOO0OOOOO[154] = 68;
                    n2 = 651920457;
                    continue block84;
                }
                case 651920457: {
                    tn.O0OoOO0OOOOO[113] = 79;
                    tn.O0OoOO0OOOOO[14] = 22;
                    tn.O0OoOO0OOOOO[390] = -1;
                    n2 = -1460920773;
                    continue block84;
                }
                case -1460920773: {
                    tn.O0OoOO0OOOOO[27] = -131;
                    tn.O0OoOO0OOOOO[10] = -74;
                    tn.O0OoOO0OOOOO[325] = -10;
                    tn.O0OoOO0OOOOO[95] = 4;
                    n2 = 1112009009;
                    continue block84;
                }
                case 1112009009: {
                    tn.O0OoOO0OOOOO[398] = 61;
                    tn.O0OoOO0OOOOO[104] = -120;
                    tn.O0OoOO0OOOOO[278] = -125;
                    tn.O0OoOO0OOOOO[252] = -175;
                    tn.O0OoOO0OOOOO[227] = -21;
                    tn.O0OoOO0OOOOO[348] = 97;
                    tn.O0OoOO0OOOOO[120] = 83;
                    tn.O0OoOO0OOOOO[392] = 75;
                    tn.O0OoOO0OOOOO[234] = -109;
                    tn.O0OoOO0OOOOO[92] = 75;
                    n2 = -1775778054;
                    continue block84;
                }
                case -1775778054: {
                    tn.O0OoOO0OOOOO[130] = -21;
                    tn.O0OoOO0OOOOO[174] = 53;
                    tn.O0OoOO0OOOOO[175] = 116;
                    n2 = 1685281217;
                    continue block84;
                }
                case 1685281217: {
                    tn.O0OoOO0OOOOO[321] = -24;
                    tn.O0OoOO0OOOOO[323] = 26;
                    tn.O0OoOO0OOOOO[379] = 99;
                    tn.O0OoOO0OOOOO[190] = 64;
                    n2 = 210275730;
                    continue block84;
                }
                case 210275730: {
                    tn.O0OoOO0OOOOO[215] = 8;
                    tn.O0OoOO0OOOOO[314] = 19;
                    tn.O0OoOO0OOOOO[127] = 56;
                    tn.O0OoOO0OOOOO[299] = -125;
                    tn.O0OoOO0OOOOO[165] = -17;
                    tn.O0OoOO0OOOOO[295] = 89;
                    tn.O0OoOO0OOOOO[84] = 2;
                    n2 = 1237053853;
                    continue block84;
                }
                case 1237053853: {
                    tn.O0OoOO0OOOOO[214] = 69;
                    tn.O0OoOO0OOOOO[302] = -60;
                    tn.O0OoOO0OOOOO[341] = -103;
                    tn.O0OoOO0OOOOO[367] = -24;
                    tn.O0OoOO0OOOOO[51] = -34;
                    tn.O0OoOO0OOOOO[177] = -41;
                    tn.O0OoOO0OOOOO[71] = -45;
                    n2 = 983317870;
                    continue block84;
                }
                case 983317870: {
                    tn.O0OoOO0OOOOO[88] = -50;
                    tn.O0OoOO0OOOOO[275] = -80;
                    tn.O0OoOO0OOOOO[364] = -121;
                    tn.O0OoOO0OOOOO[89] = 122;
                    tn.O0OoOO0OOOOO[78] = 13;
                    tn.O0OoOO0OOOOO[17] = -94;
                    tn.O0OoOO0OOOOO[70] = -101;
                    n2 = -1833580368;
                    continue block84;
                }
                case -1833580368: {
                    tn.O0OoOO0OOOOO[305] = 36;
                    n2 = -482284143;
                    continue block84;
                }
                case -482284143: {
                    tn.O0OoOO0OOOOO[45] = 17;
                    tn.O0OoOO0OOOOO[202] = 20;
                    tn.O0OoOO0OOOOO[383] = 118;
                    tn.O0OoOO0OOOOO[80] = 85;
                    tn.O0OoOO0OOOOO[133] = -107;
                    tn.O0OoOO0OOOOO[69] = -11;
                    tn.O0OoOO0OOOOO[226] = -98;
                    n2 = 40452793;
                    continue block84;
                }
                case 40452793: {
                    tn.O0OoOO0OOOOO[351] = 256;
                    tn.O0OoOO0OOOOO[103] = -66;
                    tn.O0OoOO0OOOOO[38] = 60;
                    tn.O0OoOO0OOOOO[167] = 50;
                    tn.O0OoOO0OOOOO[220] = 97;
                    tn.O0OoOO0OOOOO[357] = -151;
                    tn.O0OoOO0OOOOO[6] = 8;
                    tn.O0OoOO0OOOOO[369] = -49;
                    tn.O0OoOO0OOOOO[16] = 112;
                    tn.O0OoOO0OOOOO[28] = 18;
                    tn.O0OoOO0OOOOO[283] = 4;
                    tn.O0OoOO0OOOOO[306] = -172;
                    tn.O0OoOO0OOOOO[294] = 70;
                    tn.O0OoOO0OOOOO[23] = -126;
                    tn.O0OoOO0OOOOO[200] = 74;
                    n2 = 655869031;
                    continue block84;
                }
                case 655869031: {
                    tn.O0OoOO0OOOOO[381] = 19;
                    tn.O0OoOO0OOOOO[307] = 59;
                    n2 = 56929601;
                    continue block84;
                }
                case 56929601: {
                    tn.O0OoOO0OOOOO[94] = -35;
                    tn.O0OoOO0OOOOO[244] = -87;
                    tn.O0OoOO0OOOOO[111] = 144;
                    n2 = -1378675157;
                    continue block84;
                }
                case -1378675157: {
                    tn.O0OoOO0OOOOO[121] = -106;
                    n2 = 1958227553;
                    continue block84;
                }
                case 1958227553: {
                    tn.O0OoOO0OOOOO[237] = -37;
                    tn.O0OoOO0OOOOO[93] = 167;
                    n2 = -1500414588;
                    continue block84;
                }
                case -1500414588: {
                    tn.O0OoOO0OOOOO[125] = -94;
                    tn.O0OoOO0OOOOO[336] = -157;
                    tn.O0OoOO0OOOOO[123] = -83;
                    tn.O0OoOO0OOOOO[157] = -50;
                    tn.O0OoOO0OOOOO[301] = 38;
                    tn.O0OoOO0OOOOO[25] = 19;
                    tn.O0OoOO0OOOOO[13] = -65;
                    tn.O0OoOO0OOOOO[371] = 14;
                    tn.O0OoOO0OOOOO[101] = -25;
                    tn.O0OoOO0OOOOO[209] = -57;
                    tn.O0OoOO0OOOOO[7] = 20;
                    tn.O0OoOO0OOOOO[233] = 95;
                    tn.O0OoOO0OOOOO[316] = -17;
                    tn.O0OoOO0OOOOO[243] = 58;
                    n2 = 1813422501;
                    continue block84;
                }
                case 1813422501: {
                    tn.O0OoOO0OOOOO[189] = -9;
                    tn.O0OoOO0OOOOO[37] = -43;
                    tn.O0OoOO0OOOOO[366] = 51;
                    tn.O0OoOO0OOOOO[308] = -110;
                    tn.O0OoOO0OOOOO[76] = 56;
                    n2 = -517177164;
                    continue block84;
                }
                case -517177164: {
                    tn.O0OoOO0OOOOO[31] = 0;
                    tn.O0OoOO0OOOOO[346] = 96;
                    n2 = 976850988;
                    continue block84;
                }
                case 976850988: {
                    tn.O0OoOO0OOOOO[286] = 20;
                    tn.O0OoOO0OOOOO[265] = 89;
                    tn.O0OoOO0OOOOO[375] = 34;
                    n2 = -1322452668;
                    continue block84;
                }
                case -1322452668: {
                    tn.O0OoOO0OOOOO[236] = 119;
                    return;
                }
                default: {
                    tn.O0OoOO0OOOOO[370] = -115;
                    tn.O0OoOO0OOOOO[385] = 60;
                    tn.O0OoOO0OOOOO[242] = 15;
                    tn.O0OoOO0OOOOO[99] = -128;
                    tn.O0OoOO0OOOOO[334] = -84;
                    tn.O0OoOO0OOOOO[304] = -53;
                    tn.O0OoOO0OOOOO[116] = 101;
                    tn.O0OoOO0OOOOO[169] = -65;
                    n2 = 1981183764;
                    continue block84;
                }
                case 1981183764: {
                    tn.O0OoOO0OOOOO[238] = 26;
                    n2 = -1313136333;
                    continue block84;
                }
                case -1313136333: {
                    tn.O0OoOO0OOOOO[185] = 31;
                    tn.O0OoOO0OOOOO[359] = 62;
                    tn.O0OoOO0OOOOO[166] = 32;
                    tn.O0OoOO0OOOOO[73] = 70;
                    tn.O0OoOO0OOOOO[151] = 113;
                    tn.O0OoOO0OOOOO[271] = -8;
                    tn.O0OoOO0OOOOO[98] = -41;
                    tn.O0OoOO0OOOOO[126] = -112;
                    n2 = -165583871;
                    continue block84;
                }
                case -165583871: {
                    tn.O0OoOO0OOOOO[363] = 273;
                    n2 = 563946201;
                    continue block84;
                }
                case 563946201: {
                    tn.O0OoOO0OOOOO[358] = -89;
                    n2 = 1465103179;
                    continue block84;
                }
                case 1465103179: {
                    tn.O0OoOO0OOOOO[184] = -32;
                    tn.O0OoOO0OOOOO[196] = 65;
                    tn.O0OoOO0OOOOO[258] = 95;
                    tn.O0OoOO0OOOOO[206] = 116;
                    tn.O0OoOO0OOOOO[118] = -38;
                    tn.O0OoOO0OOOOO[225] = -232;
                    n2 = -1518869766;
                    continue block84;
                }
                case -1518869766: {
                    tn.O0OoOO0OOOOO[343] = -116;
                    tn.O0OoOO0OOOOO[313] = -115;
                    tn.O0OoOO0OOOOO[309] = 127;
                    n2 = -1075047475;
                    continue block84;
                }
                case -1075047475: {
                    tn.O0OoOO0OOOOO[160] = -51;
                    n2 = 332717664;
                    continue block84;
                }
                case 332717664: {
                    tn.O0OoOO0OOOOO[239] = 12;
                    tn.O0OoOO0OOOOO[1] = 1;
                    n2 = -1577056467;
                    continue block84;
                }
                case -1577056467: {
                    tn.O0OoOO0OOOOO[192] = -163;
                    tn.O0OoOO0OOOOO[26] = -32;
                    tn.O0OoOO0OOOOO[159] = -53;
                    n2 = 1010303014;
                    continue block84;
                }
                case 1010303014: {
                    tn.O0OoOO0OOOOO[355] = -89;
                    tn.O0OoOO0OOOOO[330] = 345;
                    tn.O0OoOO0OOOOO[257] = -91;
                    tn.O0OoOO0OOOOO[384] = 186;
                    tn.O0OoOO0OOOOO[141] = 91;
                    tn.O0OoOO0OOOOO[135] = -183;
                    tn.O0OoOO0OOOOO[47] = 5;
                    n2 = -739576705;
                    continue block84;
                }
                case -739576705: {
                    tn.O0OoOO0OOOOO[66] = -36;
                    tn.O0OoOO0OOOOO[338] = 72;
                    n2 = -2103590542;
                    continue block84;
                }
                case -2103590542: {
                    tn.O0OoOO0OOOOO[55] = 74;
                    tn.O0OoOO0OOOOO[213] = -5;
                    tn.O0OoOO0OOOOO[188] = -75;
                    tn.O0OoOO0OOOOO[91] = -99;
                    tn.O0OoOO0OOOOO[53] = 25;
                    tn.O0OoOO0OOOOO[331] = 113;
                    tn.O0OoOO0OOOOO[194] = -91;
                    tn.O0OoOO0OOOOO[228] = -31;
                    tn.O0OoOO0OOOOO[114] = 157;
                    tn.O0OoOO0OOOOO[376] = 73;
                    tn.O0OoOO0OOOOO[327] = 70;
                    n2 = 440118889;
                    continue block84;
                }
                case 440118889: {
                    tn.O0OoOO0OOOOO[395] = -62;
                    n2 = -2040145192;
                    continue block84;
                }
                case -2040145192: {
                    tn.O0OoOO0OOOOO[3] = -74;
                    n2 = -41001138;
                    continue block84;
                }
                case -41001138: {
                    tn.O0OoOO0OOOOO[110] = 21;
                    tn.O0OoOO0OOOOO[365] = 14;
                    n2 = -1682928972;
                    continue block84;
                }
                case -1682928972: {
                    tn.O0OoOO0OOOOO[22] = -28;
                    tn.O0OoOO0OOOOO[96] = -11;
                    tn.O0OoOO0OOOOO[216] = 133;
                    n2 = -1602946132;
                    continue block84;
                }
                case -1602946132: {
                    tn.O0OoOO0OOOOO[168] = -10;
                    tn.O0OoOO0OOOOO[368] = 108;
                    tn.O0OoOO0OOOOO[340] = -68;
                    tn.O0OoOO0OOOOO[41] = -89;
                    tn.O0OoOO0OOOOO[180] = 5;
                    tn.O0OoOO0OOOOO[217] = -102;
                    n2 = -617082280;
                    continue block84;
                }
                case -617082280: {
                    tn.O0OoOO0OOOOO[199] = -23;
                    tn.O0OoOO0OOOOO[152] = -34;
                    tn.O0OoOO0OOOOO[399] = 97;
                    tn.O0OoOO0OOOOO[36] = -127;
                    tn.O0OoOO0OOOOO[280] = 51;
                    tn.O0OoOO0OOOOO[256] = -82;
                    tn.O0OoOO0OOOOO[39] = -109;
                    n2 = 975527446;
                    continue block84;
                }
                case 975527446: {
                    tn.O0OoOO0OOOOO[356] = -92;
                    tn.O0OoOO0OOOOO[145] = 80;
                    tn.O0OoOO0OOOOO[310] = 99;
                    n2 = 673005569;
                    continue block84;
                }
                case 673005569: {
                    tn.O0OoOO0OOOOO[388] = 10;
                    n2 = -553998670;
                    continue block84;
                }
                case -553998670: {
                    tn.O0OoOO0OOOOO[328] = -11;
                    n2 = -1864463051;
                    continue block84;
                }
                case -1864463051: {
                    tn.O0OoOO0OOOOO[289] = -39;
                    tn.O0OoOO0OOOOO[249] = 157;
                    n2 = 2087870450;
                    continue block84;
                }
                case 2087870450: {
                    tn.O0OoOO0OOOOO[172] = 1;
                    tn.O0OoOO0OOOOO[297] = -181;
                    tn.O0OoOO0OOOOO[50] = 25;
                    tn.O0OoOO0OOOOO[229] = -91;
                    tn.O0OoOO0OOOOO[81] = -278;
                    tn.O0OoOO0OOOOO[291] = -152;
                    n2 = 776848597;
                    continue block84;
                }
                case 776848597: {
                    tn.O0OoOO0OOOOO[34] = -91;
                    tn.O0OoOO0OOOOO[276] = 173;
                    tn.O0OoOO0OOOOO[155] = -76;
                    tn.O0OoOO0OOOOO[382] = 100;
                    n2 = -1236873408;
                    continue block84;
                }
                case -1236873408: {
                    tn.O0OoOO0OOOOO[162] = -61;
                    tn.O0OoOO0OOOOO[46] = 85;
                    tn.O0OoOO0OOOOO[18] = -6;
                    n2 = 1790912468;
                    continue block84;
                }
                case 1790912468: {
                    tn.O0OoOO0OOOOO[269] = -37;
                    n2 = 252787546;
                    continue block84;
                }
                case 252787546: {
                    tn.O0OoOO0OOOOO[339] = -35;
                    tn.O0OoOO0OOOOO[86] = 29;
                    tn.O0OoOO0OOOOO[171] = 19;
                    n2 = 503142609;
                    continue block84;
                }
                case 503142609: {
                    tn.O0OoOO0OOOOO[90] = -42;
                    tn.O0OoOO0OOOOO[72] = 103;
                    tn.O0OoOO0OOOOO[311] = -27;
                    tn.O0OoOO0OOOOO[187] = 98;
                    tn.O0OoOO0OOOOO[318] = -98;
                    tn.O0OoOO0OOOOO[97] = 53;
                    tn.O0OoOO0OOOOO[56] = -16;
                    tn.O0OoOO0OOOOO[40] = 19;
                    tn.O0OoOO0OOOOO[397] = -17;
                    tn.O0OoOO0OOOOO[115] = 69;
                    tn.O0OoOO0OOOOO[277] = 48;
                    tn.O0OoOO0OOOOO[158] = -112;
                    n2 = 2024018954;
                    continue block84;
                }
                case 2024018954: {
                    tn.O0OoOO0OOOOO[224] = -80;
                    tn.O0OoOO0OOOOO[20] = 41;
                    tn.O0OoOO0OOOOO[161] = 2;
                    tn.O0OoOO0OOOOO[102] = -93;
                    tn.O0OoOO0OOOOO[342] = 51;
                    tn.O0OoOO0OOOOO[324] = 103;
                    n2 = -493908445;
                    continue block84;
                }
                case -493908445: {
                    tn.O0OoOO0OOOOO[164] = 94;
                    tn.O0OoOO0OOOOO[29] = -14;
                    n2 = -2045715025;
                    continue block84;
                }
                case -2045715025: {
                    tn.O0OoOO0OOOOO[59] = 30;
                    tn.O0OoOO0OOOOO[284] = -120;
                    tn.O0OoOO0OOOOO[109] = -6;
                    tn.O0OoOO0OOOOO[241] = -77;
                    tn.O0OoOO0OOOOO[287] = 9;
                    n2 = -149103373;
                    continue block84;
                }
                case -149103373: {
                    tn.O0OoOO0OOOOO[149] = 29;
                    n2 = -1290878780;
                    continue block84;
                }
                case -1290878780: {
                    tn.O0OoOO0OOOOO[176] = -64;
                    n2 = 132504344;
                    continue block84;
                }
                case 132504344: {
                    tn.O0OoOO0OOOOO[8] = 28;
                    tn.O0OoOO0OOOOO[389] = -123;
                    tn.O0OoOO0OOOOO[68] = -76;
                    tn.O0OoOO0OOOOO[33] = -87;
                    n2 = 416202333;
                    continue block84;
                }
                case 416202333: {
                    tn.O0OoOO0OOOOO[231] = -43;
                    n2 = 810522554;
                    continue block84;
                }
                case 810522554: {
                    tn.O0OoOO0OOOOO[137] = -30;
                    n2 = 1368986515;
                    continue block84;
                }
                case 1368986515: {
                    tn.O0OoOO0OOOOO[15] = -206;
                    tn.O0OoOO0OOOOO[136] = -93;
                    tn.O0OoOO0OOOOO[122] = -60;
                    tn.O0OoOO0OOOOO[333] = -24;
                    tn.O0OoOO0OOOOO[24] = 13;
                    tn.O0OoOO0OOOOO[268] = -19;
                    tn.O0OoOO0OOOOO[106] = 118;
                    tn.O0OoOO0OOOOO[119] = 75;
                    tn.O0OoOO0OOOOO[380] = -107;
                    tn.O0OoOO0OOOOO[393] = 51;
                    tn.O0OoOO0OOOOO[142] = -76;
                    tn.O0OoOO0OOOOO[254] = 52;
                    tn.O0OoOO0OOOOO[298] = 87;
                    tn.O0OoOO0OOOOO[326] = 45;
                    tn.O0OoOO0OOOOO[117] = 127;
                    tn.O0OoOO0OOOOO[360] = -224;
                    n2 = -857024378;
                    continue block84;
                }
                case -857024378: 
            }
            tn.O0OoOO0OOOOO[263] = 125;
            tn.O0OoOO0OOOOO[250] = 54;
            tn.O0OoOO0OOOOO[183] = 2;
            tn.O0OoOO0OOOOO[218] = 105;
            tn.O0OoOO0OOOOO[173] = 54;
            n2 = 630375925;
        }
    }

    /*
     * Unable to fully structure code
     */
    public strictfp void l(dz var1_1) {
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
                                                    tn.ooo00oo0Ooo0 = var40_2;
                                                    if (1225747363 >= tn.ooo00oo0Ooo0) break block35;
                                                    var40_2 = -1150721182;
                                                    break block35;
lbl6:
                                                    // 1 sources

                                                    while (true) {
                                                        var38_3[3] = Float.valueOf(tn.aEi.bgb.pl);
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
                                                if (!tn.o0o000o0O0o0) break block36;
                                                90 + 60;
                                                break block36;
                                            }
                                            var38_3[4] = -71;
                                            var38_3[4] = (Integer)var38_3[4] + 85;
                                            var38_3[4] = (Integer)var38_3[4] ^ 15;
                                            v0 = (Integer)var38_3[4];
                                            break block37;
                                        }
                                        if (tn.o0o000o0O0o0) {
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
                                    if (!tn.aEi.bgb.aO || !aif.enoughMovementForSprinting()) ** GOTO lbl74
                                    if (!((Boolean)this.afZ.wp()).booleanValue()) break block32;
                                    var36_7 = -1529878495;
                                    break block33;
                                    break;
                                }
lbl54:
                                // 1 sources

                                while (true) {
                                    if (tn.aEi.bgb.cqP >= this.agO) {
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
                                if (!tn.o0o000o0O0o0) break block34;
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
                                            if (tn.aEi.bgb.tR == (Integer)var38_3[21]) break block39;
                                            var37_8 = -1677655356;
                                            var38_3[7] = -26;
                                            var38_3[7] = (Integer)var38_3[7] - 68;
                                            var38_3[7] = (Integer)var38_3[7] ^ -95;
                                            if (tn.aEi.bgb.tR == (Integer)var38_3[7]) break block39;
                                            var38_3[14] = 245;
                                            var38_3[14] = (Integer)var38_3[14] ^ 116;
                                            var38_3[14] = (Integer)var38_3[14] + -125;
                                            if (tn.aEi.bgb.tR == (Integer)var38_3[14]) break block39;
                                            var38_3[20] = -163;
                                            var38_3[20] = (Integer)var38_3[20] - -74;
                                            var38_3[20] = (Integer)var38_3[20] + 94;
                                            if (tn.aEi.bgb.tR != (Integer)var38_3[20]) break block40;
                                        }
                                        if (tn.aEi.aIp.cgI.QQ()) {
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
                if (!((Boolean)this.agk.wp()).booleanValue()) {
                    var38_3[0] = 2;
                    var38_3[0] = (Integer)var38_3[0] - -49;
                    var38_3[0] = (Integer)var38_3[0] - 16;
                    if (!((akd)this.afM.wp()).getName().equals((String)tn.o0Oo000O0oO[(Integer)var38_3[0]])) {
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
        ** while (!this.km().equals((Object)((String)tn.o0Oo000O0oO[((Integer)var38_3[30]).intValue()])))
lbl138:
        // 1 sources

        ** while (true)
    }

    /*
     * Unable to fully structure code
     */
    public strictfp boolean ki() {
        block14: {
            block15: {
                var12_1 = 89174431;
                tn.ooo00oo0Ooo0 = var12_1;
                if (388044798 > tn.ooo00oo0Ooo0) break block15;
                79 & 1;
                break block15;
lbl7:
                // 1 sources

                while (true) {
                    var6_2 ^= -92;
                    v0 = var6_2 ^= 29;
lbl10:
                    // 2 sources

                    return (boolean)v0;
                }
lbl12:
                // 2 sources

                while (true) {
                    var10_4 = new Object[9];
                    if (tn.aEi.bgb != null && this.isEnabled() && ((Boolean)this.afZ.wp()).booleanValue()) {
                        var10_4[8] = 84;
                        var10_4[8] = (Integer)var10_4[8] - 4;
                        var10_4[8] = (Integer)var10_4[8] ^ 93;
                        if (((String)tn.o0Oo000O0oO[(Integer)var10_4[8]]).equals(this.km()) && aif.enoughMovementForSprinting()) {
                            var7_3 = -1527062347;
                            block10: while (true) {
                                switch (var7_3) {
                                    default: {
                                        var7_3 = -1145851450;
                                        continue block10;
                                    }
                                    case -1145851450: 
                                }
                                break;
                            }
                            break block14;
                        }
                    }
                    ** GOTO lbl-1000
                    break;
                }
            }
            if (!tn.o0o000o0O0o0) ** GOTO lbl12
            59 & 1;
            ** while (true)
        }
        var10_4[0] = -49;
        var10_4[0] = (Integer)var10_4[0] ^ 72;
        var10_4[0] = (Integer)var10_4[0] + 122;
        v0 = (Integer)var10_4[0];
        ** while (true)
        {
            switch (var8_5) lbl-1000:
            // 2 sources

            {
                default: {
                    var8_5 = 1365051754;
                    continue block11;
                }
                case 1365051754: 
            }
        }
        var6_2 = -71;
        var9_6 = -774560479;
        ** while (true)
    }

    public strictfp aka a(c c2, float f2) {
        Object[] objectArray = new Object[6];
        objectArray[2] = c2.cqX + (c2.gW - c2.cqX) * (double)f2;
        objectArray[0] = c2.cqY + (c2.gX - c2.cqY) * (double)f2 + (double)c2.getEyeHeight();
        objectArray[4] = c2.cqZ + (c2.gY - c2.cqZ) * (double)f2;
        return new aka((Double)objectArray[2], (Double)objectArray[0], (Double)objectArray[4]);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public strictfp Boolean n(b b2) {
        int n2 = 184746120;
        ooo00oo0Ooo0 = -670062366;
        if (208683669 <= ooo00oo0Ooo0) {
        }
        int n3 = -771614809;
        return b2.equals(this.Yx);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public strictfp ajy a(b b2, aib aib2, float f2, float f3, float f4, int n2) {
        int n3 = 805086078;
        ooo00oo0Ooo0 = n3;
        if (916140317 <= ooo00oo0Ooo0) {
            n3 = -212548486;
        }
        Object[] objectArray = new Object[3];
        objectArray[2] = Float.valueOf(bt.bN(tn.aEi.bgb.pl));
        objectArray[1] = Float.valueOf(bt.I((float)(Double.longBitsToDouble(0x4055400000000000L) + Math.random() * Double.longBitsToDouble(4591870180066957722L)), f3, f4));
        return aiu.m(new ajy(((Float)objectArray[2]).floatValue(), ((Float)objectArray[1]).floatValue()));
    }

    @Generated
    public strictfp int kv() {
        return this.acu;
    }

    /*
     * Unable to fully structure code
     */
    public strictfp void k(dz var1_1) {
        block17: {
            var10_2 = 1756167063;
            if (490367594 < var10_2) break block17;
            throw new IllegalStateException();
lbl4:
            // 1 sources

            return;
            while (true) {
                block16: {
                    var8_4[1] = ((Number)this.ago.wp()).doubleValue();
                    var7_5 = 1171376889;
                    break block16;
lbl10:
                    // 1 sources

                    while (true) {
                        if ((Double)var8_4[1] <= Double.longBitsToDouble(9196881157595064166L ^ 4641094888374828796L)) lbl-1000:
                        // 2 sources

                        {
                            return;
                        }
lbl14:
                        // 3 sources

                        while (true) {
                            var7_5 = -714518218;
                            break block16;
                            break;
                        }
                        break;
                    }
lbl17:
                    // 1 sources

                    while (true) {
                        var1_1.setSneakSlowDownMultiplier((Double)var8_4[1]);
                        return;
                    }
                }
                switch (var7_5) {
                    default: {
                        ** continue;
                    }
                    case 2079940143: {
                        ** continue;
                    }
                    case -2109136025: {
                        ** continue;
                    }
                    ** case -714518218:
lbl29:
                    // 1 sources

                    ** continue;
                }
                break;
            }
        }
        var8_4 = new Object[6];
        this.jp = var1_1.getForward();
        this.jq = var1_1.getStrafe();
        v0 = this.agD;
        var8_4[4] = -66;
        var8_4[4] = (Integer)var8_4[4] - 59;
        var8_4[4] = (Integer)var8_4[4] - -126;
        this.agD = v0 - (Integer)var8_4[4];
        if (v0 <= 0) ** GOTO lbl46
        var6_3 = -897106248;
        block14: while (true) {
            switch (var6_3) {
                default: {
                    var1_1.setForward(0.0f);
                    var1_1.setStrafe(0.0f);
lbl46:
                    // 2 sources

                    if (((Boolean)this.agk.wp()).booleanValue()) ** continue;
                    var6_3 = 1476406167;
                    continue block14;
                }
                case 1476406167: 
            }
            break;
        }
        ** while (true)
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public strictfp float l(float f2) {
        int n2 = 1599426505;
        ooo00oo0Ooo0 = n2;
        if (2044623641 <= ooo00oo0Ooo0) {
        }
        Object[] objectArray = new Object[]{Float.valueOf(bt.bN(f2))};
        return (float)Math.round(((Float)objectArray[0]).floatValue() / 45.0f) * 45.0f;
    }

    /*
     * Handled impossible loop by duplicating code
     * Enabled aggressive block sorting
     */
    @Override
    public strictfp void onDisable() {
        int n2;
        ajy ajy2;
        int n3 = 1197237115;
        ooo00oo0Ooo0 = n3;
        if (1894936233 < ooo00oo0Ooo0) {
            n3 = 96;
        }
        Object[] objectArray = new Object[8];
        int n4 = -1757622617;
        objectArray[0] = 30;
        objectArray[0] = (Integer)objectArray[0] + -91;
        objectArray[0] = (Integer)objectArray[0] + 63;
        if (tn.aEi.bgb.aQx % (Integer)objectArray[0] != 0) {
            ajy2(tn.aEi.bgb.pl, tn.aEi.bgb.pm);
            this.h(ajy2);
            this.kj();
            n2 = 2034627133;
        } else {
            n2 = -2022705568;
        }
        while (true) {
            switch (n2) {
                case 2034627133: {
                    objectArray[2] = 65;
                    objectArray[2] = (Integer)objectArray[2] ^ 0x5A;
                    objectArray[2] = (Integer)objectArray[2] + -27;
                    tn.aEi.bgb.crh = ((Integer)objectArray[2]).intValue();
                    return;
                }
            }
            objectArray[7] = -13;
            objectArray[7] = (Integer)objectArray[7] - -4;
            objectArray[7] = (Integer)objectArray[7] + 9;
            this.agx = ((Integer)objectArray[7]).intValue();
            ajy2(tn.aEi.bgb.pl, tn.aEi.bgb.pm);
            this.h(ajy2);
            this.kj();
            n2 = 2034627133;
        }
    }

    /*
     * Unable to fully structure code
     */
    public strictfp void kn() {
        block16: {
            block19: {
                block17: {
                    if (!tn.o0o000o0O0o0) break block17;
                    88 & 1;
                    break block17;
lbl5:
                    // 1 sources

                    return;
lbl7:
                    // 1 sources

                    block9: while (true) {
                        block18: {
                            break block18;
lbl9:
                            // 1 sources

                            while (true) {
                                var11_2[1] = 129;
                                var11_2[1] = (Integer)var11_2[1] + -37;
                                var11_2[1] = (Integer)var11_2[1] - 91;
                                if (tn.aEi.bgb.Zl != (Integer)var11_2[1]) ** GOTO lbl26
                                var10_3 = -609709498;
                                ** GOTO lbl33
                                break;
                            }
lbl16:
                            // 1 sources

                            while (true) {
                                if (this.agM == tn.aEi.bgb.aQx) ** GOTO lbl26
                                var10_3 = -239555212;
                                ** GOTO lbl33
                                break;
                            }
lbl20:
                            // 2 sources

                            while (true) {
                                var11_2[4] = 8;
                                var11_2[4] = (Integer)var11_2[4] + -68;
                                var11_2[4] = (Integer)var11_2[4] - -70;
                                this.agL = (Integer)var11_2[4];
                                this.agM = tn.aEi.bgb.aQx;
lbl26:
                                // 3 sources

                                return;
                            }
                        }
                        var9_1 = 1432808353;
                        block13: while (true) {
                            switch (var9_1) {
                                case 1432808353: {
                                    ** continue;
                                }
lbl33:
                                // 2 sources

                                switch (var10_3) {
                                    default: {
                                        ** continue;
                                    }
                                    case -239555212: {
                                        ** GOTO lbl20
                                    }
                                    ** case -1883619463:
lbl39:
                                    // 1 sources

                                    ** continue;
                                }
lbl40:
                                // 2 sources

                                while (true) {
                                    var11_2 = new Object[10];
                                    var9_1 = -652960913;
                                    continue block13;
                                    break;
                                }
                                default: {
                                    if (tn.aEi.bgb != null) {
                                        var9_1 = 702369613;
                                        continue block13;
                                    }
                                    break block16;
                                }
                                case 702369613: {
                                    if (!((Boolean)this.afZ.wp()).booleanValue()) break block16;
                                    break block9;
                                }
                            }
                            break;
                        }
                        break;
                    }
                    break block19;
                }
                if (!tn.o0o000o0O0o0) ** GOTO lbl40
                ** while (true)
            }
            ** while (((Boolean)this.agf.wp()).booleanValue())
        }
        var11_2[0] = 92;
        var11_2[0] = (Integer)var11_2[0] + 16;
        var11_2[0] = (Integer)var11_2[0] - 108;
        this.agL = (Integer)var11_2[0];
        var11_2[3] = 0;
        var11_2[3] = (Integer)var11_2[3] ^ 5;
        var11_2[3] = (Integer)var11_2[3] + -6;
        this.agM = (Integer)var11_2[3];
        ** while (true)
    }

    /*
     * Unable to fully structure code
     */
    public strictfp boolean a(k var1_1) {
        block10: {
            var9_2 = 93252543;
            tn.ooo00oo0Ooo0 = var9_2;
            if (124336764 < tn.ooo00oo0Ooo0) {
                break block10;
lbl7:
                // 1 sources

                while (true) {
                    v0 = var5_3 += -92;
lbl9:
                    // 2 sources

                    return (boolean)v0;
                }
            }
        }
        var7_5 = new Object[5];
        var6_4 = -1444391228;
        block6: while (true) {
            switch (var6_4) {
                default: {
                    if (!var1_1.xX().h(new aka(Double.longBitsToDouble(-6906060850355236474L ^ 2294793188362686854L), Double.longBitsToDouble(5867547882809543943L ^ -1252643128063210233L), Double.longBitsToDouble(-7331228812551146908L ^ 2716301956112429668L))) && var1_1.ld() != null) {
                        var6_4 = 885227267;
                        continue block6;
                    }
                    ** GOTO lbl28
                }
                case 885227267: {
                    if (var1_1.ld().bP() instanceof af) {
                        var7_5[0] = 37;
                        var7_5[0] = (Integer)var7_5[0] ^ 64;
                        var7_5[0] = (Integer)var7_5[0] ^ 100;
                        v0 = (Integer)var7_5[0];
                        ** continue;
                    }
lbl28:
                    // 3 sources

                    var6_4 = -1638924819;
                    continue block6;
                }
                case -1638924819: 
            }
            break;
        }
        var5_3 = 60;
        var5_3 ^= 96;
        ** while (true)
    }

    /*
     * Enabled aggressive block sorting
     */
    public static strictfp Object[] cfr_renamed_1() {
        if (o0o000o0O0o0) {
            int n2;
            ooo00oo0Ooo0 = -666225583;
        }
        Object[] objectArray = cfr_renamed_0;
        if (cfr_renamed_0 != null) return objectArray;
        cfr_renamed_0 = new Object[4];
        objectArray = cfr_renamed_0;
        return objectArray;
    }

    public strictfp ajy a(b b2, ao ao2, float f2, boolean bl2) {
        block0: {
            if (!o0o000o0O0o0) break block0;
        }
        return this.a(b2, ao2, f2, bl2, 82.0f);
    }

    /*
     * Enabled aggressive block sorting
     */
    public strictfp void h(ajy ajy2) {
        int n2 = 614086487;
        ooo00oo0Ooo0 = n2;
        if (1489560446 <= ooo00oo0Ooo0) {
        }
        Object[] objectArray = new Object[2];
        objectArray[0] = -40;
        objectArray[0] = (Integer)objectArray[0] - 21;
        objectArray[0] = (Integer)objectArray[0] ^ 0xFFFFFFC3;
        this.agP = ((Integer)objectArray[0]).intValue();
        this.agS = ajy2.x;
        this.agT = ajy2.y;
        this.agQ = ajy2.x;
        this.agR = ajy2.y;
    }

    public strictfp float[] a(aka aka2, aka aka3) {
        Object[] objectArray = new Object[20];
        int n2 = 1628164793;
        objectArray[12] = aka3.x - aka2.x;
        int n3 = 316539368;
        objectArray[8] = aka3.y - aka2.y;
        objectArray[19] = aka3.z - aka2.z;
        objectArray[18] = Math.sqrt((Double)objectArray[12] * (Double)objectArray[12] + (Double)objectArray[19] * (Double)objectArray[19]);
        int n4 = -1817831882;
        objectArray[5] = Float.valueOf((float)Math.toDegrees(Math.atan2((Double)objectArray[19], (Double)objectArray[12])) - 90.0f);
        int n5 = 978430372;
        objectArray[9] = Float.valueOf((float)(-Math.toDegrees(Math.atan2((Double)objectArray[8], (Double)objectArray[18]))));
        objectArray[5] = Float.valueOf(bt.bN(((Float)objectArray[5]).floatValue()));
        objectArray[9] = Float.valueOf(bt.I(((Float)objectArray[9]).floatValue(), -90.0f, 90.0f));
        objectArray[4] = 95;
        objectArray[4] = (Integer)objectArray[4] ^ 0x32;
        objectArray[4] = (Integer)objectArray[4] ^ 0x6F;
        float[] fArray = new float[((Integer)objectArray[4]).intValue()];
        objectArray[17] = -194;
        objectArray[17] = (Integer)objectArray[17] + 115;
        objectArray[17] = (Integer)objectArray[17] ^ 0xFFFFFFB1;
        fArray[((Integer)objectArray[17]).intValue()] = ((Float)objectArray[5]).floatValue();
        objectArray[15] = -81;
        objectArray[15] = (Integer)objectArray[15] - 4;
        objectArray[15] = (Integer)objectArray[15] + 86;
        fArray[((Integer)objectArray[15]).intValue()] = ((Float)objectArray[9]).floatValue();
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
        if (o0o000o0O0o0) {
        }
        this.acu = n2;
        int n3 = 544068896;
    }

    /*
     * Enabled aggressive block sorting
     */
    public strictfp boolean kh() {
        int n2;
        int n3 = 1087683062;
        if (1083765470 >= n3) {
            n3 = 10;
        }
        Object[] objectArray = new Object[5];
        if (((Boolean)this.afZ.wp()).booleanValue()) {
            int n4 = 42846261;
            block3: while (true) {
                switch (n4) {
                    default: {
                        if (this.age.wB().intValue() <= 0) break block3;
                        n4 = -1251489073;
                        continue block3;
                    }
                    case -1251489073: {
                        objectArray[3] = 29;
                        objectArray[3] = (Integer)objectArray[3] + -28;
                        n2 = (Integer)objectArray[3];
                        return n2 != 0;
                    }
                }
                break;
            }
        }
        int n5 = 92;
        n5 -= 80;
        n2 = n5 += -12;
        return n2 != 0;
    }

    /*
     * Handled duff style switch with additional control
     * Unable to fully structure code
     * Enabled aggressive block sorting
     */
    public strictfp da kt() {
        block29: {
            block30: {
                var12_1 = 434887295;
                tn.ooo00oo0Ooo0 = var12_1;
                if (1426109913 < tn.ooo00oo0Ooo0) {
                }
                if (997345769 >= (var12_1 = 1420734718)) {
                }
                if (351967125 >= (var12_1 = 1373607930)) {
                    72 + 8;
                }
                if (tn.o0o000o0O0o0) {
                    75 + 12;
                }
                var10_3 = new Object[9];
                var10_3[2] = new da((double)this.Yx.wf() + Math.random(), (double)this.Yx.wg() + Math.random(), (double)this.Yx.wj() + Math.random());
                var10_3[1] = aef.c(bs.fk, tn.aEi.bfL.Hd());
                if (this.acr == null) break block29;
                switch (ub.ahg[this.acr.vb().ordinal()]) {
                    case 5: {
                        var9_2 = 750590312;
                        ** GOTO lbl42
                    }
                    case 6: {
                        cfr_temp_0 = -751130098;
                        break block30;
                    }
                    case 1: {
                        var9_2 = -137881869;
                        ** GOTO lbl42
                    }
                    case 2: {
                        cfr_temp_0 = -694300292;
                        break block30;
                    }
                    case 3: {
                        cfr_temp_0 = 725996457;
                        break block30;
                    }
                    case 4: {
                        ((da)var10_3[2]).dzY = this.Yx.wf();
                        break;
                    }
                }
                if (true) ** GOTO lbl81
                block21: while (true) {
                    var9_2 = -762217529;
                    if (true) ** GOTO lbl42
                    block22: while (true) {
                        var9_2 = 431283171;
lbl42:
                        // 4 sources

                        block23: while (true) {
                            cfr_temp_0 = 0;
lbl44:
                            // 2 sources

                            while (true) {
                                switch (cfr_temp_0 == 0 ? var9_2 : cfr_temp_0) {
                                    case -762217529: {
                                        if (((ce)var10_3[1]).dak == null || !((ce)var10_3[1]).dg().equals(this.Yx) || ((ce)var10_3[1]).dyN != this.acr.vb()) continue block22;
                                        var10_3[2] = ((ce)var10_3[1]).dak;
                                        continue block22;
                                    }
                                    case -1716841853: {
                                        continue block22;
                                    }
                                    case 750590312: {
                                        var9_2 = -836023387;
                                        continue block23;
                                    }
                                    case -836023387: {
                                        ((da)var10_3[2]).dzZ = this.Yx.wg();
                                        ** GOTO lbl81
                                    }
                                    case -751130098: {
                                        var10_3[3] = -24;
                                        var10_3[3] = (Integer)var10_3[3] + 96;
                                        var10_3[3] = (Integer)var10_3[3] + -71;
                                        ((da)var10_3[2]).dzZ = this.Yx.wg() + (Integer)var10_3[3];
                                        ** GOTO lbl81
                                    }
                                    case -137881869: {
                                        ((da)var10_3[2]).dAa = this.Yx.wj();
                                        var9_2 = 1860814722;
                                        continue block23;
                                    }
                                    case 1860814722: {
                                        ** GOTO lbl81
                                    }
                                    case -694300292: {
                                        var10_3[4] = 77;
                                        var10_3[4] = (Integer)var10_3[4] + -49;
                                        var10_3[4] = (Integer)var10_3[4] + -27;
                                        ((da)var10_3[2]).dzY = this.Yx.wf() + (Integer)var10_3[4];
                                        ** GOTO lbl81
                                    }
                                    case 725996457: {
                                        var10_3[7] = -21;
                                        var10_3[7] = (Integer)var10_3[7] + 68;
                                        var10_3[7] = (Integer)var10_3[7] + -46;
                                        ((da)var10_3[2]).dAa = this.Yx.wj() + (Integer)var10_3[7];
lbl81:
                                        // 6 sources

                                        var9_2 = 1159297005;
                                        continue block23;
                                    }
                                    case 1159297005: {
                                        if ((ce)var10_3[1] != null && ((ce)var10_3[1]).dg() != null) ** break;
                                        continue block22;
                                        continue block21;
                                    }
                                    case 431283171: {
                                        return (da)var10_3[2];
                                    }
                                }
                                break;
                            }
                            break;
                        }
                        break;
                    }
                    break;
                }
            }
            ** while (true)
        }
        return (da)var10_3[2];
    }

    /*
     * Enabled aggressive block sorting
     */
    public strictfp boolean ks() {
        b b2;
        if (o0o000o0O0o0) {
        }
        b2(tn.aEi.bgb.gW, this.Me - 1.0, tn.aEi.bgb.gY);
        return aih.o(tn.aEi.bgb.gW, this.Me - 1.0, tn.aEi.bgb.gY).a(tn.aEi.bgb.bal, b2);
    }

    public strictfp boolean kl() {
        Object[] objectArray = new Object[2];
        objectArray[1] = -188;
        objectArray[1] = (Integer)objectArray[1] - -85;
        objectArray[1] = (Integer)objectArray[1] - -127;
        return ((akd)this.afL.wp()).getName().equals((String)o0Oo000O0oO[(Integer)objectArray[1]]);
    }

    /*
     * Unable to fully structure code
     */
    public strictfp void kp() {
        block19: {
            block20: {
                var23_1 = 590599629;
                tn.ooo00oo0Ooo0 = var23_1;
                if (1026608665 < tn.ooo00oo0Ooo0) {
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
                tn.ooo00oo0Ooo0 = var23_1;
                if (1215695303 > tn.ooo00oo0Ooo0) break block20;
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
                if (!tn.aEi.bgb.aO || aif.enoughMovementForSprinting()) {
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
            ** while (!this.km().equals((Object)((String)tn.o0Oo000O0oO[((Integer)var21_7[13]).intValue()])))
lbl47:
            // 1 sources

            var17_3 = 2126044813;
            ** while (true)
        }
        var21_7[17] = Float.valueOf(tn.aEi.bgb.pl);
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
    public strictfp ajy a(b var1_1, aib var2_2, boolean var3_3, float var4_4, float var5_5, float var6_6) {
        if (tn.o0o000o0O0o0) {
            48 & 1;
        }
        if (tn.o0o000o0O0o0) {
        }
        if (1942025177 < (tn.ooo00oo0Ooo0 = (var48_18 = 2890462))) {
        }
        if (1314317721 < (tn.ooo00oo0Ooo0 = (var48_18 = 173357569))) {
            var48_18 = -1446781167;
        }
        if (1735972329 < (tn.ooo00oo0Ooo0 = (var48_18 = 1687300755))) {
            var48_18 = 12;
        }
        var46_17 = new Object[39];
        var32_7 = 180088889;
        var46_17[25] = -7868409899082659037L;
        var46_17[27] = -100621615793956434L;
        if (var2_2 == null) {
            return new ajy(var4_4, var5_5);
        }
        var33_8 = 469682884;
        var46_17[15] = var2_2.vb();
        var34_9 = 2121246203;
        var46_17[29] = Float.valueOf(tn.aEi.bgb.pl);
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
                            if ((ajy)var46_17[6] != null) ** GOTO lbl55
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
                var46_17[7] = Float.valueOf(Math.abs(bt.bN(((ajy)var46_17[6]).x - var4_4)));
                var46_17[14] = Float.valueOf(Math.abs(((ajy)var46_17[6]).y - var5_5));
                var46_17[8] = Float.valueOf(((Float)var46_17[7]).floatValue() * ((Float)var46_17[7]).floatValue() + ((Float)var46_17[14]).floatValue() * ((Float)var46_17[14]).floatValue());
                var46_17[1] = Float.valueOf(Math.abs(bt.bN(((ajy)var46_17[6]).x - this.l(((ajy)var46_17[6]).x))));
                var46_17[33] = Float.valueOf(Math.abs(bt.bN(((ajy)var46_17[6]).x - ((Float)var46_17[29]).floatValue())));
                var45_22 = -160569857;
                block41: while (true) {
                    switch (var45_22) {
                        default: {
                            var46_17[8] = Float.valueOf(((Float)var46_17[8]).floatValue() + (((Float)var46_17[1]).floatValue() * 0.001f + ((Float)var46_17[33]).floatValue() * 5.0E-4f));
                            if (((Float)var46_17[8]).floatValue() < ((Float)var46_17[0]).floatValue()) {
                                var46_17[0] = Float.valueOf(((Float)var46_17[8]).floatValue());
                                var12_16 = (ajy)var46_17[6];
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

    /*
     * Unable to fully structure code
     */
    public strictfp void j(ei var1_1) {
        if (!tn.o0o000o0O0o0) ** GOTO lbl29
        tn.ooo00oo0Ooo0 = var4_2 = 368181655;
        ** GOTO lbl29
lbl4:
        // 1 sources

        while (true) {
            if (this.acr == null || this.Yx == null) {
                return;
            }
lbl8:
            // 3 sources

            while (true) {
                continue;
                break;
            }
lbl10:
            // 1 sources

            while (true) {
                tn.aEi.bgb.cHX.aX();
                if (((Number)this.afS.wp()).floatValue() != 1.0f) {
                    tn.aEi.bfT.dzH = ((Number)this.afS.wp()).floatValue();
                }
                return;
            }
            var2_3 = -30436393;
            block10: while (true) {
                switch (var2_3) {
                    case -525987579: {
                        ** continue;
                    }
                    case -30436393: {
                        ** continue;
                    }
lbl22:
                    // 2 sources

                    while (true) {
                        if (this.e(lw.class).isEnabled()) {
                            // empty if block
                        }
                        var2_3 = 751045327;
                        continue block10;
                        break;
                    }
                    default: {
                        ** GOTO lbl33
                    }
lbl29:
                    // 2 sources

                    if (1722607584 >= (tn.ooo00oo0Ooo0 = (var4_2 = 1373363553))) ** GOTO lbl22
                    ** continue;
lbl33:
                    // 1 sources

                    this.agy = new aka(0.0, 0.0, 0.0);
                    if (this.Yw == null) ** continue;
                    case 991725846: {
                        var2_3 = 1263350367;
                        continue block10;
                    }
                    case 1263350367: 
                }
                break;
            }
            break;
        }
        ** while (true)
    }

    /*
     * Unable to fully structure code
     */
    public strictfp void c(boolean var1_1, boolean var2_2, boolean var3_3, boolean var4_4, boolean var5_5, boolean var6_6) {
        block27: {
            block28: {
                block29: {
                    if (!tn.o0o000o0O0o0) break block29;
                    break block29;
lbl5:
                    // 1 sources

                    while (true) {
                        tn.aEi.aIp.cgH.ay(Keyboard.isKeyDown(tn.aEi.aIp.cgH.cO()));
lbl7:
                        // 2 sources

                        while (true) {
                            var7_7 = 756669883;
                            ** GOTO lbl44
lbl10:
                            // 1 sources

                            while (true) {
                                if (!var4_4) ** GOTO lbl22
                                var7_7 = 1388491345;
                                ** GOTO lbl44
                                break;
                            }
                            break;
                        }
                        break;
                    }
lbl14:
                    // 2 sources

                    while (true) {
                        block26: {
                            var9_8 = 848471625;
                            if (319232599 < var9_8) ** GOTO lbl65
                            74 + 16;
                            ** GOTO lbl65
lbl20:
                            // 1 sources

                            while (true) {
                                tn.aEi.aIp.cgF.ay(Keyboard.isKeyDown(tn.aEi.aIp.cgF.cO()));
lbl22:
                                // 2 sources

                                var7_7 = 1704367968;
                                break block26;
                                break;
                            }
lbl24:
                            // 1 sources

                            while (true) {
                                if (!var5_5) ** GOTO lbl30
                                var7_7 = -1880532836;
                                break block26;
                                break;
                            }
lbl28:
                            // 1 sources

                            while (true) {
                                tn.aEi.aIp.cgE.ay(Keyboard.isKeyDown(tn.aEi.aIp.cgE.cO()));
lbl30:
                                // 3 sources

                                while (true) {
                                    var7_7 = 1728945718;
                                    break block26;
                                    break;
                                }
                                break;
                            }
lbl33:
                            // 1 sources

                            while (true) {
                                if (!var6_6) ** GOTO lbl40
lbl35:
                                // 2 sources

                                while (true) {
                                    var7_7 = -1848441417;
                                    break block26;
                                    break;
                                }
                                break;
                            }
lbl38:
                            // 1 sources

                            while (true) {
                                tn.aEi.aIp.cgG.ay(Keyboard.isKeyDown(tn.aEi.aIp.cgG.cO()));
lbl40:
                                // 2 sources

                                var7_7 = 838482191;
                                break block26;
                                break;
                            }
lbl42:
                            // 1 sources

                            return;
                        }
                        block24: while (true) {
                            switch (var7_7) {
                                case 756669883: {
                                    ** continue;
                                }
                                case 1388491345: {
                                    ** continue;
                                }
                                case 1704367968: {
                                    ** continue;
                                }
                                case -1880532836: {
                                    ** continue;
                                }
                                case 2043576800: {
                                    ** continue;
                                }
                                case 1728945718: {
                                    ** continue;
                                }
                                case 2076002111: {
                                    ** continue;
                                }
                                case -1848441417: {
                                    ** continue;
                                }
                                case 838482191: {
                                    ** continue;
                                }
lbl65:
                                // 2 sources

                                var7_7 = 214788302;
                                continue block24;
                                default: {
                                    if (var1_1) {
                                        tn.aEi.aIp.cgJ.ay(Keyboard.isKeyDown(tn.aEi.aIp.cgJ.cO()));
                                    }
                                    var7_7 = 1485247585;
                                    continue block24;
                                }
                                case 1485247585: 
                            }
                            break;
                        }
                        if (!var2_2) break block27;
                        break block28;
                        break;
                    }
                }
                if (!tn.o0o000o0O0o0) ** GOTO lbl14
                ** while (true)
            }
            tn.aEi.aIp.cgI.ay(Keyboard.isKeyDown(tn.aEi.aIp.cgI.cO()));
        }
        ** while (!var3_3)
lbl84:
        // 1 sources

        ** while (true)
    }

    /*
     * Exception decompiling
     */
    public strictfp ajy a(b var1_1, ao var2_2, float var3_3, boolean var4_4, float var5_5) {
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

    /*
     * Unable to fully structure code
     */
    public strictfp void kq() {
        block23: {
            var10_1 = 1865344710;
            tn.ooo00oo0Ooo0 = var10_1;
            if (1903851136 >= tn.ooo00oo0Ooo0) ** GOTO lbl52
            50 + 77;
            ** GOTO lbl52
lbl7:
            // 1 sources

            while (true) {
                block22: {
                    var8_4[6] = (Integer)var8_4[6] + -104;
                    if (tn.aEi.bgb.cqP > (Integer)var8_4[6] && Math.floor(tn.aEi.bgb.gX) != this.Me && !(aih.p(0.0, Double.longBitsToDouble(8840670662418140790L ^ -4233279055838409098L), 0.0) instanceof BlockAir)) lbl-1000:
                    // 2 sources

                    {
                        while (true) {
                            this.Me = Math.floor(tn.aEi.bgb.gX);
                            break block22;
                            break;
                        }
lbl13:
                        // 1 sources

                        return;
                    }
                }
                var7_5 = -581780420;
                switch (var7_5) {
                    default: {
                        ** continue;
                    }
                    ** case -581780420:
lbl21:
                    // 1 sources

                    ** continue;
                }
                break;
            }
            {
                var8_4 = new Object[7];
                var5_2 = 1967445098;
lbl24:
                // 4 sources

                block17: while (true) {
                    switch (var5_2) {
                        default: {
                            ** GOTO lbl34
                        }
lbl28:
                        // 2 sources

                        while (true) {
                            var10_1 = 1180720852;
                            if (1056363059 < var10_1) continue block16;
                            71 & 1;
                            continue block16;
                            break;
                        }
lbl34:
                        // 1 sources

                        if (tn.aEi.aIp.cgI.QQ()) {
                            var5_2 = -635330494;
                            continue block17;
                        }
                        ** GOTO lbl44
                        ** case -635330494:
lbl39:
                        // 2 sources

                        case 1984093661: {
                            var8_4[2] = this.kr();
                            if (Double.isNaN((Double)var8_4[2])) ** GOTO lbl44
                            var6_3 = 288622769;
                        }
                        this.Me = (Double)var8_4[2];
lbl44:
                        // 3 sources

                        var5_2 = -1833695730;
                        continue block17;
                        case -1833695730: {
                            if (tn.aEi.bgb.gX < this.Me) break block16;
                            var5_2 = 1094117448;
                            continue block17;
                        }
                        case 1094117448: {
                            ** GOTO lbl55
                        }
lbl52:
                        // 2 sources

                        if (213902627 > (tn.ooo00oo0Ooo0 = (var10_1 = 69891570))) ** GOTO lbl28
                        var10_1 = 1079823098;
                        ** continue;
lbl55:
                        // 1 sources

                        if (tn.aEi.bgb.aO) {
                            break block16;
                        }
                        break block23;
                    }
                    break;
                }
            }
            var5_2 = -1262421567;
            ** GOTO lbl24
            {
                case -1262421567: {
                    if (aif.isMoving()) break block23;
                    var5_2 = -443042303;
                    ** GOTO lbl24
                }
                case -443042303: {
                    var5_2 = 1531704148;
                    ** continue;
                }
                ** case 1531704148:
            }
lbl68:
            // 1 sources

            this.Me = Math.floor(tn.aEi.bgb.gX);
        }
        var8_4[6] = 72;
        var8_4[6] = (Integer)var8_4[6] ^ 34;
        ** while (true)
    }

    /*
     * Handled impossible loop by adding 'first' condition
     * Enabled aggressive block sorting
     */
    public strictfp boolean a(ao ao2) {
        int n2;
        da da2;
        if (o0o000o0O0o0) {
            throw null;
        }
        Object[] objectArray = new Object[15];
        da2(tn.aEi.bgb.ud, 0.0, tn.aEi.bgb.uf);
        objectArray[12] = da2;
        int n3 = -587950277;
        boolean bl2 = true;
        block11: while (true) {
            if (!bl2 || (bl2 = false) || !true) {
                n3 = 1577538774;
            }
            switch (n3) {
                default: {
                    if (!(((da)objectArray[12]).atE() < Double.longBitsToDouble(4547007122018943789L))) continue block11;
                    objectArray[3] = 81;
                    n2 = 1809946596;
                    break block11;
                }
                case -1015830710: {
                    continue block11;
                }
                case 1577538774: {
                    int n4;
                    objectArray[12] = ((da)objectArray[12]).atD();
                    objectArray[11] = ao2.asy();
                    objectArray[8] = new da(((db)objectArray[11]).wf(), 0.0, ((db)objectArray[11]).wj()).atD();
                    int n5 = -2045033942;
                    objectArray[14] = ((da)objectArray[12]).t((da)objectArray[8]);
                    int n6 = (Double)objectArray[14] < 0.0 ? 1480357138 : 430421643;
                    switch (n6) {
                        default: {
                            objectArray[5] = 90;
                            objectArray[5] = (Integer)objectArray[5] ^ 0x62;
                            objectArray[5] = (Integer)objectArray[5] - 55;
                            n4 = (Integer)objectArray[5];
                            return n4 != 0;
                        }
                        case 430421643: 
                    }
                    int n7 = -22;
                    n7 -= -97;
                    n4 = n7 -= 75;
                    return n4 != 0;
                }
            }
            break;
        }
        block12: while (true) {
            switch (n2) {
                default: {
                    objectArray[3] = (Integer)objectArray[3] ^ 0xFFFFFF88;
                    n2 = 1029969703;
                    continue block12;
                }
                case 1029969703: 
            }
            break;
        }
        objectArray[3] = (Integer)objectArray[3] + 39;
        return (Integer)objectArray[3] != 0;
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
                                                tn.ooo00oo0Ooo0 = var72_3;
                                                if (1900220845 >= tn.ooo00oo0Ooo0) break block63;
                                                break block63;
lbl7:
                                                // 1 sources

                                                while (true) {
                                                    var70_9[23] = -33;
                                                    var70_9[23] = (Integer)var70_9[23] + 36;
                                                    var70_9[23] = (Integer)var70_9[23] - -29;
                                                    var70_9[54] = v0 ^ ((long)v1 << (Integer)var70_9[41] ^ v0) & -1L << (Integer)var70_9[23];
                                                    if (!tn.aEi.aIp.cgF.QQ() && !tn.aEi.aIp.cgH.QQ()) {
                                                        break block59;
                                                    }
                                                    ** GOTO lbl72
                                                    break;
                                                }
                                            }
                                            if (!tn.o0o000o0O0o0) break block64;
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
                                            if (!tn.o0o000o0O0o0) break block61;
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
                                            if (!(aif.speed() < Double.longBitsToDouble(4808354887668155705L ^ 9038816865994751960L)) && tn.aEi.bgb.aO) ** GOTO lbl53
                                            var69_16 = -511490201;
                                            break block60;
                                            break;
                                        }
lbl47:
                                        // 1 sources

                                        while (true) {
                                            if (!tn.aEi.aIp.cgI.QQ()) lbl-1000:
                                            // 2 sources

                                            {
                                                while (true) {
                                                    bs.a(new ajy(this.acs - 30.0f, this.act), Double.longBitsToDouble(4556142355098279105L ^ 9159947074177768641L), (Boolean)this.afU.wp() != false ? cb.gH : cb.gG);
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
                                            if (!tn.aEi.aIp.cgI.QQ()) {
                                                bs.a(new ajy(this.acs - 45.0f, this.act), Double.longBitsToDouble(-4739976425562917176L ^ -136171706483427640L), (Boolean)this.afU.wp() != false ? cb.gH : cb.gG);
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
                                            if (!tn.aEi.aIp.cgI.QQ()) ** GOTO lbl-1000
lbl65:
                                            // 2 sources

                                            while (true) {
                                                if (this.e(mh.class).isEnabled()) lbl-1000:
                                                // 2 sources

                                                {
                                                    while (true) {
                                                        bs.a(new ajy(this.acs - 10.0f, this.act), Double.longBitsToDouble(-5588978367069799627L ^ -987425447803995339L), (Boolean)this.afU.wp() != false ? cb.gH : cb.gG);
                                                        break;
                                                    }
                                                } else if (tn.aEi.aIp.cgI.QQ()) {
                                                    bs.a(new ajy(this.acs - 10.0f, this.act), Double.longBitsToDouble(-755733887536824115L ^ -5357286806802628403L), (Boolean)this.afU.wp() != false ? cb.gH : cb.gG);
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
                                var70_9[62] = this.a(tn.aEi.bgb, tn.aEi.bfT.bWq);
                                var70_9[30] = new aka((double)var1_1.wf() + Double.longBitsToDouble(1575604834300601048L ^ 3043778312823382744L), (double)var1_1.wg() + Double.longBitsToDouble(-5666507520295144846L ^ -8161501713858399630L), (double)var1_1.wj() + Double.longBitsToDouble(-8842472957891522803L ^ -4996398876117119219L));
                                var70_9[10] = Float.valueOf(tn.aEi.bgb.pl);
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
                                tn.ooo00oo0Ooo0 = var72_3;
                                if (309779753 > tn.ooo00oo0Ooo0) ** GOTO lbl28
                                tn.ooo00oo0Ooo0 = var72_3 = 1187171543;
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
            var70_9[34] = Float.valueOf(tn.aEi.bgb.pl);
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

    /*
     * Unable to fully structure code
     */
    public strictfp boolean ko() {
        block11: {
            block12: {
                if (!tn.o0o000o0O0o0) break block12;
                tn.ooo00oo0Ooo0 = var10_1 = 1049757149;
                break block12;
lbl4:
                // 1 sources

                while (true) {
                    block13: {
                        if (this.agL <= 0) break block13;
                        var5_2 = 376227729;
                        ** GOTO lbl25
lbl8:
                        // 1 sources

                        while (true) {
                            var8_3[3] = 64;
                            var8_3[3] = (Integer)var8_3[3] - 53;
                            var6_4 = -364861282;
                            var8_3[3] = (Integer)var8_3[3] - 10;
                            v0 = (Integer)var8_3[3];
                            ** GOTO lbl24
                            break;
                        }
                    }
lbl16:
                    // 3 sources

                    while (true) {
                        block14: {
                            var5_2 = 1124591853;
                            break block14;
lbl19:
                            // 1 sources

                            while (true) {
                                var4_5 = -174;
                                var4_5 -= -114;
                                var7_6 = 774109035;
                                v0 = var4_5 ^= -60;
lbl24:
                                // 2 sources

                                return (boolean)v0;
                            }
                        }
lbl26:
                        // 2 sources

                        while (true) {
                            switch (var5_2) {
                                case 376227729: {
                                    ** continue;
                                }
                                case 1124591853: {
                                    ** continue;
                                }
                                default: {
                                    break block11;
                                }
                            }
                            break;
                        }
                        break;
                    }
                    break;
                }
            }
            var8_3 = new Object[7];
            if (!((Boolean)this.afZ.wp()).booleanValue()) ** GOTO lbl16
            var5_2 = 1000371801;
            ** while (true)
        }
        if (((Boolean)this.agf.wp()).booleanValue()) ** break;
        ** while (true)
        ** while (true)
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public strictfp void onEnable() {
        block22: {
            if (!tn.o0o000o0O0o0) break block22;
            throw null;
lbl3:
            // 1 sources

            while (true) {
                block21: {
                    block19: {
                        block20: {
                            block18: {
                                this.acs = tn.aEi.bgb.pl - 180.0f + (float)Integer.parseInt(((akd)this.ags.wp()).getName());
                                this.act = 90.0f;
                                this.agA = (float)((Math.random() - Double.longBitsToDouble(3455139364175478600L ^ 1158303554216525640L)) * (Math.random() - Double.longBitsToDouble(198351562000995084L ^ 4404713613965038348L)) * Double.longBitsToDouble(-4258292328741071112L ^ -8880111446330042632L));
                                this.agz = (float)((Math.random() - Double.longBitsToDouble(6567034498663009085L ^ 7260588841278065469L)) * (Math.random() - Double.longBitsToDouble(4734782852045103288L ^ 9103274490594484408L)) * Double.longBitsToDouble(-7856101699012994282L ^ -3252296979933504746L));
                                var20_1[11] = 8;
                                var20_1[11] = (Integer)var20_1[11] ^ 40;
                                var20_1[11] = (Integer)var20_1[11] + -32;
                                this.agK = (Integer)var20_1[11];
                                this.Me = Math.floor(tn.aEi.bgb.gX);
                                var17_2 = 1491192047;
                                break block19;
lbl15:
                                // 1 sources

                                while (true) {
                                    this.Yw = null;
                                    this.agw = null;
                                    if (!((Boolean)this.agh.wp()).booleanValue()) break block18;
                                    var17_2 = -1840967731;
                                    break block19;
                                    break;
                                }
lbl21:
                                // 2 sources

                                while (true) {
                                    var20_1[14] = -40;
                                    var20_1[14] = (Integer)var20_1[14] + 99;
                                    var20_1[14] = (Integer)var20_1[14] - 58;
                                    this.agN = ((Integer)var20_1[14]).intValue();
                                    break block20;
                                    break;
                                }
                            }
                            var20_1[3] = -120;
                            var20_1[3] = (Integer)var20_1[3] + 59;
                            var20_1[3] = (Integer)var20_1[3] + 61;
                            this.agN = ((Integer)var20_1[3]).intValue();
                        }
lbl33:
                        // 2 sources

                        while (true) {
                            var17_2 = -1082085724;
                            break block19;
                            break;
                        }
lbl36:
                        // 1 sources

                        while (true) {
                            this.agO = this.age.ww().intValue();
                            this.h(new ajy(this.acs, this.act));
                            var20_1[6] = -14;
                            var20_1[6] = (Integer)var20_1[6] - -108;
                            var20_1[6] = (Integer)var20_1[6] + -95;
                            this.agB = (Integer)var20_1[6];
                            var20_1[7] = -26;
                            var20_1[7] = (Integer)var20_1[7] + 52;
                            var20_1[7] = (Integer)var20_1[7] + -26;
                            this.agF = (Integer)var20_1[7];
                            var20_1[2] = 43;
                            var20_1[2] = (Integer)var20_1[2] + -37;
                            var20_1[2] = (Integer)var20_1[2] ^ 6;
                            this.agC = (Integer)var20_1[2];
                            var20_1[8] = -85;
                            var20_1[8] = (Integer)var20_1[8] + 81;
                            var20_1[8] = (Integer)var20_1[8] + 4;
                            tn.aEi.bgb.crh = ((Integer)var20_1[8]).intValue();
                            var18_3 = 905600113;
                            var2_4 = this.afM.wp();
                            if (var2_4 instanceof vk) {
                                var20_1[18] = (vk)var2_4;
                                ((vk)var20_1[18]).kD().onEnable();
                            }
                            var19_5 = 1934842448;
                            break block21;
                            break;
                        }
lbl62:
                        // 1 sources

                        while (true) {
                            var19_5 = -864579802;
                            break block21;
                            break;
                        }
lbl65:
                        // 1 sources

                        return;
                    }
                    switch (var17_2) {
                        default: {
                            ** continue;
                        }
                        case -1840967731: {
                            ** GOTO lbl21
                        }
                        case -530690190: {
                            ** continue;
                        }
                        case -565173801: {
                            ** continue;
                        }
                        ** case -1082085724:
lbl78:
                        // 1 sources

                        ** continue;
                    }
                }
                switch (var19_5) {
                    default: {
                        ** continue;
                    }
                    ** case -864579802:
lbl84:
                    // 1 sources

                    ** continue;
                }
                break;
            }
        }
        var20_1 = new Object[19];
        ** while (true)
    }

    /*
     * Unable to fully structure code
     */
    public strictfp float b(float var1_1, float var2_2, float var3_3) {
        block7: {
            if (tn.o0o000o0O0o0) {
                tn.ooo00oo0Ooo0 = var8_4 = 1539730996;
                break block7;
lbl4:
                // 2 sources

                return var1_1 + ((Float)var6_6[0]).floatValue();
            }
        }
        var6_6 = new Object[2];
        var6_6[0] = Float.valueOf(bt.bN(var2_2 - var1_1));
        if (!(((Float)var6_6[0]).floatValue() > var3_3)) ** GOTO lbl15
        var5_5 = -1033873516;
        block5: while (true) {
            switch (var5_5) {
                default: {
                    var6_6[0] = Float.valueOf(var3_3);
lbl15:
                    // 2 sources

                    var5_5 = 583649879;
                    continue block5;
                }
                case 583649879: {
                    if (!(((Float)var6_6[0]).floatValue() < -var3_3)) ** GOTO lbl4
                }
                case -259504728: 
            }
            break;
        }
        var6_6[0] = Float.valueOf(-var3_3);
        ** while (true)
    }

    /*
     * Enabled aggressive block sorting
     */
    public strictfp boolean L(int n2) {
        b b2;
        if (o0o000o0O0o0) {
        }
        b2(tn.aEi.bgb);
        return aih.p(this.agy.getX(), (double)(-n2) + this.agy.getY(), this.agy.getZ()).a((w)tn.aEi.bfW, b2.hr(n2));
    }

    /*
     * Unable to fully structure code
     */
    public strictfp boolean fV() {
        block12: {
            if (!tn.o0o000o0O0o0) break block12;
            8 + 80;
            break block12;
lbl5:
            // 1 sources

            while (true) {
                block11: {
                    block13: {
                        if (((Boolean)this.agk.wp()).booleanValue()) break block13;
                        var5_2 = -154982256;
                        break block11;
lbl9:
                        // 1 sources

                        while (true) {
                            var7_1[3] = -27;
                            var7_1[3] = (Integer)var7_1[3] + -99;
                            var7_1[3] = (Integer)var7_1[3] ^ -125;
                            v0 = (Integer)var7_1[3];
                            ** GOTO lbl27
                            break;
                        }
                    }
lbl16:
                    // 2 sources

                    while (true) {
                        var5_2 = 626050849;
                        break block11;
                        break;
                    }
lbl19:
                    // 1 sources

                    while (true) {
                        var5_2 = 1071610372;
                        break block11;
                        break;
                    }
lbl22:
                    // 1 sources

                    while (true) {
                        var4_3 = -122;
                        var6_4 = -1501669137;
                        var4_3 -= -27;
                        v0 = var4_3 -= -95;
lbl27:
                        // 2 sources

                        return (boolean)v0;
                    }
                }
                switch (var5_2) {
                    default: {
                        ** continue;
                    }
                    case -1516315274: {
                        ** continue;
                    }
                    case 626050849: {
                        ** continue;
                    }
                    ** case 1071610372:
lbl37:
                    // 1 sources

                    ** continue;
                }
                break;
            }
        }
        var7_1 = new Object[6];
        ** while (true)
    }

    /*
     * Enabled aggressive block sorting
     */
    public strictfp boolean K(int n2) {
        b b2;
        if (o0o000o0O0o0) {
        }
        b2(bt.aF(tn.aEi.bgb.gW), bt.aF(tn.aEi.bgb.gX) - n2, bt.aF(tn.aEi.bgb.gY));
        return tn.aEi.bgb.bal.H(new b(bt.aF(tn.aEi.bgb.gW), bt.aF(tn.aEi.bgb.gX) - n2, bt.aF(tn.aEi.bgb.gY))).df().a(tn.aEi.bgb.bal, b2);
    }

    @Generated
    public strictfp boolean kw() {
        return this.agH;
    }

    /*
     * Exception decompiling
     */
    public strictfp void ku() {
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

    public strictfp void kj() {
        Object[] objectArray = new Object[12];
        objectArray[0] = 16;
        objectArray[0] = (Integer)objectArray[0] ^ 0xFFFFFFF1;
        objectArray[0] = (Integer)objectArray[0] - -32;
        objectArray[8] = 91;
        objectArray[8] = (Integer)objectArray[8] - 58;
        objectArray[8] = (Integer)objectArray[8] + -32;
        objectArray[2] = 75;
        objectArray[2] = (Integer)objectArray[2] + -4;
        objectArray[2] = (Integer)objectArray[2] + -70;
        objectArray[7] = -96;
        objectArray[7] = (Integer)objectArray[7] - -7;
        objectArray[7] = (Integer)objectArray[7] ^ 0xFFFFFFA6;
        objectArray[5] = -103;
        objectArray[5] = (Integer)objectArray[5] + -2;
        objectArray[5] = (Integer)objectArray[5] - -106;
        objectArray[10] = -16;
        objectArray[10] = (Integer)objectArray[10] ^ 0xFFFFFFBD;
        objectArray[10] = (Integer)objectArray[10] ^ 0x4C;
        this.c((Integer)objectArray[0] != 0, (Integer)objectArray[8] != 0, (Integer)objectArray[2] != 0, (Integer)objectArray[7] != 0, (Integer)objectArray[5] != 0, (Integer)objectArray[10] != 0);
    }

    /*
     * Enabled aggressive block sorting
     */
    public static strictfp float m(float f2) {
        int n2 = 94353833;
        ooo00oo0Ooo0 = 981490046;
        if (1040293449 <= ooo00oo0Ooo0) {
        }
        if (f2 > 89.9f) {
            return 89.9f;
        }
        int n3 = 1888496139;
        switch (n3) {
            default: {
                if (!(f2 < -89.9f)) return f2;
                return -89.9f;
            }
            case 1099576420: 
        }
        return f2;
    }
}



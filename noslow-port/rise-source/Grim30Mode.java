package rise.module.impl.movement.noslow;

import hackclient.rise.aif;
import hackclient.rise.bs;
import hackclient.rise.cb;
import hackclient.rise.ea;
import hackclient.rise.en;
import hackclient.rise.es;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.item.ai;
import net.minecraft.item.bg;
import net.minecraft.item.by;
import net.minecraft.item.cp;
import net.minecraft.network.play.client.s;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.MoveInputEvent;
import rise.event.impl.PacketSendEvent;
import rise.event.impl.PreUpdateEvent;
import rise.event.impl.SlowDownEvent;
import rise.module.Mode;
import rise.module.impl.movement.NoSlow;
import rise.module.impl.movement.Speed;
import rise.util.math.Vector2f;
import rise.value.BooleanValue;

public class Grim30Mode
extends Mode<NoSlow> {
    public static Object[] o0Oo000O0oO;
    @EventLink
    public Listener<MoveInputEvent> MT;
    public static Object[] oO00O0OO0ooO;
    public int hV;
    public static Object Oo0o00000O00;
    @EventLink
    public Listener<ea> MV;
    @EventLink
    public Listener<PacketSendEvent> MS;
    public BooleanValue MP;
    @EventLink
    public Listener<es> MQ;
    public static int[] O0OoOO0OOOOO;
    public static Object[] cfr_renamed_0;
    @EventLink
    public Listener<SlowDownEvent> MW;
    @EventLink
    public Listener<PreUpdateEvent> MR;
    @EventLink
    public Listener<en> MU;
    public static Object[] cfr_renamed_1;
    public static int ooo00oo0Ooo0;
    public static boolean o0o000o0O0o0;
    public static void Oo0o00000O00() {
        int n2 = 1241795942;
        ooo00oo0Ooo0 = n2;
        if (1479747186 < ooo00oo0Ooo0) {
        }
        O0OoOO0OOOOO = new int[400];
        Grim30Mode.O0OoOO0OOOOO[183] = 52768;
        Grim30Mode.O0OoOO0OOOOO[232] = -34075;
        Grim30Mode.O0OoOO0OOOOO[325] = 4204;
        Grim30Mode.O0OoOO0OOOOO[328] = -45425;
        Grim30Mode.O0OoOO0OOOOO[217] = 59935;
        Grim30Mode.O0OoOO0OOOOO[308] = -52214;
        Grim30Mode.O0OoOO0OOOOO[213] = 42443;
        Grim30Mode.O0OoOO0OOOOO[310] = 37809;
        Grim30Mode.O0OoOO0OOOOO[124] = 19;
        Grim30Mode.O0OoOO0OOOOO[37] = -33;
        Grim30Mode.O0OoOO0OOOOO[132] = 0;
        Grim30Mode.O0OoOO0OOOOO[111] = 14;
        Grim30Mode.O0OoOO0OOOOO[374] = 5;
        Grim30Mode.O0OoOO0OOOOO[157] = 34734;
        Grim30Mode.O0OoOO0OOOOO[346] = 21653;
        Grim30Mode.O0OoOO0OOOOO[366] = 9;
        Grim30Mode.O0OoOO0OOOOO[262] = 12628;
        Grim30Mode.O0OoOO0OOOOO[399] = 28796;
        Grim30Mode.O0OoOO0OOOOO[353] = 10;
        Grim30Mode.O0OoOO0OOOOO[270] = 37191;
        Grim30Mode.O0OoOO0OOOOO[259] = 61470;
        Grim30Mode.O0OoOO0OOOOO[253] = 51083;
        Grim30Mode.O0OoOO0OOOOO[146] = 4954;
        Grim30Mode.O0OoOO0OOOOO[191] = 24143;
        Grim30Mode.O0OoOO0OOOOO[117] = 101;
        Grim30Mode.O0OoOO0OOOOO[26] = 39;
        Grim30Mode.O0OoOO0OOOOO[53] = 0;
        Grim30Mode.O0OoOO0OOOOO[295] = 58991;
        Grim30Mode.O0OoOO0OOOOO[291] = 35812;
        Grim30Mode.O0OoOO0OOOOO[154] = 62221;
        Grim30Mode.O0OoOO0OOOOO[110] = -61;
        Grim30Mode.O0OoOO0OOOOO[163] = 7587;
        Grim30Mode.O0OoOO0OOOOO[207] = 67242;
        Grim30Mode.O0OoOO0OOOOO[319] = 55446;
        Grim30Mode.O0OoOO0OOOOO[212] = 42429;
        Grim30Mode.O0OoOO0OOOOO[287] = 51785;
        Grim30Mode.O0OoOO0OOOOO[260] = -61526;
        Grim30Mode.O0OoOO0OOOOO[381] = 87;
        Grim30Mode.O0OoOO0OOOOO[131] = 1;
        Grim30Mode.O0OoOO0OOOOO[257] = 53214;
        Grim30Mode.O0OoOO0OOOOO[46] = 22;
        Grim30Mode.O0OoOO0OOOOO[105] = 14;
        Grim30Mode.O0OoOO0OOOOO[169] = -20367;
        Grim30Mode.O0OoOO0OOOOO[225] = 54368;
        Grim30Mode.O0OoOO0OOOOO[227] = 18647;
        Grim30Mode.O0OoOO0OOOOO[139] = 17849;
        Grim30Mode.O0OoOO0OOOOO[166] = 7593;
        Grim30Mode.O0OoOO0OOOOO[6] = 174;
        Grim30Mode.O0OoOO0OOOOO[327] = 45348;
        Grim30Mode.O0OoOO0OOOOO[250] = 51096;
        Grim30Mode.O0OoOO0OOOOO[150] = 51619;
        Grim30Mode.O0OoOO0OOOOO[391] = 19938;
        Grim30Mode.O0OoOO0OOOOO[301] = 39105;
        Grim30Mode.O0OoOO0OOOOO[90] = -110;
        Grim30Mode.O0OoOO0OOOOO[39] = 96;
        Grim30Mode.O0OoOO0OOOOO[322] = 4208;
        Grim30Mode.O0OoOO0OOOOO[126] = 23;
        Grim30Mode.O0OoOO0OOOOO[345] = 23251;
        Grim30Mode.O0OoOO0OOOOO[198] = 55183;
        Grim30Mode.O0OoOO0OOOOO[385] = 256;
        Grim30Mode.O0OoOO0OOOOO[236] = -32767;
        Grim30Mode.O0OoOO0OOOOO[224] = 54300;
        Grim30Mode.O0OoOO0OOOOO[188] = 37456;
        Grim30Mode.O0OoOO0OOOOO[264] = -12583;
        Grim30Mode.O0OoOO0OOOOO[17] = 125;
        Grim30Mode.O0OoOO0OOOOO[156] = 34790;
        Grim30Mode.O0OoOO0OOOOO[394] = 60870;
        Grim30Mode.O0OoOO0OOOOO[211] = 42443;
        Grim30Mode.O0OoOO0OOOOO[112] = -60;
        Grim30Mode.O0OoOO0OOOOO[103] = 135;
        Grim30Mode.O0OoOO0OOOOO[269] = 50035;
        Grim30Mode.O0OoOO0OOOOO[65] = 52824;
        Grim30Mode.O0OoOO0OOOOO[85] = 124;
        Grim30Mode.O0OoOO0OOOOO[164] = 7593;
        Grim30Mode.O0OoOO0OOOOO[349] = 25374;
        Grim30Mode.O0OoOO0OOOOO[205] = 32989;
        Grim30Mode.O0OoOO0OOOOO[245] = 37009;
        Grim30Mode.O0OoOO0OOOOO[246] = 36759;
        Grim30Mode.O0OoOO0OOOOO[174] = 61085;
        Grim30Mode.O0OoOO0OOOOO[19] = 22;
        Grim30Mode.O0OoOO0OOOOO[378] = 4;
        Grim30Mode.O0OoOO0OOOOO[382] = 13;
        Grim30Mode.O0OoOO0OOOOO[256] = -53199;
        Grim30Mode.O0OoOO0OOOOO[315] = 9797;
        Grim30Mode.O0OoOO0OOOOO[171] = 61076;
        Grim30Mode.O0OoOO0OOOOO[27] = 11;
        Grim30Mode.O0OoOO0OOOOO[54] = 0;
        Grim30Mode.O0OoOO0OOOOO[114] = -70;
        Grim30Mode.O0OoOO0OOOOO[18] = -65;
        Grim30Mode.O0OoOO0OOOOO[210] = 42462;
        Grim30Mode.O0OoOO0OOOOO[376] = 3;
        Grim30Mode.O0OoOO0OOOOO[45] = 31;
        Grim30Mode.O0OoOO0OOOOO[41] = -11;
        Grim30Mode.O0OoOO0OOOOO[337] = 58725;
        Grim30Mode.O0OoOO0OOOOO[57] = 27589;
        Grim30Mode.O0OoOO0OOOOO[52] = 2;
        Grim30Mode.O0OoOO0OOOOO[155] = 34789;
        Grim30Mode.O0OoOO0OOOOO[148] = 51619;
        Grim30Mode.O0OoOO0OOOOO[10] = 119;
        Grim30Mode.O0OoOO0OOOOO[342] = 14415;
        Grim30Mode.O0OoOO0OOOOO[199] = 0;
        Grim30Mode.O0OoOO0OOOOO[91] = -65;
        Grim30Mode.O0OoOO0OOOOO[330] = 0;
        Grim30Mode.O0OoOO0OOOOO[93] = 89;
        Grim30Mode.O0OoOO0OOOOO[323] = 4204;
        Grim30Mode.O0OoOO0OOOOO[128] = 2;
        Grim30Mode.O0OoOO0OOOOO[25] = -39;
        Grim30Mode.O0OoOO0OOOOO[263] = 12631;
        Grim30Mode.O0OoOO0OOOOO[392] = 39429;
        Grim30Mode.O0OoOO0OOOOO[395] = 5960;
        Grim30Mode.O0OoOO0OOOOO[69] = 0;
        Grim30Mode.O0OoOO0OOOOO[187] = 37471;
        Grim30Mode.O0OoOO0OOOOO[284] = -63092;
        Grim30Mode.O0OoOO0OOOOO[106] = -192;
        Grim30Mode.O0OoOO0OOOOO[336] = 33859;
        Grim30Mode.O0OoOO0OOOOO[196] = 55183;
        Grim30Mode.O0OoOO0OOOOO[312] = -37824;
        Grim30Mode.O0OoOO0OOOOO[99] = -32;
        Grim30Mode.O0OoOO0OOOOO[95] = 86;
        Grim30Mode.O0OoOO0OOOOO[104] = -89;
        Grim30Mode.O0OoOO0OOOOO[33] = -84;
        Grim30Mode.O0OoOO0OOOOO[334] = 0;
        Grim30Mode.O0OoOO0OOOOO[143] = 4954;
        Grim30Mode.O0OoOO0OOOOO[277] = 1498;
        Grim30Mode.O0OoOO0OOOOO[87] = -24;
        Grim30Mode.O0OoOO0OOOOO[396] = 47800;
        Grim30Mode.O0OoOO0OOOOO[247] = 36743;
        Grim30Mode.O0OoOO0OOOOO[176] = 28345;
        Grim30Mode.O0OoOO0OOOOO[339] = 46853;
        Grim30Mode.O0OoOO0OOOOO[240] = 26759;
        Grim30Mode.O0OoOO0OOOOO[332] = 0;
        Grim30Mode.O0OoOO0OOOOO[50] = 1;
        Grim30Mode.O0OoOO0OOOOO[178] = 28345;
        Grim30Mode.O0OoOO0OOOOO[368] = 12;
        Grim30Mode.O0OoOO0OOOOO[125] = -36;
        Grim30Mode.O0OoOO0OOOOO[364] = 1;
        Grim30Mode.O0OoOO0OOOOO[275] = 1498;
        Grim30Mode.O0OoOO0OOOOO[223] = 54368;
        Grim30Mode.O0OoOO0OOOOO[133] = 13088;
        Grim30Mode.O0OoOO0OOOOO[23] = 44;
        Grim30Mode.O0OoOO0OOOOO[175] = 28341;
        Grim30Mode.O0OoOO0OOOOO[49] = 0;
        Grim30Mode.O0OoOO0OOOOO[229] = 18647;
        Grim30Mode.O0OoOO0OOOOO[297] = 58991;
        Grim30Mode.O0OoOO0OOOOO[100] = 184;
        Grim30Mode.O0OoOO0OOOOO[384] = 12;
        Grim30Mode.O0OoOO0OOOOO[251] = 51083;
        Grim30Mode.O0OoOO0OOOOO[82] = 179;
        Grim30Mode.O0OoOO0OOOOO[181] = -34618;
        Grim30Mode.O0OoOO0OOOOO[216] = -59927;
        Grim30Mode.O0OoOO0OOOOO[142] = 17844;
        Grim30Mode.O0OoOO0OOOOO[350] = 1;
        Grim30Mode.O0OoOO0OOOOO[21] = 126;
        Grim30Mode.O0OoOO0OOOOO[73] = -15;
        Grim30Mode.O0OoOO0OOOOO[302] = 27532;
        Grim30Mode.O0OoOO0OOOOO[377] = -113;
        Grim30Mode.O0OoOO0OOOOO[333] = 1;
        Grim30Mode.O0OoOO0OOOOO[153] = -62254;
        Grim30Mode.O0OoOO0OOOOO[387] = 0;
        Grim30Mode.O0OoOO0OOOOO[75] = -93;
        Grim30Mode.O0OoOO0OOOOO[70] = 102;
        Grim30Mode.O0OoOO0OOOOO[238] = 26870;
        Grim30Mode.O0OoOO0OOOOO[283] = 63103;
        Grim30Mode.O0OoOO0OOOOO[35] = -69;
        Grim30Mode.O0OoOO0OOOOO[184] = 52773;
        Grim30Mode.O0OoOO0OOOOO[203] = 32989;
        Grim30Mode.O0OoOO0OOOOO[309] = 52148;
        Grim30Mode.O0OoOO0OOOOO[311] = 37821;
        Grim30Mode.O0OoOO0OOOOO[165] = 7663;
        Grim30Mode.O0OoOO0OOOOO[130] = 1;
        Grim30Mode.O0OoOO0OOOOO[108] = -121;
        Grim30Mode.O0OoOO0OOOOO[221] = 27112;
        Grim30Mode.O0OoOO0OOOOO[383] = -44;
        Grim30Mode.O0OoOO0OOOOO[107] = -103;
        Grim30Mode.O0OoOO0OOOOO[44] = -106;
        Grim30Mode.O0OoOO0OOOOO[192] = 24135;
        Grim30Mode.O0OoOO0OOOOO[375] = -118;
        Grim30Mode.O0OoOO0OOOOO[305] = 27528;
        Grim30Mode.O0OoOO0OOOOO[365] = -10;
        Grim30Mode.O0OoOO0OOOOO[38] = -10;
        Grim30Mode.O0OoOO0OOOOO[265] = 12631;
        Grim30Mode.O0OoOO0OOOOO[16] = 50;
        Grim30Mode.O0OoOO0OOOOO[5] = -63;
        Grim30Mode.O0OoOO0OOOOO[388] = 3;
        Grim30Mode.O0OoOO0OOOOO[294] = 58997;
        Grim30Mode.O0OoOO0OOOOO[314] = 9809;
        Grim30Mode.O0OoOO0OOOOO[380] = 0;
        Grim30Mode.O0OoOO0OOOOO[338] = 26469;
        Grim30Mode.O0OoOO0OOOOO[80] = 4;
        Grim30Mode.O0OoOO0OOOOO[14] = 27;
        Grim30Mode.O0OoOO0OOOOO[351] = 16;
        Grim30Mode.O0OoOO0OOOOO[228] = -18609;
        Grim30Mode.O0OoOO0OOOOO[222] = 54378;
        Grim30Mode.O0OoOO0OOOOO[197] = 55215;
        Grim30Mode.O0OoOO0OOOOO[363] = 28;
        Grim30Mode.O0OoOO0OOOOO[249] = 36743;
        Grim30Mode.O0OoOO0OOOOO[123] = -109;
        Grim30Mode.O0OoOO0OOOOO[20] = -45;
        Grim30Mode.O0OoOO0OOOOO[185] = -52747;
        Grim30Mode.O0OoOO0OOOOO[239] = 26857;
        Grim30Mode.O0OoOO0OOOOO[206] = 67233;
        Grim30Mode.O0OoOO0OOOOO[71] = 16;
        Grim30Mode.O0OoOO0OOOOO[361] = 86;
        Grim30Mode.O0OoOO0OOOOO[141] = 17874;
        Grim30Mode.O0OoOO0OOOOO[78] = 38;
        Grim30Mode.O0OoOO0OOOOO[118] = 28;
        Grim30Mode.O0OoOO0OOOOO[64] = 45944;
        Grim30Mode.O0OoOO0OOOOO[201] = 40802;
        Grim30Mode.O0OoOO0OOOOO[66] = 58523;
        Grim30Mode.O0OoOO0OOOOO[298] = 39119;
        Grim30Mode.O0OoOO0OOOOO[231] = 34159;
        Grim30Mode.O0OoOO0OOOOO[235] = 32659;
        Grim30Mode.O0OoOO0OOOOO[209] = 67242;
        Grim30Mode.O0OoOO0OOOOO[202] = 32981;
        Grim30Mode.O0OoOO0OOOOO[348] = 41822;
        Grim30Mode.O0OoOO0OOOOO[62] = 57171;
        Grim30Mode.O0OoOO0OOOOO[179] = 34684;
        Grim30Mode.O0OoOO0OOOOO[36] = -8;
        Grim30Mode.O0OoOO0OOOOO[72] = -86;
        Grim30Mode.O0OoOO0OOOOO[186] = 52773;
        Grim30Mode.O0OoOO0OOOOO[317] = 9797;
        Grim30Mode.O0OoOO0OOOOO[173] = -61160;
        Grim30Mode.O0OoOO0OOOOO[86] = 68;
        Grim30Mode.O0OoOO0OOOOO[244] = -37052;
        Grim30Mode.O0OoOO0OOOOO[43] = -87;
        Grim30Mode.O0OoOO0OOOOO[151] = 62223;
        Grim30Mode.O0OoOO0OOOOO[34] = -3;
        Grim30Mode.O0OoOO0OOOOO[159] = 61034;
        Grim30Mode.O0OoOO0OOOOO[116] = 52;
        Grim30Mode.O0OoOO0OOOOO[59] = 38700;
        Grim30Mode.O0OoOO0OOOOO[230] = 34153;
        Grim30Mode.O0OoOO0OOOOO[15] = 77;
        Grim30Mode.O0OoOO0OOOOO[341] = 46155;
        Grim30Mode.O0OoOO0OOOOO[3] = 83;
        Grim30Mode.O0OoOO0OOOOO[68] = 53471;
        Grim30Mode.O0OoOO0OOOOO[313] = 37821;
        Grim30Mode.O0OoOO0OOOOO[343] = 2448;
        Grim30Mode.O0OoOO0OOOOO[362] = 10;
        Grim30Mode.O0OoOO0OOOOO[97] = 105;
        Grim30Mode.O0OoOO0OOOOO[255] = 53214;
        Grim30Mode.O0OoOO0OOOOO[389] = 49616;
        Grim30Mode.O0OoOO0OOOOO[77] = 49;
        Grim30Mode.O0OoOO0OOOOO[92] = 120;
        Grim30Mode.O0OoOO0OOOOO[393] = 1062;
        Grim30Mode.O0OoOO0OOOOO[335] = 18;
        Grim30Mode.O0OoOO0OOOOO[241] = 26857;
        Grim30Mode.O0OoOO0OOOOO[136] = 34593;
        Grim30Mode.O0OoOO0OOOOO[88] = 81;
        Grim30Mode.O0OoOO0OOOOO[40] = -84;
        Grim30Mode.O0OoOO0OOOOO[234] = 32641;
        Grim30Mode.O0OoOO0OOOOO[331] = 0;
        Grim30Mode.O0OoOO0OOOOO[56] = 674;
        Grim30Mode.O0OoOO0OOOOO[22] = 81;
        Grim30Mode.O0OoOO0OOOOO[300] = -39136;
        Grim30Mode.O0OoOO0OOOOO[158] = 34790;
        Grim30Mode.O0OoOO0OOOOO[267] = 50035;
        Grim30Mode.O0OoOO0OOOOO[329] = 45348;
        Grim30Mode.O0OoOO0OOOOO[177] = -28317;
        Grim30Mode.O0OoOO0OOOOO[79] = -28;
        Grim30Mode.O0OoOO0OOOOO[161] = -61012;
        Grim30Mode.O0OoOO0OOOOO[129] = 0;
        Grim30Mode.O0OoOO0OOOOO[74] = -116;
        Grim30Mode.O0OoOO0OOOOO[274] = 1499;
        Grim30Mode.O0OoOO0OOOOO[285] = 63103;
        Grim30Mode.O0OoOO0OOOOO[149] = -51705;
        Grim30Mode.O0OoOO0OOOOO[147] = 51624;
        Grim30Mode.O0OoOO0OOOOO[172] = 61085;
        Grim30Mode.O0OoOO0OOOOO[152] = 62221;
        Grim30Mode.O0OoOO0OOOOO[180] = 34682;
        Grim30Mode.O0OoOO0OOOOO[316] = -9763;
        Grim30Mode.O0OoOO0OOOOO[272] = -37206;
        Grim30Mode.O0OoOO0OOOOO[372] = 6;
        Grim30Mode.O0OoOO0OOOOO[24] = 2;
        Grim30Mode.O0OoOO0OOOOO[373] = -18;
        Grim30Mode.O0OoOO0OOOOO[271] = 37186;
        Grim30Mode.O0OoOO0OOOOO[2] = 21;
        Grim30Mode.O0OoOO0OOOOO[306] = 52157;
        Grim30Mode.O0OoOO0OOOOO[81] = -25;
        Grim30Mode.O0OoOO0OOOOO[324] = 4194;
        Grim30Mode.O0OoOO0OOOOO[145] = -4942;
        Grim30Mode.O0OoOO0OOOOO[292] = 35769;
        Grim30Mode.O0OoOO0OOOOO[357] = -44;
        Grim30Mode.O0OoOO0OOOOO[254] = 53184;
        Grim30Mode.O0OoOO0OOOOO[326] = 45353;
        Grim30Mode.O0OoOO0OOOOO[303] = 27528;
        Grim30Mode.O0OoOO0OOOOO[122] = 19;
        Grim30Mode.O0OoOO0OOOOO[268] = -49990;
        Grim30Mode.O0OoOO0OOOOO[135] = 34592;
        Grim30Mode.O0OoOO0OOOOO[367] = 116;
        Grim30Mode.O0OoOO0OOOOO[359] = 7;
        Grim30Mode.O0OoOO0OOOOO[113] = -22;
        Grim30Mode.O0OoOO0OOOOO[96] = 118;
        Grim30Mode.O0OoOO0OOOOO[47] = 23;
        Grim30Mode.O0OoOO0OOOOO[67] = 46395;
        Grim30Mode.O0OoOO0OOOOO[218] = 27134;
        Grim30Mode.O0OoOO0OOOOO[12] = -48;
        Grim30Mode.O0OoOO0OOOOO[127] = 1;
        Grim30Mode.O0OoOO0OOOOO[352] = 7;
        Grim30Mode.O0OoOO0OOOOO[55] = 44;
        Grim30Mode.O0OoOO0OOOOO[398] = 45435;
        Grim30Mode.O0OoOO0OOOOO[354] = 8;
        Grim30Mode.O0OoOO0OOOOO[29] = 70;
        Grim30Mode.O0OoOO0OOOOO[204] = 32948;
        Grim30Mode.O0OoOO0OOOOO[243] = 37009;
        Grim30Mode.O0OoOO0OOOOO[76] = 43;
        Grim30Mode.O0OoOO0OOOOO[266] = 50033;
        Grim30Mode.O0OoOO0OOOOO[94] = 236;
        Grim30Mode.O0OoOO0OOOOO[32] = 103;
        Grim30Mode.O0OoOO0OOOOO[48] = 3;
        Grim30Mode.O0OoOO0OOOOO[30] = 110;
        Grim30Mode.O0OoOO0OOOOO[226] = 18647;
        Grim30Mode.O0OoOO0OOOOO[258] = 61449;
        Grim30Mode.O0OoOO0OOOOO[63] = 50101;
        Grim30Mode.O0OoOO0OOOOO[137] = -34668;
        Grim30Mode.O0OoOO0OOOOO[242] = 37022;
        Grim30Mode.O0OoOO0OOOOO[281] = 54331;
        Grim30Mode.O0OoOO0OOOOO[386] = 2;
        Grim30Mode.O0OoOO0OOOOO[397] = 29593;
        Grim30Mode.O0OoOO0OOOOO[189] = 37485;
        Grim30Mode.O0OoOO0OOOOO[220] = -27115;
        Grim30Mode.O0OoOO0OOOOO[170] = 20436;
        Grim30Mode.O0OoOO0OOOOO[304] = 27629;
        Grim30Mode.O0OoOO0OOOOO[9] = 30;
        Grim30Mode.O0OoOO0OOOOO[293] = 35812;
        Grim30Mode.O0OoOO0OOOOO[358] = 14;
        Grim30Mode.O0OoOO0OOOOO[289] = 51785;
        Grim30Mode.O0OoOO0OOOOO[390] = 41713;
        Grim30Mode.O0OoOO0OOOOO[237] = 32659;
        Grim30Mode.O0OoOO0OOOOO[321] = 55446;
        Grim30Mode.O0OoOO0OOOOO[290] = 35811;
        Grim30Mode.O0OoOO0OOOOO[61] = 30223;
        Grim30Mode.O0OoOO0OOOOO[195] = 55179;
        Grim30Mode.O0OoOO0OOOOO[355] = -115;
        Grim30Mode.O0OoOO0OOOOO[200] = 40770;
        Grim30Mode.O0OoOO0OOOOO[193] = 24070;
        Grim30Mode.O0OoOO0OOOOO[4] = 20;
        Grim30Mode.O0OoOO0OOOOO[144] = 4954;
        Grim30Mode.O0OoOO0OOOOO[320] = -55473;
        Grim30Mode.O0OoOO0OOOOO[252] = 51196;
        Grim30Mode.O0OoOO0OOOOO[1] = 0;
        Grim30Mode.O0OoOO0OOOOO[60] = 751;
        Grim30Mode.O0OoOO0OOOOO[307] = 52148;
        Grim30Mode.O0OoOO0OOOOO[208] = 67327;
        Grim30Mode.O0OoOO0OOOOO[84] = 94;
        Grim30Mode.O0OoOO0OOOOO[89] = -62;
        Grim30Mode.O0OoOO0OOOOO[31] = -6;
        Grim30Mode.O0OoOO0OOOOO[28] = 80;
        Grim30Mode.O0OoOO0OOOOO[318] = 55435;
        Grim30Mode.O0OoOO0OOOOO[167] = 20435;
        Grim30Mode.O0OoOO0OOOOO[344] = 44048;
        Grim30Mode.O0OoOO0OOOOO[109] = -19;
        Grim30Mode.O0OoOO0OOOOO[288] = 51807;
        Grim30Mode.O0OoOO0OOOOO[115] = -121;
        Grim30Mode.O0OoOO0OOOOO[347] = 59165;
        Grim30Mode.O0OoOO0OOOOO[102] = 94;
        Grim30Mode.O0OoOO0OOOOO[340] = 9993;
        Grim30Mode.O0OoOO0OOOOO[233] = 34159;
        Grim30Mode.O0OoOO0OOOOO[379] = 14;
        Grim30Mode.O0OoOO0OOOOO[356] = 15;
        Grim30Mode.O0OoOO0OOOOO[296] = 58956;
        Grim30Mode.O0OoOO0OOOOO[140] = 17844;
        Grim30Mode.O0OoOO0OOOOO[42] = 62;
        Grim30Mode.O0OoOO0OOOOO[371] = 127;
        Grim30Mode.O0OoOO0OOOOO[168] = 20436;
        Grim30Mode.O0OoOO0OOOOO[370] = 2;
        Grim30Mode.O0OoOO0OOOOO[138] = 34593;
        Grim30Mode.O0OoOO0OOOOO[215] = 59935;
        Grim30Mode.O0OoOO0OOOOO[13] = 22;
        Grim30Mode.O0OoOO0OOOOO[219] = 27112;
        Grim30Mode.O0OoOO0OOOOO[119] = -65;
        Grim30Mode.O0OoOO0OOOOO[282] = 63086;
        Grim30Mode.O0OoOO0OOOOO[190] = 37456;
        Grim30Mode.O0OoOO0OOOOO[299] = 39105;
        Grim30Mode.O0OoOO0OOOOO[273] = 37186;
        Grim30Mode.O0OoOO0OOOOO[7] = 70;
        Grim30Mode.O0OoOO0OOOOO[248] = 36800;
        Grim30Mode.O0OoOO0OOOOO[360] = 11;
        Grim30Mode.O0OoOO0OOOOO[51] = 0;
        Grim30Mode.O0OoOO0OOOOO[8] = 105;
        Grim30Mode.O0OoOO0OOOOO[121] = -58;
        Grim30Mode.O0OoOO0OOOOO[134] = 13104;
        Grim30Mode.O0OoOO0OOOOO[214] = 59910;
        Grim30Mode.O0OoOO0OOOOO[98] = -105;
        Grim30Mode.O0OoOO0OOOOO[101] = 74;
        Grim30Mode.O0OoOO0OOOOO[279] = 54331;
        Grim30Mode.O0OoOO0OOOOO[160] = 61028;
        Grim30Mode.O0OoOO0OOOOO[194] = 24135;
        Grim30Mode.O0OoOO0OOOOO[369] = 32;
        Grim30Mode.O0OoOO0OOOOO[280] = 54336;
        Grim30Mode.O0OoOO0OOOOO[83] = 53;
        Grim30Mode.O0OoOO0OOOOO[120] = 61;
        Grim30Mode.O0OoOO0OOOOO[58] = 23947;
        Grim30Mode.O0OoOO0OOOOO[286] = 51794;
        Grim30Mode.O0OoOO0OOOOO[11] = 105;
        Grim30Mode.O0OoOO0OOOOO[182] = 34682;
        Grim30Mode.O0OoOO0OOOOO[278] = 54307;
        Grim30Mode.O0OoOO0OOOOO[162] = 61028;
        Grim30Mode.O0OoOO0OOOOO[0] = 21;
        Grim30Mode.O0OoOO0OOOOO[276] = -1488;
        Grim30Mode.O0OoOO0OOOOO[261] = 61470;
    }
    public static strictfp Object o0Oo000O0oO(Object[] object) {
        Object object2;
        int n2 = 52053105;
        if (18765749 >= n2) {
        }
        Object[] objectArray = new Object[41];
        Integer n3 = null;
        SecretKeyFactory secretKeyFactory = null;
        Integer n4 = null;
        byte[] byArray = null;
        Integer n5 = null;
        byte[] byArray2 = null;
        char[] cArray = null;
        byte[] byArray3 = null;
        Integer n6 = null;
        String string = null;
        Integer n7 = null;
        byte[] byArray4 = null;
        char[] cArray2 = null;
        Cipher cipher = null;
        char[] cArray3 = null;
        byte[] byArray5 = null;
        byte[] byArray6 = null;
        byte[] byArray7 = null;
        byte[] byArray8 = null;
        Integer n8 = null;
        Integer n9 = null;
        byte[] byArray9 = null;
        byte[] byArray10 = null;
        PBEKeySpec pBEKeySpec = null;
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
                byArray5 = new byte[16];
                byArray5[1] = -75;
                byArray5[13] = 102;
                byArray5[0] = -24;
                byArray5[11] = -92;
                byArray5[2] = -33;
                byArray5[3] = 72;
                byArray5[14] = -56;
                byArray5[10] = 70;
                byArray5[7] = -91;
                byArray5[9] = -123;
                byArray5[12] = -38;
                byArray5[6] = -68;
                byArray5[5] = -48;
                byArray5[15] = 61;
                byArray5[8] = 65;
                byArray5[4] = 32;
                objectArray4[0] = byArray5;
            }
            byArray7 = (byte[])objectArray3[0];
            if (Oo0o00000O00 == null) {
                byArray9 = new byte[32];
                byArray9[8] = 105;
                byArray9[11] = 85;
                byArray9[21] = 118;
                byArray9[25] = -10;
                byArray9[22] = -3;
                byArray9[10] = 124;
                byArray9[0] = -104;
                byArray9[6] = -118;
                byArray9[18] = -110;
                byArray9[31] = 110;
                byArray9[15] = -43;
                byArray9[16] = 71;
                byArray9[19] = 119;
                byArray9[30] = -17;
                byArray9[23] = -76;
                byArray9[3] = -114;
                byArray9[Grim30Mode.O0OoOO0OOOOO[266] ^ 0xC373] = -55;
                byArray9[5] = -24;
                byArray9[1] = -22;
                byArray9[24] = 123;
                byArray9[17] = -13;
                byArray9[27] = 22;
                byArray9[7] = 93;
                byArray9[26] = 35;
                byArray9[14] = -31;
                byArray9[4] = 101;
                byArray9[9] = -66;
                byArray9[12] = -3;
                byArray9[20] = -104;
                byArray9[29] = -39;
                byArray9[28] = 14;
                byArray9[13] = -85;
                byArray10 = new byte[byArray7.length + byArray9.length];
                System.arraycopy(byArray7, 0, byArray10, 0, byArray7.length);
                System.arraycopy(byArray9, 0, byArray10, byArray7.length, byArray9.length);
                Object object4 = Grim30Mode.cfr_renamed_1()[1];
                if (object4 == null) {
                    cArray2 = "\u744a\u745c\u7441\u745e\u7460\u73ac\u744d\u73e7\u73ee\u73e2\u7442\u73db\u737f\u7379\u7449\u7442\u745f\u73af".toCharArray();
                    n3 = 0;
                    while (n3 < 18) {
                        n7 = cArray2[n3];
                        n7 = n7 - 33859;
                        n7 = n7 + 58725;
                        n7 = n7 + 26469;
                        n7 = n7 ^ 0xB705;
                        n7 = n7 + 9993;
                        n7 = n7 ^ 0xB44B;
                        n7 = n7 - 14415;
                        n7 = n7 + O0OoOO0OOOOO[343];
                        n7 = n7 - 44048;
                        n7 = n7 - 23251;
                        n7 = n7 + 21653;
                        n7 = n7 - 59165;
                        n7 = n7 ^ 0xA35E;
                        n7 = n7 + 25374;
                        cArray2[n3.intValue()] = (char)n7.intValue();
                        Object[] objectArray5 = objectArray;
                        n3 = n3 + 1;
                    }
                    object4 = Grim30Mode.cfr_renamed_1()[1] = new String(cArray2);
                }
                secretKeyFactory = SecretKeyFactory.getInstance((String)object4);
                byArray3 = new byte[16];
                byArray3[7] = 10;
                byArray3[8] = -115;
                byArray3[15] = -44;
                byArray3[14] = 7;
                byArray3[11] = 86;
                byArray3[10] = 28;
                byArray3[1] = -10;
                byArray3[9] = 116;
                byArray3[12] = 32;
                byArray3[2] = 127;
                byArray3[6] = -18;
                byArray3[5] = -118;
                byArray3[3] = -113;
                byArray3[4] = 14;
                byArray3[0] = 87;
                byArray3[13] = -44;
                pBEKeySpec = new PBEKeySpec(new String(byArray10, StandardCharsets.UTF_8).toCharArray(), byArray3, 12, 256);
                byArray2 = secretKeyFactory.generateSecret(pBEKeySpec).getEncoded();
                Object object5 = Grim30Mode.cfr_renamed_1()[2];
                if (object5 == null) {
                    cArray = "\u5ac3\u5adf\u5af5".toCharArray();
                    n4 = 0;
                    while (n4 < 3) {
                        n6 = cArray[n4];
                        n6 = n6 ^ 0xC1D0;
                        n6 = n6 - 41713;
                        n6 = n6 + 19938;
                        n6 = n6 ^ 0x9A05;
                        n6 = n6 + 1062;
                        n6 = n6 - 60870;
                        n6 = n6 - 5960;
                        n6 = n6 + 47800;
                        n6 = n6 - 29593;
                        n6 = n6 - 45435;
                        n6 = n6 - 28796;
                        cArray[n4.intValue()] = (char)n6.intValue();
                        Object[] objectArray6 = objectArray;
                        n4 = n4 + 1;
                    }
                    object5 = Grim30Mode.cfr_renamed_1()[2] = new String(cArray);
                }
                Oo0o00000O00 = new SecretKeySpec(byArray2, (String)object5);
            }
            byArray6 = Base64.getDecoder().decode(string);
            byArray4 = Arrays.copyOfRange(byArray6, 0, 16);
            byArray = Arrays.copyOfRange(byArray6, 16, byArray6.length);
            Object object6 = Grim30Mode.cfr_renamed_1()[3];
            if (object6 == null) {
                cArray3 = "\u8e75\u8fa9\u8e13\u8e5f\u8fa3\u8fa2\u8fa3\u8e5f\u8fa4\u8e0b\u8fa3\u8e13\u8e39\u8fa4\u7c55\u7c88\u7c88\u7cbd\u7c9e\u7cb7".toCharArray();
                n9 = 0;
                while (n9 < 20) {
                    n5 = cArray3[n9];
                    n5 = n5 + 1761;
                    n5 = n5 + 41761;
                    n5 = n5 ^ 0xF722;
                    n5 = n5 - 964;
                    n5 = n5 - 38566;
                    n5 = n5 ^ 0x3F66;
                    n5 = n5 - 44999;
                    n5 = n5 - 17415;
                    n5 = n5 ^ 0xDE90;
                    n5 = n5 + 10450;
                    n5 = n5 ^ 0x7D73;
                    n5 = n5 ^ 0x4413;
                    n5 = n5 ^ 0x3FF3;
                    n5 = n5 ^ 0x2AF5;
                    n5 = n5 + 62203;
                    n5 = n5 + 11999;
                    cArray3[n9.intValue()] = (char)n5.intValue();
                    Object[] objectArray7 = objectArray;
                    n9 = n9 + 1;
                }
                object6 = Grim30Mode.cfr_renamed_1()[3] = new String(cArray3);
            }
            cipher = Cipher.getInstance((String)object6);
            cipher.init(2, (Key)((SecretKey)Oo0o00000O00), new IvParameterSpec(byArray4));
            byArray8 = cipher.doFinal(byArray);
            object2 = Grim30Mode.oO00O0OO0ooO[n8.intValue()] = new String(byArray8, StandardCharsets.UTF_8);
        }
        return object2;
    }
    public static strictfp Object[] cfr_renamed_1() {
        ooo00oo0Ooo0 = 259267554;
        if (1088289989 <= ooo00oo0Ooo0) {
            // empty if block
        }
        Object[] objectArray = cfr_renamed_0;
        if (cfr_renamed_0 == null) {
            Object[] objectArray2 = new Object[4];
            objectArray = objectArray2;
            cfr_renamed_0 = objectArray2;
        }
        return objectArray;
    }

    /*
     * Recovered potentially malformed switches.  Disable with '--allowmalformedswitch false'
     * Unable to fully structure code
     * Enabled aggressive block sorting
     */
    static {
        block28: {
            block29: {
                var97 = 1296015307;
                if (47309319 >= var97) {
                    Grim30Mode.ooo00oo0Ooo0 = var97 = 758955325;
                }
                if (Grim30Mode.o0o000o0O0o0) {
                    4 & 1;
                    break block29;
lbl8:
                    // 1 sources

                    while (true) {
                        var95_12[83] = -60;
                        var95_12[83] = (Integer)var95_12[83] - -22;
                        var95_12[83] = (Integer)var95_12[83] - -70;
                        if ((int)((Long)var95_12[41] >>> (Integer)var95_12[83]) < (int)((Long)var95_12[50]).longValue()) {
                            var95_12[89] = -121;
                            var95_12[89] = (Integer)var95_12[89] + 52;
                            var95_12[89] = (Integer)var95_12[89] + 101;
                            var95_12[9] = 28;
                            var95_12[9] = (Integer)var95_12[9] - -65;
                            var95_12[9] = (Integer)var95_12[9] - 61;
                            ((char[])var95_12[67])[(int)(((Long)var95_12[41]).longValue() >>> ((Integer)var95_12[89]).intValue())] = ((char[])var95_12[66])[(int)((Long)var95_12[91]).longValue() + (int)((Long)var95_12[41] >>> (Integer)var95_12[9])];
                            var95_12[41] = (Long)var95_12[41] + 0x100000000L;
                            continue;
                        }
                        block19: while (true) {
                            var93_10 = 1574211638;
                            switch (var93_10) {
                                default: {
                                    continue block19;
                                }
                                case 1574211638: 
                            }
                            break;
                        }
                        var95_12[57] = -58;
                        var95_12[57] = (Integer)var95_12[57] - 19;
                        var95_12[57] = (Integer)var95_12[57] ^ -109;
                        var95_12[93] = (Long)var95_12[93] + 0x100000000L;
                        Grim30Mode.o0Oo000O0oO[(int)(((Long)var95_12[93]).longValue() >>> ((Integer)var95_12[57]).intValue())] = new String((char[])var95_12[67]);
                        v0 = (Long)var95_12[91];
                        var95_12[75] = 19;
                        var95_12[75] = (Integer)var95_12[75] - -36;
                        var95_12[75] = (Integer)var95_12[75] - 23;
                        var95_12[91] = v0 ^ ((long)((int)((Long)var95_12[91]).longValue() + (int)((Long)var95_12[50]).longValue()) ^ v0) & -1L >>> (Integer)var95_12[75];
                        var94_11 = 24687522;
                        break block28;
                        break;
                    }
lbl40:
                    // 1 sources

                    block20: while (true) {
                        var89_6 = -803393076;
lbl42:
                        // 2 sources

                        while (true) {
                            switch (var89_6) {
                                case -840675450: {
                                    continue block20;
                                }
                                default: {
                                    v1 = (Long)var95_12[91];
                                    var95_12[52] = -28;
                                    var95_12[52] = (Integer)var95_12[52] + 4;
                                    var95_12[52] = (Integer)var95_12[52] - -25;
                                    var95_12[58] = Grim30Mode.O0OoOO0OOOOO[82];
                                    var95_12[58] = (Integer)var95_12[58] - 53;
                                    var95_12[58] = (Integer)var95_12[58] - 94;
                                    var95_12[91] = v1 ^ (v1 ^ v1 + (long)((Integer)var95_12[52]).intValue()) & -1L >>> (Integer)var95_12[58];
                                    v2 = (Long)var95_12[45];
                                    var95_12[10] = 124;
                                    var95_12[10] = (Integer)var95_12[10] - 68;
                                    var95_12[10] = (Integer)var95_12[10] + -24;
                                    var95_12[45] = v2 ^ ((long)((char[])var95_12[66])[(int)((Long)var95_12[91]).longValue()] ^ v2) & -1L >>> (Integer)var95_12[10];
                                    var90_7 = 2014960312;
                                    v3 = (Long)var95_12[91];
                                    var95_12[44] = 81;
                                    var95_12[44] = (Integer)var95_12[44] ^ -62;
                                    var95_12[44] = (Integer)var95_12[44] - -110;
                                    var95_12[22] = -65;
                                    var95_12[22] = (Integer)var95_12[22] ^ 120;
                                    var95_12[22] = (Integer)var95_12[22] + 89;
                                    var95_12[91] = v3 ^ (v3 ^ v3 + (long)((Integer)var95_12[44]).intValue()) & -1L >>> (Integer)var95_12[22];
                                    var95_12[37] = 236;
                                    var95_12[37] = (Integer)var95_12[37] - 86;
                                    var95_12[37] = (Integer)var95_12[37] - 118;
                                    v4 = (Long)var95_12[84];
                                    var95_12[15] = 105;
                                    var95_12[15] = (Integer)var95_12[15] + -105;
                                    var95_12[15] = (Integer)var95_12[15] - -32;
                                    var95_12[84] = v4 ^ ((long)((char[])var95_12[66])[(int)((Long)var95_12[91]).longValue()] << (Integer)var95_12[37] ^ v4) & -1L << (Integer)var95_12[15];
                                    var95_12[0] = 184;
                                    var95_12[0] = (Integer)var95_12[0] - 74;
                                    var95_12[0] = (Integer)var95_12[0] - 94;
                                    var95_12[42] = 135;
                                    var95_12[42] = (Integer)var95_12[42] + -89;
                                    var95_12[42] = (Integer)var95_12[42] ^ 14;
                                    v5 = (Long)var95_12[50];
                                    var95_12[28] = -192;
                                    var95_12[28] = (Integer)var95_12[28] - -103;
                                    var95_12[28] = (Integer)var95_12[28] ^ -121;
                                    var95_12[50] = v5 ^ ((long)((int)((Long)var95_12[45]).longValue() << (Integer)var95_12[0] | (int)((Long)var95_12[84] >>> (Integer)var95_12[42])) ^ v5) & -1L >>> (Integer)var95_12[28];
                                    var91_8 = 560659493;
                                    var95_12[67] = new char[(int)((Long)var95_12[50]).longValue()];
                                    var92_9 = -1654178561;
                                    v6 = (Long)var95_12[41];
                                    var95_12[82] = -19;
                                    var95_12[82] = (Integer)var95_12[82] ^ -61;
                                    var95_12[82] = (Integer)var95_12[82] ^ 14;
                                    var95_12[41] = v6 ^ (0L ^ v6) & -1L << (Integer)var95_12[82];
                                    ** continue;
                                }
                                case -803393076: 
                            }
                            return;
                        }
                        break;
                    }
                }
            }
            var95_12 = new Object[95];
            Grim30Mode.Oo0o00000O00();
            var95_12[38] = 1731964747174440990L;
            var95_12[30] = -8063761849621390151L;
            var95_12[70] = -5723263246515666618L;
            var95_12[90] = -806746529030849737L;
            var95_12[13] = -4946104894781555635L;
            var84_1 = -1123722488;
            var95_12[71] = 4942465478969275989L;
            var95_12[29] = 6963692343368628969L;
            var95_12[45] = 3624242555856205452L;
            var95_12[84] = 1265735700008186782L;
            var95_12[40] = -8333504954091341569L;
            var85_2 = 705861421;
            var95_12[50] = -739884647525289648L;
            var95_12[91] = -8504275546706877640L;
            var95_12[41] = 6675688114398818329L;
            var95_12[93] = -8284855282567717353L;
            var86_3 = -183362599;
            var95_12[14] = 62;
            var95_12[14] = (Integer)var95_12[14] ^ -87;
            var95_12[14] = (Integer)var95_12[14] ^ -106;
            var87_4 = -591766393;
            block22: while (true) {
                switch (var87_4) {
                    default: {
                        Grim30Mode.o0Oo000O0oO = new Object[((Integer)var95_12[14]).intValue()];
                        var87_4 = -265578798;
                        continue block22;
                    }
                    case -265578798: 
                }
                break;
            }
            v7 = (Long)var95_12[93];
            var95_12[18] = 31;
            var95_12[18] = (Integer)var95_12[18] ^ 22;
            var95_12[18] = (Integer)var95_12[18] + 23;
            var95_12[93] = v7 ^ (0L ^ v7) & -1L << (Integer)var95_12[18];
            v8 = new Object[3];
            v8[0] = Grim30Mode.cfr_renamed_1;
            v8[1] = 0;
            v9 = Grim30Mode.cfr_renamed_1()[0];
            if (v9 == null) {
                var95_12[49] = "\u3791\u36bb\u36be\u3791\u37c2\u36ad\u379e\u36b0\u36b3\u36bd\u3790\u36b4\u36b2\u36b5\u36b8\u37c1\u36bf\u3799\u3798\u36b5\u3796\u36b3\u36ba\u36ac\u378c\u36b2\u36ae\u3791\u37c1\u3794\u36bd\u3682\u36b3\u314e\u379b\u36b9\u36b3\u36b3\u378e\u37c1\u37c1\u36af\u3787\u378a".toCharArray();
                var95_12[74] = 0;
                while ((Integer)var95_12[74] < 44) {
                    var95_12[36] = (int)((char[])var95_12[49])[(Integer)var95_12[74]];
                    var95_12[36] = (Integer)var95_12[36] ^ 674;
                    var95_12[36] = (Integer)var95_12[36] + 27589;
                    var95_12[36] = (Integer)var95_12[36] + 23947;
                    var95_12[36] = (Integer)var95_12[36] + 38700;
                    var95_12[36] = (Integer)var95_12[36] ^ 751;
                    var95_12[36] = (Integer)var95_12[36] ^ 30223;
                    var95_12[36] = (Integer)var95_12[36] + 57171;
                    var95_12[36] = (Integer)var95_12[36] - 50101;
                    var95_12[36] = (Integer)var95_12[36] ^ 45944;
                    var95_12[36] = (Integer)var95_12[36] - 52824;
                    var95_12[36] = (Integer)var95_12[36] ^ 58523;
                    var95_12[36] = (Integer)var95_12[36] ^ 46395;
                    var95_12[36] = (Integer)var95_12[36] ^ 53471;
                    ((char[])var95_12[49])[((Integer)var95_12[74]).intValue()] = (char)((Integer)var95_12[36]).intValue();
                    var95_12[74] = (Integer)var95_12[74] + 1;
                }
                v9 = Grim30Mode.cfr_renamed_1()[0] = new String((char[])var95_12[49]);
            }
            v8[2] = (String)v9;
            var95_12[66] = ((String)Grim30Mode.o0Oo000O0oO(v8)).toCharArray();
            v10 = (Long)var95_12[13];
            var95_12[94] = 102;
            var95_12[94] = (Integer)var95_12[94] ^ 16;
            var95_12[94] = (Integer)var95_12[94] + -86;
            var95_12[13] = v10 ^ (0xA00000000L ^ v10) & -1L << (Integer)var95_12[94];
            v11 = (Long)var95_12[91];
            var95_12[27] = -15;
            var95_12[27] = (Integer)var95_12[27] ^ -116;
            var95_12[27] = (Integer)var95_12[27] + -93;
            var95_12[91] = v11 ^ (0L ^ v11) & -1L >>> (Integer)var95_12[27];
        }
        var88_5 = -319654388;
        var95_12[63] = Grim30Mode.O0OoOO0OOOOO[76];
        var95_12[63] = (Integer)var95_12[63] - 49;
        var95_12[63] = (Integer)var95_12[63] + 38;
        if ((int)((Long)var95_12[91]).longValue() < (int)((Long)var95_12[13] >>> (Integer)var95_12[63])) ** break;
        ** while (true)
        var89_6 = -1786764675;
        ** while (true)
    }
    public Grim30Mode(String string, NoSlow noSlow) {
        Object[] objectArray = new Object[4];
        Integer n2 = null;
        Integer n3 = null;
        super(string, noSlow);
        n3 = 21;
        n3 = n3 ^ 0;
        n3 = n3 ^ 0x15;
        n2 = 83;
        n2 = n2 - 20;
        n2 = n2 + -63;
        this.MP = new BooleanValue((String)o0Oo000O0oO[n3], (Mode<?>)this, (Boolean)((Object)Integer.valueOf(0)));
        this.MQ = es2 -> {};
        this.MR = preUpdateEvent -> {
            block6: {
                block7: {
                    block5: {
                        ooo00oo0Ooo0 = 100692563;
                        if (349560571 < ooo00oo0Ooo0) break block5;
                        Object[] objectArray = new Object[4];
                        Integer n2 = null;
                        if (Grim30Mode.mc.thePlayer.isUsingItem() && !Grim30Mode.mc.thePlayer.aO && !Grim30Mode.mc.aIp.cgH.QQ() && !Grim30Mode.mc.aIp.cgF.QQ()) {
                            bs.a(new Vector2f(Grim30Mode.mc.thePlayer.pl + 45.0f, Grim30Mode.mc.thePlayer.pm), Double.longBitsToDouble(0x4024000000000000L), cb.gH);
                        }
                        if (Grim30Mode.mc.thePlayer.crr) {
                            aif.strafe(Double.longBitsToDouble(4603939827068310651L));
                        }
                        if (!Grim30Mode.mc.thePlayer.isUsingItem()) break block6;
                        n2 = 96;
                        n2 = n2 + -84;
                        n2 = 1;
                        if (Grim30Mode.mc.thePlayer.cqP <= 1 || Grim30Mode.mc.aIp.cgI.QQ()) break block6;
                        break block7;
                    }
                    throw null;
                }
                if (!this.e(Speed.class).isEnabled()) {
                    aif.moveFlying(Double.longBitsToDouble(4551510721646314285L));
                } else {
                    aif.moveFlying(Double.longBitsToDouble(4547007122018943789L));
                }
                if (!(Grim30Mode.mc.aIp.cgH.QQ() || Grim30Mode.mc.aIp.cgF.QQ() || Grim30Mode.mc.thePlayer.UW().bP() instanceof ai)) {
                    bs.a(new Vector2f(Grim30Mode.mc.thePlayer.pl + 45.0f, Grim30Mode.mc.thePlayer.pm), Double.longBitsToDouble(0x4024000000000000L), cb.gH);
                }
            }
        };
        this.MS = packetSendEvent -> {
            if ((Boolean)this.MP.getValue() == false) return;
            if (packetSendEvent.dq() instanceof s) {
                if (!Grim30Mode.mc.thePlayer.isUsingItem()) return;
                if (!(Grim30Mode.mc.thePlayer.UW().bP() instanceof bg) && !(Grim30Mode.mc.thePlayer.UW().bP() instanceof by)) {
                    if (!(Grim30Mode.mc.thePlayer.UW().bP() instanceof ai)) return;
                }
                packetSendEvent.cF();
                return;
            }
        };
        this.MT = moveInputEvent -> {
            Object[] objectArray = new Object[5];
            Integer n2 = null;
            Integer n3 = null;
            n2 = -84;
            n2 = n2 - -3;
            n2 = n2 ^ 0xFFFFFFBB;
            if (this.hV >= n2) {
                n3 = -8;
                n3 = 39;
                n3 = 39 + O0OoOO0OOOOO[38];
                if (this.hV > n3) {
                    // empty if block
                }
            }
        };
        this.MU = en2 -> {
            Object[] objectArray = new Object[3];
            Integer n2 = null;
            if (!Grim30Mode.mc.thePlayer.isUsingItem()) return;
            if (Grim30Mode.mc.thePlayer.cuo > 0.0f) {
                n2 = 110;
                n2 = n2 + -6;
                n2 = 1;
                Grim30Mode.mc.thePlayer.setSprinting(true);
            }
        };
        this.MV = ea2 -> {
            int n2 = 683019146;
            if (503303026 >= n2) {
                throw new LinkageError();
            }
            Object[] objectArray = new Object[6];
            Integer n3 = null;
            Integer n4 = null;
            n4 = 2;
            n4 = n4 + -39;
            n4 = n4 + 39;
            n3 = 11;
            n3 = n3 - 80;
            n3 = 1;
            if (Grim30Mode.mc.thePlayer.tR % n4 == 1 && !Grim30Mode.mc.thePlayer.aO) {
                ea2.cF();
            }
        };
        this.MW = slowDownEvent -> {
            block15: {
                block14: {
                    int n2 = 2049602707;
                    if (1784739028 >= 622074311) {
                    }
                    if (523551473 >= 622074311) {
                        throw new FileNotFoundException();
                    }
                    n2 = 479143212;
                    Object[] objectArray = new Object[15];
                    Integer n3 = null;
                    Integer n4 = null;
                    Integer n5 = null;
                    Integer n6 = null;
                    Integer n7 = null;
                    if (Grim30Mode.mc.thePlayer.isUsingItem()) {
                        n6 = 174;
                        n6 = n6 - 70;
                        n6 = n6 ^ 0x69;
                        this.hV += n6.intValue();
                        if (!this.e(Speed.class).isEnabled()) {
                            aif.moveFlying(Double.longBitsToDouble(4547007122018943789L));
                        }
                    } else {
                        n3 = 30;
                        n3 = n3 ^ 0x77;
                        n3 = n3 - 105;
                        this.hV = n3;
                    }
                    n4 = -48;
                    n4 = n4 + 22;
                    n4 = n4 + 27;
                    if (Grim30Mode.mc.thePlayer.cqP == n4) break block14;
                    n5 = 77;
                    n5 = n5 ^ 0x32;
                    n5 = n5 - 125;
                    if (Grim30Mode.mc.thePlayer.tR % n5 == 0 && !Grim30Mode.mc.thePlayer.aO) break block14;
                    n7 = -65;
                    n7 = n7 + 22;
                    n7 = n7 - -45;
                    int n8 = Grim30Mode.mc.thePlayer.cqP % n7;
                    Integer.valueOf(O0OoOO0OOOOO[21]);
                    Integer.valueOf(45);
                    Integer.valueOf(1);
                    if (n8 != 1 || !Grim30Mode.mc.thePlayer.aO) break block15;
                }
                if (((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() && Grim30Mode.mc.thePlayer.isUsingItem() && Grim30Mode.mc.thePlayer.UW().bP() instanceof bg) {
                    slowDownEvent.cF();
                }
                if (((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && Grim30Mode.mc.thePlayer.isUsingItem() && Grim30Mode.mc.thePlayer.UW().bP() instanceof by) {
                    slowDownEvent.cF();
                }
                if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && Grim30Mode.mc.thePlayer.isUsingItem() && Grim30Mode.mc.thePlayer.UW().bP() instanceof cp) {
                    slowDownEvent.cF();
                }
                if (((Boolean)((NoSlow)this.getParent()).DR.getValue()).booleanValue() && Grim30Mode.mc.thePlayer.isUsingItem() && Grim30Mode.mc.thePlayer.UW().bP() instanceof ai) {
                    slowDownEvent.cF();
                }
            }
        };
    }
}



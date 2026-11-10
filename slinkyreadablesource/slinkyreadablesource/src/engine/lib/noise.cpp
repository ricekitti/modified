// 0x97c70: 1D gradient noise (Perlin style) on a 256-entry permutation table; used by the aim assist's humanising jitter.
// Library code of the image (just past the NanoVG range); translated from the listing, not byte-matched.
extern "C" const unsigned char data_2f63c0[256];   // permutation table
extern "C" const float rdata_2b81c0, rdata_2b81c4, rdata_2b81c8, rdata_2b81cc, rdata_2b81e0;   // -1, 6, -15, 10, output scale

// gradient at lattice point with hash h: magnitude 1..8, sign from bit 3, times the distance x
static inline float grad(unsigned char h, float x) {
    float g = (float)((h & 7) + 1);
    if (h & 8) g = -g;
    return g * x;
}

extern "C" float noise_1d(float x) {
    int i = (int)x;
    if ((float)i > x) i--;              // floor
    float f = x - (float)i;
    float fade = f * f * f * ((rdata_2b81c4 * f + rdata_2b81c8) * f + rdata_2b81cc);   // 6f^5 - 15f^4 + 10f^3
    float g0 = grad(data_2f63c0[i & 255], f);
    float g1 = grad(data_2f63c0[(i + 1) & 255], f + rdata_2b81c0);
    return (fade * (g1 - g0) + g0) * rdata_2b81e0;
}

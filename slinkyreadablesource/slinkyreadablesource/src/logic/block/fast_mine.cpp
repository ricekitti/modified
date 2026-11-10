// Fast Mine - descriptor +0x48 / +0x168.
// +0x48:  cap the block-hit delay at "break_delay" and optionally count it down by one.
// +0x168: scale the mining progress so a block breaks in fewer ticks ("speed").
#include "slinky/engine.h"

extern "C" float data_2f1fd0;           // "break_delay"
extern "C" unsigned char data_2f1ff0;   // "decr_block_hit_delay"
extern "C" float data_2f2028;           // "speed"
extern "C" int data_3043c0;             // last block-hit delay
extern "C" float data_3043c4;           // last progress value
extern "C" const float rdata_154438;
extern "C" const double rdata_154440;

FN(void *, java_get_minecraft_4, (void *a, void *x));
FN(float, java_get_player_controller_mp_cur_block_damage_mp, (void *a, void *blk));
FN(int, java_get_player_controller_mp_block_hit_delay, (void *a, void *blk));
FN(void, java_set_player_controller_mp_block_hit_delay, (void *a, void *blk, int v));
FN(void, java_set_player_controller_mp_cur_block_damage_mp, (void *a, void *blk, float v));
FN(float, libc_ceilf, (float v));

extern "C" void fast_mine_part(Hook *h) {
    Ctx *c = h->ctx;
    int delay = 0;
    void *blk = java_get_minecraft_4(c->a, c->c18);
    if (blk) {
        data_3043c4 = java_get_player_controller_mp_cur_block_damage_mp(c->a, blk);
        delay = java_get_player_controller_mp_block_hit_delay(c->a, blk);
        int v;
        if ((float)delay > data_2f1fd0)
            v = (int)data_2f1fd0;
        else if (delay > 0 && data_2f1ff0 && delay == data_3043c0)
            v = delay - 1;
        else
            goto done;
        java_set_player_controller_mp_block_hit_delay(c->a, blk, v);
    }
done:
    data_3043c0 = delay;
}

extern "C" void fast_mine_part_2(Hook *h) {
    Ctx *c = h->ctx;
    void *blk = java_get_minecraft_4(c->a, c->c18);
    if (!blk)
        return;
    float prog = java_get_player_controller_mp_cur_block_damage_mp(c->a, blk);
    if (prog == 0.0f || data_3043c4 != 0.0f)
        return;
    double d = prog;
    long long hits = (long long)libc_ceilf(rdata_154438 / prog);
    double sp = data_2f2028;
    double q = rdata_154440 / sp / d;
    long long fast = (long long)libc_ceilf((float)q);
    unsigned int n = (unsigned int)((int)hits - (int)fast + 1);
    java_set_player_controller_mp_cur_block_damage_mp(c->a, blk, (float)(long long)n * prog);
}

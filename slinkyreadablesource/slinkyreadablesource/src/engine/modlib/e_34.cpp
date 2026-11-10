// 0x34000: helper block (no descriptive name recovered).
#include "slinky/engine.h"
extern "C" int libc_strcmp(const char *a, const char *b);
extern "C" unsigned char data_2fbfb0;
extern "C" int compare_height_desc(char *a, char *b) {
    float fa = *(float *)(a + 0x3c), fb = *(float *)(b + 0x3c);
    if (fb > fa) return 1;
    if (fa > fb) return -1;
    char *pb = b + 8;
    char *pa = a + 8;
    return libc_strcmp(pa, pb);
}
extern "C" unsigned char take_pending_bool() { return __atomic_exchange_n(&data_2fbfb0, 0, __ATOMIC_SEQ_CST); }

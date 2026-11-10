// 0x31000: big-endian 128-bit compare
#include "slinky/engine.h"
extern "C" int compare_u128(void *, unsigned long long *a, unsigned long long *b) {
    unsigned long long x = __builtin_bswap64(a[0]);

    unsigned long long y = __builtin_bswap64(b[0]);
    if (x == y) {
        unsigned long long a1 = a[1], b1 = b[1];

        x = __builtin_bswap64(a1);
        y = __builtin_bswap64(b1);

        if (x == y) return 0;
    }
    return x >= y ? 1 : -1;
}

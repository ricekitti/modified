// 0x3d000: comparators
#include "slinky/engine.h"
extern "C" int rdata_153ac0[];
extern "C" int compare_module_height(char *a, char *b) { return (int)(*(float *)(b + 0x48) - *(float *)(a + 0x48)); }
extern "C" int compare_key_table(short *a, short *b) { return rdata_153ac0[*a] - rdata_153ac0[*b]; }

// release the cached player records: their two global refs and five lists of global refs
#include "slinky/jids.h"
FN(void, libc_free, (void *p));
struct RefList { int pad; int n; jobject *refs; };
struct PRec { char pad[0x30]; jobject r30, r38; char pad2[0x10]; RefList lists[5]; };
extern "C" unsigned long long data_2fc910;
extern "C" PRec data_2fc920[];
extern "C" void entity_list_for_render(Jvm *j) {
    if (data_2fc910) {
        JNIEnv *env = j->env;
        for (unsigned long long i = 0; i < data_2fc910; i++) {
            PRec *p = &data_2fc920[i];
            if (p->r38) (*env)->DeleteGlobalRef(env, p->r38);
            if (p->r30) (*env)->DeleteGlobalRef(env, p->r30);
#pragma clang loop unroll(full)
            for (int k = 0; k < 5; k++) {
                RefList *l = &p->lists[k];
                if (!l->refs) continue;
                for (long long m = 0; m < l->n; m++) (*env)->DeleteGlobalRef(env, l->refs[m]);
                libc_free(l->refs);
                l->refs = 0;
            }
        }
    }
    data_2fc910 = 0;
}

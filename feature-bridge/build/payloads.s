.section .rdata,"dr"
.globl feature_loader
feature_loader:
.incbin "C:/Users/ethan/Documents/rice slinky new/modified/feature-bridge/build/classes/rice/features/RiceFeatureLoader2.class"
.balign 4
.globl feature_loader_size
feature_loader_size:
.long 2309
.globl feature_hooks
feature_hooks:
.incbin "C:/Users/ethan/Documents/rice slinky new/modified/feature-bridge/build/classes/SlinkyHooks18.class"
.balign 4
.globl feature_hooks_size
feature_hooks_size:
.long 1520
.globl feature_named
feature_named:
.incbin "C:/Users/ethan/Documents/rice slinky new/modified/feature-bridge/build/features-named.jar"
.balign 4
.globl feature_named_size
feature_named_size:
.long 7802546
.globl feature_srg
feature_srg:
.incbin "C:/Users/ethan/Documents/rice slinky new/modified/feature-bridge/build/features-srg.jar"
.balign 4
.globl feature_srg_size
feature_srg_size:
.long 9753290
.globl feature_official
feature_official:
.incbin "C:/Users/ethan/Documents/rice slinky new/modified/feature-bridge/build/features-official.jar"
.balign 4
.globl feature_official_size
feature_official_size:
.long 9131393

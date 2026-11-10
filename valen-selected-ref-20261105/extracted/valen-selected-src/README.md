# Selected Valen module sources

Included modules:

- Fireball Boost
- Bridge Egg Clutch (source class: BridgeEgg)
- Fireball Pearl Clutch
- Auto Pearl
- Auto Quick Buy

BridgeEgg.java already declares its display name as "Bridge Egg Clutch". It is one existing module. The separate block-placement Clutch.java module is excluded, along with all other unrelated module implementations.

src/main/java contains these five module implementations, the shared PearlRescueModule base, Quick Buy helpers, and available shared framework sources. MODULES.json maps display names to files. Shared sources retain references to the original client; this source selection has not rewritten those integration points or replaced missing code with stubs.

Status: recovered/decompiled source, not a compiled or tested mod. The original capture is incomplete. Missing selected-module helpers include BridgeEgg$SupportUpdate, VoidProximity, PurchaseConfirmation (and Status), and QuickBuyPresetsSetting$Purchase. The complete transitive dependency set has not been recovered. Minecraft 1.8.9 mappings, Lunar event/mixin integration, dependencies and resources also require integration. The shared ResourceRelayClient source contains a decompiler failure.

Original recovered source collection remains in ../valen-module-src. This focused folder intentionally contains no other concrete module implementations, including no separate Clutch module. Existing references to those excluded modules still need adaptation when integrating these sources into another client.

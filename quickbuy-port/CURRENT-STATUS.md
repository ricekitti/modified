# Auto Quick Buy: independent helpers ported; automatic purchases blocked

Implemented by `modified/feature-bridge/prepare_quickbuy.py`, generated into `modified/feature-bridge/build/adapted/quickbuy/`:

- BedWarsShopItem catalog, item/enchantment/potion matching, resource/pricing and inventory progress.
- Five-preset model, source quantity caps/ordering, immutable snapshots, profile JSON load/write and dirty revision.
- Reconstructed Purchase data holder, including getItemId/getCount/copy and source synthetic accessors.
- Preset editor interaction/layout logic with a small vanilla drawing adapter in `java/quickbuy/QuickBuyUi.java`; no foreign ClickGUI runtime.
- Source shop-title/category/item-slot matching. No purchase-click loop.

`tests/QuickBuyFixture.java` checks these through the feature classloader under all three mapping formats as part of SourceFeaturesJvm. Generated reference hashes are in `build/adapted/quickbuy/source-hashes.json`.

These helpers are dependencies, not a finished Auto Quick Buy feature. No native Auto Quick Buy menu registration, automatic purchases, editor-launch/key integration or host profile-file integration is claimed. The original model's foreign config invalidation is replaced with an exposed dirty revision and explicit JSON read/write APIs; connect those to host persistence when completing the blocked module.

The exact blocker remains `valen.module.impl.minigames.quickbuy.PurchaseConfirmation` and nested `Status`. AutoQuickBuy.java calls its constructor with inventory progress/current milliseconds/3000ms and polls status, waiting or timing out before another purchase. Call sites do not recover the helper's algorithm. The selected source archive lacks it; the separate recovery collection records it unrecovered and contains no helper body identified by the prior audit. No substitute algorithm has been invented.

Fresh verification is recorded in `BLOCKER-EVIDENCE.json`: selected ZIP and both captured-class JARs contain zero matching helper entries, and the supplied recovery directory contains zero matching loose files. The independent helpers passed 21 checks in each of named/SRG/official during the v1.8.15 32/32 CTest run. Their classes are included as dependencies; no native Quick Buy module is registered.

To finish faithfully, recover the original helper source/class/JAR or equivalent authoritative behavior evidence. Then port the guarded purchase/session loop, wire native editor/key/persistence controls, test confirmation/timeout/inventory changes/window changes and all mappings, and package a new versioned EXE. A separately authorized redesign would need its own explicit behavior specification and changelog.

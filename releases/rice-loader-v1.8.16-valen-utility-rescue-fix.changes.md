# v1.8.16 - Valen values, Utility categories and rescue packet fix

- Moves the newly added Auto Pearl, Fireball Pearl Clutch and Bridge Egg Clutch into Utility in both the native menu and Java module metadata.
- Matches the supplied Valen numeric reference: Auto Pearl shows Range **40 blocks**, Avoid enemies **10 blocks**, Edge margin **2 blocks**, Pre pearl timer **0.02x**, and After pearl timer **0.1x**. The screenshot is preserved in `modified/valen-settings-port/reference/valen-numeric-values.png`.
- The screenshot's 0.02x pre-pearl default explicitly overrides the recovered source's 0.1x default. This one control receives a versioned `_valen` persistence key so an older saved 0.1x default does not mask the update. Other saved settings retain their identities. Source timer bounds 0-1 and step 0.01 are retained.
- Adds the appropriate native value formats for the new modules: integer block distances, compact x multipliers, seconds, and Fireball Boost milliseconds. A 0.1x value is displayed as 0.1x rather than a unitless 0.10.
- Fixes rescue aim handoff to the actual vanilla walking packet. Previously only Scaffold had a scoped packet-angle override; the pearl/egg modules could solve an aim but reach their post-motion item-use check with camera angles. Rescue angles now have their own scoped handoff, followed by source post-motion item use and camera restoration. Scaffold retains its existing first-choice packet path and its ownership flag stays separate.
- Fixes native conditional visibility for the new rescue controls: Above void reveals In void for when prediction is off; Time additionally requires In void for. Bridge Egg's Time follows its void switch, and its timer rows hide when Use Stasis instead is on.
- Auto Quick Buy is still not registered as a finished module. Its independent helpers remain included, but the missing PurchaseConfirmation/Status state machine still blocks automatic buying.
- Launcher project version is 1.8.16; Windows file/product versions are 1.8.16.0. Previous EXEs are preserved.

Validation: 32/32 CTest. Named/SRG/official integration checks assert the five exact screenshot strings, Utility categories, timer bounds/step, the migrated key and native conditional visibility. An end-to-end Above void test rejects a supported column, arms over empty void, demonstrates that camera-only walking cannot launch an unverified pearl, then sends the rescue angles through vanilla's actual walking method, launches the pearl and restores camera/hotbar state. Existing rescue, Scaffold and other module regressions pass. The versioned EXE's decoded DLL must match the rebuilt DLL before packaging completes.

Live Lunar/BedWars gameplay remains untested. Restart Minecraft before injecting.

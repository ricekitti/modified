# v1.8.6 - Scaffold Regular / Keep Y

- Independent native Clutch-based Scaffold; original Clutch unchanged. Walk off edges without jumping.
- Only visible control: Mode (Regular, Keep Y, Keep Y (hold right-click)). Right-click mode falls back to Regular when released. All backend settings are locked and hidden.
- Regular: CPS 20, range 7, FOV 360, randomization 30%, aim 600 degrees/s, acceleration 0.500, aim strength 30%. Snap-back delay/duration both 500 ms; Keep jump direction and Disable afterwards off.
- Keep Y uses only the supplied clutch sets.txt Clutch settings. No module enabled state, bind, or other modules imported. Range 5, FOV 360, minimum height 1, CPS 20, randomization 42.9999977%, base aim 660 degrees/s, acceleration 0.6004357, strength 27.6688457%, snap-back 250/250 ms, Keep jump on, Disable afterwards off.
- Hold Space to build upward in every mode. Space permits top placement, bypasses Keep Y's sideways-only rule and jump snap-back, and uses the valid layer below the player's feet. Keep Y captures the raised level after release/landing. Midair activation uses the last grounded height.
- Fast Place pauses while Scaffold is enabled, including while moving and holding right-click. Its toggle and settings remain intact and resume when Scaffold turns off. Block clicks require a matching Scaffold placement permit, preventing held RMB from adding placements outside Scaffold's plan. Left-click and non-block item use remain available.
- Versioned EXE preserved separately; launcher and Windows resource version 1.8.6.0. Older releases preserved.

Validation: all 26 CTest tests passed, including 16 edge, 52 mode and 219 placement checks. Native JVM mapping regressions passed. Extracted embedded DLL exactly matches the rebuilt native DLL. Original Clutch hashes and DLL callback slots verified.

Live gameplay, visual appearance and server behavior have not been tested. Restart Minecraft before injecting. Use this versioned EXE; the root slinky-modified.exe may still be the running older copy if Windows locks it.

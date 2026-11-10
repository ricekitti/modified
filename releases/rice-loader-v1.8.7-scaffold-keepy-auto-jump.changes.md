# v1.8.7 - Scaffold Keep Y auto-jump

- Keep Y automatically jumps while movement keys are held and a block item is selected. Each landing starts the next jump; standing still does not auto-jump.
- Calls the native Minecraft jump method and preserves current horizontal X/Z motion, preventing placement aim from kicking the player backwards. No forced forward key or synthetic Space input.
- Automatic jumps retain the original bridge placement height, extending horizontally rather than building an upward staircase.
- Keep Y (hold right-click) only auto-jumps while physical RMB is held. Releasing RMB returns to Regular. Regular does not auto-jump.
- Physical Space retains manual upward bridging. Original Clutch and locked presets unchanged. Fast Place stays suppressed while Scaffold is enabled.
- Jump state resets on toggle/world changes; no synthetic jump key remains held.
- Separate versioned EXE with matching launcher and Windows product/file version 1.8.7.0. Older releases preserved.

Validation: 26/26 CTest tests passed. ScaffoldModesTest passed 242 checks, including repeated fixed-height jump/landing cycles, RMB gating, idle/no-block/airborne conditions, Space override and reset. Placement ownership passed 219 checks; edge behavior passed 16. Extracted embedded DLL matches the rebuilt DLL exactly. Original Clutch source hashes unchanged.

Live gameplay and server behavior have not been tested. Restart Minecraft before injecting. Use the versioned EXE; the running root slinky-modified.exe was locked by Windows during packaging.

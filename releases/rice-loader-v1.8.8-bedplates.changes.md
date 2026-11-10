# v1.8.8 - Bedplates

- Added Bedplates under Visual, adapted from the supplied BedPlates script. Disabled by default.
- Shows the block materials covering Bedwars beds as item icons with optional counts. Preserves wool colors and other supported block variants.
- Controls: Align Top Center (off), Auto Scale (on), Show Block Count (on), Scale (0.8; 0.1-1.5), Render Distance (150; 10-200 blocks), Y Offset (1; -10 to 10), Mode (Static / Toggle / Hold), and a display Keybind dropdown for Toggle/Hold.
- Uses the supplied five-layer defense sampling, 20% air/material thresholds, distance-based refresh delays and dark rounded plate/cell colors. Uncovered beds produce no plate.
- Finds beds directly from loaded chunk storage, without requiring player swings. Searches eight loaded chunks per update in rings around the player; initial discovery across the full distance can take several seconds. Does not request unloaded chunks.
- Deduplicates bed halves using their facing metadata; removes destroyed/unloaded beds and clears all caches on world changes or module toggles. World scanning stays on the Minecraft client thread.
- Draws far plates before near plates, hides clipped points and restores render state and item-renderer Z after rendering, including failures.
- Original Clutch, current Scaffold (including Keep Y auto-jump and Fast Place suppression), and previous versioned EXEs are unchanged.
- Launcher and Windows file/product versions are 1.8.8.0.

Validation: 29/29 CTest tests passed. Bedplates passed 37 checks in each of named, SRG, and official mappings, including real Minecraft chunk/block types and headless icon/count rendering with GL stack recording. Native descriptor/menu/JNI integration passed in all three mappings. Extracted embedded DLL matches the rebuilt DLL exactly.

Live gameplay, actual GPU appearance and server behavior have not been tested. Bedplates is intended for Bedwars; other modes may not have meaningful bed defenses. Restart Minecraft before injecting and use the versioned EXE. If the running root slinky-modified.exe is locked, it remains the older copy.

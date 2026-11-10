# v1.8.21 - Bedplates customize editor

- Added a Rise-style in-world Bedplates position editor (`dev.rice.bedplates.BedplatesCustomize`, a vanilla `GuiScreen`): shows an example bed with the panel above it, header "Drag the panel up/down, scroll to scale", a live `offset N  scale X.XX` readout, and Reset / Done controls.
  - Drag the panel up/down -> sets the screen-space lift (0-300).
  - Scroll -> sets the panel scale (0.25-3.00, step 0.05).
  - Reset -> restores scale 0.80 / offset 0. Done (or Esc) saves and closes.
- New native Bedplates setting **"Customize Key"** (keybind, default None) under Visual; pressing it opens the editor. Passed to Java as frame[8] during the tick event only (overwritten by camera data during render, which never reads it).
- The live overlay now honors the edited values: `screen[1] -= liftPx * guiScaleFactor` and panel scale uses the editor's scale. Both persist to `<gameDir>/config/rice-bedplates.properties` and load on first event (skipped when no data dir, e.g. headless tests, so defaults stay deterministic).
- Scope: Bedplates only. Displace stays the verbatim 2026-11-04 handoff from v1.8.20 (this build is v1.8.20 + the customize editor). The v1.8.11 16-float glGetFloat fix is retained (bedplates no longer self-disables). All other client features unchanged.
- Launcher/Windows file+product versions 1.8.21.0. All prior versioned EXEs (v1.8.0-v1.8.20) preserved.

Validation: feature payload built for named/SRG/official; **31/31 CTest passed**. Bedplates Java suite 41 checks per mapping (adds the customize scale/lift clamp checks). Native SourceFeaturesJvm updated to expect the new "Customize Key" control (8 default controls; 9 in Hold/Toggle with the display Keybind then Customize Key). `--check-runtime` decoded embedded DLL equals the freshly built DLL (c3d234d4...). Live Lunar/BedWars gameplay and the on-GPU editor render are NOT tested; bind a key under Visual > Bedplates > Customize Key and verify in-game.

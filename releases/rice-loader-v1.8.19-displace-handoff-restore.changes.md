# v1.8.19 - Displace implementation handoff restore

- Restored the 2026-11-04 "supplied manual Displace" implementation handoff as the live client build, by user request ("restore this, forget anything else").
- Source of restore: `C:\Users\ethan\Documents\ricerakak slionky\exports\Displace-implementation-handoff-2026-11-04.zip` (sha256 231657c688ef548ee7cb0a57fd9c6def9322f143de3b64b58c1ac012f87c05a5). All 12 payload files already matched the in-tree handoff sources byte-for-byte; nothing had drifted.
- This is a binary restore of the preserved, verified handoff artifacts. No rebuild, no source edits, no legit patch, no bS port.
- Displace is the pristine handoff implementation:
  - `displace/KnockbackDisplaceModule.java` sha256 e7410ba675008691418f8af3ae615f71cf38225027f9855c8dc0769ae9331d72
  - `displace/RavenBsVoidScanner.java` sha256 8f1ef6e88eff49be9fdb9a41a04ac8e276f5a508c2ea7f67a43c975d938d43e6
- EXE sha256 1670579ce8243a83d836165fd2bde3ddc72edc6b7d14ef45e391fee75b0b04d3 (byte-identical to the verified 2026-11-04 handoff EXE).
- Embedded DLL sha256 5679f4553ffd895114b888dae5de7d64784fe04eebd50829d111c1ec7b4e3622.

Scope note: this is the v1.8.0 displace-handoff client state. Features added in later versioned releases (bedplates v1.8.8/enable-fix v1.8.11, scaffold refresh v1.8.2-v1.8.7, fireball-boost v1.8.12, auto-pearl/fireball-pearl-clutch/bridge-egg v1.8.13-v1.8.15, valen utility v1.8.16, and v1.8.17-v1.8.18) are NOT in this build.

Updated copies: `slinky-modified.exe`, `modified/rice-loader.exe`, `modified/slinky.dll`.
Preserved: all prior versioned EXEs in `modified/releases/` remain untouched (v1.8.0-v1.8.18).

Verification: EXE/DLL hashes equal the verified 2026-11-04 handoff build; embedded-payload match inherited from that build's verification (identical bytes). Live Minecraft/Lunar gameplay NOT tested. Restart Minecraft before injecting.

AntiFireball is available under Combat in the modified client. It is off by default.

Mayu defaults and settings:
- Range: 5 blocks, adjustable from 3 to 8.
- FOV: 360, adjustable in whole degrees from 1 to 360.
- Rotations: on. Holding blocks, a bow, or a fireball suppresses aiming without suppressing deflection.
- Swing: on. When off, it still sends the animation packet, as Mayu does.
- Move-fix: NONE / SILENT / STRICT; default SILENT.
- Show-target: NONE / DEFAULT / HUD; default NONE.

Behavior follows the referenced module: fireballs first discovered within three
blocks remain ignored until they unload; candidates are tracked from farther
away; the nearest candidate within Range + 3 and FOV is selected; actual attacks
require Range. Allow-flying capability suppresses selection. UseEntity, digging,
and placement packets block further action until a movement packet resets the
tracker. Aiming uses collision bounds, nearest yaw, and sensitivity rounding.
World changes clear tracking. DEFAULT draws green for approaching fireballs and
red for receding ones. Target boxes expand by 0.1 and use alpha 63/255.

The original Java module is retained byte for byte at:
slinkyreadablesource/slinkyreadablesource/references/mayu/AntiFireballModule.java
Verified Git blob SHA-1: 6fa707a931d5a4412319b72eca11a3362f20b144
https://github.com/ricekitti/Mayu-src-latest/blob/main/src/main/java/myau/AntiFireballModule.java

The running implementation is a native C++ port, not byte-identical Java code.
It uses this client's event hooks, configuration widgets, and network bridge.
HUD mode uses this client's HUD color. Local attacks call Minecraft's native
EntityPlayer attack routine, including the game's own hooks, instead of Mayu's
custom PlayerUtils and KeepSprint integration. Movement correction uses the
native movement hook and respects a rotation that another module overrides.
Math uses native trigonometry rather than Minecraft's Java sine lookup table.
The module's action tracker starts fresh when enabled. Missing mappings or a
failed game query safely disable the module instead of using guessed JNI IDs.
Minecraft 1.8.9 obfuscated mappings come from the Forge-hosted MCP SRG archive:
https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/1.8.9/mcp-1.8.9-srg.zip

AntiFireballCore and AntiFireballNative test geometry, target eligibility,
close-spawn exclusion, tracking, packet suppression/reset, rotation and swing
settings, movement correction, target rendering, world transitions, and unload
cleanup. All seven tests, including existing client/launcher regressions, pass.
Live Minecraft gameplay has not been verified.

The regular build and authentication sources are unchanged.

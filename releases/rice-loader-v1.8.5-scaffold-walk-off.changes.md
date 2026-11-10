# v1.8.5 — Scaffold activates when walking off edges — 2026-11-04

With Scaffold enabled, walk toward and across an open edge. Jumping is not
required. Original Clutch is unchanged.

- Replace Scaffold's airborne-only and minimum-drop activation gates with open-foot-column edge detection.
- Prepare placement up to one movement tick ahead, with a bounded prediction distance.
- Keep the platform's side available during the first falling frame using ceil(feetY)-1 for the search height.
- Immediately align silent aim at an actual nonascending walk-off edge so normal aim smoothing does not delay the catch. Existing aim smoothing still applies during approach and ascent.
- Remove the two fall-only controls (minimum height and require air) from Scaffold, including their profile registration, so old profiles cannot reintroduce those activation gates.
- Retain Scaffold's native Clutch target-point calculation, click callback, rotation callback, independent state and remaining controls.
- Keep supported ground idle. Failed world queries do not request edge activation.
- Keep the original Clutch module, its 18 controls and all four source files unchanged.
- Keep the plain native Scaffold path; the earlier Mayu feature set stays inactive.
- Set executable/product version to 1.8.5.0. Preserve previous versioned EXEs.

Validation: all 24 CTest checks passed, including 16 new edge/support/height
checks. The edge cases cover grounded approach, walking off without a jump,
all four directions, diagonal and negative coordinates, an initial gravity
drop, existing support and prediction bounds. Source checks verify the actual
Scaffold callback uses this probe and immediate edge alignment, that the
fall-only gates are gone, and that the click/aim-point/rotation callbacks match
the prior native copy. Original Clutch source hashes match their pre-update
values. Existing native JVM checks pass under named, SRG and official mappings.
The final EXE's extracted DLL matches the rebuilt DLL by SHA-256.

Restart Minecraft before injecting. Live Minecraft/Lunar gameplay and server
behavior have not been tested.

Historical implementation, superseded 2026-11-04. See DISPLACE-HANDOFF-STATUS.md
for the active supplied manual Displace replacement.

# OG Slinky Displace with optional Raven bS Void

Current loader: rice-loader-void-direction.exe. Restart Minecraft first.
Select Direction -> Void in Knockback Displace. The original five directions remain available.

The hash-locked native original Slinky Displace source is still the attack state
machine. The extension wraps its attack callback, GUI and setting registration;
all event priorities and other callbacks remain intact. It appends Void to the original Direction selector,
Void scan radius and one-block-wall option while retaining every original GUI
control, persistence key, cooldown value, scale and milliseconds format.

Only Raven's production Void scanner is compiled into the Java feature bridge.
It is extracted from the preserved user-provided Displace.java by
prepare_raven_void.py. The imported Displace Java module remains absent and
unregistered; there is no separate attack/timing/packet backend.

Void mode supplies an exact yaw only during an OG activation. Existing OG gates,
weapon/sprint checks, cooldown counters, rotation priority, packet hiding, attack
cancellation, next-tick restore and attack replay run through the original native
functions. A source-specific compiler alias routes the rotation emission through
a scoped yaw override, leaving the original source hash unchanged. The original
Direction value and last-strafe state are restored after every call. No path
means no displacement. Default Slinky mode delegates unchanged.

Tests: OgDisplaceVoidTest links the actual compiled OG logic object. It verifies
default jitter, Void yaw, queued aim reconciliation, original cooldown boundaries,
Timer Range/weapon conditions, no-path behavior, hidden packets and real late-tick
restoration. SourceFeaturesJvmTest verifies native scanner service/parity in named,
SRG and official mappings. See workspace .build-tools/slinky-bs-void-verification.json.
Live gameplay/server behavior has not been re-tested.

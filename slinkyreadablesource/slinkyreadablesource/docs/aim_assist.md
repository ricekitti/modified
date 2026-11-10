# Aim Assist — extracted implementation

Aim Assist is the reference example for the logic layer.  Its original
implementation is in `src/logic/combat/aim_assist.cpp` (5,024 bytes, six
functions), extracted byte-for-byte from `slinky2.decrypted.img`.

## Functions

| function | RVA | descriptor | what it does |
|---|---:|---|---|
| `logic_2b200` | `0x2b200` | `+0x58` | main tick: checks enabled state, acquires the local player/world, evaluates the selected mode and calls the rotation/target routines |
| `logic_2b5b0` | `0x2b5b0` | `+0x98` | disable/reset path: clears the module's runtime state and calls the engine cleanup callback |
| `logic_2b610` | `0x2b610` | — | small state helper: sets the module's active flags |
| `logic_2b630` | `0x2b630` | — | condition/target pass: checks the configured requirements (target, line of sight, weapon, etc.) and iterates candidate entities |
| `logic_2bf40` | `0x2bf40` | — | rotation math: clamps yaw/pitch, applies smoothing, prediction and randomization, then applies the rotation |
| `logic_2c3b0` | `0x2c3b0` | — | target selection/validation: range/FOV checks, visibility, invisibility, hitbox correction and target sorting |

The body of each function in the source file is the exact original
instruction stream.  The disassembly above each block shows the same code with
RIP-relative targets resolved, so the actual aim logic (target acquisition,
angle clamping, interpolation, FOV/range checks, prediction) is visible rather
than hidden behind an opaque stub.

In total the six functions contain 1,137 instructions and 101 calls.  Every
direct call targets another function in the client `.text` (the only indirect
call is the engine cleanup callback in the disable path); none of the aim logic
is stubbed out.

## How it is wired

The module descriptor for Aim Assist is at `0x2e8cc0`:

- `+0x00` → `"Aim Assist"`
- `+0x08` → description
- `+0x10` → category `0` (combat)
- `+0x58` → `0x2b200` (tick)
- `+0x98` → `0x2b5b0` (disable)

The settings block at `0x2e8eb8` starts with the three callbacks used by the
settings layer (`cb_2bae0`, `reg_2bd00`, `aux_2bf00`) followed by the config
objects.  The logic functions read those config objects directly, including the
current-value fields at `+0x18` (bools) and `+0x30` (numbers/enums).

## Runtime requirement

The extracted code is the real client implementation, not a placeholder.  It
expects the original runtime context: the module manager calls `descriptor
+0x58` with the module state, and the code uses the world/player pointers in
that state plus the client helper functions in `.text`.  The Aim Assist block
therefore behaves exactly like the original inside the original client image.

This repository is a source reconstruction/verification tree, not a runnable
Minecraft client.  To execute the block outside the verifier, the host must
provide the same engine/module-manager context (or the logic has to be ported
to a high-level API against that context).

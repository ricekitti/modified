# Start-up, hooks and shutdown

How the client gets from "DLL loaded into the JVM" to "game calls into the modules", and back. Most of the original
start-up is protector-virtualised, so it could not be translated; it is written as new code
(`src/engine/protected/init.cpp`, `src/platform/win/entry.cpp`) from what the surrounding readable code expects.
Facts below come from the image unless marked **inferred**. The licence check is not part of this client.

## What was found where

| Piece | Function | State |
|---|---|---|
| Class-hook installer | `fn_21270` | **real code**, was missed by the first gap scan. Rewritten readably (tables are data), checked against the original by running both (`tools/emu_install.py`) |
| Class-hook remover | `fn_22840` | real code, translated exactly |
| `ClassFileLoadHook` + class rewriter | `fn_225f0`, `fn_20330`, `fn_22a60`, ... | real code, translated |
| `SlinkyChannelHandler.onPacket` native | `fn_7360` | real code (also missed by the scan), translated |
| Key handlers for profile / module binds | `fn_7100`, `fn_71f0`, `fn_7250` | real code (missed), translated |
| `JNI_GetCreatedJavaVMs` lookup | `fn_ad80` | real code (missed), translated |
| Hooks on/off | `fn_6e10` | virtualised -> `init.cpp` (**inferred**: GetEnv JVMTI, install or remove, log failure) |
| Start-up thread body | inside `fn_1000` | virtualised -> `slinky_setup` in `init.cpp` (**inferred** order) |
| First-frame GUI setup | inside `fn_7410` | virtualised -> `gui_init` in `protected/hooks.cpp` (**inferred**) |
| Global teardown | DLL detach | virtualised -> `slinky_teardown` (**inferred**) |

The first gap scan only accepted a function that follows `0xcc/0x90/0xc3`, so functions after multi-byte NOP padding
or directly after a tail `jmp` were skipped; 33 real functions (7 KB) were found by fixing that
(`tools/extract_engine.py`). 43 protector stubs remain; `fn_129b0` is still an empty placeholder (it has no callers).

## Entry (Windows)

`DllMain` (`src/platform/win/entry.cpp`) starts a thread on `DLL_PROCESS_ATTACH`; that thread runs `slinky_setup`.
On failure it prints `Initialization failed: <code>` and starts the unload thread (`fn_4bd30`).

## `slinky_setup` (init.cpp)

1. `fn_ad80`: JavaVM from `jvm.dll` -> `data_2f6ee0`; attach the thread.
2. JNI ID table: `calloc(JIds)` -> `data_2f6ef0`, `fn_18aa0` (game class loader, mapping detection, every class/method/field
   ID). Mapping: `ids->variant` 0 = 1.7.10, 1 = 1.8.9; `ids->names` 0 = obfuscated, 1 = MCP, 2 = SRG.
3. Module manager: `fn_28000(data_2f6f08, fn_7080)` builds the lists from the static module list (66 descriptors);
   `fn_7080` is the key-bind setter it stores.
4. State the frame and input code read: key table `data_2f7218` (one byte per key), key map `data_2f7230`
   (profile binds, handler `fn_7100`, items are strings freed with `free`), key map `data_2f7238` (module binds,
   handler `fn_71f0`, several modules per key), profile list `data_2f7240` (`fn_ead0` reads `~/.slinky/profiles`).
5. Chat commands `fn_f1f0`, GLFW entry points `fn_ef80` (absent with LWJGL 2, not an error).
6. Saved settings `~/.slinky/settings` (**inferred**, mirror of `fn_bbd0`): per-module state for global modules
   through `fn_cd20`, `profile_binds` through `fn_7250`.
7. `data_304ba8 = fn_8bd0`: the input handler the window hook (`fn_4bda0`, installed lazily every frame) calls.
8. Java side: `fn_d280` defines `SlinkyChannelHandler` (class bytes in the image) and registers `onPacket = fn_7360`.
9. `fn_21270`: define `SlinkyHooks13`, register its 23 natives, add the `can_retransform_classes` capability, set the
   `ClassFileLoadHook` callback, enable the event, build the hook records for the detected mapping, retransform the 9
   classes, disable the event again. Errors are `12000 + jvmtiError`.

The hooks go last: from the first retransformation on, the game calls the natives.

## First frame (`fn_7410`, hooks.cpp)

The first `getMouseOver` runs `gui_init` once, on the render thread: GL entry points
(`slinky_load_gl` + `wglGetProcAddress`), NanoVG context (`nvgCreateGL3`), window object `data_2f7280` with the two
embedded fonts (Inter Medium = slot 0, Inter Bold = slot 1). NanoVG flags are **inferred** (antialias + stencil strokes).

## The class hooks

`SlinkyHooks13` (class file in the image, 1196 bytes) declares 23 `public static native` methods; their bodies are
`fn_7410 .. fn_8a20` (`kNatives` in `src/engine/jni/hook_tables.inc`, same order as the class file). The game classes are
not detoured: through JVMTI `RetransformClasses` the `ClassFileLoadHook` (`fn_225f0`) rewrites matching call and field
instructions into `invokestatic SlinkyHooks13.<native>`, so the receiver becomes the first `Object` argument.

What is rewritten is data: for each mapping (1.7.10 / 1.8.9 x obfuscated / MCP / SRG) the 9 classes
(Minecraft, EntityRenderer, RenderManager, RendererLivingEntity, RenderPlayer, NetHandlerPlayClient, WorldClient, Entity,
EntityLivingBase), per class the natives it may call and per method the instructions to replace
(`src/engine/jni/hook_tables.inc`, generated by `tools/gen_hooks.py` from an emulated run of the original installer,
`tools/dump_hooks.py`). 16 method patterns, 23 sites per mapping.

Rewritten call sites, by native: `getMouseOver` (EntityRenderer.getMouseOver in updateCameraAndRender),
`updateDisplay` (Display.update), `clickMouse`/`rightClickMouse`/`updateEntities`/`updateController` (inside Minecraft.runTick
and the input tick), `postWorldClear_1_7/_1_8` (glClear / GlStateManager.clear), `orientCamera`, `updateActiveRenderInfo`,
`isDisplayActivePreRender`, `setRenderX/Y` (RenderManager fields), `canRenderName` (any method of RendererLivingEntity),
`renderPlayer`, `unloadChunk`, `setBlock_1_7/_1_8`, `invalidateChunk`, `shouldClipLedge` (Entity.isSneaking), `moveFlying`,
`sendPacket_1_7/_1_8`.

**Verification:** `tools/emu_install.py` loads the built `slinky.dll`, runs `fn_21270` under Unicorn for all six mappings and
compares the records it builds with the original's: identical (9 classes, 16 patterns, 23 sites, same 23 natives).

## Packets

`SlinkyChannelHandler` (netty `ChannelDuplexHandler`, class bytes in the image) is put in the connection's pipeline as
`slinky_handler` (`fn_d390`, every frame). It queues packets and calls `onPacket(Object packet, boolean outbound,
long time, int id)` = `fn_7360`: under the lock `data_2f7258`, and unless shutting down (`data_2f7259`) it dispatches module
event 2 (inbound) or 3 (outbound) and returns the handlers' verdict.

## Shutdown

`fn_76f0` (the `updateDisplay` hook) unloads when the GUI's confirmation was given: saves settings, notifies and disables the
modules, removes the netty handler, `fn_6e10(env, 0)` (hooks off), `fn_4bcd0` (window hook off), then `fn_4bd30` starts a
thread that waits one second, runs `slinky_teardown` and calls `FreeLibraryAndExitThread`.
`slinky_teardown` (**inferred**) drops the netty class and the cached class references and frees the manager, both key maps,
the profile list, the chat command table, the profile name, the mesh cache, the key table and the ID table.
GL objects (NanoVG context, framebuffer) are not destroyed: that needs the render thread's context.

## Not verified / open

* The whole start-up order and the object sizes (`KeyMap` 0x30, key table 0x100, profile list 0x20) are inferred from their readers;
  nothing has run inside a game yet.
* `fn_129b0` (decrypts a short string, only reached through `fn_32450`, which nothing calls): empty placeholder.
* The NanoVG creation flags, JVMTI version (1.2) and the settings loader are inferred.
* Linux: no platform layer yet (JVMTI agent entry, input hook, file paths, GL loader).

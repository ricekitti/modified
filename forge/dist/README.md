# Rice Modified — experimental Forge 1.8.9 port

This is a standalone Java Forge mod built from the existing Rice Java framework and the modified client's preserved Java feature sources. It loads at game startup through Forge, without the Windows DLL or a native injector. The same production JAR targets Windows and macOS, including Intel and Apple Silicon when the host Minecraft/Lunar installation supports that Mac.

This is an initial port, not verified feature parity with the native modified executable. Live Lunar startup, gameplay, rendering, and Mac compatibility have not been tested. License activation now runs inside the game using the existing KeyAuth application; the companion input sounds are not included. A valid customer-license login still needs end-to-end testing.

## Install

1. Close Minecraft. Select Minecraft **1.8.9 with Forge** in your launcher. For Lunar, use its Forge profile and its custom-mods facility.
2. On macOS, add `rice-mac-1.8.9.jar` through Lunar's **1.8.9 → gear beside Launch → Mods/Forge Mods** screen. On Windows, add `rice-modified-forge-1.8.9.jar`. These are byte-for-byte identical verified builds. Do not add both.
3. Launch the profile and enter your license key in the Rice activation screen. Modules remain off until the server verifies the license. Keys are masked and are not saved to disk.
4. After activation, press **Right Shift** to open the menu. You can also enter `!gui` in chat to open it, then select another GUI binding. If activation was dismissed, Right Shift reopens it.

On Windows, `RiceModified-Forge-Setup.exe` installs exactly the same JAR into a mods folder you choose. It does not install Forge or launch/inject Minecraft. The default folder is ordinary Minecraft's mods folder; select Lunar's folder when using Lunar. An EXE cannot run on macOS; Mac users install the JAR from the ZIP.

Settings/profiles use the selected game's `config/rice-modified/` directory. Named profiles are available; the reused framework intentionally starts each session with fresh module toggles/settings while keeping GUI preferences and named profiles. Uninstall by closing the game and removing the JAR. Configuration may be retained separately.

## Included work

- Forge coremod and client entry point for 1.8.9.
- Load-time semantic hooks replacing native class retransformation.
- Existing Java menu, module registry, profiles, input, packet and rotation services.
- 72 registered Java modules; see `module-inventory.tsv` for the actual inventory.
- Preserved modified Knockback Displace, Bow Aimbot and prediction helpers, Fog, and Shaders sources.
- Legit Void setting on Displace's Void direction, including refinement filtering.
- Corrected bundled shader resource name (`scan_pincushion`).
- Removed Scaffold stays unregistered. The reused framework's other registration choices remain visible in the inventory.
- Input restoration tolerates missing/destroyed keyboard/mouse devices during startup/shutdown.
- GUI labels, descriptions, options and notifications use Rice branding; imported client names are replaced in displayed text and text measurements. Internal IDs and saved option values remain stable.

Native edition extensions and their settings/appearance still require comparison in-game. This JAR does not claim byte-for-byte equivalence to the C++ implementation, nor has it been tested alongside other clients/mods providing the same features. It contains no Minecraft/Forge classes, natives, launcher credentials, or test fixtures.

## Validation

The build checks all direct game member references, emits three narrow Forge access rules, transforms all 20 targeted real Forge-patched game classes in production SRG and development naming, verifies bytecode data flow, and links every generated Rice hook. It also checks duplicate transformation protection, absence of JNI methods, and initialization/configuration/shutdown of all 72 modules against real game classes in a windowless isolated fixture. That fixture does not exercise a live game, GPU, network connection, or Lunar's loader.

Version 0.1.1 addresses the two startup failures in the observed Lunar crash log: camera hooks accept extra setters while requiring coverage of both axes, and GUI input uses Forge's cancellable input events instead of replacing the call sites targeted by Lunar's scroll mixin. Regression checks reproduce extra camera writes, reject missing axes, and verify that `GuiScreen` remains byte-identical after Rice's transformer. These checks run again on the obfuscated release. A successful live Lunar launch with this revision is still required.

Version 0.1.2 addresses the observed world/server-entry crash. Game callbacks now stay passive until licensing and the complete Rice module host have both initialized; revocation or shutdown disables them immediately. Redirect-style hooks preserve the original vanilla operation while Rice is inactive. A dedicated regression invokes the first-frame camera, renderer, player, entity, input, movement, and network callbacks with an uninitialized host in named, SRG, and protected builds.

Version 0.2.0 uses the current `modified/rice-loader.exe` payload as the edition reference. Its visible registry is enforced as exactly 72 modules: the modified client's original 66 plus Stasis, Anti Fireball, Fireball Pearl, Bow Aimbot, Fog, and Shaders. Unrelated framework additions are not registered. The HUD editor exposes only the native modified build's Module list and Notifications anchors, with an Edit HUD control available from every module tab.

Version 0.2.2 relocates all bundled classes into one per-build opaque package after Zelix protection, gives all 1,253 class entries opaque names, and flattens private assets into one per-build namespace with opaque filenames. Forge entrypoints, injected hook owners, internal data lookups, and the verification harness are remapped together. The protected build reruns hook linkage, Lunar compatibility, licensing, the 72-module lifecycle, and locked world-entry tests after relocation.

The Windows installer embeds the JAR with a SHA-256 checksum. Packaging extracts it into an isolated temporary directory and compares it to the verified JAR. See `build-report.json` and the build logs for results.

## Release protection

The release build runs the supplied ZKM-compatible tool with name exclusions for Forge entry points, bytecode hooks, reflection, public APIs, and third-party libraries. It requests enhanced string encryption, integer constant encryption, and normal flow obfuscation, then verifies the resulting classes again. The supplied tool reports flow obfuscation on only one or two methods per class. Its original file and license behavior are not modified. Original assets and metadata are retained because this local tool drops resource contents. Tool identity, changed/renamed class counts and the flow limit are recorded in the build report.

KeyAuth replies must pass Ed25519 verification and a 25-second timestamp window before JSON is accepted. The application owner and active subscription are checked. Sessions are rechecked every 60 seconds, with a maximum local lease of 180 seconds; rejected or unavailable sessions disable the Rice modules. HTTPS uses normal certificate and hostname checks, with redirects disabled. The client contains only public application IDs and the server verification key, not seller credentials or private signing keys. It stores no license key. Windows uses the existing native launcher's device hash; Mac uses a hashed platform UUID. An existing Windows-bound license may need its HWID reset before use on a Mac, depending on your KeyAuth policy.

Each build has a random ID embedded in the manifest, a resource and executable code. Use `--recipient-tag "customer-reference"` when creating an individually traceable copy. The mapping from that private distribution label to build ID, together with obfuscation mappings and logs, stays under `build/private/<build-id>/` and is excluded from the release ZIP/installer. A build without a recipient tag is traceable only to that build, not to a particular customer. Watermarks can be removed and local checks can be patched; no client-side protection makes reverse engineering impossible.

Gameplay algorithms still run locally. This change uses the existing remote licensing service; it does not move gameplay logic to a new private backend or deploy a server.

## Build from source

Requires Python 3.10+, a JDK 11+ compiler (output is Java 8 bytecode), and the existing local Minecraft 1.8.9 Java development dependencies. The existing Gradle caches supply Forge's named API, Forge-patched SRG test classes, LaunchWrapper 1.12, and ASM's analysis test library. Minecraft dependencies are never included in the release.

```powershell
python build.py --game-source "path/to/Slinky-source" --jdk "path/to/jdk" --zkm "path/to/ZKM.jar" --recipient-tag "customer-reference"
```

`--game-source` supplies `build/minecraft-1.8.9-named.jar`, `deps/libraries/*.jar`, and `deps/tooling/asm*.jar`. Override automatic cache discovery with `--forge-jar`, `--forge-srg-game`, and `--launchwrapper`. On Windows the .NET Framework compiler builds the EXE installer. On other platforms packaging produces the JAR/ZIP.

`--zkm` or `ZKM_JAR` selects the obfuscator; the current workspace also discovers the supplied Downloads installation. The build refuses to publish if obfuscation or post-obfuscation tests fail. The pinned EdDSA dependency is bundled under its upstream license for Java 8 compatibility. Tests use their own signed fixtures; test keys and helper classes are not shipped.

Editable Forge integration and modified feature overrides live in `src/main/java`. The precompiled named framework is pinned in `vendor/provenance.json`; `framework-source-reference.zip` preserves the existing Java source snapshot for reference, with its provenance limitation documented there. The build reuses the already adapted framework bytecode rather than recompiling all upstream adapters. `tools/import_modified_features.py` can refresh the feature overrides from this workspace's preserved reference sources; review its changes before rebuilding.

Regular and native modified executables are separate from this project and are not replaced by this build.

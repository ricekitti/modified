DISPLACE IMPLEMENTATION HANDOFF

Start here:
  displace/KnockbackDisplaceModule.java
  displace/RavenBsVoidScanner.java

Requested feature: manual Knockback Displace (module ID knockbackdisplace).
Do not substitute KillAuraDisplaceModule or RavenBsDisplaceBackend: those implement a separate feature.
The main Displace state machine lives in KnockbackDisplaceModule, not a separate backend class.

Target: Minecraft 1.8.9, Java 8 bytecode (major version 52).
This is an implementation/porting source kit, not a standalone Gradle project or prebuilt plugin.
The receiving client must provide its Minecraft dependencies, mappings, event hooks and module framework.
The complete current main-source tree is included under reference/ so framework dependencies can be traced without guessing. Other modules in reference/ are context, not additional requested features. No personal profiles, launchers, DLLs or historical snapshots are included.

INTEGRATION CHECKLIST
1. Register new KnockbackDisplaceModule(weapons, manager); see CoreModules.java.
2. Port the constructor settings AND settings loaded by EvidenceModule/EvidenceCatalog from MetadataCatalog.json. Also inspect InitialSettings.java and InitialSettings.json for actual initialization. Preserve keys, direction indices and defaults. See extracted Displace metadata in displace/metadata/.
3. Supply ClientContext readiness/input checks, RelationshipService.hostile, WeaponsModule.isAllowed/enchantmentGroups, TimingAccess.serverYaw and the TimerRangeModule.frozenOrAdvancing guard.
4. Connect SemanticEvent types 3, 13, 14, 15 and 18 exactly as their real firing sites do. See GameHooks.beforeInteraction/afterInteraction/afterEntities, ClientEventBridge, PacketPipeline and injection transformers. Interaction active/reducing flags matter. Outbound pipeline dispatchMode=1 is not interchangeable with cancelled=true: trace the pipeline's handling.
5. Preserve RotationService claim priority 200, retain/add/applyPending sequencing, movement-remapping behavior, yaw/pitch restore and the delayed attack KeyBinding.onTick call. Preserve the +/-5 degree random offset for non-Void directions.
6. For third-person rotations, port myau.RotationState.update and its consuming render/movement hooks. Use equivalent host APIs if the receiving client has its own rotation system.
7. Port RavenBsVoidScanner with its Minecraft collision queries, radius, angle and one-block-wall option. It is the helper used by THIS Displace module.
8. Preserve sprint ownership and cleanup on disable/world unload, input loss and GUI opening. Clear pending state and do not leave temporary rotation or packet state behind.
9. Keep shared coordination in mind: KillAuraModule and AntiFireballModule inspect this module's private active field. Their reference sources show the contract; a port can expose an equivalent accessor instead. They are not required to implement KillAura Displace.
10. Use mapped reflection helpers for obfuscated Minecraft fields/methods; include equivalent mappings when porting.

TESTS AND LIMITATIONS
Original SlinkyDisplaceCheck.java is included in tests/ unchanged. It covers direction selection, Void geometry and lifecycle/rotation behavior, but its main entry also makes Rice-wide assertions about separate combat modules and requires compiled Rice classes. Adapt the harness to the destination framework; do not treat this ZIP as a standalone runnable test project. Test fixtures/stubs and build tooling are included as reference.
This export was checked for ZIP integrity and source-byte equality. No production sources were edited, no EXE was built, and no live game/injection test was run for this export.

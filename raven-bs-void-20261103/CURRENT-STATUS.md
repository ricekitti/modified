ARCHIVED: superseded by the user request to restore original native Slinky
Displace. See ../OG-DISPLACE-STATUS.md for the active build.

# Bow crash fix and Raven bS Void

Deliverable: ../rice-loader-bow-fixed-raven-void.exe. Restart Minecraft first.

Bow enable crash: the recent Lunar crash report hs_err_pid34692.log shows
SlinkyHooks18.updateEntities reaching a native event-13 dispatch with a null
JNI environment. source_features.cpp previously cached a pointer to a hook's
stack-local Jvm; movement input could also cache then delete its local player
and world references. Rotation dispatch now obtains this thread's environment
from JavaVM, creates fresh references, and restores scoped callback contexts.

Raven Void: prepare_raven_void.py extracts eight production methods and three
supporting classes from this folder's hash-locked Displace.java, copied from
the user-provided raven-bS-beta-master (1) folder. Host/Minecraft API adapters
and omitted debug observers are listed in the generated source manifest.
The original input remains intact. Select Mode -> Raven bS Void in Knockback
Displace. Direction -> Void remains supported for existing saved settings.

Validation: 22/22 modified CTest checks, including native JVM integration in
named, SRG, and official Minecraft mappings. Checks cover enable without a
target after invalidating previous hook context, active bow prediction/final
rotation, Raven selector/legacy scanner parity, Legit policy, and actual
Displace attack yaw. See verification.json for packaged runtime hashes.
Live gameplay and server behavior remain unverified.

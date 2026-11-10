Displace is a separate Combat module in the modified client. Dynamic is the default.

Included: Static and Dynamic angles; 0–500 ms delay; Left/Right and 0–180 degree
offset in Static; Static Find-void; wall-aware Dynamic paths; optional one-tick
Blink; Ignore-teammates; Has-knockback; editable item whitelist; direction arrow
with a 250 ms fade; alternating movement and attack compensation.

Hold attack near another player and move, or hold an item enchanted with
Knockback. Target range follows the reference's squared eye-to-box distance of
9 (three blocks). Dynamic needs a reachable void path; it does not fall back
to Static on solid ground. Use this module separately from Knockback Displace.

This is a native port of OpenMyau-Plus Displace.java, not Java bytecode copied
into the DLL. Reference (author credit: strangerrrrs):
https://github.com/IamNespola/OpenMyau-Plus/blob/be581744a01cd4a4a73ad65f7d7f5904165ef426/src/main/java/myau/module/modules/Displace.java

The reference's item condition uses OR, including the permissive result from
an unchecked condition. That behavior is preserved: enable both Has-knockback
and Item-whitelist to require one of those two conditions. This client has no
KillAura module, so activation uses the attack key instead of a KillAura target.
Movement input uses the native movement hook. Unloaded columns are excluded
from void scans, and a missing mapping or failed JNI world query disables the
module safely. Windows for up to 256 target entity IDs are retained.

Verification: DisplaceCore and DisplaceNative cover targeting, Static/Dynamic
scans, wall rejection, delay windows, item conditions, movement, blink release,
arrow rendering/fade, mapping failures, and world/unload cleanup. Existing
ModuleStartup, StasisNative, and LauncherRegression checks also pass.
The rebuilt executable's extracted payload is checked against the rebuilt DLL.
Live Minecraft gameplay has not been verified.

The regular directory and the launcher's authentication sources are unchanged.

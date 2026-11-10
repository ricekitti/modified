Fireball Pearl is a one-shot module under Utility. Bind a key or enable it once
with an ender pearl in your hotbar. Real Rotations defaults to off.

It follows the supplied five-tick sequence:
1. Select the first pearl hotbar slot.
2. Send the pearl use packet and animation while aiming upward.
3. Select and use the first fireball slot, if present, then send animation.
4. Restore the original hotbar slot.
5. Disable itself.

No fireball is required. A missing pearl disables the module immediately. A
non-creative fireball stack is decremented locally, including removing the
stack when empty, as in the supplied source. Pearl consumption is left to the
game/server. Manual cancellation restores the original slot when still in the
same world/player. World changes cancel without editing replacement inventory.

This is a native adaptation of fireball-pearl-source-20261030-194516.zip. All
three supplied Java files are preserved unchanged under:
slinkyreadablesource/slinkyreadablesource/references/fireball-pearl/

The ZIP depends on a Java module/event framework that is absent from this
native client. The implementation uses native module events, JNI packet
constructors, and PlayerControllerMP.syncCurrentPlayItem. Silent rotations are
applied around world updates and the original camera/previous rotation is
restored afterward. Real Rotations remain visible. The ZIP does not contain
RotationUtils.NSsr; Real Rotations uses the native sensitivity rounding helper.
The module claims the native hotbar while active to avoid conflicting slot
changes from other native modules, and safely cancels if the hotbar is busy.

FireballPearlNative tests the actual callbacks with a controlled JNI inventory:
packet constructor arguments and ordering, server-facing rotation before use,
pearl-only use, fireball consumption, creative mode, both rotation modes,
missing pearl, cancellation, occupied hotbar, disconnected sender, world
changes, and cleanup. All nine client/launcher tests pass. Live gameplay has
not been verified.

Only the modified client receives this feature. Authentication and the regular
build remain unchanged; the GUI startup fix remains included.

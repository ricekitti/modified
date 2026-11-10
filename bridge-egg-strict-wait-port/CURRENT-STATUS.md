# Rice Slinky v1.8.18 — Bridge Egg strict wait

Fix the Wait for Fireball Pearl Clutch sequence gate. In v1.8.17, Egg could start first when fireball trigger conditions were pending, an item was unavailable, or the normal pearl solver found a reachable target. That readiness-based bypass defeated the requested ordering.

With **Wait for Fireball Pearl Clutch** enabled (default), Egg now waits while Fireball Pearl Clutch is enabled and has not attempted this fall, and throughout its active attempt/flight. Egg can follow after the attempt ends if still falling and its own conditions pass. Ground contact rearms the gate for the next fall. Turning off the wait condition or disabling Fireball Pearl Clutch releases the sequence gate; existing protection against simultaneously using items during an active pearl attempt remains.

This wait is deliberate even if the fireball module is missing an item or waiting for a damage/void-duration condition. Egg cannot silently bypass it based on a readiness prediction. Fireball's own trigger conditions are retained.

Validation: full 32-test CTest suite, with named/SRG/official integration checking pending damage, pending void duration, unavailable fireball, active flight, same-fall follow-up, landing/new-fall rearming, toggle-off and module-disable behavior. Runtime extraction of the versioned EXE must equal the rebuilt native DLL. Launcher project version 1.8.18; Windows file/product version 1.8.18.0. Previous versioned EXEs preserved.

Includes v1.8.17 pearl-accuracy, flight-disable, menu-overlap and regular Fireball Pearl removal fixes. Live gameplay has not been verified. Restart Minecraft before injecting.

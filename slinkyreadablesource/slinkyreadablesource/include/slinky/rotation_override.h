#pragma once
struct RotationOverrideAngle {float yaw,pitch;};
// Event 13's reserved byte can request a final server-facing override after all
// handlers have run. The camera's queued mouse/Aim Assist delta is accounted for.
inline constexpr unsigned char kApplyRotationOverride = 1;
extern "C" void queue_rotation_override(void* java,void* player,RotationOverrideAngle target);


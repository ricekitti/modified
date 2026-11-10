#include "slinky/rotation_override.h"
extern "C" RotationOverrideAngle get_entity_rotation(void*,void*);
extern "C" RotationOverrideAngle get_render_state();
extern "C" void apply_silent_rotation(RotationOverrideAngle);
extern "C" float wrap_angle(float);
static bool finite(float v){return v==v&&v>=-3.4028234e38f&&v<=3.4028234e38f;}
extern "C" void queue_rotation_override(void* java,void* player,RotationOverrideAngle target){
    if(!java||!player||!finite(target.yaw)||!finite(target.pitch))return;
    auto actual=get_entity_rotation(java,player),pending=get_render_state();
    if(!finite(actual.yaw)||!finite(actual.pitch)||!finite(pending.yaw)||!finite(pending.pitch))return;
    // The engine will apply this queue in sensitivity steps after saving the
    // real view. Cancel the existing queue only for the final winning override.
    apply_silent_rotation({wrap_angle(target.yaw-actual.yaw-pending.yaw),
        target.pitch-actual.pitch-pending.pitch});
}


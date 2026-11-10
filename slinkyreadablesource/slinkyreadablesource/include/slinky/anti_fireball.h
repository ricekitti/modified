#pragma once
namespace slinky {
struct FireVec {double x,y,z;};
struct FireBox {FireVec min,max;};
struct FireRotation {float yaw,pitch;};
bool fireBoxNaN(FireBox);
double fireDistance(FireVec eye,FireBox,double border);
FireRotation fireAim(FireVec eye,FireBox);
float fireWrap(float);
float fireSnap(float target,float reference,float sensitivity);
float fireAimError(FireVec eye,FireBox,double border,FireRotation view);
bool fireCandidate(bool ignored,bool canFly,double distance,float error,float range,int fov,bool badBox);
void fireFixMovement(float viewYaw,float serverYaw,float& forward,float& strafe,bool sneak);
struct FireActions {
    unsigned char mask;
    void record(bool use,bool dig,bool place,bool movement);
    bool busy()const{return mask!=0;}
};
}

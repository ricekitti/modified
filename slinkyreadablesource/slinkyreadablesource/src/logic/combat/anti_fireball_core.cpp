// AntiFireball math and selection rules from ricekitti/Mayu-src-latest.
#include "slinky/anti_fireball.h"
extern "C" double sin(double),cos(double),sqrt(double),atan2(double,double),floor(double);
namespace slinky {
static constexpr double pi=3.14159265358979323846;
static double clamp(double x,double a,double b){return x<a?a:x>b?b:x;}
static FireBox expand(FireBox b,double d){b.min.x-=d;b.min.y-=d;b.min.z-=d;b.max.x+=d;b.max.y+=d;b.max.z+=d;return b;}
static FireVec closest(FireVec e,FireBox b){return {clamp(e.x,b.min.x,b.max.x),clamp(e.y,b.min.y,b.max.y),clamp(e.z,b.min.z,b.max.z)};}
bool fireBoxNaN(FireBox b){return b.min.x!=b.min.x||b.min.y!=b.min.y||b.min.z!=b.min.z||b.max.x!=b.max.x||b.max.y!=b.max.y||b.max.z!=b.max.z;}
double fireDistance(FireVec eye,FireBox b,double border){auto p=closest(eye,expand(b,border));double x=p.x-eye.x,y=p.y-eye.y,z=p.z-eye.z;return sqrt(x*x+y*y+z*z);}
FireRotation fireAim(FireVec eye,FireBox b){
    FireVec center={(b.min.x+b.max.x)*.5,(b.min.y+b.max.y)*.5,(b.min.z+b.max.z)*.5},near=closest(eye,b);
    double dx=center.x+(near.x-center.x)*.875-eye.x,dy=near.y-eye.y,dz=center.z+(near.z-center.z)*.875-eye.z;
    float yaw=(float)(atan2(dz,dx)*180/pi)-90.f,pitch=(float)(-atan2(dy,sqrt(dx*dx+dz*dz))*180/pi);
    pitch+=10.f*(float)clamp((float)((eye.y-center.y)/(center.y-b.min.y)),-.5,.5);
    return {yaw,pitch};
}
float fireWrap(float v){v-=(float)floor((v+180.f)/360.f)*360.f;return v;}
float fireSnap(float target,float reference,float sensitivity){
    float change=fireWrap(target-reference),f=sensitivity*.6f+.2f,increment=f*f*f*8.f;
    float rounded=(float)floor((double)change/((double)increment*.15)+.5);
    return (float)((double)reference+(double)(rounded*increment)*.15);
}
static bool rayHits(FireVec eye,FireBox b,FireRotation r){
    double yaw=r.yaw*pi/180,pitch=r.pitch*pi/180;
    FireVec direction={-sin(yaw)*cos(pitch),-sin(pitch),cos(yaw)*cos(pitch)};
    double start=0,end=30+b.max.x-b.min.x+b.max.y-b.min.y+b.max.z-b.min.z;
    double p[]={eye.x,eye.y,eye.z},d[]={direction.x,direction.y,direction.z},lo[]={b.min.x,b.min.y,b.min.z},hi[]={b.max.x,b.max.y,b.max.z};
    for(int k=0;k<3;++k){if(d[k]>-1e-12&&d[k]<1e-12){if(p[k]<lo[k]||p[k]>hi[k])return false;continue;}
        double a=(lo[k]-p[k])/d[k],c=(hi[k]-p[k])/d[k];if(a>c){double t=a;a=c;c=t;}if(a>start)start=a;if(c<end)end=c;if(start>end)return false;}
    return true;
}
float fireAimError(FireVec eye,FireBox b,double border,FireRotation view){
    b=expand(b,border);if(rayHits(eye,b,view))return 0;auto r=fireAim(eye,b);
    float yaw=fireWrap(r.yaw-view.yaw);if(yaw<0)yaw=-yaw;float pitch=r.pitch-view.pitch;if(pitch<0)pitch=-pitch;
    return (yaw>pitch?yaw:pitch)*2;
}
bool fireCandidate(bool ignored,bool flying,double distance,float error,float range,int fov,bool bad){return !ignored&&!flying&&!bad&&distance<=range+3&&error<=fov;}
void fireFixMovement(float view,float yaw,float& f,float& s,bool sneak){
    if(f==0&&s==0)return;
    float direction=view;if(f<0)direction+=180;float factor=f<0?-.5f:f>0?.5f:1.f;
    if(s>0)direction-=90*factor;else if(s<0)direction+=90*factor;
    int sector=(int)(fireWrap(direction-yaw+22.5f)+180.f)/45%8;
    const float forwards[]={-1,-1,0,1,1,1,0,-1},strafes[]={0,1,1,1,0,-1,-1,-1};
    f=forwards[sector];s=strafes[sector];if(sneak){f*=.3f;s*=.3f;}
}
void FireActions::record(bool use,bool dig,bool place,bool movement){if(movement){mask=0;return;}mask|=(use?1:0)|(dig?2:0)|(place?4:0);}
}

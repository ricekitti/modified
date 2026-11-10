#include "slinky/anti_fireball.h"
#include <cmath>
#include <limits>
#include <iostream>
#include <stdexcept>
using namespace slinky;
static void check(bool ok,const char* why){if(!ok)throw std::runtime_error(why);}
int main(){try{
    FireVec eye={0,1.62,0};FireBox box={{-.5,1,4.5},{.5,2,5.5}};
    check(std::abs(fireDistance(eye,box,.1)-4.4)<1e-6,"Range must include entity collision border");
    check(fireDistance({0,1.5,5},box,0)==0,"Eye inside bounds must have zero distance");
    auto aim=fireAim(eye,box);check(aim.yaw==0,"Straight-ahead fireball must have zero yaw");
    check(fireAimError(eye,box,.1,{0,0})==0,"Ray intersecting target must have zero aim error");
    check(fireAimError(eye,box,.1,{180,0})==360,"Behind target must have a 360-degree FOV error");
    check(fireCandidate(false,false,8,360,5,360,false),"Selection reaches configured range plus three");
    check(!fireCandidate(false,false,8.01,0,5,360,false),"Selection must reject beyond range plus three");
    check(!fireCandidate(true,false,5,0,5,360,false),"Initially close fireballs must stay ignored");
    check(!fireCandidate(false,true,5,0,5,360,false),"Allow-flying capability must suppress selection");
    check(!fireCandidate(false,false,5,91,5,90,false),"FOV setting must reject outside its limit");
    check(!fireCandidate(false,false,5,0,5,360,true),"NaN box must never be a target");
    box.min.x=std::numeric_limits<double>::quiet_NaN();check(fireBoxNaN(box),"NaN detection must inspect bounds");
    check(fireWrap(180)==-180&&fireWrap(-181)==179&&fireWrap(721)==1,"Angles must use Minecraft wrap convention");
    check(std::abs(fireSnap(.08f,0,.5f)-.15f)<1e-6,"Mouse sensitivity must round to the nearest step");
    check(std::abs(fireSnap(-.08f,0,.5f)+.15f)<1e-6,"Negative mouse steps must round correctly");
    check(std::abs(fireSnap(-179,179,.5f)-180.95f)<1e-4,"Yaw must cross the seam along its nearest equivalent");
    float f=1,s=0;fireFixMovement(0,-90,f,s,false);check(f==0&&s==-1,"Silent correction must preserve the original world direction");
    f=1;s=0;fireFixMovement(0,-90,f,s,true);check(f==0&&std::abs(s+.3f)<1e-6,"Sneaking must preserve the 0.3 scale");
    f=s=0;fireFixMovement(20,90,f,s,false);check(f==0&&s==0,"No-input case must not introduce movement");
    FireActions actions={};actions.record(true,false,false,false);check(actions.busy(),"UseEntity must block another action");
    actions.record(false,true,true,false);check(actions.mask==7,"Digging and placement must accumulate");
    actions.record(false,false,false,true);check(!actions.busy(),"C03 movement must reset action tracking");
    std::cout<<"AntiFireball range, aim, FOV, mouse steps, movement, and action rules passed\n";return 0;
}catch(const std::exception& e){std::cerr<<e.what()<<'\n';return 1;}}

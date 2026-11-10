#include "slinky/displace.h"
#include <cmath>
#include <iostream>
#include <stdexcept>
using namespace slinky;
static void check(bool value,const char* why){if(!value)throw std::runtime_error(why);}
struct World {bool eastVoid=true,westVoid=false,wall=false,boxed=false;int collisionQueries=0;};
static bool empty(void* p,double x,double,double){auto& w=*(World*)p;return (w.eastVoid&&x>=2)||(w.westVoid&&x<=-2);}
static bool collision(void* p,DisplaceBox b){auto& w=*(World*)p;++w.collisionQueries;return w.boxed||(w.wall&&b.max.x>=1.2);}
int main(){try{
    World w;DisplaceQuery q={&w,empty,collision};DisplaceVec player={0,64,-2},target={0,64,0};DisplaceBox box={{-.3,64,-.3},{.3,65.8,.3}};
    auto a=displaceScan(q,player,target,box,true);
    check(a.found&&std::abs(a.yaw+90)<.001,"Dynamic must aim east toward the open void");
    check(w.collisionQueries>0,"Dynamic must test swept collision boxes");
    w.wall=true;a=displaceScan(q,player,target,box,true);check(!a.found,"Dynamic must never scan through the wall to void");
    w.westVoid=true;a=displaceScan(q,player,target,box,true);check(a.found&&std::abs(a.yaw-90)<.001,"Blocked east path must choose clear west path");
    w.boxed=true;check(!displaceScan(q,player,target,box,true).found,"Boxed target must not displace");
    w={false,false,false,false};check(!displaceScan(q,player,target,box,true).found,"Solid ground must have no Dynamic fallback");
    w={true,false,true,false};a=displaceScan(q,player,target,box,false);
    check(a.found&&a.left,"Static void scan must find the edge and compute the left side");
    check(a.yaw>-90&&a.yaw<0,"Static must aim near the target edge, not along the raw void direction");
    check(displaceDistanceSquared({3.3,65,0},box)==9,"Targeting uses squared eye-to-box distance, 9 means three blocks");
    check(displaceDistanceSquared({0,65,0},box)==0,"Eye inside target box must be zero distance");
    DisplaceWindow window={};check(window.allows(1,100),"First tick opens a target window immediately");
    check(!window.allows(2,100)&&window.allows(3,100),"Delay must use 50ms ticks");
    check(!window.allows(3,101)&&window.allows(4,101),"Fractional tick delays must round up");
    check(window.allows(11,500)&&!window.allows(12,500),"Ten-tick window must restart immediately");
    check(window.allows(12,0),"Zero delay must never block");
    check(displaceItemPass(true,false,false,false),"Preserve reference's permissive single-toggle OR behavior");
    check(!displaceItemPass(true,true,false,false),"Both item checks enabled must require either match");
    check(displaceItemPass(true,true,true,false)&&displaceItemPass(true,true,false,true),"Either enabled item check may satisfy OR");
    DisplaceCycle cycle={};float f=0,s=0;
    check(!cycle.advance(true,true,true,false)&&cycle.phase,"First eligible tick must displace");
    cycle.input(f,s,true);check(f==1&&cycle.compensate,"Unenchanted displacement must push forward");
    check(cycle.advance(true,true,true,false)&&!cycle.phase,"Next tick restores the attack click");
    cycle.input(f,s,true);check(s==-1&&!cycle.compensate,"Following tick compensates with opposite strafe");
    cycle.advance(true,true,false,true);f=s=0;cycle.input(f,s,true);check(f==0,"Knockback enchantment must skip forward compensation");
    cycle.clear();cycle.advance(true,false,true,false);check(!cycle.phase&&!cycle.compensate,"Delay rejection must clear pending displacement");
    cycle.advance(false,true,false,false);check(!cycle.active&&!cycle.phase,"Losing the target resets the phase");
    std::cout<<"Displace geometry, walls, targeting, timing, item conditions, and movement passed\n";return 0;
}catch(const std::exception& error){std::cerr<<error.what()<<'\n';return 1;}}

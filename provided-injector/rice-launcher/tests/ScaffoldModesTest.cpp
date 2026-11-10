#include "slinky/scaffold_presets.h"
#include <cmath>
#include <iostream>
#include <stdexcept>
static int checks;
static void check(bool value,const char* reason){++checks;if(!value)throw std::runtime_error(reason);}
static void near(double a,double b,const char* reason){check(std::abs(a-b)<.000002,reason);}
int main(){try{
    using namespace scaffoldcontrol;
    const auto& r=regularPreset;const auto& k=keepPreset;
    near(r.range,7,"Regular maximum range");near(r.fov,360,"Regular maximum FOV");near(r.cps,20,"Regular maximum CPS");
    near(r.random,.30,"Regular 30 percent randomization");near(r.baseSpeed*20,600,"Regular 600 degree/sec base speed");
    near(r.acceleration,.500,"Regular 0.500 acceleration");near(r.strength,.30,"Regular 30 percent strength");
    near(r.snapDelay*50,500,"Regular maximum snap-back delay");near(r.snapDuration*50,500,"Regular maximum snap-back duration");
    check(!r.snapJump&&!r.disableAfter,"Regular keep-jump and disable-after must be off");
    check(!r.air&&!r.damage&&!r.backwards,"Regular hidden conditions must not gate walking");
    near(k.range,5,"Keep Y profile range");near(k.fov,360,"Keep Y profile FOV");near(k.cps,20,"Keep Y profile CPS");
    near(k.minHeight,1,"Keep Y profile minimum height");near(k.random,.429999977350235,"Keep Y profile randomization");
    near(k.baseSpeed,33,"Keep Y profile base speed");near(k.acceleration,.600435733795166,"Keep Y profile acceleration");
    near(k.strength,.27668845653533936,"Keep Y profile strength");near(k.snapDelay,5,"Keep Y profile delay");near(k.snapDuration,5,"Keep Y profile duration");
    check(k.snapJump&&!k.disableAfter&&k.sideways&&k.multipoint&&k.selectBlocks==2,"Keep Y profile flags");
    check(!k.air&&!k.damage&&!k.backwards,"Keep Y profile conditions");
    check(!keep(Regular,true)&&keep(KeepY,false)&&!keep(KeepYOnUse,false)&&keep(KeepYOnUse,true),"Three mode selection semantics");
    State state;
    check(!state.matches(Regular,false,false),"No stale plan before first frame");
    auto f=state.update(Regular,false,false,true,65);check(!f.keepPreset&&!f.holdHeight&&!f.tower&&f.targetY==64,"Regular target follows feet");
    f=state.update(Regular,false,false,false,66.2);check(f.targetY==66&&!f.holdHeight,"Regular allows higher placement");
    f=state.update(KeepY,false,false,true,65);check(f.keepPreset&&f.holdHeight&&f.targetY==64,"Keep Y captures initial level");
    f=state.update(KeepY,false,false,false,66.2);check(f.holdHeight&&f.targetY==64,"Keep Y keeps height through airborne movement");
    check(!state.matches(KeepY,false,true),"Space change invalidates the old planned click");
    f=state.update(KeepY,false,true,true,65);check(f.tower&&!f.holdHeight,"Holding Space overrides height lock");
    f=state.update(KeepY,false,true,false,66.2);check(f.tower&&f.targetY==65,"Space permits a higher block below the player's feet");
    check(allows(f,64,true)&&allows(f,65,false),"Space accepts top/side supports that raise the bridge");
    check(!allows(f,64,false)&&!allows(f,65,true)&&!allows(f,63,true),"Space rejects old flat and intersecting upper layers");
    for(double feet:{65.0,65.42,65.99,66.0,66.25,66.9}){
        auto tower=state.update(Regular,false,true,false,feet);
        check(tower.targetY+1<=feet,"Upward placement must not intersect player's feet");
    }
    state.update(KeepY,false,true,false,66.2);
    f=state.update(KeepY,false,false,false,66.2);check(!f.holdHeight,"Do not capture a new Keep Y level in midair after Space release");
    f=state.update(KeepY,false,false,true,66);check(f.holdHeight&&f.targetY==65,"Recapture the raised level after landing");
    state.reset();f=state.update(KeepYOnUse,false,false,true,65);check(!f.keepPreset&&!f.holdHeight,"Right-click mode falls back to Regular when released");
    check(!state.matches(KeepYOnUse,true,false),"Right-click press invalidates stale Regular click");
    f=state.update(KeepYOnUse,true,false,true,65);check(f.keepPreset&&f.holdHeight&&f.targetY==64,"Right-click press enables Keep Y");
    f=state.update(KeepYOnUse,true,false,false,67.1);check(f.targetY==64,"Right-click hold retains level");
    f=state.update(KeepYOnUse,false,false,false,67.1);check(!f.keepPreset&&!f.holdHeight&&f.targetY==67,"Right-click release immediately restores Regular height");
    state.reset();state.update(KeepYOnUse,false,false,true,65);
    f=state.update(KeepYOnUse,true,false,false,65.7);check(f.holdHeight&&f.targetY==64,"Pressing right-click mid-jump uses the last standing build level");
    state.reset();f=state.update(KeepYOnUse,true,false,false,65.7);check(f.targetY==64,"Enabling midair without ground history chooses a nonintersecting layer");
    state.reset();check(!state.observed&&!state.hasHeight&&!state.waitLanding,"Disable/world unload clears anchor and pending state");
    auto solid=[](int x,int y,int z){return x==0&&z==0&&y==64;};
    auto base=scaffoldedge::at_height(1.1,64,.5,.1,0,solid);check(base.active&&base.y==64,"Keep Y probe uses fixed layer");
    auto raised=scaffoldedge::at_height(.5,66,.5,0,0,solid);check(raised.active&&raised.y==66,"Upward bridge probe can seek above original platform");
    AutoJump jump;
    check(!jump.update(Regular,true,false,true,true,true,0),"Regular never auto-jumps");
    check(!jump.update(KeepY,false,false,true,false,true,0),"Keep Y does not jump in place");
    check(!jump.update(KeepY,false,false,true,true,false,0),"No auto-jump without usable blocks or while riding");
    check(!jump.update(KeepY,false,true,true,true,true,0),"Physical Space retains manual upward bridging");
    check(!jump.update(KeepYOnUse,false,false,true,true,true,0),"RMB variant does not jump when released");
    check(jump.update(KeepYOnUse,true,false,true,true,true,0),"RMB hold starts a forward takeoff");
    check(!jump.update(KeepYOnUse,true,false,true,true,true,0),"Only one jump per grounded contact");
    check(!jump.update(KeepYOnUse,true,false,false,true,true,.42),"No second jump while rising");
    check(!jump.update(KeepYOnUse,true,false,false,true,true,-.3),"No jump while falling");
    check(jump.update(KeepYOnUse,true,false,true,true,true,0),"Landing triggers the next jump");
    check(!jump.update(KeepYOnUse,false,false,true,true,true,0),"RMB release immediately stops auto-jump");
    jump.reset();state.reset();
    int takeoffs=0;
    for(int cycle=0;cycle<8;++cycle){
        f=state.update(KeepY,false,false,true,65);
        if(jump.update(KeepY,false,false,true,true,true,0))++takeoffs;
        check(f.holdHeight&&!f.tower&&f.targetY==64,"Auto-jump captures and retains flat bridge height");
        for(double feet:{65.42,65.75,66.05,66.25,66.0,65.65,65.1}){
            f=state.update(KeepY,false,false,false,feet);
            check(!jump.update(KeepY,false,false,false,true,true,.1),"No airborne repeated impulse");
            check(f.holdHeight&&!f.tower&&f.targetY==64,"Forward jumps must never turn into an upward staircase");
            check(allows(f,64,false)&&!allows(f,65,false),"Forward jump places on original bridge level");
        }
    }
    check(takeoffs==8,"Auto-jump repeats every landing while moving");
    jump.reset();check(jump.update(KeepY,false,false,true,true,true,0),"Lifecycle reset allows a new takeoff");
    jump.reset();check(!jump.update(KeepY,false,false,true,true,true,.42),"Do not overwrite a jump already started by the game");
    std::cout<<"Scaffold modes: "<<checks<<" preset, height, Space, right-click and lifecycle checks passed\n";return 0;
}catch(const std::exception& error){std::cerr<<error.what()<<'\n';return 1;}}

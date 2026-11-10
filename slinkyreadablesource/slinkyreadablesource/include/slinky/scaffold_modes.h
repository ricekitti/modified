#pragma once
#include "scaffold_edge.h"
namespace scaffoldcontrol {
enum Mode { Regular=0,KeepY=1,KeepYOnUse=2 };
inline bool keep(unsigned mode,bool right){return mode==KeepY||(mode==KeepYOnUse&&right);}
// Native jump(), not a synthetic Space press: Keep Y must retain its bridge layer.
struct AutoJump {
    bool launched=false;
    void reset(){launched=false;}
    bool update(unsigned mode,bool right,bool space,bool ground,bool moving,bool usable,double vertical){
        if(!ground)launched=false;
        if(!keep(mode,right)||space||!moving||!usable){launched=false;return false;}
        if(!ground||vertical>0.0||launched)return false;
        launched=true;return true;
    }
};
struct Frame {bool keepPreset,holdHeight,tower;int targetY;};
inline bool allows(const Frame& frame,int supportY,bool top){
    if(frame.holdHeight)return supportY==frame.targetY;
    if(frame.tower)return supportY+(top?1:0)==frame.targetY;
    return true;
}
struct State {
    unsigned mode=0;bool rightMode=false,space=false,hasHeight=false,waitLanding=false,observed=false,hasGround=false;int height=0,groundHeight=0;
    void reset(){mode=0;rightMode=false;space=false;hasHeight=false;waitLanding=false;observed=false;hasGround=false;height=groundHeight=0;}
    bool matches(unsigned choice,bool right,bool jump)const{return observed&&choice==mode&&keep(choice,right)==rightMode&&jump==space;}
    Frame update(unsigned choice,bool right,bool jump,bool ground,double feetY){
        bool requested=keep(choice,right);
        if(!observed||choice!=mode||requested!=rightMode){hasHeight=false;waitLanding=false;}
        observed=true;mode=choice;rightMode=requested;space=jump;
        int current=-scaffoldedge::block(-feetY)-1;
        if(ground){groundHeight=scaffoldedge::block(feetY)-1;hasGround=true;}
        // A raised block must end below the feet; ceil() would put it inside the player.
        if(jump){hasHeight=false;waitLanding=true;return {requested,false,true,scaffoldedge::block(feetY)-1};}
        if(!requested){hasHeight=false;waitLanding=false;return {false,false,false,current};}
        if(waitLanding&&!ground)return {true,false,false,current};
        if(waitLanding){waitLanding=false;hasHeight=false;}
        if(!hasHeight){height=ground?current:hasGround?groundHeight:scaffoldedge::block(feetY)-1;hasHeight=true;}
        return {true,true,false,height};
    }
};
struct Preset {
    float range,fov,minHeight,cps,random,baseSpeed,acceleration,strength,snapDelay,snapDuration;
    int selectBlocks;
    bool sideways,multipoint,snapJump,disableAfter,air,damage,backwards;
};
}

#pragma once
namespace slinky {
struct PearlAction {int slot;bool use,disable;};
struct FireballPearlState {
    int pearl=-1,fireball=-1,previous=-1,step=0;
    float yaw=0;
    bool active=false;
    bool begin(int pearlSlot,int fireballSlot,int previousSlot,float startYaw){
        if(pearlSlot<0||pearlSlot>8||previousSlot<0||previousSlot>8)return false;
        pearl=pearlSlot;fireball=fireballSlot;previous=previousSlot;yaw=startYaw;step=0;active=true;return true;
    }
    PearlAction tick(){
        if(!active)return {-1,false,false};
        switch(step++){
            case 0:return {pearl,false,false};
            case 1:return {-1,true,false};
            case 2:return {fireball,fireball!=-1,false};
            case 3:return {previous,false,false};
            default:active=false;return {-1,false,true};
        }
    }
    bool silentRotation(bool real)const{return active&&!real&&step<=3;}
    bool realRotation(bool real)const{return active&&real&&step>0&&step<=3;}
    void clear(){*this={};}
};
}

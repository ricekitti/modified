#pragma once
namespace scaffoldcontrol {
inline bool pause_fast_place(bool scaffold,float& timer,unsigned char& held,int& delay,float activation){
    if(!scaffold)return false;
    timer=0;held=0;delay=(int)activation;
    return true;
}
struct PlacementPermit {
    bool pending=false,up=false,keep=false,tower=false;unsigned mode=0;int x=0,y=0,z=0;
    void clear(){pending=false;}
    bool issue(int bx,int by,int bz,bool top,unsigned selected,bool keepY,bool jump){
        if(pending)return false;
        x=bx;y=by;z=bz;up=top;mode=selected;keep=keepY;tower=jump;pending=true;return true;
    }
    bool allow(bool scaffold,bool right,bool blockItem,bool validHit,int bx,int by,int bz,bool top,unsigned selected,bool keepY,bool jump){
        if(!scaffold){clear();return true;}
        if(!right)return true;
        if(!blockItem){clear();return true;}
        bool owned=pending&&validHit&&x==bx&&y==by&&z==bz&&up==top&&mode==selected&&keep==keepY&&tower==jump;
        clear();return owned;
    }
};
}

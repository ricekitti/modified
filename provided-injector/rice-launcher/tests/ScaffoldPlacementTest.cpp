#include "slinky/scaffold_placement.h"
#include <iostream>
#include <stdexcept>
static int checks;
static void check(bool value,const char* reason){++checks;if(!value)throw std::runtime_error(reason);}
int main(){try{
    using namespace scaffoldcontrol;
    float timer=4.5f;unsigned char held=1;int delay=0;
    check(!pause_fast_place(false,timer,held,delay,3),"Fast Place unchanged when Scaffold off");
    check(timer==4.5f&&held==1&&delay==0,"Inactive guard preserves Fast Place state");
    for(int tick=0;tick<100;++tick){
        timer=-15;held=1;delay=0;
        check(pause_fast_place(true,timer,held,delay,3),"Scaffold on always suppresses Fast Place regardless of motion/RMB");
        check(timer==0&&held==0&&delay==3,"Paused Fast Place cannot accumulate burst debt");
    }
    check(!pause_fast_place(false,timer,held,delay,3)&&delay==3,"Fast Place resumes with its normal activation delay");
    PlacementPermit permit;
    auto right=[&](bool hit=true,int x=0,bool up=false,unsigned mode=0,bool keep=false,bool jump=false){return permit.allow(true,true,true,hit,x,64,0,up,mode,keep,jump);};
    check(!right(),"Held RMB without a Scaffold plan cannot place");
    check(permit.issue(0,64,0,false,0,false,false),"Scaffold can authorize its own click");
    check(!permit.issue(0,64,0,false,0,false,false),"Only one pending click even with repeated render ticks");
    check(right(),"Authorized aligned Scaffold click is allowed");
    check(!right()&&!right(),"Extra Fast Place or held-RMB clicks are blocked");
    permit.issue(0,64,0,false,0,false,false);check(!right(true,1),"Unexpected target cannot spend a valid placement");
    check(!permit.pending,"Bad target clears the stale permit");
    permit.issue(0,64,0,true,0,false,true);check(!right(true,0,false,0,false,true),"Wrong face is blocked");
    permit.issue(0,64,0,true,0,false,true);check(right(true,0,true,0,false,true),"Space upward-placement click is allowed");
    permit.issue(0,64,0,false,2,true,false);check(!right(true,0,false,2,false,false),"Right-click Keep Y release invalidates the previous mode's click");
    permit.issue(0,64,0,false,0,false,false);check(!right(true,0,false,1,true,false),"Mode switch cannot leak an old click");
    permit.issue(0,64,0,false,0,false,false);check(!right(false),"No valid block ray means no placement");
    permit.issue(0,64,0,false,0,false,false);
    check(permit.allow(true,false,true,true,0,64,0,false,0,false,false)&&permit.pending,"Left-click behavior remains available");
    check(permit.allow(true,true,false,false,0,0,0,false,0,false,false)&&!permit.pending,"Food and other non-block use remains available");
    check(permit.allow(false,true,true,false,0,0,0,false,0,false,false),"Normal RMB placement returns when Scaffold off");
    permit.issue(0,64,0,false,0,false,false);permit.clear();check(!right(),"Disable/world cleanup removes placement authorization");
    std::cout<<"Scaffold placement ownership: "<<checks<<" Fast Place pause/resume and click-isolation checks passed\n";return 0;
}catch(const std::exception& failure){std::cerr<<failure.what()<<'\n';return 1;}}

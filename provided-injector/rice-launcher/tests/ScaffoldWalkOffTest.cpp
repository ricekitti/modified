#include "slinky/scaffold_edge.h"
#include <cmath>
#include <iostream>
#include <stdexcept>
static int checks;
static void check(bool value,const char* why){++checks;if(!value)throw std::runtime_error(why);}
int main(){try{
    auto platform=[](int x,int y,int z){return x==0&&y==64&&z==0;};
    auto safe=scaffoldedge::probe(.5,65,.5,.1,0,platform);check(!safe.active,"Do not activate in the middle of a supported block");
    auto edge=scaffoldedge::probe(.9,65,.5,.2,0,platform);check(edge.active&&scaffoldedge::block(edge.x)==1&&edge.y==64,"Walking toward an edge must prepare placement while still supported");
    auto off=scaffoldedge::probe(1.05,65,.5,.2,0,platform);check(off.active&&off.y==64,"Walking off must activate without jumping");
    check(!edge.open&&off.open,"Only an actual open foot column requests immediate silent alignment");
    auto falling=scaffoldedge::probe(1.2,64.92,.5,.2,0,platform);check(falling.active&&falling.y==64,"Small gravity drop must keep the original platform side in range");
    auto idleEdge=scaffoldedge::probe(1.01,65,.5,0,0,platform);check(idleEdge.active,"Open support under stationary edge must not require a movement or jump key");
    for(int axis=0;axis<2;axis++)for(int direction:{-1,1}){
        double x=axis==0?(direction>0?.9:.1):.5,z=axis==1?(direction>0?.9:.1):.5;
        auto result=scaffoldedge::probe(x,65,z,axis==0?.2*direction:0,axis==1?.2*direction:0,platform);
        check(result.active,"Walk-off activation in all four directions");
    }
    auto diagonal=scaffoldedge::probe(.9,65,.9,.2,.2,platform);check(diagonal.active&&scaffoldedge::block(diagonal.x)==1&&scaffoldedge::block(diagonal.z)==1,"Diagonal edge preparation");
    auto bridge=[](int,int y,int){return y==64;};
    check(!scaffoldedge::probe(.9,65,.9,.2,.2,bridge).active,"Existing bridge support must suppress activation");
    auto negative=[](int x,int y,int z){return x==-1&&y==64&&z==-1;};
    check(scaffoldedge::probe(-.1,65,-.5,.2,0,negative).active,"Negative-coordinate edge activation");
    auto vertical=scaffoldedge::probe(.5,66,.5,0,0,[](int,int y,int){return y==65;});check(!vertical.active&&vertical.y==65,"Correct support height at exact block boundaries");
    check(scaffoldedge::block(-.1)==-1&&scaffoldedge::block(-1)==-1,"Negative floor semantics");
    check(std::abs(scaffoldedge::lead(20)-.6)<1e-8&&std::abs(scaffoldedge::lead(-20)+.6)<1e-8,"Bound the prediction distance");
    std::cout<<"Scaffold walk-off: "<<checks<<" edge, falling-height and support checks passed\n";return 0;
}catch(const std::exception& failure){std::cerr<<failure.what()<<'\n';return 1;}}

#pragma once

// Scaffold-only edge activation; does not depend on the jump key or onGround.
namespace scaffoldedge {
struct Probe { bool active; double x,y,z; bool open; };
inline int block(double value){int integer=(int)value;return integer>value?integer-1:integer;}
inline double lead(double velocity){return velocity<-.6?-.6:velocity>.6?.6:velocity;}
template<class Solid> Probe at_height(double x,int y,double z,double motionX,double motionZ,Solid solid){
    if(!solid(block(x),y,block(z)))return {true,x,(double)y,z,true};
    double nextX=x+lead(motionX),nextZ=z+lead(motionZ);
    if(!solid(block(nextX),y,block(nextZ)))return {true,nextX,(double)y,nextZ,false};
    return {false,x,(double)y,z,false};
}
template<class Solid> Probe probe(double x,double feetY,double z,double motionX,double motionZ,Solid solid){
    // ceil(feetY)-1 keeps the platform's side available during the first falling tick.
    return at_height(x,-block(-feetY)-1,z,motionX,motionZ,solid);
}
}

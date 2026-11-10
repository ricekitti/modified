package dev.rice.bedplates;

import java.util.*;

/** Bed-defense sampling ported from the supplied BedPlates script. No game-thread work runs asynchronously. */
public final class BedplatesCore {
    public static final class Pos {
        public final int x,y,z;
        public Pos(int x,int y,int z){this.x=x;this.y=y;this.z=z;}
        public Pos add(int x,int y,int z){return new Pos(this.x+x,this.y+y,this.z+z);}
        @Override public boolean equals(Object o){if(!(o instanceof Pos))return false;Pos p=(Pos)o;return x==p.x&&y==p.y&&z==p.z;}
        @Override public int hashCode(){return (x*31+y)*31+z;}
    }
    public interface Reader {int at(Pos p);}
    public static int delay(double distance){return distance>100?4000:distance>50?3000:distance>25?2000:1000;}
    public static final class Display {
        private int previousMode=-1;private boolean down,shown=true;
        public boolean update(int mode,boolean key,boolean gui){
            if(mode!=previousMode){previousMode=mode;down=key;shown=true;}
            if(mode==0){down=key;return true;}
            if(mode==1&&key&&!down&&!gui)shown=!shown;
            down=key;return mode==2?!gui&&key:shown;
        }
        public void reset(){previousMode=-1;down=false;shown=true;}
    }
    public static Pos[] pair(Pos p,Reader world){
        int state=world.at(p);if((state>>>4)!=26)return null;
        int meta=state&15;int[][] offsets={{0,1},{-1,0},{0,-1},{1,0}};
        int[] d=offsets[meta&3];int sign=(meta&8)!=0?-1:1;
        Pos q=p.add(sign*d[0],0,sign*d[1]);
        if((world.at(q)>>>4)!=26)q=p;
        return p.x>q.x||p.z>q.z?new Pos[]{p,q}:new Pos[]{q,p};
    }
    public static Map<Integer,Integer> layers(Pos first,Pos second,Reader world){
        boolean facingZ=Math.abs(second.z-first.z)>Math.abs(second.x-first.x);
        Pos[] beds={first,second};Map<Integer,Integer> result=new TreeMap<>();int airLayers=0;
        for(int layer=1;layer<=5;layer++){
            Map<Integer,Integer> counts=new HashMap<>();int total=0,air=0;
            for(int part=0;part<2;part++){
                Pos bed=beds[part];int offset=part==0?layer:-layer;
                Pos start=facingZ?bed.add(0,0,offset):bed.add(offset,0,0);
                for(int step1=0;step1<=layer;step1++){
                    int yOffset=0;
                    for(int step2=step1;step2>=0;step2--){
                        Pos a,b;
                        if(facingZ){a=start.add(-step2,yOffset,part==0?-step1:step1);b=start.add(step2,yOffset,part==0?-step1:step1);}
                        else{a=start.add(part==0?-step1:step1,yOffset,-step2);b=start.add(part==0?-step1:step1,yOffset,step2);}
                        int state=world.at(a);counts.put(state,counts.getOrDefault(state,0)+1);total++;if(state==0)air++;
                        if(!a.equals(b)){state=world.at(b);counts.put(state,counts.getOrDefault(state,0)+1);total++;if(state==0)air++;}
                        if(step2>0)yOffset++;
                    }
                }
            }
            if(total==0||(float)air/total>.2f){if(++airLayers>=2)break;continue;}
            for(Map.Entry<Integer,Integer> e:counts.entrySet())if(e.getKey()!=0&&(float)e.getValue()/total>=.2f)
                result.put(e.getKey(),result.getOrDefault(e.getKey(),0)+e.getValue());
        }
        return result;
    }
    /** Camera-relative point projected with the native render event's captured matrices. */
    public static double[] project(double x,double y,double z,double[] f){
        double[] eye=new double[4],clip=new double[4],p={x-f[8],y-f[9],z-f[10],1};
        for(int row=0;row<4;row++)for(int col=0;col<4;col++)eye[row]+=f[14+col*4+row]*p[col];
        for(int row=0;row<4;row++)for(int col=0;col<4;col++)clip[row]+=f[30+col*4+row]*eye[col];
        if(!(clip[3]>0))return null;
        double nx=clip[0]/clip[3],ny=clip[1]/clip[3],nz=clip[2]/clip[3];
        if(!Double.isFinite(nx)||!Double.isFinite(ny)||!(nz>-1&&nz<1)||Math.abs(nx)>1||Math.abs(ny)>1)return null;
        return new double[]{(nx+1)*.5*f[11],(1-ny)*.5*f[12]};
    }
    private BedplatesCore(){}
}

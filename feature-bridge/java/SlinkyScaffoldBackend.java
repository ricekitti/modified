package com.snowy.module.player.scaffold;

import com.snowy.module.player.scaffold.Blocks;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.*;

/** Scaffold-owned port of Slinky Clutch's candidate order and clamped-face planner.
 * No Clutch module state, settings, hooks, RNG or inventory ownership is shared.
 * Mayu's caller supplies the building height, predicted feet and rotation policy.
 */
public final class SlinkyScaffoldBackend {
    public static final class Target {
        public final BlockPos support;
        public final EnumFacing face;
        public final Vec3 point;
        private final int rank;
        private final float cost;
        Target(BlockPos support,EnumFacing face,Vec3 point,int rank,float cost){
            this.support=support;this.face=face;this.point=point;this.rank=rank;this.cost=cost;
        }
    }
    private BlockPos previous;

    public void reset(){previous=null;}

    // Deterministic (randomisation=0, multipoint=true) clone of clutch_pick_aim_target.
    // Top-face clamp comes from clutch_on_late_tick's straight-below branch.
    public static Vec3 facePoint(BlockPos support,EnumFacing face,double centerX,double centerZ,int distance){
        double x=clamp(centerX,support.getX()+0.1,support.getX()+0.9);
        double z=clamp(centerZ,support.getZ()+0.1,support.getZ()+0.9);
        double y=support.getY()+0.25+clamp(distance*0.25,0,0.65);
        switch(face){
            case WEST:x=support.getX();break;
            case EAST:x=support.getX()+1;break;
            case NORTH:z=support.getZ();break;
            case SOUTH:z=support.getZ()+1;break;
            case UP:y=support.getY()+1;break;
            default:return null;
        }
        return new Vec3(x,y,z);
    }
    public static EnumFacing face(int dx,int dz){
        if(dx==0&&dz==0)return EnumFacing.UP;
        return Math.abs(dx)>Math.abs(dz)?(dx<0?EnumFacing.EAST:EnumFacing.WEST)
            :(dz<0?EnumFacing.SOUTH:EnumFacing.NORTH);
    }
    public List<Target> locate(Minecraft mc,BlockPos spot,double feetX,double feetY,double feetZ,float yaw,float pitch){
        List<Target> targets=new ArrayList<>();
        Vec3 eye=new Vec3(feetX,feetY+mc.thePlayer.getEyeHeight(),feetZ);
        double reach=Math.min(4.49,mc.playerController.getBlockReachDistance());
        for(int encoded:SlinkyClutchOffsets.OFFSETS){
            int dx=(encoded&15)-8,dy=((encoded>>4)&15)-8,dz=((encoded>>8)&15)-8;
            BlockPos support=spot.add(dx,dy,dz);
            if(!Blocks.support(mc,support))continue;
            EnumFacing face=face(dx,dz);
            BlockPos destination=support.offset(face);
            if(destination.getY()>spot.getY()||!Blocks.open(mc,destination))continue;
            Vec3 point=facePoint(support,face,feetX,feetZ,Math.abs(dx)+Math.abs(dz));
            if(eye.squareDistanceTo(point)>reach*reach)continue;
            float[] look=look(eye,point,yaw);
            MovingObjectPosition hit=Blocks.trace(mc,eye,look[0],look[1],reach);
            if(!hits(hit,support,face))continue;
            int rank=Math.abs(dx)+Math.abs(dy)+Math.abs(dz);
            // The original gives the last usable support a two-shell continuity bias.
            if(support.equals(previous))rank-=2;
            targets.add(new Target(support,face,point,rank,
                Math.abs(MathHelper.wrapAngleTo180_float(look[0]-yaw))+Math.abs(look[1]-pitch)));
        }
        Collections.sort(targets,new Comparator<Target>(){
            public int compare(Target a,Target b){int rank=Integer.compare(a.rank,b.rank);return rank!=0?rank:Float.compare(a.cost,b.cost);}
        });
        if(targets.size()>4)targets=new ArrayList<>(targets.subList(0,4));
        previous=targets.isEmpty()?null:targets.get(0).support;
        return targets;
    }
    public static float[] look(Vec3 eye,Vec3 point,float referenceYaw){
        double x=point.xCoord-eye.xCoord,y=point.yCoord-eye.yCoord,z=point.zCoord-eye.zCoord;
        float yaw=(float)(Math.atan2(z,x)*180/Math.PI-90);
        return new float[]{referenceYaw+MathHelper.wrapAngleTo180_float(yaw-referenceYaw),
            (float)(-Math.atan2(y,Math.sqrt(x*x+z*z))*180/Math.PI)};
    }
    public static boolean hits(MovingObjectPosition hit,BlockPos support,EnumFacing face){
        return hit!=null&&hit.typeOfHit==MovingObjectPosition.MovingObjectType.BLOCK
            &&support.equals(hit.getBlockPos())&&hit.sideHit==face;
    }
    private static double clamp(double value,double low,double high){return Math.max(low,Math.min(high,value));}
}

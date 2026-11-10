package dev.rice.module.impl.pearl;
import net.minecraft.world.World;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.block.state.IBlockState;
/** Explicit sibling adaptation of missing VoidProximity: same solid predicate as recovered aboveVoid. */
public final class SupportScan {
    public static boolean isUnsupported(World world,double x,double y,double z){
        if(world==null)return false;
        int bx=MathHelper.floor_double(x),bz=MathHelper.floor_double(z);
        for(int by=MathHelper.floor_double(y)-1;by>=0;by--){BlockPos p=new BlockPos(bx,by,bz);if(!world.isBlockLoaded(p))return false;IBlockState state=world.getBlockState(p);if(state.getBlock().getCollisionBoundingBox(world,p,state)!=null)return false;}
        return true;
    }
}

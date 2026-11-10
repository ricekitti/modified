import com.snowy.module.player.scaffold.SlinkyScaffoldBackend;
import java.util.*;
import net.minecraft.util.*;

/** Backend geometry and separation checks with real game classes and a test world. */
public final class SlinkyScaffoldBackendTest {
    private static int assertions;
    private static void check(boolean value,String message){assertions++;if(!value)throw new AssertionError(message);}
    private static void near(double a,double b,String message){check(Math.abs(a-b)<1e-6,message);}
    public static void main(String[] args)throws Exception{
        SuppliedScaffoldTest.nativePrepare();
        SlinkyScaffoldBackend a=new SlinkyScaffoldBackend(),b=new SlinkyScaffoldBackend();
        for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++){
            EnumFacing expected=dx==0&&dz==0?EnumFacing.UP:Math.abs(dx)>Math.abs(dz)?(dx<0?EnumFacing.EAST:EnumFacing.WEST):(dz<0?EnumFacing.SOUTH:EnumFacing.NORTH);
            check(SlinkyScaffoldBackend.face(dx,dz)==expected,"Cloned Slinky dominant-axis face mismatch");
        }
        BlockPos support=new BlockPos(0,64,0);
        Vec3 point=SlinkyScaffoldBackend.facePoint(support,EnumFacing.EAST,1.2,.5,1);
        near(point.xCoord,1,"Slinky side plane");near(point.yCoord,64.5,"Slinky side height");near(point.zCoord,.5,"Slinky clamped tangent");
        Vec3 far=SlinkyScaffoldBackend.facePoint(support,EnumFacing.NORTH,100,-100,4);
        near(far.xCoord,.9,"Slinky inset clamp");near(far.yCoord,64.9,"Slinky height clamp");near(far.zCoord,0,"Slinky north plane");
        Vec3 top=SlinkyScaffoldBackend.facePoint(support,EnumFacing.UP,-2,4,0);
        near(top.xCoord,.1,"Slinky top X inset");near(top.yCoord,65,"Slinky top height");near(top.zCoord,.9,"Slinky top Z inset");
        check(SlinkyScaffoldBackend.facePoint(support,EnumFacing.DOWN,0,0,1)==null,"No underside placement");
        List<SlinkyScaffoldBackend.Target> found=a.locate(FeatureFixture.minecraft,new BlockPos(1,64,0),1.2,65,.5,90,84);
        check(!found.isEmpty(),"Cloned Slinky search must find real neighboring support");
        check(found.get(0).support.equals(support)&&found.get(0).face==EnumFacing.EAST,"Side bridge target mismatch");
        float[] look=SlinkyScaffoldBackend.look(new Vec3(1.2,65+FeatureFixture.self.getEyeHeight(),.5),found.get(0).point,450);
        near(look[0],450,"Yaw wrapping continuity");check(look[1]>0&&look[1]<=90,"Downward pitch");
        check(!b.locate(FeatureFixture.minecraft,new BlockPos(1,64,0),1.2,65,.5,90,84).isEmpty(),"Independent backend instance");
        SuppliedScaffoldTest.blocks.clear();
        check(a.locate(FeatureFixture.minecraft,new BlockPos(1,64,0),1.2,65,.5,90,84).isEmpty(),"No unsupported air placement");
        a.reset();
        SuppliedScaffoldTest.blocks.put(new BlockPos(0,60,0),net.minecraft.init.Blocks.stone.getDefaultState());
        check(a.locate(FeatureFixture.minecraft,new BlockPos(1,64,0),1.2,65,.5,90,84).isEmpty(),"Reject beyond reach");
        SuppliedScaffoldTest.blocks.clear();
        SuppliedScaffoldTest.blocks.put(support,net.minecraft.init.Blocks.stone.getDefaultState());
        SuppliedScaffoldTest.blocks.put(new BlockPos(1,64,0),net.minecraft.init.Blocks.stone.getDefaultState());
        check(a.locate(FeatureFixture.minecraft,new BlockPos(1,64,0),1.2,65,.5,90,84).isEmpty(),"Reject occupied destinations");
        for(int mode=0;mode<3;mode++)for(int side=0;side<4;side++){
            SuppliedScaffoldTest.nativePrepare();
            double[][] feet={{1.2,.5},{.5,1.2},{-.2,.5},{.5,-.2}};
            float yaw=new float[]{90,180,-90,0}[side];
            FeatureFixture.self.setPosition(feet[side][0],65,feet[side][1]);
            FeatureFixture.self.rotationYaw=yaw;FeatureFixture.self.rotationPitch=84;
            com.snowy.module.player.scaffold.Scaffold core=new com.snowy.module.player.scaffold.Scaffold(new com.snowy.config.Configuration());
            SuppliedScaffoldTest.seed(core);
            ((dev.rice.setting.ChoiceSetting)core.properties().get(0).host).set(mode);
            com.snowy.rotation.RotationManager rotations=com.snowy.rotation.RotationManager.get();
            rotations.startTick();rotations.endTick();rotations.resetSent(yaw,84);core.enabled(true);
            for(int tick=0;tick<10;tick++){
                rotations.startTick();core.onPlayerTick(new com.snowy.core.event.TickEvent.PlayerTickEvent(com.snowy.core.event.TickEvent.Phase.START,FeatureFixture.self));
                if(tick<2)check(SuppliedScaffoldTest.placements==0,"Mayu placement warmup preserved");
                if(rotations.isActive())rotations.recordSent(rotations.getYaw(),rotations.getPitch(),true);
                rotations.endTick();
            }
            check(SuppliedScaffoldTest.placements>0,"Mode "+mode+", side "+side+" must bridge using Slinky backend");
            near(FeatureFixture.self.rotationYaw,yaw,"Mayu free camera preserved");
            core.enabled(false);check(FeatureFixture.self.inventory.currentItem==0,"Mayu slot restoration preserved");
            com.snowy.util.Claims.unregister(core);
        }
        System.out.println("Slinky Scaffold backend: "+assertions+" geometry, continuity, reach and isolation assertions passed");
    }
}

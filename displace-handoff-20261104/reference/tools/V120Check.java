import dev.rice.client.hook.GameHooks;
import dev.rice.client.mayu.MayuHost;
import dev.rice.inject.Mappings;
import dev.rice.inject.transform.RicePatches;
import dev.rice.shaded.asm.*;
import dev.rice.shaded.asm.tree.*;
import java.util.Arrays;
import myau.RotationState;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

/** Executes the production renderer patch around a headless draw probe; HookPlacementCheck also patches the
 * actual vanilla RenderManager in all three mappings. No renderer/OpenGL context or game is launched. */
public final class V120Check implements Opcodes {
   static float[] expected;
   static float cameraYaw,previousCameraYaw;
   static int draws;
   static RuntimeException drawFailure;

   static void check(boolean ok,String message){V115Check.check(ok,message);}
   static float[] pose(EntityLivingBase e){return new float[]{e.prevRenderYawOffset,e.renderYawOffset,e.prevRotationYawHead,e.rotationYawHead,e.prevRotationPitch,e.rotationPitch};}
   static float[] rotationPose(){return new float[]{RotationState.previousBodyYaw(),RotationState.bodyYaw(),RotationState.previousHeadYaw(),RotationState.headYaw(),RotationState.previousPitch(),RotationState.pitch()};}
   static void setPose(EntityLivingBase e){e.prevRenderYawOffset=11;e.renderYawOffset=17;e.prevRotationYawHead=23;e.rotationYawHead=29;e.prevRotationPitch=31;e.rotationPitch=37;e.rotationYaw=43;e.prevRotationYaw=41;}
   public static boolean draw(Entity entity,float partialTicks,boolean hideDebugBox){
      draws++;
      check(Arrays.equals(expected,pose((EntityLivingBase)entity)),"draw saw camera pose instead of server head/body/pitch");
      check(entity.rotationYaw==cameraYaw&&entity.prevRotationYaw==previousCameraYaw,"draw changed camera yaw");
      if(drawFailure!=null)throw drawFailure;
      return !hideDebugBox;
   }
   static final class Loader extends ClassLoader {
      Loader(){super(V120Check.class.getClassLoader());}
      Class<?> define(byte[] bytes){return defineClass("V120RenderProbe",bytes,0,bytes.length);}
   }
   static RenderManager patchedRenderer()throws Exception {
      Mappings.load(null);
      MethodNode m=new MethodNode(ACC_PUBLIC,"renderEntityStatic","(Lnet/minecraft/entity/Entity;FZ)Z",null,null);
      // Two original return paths, both of which must preserve their boolean and restore the pose.
      LabelNode other=new LabelNode();
      m.instructions.add(new VarInsnNode(ILOAD,3));m.instructions.add(new JumpInsnNode(IFEQ,other));
      for(int i=0;i<2;i++){
         if(i==1)m.instructions.add(other);
         m.instructions.add(new VarInsnNode(ALOAD,1));m.instructions.add(new VarInsnNode(FLOAD,2));m.instructions.add(new VarInsnNode(ILOAD,3));
         m.instructions.add(new MethodInsnNode(INVOKESTATIC,"V120Check","draw","(Lnet/minecraft/entity/Entity;FZ)Z",false));
         m.instructions.add(new InsnNode(IRETURN));
      }
      int edits=0;
      for(RicePatches.ClassEdits c:RicePatches.create(false))if(c.target.equals("net/minecraft/client/renderer/entity/RenderManager"))
         for(RicePatches.MethodEdit e:c.edits)if(e.label.startsWith("Mayu third-person")){check(e.apply(m)==2,"renderer patch missed a return");edits++;}
      check(edits==1,"missing production third-person patch");
      ClassNode n=new ClassNode();n.version=V1_8;n.access=ACC_PUBLIC;n.name="V120RenderProbe";n.superName="net/minecraft/client/renderer/entity/RenderManager";n.methods.add(m);
      ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);n.accept(w);
      return (RenderManager)V115Check.blank(new Loader().define(w.toByteArray()));
   }
   static void render(RenderManager renderer,EntityLivingBase entity,boolean expectSpoof,boolean fail){
      float[] before=pose(entity);expected=expectSpoof?rotationPose():before;
      cameraYaw=entity.rotationYaw;previousCameraYaw=entity.prevRotationYaw;
      int beforeDraws=draws;
      for(boolean debug:new boolean[]{false,true}){
         drawFailure=fail?new RuntimeException("test renderer failure"):null;
         try {
            boolean result=renderer.renderEntityStatic(entity,.5F,debug);
            check(!fail&&result==!debug,"render return value changed");
         }catch(RuntimeException error){check(fail&&error==drawFailure,"render exception was swallowed or replaced");}
         check(Arrays.equals(before,pose(entity)),"model fields leaked after render");
         check(entity.rotationYaw==cameraYaw&&entity.prevRotationYaw==previousCameraYaw,"camera yaw leaked after render");
      }
      check(draws==beforeDraws+2,"draw probe was skipped");drawFailure=null;
   }
   public static void main(String[] args)throws Exception {
      V115Check.setup();
      for(dev.rice.module.Module m:V115Check.manager.all())if(m.isEnabled())V115Check.manager.applyNow(m,false);
      RenderManager renderer=patchedRenderer();EntityPlayerSP player=V115Check.self;
      // BedNuker uses the same model-display path without competing with the camera.
      V115Check.manager.applyNow(V115Check.manager.find("bednuker"),true);
      V115Check.put(MayuHost.bedNuker(),"targetBlock",new net.minecraft.util.BlockPos(1,65,0));
      V115Check.put(MayuHost.bedNuker(),"interactionLocked",true);
      GameHooks.mayuPlayerUpdateHead(player);check(RotationState.movementPriority()==5,"BedNuker rotation missing");
      setPose(player);for(int view:new int[]{0,1,2}){V115Check.mc.gameSettings.thirdPersonView=view;render(renderer,player,true,false);render(renderer,player,true,true);}
      EntityOtherPlayerMP remote=V115Check.blank(EntityOtherPlayerMP.class);setPose(remote);render(renderer,remote,false,false);
      RotationState.update(false,0,0,0,0);check(RotationState.isActiveWithinTicks(1),"upstream one-tick render grace missing");render(renderer,player,true,false);
      RotationState.update(false,0,0,0,0);check(!RotationState.isActiveWithinTicks(1),"render rotation did not expire");render(renderer,player,false,false);
      // A different RenderManager keeps its own saved state (matching upstream @Unique fields).
      RenderManager second=patchedRenderer();RotationState.update(true,145,75,145,3);render(second,player,true,false);render(renderer,player,true,false);
      System.out.println("PASS: v120 production render scope displays BedNuker head/body/pitch in both third-person views; first-person camera unchanged; interpolation grace/inactive/remote paths; per-renderer state; both boolean returns and thrown-error restoration");
   }
}

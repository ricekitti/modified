import dev.rice.client.hook.GameHooks;
import dev.rice.client.rotation.RotationService;
import dev.rice.client.blockin.SuppliedBlockIn;
import dev.rice.client.blockin.SuppliedBlockInHost;
import dev.rice.module.impl.BlockInModule;
import dev.rice.setting.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.*;
import net.minecraft.entity.Entity;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.network.play.client.*;
import java.util.*;
import java.lang.reflect.*;
import java.nio.file.*;
public final class BlockInCheck {
 static void require(boolean value,String message){V115Check.check(value,message);}
 static final class World extends V115Check.World {
  @Override public boolean canBlockBePlaced(net.minecraft.block.Block block,BlockPos pos,boolean ignore,EnumFacing side,Entity entity,ItemStack stack){return getBlockState(pos).getBlock().isReplaceable(this,pos)&&(entity==null||!new AxisAlignedBB(pos,pos.add(1,1,1)).intersectsWith(entity.getEntityBoundingBox()));}
 }
 static final class Controller extends PlayerControllerMP {
  int attempts,placed;boolean reject;
  Controller(){super(null,null);}
  @Override public float getBlockReachDistance(){return 4.5F;}
  @Override public boolean onPlayerRightClick(EntityPlayerSP player,WorldClient raw,ItemStack stack,BlockPos support,EnumFacing face,Vec3 hit){
   World world=(World)raw;BlockPos target=support.offset(face);attempts++;
   require(!SuppliedBlockInHost.GameAccess.keepMiningOpening(target),"attempted to refill a player-mined opening");
   require(world.getBlockState(support).getBlock()!=Blocks.air,"clicked air");
   require(!new AxisAlignedBB(target,target.add(1,1,1)).intersectsWith(player.getEntityBoundingBox()),"planned inside player");
   require(V115Check.handler.sent.stream().noneMatch(p->p instanceof C03PacketPlayer),"Block In injected an extra movement packet before placement");
   float targetYaw=SuppliedBlockInHost.RotationManager.get().getYaw(),targetPitch=SuppliedBlockInHost.RotationManager.get().getPitch();
   require(player.rotationYaw==0&&player.rotationPitch==0,"silent aim changed camera during placement");
   float cy=MathHelper.cos(-targetYaw*(float)Math.PI/180F-(float)Math.PI),sy=MathHelper.sin(-targetYaw*(float)Math.PI/180F-(float)Math.PI),cp=-MathHelper.cos(-targetPitch*(float)Math.PI/180F),sp=MathHelper.sin(-targetPitch*(float)Math.PI/180F);
   Vec3 eye=player.getPositionEyes(1);MovingObjectPosition traced=world.rayTraceBlocks(eye,eye.addVector(sy*cp*4.5,sp*4.5,cy*cp*4.5),false,false,false);
   require(traced!=null&&support.equals(traced.getBlockPos())&&face==traced.sideHit,"C08 face differs from Snowify placement aim");
   player.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(support,face.getIndex(),stack,(float)(hit.xCoord-support.getX()),(float)(hit.yCoord-support.getY()),(float)(hit.zCoord-support.getZ())));
   if(reject)return false;
   require(world.canBlockBePlaced(((ItemBlock)stack.getItem()).getBlock(),target,false,face,player,stack),"invalid placement target");
   world.blocks.put(target,((ItemBlock)stack.getItem()).getBlock().getDefaultState());stack.stackSize--;placed++;return true;
  }
 }
 static BlockInModule module;static Controller controller;static World world;
 static io.netty.channel.embedded.EmbeddedChannel wire;
 static void setup()throws Exception{
  V115Check.setup();V115Check.put(V115Check.mc,"mcLanguageManager",new net.minecraft.client.resources.LanguageManager(new net.minecraft.client.resources.data.IMetadataSerializer(),"en_us"));for(dev.rice.module.Module m:V115Check.manager.all())if(m.isEnabled())V115Check.manager.applyNow(m,false);
  world=V115Check.blank(World.class);world.blocks=new HashMap<>();V115Check.put(world,"loadedEntityList",new ArrayList<>());V115Check.put(world,"worldBorder",new net.minecraft.world.border.WorldBorder());
  V115Check.world=world;V115Check.put(V115Check.mc,"theWorld",world);V115Check.put(V115Check.self,"worldObj",world);
  controller=V115Check.blank(Controller.class);V115Check.put(controller,"mc",V115Check.mc);V115Check.put(controller,"netClientHandler",V115Check.handler);V115Check.put(controller,"currentGameType",net.minecraft.world.WorldSettings.GameType.SURVIVAL);V115Check.put(V115Check.mc,"playerController",controller);
  wire=new io.netty.channel.embedded.EmbeddedChannel(new io.netty.channel.ChannelDuplexHandler());
  net.minecraft.network.NetworkManager network=new net.minecraft.network.NetworkManager(net.minecraft.network.EnumPacketDirection.CLIENTBOUND);
  V115Check.put(network,"channel",wire);V115Check.put(V115Check.handler,"netManager",network);
  module=(BlockInModule)V115Check.manager.find("blockin");
 }
 static void scene()throws Exception{
  V115Check.manager.applyNow(module,false);module.onWorldUnload(V115Check.c);RotationService.reset(V115Check.c);world.blocks.clear();
  for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)world.blocks.put(new BlockPos(x,63,z),Blocks.stone.getDefaultState());
  world.blocks.put(new BlockPos(0,67,1),Blocks.stone.getDefaultState());
  V115Check.place(V115Check.self,.5,64,.5);V115Check.self.onGround=true;V115Check.self.rotationYaw=V115Check.self.prevRotationYaw=0;V115Check.self.rotationPitch=V115Check.self.prevRotationPitch=0;V115Check.self.motionX=V115Check.self.motionY=V115Check.self.motionZ=0;
  for(int i=0;i<9;i++)V115Check.self.inventory.mainInventory[i]=null;
  V115Check.self.inventory.mainInventory[0]=new ItemStack(Items.diamond_sword);V115Check.self.inventory.mainInventory[2]=new ItemStack(Blocks.stone,64);V115Check.self.inventory.currentItem=0;
  V115Check.mc.currentScreen=null;V115Check.mc.inGameHasFocus=true;controller.attempts=controller.placed=0;controller.reject=false;V115Check.handler.sent.clear();
  ((NumberSetting)module.findSetting("speed")).set(1D);((BooleanSetting)module.findSetting("require_ground")).set(true);
  V115Check.put(V115Check.self,"lastReportedYaw",0F);V115Check.put(V115Check.self,"lastReportedPitch",0F);
  V115Check.manager.applyNow(module,true);
 }
 static void frame(){
  V115Check.handler.sent.clear();
  RotationService.beginFrame(V115Check.c,0,0);GameHooks.beforeEntities(world);GameHooks.mayuPlayerUpdateHead(V115Check.self);
  GameHooks.afterMovementInput(V115Check.self);
  myau.host.MayuMixins.applySpoofedRotationBeforeRidingCheck(V115Check.self,V115Check.self);
  V115Check.self.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(V115Check.self.posX,V115Check.self.posY,V115Check.self.posZ,SuppliedBlockInHost.spoofYaw(V115Check.self),SuppliedBlockInHost.spoofPitch(V115Check.self),V115Check.self.onGround));
  wire.runPendingTasks();
  for(net.minecraft.network.Packet<?> p:V115Check.handler.sent)wire.writeOutbound(p);
  java.util.List<Object> actual=new ArrayList<>();Object out;while((out=wire.readOutbound())!=null)actual.add(out);
  require(actual.stream().filter(p->p instanceof C03PacketPlayer).count()==1,"extra movement packet on wire");
  for(int i=0;i<actual.size();i++)if(actual.get(i) instanceof C08PacketPlayerBlockPlacement){
   C08PacketPlayerBlockPlacement place=(C08PacketPlayerBlockPlacement)actual.get(i);
   C03PacketPlayer movement=(C03PacketPlayer)actual.stream().filter(p->p instanceof C03PacketPlayer).findFirst().get();
   require(actual.indexOf(movement)>i,"Snowify placement/movement order changed");
   require(movement.getRotating(),"placement movement lacks rotation");
   require(movement.getYaw()==SuppliedBlockInHost.RotationManager.get().getYaw()&&movement.getPitch()==SuppliedBlockInHost.RotationManager.get().getPitch(),"wire look differs from placement pose");
   require(movement.getPositionX()==V115Check.self.posX&&movement.getPositionY()==V115Check.self.posY&&movement.getPositionZ()==V115Check.self.posZ,"wire rewriting altered vanilla position");
  }
  myau.host.MayuMixins.afterPlayerUpdate(V115Check.self);GameHooks.afterEntities(world);V115Check.manager.applyPendingChanges();
 }
   static final class BridgeInput implements dev.rice.module.impl.BridgeAssistModule.Input {
      boolean physical,sneak,ground=true,back,forward=true;float yaw=-90F;double center=.5;
      public boolean ready(){return true;} public boolean physicalSneak(){return physical;} public boolean sneak(){return sneak;}
      public boolean ground(){return ground;} public boolean block(){return true;} public float pitch(){return 80F;} public float yaw(){return yaw;}
      public float forward(){return back?-1F:forward?1F:0F;} public double vertical(){return ground?0:.2;}
      public AxisAlignedBB box(){return new AxisAlignedBB(center-.3,65,.2,center+.3,66.8,.8);}
      public double[] velocity(){return new double[]{0,0,0};} public boolean backward(){return back;} public boolean forwardKey(){return forward;}
      public boolean left(){return false;} public boolean right(){return false;} public float speed(){return .1F;}
      public boolean air(int x,int y,int z){return x!=0||z!=0||y!=64;} public void sneak(boolean value){sneak=value;} public void restore(){sneak=physical;}
      public MovingObjectPosition hit(){return null;} public Vec3 position(){return new Vec3(center,65,.5);}
   }
   static void bridgeChecks() {
      dev.rice.module.impl.BridgeAssistModule.State state=new dev.rice.module.impl.BridgeAssistModule.State();
      dev.rice.module.impl.BridgeAssistModule.Settings settings=new dev.rice.module.impl.BridgeAssistModule.Settings();settings.edge=.05F;settings.unsneak=3;settings.avoidDouble=true;
      BridgeInput input=new BridgeInput();dev.rice.client.rotation.RecoveredRandom random=dev.rice.client.rotation.RecoveredRandom.SHARED;
      state.update(input,settings,random,1);require(!input.sneak,"Bridge Assist sneaks in middle of support");
      input.center=.99;state.update(input,settings,random,2);require(input.sneak,"predictive fall prevention did not sneak before edge");
      state.update(input,settings,random,3);require(input.sneak,"Bridge Assist released at unsafe edge");
      input.center=.5;state.update(input,settings,random,4);require(!input.sneak,"Slinky safe unsneak did not resume");
      input.center=.99;state.update(input,settings,random,5);require(input.sneak,"Bridge Assist did not resneak");
      input.center=.5;state.update(input,settings,random,6);require(input.sneak,"Slinky unsneak delay ignored");state.update(input,settings,random,7);require(!input.sneak,"Slinky unsneak delay never elapsed");
      input.center=.99;input.back=true;input.forward=false;require(!state.willFallOff(input,settings,random,8,false),"backward WASD signs point away from safe support");
      input.physical=true;input.sneak=true;settings.requireSneak=false;state.update(input,settings,random,9);require(input.sneak&&!state.owned,"manual sneak overridden with Require sneak off");
      settings.requireSneak=true;state.cooldown=0;input.center=.5;state.update(input,settings,random,10);require(input.sneak,"Require sneak cooldown gate differs from Slinky");
      state.cooldown=3;state.countdown=0;state.update(input,settings,random,11);require(!input.sneak,"Slinky backward/cooldown unsneak gate did not release safely");state.clear(input);require(input.sneak&&!state.owned,"cleanup did not restore physical sneak");
      input.physical=false;input.sneak=false;input.back=false;input.forward=true;input.ground=false;settings.requireSneak=false;settings.jump=true;state.update(input,settings,random,12);require(!input.sneak,"airborne Bridge Assist sneaks without a predicted edge");
      System.out.println("PASS: Slinky bridge timing/manual-sneak/cooldown behavior; predictive edge protection, safe delayed release, corrected WASD and cleanup");
   }
 public static float delegatedYaw,delegatedPitch;public static boolean failWalking;
 public static final IllegalStateException WALKING_FAILURE=new IllegalStateException("delegated walking fixture failure");
 public static void delegatedWalking(EntityPlayerSP player)throws Exception{
  delegatedYaw=player.rotationYaw;delegatedPitch=player.rotationPitch;
  V115Check.put(player,"lastReportedYaw",player.rotationYaw);V115Check.put(player,"lastReportedPitch",player.rotationPitch);
  player.sendQueue.addToSendQueue(new C03PacketPlayer(true));
  if(failWalking)throw WALKING_FAILURE;
 }
 static void walkingCompatibilityChecks()throws Exception{
  dev.rice.inject.transform.RicePatches.MethodEdit edit=null;
  for(dev.rice.inject.transform.RicePatches.ClassEdits patch:dev.rice.inject.transform.RicePatches.create(false))for(dev.rice.inject.transform.RicePatches.MethodEdit candidate:patch.edits)
   if(patch.target.equals("net/minecraft/client/entity/EntityPlayerSP")&&candidate.label.startsWith("Snowify Block In"))edit=candidate;
  require(edit!=null,"walking adapter missing");
  dev.rice.shaded.asm.tree.MethodNode body=new dev.rice.shaded.asm.tree.MethodNode(9,"walk","(Lnet/minecraft/client/entity/EntityPlayerSP;)V",null,null);
  body.instructions.add(new dev.rice.shaded.asm.tree.VarInsnNode(25,0));
  body.instructions.add(new dev.rice.shaded.asm.tree.MethodInsnNode(184,"BlockInCheck","delegatedWalking","(Lnet/minecraft/client/entity/EntityPlayerSP;)V",false));
  body.instructions.add(new dev.rice.shaded.asm.tree.InsnNode(177));
  require(edit.apply(body)>0,"no delegated walking scope");
  dev.rice.shaded.asm.ClassWriter writer=new dev.rice.shaded.asm.ClassWriter(3);writer.visit(52,1,"BlockInWalkingFixture",null,"java/lang/Object",null);body.accept(writer);writer.visitEnd();
  class FixtureLoader extends ClassLoader{Class<?> load(byte[] data){return defineClass("BlockInWalkingFixture",data,0,data.length);}}
  Method walk=new FixtureLoader().load(writer.toByteArray()).getMethod("walk",EntityPlayerSP.class);
  scene();RotationService.beginFrame(V115Check.c,0,0);GameHooks.beforeEntities(world);GameHooks.mayuPlayerUpdateHead(V115Check.self);
  float aimYaw=SuppliedBlockInHost.RotationManager.get().getYaw(),aimPitch=SuppliedBlockInHost.RotationManager.get().getPitch();
  V115Check.self.rotationYaw=57;V115Check.self.rotationPitch=13;V115Check.handler.sent.clear();
  walk.invoke(null,V115Check.self);
  require(delegatedYaw==aimYaw&&delegatedPitch==aimPitch,"delegated packet construction missed aim");
  require(V115Check.self.rotationYaw==57&&V115Check.self.rotationPitch==13,"delegated normal return leaked camera");
  Field reported=EntityPlayerSP.class.getDeclaredField("lastReportedYaw");reported.setAccessible(true);require(reported.getFloat(V115Check.self)==aimYaw,"delegated lastReportedYaw missed aim");
  wire.runPendingTasks();wire.writeOutbound(V115Check.handler.sent.get(0));C03PacketPlayer forced=(C03PacketPlayer)wire.readOutbound();require(forced.getYaw()==aimYaw&&forced.getPitch()==aimPitch,"delegated normal movement missed aim");
  wire.writeOutbound(new C03PacketPlayer(true));C03PacketPlayer restored=(C03PacketPlayer)wire.readOutbound();require(restored.getYaw()==57&&restored.getPitch()==13,"wire restore captured temporary spoofed camera");
  failWalking=true;try{walk.invoke(null,V115Check.self);throw new AssertionError("delegated exception suppressed");}catch(InvocationTargetException failure){require(failure.getCause()==WALKING_FAILURE,"delegated exception changed");}finally{failWalking=false;}
  require(V115Check.self.rotationYaw==57&&V115Check.self.rotationPitch==13,"delegated exception leaked camera");
  SuppliedBlockInHost.beginWalking(V115Check.self);SuppliedBlockInHost.beginWalking(V115Check.self);SuppliedBlockInHost.endWalking(V115Check.self);require(V115Check.self.rotationYaw==aimYaw,"nested walking scope restored too soon");SuppliedBlockInHost.endWalking(V115Check.self);require(V115Check.self.rotationYaw==57,"nested walking scope did not restore");
  SuppliedBlockInHost.beginWalking(V115Check.self);V115Check.manager.applyNow(module,false);require(V115Check.self.rotationYaw==57&&V115Check.self.rotationPitch==13,"disable leaked walking pose");SuppliedBlockInHost.endWalking(V115Check.self);
  SuppliedBlockInHost.beginWalking(V115Check.self);require(V115Check.self.rotationYaw==57&&V115Check.self.rotationPitch==13,"disabled walking scope changed view");
  System.out.println("PASS: delegated Lunar-style walking adapter, last-reported aim, normal/exception/nested/disable camera cleanup and wire restoration");
 }
 static void wireChecks()throws Exception{
  scene();RotationService.beginFrame(V115Check.c,0,0);GameHooks.beforeEntities(world);GameHooks.mayuPlayerUpdateHead(V115Check.self);
  float yaw=SuppliedBlockInHost.RotationManager.get().getYaw(),pitch=SuppliedBlockInHost.RotationManager.get().getPitch();
  C03PacketPlayer position=new C03PacketPlayer.C04PacketPlayerPosition(2,64,3,true);
  V115Check.handler.addToSendQueue(position);wire.runPendingTasks();wire.writeOutbound(position);
  C03PacketPlayer promoted=(C03PacketPlayer)wire.readOutbound();require(promoted instanceof C03PacketPlayer.C06PacketPlayerPosLook&&promoted.getPositionX()==2&&promoted.getPositionY()==64&&promoted.getPositionZ()==3&&promoted.getYaw()==yaw&&promoted.getPitch()==pitch,"Snowify position-only promotion changed position/look");
  GameHooks.afterEntities(world);wire.writeOutbound(new C03PacketPlayer(true));C03PacketPlayer restore=(C03PacketPlayer)wire.readOutbound();require(restore.getRotating()&&restore.getYaw()==0&&restore.getPitch()==0,"Snowify forced look did not restore on next nonrotating movement");
  scene();RotationService.beginFrame(V115Check.c,0,0);GameHooks.beforeEntities(world);GameHooks.mayuPlayerUpdateHead(V115Check.self);
  C03PacketPlayer ground=new C03PacketPlayer(true);V115Check.handler.addToSendQueue(ground);wire.runPendingTasks();wire.writeOutbound(ground);C03PacketPlayer look=(C03PacketPlayer)wire.readOutbound();require(look instanceof C03PacketPlayer.C05PacketPlayerLook&&!look.isMoving(),"ground-only movement gained a position");
  V115Check.manager.applyNow(module,false);wire.runPendingTasks();require(wire.pipeline().get("rice_blockin_wire_rotation")==null,"disable retained Snowify handler");
  C03PacketPlayer after=new C03PacketPlayer(true);wire.writeOutbound(after);require(wire.readOutbound()==after,"disable retained pending restore");
  scene();V115Check.self.inventory.currentItem=2;V115Check.self.inventory.mainInventory[2]=new ItemStack(Blocks.wool,8);V115Check.self.inventory.mainInventory[3]=new ItemStack(Blocks.obsidian,64);frame();require(V115Check.self.inventory.currentItem==2,"Snowify held-block preference changed");
 }
 static void dig(BlockPos pos,C07PacketPlayerDigging.Action action){V115Check.handler.addToSendQueue(new C07PacketPlayerDigging(action,pos,EnumFacing.UP));}
 static void mine(BlockPos pos,boolean stop){
  world.blocks.put(pos,Blocks.stone.getDefaultState());
  dig(pos,C07PacketPlayerDigging.Action.START_DESTROY_BLOCK);
  world.blocks.remove(pos);
  if(stop)dig(pos,C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK);
 }
 static void complete(){for(int i=0;i<50&&module.isEnabled();i++)frame();}
 static void miningOpeningChecks()throws Exception{
  BlockPos feet=new BlockPos(0,64,0),hole=feet.south(),upper=hole.up();
  // The reported clip: break an access opening, then hold Block In.
  scene();V115Check.manager.applyNow(module,false);mine(hole,false);mine(upper,true);
  require(SuppliedBlockInHost.GameAccess.keepMiningOpening(hole),"creative START-only removal was not remembered before binding");
  require(SuppliedBlockInHost.GameAccess.keepMiningOpening(upper),"survival START/STOP removal was not remembered before binding");
  V115Check.manager.applyNow(module,true);frame();
  Method progress=SuppliedBlockIn.class.getDeclaredMethod("progress",net.minecraft.client.Minecraft.class);progress.setAccessible(true);
  int[] count=(int[])progress.invoke(SuppliedBlockInHost.instance.core,V115Check.mc);require(count[1]==7,"protected openings still count as unfinished progress");
  complete();
  require(world.getBlockState(hole).getBlock()==Blocks.air&&world.getBlockState(upper).getBlock()==Blocks.air,"Block In sealed the mined bed-access opening");
  for(EnumFacing side:new EnumFacing[]{EnumFacing.WEST,EnumFacing.NORTH,EnumFacing.EAST})for(int y=0;y<2;y++)require(world.getBlockState(feet.offset(side).up(y)).getBlock()!=Blocks.air,"protecting a mined opening prevented another wall");
  require(world.getBlockState(feet.up(2)).getBlock()!=Blocks.air&&!module.isEnabled()&&V115Check.self.inventory.currentItem==0,"mining exclusion prevented roof/completion/slot cleanup");
  V115Check.manager.applyNow(module,true);complete();require(world.getBlockState(hole).getBlock()==Blocks.air,"pressing the bind again forgot the mined opening");
  // A solid server correction/replacement retires protection, rather than suppressing unrelated future fills.
  world.blocks.put(hole,Blocks.stone.getDefaultState());SuppliedBlockInHost.prePlayer(V115Check.self);world.blocks.remove(hole);
  V115Check.manager.applyNow(module,true);complete();require(world.getBlockState(hole).getBlock()!=Blocks.air,"a replaced opening remained permanently blacklisted");
  // Accepted digs during placement are also observed, without stopping other placement.
  scene();mine(hole,true);frame();require(controller.placed>0&&world.getBlockState(hole).getBlock()==Blocks.air,"active-session dig was refilled or stopped all building");
  // Neither aborted attempts nor another player's removal count as our mining.
  scene();V115Check.manager.applyNow(module,false);world.blocks.put(hole,Blocks.stone.getDefaultState());dig(hole,C07PacketPlayerDigging.Action.START_DESTROY_BLOCK);
  require(!SuppliedBlockInHost.GameAccess.keepMiningOpening(hole),"unbroken block marked as an opening");
  dig(hole,C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK);world.blocks.remove(hole);V115Check.manager.applyNow(module,true);complete();require(world.getBlockState(hole).getBlock()!=Blocks.air,"aborted dig suppressed later normal placement");
  scene();dig(hole,C07PacketPlayerDigging.Action.RELEASE_USE_ITEM);complete();require(world.getBlockState(hole).getBlock()!=Blocks.air,"non-mining packet protected an ordinary gap");
  // Pending digs expire; actual nearby openings survive bind presses instead of a short timer.
  scene();V115Check.manager.applyNow(module,false);world.blocks.put(hole,Blocks.stone.getDefaultState());dig(hole,C07PacketPlayerDigging.Action.START_DESTROY_BLOCK);
  for(int i=0;i<102;i++)SuppliedBlockInHost.prePlayer(V115Check.self);
  world.blocks.remove(hole);V115Check.manager.applyNow(module,true);complete();require(world.getBlockState(hole).getBlock()!=Blocks.air,"stale unfinished dig protected an unrelated later hole");
  scene();V115Check.manager.applyNow(module,false);mine(hole,true);
  for(int i=0;i<140;i++)SuppliedBlockInHost.prePlayer(V115Check.self);
  require(SuppliedBlockInHost.GameAccess.keepMiningOpening(hole),"nearby mined access timed out while still in use");
  V115Check.place(V115Check.self,20.5,64,.5);SuppliedBlockInHost.prePlayer(V115Check.self);V115Check.place(V115Check.self,.5,64,.5);
  V115Check.manager.applyNow(module,true);complete();require(world.getBlockState(hole).getBlock()!=Blocks.air,"leaving the area retained stale mining history");
  scene();V115Check.manager.applyNow(module,false);mine(hole,true);module.onWorldUnload(V115Check.c);
  require(!SuppliedBlockInHost.GameAccess.keepMiningOpening(hole),"world unload retained mined openings");
  mine(hole,true);World other=V115Check.blank(World.class);V115Check.put(V115Check.mc,"theWorld",other);SuppliedBlockInHost.prePlayer(V115Check.self);
  V115Check.put(V115Check.mc,"theWorld",world);SuppliedBlockInHost.prePlayer(V115Check.self);require(!SuppliedBlockInHost.GameAccess.keepMiningOpening(hole),"world identity change reused an old coordinate exclusion");
  mine(hole,true);EntityPlayerSP otherPlayer=V115Check.blank(EntityPlayerSP.class);V115Check.put(V115Check.mc,"thePlayer",otherPlayer);SuppliedBlockInHost.prePlayer(otherPlayer);
  V115Check.put(V115Check.mc,"thePlayer",V115Check.self);SuppliedBlockInHost.prePlayer(V115Check.self);require(!SuppliedBlockInHost.GameAccess.keepMiningOpening(hole),"player identity change retained mining history");
  // Roof and auxiliary-support planning must obey the same exclusion, not just the side-wall loop.
  scene();V115Check.manager.applyNow(module,false);BlockPos roof=feet.up(2),support=roof.east();mine(roof,true);mine(support,true);
  V115Check.manager.applyNow(module,true);frame();
  Method plan=SuppliedBlockIn.class.getDeclaredMethod("plan",net.minecraft.client.Minecraft.class);plan.setAccessible(true);
  for(BlockPos[] step:(List<BlockPos[]>)plan.invoke(SuppliedBlockInHost.instance.core,V115Check.mc))require(!step[0].equals(roof),"roof planner reintroduced a mined opening");
  Method way=SuppliedBlockIn.class.getDeclaredMethod("wayToTop",net.minecraft.client.Minecraft.class,BlockPos.class,AxisAlignedBB.class);way.setAccessible(true);
  require(!((List<BlockPos>)way.invoke(SuppliedBlockInHost.instance.core,V115Check.mc,roof,V115Check.self.getEntityBoundingBox())).contains(support),"roof-support search reintroduced a mined opening");
  Method aim=SuppliedBlockIn.class.getDeclaredMethod("aim",net.minecraft.client.Minecraft.class,BlockPos.class);aim.setAccessible(true);
  require(aim.invoke(SuppliedBlockInHost.instance.core,V115Check.mc,roof)==null,"final aim accepted a protected opening");
  dev.rice.module.Module cancelDig=new dev.rice.module.Module("blockin_dig_cancellation_fixture","Dig cancellation fixture","test",dev.rice.module.ModuleCategory.WORLD){
   @Override public boolean listens(int type){return type==23;}
   @Override public void onEvent(dev.rice.client.ClientContext c,dev.rice.client.SemanticEvent e){if(e.packet instanceof C07PacketPlayerDigging)e.cancelled=true;}
  };
  V115Check.manager.register(cancelDig);scene();V115Check.manager.applyNow(module,false);V115Check.manager.applyNow(cancelDig,true);world.blocks.put(hole,Blocks.stone.getDefaultState());
  int sent=V115Check.handler.sent.size();dig(hole,C07PacketPlayerDigging.Action.START_DESTROY_BLOCK);require(V115Check.handler.sent.size()==sent,"outbound dig cancellation changed");
  V115Check.manager.applyNow(cancelDig,false);world.blocks.remove(hole);V115Check.manager.applyNow(module,true);complete();require(world.getBlockState(hole).getBlock()!=Blocks.air,"cancelled dig created a protected opening");
  System.out.println("PASS: mined bed-access cells preserved before/during binding and across rebinds; other walls/roof complete; progress excludes holes; abort/cancellation/non-dig/pending-expiry/replacement/distance/world/player cleanup; roof/support/aim exclusions");
 }
 public static void main(String[] args)throws Exception{
  setup();bridgeChecks();require(((NumberSetting)module.findSetting("speed")).doubleValue()==1D,"Snowify default speed differs");require(module.getSettings().size()==3,"old Block In settings retained");
  java.util.List<dev.rice.module.RecoveredUiLayout.Control> controls=dev.rice.module.RecoveredUiLayout.controls(module);for(Setting<?> s:module.getSettings())require(controls.stream().anyMatch(c->c.setting==s),"new control omitted: "+s.getKey());
  require((module.descriptorFlags()&2)==0&&module.isHoldBinding(),"toggle/hold bind wiring missing");
  ((NumberSetting)module.findSetting("speed")).set(.4D);((BooleanSetting)module.findSetting("require_ground")).set(false);((BooleanSetting)module.findSetting("show_progress_bar")).set(false);
  com.google.gson.JsonObject saved=dev.rice.config.RecoveredConfiguration.serialize(V115Check.manager,dev.rice.config.RecoveredConfiguration.Scope.PROFILE,Collections.emptyList());String original=saved.toString();
  ((NumberSetting)module.findSetting("speed")).set(1D);((BooleanSetting)module.findSetting("require_ground")).set(true);((BooleanSetting)module.findSetting("show_progress_bar")).set(true);
  dev.rice.config.RecoveredConfiguration.apply(V115Check.manager,saved,dev.rice.config.RecoveredConfiguration.Scope.PROFILE);V115Check.manager.applyPendingChanges();
  require(Math.abs(((NumberSetting)module.findSetting("speed")).doubleValue()-.4)<.00001&&!((BooleanSetting)module.findSetting("require_ground")).isEnabled()&&!((BooleanSetting)module.findSetting("show_progress_bar")).isEnabled()&&saved.toString().equals(original),"source profile roundtrip changed controls/input");((BooleanSetting)module.findSetting("show_progress_bar")).set(true);
  scene();frame();require(controller.placed==1&&module.active,"supplied backend did not activate/place exactly once");
  SuppliedBlockInHost.instance.render();require(SuppliedBlockInHost.instance.progressText!=null&&SuppliedBlockInHost.instance.progressText.startsWith("Block In "),"source progress not rendered");
  for(int i=0;i<40&&module.isEnabled();i++)frame();
  for(EnumFacing f:new EnumFacing[]{EnumFacing.SOUTH,EnumFacing.WEST,EnumFacing.NORTH,EnumFacing.EAST})for(int y=0;y<2;y++)require(world.getBlockState(new BlockPos(0,64,0).offset(f).up(y)).getBlock()!=Blocks.air,"enclosure wall missing");
  require(world.getBlockState(new BlockPos(0,66,0)).getBlock()!=Blocks.air&&!module.isEnabled(),"roof/completion missing: placed="+controller.placed+" enabled="+module.isEnabled()+" roof="+world.getBlockState(new BlockPos(0,66,0)).getBlock());require(V115Check.self.inventory.currentItem==0&&!module.active,"completion retained slot/ownership");
  require(Math.abs(MathHelper.wrapAngleTo180_float(V115Check.self.rotationYaw))<.001&&V115Check.self.rotationPitch==0,"camera leaked: "+V115Check.self.rotationYaw+"/"+V115Check.self.rotationPitch);
  int enclosure=controller.placed;scene();controller.reject=true;int stack=V115Check.self.inventory.mainInventory[2].stackSize;frame();require(controller.placed==0&&V115Check.self.inventory.mainInventory[2].stackSize==stack,"rejected placement consumed block");controller.reject=false;frame();require(controller.placed==1,"rejected target not retried");
  scene();((NumberSetting)module.findSetting("speed")).set(.1D);frame();require(controller.placed==0,"slow rotation placed before reaching face");for(int i=0;i<100&&controller.placed==0;i++)frame();require(controller.placed>0,"slow rotation never converged");
  scene();V115Check.place(V115Check.self,.95,64,.95);V115Check.self.movementInput.moveForward=1;V115Check.self.movementInput.moveStrafe=0;V115Check.self.movementInput.jump=true;V115Check.self.movementInput.sneak=true;
  RotationService.beginFrame(V115Check.c,0,0);GameHooks.beforeEntities(world);GameHooks.mayuPlayerUpdateHead(V115Check.self);GameHooks.afterMovementInput(V115Check.self);
  require(!V115Check.self.movementInput.jump&&!V115Check.self.movementInput.sneak,"centring did not clear jump/sneak");
  float f=V115Check.self.movementInput.moveForward,s=V115Check.self.movementInput.moveStrafe;double yaw=Math.toRadians(SuppliedBlockInHost.RotationManager.get().getYaw());double x=s*Math.cos(yaw)-f*Math.sin(yaw),z=f*Math.cos(yaw)+s*Math.sin(yaw);require(x<0&&z<0,"centring points away from cell center");
  scene();V115Check.self.onGround=false;frame();require(!module.isEnabled()&&controller.placed==0,"Require ground ignored");
  scene();V115Check.self.inventory.mainInventory[2]=new ItemStack(Blocks.tnt,64);frame();require(!module.isEnabled()&&controller.placed==0,"invalid TNT accepted");
  scene();frame();V115Check.mc.currentScreen=new net.minecraft.client.gui.GuiChat();frame();require(!module.active&&V115Check.self.inventory.currentItem==0,"GUI pause retained slot");V115Check.mc.currentScreen=null;frame();require(module.active,"GUI resume missing");
  module.onWorldUnload(V115Check.c);require(!module.active&&V115Check.self.inventory.currentItem==0,"world cleanup retained state");V115Check.self.inventory.currentItem=4;frame();V115Check.manager.applyNow(module,false);require(V115Check.self.inventory.currentItem==4,"new session restored previous session slot");
  scene();((NumberSetting)module.findSetting("speed")).set(.1D);RotationService.beginFrame(V115Check.c,0,0);GameHooks.beforeEntities(world);GameHooks.mayuPlayerUpdateHead(V115Check.self);
  require(controller.placed==0&&SuppliedBlockInHost.spoofPitch(V115Check.self)!=V115Check.self.rotationPitch,"slow turn not exposed to vanilla packet construction before placement");
  V115Check.manager.applyNow(module,false);require(SuppliedBlockInHost.spoofYaw(V115Check.self)==V115Check.self.rotationYaw&&SuppliedBlockInHost.spoofPitch(V115Check.self)==V115Check.self.rotationPitch,"disabled packet construction retains aim");
  walkingCompatibilityChecks();
  wireChecks();
  miningOpeningChecks();
  scene();frame();V115Check.manager.applyNow(module,false);require(!myau.RotationState.isActive(),"Block In left movement rotation owned after disable");int attempts=controller.attempts;frame();require(controller.attempts==attempts&&!module.active,"disabled backend placed");
  scene();frame();V115Check.self.rotationYaw=57;V115Check.self.rotationPitch=13;V115Check.manager.applyNow(module,false);require(V115Check.self.rotationYaw==57&&V115Check.self.rotationPitch==13,"disable overwrote camera moved after frame");
  for(String old:new String[]{"dev/rice/module/impl/blockin/B4Aim.class","dev/rice/module/impl/blockin/B4Blocks.class","dev/rice/module/impl/BlockInModule$Node.class"})require(!Files.exists(Paths.get(args[0]).resolve(old)),"old backend shipped");
  System.out.println("PASS: supplied BlockIn core, three source settings and toggle/hold/UI; "+enclosure+"-block enclosure with roof/support planning; Snowify C08-before-normal-C03 order, no extra movement, matching wire aim/face and preserved position; reject/retry, slow turns, centring/jump/sneak, block filter/ground condition, progress, GUI/session/disable/slot/camera cleanup; old OpenB4 backend absent");
 }
}

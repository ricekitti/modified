import dev.rice.client.SemanticEvent;
import dev.rice.client.bednuker.NukerPatchPlanner;
import dev.rice.client.hook.GameHooks;
import dev.rice.client.mayu.MayuHost;
import dev.rice.client.rotation.RotationService;
import dev.rice.module.Module;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ChoiceSetting;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import myau.*;
import myau.host.MayuForge;
import myau.host.MayuMixins;
import myau.host.MayuShadows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import dev.rice.shaded.asm.*;
import dev.rice.shaded.asm.tree.*;

/** Real 1.8.9 classes, headless input and recorded HUD; never launches the client or injector.
 * Source/compiled-reference fidelity is checked separately by port-mayu and MayuBytecode.
 * The seeded matrix compares the production rotation hook to the upstream mixin's event/restore contract.
 */
public final class V119Check {
   public static final class World extends V115Check.World {
      int hitEffects;
      @Override public boolean setBlockState(BlockPos p,net.minecraft.block.state.IBlockState state,int flags){blocks.put(p,state);return true;}
      @Override public void playAuxSFX(int type,BlockPos p,int data){}
   }
   public static final class Effects extends net.minecraft.client.particle.EffectRenderer {
      Effects(){super(null,null);}
      @Override public void addBlockHitEffects(BlockPos p,EnumFacing face){((World)V115Check.world).hitEffects++;}
   }
   public static final class Network extends net.minecraft.network.NetworkManager {
      Network(){super(net.minecraft.network.EnumPacketDirection.CLIENTBOUND);}
      @Override public void sendPacket(Packet p){if(!GameHooks.beforeSend(this,p))V115Check.handler.sent.add(p);}
   }
   static void check(boolean ok, String message) { V115Check.check(ok, message); }
   static Object get(Object o, String name) throws Exception {
      for (Class<?> t=o.getClass();t!=null;t=t.getSuperclass()) try {
         Field f=t.getDeclaredField(name);f.setAccessible(true);return f.get(o);
      } catch (NoSuchFieldException next) { }
      throw new NoSuchFieldException(name);
   }
   static Object call(Object o,String name) throws Exception {
      Method m=o.getClass().getDeclaredMethod(name);m.setAccessible(true);return m.invoke(o);
   }
   static void enabled(String id,boolean value) { V115Check.manager.applyNow(V115Check.manager.find(id),value); }
   static void mode(String id,String key,int value) {Module m=V115Check.manager.find(id);((ChoiceSetting)m.findSetting(key)).set(value);m.afterSettingControl(key,V115Check.c);}
   static void flag(String id,String key,boolean value) { V115Check.flag(V115Check.manager.find(id),key,value); }
   static boolean hasStopPacket() {
      for(Packet<?> p:V115Check.handler.sent)if(p instanceof C07PacketPlayerDigging
         &&((C07PacketPlayerDigging)p).getStatus()==C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK)return true;
      return false;
   }
   static void nukerPatchPlanner() {
      V115Check.world.blocks.clear();
      final BlockPos foot=new BlockPos(0,65,0),head=new BlockPos(1,65,0);
      V115Check.world.blocks.put(foot,Blocks.bed.getDefaultState());
      V115Check.world.blocks.put(head,Blocks.bed.getDefaultState());
      for(int x=-2;x<=3;x++)for(int y=65;y<=67;y++)for(int z=-2;z<=2;z++) {
         BlockPos p=new BlockPos(x,y,z);
         int footDistance=Math.abs(x)+Math.abs(y-65)+Math.abs(z);
         int headDistance=Math.abs(x-1)+Math.abs(y-65)+Math.abs(z);
         int distance=Math.min(footDistance,headDistance);
         if(distance>0&&distance<=2)V115Check.world.blocks.put(p,Blocks.obsidian.getDefaultState());
      }
      final BlockPos inner=new BlockPos(0,65,-1),outer=new BlockPos(0,65,-2);
      V115Check.world.blocks.put(inner,Blocks.wool.getDefaultState());
      V115Check.world.blocks.put(outer,Blocks.wool.getDefaultState());
      NukerPatchPlanner.Model costs=new NukerPatchPlanner.Model(){
         public double breakTicks(BlockPos p){return V115Check.world.getBlockState(p).getBlock()==Blocks.wool?5.0:100.0;}
         public boolean canBreak(BlockPos p){return true;}
         public boolean canTarget(BlockPos p){return true;}
      };
      check(outer.equals(NukerPatchPlanner.next(V115Check.world,foot,head,costs)),"NUKER PATCH did not choose outer block of cheapest complete route");
      V115Check.world.blocks.remove(outer);
      check(inner.equals(NukerPatchPlanner.next(V115Check.world,foot,head,costs)),"NUKER PATCH did not advance inward on its chosen route");
      V115Check.world.blocks.remove(inner);
      check(foot.equals(NukerPatchPlanner.next(V115Check.world,foot,head,costs)),"NUKER PATCH did not advance to the exposed bed");
      V115Check.world.blocks.clear();
   }
   static void bed() throws Exception {
      enabled("bednuker",true);
      myau.BedNukerModule b=MayuHost.bedNuker();
      check(V115Check.manager.find("bednuker").getSettings().size()==12,"BedNuker settings");
      check(((ChoiceSetting)V115Check.manager.find("bednuker").findSetting("mode")).getOptions().equals(Arrays.asList("LEGIT","SWAP","NUKER PATCH")),"BedNuker mode options");
      BlockPos target=new BlockPos(1,65,0);V115Check.world.blocks.put(target,Blocks.bed.getDefaultState());
      V115Check.put(b,"targetBlock",target);V115Check.put(b,"interactionLocked",true);
      check(MayuHost.bedNukerControlling()&&MayuMixins.beforeRightClick(),"BedNuker interaction ownership");
      RotationService.beginFrame(V115Check.c,0,0);GameHooks.mayuPlayerUpdateHead(V115Check.self);
      check(RotationState.isActive()&&RotationState.movementPriority()==5,"BedNuker rotations");
      V115Check.controller.placements.clear();
      RotationService.beginFrame(V115Check.c,0,0);GameHooks.mayuPlayerUpdateHead(V115Check.self);
      check(V115Check.controller.placements.isEmpty()&&RotationState.movementPriority()==5,"Scaffold competed with BedNuker");
      
      mode("bednuker","ignore_velocity",1);V115Check.self.motionY=-.1;
      check(MayuMixins.beforeSetVelocity(.3,.4,.5,V115Check.self)&&V115Check.self.motionY==-.1,"BedNuker CANCEL velocity");
      check(!MayuMixins.beforeSetVelocity(.3,-.4,.5,V115Check.self),"BedNuker cancelled downward velocity");
      mode("bednuker","ignore_velocity",2);
      check(GameHooks.receivePacket(null,new S12PacketEntityVelocity(V115Check.self.getEntityId(),.2,.4,.2)),"BedNuker DELAY failed");
      check(MyauClient.incomingPacketBuffer.owner()==IncomingBufferOwner.BED_NUKER&&MyauClient.incomingPacketBuffer.queuedPackets.size()==1,"incoming queue ownership");
      // Disconnected client: upstream disable must discard the held packets.
      V115Check.put(V115Check.self,"sendQueue",null);
      enabled("bednuker",false);
      V115Check.put(V115Check.self,"sendQueue",V115Check.handler);
      check(!b.isControllingInteractions()&&MyauClient.incomingPacketBuffer.queuedPackets.isEmpty()&&MyauClient.incomingPacketBuffer.owner()==IncomingBufferOwner.NONE,"BedNuker cleanup");
      check(!MayuMixins.beforeRightClick(),"disabled BedNuker interaction gate");
      // Find a real bed, start digging, accumulate vanilla hardness, stop digging and remove it.
      flag("bednuker","whitelist",false);flag("bednuker","surroundings",false);
      mode("bednuker","ignore_velocity",0);enabled("bednuker",true);
      V115Check.self.onGround=true;V115Check.self.motionY=0;V115Check.handler.sent.clear();
      // Also call directly once so an exception cannot be hidden by upstream EventBus's listener catch.
      b.onPreTick(new PreTickEvent());
      for(int i=0;i<80&&!V115Check.world.isAirBlock(target);i++) MayuMixins.beforeRunTick(V115Check.mc);
      check(V115Check.world.isAirBlock(target),"BedNuker never completed bed break: "+V115Check.describe(b)+" candidate="+call(b,"findTargetBlock")+" packets="+V115Check.handler.sent.size());
      boolean start=false,stop=false;
      for(Packet<?> p:V115Check.handler.sent)if(p instanceof C07PacketPlayerDigging) {
         C07PacketPlayerDigging.Action action=((C07PacketPlayerDigging)p).getStatus();
         if(action==C07PacketPlayerDigging.Action.START_DESTROY_BLOCK)start=true;
         if(action==C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK){check(start,"STOP sent before START");stop=true;}
      }
      check(start&&stop&&((World)V115Check.world).hitEffects>0,"BedNuker packet/effect path incomplete");
      MayuMixins.beforeRunTick(V115Check.mc);check(!b.isControllingInteractions(),"BedNuker retained completed target");
      enabled("bednuker",false);

      // NUKER PATCH must not phantom-remove a block when the server rejects STOP; it waits for a real world update.
      BlockPos confirmedTarget=new BlockPos(2,65,0);V115Check.world.blocks.put(confirmedTarget,Blocks.bed.getDefaultState());
      mode("bednuker","mode",2);flag("bednuker","surroundings",false);enabled("bednuker",true);
      V115Check.put(b,"targetBlock",confirmedTarget);V115Check.put(b,"targetIsBed",true);
      V115Check.put(b,"interactionLocked",true);V115Check.put(b,"breakPhase",0);
      V115Check.handler.sent.clear();
      for(int i=0;i<100&&!hasStopPacket();i++)MayuMixins.beforeRunTick(V115Check.mc);
      check(hasStopPacket(),"NUKER PATCH never sent STOP");
      check(!V115Check.world.isAirBlock(confirmedTarget),"NUKER PATCH removed a server-owned block locally");
      V115Check.world.blocks.remove(confirmedTarget);
      MayuMixins.beforeRunTick(V115Check.mc);
      check(get(b,"targetBlock")==null,"NUKER PATCH did not accept server confirmation and replan");
      enabled("bednuker",false);mode("bednuker","mode",0);
   }


   public static final class Font extends FontRenderer {
      public final List<String> text=new ArrayList<>();
      Font(){super(null,null,null,false);}
      @Override public int drawString(String s,float x,float y,int color,boolean shadow){text.add(s);return 0;}
   }
   public static void scale(float x,float y,float z){V115GlStub.calls++;}
   public static void blendFunc(int x,int y){V115GlStub.calls++;}
   public static void main(String[] args)throws Exception {
      V115Check.setup();
      World w=V115Check.blank(World.class);w.blocks=new HashMap<>();
      V115Check.put(w,"worldAccesses",new ArrayList<>());
      V115Check.put(w,"worldBorder",new net.minecraft.world.border.WorldBorder());
      V115Check.put(w,"loadedEntityList",new ArrayList<>());
      V115Check.world=w;V115Check.put(V115Check.mc,"theWorld",w);V115Check.put(V115Check.self,"worldObj",w);
      V115Check.put(V115Check.mc,"effectRenderer",V115Check.blank(Effects.class));
      V115Check.put(V115Check.handler,"netManager",V115Check.blank(Network.class));
      // OpenB4 builds its cache from vanilla key descriptions and GameSettings.keyBindings.
      List<KeyBinding> keys=new ArrayList<>();
      for(String name:new String[]{"Forward","Back","Left","Right","Jump","Sneak","Sprint","Attack","UseItem"}) {
         KeyBinding key=(KeyBinding)get(V115Check.mc.gameSettings,"keyBind"+name);
         V115Check.put(key,"keyDescription","key."+(name.equals("UseItem")?"use":name.toLowerCase()));keys.add(key);
      }
      for(int i=0;i<9;i++){KeyBinding key=V115Check.mc.gameSettings.keyBindsHotbar[i];V115Check.put(key,"keyDescription","key.hotbar."+(i+1));keys.add(key);}
      V115Check.put(V115Check.mc.gameSettings,"keyBindings",keys.toArray(new KeyBinding[0]));
      for(Module m:V115Check.manager.all())if(m.isEnabled())V115Check.manager.applyNow(m,false);
      nukerPatchPlanner();
      bed();
      System.out.println("PASS: BedNuker NUKER PATCH cheapest layered route/server confirmation plus retained legacy rotation/velocity/cleanup");
   }
}

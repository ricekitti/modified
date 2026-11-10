import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.hook.GameHooks;
import dev.rice.client.mayu.ClutchGeometry;
import dev.rice.client.mayu.MayuPlacement;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;

/** Uses vanilla PlayerControllerMP + ItemBlock, actual collision boxes, and the production script/packet hooks. */
public final class V122Check {
   public static final class World extends V115Check.World {
      World(){super();}
      @Override public boolean setBlockState(BlockPos p,IBlockState s,int flags){blocks.put(p,s);return true;}
      @Override public void playSoundEffect(double x,double y,double z,String name,float volume,float pitch){}
      @Override public List<AxisAlignedBB> getCollidingBoundingBoxes(net.minecraft.entity.Entity entity,AxisAlignedBB query){
         List<AxisAlignedBB> out=new ArrayList<>();
         for(Map.Entry<BlockPos,IBlockState> row:blocks.entrySet())
            row.getValue().getBlock().addCollisionBoxesToList(this,row.getKey(),row.getValue(),query,out,entity);
         return out;
      }
   }
   static final class Cancel extends Module {
      boolean cancel, hold;
      Cancel(){super("v122-check-cancel","v122 check cancel","test",ModuleCategory.WORLD);}
      @Override public boolean listens(int t){return t==23;}
      @Override public void onEvent(ClientContext c,SemanticEvent e){
         if(e.packet instanceof C08PacketPlayerBlockPlacement && (cancel||hold)) {
            if(hold) try {held().add(e.packet);}catch(Exception error){throw new IllegalStateException(error);}
            e.cancelled=true;
         }
      }
   }
   @SuppressWarnings("unchecked") static List<Packet<?>> held()throws Exception {
      Field f=dev.rice.client.combat.openb4.OpenB4KillAuraBackend.class.getDeclaredField("held");f.setAccessible(true);return (List<Packet<?>>)f.get(null);
   }
   static void check(boolean b,String s){V115Check.check(b,s);}
   static void setup()throws Exception {
      V115Check.setup();
      World w=V115Check.blank(World.class);w.blocks=new HashMap<>();
      V115Check.put(w,"worldAccesses",new ArrayList<>());V115Check.put(w,"loadedEntityList",new ArrayList<>());
      V115Check.put(w,"worldBorder",new net.minecraft.world.border.WorldBorder());
      V115Check.world=w;V115Check.put(V115Check.mc,"theWorld",w);V115Check.put(V115Check.self,"worldObj",w);
      List<KeyBinding> keys=new ArrayList<>();
      for(String n:new String[]{"Forward","Back","Left","Right","Jump","Sneak","Sprint","Attack","UseItem"}) {
         KeyBinding k=(KeyBinding)V119Check.get(V115Check.mc.gameSettings,"keyBind"+n);
         V115Check.put(k,"keyDescription","key."+(n.equals("UseItem")?"use":n.toLowerCase()));keys.add(k);
      }
      for(int i=0;i<9;i++){KeyBinding k=V115Check.mc.gameSettings.keyBindsHotbar[i];V115Check.put(k,"keyDescription","key.hotbar."+(i+1));keys.add(k);}
      V115Check.put(V115Check.mc.gameSettings,"keyBindings",keys.toArray(new KeyBinding[0]));
      for(Module m:V115Check.manager.all())if(m.isEnabled())V115Check.manager.applyNow(m,false);
   }
   static void geometry()throws Exception {
      V115Check.world.blocks.clear();V115Check.place(V115Check.self,.5,65,.5);
      for(net.minecraft.block.Block b:new net.minecraft.block.Block[]{Blocks.tallgrass,Blocks.water,Blocks.torch}){
         V115Check.world.blocks.put(new BlockPos(0,63,0),b.getDefaultState());
         check(ClutchGeometry.fallDistance()==-1,"non-collidable block counted as support: "+b);
         check(ClutchGeometry.surface(.5,65,.5,.3)<-1.0E8,"non-collidable landing surface: "+b);
         check(!ClutchGeometry.blocked(.5,63,.5,.3,1.8),"non-collidable body obstruction: "+b);
      }
      V115Check.world.blocks.put(new BlockPos(0,63,0),Blocks.stone_slab.getDefaultState());
      check(Math.abs(ClutchGeometry.surface(.5,65,.5,.3)-63.5)<1.0E-6,"slab collision height");
      check(ClutchGeometry.fallDistance()==1,"solid slab not found below player");
      V115Check.world.blocks.clear();V115Check.world.blocks.put(new BlockPos(0,63,0),Blocks.oak_fence.getDefaultState());
      check(Math.abs(ClutchGeometry.surface(.5,65,.5,.3)-64.5)<1.0E-6,"thin fence support missed between corner samples");
      check(ClutchGeometry.blocked(.5,63.9,.5,.3,1.8),"actual fence collision ignored");
      System.out.println("PASS: collision-shape prediction ignores grass/water/torches and detects slabs/thin fences");
   }
   static void placements()throws Exception {
      PlayerControllerMP controller=new PlayerControllerMP(V115Check.mc,V115Check.handler);
      V115Check.put(controller,"currentGameType",net.minecraft.world.WorldSettings.GameType.SURVIVAL);
      Cancel cancel=new Cancel();V115Check.manager.register(cancel);V115Check.manager.applyNow(cancel,true);
      BlockPos support=new BlockPos(0,63,0),target=support.east();Vec3 hit=new Vec3(1,63.6,.5);
      V115Check.world.blocks.clear();V115Check.world.blocks.put(support,Blocks.stone.getDefaultState());
      V115Check.place(V115Check.self,1.5,65,.5);V115Check.self.inventory.currentItem=2;
      ItemStack stack=new ItemStack(Blocks.stone,64);V115Check.self.inventory.setInventorySlotContents(2,stack);
      cancel.cancel=true;V115Check.handler.sent.clear();
      check(!MayuPlacement.place(controller,V115Check.self,V115Check.world,stack,support,EnumFacing.EAST,hit),"cancelled C08 reported successful placement");
      check(V115Check.world.isAirBlock(target)&&stack.stackSize==64,"cancelled C08 left a ghost block or consumed inventory");
      check(V115Check.handler.sent.stream().noneMatch(p->p instanceof C08PacketPlayerBlockPlacement),"cancelled placement reached network");
      check(V115Check.handler.sent.stream().anyMatch(p->p instanceof C09PacketHeldItemChange&&((C09PacketHeldItemChange)p).getSlotId()==2),"vanilla failed to synchronize the block slot before placement");
      // Same controller succeeds on retry: no stale transaction state and vanilla performs the block/inventory mutation.
      cancel.cancel=false;
      check(MayuPlacement.place(controller,V115Check.self,V115Check.world,stack,support,EnumFacing.EAST,hit),"valid retry failed");
      check(V115Check.world.getBlockState(target).getBlock()==Blocks.stone&&stack.stackSize==63,"vanilla placement did not commit");
      check(V115Check.handler.sent.stream().anyMatch(p->p instanceof C08PacketPlayerBlockPlacement),"valid placement packet missing");
      check(V115Check.handler.sent.get(0) instanceof C09PacketHeldItemChange&&V115Check.handler.sent.get(1) instanceof C08PacketPlayerBlockPlacement,"slot/placement packet order changed");
      // Intentionally held C08s must keep their prediction; rollback here would duplicate inventory on replay.
      V115Check.world.blocks.remove(target);cancel.hold=true;
      check(MayuPlacement.place(controller,V115Check.self,V115Check.world,stack,support,EnumFacing.EAST,hit),"ordered hold treated as rejection");
      check(stack.stackSize==62&&!V115Check.world.isAirBlock(target)&&held().size()==1,"held placement prediction changed");
      held().clear();cancel.hold=false;V115Check.manager.applyNow(cancel,false);
      System.out.println("PASS: vanilla controller/ItemBlock placement commits once; rejected C08 rolls back block+stack; retry and ordered hold intact");
   }
   public static void main(String[] args)throws Exception {setup();geometry();placements();}
}

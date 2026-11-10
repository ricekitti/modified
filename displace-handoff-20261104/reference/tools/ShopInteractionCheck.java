import dev.rice.client.*;
import dev.rice.client.action.*;
import dev.rice.client.hook.RayQueryAdapter;
import dev.rice.config.InitialSettings;
import dev.rice.module.Module;
import dev.rice.module.ModuleManager;
import dev.rice.module.impl.*;
import dev.rice.setting.*;
import dev.rice.shaded.asm.*;
import dev.rice.shaded.asm.tree.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.settings.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;

/** NPC ray selection, input ownership, item switching and combat preservation without a game launch. */
public final class ShopInteractionCheck {
   static Object unsafe; static Method allocate;
   public static boolean isButtonDown(int button) { return button == 1; }
   public static int getEventButton() { return 1; }
   public static boolean getEventButtonState() { return true; }
   static <T>T blank(Class<T> t)throws Exception { return t.cast(allocate.invoke(unsafe,t)); }
   static void put(Object o,String name,Object value)throws Exception {
      for(Class<?> t=o.getClass();t!=null;t=t.getSuperclass())try{Field f=t.getDeclaredField(name);f.setAccessible(true);f.set(o,value);return;}catch(NoSuchFieldException next){}
      throw new NoSuchFieldException(name);
   }
   static Object get(Object o,String name)throws Exception {Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
   static void check(boolean pass,String message){if(!pass)throw new AssertionError(message);}
   static void flag(Module m,String key,boolean value){((BooleanSetting)m.findSetting(key)).set(value);}
   public static final class World extends WorldClient {
      MovingObjectPosition obstruction;
      World(){super(null,null,0,null,null);}
      @Override public MovingObjectPosition rayTraceBlocks(Vec3 a,Vec3 b,boolean l,boolean n,boolean m){return obstruction;}
      @Override public IBlockState getBlockState(BlockPos p){return Blocks.air.getDefaultState();}
   }
   public static final class Controller extends PlayerControllerMP {
      Controller(){super(null,null);}
      @Override public float getBlockReachDistance(){return 4.5F;}
      @Override public boolean extendedReach(){return false;}
      @Override public void onStoppedUsingItem(EntityPlayer p){p.stopUsingItem();}
   }
   public static final class Other extends EntityOtherPlayerMP {
      String label;
      Other(){super(null,null);}
      @Override public String getName(){return label;}
      @Override public IChatComponent getDisplayName(){return new ChatComponentText(label);}
      @Override public boolean canBeCollidedWith(){return true;}
   }
   static Other other(World w,String label,double z)throws Exception {
      Other p=blank(Other.class);DataWatcher watcher=new DataWatcher(p);watcher.addObject(0,(byte)0);watcher.addObject(6,20F);put(p,"dataWatcher",watcher);p.label=label;p.worldObj=w;p.posZ=z;p.setEntityBoundingBox(new AxisAlignedBB(-.3,0,z-.3,.3,1.9,z+.3));return p;
   }
   public static void main(String[] args)throws Exception {
      Bootstrap.register();Class<?> u=Class.forName("sun.misc.Unsafe");Field uf=u.getDeclaredField("theUnsafe");uf.setAccessible(true);unsafe=uf.get(null);allocate=u.getMethod("allocateInstance",Class.class);
      final Path classes=Paths.get(args[0]);ClassLoader loader=new ClassLoader(ShopInteractionCheck.class.getClassLoader()){
         @Override protected synchronized Class<?> loadClass(String n,boolean resolve)throws ClassNotFoundException {
            if(!n.equals("dev.rice.module.impl.AutoBlockModule"))return super.loadClass(n,resolve);
            Class<?> old=findLoadedClass(n);if(old!=null)return old;
            try {ClassNode node=new ClassNode();new ClassReader(Files.readAllBytes(classes.resolve(n.replace('.','/')+".class"))).accept(node,0);
               for(MethodNode m:node.methods)for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)i;if(call.owner.equals("org/lwjgl/input/Mouse"))call.owner="ShopInteractionCheck";}
               ClassWriter writer=new ClassWriter(0);node.accept(writer);byte[] data=writer.toByteArray();return defineClass(n,data,0,data.length);
            }catch(Exception e){throw new ClassNotFoundException(n,e);}
         }
      };
      Minecraft mc=blank(Minecraft.class);World world=blank(World.class);put(world,"loadedEntityList",new ArrayList<Entity>());put(world,"playerEntities",new ArrayList<EntityPlayer>());
      EntityPlayerSP self=blank(EntityPlayerSP.class);self.worldObj=world;self.inventory=new InventoryPlayer(self);self.capabilities=new PlayerCapabilities();self.movementInput=new MovementInput();self.setEntityBoundingBox(new AxisAlignedBB(-.3,0,-.3,.3,1.8,.3));
      DataWatcher watcher=new DataWatcher(self);watcher.addObject(0,(byte)0);watcher.addObject(6,20F);put(self,"dataWatcher",watcher);
      put(mc,"thePlayer",self);put(mc,"theWorld",world);put(mc,"renderViewEntity",self);put(mc,"playerController",blank(Controller.class));
      GameSettings keys=blank(GameSettings.class);keys.keyBindUseItem=new KeyBinding("shop.use",-99,"test");keys.keyBindAttack=new KeyBinding("shop.attack",-100,"test");put(mc,"gameSettings",keys);
      Field singleton=Minecraft.class.getDeclaredField("theMinecraft");singleton.setAccessible(true);singleton.set(null,mc);
      ClientContext c=new ClientContext(mc);ModuleManager manager=new ModuleManager(c);WeaponsModule weapons=new WeaponsModule();PiercingModule piercing=new PiercingModule(weapons);manager.register(piercing);
      Module ab=(Module)loader.loadClass("dev.rice.module.impl.AutoBlockModule").getConstructor(ModuleManager.class).newInstance(manager);manager.register(ab);InitialSettings.applyMissing(manager);
      manager.requestEnabled(piercing,true);manager.requestEnabled(ab,true);manager.applyPendingChanges();flag(piercing,"require_weapon",false);flag(piercing,"require_inside_bb",false);flag(piercing,"bots",true);
      Other shop=other(world,"Shop",2),enemy=other(world,"Enemy",2.8);world.loadedEntityList.add(shop);world.loadedEntityList.add(enemy);world.playerEntities.add(shop);world.playerEntities.add(enemy);self.setEntityId(1);shop.setEntityId(2);enemy.setEntityId(3);RelationshipService.set(shop,2,true);
      SemanticEvent query=new SemanticEvent(8);query.queryOverride=true;query.distance=3;
      check(RayQueryAdapter.update(manager,query)&&mc.objectMouseOver.entityHit==shop,"Piercing must retain nearest NPC even when bots are filtered");
      world.obstruction=new MovingObjectPosition(new Vec3(0,1.62,1),EnumFacing.NORTH,BlockPos.ORIGIN);
      check(InteractionTarget.find(c).typeOfHit==MovingObjectPosition.MovingObjectType.BLOCK,"NPC behind wall must not become interaction target");world.obstruction=null;
      self.inventory.mainInventory[0]=new ItemStack(Items.diamond_sword);KeyBinding.setKeyBindState(-99,true);put(ab,"blocking",true);put(ab,"lagActive",true);put(ab,"useOwned",true);
      put(self,"itemInUse",self.getHeldItem());ActionAccess.useCount(self,72000);
      ab.onClientTickStart(c);check(!self.isUsingItem(),"Shop handoff left vanilla sword use active");check(!(Boolean)get(ab,"blocking")&&!(Boolean)get(ab,"lagActive"),"Shop must release block and packet lag");check(keys.keyBindUseItem.isKeyDown(),"Shop handoff must preserve held RMB");
      put(ab,"lagActive",true);put(ab,"useOwned",false);put(self,"itemInUse",self.getHeldItem());ActionAccess.useCount(self,72000);
      ab.onClientTickStart(c);check(!self.isUsingItem(),"Lag-only shop handoff left sword use active");
      check(!(Boolean)ab.getClass().getMethod("swallowMouseEvent",ClientContext.class).invoke(ab,c),"Shop mouse press swallowed");
      SemanticEvent use=new SemanticEvent(14);use.active=false;ab.onEvent(c,use);check(!use.cancelled,"Shop interaction cancelled");
      // Switching away after the fake sword animation must not zero another item's use timer.
      world.loadedEntityList.clear();world.playerEntities.clear();mc.objectMouseOver=null;put(ab,"blocking",true);ab.onRenderTickStart(c);
      ItemStack food=new ItemStack(Items.apple);self.inventory.mainInventory[0]=food;put(self,"itemInUse",food);ActionAccess.useCount(self,25);KeyBinding.setKeyBindState(-99,true);
      ab.onClientTickStart(c);ab.onRenderTickStart(c);ab.onClientTickStart(c);
      check(self.getItemInUseCount()==25,"Switching items cleared food use count");check(keys.keyBindUseItem.isKeyDown(),"Non-sword use key forced up");
      use=new SemanticEvent(14);use.active=false;ab.onEvent(c,use);check(!use.cancelled,"Non-sword right click cancelled");
      // Combat keeps the normal RMB interception when a real enemy is under the crosshair.
      self.inventory.mainInventory[0]=new ItemStack(Items.diamond_sword);put(self,"itemInUse",null);world.loadedEntityList.add(enemy);world.playerEntities.add(enemy);self.movementInput.moveForward=1;
      use=new SemanticEvent(14);use.active=false;ab.onEvent(c,use);check(use.cancelled,"Combat block cycle no longer owns RMB: enabled="+ab.isEnabled()+", sword="+ActionAccess.canBlock(self.getHeldItem())+", npc="+InteractionTarget.isNpc(c,enemy));
      mc.objectMouseOver=null;RayQueryAdapter.update(manager,query);check(mc.objectMouseOver!=null&&mc.objectMouseOver.entityHit==enemy,"Piercing lost ordinary enemy target");
      System.out.println("PASS: Piercing NPC/occlusion, shop mouse/use handoff, lag cleanup, other-item use and combat interception");
   }
}

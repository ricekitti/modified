import dev.rice.client.*;
import dev.rice.client.packet.PacketPipeline;
import dev.rice.client.velocity.jade.*;
import dev.rice.config.InitialSettings;
import dev.rice.config.SettingCodec;
import dev.rice.module.Module;
import dev.rice.module.ModuleManager;
import dev.rice.module.RecoveredUiLayout;
import dev.rice.module.impl.*;
import dev.rice.setting.*;
import dev.rice.shaded.asm.*;
import dev.rice.shaded.asm.tree.*;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.LongSupplier;
import java.util.jar.JarFile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.settings.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.*;
import net.minecraft.util.*;

/** Deterministic Hard Reset timing/queue/input integration plus v76 mode comparisons. */
public final class JadeVelocityCheck {
   static Object unsafe; static Method allocate;
   public static boolean mouse, forwardPhysical, jumpPhysical;
   public static List<Packet<?>> wire;
   public static boolean physical(KeyBinding key) { return key.getKeyCode()==17?forwardPhysical:key.getKeyCode()==57?jumpPhysical:mouse; }
   public static void restore(KeyBinding key) { KeyState.set(key,physical(key)); }
   public static <T>T blank(Class<T> type)throws Exception { return type.cast(allocate.invoke(unsafe,type)); }
   public static void put(Object object,String name,Object value)throws Exception {
      for(Class<?> type=object.getClass();type!=null;type=type.getSuperclass())try{Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(object,value);return;}catch(NoSuchFieldException next){}
      throw new NoSuchFieldException(name);
   }
   static Object get(Object object,String name)throws Exception {Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
   static void check(boolean pass,String message){if(!pass)throw new AssertionError(message);}
   static void flag(Module module,String key,boolean value){((BooleanSetting)module.findSetting(key)).set(value);}
   static void number(Module module,String key,double value){((NumberSetting)module.findSetting(key)).set(value);}
   static void mode(Module module,int value){((ChoiceSetting)module.findSetting("mode")).set(value);}
   public static class Player extends EntityPlayerSP {
      public boolean burning, sprinting;Player(){super(null,null,null,null);}
      @Override public boolean isSprinting(){return sprinting;}
      @Override public boolean isBurning(){return burning;}
      @Override public UUID getUniqueID(){return new UUID(0,getEntityId());}
   }
   public static class World extends WorldClient {
      World(){super(null,null,0,null,null);}
      @Override public Entity getEntityByID(int id){for(Object value:loadedEntityList)if(((Entity)value).getEntityId()==id)return (Entity)value;return null;}
   }
   public static class Backend extends JadeHardResetBackend {
      public Backend(ClientContext c,VelocityModule settings,ModuleManager modules,LongSupplier clock){super(c,settings,modules,clock);}
      @Override protected void deliver(Packet<?> packet){JadeVelocityCheck.wire.add(packet);}
   }
   static ClassLoader loader(Path classes,Path baseline)throws Exception {
      return new ClassLoader(JadeVelocityCheck.class.getClassLoader()){
         @Override protected synchronized Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException {
            if(!name.equals("dev.rice.module.impl.VelocityModule")&&!name.startsWith("dev.rice.client.velocity.jade.")&&!name.equals("JadeVelocityCheck$Backend"))return super.loadClass(name,resolve);
            Class<?> old=findLoadedClass(name);if(old!=null)return old;
            try {
               byte[] data;
               if(name.equals("JadeVelocityCheck$Backend"))data=Files.readAllBytes(Paths.get(JadeVelocityCheck.class.getProtectionDomain().getCodeSource().getLocation().toURI()).resolve(name+".class"));
               else if(baseline!=null&&name.equals("dev.rice.module.impl.VelocityModule"))try(JarFile jar=new JarFile(baseline.toFile())){data=read(jar.getInputStream(jar.getJarEntry(name.replace('.','/')+".class")));}
               else data=Files.readAllBytes(classes.resolve(name.replace('.','/')+".class"));
               ClassNode node=new ClassNode();new ClassReader(data).accept(node,0);
               for(MethodNode m:node.methods)for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)i;
                  if(call.owner.equals("dev/rice/client/KeyState")&&(call.name.equals("physical")||call.name.equals("restore")))call.owner="JadeVelocityCheck";
               }
               ClassWriter writer=new ClassWriter(0);node.accept(writer);data=writer.toByteArray();Class<?> result=defineClass(name,data,0,data.length);if(resolve)resolveClass(result);return result;
            }catch(Exception failure){throw new ClassNotFoundException(name,failure);}
         }
      };
   }
   static byte[] read(java.io.InputStream stream)throws Exception {try(java.io.InputStream in=stream;java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()){byte[] bytes=new byte[8192];int n;while((n=in.read(bytes))!=-1)out.write(bytes,0,n);return out.toByteArray();}}
   static Player player(World world,int id,double z)throws Exception {
      Player p=blank(Player.class);p.sprinting=true;p.setEntityId(id);p.worldObj=world;p.inventory=new InventoryPlayer(p);p.capabilities=new PlayerCapabilities();p.movementInput=new MovementInput();
      DataWatcher watcher=new DataWatcher(p);watcher.addObject(0,(byte)0);watcher.addObject(6,20F);put(p,"dataWatcher",watcher);put(p,"activePotionsMap",new HashMap<Integer,PotionEffect>());
      p.posY=65;p.posZ=z;p.setEntityBoundingBox(new AxisAlignedBB(-.3,65,z-.3,.3,66.8,z+.3));return p;
   }
   static class Fixture {
      long now=1000;Minecraft mc;World world;Player self,target;ClientContext c;ModuleManager manager;Module velocity;Object backend;EmbeddedChannel channel;PacketPipeline pipeline;List<Packet<?>> delivered=new ArrayList<>();
      Fixture(ClassLoader loader,int mode,boolean inject)throws Exception {
         mouse=forwardPhysical=jumpPhysical=false;wire=delivered;mc=blank(Minecraft.class);world=blank(World.class);put(world,"playerEntities",new ArrayList<EntityPlayer>());put(world,"loadedEntityList",new ArrayList<Entity>());
         self=player(world,1,0);target=player(world,2,3);world.playerEntities.add(target);world.loadedEntityList.add(target);world.loadedEntityList.add(self);
         put(mc,"thePlayer",self);put(mc,"theWorld",world);put(mc,"inGameHasFocus",true);put(self,"sendQueue",blank(NetHandlerPlayClient.class));
         GameSettings keys=blank(GameSettings.class);keys.keyBindJump=new KeyBinding("test.jump",57,"test");keys.keyBindForward=new KeyBinding("test.forward",17,"test");keys.keyBindAttack=new KeyBinding("test.attack",-100,"test");put(mc,"gameSettings",keys);mc.objectMouseOver=new MovingObjectPosition(target,new Vec3(0,66,2.7));
         c=new ClientContext(mc);manager=new ModuleManager(c);velocity=(Module)loader.loadClass("dev.rice.module.impl.VelocityModule").getConstructor(WeaponsModule.class,ModuleManager.class).newInstance(new WeaponsModule(),manager);
         manager.register(velocity);InitialSettings.applyMissing(manager);mode(velocity,mode);manager.requestEnabled(velocity,true);manager.applyPendingChanges();
         if(inject){backend=loader.loadClass("JadeVelocityCheck$Backend").getConstructor(ClientContext.class,velocity.getClass(),ModuleManager.class,LongSupplier.class).newInstance(c,velocity,manager,(LongSupplier)()->now);put(velocity,"watchdog",backend);}
         pipeline=new PacketPipeline(()->now,(packet,out,age,sequence)->{SemanticEvent e=new SemanticEvent(out?3:2);e.packet=(Packet<?>)packet;e.packetAgeMillis=age;e.packetSequence=sequence;manager.dispatch(e);return e.dispatchMode;});channel=new EmbeddedChannel(pipeline);
      }
      void disableDelayConditions(){flag(velocity,"watchdog_in_air",false);flag(velocity,"watchdog_looking_at_player",false);flag(velocity,"watchdog_require_left_mouse",false);}
      void packet(Packet<?> p){channel.writeInbound(p);}
      void start(){manager.clientTick(true);}
      void pre(){manager.dispatch(new SemanticEvent(17));}
      void post(){manager.dispatch(new SemanticEvent(18));}
      int retained()throws Exception{return (Integer)backend.getClass().getMethod("retained").invoke(backend);}
      boolean delaying()throws Exception{return (Boolean)backend.getClass().getMethod("isInboundDelaying").invoke(backend);}
      void close(){manager.worldUnload();channel.finish();Object packet;while((packet=channel.readInbound())!=null)io.netty.util.ReferenceCountUtil.release(packet);while((packet=channel.readOutbound())!=null)io.netty.util.ReferenceCountUtil.release(packet);}
   }
   static S12PacketEntityVelocity kb(int id,double speed){return new S12PacketEntityVelocity(id,speed,.4,0);}
   static void timeout() {
      TimeoutCondition timer=new TimeoutCondition(100,0);check(!timer.isOpen(100)&&timer.isOpen(101),"Timeout minimum must be 1 ms");
      check(JadeHardResetBackend.forwardHoldMillis(.45)==180&&JadeHardResetBackend.forwardHoldMillis(.6)==270&&JadeHardResetBackend.forwardHoldMillis(.75)==360,"Jade forward-hold interpolation drifted");
   }
   static void forwardOnly(ClassLoader loader)throws Exception {
      Fixture defaults=new Fixture(loader,4,true);
      Setting<?> toggle=defaults.velocity.findSetting("watchdog_require_forward");
      check(toggle instanceof BooleanSetting&&!((BooleanSetting)toggle).isEnabled(),"Forward option must default off for old profiles");
      check(toggle.getLabel().equals("Only while moving forward"),"Forward option label mismatch");
      check(RecoveredUiLayout.controls(defaults.velocity).stream().anyMatch(c->c.setting==toggle),"Watchdog forward option missing from menu");
      flag(defaults.velocity,"watchdog_delay",false);check(defaults.velocity.settingVisible("watchdog_require_forward"),"Forward option hidden when Delay off");
      for(int mode=0;mode<4;mode++){mode(defaults.velocity,mode);check(!defaults.velocity.settingVisible("watchdog_require_forward"),"Watchdog forward option leaked into legacy mode");}
      mode(defaults.velocity,4);SettingCodec.decode(toggle,new com.google.gson.JsonPrimitive(true));check(((BooleanSetting)toggle).isEnabled()&&SettingCodec.encode(toggle).getAsBoolean(),"Forward option config round-trip failed");defaults.close();
      // Fresh movement input wins over stale entity movement and knockback drift.
      for(float forward:new float[]{0,-1})for(float strafe:new float[]{-1,0,1}) {
         Fixture t=new Fixture(loader,4,true);t.disableDelayConditions();flag(t.velocity,"watchdog_require_forward",true);
         t.self.movementInput=new MovementInput();t.self.movementInput.moveForward=forward;t.self.movementInput.moveStrafe=strafe;t.self.moveForward=1;t.self.motionZ=.6;
         S12PacketEntityVelocity packet=kb(1,.6);t.packet(packet);check(t.channel.readInbound()==packet&&!t.delaying(),"Non-forward movement triggered Watchdog");t.now=1300;t.pre();check(!t.mc.gameSettings.keyBindJump.isKeyDown(),"Non-forward movement scheduled a reset");t.close();
      }
      for(float strafe:new float[]{-1,0,1}) {
         Fixture t=new Fixture(loader,4,true);t.disableDelayConditions();flag(t.velocity,"watchdog_require_forward",true);
         t.self.movementInput=new MovementInput();t.self.movementInput.moveForward=1;t.self.movementInput.moveStrafe=strafe;
         forwardPhysical=true;KeyState.set(t.mc.gameSettings.keyBindForward,true);S12PacketEntityVelocity packet=kb(1,.6);t.packet(packet);check(t.delaying(),"Forward/diagonal movement did not enable Watchdog");
         t.now=1150;t.pre();check(t.mc.gameSettings.keyBindJump.isKeyDown(),"Forward movement lost reset");t.post();t.now=1200;t.start();check(t.delivered.equals(Collections.singletonList(packet)),"Forward delay ordering changed");
         t.self.movementInput.moveForward=0;forwardPhysical=false;KeyState.set(t.mc.gameSettings.keyBindForward,false);t.post();check(!t.mc.gameSettings.keyBindForward.isKeyDown(),"Forward-only mode manufactured held forward input");t.close();
      }
      // Recheck at tick-start, reset execution, and after the refreshed input of the same tick.
      for(int boundary=0;boundary<3;boundary++) {
         Fixture t=new Fixture(loader,4,true);t.disableDelayConditions();flag(t.velocity,"watchdog_require_forward",true);t.self.movementInput=new MovementInput();t.self.movementInput.moveForward=1;
         t.packet(kb(1,.6));t.packet(new S02PacketChat(new ChatComponentText("ordered")));t.now=1150;
         if(boundary==2)t.pre();t.self.movementInput.moveForward=0;
         if(boundary==0)t.start();else if(boundary==1)t.pre();else t.post();
         check(!t.delaying()&&t.retained()==0&&t.delivered.size()==2,"Stopping forward did not release same-world queue");
         check(!t.mc.gameSettings.keyBindJump.isKeyDown()&&!t.mc.gameSettings.keyBindForward.isKeyDown(),"Stopping forward leaked synthetic input");t.now=1400;t.pre();check(!t.mc.gameSettings.keyBindJump.isKeyDown(),"Stopping forward retained pending reset");t.close();
      }
      Fixture direct=new Fixture(loader,4,true);flag(direct.velocity,"watchdog_require_forward",true);flag(direct.velocity,"watchdog_delay",false);direct.self.movementInput=null;direct.self.moveForward=1;direct.start();S12PacketEntityVelocity packet=kb(1,.6);direct.packet(packet);check(direct.channel.readInbound()==packet,"Forward-only delay-off held packet");direct.pre();check(direct.mc.gameSettings.keyBindJump.isKeyDown(),"Forward-only delay-off lost reset");jumpPhysical=true;direct.self.moveForward=0;direct.post();check(direct.mc.gameSettings.keyBindJump.isKeyDown(),"Forward condition stole physical jump");jumpPhysical=false;direct.close();
      Fixture change=new Fixture(loader,4,true);change.disableDelayConditions();change.packet(kb(1,.6));change.now=1150;change.pre();check(change.mc.gameSettings.keyBindForward.isKeyDown(),"Existing default-off forward hold changed");flag(change.velocity,"watchdog_require_forward",true);change.velocity.afterSettingControl("watchdog_require_forward",change.c);check(!change.delaying()&&!change.mc.gameSettings.keyBindForward.isKeyDown()&&!change.mc.gameSettings.keyBindJump.isKeyDown(),"Toggling forward condition did not clean owned state");change.close();
      System.out.println("PASS: Watchdog forward-only UI/default/config; forward/diagonal allow; idle/backward/strafe/drift skip; delay-off; stop/toggle queue and key cleanup; physical jump retained");
   }
   static void forwardUiScope(Path baseline)throws Exception {
      com.google.gson.JsonObject before;
      try(JarFile jar=new JarFile(baseline.toFile())) { before=new com.google.gson.JsonParser().parse(new String(read(jar.getInputStream(jar.getJarEntry("assets/ricesemantic/ModuleUi.json"))),"UTF-8")).getAsJsonObject(); }
      com.google.gson.JsonObject after=new com.google.gson.JsonParser().parse(new String(read(JadeVelocityCheck.class.getResourceAsStream("/assets/ricesemantic/ModuleUi.json")),"UTF-8")).getAsJsonObject();
      int removed=0;
      for(com.google.gson.JsonElement m:after.getAsJsonArray("modules"))if(m.getAsJsonObject().get("module").getAsString().equals("Velocity"))for(com.google.gson.JsonElement v:m.getAsJsonObject().getAsJsonArray("variants")) {
         com.google.gson.JsonArray controls=v.getAsJsonObject().getAsJsonArray("controls"), retained=new com.google.gson.JsonArray();
         for(com.google.gson.JsonElement element:controls){com.google.gson.JsonObject c=element.getAsJsonObject();if(c.has("key")&&(c.get("key").getAsString().equals("watchdog_require_forward")||c.get("key").getAsString().equals("watchdog_require_weapon"))){check(v.getAsJsonObject().getAsJsonArray("values").get(0).getAsInt()==4,"Forward UI changed non-Watchdog mode");removed++;}else retained.add(element);}
         v.getAsJsonObject().add("controls",retained);
      }
      int baselineCount=0;
      // v121 restores only standalone Displace's v72 layout. Validate it exactly before normalizing this historical UI comparison.
      com.google.gson.JsonObject v72;
      try(JarFile jar=new JarFile(Paths.get("build/myau-scaffold-v72/artifacts/payload-101.jar").toFile())) { v72=new com.google.gson.JsonParser().parse(new String(read(jar.getInputStream(jar.getJarEntry("assets/ricesemantic/ModuleUi.json"))),"UTF-8")).getAsJsonObject(); }
      com.google.gson.JsonElement expectedDisplace=null, actualDisplace=null;int beforeDisplace=0,afterDisplace=0;
      for(com.google.gson.JsonElement m:v72.getAsJsonArray("modules"))if(m.getAsJsonObject().get("module").getAsString().equals("Knockback Displace"))expectedDisplace=m;
      for(com.google.gson.JsonElement m:after.getAsJsonArray("modules"))if(m.getAsJsonObject().get("module").getAsString().equals("Knockback Displace")){actualDisplace=m;afterDisplace++;}
      check(expectedDisplace!=null,"Missing v72 Displace UI reference");
      // v170 adds exactly two controls to the Void variant; all other historical controls remain exact.
      for(com.google.gson.JsonElement v:expectedDisplace.getAsJsonObject().getAsJsonArray("variants")) {
         if(v.getAsJsonObject().getAsJsonArray("values").get(0).getAsInt()!=5)continue;
         com.google.gson.JsonArray controls=new com.google.gson.JsonArray();
         for(com.google.gson.JsonElement entry:v.getAsJsonObject().getAsJsonArray("controls")) {
            controls.add(entry);
            if(entry.getAsJsonObject().has("key")&&entry.getAsJsonObject().get("key").getAsString().equals("angle")) {
               for(String key:new String[]{"void_scan_radius","void_ignore_one_block_wall"}) {
                  com.google.gson.JsonObject added=new com.google.gson.JsonObject();added.addProperty("key",key);controls.add(added);
               }
            }
         }
         v.getAsJsonObject().add("controls",controls);
      }
      check(afterDisplace==1&&expectedDisplace.equals(actualDisplace),"Standalone Displace UI must match v72 plus the two bS Void controls exactly");
      com.google.gson.JsonArray restoredUi=new com.google.gson.JsonArray();
      for(com.google.gson.JsonElement m:before.getAsJsonArray("modules"))if(m.getAsJsonObject().get("module").getAsString().equals("Knockback Displace")){restoredUi.add(expectedDisplace);beforeDisplace++;}else restoredUi.add(m);
      check(beforeDisplace==1,"Baseline must contain one standalone Displace UI entry");before.add("modules",restoredUi);
      // v115 removed ZestTap on request; its UI entry is the only other sanctioned ModuleUi.json change.
      com.google.gson.JsonArray kept=new com.google.gson.JsonArray();int zestTap=0;
      for(com.google.gson.JsonElement m:before.getAsJsonArray("modules"))if(m.getAsJsonObject().get("module").getAsString().equals("ZestTap"))zestTap++;else kept.add(m);
      check(zestTap<=1,"Baseline lists ZestTap more than once");before.add("modules",kept);
      for(com.google.gson.JsonElement m:after.getAsJsonArray("modules"))check(!m.getAsJsonObject().get("module").getAsString().equals("ZestTap"),"ZestTap UI entry still present");
      for(com.google.gson.JsonElement m:before.getAsJsonArray("modules"))if(m.getAsJsonObject().get("module").getAsString().equals("Velocity"))for(com.google.gson.JsonElement v:m.getAsJsonObject().getAsJsonArray("variants")) {
         com.google.gson.JsonArray retained=new com.google.gson.JsonArray();
         for(com.google.gson.JsonElement element:v.getAsJsonObject().getAsJsonArray("controls")){com.google.gson.JsonObject c=element.getAsJsonObject();if(c.has("key")&&(c.get("key").getAsString().equals("watchdog_require_forward")||c.get("key").getAsString().equals("watchdog_require_weapon"))){check(v.getAsJsonObject().getAsJsonArray("values").get(0).getAsInt()==4,"Baseline forward UI in unexpected mode");baselineCount++;}else retained.add(element);}
         v.getAsJsonObject().add("controls",retained);
      }
      // v125 sanctioned change: Auto Clicker gains the Smart + Show-in-Lunar controls under a new "Smart & Lunar" section.
      for(com.google.gson.JsonObject root:new com.google.gson.JsonObject[]{before,after})
         for(com.google.gson.JsonElement m:root.getAsJsonArray("modules"))
            if(m.getAsJsonObject().get("module").getAsString().equals("Auto Clicker"))
               for(com.google.gson.JsonElement v:m.getAsJsonObject().getAsJsonArray("variants")){
                  com.google.gson.JsonArray keep=new com.google.gson.JsonArray();
                  for(com.google.gson.JsonElement e:v.getAsJsonObject().getAsJsonArray("controls")){
                     com.google.gson.JsonObject c=e.getAsJsonObject();
                     boolean skip=(c.has("key")&&(c.get("key").getAsString().equals("smart")||c.get("key").getAsString().equals("smart_amount")||c.get("key").getAsString().equals("lunar_clicks")||java.util.Arrays.asList("sound_enabled","sound_volume","sound_pitch","sound_variation","sound_select").contains(c.get("key").getAsString())))||(c.has("section")&&(c.get("section").getAsString().equals("Smart & Lunar")||c.get("section").getAsString().equals("Sound")));
                     if(!skip)keep.add(e);
                  }
                  v.getAsJsonObject().add("controls",keep);
               }
      // v160 sanctioned change: the restored Slinky Clutch carries its OG ModuleUi section layout (Main/Aim/Snap-back/
      // Conditions). Strip it from BOTH sides so this holds whether or not the chosen baseline already carries the entry.
      int clutchUi=0;
      for(com.google.gson.JsonElement m:after.getAsJsonArray("modules"))if(m.getAsJsonObject().get("module").getAsString().equals("Clutch"))clutchUi++;
      check(clutchUi==1,"expected exactly one restored Clutch UI entry");
      for(com.google.gson.JsonObject root:new com.google.gson.JsonObject[]{before,after}){
         com.google.gson.JsonArray keep=new com.google.gson.JsonArray();
         for(com.google.gson.JsonElement m:root.getAsJsonArray("modules"))if(!m.getAsJsonObject().get("module").getAsString().equals("Clutch"))keep.add(m);
         root.add("modules",keep);
      }
      // v201 adds exactly the global Edit HUD action; retain equality of every prior GUI control.
      int guiCount=0;
      for(com.google.gson.JsonObject root:new com.google.gson.JsonObject[]{before,after})for(com.google.gson.JsonElement m:root.getAsJsonArray("modules"))if(m.getAsJsonObject().get("module").getAsString().equals("GUI")){
         if(root==after)guiCount++;
         for(com.google.gson.JsonElement v:m.getAsJsonObject().getAsJsonArray("variants")){
            com.google.gson.JsonArray controls=v.getAsJsonObject().getAsJsonArray("controls");int n=controls.size();
            boolean button=n>=2&&controls.get(n-1).getAsJsonObject().has("key")&&controls.get(n-1).getAsJsonObject().get("key").getAsString().equals("edit_hud");
            if(root==after)check(button,"GUI Edit HUD action missing");
            if(button){check(controls.get(n-2).getAsJsonObject().has("section")&&controls.get(n-2).getAsJsonObject().get("section").getAsString().equals("HUD"),"Edit HUD section changed");com.google.gson.JsonArray keptControls=new com.google.gson.JsonArray();for(int i=0;i<n-2;i++)keptControls.add(controls.get(i));v.getAsJsonObject().add("controls",keptControls);}
         }
      }
      check(guiCount==1,"Expected one GUI entry");
      check(removed==2&&baselineCount<=2&&before.equals(after),"Unexpected UI edits outside the Watchdog forward toggle");
   }
   static void enableConditions(Fixture t)throws Exception {
      t.disableDelayConditions();for(String key:new String[]{"watchdog_require_aim","watchdog_require_mouse","watchdog_require_forward","watchdog_require_weapon"})flag(t.velocity,key,true);
      t.self.ticksExisted=100;t.self.movementInput.moveForward=1;forwardPhysical=true;KeyState.set(t.mc.gameSettings.keyBindForward,true);mouse=true;KeyState.set(t.mc.gameSettings.keyBindAttack,true);
      WeaponsModule weapons=(WeaponsModule)get(t.velocity,"weapons");flag(weapons,"swords",true);t.self.inventory.mainInventory[0]=new ItemStack(Items.diamond_sword);
   }
   static void breakCondition(Fixture t,int which) {
      if(which==0)t.mc.objectMouseOver=null;
      if(which==1){mouse=false;KeyState.set(t.mc.gameSettings.keyBindAttack,false);}
      if(which==2){t.self.movementInput.moveForward=0;forwardPhysical=false;KeyState.set(t.mc.gameSettings.keyBindForward,false);}
      if(which==3){t.self.inventory.mainInventory[0]=new ItemStack(Items.bow);t.self.ticksExisted++;}
   }
   static void allConditions(ClassLoader loader)throws Exception {
      Fixture ui=new Fixture(loader,4,true);
      for(String key:new String[]{"watchdog_require_aim","watchdog_require_mouse","watchdog_require_forward","watchdog_require_weapon"}){
         check(ui.velocity.findSetting(key) instanceof BooleanSetting,"Condition missing: "+key);
         check(!((BooleanSetting)ui.velocity.findSetting(key)).isEnabled(),"Condition default must preserve old profiles: "+key);
         flag(ui.velocity,"watchdog_delay",false);check(RecoveredUiLayout.controls(ui.velocity).stream().anyMatch(c->c.setting!=null&&c.setting.getKey().equals(key)),"Condition hidden with Delay off: "+key);
         SettingCodec.decode(ui.velocity.findSetting(key),new com.google.gson.JsonPrimitive(true));check(SettingCodec.encode(ui.velocity.findSetting(key)).getAsBoolean(),"Condition codec failed: "+key);
      }
      check(RecoveredUiLayout.controls(ui.velocity).stream().noneMatch(c->c.setting!=null&&c.setting.getKey().equals("require_sprinting_jump")),"Sprinting leaked into Watchdog");ui.close();
      for(int missing=0;missing<4;missing++){
         Fixture t=new Fixture(loader,4,true);enableConditions(t);breakCondition(t,missing);S12PacketEntityVelocity kb=kb(1,.6);t.packet(kb);
         check(t.channel.readInbound()==kb&&!t.delaying(),"Failed condition still held knockback: "+missing);t.now=1300;t.pre();check(!t.mc.gameSettings.keyBindJump.isKeyDown(),"Failed condition still reset: "+missing);t.close();
      }
      for(boolean delay:new boolean[]{false,true}){
         Fixture t=new Fixture(loader,4,true);enableConditions(t);flag(t.velocity,"watchdog_delay",delay);t.start();S12PacketEntityVelocity kb=kb(1,.6);t.packet(kb);
         if(!delay)check(t.channel.readInbound()==kb,"Delay-off condition held packet");t.now=delay?1150:1000;t.pre();check(t.mc.gameSettings.keyBindJump.isKeyDown(),"All conditions passing failed to reset");t.post();t.close();
      }
      for(int missing=0;missing<4;missing++)for(int boundary=0;boundary<3;boundary++){
         Fixture t=new Fixture(loader,4,true);enableConditions(t);t.packet(kb(1,.6));t.packet(new S02PacketChat(new ChatComponentText("ordered")));check(t.retained()==2,"Conditions did not start queue");t.now=1150;
         if(boundary==2)t.pre();breakCondition(t,missing);
         if(boundary==0)t.start();else if(boundary==1)t.pre();else t.post();
         check(!t.delaying()&&t.retained()==0&&t.delivered.size()==2&&!t.mc.gameSettings.keyBindJump.isKeyDown(),"Condition loss leaked queue/reset/key: "+missing+" boundary "+boundary);t.now=1400;t.pre();check(!t.mc.gameSettings.keyBindJump.isKeyDown(),"Condition loss retained pending reset");t.close();
      }
      System.out.println("PASS: four Watchdog conditions AND-combined; weapon classifier; Delay-off/config/UI; condition-loss queue/reset/key cleanup; no Watchdog sprint toggle");
   }
   static void jumpSprinting(ClassLoader loader)throws Exception {
      for(boolean sprint:new boolean[]{false,true})for(boolean enabled:new boolean[]{false,true})for(boolean reducing:new boolean[]{false,true}) {
         Fixture t=new Fixture(loader,1,false);flag(t.velocity,"require_target",false);flag(t.velocity,"require_mouse",false);flag(t.velocity,"require_forward",false);flag(t.velocity,"require_weapon",false);flag(t.velocity,"require_sprinting_jump",enabled);number(t.velocity,"jump_chance",100);
         t.self.sprinting=sprint;t.self.onGround=true;t.self.motionY=.4;t.start();SemanticEvent after=new SemanticEvent(15);after.active=true;after.reducing=reducing;t.manager.dispatch(after);
         // Vanilla advances the player tick between input clicks and post-entities.
         t.self.ticksExisted++;t.post();boolean expected=!enabled||sprint&&!reducing;
         check(t.mc.gameSettings.keyBindJump.isKeyDown()==expected,"Jump sprint/same-tick reduction condition failed");
         if(enabled&&sprint&&reducing){t.start();t.post();check(t.mc.gameSettings.keyBindJump.isKeyDown(),"Previous tick reducing click incorrectly blocked Jump");}t.close();
      }
      Fixture use=new Fixture(loader,1,false);flag(use.velocity,"require_sprinting_jump",true);flag(use.velocity,"require_target",false);flag(use.velocity,"require_mouse",false);flag(use.velocity,"require_forward",false);flag(use.velocity,"require_weapon",false);number(use.velocity,"jump_chance",100);use.self.onGround=true;use.self.motionY=.4;use.start();SemanticEvent event=new SemanticEvent(15);event.active=false;event.reducing=true;use.manager.dispatch(event);use.post();check(use.mc.gameSettings.keyBindJump.isKeyDown(),"Use interaction blocked Jump as a reducing attack");use.close();
      System.out.println("PASS: Jump-only Sprinting gate; same-client-tick reducing-player-click suppression across player tick increment; next-tick recovery; toggle-off and use-click behavior");
   }
   public static void main(String[] args)throws Exception {
      Bootstrap.register();Class<?> u=Class.forName("sun.misc.Unsafe");Field field=u.getDeclaredField("theUnsafe");field.setAccessible(true);unsafe=field.get(null);allocate=u.getMethod("allocateInstance",Class.class);
      Path classes=Paths.get(args[0]);ClassLoader loader=loader(classes,null);timeout();forwardOnly(loader);forwardUiScope(Paths.get(args[1]));allConditions(loader);jumpSprinting(loader);
      Fixture t=new Fixture(loader,4,true);t.disableDelayConditions();check(t.velocity.getName().equals("Velocity")&&t.velocity.statusText().equals("Watchdog"),"Array list must read Velocity Watchdog");
      ChoiceSetting modes=(ChoiceSetting)t.velocity.findSetting("mode");check(modes.getOptions().equals(Arrays.asList("Regular","Jump","Reduce","Ignore","Watchdog")),"Old mode indexes changed");
      check(RecoveredUiLayout.controls(t.velocity).stream().anyMatch(x->x.setting!=null&&x.setting.getKey().equals("watchdog_max_delay")),"Watchdog delay menu missing");
      for(Setting<?> setting:t.velocity.getSettings()){check(setting.isConfigured(),"New defaults missing: "+setting.getKey());SettingCodec.decode(setting,SettingCodec.encode(setting));}
      S12PacketEntityVelocity v=kb(1,.6);t.packet(v);S02PacketChat ordinary=new S02PacketChat(new ChatComponentText("queued"));t.packet(ordinary);check(t.retained()==2&&t.channel.readInbound()==null,"Inbound delay did not own packets");
      S19PacketEntityStatus hurt=new S19PacketEntityStatus(t.self,(byte)2);t.packet(hurt);check(t.channel.readInbound()==hurt&&t.retained()==2,"Own hurt status must bypass held knockback");
      t.now=1149;t.pre();check(!t.mc.gameSettings.keyBindJump.isKeyDown(),"Reset fired before maxDelay-50");
      t.now=1150;t.pre();check(t.mc.gameSettings.keyBindJump.isKeyDown()&&t.mc.gameSettings.keyBindForward.isKeyDown(),"Jade reset/forward hold missing");
      t.post();check(!t.mc.gameSettings.keyBindJump.isKeyDown()&&t.mc.gameSettings.keyBindForward.isKeyDown(),"Jump/forward cleanup timing wrong");
      t.now=1199;t.start();check(t.delivered.isEmpty(),"Knockback released early");t.now=1200;t.start();check(t.delivered.equals(Arrays.asList(v,ordinary)),"Ordered release/max-delay wrong");
      t.now=1419;t.post();check(t.mc.gameSettings.keyBindForward.isKeyDown(),"Forward released before 270 ms");t.now=1420;t.post();check(!t.mc.gameSettings.keyBindForward.isKeyDown(),"Forward hold stuck");
      t.close();
      Fixture fallback=new Fixture(loader,4,true);number(fallback.velocity,"watchdog_delay_chance",0);fallback.packet(kb(1,.5));fallback.now=1049;fallback.start();check(fallback.delivered.isEmpty(),"Jade fallback released before 50 ms");fallback.now=1050;fallback.start();check(fallback.delivered.size()==1,"Jade fallback delay is not 50 ms");fallback.close();
      Fixture noDelay=new Fixture(loader,4,true);flag(noDelay.velocity,"watchdog_delay",false);noDelay.start();S12PacketEntityVelocity direct=kb(1,.75);noDelay.packet(direct);check(noDelay.channel.readInbound()==direct&&!noDelay.delaying(),"Delay-off mode held knockback");noDelay.pre();check(noDelay.mc.gameSettings.keyBindJump.isKeyDown(),"Delay-off reset missing");forwardPhysical=jumpPhysical=true;noDelay.manager.requestEnabled(noDelay.velocity,false);noDelay.manager.applyPendingChanges();check(noDelay.mc.gameSettings.keyBindJump.isKeyDown()&&noDelay.mc.gameSettings.keyBindForward.isKeyDown(),"Disable stole physical keys");forwardPhysical=jumpPhysical=false;noDelay.close();
      Fixture other=new Fixture(loader,4,true);S12PacketEntityVelocity remote=kb(2,.6);other.packet(remote);check(other.channel.readInbound()==remote&&!other.delaying(),"Other entity velocity triggered reset");other.close();
      Fixture disable=new Fixture(loader,4,true);disable.disableDelayConditions();disable.packet(kb(1,.5));disable.manager.requestEnabled(disable.velocity,false);disable.manager.applyPendingChanges();check(disable.delivered.size()==1&&disable.retained()==0,"Disable didn't release same-world queue");disable.close();
      Fixture stale=new Fixture(loader,4,true);stale.disableDelayConditions();stale.packet(kb(1,.5));stale.manager.worldUnload();check(stale.delivered.isEmpty()&&stale.retained()==0,"Unload replayed stale packets");stale.close();
      Fixture toggled=new Fixture(loader,4,true);toggled.disableDelayConditions();toggled.packet(kb(1,.6));flag(toggled.velocity,"watchdog_delay",false);toggled.start();check(!toggled.delaying()&&toggled.delivered.size()==1,"Delay toggle didn't release queue");toggled.now=1300;toggled.pre();check(!toggled.mc.gameSettings.keyBindJump.isKeyDown(),"Delay toggle left a pending reset");toggled.close();
      Fixture aim=new Fixture(loader,4,true);flag(aim.velocity,"watchdog_require_aim",true);aim.mc.objectMouseOver=null;aim.packet(kb(1,.6));check(!aim.delaying(),"Aim condition ignored");aim.close();
      Fixture restriction=new Fixture(loader,4,true);restriction.disableDelayConditions();flag(restriction.velocity,"watchdog_restrict_held_item",true);restriction.packet(kb(1,.5));restriction.now=1050;restriction.start();check(restriction.delivered.size()==1,"Empty whitelist must use Jade's 50 ms fallback");restriction.close();
      Fixture blink=new Fixture(loader,4,true);BlinkModule b=new BlinkModule(blink.manager);blink.manager.register(b);InitialSettings.applyMissing(blink.manager);((ChoiceSetting)b.findSetting("direction")).set(0);blink.manager.requestEnabled(b,true);blink.manager.applyPendingChanges();blink.packet(kb(1,.6));check(!blink.delaying(),"Inbound Blink conflict wasn't respected");blink.close();
      Fixture blocked=new Fixture(loader,4,true);blocked.disableDelayConditions();number(blocked.velocity,"watchdog_reset_chance",0);S12PacketEntityVelocity skip=kb(1,.6);blocked.packet(skip);check(blocked.channel.readInbound()==skip&&!blocked.delaying(),"Zero reset chance still triggered");blocked.close();
      Fixture potion=new Fixture(loader,4,true);potion.disableDelayConditions();((Map<Integer,PotionEffect>)getSuper(potion.self,"activePotionsMap")).put(Potion.poison.id,new PotionEffect(Potion.poison.id,100));potion.packet(kb(1,.5));check(!potion.delaying(),"Poison must block reset");potion.close();
      Fixture burn=new Fixture(loader,4,true);burn.self.burning=true;burn.packet(kb(1,.5));check(!burn.delaying(),"Burning must block reset");burn.close();
      Fixture fall=new Fixture(loader,4,true);fall.self.fallDistance=4;fall.self.onGround=false;fall.pre();fall.self.onGround=true;fall.pre();fall.packet(kb(1,.5));check(!fall.delaying(),"Fall damage ignore missing");fall.close();
      Fixture correction=new Fixture(loader,4,true);correction.disableDelayConditions();correction.packet(kb(1,.6));S08PacketPlayerPosLook look=new S08PacketPlayerPosLook();correction.packet(look);check(!correction.delaying()&&correction.retained()==0&&correction.delivered.size()==1,"Correction did not release queue");correction.now=1300;correction.pre();check(!correction.mc.gameSettings.keyBindJump.isKeyDown(),"Correction kept pending jump");correction.close();
      Fixture lost=new Fixture(loader,4,true);lost.disableDelayConditions();lost.packet(kb(1,.5));lost.world.playerEntities.clear();lost.mc.objectMouseOver=null;lost.start();check(!lost.delaying()&&lost.delivered.size()==1,"Target loss didn't cancel delay");lost.close();
      Fixture world=new Fixture(loader,4,true);world.disableDelayConditions();world.packet(kb(1,.5));world.mc.theWorld=blank(World.class);world.start();check(world.delivered.isEmpty()&&world.retained()==0,"Old-world packets replayed");world.close();
      Fixture modeChange=new Fixture(loader,4,true);modeChange.disableDelayConditions();modeChange.packet(kb(1,.5));mode(modeChange.velocity,0);modeChange.start();check(modeChange.delivered.size()==1&&modeChange.retained()==0,"Mode change lost held packet");modeChange.close();
      Fixture focus=new Fixture(loader,4,true);focus.disableDelayConditions();focus.packet(kb(1,.5));focus.mc.inGameHasFocus=false;focus.start();check(focus.delivered.size()==1&&focus.retained()==0,"Focus-loss handoff failed");focus.close();
      // Compare original modes against the pre-change class, using the same inputs/settings.
      ClassLoader original=loader(classes,Paths.get(args[1]));
      for(int mode=0;mode<4;mode++){
         String before=legacy(original,mode),after=legacy(loader,mode);check(before.equals(after),"Existing Velocity mode "+mode+" changed: "+before+" vs "+after);
      }
      System.out.println("PASS: Watchdog label/UI/config; Jade timeout and reset/packet/forward timing; ordered Netty ownership and immediate hurt status; delay-off/fallback/chance; aim/item/Blink conditions; potion/burning/fall filters; correction/target/world/focus/mode/disable cleanup; four legacy modes match baseline");
   }
   static Object getSuper(Object o,String name)throws Exception{for(Class<?> t=o.getClass();t!=null;t=t.getSuperclass())try{Field f=t.getDeclaredField(name);f.setAccessible(true);return f.get(o);}catch(NoSuchFieldException next){}throw new NoSuchFieldException(name);}
   static String legacy(ClassLoader loader,int mode)throws Exception {
      // v76 constructor predates the optional manager; construct it directly in a minimal fixture.
      Minecraft mc=blank(Minecraft.class);World world=blank(World.class);Player self=player(world,1,0);put(mc,"thePlayer",self);put(mc,"theWorld",world);put(mc,"inGameHasFocus",true);
      GameSettings keys=blank(GameSettings.class);keys.keyBindJump=new KeyBinding("legacy.jump",57,"test");keys.keyBindAttack=new KeyBinding("legacy.attack",-100,"test");put(mc,"gameSettings",keys);
      ClientContext c=new ClientContext(mc);ModuleManager manager=new ModuleManager(c);Module velocity=(Module)loader.loadClass("dev.rice.module.impl.VelocityModule").getConstructor(WeaponsModule.class).newInstance(new WeaponsModule());manager.register(velocity);InitialSettings.applyMissing(manager);mode(velocity,mode);manager.requestEnabled(velocity,true);manager.applyPendingChanges();
      number(velocity,"reduce_amount",.5);self.onGround=true;self.motionX=.6;self.motionY=.3;self.motionZ=.8;
      if(mode==0){manager.dispatch(new SemanticEvent(18));SemanticEvent packet=new SemanticEvent(2);packet.packet=kb(1,.5);manager.dispatch(packet);manager.dispatch(new SemanticEvent(18));}
      if(mode==1)manager.dispatch(new SemanticEvent(18));
      if(mode==2){manager.dispatch(new SemanticEvent(14));SemanticEvent hit=new SemanticEvent(15);hit.reducing=true;manager.dispatch(hit);}
      if(mode==3){manager.dispatch(new SemanticEvent(17));self.motionX=2;self.motionZ=3;manager.dispatch(new SemanticEvent(0));}
      String result=self.motionX+","+self.motionY+","+self.motionZ+","+keys.keyBindJump.isKeyDown()+","+velocity.statusText();manager.worldUnload();return result;
   }
}

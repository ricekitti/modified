import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.hook.GameHooks;
import dev.rice.client.render.*;
import dev.rice.client.stasis.*;
import dev.rice.config.*;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.module.impl.GuiModule;
import dev.rice.module.impl.StasisModule;
import dev.rice.setting.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.io.*;
import dev.rice.shaded.asm.ClassReader;
import dev.rice.shaded.asm.ClassWriter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Bootstrap;
import net.minecraft.network.*;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C03PacketPlayer.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.*;

/** Uses real profile files, modal state, player movement and packets without opening a game window. */
public final class ConfigStasisCheck {
   static Object unsafe; static Method allocate;
   static <T> T blank(Class<T> type) throws Exception { return type.cast(allocate.invoke(unsafe, type)); }
   static void put(Object object, String name, Object value) throws Exception {
      for (Class<?> type=object.getClass(); type!=null; type=type.getSuperclass()) {
         try { Field field=type.getDeclaredField(name); field.setAccessible(true); field.set(object,value); return; }
         catch (NoSuchFieldException next) { }
      }
      throw new NoSuchFieldException(name);
   }
   static Object get(Object object,String name)throws Exception {
      Field field=object.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(object);
   }
   static void require(boolean condition,String message) { if(!condition)throw new AssertionError(message); }
   static final class Probe extends Module {
      final NumberSetting value=add(new NumberSetting("value","Value",1,0,20,1));
      Probe(){super("config_probe","Config Probe","Persistence regression",ModuleCategory.MOVEMENT);}
      @Override public int descriptorFlags(){return 0;}
   }
   static final class Network extends NetworkManager {
      List<Packet<?>> sent;
      Network(){super(EnumPacketDirection.CLIENTBOUND);}
      @Override public void sendPacket(Packet packet){sent.add(packet);}
   }
   static final class Handler extends NetHandlerPlayClient {
      List<Packet<?>> received;
      Handler(){super(null,null,null,null);}
      @Override public void handleEntityVelocity(S12PacketEntityVelocity p){received.add(p);}
      @Override public void handlePlayerPosLook(S08PacketPlayerPosLook p){received.add(p);}
      @Override public void handleChat(S02PacketChat p){received.add(p);}
   }
   static ClientContext context() throws Exception {
      Minecraft mc=blank(Minecraft.class); EntityPlayerSP player=blank(EntityPlayerSP.class);
      WorldClient world=blank(WorldClient.class); GameSettings settings=blank(GameSettings.class);
      put(mc,"thePlayer",player);put(mc,"theWorld",world);put(mc,"gameSettings",settings);put(mc,"mcThread",Thread.currentThread());
      settings.keyBindSneak=new KeyBinding("test.sneak",42,"test");
      player.movementInput=new MovementInput();player.posX=3;player.posY=70;player.posZ=5;player.onGround=true;
      player.setEntityBoundingBox(new AxisAlignedBB(2.7,70,4.7,3.3,71.8,5.3));
      return new ClientContext(mc);
   }
   static void profiles(Path directory)throws Exception {
      ClientContext c=context(); ModuleManager manager=new ModuleManager(c); Probe probe=new Probe();
      manager.register(probe);GuiModule gui=new GuiModule(manager);manager.register(gui);
      GuiAppearance.configureMissing(gui);InitialSettings.applyMissing(manager);
      ConfigStore global=new ConfigStore(directory.resolve("settings.json"));
      ProfileServices.FilesHost host=new ProfileServices.FilesHost(manager,directory.resolve("profiles"),global);
      manager.input().profiles(global,host);
      require(!host.pending(),"fresh session dirty");
      probe.value.set(2D);manager.requestEnabled(probe,true);manager.setBinding(probe,17);probe.setHoldBinding(true);
      host.save("A",false);require(!host.pending(),"save did not clear changes");
      byte[] saved=Files.readAllBytes(directory.resolve("profiles/A"));
      probe.value.set(8D);manager.requestEnabled(probe,false);manager.setBinding(probe,0);probe.setHoldBinding(false);
      host.save("B",false);host.load("A");
      require(probe.value.doubleValue()==2D && probe.isEnabled() && probe.getBinding()==17 && probe.isHoldBinding(),"save/load lost settings, enable, bind or hold mode");
      require(!host.pending(),"loaded profile dirty");
      probe.value.set(3D);require(host.pending(),"setting change not detected");
      probe.value.set(2D);require(!host.pending(),"reverting an edit should return to clean state");
      manager.requestEnabled(probe,false);require(host.pending(),"module toggle not detected");
      manager.requestEnabled(probe,true);manager.applyPendingChanges();
      manager.setBinding(probe,18);require(host.pending(),"binding change not detected");manager.setBinding(probe,17);
      probe.setHoldBinding(false);require(host.pending(),"hold binding change not detected");probe.setHoldBinding(true);
      probe.value.set(3D);
      RecoveredGuiScreen screen=new RecoveredGuiScreen(manager,gui,host);
      Method action=RecoveredGuiScreen.class.getDeclaredMethod("profileAction",int.class,String.class,double.class);action.setAccessible(true);
      gui.runtime().profileActionsConfirmed=true;
      action.invoke(screen,0,"B",0D);
      require(gui.runtime().modal==3 && host.current().equals("A") && probe.value.doubleValue()==3D,"dirty switch bypassed confirmation");
      gui.runtime().finishModal(false);
      require(host.current().equals("A") && probe.value.doubleValue()==3D && host.pending(),"Cancel discarded unsaved changes");
      action.invoke(screen,0,"B",1D);gui.runtime().confirmPrompt();
      require(host.current().equals("B") && probe.value.doubleValue()==8D && !host.pending(),"confirmed switch did not apply B");
      probe.value.set(9D);action.invoke(screen,0,"A",2D);
      require(gui.runtime().modal==3 && host.current().equals("B"),"later dirty switch skipped confirmation");gui.runtime().finishModal(false);
      try{host.save("A",false);throw new AssertionError("overwrote existing profile without overwrite");}catch(java.io.IOException expected){}
      require(host.pending() && Arrays.equals(saved,Files.readAllBytes(directory.resolve("profiles/A"))),"failed save lost pending edits or replaced original");
      host.save("B",true);require(!host.pending(),"overwrite save did not become clean");
      ModuleManager fresh=new ModuleManager(context());Probe restored=new Probe();fresh.register(restored);
      new ConfigStore(directory.resolve("profiles/B")).loadProfile(fresh);fresh.applyPendingChanges();
      require(restored.value.doubleValue()==9D && !restored.isEnabled(),"new manager cannot load saved overwrite");
      Files.createDirectory(directory.resolve("profiles/directory"));probe.value.set(10D);
      try{host.save("directory",true);throw new AssertionError("save onto directory succeeded");}catch(java.io.IOException expected){}
      require(host.pending() && host.current().equals("B"),"I/O failure cleared pending edits");
      global.profileBindings(Arrays.asList(new RecoveredConfiguration.ProfileBinding("A",37)));
      put(gui,"enabled",true);manager.input().profileBindings(37,(byte)1);
      require(gui.runtime().modal==3 && host.current().equals("B"),"config keybind bypassed unsaved confirmation");
      gui.runtime().finishModal(false);require(host.pending(),"keybind Cancel lost edits");
      manager.input().profileBindings(37,(byte)1);gui.runtime().confirmPrompt();
      require(host.current().equals("A") && probe.value.doubleValue()==2D,"confirmed config keybind did not load A");
      System.out.println("PASS: profile disk round-trip, toggles/binds/hold, clean/reverted edits, repeat confirmation, Cancel/Confirm, overwrite and I/O failures");
   }
   static SemanticEvent event(int type,Packet<?> packet){SemanticEvent e=new SemanticEvent(type);e.packet=packet;return e;}
   static void stasis()throws Exception {
      ClientContext c=context();Network network=blank(Network.class);network.sent=new ArrayList<>();Handler handler=blank(Handler.class);handler.received=new ArrayList<>();
      put(handler,"netManager",network);put(c.player(),"sendQueue",handler);
      BooleanSetting hypixel=new BooleanSetting("hypixel_mode","Hypixel Mode",false), shift=new BooleanSetting("shift_place_through_walls","Shift place through walls",false);
      JadeStasisBackend backend=new JadeStasisBackend(c,hypixel,shift);
      c.player().motionX=.2;c.player().motionY=-.3;c.player().motionZ=.4;backend.onEnable();
      c.player().setPosition(20,80,30);c.player().motionX=1;c.player().fallDistance=10;
      backend.onPreUpdate(new SemanticEvent(16));
      require(c.player().posX==3 && c.player().posY==70 && c.player().posZ==5 && c.player().motionX==0 && c.player().fallDistance==0 && c.player().onGround,"position/motion freeze failed");
      c.player().movementInput.jump=true;SemanticEvent input=new SemanticEvent(SemanticEvent.MOVEMENT_INPUT);input.forward=1;input.sideways=1;backend.onMoveInput(input);
      require(input.forward==0 && input.sideways==0 && !c.player().movementInput.jump,"movement input not frozen");
      SemanticEvent send=event(23,new C06PacketPlayerPosLook(100,100,100,45,20,false));backend.onPacketSend(send);
      require(send.cancelled && network.sent.get(0) instanceof C05PacketPlayerLook,"moving packet not replaced with stationary look");
      for(int i=1;i<=20;i++)backend.onPacketSend(event(23,new C03PacketPlayer(false)));
      require(network.sent.get(network.sent.size()-1) instanceof C04PacketPlayerPosition,"20-tick frozen position sync missing");
      C03PacketPlayer sync=(C03PacketPlayer)network.sent.get(network.sent.size()-1);
      require(sync.getPositionX()==3 && sync.getPositionY()==70 && sync.getPositionZ()==5 && sync.isOnGround(),"sync did not use frozen position/ground");
      backend.onDisable();require(c.player().motionX==.2 && c.player().motionY==-.3 && c.player().motionZ==.4,"saved momentum not restored");
      for(int i=0;i<3;i++){MovementInput lock=new MovementInput();lock.moveForward=1;lock.jump=true;StasisInputLock.apply(lock);require(i<2 ? lock.moveForward==0&&!lock.jump : lock.moveForward==1&&lock.jump,"two-tick disable lock incorrect");}
      backend.onEnable();hypixel.set(true);
      S12PacketEntityVelocity knockback=new S12PacketEntityVelocity(c.player().getEntityId(),.2,.3,.1);
      SemanticEvent received=event(2,knockback);backend.dispatch(received);
      require(received.dispatchMode==2 && backend.isHoldingPackets(),"self knockback was not held");
      SemanticEvent keepalive=event(2,new S00PacketKeepAlive(123));backend.dispatch(keepalive);require(keepalive.dispatchMode==0,"connection keepalive was held");
      SemanticEvent chat=event(2,new S02PacketChat(new ChatComponentText("regression")));backend.dispatch(chat);
      require(chat.dispatchMode==2 && handler.received.size()==1 && handler.received.get(0)==chat.packet,"chat did not pass through hold exactly once");
      S08PacketPlayerPosLook correction=new S08PacketPlayerPosLook();backend.dispatch(event(2,correction));
      for(int i=0;i<279;i++)backend.onTickStart(new SemanticEvent(0));
      require(backend.isHoldingPackets() && handler.received.size()==1,"hold ended early");
      backend.onTickStart(new SemanticEvent(0));
      require(!backend.isHoldingPackets() && handler.received.get(1)==knockback && handler.received.get(2)==correction,"timeout replay order/cleanup incorrect");
      backend.onEnable();backend.dispatch(event(2,knockback));hypixel.set(false);backend.onTickStart(new SemanticEvent(0));
      require(!backend.isHoldingPackets(),"turning off Hypixel Mode left packets stuck");
      hypixel.set(true);backend.onEnable();backend.dispatch(event(2,knockback));int delivered=handler.received.size();backend.onLoadWorld(new SemanticEvent(26));backend.onDisable();
      require(!backend.isHoldingPackets() && handler.received.size()==delivered,"world unload replayed old-world packets");
      backend.onEnable();hypixel.set(false);SemanticEvent velocity=event(2,knockback);backend.dispatch(velocity);require(velocity.dispatchMode==0,"normal-mode knockback held");
      c.player().setPosition(7,70,8);backend.onPostUpdate(new SemanticEvent(18));backend.onPostUpdate(new SemanticEvent(18));
      require((Double)get(backend,"frozenX")==7D,"normal knockback was not recaptured after two updates");
      Method exit=JadeStasisBackend.class.getDeclaredMethod("findRayExit",BlockPos.class,Vec3.class,Vec3.class);exit.setAccessible(true);
      Object target=exit.invoke(backend,new BlockPos(0,0,0),new Vec3(0,.5,.5),new Vec3(1,0,0));
      require(StasisPlaceTarget.face((StasisPlaceTarget)target)==EnumFacing.EAST && StasisPlaceTarget.hitVec((StasisPlaceTarget)target).xCoord==1,"through-wall exit face changed");
      StasisModule module=new StasisModule();require(module.findSetting("pulse")==null && !module.settingVisible("shift_place_through_walls"),"old settings remain or shift setting always visible");
      ((BooleanSetting)module.findSetting("hypixel_mode")).set(true);require(module.settingVisible("shift_place_through_walls"),"Hypixel Mode did not expose shift placement");
      ModuleManager order=new ModuleManager(c);
      Module other=new Module("packet_probe","Packet Probe","",ModuleCategory.MOVEMENT){@Override public boolean listens(int type){return type==2;}};
      order.register(other);order.register(module);put(other,"enabled",true);put(module,"enabled",true);
      Method listeners=ModuleManager.class.getDeclaredMethod("selectedListeners",int.class);listeners.setAccessible(true);
      require(((List<?>)listeners.invoke(order,2)).get(0)==module,"Stasis did not receive inbound packets at highest priority");
      System.out.println("PASS: OpenJade frozen position/momentum, input lock, stationary packets/periodic sync, knockback timeout/order, keepalive/chat passthrough, mode switch, world cleanup and ray exit");
   }
   public static void main(String[] args)throws Exception {
      Bootstrap.register();Class<?> type=Class.forName("sun.misc.Unsafe");Field field=type.getDeclaredField("theUnsafe");field.setAccessible(true);unsafe=field.get(null);allocate=type.getMethod("allocateInstance",Class.class);
      profiles(Files.createTempDirectory(Paths.get(args[0]),"profile-regression-"));stasis();
      if(args.length>2)payloads(args[1],args[2]);
   }
   static byte[] bytes(InputStream stream)throws Exception {
      try(InputStream input=stream){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=input.read(b))!=-1)out.write(b,0,n);return out.toByteArray();}
   }
   static byte[] noDebug(byte[] bytes){ClassWriter out=new ClassWriter(0);new ClassReader(bytes).accept(out,ClassReader.SKIP_DEBUG);return out.toByteArray();}
   static void payloads(String oldPath,String newPath)throws Exception {
      Set<String> allowed=new HashSet<>(Arrays.asList("dev/rice/client/InputRouter.class","dev/rice/client/hook/GameHooks.class",
         "dev/rice/client/render/ProfileServices$FilesHost.class","dev/rice/client/render/RecoveredGuiScreen.class",
         "dev/rice/config/ConfigStore.class","dev/rice/config/ConfigurationSession.class","dev/rice/module/ModuleManager.class",
         "dev/rice/module/impl/StasisModule.class"));
      Set<String> added=new HashSet<>(Arrays.asList("dev/rice/client/stasis/JadeStasisBackend.class","dev/rice/client/stasis/StasisInputLock.class",
         "dev/rice/client/stasis/StasisPlaceTarget.class","assets/ricesemantic/OPENJADE-STASIS.txt"));
      int debugOnly=0;
      try(JarFile old=new JarFile(oldPath);JarFile fresh=new JarFile(newPath)){
         Enumeration<JarEntry> entries=old.entries();while(entries.hasMoreElements()){
            JarEntry entry=entries.nextElement();JarEntry updated=fresh.getJarEntry(entry.getName());require(updated!=null,"removed unrelated entry: "+entry.getName());
            byte[] before=bytes(old.getInputStream(entry)),after=bytes(fresh.getInputStream(updated));
            if(Arrays.equals(before,after)||allowed.contains(entry.getName()))continue;
            require(entry.getName().endsWith(".class")&&Arrays.equals(noDebug(before),noDebug(after)),"unrelated payload changed: "+entry.getName());debugOnly++;
         }
         entries=fresh.entries();while(entries.hasMoreElements()){
            String name=entries.nextElement().getName();if(old.getJarEntry(name)==null)require(added.remove(name),"unexpected payload addition: "+name);
         }
      }
      require(added.isEmpty(),"missing Stasis files: "+added);
      System.out.println("PASS: payload scope restricted to configs/Stasis; "+debugOnly+" debug-only nested class changes; AutoBlock and Fog byte-identical");
   }
}

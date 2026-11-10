import dev.rice.client.anticheat.*;
import dev.rice.client.hook.GameHooks;
import dev.rice.client.RelationshipService;
import dev.rice.module.impl.AntiCheatModule;
import dev.rice.setting.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.*;
import net.minecraft.util.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;

public final class SnowifyAntiCheatCheck {
 static AntiCheatModule module;static Anticheat core;static Path data;
 static void check(boolean v,String m){V116Check.check(v,m);}
 static Object get(Object o,String field)throws Exception{Field f=o.getClass().getDeclaredField(field);f.setAccessible(true);return f.get(o);}
 static void set(Object o,String field,Object value)throws Exception{Field f=o.getClass().getDeclaredField(field);f.setAccessible(true);f.set(o,value);}
 static Object call(Object o,String name,Class<?>[] types,Object... args)throws Exception{Method m=o.getClass().getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(o,args);}
 static void flag(String key,boolean v){((BooleanSetting)module.findSetting(key)).set(v);}
 static void tick(){V116Check.manager.clientTick(false);V116Check.self.ticksExisted++;}
 static Map<?,?> players()throws Exception{return (Map<?,?>)get(core,"players");}
 static V116Check.Other other(int id,String name)throws Exception{V116Check.Other p=V116Check.other(id,name,0,65,5);Map<java.util.UUID,NetworkPlayerInfo> infos=(Map<java.util.UUID,NetworkPlayerInfo>)V116Check.get(net.minecraft.client.network.NetHandlerPlayClient.class,V116Check.handler,"playerInfoMap");infos.put(p.getUniqueID(),new NetworkPlayerInfo(p.getGameProfile()));return p;}
 static void scene()throws Exception{V116Check.manager.applyNow(module,false);V116Check.world.playerEntities.retainAll(Collections.singleton(V116Check.self));V116Check.world.loadedEntityList.retainAll(Collections.singleton(V116Check.self));V116Check.world.blocks.clear();V116Check.chat.clear();V116Check.handler.sent.clear();V116Check.manager.applyNow(module,true);flag("should_ping",false);}
 static void blocking(EntityPlayer p){ItemStack sword=new ItemStack(Items.diamond_sword);p.inventory.mainInventory[0]=sword;p.setItemInUse(sword,72000);p.isSwingInProgress=true;}
 static long alerts(String name){return V116Check.chatText().stream().filter(s->s.contains("flagged")&&s.contains(name)).count();}
 static Object freshData()throws Exception{Class<?> cls=Class.forName("dev.rice.client.anticheat.Anticheat$PlayerData");Constructor<?> c=cls.getDeclaredConstructor();c.setAccessible(true);return c.newInstance();}
 public static void main(String[] args)throws Exception{
  V116Check.setup();V116Check.put(V116Check.world,"mc",V116Check.mc);for(dev.rice.module.Module m:V116Check.manager.all())if(m.isEnabled())V116Check.manager.applyNow(m,false);
  data=Files.createTempDirectory(Paths.get(args[1]).resolve("build"),"snowify-anticheat-test-");V116Check.put(V116Check.mc,"mcDataDir",data.toFile());
  module=(AntiCheatModule)V116Check.manager.find("anticheat");core=module.backend.core;
  check(module.getSettings().size()==18,"Snowify controls missing: "+module.getSettings().size());
  check(((NumberSetting)module.findSetting("flag_interval_ticks")).intValue()==400&&((NumberSetting)module.findSetting("cheater_threshold")).intValue()==5,"Snowify numeric defaults");
  for(String key:new String[]{"autoblock","keep_y","tower","bridge_assist","lag_range","fastmine","no_break_delay","tab_icon","nametag_mark","cheater_popup","blacklist_warning","should_ping"})check(((BooleanSetting)module.findSetting(key)).isEnabled(),"Snowify default off: "+key);
  for(String key:new String[]{"auto_report","send_party_chat","add_cheaters_as_enemy","nuker","protocol"})check(module.findSetting(key)==null,"old OpenB4 setting retained: "+key);
  for(Setting<?> setting:module.getSettings())check(dev.rice.module.RecoveredUiLayout.controls(module).stream().anyMatch(c->c.setting==setting),"missing UI control: "+setting.getKey());
  com.google.gson.JsonObject profile=new com.google.gson.JsonObject(),record=new com.google.gson.JsonObject();record.addProperty("flag_interval",5);record.addProperty("scaffold",false);profile.add("AntiCheat",record);String before=profile.toString();
  dev.rice.config.RecoveredConfiguration.apply(V116Check.manager,profile,dev.rice.config.RecoveredConfiguration.Scope.PROFILE);V116Check.manager.applyPendingChanges();check(profile.toString().equals(before)&&((NumberSetting)module.findSetting("flag_interval_ticks")).intValue()==100&&!core.keepYProp().getBoolean()&&!core.towerProp().getBoolean(),"legacy profile migration/input preservation");flag("keep_y",true);flag("tower",true);
  scene();V116Check.Other p=other(101,"SnowifyBlocker");blocking(p);((NumberSetting)module.findSetting("cheater_threshold")).set(2D);
  for(int i=0;i<9;i++)tick();check(alerts("Autoblock")==0,"Autoblock flagged before ten samples");tick();check(alerts("Autoblock")==1,"Autoblock ten-tick threshold");for(int i=0;i<10;i++)tick();check(alerts("Autoblock")==1,"interval not respected");
  check(Anticheat.marksNametag(p)&&get(core,"popupName").equals(p.getName()),"cheater threshold/popup/nametag state");NetworkPlayerInfo info=new NetworkPlayerInfo(p.getGameProfile());String marked=SnowifyAntiCheatHost.playerName("Test",info),markedTwice=SnowifyAntiCheatHost.playerName(marked,info);check(marked.contains("\u26a0"),"tab marker absent");check(markedTwice.indexOf('\u26a0')==markedTwice.lastIndexOf('\u26a0'),"duplicate tab warning marker");IChatComponent original=new ChatComponentText("Name");check(SnowifyAntiCheatHost.displayName(original,p).getUnformattedText().contains("\u26a0")&&original.getUnformattedText().equals("Name"),"nametag marker mutated original");check(SnowifyAntiCheatHost.label(p,"Name").contains("\u26a0"),"custom Name Tags marker absent");
  Path blacklist=data.resolve("rice-slinky/anticheat_blacklist.json");check(Files.isRegularFile(blacklist),"blacklist not persisted");((Map<?,?>)get(core,"cheaters")).clear();set(core,"loaded",false);check(Anticheat.marksNametag(p),"blacklist not reloaded");
  core.setHud(.2,.3);int[] at=core.placeBox(500,300,100,40);check(at[0]==80&&at[1]==78,"popup normalized coordinates");core.moveBox(900,-30,100,40,500,300);check(core.hudX()==1&&core.hudY()==0,"HUD clamp missing");
  scene();p=other(102,"TeamBlocker");net.minecraft.scoreboard.Scoreboard board=V116Check.world.getScoreboard();net.minecraft.scoreboard.ScorePlayerTeam mine=board.createTeam("mine"),theirs=board.createTeam("theirs");mine.setNamePrefix("\u00a7a");theirs.setNamePrefix("\u00a7a");board.addPlayerToTeam(V116Check.self.getName(),"mine");board.addPlayerToTeam(p.getName(),"theirs");flag("ignore_teammates",true);blocking(p);for(int i=0;i<20;i++)tick();check(V116Check.chat.isEmpty(),"teammate flagged");
  RelationshipService.set(p,1,true);theirs.setNamePrefix("\u00a7c");check(Teams.isTeammate(V116Check.mc,p),"Snowify ignored Slinky Teams relationship bridge");RelationshipService.set(p,1,false);check(!Teams.isTeammate(V116Check.mc,p),"Snowify treated different scoreboard colours as teammates");
  net.minecraft.scoreboard.ScorePlayerTeam plainMine=board.createTeam("plainMine"),plainTheirs=board.createTeam("plainTheirs");board.addPlayerToTeam(V116Check.self.getName(),"plainMine");board.addPlayerToTeam(p.getName(),"plainTheirs");check(!Teams.isTeammate(V116Check.mc,p),"colourless scoreboard teams were treated as teammates");flag("ignore_teammates",false);
  // Exercise source scoring for Keep-Y and Tower with the source's four-sample acceleration window.
  scene();p=other(103,"Scoring");p.inventory.mainInventory[0]=new ItemStack(Blocks.stone,64);p.isSwingInProgress=true;p.rotationPitch=85;p.rotationYaw=0;p.lastTickPosZ=p.posZ+.35;
  for(String expected:new String[]{"Keep-Y","Tower"}){Object state=freshData();ArrayDeque<double[]> samples=(ArrayDeque<double[]>)get(state,"samples");double[] ys=expected.equals("Tower")?new double[]{65,65.2,65.4,65.8}:new double[]{65,65.02,65.03,65.13};for(double y:ys)samples.add(new double[]{0,y,5,0,0,85,1,1});boolean found=false;for(int i=0;i<5;i++){p.ticksExisted++;Object result=call(core,"scaffolding",new Class[]{EntityPlayer.class,state.getClass()},p,state);found|=expected.equals(result);}check(found,expected+" score never crossed source threshold");}
  Object bridge=freshData();p.rotationPitch=80;p.onGround=true;boolean bridgeFlag=false;
  for(int cycle=0;cycle<2;cycle++){p.ticksExisted=200+cycle*5;p.setSneaking(true);p.swingProgressInt=0;call(core,"bridgeAssist",new Class[]{EntityPlayer.class,bridge.getClass()},p,bridge);p.ticksExisted++;p.setSneaking(false);p.swingProgressInt=1;bridgeFlag|=(Boolean)call(core,"bridgeAssist",new Class[]{EntityPlayer.class,bridge.getClass()},p,bridge);}check(bridgeFlag,"Bridge Assist paired-tap detector missing");
  // Real inbound break packets feed both Fast Mine and No Break Delay, processed on tick END.
  scene();p=other(104,"Breaker");for(int i=0;i<4;i++){BlockPos pos=new BlockPos(i,64,5);check(!GameHooks.receivePacket(null,new S25PacketBlockBreakAnim(p.getEntityId(),pos,0)),"break packet cancelled");check(!GameHooks.receivePacket(null,new S28PacketEffect(2001,pos,net.minecraft.block.Block.getStateId(Blocks.stone.getDefaultState()),false)),"effect packet cancelled");}tick();check(alerts("Fast Mine")>=1&&alerts("No Break Delay")>=1,"break checks not wired to inbound/tick");
  scene();p=other(105,"LagRanger");Object lag=get(core,"lag");Class<?>[] moved={net.minecraft.client.Minecraft.class,int.class,double.class,double.class,double.class,boolean.class,long.class};long now=System.currentTimeMillis();call(lag,"moved",moved,V116Check.mc,p.getEntityId(),0D,65D,6D,false,now);call(lag,"moved",moved,V116Check.mc,p.getEntityId(),0D,65D,3D,false,now+10);check((Boolean)call(lag,"hit",new Class[]{net.minecraft.client.Minecraft.class,int.class,long.class},V116Check.mc,p.getEntityId(),now+15),"Lag Range burst window absent");check(alerts("Lag Range")==1,"Lag Range scheduled flag missing");
  set(lag,"suppressUntil",now+1000);call(lag,"moved",moved,V116Check.mc,p.getEntityId(),0D,65D,6D,false,now+20);call(lag,"moved",moved,V116Check.mc,p.getEntityId(),0D,65D,3D,false,now+30);check(((Map<?,?>)get(lag,"windows")).isEmpty(),"lag suppression ignored");
  myau.MyauClient.incomingPacketBuffer.setOwner(myau.IncomingBufferOwner.VELOCITY);check(!SnowifyAntiCheatHost.InboundPackets.active().isEmpty(),"own inbound buffer not visible");myau.MyauClient.incomingPacketBuffer.setOwner(myau.IncomingBufferOwner.NONE);
  java.util.Queue<Runnable> tasks=new java.util.concurrent.ConcurrentLinkedQueue<>();V116Check.put(V116Check.mc,"scheduledTasks",tasks);final int[] ran={0};
  Thread queued=new Thread(()->SnowifyAntiCheatHost.schedule(V116Check.mc,()->ran[0]++));queued.start();queued.join();check(tasks.size()==1,"background flag was not scheduled");module.onWorldUnload(V116Check.c);while(!tasks.isEmpty())tasks.poll().run();check(ran[0]==0,"old-world scheduled flag leaked");
  module.onWorldUnload(V116Check.c);check(players().isEmpty()&&((Map<?,?>)get(lag,"positions")).isEmpty()&&((Queue<?>)get(core,"placeEvents")).isEmpty()&&get(core,"popupName")==null,"world state leaked");
  V116Check.manager.applyNow(module,false);V116Check.chat.clear();blocking(p);for(int i=0;i<30;i++)tick();check(V116Check.chat.isEmpty()&&!Anticheat.marksNametag(p)&&SnowifyAntiCheatHost.playerName("Name",new NetworkPlayerInfo(p.getGameProfile())).equals("Name"),"disabled detector/markers active");
  try(java.util.stream.Stream<Path> paths=Files.walk(Paths.get(args[0]))){check(paths.noneMatch(f->f.getFileName().toString().startsWith("OpenB4AntiCheat")),"retired OpenB4 backend packaged");}
  System.out.println("PASS: Snowify controls/profile migration; Autoblock/Keep-Y/Tower/Bridge Assist/Lag Range/Fast Mine/No Break Delay; interval/threshold/teams; persistent blacklist and markers; normalized popup; tick/packet wiring, off/unload cleanup; retired backend absent");
 }
}

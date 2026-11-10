import dev.rice.anticheat.*;
import java.util.*;
import java.lang.reflect.*;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.*;
import net.minecraft.client.network.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.init.*;
import net.minecraft.network.*;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import com.mojang.authlib.GameProfile;

public final class RiceAnticheatTest {
    static int assertions;static final List<String> chat=new ArrayList<>();static boolean blocking,sneaking;static int sounds;
    static void check(boolean value,String why){assertions++;if(!value)throw new AssertionError(why);}
    static void near(double a,double b,String why){check(Math.abs(a-b)<Math.max(.0001,Math.abs(b)*.00001),why);}
    public static class Local extends EntityPlayerSP {
        public Local(){super(null,null,null,null);}
        @Override public void addChatMessage(IChatComponent message){chat.add(message.getUnformattedText().replaceAll("\u00a7.",""));}
        @Override public void playSound(String s,float a,float b){sounds++;}
        @Override public UUID getUniqueID(){return new UUID(0,getEntityId());}
        @Override public String getName(){return "Self";}
        @Override public boolean isOnSameTeam(EntityLivingBase other){return false;}
    }
    public static class Remote extends EntityOtherPlayerMP {
        public Remote(){super(null,null);}
        @Override public UUID getUniqueID(){return new UUID(0,getEntityId());}
        @Override public String getName(){return "Target";}
        @Override public boolean isBlocking(){return blocking;}
        @Override public boolean isSneaking(){return sneaking;}
        @Override public boolean isSpectator(){return false;}
    }
    static NetworkPlayerInfo info;
    public static class Network extends NetHandlerPlayClient {
        public Network(){super(null,null,null,null);}
        @Override public NetworkPlayerInfo getPlayerInfo(UUID id){return info;}
        @Override public Collection<NetworkPlayerInfo> getPlayerInfoMap(){return Collections.singleton(info);}
    }
    static final net.minecraft.scoreboard.Scoreboard scoreboard=new net.minecraft.scoreboard.Scoreboard();static boolean air;
    public static class World extends FeatureFixture.World {
        public World(){super();}
        @Override public net.minecraft.scoreboard.Scoreboard getScoreboard(){return scoreboard;}
        @Override public net.minecraft.block.state.IBlockState getBlockState(BlockPos p){return (air?Blocks.air:Blocks.stone).getDefaultState();}
    }
    static <T>T cloneTo(Object source,Class<T> target)throws Exception{
        T result=FeatureFixture.blank(target);
        for(Class<?> type=source.getClass().getSuperclass();type!=null;type=type.getSuperclass())for(Field f:type.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(result,f.get(source));}
        return result;
    }
    static Object field(Object object,String name)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
    static Object data()throws Exception{Class<?> type=Class.forName("dev.rice.anticheat.Anticheat$PlayerData");Constructor<?> c=type.getDeclaredConstructor();c.setAccessible(true);return c.newInstance();}
    static Object call(Object object,String method,Class<?>[] types,Object... args)throws Exception{Method m=object.getClass().getDeclaredMethod(method,types);m.setAccessible(true);return m.invoke(object,args);}
    public static void main(String[] args)throws Exception{
        Minecraft mc=FeatureFixture.create();Local local=cloneTo(FeatureFixture.self,Local.class);Remote remote=cloneTo(FeatureFixture.enemy,Remote.class);
        World world=FeatureFixture.blank(World.class);FeatureFixture.put(world,"loadedEntityList",new ArrayList<Entity>(Arrays.asList(local,remote)));FeatureFixture.put(world,"playerEntities",new ArrayList<EntityPlayer>(Arrays.asList(local,remote)));
        mc.thePlayer=local;mc.theWorld=world;local.worldObj=world;remote.worldObj=world;FeatureFixture.put(local,"sendQueue",FeatureFixture.blank(Network.class));info=new NetworkPlayerInfo(new GameProfile(remote.getUniqueID(),"Target"));
        Path directory=Files.createTempDirectory("riceslinky-anticheat-test-");FeatureFixture.put(mc,"mcDataDir",directory.toFile());
        Configuration config=new Configuration();Anticheat detector=new Anticheat(config);
        check(!detector.enabledProp().getBoolean(),"Anticheat defaults off");check(detector.flagIntervalProp().getInt(0)==20&&detector.thresholdProp().getInt(0)==5,"Supplied timing and threshold defaults");
        check(detector.autoblockProp().getBoolean()&&detector.keepYProp().getBoolean()&&detector.towerProp().getBoolean()&&detector.bridgeAssistProp().getBoolean()&&detector.stasisProp().getBoolean()&&detector.fastMineProp().getBoolean()&&detector.noBreakDelayProp().getBoolean(),"All seven original checks default on");
        blocking=true;remote.isSwingInProgress=true;for(int i=0;i<20;i++)detector.onClientTick();check(((Map<?,?>)field(detector,"players")).isEmpty(),"Disabled detector never samples players");
        detector.enabledProp().set(true);detector.shouldPingProp().set(false);detector.flagIntervalProp().set(0);
        for(int tick=0;tick<50;tick++){remote.ticksExisted++;detector.onClientTick();}
        check(Anticheat.isCheater("Target"),"Five supplied Autoblock flags mark player");check(Anticheat.marksNametag(remote),"Marked player gets nametag warning");check(chat.stream().allMatch(s->s.startsWith("[RiceSlinky]")),"All local alerts use RiceSlinky branding");check(chat.stream().noneMatch(s->s.toLowerCase().contains("snowify")),"No Snowify alert prefix");
        Path blacklist=directory.resolve("riceslinky/anticheat_blacklist.json");check(Files.exists(blacklist)&&new String(Files.readAllBytes(blacklist),"UTF-8").contains("Target"),"Blacklist saves in isolated RiceSlinky directory");
        check(Anticheat.decorateTab(info,"Target").contains("\u26a0"),"Tab icon decorates detected player");detector.tabIconProp().set(false);check(Anticheat.decorateTab(info,"Target").equals("Target"),"Tab icon switch respected");
        check(Anticheat.blacklist("Manual","test reason")&&Anticheat.isCheater("Manual"),"Manual blacklist supported");check(Anticheat.unblacklist("Manual")&&!Anticheat.isCheater("Manual"),"Manual removal supported");
        detector.clearWorld();check(((Map<?,?>)field(detector,"players")).isEmpty(),"World reset clears sampled player state");
        remote.inventory.mainInventory[0]=new ItemStack(Blocks.wool);remote.inventory.currentItem=0;remote.rotationPitch=85;remote.rotationYaw=90;remote.hurtTime=0;remote.posX=1.2;remote.lastTickPosX=.8;remote.posZ=remote.lastTickPosZ=0;remote.ticksExisted=100;
        Class<?> dt=Class.forName("dev.rice.anticheat.Anticheat$PlayerData");Object sample=data();@SuppressWarnings("unchecked") ArrayDeque<double[]> samples=(ArrayDeque<double[]>)field(sample,"samples");
        for(double y:new double[]{65,65.3,65.6,66})samples.add(new double[]{0,y,0,0,90,85,1,1});
        check("Tower".equals(call(detector,"scaffolding",new Class<?>[]{EntityPlayer.class,dt},remote,sample)),"Original Tower motion pattern flags");
        sample=data();samples=(ArrayDeque<double[]>)field(sample,"samples");for(double y:new double[]{65,65.1,65.2,65.35})samples.add(new double[]{0,y,0,0,90,85,1,1});
        check("Keep-Y".equals(call(detector,"scaffolding",new Class<?>[]{EntityPlayer.class,dt},remote,sample)),"Original Keep-Y motion pattern flags");
        sample=data();samples=(ArrayDeque<double[]>)field(sample,"samples");for(int i=0;i<4;i++)samples.add(new double[]{0,70,0,0,90,85,1,1});((ArrayDeque<Integer>)field(sample,"placeTicks")).add(100);air=true;remote.setPosition(0,70,0);
        check((Boolean)call(detector,"inStasis",new Class<?>[]{EntityPlayer.class,dt},remote,sample),"Original Stasis requires stationary unsupported placement");air=false;check(!(Boolean)call(detector,"inStasis",new Class<?>[]{EntityPlayer.class,dt},remote,sample),"Support prevents Stasis false flag");
        sample=data();Method bridge=Anticheat.class.getDeclaredMethod("bridgeAssist",EntityPlayer.class,dt);bridge.setAccessible(true);remote.onGround=true;remote.swingProgressInt=1;boolean detected=false;
        for(int tick=200;tick<=204;tick++){remote.ticksExisted=tick;sneaking=tick==200||tick==203;remote.swingProgressInt=sneaking?0:1;detected|=(Boolean)bridge.invoke(null,remote,sample);}
        check(detected,"Original repeated one-tick sneak taps flag Bridge Assist");
        BreakCheck breaking=new BreakCheck();List<String> flags=new ArrayList<>();BreakCheck.Flagger flagger=(p,c,n)->flags.add(c);remote.inventory.mainInventory[0]=new ItemStack(Items.wooden_pickaxe);BlockPos p=new BlockPos(0,64,0);
        Method crack=BreakCheck.class.getDeclaredMethod("crack",Minecraft.class,S25PacketBlockBreakAnim.class,long.class,boolean.class,BreakCheck.Flagger.class);crack.setAccessible(true);
        Method broken=BreakCheck.class.getDeclaredMethod("broken",Minecraft.class,S28PacketEffect.class,long.class,boolean.class,BreakCheck.Flagger.class);broken.setAccessible(true);
        for(int i=0;i<3;i++){BlockPos pos=p.add(i,0,0);crack.invoke(breaking,mc,new S25PacketBlockBreakAnim(2,pos,0),1000L+i*100,true,flagger);broken.invoke(breaking,mc,new S28PacketEffect(2001,pos,net.minecraft.block.Block.getStateId(Blocks.stone.getDefaultState()),false),1050L+i*100,true,flagger);}
        check(flags.contains("Fast Mine")&&flags.contains("No Break Delay"),"Original mining checks flag short breaks and repeated immediate starts");
        for(int level=0;level<4;level++){near(FatigueControl.multiplier(level,75,false),.75/FatigueControl.factor(level),"Fatigue multiplier follows original level factors");near(FatigueControl.multiplier(level,75,true),.75*.71999997/FatigueControl.factor(level),"Original wooden-tool share retained");}
        local.capabilities.isCreativeMode=false;local.inventory.mainInventory[0]=new ItemStack(Items.iron_pickaxe);local.addPotionEffect(new PotionEffect(Potion.digSlowdown.id,200,1));
        check(Double.isNaN(FatigueControl.speed(mc,true,0,75))&&Double.isNaN(FatigueControl.speed(mc,false,2,75)),"Off or disabled Fast Mine preserves original speed");near(FatigueControl.speed(mc,true,2,75),.75/.09,"Increase changes speed only while fatigued");local.removePotionEffect(Potion.digSlowdown.id);check(Double.isNaN(FatigueControl.speed(mc,true,2,75)),"No fatigue leaves ordinary Fast Mine alone");
        S1DPacketEntityEffect effect=new S1DPacketEntityEffect(local.getEntityId(),new PotionEffect(Potion.digSlowdown.id,200,2));
        check(!FatigueControl.packet(mc,effect,true,0),"Off never holds fatigue packets");check(FatigueControl.packet(mc,effect,true,1),"Hold consumes local fatigue effect");check(!FatigueControl.packet(mc,new S1DPacketEntityEffect(99,new PotionEffect(Potion.digSlowdown.id,200,2)),true,1),"Other players' effect packets pass through");check(!FatigueControl.packet(mc,new S1DPacketEntityEffect(local.getEntityId(),new PotionEffect(Potion.moveSpeed.id,200,2)),true,1),"Other potion types pass through");
        FatigueControl.tick(mc,false,1);check(local.getActivePotionEffect(Potion.digSlowdown)!=null,"Disabling Fast Mine restores remaining held effect");local.removePotionEffect(Potion.digSlowdown.id);
        FatigueControl.packet(mc,effect,true,1);check(!FatigueControl.packet(mc,new S1EPacketRemoveEntityEffect(local.getEntityId(),new PotionEffect(Potion.digSlowdown.id,0)),true,1),"Server fatigue removal passes through");FatigueControl.release(mc);check(local.getActivePotionEffect(Potion.digSlowdown)==null,"Removed held effect must not be replayed");
        FatigueControl.packet(mc,effect,true,1);FatigueControl.clear();FatigueControl.release(mc);check(local.getActivePotionEffect(Potion.digSlowdown)==null,"World cleanup discards held effects");
        check(FatigueControl.factor(3)==FatigueControl.factor(9),"High fatigue amplifier factor matches supplied code");
        double[] monitorSettings={1,20,0,0,1,20,0,1,0,0,0,0,0,0,0,0,.5,.12};
        dev.rice.anticheat.Monitor.event(5,monitorSettings);
        dev.rice.anticheat.Monitor.event(18,monitorSettings);
        check(info.getDisplayName()!=null&&info.getDisplayName().getFormattedText().contains("\u26a0"),"Runtime tab display gets a RiceSlinky detector marker");
        IChatComponent serverName=new ChatComponentText("ServerAlias");info.setDisplayName(serverName);dev.rice.anticheat.Monitor.event(18,monitorSettings);
        check(info.getDisplayName().getFormattedText().contains("ServerAlias"),"Server tab-name updates are preserved beneath marker");
        check(dev.rice.anticheat.Monitor.packet(new net.minecraft.network.play.client.C01PacketChatMessage("/BLACKLIST Added local reason"),false,true)==2,"Client blacklist command is consumed locally");
        dev.rice.anticheat.Monitor.event(18,monitorSettings);check(Anticheat.isCheater("Added"),"Case-insensitive local command updates blacklist");
        check(dev.rice.anticheat.Monitor.packet(new net.minecraft.network.play.client.C01PacketChatMessage("hello"),false,true)==0,"Normal outgoing chat is untouched");
        monitorSettings[0]=0;dev.rice.anticheat.Monitor.event(5,monitorSettings);check(info.getDisplayName()==serverName,"Disabling detector restores latest server tab component exactly");
        Files.deleteIfExists(blacklist);Files.deleteIfExists(blacklist.getParent());Files.deleteIfExists(directory);
        System.out.println("RiceSlinky Anticheat/Fatigue: "+assertions+" detector, blacklist, branding, motion, mining and scoped fatigue checks passed");
    }
}

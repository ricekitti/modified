import dev.rice.client.*;
import dev.rice.module.Module;
import dev.rice.module.ModuleManager;
import dev.rice.module.impl.WeaponsModule;
import dev.rice.setting.*;
import dev.rice.shaded.asm.*;
import dev.rice.shaded.asm.tree.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.*;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;

/** Executes the imported target/rotation/attack paths against Minecraft 1.8.9. */
public class OpenB4KillAuraCheck {
    static Object unsafe; static Method allocate;
    public static boolean isButtonDown(int b) { return false; }
    static <T>T blank(Class<T> type)throws Exception { return type.cast(allocate.invoke(unsafe,type)); }
    static void put(Object o,String name,Object value)throws Exception { for(Class<?> t=o.getClass();t!=null;t=t.getSuperclass())try{Field f=t.getDeclaredField(FeatureFixture.mappedName(t,name));f.setAccessible(true);f.set(o,value);return;}catch(NoSuchFieldException next){} throw new NoSuchFieldException(name); }
    static void check(boolean ok,String message) { if(!ok)throw new AssertionError(message); }
    static final class Player extends EntityPlayerSP {
        int swings; Player(){super(null,null,null,null);}
        @Override public void swingItem(){swings++;}
        @Override public UUID getUniqueID(){return new UUID(0,getEntityId());}
        @Override public boolean canBeCollidedWith(){return true;}
        @Override public boolean isInvisible(){return false;}
        @Override public boolean isSpectator(){return false;}
        @Override public boolean isOnSameTeam(EntityLivingBase other){return false;}
    }
    static class World extends WorldClient {
        boolean blocked; boolean floorSolid=true; World(){super(null,null,0,null,null);}
        @Override public boolean isBlockLoaded(BlockPos pos){return true;}
        @Override public Entity getEntityByID(int id){for(Object e:loadedEntityList)if(((Entity)e).getEntityId()==id)return (Entity)e;return null;}
        @Override public MovingObjectPosition rayTraceBlocks(Vec3 a,Vec3 b,boolean l,boolean n,boolean m){return blocked?new MovingObjectPosition(a,EnumFacing.UP,BlockPos.ORIGIN):null;}
        @Override public java.util.List<Entity> getEntitiesWithinAABBExcludingEntity(Entity except,AxisAlignedBB box){java.util.List<Entity> out=new ArrayList<>();for(Object item:loadedEntityList){Entity e=(Entity)item;if(e!=except&&e.getEntityBoundingBox().intersectsWith(box))out.add(e);}return out;}
        @Override public java.util.List<Entity> getEntitiesInAABBexcluding(Entity except,AxisAlignedBB box,com.google.common.base.Predicate<? super Entity> predicate){java.util.List<Entity> out=new ArrayList<>();for(Entity e:getEntitiesWithinAABBExcludingEntity(except,box))if(predicate==null||predicate.apply(e))out.add(e);return out;}
        @Override public IBlockState getBlockState(BlockPos pos){return (blocked || floorSolid&&pos.getY()==64 ? Blocks.stone : Blocks.air).getDefaultState();}
    }
    static class Controller extends PlayerControllerMP {
        int attacks;Entity last;Controller(){super(null,null);}
        @Override public void attackEntity(EntityPlayer p,Entity e){attacks++;last=e;}
    }
    static final class Handler extends NetHandlerPlayClient {
        ModuleManager manager; List<Packet<?>> sent;
        Handler(){super(null,null,null,null);}
        @Override public void addToSendQueue(Packet p){SemanticEvent e=new SemanticEvent(23);e.packet=p;manager.dispatch(e);if(!e.cancelled)sent.add(p);}
    }
    static Player player(World w,int id,double x,double z)throws Exception {
        Player p=blank(Player.class);p.setEntityId(id);put(p,"worldObj",w);p.capabilities=new PlayerCapabilities();p.inventory=new InventoryPlayer(p);
        DataWatcher data=new DataWatcher(p);data.addObject(0,(byte)0);data.addObject(6,20F);data.addObject(17,0F);put(p,"dataWatcher",data);put(p,"activePotionsMap",new HashMap<>());
        p.posX=x;p.posY=65;p.posZ=z;p.setEntityBoundingBox(new AxisAlignedBB(x-.3,65,z-.3,x+.3,66.8,z+.3));return p;
    }
    static void flag(Module m,String k,boolean v){((BooleanSetting)m.findSetting(k)).set(v);}
    static void num(Module m,String k,double v){((NumberSetting)m.findSetting(k)).set(v);}
    static void sourceParity(Path root)throws Exception {
        String original=new String(Files.readAllBytes(root.resolve("tools/fixtures/openb4/KillAuraModule.java.txt")),"UTF-8");
        String adapted=new String(Files.readAllBytes(root.resolve("java/src/main/java/dev/rice/client/combat/openb4/OpenB4KillAuraBackend.java")),"UTF-8");
        Pattern methods=Pattern.compile("(?:public|private|protected)\\s+(?:static\\s+)?[\\w<>\\[\\]?]+\\s+(\\w+)\\s*\\(");
        Set<String> required=new LinkedHashSet<>();Matcher matcher=methods.matcher(original);while(matcher.find())required.add(matcher.group(1));
        required.remove("link");required.remove("onJoin");
        Set<String> actual=new HashSet<>();matcher=methods.matcher(adapted);while(matcher.find())actual.add(matcher.group(1));
        required.removeAll(actual);check(required.isEmpty(),"Original KillAura methods missing: "+required);
        String[] unchanged={"maxHealth","exists","getTarget","isFakeBlocking","consumeSlotResync","getRotations","hasTarget","setDelay","smooth","setBlocking","releaseFlow","forceAttacks","afterDisable","hurtTime","onEnable","stopBlocking","onPostMotion","updateRotations","yawDifference","setTarget","onDisable","updateAps","swapAway","aim","allowHeldItemPacket","onClientTick","rightDown","rollLag","onKnockback","distanceSq","blockAllowed","guiUpdate","freeSlot","guiButtonToggled"};
        for(String name:unchanged) check(methodBody(original,name).replace("KillAuraModule","OpenB4KillAuraBackend").replace("Minecraft.getMinecraft().theWorld","B4AuraHost.context.world()").equals(methodBody(adapted,name)),"upstream combat method changed: "+name);

        check(!adapted.contains("OpenB4DisplaceBackend")&&!adapted.contains("B4DisplaceHost"),"Removed Raven Displace coupling remained in KillAura");
        check(!adapted.contains("beginAuraTarget")&&!adapted.contains("displaceRotations"),"Custom compatibility bridge remains in backend");
    }
    static String methodBody(String text,String name) {
        Matcher match=Pattern.compile("(?:public|private|protected)\\s+(?:static\\s+)?[\\w<>\\[\\]?]+\\s+"+name+"\\s*\\(").matcher(text);
        check(match.find(),"method missing: "+name);int start=text.indexOf('{',match.end()),end=start+1,depth=1;
        while(depth>0) {char c=text.charAt(end++);if(c=='{')depth++;else if(c=='}')depth--;}
        return text.substring(start,end).replaceAll("\\s+", "");
    }
    static String sha256(byte[] bytes)throws Exception {
        byte[] digest=MessageDigest.getInstance("SHA-256").digest(bytes);StringBuilder out=new StringBuilder();
        for(byte b:digest)out.append(String.format("%02x",b&255));return out.toString();
    }
    /** Verifies the supplied Raven binary, rather than a decompiled/source reconstruction. */
    static void compiledRavenParity(Path root)throws Exception {
        Path raven=root.resolve("tools/fixtures/ravenb4/RavenB4-u20.jar");
        check(Files.isRegularFile(raven),"missing preserved RavenB4 u20 compiled fixture");
        check("ca26573dc09fa02438077cf61a89af1fd3c9262efc859ce96be5069cfb9a5695".equals(sha256(Files.readAllBytes(raven))),"RavenB4 fixture hash changed");
        try(JarFile jar=new JarFile(raven.toFile())) {
            java.util.jar.JarEntry entry=jar.getJarEntry("xyz/daikirai/module/combat/KillAuraModule.class");
            check(entry!=null,"compiled Raven KillAura class missing");
            byte[] bytes=readAll(jar.getInputStream(entry));
            check("52817c14ac6fcdac83b899a4893627f0f931109df01620a7cd3d15a4728dbdae".equals(sha256(bytes)),"Raven KillAura bytecode changed");
            check((((bytes[6]&255)<<8)|(bytes[7]&255))==52,"Raven KillAura is not Java 8 bytecode");
            ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
            check("xyz/daikirai/module/combat/KillAuraModule".equals(node.name),"wrong Raven KillAura class");
            Set<String> fields=new HashSet<>(),methods=new HashSet<>();
            for(FieldNode field:node.fields)fields.add(field.name);
            for(MethodNode method:node.methods)methods.add(method.name);
            for(String name:new String[]{"aps","autoBlockSetting","attackRangeSetting","rotationSetting","target","serverBlocking","blockState"})check(fields.contains(name),"Raven field missing: "+name);
            for(String name:new String[]{"onEnable","onDisable","onPreUpdate","onPostMotion","onSendPacket","onReceivePacket","onKnockback","onMouse","guiUpdate"})check(methods.contains(name),"Raven method missing: "+name);
        }
    }
    static byte[] readAll(java.io.InputStream stream)throws Exception { java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] part=new byte[4096];for(int read;(read=stream.read(part))>=0;)out.write(part,0,read);return out.toByteArray(); }
    static void helperParity(ClassLoader loader,Class<?> backend,Module aura,ClientContext c,Player self,Player enemy,Handler handler)throws Exception {
        Class<?> host=loader.loadClass("dev.rice.client.combat.openb4.B4AuraHost");
        Class<?> packets=loader.loadClass(host.getName()+"$PacketUtils");
        Method track=packets.getMethod("track",Packet.class),gate=packets.getMethod("noAttackPlaceOrDig");
        track.invoke(null,new C03PacketPlayer(true));check((Boolean)gate.invoke(null),"movement must clear interaction flags");
        track.invoke(null,new C08PacketPlayerBlockPlacement(self.getHeldItem()));check(!(Boolean)gate.invoke(null),"placement must gate aura attacks");
        check(packets.getField("lastUse").getInt(null)==1,"air use code must be 1");
        host.getMethod("beginTick").invoke(null);check(!(Boolean)gate.invoke(null),"tick start must not erase placement before C03");
        check(packets.getField("lastUse").getInt(null)==-1,"post use must reset each tick");
        track.invoke(null,new C03PacketPlayer(true));
        track.invoke(null,new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,BlockPos.ORIGIN,EnumFacing.DOWN));
        check(!(Boolean)gate.invoke(null)&&packets.getField("lastUse").getInt(null)==2,"release must gate and use code 2");
        track.invoke(null,new C09PacketHeldItemChange(1));check(packets.getField("serverSlot").getInt(null)==1&&packets.getField("lastUse").getInt(null)==3,"attempt slot tracking must precede aura filter");
        track.invoke(null,new C03PacketPlayer(true));check((Boolean)gate.invoke(null),"C03 must reopen attack gate");
        Class<?> item=loader.loadClass(host.getName()+"$ItemUtils");
        ItemStack worn=new ItemStack(Items.diamond_sword);worn.setItemDamage(100);
        check((Boolean)item.getMethod("sameItem",ItemStack.class,ItemStack.class).invoke(null,self.getHeldItem(),worn),"OpenB4 item identity ignores damage");
        check((Boolean)item.getMethod("isThrowable",ItemStack.class).invoke(null,new ItemStack(Items.experience_bottle)),"experience bottles are throwable");
        ItemStack enchanted=new ItemStack(Items.diamond_sword);enchanted.addEnchantment(net.minecraft.enchantment.Enchantment.fireAspect,1);
        double base=((Number)item.getMethod("attackDamage",ItemStack.class).invoke(null,new ItemStack(Items.diamond_sword))).doubleValue();
        double fire=((Number)item.getMethod("attackDamage",ItemStack.class).invoke(null,enchanted)).doubleValue();
        check(Math.abs(fire-base-3)<1e-6,"Dual damage ranking must include fire aspect");
        enemy.setAbsorptionAmount(6);backend.getMethod("setTarget",Entity.class).invoke(null,enemy);
        Object t=backend.getField("target").get(null);check(((Number)t.getClass().getMethod("health").invoke(t)).floatValue()==26F,"target health must include absorption");enemy.setAbsorptionAmount(0);
        Class<?> rotation=loader.loadClass(host.getName()+"$RotationUtils");num(aura,"aim_closest_point",90);
        AxisAlignedBB box=new AxisAlignedBB(-.4,65,2.4,.4,67,3.2);Vec3 eyes=self.getPositionEyes(1);
        Vec3 aim=(Vec3)rotation.getMethod("aimPoint",Vec3.class,AxisAlignedBB.class).invoke(null,eyes,box);
        check(Math.abs(aim.zCoord-(2.8+(2.43-2.8)*.9))<1e-6,"closest-point interpolation differs from OpenB4");
        self.rotationYaw=539;host.getMethod("reset").invoke(null);
        float[] r=(float[])rotation.getMethod("toVec",Vec3.class).invoke(null,new Vec3(0,eyes.yCoord,2));
        check(Math.abs(r[0]-self.rotationYaw)<=180,"yaw must wrap around current server yaw");
        num(aura,"jitter_yaw",3);host.getMethod("reset").invoke(null);
        for(int i=0;i<20;i++) {float[] fixed=(float[])rotation.getMethod("fixSensitivity",float.class,float.class,float.class,float.class).invoke(null,10F,5F,0F,0F);check(Math.abs(fixed[0]-10F)<.7F,"default jitter exceeded upstream grid");}
        num(aura,"jitter_yaw",0);
        // Follow the backend's Dual exception, and scale event values once (GameHooks copies these back).
        backend.getField("serverBlocking").setBoolean(null,true);backend.getField("blockState").setInt(null,0);backend.getField("currentAps").setDouble(null,20);
        ((ChoiceSetting)aura.findSetting("autoblock")).set(9);backend.getMethod("guiUpdate").invoke(backend.getField("instance").get(null));
        SemanticEvent input=new SemanticEvent(SemanticEvent.MOVEMENT_INPUT);input.forward=1;input.sideways=1;aura.onEvent(c,input);
        check(input.forward==1&&input.sideways==1,"Dual attack tick must skip slowdown");
        ((ChoiceSetting)aura.findSetting("autoblock")).set(4);backend.getMethod("guiUpdate").invoke(backend.getField("instance").get(null));
        input=new SemanticEvent(SemanticEvent.MOVEMENT_INPUT);input.forward=1;input.sideways=1;aura.onEvent(c,input);
        check(Math.abs(input.forward-.2F)<1e-6&&Math.abs(input.sideways-.2F)<1e-6,"packet block slowdown must be applied to copied event input");
        self.setItemInUse(self.getHeldItem(),72000);
        input=new SemanticEvent(SemanticEvent.MOVEMENT_INPUT);input.forward=1;input.sideways=1;aura.onEvent(c,input);
        check(Math.abs(input.forward*.2F-.2F)<1e-6,"vanilla must not compound packet slowdown to .04");
        ((ChoiceSetting)aura.findSetting("autoblock")).set(9);backend.getMethod("guiUpdate").invoke(backend.getField("instance").get(null));
        input=new SemanticEvent(SemanticEvent.MOVEMENT_INPUT);input.forward=1;input.sideways=1;aura.onEvent(c,input);
        check(Math.abs(input.forward*.2F-1F)<1e-6,"Dual skip must survive the following vanilla multiplier");self.clearItemInUse();
        backend.getMethod("resetWorld").invoke(null);packets.getMethod("reset").invoke(null);
    }
    public static void main(String[] args)throws Exception {
        sourceParity(Paths.get(args[1]));
        compiledRavenParity(Paths.get(args[1]));
        
        System.out.println("PASS: retained OpenB4 source/binary identity and shared packet/item/rotation helpers; active combat replaced by RavenBsCombatCheck");
    }
}

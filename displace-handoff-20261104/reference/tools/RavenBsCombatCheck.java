import dev.rice.client.*;
import dev.rice.client.rotation.RotationService;
import dev.rice.module.Module;
import dev.rice.module.ModuleManager;
import dev.rice.module.impl.WeaponsModule;
import dev.rice.setting.*;
import dev.rice.shaded.asm.*;
import dev.rice.shaded.asm.tree.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
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

/** Executes the active bS adapters against Minecraft 1.8.9, without launching or injecting. */
public final class RavenBsCombatCheck extends OpenB4KillAuraCheck {
    static boolean leftDown;
    public static boolean isButtonDown(int button) { return button == 0 && leftDown; }
    static void init()throws Exception {
        if(unsafe==null){Class<?> u=Class.forName("sun.misc.Unsafe");Field f=u.getDeclaredField("theUnsafe");f.setAccessible(true);unsafe=f.get(null);allocate=u.getMethod("allocateInstance",Class.class);}
        Bootstrap.register();
    }
    static Object field(Object o,String name)throws Exception {Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
    static void setField(Object o,String name,Object value)throws Exception {Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
    static Object invoke(Object o,String name,Class<?>[] types,Object... values)throws Exception {Method m=o.getClass().getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(o,values);}
    static final class Remote extends net.minecraft.client.entity.EntityOtherPlayerMP {
        Remote(){super(null,null);}
        @Override public UUID getUniqueID(){return new UUID(0,getEntityId());}
        @Override public boolean canBeCollidedWith(){return true;}
        @Override public boolean isInvisible(){return false;}
        @Override public boolean isSpectator(){return false;}
        @Override public boolean isOnSameTeam(EntityLivingBase other){return false;}
    }
    static final class RecordingController extends Controller {
        @Override public void attackEntity(EntityPlayer p,Entity e){super.attackEntity(p,e);((EntityPlayerSP)p).sendQueue.addToSendQueue(new C02PacketUseEntity(e,C02PacketUseEntity.Action.ATTACK));}
    }
    static final class Network extends net.minecraft.network.NetworkManager {
        List<Packet<?>> sent;
        Network(){super(net.minecraft.network.EnumPacketDirection.CLIENTBOUND);}
        @Override public void sendPacket(Packet packet){sent.add(packet);}
    }
    static final class Fixture {
        final Minecraft mc; final World world; final Player self; final Remote enemy;
        final Controller controller; final ModuleManager manager; final ClientContext c;
        final Module aura,ka; final dev.rice.module.impl.KnockbackDisplaceModule v72;
        final Class<?> backend,kaClass; final Handler handler; final ClassLoader loader;
        final Object auraBackend;
        Fixture(Path classes,boolean withV72,boolean displaceEnabled)throws Exception {
            init();leftDown=false;RotationService.reset(null);RelationshipService.clear();KeyBinding.unPressAllKeys();
            loader=new ClassLoader(RavenBsCombatCheck.class.getClassLoader()) {
                @Override protected synchronized Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException {
                    if(!name.startsWith("dev.rice.client.combat.ravenbs.")&&!name.startsWith("dev.rice.client.combat.openb4.")&&!name.equals("dev.rice.module.impl.KillAuraModule")&&!name.equals("dev.rice.module.impl.KillAuraDisplaceModule"))return super.loadClass(name,resolve);
                    Class<?> cached=findLoadedClass(name);if(cached!=null)return cached;
                    try{String resource=name.replace('.','/')+".class";Path local=classes.resolve(resource);byte[] bytes=Files.isRegularFile(local)?Files.readAllBytes(local):readAll(getParent().getResourceAsStream(resource));ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
                        for(MethodNode m:node.methods)for(AbstractInsnNode ins:m.instructions.toArray())if(ins instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)ins;if(call.owner.equals("org/lwjgl/input/Mouse"))call.owner="RavenBsCombatCheck";}
                        ClassWriter writer=new ClassWriter(0);node.accept(writer);bytes=writer.toByteArray();Class<?> type=defineClass(name,bytes,0,bytes.length);if(resolve)resolveClass(type);return type;
                    }catch(Exception ex){throw new ClassNotFoundException(name,ex);}
                }
            };
            mc=blank(Minecraft.class);world=blank(World.class);world.floorSolid=true;put(world,"loadedEntityList",new ArrayList<Entity>());put(world,"playerEntities",new ArrayList<EntityPlayer>());
            self=player(world,101,0,0);self.rotationYaw=73;self.rotationPitch=11;self.prevRotationYaw=73;self.prevRotationPitch=11;
            self.lastTickPosX=self.posX;self.lastTickPosY=self.posY;self.lastTickPosZ=self.posZ;
            self.movementInput=new MovementInput();self.moveForward=.75F;self.moveStrafing=-.25F;self.motionX=.12;self.motionY=.03;self.motionZ=.17;
            enemy=remote(102,0,2.8);world.loadedEntityList.add(enemy);world.playerEntities.add(enemy);
            controller=blank(RecordingController.class);put(mc,"thePlayer",self);put(mc,"theWorld",world);put(mc,"playerController",controller);put(mc,"inGameHasFocus",true);put(mc,"renderViewEntity",self);put(mc,"entityRenderer",blank(net.minecraft.client.renderer.EntityRenderer.class));
            GameSettings keys=blank(GameSettings.class);keys.mouseSensitivity=.5F;keys.keyBindAttack=new KeyBinding("bs.attack",-100,"test");keys.keyBindUseItem=new KeyBinding("bs.use",-99,"test");keys.keyBindForward=new KeyBinding("bs.forward",17,"test");keys.keyBindBack=new KeyBinding("bs.back",31,"test");keys.keyBindLeft=new KeyBinding("bs.left",30,"test");keys.keyBindRight=new KeyBinding("bs.right",32,"test");put(mc,"gameSettings",keys);
            self.inventory.mainInventory[0]=new ItemStack(Items.diamond_sword);self.getDataWatcher().updateObject(0,(byte)8);
            Field singleton=Minecraft.class.getDeclaredField("theMinecraft");singleton.setAccessible(true);singleton.set(null,mc);
            c=new ClientContext(mc);manager=new ModuleManager(c);WeaponsModule weapons=new WeaponsModule();
            aura=(Module)loader.loadClass("dev.rice.module.impl.KillAuraModule").getConstructor(WeaponsModule.class,ModuleManager.class).newInstance(weapons,manager);
            kaClass=loader.loadClass("dev.rice.module.impl.KillAuraDisplaceModule");ka=(Module)kaClass.getConstructor().newInstance();manager.register(aura);manager.register(ka);
            v72=withV72?new dev.rice.module.impl.KnockbackDisplaceModule(weapons,manager):null;if(v72!=null)manager.register(v72);
            dev.rice.config.InitialSettings.applyMissing(manager);
            handler=blank(Handler.class);handler.manager=manager;handler.sent=new ArrayList<>();put(self,"sendQueue",handler);Network network=blank(Network.class);network.sent=handler.sent;put(handler,"netManager",network);
            flag(aura,"weapon_only",false);num(aura,"targets",1);num(aura,"speed",30);
            if(v72!=null){for(Setting<?> setting:v72.getSettings())if(setting instanceof BooleanSetting)((BooleanSetting)setting).set(false);flag(v72,"require_weapon",false);flag(v72,"require_kb_enchant",false);flag(v72,"require_facing_away",false);flag(v72,"require_no_kb",false);num(v72,"cooldown",0);num(v72,"angle",90);((ChoiceSetting)v72.findSetting("direction")).set(0);}
            manager.requestEnabled(aura,true);manager.requestEnabled(ka,displaceEnabled);if(v72!=null)manager.requestEnabled(v72,true);manager.applyPendingChanges();
            backend=loader.loadClass("dev.rice.client.combat.ravenbs.RavenBsKillAuraBackend");auraBackend=field(aura,"backend");
            mc.objectMouseOver=new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS,new Vec3(8,66,8),EnumFacing.UP,BlockPos.ORIGIN);
            if(v72!=null)for(int i=0;i<12;i++)v72.onEvent(c,new SemanticEvent(18));
        }
        Remote remote(int id,double x,double z)throws Exception {Remote e=blank(Remote.class);e.setEntityId(id);put(e,"worldObj",world);put(e,"gameProfile",new com.mojang.authlib.GameProfile(new UUID(0,id),"target"+id));e.capabilities=new PlayerCapabilities();e.inventory=new InventoryPlayer(e);DataWatcher d=new DataWatcher(e);d.addObject(0,(byte)0);d.addObject(6,20F);d.addObject(17,0F);put(e,"dataWatcher",d);put(e,"activePotionsMap",new HashMap<>());e.posX=x;e.posY=65;e.posZ=z;e.lastTickPosX=x;e.lastTickPosY=65;e.lastTickPosZ=z;e.setEntityBoundingBox(new AxisAlignedBB(x-.3,65,z-.3,x+.3,66.8,z+.3));return e;}
        Entity target()throws Exception{return (Entity)backend.getField("target").get(null);}
        void acquire(){manager.clientTick(true);RotationService.beginFrame(c,0,0);manager.dispatch(new SemanticEvent(17));manager.dispatch(new SemanticEvent(18));manager.clientTick(false);}
        void preInteract(){manager.dispatch(new SemanticEvent(28));}
        void mouseOver(){manager.dispatch(new SemanticEvent(8));SemanticEvent e=new SemanticEvent(29);e.timerRenderPartialTicks=1;manager.dispatch(e);}
        void walk(){C03PacketPlayer p=new C03PacketPlayer.C06PacketPlayerPosLook(self.posX,self.posY,self.posZ,self.rotationYaw,self.rotationPitch,true);handler.addToSendQueue(p);}
        void close(){manager.worldUnload();RotationService.reset(c);RelationshipService.clear();KeyBinding.unPressAllKeys();}
    }
    static void auraBehavior(Path classes)throws Exception {
        Fixture f=new Fixture(classes,false,false);
        check(f.aura.getId().equals("killaura")&&f.ka.getId().equals("killauradisplace"),"combat module IDs changed");
        check(f.aura.findSetting("aim_range")!=null&&f.aura.findSetting("speed")!=null&&f.aura.findSetting("hit_through_entities")!=null,"bS settings not exposed");
        f.acquire();check(f.target()==f.enemy,"distance selection failed");
        check(f.backend.getField("attackingEntity").get(null)==f.enemy,"bS attack-range entity missing");
        SemanticEvent camera=new SemanticEvent(13);camera.active=false;f.manager.dispatch(camera);check(camera.active,"silent rotations did not retain camera lifecycle");
        f.preInteract();check(f.mc.gameSettings.keyBindAttack.isPressed(),"bS did not queue vanilla attack key");check(!f.mc.gameSettings.keyBindAttack.isPressed(),"one due click queued duplicate presses");
        int before=f.controller.attacks;f.preInteract();check(!f.mc.gameSettings.keyBindAttack.isPressed()&&f.controller.attacks==before,"click pacing bypassed key path");
        f.mouseOver();check(f.mc.objectMouseOver!=null&&f.mc.objectMouseOver.entityHit==f.enemy,"mouse-over hook did not select aligned bS target");
        // A second target with better hurt time must win after the bS switch interval.
        Remote b=f.remote(103,1.6,2.5);f.world.loadedEntityList.add(b);f.world.playerEntities.add(b);f.enemy.hurtTime=8;b.hurtTime=1;((ChoiceSetting)f.aura.findSetting("sort_mode")).set(2);num(f.aura,"switch_delay",50);f.self.ticksExisted+=2;f.acquire();check(f.target()==b,"bS ascending hurt-time sort failed");
        RelationshipService.set(f.enemy,4,true);RelationshipService.set(b,2,true);flag(f.aura,"ignore_teammates",false);f.acquire();check(f.target()==null,"friend/bot filter failed when teammates allowed");RelationshipService.clear();
        f.world.blocked=true;f.acquire();check(f.target()==null,"wall visibility filter failed");flag(f.aura,"hit_through_walls",true);f.acquire();check(f.target()!=null,"through-wall option failed");f.world.blocked=false;flag(f.aura,"hit_through_walls",false);
        f.world.loadedEntityList.clear();f.world.playerEntities.clear();Remote edge=f.remote(104,0,3.25);f.world.loadedEntityList.add(edge);f.world.playerEntities.add(edge);num(f.aura,"attack_range",3);num(f.aura,"swing_range",3);f.acquire();check(f.target()==edge,"AABB edge target not acquired");check(f.backend.getField("attackingEntity").get(null)==edge,"AABB edge incorrectly outside attack reach");
        flag(f.aura,"require_mouse_down",true);f.acquire();check(f.target()==null,"required physical mouse was ignored");leftDown=true;f.acquire();check(f.target()==edge,"physical mouse did not enable aura");flag(f.aura,"require_mouse_down",false);leftDown=false;
        f.mc.inGameHasFocus=false;f.acquire();check(f.target()==null,"focus loss did not stop combat");f.mc.inGameHasFocus=true;f.mc.currentScreen=new net.minecraft.client.gui.GuiChat();f.acquire();check(f.target()==null,"inventory GUI gate ignored");f.mc.currentScreen=null;
        f.acquire();f.manager.requestEnabled(f.aura,false);f.manager.applyPendingChanges();check(f.target()==null&&f.backend.getField("attackingEntity").get(null)==null,"disable leaked targets");
        f.manager.requestEnabled(f.aura,true);f.manager.applyPendingChanges();f.acquire();f.close();check(f.target()==null,"world unload leaked target");
        System.out.println("PASS: bS target sorting, tick switch interval, friends/bots, AABB reach, walls, physical-input and GUI gates, click pacing through vanilla key, mouse-over, camera, disable/unload");
    }
    static void pacingParity(Path classes)throws Exception {
        Fixture f=new Fixture(classes,false,false);Random expected=new Random(551);setField(f.auraBackend,"rand",new Random(551));
        for(double cps:new double[]{1,5,10,10.5,20}){num(f.aura,"target_cps",cps);for(int i=0;i<50;i++){long actual=((Number)invoke(f.auraBackend,"nextDelay",new Class<?>[0])).longValue();int value=1000/Math.max(1,(int)cps)+expected.nextInt(21)-10;long want=Math.max(33,Math.min(180,value));check(actual==want,"bS nextDelay mismatch at CPS "+cps+": "+actual+" != "+want);}}
        f.close();System.out.println("PASS: seeded bS CPS integer truncation, jitter and 33–180 ms clamp (250 draws)");
    }
    static void priorityAndFov(Path classes)throws Exception {
        Fixture f=new Fixture(classes,false,false);Remote far=f.remote(105,1,2.8);f.world.loadedEntityList.add(far);f.world.playerEntities.add(far);
        flag(f.aura,"prioritize_enemies",true);f.acquire();check(f.target()==f.enemy,"empty explicit enemy list changed distance ordering");
        ((StringListSetting)f.aura.findSetting("enemies")).set(Arrays.asList(far.getName().toUpperCase(java.util.Locale.ROOT)));f.self.ticksExisted+=10;f.acquire();check(f.target()==far,"explicit farther enemy was not prioritized case-insensitively");
        flag(f.aura,"prioritize_enemies",false);f.self.ticksExisted+=10;f.acquire();check(f.target()==f.enemy,"disabled priority still preferred explicit enemy");
        f.world.loadedEntityList.clear();f.world.playerEntities.clear();Remote overlap=f.remote(106,0,-.1);overlap.setEntityBoundingBox(new AxisAlignedBB(-.3,65,-.4,.3,67,.2));f.world.loadedEntityList.add(overlap);f.world.playerEntities.add(overlap);num(f.aura,"fov",30);f.self.rotationYaw=0;f.acquire();check(f.target()==null,"inside-AABB target bypassed bS angular FOV");f.close();
        System.out.println("PASS: explicit enemy priority, empty-list/disabled behavior, case-insensitivity and inside-AABB FOV rejection");
    }
    static void rotationParity(Path classes,Path root)throws Exception {
        init();Fixture f=new Fixture(classes,false,false);
        String fixture=new String(Files.readAllBytes(root.resolve("tools/fixtures/ravenbs/RotationUtils.java.txt")),"UTF-8");
        String method=methodBody(fixture,"smoothRotation"); // first overload delegates to six-argument implementation
        int from=fixture.indexOf("public static float[] smoothRotation(float baseYaw, float basePitch, float targetYaw, float targetPitch, int speed, float randomizationPercent)");
        int start=fixture.indexOf('{',from),end=start+1,depth=1;while(depth>0){char ch=fixture.charAt(end++);if(ch=='{')depth++;else if(ch=='}')depth--;}
        String body=fixture.substring(start,end).replace("MathHelper.func_76142_g","MathHelper.wrapAngleTo180_float").replace("MathHelper.func_76133_a","MathHelper.sqrt_double");
        Path temp=Files.createTempDirectory("rice-bs-rotation-oracle-");Path source=temp.resolve("BsRotationOracle.java");
        Files.write(source,("import net.minecraft.util.MathHelper;public class BsRotationOracle {public static float clampPitch(float v){return MathHelper.clamp_float(v,-90,90);}public static float[] smoothRotation(float baseYaw,float basePitch,float targetYaw,float targetPitch,int speed,float randomizationPercent)"+body+"}").getBytes("UTF-8"));
        check(javax.tools.ToolProvider.getSystemJavaCompiler().run(null,null,null,"--release","8","-cp",System.getProperty("java.class.path"),"-d",temp.toString(),source.toString())==0,"bS oracle compilation failed");
        Class<?> rotations=f.loader.loadClass("dev.rice.client.combat.ravenbs.RavenBsRotations");
        try(java.net.URLClassLoader l=new java.net.URLClassLoader(new java.net.URL[]{temp.toUri().toURL()})){Method oracle=l.loadClass("BsRotationOracle").getMethod("smoothRotation",float.class,float.class,float.class,float.class,int.class,float.class);Method actual=rotations.getMethod("smoothRotation",float.class,float.class,float.class,float.class,int.class,float.class);Random random=new Random(182);for(int i=0;i<600;i++){float yaw=random.nextFloat()*1440-720,pitch=random.nextFloat()*180-90,ty=random.nextFloat()*1440-720,tp=random.nextFloat()*220-110;int speed=i%33;float[] want=(float[])oracle.invoke(null,yaw,pitch,ty,tp,speed,0F);float[] got=(float[])actual.invoke(null,yaw,pitch,ty,tp,speed,0F);check(Float.floatToIntBits(want[0])==Float.floatToIntBits(got[0])&&Float.floatToIntBits(want[1])==Float.floatToIntBits(got[1]),"bS smoothing oracle mismatch #"+i);}}
        f.close();System.out.println("PASS: bS smoothing compiled-reference bit equality (600 angle/speed cases)");
    }
    static void legacyHelpers(Path classes)throws Exception {
        Fixture f=new Fixture(classes,false,false);Class<?> host=f.loader.loadClass("dev.rice.client.combat.openb4.B4AuraHost");host.getField("context").set(null,f.c);
        Class<?> packets=f.loader.loadClass(host.getName()+"$PacketUtils");Method track=packets.getMethod("track",Packet.class),gate=packets.getMethod("noAttackPlaceOrDig");
        track.invoke(null,new C03PacketPlayer(true));check((Boolean)gate.invoke(null),"movement did not clear shared interaction flags");track.invoke(null,new C08PacketPlayerBlockPlacement(f.self.getHeldItem()));check(!(Boolean)gate.invoke(null),"placement did not gate interaction");check(packets.getField("lastUse").getInt(null)==1,"air-use code changed");host.getMethod("beginTick").invoke(null);check(!(Boolean)gate.invoke(null),"tick erased pre-movement placement");track.invoke(null,new C03PacketPlayer(true));track.invoke(null,new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,BlockPos.ORIGIN,EnumFacing.DOWN));check(!(Boolean)gate.invoke(null)&&packets.getField("lastUse").getInt(null)==2,"release gate changed");track.invoke(null,new C09PacketHeldItemChange(1));check(packets.getField("serverSlot").getInt(null)==1,"slot tracking changed");
        Class<?> item=f.loader.loadClass(host.getName()+"$ItemUtils");ItemStack worn=new ItemStack(Items.diamond_sword);worn.setItemDamage(100);check((Boolean)item.getMethod("sameItem",ItemStack.class,ItemStack.class).invoke(null,f.self.getHeldItem(),worn),"shared item identity now depends on wear");check((Boolean)item.getMethod("isThrowable",ItemStack.class).invoke(null,new ItemStack(Items.experience_bottle)),"shared throwable recognition changed");ItemStack enchanted=new ItemStack(Items.diamond_sword);enchanted.addEnchantment(net.minecraft.enchantment.Enchantment.fireAspect,1);double base=((Number)item.getMethod("attackDamage",ItemStack.class).invoke(null,new ItemStack(Items.diamond_sword))).doubleValue(),fire=((Number)item.getMethod("attackDamage",ItemStack.class).invoke(null,enchanted)).doubleValue();check(Math.abs(fire-base-3)<1e-6,"shared fire-aspect damage ranking changed");f.close();
    }
    static void displaceBehavior(Path classes)throws Exception {
        Fixture f=new Fixture(classes,false,true);check(f.ka.getName().equals("KillAura Displace")&&f.ka.hudName().equals("Displace"),"Displace identity/HUD name changed");
        check(((ChoiceSetting)f.ka.findSetting("mode")).getOptions().equals(Arrays.asList("Offset","Void")),"bS modes missing");
        check(((NumberSetting)f.ka.findSetting("angle")).doubleValue()==90&&((NumberSetting)f.ka.findSetting("delay")).doubleValue()==0,"bS angle/delay defaults changed");
        check(!((BooleanSetting)f.ka.findSetting("override_attacks")).isEnabled()&&!((BooleanSetting)f.ka.findSetting("only_knockback_items")).isEnabled(),"bS toggle defaults changed");
        Object displace=f.kaClass.getMethod("backend").invoke(f.ka);
        f.acquire();f.acquire();check((Boolean)invoke(displace,"isDisplacing",new Class<?>[0]),"Offset did not flick on first bS tick");float flick=f.self.rotationYaw;
        check(Math.abs(MathHelper.wrapAngleTo180_float(flick+90))<.3,"Left offset differs from bS 90 degrees: "+flick);
        check(f.controller.attacks==0,"rotation callback directly attacked instead of queueing vanilla clicks");
        f.acquire();check(!(Boolean)invoke(displace,"isDisplacing",new Class<?>[0]),"bS did not alternate back to aim");check(Math.abs(MathHelper.wrapAngleTo180_float(f.self.rotationYaw))<.3,"bS did not restore target aim");check(f.mc.gameSettings.keyBindAttack.isPressed(),"return tick did not queue bS follow-up click");f.close();
        f=new Fixture(classes,false,true);((ChoiceSetting)f.ka.findSetting("direction")).set(1);f.acquire();f.acquire();check(Math.abs(MathHelper.wrapAngleTo180_float(f.self.rotationYaw-90))<.3,"Right offset differs from bS");f.close();
        f=new Fixture(classes,false,true);flag(f.ka,"only_knockback_items",true);f.acquire();displace=f.kaClass.getMethod("backend").invoke(f.ka);check(!(Boolean)invoke(displace,"isActive",new Class<?>[0]),"Only-knockback-items displaced plain sword");f.self.inventory.mainInventory[0].addEnchantment(net.minecraft.enchantment.Enchantment.knockback,1);f.acquire();check((Boolean)invoke(displace,"isDisplacing",new Class<?>[0]),"knockback enchant did not allow displace: kb="+net.minecraft.enchantment.EnchantmentHelper.getKnockbackModifier(f.self)+" active="+invoke(displace,"isActive",new Class<?>[0])+" attacking="+(f.backend.getField("attackingEntity").get(null)!=null)+" raw="+field(displace,"displaceThisTick"));f.close();
        // The override must wait for its away-looking movement packet before sending C02.
        f=new Fixture(classes,false,true);flag(f.ka,"override_attacks",true);flag(f.ka,"hide_rotations",true);f.acquire();displace=f.kaClass.getMethod("backend").invoke(f.ka);f.mc.objectMouseOver=new MovingObjectPosition(f.enemy,f.enemy.getPositionVector());
        SemanticEvent click=new SemanticEvent(14);click.active=true;f.manager.dispatch(click);check(click.cancelled&&invoke(displace,"phase",new Class<?>[0]).equals("FLICKING_AWAY"),"override did not intercept vanilla attack");check((Boolean)invoke(displace,"holdsPackets",new Class<?>[0]),"override blink not owned");
        f.preInteract();check(f.controller.attacks==0,"override attacked before away movement");f.acquire();flick=f.self.rotationYaw;
        check(Math.abs(MathHelper.wrapAngleTo180_float(flick+90))<.3,"override away yaw wrong");
        // A canceled movement attempt must not advance the packet confirmation gate.
        SemanticEvent rejected=new SemanticEvent(23);rejected.packet=new C03PacketPlayer.C05PacketPlayerLook(flick,f.self.rotationPitch,true);rejected.cancelled=true;f.manager.dispatch(rejected);check(!(Boolean)field(displace,"overrideAwayPacketSent"),"canceled move confirmed override look");
        f.walk();check((Boolean)field(displace,"overrideAwayPacketSent"),"accepted away movement was not confirmed");
        f.manager.clientTick(true);check(invoke(displace,"phase",new Class<?>[0]).equals("ATTACKING"),"override did not advance after away movement");int packetCount=f.handler.sent.size();f.preInteract();
        check(f.controller.attacks==1&&f.controller.last==f.enemy,"override did not emit one attack through controller");check(invoke(displace,"phase",new Class<?>[0]).equals("RESTORING"),"override did not enter restore phase");
        check(f.handler.sent.subList(packetCount,f.handler.sent.size()).stream().anyMatch(p->p instanceof C02PacketUseEntity&&((C02PacketUseEntity)p).getAction()==C02PacketUseEntity.Action.ATTACK),"override attack did not produce C02");check(!(Boolean)invoke(displace,"holdsPackets",new Class<?>[0]),"C02 did not release override blink");
        f.preInteract();check(f.controller.attacks==1,"restore phase duplicated attack");f.acquire();check(Math.abs(MathHelper.wrapAngleTo180_float(f.self.rotationYaw))<.3,"override failed to restore aim");f.manager.clientTick(true);check(invoke(displace,"phase",new Class<?>[0]).equals("IDLE"),"restore phase never ended");
        // Focus loss cancels a newly armed override and releases its packet hold.
        f.mc.objectMouseOver=new MovingObjectPosition(f.enemy,f.enemy.getPositionVector());click=new SemanticEvent(14);click.active=true;f.manager.dispatch(click);check((Boolean)invoke(displace,"holdsPackets",new Class<?>[0]),"focus test failed to arm blink");f.mc.inGameHasFocus=false;f.manager.clientTick(true);check(invoke(displace,"phase",new Class<?>[0]).equals("IDLE")&&!(Boolean)invoke(displace,"holdsPackets",new Class<?>[0]),"focus loss retained state/hold");f.close();
        System.out.println("PASS: bS Offset Left/Right, alternate/follow-up click, KB gate, override cancellation, accepted C03 before C02, restore, no duplicate hit, blink and focus cleanup");
    }
    static void configAndTeams(Path classes)throws Exception {
        Fixture f=new Fixture(classes,false,false);com.google.gson.JsonObject root=new com.google.gson.JsonObject(),old=new com.google.gson.JsonObject();old.addProperty("mode","Right");old.addProperty("angle",75);old.addProperty("__on",false);root.add("KillAura Displace",old);String before=root.toString();
        dev.rice.config.RecoveredConfiguration.apply(f.manager,root,dev.rice.config.RecoveredConfiguration.Scope.PROFILE);f.manager.applyPendingChanges();check(((ChoiceSetting)f.ka.findSetting("mode")).selectedLabel().equals("Offset")&&((ChoiceSetting)f.ka.findSetting("direction")).selectedLabel().equals("Right"),"old Right profile did not migrate to Offset/Right");check(root.toString().equals(before),"profile migration mutated caller JSON");check(((NumberSetting)f.ka.findSetting("angle")).doubleValue()==75,"profile migration lost compatible angle");
        old.addProperty("mode","Void");before=root.toString();dev.rice.config.RecoveredConfiguration.apply(f.manager,root,dev.rice.config.RecoveredConfiguration.Scope.PROFILE);check(((ChoiceSetting)f.ka.findSetting("mode")).selectedLabel().equals("Void"),"Void profile did not survive migration");check(root.toString().equals(before),"Void load mutated caller JSON");
        check(dev.rice.module.impl.TeamsModule.prefixColor("\u00a7cEnemy")=='c'&&dev.rice.module.impl.TeamsModule.prefixColor("\u00a7a\u00a7lFriend")=='a',"OG prefix color handling changed");check(dev.rice.module.impl.TeamsModule.trailingColor("\u00a7cEnemy")=='c',"OG trailing color handling changed");
        dev.rice.module.impl.TeamsModule teams=new dev.rice.module.impl.TeamsModule();f.manager.register(teams);dev.rice.config.InitialSettings.applyMissing(f.manager);f.manager.requestEnabled(teams,true);f.manager.applyPendingChanges();RelationshipService.set(f.enemy,1,true);RelationshipService.set(f.enemy,4,true);f.manager.requestEnabled(teams,false);f.manager.applyPendingChanges();check(RelationshipService.flags(f.enemy)==4,"Teams disable cleared unrelated friendship or retained team");RelationshipService.set(f.enemy,1,true);teams.onWorldUnload(f.c);check(RelationshipService.flags(f.enemy)==4,"Teams world cleanup changed unrelated flags");f.close();
        System.out.println("PASS: old Right/Void profile migration, compatible angle, immutable JSON, OG team color helpers and mask-specific lifecycle cleanup");
    }
    static void realEventOrder(Path classes)throws Exception {
        Fixture f=new Fixture(classes,false,true);flag(f.ka,"only_knockback_items",true);Object displace=f.kaClass.getMethod("backend").invoke(f.ka);
        for(int tick=0;tick<2;tick++) {
            f.self.rotationYaw=f.self.prevRotationYaw=73;f.self.rotationPitch=f.self.prevRotationPitch=11;
            f.manager.clientTick(true);RotationService.beginFrame(f.c,0,0);
            int before=((Number)field(displace,"tickCounter")).intValue();
            f.manager.dispatch(new SemanticEvent(30));check(f.target()==f.enemy,"PRE_MOUSE_OVER did not select current target");
            f.manager.dispatch(new SemanticEvent(30));check(((Number)field(displace,"tickCounter")).intValue()==before+1,"duplicate ray recalculated rotation in one tick");
            f.manager.dispatch(new SemanticEvent(8));check(Math.abs(MathHelper.wrapAngleTo180_float(f.self.rotationYaw))<.3,"ray did not use cached silent aim");check(f.self.prevRotationYaw==f.self.rotationYaw,"ray interpolation used camera previous yaw");
            SemanticEvent tail=new SemanticEvent(29);tail.timerRenderPartialTicks=1;f.manager.dispatch(tail);
            check(f.self.rotationYaw==73&&f.self.rotationPitch==11&&f.self.prevRotationYaw==73&&f.self.prevRotationPitch==11,"POST_MOUSE_OVER did not restore camera/previous rotation");
            if(tick==1)check(f.backend.getField("attackingEntity").get(null)==f.enemy&&f.mc.objectMouseOver.entityHit==f.enemy,"next tick mouse-over lacks current attack target");
            f.preInteract();while(f.mc.gameSettings.keyBindAttack.isPressed()) { /* vanilla consumes queued presses here */ }
            f.manager.dispatch(new SemanticEvent(17));check(Math.abs(MathHelper.wrapAngleTo180_float(f.self.rotationYaw))<.3,"walking update did not publish same ray aim");check(((Number)field(displace,"tickCounter")).intValue()==before+1,"movement hook recalculated ray rotation");
            f.walk();f.manager.dispatch(new SemanticEvent(18));f.manager.clientTick(false);
            check(f.backend.getField("attackingEntity").get(null)==f.enemy,"client END did not publish bS attacking entity");
            SemanticEvent finish=new SemanticEvent(13);finish.active=false;f.manager.dispatch(finish);RotationService.finishFrame(f.c,finish);
        }
        f.close();System.out.println("PASS: real event30->8->29->28->17->C03->18->client END order; one calculation per tick, ray/camera restoration, previous angles and shared walking aim");
    }
    public static void main(String[] args)throws Exception {Path classes=Paths.get(args[0]),root=Paths.get(args[1]);auraBehavior(classes);priorityAndFov(classes);pacingParity(classes);rotationParity(classes,root);displaceBehavior(classes);configAndTeams(classes);TeamsAimAssistCheck.run(classes);realEventOrder(classes);}
}

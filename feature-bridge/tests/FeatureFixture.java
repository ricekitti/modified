import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.*;
import net.minecraft.init.*;
import net.minecraft.client.entity.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.network.*;
import net.minecraft.network.Packet;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.util.*;

/** A real Minecraft class graph allocated without starting a game or window. */
public final class FeatureFixture extends OpenB4KillAuraCheck {
    private static final Properties FIELD_NAMES=new Properties();
    static {try(java.io.InputStream input=FeatureFixture.class.getResourceAsStream("/test-fields.properties")){if(input!=null)FIELD_NAMES.load(input);}catch(Exception failure){throw new ExceptionInInitializerError(failure);}}
    public static String mappedName(Class<?> type,String name){return FIELD_NAMES.getProperty(type.getName()+"/"+name,name);}
    public static Minecraft minecraft;
    public static Player self;
    public static net.minecraft.client.entity.EntityOtherPlayerMP enemy;
    public static int releases;
    public static Packet lastSent;
    public static int sentCount;
    public static final java.util.List<Packet> sentPackets=new java.util.ArrayList<>();
    public static boolean failNetwork;
    public static Class<?> packetObserverBridge;
    public static Object lastSentObject(){return lastSent;}
    public static void prepareScaffold(Class<?> bridge,int air,double y) throws Exception {
        minecraft.gameSettings.keyBindJump=new KeyBinding("fixture.jump",57,"test");
        minecraft.gameSettings.keyBindSneak=new KeyBinding("fixture.sneak",42,"test");
        minecraft.gameSettings.keyBindSprint=new KeyBinding("fixture.sprint",29,"test");
        KeyBinding.setKeyBindState(57,true);
        self.setPosition(.5,y,.5);self.onGround=false;self.motionY=.13;self.moveForward=self.moveStrafing=0;
        voidLayout=1;lastSent=null;
        Field modules=bridge.getDeclaredField("modules");modules.setAccessible(true);Object module=((Object[])modules.get(null))[4];
        Field driver=module.getClass().getDeclaredField("driver");driver.setAccessible(true);Object state=driver.get(module);
        Field ticks=state.getClass().getDeclaredField("airTicks");ticks.setAccessible(true);ticks.setInt(state,air);
    }
    public static Object prepareScaffoldInput(){
        self.movementInput.moveForward=.3f;self.movementInput.moveStrafe=0;
        self.movementInput.sneak=true;self.movementInput.jump=false;return self.movementInput;
    }
    public static void checkScaffoldInput(){check(self.movementInput.moveForward==1f&&!self.movementInput.sneak&&!self.movementInput.jump,"Native input wrapper mismatch");}
    public static void resetScaffoldFixture(){
        self.setPosition(0,65,0);self.onGround=true;self.motionX=self.motionY=self.motionZ=0;
        self.moveForward=self.moveStrafing=0;voidLayout=0;KeyBinding.setKeyBindState(57,false);
        self.rotationYaw=self.prevRotationYaw=self.rotationPitch=self.prevRotationPitch=0;
        self.rotationYawHead=self.prevRotationYawHead=self.renderYawOffset=self.prevRenderYawOffset=0;
        self.ticksExisted=0;minecraft.gameSettings.thirdPersonView=0;
    }
    public static void checkNcp(double y,double velocity){
        check(self.posY==y&&Math.abs(self.motionY-velocity)<1e-10,"NCP position or motion mismatch");
        check(lastSent instanceof net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
            && ((net.minecraft.network.play.client.C08PacketPlayerBlockPlacement)lastSent).getPosition().equals(new BlockPos(-1,-1,-1)),"NCP must send source use-item sentinel packet");
    }
    public static void checkSilentIdentity(final Class<?> bridge) throws Exception {
        final Method mark=bridge.getDeclaredMethod("packetSilent",Object.class,boolean.class);mark.setAccessible(true);
        final Packet packet=new net.minecraft.network.play.client.C08PacketPlayerBlockPlacement((ItemStack)null);
        check((Boolean)mark.invoke(null,packet,true),"Silent packet marker unavailable");
        final java.util.concurrent.atomic.AtomicReference<Throwable> failure=new java.util.concurrent.atomic.AtomicReference<>();
        Thread thread=new Thread(new Runnable(){public void run(){try{
            check((Boolean)mark.invoke(null,packet,false),"Silent identity marker lost across threads");
            check(!(Boolean)mark.invoke(null,packet,false),"Silent marker consumed more than once");
        }catch(Throwable thrown){failure.set(thrown);}}});thread.start();thread.join();
        if(failure.get()!=null)throw new AssertionError("Silent cross-thread identity failure",failure.get());
        failNetwork=true;
        try{bridge.getMethod("sendNoEvent",Packet.class).invoke(null,packet);throw new AssertionError("Expected simulated send failure");}
        catch(InvocationTargetException expected){check(expected.getCause() instanceof IllegalStateException,"Unexpected silent send failure");}
        finally{failNetwork=false;}
        check(!(Boolean)mark.invoke(null,packet,false),"Failed silent send retained packet marker");
    }
    public static void prepareDisplace(Class<?> bridge,int inputs,int activations)throws Exception{
        Field list=bridge.getDeclaredField("modules");list.setAccessible(true);Object module=((Object[])list.get(null))[0];
        Field ctx=bridge.getDeclaredField("context");ctx.setAccessible(true);
        module.getClass().getMethod("onWorldUnload",ctx.get(null).getClass()).invoke(module,ctx.get(null));
        for(String name:new String[]{"inputTicks","activationTicks","movementDirection"}){
            Field field=module.getClass().getDeclaredField(name);field.setAccessible(true);field.setInt(module,name.equals("inputTicks")?inputs:name.equals("activationTicks")?activations:1);
        }
        self.rotationYaw=self.prevRotationYaw=0;self.rotationPitch=self.prevRotationPitch=13;
        self.setSprinting(false);minecraft.objectMouseOver=new MovingObjectPosition(enemy);
        while(minecraft.gameSettings.keyBindAttack.isPressed()){}
    }
    public static void checkDisplaceReplay(){check(minecraft.gameSettings.keyBindAttack.isPressed(),"Displace did not queue the delayed attack");check(self.rotationPitch==13,"Displace changed pitch");}
    private static int voidLayout;
    private static int fogMaterial,fogMode,fogWrites;
    private static float fogDensity,fogStart,fogEnd;
    private static final float[] fogColor=new float[4];
    private static boolean shaderActive;
    private static int shaderLoads,shaderStops;
    private static String shaderPath;
    public static final class ShaderRenderer extends net.minecraft.client.renderer.EntityRenderer {
        public ShaderRenderer(){super(null,null);}
        @Override public boolean isShaderActive(){return shaderActive;}
        @Override public void stopUseShader(){shaderActive=false;shaderStops++;}
    }
    public static void recordShader(net.minecraft.util.ResourceLocation resource){shaderPath=resource.getResourcePath();shaderLoads++;shaderActive=true;}
    public static void prepareShaders(Class<?> bridge) throws Exception {
        net.minecraft.client.renderer.OpenGlHelper.shadersSupported=true;
        put(minecraft,"entityRenderer",blank(ShaderRenderer.class));shaderActive=false;shaderLoads=shaderStops=0;
        Class<?> helper=bridge.getClassLoader().loadClass("dev.rice.inject.ReflectionHelper");
        Method find=helper.getMethod("findMethod",Class.class,Object.class,String[].class,Class[].class);
        Method original=(Method)find.invoke(null,net.minecraft.client.renderer.EntityRenderer.class,minecraft.entityRenderer,new String[]{"loadShader","func_175069_a"},new Class<?>[]{net.minecraft.util.ResourceLocation.class});
        check(original.getDeclaringClass()==net.minecraft.client.renderer.EntityRenderer.class,"Shader private loader mapping");
        Field modules=bridge.getDeclaredField("modules");modules.setAccessible(true);Object shader=((Object[])modules.get(null))[3];
        Field loader=shader.getClass().getDeclaredField("loadShader");loader.setAccessible(true);loader.set(shader,FeatureFixture.class.getMethod("recordShader",net.minecraft.util.ResourceLocation.class));
    }
    public static void checkShader(int mode,int expectedLoads) throws Exception {
        String[] files={"notch","fxaa","art","bumpy","blobs2","pencil","color_convolve","deconverge","flip","invert","ntsc","outline","phosphor","scan_pincushion","sobel","bits","desaturate","green","blur","wobble"};
        check(shaderActive&&shaderLoads==expectedLoads&&shaderPath.equals("shaders/post/"+files[mode]+".json"),"Shader selection or redundant reload");
        try(java.io.InputStream resource=Minecraft.class.getResourceAsStream("/assets/minecraft/"+shaderPath)){check(resource!=null,"Selected shader asset is missing");}
    }
    public static void loseShader(){shaderActive=false;}
    public static void checkShaderStopped(){check(!shaderActive&&shaderStops>0,"Shader effect not released");}
    public static void shaderSupport(boolean supported){net.minecraft.client.renderer.OpenGlHelper.shadersSupported=supported;}
    public static void recordFogMode(int name,int value){if(name==2917){fogMode=value;fogWrites++;}}
    public static void recordFogFloat(int name,float value){if(name==2914)fogDensity=value;else if(name==2915)fogStart=value;else if(name==2916)fogEnd=value;fogWrites++;}
    public static void recordFogColor(int name,java.nio.FloatBuffer values){if(name==2918){for(int n=0;n<4;n++)fogColor[n]=values.get(n);fogWrites++;}}
    public static void checkNativeFog(){check(fogMode==2049&&Math.abs(fogDensity-1.5f*.2875f*.5f/192)<.00001&&Math.abs(fogColor[0]-.25f)<.0001,"Native Fog menu/color/event bridge did not reach GL");}
    private static int hudMatrixMode=5888,hudProjection,hudModel,hudServer,hudClient,hudPrimitive;
    public static void resetHudGl(){hudMatrixMode=5888;hudProjection=hudModel=hudServer=hudClient=hudPrimitive=0;}
    public static int lastColorQueryCapacity;
    public static void validateGlFloatQuery(int name,java.nio.FloatBuffer buffer){
        lastColorQueryCapacity=buffer.remaining();
        if(buffer.remaining()<16)throw new IllegalArgumentException("LWJGL glGetFloat requires at least 16 remaining float elements");
    }
    public static int queryHudGl(int name){return name==2976?hudMatrixMode:name==34016?33984:0;}
    public static void recordHudGl(int operation,int value){
        if(operation==0)hudMatrixMode=value;
        else if(operation==1){if(hudMatrixMode==5889)hudProjection++;else hudModel++;}
        else if(operation==2){if(hudMatrixMode==5889)hudProjection--;else hudModel--;}
        else if(operation==3)hudServer++;else if(operation==4)hudServer--;
        else if(operation==5)hudClient++;else if(operation==6)hudClient--;
        else if(operation==7)hudPrimitive++;else if(operation==8)hudPrimitive--;
        if(hudProjection<0||hudModel<0||hudServer<0||hudClient<0||hudPrimitive<0)throw new AssertionError("Unbalanced HUD GL pop");
    }
    public static boolean hudGlBalanced(){return hudMatrixMode==5888&&hudProjection==0&&hudModel==0&&hudServer==0&&hudClient==0&&hudPrimitive==0;}
    public static int safeWalkLayout;public static boolean safeWalkDeepBlock;
    public static java.util.Map<BlockPos,net.minecraft.block.state.IBlockState> pearlBlocks;
    public static double moveX,moveY,moveZ;public static int moveCalls;public static boolean recordMove,failMove;
    public static int jumpCalls;public static boolean failJump;
    public static final class FeatureWorld extends World {
        public FeatureWorld(){super();}
        @Override public java.util.List<AxisAlignedBB> getCollidingBoundingBoxes(Entity entity,AxisAlignedBB box){
            if(pearlBlocks!=null){java.util.List<AxisAlignedBB> result=new ArrayList<>();for(BlockPos p:pearlBlocks.keySet()){AxisAlignedBB b=pearlBlocks.get(p).getBlock().getCollisionBoundingBox(this,p,pearlBlocks.get(p));if(b!=null&&b.intersectsWith(box))result.add(b);}return result;}
            if(safeWalkLayout!=0){
                java.util.List<AxisAlignedBB> collisions=new ArrayList<>();
                AxisAlignedBB[] platforms=safeWalkLayout==1?new AxisAlignedBB[]{new AxisAlignedBB(-1,64,-1,1,65,1)}
                    :new AxisAlignedBB[]{new AxisAlignedBB(-2,64,-2,2,65,1),new AxisAlignedBB(-2,64,-2,1,65,2)};
                for(AxisAlignedBB platform:platforms)if(platform.intersectsWith(box))collisions.add(platform);
                return collisions;
            }
            return java.util.Collections.emptyList();
        }
        @Override public net.minecraft.world.border.WorldBorder getWorldBorder(){return new net.minecraft.world.border.WorldBorder();}
        @Override public MovingObjectPosition rayTraceBlocks(Vec3 a,Vec3 b,boolean liquid,boolean ignore,boolean last){
            if(pearlBlocks==null)return super.rayTraceBlocks(a,b,liquid,ignore,last);
            MovingObjectPosition best=null;double distance=Double.MAX_VALUE;
            for(BlockPos p:pearlBlocks.keySet()){
                AxisAlignedBB box=pearlBlocks.get(p).getBlock().getCollisionBoundingBox(this,p,pearlBlocks.get(p));
                MovingObjectPosition hit=box==null?null:box.calculateIntercept(a,b);
                if(hit!=null&&a.squareDistanceTo(hit.hitVec)<distance){distance=a.squareDistanceTo(hit.hitVec);best=new MovingObjectPosition(hit.hitVec,hit.sideHit,p);}
            }
            return best;
        }
        @Override public net.minecraft.block.state.IBlockState getBlockState(BlockPos pos){
            if(pearlBlocks!=null)return pearlBlocks.containsKey(pos)?pearlBlocks.get(pos):Blocks.air.getDefaultState();
            if(safeWalkDeepBlock&&pos.getY()==63)return Blocks.stone.getDefaultState();
            if(fogMaterial!=0&&pos.getY()>=65)return (fogMaterial==1?Blocks.water:Blocks.lava).getDefaultState();
            if(voidLayout==0)return super.getBlockState(pos);
            boolean ground=pos.getY()==64;
            if(voidLayout==1)ground&=pos.getZ()>=0;
            if(voidLayout==2)ground&=pos.getX()<2;
            if(voidLayout==3)ground&=pos.getX()>-2;
            return (ground?Blocks.stone:Blocks.air).getDefaultState();
        }
    }
    public static final class FixtureChunk extends net.minecraft.world.chunk.Chunk {
        public FixtureChunk(){super((net.minecraft.world.World)null,0,0);}
        @Override public boolean isLoaded(){return true;}
        @Override public boolean isEmpty(){return false;}
        @Override public boolean canSeeSky(BlockPos p){return true;}
        @Override public void getEntitiesWithinAABBForEntity(Entity e,AxisAlignedBB b,java.util.List<Entity> out,com.google.common.base.Predicate<? super Entity> predicate){}
    }
    public static final class Remote extends EntityOtherPlayerMP {
        public Remote(){super(null,null);}
        @Override public void jump(){jumpCalls++;if(failJump)throw new IllegalStateException("simulated jump failure");}
        @Override public void moveEntity(double x,double y,double z){
            if(recordMove){moveX=x;moveY=y;moveZ=z;moveCalls++;if(failMove)throw new IllegalStateException("simulated movement failure");}
            else super.moveEntity(x,y,z);
        }
        @Override public UUID getUniqueID(){return new UUID(0,getEntityId());}
        @Override public boolean canBeCollidedWith(){return true;}
        @Override public boolean isInvisible(){return false;}
        @Override public boolean isSpectator(){return false;}
        @Override public boolean isOnSameTeam(EntityLivingBase entity){return false;}
    }
    public static final class BowController extends PlayerControllerMP {
        public BowController(){super(null,null);}
        @Override public void onStoppedUsingItem(EntityPlayer player){releases++;}
    }
    public static final class Network extends NetHandlerPlayClient {
        public Network(){super(null,null,null,null);}
        @Override public net.minecraft.network.NetworkManager getNetworkManager(){return fixtureConnection;}
        @Override public NetworkPlayerInfo getPlayerInfo(UUID id){return null;}
        @Override public void addToSendQueue(Packet packet){if(failNetwork)throw new IllegalStateException("fixture send failure");lastSent=packet;sentCount++;sentPackets.add(packet);if(packetObserverBridge!=null)try{packetObserverBridge.getMethod("event",int.class,int.class,boolean.class,Object.class,float[].class).invoke(null,7,3,true,packet,new float[13]);}catch(Exception e){throw new IllegalStateException(e);}}
    }
    private static net.minecraft.network.NetworkManager fixtureConnection;
    public static final class FixtureConnection extends net.minecraft.network.NetworkManager {
        public FixtureConnection(){super(net.minecraft.network.EnumPacketDirection.CLIENTBOUND);}
        @Override public boolean isChannelOpen(){return true;}
        @Override public void processReceivedPackets(){}
    }
    public static Minecraft create() throws Exception {
        Class<?> u=Class.forName("sun.misc.Unsafe");Field access=u.getDeclaredField("theUnsafe");access.setAccessible(true);unsafe=access.get(null);allocate=u.getMethod("allocateInstance",Class.class);
        Bootstrap.register();
        fixtureConnection=blank(FixtureConnection.class);
        minecraft=blank(Minecraft.class);World world=blank(FeatureWorld.class);world.floorSolid=true;
        final FixtureChunk fixtureChunk=blank(FixtureChunk.class);
        Object chunks=Proxy.newProxyInstance(FeatureFixture.class.getClassLoader(),new Class[]{net.minecraft.world.chunk.IChunkProvider.class},(proxy,method,args)->method.getReturnType()==net.minecraft.world.chunk.Chunk.class?fixtureChunk:method.getReturnType()==boolean.class?true:method.getReturnType()==int.class?0:null);
        put(world,"chunkProvider",chunks);
        net.minecraft.util.Timer clock=blank(net.minecraft.util.Timer.class);clock.timerSpeed=1;put(minecraft,"timer",clock);
        put(world,"loadedEntityList",new ArrayList<Entity>());put(world,"playerEntities",new ArrayList<EntityPlayer>());
        self=player(world,1,0,0);put(self,"foodStats",new net.minecraft.util.FoodStats());self.movementInput=new MovementInput();self.inventory.mainInventory[0]=new ItemStack(Items.diamond_sword);
        self.getAttributeMap().registerAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.1);
        self.onGround=true;
        put(self,"sendQueue",blank(Network.class));
        Remote other=blank(Remote.class);enemy=other;other.setEntityId(2);other.worldObj=world;other.posZ=2.5;other.posY=65;other.setEntityBoundingBox(new AxisAlignedBB(-.3,65,2.2,.3,66.8,2.8));other.inventory=new InventoryPlayer(other);other.capabilities=new PlayerCapabilities();
        DataWatcher data=new DataWatcher(other);data.addObject(0,(byte)0);data.addObject(6,20F);data.addObject(17,0F);put(other,"dataWatcher",data);put(other,"activePotionsMap",new HashMap<>());
        other.onGround=true;
        other.serverPosX=0;other.serverPosY=65*32;other.serverPosZ=80;
        world.loadedEntityList.add(self);world.loadedEntityList.add(other);world.playerEntities.add(self);world.playerEntities.add(other);
        GameSettings settings=blank(GameSettings.class);settings.mouseSensitivity=.5F;settings.keyBindAttack=new KeyBinding("feature.attack",-100,"test");settings.keyBindUseItem=new KeyBinding("feature.use",-99,"test");
        settings.keyBindForward=new KeyBinding("feature.forward",17,"test");settings.keyBindBack=new KeyBinding("feature.back",31,"test");settings.keyBindLeft=new KeyBinding("feature.left",30,"test");settings.keyBindRight=new KeyBinding("feature.right",32,"test");
        settings.renderDistanceChunks=12;
        minecraft.gameSettings=settings;minecraft.thePlayer=self;minecraft.theWorld=world;minecraft.inGameHasFocus=true;minecraft.objectMouseOver=new MovingObjectPosition(other);minecraft.playerController=blank(BowController.class);put(minecraft.playerController,"mc",minecraft);put(minecraft.playerController,"netClientHandler",self.sendQueue);put(minecraft.playerController,"currentGameType",net.minecraft.world.WorldSettings.GameType.SURVIVAL);put(minecraft,"renderViewEntity",self);
        Field singleton=Minecraft.class.getDeclaredField(mappedName(Minecraft.class,"theMinecraft"));singleton.setAccessible(true);singleton.set(null,minecraft);
        return minecraft;
    }
    private static Object scaffoldSlots(Class<?> bridge)throws Exception {
        Field modules=bridge.getDeclaredField("modules");modules.setAccessible(true);Object module=((Object[])modules.get(null))[4];
        Field driver=module.getClass().getDeclaredField("driver");driver.setAccessible(true);Object state=driver.get(module);
        Field slots=state.getClass().getDeclaredField("slots");slots.setAccessible(true);return slots.get(state);
    }
    public static Object prepareScaffoldSlots(Class<?> bridge)throws Exception {
        self.inventory.currentItem=0;
        self.inventory.mainInventory[3]=new ItemStack(Blocks.stone,64);
        Object slots=scaffoldSlots(bridge);Field context=bridge.getDeclaredField("context");context.setAccessible(true);
        Method select=slots.getClass().getDeclaredMethod("select",context.get(null).getClass());select.setAccessible(true);select.invoke(slots,context.get(null));
        check(self.inventory.currentItem==0&&lastSent instanceof net.minecraft.network.play.client.C09PacketHeldItemChange&&((net.minecraft.network.play.client.C09PacketHeldItemChange)lastSent).getSlotId()==3,"Native slot fixture did not sync selected slot");
        return self.inventory;
    }
    public static Object[] slotPlacementArguments(boolean allowed){
        throw new UnsupportedOperationException("scaffold slot fixture removed with the Rise reconstruction");
    }
    public static void checkSlotAttempt(Class<?> bridge,Object stack,boolean expected)throws Exception {
        Object slots=scaffoldSlots(bridge);Field map=slots.getClass().getDeclaredField("history");map.setAccessible(true);
        Object history=((java.util.Map<?,?>)map.get(slots)).get(stack);
        check((history!=null)==expected,"Native can-place hook recorded the wrong eligibility");
        if(history!=null){Field age=history.getClass().getDeclaredField("age");age.setAccessible(true);Field count=history.getClass().getDeclaredField("previousCount");count.setAccessible(true);check(age.getInt(history)==0&&count.getInt(history)==-1,"Native can-place history values mismatch");}
    }
    public static void releaseScaffoldSlots(Class<?> bridge)throws Exception {
        Object slots=scaffoldSlots(bridge);Field context=bridge.getDeclaredField("context");context.setAccessible(true);
        Method release=slots.getClass().getDeclaredMethod("release",context.get(null).getClass());release.setAccessible(true);release.invoke(slots,context.get(null));
    }
    public static void checkScaffoldHud(Class<?> bridge)throws Exception{
        Field modules=bridge.getDeclaredField("modules");modules.setAccessible(true);Object module=((Object[])modules.get(null))[4];
        Field context=bridge.getDeclaredField("context");context.setAccessible(true);Object c=context.get(null);
        Object selection=module.getClass().getMethod("hudSelection",c.getClass()).invoke(module,c);
        check(selection!=null&&selection.getClass().getField("count").getInt(selection)==64,"Native HUD did not retain the source hotbar block total");
    }
    public static Object prepareMovementProbe(){recordMove=true;failMove=false;moveCalls=0;return enemy;}
    public static void checkMovementProbe(double x,double y,double z,int calls){check(moveCalls==calls&&moveX==x&&moveY==y&&moveZ==z,"Native moveEntity wrapper changed original virtual-call arguments");}
    public static void failMovementProbe(boolean failure){failMove=failure;}
    public static void finishMovementProbe(){recordMove=failMove=false;moveCalls=0;}
    public static void checkJumpProbe(int count,boolean fail){check(jumpCalls==count,"Jump wrapper call count mismatch");failJump=fail;}
    public static void prepareWatchdogNative(Class<?> bridge,int kind)throws Exception{
        prepareScaffold(bridge,0,65.5);self.moveForward=1;self.motionX=0;self.motionZ=.3;
        self.onGround=kind==0;self.ticksExisted=kind==0?13:20;self.motionY=kind==0?.123:.2;
        Field modules=bridge.getDeclaredField("modules");modules.setAccessible(true);Object module=((Object[])modules.get(null))[4];
        Field context=bridge.getDeclaredField("context");context.setAccessible(true);Object c=context.get(null);
        Field driver=module.getClass().getDeclaredField("driver");driver.setAccessible(true);Object d=driver.get(module);
        Method activate=d.getClass().getDeclaredMethod("activateSprint",c.getClass(),module.getClass());activate.setAccessible(true);activate.invoke(d,c,module);
        Field grounded=d.getClass().getDeclaredField("groundTicks");grounded.setAccessible(true);grounded.setInt(d,kind==0?3:0);
    }
    public static void checkWatchdogNativeJump(){check(self.motionY==.123,"Canceled local jump reached original jump");}
    public static Object scaffoldBox(){return self.getEntityBoundingBox();}
    private static void scaffoldField(Object object,String name,Object value)throws Exception{
        Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);field.set(object,value);
    }
    private static Object scaffoldDriver(Class<?> bridge)throws Exception{
        Field modules=bridge.getDeclaredField("modules");modules.setAccessible(true);Object module=((Object[])modules.get(null))[4];
        Field driver=module.getClass().getDeclaredField("driver");driver.setAccessible(true);return driver.get(module);
    }
    public static void prepareScaffoldVisual(Class<?> bridge)throws Exception{
        Object driver=scaffoldDriver(bridge);Field telly=driver.getClass().getDeclaredField("telly");telly.setAccessible(true);Object state=telly.get(driver);
        scaffoldField(state,"visualActive",true);scaffoldField(state,"prevVisualYaw",120f);scaffoldField(state,"visualYaw",123f);
        scaffoldField(state,"prevVisualPitch",40f);scaffoldField(state,"visualPitch",43f);
        self.prevRotationYawHead=2;self.rotationYawHead=3;self.prevRenderYawOffset=4;self.renderYawOffset=5;
        self.prevRotationPitch=11;self.rotationPitch=12;minecraft.gameSettings.thirdPersonView=1;
    }
    public static void checkScaffoldVisual(boolean active){
        check(self.rotationYawHead==(active?123:3)&&self.renderYawOffset==(active?123:5)
            &&self.rotationPitch==(active?43:12)&&self.prevRotationPitch==(active?40:11),"Native scoped visual pose mismatch");
    }
    public static Object prepareScaffoldTiming(){self.ticksExisted=700;return new net.minecraft.network.play.server.S08PacketPlayerPosLook();}
    public static Object prepareScaffoldGuard(int kind){
        return kind==0?new net.minecraft.network.play.client.C02PacketUseEntity(enemy,net.minecraft.network.play.client.C02PacketUseEntity.Action.ATTACK)
            :kind==1?new net.minecraft.network.play.client.C03PacketPlayer():new net.minecraft.network.play.client.C0DPacketCloseWindow(0);
    }
    public static void checkScaffoldGuard(Class<?> bridge,boolean expected)throws Exception{
        Object driver=scaffoldDriver(bridge);Field component=driver.getClass().getDeclaredField("packets");component.setAccessible(true);Object state=component.get(driver);
        Field context=bridge.getDeclaredField("context");context.setAccessible(true);Object c=context.get(null);
        Method blocked=state.getClass().getDeclaredMethod("placementBlocked",c.getClass());blocked.setAccessible(true);
        check(((Boolean)blocked.invoke(state,c))==expected,"Native early packet observer ordering mismatch");
    }
    public static void checkScaffoldTiming(Class<?> bridge)throws Exception{
        Object driver=scaffoldDriver(bridge);Field component=driver.getClass().getDeclaredField("timing");component.setAccessible(true);Object state=component.get(driver);
        Field context=bridge.getDeclaredField("context");context.setAccessible(true);Object c=context.get(null);
        Method age=state.getClass().getDeclaredMethod("correctionAge",c.getClass());age.setAccessible(true);
        check(((Integer)age.invoke(state,c))==0,"Native global packet timing observer missed correction");
    }
    public static Object createObject() throws Exception {return create();}
    public static void checkFog(Class<?> bridge) throws Exception {
        org.lwjgl.opengl.GL11.glFogi(2917,1234);check(fogMode==1234,"Test GL recorder unavailable");
        Method configure=bridge.getMethod("configure",int.class,double[].class),event=bridge.getMethod("event",int.class,int.class,boolean.class,Object.class,float[].class);
        double[] values={1,170.0/255,205.0/255,1,100,1,0,1};
        event.invoke(null,2,5,true,null,new float[13]);
        for(int mode=0;mode<4;mode++){
            values[6]=mode;configure.invoke(null,2,values);fogWrites=0;event.invoke(null,2,30,true,null,new float[13]);
            check(fogWrites>0,"Fog render event did not write GL state");
            check(Math.abs(fogColor[0]-170f/255)<.0001&&Math.abs(fogColor[1]-205f/255)<.0001&&fogColor[2]==1&&fogColor[3]==1,"Fog default color");
            float effective=.2875f,far=192;
            if(mode==0)check(fogMode==2049&&Math.abs(fogDensity-1.5f*effective/far)<.00001,"Smooth fog density");
            if(mode==1)check(fogMode==2048&&Math.abs(fogDensity-3*effective/far)<.00001,"Soft fog density");
            if(mode==2)check(fogMode==9729&&Math.abs(fogEnd-far/effective)<.01&&Math.abs(fogStart-fogEnd*.75f)<.01,"Linear fog band");
            if(mode==3)check(fogMode==9729&&Math.abs((fogEnd-fogStart)-far*1024)<.1,"Constant haze span");
        }
        values[6]=0;values[4]=50;configure.invoke(null,2,values);event.invoke(null,2,30,true,null,new float[13]);check(Math.abs(fogDensity-1.5f*.2875f*.5f/192)<.00001,"Fog opacity must affect effective density");
        values[4]=0;configure.invoke(null,2,values);event.invoke(null,2,30,true,null,new float[13]);check(fogMode==9729&&fogStart==768&&fogEnd==769,"Zero opacity disables visible fog");
        values[4]=100;configure.invoke(null,2,values);fogMaterial=1;event.invoke(null,2,30,true,null,new float[13]);check(Math.abs(fogDensity-.1f*.2875f)<.00001,"Underwater fog");
        fogMaterial=2;event.invoke(null,2,30,true,null,new float[13]);check(Math.abs(fogDensity-2*.2875f)<.00001,"Lava fog");fogMaterial=0;
        event.invoke(null,2,5,false,null,new float[13]);check(fogMode==9729&&fogStart==144&&fogEnd==192,"Fog disable must restore normal render distance");
    }
    public static void nativeFatigue(int amplifier){
        self.removePotionEffect(net.minecraft.potion.Potion.digSlowdown.id);
        if(amplifier>=0)self.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.potion.Potion.digSlowdown.id,200,amplifier));
    }
    public static Object nativeFatiguePacket(){return new net.minecraft.network.play.server.S1DPacketEntityEffect(self.getEntityId(),new net.minecraft.potion.PotionEffect(net.minecraft.potion.Potion.digSlowdown.id,200,1));}
    public static Object voidTarget(){return enemy;}
    public static float scanVoid(Class<?> bridge,int layout,float yaw) throws Exception {
        voidLayout=layout;
        Field context=bridge.getDeclaredField("context");context.setAccessible(true);
        ClassLoader loader=bridge.getClassLoader();Class<?> scanner=loader.loadClass("dev.rice.client.combat.RavenBsVoidScanner");
        Method scan=scanner.getMethod("findYaw",loader.loadClass("dev.rice.client.ClientContext"),Entity.class,float.class,double.class,double.class,boolean.class);
        Float result=(Float)scan.invoke(null,context.get(null),enemy,yaw,90.0,6.0,false);
        return result==null?Float.NaN:result;
    }
    public static void prepareBow() throws Exception {
        V115Input.buttons[1]=true;
        self.inventory.mainInventory[0]=new ItemStack(Items.bow);put(self,"itemInUse",self.inventory.mainInventory[0]);put(self,"itemInUseCount",71980);
        enemy.posZ=12;enemy.setEntityBoundingBox(new AxisAlignedBB(-.3,65,11.7,.3,66.8,12.3));
        enemy.serverPosZ=12*32;
    }
    public static String diagnose(Class<?> bridge) throws Exception {
        Field modules=bridge.getDeclaredField("modules");modules.setAccessible(true);Object bow=((Object[])modules.get(null))[1];StringBuilder result=new StringBuilder();
        for(String name:new String[]{"hasRotation","entityPlayer","aimSolution","smoothingInitialised"}){Field field=bow.getClass().getDeclaredField(name);field.setAccessible(true);Object value=field.get(bow);result.append(name).append('=').append(value instanceof Boolean?value:value==null?"null":value.getClass().getName()).append(' ');}
        for(String name:new String[]{"isUsingBow","canOperate","isBowFullyDrawn"}){Method method=bow.getClass().getDeclaredMethod(name);method.setAccessible(true);result.append(name).append('=').append(method.invoke(bow)).append(' ');}
        return result.toString();
    }
}

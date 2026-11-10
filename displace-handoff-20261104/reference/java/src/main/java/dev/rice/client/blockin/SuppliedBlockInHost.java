package dev.rice.client.blockin;
import dev.rice.client.*;
import dev.rice.client.rotation.RotationService;
import dev.rice.inject.ReflectionHelper;
import dev.rice.module.ModuleManager;
import dev.rice.module.impl.BlockInModule;
import dev.rice.setting.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.*;
import java.lang.reflect.*;
import java.lang.annotation.*;

/** Shared-service adapters and player-mined opening history for the supplied module. */
public final class SuppliedBlockInHost {
 public static SuppliedBlockInHost instance;
 public final SuppliedBlockIn core;
 private final BlockInModule owner;private final ModuleManager modules;private final ClientContext c;
 private final RotationManager rotation=new RotationManager(); final MiningOpenings miningOpenings=new MiningOpenings();
 private EntityPlayerSP sessionPlayer;private net.minecraft.world.World sessionWorld;private boolean running,aimed;private float cameraYaw,cameraPitch;
 private net.minecraft.entity.Entity walkingPlayer;private int walkingDepth;private float walkingYaw,walkingPitch;
 private int placed;public String progressText;public float progress;
 private static final Field FEET=ReflectionHelper.findField(SuppliedBlockIn.class,"feet");
 private static final Field LAST_YAW=ReflectionHelper.findField(EntityPlayerSP.class,"lastReportedYaw","field_175164_bL");
 private static final Field LAST_PITCH=ReflectionHelper.findField(EntityPlayerSP.class,"lastReportedPitch","field_175165_bM");
 private static final Method END=ReflectionHelper.findMethod(SuppliedBlockIn.class,null,new String[]{"end"},EntityPlayerSP.class);
 private static final Field WAS_ON=ReflectionHelper.findField(SuppliedBlockIn.class,"wasOn");
 private static final Method SYNC=ReflectionHelper.findMethod(PlayerControllerMP.class,null,new String[]{"syncCurrentPlayItem","func_78750_j"});
 public SuppliedBlockInHost(BlockInModule owner,ModuleManager modules,NumberSetting speed,BooleanSetting ground,BooleanSetting progress){
  this.owner=owner;this.modules=modules;this.c=modules.context();instance=this;
  core=new SuppliedBlockIn(new Configuration(speed,ground,progress));
 }
 private boolean ready(){return c.inputReady()&&c.minecraft().currentScreen==null;}
 public boolean active(){try{return running&&core.enabledProp().getBoolean()&&FEET.get(core)!=null;}catch(IllegalAccessException e){throw new IllegalStateException(e);}}
 public void enable(){if(ready())start();} public void worldUnload(){miningOpenings.clear();stop();} public static void observeDig(net.minecraft.network.Packet<?> packet){SuppliedBlockInHost h=instance;if(h!=null&&packet instanceof net.minecraft.network.play.client.C07PacketPlayerDigging)h.miningOpenings.observe((net.minecraft.network.play.client.C07PacketPlayerDigging)packet,h.c.player(),h.c.world());}
 private void start(){sessionPlayer=c.player();sessionWorld=c.world();running=true;core.enabledProp().set(true);try{rotation.sentYaw=LAST_YAW.getFloat(sessionPlayer);rotation.sentPitch=LAST_PITCH.getFloat(sessionPlayer);}catch(IllegalAccessException e){throw new IllegalStateException(e);}}
 public void stop(){
  if(walkingPlayer!=null){walkingDepth=1;endWalking(walkingPlayer);}
  core.enabledProp().set(false);
  try{END.invoke(core,sessionPlayer);WAS_ON.setBoolean(core,false);}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
  if(sessionPlayer!=null&&sessionPlayer==c.player()&&sessionWorld==c.world()&&c.ready())GameAccess.syncHeldItem(c.minecraft().playerController);
  SnowifyWireRotation.detach();
  clearMovement();rotation.placing=false;
  running=false;aimed=false;owner.active=false;sessionPlayer=null;sessionWorld=null;progressText=null;progress=0;
 }
 public void reserve(){if(owner.isEnabled()&&ready())RotationService.claim(1000);}
 private void pre(EntityPlayerSP player){
  if(player!=c.player())return;miningOpenings.update(player,c.world());if(!owner.isEnabled())return;
  if(!ready()){if(running)stop();return;}
  if(running&&(sessionPlayer!=player||sessionWorld!=c.world()))stop();if(!running)start();
  aimed=false;rotation.placing=false;cameraYaw=RotationService.storedRotationValid()?RotationService.cameraYaw():player.rotationYaw;cameraPitch=RotationService.storedRotationValid()?RotationService.cameraPitch():player.rotationPitch;
  player.rotationYaw=cameraYaw;player.rotationPitch=cameraPitch;
  core.onPlayerTick(new TickEvent.PlayerTickEvent(player));
  if(!core.enabledProp().getBoolean()){stop();modules.requestEnabled(owner,false);}
  owner.active=active();
 }
 /** SnowifyHooks spoofYaw/spoofPitch: packet construction sees aim; the real camera remains untouched. */
 public static float spoofYaw(net.minecraft.entity.Entity player){SuppliedBlockInHost h=instance;return h!=null&&h.running&&h.aimed&&h.owner.isEnabled()&&player==h.c.player()&&h.ready()?h.rotation.yaw:player.rotationYaw;}
 public static float spoofPitch(net.minecraft.entity.Entity player){SuppliedBlockInHost h=instance;return h!=null&&h.running&&h.aimed&&h.owner.isEnabled()&&player==h.c.player()&&h.ready()?h.rotation.pitch:player.rotationPitch;}
 /** Fallback for clients whose walking method delegates or hides its direct yaw/pitch reads. */
 public static void beginWalking(net.minecraft.entity.Entity player){
  SuppliedBlockInHost h=instance;if(h==null)return;
  if(h.walkingPlayer==player){h.walkingDepth++;return;}
  if(!h.running||!h.aimed||!h.owner.isEnabled()||player!=h.c.player()||!h.ready())return;
  h.walkingPlayer=player;h.walkingDepth=1;h.walkingYaw=player.rotationYaw;h.walkingPitch=player.rotationPitch;
  player.rotationYaw=h.rotation.yaw;player.rotationPitch=h.rotation.pitch;
 }
 public static void endWalking(net.minecraft.entity.Entity player){
  SuppliedBlockInHost h=instance;if(h==null||h.walkingPlayer!=player)return;
  if(--h.walkingDepth>0)return;
  player.rotationYaw=h.walkingYaw;player.rotationPitch=h.walkingPitch;h.walkingPlayer=null;h.walkingDepth=0;
 }
 public static void prePlayer(EntityPlayerSP player){if(instance!=null)instance.pre(player);}
 public static void adjustInput(MovementInput input){
  SuppliedBlockInHost h=instance;if(h==null||!h.running||!h.owner.isEnabled()||!h.ready())return;
  if(h.aimed){float[] fixed=RotationManager.correctedInput(h.cameraYaw,h.rotation.yaw,input.moveForward,input.moveStrafe,input.sneak);if(fixed!=null){input.moveForward=fixed[0];input.moveStrafe=fixed[1];}}
  SuppliedBlockIn.adjustInput(input);
 }
 // SnowifyHooks.outgoingPacket: rewrite existing looks; force the normal movement packet only while placing.
 public void packet(net.minecraft.network.Packet<?> packet){
  if(!(packet instanceof C03PacketPlayer))return;
  C03PacketPlayer look=(C03PacketPlayer)packet;
  boolean active=running&&aimed&&owner.isEnabled()&&ready();
  if(active&&rotation.placing)SnowifyWireRotation.forceNextMovementLook(rotation.yaw,rotation.pitch,walkingPlayer==c.player()?walkingYaw:c.player().rotationYaw,walkingPlayer==c.player()?walkingPitch:c.player().rotationPitch);
  if(look.getRotating()){
   if(active)PacketRotation.apply(look,rotation.yaw,rotation.pitch);
   rotation.sentYaw=look.getYaw();rotation.sentPitch=look.getPitch();
  }else if(active&&rotation.placing){rotation.sentYaw=rotation.yaw;rotation.sentPitch=rotation.pitch;}
 }
 private void clearMovement(){if(aimed&&c.player()!=null&&myau.RotationState.isActive()&&myau.RotationState.movementYaw()==rotation.yaw)myau.RotationState.update(false,c.player().rotationYaw,c.player().rotationPitch,c.player().rotationYaw,0);}
 public void endTick(){clearMovement();aimed=false;rotation.placing=false;}

 public void render(){if(running&&ready())core.onRenderOverlay(new RenderGameOverlayEvent.Post(new ScaledResolution(c.minecraft())));}
 public static void renderHud(){if(instance!=null)instance.render();}
 public static final class Property {
  private boolean local;private final Setting<?> setting;
  Property(boolean value){local=value;setting=null;}Property(Setting<?> s){setting=s;}
  public boolean getBoolean(){return setting==null?local:((BooleanSetting)setting).isEnabled();}
  public double getDouble(double fallback){return setting instanceof NumberSetting?((NumberSetting)setting).doubleValue():fallback;}
  public void set(boolean value){if(setting==null)local=value;else ((BooleanSetting)setting).set(value);}
 }
 public static final class Configuration {
  final NumberSetting speed;final BooleanSetting ground,progress;
  Configuration(NumberSetting s,BooleanSetting g,BooleanSetting p){speed=s;ground=g;progress=p;}
  public Property get(String cat,String key,boolean value,String description){if(key.equals("Enabled"))return new Property(value);return new Property(key.equals("Require ground")?ground:progress);}
  public Property get(String cat,String key,double value,String description,double min,double max){return new Property(speed);}
 }
 @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) public @interface SubscribeEvent{}
 public static final class TickEvent {public enum Phase{START,END}public static final class PlayerTickEvent{public final Phase phase=Phase.START;public final EntityPlayerSP player;PlayerTickEvent(EntityPlayerSP player){this.player=player;}}}
 public static final class RenderGameOverlayEvent {public enum ElementType{ALL}public static final class Post{public final ElementType type=ElementType.ALL;public final ScaledResolution resolution;Post(ScaledResolution resolution){this.resolution=resolution;}}}
 public static final class Claims {
  public enum Resource{LOOK,SLOT,ATTACK,CLICKS}public interface Holder{boolean holds(Resource resource);}
  private static Object registeredOwner;private static Holder registeredHolder;
  public static void register(Object owner,int priority,Holder holder){registeredOwner=owner;registeredHolder=holder;}
  public static boolean held(Resource resource){return registeredHolder!=null&&registeredHolder.holds(resource);}
  public static boolean blocked(Object asker,Resource resource){return instance==null||!instance.ready()||!RotationService.claim(1000);}
 }
 public static final class PacketRotation {
  private static final Field YAW=ReflectionHelper.findField(C03PacketPlayer.class,"yaw","field_149476_e");
  private static final Field PITCH=ReflectionHelper.findField(C03PacketPlayer.class,"pitch","field_149473_f");
  public static boolean apply(C03PacketPlayer packet,float yaw,float pitch){try{YAW.setFloat(packet,yaw);PITCH.setFloat(packet,pitch);return true;}catch(IllegalAccessException e){throw new IllegalStateException("Block In packet rotation failed",e);}}
 }
 public static final class GameAccess { public static boolean keepMiningOpening(BlockPos pos){return instance!=null&&instance.miningOpenings.protects(pos);}
  private static final Field CHANNEL=ReflectionHelper.findField(net.minecraft.network.NetworkManager.class,"channel","field_150746_k");
  public static io.netty.channel.Channel channel(net.minecraft.network.NetworkManager manager){try{return (io.netty.channel.Channel)CHANNEL.get(manager);}catch(IllegalAccessException e){throw new IllegalStateException(e);}}

  public static void syncHeldItem(PlayerControllerMP controller){if(controller==null)return;try{SYNC.invoke(controller);}catch(ReflectiveOperationException e){throw new IllegalStateException("Block In held-slot synchronization failed",e);}}
 }
 public static final class WireRotation {public static void ensurePlaced(){SnowifyWireRotation.ensurePlaced();}}
 public static final class PlaceWatch {public static void placed(String source,BlockPos pos){if(instance!=null)instance.placed++;}}
 public static final class Rice {
  public static final Rice instance=new Rice();public Rice getBinds(){return this;}public int blockIn(){return SuppliedBlockInHost.instance==null?0:SuppliedBlockInHost.instance.owner.getBinding();}
  public boolean heldDown(int key){if(key==0)return false;return key<0?org.lwjgl.input.Mouse.isButtonDown(-key-1):org.lwjgl.input.Keyboard.isKeyDown(key);}
 }
 public static final class ProgressBar {
  public static void draw(ScaledResolution sr,String text,float value){SuppliedBlockInHost h=instance;if(h==null)return;h.progressText=text;h.progress=value;paint(sr,text,value);
  }
  /** Same progress HUD as gameplay, without changing recorded progress or starting Block In. */
  public static void preview(ScaledResolution sr,String text,float value){paint(sr,text,value);}
  private static void paint(ScaledResolution sr,String text,float value){Minecraft mc=Minecraft.getMinecraft();if(mc.fontRendererObj==null)return;
   int x=sr.getScaledWidth()/2+(int)Math.round(dev.rice.client.render.HudLayout.offset("blockin",true)*sr.getScaledWidth()),y=sr.getScaledHeight()/2+16+(int)Math.round(dev.rice.client.render.HudLayout.offset("blockin",false)*sr.getScaledHeight());net.minecraft.client.gui.Gui.drawRect(x-50,y,x+50,y+3,0xAA202020);net.minecraft.client.gui.Gui.drawRect(x-50,y,x-50+(int)(100*value),y+3,0xFFB76DDB);mc.fontRendererObj.drawString(text,x-mc.fontRendererObj.getStringWidth(text)/2,y+5,0xFFFFFFFF,true);
  }
 }
 public static final class RotationManager {
  private float yaw,pitch;private boolean placing;private volatile float sentYaw,sentPitch;
  public static RotationManager get(){return instance.rotation;}
  public float getSentYaw(){return sentYaw;}public float getSentPitch(){return sentPitch;}public float getYaw(){return yaw;}public float getPitch(){return pitch;}
  public boolean shouldFixMovement(){return instance.aimed;}
  public boolean aim(Object owner,int priority,float yaw,float pitch,boolean fix){
   SuppliedBlockInHost h=instance;if(!RotationService.claim(1000))return false;this.yaw=yaw;this.pitch=pitch;
   h.aimed=true;
   myau.RotationState.update(true,yaw,pitch,yaw,3);return true;
  }
  /** Snowify only marks the placement pose; it does not emit an additional movement packet. */
  public void notePlacing(){placing=true;SnowifyWireRotation.notePlacementLook(yaw,pitch);}
    public static float[] correctedInput(float realYaw, float spoofedYaw, float forward, float strafe, boolean sneaking) {
        if (forward == 0.0f && strafe == 0.0f) {
            return null;
        }
        float sneakMultiplier = sneaking ? 0.3f : 1.0f;
        double intended = MathHelper.wrapAngleTo180_double((double)Math.toDegrees(RotationManager.direction(realYaw, forward, strafe)));
        float bestForward = 0.0f;
        float bestStrafe = 0.0f;
        double bestDifference = Double.MAX_VALUE;
        for (float f = -1.0f; f <= 1.0f; f += 1.0f) {
            for (float s = -1.0f; s <= 1.0f; s += 1.0f) {
                float candidateStrafe;
                float candidateForward;
                double candidate;
                double difference;
                if (f == 0.0f && s == 0.0f || !((difference = Math.abs(MathHelper.wrapAngleTo180_double((double)(intended - (candidate = MathHelper.wrapAngleTo180_double((double)Math.toDegrees(RotationManager.direction(spoofedYaw, candidateForward = f * sneakMultiplier, candidateStrafe = s * sneakMultiplier)))))))) < bestDifference)) continue;
                bestDifference = difference;
                bestForward = candidateForward;
                bestStrafe = candidateStrafe;
            }
        }
        return new float[]{bestForward, bestStrafe};
    }

    private static double direction(float yaw, double forward, double strafe) {
        if (forward < 0.0) {
            yaw += 180.0f;
        }
        float sign = 1.0f;
        if (forward < 0.0) {
            sign = -0.5f;
        } else if (forward > 0.0) {
            sign = 0.5f;
        }
        if (strafe > 0.0) {
            yaw -= 90.0f * sign;
        }
        if (strafe < 0.0) {
            yaw += 90.0f * sign;
        }
        return Math.toRadians(yaw);
    }

 }
}

package dev.rice.client.openjade;

import dev.rice.client.ClientContext;
import dev.rice.client.MinecraftFields;
import dev.rice.client.SemanticEvent;
import dev.rice.client.packet.NetworkSendAdapter;
import dev.rice.client.rotation.RotationService;
import dev.rice.inject.ReflectionHelper;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockLadder;
import net.minecraft.network.Packet;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/** Host types used by the source-faithful OpenJade module bodies. */
final class OpenJadeCompat { private OpenJadeCompat() {} }

enum Category { player, minigames }

@Retention(RetentionPolicy.SOURCE)
@interface Subscribe { EventPriority priority() default EventPriority.NORMAL; }
enum EventPriority { HIGHEST, HIGH, NORMAL, LOW, LOWEST }
enum EventPhase { START, END }

abstract class OpenJadeModule {
   protected static final Minecraft mc = Minecraft.getMinecraft();
   protected boolean initialized;
   private final String name;
   private boolean enabled;
   private boolean disableRequested;
   private final List<JadeSetting> settings = new ArrayList<>();

   OpenJadeModule(String name, Category category) { this.name = name; }
   protected final <T extends JadeSetting> T registerSetting(T value) { settings.add(value); return value; }
   public final List<JadeSetting> settings() { return Collections.unmodifiableList(settings); }
   public final String getName() { return name; }
   public final boolean isEnabled() { return enabled; }
   public final void hostEnable() { if (enabled) return; enabled = true; disableRequested = false; onEnable(); }
   public final void hostDisable() { if (enabled) onDisable(); enabled = false; disableRequested = false; }
   public final boolean consumeDisableRequest() { boolean value = disableRequested; disableRequested = false; return value; }
   protected final void disable() { disableRequested = true; }
   public void guiUpdate() {}
   public void onEnable() {}
   public void onDisable() {}
   public void onUpdate() {}
   public String getInfo() { return null; }
   public List<Module$2> getSettingAliases() { return Collections.emptyList(); }
   protected static Module$2 buildSettingAlias(String a, String b, String[] c, String[] d) { return new Module$2(); }
   protected static List<Module$2> BIqm(Module$2... values) { return java.util.Arrays.asList(values); }
   public final void setBoolean(String name, boolean value) { JadeSetting s=find(name); if(s instanceof BooleanSetting)((BooleanSetting)s).value=value; }
   public final void setNumber(String name, double value) { JadeSetting s=find(name); if(s instanceof SliderSetting)((SliderSetting)s).input=value; }
   public final boolean settingVisible(String name) { JadeSetting s=find(name); return s == null || s.visible; }
   private JadeSetting find(String name) { for(JadeSetting s:settings)if(s.name.equalsIgnoreCase(name))return s;return null; }
}

final class Module$2 {}
interface ProgressBarSource { boolean isProgressActive(); float getProgressFraction(); String getProgressLabel(); }

abstract class JadeSetting {
   final String name;
   boolean visible = true;
   JadeSetting(String name) { this.name=name; }
   public void setVisible(boolean visible, OpenJadeModule owner) { this.visible=visible; }
}

final class BooleanSetting extends JadeSetting {
   boolean value;
   BooleanSetting(String name, boolean value) { super(name); this.value=value; }
   boolean isToggled() { return value; }
}

class SliderSetting extends JadeSetting {
   double input;
   final double min, max, step;
   final String[] options;
   private String suffix = "";
   SliderSetting(String name, int selected, String[] options) { this(name, selected, options, null); }
   SliderSetting(String name, int selected, String[] options, String[] aliases) { super(name); this.input=selected;this.min=0;this.max=options.length-1;this.step=1;this.options=options.clone(); }
   SliderSetting(String name, String suffix, double value, double min, double max, double step) { super(name);this.suffix=suffix;this.input=value;this.min=min;this.max=max;this.step=step;this.options=null; }
   SliderSetting(String name, double value, double min, double max, double step, String[] aliases) { this(name,"",value,min,max,step); }
   double getInput() { return input; }
   void setSuffix(String suffix) { this.suffix=suffix; }
   String[] getOptions() { return options == null ? new String[0] : options.clone(); }
}

final class FontSetting extends SliderSetting {
   private static final String[] FONTS={"Modern","Bold","Smooth","Pixel","Tahoma","Minecraft"};
   FontSetting(String name, String initial) { super(name,index(initial),FONTS); }
   private static int index(String value){for(int i=0;i<FONTS.length;i++)if(FONTS[i].equalsIgnoreCase(value))return i;return 0;}
   String getResolvedFontName(){return FONTS[MathHelper.clamp_int((int)Math.round(input),0,FONTS.length-1)];}
}

final class RotationEvent {
   Float MGzP2;
   Float pitch;
   boolean suppressRotations;
   private int accepted;
   RotationEvent(float yaw,float pitch){this.MGzP2=yaw;this.pitch=pitch;}
   boolean setRotation(float yaw,float pitch,int sourcePriority){int mapped=OpenJadeRuntime.mapRotationPriority(sourcePriority);if(!RotationService.claim(mapped))return false;this.MGzP2=yaw;this.pitch=pitch;this.accepted=sourcePriority;RotationUtils.lastSentRotation[0]=yaw;RotationUtils.lastSentRotation[1]=pitch;return true;}
   int Jxhy(){return accepted;}
}
final class PreUpdateEvent {}
final class TickEndEvent { final EventPhase eventPhase; TickEndEvent(EventPhase phase){eventPhase=phase;} }
final class RenderTickEvent { final EventPhase eventPhase; RenderTickEvent(EventPhase phase){eventPhase=phase;} }
final class RenderWorldLastEvent {}
final class LeftClickEvent { boolean cancelled; void setCancelled(boolean value){cancelled=value;} }
final class RightClickEvent { boolean cancelled; void setCancelled(boolean value){cancelled=value;} }
final class ChangeCurrentItemEvent { boolean cancelled; void setCancelled(boolean value){cancelled=value;} }
final class SetCurrentItemEvent { final int slot; boolean cancelled; SetCurrentItemEvent(int slot){this.slot=slot;} void setCancelled(boolean value){cancelled=value;} void setCanceled(boolean value){cancelled=value;} }
final class MouseEvent { final int button; boolean cancelled; MouseEvent(int button){this.button=button;} void setCanceled(boolean value){cancelled=value;} }
final class MoveInputEvent { boolean jump,sneak; void setJumping(boolean v){jump=v;} void setSneaking(boolean v){sneak=v;} }
final class MoveStateUpdateEvent {}
final class MoveFlyingEvent { float yaw; MoveFlyingEvent(float yaw){this.yaw=yaw;} }
final class UpdateWalkingPlayerEvent {
   private float yaw,pitch; private boolean onGround;
   UpdateWalkingPlayerEvent(float yaw,float pitch,boolean onGround){this.yaw=yaw;this.pitch=pitch;this.onGround=onGround;}
   float getYaw(){return yaw;} float getPitch(){return pitch;}
   void ebfpOkg(float y,float p){yaw=y;pitch=p;}
   void LWId(boolean value){onGround=value;}
}

final class ClientUtils {
   private static final Random RANDOM=new Random();
   static boolean isInWorld(){return mc().thePlayer!=null&&mc().theWorld!=null;}
   static boolean vAxywR(){return isInWorld()&&mc().inGameHasFocus&&mc().currentScreen==null;}
   static double randomDouble(double min,double max){return min+RANDOM.nextDouble()*(max-min);}
   static boolean xusXfhC(KeyBinding key){int code=key.getKeyCode();return code<0?Mouse.isButtonDown(code+100):Keyboard.isKeyDown(code);}
   static boolean isJumpKeyDown(){return xusXfhC(mc().gameSettings.keyBindJump);}
   static boolean isPassableBlock(ItemBlock item){Block b=item.getBlock();return b!=null&&b!=net.minecraft.init.Blocks.air&&b!=net.minecraft.init.Blocks.ladder&&!(b instanceof BlockLiquid)&&b.getMaterial()!=Material.air;}
   static int YVVZ(int rgb,int alpha){return MathHelper.clamp_int(alpha,0,255)<<24|rgb&0xFFFFFF;}
   static void eqyXnoq(){MinecraftFields.timer(mc()).timerSpeed=1.0F;}
   static int getBedWarsBoardType(){return OpenJadeRuntime.bedWarsBoardType();}
   private static Minecraft mc(){return Minecraft.getMinecraft();}
}

final class BlockUtils {
   static Block iepjdt(BlockPos pos){return Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();}
   static boolean isReplaceableAt(BlockPos pos){return Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock().isReplaceable(Minecraft.getMinecraft().theWorld,pos);}
   static boolean isInteractiveBlock(Block block){return block instanceof BlockContainer||block==net.minecraft.init.Blocks.crafting_table||block==net.minecraft.init.Blocks.anvil||block==net.minecraft.init.Blocks.enchanting_table||block==net.minecraft.init.Blocks.bed;}
   static AxisAlignedBB getSelectedBounds(BlockPos pos){Block b=iepjdt(pos);return b.getSelectedBoundingBox(Minecraft.getMinecraft().theWorld,pos);}
   static boolean canPlaceItemOnSide(ItemStack stack,BlockPos pos,EnumFacing face){return stack!=null&&stack.getItem() instanceof ItemBlock&&((ItemBlock)stack.getItem()).canPlaceBlockOnSide(Minecraft.getMinecraft().theWorld,pos,face,Minecraft.getMinecraft().thePlayer,stack);}
}

final class RotationUtils {
   static final float[] lastSentRotation={0F,0F};
   static float[] NSsr(float yaw,float pitch,float oldYaw,float oldPitch){float sensitivity=Minecraft.getMinecraft().gameSettings.mouseSensitivity*0.6F+0.2F;float gcd=sensitivity*sensitivity*sensitivity*8F*0.15F;float y=oldYaw+Math.round(MathHelper.wrapAngleTo180_float(yaw-oldYaw)/gcd)*gcd;float p=oldPitch+Math.round((pitch-oldPitch)/gcd)*gcd;return new float[]{y,MathHelper.clamp_float(p,-90F,90F)};}
   static float[] smoothAnglesRandomized(float oldYaw,float oldPitch,float yaw,float pitch,int speed,float random){float cap=Math.max(1F,speed)+(random<=0?0:(float)((Math.random()*2-1)*random));float dy=MathHelper.clamp_float(MathHelper.wrapAngleTo180_float(yaw-oldYaw),-cap,cap);float dp=MathHelper.clamp_float(pitch-oldPitch,-cap,cap);return new float[]{oldYaw+dy,MathHelper.clamp_float(oldPitch+dp,-90F,90F)};}
   static Vec3 getLookVector(float pitch,float yaw){float cy=MathHelper.cos(-yaw*0.017453292F-(float)Math.PI);float sy=MathHelper.sin(-yaw*0.017453292F-(float)Math.PI);float cp=-MathHelper.cos(-pitch*0.017453292F);float sp=MathHelper.sin(-pitch*0.017453292F);return new Vec3(sy*cp,sp,cy*cp);}
   static MovingObjectPosition traceBlockHit(double reach,float yaw,float pitch){return trace(reach,yaw,pitch,false,false,true);}
   static MovingObjectPosition traceBlockThroughUncollidable(double reach,float yaw,float pitch){return trace(reach,yaw,pitch,false,false,true);}
   private static MovingObjectPosition trace(double reach,float yaw,float pitch,boolean stop,boolean liquid,boolean last){Minecraft m=Minecraft.getMinecraft();Vec3 eye=m.thePlayer.getPositionEyes(1F);Vec3 look=getLookVector(pitch,yaw);return m.theWorld.rayTraceBlocks(eye,eye.addVector(look.xCoord*reach,look.yCoord*reach,look.zCoord*reach),stop,liquid,last);}
}

final class RotationHandler {
   private static final RotationHandler INSTANCE=new RotationHandler();
   static RotationHandler getInstance(){return INSTANCE;}
   Float getTargetYaw(){return RotationUtils.lastSentRotation[0];}
   Float cvZx(){return RotationUtils.lastSentRotation[1];}
}

final class PacketUtils {
   static void sendSilently(Packet<?> packet){Minecraft m=Minecraft.getMinecraft();if(m.getNetHandler()!=null)NetworkSendAdapter.sendDirect(m.getNetHandler().getNetworkManager(),packet);}
}

final class RenderUtils$1 {}
final class RenderUtils {
   static void drawBlockBoundsBox(BlockPos pos,int color,boolean fill,boolean outline){Minecraft m=Minecraft.getMinecraft();if(m.theWorld==null)return;AxisAlignedBB b=BlockUtils.getSelectedBounds(pos).offset(-m.getRenderManager().viewerPosX,-m.getRenderManager().viewerPosY,-m.getRenderManager().viewerPosZ);float a=((color>>>24)&255)/255F,r=((color>>>16)&255)/255F,g=((color>>>8)&255)/255F,bl=(color&255)/255F;GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glEnable(GL11.GL_BLEND);GL11.glColor4f(r,g,bl,a);RenderGlobal.drawSelectionBoundingBox(b);GL11.glPopAttrib();}
   static RenderUtils$1 uyB6(){return new RenderUtils$1();}
   static void restoreLightmapState(RenderUtils$1 state){}
   static void resetGlPipelineState(){GL11.glColor4f(1,1,1,1);}
}

final class OpenJadeAccess {
   private static final Field LAST_X=ReflectionHelper.findField(EntityPlayerSP.class,"lastReportedPosX","field_175172_bI");
   private static final Field LAST_Y=ReflectionHelper.findField(EntityPlayerSP.class,"lastReportedPosY","field_175166_bJ");
   private static final Field LAST_Z=ReflectionHelper.findField(EntityPlayerSP.class,"lastReportedPosZ","field_175167_bK");
   private static final Field LAST_YAW=ReflectionHelper.findField(EntityPlayerSP.class,"lastReportedYaw","field_175164_bL");
   private static final Field LAST_PITCH=ReflectionHelper.findField(EntityPlayerSP.class,"lastReportedPitch","field_175165_bM");
   private static final Field HITTING=ReflectionHelper.findField(PlayerControllerMP.class,"isHittingBlock","field_78778_j");
   private static final Field DAMAGE=ReflectionHelper.findField(PlayerControllerMP.class,"curBlockDamageMP","field_78770_f");
   private static final Field DELAY=ReflectionHelper.findField(PlayerControllerMP.class,"blockHitDelay","field_78781_i");
   private static final Method SYNC=ReflectionHelper.findMethod(PlayerControllerMP.class,(PlayerControllerMP)null,new String[]{"syncCurrentPlayItem","func_78750_j"});
   static void syncCurrentPlayItem(PlayerControllerMP c){try{SYNC.invoke(c);}catch(Exception e){throw new IllegalStateException(e);}}
   static double lastX(EntityPlayerSP p){return number(LAST_X,p).doubleValue();} static double lastY(EntityPlayerSP p){return number(LAST_Y,p).doubleValue();} static double lastZ(EntityPlayerSP p){return number(LAST_Z,p).doubleValue();}
   static float lastYaw(EntityPlayerSP p){return number(LAST_YAW,p).floatValue();} static float lastPitch(EntityPlayerSP p){return number(LAST_PITCH,p).floatValue();}
   static boolean hitting(PlayerControllerMP c){try{return HITTING.getBoolean(c);}catch(Exception e){return false;}}
   static float damage(PlayerControllerMP c){return number(DAMAGE,c).floatValue();}
   static void stopMining(PlayerControllerMP c){try{HITTING.setBoolean(c,false);DAMAGE.setFloat(c,0F);DELAY.setInt(c,0);}catch(Exception e){throw new IllegalStateException(e);}}
   private static Number number(Field f,Object o){try{return (Number)f.get(o);}catch(Exception e){throw new IllegalStateException(e);}}
}

final class BedwarsUtils$2 { static BlockPos getSpawnBlockPos(){return OpenJadeRuntime.spawnBlockPos();} }


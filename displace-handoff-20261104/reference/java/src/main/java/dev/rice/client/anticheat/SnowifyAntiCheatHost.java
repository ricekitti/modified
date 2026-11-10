package dev.rice.client.anticheat;

import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.packet.PacketPipeline;
import dev.rice.client.render.DrawPlan;
import dev.rice.client.render.RenderAccess;
import dev.rice.client.render.RenderPassScope;
import dev.rice.module.impl.AntiCheatModule;
import dev.rice.setting.*;
import org.lwjgl.opengl.GL11;
import java.io.File;
import java.util.*;
import java.lang.annotation.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.*;

/** Rice services for the pinned Snowify detectors; no OpenB4 anticheat remains. */
public final class SnowifyAntiCheatHost {
 public static SnowifyAntiCheatHost instance;
 public final Anticheat core;
 public final AnticheatAlerts alerts=new AnticheatAlerts();
 private final AntiCheatModule owner;
 private final List<InboundPackets.Watcher> watchers=new ArrayList<>();
 private net.minecraft.world.World world;private EntityPlayer player;private volatile long session;
 public SnowifyAntiCheatHost(AntiCheatModule owner){this.owner=owner;instance=this;core=new Anticheat(new Configuration(owner));}
 private ClientContext context(){return owner.context();}
 private void ensureSession(){ClientContext c=context();if(world!=c.world()||player!=c.player()){reset();world=c.world();player=c.player();}}
 public synchronized void enable(){ensureSession();core.enabledProp().set(true);}
 public synchronized void disable(){core.enabledProp().set(false);reset();}
 public synchronized void reset(){++session;core.resetSession();alerts.clear();world=null;player=null;}
 public synchronized void tick(){ensureSession();if(owner.isEnabled()&&context().ready())core.onClientTick(new TickEvent.ClientTickEvent());}
 public static void received(Packet<?> packet){SnowifyAntiCheatHost h=instance;if(h==null)return;synchronized(h){if(!h.owner.isEnabled()||!h.context().ready()||h.world!=h.context().world())return;for(InboundPackets.Watcher w:h.watchers)w.seen(packet);}}
 public static void joined(Entity entity){SnowifyAntiCheatHost h=instance;if(h!=null&&entity==h.context().player()){synchronized(h){h.reset();h.ensureSession();}}}
 /** Draws the styled flag toasts in place of the retired aggregate popup, under the live canvas projection. */
 public static void renderHud(){
  SnowifyAntiCheatHost h=instance;if(h==null||!h.owner.isEnabled()||!h.context().ready())return;
  Minecraft mc=h.context().minecraft();if(mc.thePlayer==null||mc.gameSettings.showDebugInfo)return;
  ScaledResolution sr=new ScaledResolution(mc);int sw=sr.getScaledWidth(),sh=sr.getScaledHeight();
  DrawPlan plan=h.alerts.buildPlan(mc.fontRendererObj,sw,sh,h.core.hudX(),h.core.hudY(),h.alerts.consumeDt());
  // Match the live ForgeUiSurface projection: canvas fills reset the modelview to identity (z=0),
  // so the z-range must include 0 (-3000..3000), not the HUD pass's 1000 near-plane which clips them.
  try(RenderPassScope pass=RenderPassScope.hud(mc.displayWidth,mc.displayHeight)) {
   GL11.glMatrixMode(5889);GL11.glLoadIdentity();
   GL11.glOrtho(0.0,(double)sw,(double)sh,0.0,-3000.0,3000.0);
   GL11.glMatrixMode(5888);GL11.glLoadIdentity();
   RenderAccess.submit(new SemanticEvent(10),plan);
  }
 }
 public static void schedule(Minecraft mc,final Runnable task){final SnowifyAntiCheatHost h=instance;final long token=h.session;final net.minecraft.world.World expected=mc.theWorld;mc.addScheduledTask(new Runnable(){public void run(){synchronized(h){if(h.session==token&&h.owner.isEnabled()&&h.context().world()==expected)task.run();}}});}
 public static File blacklistFile(){return new File(new File(Minecraft.getMinecraft().mcDataDir,"rice-slinky"),"anticheat_blacklist.json");}
 public static String playerName(String name,NetworkPlayerInfo info){return name!=null&&name.contains("\u26a0")?name:Anticheat.decorateTab(info,name);}
 public static IChatComponent displayName(IChatComponent name,EntityPlayer player){if(name==null||!Anticheat.marksNametag(player))return name;ChatComponentText marked=new ChatComponentText("");marked.appendSibling(new ChatComponentText("\u26a0 ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));marked.appendSibling(name);return marked;}
 public static String label(EntityPlayer player,String label){if(label==null||!Anticheat.marksNametag(player)||label.contains("\u26a0"))return label;return "\u00a7c\u26a0 \u00a7r"+label;}
 public void openEditor(){Minecraft.getMinecraft().displayGuiScreen(new HudEditor());}
 public static String key(String name){if(name.equals("Flag interval (ticks)"))return "flag_interval_ticks";if(name.equals("Fast Mine"))return "fastmine";if(name.equals("Keep-Y"))return "keep_y";return name.toLowerCase(Locale.ROOT).replace(' ','_');}
 public static final class Property {
  private boolean local;private final Setting<?> setting;
  Property(boolean value){local=value;setting=null;}Property(Setting<?> value){setting=value;}
  public boolean getBoolean(){return setting==null?local:((BooleanSetting)setting).isEnabled();}public boolean getBoolean(boolean fallback){return getBoolean();}
  public int getInt(int fallback){return setting instanceof NumberSetting?((NumberSetting)setting).intValue():fallback;}
  public double getDouble(double fallback){return setting instanceof NumberSetting?((NumberSetting)setting).doubleValue():fallback;}
  public void set(boolean value){if(setting==null)local=value;else ((BooleanSetting)setting).set(value);}
  public void set(int value){set((double)value);}public void set(double value){((NumberSetting)setting).set(value);}
 }
 public static final class Configuration {
  final AntiCheatModule owner;Configuration(AntiCheatModule owner){this.owner=owner;}
  public Map<String,Property> getCategory(String category){return Collections.emptyMap();}
  public Property get(String cat,String name,boolean value,String description){return name.equals("Enabled")?new Property(value):new Property(owner.install(new BooleanSetting(key(name),name,value)));}
  public Property get(String cat,String name,int value,String description,int min,int max){return new Property(owner.install(new NumberSetting(key(name),name,value,min,max,1)));}
  public Property get(String cat,String name,double value,String description,double min,double max){return new Property(owner.install(new NumberSetting(key(name),name,value,min,max,0)));}
 }
 @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) public @interface SubscribeEvent{}
 public static final class TickEvent{public enum Phase{START,END}public static final class ClientTickEvent{public final Phase phase=Phase.END;}}
 public static final class EntityJoinWorldEvent{public final Entity entity;public EntityJoinWorldEvent(Entity entity){this.entity=entity;}}
 public static final class RenderGameOverlayEvent{public enum ElementType{ALL}public static final class Post{public final ElementType type=ElementType.ALL;public final ScaledResolution resolution;public Post(ScaledResolution resolution){this.resolution=resolution;}}}
 public static final class InboundPackets {
  public interface Watcher{void seen(Packet<?> packet);}public static void watch(Watcher watcher){instance.watchers.add(watcher);}
  private static final java.lang.reflect.Field PIPES=dev.rice.inject.ReflectionHelper.findField(PacketPipeline.class,"ACTIVE");
  private static boolean hasInboundHold(){try{for(Object pipe:(Iterable<?>)PIPES.get(null))if(((PacketPipeline)pipe).retained(false)>0)return true;return false;}catch(IllegalAccessException e){throw new IllegalStateException(e);}}
  public static List<String> active(){
   boolean held=hasInboundHold()||myau.MyauClient.incomingPacketBuffer!=null&&myau.MyauClient.incomingPacketBuffer.owner()!=myau.IncomingBufferOwner.NONE;
   dev.rice.module.Module stasis=instance.owner.modules().find("stasis");if(stasis instanceof dev.rice.module.impl.StasisModule)held|=((dev.rice.module.impl.StasisModule)stasis).isHoldingPackets();
   return held?Collections.singletonList("Rice inbound hold"):Collections.<String>emptyList();
  }
 }
 public static final class Conditions{public static void message(String source,String text){Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer!=null)mc.thePlayer.addChatMessage(new ChatComponentText("\u00a77[\u00a7d"+source+"\u00a77]\u00a7r "+text));SnowifyAntiCheatHost h=instance;if(h!=null&&h.owner.isEnabled()){Setting<?> popup=h.owner.findSetting("cheater_popup");if(popup instanceof BooleanSetting&&((BooleanSetting)popup).isEnabled())h.alerts.offerFlagMessage(text);}}}
 public static final class Snowify{public static final Snowify instance=new Snowify();public Snowify getGuiConfig(){return this;}public FontRenderer noticeFont(){return Minecraft.getMinecraft().fontRendererObj;}public boolean glow(){return false;}}
 public static final class Theme{public static void refresh(){}public static int accent(){return 0xFFB76DDB;}public static int[] stops(){return new int[]{0xFFB76DDB,0xFF6E9EE8};}public static int window(){return 0xE61C1C24;}public static int text(){return 0xFFFFFFFF;}public static int textDim(){return 0xFFCCCCD8;}}
 public static final class Gradient{
  public static void rect(float x,float y,float right,float bottom,int[] stops,int alpha){rect(x,y,right,bottom,x,y,right,bottom,stops,alpha);}
  public static void rect(float x,float y,float right,float bottom,float ax,float ay,float ar,float ab,int[] stops,int alpha){for(int i=0;i<16;i++){float l=x+(right-x)*i/16F,r=x+(right-x)*(i+1)/16F;int a=stops[0],b=stops[stops.length-1];float t=(l-ax)/Math.max(1,ar-ax);t=Math.max(0,Math.min(1,t));int color=(alpha<<24)|((int)(((a>>16)&255)*(1-t)+((b>>16)&255)*t)<<16)|((int)(((a>>8)&255)*(1-t)+((b>>8)&255)*t)<<8)|(int)((a&255)*(1-t)+(b&255)*t);Gui.drawRect((int)l,(int)y,(int)Math.ceil(r),(int)bottom,color);}}
 }
 public static final class Draw{
  public static void glow(float x,float y,float w,float h,float radius,float blur,int color,int[] stops){round(x,y,w,h,radius,color);}
  public static void round(float x,float y,float w,float h,float radius,int color){Gui.drawRect((int)x,(int)y,(int)(x+w),(int)(y+h),color);}
  public static void roundGradient(float x,float y,float w,float h,float radius,int[] stops,int alpha){Gradient.rect(x,y,x+w,y+h,stops,alpha);}
  public static void roundVertical(float x,float y,float w,float h,float radius,int a,int b){round(x,y,w,h,radius,a);}
  public static void roundOutline(float x,float y,float w,float h,float radius,float width,int color,int[] stops){Gradient.rect(x,y,x+w,y+width,stops,255);}
 }
 public abstract static class SmoothFont extends FontRenderer{
  protected SmoothFont(net.minecraft.client.settings.GameSettings g,ResourceLocation r,net.minecraft.client.renderer.texture.TextureManager t,boolean u){super(g,r,t,u);}public void drawGlow(String s,float x,float y,int color){drawStringWithShadow(s,x,y,color);}
 }
 public interface HudMovable{
  double hudX();double hudY();void setHud(double x,double y);int[] previewSize(FontRenderer fr);void drawPreview(FontRenderer fr,int x,int y);
  default int[] placeBox(int sw,int sh,int w,int h){return new int[]{(int)Math.round(hudX()*Math.max(1,sw-w)),(int)Math.round(hudY()*Math.max(1,sh-h))};}
  default void moveBox(int x,int y,int w,int h,int sw,int sh){setHud(Math.max(0,Math.min(1,(double)x/Math.max(1,sw-w))),Math.max(0,Math.min(1,(double)y/Math.max(1,sh-h))));}
 }
 public static final class HudEditor extends GuiScreen{
  private boolean dragging;private int dx,dy;
  @Override public boolean doesGuiPauseGame(){return false;}
  @Override public void drawScreen(int mx,int my,float partial){drawDefaultBackground();Anticheat core=instance.core;int[] size=core.previewSize(fontRendererObj),at=core.placeBox(width,height,size[0],size[1]);if(dragging){core.moveBox(mx-dx,my-dy,size[0],size[1],width,height);at=core.placeBox(width,height,size[0],size[1]);}core.drawPreview(fontRendererObj,at[0],at[1]);drawCenteredString(fontRendererObj,"Drag popup to move; Esc to finish",width/2,height-24,0xFFFFFFFF);}
  @Override protected void mouseClicked(int x,int y,int button){if(button!=0)return;int[] size=instance.core.previewSize(fontRendererObj),at=instance.core.placeBox(width,height,size[0],size[1]);if(x>=at[0]&&x<=at[0]+size[0]&&y>=at[1]&&y<=at[1]+size[1]){dragging=true;dx=x-at[0];dy=y-at[1];}}
  @Override protected void mouseReleased(int x,int y,int button){dragging=false;}
 }
 /** Slinky's Teams module owns relationship bit 1, including scoreboard colour matching and cleanup. */
 public static boolean isSlinkyTeammate(EntityPlayer player){return player!=null&&(dev.rice.client.RelationshipService.flags(player)&1)!=0;}
}

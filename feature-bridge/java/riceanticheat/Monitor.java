package dev.rice.anticheat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;

public final class Monitor {
    private static final Configuration config=new Configuration();
    private static final Anticheat detector=new Anticheat(config);
    private static final ConcurrentLinkedQueue<Packet<?>> packets=new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<String> commands=new ConcurrentLinkedQueue<>();
    private static Object world;
    private static final Map<NetworkPlayerInfo,Tab> decorated=new IdentityHashMap<>();
    private static final class Tab {IChatComponent old,written;Tab(IChatComponent old,IChatComponent written){this.old=old;this.written=written;}}
    private static final String[] keys={"Enabled","Flag interval","Ignore teammates","Should ping","Tab Icon","Cheater threshold","Cheater popup","Nametag mark","Blacklist warning","Autoblock","Keep-Y","Tower","Bridge Assist","Stasis","Fast Mine","No Break Delay","Popup x","Popup y"};
    public static void configure(double[] values){for(int i=0;i<keys.length;i++){Property p=config.getCategory("anticheat").get(keys[i]);if(i==1||i==5)p.set((int)values[i]);else if(i>=16)p.set(values[i]);else p.set(values[i]!=0);}}
    public static void clear(){detector.clearWorld();packets.clear();commands.clear();restoreTab();world=null;}
    private static void restoreTab(){for(Map.Entry<NetworkPlayerInfo,Tab> e:decorated.entrySet())if(e.getKey().getDisplayName()==e.getValue().written)e.getKey().setDisplayName(e.getValue().old);decorated.clear();}
    private static void tabs(Minecraft mc){
        if(mc.getNetHandler()==null){restoreTab();return;}
        Set<NetworkPlayerInfo> current=new HashSet<>(mc.getNetHandler().getPlayerInfoMap());
        Iterator<Map.Entry<NetworkPlayerInfo,Tab>> iterator=decorated.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<NetworkPlayerInfo,Tab> e=iterator.next();
            if(!current.contains(e.getKey())||!detector.tabIconProp().getBoolean()||!Anticheat.isCheater(e.getKey().getGameProfile().getName())){
                if(e.getKey().getDisplayName()==e.getValue().written)e.getKey().setDisplayName(e.getValue().old);iterator.remove();
            }
        }
        if(!detector.tabIconProp().getBoolean())return;
        for(NetworkPlayerInfo info:current){
            if(!Anticheat.isCheater(info.getGameProfile().getName()))continue;
            Tab existing=decorated.get(info);IChatComponent display=info.getDisplayName();if(existing!=null&&display==existing.written)continue;
            String original=display!=null?display.getFormattedText():net.minecraft.scoreboard.ScorePlayerTeam.formatPlayerName(info.getPlayerTeam(),info.getGameProfile().getName());
            IChatComponent written=new ChatComponentText(Anticheat.decorateTab(info,original));decorated.put(info,new Tab(display,written));info.setDisplayName(written);
        }
    }
    public static int packet(Object object,boolean incoming,boolean enabled){
        if(!enabled||!(object instanceof Packet))return 0;
        if(incoming){if(object instanceof net.minecraft.network.play.server.S22PacketMultiBlockChange||object instanceof net.minecraft.network.play.server.S23PacketBlockChange||object instanceof net.minecraft.network.play.server.S25PacketBlockBreakAnim||object instanceof net.minecraft.network.play.server.S28PacketEffect)packets.offer((Packet<?>)object);return 0;}
        if(object instanceof C01PacketChatMessage){String text=((C01PacketChatMessage)object).getMessage();String lower=text.toLowerCase(java.util.Locale.ROOT);
            if(lower.equals("/blacklist")||lower.startsWith("/blacklist ")||lower.equals(".blacklist")||lower.startsWith(".blacklist ")){commands.offer(text);return 2;}}
        return 0;
    }
    private static void render(Minecraft mc,double[] frame){
        if(mc.theWorld==null||mc.thePlayer==null||mc.fontRendererObj==null)return;
        ScaledResolution resolution=new ScaledResolution(mc);RenderState saved=new RenderState();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();
        try{
            GL11.glLoadIdentity();GlStateManager.disableDepth();GlStateManager.enableTexture2D();GlStateManager.enableBlend();GlStateManager.tryBlendFuncSeparate(770,771,1,0);
            double scale=resolution.getScaleFactor();GlStateManager.scale(scale,scale,1);
            detector.onRender(resolution.getScaledWidth(),resolution.getScaledHeight());
            if(detector.nametagMarkProp().getBoolean()&&!mc.gameSettings.hideGUI){
                double[] projection=new double[46];System.arraycopy(frame,18,projection,0,46);
                for(EntityPlayer player:mc.theWorld.playerEntities)if(player!=mc.thePlayer&&Anticheat.marksNametag(player)){
                    float partial=(float)projection[13];double x=player.lastTickPosX+(player.posX-player.lastTickPosX)*partial,y=player.lastTickPosY+(player.posY-player.lastTickPosY)*partial+player.height+.5,z=player.lastTickPosZ+(player.posZ-player.lastTickPosZ)*partial;
                    double[] point=dev.rice.bedplates.BedplatesCore.project(x,y,z,projection);if(point!=null)mc.fontRendererObj.drawStringWithShadow("\u00a7c!",(float)(point[0]/scale-4),(float)(point[1]/scale-10),0xffff4444);
                }
            }
        }finally{GL11.glPopMatrix();saved.restore();GL11.glPopAttrib();GL11.glMatrixMode(saved.matrix);}
    }
    public static void event(int type,double[] settings){
        configure(settings);Minecraft mc=Minecraft.getMinecraft();
        if(type==5||type==26){clear();if(type==5)world=mc.theWorld;return;}
        if(!detector.enabledProp().getBoolean())return;
        if(world!=mc.theWorld){clear();world=mc.theWorld;if(mc.thePlayer!=null)detector.onWorldJoin(mc.thePlayer);}
        if(type==18){
            if(mc.theWorld==null||mc.thePlayer==null)return;
            Packet<?> packet;while((packet=packets.poll())!=null)detector.onPacket(packet);
            String command;while((command=commands.poll())!=null){int split=command.indexOf(' ');String args=split<0?"":command.substring(split+1).trim();new CommandBlacklist().processCommand(mc.thePlayer,args.isEmpty()?new String[0]:args.split("\\s+"));}
            detector.onClientTick();tabs(mc);
        }else if(type==7)render(mc,settings);
    }
    private Monitor(){}
}

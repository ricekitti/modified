package dev.rice.anticheat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
public final class Host {
    public static boolean menuOpen(){return dev.rice.module.NativeFeatureBridge.guiOpen()||Minecraft.getMinecraft().currentScreen!=null;}
    public static void message(String module,String text){Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer!=null)mc.thePlayer.addChatMessage(new ChatComponentText("\u00a7d[RiceSlinky]\u00a77 ["+module+"] \u00a7r"+text));}
    public static boolean isTeammate(Minecraft mc,EntityPlayer player){return mc.thePlayer!=null&&mc.thePlayer.isOnSameTeam(player);}
    public static void drawPopup(FontRenderer font,int x,int y,int width,int height,String name,String flags,float alpha){
        Gui.drawRect(x,y,x+width,y+height,0xe625252b);Gui.drawRect(x,y,x+2,y+height,0xffff5b94);
        font.drawStringWithShadow("RiceSlinky \u00a7lCHEATER DETECTED",x+7,y+4,0xffff5b94);
        font.drawStringWithShadow(name,x+7,y+15,0xffffffff);font.drawStringWithShadow(flags,x+7,y+26,0xffaaaaaa);
    }
    private Host(){}
}

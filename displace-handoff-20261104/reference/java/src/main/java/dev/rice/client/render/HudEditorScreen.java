package dev.rice.client.render;

import dev.rice.module.ModuleManager;
import dev.rice.module.impl.AntiCheatModule;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** One editor for all anchored client HUD elements, including previews for temporarily inactive widgets. */
public final class HudEditorScreen extends GuiScreen {
 private final ModuleManager modules;private int selected=-1,dx,dy;
 public HudEditorScreen(ModuleManager modules){this.modules=modules;}
 @Override public boolean doesGuiPauseGame(){return false;}
 @Override public void initGui(){buttonList.clear();buttonList.add(new GuiButton(0,width/2-105,height-28,100,20,"Done"));buttonList.add(new GuiButton(1,width/2+5,height-28,100,20,"Reset HUD"));}
 @Override public void drawScreen(int mx,int my,float partial){
  if(selected>=0)HudLayout.move(HudLayout.IDS[selected],mx-dx,my-dy,width,height,fontRendererObj);
  drawCenteredString(fontRendererObj,"Edit HUD - drag a box; Esc saves and closes",width/2,8,0xFFFFFFFF);
  for(int i=0;i<HudLayout.IDS.length;i++){
   String id=HudLayout.IDS[i];int[] b=HudLayout.bounds(id,width,height,fontRendererObj);int color=i==selected?0xFFB76DDB:0xFF77778A;
   HudPreviewRenderer.draw(modules,id,fontRendererObj);
   boolean hovered=mx>=b[0]&&mx<=b[0]+b[2]&&my>=b[1]&&my<=b[1]+b[3];
   if(i==selected||hovered){
    drawRect(b[0]-1,b[1]-1,b[0]+b[2]+1,b[1],color);
    drawRect(b[0]-1,b[1]+b[3],b[0]+b[2]+1,b[1]+b[3]+1,color);
    drawRect(b[0]-1,b[1],b[0],b[1]+b[3],color);
    drawRect(b[0]+b[2],b[1],b[0]+b[2]+1,b[1]+b[3],color);
    String label=HudLayout.LABELS[i];int labelX=Math.max(2,Math.min(width-fontRendererObj.getStringWidth(label)-2,b[0]));
    drawString(fontRendererObj,label,labelX,Math.max(2,b[1]-11),0xFFCCCCDD);
   }
  }
  super.drawScreen(mx,my,partial);
 }
 @Override protected void mouseClicked(int mx,int my,int button)throws IOException{
  if(button==0&&my<height-32)for(int i=HudLayout.IDS.length-1;i>=0;i--){int[] b=HudLayout.bounds(HudLayout.IDS[i],width,height,fontRendererObj);if(mx>=b[0]&&mx<=b[0]+b[2]&&my>=b[1]&&my<=b[1]+b[3]){selected=i;dx=mx-b[0];dy=my-b[1];return;}}
  super.mouseClicked(mx,my,button);
 }
 @Override protected void mouseReleased(int mx,int my,int button){selected=-1;super.mouseReleased(mx,my,button);}
 @Override protected void actionPerformed(GuiButton button){if(button.id==0)mc.displayGuiScreen(null);else if(button.id==1)HudLayout.reset();}
 @Override public void onGuiClosed(){selected=-1;modules.persist();}
}

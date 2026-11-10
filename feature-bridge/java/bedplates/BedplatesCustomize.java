package dev.rice.bedplates;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

/** Rise-style in-world editor for the Bedplates panel: drag the panel up/down to lift it,
 *  scroll to scale. Values persist through Bedplates (config/rice-bedplates.properties). */
public final class BedplatesCustomize extends GuiScreen {
    private final GuiScreen parent;
    private final Map<Integer,Integer> sample=new LinkedHashMap<Integer,Integer>();
    private boolean dragging;private int grabY;private float grabLift;
    private final float buttonW=70,buttonH=20;private float doneX,resetX,buttonsY;

    public BedplatesCustomize(GuiScreen parent){
        this.parent=parent;
        sample.put(80<<4,14);  // snow block
        sample.put(24<<4,5);   // sandstone
        sample.put(49<<4,2);   // obsidian
    }

    @Override public boolean doesGuiPauseGame(){return false;}
    @Override public void initGui(){layout();}
    private void layout(){buttonsY=height-42;resetX=width/2f-buttonW-6;doneX=width/2f+6;}
    private float bedTopY(){return height*0.56f;}
    private float panelTop(){return bedTopY()-26-Bedplates.liftPx;}

    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks){
        layout();
        drawRect(0,0,width,height,0x33000000);
        drawCenteredString(fontRendererObj,"§bDrag the panel§r up/down, §bscroll§r to scale",width/2,16,0xffffff);
        drawCenteredString(fontRendererObj,"offset "+(int)Bedplates.liftPx+"    scale "+String.format(Locale.ROOT,"%.2f",Bedplates.editScale),width/2,30,0xffb8bcc6);
        float cx=width/2f,sq=78;
        drawRect((int)(cx-sq/2),(int)(bedTopY()-sq),(int)(cx+sq/2),(int)bedTopY(),0x2233cc66);
        Bedplates.panel(mc,sample,cx,panelTop(),Bedplates.editScale,true,true);
        drawExampleBed(cx,bedTopY());
        drawCenteredString(fontRendererObj,"example bed",(int)cx,(int)bedTopY()+20,0xffd0d4dc);
        pill("Reset",resetX,buttonsY,hover(mouseX,mouseY,resetX,buttonsY));
        pill("Done",doneX,buttonsY,hover(mouseX,mouseY,doneX,buttonsY));
    }

    private void drawExampleBed(float cx,float topY){
        int x0=(int)(cx-22),x1=(int)(cx+22),y0=(int)topY,y1=(int)(topY+14);
        drawRect(x0,y0,x1,y1,0xcce891c7);
        drawRect(x0,y0,x1,y0+4,0xddf3b6df);
        drawRect(x0,y1-2,x1,y1,0x99a85b93);
    }

    private boolean hover(int mx,int my,float x,float y){return mx>=x&&my>=y&&mx<=x+buttonW&&my<=y+buttonH;}
    private void pill(String label,float x,float y,boolean hot){
        drawRect((int)x,(int)y,(int)(x+buttonW),(int)(y+buttonH),hot?0xff2b2f3a:0xf0171a21);
        drawRect((int)x,(int)y,(int)(x+buttonW),(int)y+1,hot?0x66ffffff:0x22ffffff);
        drawCenteredString(fontRendererObj,label,(int)(x+buttonW/2),(int)(y+buttonH/2-4),0xfff2f4f8);
    }

    @Override protected void mouseClicked(int mouseX,int mouseY,int mouseButton) throws IOException {
        super.mouseClicked(mouseX,mouseY,mouseButton);
        if(mouseButton!=0)return;
        if(hover(mouseX,mouseY,resetX,buttonsY)){Bedplates.editScale=0.8f;Bedplates.liftPx=0;return;}
        if(hover(mouseX,mouseY,doneX,buttonsY)){close();return;}
        if(mouseY<=bedTopY()+2&&mouseY>=panelTop()-14){dragging=true;grabY=mouseY;grabLift=Bedplates.liftPx;}
    }

    @Override protected void mouseClickMove(int mouseX,int mouseY,int clickedMouseButton,long timeSinceLastClick){
        if(dragging)Bedplates.liftPx=Bedplates.clampLift(grabLift+(grabY-mouseY));
    }

    @Override protected void mouseReleased(int mouseX,int mouseY,int state){dragging=false;}

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel=Mouse.getDWheel();
        if(wheel!=0)Bedplates.editScale=Bedplates.clampScale(Bedplates.editScale+(wheel>0?0.05f:-0.05f));
    }

    @Override public void onGuiClosed(){Bedplates.save(mc);}
    private void close(){Bedplates.save(mc);mc.displayGuiScreen(parent);}
}

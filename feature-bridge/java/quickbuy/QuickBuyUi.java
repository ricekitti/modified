package dev.rice.quickbuy;
import net.minecraft.client.gui.Gui;
/** Small vanilla-widget adapter; the foreign ClickGUI framework is not loaded. */
public final class QuickBuyUi {
    public static final int PANEL_BACKGROUND=0xf0161619,SIDEBAR_BACKGROUND=0xff1c1d22,SURFACE=0xff282a31,SURFACE_ALT=0xff22242a,
        SURFACE_HOVER=0xff343741,SURFACE_ACTIVE=0xff414652,TRACK=0xff30323a,DISABLED_SURFACE=0xff202127,
        TEXT_PRIMARY=0xfff4f5f7,TEXT_SECONDARY=0xffc6c9d2,TEXT_MUTED=0xff969ca9,TEXT_DISABLED=0xff666c78,ACCENT_SOFT=0xff454f67;
    public static void surface(int a,int b,int c,int d,int color){Gui.drawRect(a,b,c,d,color);}
    public static void capsule(int a,int b,int c,int d,int color){Gui.drawRect(a,b,c,d,color);}
    public static void fill(float a,float b,float c,float d,float radius,int color){Gui.drawRect((int)a,(int)b,(int)c,(int)d,color);}
}

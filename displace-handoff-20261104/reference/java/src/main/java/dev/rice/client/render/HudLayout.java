package dev.rice.client.render;

import dev.rice.module.Module;
import dev.rice.module.ModuleManager;
import dev.rice.module.impl.AntiCheatModule;
import dev.rice.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import dev.rice.client.render.openb4.OpenB4TargetHudBackend;

/** Shared editor coordinates for every anchored Rice HUD widget. World-space labels/arrows stay attached to entities. */
public final class HudLayout {
 public static final String[] IDS={"mod_overlay","notifications","targethud","blockin","bednuker","anticheat"};
 public static final String[] LABELS={"Module list","Notifications","TargetHUD","Block In progress","Bed Nuker progress","Anticheat popup"};
 public static final String[] OFFSETS={"mod_overlay","notifications","blockin","bednuker"};
 private static ModuleManager manager;
 private HudLayout(){}
 public static void bind(ModuleManager modules){manager=modules;}
 private static NumberSetting number(Module m,String key){return m!=null&&m.findSetting(key) instanceof NumberSetting?(NumberSetting)m.findSetting(key):null;}
 private static double value(Module m,String key,double fallback){NumberSetting s=number(m,key);return s==null?fallback:s.doubleValue();}
 public static double offset(String id,boolean x){return manager==null?0:value(manager.find("gui"),"hud_"+id+(x?"_x":"_y"),0);}
 public static boolean shifted(String id){return offset(id,true)!=0||offset(id,false)!=0;}
 public static DrawPlan apply(String id,DrawPlan plan){if(shifted(id)){Minecraft mc=Minecraft.getMinecraft();plan.translate((float)(offset(id,true)*mc.displayWidth),(float)(offset(id,false)*mc.displayHeight),0);}return plan;}
 public static boolean enabled(String id){Module m=manager==null?null:manager.find(id);return m!=null&&m.isEnabled();}
 public static boolean editing(){return Minecraft.getMinecraft().currentScreen instanceof HudEditorScreen;}
 public static int[] bounds(String id,int sw,int sh,FontRenderer font){return HudPreviewRenderer.bounds(manager,id,sw,sh,font);}
 public static void move(String id,int left,int top,int sw,int sh,FontRenderer font){
  int[] old=bounds(id,sw,sh,font);left=Math.max(0,Math.min(Math.max(0,sw-old[2]),left));top=Math.max(0,Math.min(Math.max(0,sh-old[3]),top));
  if(id.equals("anticheat")){((AntiCheatModule)manager.find(id)).backend.core.moveBox(left,top,old[2],old[3],sw,sh);storeOwnedPositions();return;}
  Module target=manager.find(id);
  if(id.equals("targethud")){number(target,"pos_x").set(value(target,"pos_x",70)+left-old[0]);number(target,"pos_y").set(value(target,"pos_y",40)+top-old[1]);OpenB4TargetHudBackend.offsetX=number(target,"pos_x").intValue();OpenB4TargetHudBackend.offsetY=number(target,"pos_y").intValue();storeOwnedPositions();return;}
  Module gui=manager.find("gui");number(gui,"hud_"+id+"_x").set(offset(id,true)+(double)(left-old[0])/Math.max(1,sw));number(gui,"hud_"+id+"_y").set(offset(id,false)+(double)(top-old[1])/Math.max(1,sh));
 }
 public static void reset(){Module gui=manager.find("gui");for(String id:OFFSETS){number(gui,"hud_"+id+"_x").set(0D);number(gui,"hud_"+id+"_y").set(0D);}Module target=manager.find("targethud");number(target,"pos_x").set(70D);number(target,"pos_y").set(40D);OpenB4TargetHudBackend.offsetX=70;OpenB4TargetHudBackend.offsetY=40;((AntiCheatModule)manager.find("anticheat")).backend.core.setHud(.5,.12);storeOwnedPositions();}
 /** Mirrors preserve existing per-module settings while saving global editor positions across restarts. */
 private static void storeOwnedPositions(){Module gui=manager.find("gui"),target=manager.find("targethud");number(gui,"hud_targethud_x").set(value(target,"pos_x",70));number(gui,"hud_targethud_y").set(value(target,"pos_y",40));AntiCheatModule ac=(AntiCheatModule)manager.find("anticheat");number(gui,"hud_anticheat_x").set(ac.backend.core.hudX());number(gui,"hud_anticheat_y").set(ac.backend.core.hudY());}
 public static void restoreOwnedPositions(Module gui){if(manager==null||manager.find("gui")!=gui)return;Module target=manager.find("targethud");if(target!=null){number(target,"pos_x").set(value(gui,"hud_targethud_x",70));number(target,"pos_y").set(value(gui,"hud_targethud_y",40));OpenB4TargetHudBackend.offsetX=number(target,"pos_x").intValue();OpenB4TargetHudBackend.offsetY=number(target,"pos_y").intValue();}Module ac=manager.find("anticheat");if(ac instanceof AntiCheatModule)((AntiCheatModule)ac).backend.core.setHud(value(gui,"hud_anticheat_x",.5),value(gui,"hud_anticheat_y",.12));}
 public static void open(ModuleManager modules){bind(modules);Minecraft mc=modules.context().minecraft();if(modules.context().ready())mc.displayGuiScreen(new HudEditorScreen(modules));}
}

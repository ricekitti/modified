import dev.rice.client.render.*;
import dev.rice.module.Module;
import dev.rice.module.impl.GuiModule;
import dev.rice.setting.*;
import dev.rice.config.RecoveredConfiguration;
import java.lang.reflect.*;
import java.util.*;

public final class HudEditorCheck {
 static void check(boolean v,String m){V116Check.check(v,m);}
 static Object get(Object o,String key)throws Exception{Field f=o.getClass().getDeclaredField(key);f.setAccessible(true);return f.get(o);}
 static <T> void configure(dev.rice.setting.Setting<T> s){s.set(s.get());}
 static java.util.List<String> signature(DrawPlan plan){
  java.util.List<String> out=new ArrayList<>();for(DrawPlan.Op op:plan.operations())out.add(op.kind+Arrays.toString(op.values)+(op.value instanceof CanvasFonts.Text?((CanvasFonts.Text)op.value).text:""));return out;
 }
 static int textColor(DrawPlan.Op op){return Float.floatToRawIntBits(op.values[2]);}
 static boolean hasRun(DrawPlan plan,String text){for(DrawPlan.Op o:plan.operations())if(o.kind.equals("text")&&text.equals(o.value))return true;return false;}
 static float boxWidth(DrawPlan plan){for(DrawPlan.Op o:plan.operations())if(o.kind.equals("roundedRect"))return o.values[2];return 0F;}
 static java.util.List<DrawPlan.Op> runs(DrawPlan plan){java.util.List<DrawPlan.Op> out=new ArrayList<>();for(DrawPlan.Op o:plan.operations())if(o.kind.equals("text")&&o.value instanceof String)out.add(o);return out;}
 static void anticheatAlerts()throws Exception{
  dev.rice.module.impl.AntiCheatModule ac=(dev.rice.module.impl.AntiCheatModule)V116Check.manager.find("anticheat");
  dev.rice.client.anticheat.AnticheatAlerts alerts=ac.backend.alerts;
  boolean wasEnabled=ac.isEnabled();Object popupBefore=get(ac.backend.core,"popupName");
  alerts.clear();check(alerts.active()==0,"alerts did not start empty");
  // One flag paints a frosted box, a hairline border and the seven coloured runs of "[AC] <player> failed <check> x<VL>".
  check(alerts.flag("Steve","Speed",4),"first flag not queued");
  DrawPlan one=alerts.buildPlan(V116Check.font,854,480,.95,.9,0.016);
  check(one.operations().stream().filter(o->o.kind.equals("roundedRect")).count()==1,"alert box missing");
  check(one.operations().stream().anyMatch(o->o.kind.equals("roundedStroke")),"alert border missing");
  java.util.List<DrawPlan.Op> runs=runs(one);check(runs.size()==7,"expected seven coloured text runs, got "+runs.size());
  check(runs.get(0).value.equals("[")&&textColor(runs.get(0))==0xFFB4B4B8,"bracket run not gray");
  check(runs.get(1).value.equals("AC")&&textColor(runs.get(1))==0xFFFF6464,"AC run not red");
  check(runs.get(3).value.equals("Steve")&&textColor(runs.get(3))==0xFFFFFFFF,"player run not white");
  check(runs.get(4).value.equals(" failed ")&&textColor(runs.get(4))==0xFFB4B4B8,"failed run not gray");
  check(runs.get(5).value.equals("Speed")&&textColor(runs.get(5))==0xFFFF6464,"check run not red");
  check(runs.get(6).value.equals(" x4")&&textColor(runs.get(6))==0xFFB4B4B8,"VL run not gray");
  // Same player+check is muted by the ten-second cooldown; distinct checks stack and the sixth drops the oldest.
  check(!alerts.flag("Steve","Speed",5),"cooldown did not mute repeat");check(alerts.active()==1,"cooldown added a toast");
  for(int i=0;i<6;i++)alerts.flag("P"+i,"Check"+i,i+1);
  check(alerts.active()==5,"six distinct flags did not cap at five: "+alerts.active());
  DrawPlan capped=alerts.buildPlan(V116Check.font,854,480,.95,.9,0.016);
  check(capped.operations().stream().filter(o->o.kind.equals("roundedRect")).count()==5,"capped stack did not paint five boxes");
  check(hasRun(capped,"P5")&&!hasRun(capped,"Steve"),"newest kept / oldest evicted order wrong");
  // Message parsing: a real detector flag line queues a toast; a cheater summary line does not.
  alerts.clear();check(alerts.offerFlagMessage("Target §7flagged §fNo Break Delay §7§ox2"),"detector flag line not parsed");
  DrawPlan parsed=alerts.buildPlan(V116Check.font,854,480,.95,.9,0.016);
  check(hasRun(parsed,"No Break Delay")&&hasRun(parsed," x2"),"parsed check/VL wrong");
  check(!alerts.offerFlagMessage("§lRuby §cis cheating §7(Scaffold x3)"),"non-flag line produced a toast");check(alerts.active()==1,"non-flag line queued a toast");
  // Longer names widen the box; the painter never toggles the module or disturbs the pinned detector.
  alerts.clear();alerts.flag("Al","Fly",1);float narrow=boxWidth(alerts.buildPlan(V116Check.font,854,480,.95,.9,0.016));
  alerts.clear();alerts.flag("AVeryLongPlayerName","KillAura",12);float wide=boxWidth(alerts.buildPlan(V116Check.font,854,480,.95,.9,0.016));
  check(wide>narrow,"box width does not follow text width");
  check(ac.isEnabled()==wasEnabled,"alert painter toggled the AntiCheat module");
  check(java.util.Objects.equals(get(ac.backend.core,"popupName"),popupBefore),"alert painter mutated the pinned detector popup state");
  alerts.clear();
  System.out.println("PASS: anticheat flag toasts: seven-run coloured line, cooldown mute, five-item cap with oldest eviction, detector-line parsing, width-follows-text, module/detector isolation");
 }
 static void realPreviews(java.nio.file.Path classes)throws Exception{
  dev.rice.module.impl.ModOverlayModule overlay=(dev.rice.module.impl.ModOverlayModule)V116Check.manager.find("mod_overlay");
  java.util.List<?> oldRows=new ArrayList<>(overlay.rows());
  DrawPlan list=overlay.editorPlan(V116Check.mc.displayWidth);
  check(list.operations().stream().anyMatch(o->o.kind.equals("roundedRectVarying")),"editor module list does not use production row backgrounds");
  check(list.operations().stream().anyMatch(o->o.value instanceof CanvasFonts.Text),"editor module list bypasses real canvas font");
  check(overlay.rows().equals(oldRows),"preview changed live module-list rows");
  int[] b=HudPreviewRenderer.canvasBounds(list,854,480);check(b[2]>1&&b[3]>1&&b[2]<160,"module list still uses fixed placeholder bounds");
  NumberSetting padding=(NumberSetting)overlay.findSetting("padding_x");double oldPadding=padding.doubleValue();
  padding.set(padding.getMinimum());int[] narrow=HudPreviewRenderer.canvasBounds(overlay.editorPlan(854),854,480);
  padding.set(padding.getMaximum());int[] wide=HudPreviewRenderer.canvasBounds(overlay.editorPlan(854),854,480);
  check(wide[2]>=narrow[2]+(int)Math.round(2*(padding.getMaximum()-padding.getMinimum()))-1,"preview ignores real module-list padding setting");padding.set(oldPadding);
  dev.rice.module.impl.NotificationsModule notices=(dev.rice.module.impl.NotificationsModule)V116Check.manager.find("notifications");
  int queued=notices.queue().messages().size();DrawPlan notification=notices.editorPlan();
  check(notices.queue().messages().size()==queued,"preview polluted live notification queue");
  check(notification.operations().stream().anyMatch(o->o.kind.equals("roundedRect"))&&notification.operations().stream().anyMatch(o->o.value instanceof CanvasFonts.Text&&((CanvasFonts.Text)o.value).text.equals("Enabled Block In")),"preview is not a real notification");
  for(dev.rice.setting.Setting<?> s:notices.getSettings())configure(s);((BooleanSetting)notices.findSetting("hide_ingame")).set(false);
  V116Check.manager.applyNow(notices,true);notices.queue().clearAll();notices.queue().append(1,"Enabled Block In");notices.queue().messages().get(0).age=1F;
  check(signature(notification).equals(signature(notices.plan(V116Check.c))),"notification preview differs from production plan");V116Check.manager.applyNow(notices,false);notices.queue().clearAll();
  ClassLoader recorder=V116Check.recorder(classes);Class<?> type=recorder.loadClass("dev.rice.module.impl.TargetHudModule");
  Object hud=type.getConstructor(dev.rice.module.ModuleManager.class).newInstance(V116Check.manager);
  ((ChoiceSetting)((Module)hud).findSetting("mode")).set(2);
  Class<?> backend=recorder.loadClass("dev.rice.client.render.openb4.OpenB4TargetHudBackend");
  Field shown=backend.getDeclaredField("shown");shown.setAccessible(true);shown.setDouble(null,.237);
  V116GlStub.reset();V116Check.font.drawn.clear();type.getMethod("renderEditor").invoke(hud);
  check(!V116GlStub.primitives.isEmpty()&&!V116Check.font.drawn.isEmpty(),"TargetHUD preview did not execute the actual backend");
  check(shown.getDouble(null)==.237&&V116GlStub.matrixDepth==0&&V116GlStub.attribDepth==0,"TargetHUD preview leaked animation/GL state");
  // The saved GUI preferences remain bound to the registered module's settings.
  for(dev.rice.setting.Setting<?> setting:V116Check.manager.find("targethud").getSettings()){
   String key=setting.getKey();String label=setting.getLabel();
   if(key.equals("mode")||key.equals("theme"))dev.rice.client.render.openb4.OpenB4TargetHudHost.bind(label,new dev.rice.client.render.openb4.OpenB4TargetHudHost.SliderSetting(setting));
   else if(setting instanceof BooleanSetting)dev.rice.client.render.openb4.OpenB4TargetHudHost.bind(label,new dev.rice.client.render.openb4.OpenB4TargetHudHost.ButtonSetting((BooleanSetting)setting));
  }
  dev.rice.shaded.asm.tree.ClassNode screen=new dev.rice.shaded.asm.tree.ClassNode();new dev.rice.shaded.asm.ClassReader(java.nio.file.Files.readAllBytes(classes.resolve("dev/rice/client/render/HudEditorScreen.class"))).accept(screen,0);
  check(screen.methods.stream().filter(m->m.name.equals("drawScreen")).anyMatch(m->Arrays.stream(m.instructions.toArray()).anyMatch(i->i instanceof dev.rice.shaded.asm.tree.MethodInsnNode&&((dev.rice.shaded.asm.tree.MethodInsnNode)i).owner.equals("dev/rice/client/render/HudPreviewRenderer"))),"editor bypasses real HUD renderer");
  dev.rice.shaded.asm.tree.ClassNode progress=new dev.rice.shaded.asm.tree.ClassNode();new dev.rice.shaded.asm.ClassReader(java.nio.file.Files.readAllBytes(classes.resolve("dev/rice/client/blockin/SuppliedBlockInHost$ProgressBar.class"))).accept(progress,0);
  for(String name:new String[]{"draw","preview"})check(progress.methods.stream().filter(m->m.name.equals(name)).anyMatch(m->Arrays.stream(m.instructions.toArray()).anyMatch(i->i instanceof dev.rice.shaded.asm.tree.MethodInsnNode&&((dev.rice.shaded.asm.tree.MethodInsnNode)i).name.equals("paint"))),"Block In preview does not share its live HUD painter");
  System.out.println("PASS: actual module-list fonts/backgrounds/padding/bounds; byte-for-byte notification plan equality and queue isolation; real TargetHUD backend drawing and animation/GL restoration; shared Block In painter");
 }
 static void guiBindingToggle()throws Exception{
  GuiModule gui=(GuiModule)V116Check.manager.find("gui");int oldBind=gui.getBinding();
  for(Setting<?> setting:gui.getSettings())configure(setting);
  Object q=get(V116Check.manager,"stateChanges");dev.rice.module.ModuleStateQueue<?> queue=(dev.rice.module.ModuleStateQueue<?>)q;
  java.util.List<?> enables=(java.util.List<?>)get(q,"enables"),disables=(java.util.List<?>)get(q,"disables");
  gui.setBinding(99);V116Check.put(gui,"enabled",false);V116Check.mc.currentScreen=null;queue.clear();
  gui.pollBinding(false,true);gui.pollBinding(true,true);check(enables.size()==1,"GUI press did not open from physical polling");
  gui.dispatchInput(2,99,true,1,new byte[0],0,0,0);gui.pollBinding(true,true);check(enables.size()==1,"duplicate event/held opening key queued twice");
  queue.clear();V116Check.put(gui,"enabled",true);V116Check.mc.currentScreen=V116Check.blank(RecoveredGuiScreen.class);
  gui.pollBinding(true,true);check(!queue.pending(),"opening key held across screen handoff immediately closed GUI");
  gui.pollBinding(false,true);gui.pollBinding(true,true);check(disables.size()==1,"second press did not close open menu");
  gui.dispatchInput(2,99,true,0,new byte[0],0,0,0);check(disables.size()==1,"close press dispatched twice");
  queue.clear();V116Check.put(gui,"enabled",false);V116Check.mc.currentScreen=null;V116Check.manager.input().resetKey(99);
  gui.pollBinding(true,true);gui.dispatchInput(2,99,true,0,new byte[0],0,0,0);check(!queue.pending(),"held closing key reopened GUI after screen cleanup");
  gui.pollBinding(false,true);gui.dispatchInput(2,99,true,1,new byte[0],0,0,0);check(enables.size()==1,"missed native release prevented later reopen");
  queue.clear();V116Check.put(gui,"enabled",true);V116Check.mc.currentScreen=V116Check.blank(RecoveredGuiScreen.class);gui.pollBinding(false,true);
  gui.runtime().activateBinding(new Object(),gui::setBinding);gui.dispatchInput(2,28,true,0,new byte[0],0,0,0);gui.pollBinding(true,true);
  check(gui.getBinding()==28&&!queue.pending(),"assigning GUI bind closed menu while key was held");
  gui.pollBinding(false,true);gui.runtime().activateText(new Object(),(text,ch)->text.append((char)ch));
  gui.dispatchInput(2,28,true,0,new byte[]{'c'},0,0,0);gui.pollBinding(true,true);check(!queue.pending()&&gui.runtime().text.toString().equals("c"),"typing GUI binding closed text input");
  gui.runtime().clearInteraction();gui.pollBinding(false,true);gui.pollBinding(true,true);check(disables.size()==1,"bind failed after text field released focus");
  queue.clear();V116Check.put(gui,"enabled",false);V116Check.mc.currentScreen=null;gui.pollBinding(false,false);gui.pollBinding(true,false);gui.pollBinding(true,true);
  check(!queue.pending(),"key held from unrelated screen opened menu on return");
  gui.setBinding(4);gui.pollBinding(false,true);gui.pollBinding(true,true);check(enables.size()==1,"mouse GUI bind did not open");
  queue.clear();V116Check.put(gui,"enabled",true);V116Check.mc.currentScreen=V116Check.blank(RecoveredGuiScreen.class);gui.pollBinding(false,true);gui.dispatchInput(2,4,true,0,new byte[0],0,0,0);gui.pollBinding(true,true);check(disables.size()==1,"mouse GUI bind did not close exactly once");
  queue.clear();V116Check.put(gui,"enabled",false);V116Check.mc.currentScreen=null;gui.setBinding(oldBind);gui.pollBinding(false,true);gui.runtime().clearInteraction();V116Check.manager.input().clear();
  System.out.println("PASS: GUI bind opens/closes/reopens through polling and events; held/duplicate keys suppressed; missed releases recovered; text/bind capture and mouse binds preserved");
 }
 public static void main(String[] args)throws Exception{
  V116Check.setup();for(Module m:V116Check.manager.all())if(m.isEnabled())V116Check.manager.applyNow(m,false);
  GuiModule gui=(GuiModule)V116Check.manager.find("gui");check(gui.findSetting("edit_hud") instanceof BooleanSetting,"global Edit HUD control absent");check(dev.rice.module.RecoveredUiLayout.controls(gui).stream().anyMatch(c->c.setting==gui.findSetting("edit_hud")),"Edit HUD control hidden");
  guiBindingToggle();
  Map<Module,Boolean> states=new HashMap<>();for(Module m:V116Check.manager.all())states.put(m,m.isEnabled());
  realPreviews(java.nio.file.Paths.get(args[0]));
  anticheatAlerts();
  int sw=854,sh=480;
  for(String id:HudLayout.IDS){int[] b=HudLayout.bounds(id,sw,sh,V116Check.font);int x=id.equals("mod_overlay")?b[0]-90:b[0]+20,y=b[1]+25;HudLayout.move(id,x,y,sw,sh,V116Check.font);int[] moved=HudLayout.bounds(id,sw,sh,V116Check.font);check(Math.abs(moved[0]-x)<=1&&Math.abs(moved[1]-y)<=1,"widget did not follow drag: "+id);HudLayout.move(id,-500,10000,sw,sh,V116Check.font);int[] clamped=HudLayout.bounds(id,sw,sh,V116Check.font);check(clamped[0]>=0&&clamped[1]>=0&&clamped[0]+clamped[2]<=sw&&clamped[1]+clamped[3]<=sh,"widget not clamped: "+id);}
  DrawPlan plan=HudLayout.apply("mod_overlay",new DrawPlan().canvas());check(plan.operations().stream().anyMatch(o->o.kind.equals("translate")),"module-list layout not applied to render plan");
  com.google.gson.JsonObject global=RecoveredConfiguration.serialize(V116Check.manager,RecoveredConfiguration.Scope.GLOBAL,Collections.emptyList()),profile=RecoveredConfiguration.serialize(V116Check.manager,RecoveredConfiguration.Scope.PROFILE,Collections.emptyList());
  java.nio.file.Path prefFile=java.nio.file.Files.createTempDirectory(java.nio.file.Paths.get(args[1]).resolve("build"),"hud-prefs-test-").resolve("gui.json");dev.rice.config.GuiPreferences preferences=new dev.rice.config.GuiPreferences(prefFile);preferences.save(gui);
  double original=HudLayout.offset("notifications",true);HudLayout.reset();check(HudLayout.offset("notifications",true)==0,"reset did not restore shared offsets");
  preferences.load(gui);RecoveredConfiguration.apply(V116Check.manager,global,RecoveredConfiguration.Scope.GLOBAL);RecoveredConfiguration.apply(V116Check.manager,profile,RecoveredConfiguration.Scope.PROFILE);V116Check.manager.applyPendingChanges();check(HudLayout.offset("notifications",true)==original,"positions not persisted through settings");
  for(Map.Entry<Module,Boolean> state:states.entrySet())check(state.getKey().isEnabled()==state.getValue(),"moving HUD toggled a feature: "+state.getKey().getName());
  HudLayout.reset();check(((NumberSetting)V116Check.manager.find("targethud").findSetting("pos_x")).intValue()==70&&((NumberSetting)V116Check.manager.find("targethud").findSetting("pos_y")).intValue()==40&&HudLayout.offset("bednuker",false)==0,"reset original positions changed");
  HudEditorScreen editor=new HudEditorScreen(V116Check.manager);V116Check.put(editor,"width",sw);V116Check.put(editor,"height",sh);V116Check.put(editor,"fontRendererObj",V116Check.font);
  int[] b=HudLayout.bounds("mod_overlay",sw,sh,V116Check.font);Method click=HudEditorScreen.class.getDeclaredMethod("mouseClicked",int.class,int.class,int.class);click.setAccessible(true);click.invoke(editor,b[0]+5,b[1]+5,0);check((int)get(editor,"selected")==0,"drag hit testing failed");
  Method release=HudEditorScreen.class.getDeclaredMethod("mouseReleased",int.class,int.class,int.class);release.setAccessible(true);release.invoke(editor,0,0,0);check((int)get(editor,"selected")==-1,"drag selection leaked on release");
  final int[] saved={0};V116Check.manager.setPersistence(()->saved[0]++);editor.onGuiClosed();check(saved[0]==1&&(int)get(editor,"selected")==-1,"editor did not persist/release on close");
  // The momentary button is safe while no world is loaded and never becomes a persisted toggle.
  V116Check.put(V116Check.mc,"theWorld",null);((BooleanSetting)gui.findSetting("edit_hud")).set(true);check(gui.afterSettingControl("edit_hud",V116Check.c)&&!((BooleanSetting)gui.findSetting("edit_hud")).isEnabled(),"Edit HUD button is not momentary");
  System.out.println("PASS: one Edit HUD control; six independent widget drags; render translation; clamping/reset; global/profile persistence; feature states preserved; release/close cleanup and momentary button");
 }
}

package dev.rice.module.impl;
import dev.rice.client.*;
import dev.rice.client.blockin.SuppliedBlockInHost;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.setting.*;
import org.lwjgl.input.Mouse;
/** Full supplied Rice/Snowify BlockIn module with target lifecycle and settings. */
public final class BlockInModule extends Module {
 public boolean active;
 private final NumberSetting speed=add(new NumberSetting("speed","Speed",1.0,.1,1,.1));
 private final BooleanSetting requireGround=add(new BooleanSetting("require_ground","Require ground",true));
 private final BooleanSetting progressBar=add(new BooleanSetting("show_progress_bar","Show progress bar",true));
 private final SuppliedBlockInHost backend;
 public BlockInModule(ModuleManager modules){super("blockin","Block In","Supplied Rice/Snowify Block In",ModuleCategory.WORLD);for(Setting<?> s:getSettings()){s.parseAndSet(s.format());s.saveCurrent();}setHoldBinding(true);backend=new SuppliedBlockInHost(this,modules,speed,requireGround,progressBar);}
 @Override public int descriptorFlags(){return super.descriptorFlags() & ~2;}
 @Override public int metadataCategory(){return 3;}
 @Override public boolean listens(int type){return type==17||type==18||type==23||type==14||type==26;}
 @Override protected void onEnable(ClientContext c){backend.enable();}
 @Override protected void onDisable(ClientContext c){backend.stop();active=false;}
 @Override public void onWorldUnload(ClientContext c){backend.worldUnload();active=false;}
 @Override public void onEvent(ClientContext c,SemanticEvent e){if(e.type==17)backend.reserve();else if(e.type==18)backend.endTick();else if(e.type==23)backend.packet(e.packet);else if(e.type==26)onWorldUnload(c);else if(e.type==14&&active)e.cancelled=true;}
 @Override public String statusText(){return backend.progressText==null?"Block In":backend.progressText;}
 public boolean swallowMouseEvent(){return isEnabled()&&active&&Mouse.getEventButtonState();}
}

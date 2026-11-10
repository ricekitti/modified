package dev.rice.module.impl;
import dev.rice.client.ClientContext;
import dev.rice.client.anticheat.SnowifyAntiCheatHost;
import dev.rice.module.Module;
import dev.rice.module.ModuleManager;
import dev.rice.module.ModuleCategory;
import dev.rice.setting.*;
/** Snowify's full local cheater detector and persistent blacklist. */
public final class AntiCheatModule extends Module {
 private final ModuleManager modules;
 public final SnowifyAntiCheatHost backend;
 private final BooleanSetting edit;
 public AntiCheatModule(ModuleManager modules){super("anticheat","AntiCheat","Detects other cheaters.",ModuleCategory.UTILITY);this.modules=modules;backend=new SnowifyAntiCheatHost(this);edit=add(new BooleanSetting("edit_position","Edit popup position",false));for(Setting<?> s:getSettings()){s.parseAndSet(s.format());s.saveCurrent();}}
 public <T extends Setting<?>> T install(T setting){return add(setting);}
 public ModuleManager modules(){return modules;}public ClientContext context(){return modules.context();}
 @Override public boolean afterSettingControl(String key,ClientContext c){if(key.equals("edit_position")&&edit.isEnabled()){edit.set(false);if(c.ready())backend.openEditor();return true;}return super.afterSettingControl(key,c);}
 @Override public int metadataCategory(){return 4;}
 @Override public boolean listens(int type){return false;}
 @Override protected void onEnable(ClientContext c){backend.enable();}
 @Override protected void onDisable(ClientContext c){backend.disable();}
 @Override public void onWorldUnload(ClientContext c){backend.reset();}
 @Override public void onClientTick(ClientContext c){if(edit.isEnabled()){edit.set(false);backend.openEditor();}backend.tick();}
}

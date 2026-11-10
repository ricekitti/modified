package dev.rice.module.impl;

import dev.rice.client.ClientContext;import dev.rice.client.SemanticEvent;import dev.rice.client.openjade.OpenJadeFireballBoostBackend;import dev.rice.client.openjade.OpenJadeHost;import dev.rice.module.Module;import dev.rice.module.ModuleCategory;import dev.rice.module.ModuleManager;import dev.rice.setting.NumberSetting;
public final class FireballBoostModule extends Module {
 private final ModuleManager modules;private final OpenJadeFireballBoostBackend backend=new OpenJadeFireballBoostBackend();private final NumberSetting delay=add(new NumberSetting("boost_delay","Boost Delay (ms)",550,450,650,10));private boolean published;
 public FireballBoostModule(ModuleManager modules){super("fireball_boost","Fireball Boost","Fireball and snowball boost sequence",ModuleCategory.UTILITY);this.modules=modules;}
 @Override public boolean listens(int type){return type==13||type==17||type==26;}private void sync(ClientContext c){OpenJadeHost.bind(modules,c);backend.setNumber("Boost Delay",delay.doubleValue());}
 @Override protected void onEnable(ClientContext c){sync(c);backend.hostEnable();OpenJadeHost.syncDisable(backend,this);}@Override protected void onDisable(ClientContext c){sync(c);backend.hostDisable();published=false;}
 @Override public void onClientTickStart(ClientContext c){sync(c);backend.onTickEndStart();OpenJadeHost.syncDisable(backend,this);}@Override public void onEvent(ClientContext c,SemanticEvent e){sync(c);if(e.type==17)published=backend.hostRotation(600);else if(e.type==13)OpenJadeHost.finalRotation(e,published);else if(e.type==26)backend.hostDisable();OpenJadeHost.syncDisable(backend,this);}@Override public void onWorldUnload(ClientContext c){backend.hostDisable();published=false;}
}

package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.openjade.OpenJadeFireballPearlBackend;
import dev.rice.client.openjade.OpenJadeHost;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.setting.BooleanSetting;

public final class FireballPearlModule extends Module {
   private final ModuleManager modules; private final OpenJadeFireballPearlBackend backend=new OpenJadeFireballPearlBackend();
   private final BooleanSetting realRotations=add(new BooleanSetting("real_rotations","Real Rotations",false)); private boolean published;
   public FireballPearlModule(ModuleManager modules){super("fireball_pearl","Fireball Pearl","Throws a pearl and optional fireball in sequence",ModuleCategory.UTILITY);this.modules=modules;}
   @Override public boolean listens(int type){return type==13||type==17||type==26;}
   private void sync(ClientContext c){OpenJadeHost.bind(modules,c);backend.setBoolean("Real Rotations",realRotations.isEnabled());}
   @Override protected void onEnable(ClientContext c){sync(c);backend.hostEnable();OpenJadeHost.syncDisable(backend,this);}
   @Override protected void onDisable(ClientContext c){sync(c);backend.hostDisable();published=false;}
   @Override public void onClientTickStart(ClientContext c){sync(c);backend.onTickEndStart();OpenJadeHost.syncDisable(backend,this);}
   @Override public void onEvent(ClientContext c,SemanticEvent e){sync(c);if(e.type==17){published=backend.hostRotation(600);backend.onPreUpdateHost();}else if(e.type==13)OpenJadeHost.finalRotation(e,published);else if(e.type==26)backend.hostDisable();OpenJadeHost.syncDisable(backend,this);}
   @Override public void onWorldUnload(ClientContext c){backend.hostDisable();published=false;}
}

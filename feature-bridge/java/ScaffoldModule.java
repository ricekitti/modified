package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.setting.Setting;
import com.snowy.module.player.scaffold.Scaffold;
import com.snowy.config.Configuration;
import com.snowy.config.Property;
import com.snowy.core.event.*;
import com.snowy.rotation.RotationManager;
import com.snowy.util.Claims;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import java.lang.reflect.Field;
import java.util.*;

/** Host settings, lifecycle and call-site adapters for the supplied source, not a Scaffold implementation. */
public final class ScaffoldModule extends Module {
    private Scaffold core=new Scaffold(new Configuration());
    private EntityPlayerSP player;
    private Object world;
    private boolean suspended=true;
    private float previousVisualYaw,visualYaw,previousVisualPitch,visualPitch;
    private boolean visual;
    private static final Field SAVED=dev.rice.inject.ReflectionHelper.findField(Scaffold.class,"previousSlot");
    private static final Field HELD=dev.rice.inject.ReflectionHelper.findField(Scaffold.class,"heldSlot");

    public ScaffoldModule(){
        super("supplied_scaffold","Scaffold","Slinky placement with Mayu movement",ModuleCategory.WORLD);
        for(Property p:core.properties())add(p.host);
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    private void configureCore(){
        List<Property> properties=core.properties();
        for(int i=0;i<properties.size();i++)((Setting)properties.get(i).host).set(getSettings().get(i).get());
    }
    @Override public boolean settingVisible(String key){
        configureCore();
        for(Property p:core.properties())if(p.host.getKey().equals(key))return p.visible.getAsBoolean();
        return true;
    }
    private void detach(){
        if(player!=null){
            int saved=slot(SAVED);
            if(saved>=0)player.inventory.currentItem=saved;
        }
        Claims.unregister(core);
        core=new Scaffold(new Configuration());
        RotationManager r=RotationManager.get();r.endTick();r.startTick();r.endTick();
        suspended=true;visual=false;
    }
    private static int read(Field field,Object object){
        try{return field.getInt(object);}catch(IllegalAccessException e){throw new IllegalStateException(e);}
    }
    private int slot(Field field){return read(field,core);}
    private void saveSlot(int slot){
        try{SAVED.setInt(core,slot);}catch(IllegalAccessException e){throw new IllegalStateException(e);}
    }
    private boolean context(ClientContext c){
        if(!c.ready())return false;
        if(player!=c.player()||world!=c.world()){
            detach();player=c.player();world=c.world();
            RotationManager.get().resetSent(player.rotationYaw,player.rotationPitch);
            visualYaw=previousVisualYaw=player.rotationYaw;visualPitch=previousVisualPitch=player.rotationPitch;
        }
        configureCore();return true;
    }
    private boolean inputReady(ClientContext c){
        return c.ready()&&c.inputReady()&&c.minecraft().currentScreen==null&&!dev.rice.module.NativeFeatureBridge.guiOpen();
    }
    @Override protected void onEnable(ClientContext c){
        if(context(c)&&inputReady(c)){core.enabled(true);suspended=false;}
    }
    @Override protected void onDisable(ClientContext c){core.enabled(false);suspended=false;}
    @Override public void onWorldUnload(ClientContext c){detach();player=null;world=null;}
    public void suspend(ClientContext c){
        if(player==c.player())core.enabled(false);else detach();
        RotationManager.get().endTick();suspended=true;visual=false;
    }
    @Override public boolean listens(int type){return type==6||type==14||type==18;}
    @Override public void onClientTick(ClientContext c){}
    @Override public void onEvent(ClientContext c,SemanticEvent e){
        if(e.type==18){RotationManager.get().endTick();return;}
        if(!context(c)||!inputReady(c))return;
        if(e.type==6)core.onRenderWorld(new RenderWorldLastEvent(e.timerRenderPartialTicks));
        else if(e.type==14&&!e.active&&RotationManager.get().isActive())e.cancelled=true;
    }
    public void playerUpdate(ClientContext c){
        if(!context(c))return;
        if(!inputReady(c)){suspend(c);return;}
        if(isEnabled()&&(suspended||!core.on())){core.enabled(true);suspended=false;}
        RotationManager r=RotationManager.get();r.startTick();r.noteUpdate(player.isRiding());
        core.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.START,player));
    }
    public void packetEnd(ClientContext c,boolean replaced){
        if(!context(c))return;
        RotationManager r=RotationManager.get();
        r.noteWalkingPlayer();
        try{
            float yaw=LastReported.yaw.getFloat(player),pitch=LastReported.pitch.getFloat(player);
            r.recordSent(yaw,pitch,replaced);
            if(replaced){previousVisualYaw=visualYaw;previousVisualPitch=visualPitch;visualYaw=yaw;visualPitch=pitch;visual=true;}
            else visual=false;
        }catch(IllegalAccessException e){throw new IllegalStateException(e);}
        r.endTick();
    }
    public void movementInput(ClientContext c){
        if(!context(c)||!inputReady(c))return;
        RotationManager r=RotationManager.get();
        if(r.shouldFixMovement()){
            float[] corrected=RotationManager.correctedInput(player.rotationYaw,r.getYaw(),
                player.movementInput.moveForward,player.movementInput.moveStrafe,player.movementInput.sneak);
            if(corrected!=null){player.movementInput.moveForward=corrected[0];player.movementInput.moveStrafe=corrected[1];}
            r.noteInputCorrected();
        }
        core.input(player.movementInput);
    }
    public void livingUpdate(ClientContext c){}
    public float[] motionRotation(){
        RotationManager r=RotationManager.get();return r.isActive()?new float[]{r.getYaw(),r.getPitch()}:null;
    }
    public float movementYaw(){return RotationManager.get().shouldFixMovement()?RotationManager.get().getYaw():Float.NaN;}
    public int hudSlot(){return -1;}
    public void hotbarChange(ClientContext c,int requested,int scroll){
        if(!context(c))return;
        int held=slot(HELD),saved=slot(SAVED);
        int target=requested>=0?requested:Math.floorMod((saved>=0?saved:player.inventory.currentItem)-Integer.signum(scroll),9);
        if(held>=0&&core.on())saveSlot(target);else player.inventory.currentItem=target;
    }
    public void moveEntity(ClientContext c,Entity entity,double x,double y,double z){
        if(entity!=c.player()||!context(c)||!inputReady(c)){entity.moveEntity(x,y,z);return;}
        boolean wasSneaking=player.movementInput.sneak;
        core.preMove(player);
        try{
            if(!wasSneaking&&player.movementInput.sneak){
                AxisAlignedBB bounds=player.getEntityBoundingBox();
                while(x!=0&&c.world().getCollidingBoundingBoxes(player,bounds.offset(x,-1,0)).isEmpty())x=step(x);
                while(z!=0&&c.world().getCollidingBoundingBoxes(player,bounds.offset(0,-1,z)).isEmpty())z=step(z);
                while(x!=0&&z!=0&&c.world().getCollidingBoundingBoxes(player,bounds.offset(x,-1,z)).isEmpty()){x=step(x);z=step(z);}
            }
            entity.moveEntity(x,y,z);
        }finally{core.postMove(player);}
    }
    private static double step(double value){return value>=-.05&&value<.05?0:value>0?value-.05:value+.05;}
    public void renderHud(ClientContext c,int accent,double now){
        if(!context(c))return;
        com.snowy.gui.Theme.accent(accent);
        core.onOverlay(new RenderGameOverlayEvent.Post(new ScaledResolution(c.minecraft())));
    }
    public float[] visualValues(ClientContext c){return visual?new float[]{previousVisualYaw,visualYaw,previousVisualYaw,visualYaw,previousVisualPitch,visualPitch}:null;}
    @Override public String statusText(){configureCore();return core.suffix();}
    private static final class LastReported{
        static final Field yaw=dev.rice.inject.ReflectionHelper.findField(EntityPlayerSP.class,"lastReportedYaw","field_175164_bL","bN");
        static final Field pitch=dev.rice.inject.ReflectionHelper.findField(EntityPlayerSP.class,"lastReportedPitch","field_175165_bM","bO");
    }
}

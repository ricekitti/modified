package dev.rice.module;

import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.rotation.RotationService;
import dev.rice.module.impl.BowAimbotModule;
import dev.rice.module.impl.WeaponsModule;
import dev.rice.setting.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import java.util.List;

/** Adapts the supplied modules to the existing native menu/event dispatcher. */
public final class NativeFeatureBridge {
    public static void riceAnticheat(int event,double[] settings){dev.rice.anticheat.Monitor.event(event,settings);}
    public static int riceAnticheatPacket(Object packet,boolean incoming,boolean enabled){return dev.rice.anticheat.Monitor.packet(packet,incoming,enabled);}
    public static double fatigueEvent(int event,Object object,boolean enabled,int mode,double percent){
        net.minecraft.client.Minecraft mc=net.minecraft.client.Minecraft.getMinecraft();
        if(event==26){dev.rice.anticheat.FatigueControl.clear();return Double.NaN;}
        if(event==5){if(!enabled)dev.rice.anticheat.FatigueControl.release(mc);return Double.NaN;}
        if(event==2)return dev.rice.anticheat.FatigueControl.packet(mc,(net.minecraft.network.Packet<?>)object,enabled,mode)?2:0;
        dev.rice.anticheat.FatigueControl.tick(mc,enabled,mode);
        return dev.rice.anticheat.FatigueControl.speed(mc,enabled,mode,percent);
    }
    public static void cleanupRiceUtilities(){dev.rice.anticheat.Monitor.clear();dev.rice.anticheat.FatigueControl.release(net.minecraft.client.Minecraft.getMinecraft());}

    public static void bedplates(Object minecraft,int event,double[] frame){dev.rice.bedplates.Bedplates.event(minecraft,event,frame);}

    private static ClientContext context;
    private static Module[] modules;
    private static ModuleManager manager;
    private static Object lastWorld,lastPlayer;
    public static native double rotation(int operation,double a,double b);
    public static native int flags(Object entity);
    public static native boolean guiOpen();
    public static native boolean timerBlocked();
    public static native double rescueHost(int operation,double value);
    public static boolean mayuLoaded(Object world,Object position){
        boolean loaded=((net.minecraft.world.World)world).isBlockLoaded((net.minecraft.util.BlockPos)position);
        if(loaded&&context!=null&&context.world()==world&&modules!=null)
            ((dev.rice.module.impl.ScaffoldModule)modules[4]).playerUpdate(context);
        return loaded;
    }
    public static void mayuLiving(Object player){
        if(context!=null&&context.player()==player&&modules!=null)
            ((dev.rice.module.impl.ScaffoldModule)modules[4]).livingUpdate(context);
    }
    public static void scaffoldPacketEnd(boolean replaced){
        if(context!=null&&modules!=null)((dev.rice.module.impl.ScaffoldModule)modules[4]).packetEnd(context,replaced);
    }
    public static void mayuScroll(Object inventory,int scroll){
        if(context!=null&&context.ready()&&context.player().inventory==inventory&&modules[4].isEnabled())
            ((dev.rice.module.impl.ScaffoldModule)modules[4]).hotbarChange(context,-1,scroll);
        else ((net.minecraft.entity.player.InventoryPlayer)inventory).changeCurrentItem(scroll);
    }
    public static void mayuSelect(Object inventory,int slot){
        if(context!=null&&context.ready()&&context.player().inventory==inventory&&modules[4].isEnabled())
            ((dev.rice.module.impl.ScaffoldModule)modules[4]).hotbarChange(context,slot,0);
        else ((net.minecraft.entity.player.InventoryPlayer)inventory).currentItem=slot;
    }
    public static boolean mayuBlockClick(){return context!=null&&context.ready()&&modules!=null&&modules[4].isEnabled()&&!guiOpen();}
    public static void mayuResetBlock(){if(context!=null&&context.ready())context.minecraft().playerController.resetBlockRemoving();}
    /** Mayu's GuiIngame.updateTick item-label redirect, scoped to that GUI call. */
    public static void mayuGuiUpdate(Object object){
        net.minecraft.client.gui.GuiIngame gui=(net.minecraft.client.gui.GuiIngame)object;
        net.minecraft.entity.player.InventoryPlayer inventory=context!=null&&context.ready()?context.player().inventory:null;
        int slot=modules==null?-1:((dev.rice.module.impl.ScaffoldModule)modules[4]).hudSlot();
        if(inventory==null||slot<0){gui.updateTick();return;}
        int actual=inventory.currentItem;
        try{inventory.currentItem=slot;gui.updateTick();}
        finally{if(inventory.currentItem==slot)inventory.currentItem=actual;}
    }
    public static boolean mayuMove(Object object,float strafe,float forward,float friction){
        if(context==null||modules==null||object!=context.player())return false;
        net.minecraft.entity.Entity entity=(net.minecraft.entity.Entity)object;
        float yaw=((dev.rice.module.impl.ScaffoldModule)modules[4]).movementYaw();
        if(Float.isNaN(yaw))return false;
        com.snowy.rotation.RotationManager rotations=com.snowy.rotation.RotationManager.get();
        rotations.beginMovementYaw(entity);
        try{entity.moveFlying(strafe,forward,friction);}
        finally{rotations.endMovementYaw(entity);}
        return true;
    }
    private static final class JumpAccess{
        static final java.lang.reflect.Method method=dev.rice.inject.ReflectionHelper.findMethod(net.minecraft.entity.EntityLivingBase.class,null,
              new String[]{"jump","func_70664_aZ","bF"},new Class<?>[0]);
    }

    public static void jumpEntity(Object entity){

        net.minecraft.entity.Entity value=(net.minecraft.entity.Entity)entity;
        float original=value.rotationYaw;
        float yaw=context!=null&&entity==context.player()&&modules!=null&&modules[4].isEnabled()
            ?((dev.rice.module.impl.ScaffoldModule)modules[4]).movementYaw():Float.NaN;
        try{if(!Float.isNaN(yaw))value.rotationYaw=yaw;JumpAccess.method.invoke(entity);}
        catch(java.lang.reflect.InvocationTargetException failure){
            Throwable cause=failure.getCause();if(cause instanceof RuntimeException)throw (RuntimeException)cause;
            if(cause instanceof Error)throw (Error)cause;throw new IllegalStateException(cause);
        }catch(IllegalAccessException failure){throw new IllegalStateException(failure);}
        finally{value.rotationYaw=original;}
    }
    public static float[] scaffoldMotionRotation(){
        return modules==null?null:((dev.rice.module.impl.ScaffoldModule)modules[4]).motionRotation();
    }
    public static float[] rescueMotionRotation(){
        if(modules==null||context==null||!context.ready())return null;
        for(int index=6;index<=7;index++){
            float[] angles=((dev.rice.module.impl.pearl.PearlRescueModule)modules[index]).motionRotation();
            if(angles!=null)return angles;
        }
        return ((dev.rice.module.impl.pearl.BridgeEgg)modules[8]).motionRotation();
    }

    public static void suspendScaffold(){
        if(modules!=null&&context!=null)((dev.rice.module.impl.ScaffoldModule)modules[4]).suspend(context);
    }
    private static net.minecraft.entity.Entity displacePlayer;
    private static float displaceYaw,displacePitch,displaceWrittenYaw,displaceWrittenPitch;
    private static void restoreDisplace(){
        if(displacePlayer!=null){
            if(displacePlayer.rotationYaw==displaceWrittenYaw)displacePlayer.rotationYaw=displaceYaw;
            if(displacePlayer.rotationPitch==displaceWrittenPitch)displacePlayer.rotationPitch=displacePitch;
            displacePlayer=null;
        }
    }
    private static float counterX,counterY;
    public static void scaffoldHudPosition(float x,float y){counterX=x;counterY=y;}
    public static float[] scaffoldHudLayout(){
        if(context==null||!context.ready())return null;
        Minecraft mc=context.minecraft();
        net.minecraft.client.gui.ScaledResolution resolution=new net.minecraft.client.gui.ScaledResolution(mc);
        int count=com.snowy.module.player.scaffold.Blocks.count(context.player());
        String label=String.valueOf(count)+(count==1?" block":" blocks");
        float width=29+mc.fontRendererObj.getStringWidth(label);
        return new float[]{width/resolution.getScaledWidth(),16f/resolution.getScaledHeight(),14f/resolution.getScaledHeight(),count};
    }
    public static void scaffoldHud(int accent,double now){
        if(modules==null||context==null)return;
        dev.rice.module.impl.ScaffoldModule module=(dev.rice.module.impl.ScaffoldModule)modules[4];
        if(guiOpen()){module.suspend(context);return;}
        net.minecraft.client.gui.ScaledResolution resolution=new net.minecraft.client.gui.ScaledResolution(context.minecraft());
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        try{
            net.minecraft.client.renderer.GlStateManager.translate(counterX*resolution.getScaledWidth(),counterY*resolution.getScaledHeight(),0);
            module.renderHud(context,accent,now);
        }finally{net.minecraft.client.renderer.GlStateManager.popMatrix();}
    }
    public static void scaffoldMove(Object object,double x,double y,double z){
        net.minecraft.entity.Entity entity=(net.minecraft.entity.Entity)object;
        if(modules!=null&&context!=null&&object==context.player()&&modules[8].isEnabled()
            &&((dev.rice.module.impl.pearl.BridgeEgg)modules[8]).cancelMove())return;
        if(modules!=null&&context!=null&&object==context.player())
            ((dev.rice.module.impl.ScaffoldModule)modules[4]).moveEntity(context,entity,x,y,z);
        else entity.moveEntity(x,y,z);
    }
    /** Scoped model pose; the camera and movement packet rotations stay independent. */
    public static float[] scaffoldVisual(Object object,float[] previous) {
        if (!(object instanceof net.minecraft.entity.EntityLivingBase)) return null;
        net.minecraft.entity.EntityLivingBase entity=(net.minecraft.entity.EntityLivingBase)object;
        if(previous!=null){
            if(previous.length!=12)return null;
            if(entity.prevRotationYawHead==previous[6])entity.prevRotationYawHead=previous[0];
            if(entity.rotationYawHead==previous[7])entity.rotationYawHead=previous[1];
            if(entity.prevRenderYawOffset==previous[8])entity.prevRenderYawOffset=previous[2];
            if(entity.renderYawOffset==previous[9])entity.renderYawOffset=previous[3];
            if(entity.prevRotationPitch==previous[10])entity.prevRotationPitch=previous[4];
            if(entity.rotationPitch==previous[11])entity.rotationPitch=previous[5];
            return null;
        }
        if(context==null||!context.ready()||object!=context.player()||modules==null)return null;
        float[] visual=((dev.rice.module.impl.ScaffoldModule)modules[4]).visualValues(context);
        if(visual==null&&modules[0].isEnabled()&&myau.RotationState.isActiveWithinTicks(1))
            visual=new float[]{myau.RotationState.previousHeadYaw(),myau.RotationState.headYaw(),
                myau.RotationState.previousBodyYaw(),myau.RotationState.bodyYaw(),myau.RotationState.previousPitch(),myau.RotationState.pitch()};
        if(visual==null)return null;
        float[] saved={entity.prevRotationYawHead,entity.rotationYawHead,entity.prevRenderYawOffset,entity.renderYawOffset,
            entity.prevRotationPitch,entity.rotationPitch,visual[0],visual[1],visual[2],visual[3],visual[4],visual[5]};
        entity.prevRotationYawHead=visual[0];entity.rotationYawHead=visual[1];
        entity.prevRenderYawOffset=visual[2];entity.renderYawOffset=visual[3];
        entity.prevRotationPitch=visual[4];entity.rotationPitch=visual[5];
        return saved;
    }
    private static native boolean packetSilent(Object packet,boolean mark);

    public static void sendNoEvent(Packet<?> packet) {
        if(packet==null||context==null||!context.ready())return;
        if(!packetSilent(packet,true))throw new IllegalStateException("Silent packet dispatch unavailable");
        try {context.player().sendQueue.addToSendQueue(packet);}
        catch(RuntimeException|Error failure){packetSilent(packet,false);throw failure;}
    }
    public static void initialize(Object minecraft) {
        try {dev.rice.inject.Mappings.load(NativeFeatureBridge.class.getResourceAsStream("/dev/rice/inject/mappings.txt"));}
        catch(java.io.IOException failure){throw new IllegalStateException("Feature mappings unavailable",failure);}
        context=new ClientContext((Minecraft)minecraft);
        manager=new ModuleManager(context);
        WeaponsModule weapons=new WeaponsModule();
        manager.register(weapons);
        manager.register(new dev.rice.module.impl.TimerRangeModule(weapons));
        // Supplied manual Displace owns slot zero and the existing native descriptor.
        modules=new Module[9];modules[0]=new dev.rice.module.impl.KnockbackDisplaceModule(weapons,manager);
        manager.register(modules[0]);modules[1]=new BowAimbotModule();
        manager.register(modules[1]);
        dev.rice.config.InitialSettings.applyMissing(manager);
        // User-selected Displace defaults from the supplied settings screenshot.
        ((ChoiceSetting)modules[0].findSetting("direction")).set(5);
        ((NumberSetting)modules[0].findSetting("cooldown")).set(5.0);
        ((NumberSetting)modules[0].findSetting("angle")).set(90.0);
        for(Setting<?> setting:modules[0].getSettings())setting.saveCurrent();
        modules[2]=new dev.rice.module.impl.FogModule();
        manager.register(modules[2]);
        modules[3]=new dev.rice.module.impl.ShaderModule(manager);
        manager.register(modules[3]);
        modules[4]=new dev.rice.module.impl.ScaffoldModule();
        manager.register(modules[4]);
        modules[5]=new dev.rice.module.impl.FireballBoostFeatureModule();
        manager.register(modules[5]);
        modules[6]=new dev.rice.module.impl.AutoPearlFeatureModule();
        dev.rice.module.impl.pearl.RescueServices.autoPearl=(dev.rice.module.impl.pearl.PearlRescueModule)modules[6];
        manager.register(modules[6]);
        modules[7]=new dev.rice.module.impl.FireballPearlClutchFeatureModule();
        dev.rice.module.impl.pearl.RescueServices.fireballPearl=(dev.rice.module.impl.pearl.PearlRescueModule)modules[7];
        manager.register(modules[7]);
        modules[8]=new dev.rice.module.impl.pearl.BridgeEgg();
        dev.rice.module.impl.pearl.RescueServices.bridgeEgg=(dev.rice.module.impl.pearl.BridgeEgg)modules[8];
        manager.register(modules[8]);
        context.setGuiEnabled(()->guiOpen());
    }
    public static float ravenVoidYaw(Object target,float yaw,float angle,float radius,boolean wall){
        if(context==null||!context.ready()||!(target instanceof net.minecraft.entity.Entity))return Float.NaN;
        Float result=dev.rice.client.combat.RavenBsVoidScanner.findYaw(context,(net.minecraft.entity.Entity)target,yaw,angle,radius,wall);
        return result==null?Float.NaN:result;
    }
    // A RangeSetting is surfaced to the native menu as two Number rows (min + max endpoint),
    // keeping the two endpoints independently adjustable and persisted; sampling between them
    // stays in the owning module. All other setting kinds are emitted unchanged (byte-identical
    // menu for the existing displace/bow/fog/shader features).
    public static String[][] labels(int index) {
        if(modules[index]==null)return new String[0][];
        List<Setting<?>> settings=modules[index].getSettings();
        List<String[]> rows=new java.util.ArrayList<>();
        for(Setting<?> setting:settings) {
            if(setting instanceof RangeSetting) {
                rows.add(new String[]{setting.getKey()+"_lo",setting.getLabel()+" (min)"});
                rows.add(new String[]{setting.getKey()+"_hi",setting.getLabel()+" (max)"});
            } else {
                List<String> options=setting instanceof ChoiceSetting?((ChoiceSetting)setting).getOptions():java.util.Collections.emptyList();
                String[] row=new String[options.size()+2];row[0]=setting.getKey();row[1]=setting.getLabel();
                for(int n=0;n<options.size();n++)row[n+2]=options.get(n);
                rows.add(row);
            }
        }
        return rows.toArray(new String[0][]);
    }
    public static double[] specification(int index,int settingIndex) {
        int cursor=0;
        for(Setting<?> setting:modules[index].getSettings()) {
            if(setting instanceof RangeSetting) {
                if(settingIndex==cursor||settingIndex==cursor+1) {
                    RangeSetting r=(RangeSetting)setting;
                    double v=settingIndex==cursor?r.get().lower:r.get().upper;
                    double span=r.maximum()-r.minimum();double step=index==4?1:span<=5?0.05:1;
                    return new double[]{1,r.minimum(),r.maximum(),step,v};
                }
                cursor+=2;
            } else {
                if(settingIndex==cursor) {
                    if(setting instanceof BooleanSetting)return new double[]{0,0,1,1,((BooleanSetting)setting).isEnabled()?1:0};
                    if(setting instanceof NumberSetting){NumberSetting s=(NumberSetting)setting;return new double[]{1,s.getMinimum(),s.getMaximum(),s.getStep(),s.doubleValue()};}
                    if(setting instanceof ChoiceSetting){ChoiceSetting s=(ChoiceSetting)setting;return new double[]{2,0,s.getOptions().size()-1,1,s.index()};}
                    if(setting instanceof ColorSetting){ColorSetting.Value color=((ColorSetting)setting).get();return new double[]{3,color.red,color.green,color.blue,0};}
                    throw new IllegalStateException("Unsupported module setting "+setting.getKey());
                }
                cursor+=1;
            }
        }
        throw new IllegalStateException("setting row out of range "+settingIndex);
    }
    public static void configure(int index,double[] values) {
        if(modules[index]==null)return;
        List<Setting<?>> settings=modules[index].getSettings();
        int required=0;for(Setting<?> setting:settings)required+=(setting instanceof ColorSetting)?3:(setting instanceof RangeSetting)?2:1;
        if(values.length!=required)throw new IllegalArgumentException("setting count");
        int cursor=0;
        for(int i=0;i<settings.size();i++) {
            Setting<?> s=settings.get(i);
            if(s instanceof BooleanSetting)((BooleanSetting)s).set(values[cursor++]!=0);
            else if(s instanceof NumberSetting)((NumberSetting)s).set(values[cursor++]);
            else if(s instanceof ChoiceSetting)((ChoiceSetting)s).set((int)values[cursor++]);
            else if(s instanceof RangeSetting){double lo=values[cursor++],hi=values[cursor++];if(hi<lo){double t=lo;lo=hi;hi=t;}((RangeSetting)s).set(new RangeSetting.Value(lo,hi));}
            else if(s instanceof ColorSetting)((ColorSetting)s).set(new ColorSetting.Value((float)values[cursor++],(float)values[cursor++],(float)values[cursor++]));
        }
    }
    public static boolean visible(int index,int rowIndex){
        int cursor=0;
        for(Setting<?> setting:modules[index].getSettings()) {
            int width=setting instanceof RangeSetting?2:1;
            if(rowIndex>=cursor && rowIndex<cursor+width)return modules[index].settingVisible(setting.getKey());
            cursor+=width;
        }
        throw new IllegalArgumentException("setting row out of range "+rowIndex);
    }





    public static String status(int index){return modules[index]==null?"":modules[index].statusText();}
    public static void disableFeature(Module module){if(context!=null)module.setEnabledInternal(false,context);}
    public static void cleanupScaffold() {
        restoreDisplace();
        if(context!=null&&modules!=null)for(Module module:modules)if(module!=null)module.onWorldUnload(context);
    }
    // Motion positions retain double precision. cameraX/Y/Z carry coordinates only for
    // these dedicated motion event types; active carries the packet's grounded flag.

    public static void input(Object input) {
        if(context!=null&&context.ready()&&context.player().movementInput==input&&modules!=null)
            ((dev.rice.module.impl.ScaffoldModule)modules[4]).movementInput(context);
    }
    public static float[] event(int index,int type,boolean active,Object packet,float[] values) {
        if(type==3)dev.rice.module.impl.pearl.RescueItemUse.outgoing((Packet<?>)packet);
        Module module=modules[index];
        if(module==null)return values;
        if(type!=2&&type!=3&&type!=13){
            Object world=context.world(),player=context.player();
            if(lastWorld!=world||lastPlayer!=player){
                restoreDisplace();
                if(lastWorld!=null)for(Module previous:modules)if(previous!=null)previous.onWorldUnload(context);
                lastWorld=world;lastPlayer=player;
            }
        }
        if(index==0&&(type==26||(type==5&&!active)))restoreDisplace();
        if(type==5){module.setEnabledInternal(active,context);if(index==3){manager.applyPendingChanges();if(active&&!module.isEnabled())values[12]=-1;}return values;}
        if(type==26){module.onWorldUnload(context);return values;}
        if(index==4&&type==18){module.onEvent(context,new SemanticEvent(18));return values;}
        if(!module.isEnabled())return values;
        if(index==3&&type==18){module.onClientTick(context);manager.applyPendingChanges();if(!module.isEnabled())values[12]=-1;return values;}
        // The other client's menu owns currentScreen. This client's native menu
        // is an overlay, so explicitly supply the same pause boundary to Bow.
        if(index==0&&(guiOpen()||!context.inputReady()||context.minecraft().currentScreen!=null)){restoreDisplace();module.onWorldUnload(context);return values;}
        if(index==1&&guiOpen()){
            if(type==17)module.onWorldUnload(context);
            return values;
        }
        if(type==16){RotationService.beginFrame(context,0,0);module.onClientTickStart(context);return values;}
        SemanticEvent e=new SemanticEvent(type);
        e.active=active;e.packet=(Packet<?>)packet;
        e.cancelled=values[0]!=0;e.dispatchMode=(int)values[1];e.yaw=values[2];e.pitch=values[3];
        e.remapMovement=values[4]!=0;e.reducing=values[5]!=0;e.sideways=values[6];e.forward=values[7];
        e.timerRenderPartialTicks=values[8];if(type==21||type==22)e.factor=values[8];e.cameraX=values[9];e.cameraY=values[10];e.cameraZ=values[11];
        if(index==0&&type==18&&context.ready())myau.RotationState.update(false,0,0,context.player().rotationYaw,0);
        float beforeYaw=index==0&&type==14?context.player().rotationYaw:0;
        float beforePitch=index==0&&type==14?context.player().rotationPitch:0;
        if(module.listens(type))module.onEvent(context,e);
        if(index==0&&type==14&&e.cancelled&&displacePlayer==null&&context.player().rotationYaw!=beforeYaw){
            displacePlayer=context.player();displaceYaw=beforeYaw;displacePitch=beforePitch;
            displaceWrittenYaw=displacePlayer.rotationYaw;displaceWrittenPitch=displacePlayer.rotationPitch;
        }
        if(index==0&&type==18)displacePlayer=null;
        values[0]=e.cancelled?1:0;values[1]=e.dispatchMode;values[2]=e.yaw;values[3]=e.pitch;
        values[4]=e.remapMovement?1:0;values[6]=e.sideways;values[7]=e.forward;if(type==21||type==22)values[8]=e.factor;values[12]=!module.isEnabled()?-1:e.overrideRotation?1:0;
        return values;
    }
    public static void rescuePostMotion(Object player) {
        if(context==null||!context.ready()||context.player()!=player||modules==null)return;
        dev.rice.module.impl.pearl.RescueServices.serverRotations[0]=context.player().rotationYaw;
        dev.rice.module.impl.pearl.RescueServices.serverRotations[1]=context.player().rotationPitch;
        for(int i=6;i<modules.length;i++)if(modules[i] instanceof dev.rice.module.impl.pearl.PearlRescueModule)
            ((dev.rice.module.impl.pearl.PearlRescueModule)modules[i]).postMotion();
        if(modules[8].isEnabled())((dev.rice.module.impl.pearl.BridgeEgg)modules[8]).postMotion();
    }
}

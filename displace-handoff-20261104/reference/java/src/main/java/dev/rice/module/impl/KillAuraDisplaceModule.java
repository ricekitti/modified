package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.combat.openb4.B4AuraHost;
import dev.rice.client.combat.ravenbs.RavenBsDisplaceBackend;
import dev.rice.client.combat.ravenbs.RavenBsRotations;
import dev.rice.client.rotation.RotationService;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ChoiceSetting;
import dev.rice.setting.NumberSetting;
import net.minecraft.entity.Entity;

/** Rice lifecycle adapter for the supplied Raven bS Displace state machine. */
public final class KillAuraDisplaceModule extends Module {
    public static KillAuraDisplaceModule instance;
    private final RavenBsDisplaceBackend backend;
    private boolean published;
    private float yaw,pitch;
    // Retain binary/source contracts for the archived OpenB4 backend. The bS path never reads these.
    public static B4AuraHost.SliderSetting modeSetting,angle,cooldown;
    public static B4AuraHost.ButtonSetting hideRotations,onlyKnockbackItems,overrideAttacks,playersOnly,renderArrow;
    public enum Mode { Left, Right, Random, Void }
    public static Mode mode = Mode.Left;
    public static boolean queued,holding;
    public static long lastAt;
    public KillAuraDisplaceModule() {
        super("killauradisplace","KillAura Displace","Raven bS displacement, attack override and void direction selection.",ModuleCategory.COMBAT);
        instance=this;
        modeSetting=new B4AuraHost.SliderSetting(add(new ChoiceSetting("mode","Mode",0,"Offset","Void")));
        angle=new B4AuraHost.SliderSetting(add(new NumberSetting("angle","Yaw offset",90,0,180,1)));
        add(new ChoiceSetting("direction","Direction",0,"Left","Right"));
        add(new NumberSetting("void_scan_radius","Scan radius",6,1,12,.5));
        add(new NumberSetting("delay","Delay (ms)",0,0,500,50));
        add(new BooleanSetting("find_void","Find void",false));
        hideRotations=new B4AuraHost.ButtonSetting(add(new BooleanSetting("hide_rotations","Blink",false)));
        add(new BooleanSetting("void_ignore_one_block_wall","Ignore one block wall",false));
        add(new BooleanSetting("ignore_teammates","Ignore teammates",true));
        onlyKnockbackItems=new B4AuraHost.ButtonSetting(add(new BooleanSetting("only_knockback_items","Only knockback items",false)));
        overrideAttacks=new B4AuraHost.ButtonSetting(add(new BooleanSetting("override_attacks","Override attack",false)));
        renderArrow=new B4AuraHost.ButtonSetting(add(new BooleanSetting("render_arrow","Render arrow",true)));
        add(new BooleanSetting("weapon_only","Weapon only",false));
        add(new BooleanSetting("third_person_rotations","Third person rotations",true));
        // Legacy fields remain valid for archived OpenB4 checks; unsupported old controls are not shown.
        cooldown=new B4AuraHost.SliderSetting(new NumberSetting("cooldown","Cooldown",.5,.2,8,.1));
        playersOnly=new B4AuraHost.ButtonSetting(new BooleanSetting("players_only","Players only",true));
        backend=new RavenBsDisplaceBackend(this);
    }
    public RavenBsDisplaceBackend backend(){return backend;}
    @Override public int metadataCategory(){return 0;}
    @Override public String hudName(){return "Displace";}
    @Override public String statusText(){return backend.getInfo();}
    @Override public boolean settingVisible(String key){
        boolean offset=((ChoiceSetting)findSetting("mode")).index()==0;
        if(key.equals("direction")||key.equals("find_void"))return offset;
        if(key.equals("void_scan_radius")||key.equals("void_ignore_one_block_wall"))return !offset;
        return true;
    }
    @Override public boolean afterSettingControl(String key,ClientContext c){backend.settingChanged(key);return false;}
    @Override protected void onEnable(ClientContext c){published=false;backend.onEnable();}
    @Override public void onClientTickStart(ClientContext c){
        if(!c.inputReady()||c.minecraft().currentScreen!=null){reset();return;}
        backend.onGameTick(new RavenBsDisplaceBackend.GameTickEvent());
    }
    @Override public boolean listens(int type){return type==3||type==6||type==13||type==14||type==23||type==27||type==28||type==30||type==31;}
    @Override public void onEvent(ClientContext c,SemanticEvent e){
        if(!c.ready()){reset();return;}
        if(e.type==3){if(backend.holdsPackets()&&!c.minecraft().isSingleplayer())e.dispatchMode=1;return;}
        if(e.type==6){backend.onRenderWorld(new RavenBsDisplaceBackend.RenderWorldLastEvent(e.timerRenderPartialTicks));return;}
        if(!c.inputReady()||c.minecraft().currentScreen!=null){reset();return;}
        if(e.type==23){
            RavenBsDisplaceBackend.SendPacketEvent packet=new RavenBsDisplaceBackend.SendPacketEvent(e.packet);
            packet.setCanceled(e.cancelled);backend.onSendPacket(packet);backend.onArrowNudgeAttackPacket(packet);backend.onVoidDebugAttackPacket(packet);e.cancelled|=packet.isCanceled();
        }else if(e.type==14){
            if(e.active){
                RavenBsDisplaceBackend.PreAttackEvent attack=new RavenBsDisplaceBackend.PreAttackEvent(c.minecraft().objectMouseOver);
                backend.onPreAttack(attack);e.cancelled|=attack.isCanceled();
            }else{
                RavenBsDisplaceBackend.RightClickMouseEvent use=new RavenBsDisplaceBackend.RightClickMouseEvent();backend.onRightClickMouse(use);e.cancelled|=use.isCanceled();
            }
        }else if(e.type==31){
            RavenBsDisplaceBackend.AttackEvent attack=new RavenBsDisplaceBackend.AttackEvent(c.player(),e.entity);backend.onAttack(attack);e.cancelled|=attack.isCanceled();
        }else if(e.type==30){
            dev.rice.client.combat.ravenbs.RavenBsKillAuraBackend aura=dev.rice.client.combat.ravenbs.RavenBsKillAuraBackend.instance;
            if(aura==null||!aura.enabled){
                float realYaw=c.player().rotationYaw,realPitch=c.player().rotationPitch;
                float[] rotations=applyRotations(c,null);
                if(rotations!=null&&published){RotationService.retain(c,false,realYaw,realPitch);c.player().rotationYaw=rotations[0];c.player().rotationPitch=rotations[1];}
            }
        }else if(e.type==28){backend.onPrePlayerInteract(new RavenBsDisplaceBackend.PrePlayerInteractEvent());
        }else if(e.type==27){
            fixInput(c);
            backend.onPostInput(new RavenBsDisplaceBackend.PostPlayerInputEvent());
            e.forward=c.player().movementInput.moveForward;e.sideways=c.player().movementInput.moveStrafe;
        }else if(e.type==13&&published){e.active=true;e.overrideRotation=false;}
    }
    /** Runs after KillAura LOW-priority rotation selection, matching bS Displace LOWEST. */
    public static float[] applyRotations(ClientContext c,float[] base){
        KillAuraDisplaceModule self=instance;
        if(self==null||!self.isEnabled())return base;
        self.published=false;
        if(!c.inputReady()||c.minecraft().currentScreen!=null){reset();return base;}
        Float baseYaw=base==null?null:base[0],basePitch=base==null?null:base[1];
        RavenBsDisplaceBackend.ClientRotationEvent e=new RavenBsDisplaceBackend.ClientRotationEvent(baseYaw,basePitch);
        self.backend.onClientRotation(e);
        boolean changed=e.yaw!=null&&(baseYaw==null||Float.compare(e.yaw,baseYaw)!=0)||e.pitch!=null&&(basePitch==null||Float.compare(e.pitch,basePitch)!=0);
        if((changed||self.backend.forceMovementFix&&e.yaw!=null)&&RotationService.claim(600)){
            float requestedYaw=e.yaw==null?RavenBsRotations.serverRotations[0]:e.yaw;
            float requestedPitch=e.pitch==null?RavenBsRotations.serverRotations[1]:e.pitch;
            float[] fixed=RavenBsRotations.fixRotation(requestedYaw,requestedPitch,RavenBsRotations.serverRotations[0],RavenBsRotations.serverRotations[1]);
            self.yaw=fixed[0];self.pitch=fixed[1];
            self.published=true;
            reapplyMovementState(c);
            return new float[]{self.yaw,self.pitch};
        }
        return base;
    }
    /** Called after the shared Mayu rotation hook, which refreshes RotationState each player update. */
    public static void reapplyMovementState(ClientContext c){
        KillAuraDisplaceModule self=instance;
        if(self==null||!self.isEnabled()||!self.published||!self.backend.forceMovementFix||!c.ready()||!RotationService.claim(600))return;
        float cameraYaw=RotationService.storedRotationValid()?RotationService.cameraYaw():c.player().rotationYaw;
        float cameraPitch=RotationService.storedRotationValid()?RotationService.cameraPitch():c.player().rotationPitch;
        myau.RotationState.update(true,showThirdPersonRotations()?self.yaw:cameraYaw,showThirdPersonRotations()?self.pitch:cameraPitch,self.yaw,3);
    }
    /** bS RotationHelper HIGHEST input correction, before Displace LOWEST compensation. */
    private void fixInput(ClientContext c){
        if(!published||!backend.forceMovementFix)return;
        float forward=c.player().movementInput.moveForward,strafe=c.player().movementInput.moveStrafe;
        if(forward==0&&strafe==0)return;
        float sneak=c.player().movementInput.sneak?.3F:1F;
        float camera=RotationService.storedRotationValid()?RotationService.cameraYaw():c.player().rotationYaw;
        double angle=Math.toDegrees(direction(camera,forward,strafe));
        float closestForward=0,closestStrafe=0,closestDifference=Float.MAX_VALUE;
        for(float pf=-1;pf<=1;pf++)for(float ps=-1;ps<=1;ps++)if(pf!=0||ps!=0){
            float predictedForward=pf*sneak,predictedStrafe=ps*sneak;
            double predictedAngle=Math.toDegrees(direction(yaw,predictedForward,predictedStrafe));
            double difference=Math.abs(net.minecraft.util.MathHelper.wrapAngleTo180_double(angle-predictedAngle));
            if(difference<closestDifference){closestDifference=(float)difference;closestForward=predictedForward;closestStrafe=predictedStrafe;}
        }
        c.player().movementInput.moveForward=closestForward;c.player().movementInput.moveStrafe=closestStrafe;
    }
    private static double direction(float rotationYaw,double forward,double strafe){
        if(forward<0)rotationYaw+=180;
        float factor=forward<0?-.5F:forward>0?.5F:1F;
        if(strafe>0)rotationYaw-=90*factor;if(strafe<0)rotationYaw+=90*factor;
        return Math.toRadians(rotationYaw);
    }
    public static boolean displacedRotation(){return instance!=null&&instance.published;}
    public static boolean forceMovementFix(){return instance!=null&&instance.isEnabled()&&instance.backend.forceMovementFix;}
    public static boolean showThirdPersonRotations(){return instance!=null&&instance.isEnabled()&&((BooleanSetting)instance.findSetting("third_person_rotations")).isEnabled();}
    @Override protected void onDisable(ClientContext c){reset();}
    @Override public void onWorldUnload(ClientContext c){reset();}
    public static void reset(){
        if(instance!=null){
            net.minecraft.client.Minecraft mc=net.minecraft.client.Minecraft.getMinecraft();
            if(instance.published&&mc.thePlayer!=null&&myau.RotationState.isActive()
                    &&myau.RotationState.movementPriority()==3&&Float.compare(myau.RotationState.movementYaw(),instance.yaw)==0)
                myau.RotationState.update(false,mc.thePlayer.rotationYaw,mc.thePlayer.rotationPitch,mc.thePlayer.rotationYaw,0);
            instance.backend.onDisable();instance.published=false;
        }
        queued=false;holding=false;lastAt=0;
    }
    /** Archived OpenB4 hooks, kept for linkage only. The registered aura is Raven bS. */
    public static boolean active(){return instance!=null&&instance.isEnabled();}
    public static void preUpdate(){}
    public static void postMotion(){queued=false;}
    public static void trigger(){queued=true;lastAt=System.currentTimeMillis();}
    public static void rearm(){}
    public static void clear(){lastAt=0;}
    public static void guiUpdate(){mode=(int)modeSetting.getInput()==1?Mode.Void:Mode.Left;}
    public static void startBlink(){}
    public static float offset(float baseYaw){return 0;}
    public static boolean canDisplace(Entity target,boolean hurt,boolean delay){return false;}
}

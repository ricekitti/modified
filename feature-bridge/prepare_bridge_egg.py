"""Bridge Egg adaptation. Preserve recovered attempt/aim logic and name the support-scan adaptation."""
from pathlib import Path
import re,hashlib,json
from prepare_pearl import body

def prepare(root,adapted):
    ref=root.parent/'valen-selected-ref-20261105/extracted/valen-selected-src/src/main/java/valen'
    out=adapted/'pearl';out.mkdir(exist_ok=True)
    text=(ref/'module/impl/player/BridgeEgg.java').read_text();text=text[text.index('package valen.'):]
    text=re.sub(r'^import valen\..*;\n','',text,flags=re.M)
    text=text.replace('package valen.module.impl.player;', '''package dev.rice.module.impl.pearl;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.NativeFeatureBridge;
import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.NumberSetting;
import dev.rice.module.impl.fireball.TimerController;
import net.minecraft.client.Minecraft;''')
    text=text.replace('extends Module {','extends Module implements RescueServices.BridgeEggQuery {\n    private static final Minecraft mc=Minecraft.getMinecraft();')
    text=text.replace('super("Bridge Egg Clutch", Module.category.player);','super("bridge_egg_clutch","Bridge Egg Clutch","Recovered bridge egg rescue",ModuleCategory.UTILITY);')
    text=re.sub(r'    @SubscribeEvent[^\n]*\n','',text)
    text=text.replace('SliderSetting','NumberSetting').replace('ButtonSetting','BooleanSetting')
    def setting(m):
        kind,label,tail=m.groups();tail=re.sub(r'^, "[^\"]*"','',tail) if kind=='NumberSetting' else tail
        return 'new '+kind+'("'+label.lower().replace(' ','_')+'", "'+label+'"'+tail+')'
    text=re.sub(r'new (NumberSetting|BooleanSetting)\("([^\"]+)"([^;]*?)\)',setting,text)
    text=text.replace('this.registerSetting(','this.add(')
    text=re.sub(r'^\s*this\.[A-Za-z]+\.visible = [^;]+;\n','\n',text,flags=re.M)
    start=text.index('    @Override\n    public void guiUpdate()');end=text.index('    public static boolean',start)
    text=text[:start]+'''    @Override public boolean settingVisible(String key){
        if(key.equals("time"))return inVoidFor.isEnabled();
        if(key.equals("pre_timer_speed")||key.equals("after_timer_speed"))return !useStasis.isEnabled();
        return true;
    }
    public boolean listens(int type){return type==1||type==2||type==13||type==18;}
    public void onEvent(ClientContext c,SemanticEvent e){
        if(NativeFeatureBridge.guiOpen()||NativeFeatureBridge.timerBlocked()){reset();return;}
        if(e.type==13)onRotation(e);else if(e.type==2)onPacket(e);else refresh();
    }
    public void postMotion(){if(isEnabled()&&!NativeFeatureBridge.guiOpen())onPostMotion();}
    public float[] motionRotation(){if(!isEnabled()||NativeFeatureBridge.guiOpen()||NativeFeatureBridge.timerBlocked())return null;refresh();return attempt.state()==RescueAttempt.State.AIMING&&eligible()?new float[]{yaw,aimPitch}:null;}
    public void onWorldUnload(ClientContext c){reset();}
''' +text[end:]
    text=text.replace('public void onEnable()','protected void onEnable(ClientContext c)').replace('public void onDisable()','protected void onDisable(ClientContext c)')
    text=text.replace('public void onRotation(ClientRotationEvent clientRotationEvent)','public void onRotation(SemanticEvent clientRotationEvent)')
    text=text.replace('public void onPostMotion(PostMotionEvent postMotionEvent)','public void onPostMotion()')
    text=text.replace('TickEvent.RenderTickEvent renderTickEvent','').replace('TickEvent.ClientTickEvent clientTickEvent','')
    text=text.replace('ReceivePacketEvent receivePacketEvent','SemanticEvent receivePacketEvent').replace('receivePacketEvent.getPacket()','receivePacketEvent.packet')
    text=text.replace('RotationHelper.get().forceMovementFix = true;','clientRotationEvent.remapMovement=true;clientRotationEvent.overrideRotation=true;')
    text=re.sub(r'clientRotationEvent.setYaw\(Float.valueOf\(([^)]+)\)\);',r'clientRotationEvent.yaw=\1;',text)
    text=re.sub(r'clientRotationEvent.setPitch\(Float.valueOf\(([^)]+)\)\);',r'clientRotationEvent.pitch=\1;',text)
    text=text.replace('.isToggled()','.isEnabled()').replace('.getInput()','.doubleValue()')
    text=text.replace('Valen.timerController','RescueServices.timer()').replace('Utils.nullCheck()','RescueServices.ready()').replace('ModuleManager.isBedMining()','RescueServices.bedMining()')
    text=text.replace('''        if (ModuleManager.autoPearl != null) {
            if (ModuleManager.autoPearl.isRescueActive()) return false;
        }
        if (ModuleManager.fireballPearlClutch != null) {
            if (ModuleManager.fireballPearlClutch.isRescueActive()) return false;
        }''','''        if(RescueServices.pearlActive(false)||RescueServices.pearlActive(true))return false;''')
    text=text.replace('if (ModuleManager.clutch == null) return true;\n        if (ModuleManager.clutch.canSave()) return false;','if (RescueServices.clutchCanSave()) return false;')
    text=text.replace('ModuleManager.fireballPearlClutch != null && ModuleManager.fireballPearlClutch.isRescueActive()','RescueServices.pearlActive(true)').replace('ModuleManager.autoPearl != null && ModuleManager.autoPearl.isRescueActive()','RescueServices.pearlActive(false)')
    text=text.replace('RotationUtils.serverRotations','RescueServices.serverRotations').replace('Trajectories.getThrowRotations','TrajectoryAim.getThrowRotations').replace('VoidProximity.isUnsupported','SupportScan.isUnsupported')
    text=text.replace('public void onPlayerMove(PlayerMoveEvent playerMoveEvent)','public boolean cancelMove()').replace('playerMoveEvent.setCanceled(true);','return true;')
    text=text.replace('''            return true;
        }
    }

    private void releaseStasis''','''            return true;
        }
        return false;
    }

    private void releaseStasis''')
    text=text.rsplit('}',1)[0]+'''    private static final class SupportUpdate {
        final BlockPos pos;final net.minecraft.block.state.IBlockState state;
        SupportUpdate(BlockPos p,net.minecraft.block.state.IBlockState s){pos=p;state=s;}
    }
}
'''
    text=text.replace('    private final BooleanSetting waitForClutch', '    private final BooleanSetting waitForFireballPearl = new BooleanSetting("wait_for_fireball_pearl_clutch", "Wait for Fireball Pearl Clutch", true);\n    private final BooleanSetting waitForClutch')
    text=text.replace('this.add(this.waitForClutch);','this.add(this.waitForClutch);\n        this.add(this.waitForFireballPearl);')
    text=text.replace('if(RescueServices.pearlActive(false)||RescueServices.pearlActive(true))return false;', 'if(RescueServices.pearlActive(false)||RescueServices.pearlActive(true)||waitForFireballPearl.isEnabled()&&RescueServices.fireballPearlHasPriority())return false;')
    text=text.replace('        if (this.attempt.active()) {\n            return true;', '        if(waitForFireballPearl.isEnabled()&&RescueServices.fireballPearlHasPriority())return false;\n        if (this.attempt.active()) {\n            return true;')
    paths=[]
    for name,data in [('BridgeEgg.java',text),('RescueAttempt.java',(ref/'utility/RescueAttempt.java').read_text().replace('package valen.utility;','package dev.rice.module.impl.pearl;'))]:
        p=out/name;p.write_text(data,encoding='utf-8');paths.append(p)
    # Only the recovered throw solver is needed, not the foreign rendering module.
    trajectory=Path(r'C:/Users/ethan/Documents/ChatGPT/dfdfdf/valen-module-src/src/main/java/valen/module/impl/render/Trajectories.java')
    original=trajectory.read_text()
    solver=original[original.index('    public static float[] getThrowRotations'):original.index('    private float getBowVelocity')]
    # The missing props holder is data-only: constructor constants and field reads pin all used values.
    header='''package dev.rice.module.impl.pearl;
import net.minecraft.entity.player.EntityPlayer;import net.minecraft.item.Item;import net.minecraft.init.Items;import net.minecraft.util.Vec3;
public final class TrajectoryAim {
    private enum PhysicsModel {THROWABLE,FISH_HOOK}
    private static final class TrajectoryProps {
        final double gravity,drag,initialVelocity;final boolean requiresInitialTickCorrection;
        TrajectoryProps(PhysicsModel m,double gravity,double drag,double water,double width,double height,double radius,double velocity,boolean ignore,boolean correction){this.gravity=gravity;this.drag=drag;initialVelocity=velocity;requiresInitialTickCorrection=correction;}
    }
'''
    p=out/'TrajectoryAim.java';p.write_text(header+solver.replace('Trajectories.','TrajectoryAim.')+'}\n');paths.append(p)
    manifest={'bridge_egg':hashlib.sha256((ref/'module/impl/player/BridgeEgg.java').read_bytes()).hexdigest(),'trajectory_source':str(trajectory),'trajectory_sha256':hashlib.sha256(trajectory.read_bytes()).hexdigest(),'adaptation':'VoidProximity replaced by recovered PearlRescue footprint support semantics; props/update holders reconstructed from constants and reads'}
    (out/'bridge-egg-source-hashes.json').write_text(json.dumps(manifest,indent=2)+'\n')
    return paths

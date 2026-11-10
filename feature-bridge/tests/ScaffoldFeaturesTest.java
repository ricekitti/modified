import dev.rice.mayu.*;
import dev.rice.setting.ChoiceSetting;
import dev.rice.setting.NumberSetting;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.*;
import java.lang.reflect.*;
import java.util.*;

/** Acceptance checks for every requested control around the retained Clutch planner. */
public final class ScaffoldFeaturesTest {
    static int assertions;
    static void check(boolean value,String message){assertions++;if(!value)throw new AssertionError(message);}
    static void near(double a,double b,String message){check(Math.abs(a-b)<1e-6,message+": "+a+" != "+b);}
    static Object field(Object target,String name)throws Exception{Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(target);}
    static void set(Object target,String name,Object value)throws Exception{Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);f.set(target,value);}
    public static class DelayedController extends MayuScaffoldTest.Controller{
        static int calls;
        @Override public boolean onPlayerRightClick(net.minecraft.client.entity.EntityPlayerSP p,net.minecraft.client.multiplayer.WorldClient world,net.minecraft.item.ItemStack stack,BlockPos support,EnumFacing face,Vec3 hit){
            check(MayuScaffoldTest.blocks.containsKey(support)&&hit!=null,"multi-place must use validated support");calls++;stack.stackSize--;return true;
        }
    }
    static int counterWrites;static String counterLabel;
    public static class CounterFont extends net.minecraft.client.gui.FontRenderer{
        public CounterFont(){super(null,null,null,false);}
        @Override public int getStringWidth(String label){return label.length()*6;}
        @Override public int drawString(String text,float x,float y,int color,boolean shadow){counterWrites++;counterLabel=text;return getStringWidth(text);}
    }
    public static void main(String[] args)throws Exception{
        ScaffoldModule core=MayuScaffoldTest.setup();core.setEnabled(false);
        dev.rice.module.impl.ScaffoldModule host=new dev.rice.module.impl.ScaffoldModule();
        String[] keys={"rotations","move-fix","sprint","ground-motion","air-motion","speed-motion","tower","keep-y","keep-y-on-press","multi-place","safe-walk","swing","item-spoof","block-counter"};
        check(host.getSettings().size()==keys.length,"Exactly fourteen settings");
        for(int n=0;n<keys.length;n++)check(host.getSettings().get(n).getKey().equals(keys[n]),"Exact setting key/order "+keys[n]);
        check(((ChoiceSetting)host.findSetting("rotations")).getOptions().equals(Arrays.asList("NONE","DEFAULT","BACKWARDS","SIDEWAYS")),"Four rotation choices");
        check(((ChoiceSetting)host.findSetting("move-fix")).index()==1,"SILENT default");
        for(String key:new String[]{"tower","keep-y"})check(((ChoiceSetting)host.findSetting(key)).getOptions().equals(Arrays.asList("NONE","VANILLA","EXTRA","TELLY")),key+" modes");
        for(String key:new String[]{"ground-motion","air-motion","speed-motion"}){
            NumberSetting setting=(NumberSetting)host.findSetting(key);check(setting.getMinimum()==0&&setting.getMaximum()==100&&setting.doubleValue()==100,"Percentage range/default");
        }
        check(!host.settingVisible("keep-y-on-press"),"On-press hidden for NONE");
        ((ChoiceSetting)host.findSetting("keep-y")).set(3);check(host.settingVisible("keep-y-on-press"),"On-press visible for TELLY");

        for(int mode=0;mode<4;mode++){
            core=MayuScaffoldTest.setup();core.rotations.setValue(mode);core.multiPlace.setValue(false);
            RotationUpdateEvent event=MayuScaffoldTest.update(core,90,84);
            if(mode==0)check(!event.isModified(),"NONE never claims automatic look");
            else check(event.isModified(),"Rotation mode must publish valid look");
            check(field(core,"clutchPlanner")!=null,"Every rotation mode uses Clutch planner");core.setEnabled(false);
        }
        for(int percent:new int[]{0,1,50,100})for(int state=0;state<3;state++){
            core=MayuScaffoldTest.setup();core.groundMotion.setValue(percent);core.airMotion.setValue(percent);core.speedMotion.setValue(percent);
            FeatureFixture.self.onGround=state!=1;
            Map active=new HashMap();FeatureFixture.put(FeatureFixture.self,"activePotionsMap",active);
            if(state==2)active.put(net.minecraft.potion.Potion.moveSpeed.id,new net.minecraft.potion.PotionEffect(net.minecraft.potion.Potion.moveSpeed.id,200,0));
            FeatureFixture.self.movementInput.moveForward=1;FeatureFixture.self.movementInput.moveStrafe=0;
            core.onLivingUpdate(new LivingUpdateEvent());near(FeatureFixture.self.movementInput.moveForward,percent/100.0,"Motion percent in ground/air/speed state");
            active.clear();core.setEnabled(false);
        }
        core=MayuScaffoldTest.setup();core.sprint.setValue(0);FeatureFixture.self.setSprinting(true);core.onLivingUpdate(new LivingUpdateEvent());check(!FeatureFixture.self.isSprinting(),"NONE suppresses sprint");
        core.sprint.setValue(1);FeatureFixture.self.setSprinting(true);core.onLivingUpdate(new LivingUpdateEvent());check(FeatureFixture.self.isSprinting(),"VANILLA preserves sprint");core.setEnabled(false);

        for(int mode=1;mode<=3;mode++){
            core=MayuScaffoldTest.setup();core.keepY.setValue(mode);core.keepYOnPress.setValue(true);
            FeatureFixture.self.setPosition(.5,65,.5);KeyBinding.setKeyBindState(17,true);V115Input.buttons[1]=false;
            MayuScaffoldTest.update(core,0,75);FeatureFixture.self.movementInput.jump=false;core.onMovementInput(new MovementInputEvent());check(!FeatureFixture.self.movementInput.jump,"Keep Y press gate off");
            V115Input.buttons[1]=true;MayuScaffoldTest.update(core,0,75);FeatureFixture.self.movementInput.jump=false;core.onMovementInput(new MovementInputEvent());check(FeatureFixture.self.movementInput.jump,"Keep Y mode "+mode+" auto-jump with press");
            if(mode==3)check((Integer)field(core,"placementDelayTicks")==3,"Keep Y TELLY delay");
            core.setEnabled(false);V115Input.buttons[1]=false;
        }
        core=MayuScaffoldTest.setup();core.keepY.setValue(2);set(core,"keepYState",1);set(core,"keepYLevel",65);
        FeatureFixture.self.onGround=false;FeatureFixture.self.setPosition(1.2,66.2,.5);FeatureFixture.self.motionY=-.5;
        Method recovery=core.getClass().getDeclaredMethod("handleExtraKeepYRecovery");recovery.setAccessible(true);recovery.invoke(core);check((Boolean)field(core,"keepYRecoveryPlacement"),"EXTRA Keep Y recovery");core.setEnabled(false);

        core=MayuScaffoldTest.setup();core.tower.setValue(2);FeatureFixture.self.inventory.currentItem=8;FeatureFixture.self.setPosition(.5,65,.5);KeyBinding.setKeyBindState(57,true);
        core.onMoveFlying(new MoveFlyingEvent(0,0,.1f));near(FeatureFixture.self.motionY,-.0784000015258789,"EXTRA tower settle");
        core.onMoveFlying(new MoveFlyingEvent(0,0,.1f));check((Integer)field(core,"towerPhase")!=0,"EXTRA tower edge-position cycle");core.setEnabled(false);

        for(boolean multi:new boolean[]{false,true}){
            core=MayuScaffoldTest.setup();core.multiPlace.setValue(multi);core.swing.setValue(false);
            FeatureFixture.minecraft.playerController=FeatureFixture.blank(DelayedController.class);DelayedController.calls=0;
            MayuScaffoldTest.update(core,90,84);check(DelayedController.calls==(multi?4:1),"Multi-place attempts one plus up to three extras");
            check(FeatureFixture.lastSent instanceof net.minecraft.network.play.client.C0APacketAnimation,"Swing off still sends animation packet");core.setEnabled(false);
        }
        core=MayuScaffoldTest.setup();core.safeWalk.setValue(false);FeatureFixture.self.motionX=.5;SafeWalkEvent event=new SafeWalkEvent(false);core.onSafeWalk(event);check(!event.isSafeWalk(),"Safe walk off");core.setEnabled(false);
        core=MayuScaffoldTest.setup();core.sprint.setValue(0);core.keepY.setValue(1);set(core,"keepYState",1);FeatureFixture.self.setSprinting(true);core.onLivingUpdate(new LivingUpdateEvent());check(FeatureFixture.self.isSprinting(),"Active Keep Y sprint exception");core.setEnabled(false);
        core=MayuScaffoldTest.setup();core.sprint.setValue(0);core.tower.setValue(3);FeatureFixture.self.setPosition(.5,65,.5);KeyBinding.setKeyBindState(17,true);FeatureFixture.self.setSprinting(true);KeyBinding.setKeyBindState(57,true);core.onLivingUpdate(new LivingUpdateEvent());check(FeatureFixture.self.isSprinting(),"Telly takeoff sprint exception");core.setEnabled(false);
        core=MayuScaffoldTest.setup();FeatureFixture.put(FeatureFixture.minecraft,"fontRendererObj",FeatureFixture.blank(CounterFont.class));
        FeatureFixture.self.inventory.mainInventory[10]=new net.minecraft.item.ItemStack(net.minecraft.init.Blocks.stone,100);
        FeatureFixture.self.inventory.mainInventory[1]=new net.minecraft.item.ItemStack(net.minecraft.init.Blocks.torch,10);
        FeatureFixture.resetHudGl();counterWrites=0;core.onRenderOverlay(new RenderOverlayEvent(0));check(counterWrites==1&&"76 blocks".equals(counterLabel),"Block counter counts suitable hotbar blocks only");
        core.blockCounter.setValue(false);core.onRenderOverlay(new RenderOverlayEvent(0));check(counterWrites==1,"Block counter off suppresses drawing");core.setEnabled(false);
        System.out.println("Scaffold requested features: "+assertions+" assertions passed");
    }
}

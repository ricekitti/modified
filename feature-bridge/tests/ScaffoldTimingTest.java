import dev.rice.client.*;
import dev.rice.module.NativeFeatureBridge;
import dev.rice.module.impl.ScaffoldModule;
import dev.rice.setting.*;
import java.lang.reflect.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.*;

/** Source ao/world-w timing semantics and the recovered dependent handlers. */
public final class ScaffoldTimingTest {
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);System.out.println("ok   "+message);}
    static Object call(Object object,String name,Class<?>[] types,Object... values)throws Exception{Method method=object.getClass().getDeclaredMethod(name,types);method.setAccessible(true);return method.invoke(object,values);}
    static void select(ScaffoldModule module,String key,int value){((ChoiceSetting)module.findSetting(key)).set(value);}
    public static void main(String[] args)throws Exception{
        Minecraft mc=FeatureFixture.create();ClientContext c=new ClientContext(mc);ScaffoldModule module=new ScaffoldModule();
        mc.gameSettings.keyBindSneak=new KeyBinding("timing.sneak",42,"test");mc.gameSettings.keyBindJump=new KeyBinding("timing.jump",57,"test");mc.gameSettings.keyBindSprint=new KeyBinding("timing.sprint",29,"test");
        Field ctx=NativeFeatureBridge.class.getDeclaredField("context");ctx.setAccessible(true);ctx.set(null,c);
        Field modules=NativeFeatureBridge.class.getDeclaredField("modules");modules.setAccessible(true);modules.set(null,new dev.rice.module.Module[]{module,module,module,module,module});
        ScaffoldSlotFixtureBridge.use(NativeFeatureBridge.class);
        Field driverField=ScaffoldModule.class.getDeclaredField("driver");driverField.setAccessible(true);Object driver=driverField.get(module);
        Field field=driver.getClass().getDeclaredField("timing");field.setAccessible(true);Object timing=field.get(driver);
        Class<?>[] ctxType={ClientContext.class};
        Field guardField=driver.getClass().getDeclaredField("packets");guardField.setAccessible(true);Object guard=guardField.get(driver);
        check(!(Boolean)call(guard,"placementBlocked",ctxType,c),"Packet-order guard starts clear with Scaffold disabled");
        NativeFeatureBridge.packetBegin(new C02PacketUseEntity(FeatureFixture.enemy,C02PacketUseEntity.Action.ATTACK),true);
        check((Boolean)call(guard,"placementBlocked",ctxType,c),"Global early packet observer blocks placement after entity use");
        NativeFeatureBridge.packetBegin(new C03PacketPlayer(),false);
        check((Boolean)call(guard,"placementBlocked",ctxType,c),"Inbound packets do not reset outbound ordering history");
        NativeFeatureBridge.packetBegin(new C03PacketPlayer.C05PacketPlayerLook(0,0,true),true);
        check(!(Boolean)call(guard,"placementBlocked",ctxType,c),"Every outbound movement packet subclass resets the source guard");
        NativeFeatureBridge.packetBegin(new C08PacketPlayerBlockPlacement((ItemStack)null),true);
        check(!(Boolean)call(guard,"placementBlocked",ctxType,c),"Placement and use-item packets do not imitate the parent's attack/inventory guard flags");
        NativeFeatureBridge.packetBegin(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT),true);
        check((Boolean)call(guard,"placementBlocked",ctxType,c),"Opening inventory marks the source inventory packet guard");
        NativeFeatureBridge.packetBegin(new C03PacketPlayer(),true);NativeFeatureBridge.packetBegin(new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN),true);
        check(!(Boolean)call(guard,"placementBlocked",ctxType,c),"Other client-status actions leave the inventory flag clear");
        NativeFeatureBridge.packetBegin(new C0DPacketCloseWindow(0),true);check((Boolean)call(guard,"placementBlocked",ctxType,c),"Closing a window blocks until the next movement packet");
        NativeFeatureBridge.packetBegin(new C03PacketPlayer(),true);NativeFeatureBridge.packetBegin(new C0EPacketClickWindow(),true);
        check((Boolean)call(guard,"placementBlocked",ctxType,c),"Click-window packets participate in source inventory ordering");
        NativeFeatureBridge.packetBegin(new C03PacketPlayer(),true);
        check((Integer)call(timing,"placementAge",ctxType,c)==Integer.MAX_VALUE,"Unobserved timing history remains unknown instead of inventing a recent placement");
        FeatureFixture.self.ticksExisted=100;
        NativeFeatureBridge.timingPacket(new C08PacketPlayerBlockPlacement(new BlockPos(0,64,0),1,null,0,0,0),true);
        check((Integer)call(timing,"placementAge",ctxType,c)==0&&!module.isEnabled(),"Global packet timing works while Scaffold is disabled");
        FeatureFixture.self.ticksExisted=105;NativeFeatureBridge.timingPacket(new C08PacketPlayerBlockPlacement((ItemStack)null),true);
        check((Integer)call(timing,"placementAge",ctxType,c)==5,"Use-item sentinel does not reset placement timing");
        NativeFeatureBridge.timingPacket(new S08PacketPlayerPosLook(),true);
        check((Integer)call(timing,"correctionAge",ctxType,c)==Integer.MAX_VALUE,"Outbound packet does not imitate an inbound correction");
        NativeFeatureBridge.timingPacket(new S08PacketPlayerPosLook(),false);
        FeatureFixture.self.ticksExisted=108;
        check((Integer)call(timing,"correctionAge",ctxType,c)==3,"Correction timing advances with actual entity ticks");
        NativeFeatureBridge.timingPacket(new S12PacketEntityVelocity(2,.3,.2,0),false);
        check((Integer)call(timing,"velocityAge",ctxType,c)==Integer.MAX_VALUE,"Other entity velocity does not reset local timing");
        NativeFeatureBridge.timingPacket(new S12PacketEntityVelocity(FeatureFixture.self.getEntityId(),.3,.2,0),false);
        check((Integer)call(timing,"velocityAge",ctxType,c)==0&&(Integer)call(timing,"significantVelocityAge",ctxType,c)==0,"Significant local velocity resets both source counters");
        FeatureFixture.self.ticksExisted=110;NativeFeatureBridge.timingPacket(new S12PacketEntityVelocity(FeatureFixture.self.getEntityId(),.1,.05,0),false);
        check((Integer)call(timing,"velocityAge",ctxType,c)==0&&(Integer)call(timing,"significantVelocityAge",ctxType,c)==2,"Small velocity preserves significant-velocity age");
        Method toggle=dev.rice.module.Module.class.getDeclaredMethod("setEnabledInternal",boolean.class,ClientContext.class);toggle.setAccessible(true);toggle.invoke(module,true,c);
        select(module,"downwards",ScaffoldModule.DOWN_VERUS_VB);FeatureFixture.self.ticksExisted=200;
        NativeFeatureBridge.timingPacket(new C08PacketPlayerBlockPlacement(new BlockPos(0,64,0),1,null,0,0,0),true);
        V115Input.keys[42]=true;KeyBinding.setKeyBindState(42,true);FeatureFixture.self.posY=65;FeatureFixture.self.motionY=-.1;
        Class<?>[] modeTypes={ClientContext.class,ScaffoldModule.class};
        Field offset=driver.getClass().getDeclaredField("placeOffset");offset.setAccessible(true);
        call(driver,"prepareDownwards",modeTypes,c,module);call(driver,"prepareOffset",modeTypes,c,module);
        check(((Vec3)offset.get(driver)).yCoord==-1,"Verus downward placement uses recent placement and physical sneak");
        FeatureFixture.self.ticksExisted=201;FeatureFixture.self.posY=65.2;call(driver,"prepareDownwards",modeTypes,c,module);
        FeatureFixture.self.posY=65;
        for(int n=0;n<14;n++){FeatureFixture.self.ticksExisted++;call(driver,"prepareDownwards",modeTypes,c,module);}
        call(driver,"prepareOffset",modeTypes,c,module);check(((Vec3)offset.get(driver)).yCoord==0,"Fractional Y resets Verus full-block delay");
        FeatureFixture.self.ticksExisted++;call(driver,"prepareDownwards",modeTypes,c,module);call(driver,"prepareOffset",modeTypes,c,module);
        check(((Vec3)offset.get(driver)).yCoord==-1,"Verus downward offset resumes after fifteen integer-Y ticks");
        FeatureFixture.self.ticksExisted=221;call(driver,"prepareDownwards",modeTypes,c,module);call(driver,"prepareOffset",modeTypes,c,module);
        check(((Vec3)offset.get(driver)).yCoord==0,"Verus gate expires after twenty ticks without placement");
        FeatureFixture.self.setSprinting(true);KeyBinding.setKeyBindState(29,true);module.onEvent(c,new SemanticEvent(21));
        check(!FeatureFixture.self.isSprinting()&&!mc.gameSettings.keyBindSprint.isKeyDown(),"Verus downward strafe disables sprint while physical sneak is held");
        select(module,"downwards",ScaffoldModule.DOWN_WATCHDOG_VC);call(driver,"prepareDownwards",modeTypes,c,module);
        FeatureFixture.self.posY=65;FeatureFixture.self.rotationYaw=0;KeyBinding.setKeyBindState(42,true);
        for(int n=0;n<5;n++){FeatureFixture.self.ticksExisted=296+n;call(driver,"prepareOffset",modeTypes,c,module);call(driver,"applyWatchdogDownwards",modeTypes,c,module);}
        check(((Vec3)offset.get(driver)).yCoord==0,"Watchdog downward waits for six integer-Y passes");
        KeyBinding.onTick(42);FeatureFixture.self.ticksExisted=301;call(driver,"prepareOffset",modeTypes,c,module);call(driver,"applyWatchdogDownwards",modeTypes,c,module);
        check(((Vec3)offset.get(driver)).yCoord==0,"Watchdog downward preserves source first-press modulo-three exception");
        FeatureFixture.self.ticksExisted=302;call(driver,"prepareOffset",modeTypes,c,module);call(driver,"applyWatchdogDownwards",modeTypes,c,module);
        check(((Vec3)offset.get(driver)).yCoord==-1,"Watchdog downward uses the consumed-key state on subsequent passes");
        ((BooleanSetting)module.findSetting("up_side_down")).set(true);call(driver,"prepareOffset",modeTypes,c,module);call(driver,"applyWatchdogDownwards",modeTypes,c,module);
        check(((Vec3)offset.get(driver)).yCoord==-1,"Watchdog downward overrides the earlier upward offset in source order");
        ((BooleanSetting)module.findSetting("up_side_down")).set(false);FeatureFixture.self.rotationYaw=45;
        call(driver,"prepareOffset",modeTypes,c,module);call(driver,"applyWatchdogDownwards",modeTypes,c,module);
        check(((Vec3)offset.get(driver)).yCoord==0,"Watchdog downward rejects non-axis-aligned yaw");
        select(module,"downwards",ScaffoldModule.DOWN_OFF);V115Input.keys[42]=false;KeyBinding.setKeyBindState(42,false);
        select(module,"sprint",ScaffoldModule.SPRINT_WATCHDOG_VN);FeatureFixture.self.onGround=true;FeatureFixture.self.moveForward=1;FeatureFixture.self.rotationYaw=0;FeatureFixture.self.ticksExisted=400;
        module.onEvent(c,new SemanticEvent(21));
        check(FeatureFixture.self.motionZ>=.0829&&FeatureFixture.self.motionZ<=.083&&FeatureFixture.self.isSprinting()&&mc.gameSettings.keyBindSprint.isKeyDown(),"Watchdog Sprint uses nominal source speed and owns the sprint key");
        NativeFeatureBridge.timingPacket(new S08PacketPlayerPosLook(),false);module.onEvent(c,new SemanticEvent(21));
        check(FeatureFixture.self.motionZ>=-.0171&&FeatureFixture.self.motionZ<=-.017,"Watchdog Sprint retains source signed correction penalty");
        FeatureFixture.self.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.potion.Potion.moveSpeed.id,100,0));module.onEvent(c,new SemanticEvent(21));
        check(FeatureFixture.self.motionZ>=.0179&&FeatureFixture.self.motionZ<=.018,"Watchdog Sprint potion threshold uses source correction-speed branch");
        FeatureFixture.self.removePotionEffect(net.minecraft.potion.Potion.moveSpeed.id);FeatureFixture.self.onGround=false;FeatureFixture.self.motionX=0;FeatureFixture.self.motionZ=.4;module.onEvent(c,new SemanticEvent(21));
        check(Math.abs(FeatureFixture.self.motionZ-.083)<1e-10,"Watchdog Sprint air drag follows source additive motion");
        KeyBinding.setKeyBindState(57,true);FeatureFixture.self.motionZ=.4;module.onEvent(c,new SemanticEvent(21));
        check(FeatureFixture.self.motionZ==.4,"Watchdog Sprint held jump suppresses additive drag");
        select(module,"sprint",ScaffoldModule.SPRINT_NORMAL);module.onEvent(c,new SemanticEvent(32));
        check(!mc.gameSettings.keyBindSprint.isKeyDown(),"Leaving Watchdog Sprint releases its key claim");
        module.onWorldUnload(c);check((Integer)call(timing,"correctionAge",ctxType,c)==Integer.MAX_VALUE,"World unload removes timing history");
        System.out.println("ALL SCAFFOLD TIMING CHECKS PASSED");
    }
}

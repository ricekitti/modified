package dev.rice.module.impl.pearl;

import dev.rice.client.MinecraftFields;
import dev.rice.client.RelationshipService;
import dev.rice.inject.ReflectionHelper;
import dev.rice.module.NativeFeatureBridge;
import dev.rice.module.impl.fireball.TimerController;
import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

/** Host boundaries for the recovered rescue logic; no foreign module manager/startup is loaded. */
public final class RescueServices {
    private static final TimerController TIMER = new TimerController(speed -> {
        Minecraft mc=Minecraft.getMinecraft();
        if(mc!=null) MinecraftFields.timer(mc).timerSpeed=(float)speed;
    });
    public static final float[] serverRotations=new float[2];
    public static PearlRescueModule autoPearl, fireballPearl;
    public static TimerController timer(){return TIMER;}
    public static boolean ready(){Minecraft mc=Minecraft.getMinecraft();return mc!=null&&mc.thePlayer!=null&&mc.theWorld!=null;}
    public static boolean pearlActive(boolean fireball){PearlRescueModule m=fireball?fireballPearl:autoPearl;return m!=null&&m.isRescueActive();}
    public static boolean fireballPearlHasPriority(){return fireballPearl!=null&&fireballPearl.shouldPrecedeBridgeEgg();}
    public static boolean clutchCanSave(){return NativeFeatureBridge.rescueHost(0,0)!=0;}
    public static boolean bedMining(){
        Minecraft mc=Minecraft.getMinecraft();
        if(!ready()||mc.playerController==null||mc.objectMouseOver==null||mc.objectMouseOver.typeOfHit!=net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK)return false;
        if(!(mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock() instanceof net.minecraft.block.BlockBed))return false;
        try{return ReflectionHelper.findField(net.minecraft.client.multiplayer.PlayerControllerMP.class,"isHittingBlock").getBoolean(mc.playerController);}catch(IllegalAccessException e){throw new IllegalStateException(e);}
    }
    public interface BridgeEggQuery {boolean canSave();}
    public static BridgeEggQuery bridgeEgg;
    public static boolean bridgeEggCanSave(){return bridgeEgg!=null&&bridgeEgg.canSave();}
    public static boolean friendly(Entity entity){return (RelationshipService.flags(entity)&5)!=0;}
    public static boolean bot(Entity entity){return (RelationshipService.flags(entity)&2)!=0;}
    public static void recordLaunch(float yaw,float pitch){NativeFeatureBridge.rescueHost(3,0);}
    public static float itemSlowdown(EntityPlayer player){return (float)NativeFeatureBridge.rescueHost(4,player.getHeldItem()==null?0:net.minecraft.item.Item.getIdFromItem(player.getHeldItem().getItem()));}
    // Pin the declaring class: obfuscated subclasses can shadow the mapped field name with another type.
    public static int intField(Object value,String name){try{return ReflectionHelper.findField(name.equals("jumpTicks")?net.minecraft.entity.EntityLivingBase.class:Entity.class,name).getInt(value);}catch(IllegalAccessException e){throw new IllegalStateException(e);}}
    public static boolean web(Object value){try{return ReflectionHelper.findField(Entity.class,"isInWeb").getBoolean(value);}catch(IllegalAccessException e){throw new IllegalStateException(e);}}
}

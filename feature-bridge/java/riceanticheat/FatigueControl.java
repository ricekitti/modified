package dev.rice.anticheat;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1EPacketRemoveEntityEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/** Snowify's fatigue multiplier and scoped Hold behavior; other packets and effects pass through. */
public final class FatigueControl {
    private static EntityPlayer owner;
    private static Object world;
    private static int amplifier;
    private static long endAt;
    private static boolean held;
    public static float factor(int amplifier){return amplifier==0?.3f:amplifier==1?.09f:amplifier==2?.0027f:.00081f;}
    public static float multiplier(int amplifier,double percent,boolean wooden){return Math.max(1,(float)(percent/100)*(wooden?.71999997f:1)/factor(amplifier));}
    public static double speed(Minecraft mc,boolean enabled,int mode,double percent){
        if(!enabled||mode!=2||mc.thePlayer==null||mc.thePlayer.capabilities.isCreativeMode)return Double.NaN;
        PotionEffect effect=mc.thePlayer.getActivePotionEffect(Potion.digSlowdown);if(effect==null)return Double.NaN;
        ItemStack stack=mc.thePlayer.getHeldItem();boolean wooden=stack!=null&&(stack.getItem()==Items.wooden_pickaxe||stack.getItem()==Items.wooden_axe);
        return multiplier(effect.getAmplifier(),percent,wooden);
    }
    public static synchronized boolean packet(Minecraft mc,Packet<?> packet,boolean enabled,int mode){
        if(!enabled||mode!=1||mc.thePlayer==null)return false;
        if(packet instanceof S1DPacketEntityEffect){
            S1DPacketEntityEffect effect=(S1DPacketEntityEffect)packet;
            if(effect.getEntityId()!=mc.thePlayer.getEntityId()||effect.getEffectId()!=Potion.digSlowdown.id)return false;
            owner=mc.thePlayer;world=mc.theWorld;amplifier=effect.getAmplifier();endAt=Math.max(endAt,System.currentTimeMillis()+effect.getDuration()*50L);held=true;return true;
        }
        if(packet instanceof S1EPacketRemoveEntityEffect){
            S1EPacketRemoveEntityEffect effect=(S1EPacketRemoveEntityEffect)packet;
            if(effect.getEntityId()==mc.thePlayer.getEntityId()&&effect.getEffectId()==Potion.digSlowdown.id)clear();
        }
        return false;
    }
    public static synchronized void tick(Minecraft mc,boolean enabled,int mode){
        if(!held)return;
        if(owner!=mc.thePlayer||world!=mc.theWorld){clear();return;}
        if(!enabled||mode!=1){release(mc);return;}
        if(System.currentTimeMillis()>endAt+5000)clear();
    }
    public static synchronized void release(Minecraft mc){
        if(held&&owner==mc.thePlayer&&world==mc.theWorld){int remaining=(int)Math.max(0,(endAt-System.currentTimeMillis()+49)/50);if(remaining>0)mc.thePlayer.addPotionEffect(new PotionEffect(Potion.digSlowdown.id,remaining,amplifier));}
        clear();
    }
    public static synchronized void clear(){owner=null;world=null;endAt=0;amplifier=0;held=false;}
    private FatigueControl(){}
}

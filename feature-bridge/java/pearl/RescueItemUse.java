package dev.rice.module.impl.pearl;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

/** Source use/restore sequence, with outgoing packet observation instead of a Forge event listener. */
public final class RescueItemUse {
    private static final ThreadLocal<Item> watching=new ThreadLocal<>();
    private static final ThreadLocal<Boolean> sent=new ThreadLocal<>();
    public static void outgoing(Packet<?> packet){
        if(packet instanceof C08PacketPlayerBlockPlacement){ItemStack s=((C08PacketPlayerBlockPlacement)packet).getStack();if(s!=null&&s.getItem()==watching.get())sent.set(true);}
    }
    public static boolean use(int slot){
        Minecraft mc=Minecraft.getMinecraft();
        if(!RescueServices.ready()||mc.playerController==null||slot<0||slot>8)return false;
        ItemStack stack=mc.thePlayer.inventory.mainInventory[slot];if(stack==null||stack.stackSize<=0)return false;
        Item previous=watching.get();Boolean previousSent=sent.get();watching.set(stack.getItem());sent.set(false);
        try{mc.thePlayer.inventory.currentItem=slot;mc.playerController.updateController();ItemStack held=mc.thePlayer.getHeldItem();return (held!=null&&mc.playerController.sendUseItem(mc.thePlayer,mc.theWorld,held))||Boolean.TRUE.equals(sent.get());}
        finally{if(previous==null)watching.remove();else watching.set(previous);if(previousSent==null)sent.remove();else sent.set(previousSent);}
    }
    public static void restore(int slot){Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer!=null&&mc.playerController!=null&&slot>=0&&slot<9){mc.thePlayer.inventory.currentItem=slot;mc.playerController.updateController();}}
}

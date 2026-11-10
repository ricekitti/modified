/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.NetHandlerPlayClient
 *  net.minecraft.network.play.server.S02PacketChat
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package valen.lunar.mixin;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valen.lunar.forge.client.event.ClientChatReceivedEvent;
import valen.lunar.forge.common.MinecraftForge;
import valen.lunar.mixin.ChatPacketAccess;

@Mixin(value={NetHandlerPlayClient.class}, remap=false)
public abstract class ChatEvents {
    @Inject(method={"handleChat"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V", shift=At.Shift.AFTER)}, cancellable=true, require=1)
    private void valen$lunarChat(S02PacketChat s02PacketChat, CallbackInfo callbackInfo) {
        ClientChatReceivedEvent clientChatReceivedEvent = new ClientChatReceivedEvent(s02PacketChat.getType(), s02PacketChat.getChatComponent());
        if (MinecraftForge.EVENT_BUS.post(clientChatReceivedEvent)) {
            callbackInfo.cancel();
        } else {
            ((ChatPacketAccess)s02PacketChat).valen$lunarSetMessage(clientChatReceivedEvent.message);
        }
    }
}


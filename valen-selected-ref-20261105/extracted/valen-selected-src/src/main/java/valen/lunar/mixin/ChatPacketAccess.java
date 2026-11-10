/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.play.server.S02PacketChat
 *  net.minecraft.util.IChatComponent
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package valen.lunar.mixin;

import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={S02PacketChat.class}, remap=false)
public interface ChatPacketAccess {
    @Accessor(value="chatComponent", remap=false)
    public void valen$lunarSetMessage(IChatComponent var1);
}


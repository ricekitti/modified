/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.NetworkManager
 *  net.minecraft.util.Timer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package valen.mixin.impl.accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import valen.lunar.forge.fml.relauncher.Side;
import valen.lunar.forge.fml.relauncher.SideOnly;

@SideOnly(value=Side.CLIENT)
@Mixin(value={Minecraft.class}, remap=false)
public interface IAccessorMinecraft {
    @Accessor(value="timer", remap=false)
    public Timer valen$lunar$getTimer();

    @Accessor(value="myNetworkManager", remap=false)
    public NetworkManager valen$lunar$getMyNetworkManager();

    @Accessor(value="rightClickDelayTimer", remap=false)
    public int valen$lunar$getRightClickDelayTimer();

    @Accessor(value="rightClickDelayTimer", remap=false)
    public void valen$lunar$setRightClickDelayTimer(int var1);

    @Accessor(value="leftClickCounter", remap=false)
    public void valen$lunar$setLeftClickCounter(int var1);

    @Invoker(value="rightClickMouse", remap=false)
    public void valen$lunar$callRightClickMouse();

    @Invoker(value="clickMouse", remap=false)
    public void valen$lunar$callClickMouse();

    @Invoker(value="dispatchKeypresses", remap=false)
    public void valen$lunar$invokeDispatchKeypresses();
}


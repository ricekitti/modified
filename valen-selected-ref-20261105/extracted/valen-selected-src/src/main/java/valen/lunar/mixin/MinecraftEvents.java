/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.multiplayer.WorldClient
 *  net.minecraft.util.Timer
 *  net.minecraft.world.World
 *  org.lwjgl.input.Mouse
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package valen.lunar.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.Timer;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valen.lunar.LunarRuntime;
import valen.lunar.forge.client.event.GuiOpenEvent;
import valen.lunar.forge.client.event.MouseEvent;
import valen.lunar.forge.common.MinecraftForge;
import valen.lunar.forge.event.world.WorldEvent;
import valen.lunar.forge.fml.common.gameevent.TickEvent;

@Mixin(value={Minecraft.class}, remap=false)
public abstract class MinecraftEvents {
    @Shadow
    private Timer timer;
    @Unique
    private boolean valen$lunarChangingScreen;

    @Inject(method={"startGame"}, at={@At(value="RETURN")}, require=1)
    private void valen$lunarStart(CallbackInfo callbackInfo) {
        LunarRuntime.start();
    }

    @Inject(method={"runTick"}, at={@At(value="HEAD")}, require=1)
    private void valen$lunarTickStart(CallbackInfo callbackInfo) {
        MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START));
    }

    @Inject(method={"runTick"}, at={@At(value="RETURN")}, require=1)
    private void valen$lunarTickEnd(CallbackInfo callbackInfo) {
        MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
    }

    @Inject(method={"runGameLoop"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/EntityRenderer;updateCameraAndRender(FJ)V")}, require=1)
    private void valen$lunarRenderStart(CallbackInfo callbackInfo) {
        MinecraftForge.EVENT_BUS.post(new TickEvent.RenderTickEvent(TickEvent.Phase.START, this.timer.renderPartialTicks));
    }

    @Inject(method={"runGameLoop"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/EntityRenderer;updateCameraAndRender(FJ)V", shift=At.Shift.AFTER)}, require=1)
    private void valen$lunarRenderEnd(CallbackInfo callbackInfo) {
        MinecraftForge.EVENT_BUS.post(new TickEvent.RenderTickEvent(TickEvent.Phase.END, this.timer.renderPartialTicks));
    }

    @Redirect(method={"runTick"}, at=@At(value="INVOKE", target="Lorg/lwjgl/input/Mouse;next()Z"), require=1)
    private boolean valen$lunarMouse() {
        while (Mouse.next()) {
            if (MinecraftForge.EVENT_BUS.post(new MouseEvent())) continue;
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"displayGuiScreen"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void valen$lunarGui(GuiScreen guiScreen, CallbackInfo callbackInfo) {
        if (this.valen$lunarChangingScreen) {
            return;
        }
        GuiOpenEvent guiOpenEvent = new GuiOpenEvent(guiScreen);
        if (MinecraftForge.EVENT_BUS.post(guiOpenEvent)) {
            callbackInfo.cancel();
        } else if (guiOpenEvent.gui != guiScreen) {
            this.valen$lunarChangingScreen = true;
            try {
                ((Minecraft)this).displayGuiScreen(guiOpenEvent.gui);
            }
            finally {
                this.valen$lunarChangingScreen = false;
            }
            callbackInfo.cancel();
        }
    }

    @Inject(method={"loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V"}, at={@At(value="RETURN")}, require=1)
    private void valen$lunarWorld(WorldClient worldClient, String string, CallbackInfo callbackInfo) {
        if (worldClient != null) {
            MinecraftForge.EVENT_BUS.post(new WorldEvent.Load((World)worldClient));
        }
    }
}


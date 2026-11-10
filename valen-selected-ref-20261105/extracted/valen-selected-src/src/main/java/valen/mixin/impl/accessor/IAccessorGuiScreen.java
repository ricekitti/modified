/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiScreen
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package valen.mixin.impl.accessor;

import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import valen.lunar.forge.fml.relauncher.Side;
import valen.lunar.forge.fml.relauncher.SideOnly;

@SideOnly(value=Side.CLIENT)
@Mixin(value={GuiScreen.class}, remap=false)
public interface IAccessorGuiScreen {
    @Invoker(value="mouseClicked", remap=false)
    public void valen$lunar$callMouseClicked(int var1, int var2, int var3);
}


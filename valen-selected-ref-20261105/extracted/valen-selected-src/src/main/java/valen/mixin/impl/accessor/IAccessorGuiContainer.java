/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.inventory.GuiContainer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package valen.mixin.impl.accessor;

import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import valen.lunar.forge.fml.relauncher.Side;
import valen.lunar.forge.fml.relauncher.SideOnly;

@SideOnly(value=Side.CLIENT)
@Mixin(value={GuiContainer.class}, remap=false)
public interface IAccessorGuiContainer {
    @Invoker(value="mouseClicked", remap=false)
    public void valen$mouseClicked(int var1, int var2, int var3);
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiIngame
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package valen.mixin.impl.accessor;

import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import valen.lunar.forge.fml.relauncher.Side;
import valen.lunar.forge.fml.relauncher.SideOnly;

@SideOnly(value=Side.CLIENT)
@Mixin(value={GuiIngame.class}, remap=false)
public interface IAccessorGuiIngame {
    @Accessor(value="recordPlaying", remap=false)
    public String valen$lunar$getRecordPlaying();

    @Accessor(value="displayedTitle", remap=false)
    public String valen$lunar$getDisplayedTitle();

    @Accessor(value="displayedSubTitle", remap=false)
    public String valen$lunar$getDisplayedSubTitle();
}


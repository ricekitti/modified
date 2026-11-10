/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiScreenBook
 *  net.minecraft.util.IChatComponent
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package valen.mixin.impl.accessor;

import java.util.List;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={GuiScreenBook.class}, remap=false)
public interface IAccessorGuiScreenBook {
    @Accessor(value="field_175386_A", remap=false)
    public List<IChatComponent> valen$lunar$getBookContents();
}


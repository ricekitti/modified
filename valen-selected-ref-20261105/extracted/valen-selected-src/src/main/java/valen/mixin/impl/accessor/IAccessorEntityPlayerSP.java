/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.entity.EntityPlayerSP
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package valen.mixin.impl.accessor;

import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import valen.lunar.forge.fml.relauncher.Side;
import valen.lunar.forge.fml.relauncher.SideOnly;

@SideOnly(value=Side.CLIENT)
@Mixin(value={EntityPlayerSP.class}, remap=false)
public interface IAccessorEntityPlayerSP {
    @Accessor(value="lastReportedPosX", remap=false)
    public double valen$lunar$getLastReportedPosX();

    @Accessor(value="lastReportedPosY", remap=false)
    public double valen$lunar$getLastReportedPosY();

    @Accessor(value="lastReportedPosZ", remap=false)
    public double valen$lunar$getLastReportedPosZ();

    @Accessor(value="lastReportedYaw", remap=false)
    public float valen$lunar$getLastReportedYaw();
}


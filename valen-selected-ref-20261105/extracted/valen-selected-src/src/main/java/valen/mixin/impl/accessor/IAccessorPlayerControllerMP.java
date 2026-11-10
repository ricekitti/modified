/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.PlayerControllerMP
 *  net.minecraft.util.BlockPos
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package valen.mixin.impl.accessor;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import valen.lunar.forge.fml.relauncher.Side;
import valen.lunar.forge.fml.relauncher.SideOnly;

@SideOnly(value=Side.CLIENT)
@Mixin(value={PlayerControllerMP.class}, remap=false)
public interface IAccessorPlayerControllerMP {
    @Accessor(value="curBlockDamageMP", remap=false)
    public float valen$lunar$getCurBlockDamageMP();

    @Accessor(value="currentBlock", remap=false)
    public BlockPos valen$lunar$getCurrentBlock();

    @Accessor(value="curBlockDamageMP", remap=false)
    public void valen$lunar$setCurBlockDamageMP(float var1);

    @Accessor(value="blockHitDelay", remap=false)
    public int valen$lunar$getBlockHitDelay();

    @Accessor(value="blockHitDelay", remap=false)
    public void valen$lunar$setBlockHitDelay(int var1);

    @Invoker(value="syncCurrentPlayItem", remap=false)
    public void valen$lunar$callSyncCurrentPlayItem();
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.block.model.ItemCameraTransforms$TransformType
 *  net.minecraft.client.resources.model.IBakedModel
 */
package valen.lunar.forge.client;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.resources.model.IBakedModel;

public final class ForgeHooksClient {
    public static IBakedModel handleCameraTransforms(IBakedModel iBakedModel, ItemCameraTransforms.TransformType transformType) {
        iBakedModel.getItemCameraTransforms().applyTransform(transformType);
        return iBakedModel;
    }
}


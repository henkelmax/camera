package de.maxhenkel.camera.items.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.camera.Config;
import de.maxhenkel.camera.entities.ImageRenderer;
import de.maxhenkel.camera.items.ItemImage;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

public class ImageItemRenderer extends ItemStackTileEntityRenderer {

    @Override
    public void render(ItemStack itemStackIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.translate(0.5D, 0D, 0.5D);
        ImageRenderer.renderImage(Config.CLIENT.RENDER_IMAGE_ITEM.get() ? ItemImage.getUUID(itemStackIn) : ImageRenderer.DEFAULT_IMAGE_UUID, Direction.SOUTH, 1, 1, matrixStackIn, bufferIn, combinedLightIn);
    }
}

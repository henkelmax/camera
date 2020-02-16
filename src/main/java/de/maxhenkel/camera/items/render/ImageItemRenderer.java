package de.maxhenkel.camera.items.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.camera.Config;
import de.maxhenkel.camera.entities.ImageRenderer;
import de.maxhenkel.camera.items.ItemImage;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

import java.util.UUID;

public class ImageItemRenderer extends ItemStackTileEntityRenderer {

    @Override
    public void render(ItemStack itemStackIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!Config.CLIENT.RENDER_IMAGE_ITEM.get()) {
            return;
        }
        UUID uuid = ItemImage.getUUID(itemStackIn);
        if (uuid == null) {
            uuid = ImageRenderer.DEFAULT_IMAGE_UUID;
        }
        matrixStackIn.translate(0.5D, 0D, 0.5D);
        ImageRenderer.renderImage(uuid, Direction.SOUTH, 1, 1, matrixStackIn, bufferIn, combinedLightIn);
    }
}

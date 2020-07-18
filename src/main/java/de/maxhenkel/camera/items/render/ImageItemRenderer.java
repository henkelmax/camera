package de.maxhenkel.camera.items.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.ImageRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

import java.util.UUID;

public class ImageItemRenderer extends ItemStackTileEntityRenderer {

    @Override
    public void func_239207_a_(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        if (!Main.CLIENT_CONFIG.renderImageItem.get()) {
            return;
        }
        UUID uuid = Main.IMAGE.getUUID(itemStack);
        if (uuid == null) {
            uuid = ImageRenderer.DEFAULT_IMAGE_UUID;
        }
        matrixStack.translate(0.5D, 0D, 0.5D);
        ImageRenderer.renderImage(uuid, Direction.SOUTH, 1, 1, matrixStack, buffer, combinedLight);
    }

}

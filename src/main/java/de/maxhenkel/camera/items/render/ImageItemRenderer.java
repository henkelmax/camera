package de.maxhenkel.camera.items.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.ImageRenderer;
import de.maxhenkel.corelib.client.ItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ImageItemRenderer extends ItemRenderer {

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
        if (!Main.CLIENT_CONFIG.renderImageItem.get()) {
            return;
        }
        UUID uuid = ImageData.getImageID(stack);
        if (uuid == null) {
            uuid = ImageRenderer.DEFAULT_IMAGE_UUID;
        }
        poseStack.translate(0.5D, 0D, 0.5D);
        ImageRenderer.renderImage(uuid, Direction.SOUTH, 1, 1, poseStack, multiBufferSource, light);
    }

}

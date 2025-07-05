package de.maxhenkel.camera.items.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.CameraMod;
import de.maxhenkel.camera.entities.ImageEntityRenderState;
import de.maxhenkel.camera.entities.ImageRenderer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Set;

public class ImageSpecialRenderer implements SpecialModelRenderer<ImageEntityRenderState.ImageState> {

    public ImageSpecialRenderer() {

    }

    @Override
    public void render(@Nullable ImageEntityRenderState.ImageState imageState, ItemDisplayContext itemDisplayContext, PoseStack stack, MultiBufferSource bufferSource, int light, int overlay, boolean b) {
        stack.translate(0.5D, 0D, 0.5D);
        ImageRenderer.renderImage(imageState, Direction.SOUTH, 1, 1, stack, bufferSource, light);
    }

    @Override
    public void getExtents(Set<Vector3f> vecs) {

    }

    @Nullable
    @Override
    public ImageEntityRenderState.ImageState extractArgument(ItemStack stack) {
        if (CameraMod.CLIENT_CONFIG.renderImageItem.get()) {
            ImageData imageData = ImageData.fromStack(stack);
            if (imageData != null) {
                return ImageRenderer.extractImageState(imageData.getId());
            }
        }
        return ImageRenderer.extractImageState(ImageRenderer.DEFAULT_IMAGE_UUID);
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked {

        public static final MapCodec<ImageSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(ImageSpecialRenderer.Unbaked::new);

        public Unbaked() {

        }

        @Override
        public MapCodec<ImageSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
            return new ImageSpecialRenderer();
        }
    }
}


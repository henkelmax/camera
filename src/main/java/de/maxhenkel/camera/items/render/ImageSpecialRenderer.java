package de.maxhenkel.camera.items.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.CameraMod;
import de.maxhenkel.camera.entities.ImageEntityRenderState;
import de.maxhenkel.camera.entities.ImageRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Set;

public class ImageSpecialRenderer implements SpecialModelRenderer<ImageEntityRenderState.ImageState> {

    public ImageSpecialRenderer() {

    }

    @Override
    public void submit(@Nullable ImageEntityRenderState.ImageState imageState, ItemDisplayContext context, PoseStack stack, SubmitNodeCollector collector, int light, int overlay, boolean b) {
        if (imageState == null) {
            return;
        }
        ImageRenderer.submitImage(imageState, Direction.SOUTH, 1, 1, light, stack, collector);
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
        @Nullable
        public SpecialModelRenderer<?> bake(BakingContext p_433472_) {
            return new ImageSpecialRenderer();
        }

        @Override
        public MapCodec<ImageSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

    }
}


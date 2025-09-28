package de.maxhenkel.camera.entities;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.maxhenkel.camera.CameraMod;
import de.maxhenkel.camera.TextureCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ImageRenderer extends EntityRenderer<ImageEntity, ImageEntityRenderState> {

    private static final ResourceLocation DEFAULT_IMAGE = ResourceLocation.fromNamespaceAndPath(CameraMod.MODID, "textures/images/default_image.png");
    private static final ResourceLocation EMPTY_IMAGE = ResourceLocation.fromNamespaceAndPath(CameraMod.MODID, "textures/images/empty_image.png");
    private static final ResourceLocation FRAME_SIDE = ResourceLocation.fromNamespaceAndPath(CameraMod.MODID, "textures/images/frame_side.png");
    private static final ResourceLocation FRAME_BACK = ResourceLocation.fromNamespaceAndPath(CameraMod.MODID, "textures/images/frame_back.png");

    private static final float THICKNESS = 1F / 16F;
    public static final UUID DEFAULT_IMAGE_UUID = new UUID(0L, 0L);

    private static Minecraft mc;

    public ImageRenderer(EntityRendererProvider.Context context) {
        super(context);
        mc = Minecraft.getInstance();
    }

    @Override
    public ImageEntityRenderState createRenderState() {
        return new ImageEntityRenderState();
    }


    @Override
    public void submit(ImageEntityRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        super.submit(state, stack, collector, cameraRenderState);
        stack.pushPose();
        stack.translate(-0.5D, 0D, -0.5D);
        submitImage(state.imageState, state.facing, state.frameWidth, state.frameHeight, state.light, stack, collector);
        stack.popPose();
        submitBoundingBox(state, stack, collector);
    }

    public static void submitImage(ImageEntityRenderState.ImageState imageState, Direction facing, float width, float height, int light, PoseStack stack, SubmitNodeCollector collector) {
        stack.pushPose();

        ResourceLocation resourceLocation = imageState.resourceLocation();
        float imageRatio = imageState.imageRatio();
        boolean stretch = DEFAULT_IMAGE.equals(resourceLocation);

        rotate(facing, stack);

        float frameRatio = width / height;

        float ratio = imageRatio / frameRatio;

        float ratioX;
        float ratioY;

        if (stretch) {
            ratioX = 0F;
            ratioY = 0F;
        } else {
            if (ratio >= 1F) {
                ratioY = ((1F - 1F / ratio) / 2F) * height;
                ratioX = 0F;
            } else {
                ratioX = ((1F - ratio) / 2F) * width;
                ratioY = 0F;
            }
        }

        collector.submitCustomGeometry(stack, RenderType.entityCutout(resourceLocation), (pose, vertexConsumer) -> {
            // Front
            vertex(vertexConsumer, pose, 0F + ratioX, ratioY, THICKNESS, 0F, 1F, light);
            vertex(vertexConsumer, pose, width - ratioX, ratioY, THICKNESS, 1F, 1F, light);
            vertex(vertexConsumer, pose, width - ratioX, height - ratioY, THICKNESS, 1F, 0F, light);
            vertex(vertexConsumer, pose, ratioX, height - ratioY, THICKNESS, 0F, 0F, light);
        });

        collector.submitCustomGeometry(stack, RenderType.entityCutout(FRAME_SIDE), (pose, vertexConsumer) -> {
            //Left
            vertex(vertexConsumer, pose, 0F + ratioX, 0F + ratioY, 0F, 1F, 0F + ratioY, light);
            vertex(vertexConsumer, pose, 0F + ratioX, 0F + ratioY, THICKNESS, 1F - THICKNESS, 0F + ratioY, light);
            vertex(vertexConsumer, pose, 0F + ratioX, height - ratioY, THICKNESS, 1F - THICKNESS, 1F - ratioY, light);
            vertex(vertexConsumer, pose, 0F + ratioX, height - ratioY, 0F, 1F, 1F - ratioY, light);

            //Right
            vertex(vertexConsumer, pose, width - ratioX, 0F + ratioY, 0F, 0F, 0F + ratioY, light);
            vertex(vertexConsumer, pose, width - ratioX, height - ratioY, 0F, 0F, 1F - ratioY, light);
            vertex(vertexConsumer, pose, width - ratioX, height - ratioY, THICKNESS, THICKNESS, 1F - ratioY, light);
            vertex(vertexConsumer, pose, width - ratioX, 0F + ratioY, THICKNESS, THICKNESS, 0F + ratioY, light);

            //Top
            vertex(vertexConsumer, pose, 0F + ratioX, height - ratioY, 0F, 0F + ratioX, 1F, light);
            vertex(vertexConsumer, pose, 0F + ratioX, height - ratioY, THICKNESS, 0F + ratioX, 1F - THICKNESS, light);
            vertex(vertexConsumer, pose, width - ratioX, height - ratioY, THICKNESS, 1F - ratioX, 1F - THICKNESS, light);
            vertex(vertexConsumer, pose, width - ratioX, height - ratioY, 0F, 1F - ratioX, 1F, light);

            //Bottom
            vertex(vertexConsumer, pose, 0F + ratioX, 0F + ratioY, 0F, 0F + ratioX, 0F, light);
            vertex(vertexConsumer, pose, width - ratioX, 0F + ratioY, 0F, 1F - ratioX, 0F, light);
            vertex(vertexConsumer, pose, width - ratioX, 0F + ratioY, THICKNESS, 1F - ratioX, THICKNESS, light);
            vertex(vertexConsumer, pose, 0F + ratioX, 0F + ratioY, THICKNESS, 0F + ratioX, THICKNESS, light);
        });

        collector.submitCustomGeometry(stack, RenderType.entityCutout(FRAME_BACK), (pose, vertexConsumer) -> {
            //Back
            vertex(vertexConsumer, pose, width - ratioX, 0F + ratioY, 0F, 1F - ratioX, 0F + ratioY, light);
            vertex(vertexConsumer, pose, 0F + ratioX, 0F + ratioY, 0F, 0F + ratioX, 0F + ratioY, light);
            vertex(vertexConsumer, pose, 0F + ratioX, height - ratioY, 0F, 0F + ratioX, 1F - ratioY, light);
            vertex(vertexConsumer, pose, width - ratioX, height - ratioY, 0F, 1F - ratioX, 1F - ratioY, light);
        });

        stack.popPose();
    }

    @Override
    public void extractRenderState(ImageEntity image, ImageEntityRenderState state, float partialTicks) {
        super.extractRenderState(image, state, partialTicks);

        state.imageEntityUUID = image.getUUID();

        state.frameWidth = image.getFrameWidth();
        state.frameHeight = image.getFrameHeight();
        state.facing = image.getFacing();
        state.imageState = extractImageState(image.getImageUUID().orElse(DEFAULT_IMAGE_UUID));
        state.light = LevelRenderer.getLightColor(image.level(), image.getCenterPosition());
        state.imageBoundingBox = image.getBoundingBox().move(-image.getX(), -image.getY(), -image.getZ());
    }

    public static ImageEntityRenderState.ImageState extractImageState(@Nonnull UUID imageId) {
        ResourceLocation resourceLocation;
        float imageRatio;
        if (DEFAULT_IMAGE_UUID.equals(imageId)) {
            resourceLocation = DEFAULT_IMAGE;
            imageRatio = 1.5F;
        } else {
            ResourceLocation rl = TextureCache.instance().getImage(imageId);
            if (rl != null) {
                resourceLocation = rl;
                NativeImage nativeImage = TextureCache.instance().getNativeImage(imageId);
                imageRatio = (float) nativeImage.getWidth() / (float) nativeImage.getHeight();
            } else {
                resourceLocation = DEFAULT_IMAGE;
                imageRatio = 1.5F;
            }
        }
        return new ImageEntityRenderState.ImageState(imageId, imageRatio, resourceLocation);
    }

    private static void vertex(VertexConsumer builder, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light) {
        builder.addVertex(pose.pose(), x, y, z)
                .setColor(1F, 1F, 1F, 1F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0F, 0F, -1F);
    }

    private static void submitBoundingBox(ImageEntityRenderState state, PoseStack stack, SubmitNodeCollector collector) {
        if (!(mc.hitResult instanceof EntityHitResult entityHitResult) || !entityHitResult.getEntity().getUUID().equals(state.imageEntityUUID)) {
            return;
        }
        if (mc.options.hideGui) {
            return;
        }
        collector.submitCustomGeometry(stack, RenderType.lines(), (pose, vertexConsumer) -> {
            ShapeRenderer.renderLineBox(pose, vertexConsumer, state.imageBoundingBox, 0F, 0F, 0F, 0.4F);
        });
    }

    public static void rotate(Direction facing, PoseStack matrixStack) {
        switch (facing) {
            case NORTH:
                matrixStack.translate(1D, 0D, 1D);
                matrixStack.mulPose(Axis.YP.rotationDegrees(180F));
                break;
            case SOUTH:
                break;
            case EAST:
                matrixStack.translate(0D, 0D, 1D);
                matrixStack.mulPose(Axis.YP.rotationDegrees(90F));
                break;
            case WEST:
                matrixStack.translate(1D, 0D, 0D);
                matrixStack.mulPose(Axis.YP.rotationDegrees(270F));
                break;
        }
    }

}

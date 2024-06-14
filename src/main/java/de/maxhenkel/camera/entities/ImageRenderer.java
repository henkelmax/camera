package de.maxhenkel.camera.entities;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.TextureCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.UUID;

public class ImageRenderer extends EntityRenderer<ImageEntity> {

    private static final ResourceLocation DEFAULT_IMAGE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/images/default_image.png");
    private static final ResourceLocation EMPTY_IMAGE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/images/empty_image.png");
    private static final ResourceLocation FRAME_SIDE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/images/frame_side.png");
    private static final ResourceLocation FRAME_BACK = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/images/frame_back.png");

    private static final float THICKNESS = 1F / 16F;
    public static final UUID DEFAULT_IMAGE_UUID = new UUID(0L, 0L);

    private static Minecraft mc;

    public ImageRenderer(EntityRendererProvider.Context context) {
        super(context);
        mc = Minecraft.getInstance();
    }

    @Override
    public void render(ImageEntity entity, float f1, float f2, PoseStack matrixStack, MultiBufferSource buffer1, int light) {
        int imageLight = LevelRenderer.getLightColor(entity.level(), entity.getCenterPosition());
        renderImage(entity.getImageUUID().orElse(null), entity.getFacing(), entity.getFrameWidth(), entity.getFrameHeight(), matrixStack, buffer1, imageLight);
        renderBoundingBox(entity, matrixStack, buffer1);
        super.render(entity, f1, f2, matrixStack, buffer1, light);
    }

    public static void renderImage(UUID imageUUID, Direction facing, float width, float height, PoseStack matrixStack, MultiBufferSource buffer1, int light) {
        matrixStack.pushPose();

        float imageRatio = 1F;
        boolean stretch = true;
        ResourceLocation resourceLocation = EMPTY_IMAGE;
        if (DEFAULT_IMAGE_UUID.equals(imageUUID)) {
            resourceLocation = DEFAULT_IMAGE;
            imageRatio = 1.5F;
            stretch = false;
        } else if (imageUUID != null) {
            ResourceLocation rl = TextureCache.instance().getImage(imageUUID);
            if (rl != null) {
                resourceLocation = rl;
                NativeImage image = TextureCache.instance().getNativeImage(imageUUID);
                imageRatio = (float) image.getWidth() / (float) image.getHeight();
                stretch = false;
            } else {
                resourceLocation = DEFAULT_IMAGE;
                imageRatio = 1.5F;
                stretch = false;
            }
        }

        matrixStack.translate(-0.5D, 0D, -0.5D);

        rotate(facing, matrixStack);

        float frameRatio = width / height;

        float ratio = imageRatio / frameRatio;

        float ratioX;
        float ratioY;

        if (stretch) {
            ratioX = 0F;
            ratioY = 0F;
        } else {
            if (ratio >= 1F) {
                ratioY = (1F - 1F / ratio) / 2F;
                ratioX = 0F;
            } else {
                ratioX = (1F - ratio) / 2F;
                ratioY = 0F;
            }

            ratioX *= width;
            ratioY *= height;
        }

        VertexConsumer builderFront = buffer1.getBuffer(RenderType.entityCutout(resourceLocation));

        // Front
        vertex(builderFront, matrixStack, 0F + ratioX, ratioY, THICKNESS, 0F, 1F, light);
        vertex(builderFront, matrixStack, width - ratioX, ratioY, THICKNESS, 1F, 1F, light);
        vertex(builderFront, matrixStack, width - ratioX, height - ratioY, THICKNESS, 1F, 0F, light);
        vertex(builderFront, matrixStack, ratioX, height - ratioY, THICKNESS, 0F, 0F, light);

        VertexConsumer builderSide = buffer1.getBuffer(RenderType.entityCutout(FRAME_SIDE));

        //Left
        vertex(builderSide, matrixStack, 0F + ratioX, 0F + ratioY, 0F, 1F, 0F + ratioY, light);
        vertex(builderSide, matrixStack, 0F + ratioX, 0F + ratioY, THICKNESS, 1F - THICKNESS, 0F + ratioY, light);
        vertex(builderSide, matrixStack, 0F + ratioX, height - ratioY, THICKNESS, 1F - THICKNESS, 1F - ratioY, light);
        vertex(builderSide, matrixStack, 0F + ratioX, height - ratioY, 0F, 1F, 1F - ratioY, light);

        //Right
        vertex(builderSide, matrixStack, width - ratioX, 0F + ratioY, 0F, 0F, 0F + ratioY, light);
        vertex(builderSide, matrixStack, width - ratioX, height - ratioY, 0F, 0F, 1F - ratioY, light);
        vertex(builderSide, matrixStack, width - ratioX, height - ratioY, THICKNESS, THICKNESS, 1F - ratioY, light);
        vertex(builderSide, matrixStack, width - ratioX, 0F + ratioY, THICKNESS, THICKNESS, 0F + ratioY, light);

        //Top
        vertex(builderSide, matrixStack, 0F + ratioX, height - ratioY, 0F, 0F + ratioX, 1F, light);
        vertex(builderSide, matrixStack, 0F + ratioX, height - ratioY, THICKNESS, 0F + ratioX, 1F - THICKNESS, light);
        vertex(builderSide, matrixStack, width - ratioX, height - ratioY, THICKNESS, 1F - ratioX, 1F - THICKNESS, light);
        vertex(builderSide, matrixStack, width - ratioX, height - ratioY, 0F, 1F - ratioX, 1F, light);

        //Bottom
        vertex(builderSide, matrixStack, 0F + ratioX, 0F + ratioY, 0F, 0F + ratioX, 0F, light);
        vertex(builderSide, matrixStack, width - ratioX, 0F + ratioY, 0F, 1F - ratioX, 0F, light);
        vertex(builderSide, matrixStack, width - ratioX, 0F + ratioY, THICKNESS, 1F - ratioX, THICKNESS, light);
        vertex(builderSide, matrixStack, 0F + ratioX, 0F + ratioY, THICKNESS, 0F + ratioX, THICKNESS, light);

        VertexConsumer builderBack = buffer1.getBuffer(RenderType.entityCutout(FRAME_BACK));

        //Back
        vertex(builderBack, matrixStack, width - ratioX, 0F + ratioY, 0F, 1F - ratioX, 0F + ratioY, light);
        vertex(builderBack, matrixStack, 0F + ratioX, 0F + ratioY, 0F, 0F + ratioX, 0F + ratioY, light);
        vertex(builderBack, matrixStack, 0F + ratioX, height - ratioY, 0F, 0F + ratioX, 1F - ratioY, light);
        vertex(builderBack, matrixStack, width - ratioX, height - ratioY, 0F, 1F - ratioX, 1F - ratioY, light);

        matrixStack.popPose();
    }

    private static void vertex(VertexConsumer builder, PoseStack matrixStack, float x, float y, float z, float u, float v, int light) {
        PoseStack.Pose entry = matrixStack.last();
        Matrix4f matrix4f = entry.pose();
        builder.addVertex(matrix4f, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(entry, 0F, 0F, -1F);
    }

    private static void renderBoundingBox(ImageEntity entity, PoseStack matrixStack, MultiBufferSource buffer) {
        if (!(mc.hitResult instanceof EntityHitResult) || ((EntityHitResult) mc.hitResult).getEntity() != entity) {
            return;
        }
        if (mc.options.hideGui) {
            return;
        }
        matrixStack.pushPose();
        AABB axisalignedbb = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
        LevelRenderer.renderLineBox(matrixStack, buffer.getBuffer(RenderType.lines()), axisalignedbb, 0F, 0F, 0F, 0.4F);
        matrixStack.popPose();
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

    @Nullable
    @Override
    public ResourceLocation getTextureLocation(ImageEntity entity) {
        return EMPTY_IMAGE;
    }

}

package de.maxhenkel.camera.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.TextureCache;
import de.maxhenkel.camera.Tools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.UUID;

public class ImageRenderer extends EntityRenderer<ImageEntity> {

    private static final ResourceLocation DEFAULT_IMAGE = new ResourceLocation(Main.MODID, "textures/images/default_image.png");
    private static final ResourceLocation EMPTY_IMAGE = new ResourceLocation(Main.MODID, "textures/images/empty_image.png");
    private static final ResourceLocation FRAME_SIDE = new ResourceLocation(Main.MODID, "textures/images/frame_side.png");
    private static final ResourceLocation FRAME_BACK = new ResourceLocation(Main.MODID, "textures/images/frame_back.png");

    private static final float THICKNESS = 1F / 16F;
    public static final UUID DEFAULT_IMAGE_UUID = new UUID(0L, 0L);

    private static Minecraft mc;

    public ImageRenderer(EntityRendererManager renderManager) {
        super(renderManager);
        mc = Minecraft.getInstance();
    }

    @Override
    public void render(ImageEntity entity, float f1, float f2, MatrixStack matrixStack, IRenderTypeBuffer buffer1, int light) {
        int imageLight = WorldRenderer.getCombinedLight(entity.world, entity.getCenterPosition());
        renderImage(entity.getImageUUID(), entity.getFacing(), entity.getFrameWidth(), entity.getFrameHeight(), matrixStack, buffer1, imageLight);
        renderBoundingBox(entity, matrixStack, buffer1);
        super.render(entity, f1, f2, matrixStack, buffer1, light);
    }

    public static void renderImage(UUID imageUUID, Direction facing, float width, float height, MatrixStack matrixStack, IRenderTypeBuffer buffer1, int light) {
        matrixStack.push();

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

        IVertexBuilder builderFront = buffer1.getBuffer(getRenderType(resourceLocation));

        // Front
        vertex(builderFront, matrixStack, 0F + ratioX, ratioY, THICKNESS, 0F, 1F, light);
        vertex(builderFront, matrixStack, width - ratioX, ratioY, THICKNESS, 1F, 1F, light);
        vertex(builderFront, matrixStack, width - ratioX, height - ratioY, THICKNESS, 1F, 0F, light);
        vertex(builderFront, matrixStack, ratioX, height - ratioY, THICKNESS, 0F, 0F, light);

        IVertexBuilder builderSide = buffer1.getBuffer(getRenderType(FRAME_SIDE));

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

        IVertexBuilder builderBack = buffer1.getBuffer(getRenderType(FRAME_BACK));

        //Back
        vertex(builderBack, matrixStack, width - ratioX, 0F + ratioY, 0F, 1F - ratioX, 0F + ratioY, light);
        vertex(builderBack, matrixStack, 0F + ratioX, 0F + ratioY, 0F, 0F + ratioX, 0F + ratioY, light);
        vertex(builderBack, matrixStack, 0F + ratioX, height - ratioY, 0F, 0F + ratioX, 1F - ratioY, light);
        vertex(builderBack, matrixStack, width - ratioX, height - ratioY, 0F, 1F - ratioX, 1F - ratioY, light);

        matrixStack.pop();
    }

    private static void vertex(IVertexBuilder builder, MatrixStack matrixStack, float x, float y, float z, float u, float v, int light) {
        MatrixStack.Entry entry = matrixStack.getLast();
        Matrix4f matrix4f = entry.getMatrix();
        Matrix3f matrix3f = entry.getNormal();
        builder.pos(matrix4f, x, y, z)
                .color(255, 255, 255, 255)
                .tex(u, v)
                .overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(light)
                .normal(matrix3f, 0F, 0F, -1F)
                .endVertex();
    }

    private static RenderType getRenderType(ResourceLocation resourceLocation) {
        RenderType.State state = RenderType.State
                .getBuilder()
                .texture(new RenderState.TextureState(resourceLocation, false, false))
                .diffuseLighting(new RenderState.DiffuseLightingState(false))
                .lightmap(new RenderState.LightmapState(true))
                .overlay(new RenderState.OverlayState(true))
                .cull(new RenderState.CullState(true))
                .build(true);
        return RenderType.makeType("entity_cutout", DefaultVertexFormats.ENTITY, GL11.GL_QUADS, 256, true, false, state);
    }

    private static void renderBoundingBox(ImageEntity entity, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        if (Tools.getEntityLookingAt() != entity) {
            return;
        }
        if (mc.gameSettings.hideGUI) {
            return;
        }
        matrixStack.push();
        renderBoundingBox(matrixStack, buffer, entity);
        matrixStack.pop();
    }

    private static void renderBoundingBox(MatrixStack matrixStack, IRenderTypeBuffer buffer, Entity entity) {
        AxisAlignedBB axisalignedbb = entity.getBoundingBox().offset(-entity.getPosX(), -entity.getPosY(), -entity.getPosZ());
        WorldRenderer.drawBoundingBox(matrixStack, buffer.getBuffer(RenderType.getLines()), axisalignedbb, 0F, 0F, 0F, 0.4F);
    }

    public static void rotate(Direction facing, MatrixStack matrixStack) {
        switch (facing) {
            case NORTH:
                matrixStack.translate(1D, 0D, 1D);
                matrixStack.rotate(Vector3f.YP.rotationDegrees(180F));
                break;
            case SOUTH:
                break;
            case EAST:
                matrixStack.translate(0D, 0D, 1D);
                matrixStack.rotate(Vector3f.YP.rotationDegrees(90F));
                break;
            case WEST:
                matrixStack.translate(1D, 0D, 0D);
                matrixStack.rotate(Vector3f.YP.rotationDegrees(270F));
                break;
        }
    }

    @Nullable
    @Override
    public ResourceLocation getEntityTexture(ImageEntity entity) {
        return EMPTY_IMAGE;
    }

}

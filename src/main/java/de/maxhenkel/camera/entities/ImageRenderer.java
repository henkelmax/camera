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
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.UUID;

public class ImageRenderer extends EntityRenderer<ImageEntity> {

    private static final ResourceLocation DEFAULT_IMAGE = new ResourceLocation(Main.MODID, "textures/images/default_image.png");
    private static final ResourceLocation EMPTY_IMAGE = new ResourceLocation(Main.MODID, "textures/images/empty_image.png");
    private static final ResourceLocation FRAME_SIDE = new ResourceLocation(Main.MODID, "textures/images/frame_side.png");
    private static final ResourceLocation FRAME_BACK = new ResourceLocation(Main.MODID, "textures/images/frame_back.png");

    private static final float THICKNESS = 1F / 16F;

    private Minecraft mc;

    public ImageRenderer(EntityRendererManager renderManager) {
        super(renderManager);
        mc = Minecraft.getInstance();
    }

    private void vertex(ImageEntity image, IVertexBuilder builder, MatrixStack matrixStack, float x, float y, float z, float u, float v) {
        MatrixStack.Entry entry = matrixStack.func_227866_c_();
        Matrix4f matrix4f = entry.func_227870_a_();
        Matrix3f matrix3f = entry.func_227872_b_();
        int lightLevel = WorldRenderer.func_228421_a_(image.world, image.getCenterPosition());
        builder.func_227888_a_(matrix4f, x, y, z) // Matrix and position?
                .func_225586_a_(255, 255, 255, 255) // Color
                .func_225583_a_(u, v) // U V
                .func_227891_b_(OverlayTexture.field_229196_a_) //Overlay Texture
                .func_227886_a_(lightLevel) // Light
                .func_227887_a_(matrix3f, 0F, 0F, -1F) // ???
                .endVertex();
    }

    @Override
    public void func_225623_a_(ImageEntity entity, float f1, float f2, MatrixStack matrixStack, IRenderTypeBuffer buffer1, int i) {
        matrixStack.func_227860_a_();

        float imageRatio = 1F;
        boolean stretch = true;
        ResourceLocation resourceLocation = EMPTY_IMAGE;
        UUID imageUUID = entity.getImageUUID();
        if (imageUUID != null) {
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

        matrixStack.func_227861_a_(-0.5D, 0D, -0.5D);

        Direction facing = entity.getFacing();
        float width = entity.getFrameWidth();
        float height = entity.getFrameHeight();

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

        IVertexBuilder builderFront = buffer1.getBuffer(RenderType.func_228634_a_(resourceLocation));

        // Front
        vertex(entity, builderFront, matrixStack, 0F + ratioX, ratioY, THICKNESS, 0F, 1F);
        vertex(entity, builderFront, matrixStack, width - ratioX, ratioY, THICKNESS, 1F, 1F);
        vertex(entity, builderFront, matrixStack, width - ratioX, height - ratioY, THICKNESS, 1F, 0F);
        vertex(entity, builderFront, matrixStack, ratioX, height - ratioY, THICKNESS, 0F, 0F);

        IVertexBuilder builderSide = buffer1.getBuffer(RenderType.func_228634_a_(FRAME_SIDE));

        //Left
        vertex(entity, builderSide, matrixStack, 0F + ratioX, 0F + ratioY, 0F, 1F, 0F + ratioY);
        vertex(entity, builderSide, matrixStack, 0F + ratioX, 0F + ratioY, THICKNESS, 1F - THICKNESS, 0F + ratioY);
        vertex(entity, builderSide, matrixStack, 0F + ratioX, height - ratioY, THICKNESS, 1F - THICKNESS, 1F - ratioY);
        vertex(entity, builderSide, matrixStack, 0F + ratioX, height - ratioY, 0F, 1F, 1F - ratioY);

        //Right
        vertex(entity, builderSide, matrixStack, width - ratioX, 0F + ratioY, 0F, 0F, 0F + ratioY);
        vertex(entity, builderSide, matrixStack, width - ratioX, height - ratioY, 0F, 0F, 1F - ratioY);
        vertex(entity, builderSide, matrixStack, width - ratioX, height - ratioY, THICKNESS, THICKNESS, 1F - ratioY);
        vertex(entity, builderSide, matrixStack, width - ratioX, 0F + ratioY, THICKNESS, THICKNESS, 0F + ratioY);

        //Top
        vertex(entity, builderSide, matrixStack, 0F + ratioX, height - ratioY, 0F, 0F + ratioX, 1F);
        vertex(entity, builderSide, matrixStack, 0F + ratioX, height - ratioY, THICKNESS, 0F + ratioX, 1F - THICKNESS);
        vertex(entity, builderSide, matrixStack, width - ratioX, height - ratioY, THICKNESS, 1F - ratioX, 1F - THICKNESS);
        vertex(entity, builderSide, matrixStack, width - ratioX, height - ratioY, 0F, 1F - ratioX, 1F);

        //Bottom
        vertex(entity, builderSide, matrixStack, 0F + ratioX, 0F + ratioY, 0F, 0F + ratioX, 0F);
        vertex(entity, builderSide, matrixStack, width - ratioX, 0F + ratioY, 0F, 1F - ratioX, 0F);
        vertex(entity, builderSide, matrixStack, width - ratioX, 0F + ratioY, THICKNESS, 1F - ratioX, THICKNESS);
        vertex(entity, builderSide, matrixStack, 0F + ratioX, 0F + ratioY, THICKNESS, 0F + ratioX, THICKNESS);

        IVertexBuilder builderBack = buffer1.getBuffer(RenderType.func_228634_a_(FRAME_BACK));

        //Back
        vertex(entity, builderBack, matrixStack, width - ratioX, 0F + ratioY, 0F, 1F - ratioX, 0F + ratioY);
        vertex(entity, builderBack, matrixStack, 0F + ratioX, 0F + ratioY, 0F, 0F + ratioX, 0F + ratioY);
        vertex(entity, builderBack, matrixStack, 0F + ratioX, height - ratioY, 0F, 0F + ratioX, 1F - ratioY);
        vertex(entity, builderBack, matrixStack, width - ratioX, height - ratioY, 0F, 1F - ratioX, 1F - ratioY);

        matrixStack.func_227865_b_();

        renderBoundingBox(entity, matrixStack, buffer1);
        super.func_225623_a_(entity, f1, f2, matrixStack, buffer1, 0xFFFFFF);
    }

    private void renderBoundingBox(ImageEntity entity, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        if (Tools.getEntityLookingAt() != entity) {
            return;
        }
        if (mc.gameSettings.hideGUI) {
            return;
        }
        matrixStack.func_227860_a_();
        renderBoundingBox(matrixStack, buffer, entity);
        matrixStack.func_227865_b_();
    }

    private void renderBoundingBox(MatrixStack matrixStack, IRenderTypeBuffer buffer, Entity entity) {
        AxisAlignedBB axisalignedbb = entity.getBoundingBox().offset(-entity.func_226277_ct_(), -entity.func_226278_cu_(), -entity.func_226281_cx_());
        WorldRenderer.func_228430_a_(matrixStack, buffer.getBuffer(RenderType.func_228659_m_()), axisalignedbb, 0.125F, 0.125F, 0.125F, 1.0F);
    }

    public static void rotate(Direction facing, MatrixStack matrixStack) {
        switch (facing) {
            case NORTH:
                matrixStack.func_227861_a_(1D, 0D, 1D);
                matrixStack.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(180F));
                break;
            case SOUTH:
                break;
            case EAST:
                matrixStack.func_227861_a_(0D, 0D, 1D);
                matrixStack.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(90F));
                break;
            case WEST:
                matrixStack.func_227861_a_(1D, 0D, 0D);
                matrixStack.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(270F));
                break;
        }
    }

    @Nullable
    @Override
    public ResourceLocation getEntityTexture(ImageEntity entity) {
        return EMPTY_IMAGE;
    }

}

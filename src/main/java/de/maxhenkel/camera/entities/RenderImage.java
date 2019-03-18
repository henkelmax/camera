package de.maxhenkel.camera.entities;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.TextureCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.UUID;

public class RenderImage extends Render<EntityImage> {

    private static final ResourceLocation DEFAULT_IMAGE = new ResourceLocation(Main.MODID, "textures/images/default_image.png");
    private static final ResourceLocation EMPTY_IMAGE = new ResourceLocation(Main.MODID, "textures/images/empty_image.png");
    private static final ResourceLocation FRAME_SIDE = new ResourceLocation(Main.MODID, "textures/images/frame_side.png");
    private static final ResourceLocation FRAME_BACK = new ResourceLocation(Main.MODID, "textures/images/frame_back.png");

    private static final double THICKNESS = 1D / 16D;

    private Minecraft mc;

    public RenderImage(RenderManager renderManager) {
        super(renderManager);
        mc = Minecraft.getInstance();
    }

    @Override
    public void doRender(EntityImage entity, double x, double y, double z, float entityYaw, float partialTicks) {
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

        GlStateManager.pushMatrix();
        GlStateManager.translated(x - 0.5D, y, z - 0.5D);
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();

        EnumFacing facing = entity.getFacing();
        double width = entity.getWidth();
        double height = entity.getHeight();

        rotate(facing);

        bindTexture(resourceLocation);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        float frameRatio = (float) (width / height);

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

        buffer.pos(0D + ratioX, ratioY, THICKNESS).tex(0D, 1D).endVertex();
        buffer.pos(width - ratioX, ratioY, THICKNESS).tex(1D, 1D).endVertex();
        buffer.pos(width - ratioX, height - ratioY, THICKNESS).tex(1D, 0D).endVertex();
        buffer.pos(0D + ratioX, height - ratioY, THICKNESS).tex(0D, 0D).endVertex();

        tessellator.draw();

        bindTexture(FRAME_SIDE);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        //Left
        buffer.pos(0D + ratioX, 0D + ratioY, 0D).tex(1D, 0D + ratioY).endVertex();
        buffer.pos(0D + ratioX, 0D + ratioY, THICKNESS).tex(1D - THICKNESS, 0D + ratioY).endVertex();
        buffer.pos(0D + ratioX, height - ratioY, THICKNESS).tex(1D - THICKNESS, 1D - ratioY).endVertex();
        buffer.pos(0D + ratioX, height - ratioY, 0D).tex(1D, 1D - ratioY).endVertex();

        //Right
        buffer.pos(width - ratioX, 0D + ratioY, 0D).tex(0D, 0D + ratioY).endVertex();
        buffer.pos(width - ratioX, height - ratioY, 0D).tex(0D, 1D - ratioY).endVertex();
        buffer.pos(width - ratioX, height - ratioY, THICKNESS).tex(THICKNESS, 1D - ratioY).endVertex();
        buffer.pos(width - ratioX, 0D + ratioY, THICKNESS).tex(THICKNESS, 0D + ratioY).endVertex();

        //Top
        buffer.pos(0D + ratioX, height - ratioY, 0D).tex(0D + ratioX, 1D).endVertex();
        buffer.pos(0D + ratioX, height - ratioY, THICKNESS).tex(0D + ratioX, 1D - THICKNESS).endVertex();
        buffer.pos(width - ratioX, height - ratioY, THICKNESS).tex(1D - ratioX, 1D - THICKNESS).endVertex();
        buffer.pos(width - ratioX, height - ratioY, 0D).tex(1D - ratioX, 1D).endVertex();

        //Bottom
        buffer.pos(0D + ratioX, 0D + ratioY, 0D).tex(0D + ratioX, 0D).endVertex();
        buffer.pos(width - ratioX, 0D + ratioY, 0D).tex(1D - ratioX, 0D).endVertex();
        buffer.pos(width - ratioX, 0D + ratioY, THICKNESS).tex(1D - ratioX, THICKNESS).endVertex();
        buffer.pos(0D + ratioX, 0D + ratioY, THICKNESS).tex(0D + ratioX, THICKNESS).endVertex();

        tessellator.draw();

        bindTexture(FRAME_BACK);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        //Back
        buffer.pos(width - ratioX, 0D + ratioY, 0D).tex(1D - ratioX, 0D + ratioY).endVertex();
        buffer.pos(0D + ratioX, 0D + ratioY, 0D).tex(0D + ratioX, 0D + ratioY).endVertex();
        buffer.pos(0D + ratioX, height - ratioY, 0D).tex(0D + ratioX, 1D - ratioY).endVertex();
        buffer.pos(width - ratioX, height - ratioY, 0D).tex(1D - ratioX, 1D - ratioY).endVertex();

        tessellator.draw();

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        renderBoundingBox(entity, x, y, z);

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    private void renderBoundingBox(EntityImage entity, double x, double y, double z) {
        if (mc.objectMouseOver.entity != entity) {
            return;
        }

        if (mc.gameSettings.hideGUI) {
            return;
        }

        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        AxisAlignedBB axisalignedbb = entity.getBoundingBox();
        WorldRenderer.drawBoundingBox(
                axisalignedbb.minX - entity.posX + x,
                axisalignedbb.minY - entity.posY + y,
                axisalignedbb.minZ - entity.posZ + z,
                axisalignedbb.maxX - entity.posX + x,
                axisalignedbb.maxY - entity.posY + y,
                axisalignedbb.maxZ - entity.posZ + z,
                0.25F, 0.25F, 0.25F, 1F
        );
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    public static void rotate(EnumFacing facing) {
        switch (facing) {
            case NORTH:
                GlStateManager.translated(1D, 0D, 1D);
                GlStateManager.rotatef(180F, 0F, 1F, 0F);
                break;
            case SOUTH:
                break;
            case EAST:
                GlStateManager.translated(0D, 0D, 1D);
                GlStateManager.rotatef(90F, 0F, 1F, 0F);
                break;
            case WEST:
                GlStateManager.translated(1D, 0D, 0D);
                GlStateManager.rotatef(270F, 0F, 1F, 0F);
                break;
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityImage entity) {
        return DEFAULT_IMAGE;
    }


}

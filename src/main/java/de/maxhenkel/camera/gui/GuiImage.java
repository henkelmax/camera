package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.TextureCache;
import de.maxhenkel.camera.items.ItemImage;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import java.util.UUID;

public class GuiImage extends GuiContainer {

    public static final ResourceLocation DEFAULT_IMAGE = new ResourceLocation(Main.MODID, "textures/images/default_image.png");

    private UUID imageUUID;

    public GuiImage(ItemStack image) {
        super(new ContainerImage());

        imageUUID = ItemImage.getUUID(image);
    }

    //https://stackoverflow.com/questions/6565703/math-algorithm-fit-image-to-screen-retain-aspect-ratio
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawDefaultBackground();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (imageUUID == null) {
            return;
        }

        GlStateManager.pushMatrix();

        ResourceLocation location = TextureCache.instance().getImage(imageUUID);

        float imageWidth = 12F;
        float imageHeight = 8F;


        if (location == null) {
            mc.getTextureManager().bindTexture(DEFAULT_IMAGE);
        } else {
            mc.getTextureManager().bindTexture(location);
            NativeImage image = TextureCache.instance().getNativeImage(imageUUID);
            imageWidth = (float) image.getWidth();
            imageHeight = (float) image.getHeight();
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        float scale = 0.8F;

        float ws = (float) width * scale;
        float hs = (float) height * scale;

        float rs = ws / hs;
        float ri = imageWidth / imageHeight;

        float hnew;
        float wnew;

        if (rs > ri) {
            wnew = imageWidth * hs / imageHeight;
            hnew = hs;
        } else {
            wnew = ws;
            hnew = imageHeight * ws / imageWidth;
        }

        float top = (hs - hnew) / 2F;
        float left = (ws - wnew) / 2F;

        left += ((1F - scale) * ws) / 2F;
        top += ((1F - scale) * hs) / 2F;

        buffer.pos(left, top, zLevel).tex(0D, 0D).endVertex();
        buffer.pos(left, top + hnew, zLevel).tex(0D, 1D).endVertex();
        buffer.pos(left + wnew, top + hnew, zLevel).tex(1D, 1D).endVertex();
        buffer.pos(left + wnew, top, zLevel).tex(1D, 0D).endVertex();

        tessellator.draw();

        GlStateManager.popMatrix();
    }
}
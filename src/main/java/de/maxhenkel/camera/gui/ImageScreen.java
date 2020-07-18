package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.TextureCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;

import java.util.UUID;

public class ImageScreen extends ContainerScreen<Container> {

    public static final ResourceLocation DEFAULT_IMAGE = new ResourceLocation(Main.MODID, "textures/images/default_image.png");

    private UUID imageUUID;

    public ImageScreen(ItemStack image) {
        super(new DummyContainer(), null, new TranslationTextComponent("gui.image.title"));

        imageUUID = Main.IMAGE.getUUID(image);
    }

    //https://stackoverflow.com/questions/6565703/math-algorithm-fit-image-to-screen-retain-aspect-ratio
    @Override
    protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        func_230446_a_(matrixStack);
        RenderSystem.color4f(1F, 1F, 1F, 1F);

        if (imageUUID == null) {
            return;
        }

        drawImage(field_230706_i_, field_230708_k_, field_230709_l_, 100, imageUUID);
    }

    public static void drawImage(Minecraft minecraft, int width, int height, float zLevel, UUID uuid) {
        RenderSystem.pushMatrix();

        ResourceLocation location = TextureCache.instance().getImage(uuid);

        float imageWidth = 12F;
        float imageHeight = 8F;


        if (location == null) {
            minecraft.getTextureManager().bindTexture(DEFAULT_IMAGE);
        } else {
            minecraft.getTextureManager().bindTexture(location);
            NativeImage image = TextureCache.instance().getNativeImage(uuid);
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

        buffer.pos(left, top, zLevel).tex(0F, 0F).endVertex();
        buffer.pos(left, top + hnew, zLevel).tex(0F, 1F).endVertex();
        buffer.pos(left + wnew, top + hnew, zLevel).tex(1F, 1F).endVertex();
        buffer.pos(left + wnew, top, zLevel).tex(1F, 0F).endVertex();

        tessellator.draw();

        RenderSystem.popMatrix();
    }

    @Override
    protected void func_230451_b_(MatrixStack p_230451_1_, int p_230451_2_, int p_230451_3_) {

    }
}
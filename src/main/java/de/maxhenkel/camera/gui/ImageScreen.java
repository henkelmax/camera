package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.TextureCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.UUID;

public class ImageScreen extends AbstractContainerScreen<AbstractContainerMenu> {

    public static final ResourceLocation DEFAULT_IMAGE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/images/default_image.png");

    @Nullable
    private UUID imageID;

    public ImageScreen(ItemStack image) {
        super(new DummyContainer(), Minecraft.getInstance().player.getInventory(), Component.translatable("gui.image.title"));

        ImageData imageData = ImageData.fromStack(image);
        if (imageData != null) {
            imageID = imageData.getId();
        }
    }

    //https://stackoverflow.com/questions/6565703/math-algorithm-fit-image-to-screen-retain-aspect-ratio
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBlurredBackground();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        if (imageID == null) {
            return;
        }

        drawImage(guiGraphics, width, height, 100, imageID);
    }

    public static void drawImage(GuiGraphics guiGraphics, int width, int height, float zLevel, UUID uuid) {
        guiGraphics.pose().pushPose();
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);


        ResourceLocation location = TextureCache.instance().getImage(uuid);

        float imageWidth = 12F;
        float imageHeight = 8F;


        if (location == null) {
            RenderSystem.setShaderTexture(0, DEFAULT_IMAGE);
        } else {
            RenderSystem.setShaderTexture(0, location);
            NativeImage image = TextureCache.instance().getNativeImage(uuid);
            imageWidth = (float) image.getWidth();
            imageHeight = (float) image.getHeight();
        }

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

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

        Matrix4f matrix = guiGraphics.pose().last().pose();
        buffer.addVertex(matrix, left, top, zLevel).setUv(0F, 0F);
        buffer.addVertex(matrix, left, top + hnew, zLevel).setUv(0F, 1F);
        buffer.addVertex(matrix, left + wnew, top + hnew, zLevel).setUv(1F, 1F);
        buffer.addVertex(matrix, left + wnew, top, zLevel).setUv(1F, 0F);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        guiGraphics.pose().popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int x, int y) {

    }

    @Override
    protected void renderBg(GuiGraphics p_283065_, float p_97788_, int p_97789_, int p_97790_) {

    }
}
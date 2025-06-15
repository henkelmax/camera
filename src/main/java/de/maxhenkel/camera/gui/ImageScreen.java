package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.platform.NativeImage;
import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.TextureCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

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
        renderBlurredBackground(guiGraphics);

        if (imageID == null) {
            return;
        }

        drawImage(guiGraphics, width, height, imageID);
    }

    public static void drawImage(GuiGraphics guiGraphics, int width, int height, UUID uuid) {
        ResourceLocation location = TextureCache.instance().getImage(uuid);
        int imageWidth;
        int imageHeight;


        if (location == null) {
            location = DEFAULT_IMAGE;
            imageWidth = 12;
            imageHeight = 8;
        } else {
            NativeImage image = TextureCache.instance().getNativeImage(uuid);
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();
        }

        float scale = 0.8F;

        float ws = (float) width * scale;
        float hs = (float) height * scale;

        float rs = ws / hs;
        float ri = (float) imageWidth / (float) imageHeight;

        float hnew;
        float wnew;

        if (rs > ri) {
            wnew = (float) imageWidth * hs / (float) imageHeight;
            hnew = hs;
        } else {
            wnew = ws;
            hnew = (float) imageHeight * ws / (float) imageWidth;
        }

        float top = (hs - hnew) / 2F;
        float left = (ws - wnew) / 2F;

        left += ((1F - scale) * ws) / 2F;
        top += ((1F - scale) * hs) / 2F;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, location, (int) left, (int) top, 0F, 0F, (int) wnew, (int) hnew, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int x, int y) {

    }

    @Override
    protected void renderBg(GuiGraphics p_283065_, float p_97788_, int p_97789_, int p_97790_) {

    }
}
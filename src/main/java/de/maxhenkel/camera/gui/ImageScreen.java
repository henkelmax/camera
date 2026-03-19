package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.platform.NativeImage;
import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.CameraMod;
import de.maxhenkel.camera.TextureCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

public class ImageScreen extends AbstractContainerScreen<AbstractContainerMenu> {

    public static final Identifier DEFAULT_IMAGE = Identifier.fromNamespaceAndPath(CameraMod.MODID, "textures/images/default_image.png");

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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        extractBlurredBackground(graphics);

        if (imageID == null) {
            return;
        }

        drawImage(graphics, width, height, imageID);
    }

    public static void drawImage(GuiGraphicsExtractor guiGraphics, int width, int height, UUID uuid) {
        Identifier location = TextureCache.instance().getImage(uuid);
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
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {

    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

    }
}
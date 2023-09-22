package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.ClientImageUploadManager;
import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.Shaders;
import de.maxhenkel.camera.net.MessageRequestUploadCustomImage;
import de.maxhenkel.camera.net.MessageSetShader;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corelib.net.NetUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

public class CameraScreen extends ScreenBase<AbstractContainerMenu> {

    private static final ResourceLocation CAMERA_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/camera.png");
    private static final int FONT_COLOR = 4210752;
    private static final int PADDING = 10;
    private static final int BUTTON_WIDTH = 70;
    private static final int BUTTON_HEIGHT = 20;

    private int index = 0;

    @Nullable
    private Button upload;

    public CameraScreen(String currentShader) {
        super(CAMERA_TEXTURE, new DummyContainer(), Minecraft.getInstance().player.getInventory(), Component.translatable("gui.camera.title"));
        imageWidth = 248;
        imageHeight = 109;

        for (int i = 0; i < Shaders.SHADER_LIST.size(); i++) {
            String s = Shaders.SHADER_LIST.get(i);
            if (currentShader == null) {
                if (s.equals("none")) {
                    index = i;
                    break;
                }
            } else if (s.equals(currentShader)) {
                index = i;
                break;
            }
        }
    }

    // https://github.com/MinecraftForge/MinecraftForge/commit/007cd42ec6eed0e023c1324525cd44484ee0e79c
    @Override
    protected void init() {
        super.init();
        clearWidgets();
        addRenderableWidget(Button.builder(Component.translatable("button.camera.prev"), button -> {
            index--;
            if (index < 0) {
                index = Shaders.SHADER_LIST.size() - 1;
            }
            sendShader();
        }).bounds(leftPos + PADDING, topPos + PADDING + font.lineHeight + PADDING, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("button.camera.next"), button -> {
            index++;
            if (index >= Shaders.SHADER_LIST.size()) {
                index = 0;
            }
            sendShader();
        }).bounds(leftPos + imageWidth - BUTTON_WIDTH - PADDING, topPos + PADDING + font.lineHeight + PADDING, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        if (Main.SERVER_CONFIG.allowImageUpload.get()) {
            upload = addRenderableWidget(Button.builder(Component.translatable("button.camera.upload"), button -> {
                ImageTools.chooseImage(file -> {
                    try {
                        UUID uuid = UUID.randomUUID();
                        BufferedImage image = ImageTools.loadImage(file);
                        ClientImageUploadManager.addImage(uuid, image);
                        NetUtils.sendToServer(Main.SIMPLE_CHANNEL, new MessageRequestUploadCustomImage(uuid));
                    } catch (IOException e) {
                        minecraft.player.displayClientMessage(Component.translatable("message.upload_error", e.getMessage()), true);
                        e.printStackTrace();
                    }
                    minecraft.screen = null;
                });
            }).bounds(leftPos + imageWidth / 2 - BUTTON_WIDTH / 2, topPos + imageHeight - BUTTON_HEIGHT - PADDING, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (upload != null) {
            upload.active = !ImageTools.isFileChooserOpen();
        }
    }

    private void sendShader() {
        NetUtils.sendToServer(Main.SIMPLE_CHANNEL, new MessageSetShader(Shaders.SHADER_LIST.get(index)));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        MutableComponent chooseFilter = Component.translatable("gui.camera.choose_filter");
        int chooseFilterWidth = font.width(chooseFilter);
        guiGraphics.drawString(font, chooseFilter.getVisualOrderText(), imageWidth / 2 - chooseFilterWidth / 2, 10, FONT_COLOR, false);

        MutableComponent shaderName = Component.translatable("shader." + Shaders.SHADER_LIST.get(index));
        int shaderWidth = font.width(shaderName);
        guiGraphics.drawString(font, shaderName.getVisualOrderText(), imageWidth / 2 - shaderWidth / 2, PADDING + font.lineHeight + PADDING + BUTTON_HEIGHT / 2 - font.lineHeight / 2, ChatFormatting.WHITE.getColor(), false);

        MutableComponent uploadImage = Component.translatable("gui.camera.upload_image");
        int uploadImageWidth = font.width(uploadImage);
        guiGraphics.drawString(font, uploadImage.getVisualOrderText(), imageWidth / 2 - uploadImageWidth / 2, imageHeight - PADDING - BUTTON_HEIGHT - PADDING - font.lineHeight, FONT_COLOR, false);
    }

}
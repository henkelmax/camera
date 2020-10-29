package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.camera.*;
import de.maxhenkel.camera.net.MessageRequestUploadCustomImage;
import de.maxhenkel.camera.net.MessageSetShader;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CameraScreen extends ContainerScreen {

    private static final ResourceLocation CAMERA_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/camera.png");
    private static final int FONT_COLOR = 4210752;

    private int index = 0;

    private Button upload;

    public CameraScreen(String currentShader) {
        super(new DummyContainer(), null, new TranslationTextComponent("gui.camera.title"));
        xSize = 248;
        ySize = 109;

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

    @Override
    protected void init() {
        super.init();
        buttons.clear();
        int padding = 10;
        int buttonWidth = 70;
        int buttonHeight = 20;
        addButton(new Button(guiLeft + padding, guiTop + ySize / 2 - buttonHeight / 2, buttonWidth, buttonHeight, new TranslationTextComponent("button.camera.prev").getFormattedText(), button -> {
            index--;
            if (index < 0) {
                index = Shaders.SHADER_LIST.size() - 1;
            }
            sendShader();
        }));
        addButton(new Button(guiLeft + xSize - buttonWidth - padding, guiTop + ySize / 2 - buttonHeight / 2, buttonWidth, buttonHeight, new TranslationTextComponent("button.camera.next").getFormattedText(), button -> {
            index++;
            if (index >= Shaders.SHADER_LIST.size()) {
                index = 0;
            }
            sendShader();
        }));

        if (Config.SERVER.ALLOW_IMAGE_UPLOAD.get()) {
            upload = addButton(new Button(guiLeft + xSize / 2 - buttonWidth / 2, height / 2 + ySize / 2 - buttonHeight - padding, buttonWidth, buttonHeight, new TranslationTextComponent("button.camera.upload").getFormattedText(), button -> {
                ImageTools.chooseImage(file -> {
                    try {
                        UUID uuid = UUID.randomUUID();
                        BufferedImage image = ImageTools.loadImage(file);
                        ClientImageUploadManager.addImage(uuid, image);
                        Main.SIMPLE_CHANNEL.sendToServer(new MessageRequestUploadCustomImage(uuid));
                    } catch (IOException e) {
                        playerInventory.player.sendMessage(new TranslationTextComponent("message.upload_error", e.getMessage()));
                        e.printStackTrace();
                    }
                    minecraft.currentScreen = null;
                });
            }));
            upload.active = ImageTools.isFileChooserAvailable();
        }
    }

    private void sendShader() {
        Main.SIMPLE_CHANNEL.sendToServer(new MessageSetShader(Shaders.SHADER_LIST.get(index)));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        String title = new TranslationTextComponent("gui.camera.choosefilter").getFormattedText();

        int titleWidth = font.getStringWidth(title);

        font.drawString(title, xSize / 2 - titleWidth / 2, 10, FONT_COLOR);

        String shaderName = new TranslationTextComponent("shader." + Shaders.SHADER_LIST.get(index)).getFormattedText();

        int shaderWidth = font.getStringWidth(shaderName);

        font.drawStringWithShadow(shaderName, xSize / 2 - shaderWidth / 2, ySize / 2 - font.FONT_HEIGHT / 2, 0xFFFFFFFF);

        if (upload != null && upload.isHovered() && !ImageTools.isFileChooserAvailable()) {
            List<String> list = new ArrayList<>();
            list.add(new TranslationTextComponent("message.camera.no_java_fx").getUnformattedComponentText());
            renderTooltip(list, x - guiLeft, y - guiTop);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        renderBackground();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(CAMERA_TEXTURE);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
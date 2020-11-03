package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.camera.ClientImageUploadManager;
import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.Shaders;
import de.maxhenkel.camera.net.MessageRequestUploadCustomImage;
import de.maxhenkel.camera.net.MessageSetShader;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CameraScreen extends ScreenBase<Container> {

    private static final ResourceLocation CAMERA_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/camera.png");
    private static final int FONT_COLOR = 4210752;
    private static final int PADDING = 10;
    private static final int BUTTON_WIDTH = 70;
    private static final int BUTTON_HEIGHT = 20;

    private int index = 0;

    private Button prev;
    private Button next;
    private Button upload;

    public CameraScreen(String currentShader) {
        super(CAMERA_TEXTURE, new DummyContainer(), null, new TranslationTextComponent("gui.camera.title"));
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
        prev = addButton(new Button(guiLeft + PADDING, guiTop + ySize / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT, new TranslationTextComponent("button.camera.prev"), button -> {
            index--;
            if (index < 0) {
                index = Shaders.SHADER_LIST.size() - 1;
            }
            sendShader();
        }));
        prev.active = false; //TODO fix shaders
        next = addButton(new Button(guiLeft + xSize - BUTTON_WIDTH - PADDING, guiTop + ySize / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT, new TranslationTextComponent("button.camera.next"), button -> {
            index++;
            if (index >= Shaders.SHADER_LIST.size()) {
                index = 0;
            }
            sendShader();
        }));
        next.active = false; //TODO fix shaders

        if (Main.SERVER_CONFIG.allowImageUpload.get()) {
            upload = addButton(new Button(guiLeft + xSize / 2 - BUTTON_WIDTH / 2, height / 2 + ySize / 2 - BUTTON_HEIGHT - PADDING, BUTTON_WIDTH, BUTTON_HEIGHT, new TranslationTextComponent("button.camera.upload"), button -> {
                ImageTools.chooseImage(file -> {
                    try {
                        UUID uuid = UUID.randomUUID();
                        BufferedImage image = ImageTools.loadImage(file);
                        ClientImageUploadManager.addImage(uuid, image);
                        Main.SIMPLE_CHANNEL.sendToServer(new MessageRequestUploadCustomImage(uuid));
                    } catch (IOException e) {
                        playerInventory.player.sendMessage(new TranslationTextComponent("message.upload_error", e.getMessage()), playerInventory.player.getUniqueID());
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
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);

        TranslationTextComponent title = new TranslationTextComponent("gui.camera.choosefilter");

        int titleWidth = font.getStringPropertyWidth(title);

        font.func_238422_b_(matrixStack, title.func_241878_f(), xSize / 2 - titleWidth / 2, 10, FONT_COLOR);

        TranslationTextComponent shaderName = new TranslationTextComponent("shader." + Shaders.SHADER_LIST.get(index));

        int shaderWidth = font.getStringPropertyWidth(shaderName);

        font.func_238422_b_(matrixStack, shaderName.func_241878_f(), xSize / 2 - shaderWidth / 2, ySize / 2 - font.FONT_HEIGHT / 2, TextFormatting.WHITE.getColor());

        if (prev.isHovered() || next.isHovered()) {
            List<IReorderingProcessor> list = new ArrayList<>();
            list.add(new TranslationTextComponent("message.camera.filters_unavailable").func_241878_f());
            renderTooltip(matrixStack, list, mouseX - guiLeft, mouseY - guiTop);
        }
        if (upload != null && upload.isHovered() && !ImageTools.isFileChooserAvailable()) {
            List<IReorderingProcessor> list = new ArrayList<>();
            list.add(new TranslationTextComponent("message.camera.no_java_fx").func_241878_f());
            renderTooltip(matrixStack, list, mouseX - guiLeft, mouseY - guiTop);
        }
    }

}
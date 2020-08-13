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
    protected void func_231160_c_() {
        super.func_231160_c_();
        field_230710_m_.clear();
        Button prev = func_230480_a_(new Button(guiLeft + PADDING, guiTop + ySize / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT, new TranslationTextComponent("button.camera.prev"), button -> {
            index--;
            if (index < 0) {
                index = Shaders.SHADER_LIST.size() - 1;
            }
            sendShader();
        }));
        prev.field_230693_o_ = false; //TODO fix shaders
        Button next = func_230480_a_(new Button(guiLeft + xSize - BUTTON_WIDTH - PADDING, guiTop + ySize / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT, new TranslationTextComponent("button.camera.next"), button -> {
            index++;
            if (index >= Shaders.SHADER_LIST.size()) {
                index = 0;
            }
            sendShader();
        }));
        next.field_230693_o_ = false; //TODO fix shaders

        if (Main.SERVER_CONFIG.allowImageUpload.get()) {
            func_230480_a_(new Button(guiLeft + xSize / 2 - BUTTON_WIDTH / 2, field_230709_l_ / 2 + ySize / 2 - BUTTON_HEIGHT - PADDING, BUTTON_WIDTH, BUTTON_HEIGHT, new TranslationTextComponent("button.camera.upload"), button -> {
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
                    field_230706_i_.currentScreen = null;
                });
            }));
        }
    }

    private void sendShader() {
        Main.SIMPLE_CHANNEL.sendToServer(new MessageSetShader(Shaders.SHADER_LIST.get(index)));
    }

    @Override
    protected void func_230451_b_(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.func_230451_b_(matrixStack, mouseX, mouseY);

        String title = new TranslationTextComponent("gui.camera.choosefilter").getString();

        int titleWidth = field_230712_o_.getStringWidth(title);

        field_230712_o_.func_238421_b_(matrixStack, title, xSize / 2 - titleWidth / 2, 10, FONT_COLOR);

        String shaderName = new TranslationTextComponent("shader." + Shaders.SHADER_LIST.get(index)).getString();

        int shaderWidth = field_230712_o_.getStringWidth(shaderName);

        field_230712_o_.func_238421_b_(matrixStack, shaderName, xSize / 2 - shaderWidth / 2, ySize / 2 - field_230712_o_.FONT_HEIGHT / 2, TextFormatting.WHITE.getColor());

        if (isHoveringButton(mouseX, mouseY)) {
            List<IReorderingProcessor> list = new ArrayList<>();
            list.add(new TranslationTextComponent("message.camera.filters_unavailable").func_241878_f());
            func_238654_b_(matrixStack, list, mouseX - guiLeft, mouseY - guiTop);
        }
    }

    private boolean isHoveringButton(int mouseX, int mouseY) {
        if (mouseX >= guiLeft + PADDING && mouseX < guiLeft + PADDING + BUTTON_WIDTH) {
            if (mouseY >= guiTop + ySize / 2 - BUTTON_HEIGHT / 2 && mouseY < guiTop + ySize / 2 - BUTTON_HEIGHT / 2 + BUTTON_HEIGHT) {
                return true;
            }
        }
        if (mouseX >= guiLeft + xSize - BUTTON_WIDTH - PADDING && mouseX < guiLeft + xSize - PADDING) {
            if (mouseY >= guiTop + ySize / 2 - BUTTON_HEIGHT / 2 && mouseY < guiTop + ySize / 2 + BUTTON_HEIGHT / 2) {
                return true;
            }
        }
        return false;
    }
}
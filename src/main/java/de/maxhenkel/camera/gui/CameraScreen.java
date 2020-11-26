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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

public class CameraScreen extends ScreenBase<Container> {

    private static final ResourceLocation CAMERA_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/camera.png");
    private static final int FONT_COLOR = 4210752;
    private static final int PADDING = 10;
    private static final int BUTTON_WIDTH = 70;
    private static final int BUTTON_HEIGHT = 20;

    private int index = 0;

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
        addButton(new Button(guiLeft + PADDING, guiTop + PADDING + font.FONT_HEIGHT + PADDING, BUTTON_WIDTH, BUTTON_HEIGHT, new TranslationTextComponent("button.camera.prev"), button -> {
            //TODO fix shaders
            /*index--;
            if (index < 0) {
                index = Shaders.SHADER_LIST.size() - 1;
            }
            sendShader();*/
            showBugMessage();
        }));
        addButton(new Button(guiLeft + xSize - BUTTON_WIDTH - PADDING, guiTop + PADDING + font.FONT_HEIGHT + PADDING, BUTTON_WIDTH, BUTTON_HEIGHT, new TranslationTextComponent("button.camera.next"), button -> {
            //TODO fix shaders
            /*index++;
            if (index >= Shaders.SHADER_LIST.size()) {
                index = 0;
            }
            sendShader();*/
            showBugMessage();
        }));

        if (Main.SERVER_CONFIG.allowImageUpload.get()) {
            upload = addButton(new Button(guiLeft + xSize / 2 - BUTTON_WIDTH / 2, guiTop + ySize - BUTTON_HEIGHT - PADDING, BUTTON_WIDTH, BUTTON_HEIGHT, new TranslationTextComponent("button.camera.upload"), button -> {
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
        }
    }

    @Override
    public void tick() {
        super.tick();
        upload.active = !ImageTools.isFileChooserOpen();
    }

    private void sendShader() {
        Main.SIMPLE_CHANNEL.sendToServer(new MessageSetShader(Shaders.SHADER_LIST.get(index)));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);

        TranslationTextComponent chooseFilter = new TranslationTextComponent("gui.camera.choose_filter");
        int chooseFilterWidth = font.getStringPropertyWidth(chooseFilter);
        font.func_238422_b_(matrixStack, chooseFilter.func_241878_f(), xSize / 2 - chooseFilterWidth / 2, 10, FONT_COLOR);

        TranslationTextComponent shaderName = new TranslationTextComponent("shader." + Shaders.SHADER_LIST.get(index));
        int shaderWidth = font.getStringPropertyWidth(shaderName);
        font.func_238422_b_(matrixStack, shaderName.func_241878_f(), xSize / 2 - shaderWidth / 2, PADDING + font.FONT_HEIGHT + PADDING + BUTTON_HEIGHT / 2 - font.FONT_HEIGHT / 2, TextFormatting.WHITE.getColor());

        TranslationTextComponent uploadImage = new TranslationTextComponent("gui.camera.upload_image");
        int uploadImageWidth = font.getStringPropertyWidth(uploadImage);
        font.func_238422_b_(matrixStack, uploadImage.func_241878_f(), xSize / 2 - uploadImageWidth / 2, ySize - PADDING - BUTTON_HEIGHT - PADDING - font.FONT_HEIGHT, FONT_COLOR);
    }

    private void showBugMessage() {
        minecraft.displayGuiScreen(null);
        minecraft.player.sendMessage(
                new StringTextComponent("Due to a Minecraft bug, filters do not work.\nPlease upvote this issue to get it fixed! ")
                        .append(TextComponentUtils.wrapWithSquareBrackets(new StringTextComponent("MC-194675")).modifyStyle(style -> style
                                .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://bugs.mojang.com/browse/MC-194675"))
                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("https://bugs.mojang.com/browse/MC-194675"))))
                                .mergeStyle(TextFormatting.GREEN)
                        )
                , Util.DUMMY_UUID);
    }

}
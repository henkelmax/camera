package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.Shaders;
import de.maxhenkel.camera.net.MessageSetShader;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class CameraScreen extends ContainerScreen {

    private static final ResourceLocation CAMERA_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/camera.png");
    private static final int FONT_COLOR = 4210752;

    private int index = 0;

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
        int left = (width - xSize) / 2;
        int padding = 10;
        int buttonWidth = 75;
        int buttonHeight = 20;
        addButton(new Button(left + padding, height / 2 + ySize / 2 - buttonHeight - padding, buttonWidth, buttonHeight, new TranslationTextComponent("button.prev").getFormattedText(), (button) -> {
            index--;
            if (index < 0) {
                index = Shaders.SHADER_LIST.size() - 1;
            }
            sendShader();
        }));
        addButton(new Button(left + xSize - buttonWidth - padding, height / 2 + ySize / 2 - buttonHeight - padding, buttonWidth, buttonHeight, new TranslationTextComponent("button.next").getFormattedText(), (button) -> {
            index++;
            if (index >= Shaders.SHADER_LIST.size()) {
                index = 0;
            }
            sendShader();
        }));
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

        font.drawStringWithShadow(shaderName, xSize / 2 - shaderWidth / 2, 40, 0xFFFFFFFF);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        renderBackground();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(CAMERA_TEXTURE);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.Shaders;
import de.maxhenkel.camera.net.MessageSetShader;
import de.maxhenkel.camera.proxy.CommonProxy;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;

public class GuiCamera extends GuiContainer {

    private static final ResourceLocation CAMERA_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/camera.png");
    private static final int FONT_COLOR = 4210752;

    private int index = 0;

    public GuiCamera(String currentShader) {
        super(new ContainerImage());
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
    public void initGui() {
        super.initGui();

        buttonList.clear();
        int left = (width - xSize) / 2;
        int padding = 10;
        int buttonWidth = 75;
        int buttonHeight = 20;
        addButton(new GuiButton(0, left + padding, height / 2 + ySize / 2 - buttonHeight - padding, buttonWidth, buttonHeight, new TextComponentTranslation("button.prev").getFormattedText()));
        addButton(new GuiButton(1, left + xSize - buttonWidth - padding, height / 2 + ySize / 2 - buttonHeight - padding, buttonWidth, buttonHeight, new TextComponentTranslation("button.next").getFormattedText()));
    }

    private void sendShader() {
        CommonProxy.simpleNetworkWrapper.sendToServer(new MessageSetShader(Shaders.SHADER_LIST.get(index)));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {

        if (button.id == 0) {
            index--;
            if (index < 0) {
                index = Shaders.SHADER_LIST.size() - 1;
            }
            sendShader();
        } else if (button.id == 1) {
            index++;
            if (index >= Shaders.SHADER_LIST.size()) {
                index = 0;
            }
            sendShader();
        }

        super.actionPerformed(button);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        String title = new TextComponentTranslation("gui.camera.choosefilter").getFormattedText();

        int titleWidth = fontRenderer.getStringWidth(title);

        fontRenderer.drawString(title, xSize / 2 - titleWidth / 2, 10, FONT_COLOR);

        String shaderName = new TextComponentTranslation("shader." + Shaders.SHADER_LIST.get(index)).getFormattedText();

        int shaderWidth = fontRenderer.getStringWidth(shaderName);

        fontRenderer.drawStringWithShadow(shaderName, xSize / 2 - shaderWidth / 2, 40, 0xFFFFFFFF);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(CAMERA_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
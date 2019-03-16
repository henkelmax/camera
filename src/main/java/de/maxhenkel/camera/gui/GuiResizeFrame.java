package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.EntityImage;
import de.maxhenkel.camera.net.MessageResizeFrame;
import de.maxhenkel.camera.proxy.CommonProxy;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;
import java.util.UUID;

public class GuiResizeFrame extends GuiContainer {

    private static final ResourceLocation CAMERA_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/camera.png");
    private static final int FONT_COLOR = 4210752;

    private UUID uuid;

    public GuiResizeFrame(UUID uuid) {
        super(new ContainerResizeFrame());
        this.uuid = uuid;
        xSize = 248;
        ySize = 109;
    }

    @Override
    public void initGui() {
        super.initGui();

        buttonList.clear();
        int left = (width - xSize) / 2;
        int padding = 10;
        int buttonWidth = 50;
        int buttonHeight = 20;
        addButton(new GuiButton(0, left + padding, height / 2 - buttonHeight / 2, buttonWidth, buttonHeight, new TextComponentTranslation("button.left").getFormattedText()));

        addButton(new GuiButton(1, left + xSize - buttonWidth - padding, height / 2 - buttonHeight / 2, buttonWidth, buttonHeight, new TextComponentTranslation("button.right").getFormattedText()));

        addButton(new GuiButton(2, width / 2 - buttonWidth / 2, guiTop + padding, buttonWidth, buttonHeight, new TextComponentTranslation("button.up").getFormattedText()));

        addButton(new GuiButton(3, width / 2 - buttonWidth / 2, guiTop + ySize - padding - buttonHeight, buttonWidth, buttonHeight, new TextComponentTranslation("button.down").getFormattedText()));

    }

    private void sendMoveImage(MessageResizeFrame.Direction direction) {
        CommonProxy.simpleNetworkWrapper.sendToServer(new MessageResizeFrame(uuid, direction, !GuiScreen.isShiftKeyDown()));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {

        if (button.id == 0) {
            sendMoveImage(MessageResizeFrame.Direction.LEFT);
        } else if (button.id == 1) {
            sendMoveImage(MessageResizeFrame.Direction.RIGHT);
        } else if (button.id == 2) {
            sendMoveImage(MessageResizeFrame.Direction.UP);
        } else if (button.id == 3) {
            sendMoveImage(MessageResizeFrame.Direction.DOWN);
        }

        super.actionPerformed(button);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        String title = new TextComponentTranslation("gui.frame.resize").getFormattedText();

        int titleWidth = fontRenderer.getStringWidth(title);
        fontRenderer.drawString(title, xSize / 2 - titleWidth / 2, ySize / 2 - fontRenderer.FONT_HEIGHT / 2, FONT_COLOR);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(CAMERA_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    private long lastCheck;


    @Override
    public void updateScreen() {
        super.updateScreen();

        if (System.currentTimeMillis() - lastCheck > 500L) {
            if (!isImagePresent()) {
                mc.player.closeScreen();
            }
            lastCheck = System.currentTimeMillis();
        }
    }

    public boolean isImagePresent() {
        return mc.player.world.getEntities(EntityImage.class, image -> image.getUniqueID().equals(uuid)).size() > 0;
    }
}
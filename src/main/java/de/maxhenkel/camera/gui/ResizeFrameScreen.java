package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.ImageEntity;
import de.maxhenkel.camera.net.MessageResizeFrame;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.UUID;

public class ResizeFrameScreen extends ContainerScreen {

    private static final ResourceLocation CAMERA_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/camera.png");
    private static final int FONT_COLOR = 4210752;

    private UUID uuid;

    public ResizeFrameScreen(UUID uuid) {
        super(new DummyContainer(), null, new TranslationTextComponent("gui.frame.resize")); //TODO
        this.uuid = uuid;
        xSize = 248;
        ySize = 109;
    }

    @Override
    protected void init() {
        super.init();

        buttons.clear();
        int left = (width - xSize) / 2;
        int padding = 10;
        int buttonWidth = 50;
        int buttonHeight = 20;
        addButton(new Button(left + padding, height / 2 - buttonHeight / 2, buttonWidth, buttonHeight, new TranslationTextComponent("button.frame.left").getFormattedText(), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.LEFT);
        }));

        addButton(new Button(left + xSize - buttonWidth - padding, height / 2 - buttonHeight / 2, buttonWidth, buttonHeight, new TranslationTextComponent("button.frame.right").getFormattedText(), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.RIGHT);
        }));

        addButton(new Button(width / 2 - buttonWidth / 2, guiTop + padding, buttonWidth, buttonHeight, new TranslationTextComponent("button.frame.up").getFormattedText(), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.UP);
        }));

        addButton(new Button(width / 2 - buttonWidth / 2, guiTop + ySize - padding - buttonHeight, buttonWidth, buttonHeight, new TranslationTextComponent("button.frame.down").getFormattedText(), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.DOWN);
        }));
    }

    private void sendMoveImage(MessageResizeFrame.Direction direction) {
        Main.SIMPLE_CHANNEL.sendToServer(new MessageResizeFrame(uuid, direction, !Screen.hasShiftDown()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        String title = new TranslationTextComponent("gui.frame.resize").getFormattedText();

        int titleWidth = font.getStringWidth(title);
        font.drawString(title, xSize / 2 - titleWidth / 2, ySize / 2 - font.FONT_HEIGHT / 2, FONT_COLOR);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        renderBackground();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(CAMERA_TEXTURE);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    private long lastCheck;

    @Override
    public void tick() {
        super.tick();

        if (System.currentTimeMillis() - lastCheck > 500L) {
            if (!isImagePresent()) {
                minecraft.player.closeScreen();
            }
            lastCheck = System.currentTimeMillis();
        }
    }

    public boolean isImagePresent() {
        AxisAlignedBB aabb = minecraft.player.getBoundingBox();
        if (aabb == null) {
            return false;
        }
        aabb = aabb.grow(10D);
        return minecraft.world.getEntitiesWithinAABB(ImageEntity.class, aabb).stream().anyMatch(image -> image.getUniqueID().equals(uuid) && image.getDistance(minecraft.player) <= 5);
    }
}
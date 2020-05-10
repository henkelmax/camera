package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.camera.Config;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.ImageEntity;
import de.maxhenkel.camera.net.MessageResizeFrame;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.UUID;

public class ResizeFrameScreen extends ContainerScreen {

    private static final ResourceLocation CAMERA_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/resize_frame.png");
    private static final int PADDING = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 50;

    private UUID uuid;
    private float visibility;
    private Button visibilityButton;

    public ResizeFrameScreen(UUID uuid) {
        super(new DummyContainer(), null, new TranslationTextComponent("gui.frame.resize"));
        this.uuid = uuid;
        visibility = Config.CLIENT.RESIZE_GUI_OPACITY.get().floatValue();
        xSize = 248;
        ySize = 109;
    }

    @Override
    protected void init() {
        super.init();

        buttons.clear();
        int left = (width - xSize) / 2;
        addButton(new Button(left + PADDING, height / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT, "", (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.LEFT);
        }));

        addButton(new Button(left + xSize - BUTTON_WIDTH - PADDING, height / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT, "", (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.RIGHT);
        }));

        addButton(new Button(width / 2 - BUTTON_WIDTH / 2, guiTop + PADDING, BUTTON_WIDTH, BUTTON_HEIGHT, "", (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.UP);
        }));

        addButton(new Button(width / 2 - BUTTON_WIDTH / 2, guiTop + ySize - PADDING - BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT, "", (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.DOWN);
        }));

        visibilityButton = new Button(left + xSize - 20 - PADDING, guiTop + PADDING, 20, 20, new TranslationTextComponent("tooltip.visibility_short").getFormattedText(), (button) -> {
            visibility -= 0.25;
            if (visibility < 0F) {
                visibility = 1F;
            }
            Config.CLIENT.RESIZE_GUI_OPACITY.set((double) visibility);
            Config.CLIENT.RESIZE_GUI_OPACITY.save();
        });
        addButton(visibilityButton);
    }

    private void sendMoveImage(MessageResizeFrame.Direction direction) {
        Main.SIMPLE_CHANNEL.sendToServer(new MessageResizeFrame(uuid, direction, !Screen.hasShiftDown()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        String title = new TranslationTextComponent("gui.frame.resize").getFormattedText();
        int titleWidth = font.getStringWidth(title);
        font.drawString(title, xSize / 2 - titleWidth / 2, ySize / 2 - font.FONT_HEIGHT - 1, TextFormatting.DARK_GRAY.getColor());

        String description = new TranslationTextComponent("gui.frame.resize_description").getFormattedText();
        int descriptionWidth = font.getStringWidth(description);
        font.drawString(description, xSize / 2 - descriptionWidth / 2, ySize / 2 + 1, TextFormatting.GRAY.getColor());

        minecraft.getTextureManager().bindTexture(CAMERA_TEXTURE);
        if (Screen.hasShiftDown()) {
            blit(xSize / 2 - 8, PADDING + 2, 16, 109, 16, 16);
            blit(xSize / 2 - 8, ySize - PADDING - BUTTON_HEIGHT + 2, 0, 109, 16, 16);
            blit(PADDING + BUTTON_WIDTH / 2 - 8, ySize / 2 - BUTTON_HEIGHT / 2 + 3, 0, 125, 16, 16);
            blit(xSize - PADDING - BUTTON_WIDTH / 2 - 8, ySize / 2 - BUTTON_HEIGHT / 2 + 3, 16, 125, 16, 16);
        } else {
            blit(xSize / 2 - 8, PADDING + 2, 0, 109, 16, 16);
            blit(xSize / 2 - 8, ySize - PADDING - BUTTON_HEIGHT + 2, 16, 109, 16, 16);
            blit(PADDING + BUTTON_WIDTH / 2 - 8, ySize / 2 - BUTTON_HEIGHT / 2 + 3, 16, 125, 16, 16);
            blit(xSize - PADDING - BUTTON_WIDTH / 2 - 8, ySize / 2 - BUTTON_HEIGHT / 2 + 3, 0, 125, 16, 16);
        }

        if (visibilityButton.isHovered()) {
            renderTooltip(new TranslationTextComponent("tooltip.visibility").getFormattedText(), x - guiLeft, y - guiTop);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (visibility >= 1F) {
            renderBackground();
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, visibility);
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
        aabb = aabb.grow(32D);
        return minecraft.world.getEntitiesWithinAABB(ImageEntity.class, aabb).stream().anyMatch(image -> image.getUniqueID().equals(uuid) && image.getDistance(minecraft.player) <= 32F);
    }
}
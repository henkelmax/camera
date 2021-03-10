package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.ImageEntity;
import de.maxhenkel.camera.net.MessageResizeFrame;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.UUID;

public class ResizeFrameScreen extends ContainerScreen<Container> {

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
        visibility = Main.CLIENT_CONFIG.resizeGuiOpacity.get().floatValue();
        imageWidth = 248;
        imageHeight = 109;
    }

    @Override
    protected void init() {
        super.init();

        buttons.clear();
        int left = (width - imageWidth) / 2;
        addButton(new Button(left + PADDING, height / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT, new StringTextComponent(""), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.LEFT);
        }));

        addButton(new Button(left + imageWidth - BUTTON_WIDTH - PADDING, height / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT, new StringTextComponent(""), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.RIGHT);
        }));

        addButton(new Button(width / 2 - BUTTON_WIDTH / 2, topPos + PADDING, BUTTON_WIDTH, BUTTON_HEIGHT, new StringTextComponent(""), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.UP);
        }));

        addButton(new Button(width / 2 - BUTTON_WIDTH / 2, topPos + imageHeight - PADDING - BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT, new StringTextComponent(""), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.DOWN);
        }));

        visibilityButton = addButton(new Button(left + imageWidth - 20 - PADDING, topPos + PADDING, 20, 20, new TranslationTextComponent("tooltip.visibility_short"), (button) -> {
            visibility -= 0.25;
            if (visibility < 0F) {
                visibility = 1F;
            }
            Main.CLIENT_CONFIG.resizeGuiOpacity.set((double) visibility);
            Main.CLIENT_CONFIG.resizeGuiOpacity.save();
        }));
    }

    private void sendMoveImage(MessageResizeFrame.Direction direction) {
        Main.SIMPLE_CHANNEL.sendToServer(new MessageResizeFrame(uuid, direction, !Screen.hasShiftDown()));
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
        if (visibility >= 1F) {
            renderBackground(matrixStack);
        }
        RenderSystem.color4f(1F, 1F, 1F, visibility);
        minecraft.getTextureManager().bind(CAMERA_TEXTURE);

        blit(matrixStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int x, int y) {
        TranslationTextComponent title = new TranslationTextComponent("gui.frame.resize");
        int titleWidth = font.width(title);
        font.draw(matrixStack, title.getVisualOrderText(), imageWidth / 2 - titleWidth / 2, imageHeight / 2 - font.lineHeight - 1, TextFormatting.DARK_GRAY.getColor());

        TranslationTextComponent description = new TranslationTextComponent("gui.frame.resize_description");
        int descriptionWidth = font.width(description);
        font.draw(matrixStack, description.getVisualOrderText(), imageWidth / 2 - descriptionWidth / 2, imageHeight / 2 + 1, TextFormatting.GRAY.getColor());

        minecraft.getTextureManager().bind(CAMERA_TEXTURE);
        if (Screen.hasShiftDown()) {
            blit(matrixStack, imageWidth / 2 - 8, PADDING + 2, 16, 109, 16, 16);
            blit(matrixStack, imageWidth / 2 - 8, imageHeight - PADDING - BUTTON_HEIGHT + 2, 0, 109, 16, 16);
            blit(matrixStack, PADDING + BUTTON_WIDTH / 2 - 8, imageHeight / 2 - BUTTON_HEIGHT / 2 + 3, 0, 125, 16, 16);
            blit(matrixStack, imageWidth - PADDING - BUTTON_WIDTH / 2 - 8, imageHeight / 2 - BUTTON_HEIGHT / 2 + 3, 16, 125, 16, 16);
        } else {
            blit(matrixStack, imageWidth / 2 - 8, PADDING + 2, 0, 109, 16, 16);
            blit(matrixStack, imageWidth / 2 - 8, imageHeight - PADDING - BUTTON_HEIGHT + 2, 16, 109, 16, 16);
            blit(matrixStack, PADDING + BUTTON_WIDTH / 2 - 8, imageHeight / 2 - BUTTON_HEIGHT / 2 + 3, 16, 125, 16, 16);
            blit(matrixStack, imageWidth - PADDING - BUTTON_WIDTH / 2 - 8, imageHeight / 2 - BUTTON_HEIGHT / 2 + 3, 0, 125, 16, 16);
        }

        if (visibilityButton.isHovered()) {
            renderTooltip(matrixStack, Arrays.asList(new TranslationTextComponent("tooltip.visibility").getVisualOrderText()), x - leftPos, y - topPos);
        }
    }

    private long lastCheck;

    @Override
    public void tick() {
        super.tick();

        if (System.currentTimeMillis() - lastCheck > 500L) {
            if (!isImagePresent()) {
                minecraft.player.closeContainer();
            }
            lastCheck = System.currentTimeMillis();
        }
    }

    public boolean isImagePresent() {
        AxisAlignedBB aabb = minecraft.player.getBoundingBox();
        aabb = aabb.inflate(32D);
        return minecraft.level.getEntitiesOfClass(ImageEntity.class, aabb).stream().anyMatch(image -> image.getUUID().equals(uuid) && image.distanceTo(minecraft.player) <= 32F);
    }
}
package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.CameraMod;
import de.maxhenkel.camera.entities.ImageEntity;
import de.maxhenkel.camera.net.MessageResizeFrame;
import de.maxhenkel.corelib.FontColorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;
import java.util.UUID;

public class ResizeFrameScreen extends AbstractContainerScreen<AbstractContainerMenu> {

    private static final ResourceLocation CAMERA_TEXTURE = ResourceLocation.fromNamespaceAndPath(CameraMod.MODID, "textures/gui/resize_frame.png");
    private static final int PADDING = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 50;

    private UUID uuid;
    private float visibility;
    private Button visibilityButton;

    public ResizeFrameScreen(UUID uuid) {
        super(new DummyContainer(), Minecraft.getInstance().player.getInventory(), Component.translatable("gui.frame.resize"));
        this.uuid = uuid;
        visibility = CameraMod.CLIENT_CONFIG.resizeGuiOpacity.get().floatValue();
        imageWidth = 248;
        imageHeight = 109;
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        int left = (width - imageWidth) / 2;
        addRenderableWidget(Button.builder(Component.empty(), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.LEFT);
        }).bounds(left + PADDING, height / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(Component.empty(), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.RIGHT);
        }).bounds(left + imageWidth - BUTTON_WIDTH - PADDING, height / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(Component.empty(), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.UP);
        }).bounds(width / 2 - BUTTON_WIDTH / 2, topPos + PADDING, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(Component.empty(), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.DOWN);
        }).bounds(width / 2 - BUTTON_WIDTH / 2, topPos + imageHeight - PADDING - BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        visibilityButton = addRenderableWidget(Button.builder(Component.translatable("tooltip.visibility_short"), (button) -> {
            visibility -= 0.25;
            if (visibility < 0F) {
                visibility = 1F;
            }
            CameraMod.CLIENT_CONFIG.resizeGuiOpacity.set((double) visibility);
            CameraMod.CLIENT_CONFIG.resizeGuiOpacity.save();
        }).bounds(left + imageWidth - 20 - PADDING, topPos + PADDING, 20, 20).build());
    }

    private void sendMoveImage(MessageResizeFrame.Direction direction) {
        ClientPacketDistributor.sendToServer(new MessageResizeFrame(uuid, direction, !minecraft.hasShiftDown()));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (visibility >= 1F) {
            renderTransparentBackground(guiGraphics);
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CAMERA_TEXTURE, leftPos, topPos, 0F, 0F, imageWidth, imageHeight, imageWidth, imageHeight, 256, 256, ARGB.colorFromFloat(visibility, 1F, 1F, 1F));

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(leftPos, topPos);

        MutableComponent title = Component.translatable("gui.frame.resize");
        int titleWidth = font.width(title);
        guiGraphics.drawString(font, title.getVisualOrderText(), imageWidth / 2 - titleWidth / 2, imageHeight / 2 - font.lineHeight - 1, FontColorUtils.getFontColor(ChatFormatting.DARK_GRAY), false);

        MutableComponent description = Component.translatable("gui.frame.resize_description");
        int descriptionWidth = font.width(description);
        guiGraphics.drawString(font, description.getVisualOrderText(), imageWidth / 2 - descriptionWidth / 2, imageHeight / 2 + 1, FontColorUtils.getFontColor(ChatFormatting.GRAY), false);

        if (minecraft.hasShiftDown()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CAMERA_TEXTURE, imageWidth / 2 - 8, PADDING + 2, 16, 109, 16, 16, 256, 256);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CAMERA_TEXTURE, imageWidth / 2 - 8, imageHeight - PADDING - BUTTON_HEIGHT + 2, 0, 109, 16, 16, 256, 256);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CAMERA_TEXTURE, PADDING + BUTTON_WIDTH / 2 - 8, imageHeight / 2 - BUTTON_HEIGHT / 2 + 3, 0, 125, 16, 16, 256, 256);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CAMERA_TEXTURE, imageWidth - PADDING - BUTTON_WIDTH / 2 - 8, imageHeight / 2 - BUTTON_HEIGHT / 2 + 3, 16, 125, 16, 16, 256, 256);
        } else {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CAMERA_TEXTURE, imageWidth / 2 - 8, PADDING + 2, 0, 109, 16, 16, 256, 256);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CAMERA_TEXTURE, imageWidth / 2 - 8, imageHeight - PADDING - BUTTON_HEIGHT + 2, 16, 109, 16, 16, 256, 256);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CAMERA_TEXTURE, PADDING + BUTTON_WIDTH / 2 - 8, imageHeight / 2 - BUTTON_HEIGHT / 2 + 3, 16, 125, 16, 16, 256, 256);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CAMERA_TEXTURE, imageWidth - PADDING - BUTTON_WIDTH / 2 - 8, imageHeight / 2 - BUTTON_HEIGHT / 2 + 3, 0, 125, 16, 16, 256, 256);
        }

        if (visibilityButton.isHovered()) {
            guiGraphics.setTooltipForNextFrame(font, List.of(Component.translatable("tooltip.visibility").getVisualOrderText()), mouseX, mouseY);
        }

        guiGraphics.pose().popMatrix();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int x, int y) {

    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int x, int y, float partialTicks) {

    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int x, int y) {

    }

    private long lastCheck;

    @Override
    public void containerTick() {
        super.containerTick();

        if (System.currentTimeMillis() - lastCheck > 500L) {
            if (!isImagePresent()) {
                minecraft.player.closeContainer();
            }
            lastCheck = System.currentTimeMillis();
        }
    }

    public boolean isImagePresent() {
        AABB aabb = minecraft.player.getBoundingBox();
        aabb = aabb.inflate(32D);
        return minecraft.level.getEntitiesOfClass(ImageEntity.class, aabb).stream().anyMatch(image -> image.getUUID().equals(uuid) && image.distanceTo(minecraft.player) <= 32F);
    }
}
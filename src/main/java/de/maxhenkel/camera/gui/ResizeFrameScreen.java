package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
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
    protected void func_231160_c_() {
        super.func_231160_c_();

        field_230710_m_.clear();
        int left = (field_230708_k_ - xSize) / 2;
        func_230480_a_(new Button(left + PADDING, field_230709_l_ / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT, new StringTextComponent(""), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.LEFT);
        }));

        func_230480_a_(new Button(left + xSize - BUTTON_WIDTH - PADDING, field_230709_l_ / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT, new StringTextComponent(""), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.RIGHT);
        }));

        func_230480_a_(new Button(field_230708_k_ / 2 - BUTTON_WIDTH / 2, guiTop + PADDING, BUTTON_WIDTH, BUTTON_HEIGHT, new StringTextComponent(""), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.UP);
        }));

        func_230480_a_(new Button(field_230708_k_ / 2 - BUTTON_WIDTH / 2, guiTop + ySize - PADDING - BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT, new StringTextComponent(""), (button) -> {
            sendMoveImage(MessageResizeFrame.Direction.DOWN);
        }));

        visibilityButton = new Button(left + xSize - 20 - PADDING, guiTop + PADDING, 20, 20, new TranslationTextComponent("tooltip.visibility_short"), (button) -> {
            visibility -= 0.25;
            if (visibility < 0F) {
                visibility = 1F;
            }
            Config.CLIENT.RESIZE_GUI_OPACITY.set((double) visibility);
            Config.CLIENT.RESIZE_GUI_OPACITY.save();
        });
        func_230480_a_(visibilityButton);
    }

    private void sendMoveImage(MessageResizeFrame.Direction direction) {
        Main.SIMPLE_CHANNEL.sendToServer(new MessageResizeFrame(uuid, direction, !Screen.func_231173_s_()));
    }

    @Override
    protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int x, int y) {
        if (visibility >= 1F) {
            func_230446_a_(matrixStack);
        }
        RenderSystem.color4f(1F, 1F, 1F, visibility);
        field_230706_i_.getTextureManager().bindTexture(CAMERA_TEXTURE);

        func_238474_b_(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void func_230451_b_(MatrixStack matrixStack, int x, int y) {
        String title = new TranslationTextComponent("gui.frame.resize").getString();
        int titleWidth = field_230712_o_.getStringWidth(title);
        field_230712_o_.func_238421_b_(matrixStack, title, xSize / 2 - titleWidth / 2, ySize / 2 - field_230712_o_.FONT_HEIGHT - 1, TextFormatting.DARK_GRAY.getColor());

        String description = new TranslationTextComponent("gui.frame.resize_description").getString();
        int descriptionWidth = field_230712_o_.getStringWidth(description);
        field_230712_o_.func_238421_b_(matrixStack, description, xSize / 2 - descriptionWidth / 2, ySize / 2 + 1, TextFormatting.GRAY.getColor());

        field_230706_i_.getTextureManager().bindTexture(CAMERA_TEXTURE);
        if (Screen.func_231173_s_()) {
            func_238474_b_(matrixStack, xSize / 2 - 8, PADDING + 2, 16, 109, 16, 16);
            func_238474_b_(matrixStack, xSize / 2 - 8, ySize - PADDING - BUTTON_HEIGHT + 2, 0, 109, 16, 16);
            func_238474_b_(matrixStack, PADDING + BUTTON_WIDTH / 2 - 8, ySize / 2 - BUTTON_HEIGHT / 2 + 3, 0, 125, 16, 16);
            func_238474_b_(matrixStack, xSize - PADDING - BUTTON_WIDTH / 2 - 8, ySize / 2 - BUTTON_HEIGHT / 2 + 3, 16, 125, 16, 16);
        } else {
            func_238474_b_(matrixStack, xSize / 2 - 8, PADDING + 2, 0, 109, 16, 16);
            func_238474_b_(matrixStack, xSize / 2 - 8, ySize - PADDING - BUTTON_HEIGHT + 2, 16, 109, 16, 16);
            func_238474_b_(matrixStack, PADDING + BUTTON_WIDTH / 2 - 8, ySize / 2 - BUTTON_HEIGHT / 2 + 3, 16, 125, 16, 16);
            func_238474_b_(matrixStack, xSize - PADDING - BUTTON_WIDTH / 2 - 8, ySize / 2 - BUTTON_HEIGHT / 2 + 3, 0, 125, 16, 16);
        }

        if (visibilityButton.func_230449_g_()) {
            func_238654_b_(matrixStack, Arrays.asList(new TranslationTextComponent("tooltip.visibility")), x - guiLeft, y - guiTop);
        }
    }

    private long lastCheck;

    @Override
    public void func_231023_e_() {
        super.func_231023_e_();

        if (System.currentTimeMillis() - lastCheck > 500L) {
            if (!isImagePresent()) {
                field_230706_i_.player.closeScreen();
            }
            lastCheck = System.currentTimeMillis();
        }
    }

    public boolean isImagePresent() {
        AxisAlignedBB aabb = field_230706_i_.player.getBoundingBox();
        if (aabb == null) {
            return false;
        }
        aabb = aabb.grow(32D);
        return field_230706_i_.world.getEntitiesWithinAABB(ImageEntity.class, aabb).stream().anyMatch(image -> image.getUniqueID().equals(uuid) && image.getDistance(field_230706_i_.player) <= 32F);
    }
}
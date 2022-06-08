package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.camera.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlbumScreen extends AbstractContainerScreen<AlbumContainer> {

    protected int index;
    protected List<UUID> images;

    public AlbumScreen(AlbumContainer screenContainer, Inventory inv, Component titleIn, List<UUID> images) {
        super(screenContainer, inv, titleIn);
        this.images = images;
    }

    public AlbumScreen(AlbumContainer screenContainer, Inventory inv, Component titleIn) {
        this(screenContainer, inv, titleIn, new ArrayList<>());
    }

    public AlbumScreen(List<UUID> images) {
        this(new AlbumContainer(-1), Minecraft.getInstance().player.getInventory(), Component.translatable("gui.album.title"), images);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {

    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (images.isEmpty()) {
            return;
        }
        UUID uuid = images.get(index);
        ImageScreen.drawImage(matrixStack, minecraft, width, height, 100, uuid);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta < 0D) {
            next();
        } else {
            previous();
        }
        return true;
    }

    protected void next() {
        setIndex(index + 1);
    }

    protected void previous() {
        setIndex(index - 1);
    }

    protected void setIndex(int i) {
        if (index == i) {
            return;
        }
        index = i;
        if (index >= images.size()) {
            index = 0;
        } else if (index < 0) {
            index = images.size() - 1;
        }
        playPageTurnSound();
    }

    protected void playPageTurnSound() {
        minecraft.player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.MASTER, 1F, minecraft.level.random.nextFloat() * 0.1F + 0.9F);
    }

    private boolean wasNextDown;
    private boolean wasPreviousDown;

    @Override
    public void containerTick() {
        super.containerTick();
        boolean isNextDown = InputConstants.isKeyDown(minecraft.getWindow().getWindow(), Main.KEY_NEXT.getKey().getValue());
        boolean isPreviousDown = InputConstants.isKeyDown(minecraft.getWindow().getWindow(), Main.KEY_PREVIOUS.getKey().getValue());
        if (wasNextDown != (wasNextDown = isNextDown) && !isNextDown) {
            next();
        } else if (wasPreviousDown != (wasPreviousDown = isPreviousDown) && !isPreviousDown) {
            previous();
        }
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int x, int y) {

    }

}
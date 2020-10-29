package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.camera.Main;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlbumScreen extends ContainerScreen<AlbumContainer> {

    protected int index;
    protected List<UUID> images;

    public AlbumScreen(AlbumContainer screenContainer, PlayerInventory inv, ITextComponent titleIn, List<UUID> images) {
        super(screenContainer, inv, titleIn);
        this.images = images;
    }

    public AlbumScreen(AlbumContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        this(screenContainer, inv, titleIn, new ArrayList<>());
    }

    public AlbumScreen(List<UUID> images) {
        this(new AlbumContainer(-1), null, new TranslationTextComponent("gui.album.title"), images);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {

    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        RenderSystem.color4f(1F, 1F, 1F, 1F);

        if (images.isEmpty()) {
            return;
        }
        UUID uuid = images.get(index);
        ImageScreen.drawImage(minecraft, width, height, 100, uuid);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
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
        minecraft.player.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.MASTER, 1F, minecraft.world.rand.nextFloat() * 0.1F + 0.9F);
    }

    private boolean wasNextDown;
    private boolean wasPreviousDown;

    @Override
    public void tick() {
        super.tick();
        boolean isNextDown = InputMappings.isKeyDown(minecraft.getMainWindow().getHandle(), Main.KEY_NEXT.getKey().getKeyCode());
        boolean isPreviousDown = InputMappings.isKeyDown(minecraft.getMainWindow().getHandle(), Main.KEY_PREVIOUS.getKey().getKeyCode());
        if (wasNextDown != (wasNextDown = isNextDown) && !isNextDown) {
            next();
        } else if (wasPreviousDown != (wasPreviousDown = isPreviousDown) && !isPreviousDown) {
            previous();
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {

    }

}
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
    protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {

    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, int x, int y, float f) {
        func_230446_a_(matrixStack);
        RenderSystem.color4f(1F, 1F, 1F, 1F);

        if (images.isEmpty()) {
            return;
        }
        UUID uuid = images.get(index);
        ImageScreen.drawImage(field_230706_i_, field_230708_k_, field_230709_l_, 100, uuid);
        super.func_230430_a_(matrixStack, x, y, f);
    }

    @Override
    public boolean func_231043_a_(double x, double y, double amount) {
        if (amount < 0D) {
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
        field_230706_i_.player.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.MASTER, 1F, field_230706_i_.world.rand.nextFloat() * 0.1F + 0.9F);
    }

    private boolean wasNextDown;
    private boolean wasPreviousDown;

    @Override
    public void func_231023_e_() {
        super.func_231023_e_();
        boolean isNextDown = InputMappings.isKeyDown(field_230706_i_.getMainWindow().getHandle(), Main.KEY_NEXT.getKey().getKeyCode());
        boolean isPreviousDown = InputMappings.isKeyDown(field_230706_i_.getMainWindow().getHandle(), Main.KEY_PREVIOUS.getKey().getKeyCode());
        if (wasNextDown != (wasNextDown = isNextDown) && !isNextDown) {
            next();
        } else if (wasPreviousDown != (wasPreviousDown = isPreviousDown) && !isPreviousDown) {
            previous();
        }
    }

    @Override
    protected void func_230451_b_(MatrixStack p_230451_1_, int p_230451_2_, int p_230451_3_) {

    }
}
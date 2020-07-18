package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.camera.Main;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.UUID;

public class AlbumScreen extends ContainerScreen<Container> {

    private int index;
    private List<UUID> images;

    public AlbumScreen(List<UUID> images) {
        super(new DummyContainer(), null, new TranslationTextComponent("gui.album.title"));
        this.images = images;
    }

    @Override
    protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        func_230446_a_(matrixStack);
        RenderSystem.color4f(1F, 1F, 1F, 1F);

        if (images.isEmpty()) {
            return;
        }
        UUID uuid = images.get(index);
        ImageScreen.drawImage(field_230706_i_, field_230708_k_, field_230709_l_, 100, uuid);
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

    private void next() {
        index++;
        if (index >= images.size()) {
            index = 0;
        }
    }

    private void previous() {
        index--;
        if (index < 0) {
            index = images.size() - 1;
        }
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
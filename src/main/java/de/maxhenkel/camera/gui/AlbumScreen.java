package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import de.maxhenkel.camera.Main;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.UUID;

public class AlbumScreen extends ContainerScreen {

    private int index;
    private List<UUID> images;

    public AlbumScreen(List<UUID> images) {
        super(new DummyContainer(), null, new TranslationTextComponent("gui.album.title"));
        this.images = images;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        renderBackground();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (images.isEmpty()) {
            return;
        }
        UUID uuid = images.get(index);
        ImageScreen.drawImage(minecraft, width, height, 100, uuid);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
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
    public void tick() {
        super.tick();

        boolean isNextDown = InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), Main.KEY_NEXT.getKey().getKeyCode());
        boolean isPreviousDown = InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), Main.KEY_PREVIOUS.getKey().getKeyCode());
        if (wasNextDown != (wasNextDown = isNextDown) && !isNextDown) {
            next();
        } else if (wasPreviousDown != (wasPreviousDown = isPreviousDown) && !isPreviousDown) {
            previous();
        }
    }
}
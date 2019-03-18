package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.Main;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.InputMappings;

import java.util.List;
import java.util.UUID;

public class GuiAlbum extends GuiContainer {

    private int index;
    private List<UUID> images;

    public GuiAlbum(List<UUID> images) {
        super(new ContainerImage());
        this.images = images;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawDefaultBackground();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (images.isEmpty()) {
            return;
        }
        UUID uuid = images.get(index);
        GuiImage.drawImage(mc, width, height, zLevel, uuid);
    }

    @Override
    public boolean mouseScrolled(double amount) {
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

        boolean isNextDown = InputMappings.isKeyDown(Main.KEY_NEXT.getKey().getKeyCode());
        boolean isPreviousDown = InputMappings.isKeyDown(Main.KEY_PREVIOUS.getKey().getKeyCode());
        if (wasNextDown != (wasNextDown = isNextDown) && !isNextDown) {
            next();
        } else if (wasPreviousDown != (wasPreviousDown = isPreviousDown) && !isPreviousDown) {
            previous();
        }
    }
}
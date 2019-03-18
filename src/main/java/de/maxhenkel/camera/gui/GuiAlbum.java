package de.maxhenkel.camera.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;

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
            index++;
            if (index >= images.size()) {
                index = 0;
            }
        } else {
            index--;
            if (index < 0) {
                index = images.size() - 1;
            }
        }
        return true;
    }
}
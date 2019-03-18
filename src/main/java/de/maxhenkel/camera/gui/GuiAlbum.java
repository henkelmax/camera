package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.proxy.ClientProxy;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.io.IOException;
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
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if (images.isEmpty()) {
            return;
        }
        UUID uuid = images.get(index);
        GuiImage.drawImage(mc, width, height, zLevel, uuid);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int amount = Mouse.getEventDWheel();
        if (amount < 0) {
            next();
        } else if (amount > 0) {
            previous();
        }
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

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (keyCode == ClientProxy.KEY_NEXT.getKeyCode()) {
            next();
        } else if (keyCode == ClientProxy.KEY_PREVIOUS.getKeyCode()) {
            previous();
        }
    }
}
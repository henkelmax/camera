package de.maxhenkel.camera.integration.jei;

import mezz.jei.api.gui.handlers.IGuiProperties;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class NoJEIGuiProperties implements IGuiProperties {

    private final AbstractContainerScreen<?> containerScreen;

    public NoJEIGuiProperties(AbstractContainerScreen<?> containerScreen) {
        this.containerScreen = containerScreen;
    }

    @Override
    public Class<? extends Screen> screenClass() {
        return containerScreen.getClass();
    }

    @Override
    public int guiLeft() {
        return containerScreen.width;
    }

    @Override
    public int guiTop() {
        return containerScreen.height;
    }

    @Override
    public int guiXSize() {
        return containerScreen.width;
    }

    @Override
    public int guiYSize() {
        return containerScreen.height;
    }

    @Override
    public int screenWidth() {
        return containerScreen.width;
    }

    @Override
    public int screenHeight() {
        return containerScreen.height;
    }
}

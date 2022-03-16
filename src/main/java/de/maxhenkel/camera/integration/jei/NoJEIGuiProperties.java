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
    public Class<? extends Screen> getScreenClass() {
        return containerScreen.getClass();
    }

    @Override
    public int getGuiLeft() {
        return containerScreen.width;
    }

    @Override
    public int getGuiTop() {
        return containerScreen.height;
    }

    @Override
    public int getGuiXSize() {
        return containerScreen.width;
    }

    @Override
    public int getGuiYSize() {
        return containerScreen.height;
    }

    @Override
    public int getScreenWidth() {
        return containerScreen.width;
    }

    @Override
    public int getScreenHeight() {
        return containerScreen.height;
    }
}

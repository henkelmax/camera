package de.maxhenkel.camera.integration.jei;

import mezz.jei.api.gui.handlers.IGuiProperties;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;

public class NoJEIGuiProperties implements IGuiProperties {

    private final ContainerScreen<?> containerScreen;

    public NoJEIGuiProperties(ContainerScreen<?> containerScreen) {
        this.containerScreen = containerScreen;
    }

    @Override
    public Class<? extends Screen> getScreenClass() {
        return containerScreen.getClass();
    }

    @Override
    public int getGuiLeft() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getGuiTop() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getGuiXSize() {
        return containerScreen.getXSize();
    }

    @Override
    public int getGuiYSize() {
        return containerScreen.getYSize();
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

package de.maxhenkel.camera.integration.jei;

import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.minecraft.client.gui.inventory.GuiContainer;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public abstract class NoJEIContainerHandler<T extends GuiContainer> implements IAdvancedGuiHandler<T> {

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(T guiContainer) {
        return Arrays.asList(new Rectangle(0, 0, guiContainer.width, guiContainer.height));
    }
}

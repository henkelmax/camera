package de.maxhenkel.camera.integration.jei;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;

import java.util.Arrays;
import java.util.List;

public class NoJEIContainerHandler<T extends ContainerScreen<?>> implements IGuiContainerHandler<T> {

    @Override
    public List<Rectangle2d> getGuiExtraAreas(T containerScreen) {
        return Arrays.asList(new Rectangle2d(0, 0, containerScreen.field_230708_k_, containerScreen.field_230709_l_));
    }

}
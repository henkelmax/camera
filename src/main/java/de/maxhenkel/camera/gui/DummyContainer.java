package de.maxhenkel.camera.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;

public class DummyContainer extends Container {
    protected DummyContainer() {
        super(null, 0);
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return true;
    }
}

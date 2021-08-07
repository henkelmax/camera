package de.maxhenkel.camera.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class DummyContainer extends AbstractContainerMenu {

    protected DummyContainer() {
        super(null, 0);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }
}

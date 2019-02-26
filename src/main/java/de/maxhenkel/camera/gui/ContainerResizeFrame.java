package de.maxhenkel.camera.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerResizeFrame extends Container {

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}

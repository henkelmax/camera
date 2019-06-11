package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.items.ItemImage;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class AlbumSlot extends Slot {
    public AlbumSlot(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack.getItem() instanceof ItemImage;
    }
}

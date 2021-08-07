package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.items.ImageItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AlbumSlot extends Slot {

    public AlbumSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof ImageItem;
    }
}

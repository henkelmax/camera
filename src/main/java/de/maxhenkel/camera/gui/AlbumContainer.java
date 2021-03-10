package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.Main;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;

public class AlbumContainer extends Container {

    private final IInventory inventory;
    private final IIntArray intArray;

    public AlbumContainer(int id) {
        this(id, new Inventory(1), new IntArray(1));
    }

    public AlbumContainer(int id, IInventory inventory, IIntArray intArray) {
        super(Main.ALBUM_CONTAINER, id);
        checkContainerSize(inventory, 1);
        checkContainerDataCount(intArray, 1);
        this.inventory = inventory;
        this.intArray = intArray;
        addSlot(new Slot(inventory, 0, Integer.MIN_VALUE, Integer.MIN_VALUE) {
            @Override
            public void setChanged() {
                super.setChanged();
                slotsChanged(inventory);
            }
        });
        addDataSlots(intArray);
    }

    @Override
    public void setData(int id, int data) {
        super.setData(id, data);
        broadcastChanges();
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return inventory.stillValid(player);
    }

    public ItemStack getAlbum() {
        return inventory.getItem(0);
    }

    public int getPage() {
        return intArray.get(0);
    }

    public void setPage(int page) {
        setData(0, page);
    }

    public void takeBook(PlayerEntity player) {
        if (!player.mayBuild()) {
            return;
        }
        ItemStack itemstack = inventory.removeItemNoUpdate(0);
        inventory.setChanged();
        if (!player.inventory.add(itemstack)) {
            player.drop(itemstack, false);
        }
    }
}

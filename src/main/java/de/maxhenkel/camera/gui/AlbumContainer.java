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
        assertInventorySize(inventory, 1);
        assertIntArraySize(intArray, 1);
        this.inventory = inventory;
        this.intArray = intArray;
        addSlot(new Slot(inventory, 0, Integer.MIN_VALUE, Integer.MIN_VALUE) {
            public void onSlotChanged() {
                super.onSlotChanged();
                onCraftMatrixChanged(inventory);
            }
        });
        trackIntArray(intArray);
    }

    @Override
    public void updateProgressBar(int id, int data) {
        super.updateProgressBar(id, data);
        detectAndSendChanges();
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return inventory.isUsableByPlayer(player);
    }

    public ItemStack getAlbum() {
        return inventory.getStackInSlot(0);
    }

    public int getPage() {
        return intArray.get(0);
    }

    public void setPage(int page) {
        updateProgressBar(0, page);
    }

    public void takeBook(PlayerEntity player) {
        if (!player.isAllowEdit()) {
            return;
        }
        ItemStack itemstack = inventory.removeStackFromSlot(0);
        inventory.markDirty();
        if (!player.inventory.addItemStackToInventory(itemstack)) {
            player.dropItem(itemstack, false);
        }
    }
}

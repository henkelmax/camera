package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.Main;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AlbumContainer extends AbstractContainerMenu {

    private final Container inventory;
    private final ContainerData intArray;

    public AlbumContainer(int id) {
        this(id, new SimpleContainer(1), new SimpleContainerData(1));
    }

    public AlbumContainer(int id, Container inventory, ContainerData intArray) {
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
    public boolean stillValid(Player player) {
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

    public void takeBook(Player player) {
        if (!player.mayBuild()) {
            return;
        }
        ItemStack itemstack = inventory.removeItemNoUpdate(0);
        inventory.setChanged();
        if (!player.getInventory().add(itemstack)) {
            player.drop(itemstack, false);
        }
    }
}

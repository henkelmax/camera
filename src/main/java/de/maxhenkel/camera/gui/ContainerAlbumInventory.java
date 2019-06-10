package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.Main;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;

public class ContainerAlbumInventory extends ContainerBase {

    private IInventory albumInventory;

    public ContainerAlbumInventory(int id, IInventory playerInventory, IInventory albumInventory) {
        super(Main.ALBUM_INVENTORY_CONTAINER, id, playerInventory, albumInventory);
        this.albumInventory = albumInventory;

        for (int x = 0; x < 6; x++) {
            for (int y = 0; y < 9; y++) {

                addSlot(new SlotAlbum(albumInventory, y + x * 9, 8 + y * 18, 18 + x * 18));
            }
        }

        addInvSlots();
    }

    public ContainerAlbumInventory(int id, IInventory playerInventory) {
        this(id, playerInventory, new Inventory(54));
    }

    @Override
    public int getInventorySize() {
        return 54;
    }

    @Override
    public int getInvOffset() {
        return 56;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return albumInventory.isUsableByPlayer(playerIn);
    }
}

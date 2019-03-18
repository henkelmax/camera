package de.maxhenkel.camera.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public class ContainerAlbumInventory extends ContainerBase {

    private IInventory albumInventory;

    public ContainerAlbumInventory(IInventory playerInventory, IInventory albumInventory) {
        super(playerInventory, albumInventory);
        this.albumInventory = albumInventory;

        for (int x = 0; x < 6; x++) {
            for (int y = 0; y < 9; y++) {
                addSlot(new SlotAlbum(albumInventory, y + x * 9, 8 + y * 18, 18 + x * 18));
            }
        }

        addInvSlots();
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
    public boolean canInteractWith(EntityPlayer playerIn) {
        return albumInventory.isUsableByPlayer(playerIn);
    }
}

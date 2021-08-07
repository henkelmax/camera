package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.Main;
import de.maxhenkel.corelib.inventory.ContainerBase;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;

public class AlbumInventoryContainer extends ContainerBase {

    public AlbumInventoryContainer(int id, Container playerInventory, Container albumInventory) {
        super(Main.ALBUM_INVENTORY_CONTAINER, id, playerInventory, albumInventory);

        for (int x = 0; x < 6; x++) {
            for (int y = 0; y < 9; y++) {
                addSlot(new AlbumSlot(albumInventory, y + x * 9, 8 + y * 18, 18 + x * 18));
            }
        }

        addPlayerInventorySlots();
    }

    public AlbumInventoryContainer(int id, Container playerInventory) {
        this(id, playerInventory, new SimpleContainer(54));
    }

    @Override
    public int getInventorySize() {
        return 54;
    }

    @Override
    public int getInvOffset() {
        return 56;
    }
}

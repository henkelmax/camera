package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.Main;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GUIManager {
    @OnlyIn(Dist.CLIENT)
    public static void clientSetup() {
        ScreenManager.IScreenFactory factory = (ScreenManager.IScreenFactory<ContainerAlbumInventory, GuiAlbumInventory>) (container, playerInventory, name) -> new GuiAlbumInventory(playerInventory, container, name);
        ScreenManager.registerFactory(Main.ALBUM_INVENTORY_CONTAINER, factory);
    }
}

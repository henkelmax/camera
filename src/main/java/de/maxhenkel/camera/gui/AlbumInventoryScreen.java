package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.CameraMod;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class AlbumInventoryScreen extends ScreenBase<AlbumInventoryContainer> {

    public static final Identifier DEFAULT_IMAGE = Identifier.fromNamespaceAndPath(CameraMod.MODID, "textures/gui/album.png");

    private Inventory playerInventory;

    public AlbumInventoryScreen(AlbumInventoryContainer albumInventory, Inventory playerInventory, Component name) {
        super(DEFAULT_IMAGE, albumInventory, playerInventory, name, 176, 222);

        this.playerInventory = playerInventory;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int x, int y) {
        super.extractLabels(guiGraphics, x, y);
        guiGraphics.text(font, getTitle().getVisualOrderText(), 8, 6, FONT_COLOR, false);
        guiGraphics.text(font, playerInventory.getDisplayName().getVisualOrderText(), 8, imageHeight - 96 + 2, FONT_COLOR, false);
    }

}
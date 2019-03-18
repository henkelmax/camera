package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.Main;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class GuiAlbumInventory extends GUIBase {

    public static final ResourceLocation DEFAULT_IMAGE = new ResourceLocation(Main.MODID, "textures/gui/album.png");

    private IInventory playerInventory;
    private IInventory albumInventory;

    public GuiAlbumInventory(IInventory playerInventory, IInventory albumInventory) {
        super(DEFAULT_IMAGE, new ContainerAlbumInventory(playerInventory, albumInventory));

        this.playerInventory = playerInventory;
        this.albumInventory = albumInventory;

        xSize = 176;
        ySize = 222;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString(albumInventory.getDisplayName().getFormattedText(), 8, 6, FONT_COLOR);
        fontRenderer.drawString(playerInventory.getDisplayName().getFormattedText(), 8, ySize - 96 + 2, FONT_COLOR);
    }
}
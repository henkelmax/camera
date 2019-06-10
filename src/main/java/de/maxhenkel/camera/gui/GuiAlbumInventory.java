package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.Main;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiAlbumInventory extends GUIBase<ContainerAlbumInventory> {

    public static final ResourceLocation DEFAULT_IMAGE = new ResourceLocation(Main.MODID, "textures/gui/album.png");

    private PlayerInventory playerInventory;

    public GuiAlbumInventory(PlayerInventory playerInventory, ContainerAlbumInventory albumInventory, ITextComponent name) {
        super(DEFAULT_IMAGE, albumInventory, playerInventory, name);

        this.playerInventory = playerInventory;
        xSize = 176;
        ySize = 222;
    }


    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        font.drawString(getTitle().getFormattedText(), 8.0F, 6.0F, FONT_COLOR);
        font.drawString(playerInventory.getDisplayName().getFormattedText(), 8.0F, (float) (ySize - 96 + 2), FONT_COLOR);
    }
}
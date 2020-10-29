package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.camera.Main;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class AlbumInventoryScreen extends ScreenBase<AlbumInventoryContainer> {

    public static final ResourceLocation DEFAULT_IMAGE = new ResourceLocation(Main.MODID, "textures/gui/album.png");

    private PlayerInventory playerInventory;

    public AlbumInventoryScreen(AlbumInventoryContainer albumInventory, PlayerInventory playerInventory, ITextComponent name) {
        super(DEFAULT_IMAGE, albumInventory, playerInventory, name);

        this.playerInventory = playerInventory;
        xSize = 176;
        ySize = 222;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        font.func_238422_b_(matrixStack, getTitle().func_241878_f(), 8F, 6F, FONT_COLOR);
        font.func_238422_b_(matrixStack, playerInventory.getDisplayName().func_241878_f(), 8F, (float) (ySize - 96 + 2), FONT_COLOR);
    }

}
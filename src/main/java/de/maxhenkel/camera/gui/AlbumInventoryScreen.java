package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.camera.Main;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class AlbumInventoryScreen extends ScreenBase<ContainerAlbumInventory> {

    public static final ResourceLocation DEFAULT_IMAGE = new ResourceLocation(Main.MODID, "textures/gui/album.png");

    private PlayerInventory playerInventory;

    public AlbumInventoryScreen(PlayerInventory playerInventory, ContainerAlbumInventory albumInventory, ITextComponent name) {
        super(DEFAULT_IMAGE, albumInventory, playerInventory, name);

        this.playerInventory = playerInventory;
        xSize = 176;
        ySize = 222;
    }

    @Override
    protected void func_230451_b_(MatrixStack matrixStack, int x, int y) {
        super.func_230451_b_(matrixStack, x, y);
        field_230712_o_.func_238421_b_(matrixStack, func_231171_q_().getString(), 8F, 6F, FONT_COLOR);
        field_230712_o_.func_238421_b_(matrixStack, playerInventory.getDisplayName().getString(), 8F, (float) (ySize - 96 + 2), FONT_COLOR);

    }
}
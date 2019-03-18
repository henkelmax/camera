package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.inventory.InventoryAlbum;
import de.maxhenkel.camera.items.ItemAlbum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GUIManager {
    @OnlyIn(Dist.CLIENT)
    public static void clientSetup() {
        GUIRegistry.register(new ResourceLocation(Main.MODID, "album_inventory"), openContainer -> {
            EntityPlayerSP player = Minecraft.getInstance().player;
            for (EnumHand hand : EnumHand.values()) {
                ItemStack stack = player.getHeldItem(hand);
                if (stack.getItem() instanceof ItemAlbum) {
                    return new GuiAlbumInventory(player.inventory, new InventoryAlbum(stack));
                }
            }
            return null;
        });
    }
}

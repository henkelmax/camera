package de.maxhenkel.camera.items;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.ModItems;
import de.maxhenkel.camera.gui.GuiHandler;
import de.maxhenkel.camera.inventory.InventoryAlbum;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemAlbum extends Item {

    public ItemAlbum() {
        setRegistryName(new ResourceLocation(Main.MODID, "album"));
        setUnlocalizedName("album");
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (playerIn.isSneaking()) {
            playerIn.openGui(Main.MODID, GuiHandler.GUI_ALBUM_INVENTORY, worldIn, (int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
        } else {
            playerIn.openGui(Main.MODID, GuiHandler.GUI_ALBUM, worldIn, (int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public List<UUID> getImages(ItemStack stack) {
        List<UUID> images = new ArrayList<>();
        IInventory inventory = new InventoryAlbum(stack);
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack s = inventory.getStackInSlot(i);
            UUID uuid = ModItems.IMAGE.getUUID(s);
            if (uuid == null) {
                continue;
            }
            images.add(uuid);
        }
        return images;
    }
}

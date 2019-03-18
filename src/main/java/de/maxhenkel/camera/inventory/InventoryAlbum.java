package de.maxhenkel.camera.inventory;

import de.maxhenkel.camera.items.ItemImage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public class InventoryAlbum implements IInventory {

    private NonNullList<ItemStack> items;
    private ItemStack album;
    private int invSize;
    private NBTTagCompound inventoryTag;


    public InventoryAlbum(ItemStack album) {
        this.album = album;
        this.invSize = 54;
        this.items = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);

        NBTTagCompound c = album.getOrCreateTag();

        if (c.hasKey("Images")) {
            inventoryTag = c.getCompound("Images");
            ItemStackHelper.loadAllItems(inventoryTag, items);
        }
    }

    @Override
    public ITextComponent getName() {
        return album.getDisplayName();
    }

    @Override
    public boolean hasCustomName() {
        return album.hasDisplayName();
    }

    @Override
    public ITextComponent getDisplayName() {
        return getName();
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return hasCustomName() ? getCustomName() : null;
    }

    @Override
    public int getSizeInventory() {
        return invSize;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return items.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(items, index, count);
        markDirty();
        return itemstack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(items, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        items.set(index, stack);
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        if (inventoryTag == null) {
            NBTTagCompound tag = album.getOrCreateTag();
            tag.setTag("Images", inventoryTag = new NBTTagCompound());
        }

        ItemStackHelper.saveAllItems(inventoryTag, items, true);
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return !(stack.getItem() instanceof ItemImage);
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        items.clear();
        markDirty();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        for (EnumHand hand : EnumHand.values()) {
            if (player.getHeldItem(hand).equals(album)) {
                return true;
            }
        }
        return false;
    }

}

package de.maxhenkel.camera.inventory;

import de.maxhenkel.camera.items.ItemImage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;

public class AlbumInventory implements IInventory {

    private NonNullList<ItemStack> items;
    private ItemStack album;
    private int invSize;
    private CompoundNBT inventoryTag;


    public AlbumInventory(ItemStack album) {
        this.album = album;
        this.invSize = 54;
        this.items = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);

        CompoundNBT c = album.getOrCreateTag();

        if (c.contains("Images")) {
            inventoryTag = c.getCompound("Images");
            ItemStackHelper.loadAllItems(inventoryTag, items);
        }
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
            CompoundNBT tag = album.getOrCreateTag();
            tag.put("Images", inventoryTag = new CompoundNBT());
        }

        ItemStackHelper.saveAllItems(inventoryTag, items, true);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return !(stack.getItem() instanceof ItemImage);
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
    public boolean isUsableByPlayer(PlayerEntity player) {
        for (Hand hand : Hand.values()) {
            if (player.getHeldItem(hand).equals(album)) {
                return true;
            }
        }
        return false;
    }

}

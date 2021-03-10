package de.maxhenkel.camera.inventory;

import de.maxhenkel.camera.items.ImageItem;
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
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);

        CompoundNBT c = album.getOrCreateTag();

        if (c.contains("Images")) {
            inventoryTag = c.getCompound("Images");
            ItemStackHelper.loadAllItems(inventoryTag, items);
        }
    }

    @Override
    public int getContainerSize() {
        return invSize;
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack itemstack = ItemStackHelper.removeItem(items, index, count);
        setChanged();
        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ItemStackHelper.takeItem(items, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        items.set(index, stack);
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
        if (inventoryTag == null) {
            CompoundNBT tag = album.getOrCreateTag();
            tag.put("Images", inventoryTag = new CompoundNBT());
        }

        ItemStackHelper.saveAllItems(inventoryTag, items, true);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return !(stack.getItem() instanceof ImageItem);
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        for (Hand hand : Hand.values()) {
            if (player.getItemInHand(hand).equals(album)) {
                return true;
            }
        }
        return false;
    }

}

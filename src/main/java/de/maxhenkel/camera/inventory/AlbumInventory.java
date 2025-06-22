package de.maxhenkel.camera.inventory;

import de.maxhenkel.camera.items.ImageItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class AlbumInventory implements Container {

    public static final int SIZE = 54;

    private NonNullList<ItemStack> items;
    private ItemStack album;

    public AlbumInventory(ItemStack album) {
        assert !album.isEmpty();
        this.album = album;
        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ItemContainerContents contents = album.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        contents.copyInto(items);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack itemstack = ContainerHelper.removeItem(items, index, count);
        setChanged();
        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(items, index);
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
        album.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
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
    public boolean stillValid(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (player.getItemInHand(hand).equals(album)) {
                return true;
            }
        }
        return false;
    }

}

package de.maxhenkel.camera.inventory;

import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.items.AlbumItem;
import de.maxhenkel.camera.items.ImageItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.Optional;

public class AlbumInventory implements Container {

    public static final int SIZE = 54;

    private NonNullList<ItemStack> items;
    private ItemStack album;

    public AlbumInventory(HolderLookup.Provider provider, ItemStack album) {
        assert !album.isEmpty();
        this.album = album;
        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

        convert(provider, album);

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

    public static void convert(HolderLookup.Provider provider, ItemStack album) {
        if (!(album.getItem() instanceof AlbumItem)) {
            return;
        }
        CustomData customData = album.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return;
        }
        CompoundTag itemTag = customData.copyTag();
        if (!(itemTag.contains("Images"))) {
            return;
        }
        CompoundTag images = itemTag.getCompoundOrEmpty("Images");
        itemTag.remove("Images");
        if (itemTag.isEmpty()) {
            album.remove(DataComponents.CUSTOM_DATA);
        } else {
            album.set(DataComponents.CUSTOM_DATA, CustomData.of(itemTag));
        }
        NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

        ListTag itemsTag = images.getListOrEmpty("Items");

        for (int i = 0; i < itemsTag.size(); i++) {
            Optional<CompoundTag> tagOptional = itemsTag.getCompound(i);
            if (tagOptional.isEmpty()) {
                continue;
            }
            CompoundTag compoundtag = tagOptional.get();
            int j = compoundtag.getByteOr("Slot", (byte) 0) & 255;
            if (j > items.size()) {
                continue;
            }
            ItemStack itemStack = ItemStack.parse(provider, compoundtag).orElse(ItemStack.EMPTY);
            CompoundTag tag = compoundtag.getCompoundOrEmpty("tag");
            ImageData imageData = ImageData.fromImageTag(tag.getCompoundOrEmpty("image"));
            if (imageData == null) {
                items.set(j, itemStack);
                continue;
            }
            itemStack.set(Main.IMAGE_DATA_COMPONENT, imageData);
            items.set(j, itemStack);
        }

        album.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    }

}

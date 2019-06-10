package de.maxhenkel.camera;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

//TODO
public class RecipeImageCloning/* extends IRecipeHidden */ {
/*
    public RecipeImageCloning(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(IInventory inv) {
        NonNullList<ItemStack> items = NonNullList.create();
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem().equals(Main.IMAGE)) {
                items.add(stack.copy());
            } else {
                items.add(ItemStack.EMPTY);
            }
        }
        return items;
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        ItemStack paper = null;
        ItemStack image = null;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (ItemTools.isStackEmpty(stack)) {
                continue;
            } else if (stack.getItem().equals(Items.PAPER)) {
                paper = stack;
            } else if (stack.getItem().equals(Main.IMAGE)) {
                image = stack;
            } else {
                return false;
            }
        }

        return paper != null && image != null;
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem().equals(Main.IMAGE)) {
                return stack.copy();
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(Main.IMAGE);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return Main.CRAFTING_SPECIAL_IMAGE_CLONING;
    }*/

}

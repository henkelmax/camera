package de.maxhenkel.camera;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class RecipeCopyImage implements IRecipe {

    private ResourceLocation resourceLocation;

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        ItemStack paper = null;
        ItemStack image = null;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (ItemTools.isStackEmpty(stack)) {
                continue;
            } else if (stack.getItem().equals(Items.PAPER)) {
                paper = stack;
            } else if (stack.getItem().equals(ModItems.IMAGE)) {
                image = stack;
            } else {
                return false;
            }
        }

        return paper != null && image != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem().equals(ModItems.IMAGE)) {
                return stack.copy();
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> items=NonNullList.create();
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem().equals(ModItems.IMAGE)) {
                items.add(stack.copy());
            }else{
                items.add(ItemStack.EMPTY);
            }
        }
        return items;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(ModItems.IMAGE);
    }

    @Override
    public IRecipe setRegistryName(ResourceLocation name) {
        resourceLocation = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return resourceLocation;
    }

    @Override
    public Class<IRecipe> getRegistryType() {
        return IRecipe.class;
    }
}

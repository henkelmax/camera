package de.maxhenkel.camera;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RecipeImageCloning extends SpecialRecipe {

    public RecipeImageCloning(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        ItemStack paper = null;
        ItemStack image = null;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            } else if (stack.getItem().is(Main.IMAGE_PAPER)) {
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
    public ItemStack assemble(CraftingInventory inv) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem().equals(Main.IMAGE)) {
                return stack.copy();
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return Main.CRAFTING_SPECIAL_IMAGE_CLONING;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        NonNullList<ItemStack> items = NonNullList.create();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem().equals(Main.IMAGE)) {
                items.add(stack.copy());
            } else {
                items.add(ItemStack.EMPTY);
            }
        }
        return items;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public ItemStack getResultItem() {
        return new ItemStack(Main.IMAGE);
    }
}

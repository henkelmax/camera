package de.maxhenkel.camera;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class RecipeImageCloning extends CustomRecipe {

    public RecipeImageCloning(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        ItemStack paper = null;
        ItemStack image = null;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            } else if (Main.IMAGE_PAPER.contains(stack.getItem())) {
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
    public ItemStack assemble(CraftingContainer inv) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem().equals(Main.IMAGE)) {
                return stack.copy();
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Main.CRAFTING_SPECIAL_IMAGE_CLONING;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
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

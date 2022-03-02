package de.maxhenkel.camera.integration.jei;

import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.RecipeImageCloning;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICustomCraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class ImageCopyExtension<T extends RecipeImageCloning> implements ICustomCraftingCategoryExtension {

    private final T recipe;

    public ImageCopyExtension(T recipe) {
        this.recipe = recipe;
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = layout.getItemStacks();

        ItemStack image = new ItemStack(Main.IMAGE);
        ImageData.dummy().addToImage(image);

        ItemStack out = image.copy();

        List<ItemStack> paper = Main.IMAGE_PAPER.getAll().stream().map(ItemStack::new).collect(Collectors.toList());

        guiItemStacks.set(0, out);
        guiItemStacks.set(1, paper);
        guiItemStacks.set(2, image);
    }

    @Override
    public void setIngredients(IIngredients ingredients) {
        List<ItemStack> paper = Main.IMAGE_PAPER.getAll().stream().map(ItemStack::new).collect(Collectors.toList());
        ingredients.setInputs(VanillaTypes.ITEM, paper);
        ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(Main.IMAGE));
    }
}

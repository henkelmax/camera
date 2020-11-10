package de.maxhenkel.camera.integration.jei;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.RecipeImageCloning;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICustomCraftingCategoryExtension;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.UUID;
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
        Main.IMAGE.setUUID(image, new UUID(0L, 0L));
        Main.IMAGE.setTime(image, System.currentTimeMillis());
        Main.IMAGE.setOwner(image, "Steve");

        ItemStack out = image.copy();

        List<ItemStack> paper = Main.IMAGE_PAPER.getAllElements().stream().map(ItemStack::new).collect(Collectors.toList());

        guiItemStacks.set(0, out);
        guiItemStacks.set(1, paper);
        guiItemStacks.set(2, image);
    }

    @Override
    public void setIngredients(IIngredients ingredients) {
        List<ItemStack> paper = Main.IMAGE_PAPER.getAllElements().stream().map(ItemStack::new).collect(Collectors.toList());
        ingredients.setInputs(VanillaTypes.ITEM, paper);
        ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(Main.IMAGE));
    }
}

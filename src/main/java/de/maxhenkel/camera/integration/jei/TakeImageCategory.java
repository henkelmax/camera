package de.maxhenkel.camera.integration.jei;

import de.maxhenkel.camera.Main;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class TakeImageCategory implements IRecipeCategory<ItemStack> {

    private final IGuiHelper helper;

    public TakeImageCategory(IGuiHelper helper) {
        this.helper = helper;
    }

    @Override
    public RecipeType<ItemStack> getRecipeType() {
        return JEIPlugin.RECIPE_TYPE_TAKE_IMAGE;
    }

    @Override
    public IDrawable getBackground() {
        return helper.createDrawable(new ResourceLocation(Main.MODID, "textures/gui/container/jei_take_image.png"), 0, 0, 105, 18);
    }

    @Override
    public IDrawable getIcon() {
        return helper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(Main.CAMERA));
    }

    @Override
    public List<Component> getTooltipStrings(ItemStack recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        return Collections.singletonList(new TranslatableComponent("jei.camera.tooltip.take_image", recipe.getCount(), recipe.getHoverName()));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ItemStack recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addIngredient(VanillaTypes.ITEM, new ItemStack(Main.CAMERA));

        builder.addSlot(RecipeIngredientRole.INPUT, 40, 1)
                .addIngredient(VanillaTypes.ITEM, recipe);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 88, 1)
                .addIngredient(VanillaTypes.ITEM, new ItemStack(Main.IMAGE));
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("jei.camera.take_image");
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(Main.MODID, "take_image");
    }

    @Override
    public Class<? extends ItemStack> getRecipeClass() {
        return ItemStack.class;
    }

}
package de.maxhenkel.camera.integration.jei;

import de.maxhenkel.camera.Main;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;
import java.util.List;

public class TakeImageCategory implements IRecipeCategory<ItemStack> {

    private IGuiHelper helper;

    public TakeImageCategory(IGuiHelper helper) {
        this.helper = helper;
    }

    @Override
    public IDrawable getBackground() {
        return helper.createDrawable(new ResourceLocation(Main.MODID, "textures/gui/container/jei_take_image.png"), 0, 0, 105, 18);
    }

    @Override
    public IDrawable getIcon() {
        return helper.createDrawableIngredient(new ItemStack(Main.CAMERA));
    }

    @Override
    public List<ITextComponent> getTooltipStrings(ItemStack recipe, double mouseX, double mouseY) {
        return Collections.singletonList(new TranslationTextComponent("jei.camera.tooltip.take_image", recipe.getCount(), recipe.getHoverName()));
    }

    @Override
    public void setIngredients(ItemStack recipe, IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, new ItemStack(Main.CAMERA));
        ingredients.setInput(VanillaTypes.ITEM, recipe);
        ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(Main.IMAGE));
    }

    @Override
    public String getTitle() {
        return new TranslationTextComponent("jei.camera.take_image").getString();
    }

    @Override
    public ResourceLocation getUid() {
        return JEIPlugin.CATEGORY_TAKE_IMAGE;
    }

    @Override
    public Class<? extends ItemStack> getRecipeClass() {
        return ItemStack.class;
    }

    @Override
    public void setRecipe(IRecipeLayout layout, ItemStack wrapper, IIngredients ingredients) {
        IGuiItemStackGroup group = layout.getItemStacks();
        group.init(0, true, 0, 0);
        group.set(0, new ItemStack(Main.CAMERA));
        group.init(1, true, 39, 0);
        group.set(1, wrapper);
        group.init(2, true, 87, 0);
        group.set(2, new ItemStack(Main.IMAGE));
    }

}
package de.maxhenkel.camera.integration.jei;

import de.maxhenkel.camera.ImageCloningRecipe;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.*;
import de.maxhenkel.corelib.tag.TagUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    public static final RecipeType<ItemStack> RECIPE_TYPE_TAKE_IMAGE = RecipeType.create(Main.MODID, "take_image", ItemStack.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Main.MODID, "camera");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(VanillaTypes.ITEM, new ItemStack(Main.CAMERA), RECIPE_TYPE_TAKE_IMAGE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new TakeImageCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
//        registration.addGuiScreenHandler(ImageScreen.class, NoJEIGuiProperties::new);
//        registration.addGuiScreenHandler(AlbumScreen.class, NoJEIGuiProperties::new);
//        registration.addGuiScreenHandler(LecternAlbumScreen.class, NoJEIGuiProperties::new);
//        registration.addGuiScreenHandler(ResizeFrameScreen.class, NoJEIGuiProperties::new);
//        registration.addGuiScreenHandler(CameraScreen.class, NoJEIGuiProperties::new);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<ItemStack> paper = TagUtils.getItemTag(Main.IMAGE_PAPER.location()).getAll().stream().map(ItemStack::new).toList();
        registration.addRecipes(RECIPE_TYPE_TAKE_IMAGE, paper);
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        IExtendableRecipeCategory<CraftingRecipe, ICraftingCategoryExtension> craftingCategory = registration.getCraftingCategory();
        craftingCategory.addCategoryExtension(ImageCloningRecipe.class, ImageCloneExtension::new);
    }

}
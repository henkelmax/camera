package de.maxhenkel.camera.integration.jei;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.RecipeImageCloning;
import de.maxhenkel.camera.gui.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.util.ResourceLocation;

import java.util.stream.Collectors;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    public static final ResourceLocation CATEGORY_TAKE_IMAGE = new ResourceLocation(Main.MODID, "take_image");

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Main.MODID, "camera");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Main.CAMERA), CATEGORY_TAKE_IMAGE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new TakeImageCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiScreenHandler(ImageScreen.class, NoJEIGuiProperties::new);
        registration.addGuiScreenHandler(AlbumScreen.class, NoJEIGuiProperties::new);
        registration.addGuiScreenHandler(LecternAlbumScreen.class, NoJEIGuiProperties::new);
        registration.addGuiScreenHandler(ResizeFrameScreen.class, NoJEIGuiProperties::new);
        registration.addGuiScreenHandler(CameraScreen.class, NoJEIGuiProperties::new);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(Main.SERVER_CONFIG.cameraConsumeItem.getValues().stream().map(item -> new ItemStack(item, Main.SERVER_CONFIG.cameraConsumeItemAmount.get())).collect(Collectors.toList()), CATEGORY_TAKE_IMAGE);
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        IExtendableRecipeCategory<ICraftingRecipe, ICraftingCategoryExtension> craftingCategory = registration.getCraftingCategory();
        craftingCategory.addCategoryExtension(RecipeImageCloning.class, ImageCopyExtension::new);
    }

}
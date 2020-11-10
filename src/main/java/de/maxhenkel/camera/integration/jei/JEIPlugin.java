package de.maxhenkel.camera.integration.jei;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.RecipeImageCloning;
import de.maxhenkel.camera.gui.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.util.ResourceLocation;

@mezz.jei.api.JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Main.MODID, "camera");
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
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        IExtendableRecipeCategory<ICraftingRecipe, ICraftingCategoryExtension> craftingCategory = registration.getCraftingCategory();
        craftingCategory.addCategoryExtension(RecipeImageCloning.class, ImageCopyExtension::new);
    }

}
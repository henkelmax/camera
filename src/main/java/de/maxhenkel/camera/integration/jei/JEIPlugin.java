package de.maxhenkel.camera.integration.jei;

import de.maxhenkel.camera.ImageCloningRecipe;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.IExtendableCraftingRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Main.MODID, "camera");
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
        IExtendableCraftingRecipeCategory craftingCategory = registration.getCraftingCategory();
        craftingCategory.addExtension(ImageCloningRecipe.class, new ImageCloneExtension<>());
    }

}
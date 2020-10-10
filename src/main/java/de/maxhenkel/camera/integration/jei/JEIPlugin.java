package de.maxhenkel.camera.integration.jei;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.AlbumScreen;
import de.maxhenkel.camera.gui.CameraScreen;
import de.maxhenkel.camera.gui.ImageScreen;
import de.maxhenkel.camera.gui.ResizeFrameScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
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
        registration.addGuiScreenHandler(ResizeFrameScreen.class, NoJEIGuiProperties::new);
        registration.addGuiScreenHandler(CameraScreen.class, NoJEIGuiProperties::new);
    }

}
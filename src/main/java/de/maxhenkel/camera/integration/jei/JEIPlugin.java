package de.maxhenkel.camera.integration.jei;

import de.maxhenkel.camera.gui.GuiAlbum;
import de.maxhenkel.camera.gui.GuiCamera;
import de.maxhenkel.camera.gui.GuiImage;
import de.maxhenkel.camera.gui.GuiResizeFrame;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        registry.addAdvancedGuiHandlers(new NoJEIContainerHandler<GuiImage>() {
            @Override
            public Class<GuiImage> getGuiContainerClass() {
                return GuiImage.class;
            }
        });
        registry.addAdvancedGuiHandlers(new NoJEIContainerHandler<GuiAlbum>() {
            @Override
            public Class<GuiAlbum> getGuiContainerClass() {
                return GuiAlbum.class;
            }
        });
        registry.addAdvancedGuiHandlers(new NoJEIContainerHandler<GuiResizeFrame>() {
            @Override
            public Class<GuiResizeFrame> getGuiContainerClass() {
                return GuiResizeFrame.class;
            }
        });
        registry.addAdvancedGuiHandlers(new NoJEIContainerHandler<GuiCamera>() {
            @Override
            public Class<GuiCamera> getGuiContainerClass() {
                return GuiCamera.class;
            }
        });
    }
}

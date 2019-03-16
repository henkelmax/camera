package de.maxhenkel.camera.entities;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderFactoryImage implements IRenderFactory<EntityImage> {
    @Override
    public Render<? super EntityImage> createRenderFor(RenderManager manager) {
        return new RenderImage(manager);
    }
}

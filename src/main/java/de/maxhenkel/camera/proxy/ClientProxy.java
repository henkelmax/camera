package de.maxhenkel.camera.proxy;

import de.maxhenkel.camera.ClientEvents;
import de.maxhenkel.camera.ImageTaker;
import de.maxhenkel.camera.entities.EntityImage;
import de.maxhenkel.camera.entities.RenderFactoryImage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    public void preinit(FMLPreInitializationEvent event) {
        super.preinit(event);

        RenderingRegistry.registerEntityRenderingHandler(EntityImage.class, new RenderFactoryImage());
    }

    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(new ImageTaker());
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }

    public void postinit(FMLPostInitializationEvent event) {
        super.postinit(event);
    }

}

package de.maxhenkel.camera.proxy;

import de.maxhenkel.camera.ClientEvents;
import de.maxhenkel.camera.ImageTaker;
import de.maxhenkel.camera.entities.EntityImage;
import de.maxhenkel.camera.entities.RenderFactoryImage;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy {

    public static KeyBinding KEY_NEXT;
    public static KeyBinding KEY_PREVIOUS;

    public void preinit(FMLPreInitializationEvent event) {
        super.preinit(event);

        RenderingRegistry.registerEntityRenderingHandler(EntityImage.class, new RenderFactoryImage());
    }

    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(new ImageTaker());
        MinecraftForge.EVENT_BUS.register(new ClientEvents());

        KEY_NEXT = new KeyBinding("key.next_image", Keyboard.KEY_DOWN, "key.categories.misc");
        ClientRegistry.registerKeyBinding(KEY_NEXT);

        KEY_PREVIOUS = new KeyBinding("key.previous_image", Keyboard.KEY_UP, "key.categories.misc");
        ClientRegistry.registerKeyBinding(KEY_PREVIOUS);
    }

    public void postinit(FMLPostInitializationEvent event) {
        super.postinit(event);
    }

}

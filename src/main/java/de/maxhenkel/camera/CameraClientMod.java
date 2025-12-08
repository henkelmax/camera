package de.maxhenkel.camera;

import de.maxhenkel.camera.entities.ImageRenderer;
import de.maxhenkel.camera.gui.*;
import de.maxhenkel.camera.items.render.ImageSpecialRenderer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

@Mod(value = CameraMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CameraMod.MODID, value = Dist.CLIENT)
public class CameraClientMod {

    public static KeyMapping KEY_NEXT;
    public static KeyMapping KEY_PREVIOUS;

    public CameraClientMod(IEventBus eventBus) {

    }

    @SubscribeEvent
    static void clientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ClientEvents());
        EntityRenderers.register(CameraMod.IMAGE_ENTITY_TYPE.get(), ImageRenderer::new);
    }

    @SubscribeEvent
    static void onRegisterScreens(RegisterMenuScreensEvent containers) {
        containers.<AlbumInventoryContainer, AlbumInventoryScreen>register(CameraMod.ALBUM_INVENTORY_CONTAINER.get(), AlbumInventoryScreen::new);
        containers.<AlbumContainer, LecternAlbumScreen>register(CameraMod.ALBUM_CONTAINER.get(), LecternAlbumScreen::new);
    }

    @SubscribeEvent
    static void registerKeyBinds(RegisterKeyMappingsEvent event) {
        KEY_NEXT = new KeyMapping("key.next_image", GLFW.GLFW_KEY_DOWN, KeyMapping.Category.MISC);
        KEY_PREVIOUS = new KeyMapping("key.previous_image", GLFW.GLFW_KEY_UP, KeyMapping.Category.MISC);
        event.register(KEY_NEXT);
        event.register(KEY_PREVIOUS);
    }

    @SubscribeEvent
    static void registerItemModels(RegisterSpecialModelRendererEvent event) {
        event.register(Identifier.fromNamespaceAndPath(CameraMod.MODID, "image"), ImageSpecialRenderer.Unbaked.MAP_CODEC);
    }

    public static void openImageScreen(ItemStack stack) {
        Minecraft.getInstance().setScreen(new ImageScreen(stack));
    }

    public static void openAlbumScreen(List<UUID> images) {
        Minecraft.getInstance().setScreen(new AlbumScreen(images));
    }

    public static void openResizeFrameScreen(UUID id) {
        Minecraft.getInstance().setScreen(new ResizeFrameScreen(id));
    }

    public static void openCameraScreen(String currentShader) {
        Minecraft.getInstance().setScreen(new CameraScreen(currentShader));
    }

}

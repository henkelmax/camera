package de.maxhenkel.camera;

import de.maxhenkel.camera.entities.ImageEntity;
import de.maxhenkel.camera.entities.ImageRenderer;
import de.maxhenkel.camera.gui.AlbumContainer;
import de.maxhenkel.camera.gui.AlbumInventoryContainer;
import de.maxhenkel.camera.gui.AlbumInventoryScreen;
import de.maxhenkel.camera.gui.LecternAlbumScreen;
import de.maxhenkel.camera.items.AlbumItem;
import de.maxhenkel.camera.items.CameraItem;
import de.maxhenkel.camera.items.ImageFrameItem;
import de.maxhenkel.camera.items.ImageItem;
import de.maxhenkel.camera.net.*;
import de.maxhenkel.corelib.ClientRegistry;
import de.maxhenkel.corelib.CommonRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.simple.SimpleChannel;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Mod(Main.MODID)
public class Main {

    public static final String MODID = "camera";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static SimpleChannel SIMPLE_CHANNEL;
    public static PacketManager PACKET_MANAGER;

    private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);

    public static final DeferredHolder<Item, ImageFrameItem> FRAME_ITEM = ITEM_REGISTER.register("image_frame", ImageFrameItem::new);
    public static final DeferredHolder<Item, CameraItem> CAMERA = ITEM_REGISTER.register("camera", CameraItem::new);
    public static final DeferredHolder<Item, ImageItem> IMAGE = ITEM_REGISTER.register("image", ImageItem::new);
    public static final DeferredHolder<Item, AlbumItem> ALBUM = ITEM_REGISTER.register("album", AlbumItem::new);

    private static final DeferredRegister<MenuType<?>> MENU_REGISTER = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<AlbumInventoryContainer>> ALBUM_INVENTORY_CONTAINER = MENU_REGISTER.register("album_inventory", () -> IMenuTypeExtension.create((windowId, inv, data) -> new AlbumInventoryContainer(windowId, inv)));
    public static final DeferredHolder<MenuType<?>, MenuType<AlbumContainer>> ALBUM_CONTAINER = MENU_REGISTER.register("album", () -> IMenuTypeExtension.create((windowId, inv, data) -> new AlbumContainer(windowId)));

    private static final DeferredRegister<EntityType<?>> ENTITY_REGISTER = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<ImageEntity>> IMAGE_ENTITY_TYPE = ENTITY_REGISTER.register("image_frame", Main::createImageEntityType);

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, MODID);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ImageCloningRecipe>> IMAGE_CLONING_SERIALIZER = RECIPE_SERIALIZER_REGISTER.register("image_cloning", ImageCloningRecipe.ImageCloningSerializer::new);

    public static TagKey<Item> IMAGE_PAPER = ItemTags.create(new ResourceLocation(Main.MODID, "image_paper"));

    public static ServerConfig SERVER_CONFIG;
    public static ClientConfig CLIENT_CONFIG;

    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_NEXT;
    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_PREVIOUS;

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CreativeTabEvents::onCreativeModeTabBuildContents);

        SERVER_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.SERVER, ServerConfig.class, true);
        CLIENT_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.CLIENT, ClientConfig.class, true);

        if (FMLEnvironment.dist.isClient()) {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::registerKeyBinds);
        }

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEM_REGISTER.register(eventBus);
        MENU_REGISTER.register(eventBus);
        ENTITY_REGISTER.register(eventBus);
        RECIPE_SERIALIZER_REGISTER.register(eventBus);
        ModSounds.SOUND_REGISTER.register(eventBus);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        SIMPLE_CHANNEL = CommonRegistry.registerChannel(Main.MODID, "default");
        PACKET_MANAGER = new PacketManager();
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 0, MessagePartialImage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 1, MessageTakeImage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 2, MessageRequestImage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 3, MessageImage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 4, MessageImageUnavailable.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 5, MessageSetShader.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 6, MessageDisableCameraMode.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 7, MessageResizeFrame.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 8, MessageRequestUploadCustomImage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 9, MessageUploadCustomImage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 10, MessageAlbumPage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 11, MessageTakeBook.class);
    }

    private static EntityType<ImageEntity> createImageEntityType() {
        return CommonRegistry.registerEntity(Main.MODID, "image_frame", MobCategory.MISC, ImageEntity.class, builder -> {
            builder.setTrackingRange(256)
                    .setUpdateInterval(20)
                    .setShouldReceiveVelocityUpdates(false)
                    .sized(1F, 1F)
                    .setCustomClientFactory((spawnEntity, world) -> new ImageEntity(world));
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ClientEvents());

        ClientRegistry.<AlbumInventoryContainer, AlbumInventoryScreen>registerScreen(Main.ALBUM_INVENTORY_CONTAINER.get(), AlbumInventoryScreen::new);
        ClientRegistry.<AlbumContainer, LecternAlbumScreen>registerScreen(Main.ALBUM_CONTAINER.get(), LecternAlbumScreen::new);

        EntityRenderers.register(IMAGE_ENTITY_TYPE.get(), ImageRenderer::new);
    }

    @OnlyIn(Dist.CLIENT)
    public void registerKeyBinds(RegisterKeyMappingsEvent event) {
        KEY_NEXT = new KeyMapping("key.next_image", GLFW.GLFW_KEY_DOWN, "key.categories.misc");
        KEY_PREVIOUS = new KeyMapping("key.previous_image", GLFW.GLFW_KEY_UP, "key.categories.misc");
        event.register(KEY_NEXT);
        event.register(KEY_PREVIOUS);
    }
}

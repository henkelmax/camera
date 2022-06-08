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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Mod(Main.MODID)
public class Main {

    public static final String MODID = "camera";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static SimpleChannel SIMPLE_CHANNEL;
    public static PacketManager PACKET_MANAGER;

    private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<ImageFrameItem> FRAME_ITEM = ITEM_REGISTER.register("image_frame", ImageFrameItem::new);
    public static final RegistryObject<CameraItem> CAMERA = ITEM_REGISTER.register("camera", CameraItem::new);
    public static final RegistryObject<ImageItem> IMAGE = ITEM_REGISTER.register("image", ImageItem::new);
    public static final RegistryObject<AlbumItem> ALBUM = ITEM_REGISTER.register("album", AlbumItem::new);

    private static final DeferredRegister<MenuType<?>> MENU_REGISTER = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);
    public static final RegistryObject<MenuType<AlbumInventoryContainer>> ALBUM_INVENTORY_CONTAINER = MENU_REGISTER.register("album_inventory", () -> IForgeMenuType.create((windowId, inv, data) -> new AlbumInventoryContainer(windowId, inv)));
    public static final RegistryObject<MenuType<AlbumContainer>> ALBUM_CONTAINER = MENU_REGISTER.register("album", () -> IForgeMenuType.create((windowId, inv, data) -> new AlbumContainer(windowId)));

    private static final DeferredRegister<EntityType<?>> ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.ENTITIES, MODID);
    public static final RegistryObject<EntityType<ImageEntity>> IMAGE_ENTITY_TYPE = ENTITY_REGISTER.register("image_frame", Main::createImageEntityType);

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    public static final RegistryObject<RecipeSerializer<ImageCloningRecipe>> IMAGE_CLONING_SERIALIZER = RECIPE_SERIALIZER_REGISTER.register("image_cloning", ImageCloningRecipe.ImageCloningSerializer::new);

    public static TagKey<Item> IMAGE_PAPER = ItemTags.create(new ResourceLocation(Main.MODID, "image_paper"));

    public static ServerConfig SERVER_CONFIG;
    public static ClientConfig CLIENT_CONFIG;

    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_NEXT;
    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_PREVIOUS;

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

        SERVER_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.SERVER, ServerConfig.class, true);
        CLIENT_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.CLIENT, ClientConfig.class, true);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup));

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEM_REGISTER.register(eventBus);
        MENU_REGISTER.register(eventBus);
        ENTITY_REGISTER.register(eventBus);
        RECIPE_SERIALIZER_REGISTER.register(eventBus);
        ModSounds.SOUND_REGISTER.register(eventBus);
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ServerEvents());

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

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());

        KEY_NEXT = ClientRegistry.registerKeyBinding("key.next_image", "key.categories.misc", GLFW.GLFW_KEY_DOWN);

        KEY_PREVIOUS = ClientRegistry.registerKeyBinding("key.previous_image", "key.categories.misc", GLFW.GLFW_KEY_UP);

        ClientRegistry.<AlbumInventoryContainer, AlbumInventoryScreen>registerScreen(Main.ALBUM_INVENTORY_CONTAINER.get(), AlbumInventoryScreen::new);
        ClientRegistry.<AlbumContainer, LecternAlbumScreen>registerScreen(Main.ALBUM_CONTAINER.get(), LecternAlbumScreen::new);

        EntityRenderers.register(IMAGE_ENTITY_TYPE.get(), ImageRenderer::new);
    }
}

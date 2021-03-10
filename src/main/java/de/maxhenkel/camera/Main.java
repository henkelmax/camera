package de.maxhenkel.camera;

import de.maxhenkel.camera.entities.ImageEntity;
import de.maxhenkel.camera.entities.ImageRenderer;
import de.maxhenkel.camera.gui.AlbumContainer;
import de.maxhenkel.camera.gui.AlbumInventoryScreen;
import de.maxhenkel.camera.gui.AlbumInventoryContainer;
import de.maxhenkel.camera.gui.LecternAlbumScreen;
import de.maxhenkel.camera.items.AlbumItem;
import de.maxhenkel.camera.items.CameraItem;
import de.maxhenkel.camera.items.ImageFrameItem;
import de.maxhenkel.camera.items.ImageItem;
import de.maxhenkel.camera.net.*;
import de.maxhenkel.corelib.ClientRegistry;
import de.maxhenkel.corelib.CommonRegistry;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Mod(Main.MODID)
public class Main {

    public static final String MODID = "camera";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static SimpleChannel SIMPLE_CHANNEL;
    public static PacketManager PACKET_MANAGER;

    public static SpecialRecipeSerializer<RecipeImageCloning> CRAFTING_SPECIAL_IMAGE_CLONING;
    public static ImageFrameItem FRAME_ITEM;
    public static CameraItem CAMERA;
    public static ImageItem IMAGE;
    public static AlbumItem ALBUM;
    public static ContainerType<AlbumInventoryContainer> ALBUM_INVENTORY_CONTAINER;
    public static ContainerType<AlbumContainer> ALBUM_CONTAINER;
    public static EntityType<ImageEntity> IMAGE_ENTITY_TYPE;
    public static ITag<Item> IMAGE_PAPER = ItemTags.bind(new ResourceLocation(Main.MODID, "image_paper").toString());

    public static ServerConfig SERVER_CONFIG;
    public static ClientConfig CLIENT_CONFIG;

    @OnlyIn(Dist.CLIENT)
    public static KeyBinding KEY_NEXT;
    @OnlyIn(Dist.CLIENT)
    public static KeyBinding KEY_PREVIOUS;

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, this::registerItems);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(SoundEvent.class, this::registerSounds);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(EntityType.class, this::registerEntities);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(ContainerType.class, this::registerContainers);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(IRecipeSerializer.class, this::registerRecipes);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

        SERVER_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.SERVER, ServerConfig.class, true);
        CLIENT_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.CLIENT, ClientConfig.class, true);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup));
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

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());

        KEY_NEXT = ClientRegistry.registerKeyBinding("key.next_image", "key.categories.misc", GLFW.GLFW_KEY_DOWN);

        KEY_PREVIOUS = ClientRegistry.registerKeyBinding("key.previous_image", "key.categories.misc", GLFW.GLFW_KEY_UP);

        ClientRegistry.<AlbumInventoryContainer, AlbumInventoryScreen>registerScreen(Main.ALBUM_INVENTORY_CONTAINER, AlbumInventoryScreen::new);
        ClientRegistry.<AlbumContainer, LecternAlbumScreen>registerScreen(Main.ALBUM_CONTAINER, LecternAlbumScreen::new);

        RenderingRegistry.registerEntityRenderingHandler(IMAGE_ENTITY_TYPE, ImageRenderer::new);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                FRAME_ITEM = new ImageFrameItem(),
                CAMERA = new CameraItem(),
                IMAGE = new ImageItem(),
                ALBUM = new AlbumItem()
        );
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(
                ModSounds.TAKE_IMAGE
        );
    }

    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        IMAGE_ENTITY_TYPE = CommonRegistry.registerEntity(Main.MODID, "image_frame", EntityClassification.MISC, ImageEntity.class, builder -> {
            builder.setTrackingRange(256)
                    .setUpdateInterval(20)
                    .setShouldReceiveVelocityUpdates(false)
                    .sized(1F, 1F)
                    .setCustomClientFactory((spawnEntity, world) -> new ImageEntity(world));
        });
        event.getRegistry().register(IMAGE_ENTITY_TYPE);
    }

    @SubscribeEvent
    public void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        ALBUM_INVENTORY_CONTAINER = new ContainerType<>(AlbumInventoryContainer::new);
        ALBUM_INVENTORY_CONTAINER.setRegistryName(new ResourceLocation(Main.MODID, "album_inventory"));
        event.getRegistry().register(ALBUM_INVENTORY_CONTAINER);

        ALBUM_CONTAINER = new ContainerType<>((i, playerInventory) -> new AlbumContainer(i));
        ALBUM_CONTAINER.setRegistryName(new ResourceLocation(Main.MODID, "album"));
        event.getRegistry().register(ALBUM_CONTAINER);
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        CRAFTING_SPECIAL_IMAGE_CLONING = new SpecialRecipeSerializer<>(RecipeImageCloning::new);
        CRAFTING_SPECIAL_IMAGE_CLONING.setRegistryName(MODID, "crafting_special_imagecloning");
        event.getRegistry().register(CRAFTING_SPECIAL_IMAGE_CLONING);
    }

}

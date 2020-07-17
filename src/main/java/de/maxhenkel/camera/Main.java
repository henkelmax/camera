package de.maxhenkel.camera;

import de.maxhenkel.camera.entities.ImageEntity;
import de.maxhenkel.camera.entities.ImageRenderer;
import de.maxhenkel.camera.gui.AlbumInventoryScreen;
import de.maxhenkel.camera.gui.ContainerAlbumInventory;
import de.maxhenkel.camera.items.AlbumItem;
import de.maxhenkel.camera.items.CameraItem;
import de.maxhenkel.camera.items.ImageFrameItem;
import de.maxhenkel.camera.items.ImageItem;
import de.maxhenkel.camera.net.*;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
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
    public static ContainerType<ContainerAlbumInventory> ALBUM_INVENTORY_CONTAINER;
    public static EntityType<ImageEntity> IMAGE_ENTITY_TYPE;

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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::configEvent);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup));
    }

    @SubscribeEvent
    public void configEvent(ModConfig.ModConfigEvent event) {
        if (event.getConfig().getType() == ModConfig.Type.SERVER) {
            Config.onServerConfigUpdate();
        } else if (event.getConfig().getType() == ModConfig.Type.CLIENT) {
            Config.onClientConfigUpdate();
        }
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ServerEvents());

        SIMPLE_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(Main.MODID, "default"), () -> "1.0.0", s -> true, s -> true);
        PACKET_MANAGER = new PacketManager();
        SIMPLE_CHANNEL.registerMessage(0, MessagePartialImage.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessagePartialImage().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(1, MessageTakeImage.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageTakeImage().fromBytes(buf), (msg, fun) -> msg.executeClientSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(2, MessageRequestImage.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageRequestImage().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(3, MessageImage.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageImage().fromBytes(buf), (msg, fun) -> msg.executeClientSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(4, MessageImageUnavailable.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageImageUnavailable().fromBytes(buf), (msg, fun) -> msg.executeClientSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(5, MessageSetShader.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageSetShader().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(6, MessageDisableCameraMode.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageDisableCameraMode().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(7, MessageResizeFrame.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageResizeFrame().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(8, MessageRequestUploadCustomImage.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageRequestUploadCustomImage().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(9, MessageUploadCustomImage.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageUploadCustomImage().fromBytes(buf), (msg, fun) -> msg.executeClientSide(fun.get()));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ImageTaker());
        MinecraftForge.EVENT_BUS.register(new ClientEvents());

        KEY_NEXT = new KeyBinding("key.next_image", GLFW.GLFW_KEY_DOWN, "key.categories.misc");
        ClientRegistry.registerKeyBinding(KEY_NEXT);

        KEY_PREVIOUS = new KeyBinding("key.previous_image", GLFW.GLFW_KEY_UP, "key.categories.misc");
        ClientRegistry.registerKeyBinding(KEY_PREVIOUS);

        ScreenManager.IScreenFactory factory = (ScreenManager.IScreenFactory<ContainerAlbumInventory, AlbumInventoryScreen>) (container, playerInventory, name) -> new AlbumInventoryScreen(playerInventory, container, name);
        ScreenManager.registerFactory(Main.ALBUM_INVENTORY_CONTAINER, factory);

        RenderingRegistry.registerEntityRenderingHandler(IMAGE_ENTITY_TYPE, manager -> new ImageRenderer(manager));
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
        IMAGE_ENTITY_TYPE = EntityType.Builder.<ImageEntity>create(ImageEntity::new, EntityClassification.MISC)
                .setTrackingRange(256)
                .setUpdateInterval(20)
                .setShouldReceiveVelocityUpdates(false)
                .size(1F, 1F)
                .setCustomClientFactory((spawnEntity, world) -> new ImageEntity(world))
                .build(Main.MODID + ":image_frame");
        IMAGE_ENTITY_TYPE.setRegistryName(new ResourceLocation(Main.MODID, "image_frame"));
        event.getRegistry().register(IMAGE_ENTITY_TYPE);
    }

    @SubscribeEvent
    public void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        ALBUM_INVENTORY_CONTAINER = new ContainerType<>(ContainerAlbumInventory::new);
        ALBUM_INVENTORY_CONTAINER.setRegistryName(new ResourceLocation(Main.MODID, "album_inventory"));
        event.getRegistry().register(ALBUM_INVENTORY_CONTAINER);
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        CRAFTING_SPECIAL_IMAGE_CLONING = new SpecialRecipeSerializer<>(RecipeImageCloning::new);
        CRAFTING_SPECIAL_IMAGE_CLONING.setRegistryName(MODID, "crafting_special_imagecloning");
        event.getRegistry().register(CRAFTING_SPECIAL_IMAGE_CLONING);
    }
}

package de.maxhenkel.camera;

import de.maxhenkel.camera.entities.EntityImage;
import de.maxhenkel.camera.entities.RenderImage;
import de.maxhenkel.camera.gui.*;
import de.maxhenkel.camera.items.ItemAlbum;
import de.maxhenkel.camera.items.ItemCamera;
import de.maxhenkel.camera.items.ItemImage;
import de.maxhenkel.camera.items.ItemImageFrame;
import de.maxhenkel.camera.net.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.Registry;
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
import net.minecraftforge.registries.ObjectHolder;
import org.lwjgl.glfw.GLFW;

@Mod(Main.MODID)
public class Main {

    public static final String MODID = "camera";

    private static Main instance;

    public static SimpleChannel SIMPLE_CHANNEL;
    public static PacketManager PACKET_MANAGER;

    // TODO
    // public static final RecipeSerializers.SimpleSerializer<RecipeImageCloning> CRAFTING_SPECIAL_IMAGE_CLONING = registerRecipe(new SimpleSerializer<>("crafting_special_imagecloning", RecipeImageCloning::new));

    @ObjectHolder(MODID + ":image_frame")
    public static ItemImageFrame FRAME_ITEM;

    @ObjectHolder(MODID + ":camera")
    public static ItemCamera CAMERA;

    @ObjectHolder(MODID + ":image")
    public static ItemImage IMAGE;

    @ObjectHolder(MODID + ":album")
    public static ItemAlbum ALBUM;

    @OnlyIn(Dist.CLIENT)
    public static KeyBinding KEY_NEXT;

    @OnlyIn(Dist.CLIENT)
    public static KeyBinding KEY_PREVIOUS;

    public static ContainerType ALBUM_INVENTORY_CONTAINER = registerContainer("album_inventory", ContainerAlbumInventory::new);

    // TODO
    // public static final EntityType<EntityImage> IMAGE_ENTITY_TYPE = EntityType.register(MODID + ":image_frame", EntityType.Builder.create(EntityImage.class, EntityImage::new).tracker(256, 20, false));
    public static final EntityType<EntityImage> IMAGE_ENTITY_TYPE = registerEntity(MODID + ":image_frame", EntityType.Builder.<EntityImage>create(EntityImage::new, EntityClassification.MISC).setTrackingRange(256).setUpdateInterval(20).setShouldReceiveVelocityUpdates(false).size(1F, 1F));

    public Main() {
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, this::registerItems);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(SoundEvent.class, this::registerSounds);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::configEvent);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup);
        });
    }

    @SubscribeEvent
    public void configEvent(ModConfig.ModConfigEvent event) {
        if (event.getConfig().getType() == ModConfig.Type.SERVER) {
            Config.loadServer();
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
        /*CustomRecipeBuilder.customRecipe(CRAFTING_SPECIAL_IMAGE_CLONING).build((recipe) -> {

        }, "book_cloning");*/
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityImage.class, manager -> new RenderImage(manager));
        GUIManager.clientSetup();
        MinecraftForge.EVENT_BUS.register(new ImageTaker());
        MinecraftForge.EVENT_BUS.register(new ClientEvents());

        KEY_NEXT = new KeyBinding("key.next_image", GLFW.GLFW_KEY_DOWN, "key.categories.misc");
        ClientRegistry.registerKeyBinding(KEY_NEXT);

        KEY_PREVIOUS = new KeyBinding("key.previous_image", GLFW.GLFW_KEY_UP, "key.categories.misc");
        ClientRegistry.registerKeyBinding(KEY_PREVIOUS);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                FRAME_ITEM = new ItemImageFrame(),
                CAMERA = new ItemCamera(),
                IMAGE = new ItemImage(),
                ALBUM = new ItemAlbum()
        );
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(
                ModSounds.TAKE_IMAGE
        );
    }

    public static Main instance() {
        return instance;
    }

    //TODO
    private static <T extends Entity> EntityType<T> registerEntity(String id, EntityType.Builder<T> builder) {
        return Registry.register(Registry.field_212629_r, id, builder.build(id));
    }

    private static <T extends Container> ContainerType<T> registerContainer(String name, ContainerType.IFactory<T> factory) {
        return Registry.register(Registry.field_218366_G, new ResourceLocation(Main.MODID, name), new ContainerType<>(factory));
    }
}

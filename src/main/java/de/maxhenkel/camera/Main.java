package de.maxhenkel.camera;

import com.mojang.serialization.Codec;
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
import de.maxhenkel.corelib.CommonRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
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
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Mod(Main.MODID)
public class Main {

    public static final String MODID = "camera";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

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

    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPE_REGISTER = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, Main.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ImageData>> IMAGE_DATA_COMPONENT = DATA_COMPONENT_TYPE_REGISTER.register("image", () -> DataComponentType.<ImageData>builder().persistent(ImageData.CODEC).networkSynchronized(ImageData.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> ACTIVE_DATA_COMPONENT = DATA_COMPONENT_TYPE_REGISTER.register("active", () -> DataComponentType.<Unit>builder().networkSynchronized(StreamCodec.unit(Unit.INSTANCE)).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SHADER_DATA_COMPONENT = DATA_COMPONENT_TYPE_REGISTER.register("shader", () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());

    public static TagKey<Item> IMAGE_PAPER = ItemTags.create(ResourceLocation.fromNamespaceAndPath(Main.MODID, "image_paper"));

    public static ServerConfig SERVER_CONFIG;
    public static ClientConfig CLIENT_CONFIG;

    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_NEXT;
    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_PREVIOUS;

    public Main(IEventBus eventBus) {
        eventBus.addListener(this::commonSetup);
        eventBus.addListener(this::onRegisterPayloadHandler);
        eventBus.addListener(CreativeTabEvents::onCreativeModeTabBuildContents);

        SERVER_CONFIG = CommonRegistry.registerConfig(MODID, ModConfig.Type.SERVER, ServerConfig.class, true);
        CLIENT_CONFIG = CommonRegistry.registerConfig(MODID, ModConfig.Type.CLIENT, ClientConfig.class, true);

        if (FMLEnvironment.dist.isClient()) {
            eventBus.addListener(Main.this::clientSetup);
            eventBus.addListener(Main.this::registerKeyBinds);
            eventBus.addListener(Main.this::onRegisterScreens);
        }
        ITEM_REGISTER.register(eventBus);
        MENU_REGISTER.register(eventBus);
        ENTITY_REGISTER.register(eventBus);
        RECIPE_SERIALIZER_REGISTER.register(eventBus);
        DATA_COMPONENT_TYPE_REGISTER.register(eventBus);
        ModSounds.SOUND_REGISTER.register(eventBus);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        PACKET_MANAGER = new PacketManager();
    }

    private static EntityType<ImageEntity> createImageEntityType() {
        return CommonRegistry.registerEntity(Main.MODID, "image_frame", MobCategory.MISC, ImageEntity.class, builder -> {
            builder.setTrackingRange(256)
                    .setUpdateInterval(20)
                    .setShouldReceiveVelocityUpdates(false)
                    .sized(1F, 1F);
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ClientEvents());
        EntityRenderers.register(IMAGE_ENTITY_TYPE.get(), ImageRenderer::new);
    }

    @OnlyIn(Dist.CLIENT)
    public void onRegisterScreens(RegisterMenuScreensEvent containers) {
        containers.<AlbumInventoryContainer, AlbumInventoryScreen>register(Main.ALBUM_INVENTORY_CONTAINER.get(), AlbumInventoryScreen::new);
        containers.<AlbumContainer, LecternAlbumScreen>register(Main.ALBUM_CONTAINER.get(), LecternAlbumScreen::new);
    }

    public void onRegisterPayloadHandler(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID).versioned("0");
        CommonRegistry.registerMessage(registrar, MessagePartialImage.class);
        CommonRegistry.registerMessage(registrar, MessageTakeImage.class);
        CommonRegistry.registerMessage(registrar, MessageRequestImage.class);
        CommonRegistry.registerMessage(registrar, MessageImage.class);
        CommonRegistry.registerMessage(registrar, MessageImageUnavailable.class);
        CommonRegistry.registerMessage(registrar, MessageSetShader.class);
        CommonRegistry.registerMessage(registrar, MessageDisableCameraMode.class);
        CommonRegistry.registerMessage(registrar, MessageResizeFrame.class);
        CommonRegistry.registerMessage(registrar, MessageRequestUploadCustomImage.class);
        CommonRegistry.registerMessage(registrar, MessageUploadCustomImage.class);
        CommonRegistry.registerMessage(registrar, MessageAlbumPage.class);
        CommonRegistry.registerMessage(registrar, MessageTakeBook.class);
    }

    @OnlyIn(Dist.CLIENT)
    public void registerKeyBinds(RegisterKeyMappingsEvent event) {
        KEY_NEXT = new KeyMapping("key.next_image", GLFW.GLFW_KEY_DOWN, "key.categories.misc");
        KEY_PREVIOUS = new KeyMapping("key.previous_image", GLFW.GLFW_KEY_UP, "key.categories.misc");
        event.register(KEY_NEXT);
        event.register(KEY_PREVIOUS);
    }
}

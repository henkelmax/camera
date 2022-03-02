package de.maxhenkel.camera;

import de.maxhenkel.corelib.config.ConfigBase;
import de.maxhenkel.corelib.tag.Tag;
import de.maxhenkel.corelib.tag.TagUtils;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class ServerConfig extends ConfigBase {

    public final ForgeConfigSpec.IntValue imageCooldown;
    private final ForgeConfigSpec.ConfigValue<String> cameraConsumeItemSpec;
    public final ForgeConfigSpec.IntValue cameraConsumeItemAmount;
    public final ForgeConfigSpec.IntValue maxImageSize;
    public final ForgeConfigSpec.DoubleValue imageCompression;
    public final ForgeConfigSpec.BooleanValue allowImageUpload;
    public final ForgeConfigSpec.BooleanValue frameOnlyOwnerModify;
    public final ForgeConfigSpec.BooleanValue advancedImageData;
    public final ForgeConfigSpec.IntValue advancedDataMaxEntities;

    public Tag<Item> cameraConsumeItem;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
        super(builder);
        imageCooldown = builder
                .comment("The time in milliseconds the camera will be on cooldown after taking an image")
                .defineInRange("camera.cooldown", 5000, 100, Integer.MAX_VALUE);
        cameraConsumeItemSpec = builder
                .comment("The item that is consumed when taking an image", "If it starts with '#' it is a tag")
                .define("camera.consumed_item.item", "#camera:image_paper");
        cameraConsumeItemAmount = builder
                .comment("The amount of the item that is consumed when taking an image")
                .defineInRange("camera.consumed_item.amount", 1, 1, Short.MAX_VALUE);
        maxImageSize = builder
                .comment("The maximum size of an image in bytes when transferred to the server", "Higher values mean more delay/lag between taking an image and getting it into your inventory")
                .defineInRange("image.max_size", 200_000, 50_000, 1_000_000);
        imageCompression = builder
                .comment("The amount of jpeg compression applied to the image", "If the image exceeds the 'max_image_size', it will get compressed anyways")
                .defineInRange("image.compression", 0.5D, 0.1D, 1D);
        allowImageUpload = builder
                .comment("If it is allowed to upload custom images")
                .define("image.allow_upload", true);
        frameOnlyOwnerModify = builder
                .comment("If only the owner can modify or break the image frame")
                .define("image_frame.only_owner_modify", false);
        advancedImageData = builder
                .comment("If the image items should store additional data", "This isn't used by the mod itself", "Only enable this if you know what you are doing")
                .define("advanced_data.enable", false);
        advancedDataMaxEntities = builder
                .comment("The amount of entities that should be stored")
                .defineInRange("advanced_data.max_entities", 16, 1, 128);
    }

    @Override
    public void onReload(ModConfigEvent event) {
        super.onReload(event);
        cameraConsumeItem = TagUtils.getItem(cameraConsumeItemSpec.get());
        if (cameraConsumeItem == null) {
            Main.LOGGER.error("Can't read config value 'camera.consumed_item.item'. Defaulting to '#camera:image_paper'.");
            cameraConsumeItem = Main.IMAGE_PAPER;
        }
    }

}

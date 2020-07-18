package de.maxhenkel.camera;

import de.maxhenkel.corelib.config.ConfigBase;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

public class ServerConfig extends ConfigBase {

    public final ForgeConfigSpec.IntValue imageCooldown;
    private final ForgeConfigSpec.ConfigValue<String> cameraConsumeItemSpec;
    public final ForgeConfigSpec.IntValue cameraConsumeItemAmount;
    public final ForgeConfigSpec.IntValue maxImageSize;
    public final ForgeConfigSpec.DoubleValue imageCompression;
    public final ForgeConfigSpec.BooleanValue allowImageUpload;

    public Item cameraConsumeItem;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
        super(builder);
        imageCooldown = builder
                .comment("The time in milliseconds the camera will be on cooldown after taking an image")
                .defineInRange("camera.cooldown", 5000, 100, Integer.MAX_VALUE);
        cameraConsumeItemSpec = builder
                .comment("The item that is consumed when taking an image")
                .define("camera.consumed_item.item", "minecraft:paper");
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
    }

    @Override
    public void onReload(ModConfig.ModConfigEvent event) {
        super.onReload(event);
        cameraConsumeItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(cameraConsumeItemSpec.get()));
        if (cameraConsumeItem == null) {
            cameraConsumeItem = Items.PAPER;
        }
    }

}

package de.maxhenkel.camera;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.text.SimpleDateFormat;

public class Config {

    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        Pair<ServerConfig, ForgeConfigSpec> specPairServer = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPairServer.getRight();
        SERVER = specPairServer.getLeft();
    }

    public static SimpleDateFormat imageDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    public static int imageCooldown = 5000;
    public static ItemStack cameraConsume;

    public static void loadServer() {
        imageDateFormat = new SimpleDateFormat(SERVER.imageDateFormat.get());
        imageCooldown = SERVER.imageCooldown.get();
        cameraConsume = ItemTools.deserializeItemStack(SERVER.cameraConsume.get());
    }

    public static class ServerConfig {
        public ForgeConfigSpec.IntValue imageCooldown;
        public ForgeConfigSpec.ConfigValue<String> imageDateFormat;
        public ForgeConfigSpec.ConfigValue<String> cameraConsume;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            imageCooldown = builder
                    .comment("The time in milliseconds the camera will be on cooldown after taking an image")
                    .translation("image_cooldown")
                    .defineInRange("image_cooldown", 5000, 100, Integer.MAX_VALUE);
            imageDateFormat = builder
                    .comment("The format the date will be displayed on the image")
                    .translation("image_date_format")
                    .define("image_date_format", "MM/dd/yyyy HH:mm");
            cameraConsume = builder
                    .comment("The Item that is consumed when taking an image")
                    .translation("camera_consume")
                    .define("camera_consume_item", ItemTools.serializeItemStack(new ItemStack(Items.PAPER, 1)));
        }
    }

}

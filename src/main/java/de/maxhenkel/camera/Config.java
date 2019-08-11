package de.maxhenkel.camera;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.text.SimpleDateFormat;

public class Config {

    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        Pair<ServerConfig, ForgeConfigSpec> specPairServer = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPairServer.getRight();
        SERVER = specPairServer.getLeft();

        Pair<ClientConfig, ForgeConfigSpec> specPairClient = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPairClient.getRight();
        CLIENT = specPairClient.getLeft();
    }

    public static class ServerConfig {
        public ForgeConfigSpec.IntValue IMAGE_COOLDOWN;
        public ForgeConfigSpec.ConfigValue<String> CAMERA_CONSUME_ITEM;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            IMAGE_COOLDOWN = builder
                    .comment("The time in milliseconds the camera will be on cooldown after taking an image")
                    .defineInRange("image_cooldown", 5000, 100, Integer.MAX_VALUE);

            CAMERA_CONSUME_ITEM = builder
                    .comment("The Item that is consumed when taking an image")
                    .define("camera_consume_item", ItemTools.serializeItemStack(new ItemStack(Items.PAPER, 1)));
        }
    }

    public static class ClientConfig {
        public ForgeConfigSpec.ConfigValue<String> IMAGE_DATE_FORMAT;
        public ForgeConfigSpec.ConfigValue<String> LAST_IMAGE_PATH;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            IMAGE_DATE_FORMAT = builder
                    .comment("The format the date will be displayed on the image")
                    .define("image_date_format", "MM/dd/yyyy HH:mm");
            LAST_IMAGE_PATH = builder.define("last_image_path", "");
        }
    }

    private static SimpleDateFormat getDateFormat(String format) {
        return new SimpleDateFormat(format);
    }

    private static ItemStack getItemStack(String stack) {
        return ItemTools.deserializeItemStack(stack);
    }

    private static ItemStack cachedStack;

    public static ItemStack getConsumingStack() {
        if (cachedStack == null) {
            cachedStack = getItemStack(SERVER.CAMERA_CONSUME_ITEM.get());
        }
        return cachedStack;
    }

    private static SimpleDateFormat cachedDateFormat;

    public static SimpleDateFormat getImageDateFormat() {
        if (cachedDateFormat == null) {
            cachedDateFormat = getDateFormat(CLIENT.IMAGE_DATE_FORMAT.get());
        }
        return cachedDateFormat;
    }

    public static void onServerConfigUpdate() {
        cachedStack = null;
    }

    public static void onClientConfigUpdate() {
        cachedDateFormat = null;
    }

}

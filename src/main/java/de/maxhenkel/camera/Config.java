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
        public ForgeConfigSpec.IntValue MAX_IMAGE_SIZE;
        public ForgeConfigSpec.BooleanValue ALLOW_IMAGE_UPLOAD;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            IMAGE_COOLDOWN = builder
                    .comment("The time in milliseconds the camera will be on cooldown after taking an image")
                    .defineInRange("image_cooldown", 5000, 100, Integer.MAX_VALUE);

            CAMERA_CONSUME_ITEM = builder
                    .comment("The Item that is consumed when taking an image")
                    .define("camera_consume_item", ItemTools.serializeItemStack(new ItemStack(Items.PAPER, 1)));

            MAX_IMAGE_SIZE = builder
                    .comment("The maximum size of an image in bytes when transferred to the server", "Higher values mean more delay/lag between taking an image and getting it into your inventory")
                    .defineInRange("max_image_size", 200_000, 50_000, 1_000_000);

            ALLOW_IMAGE_UPLOAD = builder
                    .comment("If it is allowed to upload custom images")
                    .define("allow_image_upload", true);
        }
    }

    public static class ClientConfig {
        public ForgeConfigSpec.ConfigValue<String> IMAGE_DATE_FORMAT;
        public ForgeConfigSpec.ConfigValue<String> LAST_IMAGE_PATH;
        public ForgeConfigSpec.BooleanValue RENDER_IMAGE_ITEM;
        public ForgeConfigSpec.DoubleValue RESIZE_GUI_OPACITY;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            IMAGE_DATE_FORMAT = builder
                    .comment("The format the date will be displayed on the image")
                    .define("image_date_format", "MM/dd/yyyy HH:mm");
            LAST_IMAGE_PATH = builder.define("last_image_path", "");
            RENDER_IMAGE_ITEM = builder
                    .comment("If the image item should render the actual image (This may cause poor performance)")
                    .define("render_image_item", true);
            RESIZE_GUI_OPACITY = builder
                    .comment("The opacity of the resize image frame GUI")
                    .defineInRange("resize_gui_opacity", 1D, 0D, 1D);
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

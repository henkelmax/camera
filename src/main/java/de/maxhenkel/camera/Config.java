package de.maxhenkel.camera;

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

    public static SimpleDateFormat imageDateFormat=new SimpleDateFormat("MM/dd/yyyy HH:mm");
    public static long imageCooldown=5000;

    public static void loadServer() {
        imageDateFormat=new SimpleDateFormat(SERVER.imageDateFormat.get());
        imageCooldown=SERVER.imageCooldown.get();
    }

    public static class ServerConfig {
        public ForgeConfigSpec.LongValue imageCooldown;
        public ForgeConfigSpec.ConfigValue<String> imageDateFormat;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            imageCooldown = builder
                    .comment("The time in milliseconds the CAMERA will be on cooldown after taking an IMAGE")
                    .translation("image_cooldown")
                    .defineInRange("image_cooldown", 5000L, 100L, Integer.MAX_VALUE);
            imageDateFormat = builder
                    .comment("The format the date will be displayed on the IMAGE")
                    .translation("image_date_format")
                    .define("image_date_format", "MM/dd/yyyy HH:mm");
        }
    }

}

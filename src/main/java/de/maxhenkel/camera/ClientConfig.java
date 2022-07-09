package de.maxhenkel.camera;

import de.maxhenkel.corelib.config.ConfigBase;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.text.SimpleDateFormat;

public class ClientConfig extends ConfigBase {

    private final ForgeConfigSpec.ConfigValue<String> imageDateFormatSpec;
    public final ForgeConfigSpec.ConfigValue<String> lastImagePath;
    public final ForgeConfigSpec.BooleanValue renderImageItem;
    public final ForgeConfigSpec.DoubleValue resizeGuiOpacity;

    public SimpleDateFormat imageDateFormat;

    public ClientConfig(ForgeConfigSpec.Builder builder) {
        super(builder);
        imageDateFormatSpec = builder
                .comment("The format the date will be displayed on the image")
                .define("image_date_format", "MM/dd/yyyy HH:mm");
        lastImagePath = builder.define("last_image_path", "");
        renderImageItem = builder
                .comment("If the image item should render the actual image")
                .define("render_image_item", true);
        resizeGuiOpacity = builder
                .comment("The opacity of the resize image frame GUI")
                .defineInRange("resize_gui_opacity", 1D, 0D, 1D);
    }

    @Override
    public void onReload(ModConfigEvent event) {
        super.onReload(event);
        imageDateFormat = new SimpleDateFormat(imageDateFormatSpec.get());
    }

}

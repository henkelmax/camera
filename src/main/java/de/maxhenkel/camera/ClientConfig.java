package de.maxhenkel.camera;

import de.maxhenkel.corelib.config.ConfigBase;

import java.text.SimpleDateFormat;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig extends ConfigBase {

    private final ModConfigSpec.ConfigValue<String> imageDateFormatSpec;
    public final ModConfigSpec.ConfigValue<String> lastImagePath;
    public final ModConfigSpec.BooleanValue renderImageItem;
    public final ModConfigSpec.DoubleValue resizeGuiOpacity;

    public SimpleDateFormat imageDateFormat;

    public ClientConfig(ModConfigSpec.Builder builder) {
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
    public void onLoad(ModConfigEvent.Loading event) {
        super.onLoad(event);
        onConfigChanged();
    }

    @Override
    public void onReload(ModConfigEvent.Reloading event) {
        super.onReload(event);
        onConfigChanged();
    }

    private void onConfigChanged() {
        imageDateFormat = new SimpleDateFormat(imageDateFormatSpec.get());
    }

}

package de.maxhenkel.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT)
public class ImageTaker {

    private static boolean takeScreenshot;
    private static UUID uuid;
    private static boolean hide;

    public static void takeScreenshot(UUID id) {
        if (takeScreenshot && id.equals(uuid)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();

        hide = mc.options.hideGui;
        mc.options.hideGui = true;

        takeScreenshot = true;
        uuid = id;
        mc.setScreen(null);
    }

    @SubscribeEvent
    public static void onRenderTickEnd(RenderFrameEvent.Post event) {
        if (!takeScreenshot) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        Screenshot.takeScreenshot(mc.getMainRenderTarget(), image -> {
            mc.options.hideGui = hide;
            takeScreenshot = false;

            ImageProcessor.sendScreenshotThreaded(uuid, image);
        });
    }

}

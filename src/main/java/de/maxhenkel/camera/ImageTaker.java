package de.maxhenkel.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.util.UUID;

@EventBusSubscriber(modid = CameraMod.MODID, value = Dist.CLIENT)
public class ImageTaker {

    private static UUID pendingImage;
    private static int delay;
    private static boolean hide;

    public static void takeScreenshot(UUID id) {
        if (id.equals(pendingImage)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();

        hide = mc.gui.hud.isHidden();
        if (!hide) {
            mc.gui.hud.toggle();
        }

        pendingImage = id;
        delay = 2;
        mc.gui.setScreen(null);
    }

    @SubscribeEvent
    public static void onRenderTickEnd(RenderFrameEvent.Pre event) {
        if (pendingImage == null) {
            return;
        }
        if (delay > 0) {
            delay--;
            return;
        }

        UUID id = pendingImage;
        pendingImage = null;

        Minecraft mc = Minecraft.getInstance();
        Screenshot.takeScreenshot(mc.gameRenderer.mainRenderTarget(), image -> {
            if (mc.gui.hud.isHidden() != hide) {
                mc.gui.hud.toggle();
            }
            ImageProcessor.sendScreenshotThreaded(id, image);
        });
    }

}

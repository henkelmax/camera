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

    private static int screenshotTime;
    private static UUID uuid;
    private static boolean hide;

    public static void takeScreenshot(UUID id) {
        if (screenshotTime >= 0 && id.equals(uuid)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();

        hide = mc.gui.hud.isHidden();
        if (!hide) {
            mc.gui.hud.toggle();
        }

        screenshotTime = 2;
        uuid = id;
        mc.gui.setScreen(null);
    }

    @SubscribeEvent
    public static void onRenderTickEnd(RenderFrameEvent.Pre event) {
        if (screenshotTime != 0) {
            if (screenshotTime > 0) {
                screenshotTime--;
            }
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        Screenshot.takeScreenshot(mc.gameRenderer.mainRenderTarget(), image -> {
            if (mc.gui.hud.isHidden() != hide) {
                mc.gui.hud.toggle();
            }
            screenshotTime = Integer.MIN_VALUE;

            ImageProcessor.sendScreenshotThreaded(uuid, image);
        });
    }

}

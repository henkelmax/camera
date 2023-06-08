package de.maxhenkel.camera;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT)
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
    public static void onRenderTickEnd(TickEvent.RenderTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.END)) {
            return;
        }

        if (!takeScreenshot) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget());

        mc.options.hideGui = hide;
        takeScreenshot = false;

        ImageProcessor.sendScreenshotThreaded(uuid, image);
    }

}

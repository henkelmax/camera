package de.maxhenkel.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ScreenShotHelper;
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
        Minecraft mc = Minecraft.getInstance();

        hide = mc.gameSettings.hideGUI;
        mc.gameSettings.hideGUI = true;

        takeScreenshot = true;
        uuid = id;
        mc.displayGuiScreen(null);
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

        NativeImage image = ScreenShotHelper.createScreenshot(mc.func_228018_at_().getWidth(), mc.func_228018_at_().getHeight(), mc.getFramebuffer());

        mc.gameSettings.hideGUI = hide;
        takeScreenshot = false;

        ImageProcessor.sendScreenshodThreaded(uuid, image);
    }

}

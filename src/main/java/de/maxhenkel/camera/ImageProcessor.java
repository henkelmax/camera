package de.maxhenkel.camera;

import com.mojang.blaze3d.platform.NativeImage;
import de.maxhenkel.camera.net.MessagePartialImage;
import de.maxhenkel.corelib.net.NetUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

public class ImageProcessor {

    public static void sendScreenshot(UUID uuid, BufferedImage image) {
        byte[] data;
        try {
            data = ImageTools.optimizeImage(image);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int size = data.length;
        if (size < 30_000) {
            NetUtils.sendToServer(Main.SIMPLE_CHANNEL, new MessagePartialImage(uuid, 0, size, data));
        } else {

            int bufferProgress = 0;
            byte[] currentBuffer = new byte[30_000];
            for (int i = 0; i < size; i++) {
                if (bufferProgress >= currentBuffer.length) {
                    NetUtils.sendToServer(Main.SIMPLE_CHANNEL, new MessagePartialImage(uuid, i - currentBuffer.length, data.length, currentBuffer));
                    bufferProgress = 0;
                    currentBuffer = new byte[currentBuffer.length];
                }
                currentBuffer[bufferProgress] = data[i];
                bufferProgress++;
            }

            if (bufferProgress > 0) {
                byte[] rest = new byte[bufferProgress];
                System.arraycopy(currentBuffer, 0, rest, 0, bufferProgress);
                NetUtils.sendToServer(Main.SIMPLE_CHANNEL, new MessagePartialImage(uuid, size - rest.length, data.length, rest));
            }
        }
    }

    public static void sendScreenshotThreaded(UUID uuid, BufferedImage image) {
        new Thread(() -> sendScreenshot(uuid, image), "ProcessImageThread").start();
    }

    public static void sendScreenshotThreaded(UUID uuid, NativeImage image) {
        new Thread(() -> sendScreenshot(uuid, ImageTools.fromNativeImage(image)), "ProcessImageThread").start();
    }

}

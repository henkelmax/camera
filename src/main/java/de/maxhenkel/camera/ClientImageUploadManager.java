package de.maxhenkel.camera;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientImageUploadManager {

    private static Map<UUID, BufferedImage> images = new HashMap<>();

    public static void addImage(UUID uuid, BufferedImage image) {
        images.put(uuid, image);
    }

    public static BufferedImage getAndRemoveImage(UUID uuid) {
        return images.remove(uuid);
    }

}

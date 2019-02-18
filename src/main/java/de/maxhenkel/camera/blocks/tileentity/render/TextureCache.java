package de.maxhenkel.camera.blocks.tileentity.render;

import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.net.MessageRequestImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TextureCache {

    private Map<UUID, CameraTextureObject> clientImageCache;
    private Map<UUID, ResourceLocation> clientResourceCache;
    private Map<UUID, Long> awaitingImages;

    public static TextureCache instance;

    public TextureCache() {
        clientImageCache = new HashMap<>();
        clientResourceCache = new HashMap<>();
        awaitingImages = new HashMap<>();
    }

    public void addImage(UUID uuid, BufferedImage image) {
        if (awaitingImages.containsKey(uuid)) {
            awaitingImages.remove(uuid);
        }

        ResourceLocation resourceLocation = new ResourceLocation(Main.MODID, "texures/camera/" + uuid.toString());
        CameraTextureObject cameraTextureObject = new CameraTextureObject(ImageTools.toNativeImage(image));
        clientImageCache.put(uuid, cameraTextureObject);
        clientResourceCache.put(uuid, resourceLocation);
        Minecraft.getInstance().getRenderManager().textureManager.loadTexture(resourceLocation, cameraTextureObject);
    }

    public ResourceLocation getImage(UUID uuid) {
        CameraTextureObject cameraTextureObject = clientImageCache.get(uuid);

        if (checkImage(uuid, cameraTextureObject)) {
            return null;
        }
        return clientResourceCache.get(uuid);
    }

    private boolean checkImage(UUID uuid, CameraTextureObject cameraTextureObject) {
        if (cameraTextureObject == null) {
            if (awaitingImages.containsKey(uuid)) {
                if (awaitingImages.get(uuid).longValue() + 10_000 > System.currentTimeMillis()) {
                    return true;
                }
            }
            awaitingImages.put(uuid, System.currentTimeMillis());
            Main.SIMPLE_CHANNEL.sendToServer(new MessageRequestImage(uuid));

            return true;
        }
        return false;
    }

    public NativeImage getNativeImage(UUID uuid) {
        CameraTextureObject cameraTextureObject = clientImageCache.get(uuid);

        if (checkImage(uuid, cameraTextureObject)) {
            return null;
        }
        return cameraTextureObject.getTextureData();
    }

    public class CameraTextureObject extends DynamicTexture {

        public CameraTextureObject(NativeImage image) {
            super(image);
        }
    }

    public static TextureCache instance() {
        if (instance == null) {
            instance = new TextureCache();
        }
        return instance;
    }

}

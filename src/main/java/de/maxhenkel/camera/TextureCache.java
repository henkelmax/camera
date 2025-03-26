package de.maxhenkel.camera;

import com.mojang.blaze3d.platform.NativeImage;
import de.maxhenkel.camera.net.MessageRequestImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
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

        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(Main.MODID, "texures/camera/" + uuid.toString());
        CameraTextureObject cameraTextureObject = new CameraTextureObject(resourceLocation::toString, ImageTools.toNativeImage(image));
        clientImageCache.put(uuid, cameraTextureObject);
        clientResourceCache.put(uuid, resourceLocation);
        Minecraft.getInstance().getEntityRenderDispatcher().textureManager.register(resourceLocation, cameraTextureObject);
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
            PacketDistributor.sendToServer(new MessageRequestImage(uuid));

            return true;
        }
        return false;
    }

    public NativeImage getNativeImage(UUID uuid) {
        CameraTextureObject cameraTextureObject = clientImageCache.get(uuid);

        if (checkImage(uuid, cameraTextureObject)) {
            return null;
        }
        return cameraTextureObject.getPixels();
    }

    public static class CameraTextureObject extends DynamicTexture {

        public CameraTextureObject(Supplier<String> stringSupplier, NativeImage image) {
            super(stringSupplier, image);
        }
    }

    public static TextureCache instance() {
        if (instance == null) {
            instance = new TextureCache();
        }
        return instance;
    }

}

package de.maxhenkel.camera;

import de.maxhenkel.camera.net.MessageRequestImage;
import de.maxhenkel.camera.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SideOnly(Side.CLIENT)
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

        ResourceLocation resourceLocation = new ResourceLocation(Main.MODID, "textures/camera/" + uuid.toString());
        CameraTextureObject cameraTextureObject = new CameraTextureObject(resourceLocation, image);
        clientImageCache.put(uuid, cameraTextureObject);
        clientResourceCache.put(uuid, resourceLocation);
        Minecraft.getMinecraft().renderEngine.loadTexture(resourceLocation, cameraTextureObject);
    }

    public ResourceLocation getImage(UUID uuid) {
        CameraTextureObject cameraTextureObject = clientImageCache.get(uuid);

        if (cameraTextureObject == null) {
            if (awaitingImages.containsKey(uuid)) {
                if (awaitingImages.get(uuid).longValue() + 10_000 > System.currentTimeMillis()) {
                    return null;
                }
            }
            awaitingImages.put(uuid, System.currentTimeMillis());
            CommonProxy.simpleNetworkWrapper.sendToServer(new MessageRequestImage(uuid));

            return null;
        }
        return clientResourceCache.get(uuid);
    }

    public BufferedImage getBufferedImage(UUID uuid) {
        CameraTextureObject cameraTextureObject = clientImageCache.get(uuid);

        if (cameraTextureObject == null) {
            if (awaitingImages.containsKey(uuid)) {
                if (awaitingImages.get(uuid).longValue() + 10_000 > System.currentTimeMillis()) {
                    return null;
                }
            }
            awaitingImages.put(uuid, System.currentTimeMillis());
            CommonProxy.simpleNetworkWrapper.sendToServer(new MessageRequestImage(uuid));

            return null;
        }
        return cameraTextureObject.image;
    }

    public class CameraTextureObject extends SimpleTexture {

        private BufferedImage image;

        public CameraTextureObject(ResourceLocation textureResourceLocation, BufferedImage image) {
            super(textureResourceLocation);
            this.image = image;
        }

        @Override
        public void loadTexture(IResourceManager resourceManager) throws IOException {
            TextureUtil.uploadTextureImage(super.getGlTextureId(), image);
        }
    }

    public static TextureCache instance() {
        if (instance == null) {
            instance = new TextureCache();
        }
        return instance;
    }


}

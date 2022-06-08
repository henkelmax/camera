package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.Main;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PacketManager {

    private Map<UUID, byte[]> clientDataMap;

    private Map<UUID, BufferedImage> imageCache;

    private Map<UUID, Long> cooldowns;

    public PacketManager() {
        this.clientDataMap = new HashMap<>();
        this.imageCache = new HashMap<>();
        this.cooldowns = new HashMap<>();
    }

    public void addBytes(ServerPlayer playerMP, UUID imagegID, int offset, int length, byte[] bytes) {
        byte[] data;
        if (!clientDataMap.containsKey(imagegID)) {
            data = new byte[length];
        } else {
            data = clientDataMap.get(imagegID);
        }

        System.arraycopy(bytes, 0, data, offset, bytes.length);

        clientDataMap.put(imagegID, data);

        if (offset + bytes.length >= data.length) {
            try {
                BufferedImage image = completeImage(imagegID);
                if (image == null) {
                    throw new IOException("Image incomplete");
                }
                imageCache.put(imagegID, image);

                new Thread(() -> {
                    try {
                        ImageTools.saveImage(playerMP, imagegID, image);

                        playerMP.getServer().submitAsync(() -> {
                            ItemStack stack = new ItemStack(Main.IMAGE.get());
                            ImageData imageData = ImageData.create(playerMP, imagegID);
                            imageData.addToImage(stack);
                            if (!playerMP.addItem(stack)) {
                                Containers.dropItemStack(playerMP.level, playerMP.getX(), playerMP.getY(), playerMP.getZ(), stack);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, "SaveImageThread").start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BufferedImage getExistingImage(ServerPlayer playerMP, UUID uuid) throws IOException {
        if (imageCache.containsKey(uuid)) {
            return imageCache.get(uuid);
        }
        BufferedImage image = ImageTools.loadImage(playerMP, uuid);
        imageCache.put(uuid, image);
        return image;
    }

    public BufferedImage completeImage(UUID imgUUID) {
        byte[] data = clientDataMap.get(imgUUID);
        if (data == null) {
            return null;
        }

        try {
            BufferedImage image = ImageTools.fromBytes(data);
            clientDataMap.remove(imgUUID);
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean canTakeImage(UUID player) {
        if (cooldowns.containsKey(player)) {
            if (System.currentTimeMillis() - cooldowns.get(player) < Main.SERVER_CONFIG.imageCooldown.get()) {
                return false;
            } else {
                cooldowns.put(player, System.currentTimeMillis());
                return true;
            }
        } else {
            cooldowns.put(player, System.currentTimeMillis());
            return true;
        }
    }

}

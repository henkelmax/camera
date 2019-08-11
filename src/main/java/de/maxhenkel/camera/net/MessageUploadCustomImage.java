package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ClientImageUploadManager;
import de.maxhenkel.camera.ImageProcessor;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.awt.image.BufferedImage;
import java.util.UUID;

public class MessageUploadCustomImage implements Message {

    private UUID uuid;

    public MessageUploadCustomImage() {

    }

    public MessageUploadCustomImage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {

    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        BufferedImage image = ClientImageUploadManager.getAndRemoveImage(uuid);

        if (image == null) {
            return;
        }

        ImageProcessor.sendScreenshodThreaded(uuid, image);
    }

    @Override
    public MessageUploadCustomImage fromBytes(PacketBuffer buf) {
        uuid = buf.readUniqueId();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(uuid);
    }
}

package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ClientImageUploadManager;
import de.maxhenkel.camera.ImageProcessor;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.awt.image.BufferedImage;
import java.util.UUID;

public class MessageUploadCustomImage implements Message<MessageUploadCustomImage> {

    private UUID uuid;

    public MessageUploadCustomImage() {

    }

    public MessageUploadCustomImage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        BufferedImage image = ClientImageUploadManager.getAndRemoveImage(uuid);

        if (image == null) {
            return;
        }

        ImageProcessor.sendScreenshotThreaded(uuid, image);
    }

    @Override
    public MessageUploadCustomImage fromBytes(FriendlyByteBuf buf) {
        uuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
    }

}

package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.TextureCache;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

public class MessageImage implements Message<MessageImage> {

    private UUID uuid;
    private byte[] image;

    public MessageImage() {

    }

    public MessageImage(UUID uuid, byte[] image) throws IOException {
        this.uuid = uuid;
        this.image = image;
        if (image.length > 1_000_000) {
            throw new IOException("Image too large: " + image.length + " bytes (max 1.000.000)");
        }
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        try {
            BufferedImage img = ImageTools.fromBytes(image);
            Minecraft.getInstance().submitAsync(() -> TextureCache.instance().addImage(uuid, img));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MessageImage fromBytes(FriendlyByteBuf buf) {
        uuid = buf.readUUID();
        image = buf.readByteArray();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);

        buf.writeByteArray(image);
    }
}

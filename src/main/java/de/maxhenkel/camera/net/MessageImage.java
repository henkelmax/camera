package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.TextureCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

public class MessageImage implements Message {

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
    public void executeServerSide(NetworkEvent.Context context) {

    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        try {
            BufferedImage img = ImageTools.fromBytes(image);
            Minecraft.getInstance().deferTask(() -> TextureCache.instance().addImage(uuid, img));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MessageImage fromBytes(PacketBuffer buf) {
        uuid = buf.readUniqueId();
        image = buf.readByteArray();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(uuid);

        buf.writeByteArray(image);
    }
}

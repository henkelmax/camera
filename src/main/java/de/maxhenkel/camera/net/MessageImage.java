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

    public MessageImage(UUID uuid, byte[] image) {
        this.uuid = uuid;
        this.image = image;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {

    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        try {
            BufferedImage img = ImageTools.fromBytes(image);
            Minecraft.getInstance().func_213165_a(() -> TextureCache.instance().addImage(uuid, img)); //TODO scheduledTask
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MessageImage fromBytes(PacketBuffer buf) {
        long l1 = buf.readLong();
        long l2 = buf.readLong();
        uuid = new UUID(l1, l2);

        int length = buf.readInt();
        image = new byte[length];
        buf.readBytes(image);
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());

        buf.writeInt(image.length);
        buf.writeBytes(image);
    }
}

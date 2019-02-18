package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import java.util.UUID;

public class MessagePartialImage implements Message {

    private UUID imgUUID;
    private int offset;
    private int length;
    private byte[] bytes;

    public MessagePartialImage() {

    }

    public MessagePartialImage(UUID imgUUID, int offset, int length, byte[] bytes) {
        this.imgUUID = imgUUID;
        this.offset = offset;
        this.length = length;
        this.bytes = bytes;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        Main.PACKET_MANAGER.addBytes(context.getSender(), imgUUID, offset, length, bytes);
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {

    }

    @Override
    public MessagePartialImage fromBytes(PacketBuffer buf) {
        long l1 = buf.readLong();
        long l2 = buf.readLong();
        imgUUID = new UUID(l1, l2);
        offset = buf.readInt();
        length = buf.readInt();

        int length = buf.readInt();
        bytes = new byte[length];
        buf.readBytes(bytes);
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeLong(imgUUID.getMostSignificantBits());
        buf.writeLong(imgUUID.getLeastSignificantBits());
        buf.writeInt(offset);
        buf.writeInt(length);

        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }
}

package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

public class MessagePartialImage implements Message<MessagePartialImage> {

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
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        Main.PACKET_MANAGER.addBytes(context.getSender(), imgUUID, offset, length, bytes);
    }

    @Override
    public MessagePartialImage fromBytes(PacketBuffer buf) {
        imgUUID = buf.readUniqueId();
        offset = buf.readInt();
        length = buf.readInt();
        bytes = buf.readByteArray();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(imgUUID);
        buf.writeInt(offset);
        buf.writeInt(length);

        buf.writeByteArray(bytes);
    }

}
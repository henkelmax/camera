package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.Main;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;
import java.util.UUID;

public class MessageRequestImage implements Message {

    private UUID imgUUID;

    public MessageRequestImage() {

    }

    public MessageRequestImage(UUID imgUUID) {
        this.imgUUID = imgUUID;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        try {
            byte[] data = ImageTools.toBytes(Main.PACKET_MANAGER.getExistingImage(context.getSender(), imgUUID));
            Main.SIMPLE_CHANNEL.reply(new MessageImage(imgUUID, data), context);
        } catch (IOException e) {
            e.printStackTrace();
            Main.SIMPLE_CHANNEL.reply(new MessageImageUnavailable(imgUUID), context);
        }
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {

    }

    @Override
    public MessageRequestImage fromBytes(PacketBuffer buf) {
        imgUUID = buf.readUniqueId();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(imgUUID);
    }
}

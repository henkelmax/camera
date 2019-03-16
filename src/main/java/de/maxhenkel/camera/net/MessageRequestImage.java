package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.proxy.CommonProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.IOException;
import java.util.UUID;

public class MessageRequestImage extends MessageToServer<MessageRequestImage> {

    private UUID imgUUID;

    public MessageRequestImage() {

    }

    public MessageRequestImage(UUID imgUUID) {
        this.imgUUID = imgUUID;
    }

    @Override
    public void execute(EntityPlayerMP player, MessageRequestImage message) {
        try {
            byte[] data = ImageTools.toBytes(CommonProxy.packetManager.getExistingImage(player, message.imgUUID));
            CommonProxy.simpleNetworkWrapper.sendTo(new MessageImage(message.imgUUID, data), player);
        } catch (IOException e) {
            e.printStackTrace();
            CommonProxy.simpleNetworkWrapper.sendTo(new MessageImageUnavailable(message.imgUUID), player);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long l1 = buf.readLong();
        long l2 = buf.readLong();
        imgUUID = new UUID(l1, l2);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(imgUUID.getMostSignificantBits());
        buf.writeLong(imgUUID.getLeastSignificantBits());
    }
}

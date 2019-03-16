package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.TextureCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

public class MessageImage implements IMessage, IMessageHandler<MessageImage, IMessage> {

    private UUID uuid;
    private byte[] image;

    public MessageImage() {

    }

    public MessageImage(UUID uuid, byte[] image) {
        this.uuid = uuid;
        this.image = image;
    }

    @Override
    public IMessage onMessage(MessageImage message, MessageContext ctx) {
        try {
            BufferedImage img = ImageTools.fromBytes(message.image);
            Minecraft.getMinecraft().addScheduledTask(() -> TextureCache.instance().addImage(message.uuid, img));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long l1 = buf.readLong();
        long l2 = buf.readLong();
        uuid = new UUID(l1, l2);

        int length = buf.readInt();
        image = new byte[length];
        buf.readBytes(image);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());

        buf.writeInt(image.length);
        buf.writeBytes(image);
    }


}

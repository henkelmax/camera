package de.maxhenkel.camera.net;

import de.maxhenkel.camera.proxy.CommonProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class MessagePartialImage extends MessageToServer<MessagePartialImage> {

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
    public void execute(EntityPlayerMP player, MessagePartialImage message) {

    }

    @Override
    public IMessage onMessage(MessagePartialImage message, MessageContext ctx) {
        if(ctx.side.equals(Side.SERVER)){
            EntityPlayerMP player=ctx.getServerHandler().player;
            CommonProxy.manager.addBytes(player, message.imgUUID, message.offset, message.length, message.bytes);
        }
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long l1=buf.readLong();
        long l2=buf.readLong();
        imgUUID=new UUID(l1, l2);
        offset=buf.readInt();
        length=buf.readInt();

        int length = buf.readInt();
        bytes = new byte[length];
        buf.readBytes(bytes);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(imgUUID.getMostSignificantBits());
        buf.writeLong(imgUUID.getLeastSignificantBits());
        buf.writeInt(offset);
        buf.writeInt(length);

        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

}

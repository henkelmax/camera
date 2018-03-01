package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.blocks.tileentity.render.TextureCache;
import de.maxhenkel.camera.proxy.CommonProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

public class MessageImageUnavailable extends MessageToClient<MessageImageUnavailable>{

    private UUID imgUUID;

    public MessageImageUnavailable() {

    }

    public MessageImageUnavailable(UUID imgUUID) {
        this.imgUUID = imgUUID;
    }


    @Override
    public void execute(EntityPlayerSP player, MessageImageUnavailable message) {
        TextureCache.instance().addImage(message.imgUUID, new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long l1=buf.readLong();
        long l2=buf.readLong();
        imgUUID=new UUID(l1, l2);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(imgUUID.getMostSignificantBits());
        buf.writeLong(imgUUID.getLeastSignificantBits());
    }


}

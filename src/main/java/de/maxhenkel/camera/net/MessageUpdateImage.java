package de.maxhenkel.camera.net;

import de.maxhenkel.camera.blocks.tileentity.TileentityImage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import java.util.UUID;

public class MessageUpdateImage extends MessageToClient<MessageUpdateImage> {

    private int x, y, z;
    private UUID uuid;

    public MessageUpdateImage() {

    }

    public MessageUpdateImage(int x, int y, int z, UUID uuid) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.uuid = uuid;
    }

    @Override
    public void execute(EntityPlayerSP player, MessageUpdateImage message) {
        TileEntity te=player.world.getTileEntity(new BlockPos(message.x, message.y, message.z));

        if(te instanceof TileentityImage){
            ((TileentityImage) te).setUUIDOnClient(message.uuid);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x=buf.readInt();
        y=buf.readInt();
        z=buf.readInt();
        long l1=buf.readLong();
        long l2=buf.readLong();
        uuid=new UUID(l1, l2);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }
}

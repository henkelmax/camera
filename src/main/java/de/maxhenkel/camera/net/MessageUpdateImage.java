package de.maxhenkel.camera.net;

import de.maxhenkel.camera.blocks.tileentity.TileEntityImage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import java.util.UUID;

public class MessageUpdateImage implements Message {

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
    public void executeServerSide(NetworkEvent.Context context) {

    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        TileEntity te = Minecraft.getInstance().player.world.getTileEntity(new BlockPos(x, y, z));

        if (te instanceof TileEntityImage) {
            ((TileEntityImage) te).setUUIDOnClient(uuid);
        }
    }

    @Override
    public MessageUpdateImage fromBytes(PacketBuffer buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        long l1 = buf.readLong();
        long l2 = buf.readLong();
        uuid = new UUID(l1, l2);
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }
}

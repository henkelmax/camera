package de.maxhenkel.camera.net;

import de.maxhenkel.camera.entities.EntityImage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.UUID;

public class MessageResizeFrame extends MessageToServer<MessageResizeFrame> {

    private UUID uuid;
    private Direction direction;
    private boolean larger;

    public MessageResizeFrame() {

    }

    public MessageResizeFrame(UUID uuid, Direction direction, boolean larger) {
        this.uuid = uuid;
        this.direction = direction;
        this.larger = larger;
    }

    @Override
    public void execute(EntityPlayerMP player, MessageResizeFrame message) {
        player.world.getEntities(EntityImage.class, entityImage -> entityImage.getUniqueID().equals(message.uuid)).forEach(image -> image.resize(message.direction, message.larger));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long most = buf.readLong();
        long least = buf.readLong();
        uuid = new UUID(most, least);
        direction = Direction.valueOf(ByteBufUtils.readUTF8String(buf));
        larger = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
        ByteBufUtils.writeUTF8String(buf, direction.name());
        buf.writeBoolean(larger);
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT;
    }
}

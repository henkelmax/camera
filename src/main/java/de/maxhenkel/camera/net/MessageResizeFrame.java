package de.maxhenkel.camera.net;

import de.maxhenkel.camera.entities.ImageEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

public class MessageResizeFrame implements Message {

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
    public void executeServerSide(NetworkEvent.Context context) {
        if (context.getSender().world instanceof ServerWorld && context.getSender().abilities.allowEdit) {
            ServerWorld world = (ServerWorld) context.getSender().world;
            Entity entity = world.getEntityByUuid(uuid);
            if (entity instanceof ImageEntity) {
                ImageEntity image = (ImageEntity) entity;
                image.resize(direction, larger);
            }
        }
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {

    }

    @Override
    public MessageResizeFrame fromBytes(PacketBuffer buf) {
        long most = buf.readLong();
        long least = buf.readLong();
        uuid = new UUID(most, least);
        direction = Direction.valueOf(buf.readString(16));
        larger = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
        buf.writeString(direction.name());
        buf.writeBoolean(larger);
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT;
    }
}

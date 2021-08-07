package de.maxhenkel.camera.net;

import de.maxhenkel.camera.entities.ImageEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.UUID;

public class MessageResizeFrame implements Message<MessageResizeFrame> {

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
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        if (context.getSender().level instanceof ServerLevel && context.getSender().getAbilities().mayBuild) {
            ServerLevel world = (ServerLevel) context.getSender().level;
            Entity entity = world.getEntity(uuid);
            if (entity instanceof ImageEntity) {
                ImageEntity image = (ImageEntity) entity;
                image.resize(direction, larger);
            }
        }
    }

    @Override
    public MessageResizeFrame fromBytes(FriendlyByteBuf buf) {
        uuid = buf.readUUID();
        direction = Direction.values()[buf.readInt()];
        larger = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeInt(direction.ordinal());
        buf.writeBoolean(larger);
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT;
    }

}

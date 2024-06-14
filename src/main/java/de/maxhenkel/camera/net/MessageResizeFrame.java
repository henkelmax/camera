package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.ImageEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class MessageResizeFrame implements Message<MessageResizeFrame> {

    public static final CustomPacketPayload.Type<MessageResizeFrame> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "resize_frame"));

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
    public PacketFlow getExecutingSide() {
        return PacketFlow.SERVERBOUND;
    }

    @Override
    public void executeServerSide(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) {
            return;
        }
        if (sender.level() instanceof ServerLevel serverLevel && sender.getAbilities().mayBuild) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof ImageEntity) {
                ImageEntity image = (ImageEntity) entity;
                image.resize(direction, larger);
            }
        }
    }

    @Override
    public MessageResizeFrame fromBytes(RegistryFriendlyByteBuf buf) {
        uuid = buf.readUUID();
        direction = Direction.values()[buf.readInt()];
        larger = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeInt(direction.ordinal());
        buf.writeBoolean(larger);
    }

    @Override
    public Type<MessageResizeFrame> type() {
        return TYPE;
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT;
    }

}

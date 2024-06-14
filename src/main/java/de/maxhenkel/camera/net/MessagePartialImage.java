package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class MessagePartialImage implements Message<MessagePartialImage> {

    public static final CustomPacketPayload.Type<MessagePartialImage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "partial_image"));

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
    public PacketFlow getExecutingSide() {
        return PacketFlow.SERVERBOUND;
    }

    @Override
    public void executeServerSide(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) {
            return;
        }
        Main.PACKET_MANAGER.addBytes(sender, imgUUID, offset, length, bytes);
    }

    @Override
    public MessagePartialImage fromBytes(RegistryFriendlyByteBuf buf) {
        imgUUID = buf.readUUID();
        offset = buf.readInt();
        length = buf.readInt();
        bytes = buf.readByteArray();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(imgUUID);
        buf.writeInt(offset);
        buf.writeInt(length);

        buf.writeByteArray(bytes);
    }

    @Override
    public Type<MessagePartialImage> type() {
        return TYPE;
    }

}
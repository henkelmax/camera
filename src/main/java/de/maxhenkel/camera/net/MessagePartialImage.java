package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.UUID;

public class MessagePartialImage implements Message<MessagePartialImage> {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "partial_image");

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
    public void executeServerSide(PlayPayloadContext context) {
        if (!(context.player().orElse(null) instanceof ServerPlayer sender)) {
            return;
        }
        Main.PACKET_MANAGER.addBytes(sender, imgUUID, offset, length, bytes);
    }

    @Override
    public MessagePartialImage fromBytes(FriendlyByteBuf buf) {
        imgUUID = buf.readUUID();
        offset = buf.readInt();
        length = buf.readInt();
        bytes = buf.readByteArray();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(imgUUID);
        buf.writeInt(offset);
        buf.writeInt(length);

        buf.writeByteArray(bytes);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

}
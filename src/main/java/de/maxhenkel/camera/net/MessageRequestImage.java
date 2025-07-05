package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.CameraMod;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.util.UUID;

public class MessageRequestImage implements Message<MessageRequestImage> {

    public static final CustomPacketPayload.Type<MessageRequestImage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CameraMod.MODID, "request_image"));

    private UUID imgUUID;

    public MessageRequestImage() {

    }

    public MessageRequestImage(UUID imgUUID) {
        this.imgUUID = imgUUID;
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
        try {
            byte[] data = ImageTools.toBytes(CameraMod.PACKET_MANAGER.getExistingImage(sender, imgUUID));
            context.reply(new MessageImage(imgUUID, data));
        } catch (IOException e) {
            //TODO Properly log an error
            e.printStackTrace();
            context.reply(new MessageImageUnavailable(imgUUID));
        }
    }

    @Override
    public MessageRequestImage fromBytes(RegistryFriendlyByteBuf buf) {
        imgUUID = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(imgUUID);
    }

    @Override
    public Type<MessageRequestImage> type() {
        return TYPE;
    }

}

package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ClientImageUploadManager;
import de.maxhenkel.camera.ImageProcessor;
import de.maxhenkel.camera.CameraMod;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.awt.image.BufferedImage;
import java.util.UUID;

public class MessageUploadCustomImage implements Message<MessageUploadCustomImage> {

    public static final CustomPacketPayload.Type<MessageUploadCustomImage> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CameraMod.MODID, "upload"));

    private UUID uuid;

    public MessageUploadCustomImage() {

    }

    public MessageUploadCustomImage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.CLIENTBOUND;
    }

    @Override
    public void executeClientSide(IPayloadContext context) {
        BufferedImage image = ClientImageUploadManager.getAndRemoveImage(uuid);

        if (image == null) {
            return;
        }

        ImageProcessor.sendScreenshotThreaded(uuid, image);
    }

    @Override
    public MessageUploadCustomImage fromBytes(RegistryFriendlyByteBuf buf) {
        uuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(uuid);
    }

    @Override
    public Type<MessageUploadCustomImage> type() {
        return TYPE;
    }

}

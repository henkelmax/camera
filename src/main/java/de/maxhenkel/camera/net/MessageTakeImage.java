package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageTaker;
import de.maxhenkel.camera.CameraMod;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class MessageTakeImage implements Message<MessageTakeImage> {

    public static final CustomPacketPayload.Type<MessageTakeImage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CameraMod.MODID, "take_image"));

    private UUID uuid;

    public MessageTakeImage() {

    }

    public MessageTakeImage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.CLIENTBOUND;
    }

    @Override
    public void executeClientSide(IPayloadContext context) {
        ImageTaker.takeScreenshot(uuid);
    }

    @Override
    public MessageTakeImage fromBytes(RegistryFriendlyByteBuf buf) {
        uuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(uuid);
    }

    @Override
    public Type<MessageTakeImage> type() {
        return TYPE;
    }

}

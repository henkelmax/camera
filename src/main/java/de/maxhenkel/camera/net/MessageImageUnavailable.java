package de.maxhenkel.camera.net;

import de.maxhenkel.camera.CameraMod;
import de.maxhenkel.camera.TextureCache;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.awt.image.BufferedImage;
import java.util.UUID;

public class MessageImageUnavailable implements Message<MessageImageUnavailable> {

    public static final CustomPacketPayload.Type<MessageImageUnavailable> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CameraMod.MODID, "image_unavailable"));

    private UUID imgUUID;

    public MessageImageUnavailable() {

    }

    public MessageImageUnavailable(UUID imgUUID) {
        this.imgUUID = imgUUID;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.CLIENTBOUND;
    }

    @Override
    public void executeClientSide(IPayloadContext context) {
        TextureCache.instance().addImage(imgUUID, new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
    }

    @Override
    public MessageImageUnavailable fromBytes(RegistryFriendlyByteBuf buf) {
        imgUUID = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(imgUUID);
    }

    @Override
    public Type<MessageImageUnavailable> type() {
        return TYPE;
    }

}

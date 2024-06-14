package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.TextureCache;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

public class MessageImage implements Message<MessageImage> {

    public static final CustomPacketPayload.Type<MessageImage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "image"));

    private UUID uuid;
    private byte[] image;

    public MessageImage() {

    }

    public MessageImage(UUID uuid, byte[] image) throws IOException {
        this.uuid = uuid;
        this.image = image;
        if (image.length > 1_000_000) {
            throw new IOException("Image too large: " + image.length + " bytes (max 1.000.000)");
        }
    }

    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.CLIENTBOUND;
    }

    @Override
    public void executeClientSide(IPayloadContext context) {
        try {
            BufferedImage img = ImageTools.fromBytes(image);
            Minecraft.getInstance().submitAsync(() -> TextureCache.instance().addImage(uuid, img));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MessageImage fromBytes(RegistryFriendlyByteBuf buf) {
        uuid = buf.readUUID();
        image = buf.readByteArray();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(uuid);

        buf.writeByteArray(image);
    }

    @Override
    public Type<MessageImage> type() {
        return TYPE;
    }

}

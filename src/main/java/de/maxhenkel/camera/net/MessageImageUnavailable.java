package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.TextureCache;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.awt.image.BufferedImage;
import java.util.UUID;

public class MessageImageUnavailable implements Message<MessageImageUnavailable> {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "image_unavailable");

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
    public void executeClientSide(PlayPayloadContext context) {
        TextureCache.instance().addImage(imgUUID, new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
    }

    @Override
    public MessageImageUnavailable fromBytes(FriendlyByteBuf buf) {
        imgUUID = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(imgUUID);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}

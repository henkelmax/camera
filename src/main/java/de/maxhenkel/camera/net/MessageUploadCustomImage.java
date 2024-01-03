package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ClientImageUploadManager;
import de.maxhenkel.camera.ImageProcessor;
import de.maxhenkel.camera.Main;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.awt.image.BufferedImage;
import java.util.UUID;

public class MessageUploadCustomImage implements Message<MessageUploadCustomImage> {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "upload");

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
    public void executeClientSide(PlayPayloadContext context) {
        BufferedImage image = ClientImageUploadManager.getAndRemoveImage(uuid);

        if (image == null) {
            return;
        }

        ImageProcessor.sendScreenshotThreaded(uuid, image);
    }

    @Override
    public MessageUploadCustomImage fromBytes(FriendlyByteBuf buf) {
        uuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

}

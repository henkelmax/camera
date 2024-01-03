package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.Main;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.io.IOException;
import java.util.UUID;

public class MessageRequestImage implements Message<MessageRequestImage> {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "request_image");

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
    public void executeServerSide(PlayPayloadContext context) {
        if (!(context.player().orElse(null) instanceof ServerPlayer sender)) {
            return;
        }
        try {
            byte[] data = ImageTools.toBytes(Main.PACKET_MANAGER.getExistingImage(sender, imgUUID));
            context.replyHandler().send(new MessageImage(imgUUID, data));
        } catch (IOException e) {
            //TODO Properly log an error
            e.printStackTrace();
            context.replyHandler().send(new MessageImageUnavailable(imgUUID));
        }
    }

    @Override
    public MessageRequestImage fromBytes(FriendlyByteBuf buf) {
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

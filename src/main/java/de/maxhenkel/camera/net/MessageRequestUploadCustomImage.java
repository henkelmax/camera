package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.items.CameraItem;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.UUID;

public class MessageRequestUploadCustomImage implements Message<MessageRequestUploadCustomImage> {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "request_upload");

    private UUID uuid;

    public MessageRequestUploadCustomImage() {

    }

    public MessageRequestUploadCustomImage(UUID uuid) {
        this.uuid = uuid;
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
        if (Main.PACKET_MANAGER.canTakeImage(sender.getUUID())) {
            if (CameraItem.consumePaper(sender)) {
                context.replyHandler().send(new MessageUploadCustomImage(uuid));
            } else {
                sender.displayClientMessage(Component.translatable("message.no_consumable"), true);
            }
        } else {
            sender.displayClientMessage(Component.translatable("message.image_cooldown"), true);
        }
    }

    @Override
    public MessageRequestUploadCustomImage fromBytes(FriendlyByteBuf buf) {
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

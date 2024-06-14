package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.items.CameraItem;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class MessageRequestUploadCustomImage implements Message<MessageRequestUploadCustomImage> {

    public static final CustomPacketPayload.Type<MessageRequestUploadCustomImage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "request_upload"));

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
    public void executeServerSide(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) {
            return;
        }
        if (Main.PACKET_MANAGER.canTakeImage(sender.getUUID())) {
            if (CameraItem.consumePaper(sender)) {
                context.reply(new MessageUploadCustomImage(uuid));
            } else {
                sender.displayClientMessage(Component.translatable("message.no_consumable"), true);
            }
        } else {
            sender.displayClientMessage(Component.translatable("message.image_cooldown"), true);
        }
    }

    @Override
    public MessageRequestUploadCustomImage fromBytes(RegistryFriendlyByteBuf buf) {
        uuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(uuid);
    }

    @Override
    public Type<MessageRequestUploadCustomImage> type() {
        return TYPE;
    }

}

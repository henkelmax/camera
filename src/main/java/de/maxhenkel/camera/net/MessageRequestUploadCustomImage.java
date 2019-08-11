package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Config;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.items.CameraItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

public class MessageRequestUploadCustomImage implements Message {

    private UUID uuid;

    public MessageRequestUploadCustomImage() {

    }

    public MessageRequestUploadCustomImage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (Main.PACKET_MANAGER.canTakeImage(player.getUniqueID())) {
            if (CameraItem.consumePaper(player)) {
                Main.SIMPLE_CHANNEL.reply(new MessageUploadCustomImage(uuid), context);
            } else {
                player.sendStatusMessage(new TranslationTextComponent("message.no_consumable", Config.getConsumingStack().getDisplayName(), Config.getConsumingStack().getCount()), true);
            }
        } else {
            player.sendStatusMessage(new TranslationTextComponent("message.image_cooldown"), true);
        }
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {

    }

    @Override
    public MessageRequestUploadCustomImage fromBytes(PacketBuffer buf) {
        uuid = buf.readUniqueId();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(uuid);
    }
}

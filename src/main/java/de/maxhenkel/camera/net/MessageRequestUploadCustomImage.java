package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.items.CameraItem;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

public class MessageRequestUploadCustomImage implements Message<MessageRequestUploadCustomImage> {

    private UUID uuid;

    public MessageRequestUploadCustomImage() {

    }

    public MessageRequestUploadCustomImage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (Main.PACKET_MANAGER.canTakeImage(player.getUniqueID())) {
            if (CameraItem.consumePaper(player)) {
                Main.SIMPLE_CHANNEL.reply(new MessageUploadCustomImage(uuid), context);
            } else {
                player.sendStatusMessage(new TranslationTextComponent("message.no_consumable", new TranslationTextComponent(Main.SERVER_CONFIG.cameraConsumeItem.getTranslationKey()), Main.SERVER_CONFIG.cameraConsumeItemAmount.get()), true);
            }
        } else {
            player.sendStatusMessage(new TranslationTextComponent("message.image_cooldown"), true);
        }
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

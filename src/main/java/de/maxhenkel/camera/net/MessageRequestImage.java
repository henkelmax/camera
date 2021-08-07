package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.Main;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.io.IOException;
import java.util.UUID;

public class MessageRequestImage implements Message<MessageRequestImage> {

    private UUID imgUUID;

    public MessageRequestImage() {

    }

    public MessageRequestImage(UUID imgUUID) {
        this.imgUUID = imgUUID;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        try {
            byte[] data = ImageTools.toBytes(Main.PACKET_MANAGER.getExistingImage(context.getSender(), imgUUID));
            Main.SIMPLE_CHANNEL.reply(new MessageImage(imgUUID, data), context);
        } catch (IOException e) {
            e.printStackTrace();
            Main.SIMPLE_CHANNEL.reply(new MessageImageUnavailable(imgUUID), context);
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

}

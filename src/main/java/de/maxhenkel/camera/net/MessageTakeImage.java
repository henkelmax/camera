package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageTaker;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.UUID;

public class MessageTakeImage implements Message<MessageTakeImage> {

    private UUID uuid;

    public MessageTakeImage() {

    }

    public MessageTakeImage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    public void executeClientSide(CustomPayloadEvent.Context context) {
        ImageTaker.takeScreenshot(uuid);
    }

    @Override
    public MessageTakeImage fromBytes(FriendlyByteBuf buf) {
        uuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
    }

}

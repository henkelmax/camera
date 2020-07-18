package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageTaker;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

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
    public void executeClientSide(NetworkEvent.Context context) {
        ImageTaker.takeScreenshot(uuid);
    }

    @Override
    public MessageTakeImage fromBytes(PacketBuffer buf) {
        uuid = buf.readUniqueId();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(uuid);
    }

}

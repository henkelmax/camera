package de.maxhenkel.camera.net;

import de.maxhenkel.camera.TextureCache;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

import java.awt.image.BufferedImage;
import java.util.UUID;

public class MessageImageUnavailable implements Message<MessageImageUnavailable> {

    private UUID imgUUID;

    public MessageImageUnavailable() {

    }

    public MessageImageUnavailable(UUID imgUUID) {
        this.imgUUID = imgUUID;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        TextureCache.instance().addImage(imgUUID, new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
    }

    @Override
    public MessageImageUnavailable fromBytes(PacketBuffer buf) {
        imgUUID = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUUID(imgUUID);
    }
}

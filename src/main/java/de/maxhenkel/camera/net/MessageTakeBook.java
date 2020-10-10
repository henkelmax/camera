package de.maxhenkel.camera.net;

import de.maxhenkel.camera.gui.AlbumContainer;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageTakeBook implements Message<MessageTakeBook> {

    public MessageTakeBook() {

    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        if (context.getSender().openContainer instanceof AlbumContainer) {
            AlbumContainer container = (AlbumContainer) context.getSender().openContainer;
            container.takeBook(context.getSender());
        }
    }

    @Override
    public MessageTakeBook fromBytes(PacketBuffer buf) {
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
    }

}

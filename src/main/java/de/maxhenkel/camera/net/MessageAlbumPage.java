package de.maxhenkel.camera.net;

import de.maxhenkel.camera.gui.AlbumContainer;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageAlbumPage implements Message<MessageAlbumPage> {

    private int page;

    public MessageAlbumPage() {

    }

    public MessageAlbumPage(int page) {
        this.page = page;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        if (context.getSender().openContainer instanceof AlbumContainer) {
            AlbumContainer container = (AlbumContainer) context.getSender().openContainer;
            container.setPage(page);
        }
    }

    @Override
    public MessageAlbumPage fromBytes(PacketBuffer buf) {
        page = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(page);
    }

}

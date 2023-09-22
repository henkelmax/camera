package de.maxhenkel.camera.net;

import de.maxhenkel.camera.gui.AlbumContainer;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;

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
    public void executeServerSide(CustomPayloadEvent.Context context) {
        if (context.getSender().containerMenu instanceof AlbumContainer) {
            AlbumContainer container = (AlbumContainer) context.getSender().containerMenu;
            container.setPage(page);
        }
    }

    @Override
    public MessageAlbumPage fromBytes(FriendlyByteBuf buf) {
        page = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(page);
    }

}

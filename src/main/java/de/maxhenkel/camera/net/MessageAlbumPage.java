package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.AlbumContainer;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class MessageAlbumPage implements Message<MessageAlbumPage> {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "album_page");

    private int page;

    public MessageAlbumPage() {

    }

    public MessageAlbumPage(int page) {
        this.page = page;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.SERVERBOUND;
    }

    @Override
    public void executeServerSide(PlayPayloadContext context) {
        if (!(context.player().orElse(null) instanceof ServerPlayer sender)) {
            return;
        }
        if (sender.containerMenu instanceof AlbumContainer container) {
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

    @Override
    public ResourceLocation id() {
        return ID;
    }

}

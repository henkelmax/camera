package de.maxhenkel.camera.net;

import de.maxhenkel.camera.CameraMod;
import de.maxhenkel.camera.gui.AlbumContainer;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class MessageAlbumPage implements Message<MessageAlbumPage> {

    public static final CustomPacketPayload.Type<MessageAlbumPage> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CameraMod.MODID, "album_page"));

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
    public void executeServerSide(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) {
            return;
        }
        if (sender.containerMenu instanceof AlbumContainer container) {
            container.setPage(page);
        }
    }

    @Override
    public MessageAlbumPage fromBytes(RegistryFriendlyByteBuf buf) {
        page = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeInt(page);
    }

    @Override
    public Type<MessageAlbumPage> type() {
        return TYPE;
    }

}

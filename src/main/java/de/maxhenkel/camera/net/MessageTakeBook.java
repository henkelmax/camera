package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.AlbumContainer;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class MessageTakeBook implements Message<MessageTakeBook> {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "take_book");

    public MessageTakeBook() {

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
            container.takeBook(sender);
        }
    }

    @Override
    public MessageTakeBook fromBytes(FriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

}

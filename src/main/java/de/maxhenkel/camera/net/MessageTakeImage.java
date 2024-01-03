package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageTaker;
import de.maxhenkel.camera.Main;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.UUID;

public class MessageTakeImage implements Message<MessageTakeImage> {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "take_image");

    private UUID uuid;

    public MessageTakeImage() {

    }

    public MessageTakeImage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.CLIENTBOUND;
    }

    @Override
    public void executeClientSide(PlayPayloadContext context) {
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

    @Override
    public ResourceLocation id() {
        return ID;
    }

}

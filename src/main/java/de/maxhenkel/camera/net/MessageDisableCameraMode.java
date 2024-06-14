package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class MessageDisableCameraMode implements Message<MessageDisableCameraMode> {

    public static final CustomPacketPayload.Type<MessageDisableCameraMode> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "disable_camera_mode"));

    public MessageDisableCameraMode() {

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
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = sender.getItemInHand(hand);
            if (stack.getItem().equals(Main.CAMERA.get())) {
                Main.CAMERA.get().setActive(stack, false);
            }
        }
    }

    @Override
    public MessageDisableCameraMode fromBytes(RegistryFriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {

    }

    @Override
    public Type<MessageDisableCameraMode> type() {
        return TYPE;
    }

}

package de.maxhenkel.camera.net;

import de.maxhenkel.camera.CameraMod;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class MessageSetShader implements Message<MessageSetShader> {

    public static final CustomPacketPayload.Type<MessageSetShader> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CameraMod.MODID, "set_shader"));

    private String shader;

    public MessageSetShader() {

    }

    public MessageSetShader(String shader) {
        this.shader = shader;
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
            if (stack.getItem().equals(CameraMod.CAMERA.get())) {
                stack.set(CameraMod.SHADER_DATA_COMPONENT, shader);
            }
        }
    }

    @Override
    public MessageSetShader fromBytes(RegistryFriendlyByteBuf buf) {
        shader = buf.readUtf(128);
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(shader);
    }

    @Override
    public Type<MessageSetShader> type() {
        return TYPE;
    }

}

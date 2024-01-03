package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class MessageSetShader implements Message<MessageSetShader> {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "set_shader");

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
    public void executeServerSide(PlayPayloadContext context) {
        if (!(context.player().orElse(null) instanceof ServerPlayer sender)) {
            return;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = sender.getItemInHand(hand);
            if (stack.getItem().equals(Main.CAMERA.get())) {
                Main.CAMERA.get().setShader(stack, shader);
            }
        }
    }

    @Override
    public MessageSetShader fromBytes(FriendlyByteBuf buf) {
        shader = buf.readUtf(128);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(shader);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

}

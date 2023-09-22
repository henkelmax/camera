package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class MessageSetShader implements Message<MessageSetShader> {

    private String shader;

    public MessageSetShader() {

    }

    public MessageSetShader(String shader) {
        this.shader = shader;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(CustomPayloadEvent.Context context) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = context.getSender().getItemInHand(hand);
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

}

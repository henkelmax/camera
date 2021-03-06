package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

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
    public void executeServerSide(NetworkEvent.Context context) {
        for (Hand hand : Hand.values()) {
            ItemStack stack = context.getSender().getItemInHand(hand);
            if (stack.getItem().equals(Main.CAMERA)) {
                Main.CAMERA.setShader(stack, shader);
            }
        }
    }

    @Override
    public MessageSetShader fromBytes(PacketBuffer buf) {
        shader = buf.readUtf(128);
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUtf(shader);
    }

}

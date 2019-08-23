package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageSetShader implements Message {

    private String shader;

    public MessageSetShader() {

    }

    public MessageSetShader(String shader) {
        this.shader = shader;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ItemStack stack = context.getSender().getHeldItemMainhand();
        if (stack.getItem().equals(Main.CAMERA)) {
            Main.CAMERA.setShader(stack, shader);
        }
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {

    }

    @Override
    public MessageSetShader fromBytes(PacketBuffer buf) {
        shader = buf.readString(128);
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeString(shader);
    }
}

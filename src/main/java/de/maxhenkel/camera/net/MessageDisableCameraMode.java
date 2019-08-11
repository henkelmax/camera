package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageDisableCameraMode implements Message {

    public MessageDisableCameraMode() {

    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ItemStack stack = context.getSender().getHeldItemMainhand();
        if (stack.getItem().equals(Main.CAMERA)) {
            Main.CAMERA.setActive(stack, false);
        }
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {

    }

    @Override
    public MessageDisableCameraMode fromBytes(PacketBuffer buf) {
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {

    }
}

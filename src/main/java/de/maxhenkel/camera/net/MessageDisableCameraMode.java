package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageDisableCameraMode implements Message<MessageDisableCameraMode> {

    public MessageDisableCameraMode() {

    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ItemStack stack = context.getSender().getMainHandItem();
        if (stack.getItem().equals(Main.CAMERA)) {
            Main.CAMERA.setActive(stack, false);
        }
    }

    @Override
    public MessageDisableCameraMode fromBytes(PacketBuffer buf) {
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {

    }
}

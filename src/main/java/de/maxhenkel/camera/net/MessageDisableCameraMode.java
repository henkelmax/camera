package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ModItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class MessageDisableCameraMode extends MessageToServer<MessageDisableCameraMode> {

    public MessageDisableCameraMode() {

    }

    @Override
    public void execute(EntityPlayerMP player, MessageDisableCameraMode message) {
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.getItem().equals(ModItems.CAMERA)) {
            ModItems.CAMERA.setActive(stack, false);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }
}

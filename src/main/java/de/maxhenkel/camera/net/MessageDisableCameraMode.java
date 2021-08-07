package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Main;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageDisableCameraMode implements Message<MessageDisableCameraMode> {

    public MessageDisableCameraMode() {

    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = context.getSender().getItemInHand(hand);
            if (stack.getItem().equals(Main.CAMERA)) {
                Main.CAMERA.setActive(stack, false);
            }
        }

    }

    @Override
    public MessageDisableCameraMode fromBytes(FriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }
}

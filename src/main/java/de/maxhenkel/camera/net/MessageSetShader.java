package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ModItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class MessageSetShader extends MessageToServer<MessageSetShader> {

    private String shader;

    public MessageSetShader() {

    }

    @Override
    public void execute(EntityPlayerMP player, MessageSetShader message) {
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.getItem().equals(ModItems.CAMERA)) {
            ModItems.CAMERA.setShader(stack, message.shader);
        }
    }

    public MessageSetShader(String shader) {
        this.shader = shader;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        shader = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, shader);
    }
}

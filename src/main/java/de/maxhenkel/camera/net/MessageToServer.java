package de.maxhenkel.camera.net;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public abstract class MessageToServer<T extends IMessage> implements IMessage, IMessageHandler<T, IMessage> {

    @Override
    public IMessage onMessage(T message, MessageContext ctx) {
        if (ctx.side.equals(Side.SERVER)) {
            EntityPlayerMP player = ctx.getServerHandler().player;

            player.getServer().addScheduledTask(() -> execute(player, message));
        }
        return null;
    }

    /**
     * This will be executed in a scheduled task
     *
     * @param player
     */
    public abstract void execute(EntityPlayerMP player, T message);

}

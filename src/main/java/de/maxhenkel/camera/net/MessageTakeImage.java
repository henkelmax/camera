package de.maxhenkel.camera.net;

import de.maxhenkel.camera.ImageTaker;
import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.items.ItemCamera;
import de.maxhenkel.camera.proxy.CommonProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.ScreenShotHelper;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

public class MessageTakeImage extends MessageToClient<MessageTakeImage>{

    private UUID uuid;

    public MessageTakeImage(){

    }

    public MessageTakeImage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void execute(EntityPlayerSP player, MessageTakeImage message) {
        try {
            ImageTaker.takeScreenhot(message.uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long l1=buf.readLong();
        long l2=buf.readLong();
        uuid=new UUID(l1, l2);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }


}

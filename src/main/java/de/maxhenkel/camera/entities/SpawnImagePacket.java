package de.maxhenkel.camera.entities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpawnImagePacket extends SSpawnObjectPacket {

    public SpawnImagePacket(ImageEntity entity) {
        super(entity);
    }

    public SpawnImagePacket(){

    }

    @Override
    public void processPacket(IClientPlayNetHandler handler) {
        processPacketClient(handler);
    }

    @OnlyIn(Dist.CLIENT)
    public void processPacketClient(IClientPlayNetHandler handler) {
        Minecraft mc = Minecraft.getInstance();
        PacketThreadUtil.func_218797_a(this, handler, mc);
        double x = getX();
        double y = getY();
        double z = getZ();
        ImageEntity entity = new ImageEntity(mc.world, x, y, z);

        int i = getEntityID();
        entity.func_213312_b(x, y, z);
        entity.rotationPitch = (float) (getPitch() * 360) / 256.0F;
        entity.rotationYaw = (float) (getYaw() * 360) / 256.0F;
        entity.setEntityId(i);
        entity.setUniqueId(getUniqueId());
        mc.world.func_217411_a(i, entity);
    }
}

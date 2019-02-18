package de.maxhenkel.camera.blocks.tileentity;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.items.ItemImage;
import de.maxhenkel.camera.net.MessageUpdateImage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.network.NetworkDirection;
import java.util.UUID;

public class TileEntityImage extends TileEntity {

    private UUID uuid;
    private ItemStack item;

    public TileEntityImage() {
        super(Main.IMAGE_TILE_ENTITY_TYPE);
        uuid = new UUID(0, 0);
        item = null;
    }

    public void setUUIDOnClient(UUID uuid) {
        this.uuid = uuid;
    }

    public void setImage(ItemStack image) {
        if (!image.getItem().equals(Main.IMAGE)) {
            return;
        }
        this.uuid = ItemImage.getUUID(image);
        this.item = image;
        markDirty();
        synchronize();
    }

    public ItemStack removeImage() {
        uuid = new UUID(0, 0);
        ItemStack stack = item;
        item = null;
        markDirty();
        synchronize();
        return stack;
    }

    public ItemStack getImage() {
        return item;
    }

    /**
     * Broadcasts the image update packet to all players nearby
     */
    public void synchronize() {
        if (!world.isRemote) {
            world.getEntitiesWithinAABB(EntityPlayer.class,
                    new AxisAlignedBB(pos.getX() - 512, 0, pos.getZ() - 512, pos.getX() + 512, 255, pos.getZ() + 512),
                    entityPlayer -> true)
                    .stream().forEach(p -> Main.SIMPLE_CHANNEL.sendTo(
                    new MessageUpdateImage(pos.getX(), pos.getY(), pos.getZ(), uuid),
                    ((EntityPlayerMP) p).connection.netManager,
                    NetworkDirection.PLAY_TO_CLIENT));
        }
    }

    public boolean hasImage() {
        return uuid.getLeastSignificantBits() != 0 || uuid.getMostSignificantBits() != 0L;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public NBTTagCompound write(NBTTagCompound compound) {
        if (item != null) {
            NBTTagCompound itemCompound = new NBTTagCompound();
            item.write(itemCompound);
            compound.setTag("image", itemCompound);
        }
        return super.write(compound);
    }

    @Override
    public void read(NBTTagCompound compound) {
        super.read(compound);
        if (compound.hasKey("image")) {
            NBTTagCompound itemCompund = compound.getCompound("image");
            item = ItemStack.read(itemCompund);
            UUID id = ItemImage.getUUID(item);
            if (id != null) {
                uuid = id;
            }
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        read(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return write(new NBTTagCompound());
    }
}

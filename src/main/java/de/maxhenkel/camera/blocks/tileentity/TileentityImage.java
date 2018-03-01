package de.maxhenkel.camera.blocks.tileentity;

import de.maxhenkel.camera.items.ItemImage;
import de.maxhenkel.camera.items.ModItems;
import de.maxhenkel.camera.net.MessageUpdateImage;
import de.maxhenkel.camera.proxy.CommonProxy;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.UUID;

public class TileentityImage extends TileEntity {

    private UUID uuid;
    private ItemStack item;

    public TileentityImage() {
        uuid = new UUID(0, 0);
        item = null;
    }

    public void setUUIDOnClient(UUID uuid){
        this.uuid=uuid;
    }

    public void setImage(ItemStack image) {
        if (!image.getItem().equals(ModItems.IMAGE)) {
            return;
        }
        this.uuid = ItemImage.getUUID(image);
        this.item=image;
        markDirty();
        synchronize();
    }

    public ItemStack removeImage() {
        uuid = new UUID(0, 0);
        ItemStack stack=item;
        item = null;
        markDirty();
        synchronize();
        return stack;
    }

    public ItemStack getImage(){
        return item;
    }

    public void synchronize() {
        if (!world.isRemote) {
            CommonProxy.simpleNetworkWrapper.sendToAllAround(new MessageUpdateImage(pos.getX(), pos.getY(), pos.getZ(), uuid), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512D));
        }
    }

    public boolean hasImage() {
        return uuid.getLeastSignificantBits() != 0 || uuid.getMostSignificantBits() != 0L;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if(item!=null){
            NBTTagCompound itemCompound=new NBTTagCompound();
            item.writeToNBT(itemCompound);
            compound.setTag("image", itemCompound);
        }
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.hasKey("image")){
            NBTTagCompound itemCompund=compound.getCompoundTag("image");
            item=new ItemStack(itemCompund);
            UUID id=ItemImage.getUUID(item);
            if(id!=null){
                uuid=id;
            }
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }
}

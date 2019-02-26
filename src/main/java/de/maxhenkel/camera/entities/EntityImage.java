package de.maxhenkel.camera.entities;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.items.ItemImage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class EntityImage extends Entity {

    private static final DataParameter<Optional<UUID>> ID = EntityDataManager.createKey(EntityImage.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<EnumFacing> FACING = EntityDataManager.createKey(EntityImage.class, DataSerializers.FACING);
    private static final DataParameter<BlockPos> POSITION = EntityDataManager.createKey(EntityImage.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Integer> WIDTH = EntityDataManager.createKey(EntityImage.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> HEIGHT = EntityDataManager.createKey(EntityImage.class, DataSerializers.VARINT);
    private static final DataParameter<ItemStack> ITEM = EntityDataManager.createKey(EntityImage.class, DataSerializers.ITEM_STACK);

    private static final double THICKNESS = 1D / 16D;

    private AxisAlignedBB boundingBox;

    public EntityImage(World world) {
        super(Main.IMAGE_ENTITY_TYPE, world);
        width = 1;
        height = 1;
        boundingBox = new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D);
    }

    public EntityImage(World worldIn, BlockPos pos, EnumFacing facing) {
        this(worldIn);
        setPositionAndUpdate(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);

        dataManager.set(POSITION, pos);
        dataManager.set(FACING, facing);
        dataManager.set(WIDTH, 3);
        dataManager.set(HEIGHT, 3);
        fixBoundingBox();
    }

    @Override
    public void tick() {
        fixBoundingBox();
        super.tick();
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (hasImage()) {
            if (stack.equals(ItemStack.EMPTY)) {
                ItemStack containedItem = removeImage();
                if (!world.isRemote) {
                    player.setHeldItem(hand, containedItem);
                    world.playSound(null, getPosition(), SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
            } else {
                ItemStack image = removeImage();
                if (!world.isRemote) {
                    world.playSound(null, getPosition(), SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    if (!player.addItemStackToInventory(image)) {
                        InventoryHelper.spawnItemStack(world, posX, posY, posZ, image); //TODO fix pos
                    }
                }
            }
        }

        if (stack.getItem().equals(Main.IMAGE)) {
            UUID uuid = ItemImage.getUUID(stack);
            if (uuid == null) {
                return true;
            }
            ItemStack frameStack = stack.split(1);
            setItem(frameStack);
            setUUID(uuid);
            player.setHeldItem(hand, stack);
            world.playSound(null, getPosition(), SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        return true;
    }

    private void fixBoundingBox() {
        BlockPos pos = dataManager.get(POSITION);
        EnumFacing facing = dataManager.get(FACING);
        int width = dataManager.get(WIDTH);
        int height = dataManager.get(HEIGHT);

        if (facing.getAxis().isHorizontal()) {
            rotationPitch = 0.0F;
            rotationYaw = facing.getHorizontalIndex() * 90F;
        } else {
            rotationPitch = -90F * facing.getAxisDirection().getOffset();
            rotationYaw = 0.0F;
        }

        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;

        boundingBox = getBoundingBox(pos, facing, width, height);
    }

    private AxisAlignedBB getBoundingBox(BlockPos pos, EnumFacing facing, double width, double height) {
        switch (facing) {
            case UP:
            case DOWN:
            case NORTH:
            default:
                return new AxisAlignedBB(pos.getX() + 1D, pos.getY(), pos.getZ() + 1D - THICKNESS, pos.getX() - width + 1D, pos.getY() + height, pos.getZ() + 1D);
            case SOUTH:
                return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + width, pos.getY() + height, pos.getZ() + THICKNESS);
            case WEST:
                return new AxisAlignedBB(pos.getX() + 1D - THICKNESS, pos.getY(), pos.getZ(), pos.getX() + 1D, pos.getY() + height, pos.getZ() + width);
            case EAST:
                return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ() + 1D, pos.getX() + THICKNESS, pos.getY() + height, pos.getZ() - width + 1D);
        }
    }

    @Override
    protected void registerData() {
        dataManager.register(POSITION, new BlockPos(0, 0, 0));
        dataManager.register(ID, Optional.empty());
        dataManager.register(FACING, EnumFacing.NORTH);
        dataManager.register(WIDTH, 1);
        dataManager.register(HEIGHT, 1);
        dataManager.register(ITEM, ItemStack.EMPTY);
    }

    public void writeAdditional(NBTTagCompound compound) {
        BlockPos pos = dataManager.get(POSITION);
        compound.setInt("posX", pos.getX());
        compound.setInt("posY", pos.getY());
        compound.setInt("posZ", pos.getZ());
        if (dataManager.get(ID).isPresent()) {
            UUID uuid = dataManager.get(ID).get();
            compound.setLong("id_most", uuid.getMostSignificantBits());
            compound.setLong("id_least", uuid.getLeastSignificantBits());
        }
        compound.setInt("facing", dataManager.get(FACING).getIndex());
        compound.setInt("width", dataManager.get(WIDTH));
        compound.setInt("height", dataManager.get(HEIGHT));
        compound.setTag("item", dataManager.get(ITEM).write(new NBTTagCompound()));
    }

    public void readAdditional(NBTTagCompound compound) {
        int x = compound.getInt("posX");
        int y = compound.getInt("posY");
        int z = compound.getInt("posZ");
        dataManager.set(POSITION, new BlockPos(x, y, z));
        if (compound.hasKey("id_most") && compound.hasKey("id_least")) {
            dataManager.set(ID, Optional.of(new UUID(compound.getLong("id_most"), compound.getLong("id_least"))));
        }
        dataManager.set(FACING, EnumFacing.byIndex(compound.getInt("facing")));
        dataManager.set(WIDTH, compound.getInt("width"));
        dataManager.set(HEIGHT, compound.getInt("height"));
        dataManager.set(ITEM, ItemStack.read(compound.getCompound("item")));
        fixBoundingBox();
    }

    public boolean onValidSurface() {
        return true;
    }

    public void onBroken(Entity entity) {
        if (!world.getGameRules().getBoolean("doEntityDrops")) {
            return;
        }
        playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0F, 1.0F);
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.abilities.isCreativeMode) {
                return;
            }
        }

        entityDropItem(Main.FRAME_ITEM);
    }

    public void playPlaceSound() {
        playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (hasImage()) {
            ItemStack image = removeImage();
            if (!world.isRemote) {
                world.playSound(null, getPosition(), SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
                InventoryHelper.spawnItemStack(world, posX, posY, posZ, image); //TODO fix pos
            }
            return true;
        } else if (isInvulnerableTo(source)) {
            return false;
        } else {
            if (!removed && !world.isRemote) {
                onBroken(source.getTrueSource());
                //remove();
                world.removeEntityDangerously(this);
                //markVelocityChanged(); //TODO needed?
            }
            return true;
        }
    }

    @Override
    protected boolean shouldSetPosAfterLoading() {
        return false;
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt lightningBolt) {
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return getBoundingBox();
    }

    @Override
    public boolean canBeCollidedWith() {
        return !removed;
    }

    public EnumFacing getFacing() {
        return dataManager.get(FACING);
    }

    public UUID getImageUUID() {
        Optional<UUID> uuid = dataManager.get(ID);
        if (uuid.isPresent()) {
            return uuid.get();
        } else {
            return null;
        }
    }

    public void setUUID(UUID uuid) {
        if (uuid == null) {
            dataManager.set(ID, Optional.empty());
        } else {
            dataManager.set(ID, Optional.of(uuid));
        }
    }

    public int getWidth() {
        return dataManager.get(WIDTH);
    }

    public int getHeight() {
        return dataManager.get(HEIGHT);
    }

    public void setWidth(int width) {
        dataManager.set(WIDTH, width);
    }

    public void setHeight(int height) {
        dataManager.set(HEIGHT, height);
    }

    public ItemStack getItem() {
        return dataManager.get(ITEM);
    }

    public void setItem(ItemStack stack) {
        dataManager.set(ITEM, stack);
    }

    private boolean hasImage() {
        return !ItemStack.EMPTY.equals(getItem());
    }

    private ItemStack removeImage() {
        ItemStack item = getItem();
        setItem(ItemStack.EMPTY);
        setUUID(null);
        return item;
    }
}

package de.maxhenkel.camera.entities;

import com.google.common.base.Optional;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.ModItems;
import de.maxhenkel.camera.gui.GuiHandler;
import de.maxhenkel.camera.items.ItemImage;
import de.maxhenkel.camera.net.MessageResizeFrame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityImage extends Entity {

    private static final DataParameter<Optional<UUID>> ID = EntityDataManager.createKey(EntityImage.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<EnumFacing> FACING = EntityDataManager.createKey(EntityImage.class, DataSerializers.FACING);
    private static final DataParameter<Integer> WIDTH = EntityDataManager.createKey(EntityImage.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> HEIGHT = EntityDataManager.createKey(EntityImage.class, DataSerializers.VARINT);
    private static final DataParameter<ItemStack> ITEM = EntityDataManager.createKey(EntityImage.class, DataSerializers.ITEM_STACK);

    private static final AxisAlignedBB NULL_AABB = new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D);

    private static final double THICKNESS = 1D / 16D;
    private static final int MAX_WIDTH = 12;
    private static final int MAX_HEIGHT = 12;

    private AxisAlignedBB boundingBox;

    public EntityImage(World world) {
        super(world);
        width = 1;
        height = 1;
        boundingBox = NULL_AABB;
    }

    @Override
    public void onUpdate() {
        updateBoundingBox();
        super.onUpdate();
        checkValid();
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (player.isSneaking() && player.capabilities.allowEdit) {
            player.openGui(Main.MODID, GuiHandler.GUI_RESIZE_FRAME, player.world, (int) posX, (int) posY, (int) posZ);
            return true;
        }

        ItemStack stack = player.getHeldItem(hand);

        if (hasImage()) {
            if (stack.isEmpty()) {
                ItemStack containedItem = removeImage();
                if (!world.isRemote) {
                    player.setHeldItem(hand, containedItem);
                    playRemoveSound();
                }
            } else {
                ItemStack image = removeImage();
                if (!world.isRemote) {
                    playRemoveSound();
                    if (!player.addItemStackToInventory(image)) {
                        dropItem(image);
                    }
                }
            }
        }

        if (stack.getItem().equals(ModItems.IMAGE)) {
            UUID uuid = ItemImage.getUUID(stack);
            if (uuid == null) {
                return true;
            }
            ItemStack frameStack = stack.splitStack(1);
            setItem(frameStack);
            setUUID(uuid);
            player.setHeldItem(hand, stack);
            playAddSound();
            return true;
        }

        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!(source.getImmediateSource() instanceof EntityPlayer)) {
            return false;
        }
        if (!((EntityPlayer) source.getImmediateSource()).capabilities.allowEdit) {
            return false;
        }

        if (hasImage()) {
            ItemStack image = removeImage();
            if (!world.isRemote) {
                playRemoveSound();
                dropItem(image);
            }
            return true;
        } else {
            removeFrame(source.getTrueSource());
            return true;
        }
    }

    public boolean isValid() {
        return !world.collidesWithAnyBlock(getEntityBoundingBox()) && world.getEntitiesWithinAABB(EntityImage.class, getEntityBoundingBox().grow(getFacing().getFrontOffsetX() == 0 ? -2D / 16D : 0D, getFacing().getFrontOffsetY() == 0 ? -2D / 16D : 0D, getFacing().getFrontOffsetZ() == 0 ? -2D / 16D : 0D), image -> image != this).isEmpty();
    }

    public void checkValid() {
        if (!isValid()) {
            removeFrame();
        }
    }

    public void onBroken(Entity entity) {
        if (!world.getGameRules().getBoolean("doEntityDrops")) {
            return;
        }
        playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0F, 1.0F);
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.capabilities.isCreativeMode) {
                return;
            }
        }

        dropItem(new ItemStack(ModItems.IMAGE_FRAME));
        if (hasImage()) {
            dropItem(removeImage());
        }
    }

    public void resize(MessageResizeFrame.Direction direction, boolean larger) {
        int amount = larger ? 1 : -1;
        switch (direction) {
            case UP:
                setHeight(getHeight() + amount);
                break;
            case DOWN:
                if (setHeight(getHeight() + amount)) {
                    setImagePosition(getPosition().offset(EnumFacing.DOWN, amount));
                }
                break;
            case RIGHT:
                setWidth(getWidth() + amount);
                break;
            case LEFT:
                if (setWidth(getWidth() + amount)) {
                    setImagePosition(getPosition().offset(getResizeOffset(), amount));
                }
                break;
        }
    }

    private EnumFacing getResizeOffset() {
        switch (getFacing()) {
            case EAST:
            default:
                return EnumFacing.SOUTH;
            case WEST:
                return EnumFacing.NORTH;
            case NORTH:
                return EnumFacing.EAST;
            case SOUTH:
                return EnumFacing.WEST;
        }
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        if (hasImage()) {
            return getItem().copy();
        }
        return new ItemStack(ModItems.IMAGE_FRAME);
    }

    private void updateBoundingBox() {
        BlockPos pos = getPosition();
        EnumFacing facing = getFacing();
        int width = getWidth();
        int height = getHeight();

        if (facing.getAxis().isHorizontal()) {
            rotationPitch = 0.0F;
            rotationYaw = facing.getHorizontalIndex() * 90F;
        } else {
            rotationPitch = -90F * facing.getAxisDirection().getOffset();
            rotationYaw = 0.0F;
        }

        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;

        boundingBox = calculateBoundingBox(pos, facing, width, height);
    }

    private AxisAlignedBB calculateBoundingBox(BlockPos pos, EnumFacing facing, double width, double height) {
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


    public BlockPos getCenterPosition() {
        Vec3d center = getCenter(getEntityBoundingBox());
        return new BlockPos(center.x, center.y, center.z);
    }

    public Vec3d getCenter(AxisAlignedBB aabb) {
        return new Vec3d(aabb.minX + (aabb.maxX - aabb.minX) * 0.5D, aabb.minY + (aabb.maxY - aabb.minY) * 0.5D, aabb.minZ + (aabb.maxZ - aabb.minZ) * 0.5D);
    }

    @Nullable
    public EntityItem dropItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        BlockPos center = getCenterPosition();
        EntityItem entityitem = new EntityItem(world, center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D, stack);
        entityitem.setDefaultPickupDelay();
        world.spawnEntity(entityitem);
        return entityitem;
    }

    public void removeFrame(Entity source) {
        if (!isDead && !world.isRemote) {
            onBroken(source);
            setDead();
        }
    }

    public void removeFrame() {
        removeFrame(null);
    }

    @Override
    protected boolean shouldSetPosAfterLoading() {
        return false;
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt lightningBolt) {
    }

    @Override
    public AxisAlignedBB getEntityBoundingBox() {
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

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return getEntityBoundingBox();
    }

    @Override
    public boolean canBeCollidedWith() {
        return !isDead;
    }

    public void playPlaceSound() {
        world.playSound(null, getCenterPosition(), SoundEvents.ENTITY_PAINTING_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public void playAddSound() {
        world.playSound(null, getCenterPosition(), SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public void playRemoveSound() {
        world.playSound(null, getCenterPosition(), SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
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
            dataManager.set(ID, Optional.absent());
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

    public boolean setWidth(int width) {
        if (width <= 0) {
            width = 1;
        } else if (width > MAX_WIDTH) {
            width = MAX_WIDTH;
        }
        int oldWidth = getWidth();
        dataManager.set(WIDTH, width);
        return oldWidth != width;
    }

    public boolean setHeight(int height) {
        if (height <= 0) {
            height = 1;
        } else if (height > MAX_HEIGHT) {
            height = MAX_HEIGHT;
        }
        int oldHeight = getHeight();
        dataManager.set(HEIGHT, height);
        return oldHeight != height;
    }

    public ItemStack getItem() {
        return dataManager.get(ITEM);
    }

    public void setItem(ItemStack stack) {
        dataManager.set(ITEM, stack);
    }

    public void setImagePosition(BlockPos position) {
        setLocationAndAngles(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D, rotationYaw, rotationPitch);
        updateBoundingBox();
    }

    public void setFacing(EnumFacing facing) {
        dataManager.set(FACING, facing);
        updateBoundingBox();
    }

    public EnumFacing getFacing() {
        return dataManager.get(FACING);
    }

    private boolean hasImage() {
        return !getItem().isEmpty();
    }

    private ItemStack removeImage() {
        ItemStack item = getItem();
        setItem(ItemStack.EMPTY);
        setUUID(null);
        return item;
    }


    @Override
    protected void entityInit() {
        dataManager.register(ID, Optional.absent());
        dataManager.register(FACING, EnumFacing.NORTH);
        dataManager.register(WIDTH, 1);
        dataManager.register(HEIGHT, 1);
        dataManager.register(ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("id_most") && compound.hasKey("id_least")) {
            setUUID(new UUID(compound.getLong("id_most"), compound.getLong("id_least")));
        }
        setFacing(EnumFacing.getFront(compound.getInteger("facing")));
        setWidth(compound.getInteger("width"));
        setHeight(compound.getInteger("height"));
        setItem(new ItemStack(compound.getCompoundTag("item")));
        updateBoundingBox();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (getImageUUID() != null) {
            UUID uuid = getImageUUID();
            compound.setLong("id_most", uuid.getMostSignificantBits());
            compound.setLong("id_least", uuid.getLeastSignificantBits());
        }
        compound.setInteger("facing", getFacing().getIndex());
        compound.setInteger("width", getWidth());
        compound.setInteger("height", getHeight());
        compound.setTag("item", getItem().writeToNBT(new NBTTagCompound()));
    }
}

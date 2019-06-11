package de.maxhenkel.camera.entities;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.ResizeFrameScreen;
import de.maxhenkel.camera.items.ItemImage;
import de.maxhenkel.camera.net.MessageResizeFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class ImageEntity extends Entity {

    private static final DataParameter<Optional<UUID>> ID = EntityDataManager.createKey(ImageEntity.class, DataSerializers.field_187203_m);
    private static final DataParameter<Direction> FACING = EntityDataManager.createKey(ImageEntity.class, DataSerializers.field_187202_l);
    private static final DataParameter<Integer> WIDTH = EntityDataManager.createKey(ImageEntity.class, DataSerializers.field_187192_b);
    private static final DataParameter<Integer> HEIGHT = EntityDataManager.createKey(ImageEntity.class, DataSerializers.field_187192_b);
    private static final DataParameter<ItemStack> ITEM = EntityDataManager.createKey(ImageEntity.class, DataSerializers.field_187196_f);

    private static final AxisAlignedBB NULL_AABB = new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D);

    private static final double THICKNESS = 1D / 16D;
    private static final int MAX_WIDTH = 12;
    private static final int MAX_HEIGHT = 12;

    private AxisAlignedBB boundingBox;

    public ImageEntity(EntityType type, World world) {
        super(type, world);
        boundingBox = NULL_AABB;
    }

    public ImageEntity(World world, double x, double y, double z) {
        this(Main.IMAGE_ENTITY_TYPE, world);
        this.setPosition(x, y, z);
        this.setMotion(Vec3d.ZERO);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
    }

    @Override
    public void tick() {
        updateBoundingBox();
        super.tick();
        checkValid();
    }

    @Override
    public boolean processInitialInteract(PlayerEntity player, Hand hand) {
        if (player.isSneaking()) {
            if (world.isRemote) {
                openClientGui();
            }
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

        if (stack.getItem().equals(Main.IMAGE)) {
            UUID uuid = ItemImage.getUUID(stack);
            if (uuid == null) {
                return true;
            }
            ItemStack frameStack = stack.split(1);
            setItem(frameStack);
            setUUID(uuid);
            player.setHeldItem(hand, stack);
            playAddSound();
            return true;
        }

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private void openClientGui() {
        Minecraft.getInstance().displayGuiScreen(new ResizeFrameScreen(getUniqueID()));
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (hasImage()) {
            ItemStack image = removeImage();
            if (!world.isRemote) {
                playRemoveSound();
                dropItem(image);
            }
            return true;
        } else if (isInvulnerableTo(source)) {
            return false;
        } else {
            removeFrame(source.getTrueSource());
            return true;
        }
    }

    public boolean isValid() {
        return world.isCollisionBoxesEmpty(this, getBoundingBox()) && world.getEntitiesWithinAABB(ImageEntity.class, getBoundingBox().contract(getFacing().getXOffset() == 0 ? 2D / 16D : 0D, getFacing().getYOffset() == 0 ? 2D / 16D : 0D, getFacing().getZOffset() == 0 ? 2D / 16D : 0D), image -> image != this).isEmpty(); // was shrink //TODO
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
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            if (player.playerAbilities.isCreativeMode) {
                return;
            }
        }

        dropItem(new ItemStack(Main.FRAME_ITEM));
        if (hasImage()) {
            dropItem(removeImage());
        }
    }

    public void resize(MessageResizeFrame.Direction direction, boolean larger) {
        int amount = larger ? 1 : -1;
        switch (direction) {
            case UP:
                setFrameHeight(getFrameHeight() + amount);
                break;
            case DOWN:
                if (setFrameHeight(getFrameHeight() + amount)) {
                    setImagePosition(getPosition().offset(Direction.DOWN, amount));
                }
                break;
            case RIGHT:
                setFrameWidth(getFrameWidth() + amount);
                break;
            case LEFT:
                if (setFrameWidth(getFrameWidth() + amount)) {
                    setImagePosition(getPosition().offset(getResizeOffset(), amount));
                }
                break;
        }
    }

    private Direction getResizeOffset() {
        switch (getFacing()) {
            case EAST:
            default:
                return Direction.SOUTH;
            case WEST:
                return Direction.NORTH;
            case NORTH:
                return Direction.EAST;
            case SOUTH:
                return Direction.WEST;
        }
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        if (hasImage()) {
            return getItem().copy();
        }
        return new ItemStack(Main.FRAME_ITEM);
    }

    private void updateBoundingBox() {
        BlockPos pos = getPosition();
        Direction facing = getFacing();
        int width = getFrameWidth();
        int height = getFrameHeight();

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

    private AxisAlignedBB calculateBoundingBox(BlockPos pos, Direction facing, double width, double height) {
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
        Vec3d center = getCenter(getBoundingBox());
        return new BlockPos(center.x, center.y, center.z);
    }

    public Vec3d getCenter(AxisAlignedBB aabb) {
        return new Vec3d(aabb.minX + (aabb.maxX - aabb.minX) * 0.5D, aabb.minY + (aabb.maxY - aabb.minY) * 0.5D, aabb.minZ + (aabb.maxZ - aabb.minZ) * 0.5D);
    }

    @Nullable
    public ItemEntity dropItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        BlockPos center = getCenterPosition();
        ItemEntity entityitem = new ItemEntity(world, center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D, stack);
        entityitem.setDefaultPickupDelay();
        world.func_217376_c(entityitem);
        return entityitem;
    }

    public void removeFrame(Entity source) {
        if (!removed && !world.isRemote) {
            onBroken(source);
            remove();
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
    public void onStruckByLightning(LightningBoltEntity lightningBolt) {
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
    public IPacket<?> createSpawnPacket() {
        return new SpawnImagePacket(this);
    }

    @Override
    public boolean canBeCollidedWith() {
        return !removed;
    }

    public void playPlaceSound() {
        world.playSound(null, getCenterPosition(), SoundEvents.ENTITY_PAINTING_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public void playAddSound() {
        world.playSound(null, getCenterPosition(), SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public void playRemoveSound() {
        world.playSound(null, getCenterPosition(), SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
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

    public int getFrameWidth() {
        return dataManager.get(WIDTH);
    }

    public int getFrameHeight() {
        return dataManager.get(HEIGHT);
    }

    public boolean setFrameWidth(int width) {
        if (width <= 0) {
            width = 1;
        } else if (width > MAX_WIDTH) {
            width = MAX_WIDTH;
        }
        int oldWidth = getFrameWidth();
        dataManager.set(WIDTH, width);
        return oldWidth != width;
    }

    public boolean setFrameHeight(int height) {
        if (height <= 0) {
            height = 1;
        } else if (height > MAX_HEIGHT) {
            height = MAX_HEIGHT;
        }
        int oldHeight = getFrameHeight();
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

    public void setFacing(Direction facing) {
        dataManager.set(FACING, facing);
        updateBoundingBox();
    }

    public Direction getFacing() {
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
    protected void registerData() {
        dataManager.register(ID, Optional.empty());
        dataManager.register(FACING, Direction.NORTH);
        dataManager.register(WIDTH, 1);
        dataManager.register(HEIGHT, 1);
        dataManager.register(ITEM, ItemStack.EMPTY);
    }

    public void writeAdditional(CompoundNBT compound) {
        if (getImageUUID() != null) {
            UUID uuid = getImageUUID();
            compound.putLong("id_most", uuid.getMostSignificantBits());
            compound.putLong("id_least", uuid.getLeastSignificantBits());
        }
        compound.putInt("facing", getFacing().getIndex());
        compound.putInt("width", getFrameWidth());
        compound.putInt("height", getFrameHeight());
        compound.put("item", getItem().write(new CompoundNBT()));
    }

    public void readAdditional(CompoundNBT compound) {
        if (compound.contains("id_most") && compound.contains("id_least")) {
            setUUID(new UUID(compound.getLong("id_most"), compound.getLong("id_least")));
        }
        setFacing(Direction.byIndex(compound.getInt("facing")));
        setFrameWidth(compound.getInt("width"));
        setFrameHeight(compound.getInt("height"));
        setItem(ItemStack.read(compound.getCompound("item")));
        updateBoundingBox();
    }
}

package de.maxhenkel.camera.entities;

import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.ResizeFrameScreen;
import de.maxhenkel.camera.items.ImageItem;
import de.maxhenkel.camera.net.MessageResizeFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class ImageEntity extends Entity {

    private static final DataParameter<Optional<UUID>> ID = EntityDataManager.defineId(ImageEntity.class, DataSerializers.OPTIONAL_UUID);
    private static final DataParameter<Direction> FACING = EntityDataManager.defineId(ImageEntity.class, DataSerializers.DIRECTION);
    private static final DataParameter<Integer> WIDTH = EntityDataManager.defineId(ImageEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> HEIGHT = EntityDataManager.defineId(ImageEntity.class, DataSerializers.INT);
    private static final DataParameter<ItemStack> ITEM = EntityDataManager.defineId(ImageEntity.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<Optional<UUID>> OWNER = EntityDataManager.defineId(ImageEntity.class, DataSerializers.OPTIONAL_UUID);

    private static final AxisAlignedBB NULL_AABB = new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D);

    private static final double THICKNESS = 1D / 16D;
    private static final int MAX_WIDTH = 8;
    private static final int MAX_HEIGHT = 8;

    private AxisAlignedBB boundingBox;

    public ImageEntity(EntityType type, World world) {
        super(type, world);
        boundingBox = NULL_AABB;
    }

    public ImageEntity(World world) {
        this(Main.IMAGE_ENTITY_TYPE, world);
    }

    public ImageEntity(World world, double x, double y, double z) {
        this(Main.IMAGE_ENTITY_TYPE, world);
        this.setPos(x, y, z);
        this.setDeltaMovement(Vector3d.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    public void tick() {
        updateBoundingBox();
        super.tick();
        checkValid();
    }

    @Override
    public ActionResultType interact(PlayerEntity player, Hand hand) {
        if (!canModify(player)) {
            return ActionResultType.FAIL;
        }

        if (player.isShiftKeyDown() && canModify(player)) {
            if (level.isClientSide) {
                openClientGui();
            }
            return ActionResultType.SUCCESS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        if (hasImage()) {
            if (heldItem.isEmpty()) {
                ItemStack containedItem = removeImage();
                player.setItemInHand(hand, containedItem);
                playRemoveSound();
            } else {
                ItemStack image = removeImage();
                playRemoveSound();
                if (!player.addItem(image)) {
                    dropItem(image);
                }
            }
            return ActionResultType.SUCCESS;
        }

        if (!(heldItem.getItem() instanceof ImageItem)) {
            return ActionResultType.PASS;
        }
        UUID imageID = ImageData.getImageID(heldItem);
        if (imageID == null) {
            return ActionResultType.PASS;
        }

        ItemStack frameStack = heldItem.split(1);
        setItem(frameStack);
        setImageUUID(imageID);
        playAddSound();

        return ActionResultType.sidedSuccess(level.isClientSide);
    }

    public boolean canModify(PlayerEntity player) {
        if (!player.abilities.mayBuild) {
            return false;
        }
        if (!Main.SERVER_CONFIG.frameOnlyOwnerModify.get()) {
            return true;
        }
        if (player.isCreative() && player.hasPermissions(1)) {
            return true;
        }
        if (!getOwner().isPresent()) {
            return true;
        }
        return getOwner().get().equals(player.getUUID());
    }

    @OnlyIn(Dist.CLIENT)
    private void openClientGui() {
        Minecraft.getInstance().setScreen(new ResizeFrameScreen(getUUID()));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level.isClientSide) {
            return true;
        }
        if (!(source.getDirectEntity() instanceof PlayerEntity)) {
            return false;
        }
        if (!canModify((PlayerEntity) source.getDirectEntity())) {
            return false;
        }
        if (hasImage()) {
            ItemStack image = removeImage();
            if (!level.isClientSide) {
                playRemoveSound();
                dropItem(image);
            }
            return true;
        } else if (isInvulnerableTo(source)) {
            return false;
        } else {
            removeFrame(source.getEntity());
            return true;
        }
    }

    public boolean isValid() {
        return level.noCollision(this, getBoundingBox()) && level.getEntitiesOfClass(ImageEntity.class, getBoundingBox().contract(getFacing().getStepX() == 0 ? 2D / 16D : 0D, getFacing().getStepY() == 0 ? 2D / 16D : 0D, getFacing().getStepZ() == 0 ? 2D / 16D : 0D), image -> image != this).isEmpty();
    }

    public void checkValid() {
        if (!isValid()) {
            removeFrame();
        }
    }

    public void onBroken(Entity entity) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }
        playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            if (player.abilities.instabuild) {
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
                    setImagePosition(blockPosition().relative(Direction.DOWN, amount));
                }
                break;
            case RIGHT:
                setFrameWidth(getFrameWidth() + amount);
                break;
            case LEFT:
                if (setFrameWidth(getFrameWidth() + amount)) {
                    setImagePosition(blockPosition().relative(getResizeOffset(), amount));
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
        BlockPos pos = blockPosition();
        Direction facing = getFacing();
        int width = getFrameWidth();
        int height = getFrameHeight();

        if (facing.getAxis().isHorizontal()) {
            xRot = 0.0F;
            yRot = facing.get2DDataValue() * 90F;
        } else {
            xRot = -90F * facing.getAxisDirection().getStep();
            yRot = 0.0F;
        }

        xRotO = xRot;
        yRotO = yRot;

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
        Vector3d center = getCenter(getBoundingBox());
        return new BlockPos(center.x, center.y, center.z);
    }

    public Vector3d getCenter(AxisAlignedBB aabb) {
        return new Vector3d(aabb.minX + (aabb.maxX - aabb.minX) * 0.5D, aabb.minY + (aabb.maxY - aabb.minY) * 0.5D, aabb.minZ + (aabb.maxZ - aabb.minZ) * 0.5D);
    }

    @Nullable
    public ItemEntity dropItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        BlockPos center = getCenterPosition();
        ItemEntity entityitem = new ItemEntity(level, center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D, stack);
        entityitem.setDefaultPickUpDelay();
        level.addFreshEntity(entityitem);
        return entityitem;
    }

    public void removeFrame(Entity source) {
        if (!removed && !level.isClientSide) {
            onBroken(source);
            remove();
        }
    }

    public void removeFrame() {
        removeFrame(null);
    }

    @Override
    protected boolean repositionEntityAfterLoad() {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public AxisAlignedBB getBoundingBoxForCulling() {
        return getBoundingBox();
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPickable() {
        return !removed;
    }

    public void playPlaceSound() {
        level.playSound(null, getCenterPosition(), SoundEvents.PAINTING_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public void playAddSound() {
        level.playSound(null, getCenterPosition(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public void playRemoveSound() {
        level.playSound(null, getCenterPosition(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public Optional<UUID> getOwner() {
        return entityData.get(OWNER);
    }

    public void setOwner(UUID owner) {
        entityData.set(OWNER, Optional.ofNullable(owner));
    }

    public Optional<UUID> getImageUUID() {
        return entityData.get(ID);
    }

    public void setImageUUID(UUID uuid) {
        entityData.set(ID, Optional.ofNullable(uuid));
    }

    public int getFrameWidth() {
        return entityData.get(WIDTH);
    }

    public int getFrameHeight() {
        return entityData.get(HEIGHT);
    }

    public boolean setFrameWidth(int width) {
        if (width <= 0) {
            width = 1;
        } else if (width > MAX_WIDTH) {
            width = MAX_WIDTH;
        }
        int oldWidth = getFrameWidth();
        entityData.set(WIDTH, width);
        return oldWidth != width;
    }

    public boolean setFrameHeight(int height) {
        if (height <= 0) {
            height = 1;
        } else if (height > MAX_HEIGHT) {
            height = MAX_HEIGHT;
        }
        int oldHeight = getFrameHeight();
        entityData.set(HEIGHT, height);
        return oldHeight != height;
    }

    public ItemStack getItem() {
        return entityData.get(ITEM);
    }

    public void setItem(ItemStack stack) {
        entityData.set(ITEM, stack);
    }

    public void setImagePosition(BlockPos position) {
        moveTo(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D, yRot, xRot);
        updateBoundingBox();
    }

    public void setFacing(Direction facing) {
        entityData.set(FACING, facing);
        updateBoundingBox();
    }

    public Direction getFacing() {
        return entityData.get(FACING);
    }

    private boolean hasImage() {
        return !getItem().isEmpty();
    }

    private ItemStack removeImage() {
        ItemStack item = getItem();
        setItem(ItemStack.EMPTY);
        setImageUUID(null);
        return item;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ID, Optional.empty());
        entityData.define(FACING, Direction.NORTH);
        entityData.define(WIDTH, 1);
        entityData.define(HEIGHT, 1);
        entityData.define(ITEM, ItemStack.EMPTY);
        entityData.define(OWNER, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        if (getImageUUID().isPresent()) {
            compound.putUUID("image_id", getImageUUID().get());
        }
        if (getOwner().isPresent()) {
            compound.putUUID("owner", getOwner().get());
        }
        compound.putInt("facing", getFacing().get3DDataValue());
        compound.putInt("width", getFrameWidth());
        compound.putInt("height", getFrameHeight());
        compound.put("item", getItem().save(new CompoundNBT()));
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        if (compound.contains("id_most") && compound.contains("id_least")) { //TODO remove
            setImageUUID(new UUID(compound.getLong("id_most"), compound.getLong("id_least")));
        } else if (compound.contains("image_id")) {
            setImageUUID(compound.getUUID("image_id"));
        }
        if (compound.contains("owner_most") && compound.contains("owner_least")) { //TODO remove
            setOwner(new UUID(compound.getLong("owner_most"), compound.getLong("owner_least")));
        } else if (compound.contains("owner")) {
            setOwner(compound.getUUID("owner"));
        }
        setFacing(Direction.from3DDataValue(compound.getInt("facing")));
        setFrameWidth(compound.getInt("width"));
        setFrameHeight(compound.getInt("height"));
        setItem(ItemStack.of(compound.getCompound("item")));

        updateBoundingBox();
    }
}

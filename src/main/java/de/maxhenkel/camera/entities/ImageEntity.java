package de.maxhenkel.camera.entities;

import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.ResizeFrameScreen;
import de.maxhenkel.camera.items.ImageItem;
import de.maxhenkel.camera.net.MessageResizeFrame;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class ImageEntity extends Entity {

    private static final EntityDataAccessor<Optional<UUID>> ID = SynchedEntityData.defineId(ImageEntity.class, Main.UUID_ENTITY_DATA_SERIALIZER.get());
    private static final EntityDataAccessor<Direction> FACING = SynchedEntityData.defineId(ImageEntity.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Integer> WIDTH = SynchedEntityData.defineId(ImageEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HEIGHT = SynchedEntityData.defineId(ImageEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(ImageEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(ImageEntity.class, Main.UUID_ENTITY_DATA_SERIALIZER.get());

    private static final AABB NULL_AABB = new AABB(0D, 0D, 0D, 0D, 0D, 0D);

    private static final double THICKNESS = 1D / 16D;
    private static final int MAX_WIDTH = 8;
    private static final int MAX_HEIGHT = 8;


    public ImageEntity(EntityType type, Level world) {
        super(type, world);
        setBoundingBox(NULL_AABB);
    }

    public ImageEntity(Level world) {
        this(Main.IMAGE_ENTITY_TYPE.get(), world);
    }

    public ImageEntity(Level world, double x, double y, double z) {
        this(Main.IMAGE_ENTITY_TYPE.get(), world);
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
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
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!canModify(player)) {
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown() && canModify(player)) {
            if (level().isClientSide) {
                openClientGui();
            }
            return InteractionResult.SUCCESS;
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
            return InteractionResult.SUCCESS;
        }

        if (!(heldItem.getItem() instanceof ImageItem)) {
            return InteractionResult.PASS;
        }
        ImageData imageData = ImageData.fromStack(heldItem);
        if (imageData == null) {
            return InteractionResult.PASS;
        }

        ItemStack frameStack = heldItem.split(1);
        setItem(frameStack);
        setImageUUID(imageData.getId());
        playAddSound();

        return InteractionResult.SUCCESS;
    }

    public boolean canModify(Player player) {
        if (!player.getAbilities().mayBuild) {
            return false;
        }
        if (!Main.SERVER_CONFIG.frameOnlyOwnerModify.get()) {
            return true;
        }
        if (player.isCreative() && player.hasPermissions(1)) {
            return true;
        }
        return getOwner().orElse(Util.NIL_UUID).equals(player.getUUID());
    }

    @OnlyIn(Dist.CLIENT)
    private void openClientGui() {
        Minecraft.getInstance().setScreen(new ResizeFrameScreen(getUUID()));
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        if (!(source.getDirectEntity() instanceof Player)) {
            return false;
        }
        if (!canModify((Player) source.getDirectEntity())) {
            return false;
        }
        if (hasImage()) {
            ItemStack image = removeImage();
            if (!level().isClientSide) {
                playRemoveSound();
                dropItem(image);
            }
            return true;
        } else {
            removeFrame(source.getEntity());
            return true;
        }
    }

    public boolean isValid() {
        return level().noCollision(this, getBoundingBox()) && level().getEntitiesOfClass(ImageEntity.class, getBoundingBox().contract(getFacing().getStepX() == 0 ? 2D / 16D : 0D, getFacing().getStepY() == 0 ? 2D / 16D : 0D, getFacing().getStepZ() == 0 ? 2D / 16D : 0D), image -> image != this).isEmpty();
    }

    public void checkValid() {
        if (!isValid()) {
            removeFrame();
        }
    }

    public void onBroken(Entity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }
        playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (player.getAbilities().instabuild) {
                return;
            }
        }

        dropItem(new ItemStack(Main.FRAME_ITEM.get()));
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

    @Nullable
    @Override
    public ItemStack getPickResult() {
        if (hasImage()) {
            return getItem().copy();
        }
        return new ItemStack(Main.FRAME_ITEM.get());
    }

    private void updateBoundingBox() {
        Direction facing = getFacing();

        if (facing.getAxis().isHorizontal()) {
            setXRot(0F);
            setYRot(facing.get2DDataValue() * 90F);
        } else {
            setXRot(-90F * facing.getAxisDirection().getStep());
            setYRot(0F);
        }

        xRotO = getXRot();
        yRotO = getYRot();

        setBoundingBox(makeBoundingBox());
    }

    @Override
    protected AABB makeBoundingBox(Vec3 pos) {
        return calculateBoundingBox(blockPosition(), getFacing(), getFrameWidth(), getFrameHeight());
    }

    private AABB calculateBoundingBox(BlockPos pos, Direction facing, double width, double height) {
        switch (facing) {
            case UP:
            case DOWN:
            case NORTH:
            default:
                return new AABB(pos.getX() + 1D, pos.getY(), pos.getZ() + 1D - THICKNESS, pos.getX() - width + 1D, pos.getY() + height, pos.getZ() + 1D);
            case SOUTH:
                return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + width, pos.getY() + height, pos.getZ() + THICKNESS);
            case WEST:
                return new AABB(pos.getX() + 1D - THICKNESS, pos.getY(), pos.getZ(), pos.getX() + 1D, pos.getY() + height, pos.getZ() + width);
            case EAST:
                return new AABB(pos.getX(), pos.getY(), pos.getZ() + 1D, pos.getX() + THICKNESS, pos.getY() + height, pos.getZ() - width + 1D);
        }
    }

    public BlockPos getCenterPosition() {
        Vec3 center = getBoundingBox().getCenter();
        return new BlockPos.MutableBlockPos(center.x, center.y, center.z);
    }

    @Nullable
    public ItemEntity dropItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        BlockPos center = getCenterPosition();
        ItemEntity entityitem = new ItemEntity(level(), center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D, stack);
        entityitem.setDefaultPickUpDelay();
        level().addFreshEntity(entityitem);
        return entityitem;
    }

    public void removeFrame(Entity source) {
        if (!isRemoved() && level() instanceof ServerLevel serverLevel) {
            onBroken(source);
            kill(serverLevel);
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
    public boolean isPickable() {
        return !isRemoved();
    }

    public void playPlaceSound() {
        level().playSound(null, getCenterPosition(), SoundEvents.PAINTING_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public void playAddSound() {
        level().playSound(null, getCenterPosition(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public void playRemoveSound() {
        level().playSound(null, getCenterPosition(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public Optional<UUID> getOwner() {
        return entityData.get(OWNER);
    }

    public void setOwner(@Nullable UUID owner) {
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
        snapTo(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D, getYRot(), getXRot());
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ID, Optional.empty());
        builder.define(FACING, Direction.NORTH);
        builder.define(WIDTH, 1);
        builder.define(HEIGHT, 1);
        builder.define(ITEM, ItemStack.EMPTY);
        builder.define(OWNER, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        getImageUUID().ifPresent(id -> compound.store("image_id", UUIDUtil.CODEC, id));
        getOwner().ifPresent(owner -> compound.store("owner", UUIDUtil.CODEC, owner));

        compound.putInt("facing", getFacing().get3DDataValue());
        compound.putInt("width", getFrameWidth());
        compound.putInt("height", getFrameHeight());
        ItemStack item = getItem();
        if (!item.isEmpty()) {
            compound.put("item", item.save(registryAccess()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        compound.read("image_id", UUIDUtil.CODEC).ifPresent(this::setImageUUID);
        compound.read("owner", UUIDUtil.CODEC).ifPresent(this::setOwner);

        setFacing(Direction.from3DDataValue(compound.getIntOr("facing", 0)));
        setFrameWidth(compound.getIntOr("width", 0));
        setFrameHeight(compound.getIntOr("height", 0));
        compound.getCompound("item").ifPresent(compoundTag -> {
            setItem(ItemStack.parse(registryAccess(), compoundTag).orElse(ItemStack.EMPTY));
        });

        updateBoundingBox();
    }
}

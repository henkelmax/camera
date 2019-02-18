package de.maxhenkel.camera.blocks;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.blocks.tileentity.TileEntityImage;
import de.maxhenkel.camera.items.ItemImage;
import de.maxhenkel.camera.items.ItemImageFrame;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.*;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import javax.annotation.Nullable;
import java.util.UUID;

public class BlockImageFrame extends Block implements ITileEntityProvider, IItemBlock {

    public static final DirectionProperty FACING = DirectionProperty.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockImageFrame() {
        super(Properties.create(Material.CLOTH, MaterialColor.AIR).hardnessAndResistance(0.25F, 3F).sound(SoundType.CLOTH));
        setRegistryName("image_frame");
        setDefaultState(this.stateContainer.getBaseState().with(FACING, EnumFacing.NORTH));
    }

    @Override
    public Item toItem() {
        return new ItemImageFrame(this).setRegistryName(this.getRegistryName());
    }

    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!canStay(state, worldIn, pos)) {
            worldIn.destroyBlock(pos, true);
        }

        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    public static boolean canStay(IBlockState state, World worldIn, BlockPos pos) {
        EnumFacing enumfacing = state.get(FACING);
        return canStay(enumfacing, worldIn, pos);
    }

    public static boolean canStay(EnumFacing facing, World worldIn, BlockPos pos) {
        IBlockState state = worldIn.getBlockState(pos.offset(facing.getOpposite()));
        return !facing.equals(EnumFacing.DOWN) && !facing.equals(EnumFacing.UP) && state.getMaterial().isSolid() && state.getBlockFaceShape(worldIn, pos.offset(facing.getOpposite()), facing).equals(BlockFaceShape.SOLID);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockReader p_193383_1_, IBlockState p_193383_2_, BlockPos p_193383_3_, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune) {
        super.getDrops(state, drops, world, pos, fortune);
        ItemStack stack = getImage(world, pos);
        if (stack != null) {
            drops.add(stack);
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest, IFluidState fluid) {
        TileEntity tileEntity = world.getTileEntity(pos);
        ItemStack image = null;
        if (tileEntity instanceof TileEntityImage) {
            image = ((TileEntityImage) tileEntity).removeImage();
        }

        boolean removed = super.removedByPlayer(state, world, pos, player, willHarvest, fluid);

        if (removed) {
            if (!world.isRemote && player.abilities.isCreativeMode && image != null) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), image);
            }
        }

        return removed;
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity te = worldIn.getTileEntity(pos);

        if (!(te instanceof TileEntityImage)) {
            return true;
        }

        TileEntityImage tileentityImage = (TileEntityImage) te;
        ItemStack stack = player.getHeldItem(hand);

        if (tileentityImage.hasImage()) {
            if (stack.equals(ItemStack.EMPTY)) {
                ItemStack containedItem = tileentityImage.removeImage();
                if (!worldIn.isRemote) {
                    player.setHeldItem(hand, containedItem);
                    worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }

            } else {
                ItemStack image = tileentityImage.removeImage();
                if (!worldIn.isRemote) {
                    worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    if (!player.addItemStackToInventory(image)) {
                        InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), image);
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
            tileentityImage.setImage(frameStack);
            player.setHeldItem(hand, stack);
            worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        return true;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public IBlockState getStateForPlacement(BlockItemUseContext context) {
        EnumFacing facing = context.getFace();
        if (facing.equals(EnumFacing.DOWN) || facing.equals(EnumFacing.UP)) {
            facing = context.getPlacementHorizontalFacing().getOpposite();
        }
        return getDefaultState().with(FACING, facing);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isTopSolid(IBlockState state) {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IWorldReader world, BlockPos pos, EnumFacing face) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockReader world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, EntityPlayer player) {
        ItemStack stack = getImage(world, pos);
        if (stack != null) {
            return stack;
        }

        return super.getPickBlock(state, target, world, pos, player);
    }

    public ItemStack getImage(IBlockReader world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityImage) {
            TileEntityImage image = (TileEntityImage) te;
            return image.getImage();
        }
        return null;
    }

    public static final VoxelShape SHAPE_NORTH = Block.makeCuboidShape(0D, 0D, 16D, 16D, 16D, 15D);
    public static final VoxelShape SHAPE_SOUTH = Block.makeCuboidShape(0D, 0D, 0D, 16D, 16D, 1D);
    public static final VoxelShape SHAPE_EAST = Block.makeCuboidShape(1D, 0D, 16D, 0D, 16D, 0D);
    public static final VoxelShape SHAPE_WEST = Block.makeCuboidShape(16D, 0D, 0D, 15D, 16D, 16D);

    @Override
    public VoxelShape getShape(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        switch (state.get(FACING)) {
            case NORTH:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
        }
        return SHAPE_NORTH;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return new TileEntityImage();
    }
}

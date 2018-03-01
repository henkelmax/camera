package de.maxhenkel.camera.blocks;

import de.maxhenkel.camera.ItemTools;
import de.maxhenkel.camera.blocks.tileentity.TileentityImage;
import de.maxhenkel.camera.items.ItemImage;
import de.maxhenkel.camera.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemSign;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public class BlockImageFrame extends BlockContainer {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    protected BlockImageFrame() {
        super(Material.CLOTH, MapColor.AIR);
        setUnlocalizedName("image_frame");
        setRegistryName("image_frame");
        setHardness(0.25F);

        setCreativeTab(CreativeTabs.DECORATIONS);
        setSoundType(SoundType.CLOTH);

        setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!canStay(state, worldIn, pos)) {
            dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }

        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    public static boolean canStay(IBlockState state, World worldIn, BlockPos pos) {
        EnumFacing enumfacing = state.getValue(FACING);
        return canStay(enumfacing, worldIn, pos);
    }

    public static boolean canStay(EnumFacing facing, World worldIn, BlockPos pos) {
        IBlockState state = worldIn.getBlockState(pos.offset(facing.getOpposite()));
        return !facing.equals(EnumFacing.DOWN) && !facing.equals(EnumFacing.UP) && state.getMaterial().isSolid() && state.getBlockFaceShape(worldIn, pos.offset(facing.getOpposite()), facing).equals(BlockFaceShape.SOLID);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, fortune);
        ItemStack stack = getImage(world, pos);
        if (stack != null) {
            drops.add(stack);
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {

        TileEntity tileEntity=world.getTileEntity(pos);
        ItemStack image=null;
        if(tileEntity instanceof TileentityImage){
            image= ((TileentityImage) tileEntity).removeImage();
        }

        boolean removed= super.removedByPlayer(state, world, pos, player, willHarvest);

        if(removed){
            if(!world.isRemote&&player.capabilities.isCreativeMode&&image!=null){
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), image);
            }
        }

        return removed;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = worldIn.getTileEntity(pos);

        if (!(te instanceof TileentityImage)) {
            return true;
        }

        TileentityImage tileentityImage = (TileentityImage) te;
        ItemStack stack = playerIn.getHeldItem(hand);

        if (tileentityImage.hasImage()) {
            if (stack.equals(ItemStack.EMPTY)) {
                ItemStack containedItem=tileentityImage.removeImage();
                if (!worldIn.isRemote) {
                    playerIn.setHeldItem(hand, containedItem);
                    worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }

            } else {
                ItemStack image = tileentityImage.removeImage();
                if (!worldIn.isRemote) {
                    worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    if (!playerIn.addItemStackToInventory(image)) {
                        InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), image);
                    }
                }
            }
        }

        if (stack.getItem().equals(ModItems.IMAGE)) {
            UUID uuid = ItemImage.getUUID(stack);
            if (uuid == null) {
                return true;
            }
            ItemStack frameStack=stack.splitStack(1);
            tileentityImage.setImage(frameStack);
            playerIn.setHeldItem(hand, stack);
            worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        }


        return true;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING});
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        if (facing.equals(EnumFacing.DOWN) || facing.equals(EnumFacing.UP)) {
            facing = placer.getHorizontalFacing().getOpposite();
        }
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileentityImage();
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isTopSolid(IBlockState state) {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
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
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        ItemStack stack = getImage(world, pos);
        if (stack != null) {
            return stack;
        }

        return super.getPickBlock(state, target, world, pos, player);
    }

   public ItemStack getImage(IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileentityImage) {
            TileentityImage image = (TileentityImage) te;
            return image.getImage();
        }
        return null;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return null;
    }

    public static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0D, 0D, 1D, 1D, 1D, 0.9375D);
    public static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0D, 0D, 0D, 1D, 1D, 0.0625D);
    public static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(0.0625D, 0D, 1D, 0D, 1D, 0D);
    public static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(1D, 0D, 0D, 0.9375D, 1D, 1D);

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case NORTH:
                return AABB_NORTH;
            case SOUTH:
                return AABB_SOUTH;
            case EAST:
                return AABB_EAST;
            case WEST:
                return AABB_WEST;
        }
        return AABB_NORTH;
    }
}

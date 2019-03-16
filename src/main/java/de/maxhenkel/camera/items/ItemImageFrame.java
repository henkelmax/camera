package de.maxhenkel.camera.items;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.EntityImage;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemImageFrame extends Item {

    public ItemImageFrame() {
        setRegistryName(new ResourceLocation(Main.MODID, "image_frame"));
        setUnlocalizedName("image_frame");
        setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        BlockPos offset = pos.offset(facing);
        if (player != null && !canPlace(player, facing, player.getHeldItem(hand), offset)) {
            return EnumActionResult.FAIL;
        }

        EntityImage image = new EntityImage(worldIn);
        image.setFacing(facing);
        image.setImagePosition(offset);
        if (image.isValid()) {
            if (!worldIn.isRemote) {
                image.playPlaceSound();
                worldIn.spawnEntity(image);
            }
            player.getHeldItem(hand).shrink(1);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

    protected boolean canPlace(EntityPlayer player, EnumFacing facing, ItemStack stack, BlockPos pos) {
        return !facing.getAxis().isVertical() && player.canPlayerEdit(pos, facing, stack);
    }
}
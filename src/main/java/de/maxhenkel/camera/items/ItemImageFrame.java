package de.maxhenkel.camera.items;

import de.maxhenkel.camera.blocks.BlockImageFrame;
import de.maxhenkel.camera.blocks.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemImageFrame extends ItemBlock{

    public ItemImageFrame(){
        super(ModBlocks.IMAGE);
        setCreativeTab(CreativeTabs.DECORATIONS);
        setUnlocalizedName("image_frame");
        setRegistryName("image_frame");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(BlockImageFrame.canStay(facing, worldIn, pos.offset(facing))){
            return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        }
        return EnumActionResult.FAIL;
    }
}

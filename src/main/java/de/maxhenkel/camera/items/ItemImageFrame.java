package de.maxhenkel.camera.items;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.EntityImage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemImageFrame extends Item {

    public ItemImageFrame() {
        super(new Properties().group(ItemGroup.DECORATIONS));
        setRegistryName(new ResourceLocation(Main.MODID, "image_frame"));
    }

    @Override
    public EnumActionResult onItemUse(ItemUseContext context) {
        BlockPos pos = context.getPos();
        EnumFacing facing = context.getFace();
        BlockPos offset = pos.offset(facing);
        EntityPlayer player = context.getPlayer();
        if (player != null && !canPlace(player, facing, context.getItem(), offset)) {
            return EnumActionResult.FAIL;
        }
        World world = context.getWorld();

        if (!world.isRemote) {
            EntityImage image = new EntityImage(world, offset, facing);
            //if (image.onValidSurface()) {
                image.playPlaceSound();
                world.spawnEntity(image);
            //}
        }

        context.getItem().shrink(1);

        return EnumActionResult.SUCCESS;
    }

    protected boolean canPlace(EntityPlayer player, EnumFacing facing, ItemStack stack, BlockPos pos) {
        return !facing.getAxis().isVertical() && player.canPlayerEdit(pos, facing, stack);
    }
}
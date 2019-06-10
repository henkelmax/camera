package de.maxhenkel.camera.items;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.EntityImage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemImageFrame extends Item {

    public ItemImageFrame() {
        super(new Properties().group(ItemGroup.DECORATIONS));
        setRegistryName(new ResourceLocation(Main.MODID, "image_frame"));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockPos pos = context.getPos();
        Direction facing = context.getFace();
        BlockPos offset = pos.offset(facing);
        PlayerEntity player = context.getPlayer();
        if (player != null && !canPlace(player, facing, context.getItem(), offset)) {
            return ActionResultType.FAIL;
        }

        World world = context.getWorld();
        EntityImage image = Main.IMAGE_ENTITY_TYPE.create(world);
        image.setFacing(facing);
        image.setImagePosition(offset);
        if (image.isValid()) {
            if (!world.isRemote) {
                image.playPlaceSound();
                world.func_217376_c(image);
            }
            context.getItem().shrink(1);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    protected boolean canPlace(PlayerEntity player, Direction facing, ItemStack stack, BlockPos pos) {
        return !facing.getAxis().isVertical() && player.canPlayerEdit(pos, facing, stack);
    }
}
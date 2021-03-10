package de.maxhenkel.camera.items;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.ImageEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ImageFrameItem extends Item {

    public ImageFrameItem() {
        super(new Properties().tab(ItemGroup.TAB_DECORATIONS));
        setRegistryName(new ResourceLocation(Main.MODID, "image_frame"));
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getClickedFace();
        BlockPos offset = pos.relative(facing);
        PlayerEntity player = context.getPlayer();
        if (player != null && !canPlace(player, facing, context.getItemInHand(), offset)) {
            return ActionResultType.FAIL;
        }

        World world = context.getLevel();
        ImageEntity image = Main.IMAGE_ENTITY_TYPE.create(world);
        if (image == null) {
            return ActionResultType.FAIL;
        }
        image.setFacing(facing);
        image.setImagePosition(offset);
        image.setOwner(context.getPlayer().getUUID());
        if (image.isValid()) {
            if (!world.isClientSide) {
                image.playPlaceSound();
                world.addFreshEntity(image);
            }
            context.getItemInHand().shrink(1);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    protected boolean canPlace(PlayerEntity player, Direction facing, ItemStack stack, BlockPos pos) {
        return !facing.getAxis().isVertical() && player.mayUseItemAt(pos, facing, stack);
    }
}
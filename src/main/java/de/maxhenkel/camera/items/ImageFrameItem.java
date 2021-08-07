package de.maxhenkel.camera.items;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.ImageEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ImageFrameItem extends Item {

    public ImageFrameItem() {
        super(new Properties().tab(CreativeModeTab.TAB_DECORATIONS));
        setRegistryName(new ResourceLocation(Main.MODID, "image_frame"));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getClickedFace();
        BlockPos offset = pos.relative(facing);
        Player player = context.getPlayer();
        if (player != null && !canPlace(player, facing, context.getItemInHand(), offset)) {
            return InteractionResult.FAIL;
        }

        Level world = context.getLevel();
        ImageEntity image = Main.IMAGE_ENTITY_TYPE.create(world);
        if (image == null) {
            return InteractionResult.FAIL;
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
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    protected boolean canPlace(Player player, Direction facing, ItemStack stack, BlockPos pos) {
        return !facing.getAxis().isVertical() && player.mayUseItemAt(pos, facing, stack);
    }
}
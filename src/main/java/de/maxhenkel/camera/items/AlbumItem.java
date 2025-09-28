package de.maxhenkel.camera.items;

import de.maxhenkel.camera.CameraClientMod;
import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.CameraMod;
import de.maxhenkel.camera.gui.AlbumInventoryContainer;
import de.maxhenkel.camera.inventory.AlbumInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AlbumItem extends Item {

    public AlbumItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (playerIn.isShiftKeyDown()) {
            if (!playerIn.level().isClientSide() && playerIn instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new MenuProvider() {

                    @Override
                    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                        return new AlbumInventoryContainer(id, playerInventory, new AlbumInventory(stack));
                    }

                    @Override
                    public Component getDisplayName() {
                        return Component.translatable(AlbumItem.this.getDescriptionId());
                    }
                });
            }
        } else {
            openAlbum(playerIn, stack);
        }
        return InteractionResult.SUCCESS;
    }

    public static void openAlbum(Player player, ItemStack album) {
        if (player.level().isClientSide()) {
            List<UUID> images = CameraMod.ALBUM.get().getImages(album);
            if (!images.isEmpty()) {
                CameraClientMod.openAlbumScreen(images);
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.is(Blocks.LECTERN)) {
            return LecternBlock.tryPlaceBook(context.getPlayer(), world, blockpos, blockstate, context.getItemInHand()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        } else {
            return InteractionResult.PASS;
        }
    }

    public List<UUID> getImages(ItemStack stack) {
        if (stack.isEmpty()) {
            return Collections.emptyList();
        }
        List<UUID> images = new ArrayList<>();
        Container inventory = new AlbumInventory(stack);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack s = inventory.getItem(i);
            ImageData imageData = ImageData.fromStack(s);
            if (imageData == null) {
                continue;
            }
            images.add(imageData.getId());
        }
        return images;
    }
}

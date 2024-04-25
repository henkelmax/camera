package de.maxhenkel.camera.items;

import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.AlbumInventoryContainer;
import de.maxhenkel.camera.gui.AlbumScreen;
import de.maxhenkel.camera.inventory.AlbumInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AlbumItem extends Item {

    public AlbumItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (playerIn.isShiftKeyDown()) {
            if (!playerIn.level().isClientSide && playerIn instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new MenuProvider() {

                    @Override
                    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                        return new AlbumInventoryContainer(id, playerInventory, new AlbumInventory(serverPlayer.registryAccess(), stack));
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
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    public static void openAlbum(Player player, ItemStack album) {
        if (player.level().isClientSide) {
            List<UUID> images = Main.ALBUM.get().getImages(player.level().registryAccess(), album);
            if (!images.isEmpty()) {
                openClientGui(images);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void openClientGui(List<UUID> images) {
        Minecraft.getInstance().setScreen(new AlbumScreen(images));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.is(Blocks.LECTERN)) {
            return LecternBlock.tryPlaceBook(context.getPlayer(), world, blockpos, blockstate, context.getItemInHand()) ? InteractionResult.sidedSuccess(world.isClientSide) : InteractionResult.PASS;
        } else {
            return InteractionResult.PASS;
        }
    }

    public List<UUID> getImages(HolderLookup.Provider provider, ItemStack stack) {
        if (stack.isEmpty()) {
            return Collections.emptyList();
        }
        List<UUID> images = new ArrayList<>();
        Container inventory = new AlbumInventory(provider, stack);
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

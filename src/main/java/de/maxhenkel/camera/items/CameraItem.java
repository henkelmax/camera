package de.maxhenkel.camera.items;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.ModSounds;
import de.maxhenkel.camera.gui.CameraScreen;
import de.maxhenkel.camera.net.MessageTakeImage;
import de.maxhenkel.corelib.item.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CameraItem extends Item {

    public CameraItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        if (playerIn.isShiftKeyDown() && !isActive(stack)) {
            if (worldIn.isClientSide) {
                openClientGui(stack.get(Main.SHADER_DATA_COMPONENT));
            }
            return InteractionResult.SUCCESS;
        }

        if (!(playerIn instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (!isActive(stack)) {
            Main.CAMERA.get().setActive(stack, true);
        } else if (Main.PACKET_MANAGER.canTakeImage(playerIn.getUUID())) {
            if (consumePaper(playerIn)) {
                worldIn.playSound(null, playerIn.blockPosition(), ModSounds.TAKE_IMAGE.get(), SoundSource.AMBIENT, 1F, 1F);
                UUID uuid = UUID.randomUUID();
                PacketDistributor.sendToPlayer(serverPlayer, new MessageTakeImage(uuid));
                Main.CAMERA.get().setActive(stack, false);
            } else {
                playerIn.displayClientMessage(Component.translatable("message.no_consumable"), true);
            }
        } else {
            playerIn.displayClientMessage(Component.translatable("message.image_cooldown"), true);
        }
        return InteractionResult.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    private void openClientGui(String currentShader) {
        Minecraft.getInstance().setScreen(new CameraScreen(currentShader));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 50000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        if (isActive(stack)) {
            return ItemUseAnimation.BOW;
        } else {
            return ItemUseAnimation.NONE;
        }
    }

    public static boolean consumePaper(Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        int amountNeeded = Main.SERVER_CONFIG.cameraConsumeItemAmount.get();
        List<ItemStack> consumeStacks = findPaper(player);

        int count = 0;
        for (ItemStack stack : consumeStacks) {
            count += stack.getCount();
        }
        if (count >= amountNeeded) {
            for (ItemStack stack : consumeStacks) {
                amountNeeded -= stack.getCount() - ItemUtils.itemStackAmount(-amountNeeded, stack, null).getCount();
            }
            return true;
        }

        return false;
    }

    private static List<ItemStack> findPaper(Player player) {
        List<ItemStack> items = new ArrayList<>();
        if (isPaper(player.getItemInHand(InteractionHand.MAIN_HAND))) {
            items.add(player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        if (isPaper(player.getItemInHand(InteractionHand.OFF_HAND))) {
            items.add(player.getItemInHand(InteractionHand.OFF_HAND));
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack itemstack = player.getInventory().getItem(i);

            if (isPaper(itemstack)) {
                items.add(itemstack);
            }
        }
        return items;
    }

    protected static boolean isPaper(ItemStack stack) {
        return stack.is(Main.IMAGE_PAPER);
    }

    public boolean isActive(ItemStack stack) {
        return stack.has(Main.ACTIVE_DATA_COMPONENT);
    }

    public void setActive(ItemStack stack, boolean active) {
        if (active) {
            stack.set(Main.ACTIVE_DATA_COMPONENT, Unit.INSTANCE);
        } else {
            stack.remove(Main.ACTIVE_DATA_COMPONENT);
        }
    }

}
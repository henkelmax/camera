package de.maxhenkel.camera.items;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.ModSounds;
import de.maxhenkel.camera.gui.CameraScreen;
import de.maxhenkel.camera.net.MessageTakeImage;
import de.maxhenkel.corelib.item.ItemUtils;
import de.maxhenkel.corelib.net.NetUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CameraItem extends Item {

    public CameraItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        if (playerIn.isShiftKeyDown() && !isActive(stack)) {
            if (worldIn.isClientSide) {
                openClientGui(getShader(stack));
            }
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        if (!(playerIn instanceof ServerPlayer)) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        if (!isActive(stack)) {
            Main.CAMERA.get().setActive(stack, true);
        } else if (Main.PACKET_MANAGER.canTakeImage(playerIn.getUUID())) {
            if (consumePaper(playerIn)) {
                worldIn.playSound(null, playerIn.blockPosition(), ModSounds.TAKE_IMAGE.get(), SoundSource.AMBIENT, 1F, 1F);
                UUID uuid = UUID.randomUUID();
                NetUtils.sendTo(Main.SIMPLE_CHANNEL, (ServerPlayer) playerIn, new MessageTakeImage(uuid));
                Main.CAMERA.get().setActive(stack, false);
            } else {
                playerIn.displayClientMessage(Component.translatable("message.no_consumable"), true);
            }
        } else {
            playerIn.displayClientMessage(Component.translatable("message.image_cooldown"), true);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @OnlyIn(Dist.CLIENT)
    private void openClientGui(String currentShader) {
        Minecraft.getInstance().setScreen(new CameraScreen(currentShader));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 50000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if (isActive(stack)) {
            return UseAnim.BOW;
        } else {
            return UseAnim.NONE;
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
        CompoundTag compound = stack.getOrCreateTag();
        if (!compound.contains("active")) {
            compound.putBoolean("active", false);
        }
        return compound.getBoolean("active");
    }

    public void setActive(ItemStack stack, boolean active) {
        stack.getOrCreateTag().putBoolean("active", active);
    }

    public String getShader(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        if (!compound.contains("shader")) {
            return null;
        }
        return compound.getString("shader");
    }

    public void setShader(ItemStack stack, String shader) {
        if (shader != null) {
            stack.getOrCreateTag().putString("shader", shader);
        }
    }

}
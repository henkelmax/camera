package de.maxhenkel.camera.items;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.ModSounds;
import de.maxhenkel.camera.gui.CameraScreen;
import de.maxhenkel.camera.net.MessageTakeImage;
import de.maxhenkel.corelib.item.ItemUtils;
import de.maxhenkel.corelib.net.NetUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CameraItem extends Item {

    public CameraItem() {
        super(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_DECORATIONS));
        setRegistryName(new ResourceLocation(Main.MODID, "camera"));
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        if (playerIn.isShiftKeyDown() && !isActive(stack)) {
            if (worldIn.isClientSide) {
                openClientGui(getShader(stack));
            }
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        if (!(playerIn instanceof ServerPlayerEntity)) {
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        if (!isActive(stack)) {
            Main.CAMERA.setActive(stack, true);
        } else if (Main.PACKET_MANAGER.canTakeImage(playerIn.getUUID())) {
            if (consumePaper(playerIn)) {
                worldIn.playSound(null, playerIn.blockPosition(), ModSounds.TAKE_IMAGE, SoundCategory.AMBIENT, 1F, 1F);
                UUID uuid = UUID.randomUUID();
                NetUtils.sendTo(Main.SIMPLE_CHANNEL, (ServerPlayerEntity) playerIn, new MessageTakeImage(uuid));
                Main.CAMERA.setActive(stack, false);
            } else {
                playerIn.displayClientMessage(new TranslationTextComponent("message.no_consumable"), true);
            }
        } else {
            playerIn.displayClientMessage(new TranslationTextComponent("message.image_cooldown"), true);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
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
    public UseAction getUseAnimation(ItemStack stack) {
        if (isActive(stack)) {
            return UseAction.BOW;
        } else {
            return UseAction.NONE;
        }
    }

    public static boolean consumePaper(PlayerEntity player) {
        if (player.abilities.instabuild) {
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

    private static List<ItemStack> findPaper(PlayerEntity player) {
        List<ItemStack> items = new ArrayList<>();
        if (isPaper(player.getItemInHand(Hand.MAIN_HAND))) {
            items.add(player.getItemInHand(Hand.MAIN_HAND));
        }
        if (isPaper(player.getItemInHand(Hand.OFF_HAND))) {
            items.add(player.getItemInHand(Hand.OFF_HAND));
        }
        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            ItemStack itemstack = player.inventory.getItem(i);

            if (isPaper(itemstack)) {
                items.add(itemstack);
            }
        }
        return items;
    }

    protected static boolean isPaper(ItemStack stack) {
        return stack.getItem().is(Main.SERVER_CONFIG.cameraConsumeItem);
    }

    public boolean isActive(ItemStack stack) {
        CompoundNBT compound = stack.getOrCreateTag();
        if (!compound.contains("active")) {
            compound.putBoolean("active", false);
        }
        return compound.getBoolean("active");
    }

    public void setActive(ItemStack stack, boolean active) {
        stack.getOrCreateTag().putBoolean("active", active);
    }

    public String getShader(ItemStack stack) {
        CompoundNBT compound = stack.getOrCreateTag();
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
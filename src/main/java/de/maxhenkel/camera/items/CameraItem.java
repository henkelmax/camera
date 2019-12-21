package de.maxhenkel.camera.items;

import de.maxhenkel.camera.Config;
import de.maxhenkel.camera.ItemTools;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.ModSounds;
import de.maxhenkel.camera.gui.CameraScreen;
import de.maxhenkel.camera.net.MessageTakeImage;
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
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CameraItem extends Item {

    public CameraItem() {
        super(new Item.Properties().maxStackSize(1).group(ItemGroup.DECORATIONS));
        setRegistryName(new ResourceLocation(Main.MODID, "camera"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (playerIn.func_225608_bj_() && !isActive(stack)) {
            if (worldIn.isRemote) {
                openClientGui(getShader(stack));
            }
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        if (!(playerIn instanceof ServerPlayerEntity)) {
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        if (!isActive(stack)) {
            Main.CAMERA.setActive(stack, true);
        } else if (Main.PACKET_MANAGER.canTakeImage(playerIn.getUniqueID())) {
            if (consumePaper(playerIn)) {
                worldIn.playSound(null, playerIn.getPosition(), ModSounds.TAKE_IMAGE, SoundCategory.AMBIENT, 1.0F, 1.0F);
                UUID uuid = UUID.randomUUID();
                Main.SIMPLE_CHANNEL.sendTo(new MessageTakeImage(uuid), ((ServerPlayerEntity) playerIn).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
                Main.CAMERA.setActive(stack, false);
            } else {
                playerIn.sendStatusMessage(new TranslationTextComponent("message.no_consumable", Config.getConsumingStack().getDisplayName(), Config.getConsumingStack().getCount()), true);
            }
        } else {
            playerIn.sendStatusMessage(new TranslationTextComponent("message.image_cooldown"), true);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @OnlyIn(Dist.CLIENT)
    private void openClientGui(String currentShader) {
        Minecraft.getInstance().displayGuiScreen(new CameraScreen(currentShader));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 50000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        if (isActive(stack)) {
            return UseAction.BOW;
        } else {
            return UseAction.NONE;
        }
    }

    public static boolean consumePaper(PlayerEntity player) {
        if (player.abilities.isCreativeMode) {
            return true;
        }

        int amountNeeded = Config.getConsumingStack().getCount();
        List<ItemStack> consumeStacks = findPaper(player);

        int count = 0;
        for (ItemStack stack : consumeStacks) {
            count += stack.getCount();
        }
        if (count >= amountNeeded) {
            for (ItemStack stack : consumeStacks) {
                amountNeeded -= stack.getCount() - ItemTools.itemStackAmount(-amountNeeded, stack, null);
            }
            return true;
        }

        return false;
    }

    private static List<ItemStack> findPaper(PlayerEntity player) {
        List<ItemStack> items = new ArrayList<>();
        if (isPaper(player.getHeldItem(Hand.MAIN_HAND))) {
            items.add(player.getHeldItem(Hand.MAIN_HAND));
        }
        if (isPaper(player.getHeldItem(Hand.OFF_HAND))) {
            items.add(player.getHeldItem(Hand.OFF_HAND));
        }
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack itemstack = player.inventory.getStackInSlot(i);

            if (isPaper(itemstack)) {
                items.add(itemstack);
            }
        }
        return items;
    }

    protected static boolean isPaper(ItemStack stack) {
        return ItemTools.areItemsEqual(stack, Config.getConsumingStack());
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

package de.maxhenkel.camera;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;

public class ItemTools {

    public static boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
        if (stack1 == null || stack2 == null) {
            return false;
        }

        if (stack1.getItem() == stack2.getItem()) {
            return stack1.getDamage() == stack2.getDamage();
        }

        return false;
    }

    /**
     * Changes the Itemstack amount. If a player is provided and the player is in
     * Creative Mode, the stack wont be changed
     *
     * @param amount The amount to change
     * @param stack  The Item Stack
     * @param player The player. Can be null
     * @return The amount left
     */
    public static int itemStackAmount(int amount, ItemStack stack, PlayerEntity player) {
        if (stack == null) {
            return 0;
        }

        if (player == null || !player.abilities.isCreativeMode) {
            stack.setCount(stack.getCount() + amount);
            if (stack.getCount() <= 0) {
                stack.setCount(0);
            }
        }


        if (stack.getCount() > stack.getMaxStackSize()) {
            stack.setCount(stack.getMaxStackSize());
        }

        return stack.getCount();
    }

    public static String serializeItemStack(ItemStack stack) {
        return stack.serializeNBT().toString();
    }

    public static ItemStack deserializeItemStack(String json) {
        try {
            return ItemStack.read(JsonToNBT.getTagFromJson(json));
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return ItemStack.EMPTY;
        }
    }

}

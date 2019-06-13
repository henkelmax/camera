package de.maxhenkel.camera;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;

import java.util.List;

public class ItemTools {

    public static boolean isStackEmpty(ItemStack stack) {
        if (stack == null) {
            return true;
        }

        if (stack.equals(ItemStack.EMPTY)) {
            return true;
        }

        if (stack.getItem().equals(Items.AIR)) {
            return true;
        }

        if (stack.getCount() <= 0) {
            return true;
        }

        return false;
    }

    //TODO implement
	/*public static boolean matchesOredict(ItemStack stack, String name) {
		return contains(OreDictionary.getOres(name), stack);
	}*/

    public static boolean areItemsEqualWithEmpty(ItemStack stack1, ItemStack stack2) {
        if (isStackEmpty(stack1) && isStackEmpty(stack2)) {
            return true;
        }

        return areItemsEqual(stack1, stack2);
    }

	/*public static boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
		if (stack1 == null || stack2 == null) {
			return false;
		}
		return ItemStack.areItemsEqual(stack1, stack2);
	}*/

    public static boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
        if (stack1 == null || stack2 == null) {
            return false;
        }

        if (stack1.getItem() == null || stack2.getItem() == null) {
            return false;
        }

        if (stack1.getItem() == stack2.getItem()) {

            return stack1.getDamage() == -1//OreDictionary.WILDCARD_VALUE // TODO fix
                    || stack2.getDamage() == -1//OreDictionary.WILDCARD_VALUE // TODO fix
                    || stack1.getDamage() == stack2.getDamage();
        }

        return false;
    }

    public static boolean contains(List<ItemStack> list, ItemStack item) {
        for (ItemStack i : list) {
            if (areItemsEqual(item, i)) {
                return true;
            }
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

        if (player == null || !player.playerAbilities.isCreativeMode) {
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

    public static int decrItemStack(ItemStack stack, PlayerEntity player) {
        return itemStackAmount(-1, stack, player);
    }

    public static int incrItemStack(ItemStack stack, PlayerEntity player) {
        return itemStackAmount(1, stack, player);
    }

    /*
    public static ItemStack damageStack(ItemStack stack, int amount, LivingEntity entity) {
        //TODO check
        stack.func_222118_a(amount, entity, (livingEntity -> {
        }));
        return stack;
    }
    */

    public static void removeStackFromSlot(IInventory inventory, int index) {
        inventory.setInventorySlotContents(index, ItemStack.EMPTY);
    }

    public static void saveInventory(CompoundNBT compound, String name, IInventory inv) {
        ListNBT nbttaglist = new ListNBT();

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (!isStackEmpty(inv.getStackInSlot(i))) {
                CompoundNBT nbttagcompound = new CompoundNBT();
                nbttagcompound.putInt("Slot", i);
                inv.getStackInSlot(i).write(nbttagcompound);
                nbttaglist.add(nbttagcompound);
            }
        }

        compound.put(name, nbttaglist);
    }

    public static void readInventory(CompoundNBT compound, String name, IInventory inv) {
        if (!compound.contains(name)) {
            return;
        }

        ListNBT nbttaglist = compound.getList(name, 10);

        if (nbttaglist == null) {
            return;
        }

        for (int i = 0; i < nbttaglist.size(); i++) {
            CompoundNBT nbttagcompound = nbttaglist.getCompound(i);
            int j = nbttagcompound.getInt("Slot");

            if (j >= 0 && j < inv.getSizeInventory()) {
                inv.setInventorySlotContents(j, ItemStack.read(nbttagcompound));
            }
        }
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

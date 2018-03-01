package de.maxhenkel.camera;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

public class ItemTools {

	public static final ItemStack EMPTY = ItemStack.EMPTY;

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

	public static boolean matchesOredict(ItemStack stack, String name) {
		return contains(OreDictionary.getOres(name), stack);
	}

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

			return stack1.getItemDamage() == OreDictionary.WILDCARD_VALUE
					|| stack2.getItemDamage() == OreDictionary.WILDCARD_VALUE
					|| stack1.getItemDamage() == stack2.getItemDamage();
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
	 * @param amount
	 *            The amount to change
	 * @param stack
	 *            The Item Stack
	 * @param player
	 *            The player. Can be null
	 * @return The Itemstack with the changed amount
	 */
	public static ItemStack itemStackAmount(int amount, ItemStack stack, EntityPlayer player) {
		if (stack == null) {
			return ItemStack.EMPTY;
		}

		if (player != null && player.capabilities.isCreativeMode) {
			return stack;
		}

		stack.setCount(stack.getCount() + amount);
		if (stack.getCount() <= 0) {
			stack.setCount(0);
			return ItemStack.EMPTY;
		}

		if (stack.getCount() > stack.getMaxStackSize()) {
			stack.setCount(stack.getMaxStackSize());
		}

		return stack;
	}

	public static ItemStack decrItemStack(ItemStack stack, EntityPlayer player) {
		return itemStackAmount(-1, stack, player);
	}

	public static ItemStack incrItemStack(ItemStack stack, EntityPlayer player) {
		return itemStackAmount(1, stack, player);
	}

	public static ItemStack damageStack(ItemStack stack, int amount, EntityLivingBase entity) {
		stack.damageItem(amount, entity);
		return stack;
	}

	public static void removeStackFromSlot(IInventory inventory, int index) {
		inventory.setInventorySlotContents(index, ItemStack.EMPTY);
	}

	public static void saveInventory(NBTTagCompound compound, String name, IInventory inv) {
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			if (!isStackEmpty(inv.getStackInSlot(i))) {
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setInteger("Slot", i);
				inv.getStackInSlot(i).writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}

		compound.setTag(name, nbttaglist);
	}

	public static void readInventory(NBTTagCompound compound, String name, IInventory inv) {
		if (!compound.hasKey(name)) {
			return;
		}

		NBTTagList nbttaglist = compound.getTagList(name, 10);

		if (nbttaglist == null) {
			return;
		}

		for (int i = 0; i < nbttaglist.tagCount(); i++) {
			NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound.getInteger("Slot");

			if (j >= 0 && j < inv.getSizeInventory()) {
				inv.setInventorySlotContents(j, new ItemStack(nbttagcompound));
			}
		}
	}

}

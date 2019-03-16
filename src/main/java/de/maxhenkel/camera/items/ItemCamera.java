package de.maxhenkel.camera.items;

import de.maxhenkel.camera.ItemTools;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.ModItems;
import de.maxhenkel.camera.ModSounds;
import de.maxhenkel.camera.gui.GuiHandler;
import de.maxhenkel.camera.net.MessageTakeImage;
import de.maxhenkel.camera.proxy.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.UUID;

public class ItemCamera extends Item {

    public ItemCamera() {
        setRegistryName(new ResourceLocation(Main.MODID, "camera"));
        setUnlocalizedName("camera");
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (playerIn.isSneaking() && !isActive(stack)) {
            playerIn.openGui(Main.MODID, GuiHandler.GUI_CAMERA, worldIn, (int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        if (worldIn.isRemote || !(playerIn instanceof EntityPlayerMP)) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        if (!isActive(stack)) {
            ModItems.CAMERA.setActive(stack, true);
        } else if (CommonProxy.packetManager.canTakeImage(playerIn.getUniqueID())) {
            if (consumePaper(playerIn)) {
                ModItems.CAMERA.setActive(stack, false);
                worldIn.playSound(null, playerIn.getPosition(), ModSounds.TAKE_IMAGE, SoundCategory.AMBIENT, 1.0F, 1.0F);
                UUID uuid = UUID.randomUUID();
                CommonProxy.simpleNetworkWrapper.sendTo(new MessageTakeImage(uuid), (EntityPlayerMP) playerIn);
            } else {
                playerIn.sendStatusMessage(new TextComponentTranslation("message.no_paper"), true);
            }
        } else {
            playerIn.sendStatusMessage(new TextComponentTranslation("message.image_cooldown"), true);
        }


        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entity, ItemStack stack) {
        if (!isActive(stack)) {
            return false;
        }

        if (entity instanceof EntityPlayer) {
            onItemRightClick(entity.world, (EntityPlayer) entity, EnumHand.MAIN_HAND);
        }
        return true;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 50000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    private boolean consumePaper(EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            return true;
        }

        ItemStack paper = findPaper(player);

        if (paper == null) {
            return false;
        }

        ItemTools.decrItemStack(paper, null);
        return true;
    }

    private ItemStack findPaper(EntityPlayer player) {
        if (isPaper(player.getHeldItem(EnumHand.OFF_HAND))) {
            return player.getHeldItem(EnumHand.OFF_HAND);
        } else if (isPaper(player.getHeldItem(EnumHand.MAIN_HAND))) {
            return player.getHeldItem(EnumHand.MAIN_HAND);
        } else {
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack itemstack = player.inventory.getStackInSlot(i);

                if (isPaper(itemstack)) {
                    return itemstack;
                }
            }

            return null;
        }
    }

    protected boolean isPaper(ItemStack stack) {
        return stack.getItem().equals(Items.PAPER);
    }

    public boolean isActive(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            stack.setTagCompound(new NBTTagCompound());
            compound = stack.getTagCompound();
        }
        if (!compound.hasKey("active")) {
            compound.setBoolean("active", false);
        }
        return compound.getBoolean("active");
    }

    public void setActive(ItemStack stack, boolean active) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            stack.setTagCompound(new NBTTagCompound());
            compound = stack.getTagCompound();
        }
        compound.setBoolean("active", active);
    }

    public String getShader(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            stack.setTagCompound(new NBTTagCompound());
            compound = stack.getTagCompound();
        }
        if (!compound.hasKey("shader")) {
            return null;
        }
        return compound.getString("shader");
    }

    public void setShader(ItemStack stack, String shader) {
        if (shader != null) {
            NBTTagCompound compound = stack.getTagCompound();
            if (compound == null) {
                stack.setTagCompound(new NBTTagCompound());
                compound = stack.getTagCompound();
            }
            compound.setString("shader", shader);
        }
    }

}

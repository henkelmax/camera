package de.maxhenkel.camera.items;

import de.maxhenkel.camera.ItemTools;
import de.maxhenkel.camera.ModSounds;
import de.maxhenkel.camera.net.MessageTakeImage;
import de.maxhenkel.camera.proxy.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.UUID;

public class ItemCamera extends Item {

    public ItemCamera() {
        setUnlocalizedName("camera");
        setRegistryName("camera");
        setMaxStackSize(1);

        setCreativeTab(CreativeTabs.DECORATIONS);

    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        UUID uuid = UUID.randomUUID();

        if (!worldIn.isRemote && playerIn instanceof EntityPlayerMP) {
            if(CommonProxy.manager.canTakeImage(playerIn.getUniqueID())){
                if(consumePaper(playerIn)){
                    worldIn.playSound(null, playerIn.getPosition(), ModSounds.take_image, SoundCategory.AMBIENT, 1.0F, 1.0F);
                    CommonProxy.simpleNetworkWrapper.sendTo(new MessageTakeImage(uuid), (EntityPlayerMP) playerIn);
                }else{
                    playerIn.sendStatusMessage(new TextComponentTranslation("message.no_paper"), true);
                }
            }else{
                playerIn.sendStatusMessage(new TextComponentTranslation("message.image_cooldown"), true);
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    private boolean consumePaper(EntityPlayer player){
        if(player.capabilities.isCreativeMode){
            return true;
        }

        ItemStack paper=findPaper(player);

        if(paper==null){
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


}

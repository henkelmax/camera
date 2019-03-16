package de.maxhenkel.camera.items;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.GuiHandler;
import de.maxhenkel.camera.proxy.CommonProxy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ItemImage extends Item {

    public ItemImage() {
        setRegistryName(new ResourceLocation(Main.MODID, "image"));
        setUnlocalizedName("image");
        setMaxDamage(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        playerIn.openGui(Main.MODID, GuiHandler.GUI_IMAGE, worldIn, (int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        String name = getOwner(stack);

        if (!name.isEmpty()) {
            tooltip.add(new TextComponentTranslation("tooltip.image_owner", TextFormatting.DARK_GRAY + name).setStyle(new Style().setColor(TextFormatting.GRAY)).getFormattedText());
        }

        long time = getTime(stack);
        if (time > 0L) {
            tooltip.add(new TextComponentTranslation("tooltip.image_time", TextFormatting.DARK_GRAY + CommonProxy.imageDateFormat.format(new Date(time))).setStyle(new Style().setColor(TextFormatting.GRAY)).getFormattedText());
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    private static NBTTagCompound getImageTag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound compound = stack.getTagCompound();

        if (!compound.hasKey("image")) {
            compound.setTag("image", new NBTTagCompound());
        }

        return compound.getCompoundTag("image");
    }

    public static void setUUID(ItemStack stack, UUID uuid) {
        NBTTagCompound compound = getImageTag(stack);

        compound.setLong("image_id_most", uuid.getMostSignificantBits());
        compound.setLong("image_id_least", uuid.getLeastSignificantBits());
    }

    public static UUID getUUID(ItemStack stack) {
        NBTTagCompound compound = getImageTag(stack);

        if (!compound.hasKey("image_id_most") || !compound.hasKey("image_id_least")) {
            return null;
        }

        long most = compound.getLong("image_id_most");
        long least = compound.getLong("image_id_least");
        return new UUID(most, least);
    }

    public static void setTime(ItemStack stack, long time) {
        NBTTagCompound compound = getImageTag(stack);
        compound.setLong("image_time", time);
    }

    public static long getTime(ItemStack stack) {
        NBTTagCompound compound = getImageTag(stack);

        if (!compound.hasKey("image_time")) {
            return 0L;
        }

        return compound.getLong("image_time");
    }

    public static void setOwner(ItemStack stack, String name) {
        NBTTagCompound compound = getImageTag(stack);
        compound.setString("owner", name);
    }

    public static String getOwner(ItemStack stack) {
        NBTTagCompound compound = getImageTag(stack);

        if (!compound.hasKey("owner")) {
            return "";
        }

        return compound.getString("owner");
    }

}

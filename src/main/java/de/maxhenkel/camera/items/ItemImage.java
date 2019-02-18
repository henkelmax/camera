package de.maxhenkel.camera.items;

import de.maxhenkel.camera.Config;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.ContainerImage;
import de.maxhenkel.camera.gui.GuiImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ItemImage extends Item {

    public ItemImage() {
        super(new Item.Properties().maxStackSize(1));
        setRegistryName("image");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (playerIn.world.isRemote) {
            playerIn.displayGui(new IInteractionObject() {
                @Override
                public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
                    return new ContainerImage();
                }

                @Override
                public String getGuiID() {
                    return Main.MODID + ":image";
                }

                @Override
                public ITextComponent getName() {
                    return new TextComponentTranslation(ItemImage.this.getTranslationKey());
                }

                @Override
                public boolean hasCustomName() {
                    return false;
                }

                @Nullable
                @Override
                public ITextComponent getCustomName() {
                    return null;
                }
            });
            openClientGui(stack);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @OnlyIn(Dist.CLIENT)
    private void openClientGui(ItemStack stack) {
        Minecraft.getInstance().displayGuiScreen(new GuiImage(stack));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        String name = getOwner(stack);

        if (!name.isEmpty()) {
            tooltip.add(new TextComponentTranslation("tooltip.image_owner", name));
        }

        long time = getTime(stack);
        if (time > 0L) {
            tooltip.add(new TextComponentTranslation("tooltip.image_time", Config.imageDateFormat.format(new Date(time))));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    private static NBTTagCompound getImageTag(ItemStack stack) {
        if (!stack.hasTag()) {
            stack.setTag(new NBTTagCompound());
        }

        NBTTagCompound compound = stack.getTag();

        if (!compound.hasKey("image")) {
            compound.setTag("image", new NBTTagCompound());
        }

        return compound.getCompound("image");
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

package de.maxhenkel.camera.items;

import de.maxhenkel.camera.Config;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.ImageScreen;
import de.maxhenkel.camera.items.render.ImageItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ImageItem extends Item {

    public ImageItem() {
        super(new Item.Properties().maxStackSize(1).setISTER(() -> ImageItemRenderer::new));
        setRegistryName(new ResourceLocation(Main.MODID, "image"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (playerIn.world.isRemote) {
            openClientGui(stack);
        }

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @OnlyIn(Dist.CLIENT)
    private void openClientGui(ItemStack stack) {
        Minecraft.getInstance().displayGuiScreen(new ImageScreen(stack));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        String name = getOwner(stack);

        if (!name.isEmpty()) {
            tooltip.add(new TranslationTextComponent("tooltip.image_owner", TextFormatting.DARK_GRAY + name).setStyle(new Style().setColor(TextFormatting.GRAY)));
        }

        long time = getTime(stack);
        if (time > 0L) {
            tooltip.add(new TranslationTextComponent("tooltip.image_time", TextFormatting.DARK_GRAY + Config.getImageDateFormat().format(new Date(time))).setStyle(new Style().setColor(TextFormatting.GRAY)));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    private CompoundNBT getImageTag(ItemStack stack) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundNBT());
        }

        CompoundNBT compound = stack.getTag();

        if (!compound.contains("image")) {
            compound.put("image", new CompoundNBT());
        }

        return compound.getCompound("image");
    }

    public void setUUID(ItemStack stack, UUID uuid) {
        CompoundNBT compound = getImageTag(stack);

        compound.putLong("image_id_most", uuid.getMostSignificantBits());
        compound.putLong("image_id_least", uuid.getLeastSignificantBits());
    }

    public UUID getUUID(ItemStack stack) {
        CompoundNBT compound = getImageTag(stack);

        if (!compound.contains("image_id_most") || !compound.contains("image_id_least")) {
            return null;
        }

        long most = compound.getLong("image_id_most");
        long least = compound.getLong("image_id_least");
        return new UUID(most, least);
    }

    public void setTime(ItemStack stack, long time) {
        CompoundNBT compound = getImageTag(stack);
        compound.putLong("image_time", time);
    }

    public long getTime(ItemStack stack) {
        CompoundNBT compound = getImageTag(stack);

        if (!compound.contains("image_time")) {
            return 0L;
        }

        return compound.getLong("image_time");
    }

    public void setOwner(ItemStack stack, String name) {
        CompoundNBT compound = getImageTag(stack);
        compound.putString("owner", name);
    }

    public String getOwner(ItemStack stack) {
        CompoundNBT compound = getImageTag(stack);

        if (!compound.contains("owner")) {
            return "";
        }

        return compound.getString("owner");
    }

}

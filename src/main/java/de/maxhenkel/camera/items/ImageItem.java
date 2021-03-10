package de.maxhenkel.camera.items;

import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.ImageScreen;
import de.maxhenkel.camera.items.render.ImageItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

public class ImageItem extends Item {

    public ImageItem() {
        super(new Item.Properties().stacksTo(1).setISTER(() -> ImageItemRenderer::new));
        setRegistryName(new ResourceLocation(Main.MODID, "image"));
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (playerIn.level.isClientSide) {
            openClientGui(stack);
        }

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @OnlyIn(Dist.CLIENT)
    private void openClientGui(ItemStack stack) {
        Minecraft.getInstance().setScreen(new ImageScreen(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        ImageData data = ImageData.fromStack(stack);
        if (data != null) {
            String name = data.getOwner();
            if (!name.isEmpty()) {
                tooltip.add(new TranslationTextComponent("tooltip.image_owner", TextFormatting.DARK_GRAY + name).withStyle(TextFormatting.GRAY));
            }
            long time = data.getTime();
            if (time > 0L) {
                tooltip.add(new TranslationTextComponent("tooltip.image_time", TextFormatting.DARK_GRAY + Main.CLIENT_CONFIG.imageDateFormat.format(new Date(time))).withStyle(TextFormatting.GRAY));
            }
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }


}
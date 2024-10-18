package de.maxhenkel.camera.items;

import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.ImageScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Date;
import java.util.List;

public class ImageItem extends Item {

    public ImageItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (playerIn.level().isClientSide) {
            openClientGui(stack);
        }

        return InteractionResult.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    private void openClientGui(ItemStack stack) {
        Minecraft.getInstance().setScreen(new ImageScreen(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        ImageData data = ImageData.fromStack(stack);
        if (data != null) {
            String name = data.getOwner();
            if (!name.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.image_owner", ChatFormatting.DARK_GRAY + name).withStyle(ChatFormatting.GRAY));
            }
            long time = data.getTime();
            if (time > 0L) {
                tooltip.add(Component.translatable("tooltip.image_time", ChatFormatting.DARK_GRAY + Main.CLIENT_CONFIG.imageDateFormat.format(new Date(time))).withStyle(ChatFormatting.GRAY));
            }
        }
        super.appendHoverText(stack, context, tooltip, flagIn);
    }

}
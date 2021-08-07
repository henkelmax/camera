package de.maxhenkel.camera.items;

import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.ImageScreen;
import de.maxhenkel.camera.items.render.ImageItemRenderer;
import de.maxhenkel.corelib.client.CustomRendererItem;
import de.maxhenkel.corelib.client.ItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

public class ImageItem extends CustomRendererItem {

    public ImageItem() {
        super(new Item.Properties().stacksTo(1));
        setRegistryName(new ResourceLocation(Main.MODID, "image"));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemRenderer createItemRenderer() {
        return new ImageItemRenderer();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (playerIn.level.isClientSide) {
            openClientGui(stack);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @OnlyIn(Dist.CLIENT)
    private void openClientGui(ItemStack stack) {
        Minecraft.getInstance().setScreen(new ImageScreen(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        ImageData data = ImageData.fromStack(stack);
        if (data != null) {
            String name = data.getOwner();
            if (!name.isEmpty()) {
                tooltip.add(new TranslatableComponent("tooltip.image_owner", ChatFormatting.DARK_GRAY + name).withStyle(ChatFormatting.GRAY));
            }
            long time = data.getTime();
            if (time > 0L) {
                tooltip.add(new TranslatableComponent("tooltip.image_time", ChatFormatting.DARK_GRAY + Main.CLIENT_CONFIG.imageDateFormat.format(new Date(time))).withStyle(ChatFormatting.GRAY));
            }
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

}
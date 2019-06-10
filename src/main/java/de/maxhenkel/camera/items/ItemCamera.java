package de.maxhenkel.camera.items;

import de.maxhenkel.camera.ItemTools;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.ModSounds;
import de.maxhenkel.camera.gui.GuiCamera;
import de.maxhenkel.camera.net.MessageTakeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.UUID;

public class ItemCamera extends Item {

    public ItemCamera() {
        super(new Item.Properties().maxStackSize(1).group(ItemGroup.DECORATIONS));
        setRegistryName(new ResourceLocation(Main.MODID, "camera"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (playerIn.isSneaking() && !isActive(stack)) {
            if (worldIn.isRemote) {
                openClientGui(getShader(stack));
            }
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        if (worldIn.isRemote || !(playerIn instanceof ServerPlayerEntity)) {
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        if (!isActive(stack)) {
            Main.CAMERA.setActive(stack, true);
        } else if (Main.PACKET_MANAGER.canTakeImage(playerIn.getUniqueID())) {
            if (consumePaper(playerIn)) {
                worldIn.playSound(null, playerIn.getPosition(), ModSounds.TAKE_IMAGE, SoundCategory.AMBIENT, 1.0F, 1.0F);
                UUID uuid = UUID.randomUUID();
                Main.SIMPLE_CHANNEL.sendTo(new MessageTakeImage(uuid), ((ServerPlayerEntity) playerIn).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
                Main.CAMERA.setActive(stack, false);
            } else {
                playerIn.sendStatusMessage(new TranslationTextComponent("message.no_paper"), true);
            }
        } else {
            playerIn.sendStatusMessage(new TranslationTextComponent("message.image_cooldown"), true);
        }


        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @OnlyIn(Dist.CLIENT)
    private void openClientGui(String currentShader) {
        Minecraft.getInstance().displayGuiScreen(new GuiCamera(currentShader));
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (!isActive(stack)) {
            return false;
        }

        if (entity instanceof PlayerEntity) {
            onItemRightClick(entity.world, (PlayerEntity) entity, Hand.MAIN_HAND);
        }
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 50000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    private boolean consumePaper(PlayerEntity player) {
        if (player.playerAbilities.isCreativeMode) {
            return true;
        }

        ItemStack paper = findPaper(player);

        if (paper == null) {
            return false;
        }

        ItemTools.decrItemStack(paper, null);
        return true;
    }

    private ItemStack findPaper(PlayerEntity player) {
        if (isPaper(player.getHeldItem(Hand.OFF_HAND))) {
            return player.getHeldItem(Hand.OFF_HAND);
        } else if (isPaper(player.getHeldItem(Hand.MAIN_HAND))) {
            return player.getHeldItem(Hand.MAIN_HAND);
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
        CompoundNBT compound = stack.getOrCreateTag();
        if (!compound.contains("active")) {
            compound.putBoolean("active", false);
        }
        return compound.getBoolean("active");
    }

    public void setActive(ItemStack stack, boolean active) {
        stack.getOrCreateTag().putBoolean("active", active);
    }

    public String getShader(ItemStack stack) {
        CompoundNBT compound = stack.getOrCreateTag();
        if (!compound.contains("shader")) {
            return null;
        }
        return compound.getString("shader");
    }

    public void setShader(ItemStack stack, String shader) {
        if (shader != null) {
            stack.getOrCreateTag().putString("shader", shader);
        }
    }

}

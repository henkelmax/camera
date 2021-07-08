package de.maxhenkel.camera;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class ServerEvents {

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (event.player.getMainHandItem().getItem().equals(Main.CAMERA) || event.player.getOffhandItem().getItem().equals(Main.CAMERA)) {
            return;
        }

        disableCamera(event.player.inventory.getSelected());

        for (ItemStack stack : event.player.inventory.items) {
            disableCamera(stack);
        }

        for (ItemStack stack : event.player.inventory.offhand) {
            disableCamera(stack);
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();
        for (Hand hand : Hand.values()) {
            ItemStack item = player.getItemInHand(hand);
            if (item.getItem().equals(Main.CAMERA) && Main.CAMERA.isActive(item)) {
                event.setUseBlock(Event.Result.DENY);
                event.setCanceled(true);
                break;
            }
        }
    }

    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        handleLeftClick(event);
    }

    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        handleLeftClick(event);
    }

    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.EntityInteract event) {
        handleLeftClick(event);
    }

    public void handleLeftClick(PlayerInteractEvent event) {
        for (Hand hand : Hand.values()) {
            ItemStack stack = event.getPlayer().getItemInHand(hand);
            if (stack.getItem().equals(Main.CAMERA) && Main.CAMERA.isActive(stack)) {
                if (event.isCancelable()) {
                    event.setCanceled(true);
                }
                event.setCancellationResult(ActionResultType.PASS);
                break;
            }
        }

    }

    @SubscribeEvent
    public void onHit(LivingAttackEvent event) {
        Entity source = event.getSource().getDirectEntity();
        if (!(source instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity player = (PlayerEntity) source;
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem().equals(Main.CAMERA) && Main.CAMERA.isActive(stack)) {
                event.setCanceled(true);
                break;
            }
        }
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        disableCamera(event.getEntityItem().getItem());
    }

    private void disableCamera(ItemStack stack) {
        if (stack.getItem().equals(Main.CAMERA)) {
            Main.CAMERA.setActive(stack, false);
        }
    }

}

package de.maxhenkel.camera;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
        if (event.player.getMainHandItem().getItem().equals(Main.CAMERA.get()) || event.player.getOffhandItem().getItem().equals(Main.CAMERA.get())) {
            return;
        }

        disableCamera(event.player.getInventory().getSelected());

        for (ItemStack stack : event.player.getInventory().items) {
            disableCamera(stack);
        }

        for (ItemStack stack : event.player.getInventory().offhand) {
            disableCamera(stack);
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getPlayer();
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack item = player.getItemInHand(hand);
            if (item.getItem().equals(Main.CAMERA.get()) && Main.CAMERA.get().isActive(item)) {
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
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = event.getPlayer().getItemInHand(hand);
            if (stack.getItem().equals(Main.CAMERA.get()) && Main.CAMERA.get().isActive(stack)) {
                if (event.isCancelable()) {
                    event.setCanceled(true);
                }
                event.setCancellationResult(InteractionResult.PASS);
                break;
            }
        }

    }

    @SubscribeEvent
    public void onHit(LivingAttackEvent event) {
        Entity source = event.getSource().getDirectEntity();
        if (!(source instanceof Player)) {
            return;
        }
        Player player = (Player) source;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem().equals(Main.CAMERA.get()) && Main.CAMERA.get().isActive(stack)) {
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
        if (stack.getItem().equals(Main.CAMERA.get())) {
            Main.CAMERA.get().setActive(stack, false);
        }
    }

}

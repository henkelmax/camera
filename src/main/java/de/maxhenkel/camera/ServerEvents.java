package de.maxhenkel.camera;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Main.MODID)
public class ServerEvents {

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Pre event) {
        if (event.getEntity().getMainHandItem().getItem().equals(Main.CAMERA.get()) || event.getEntity().getOffhandItem().getItem().equals(Main.CAMERA.get())) {
            return;
        }

        disableCamera(event.getEntity().getInventory().getSelected());

        for (ItemStack stack : event.getEntity().getInventory().items) {
            disableCamera(stack);
        }

        for (ItemStack stack : event.getEntity().getInventory().offhand) {
            disableCamera(stack);
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
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
    public static void onHit(LivingAttackEvent event) {
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
    public static void onItemToss(ItemTossEvent event) {
        disableCamera(event.getEntity().getItem());
    }

    private static void disableCamera(ItemStack stack) {
        if (stack.getItem().equals(Main.CAMERA.get())) {
            Main.CAMERA.get().setActive(stack, false);
        }
    }

}

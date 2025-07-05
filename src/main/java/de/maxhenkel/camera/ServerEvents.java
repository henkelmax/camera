package de.maxhenkel.camera;

import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = CameraMod.MODID)
public class ServerEvents {

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Pre event) {
        if (event.getEntity().getMainHandItem().getItem().equals(CameraMod.CAMERA.get()) || event.getEntity().getOffhandItem().getItem().equals(CameraMod.CAMERA.get())) {
            return;
        }

        disableCamera(event.getEntity().getInventory().getSelectedItem());

        for (int i = 0; i < event.getEntity().getInventory().getContainerSize(); i++) {
            ItemStack stack = event.getEntity().getInventory().getItem(i);
            disableCamera(stack);
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack item = player.getItemInHand(hand);
            if (item.getItem().equals(CameraMod.CAMERA.get()) && CameraMod.CAMERA.get().isActive(item)) {
                event.setUseBlock(TriState.FALSE);
                event.setCanceled(true);
                break;
            }
        }
    }

    @SubscribeEvent
    public static void onHit(LivingIncomingDamageEvent event) {
        Entity source = event.getSource().getDirectEntity();
        if (!(source instanceof Player)) {
            return;
        }
        Player player = (Player) source;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem().equals(CameraMod.CAMERA.get()) && CameraMod.CAMERA.get().isActive(stack)) {
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
        if (stack.getItem().equals(CameraMod.CAMERA.get())) {
            CameraMod.CAMERA.get().setActive(stack, false);
        }
    }

}

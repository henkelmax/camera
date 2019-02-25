package de.maxhenkel.camera;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class ServerEvents {

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (event.player.getHeldItemMainhand().getItem().equals(Main.CAMERA)) {
            return;
        }

        disableCamera(event.player.inventory.getCurrentItem());

        for (ItemStack stack : event.player.inventory.mainInventory) {
            disableCamera(stack);
        }

        for (ItemStack stack : event.player.inventory.offHandInventory) {
            disableCamera(stack);
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.getItem().equals(Main.CAMERA) && Main.CAMERA.isActive(stack)) {
            event.setUseBlock(Event.Result.DENY);
            event.setCanceled(true);
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
        ItemStack stack = event.getEntityPlayer().getHeldItemMainhand();
        if (stack.getItem().equals(Main.CAMERA) && Main.CAMERA.isActive(stack)) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            }
            event.setCancellationResult(EnumActionResult.PASS);
        }
    }

    @SubscribeEvent
    public void onHit(LivingAttackEvent event) {
        Entity source = event.getSource().getImmediateSource();
        if (!(source instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) source;

        ItemStack stack = player.getHeldItemMainhand();
        if (stack.getItem().equals(Main.CAMERA) && Main.CAMERA.isActive(stack)) {
            event.setCanceled(true);
        }
    }

    private void disableCamera(ItemStack stack) {
        if (stack.getItem().equals(Main.CAMERA)) {
            Main.CAMERA.setActive(stack, false);
        }
    }

}

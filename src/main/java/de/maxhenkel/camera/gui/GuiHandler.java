package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    public static final int GUI_IMAGE = 0;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

        if (id == GUI_IMAGE) {
            ItemStack stack=player.getHeldItem(EnumHand.MAIN_HAND);
            if(!stack.getItem().equals(ModItems.IMAGE)){
                stack=player.getHeldItem(EnumHand.OFF_HAND);
                if(!stack.getItem().equals(ModItems.IMAGE)){
                    return null;
                }
            }
            return new ContainerImage();
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

        if (id == GUI_IMAGE) {
            ItemStack stack=player.getHeldItem(EnumHand.MAIN_HAND);
            if(!stack.getItem().equals(ModItems.IMAGE)){
                stack=player.getHeldItem(EnumHand.OFF_HAND);
                if(!stack.getItem().equals(ModItems.IMAGE)){
                    return null;
                }
            }
            return new GuiImage(stack);
        }

        return null;
    }

}

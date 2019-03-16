package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.ModItems;
import de.maxhenkel.camera.entities.EntityImage;
import de.maxhenkel.camera.items.ItemCamera;
import de.maxhenkel.camera.items.ItemImage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiHandler implements IGuiHandler {

    public static final int GUI_CAMERA = 0;
    public static final int GUI_IMAGE = 1;
    public static final int GUI_RESIZE_FRAME = 2;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

        if (id == GUI_CAMERA) {
            return new ContainerCamera();
        } else if (id == GUI_IMAGE) {
            return new ContainerImage();
        } else if (id == GUI_RESIZE_FRAME) {
            return new ContainerResizeFrame();
        }

        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

        if (id == GUI_CAMERA) {
            for (EnumHand hand : EnumHand.values()) {
                ItemStack stack = player.getHeldItem(hand);
                if (stack.getItem() instanceof ItemCamera) {
                    return new GuiCamera(ModItems.CAMERA.getShader(stack));
                }
            }
        } else if (id == GUI_IMAGE) {
            for (EnumHand hand : EnumHand.values()) {
                ItemStack stack = player.getHeldItem(hand);
                if (stack.getItem() instanceof ItemImage) {
                    return new GuiImage(stack);
                }
            }
        } else if (id == GUI_RESIZE_FRAME) {
            Entity entity = Minecraft.getMinecraft().objectMouseOver.entityHit;
            if (entity instanceof EntityImage) {
                return new GuiResizeFrame(entity.getUniqueID());
            }
        }

        return null;
    }
}

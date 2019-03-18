package de.maxhenkel.camera.items;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.ContainerAlbum;
import de.maxhenkel.camera.gui.ContainerAlbumInventory;
import de.maxhenkel.camera.gui.GuiAlbum;
import de.maxhenkel.camera.inventory.InventoryAlbum;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemAlbum extends Item {

    public ItemAlbum() {
        super(new Properties().maxStackSize(1).group(ItemGroup.DECORATIONS));
        setRegistryName(new ResourceLocation(Main.MODID, "album"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (playerIn.isSneaking()) {
            if (!playerIn.world.isRemote && playerIn instanceof EntityPlayerMP) {
                NetworkHooks.openGui((EntityPlayerMP) playerIn, new IInteractionObject() {
                    @Override
                    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
                        return new ContainerAlbumInventory(playerInventory, new InventoryAlbum(stack));
                    }

                    @Override
                    public String getGuiID() {
                        return Main.MODID + ":album_inventory";
                    }

                    @Override
                    public ITextComponent getName() {
                        return new TextComponentTranslation(ItemAlbum.this.getTranslationKey());
                    }

                    @Override
                    public boolean hasCustomName() {
                        return false;
                    }

                    @Nullable
                    @Override
                    public ITextComponent getCustomName() {
                        return null;
                    }
                });
            }
        } else {
            if (playerIn.world.isRemote) {
                List<UUID> images = Main.ALBUM.getImages(stack);
                if (!images.isEmpty()) {
                    playerIn.displayGui(new IInteractionObject() {
                        @Override
                        public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
                            return new ContainerAlbum();
                        }

                        @Override
                        public String getGuiID() {
                            return Main.MODID + ":album";
                        }

                        @Override
                        public ITextComponent getName() {
                            return new TextComponentTranslation(ItemAlbum.this.getTranslationKey());
                        }

                        @Override
                        public boolean hasCustomName() {
                            return false;
                        }

                        @Nullable
                        @Override
                        public ITextComponent getCustomName() {
                            return null;
                        }
                    });
                    openClientGui(images);
                }
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @OnlyIn(Dist.CLIENT)
    private void openClientGui(List<UUID> images) {
        Minecraft.getInstance().displayGuiScreen(new GuiAlbum(images));
    }

    public List<UUID> getImages(ItemStack stack) {
        List<UUID> images = new ArrayList<>();
        IInventory inventory = new InventoryAlbum(stack);
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack s = inventory.getStackInSlot(i);
            UUID uuid = Main.IMAGE.getUUID(s);
            if (uuid == null) {
                continue;
            }
            images.add(uuid);
        }
        return images;
    }
}

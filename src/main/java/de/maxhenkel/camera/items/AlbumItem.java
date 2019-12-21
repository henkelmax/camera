package de.maxhenkel.camera.items;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.ContainerAlbumInventory;
import de.maxhenkel.camera.gui.AlbumScreen;
import de.maxhenkel.camera.inventory.AlbumInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlbumItem extends Item {

    public AlbumItem() {
        super(new Properties().maxStackSize(1).group(ItemGroup.DECORATIONS));
        setRegistryName(new ResourceLocation(Main.MODID, "album"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (playerIn.isCrouching()) {
            if (!playerIn.world.isRemote && playerIn instanceof ServerPlayerEntity) {
                NetworkHooks.openGui((ServerPlayerEntity) playerIn, new INamedContainerProvider() {

                    @Nullable
                    @Override
                    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                        return new ContainerAlbumInventory(id, playerInventory, new AlbumInventory(stack));
                    }

                    @Override
                    public ITextComponent getDisplayName() {
                        return new TranslationTextComponent(AlbumItem.this.getTranslationKey());
                    }
                });
            }
        } else {
            if (playerIn.world.isRemote) {
                List<UUID> images = Main.ALBUM.getImages(stack);
                if (!images.isEmpty()) {
                    openClientGui(images);
                }
            }
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @OnlyIn(Dist.CLIENT)
    private void openClientGui(List<UUID> images) {
        Minecraft.getInstance().displayGuiScreen(new AlbumScreen(images));
    }

    public List<UUID> getImages(ItemStack stack) {
        List<UUID> images = new ArrayList<>();
        IInventory inventory = new AlbumInventory(stack);
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

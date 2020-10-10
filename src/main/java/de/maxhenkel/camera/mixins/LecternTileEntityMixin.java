package de.maxhenkel.camera.mixins;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.AlbumContainer;
import de.maxhenkel.camera.items.AlbumItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IIntArray;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(LecternTileEntity.class)
public abstract class LecternTileEntityMixin extends TileEntity {

    @Shadow
    private ItemStack book;

    @Shadow
    private int page;

    @Shadow
    private int pages;

    @Shadow
    private IInventory inventory;

    @Shadow
    IIntArray field_214049_b;

    public LecternTileEntityMixin(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Inject(method = "hasBook", at = @At("HEAD"), cancellable = true)
    public void hasBook(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!book.isEmpty());
    }

    @Inject(method = "ensureResolved", at = @At("HEAD"), cancellable = true)
    public void ensureResolved(ItemStack stack, @Nullable PlayerEntity player, CallbackInfoReturnable<ItemStack> cir) {
        if (world instanceof ServerWorld && stack.getItem() instanceof AlbumItem) {
            cir.setReturnValue(stack);
        }
    }

    @Inject(method = "setBook(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V", at = @At("HEAD"), cancellable = true)
    public void setBook(ItemStack stack, @Nullable PlayerEntity player, CallbackInfo info) {
        if (!(stack.getItem() instanceof AlbumItem)) {
            return;
        }
        info.cancel();
        book = ensureResolved(stack, player);
        page = 0;
        pages = Main.ALBUM.getImages(book).size();
        markDirty();
    }

    @Inject(method = "createMenu", at = @At("HEAD"), cancellable = true)
    public void createMenu(int id, PlayerInventory playerInventory, PlayerEntity player, CallbackInfoReturnable<Container> cir) {
        if (!(book.getItem() instanceof AlbumItem)) {
            return;
        }
        cir.setReturnValue(new AlbumContainer(id, inventory, field_214049_b));
    }

    @Inject(method = "func_230337_a_", at = @At("TAIL"), cancellable = true)
    public void func_230337_a_(BlockState state, CompoundNBT compound, CallbackInfo info) {
        pages = Main.ALBUM.getImages(book).size();
    }

    @Shadow
    public abstract ItemStack ensureResolved(ItemStack stack, @Nullable PlayerEntity player);

}

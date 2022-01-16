package de.maxhenkel.camera.mixins;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.gui.AlbumContainer;
import de.maxhenkel.camera.items.AlbumItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(LecternBlockEntity.class)
public abstract class LecternTileEntityMixin extends BlockEntity {

    @Shadow
    ItemStack book;

    @Shadow
    int page;

    @Shadow
    private int pageCount;

    @Shadow
    @Final
    private Container bookAccess;

    @Shadow
    @Final
    private ContainerData dataAccess;

    public LecternTileEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "hasBook", at = @At("HEAD"), cancellable = true)
    public void hasBook(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!book.isEmpty());
    }

    @Inject(method = "resolveBook", at = @At("HEAD"), cancellable = true)
    public void resolveBook(ItemStack stack, @Nullable Player player, CallbackInfoReturnable<ItemStack> cir) {
        if (level instanceof ServerLevel && stack.getItem() instanceof AlbumItem) {
            cir.setReturnValue(stack);
        }
    }

    @Inject(method = "setBook(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"), cancellable = true)
    public void setBook(ItemStack stack, @Nullable Player player, CallbackInfo info) {
        if (!(stack.getItem() instanceof AlbumItem)) {
            return;
        }
        info.cancel();
        book = resolveBook(stack, player);
        page = 0;
        pageCount = Main.ALBUM.getImages(book).size();
        setChanged();
    }

    @Inject(method = "createMenu", at = @At("HEAD"), cancellable = true)
    public void createMenu(int id, Inventory playerInventory, Player player, CallbackInfoReturnable<AbstractContainerMenu> cir) {
        if (!(book.getItem() instanceof AlbumItem)) {
            return;
        }
        cir.setReturnValue(new AlbumContainer(id, bookAccess, dataAccess));
    }

    @Inject(method = "load", at = @At("TAIL"))
    public void read(CompoundTag compound, CallbackInfo info) {
        if (!(book.getItem() instanceof AlbumItem)) {
            return;
        }
        pageCount = Main.ALBUM.getImages(book).size();
    }

    @Shadow
    protected abstract ItemStack resolveBook(ItemStack stack, @Nullable Player player);

}

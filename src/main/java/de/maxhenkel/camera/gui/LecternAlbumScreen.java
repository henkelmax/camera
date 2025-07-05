package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.CameraMod;
import de.maxhenkel.camera.net.MessageAlbumPage;
import de.maxhenkel.camera.net.MessageTakeBook;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class LecternAlbumScreen extends AlbumScreen {

    private final AlbumContainer albumContainer;
    private final ContainerListener listener = new ContainerListener() {
        @Override
        public void slotChanged(AbstractContainerMenu containerToSend, int slotInd, ItemStack stack) {
            updateContents();
        }

        @Override
        public void dataChanged(AbstractContainerMenu containerIn, int varToUpdate, int newValue) {
            if (varToUpdate == 0) {
                updatePage();
            }
        }
    };

    public LecternAlbumScreen(AlbumContainer albumContainer, Inventory inv, Component titleIn) {
        super(albumContainer, inv, titleIn);
        this.albumContainer = albumContainer;
    }

    @Override
    public AlbumContainer getMenu() {
        return this.albumContainer;
    }

    @Override
    protected void init() {
        super.init();
        albumContainer.addSlotListener(listener);

        if (minecraft.player.mayBuild()) {
            addRenderableWidget(Button.builder(Component.translatable("lectern.take_book"), (button) -> {
                ClientPacketDistributor.sendToServer(new MessageTakeBook());
            }).bounds(width / 2 - 50, height - 25, 100, 20).build());
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        albumContainer.removeSlotListener(listener);
    }

    @Override
    protected void next() {
        super.next();
        sendPageUpdate(index);
    }

    @Override
    protected void previous() {
        super.previous();
        sendPageUpdate(index);
    }

    private void sendPageUpdate(int page) {
        ClientPacketDistributor.sendToServer(new MessageAlbumPage(page));
    }

    @Override
    protected void playPageTurnSound() {

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void updateContents() {
        images = CameraMod.ALBUM.get().getImages(albumContainer.getAlbum());
    }

    private void updatePage() {
        setIndex(albumContainer.getPage());
    }
}

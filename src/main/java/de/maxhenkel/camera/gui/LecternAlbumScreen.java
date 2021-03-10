package de.maxhenkel.camera.gui;

import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.net.MessageAlbumPage;
import de.maxhenkel.camera.net.MessageTakeBook;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkDirection;

public class LecternAlbumScreen extends AlbumScreen {

    private final AlbumContainer albumContainer;
    private final IContainerListener listener = new IContainerListener() {
        @Override
        public void refreshContainer(Container containerToSend, NonNullList<ItemStack> itemsList) {
            updateContents();
        }

        @Override
        public void slotChanged(Container containerToSend, int slotInd, ItemStack stack) {
            updateContents();
        }

        @Override
        public void setContainerData(Container containerIn, int varToUpdate, int newValue) {
            if (varToUpdate == 0) {
                updatePage();
            }
        }
    };

    public LecternAlbumScreen(AlbumContainer albumContainer, PlayerInventory inv, ITextComponent titleIn) {
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
            addButton(new Button(width / 2 - 50, height - 25, 100, 20, new TranslationTextComponent("lectern.take_book"), (button) -> {
                Main.SIMPLE_CHANNEL.sendTo(new MessageTakeBook(), minecraft.getConnection().getConnection(), NetworkDirection.PLAY_TO_SERVER);
            }));
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
        Main.SIMPLE_CHANNEL.sendTo(new MessageAlbumPage(page), minecraft.getConnection().getConnection(), NetworkDirection.PLAY_TO_SERVER);
    }

    @Override
    protected void playPageTurnSound() {

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void updateContents() {
        images = Main.ALBUM.getImages(albumContainer.getAlbum());
    }

    private void updatePage() {
        setIndex(albumContainer.getPage());
    }
}

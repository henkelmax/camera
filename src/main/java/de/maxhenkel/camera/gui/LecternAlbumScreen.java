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
        public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
            updateContents();
        }

        public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
            updateContents();
        }

        public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
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
    public AlbumContainer getContainer() {
        return this.albumContainer;
    }

    @Override
    protected void func_231160_c_() {
        super.func_231160_c_();
        albumContainer.addListener(listener);

        if (field_230706_i_.player.isAllowEdit()) {
            func_230480_a_(new Button(field_230708_k_ / 2 - 50, field_230709_l_ - 25, 100, 20, new TranslationTextComponent("lectern.take_book"), (button) -> {
                Main.SIMPLE_CHANNEL.sendTo(new MessageTakeBook(), field_230706_i_.getConnection().getNetworkManager(), NetworkDirection.PLAY_TO_SERVER);
            }));
        }
    }

    @Override
    public void func_231175_as__() {
        field_230706_i_.player.closeScreen();
        super.func_231175_as__();
    }

    @Override
    public void func_231164_f_() {
        super.func_231164_f_();
        albumContainer.removeListener(listener);
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
        Main.SIMPLE_CHANNEL.sendTo(new MessageAlbumPage(page), field_230706_i_.getConnection().getNetworkManager(), NetworkDirection.PLAY_TO_SERVER);
    }

    @Override
    protected void playPageTurnSound() {

    }

    @Override
    public boolean func_231177_au__() {
        return false;
    }

    private void updateContents() {
        images = Main.ALBUM.getImages(albumContainer.getAlbum());
    }

    private void updatePage() {
        setIndex(albumContainer.getPage());
    }
}

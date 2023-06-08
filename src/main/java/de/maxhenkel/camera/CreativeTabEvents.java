package de.maxhenkel.camera;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CreativeTabEvents {

    @SubscribeEvent
    public static void onCreativeModeTabBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            event.accept(new ItemStack(Main.CAMERA.get()));
            event.accept(new ItemStack(Main.ALBUM.get()));
            event.accept(new ItemStack(Main.FRAME_ITEM.get()));
        }
    }

}

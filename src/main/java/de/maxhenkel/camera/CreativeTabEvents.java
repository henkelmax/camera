package de.maxhenkel.camera;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class CreativeTabEvents {

    @SubscribeEvent
    public static void onCreativeModeTabBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            event.accept(new ItemStack(CameraMod.CAMERA.get()));
            event.accept(new ItemStack(CameraMod.ALBUM.get()));
            event.accept(new ItemStack(CameraMod.FRAME_ITEM.get()));
        }
    }

}

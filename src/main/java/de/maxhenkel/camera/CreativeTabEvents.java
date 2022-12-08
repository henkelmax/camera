package de.maxhenkel.camera;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CreativeTabEvents {

    @SubscribeEvent
    public static void onCreativeModeTabBuildContents(CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.register((flags, builder, hasPermissions) -> {
                builder.accept(new ItemStack(Main.CAMERA.get()));
                builder.accept(new ItemStack(Main.ALBUM.get()));
                builder.accept(new ItemStack(Main.FRAME_ITEM.get()));
            });
        }
    }

}

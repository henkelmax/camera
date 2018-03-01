package de.maxhenkel.camera;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class ModSounds {

    //https://www.soundjay.com/mechanical/sounds/camera-shutter-click-01.mp3
    public static SoundEvent take_image = registerSound("take_image");

    public static SoundEvent registerSound(String soundName) {
        SoundEvent event = new SoundEvent(new ResourceLocation(Main.MODID, soundName));
        event.setRegistryName(new ResourceLocation(Main.MODID, soundName));
        return event;
    }

}

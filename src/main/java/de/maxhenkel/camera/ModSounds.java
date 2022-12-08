package de.maxhenkel.camera;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Main.MODID);

    //https://www.soundjay.com/mechanical/sounds/camera-shutter-click-01.mp3
    public static final RegistryObject<SoundEvent> TAKE_IMAGE = SOUND_REGISTER.register("take_image", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Main.MODID, "take_image")));

}

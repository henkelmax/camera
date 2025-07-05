package de.maxhenkel.camera;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_REGISTER = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CameraMod.MODID);

    //https://www.soundjay.com/mechanical/sounds/camera-shutter-click-01.mp3
    public static final DeferredHolder<SoundEvent, SoundEvent> TAKE_IMAGE = SOUND_REGISTER.register("take_image", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CameraMod.MODID, "take_image")));

}

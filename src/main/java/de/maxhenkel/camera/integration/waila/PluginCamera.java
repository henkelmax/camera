package de.maxhenkel.camera.integration.waila;

import de.maxhenkel.camera.entities.ImageEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class PluginCamera implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(HUDHandlerImageFrame.INSTANCE, ImageEntity.class);
    }
}

package de.maxhenkel.camera.integration.waila;

import de.maxhenkel.camera.entities.ImageEntity;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin
public class PluginCamera implements IWailaPlugin {

    @Override
    public void register(IRegistrar registrar) {
        registrar.registerComponentProvider(HUDHandlerImageFrame.INSTANCE, TooltipPosition.HEAD, ImageEntity.class);
        registrar.registerComponentProvider(HUDHandlerImageFrame.INSTANCE, TooltipPosition.BODY, ImageEntity.class);
        registrar.registerComponentProvider(HUDHandlerImageFrame.INSTANCE, TooltipPosition.TAIL, ImageEntity.class);
    }

}

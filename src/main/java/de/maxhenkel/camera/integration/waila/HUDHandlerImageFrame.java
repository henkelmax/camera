package de.maxhenkel.camera.integration.waila;

import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.ImageEntity;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Date;

public class HUDHandlerImageFrame implements IEntityComponentProvider {

    private static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation("waila", "object_name");

    public static final HUDHandlerImageFrame INSTANCE = new HUDHandlerImageFrame();

    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        if (entityAccessor.getEntity() instanceof ImageEntity image) {
            if (entityAccessor.getTooltipPosition().equals(TooltipPosition.BODY)) {
                ImageData imageData = ImageData.fromStack(image.getItem());
                if (imageData == null) {
                    iTooltip.add(new TranslatableComponent("tooltip.image_frame_empty"));
                    return;
                }
                if (!imageData.getOwner().isEmpty()) {
                    iTooltip.add(new TranslatableComponent("tooltip.image_owner", ChatFormatting.DARK_GRAY + imageData.getOwner()).withStyle(ChatFormatting.GRAY));
                }
                if (imageData.getTime() > 0L) {
                    iTooltip.add(new TranslatableComponent("tooltip.image_time", ChatFormatting.DARK_GRAY + Main.CLIENT_CONFIG.imageDateFormat.format(new Date(imageData.getTime()))).withStyle(ChatFormatting.GRAY));
                }
            } else if (entityAccessor.getTooltipPosition().equals(TooltipPosition.HEAD)) {
                iTooltip.remove(OBJECT_NAME_TAG);
                iTooltip.add(new TextComponent(String.format(Waila.CONFIG.get().getFormatting().getEntityName(), image.getDisplayName().getString())).withStyle(ChatFormatting.WHITE));
            }
        }
    }


}
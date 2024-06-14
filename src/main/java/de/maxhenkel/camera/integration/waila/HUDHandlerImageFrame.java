package de.maxhenkel.camera.integration.waila;

import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.ImageEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

import java.util.Date;

public class HUDHandlerImageFrame implements IEntityComponentProvider {

    private static final ResourceLocation OBJECT_NAME_TAG = ResourceLocation.fromNamespaceAndPath("jade", "object_name");

    public static final HUDHandlerImageFrame INSTANCE = new HUDHandlerImageFrame();

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Main.MODID, "image_frame");

    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        if (entityAccessor.getEntity() instanceof ImageEntity image) {
            iTooltip.remove(OBJECT_NAME_TAG);
            iTooltip.add(image.getDisplayName().copy().withStyle(ChatFormatting.WHITE));
            ImageData imageData = ImageData.fromStack(image.getItem());
            if (imageData == null) {
                iTooltip.add(Component.translatable("tooltip.image_frame_empty"));
                return;
            }
            if (!imageData.getOwner().isEmpty()) {
                iTooltip.add(Component.translatable("tooltip.image_owner", ChatFormatting.DARK_GRAY + imageData.getOwner()).withStyle(ChatFormatting.GRAY));
            }
            if (imageData.getTime() > 0L) {
                iTooltip.add(Component.translatable("tooltip.image_time", ChatFormatting.DARK_GRAY + Main.CLIENT_CONFIG.imageDateFormat.format(new Date(imageData.getTime()))).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
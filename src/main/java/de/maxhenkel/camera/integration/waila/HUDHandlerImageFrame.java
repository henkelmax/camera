package de.maxhenkel.camera.integration.waila;

import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.ImageEntity;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITaggableList;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Date;
import java.util.List;

public class HUDHandlerImageFrame implements IEntityComponentProvider {

    static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation("waila", "object_name");
    static final ResourceLocation CONFIG_SHOW_REGISTRY = new ResourceLocation("waila", "show_registry");
    static final ResourceLocation REGISTRY_NAME_TAG = new ResourceLocation("waila", "registry_name");

    static final HUDHandlerImageFrame INSTANCE = new HUDHandlerImageFrame();

    @Override
    public void appendHead(List<ITextComponent> t, IEntityAccessor accessor, IPluginConfig config) {
        ITaggableList<ResourceLocation, ITextComponent> tooltip = (ITaggableList<ResourceLocation, ITextComponent>) t;
        tooltip.setTag(OBJECT_NAME_TAG, new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getEntityName(), accessor.getEntity().getDisplayName().getString())));
        if (config.get(CONFIG_SHOW_REGISTRY)) {
            tooltip.setTag(REGISTRY_NAME_TAG, new StringTextComponent(accessor.getEntity().getType().getRegistryName().toString()).withStyle(TextFormatting.GRAY));
        }
    }

    @Override
    public void appendBody(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof ImageEntity)) {
            return;
        }
        ImageEntity image = (ImageEntity) accessor.getEntity();

        ImageData imageData = ImageData.fromStack(image.getItem());
        if (imageData == null) {
            tooltip.add(new TranslationTextComponent("tooltip.image_frame_empty"));
            return;
        }
        if (!imageData.getOwner().isEmpty()) {
            tooltip.add(new TranslationTextComponent("tooltip.image_owner", TextFormatting.DARK_GRAY + imageData.getOwner()).withStyle(TextFormatting.GRAY));
        }
        if (imageData.getTime() > 0L) {
            tooltip.add(new TranslationTextComponent("tooltip.image_time", TextFormatting.DARK_GRAY + Main.CLIENT_CONFIG.imageDateFormat.format(new Date(imageData.getTime()))).withStyle(TextFormatting.GRAY));
        }
    }

    @Override
    public void appendTail(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
        tooltip.add(new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getModName(), ModIdentification.getModInfo(accessor.getEntity()).getName())));
    }

}
package de.maxhenkel.camera.integration.waila;

import de.maxhenkel.camera.Config;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.entities.ImageEntity;
import de.maxhenkel.camera.items.ImageItem;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITaggableList;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;

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
        tooltip.setTag(OBJECT_NAME_TAG, new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getEntityName(), accessor.getEntity().getDisplayName().getFormattedText())));
        if (config.get(CONFIG_SHOW_REGISTRY)){
            tooltip.setTag(REGISTRY_NAME_TAG, new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getRegistryName(), accessor.getEntity().getType().getRegistryName().toString())));
        }
    }

    @Override
    public void appendBody(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof ImageEntity)) {
            return;
        }
        ImageEntity image = (ImageEntity) accessor.getEntity();

        ItemStack imageItem = image.getItem();

        if (!(imageItem.getItem() instanceof ImageItem)) {
            tooltip.add(new TranslationTextComponent("tooltip.image_frame_empty"));
            return;
        }

        String name = Main.IMAGE.getOwner(imageItem);

        if (!name.isEmpty()) {
            tooltip.add(new TranslationTextComponent("tooltip.image_owner", TextFormatting.DARK_GRAY + name).setStyle(new Style().setColor(TextFormatting.GRAY)));
        }

        long time = Main.IMAGE.getTime(imageItem);
        if (time > 0L) {
            tooltip.add(new TranslationTextComponent("tooltip.image_time", TextFormatting.DARK_GRAY + Config.getImageDateFormat().format(new Date(time))).setStyle(new Style().setColor(TextFormatting.GRAY)));
        }
    }

    @Override
    public void appendTail(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
        tooltip.add(new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getModName(), ModIdentification.getModInfo(accessor.getEntity()).getName())));
    }
}
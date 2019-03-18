package de.maxhenkel.camera.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.network.FMLPlayMessages;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class GUIRegistry {

    private static final Map<ModContainer, Map<ResourceLocation, Function<FMLPlayMessages.OpenContainer, GuiScreen>>> GUIS = new HashMap<>();

    public static void register(ResourceLocation location, Function<FMLPlayMessages.OpenContainer, GuiScreen> function) {
        ModLoadingContext context = ModLoadingContext.get();
        ModContainer container = context.getActiveContainer();
        if (!GUIS.containsKey(container)) {
            GUIS.put(container, new HashMap<>());
            context.registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> openContainer -> GUIS.get(container).getOrDefault(openContainer.getId(), nullFunction -> null).apply(openContainer));
        }
        GUIS.get(container).put(location, function);
    }

}

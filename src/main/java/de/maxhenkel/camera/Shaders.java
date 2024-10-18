package de.maxhenkel.camera;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class Shaders {

    public static ResourceLocation BLACK_AND_WHITE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "black_and_white");
    public static ResourceLocation SEPIA = ResourceLocation.fromNamespaceAndPath(Main.MODID, "sepia");
    public static ResourceLocation DESATURATED = ResourceLocation.fromNamespaceAndPath(Main.MODID, "desaturated");
    public static ResourceLocation OVEREXPOSED = ResourceLocation.fromNamespaceAndPath(Main.MODID, "overexposed");
    public static ResourceLocation OVERSATURATED = ResourceLocation.fromNamespaceAndPath(Main.MODID, "oversaturated");
    public static ResourceLocation BLURRY = ResourceLocation.fromNamespaceAndPath(Main.MODID, "blurry");
    public static ResourceLocation INVERTED = ResourceLocation.fromNamespaceAndPath(Main.MODID, "inverted");

    private static Map<String, ResourceLocation> shaders;
    public static final List<String> SHADER_LIST;

    static {
        shaders = new HashMap<>();
        shaders.put("none", null);
        shaders.put("black_and_white", BLACK_AND_WHITE);
        shaders.put("sepia", SEPIA);
        shaders.put("desaturated", DESATURATED);
        shaders.put("overexposed", OVEREXPOSED);
        shaders.put("oversaturated", OVERSATURATED);
        shaders.put("blurry", BLURRY);
        shaders.put("inverted", INVERTED);
        SHADER_LIST = Collections.unmodifiableList(new ArrayList<>(shaders.keySet()));
    }

    public static ResourceLocation getShader(String name) {
        return shaders.get(name);
    }

}

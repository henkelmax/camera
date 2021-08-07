package de.maxhenkel.camera;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class Shaders {

    public static ResourceLocation BLACK_AND_WHITE = new ResourceLocation(Main.MODID, "shaders/black_and_white.json");
    public static ResourceLocation SEPIA = new ResourceLocation(Main.MODID, "shaders/sepia.json");
    public static ResourceLocation DESATURATED = new ResourceLocation(Main.MODID, "shaders/desaturated.json");
    public static ResourceLocation OVEREXPOSED = new ResourceLocation(Main.MODID, "shaders/overexposed.json");
    public static ResourceLocation OVERSATURATED = new ResourceLocation(Main.MODID, "shaders/oversaturated.json");
    public static ResourceLocation BLURRY = new ResourceLocation(Main.MODID, "shaders/blurry.json");
    public static ResourceLocation INVERTED = new ResourceLocation(Main.MODID, "shaders/inverted.json");

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

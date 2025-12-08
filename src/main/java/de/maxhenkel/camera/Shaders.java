package de.maxhenkel.camera;

import net.minecraft.resources.Identifier;

import java.util.*;

public class Shaders {

    public static Identifier BLACK_AND_WHITE = Identifier.fromNamespaceAndPath(CameraMod.MODID, "black_and_white");
    public static Identifier SEPIA = Identifier.fromNamespaceAndPath(CameraMod.MODID, "sepia");
    public static Identifier DESATURATED = Identifier.fromNamespaceAndPath(CameraMod.MODID, "desaturated");
    public static Identifier OVEREXPOSED = Identifier.fromNamespaceAndPath(CameraMod.MODID, "overexposed");
    public static Identifier OVERSATURATED = Identifier.fromNamespaceAndPath(CameraMod.MODID, "oversaturated");
    public static Identifier BLURRY = Identifier.fromNamespaceAndPath(CameraMod.MODID, "blurry");
    public static Identifier INVERTED = Identifier.fromNamespaceAndPath(CameraMod.MODID, "inverted");

    private static Map<String, Identifier> shaders;
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

    public static Identifier getShader(String name) {
        return shaders.get(name);
    }

}

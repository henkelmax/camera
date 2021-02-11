package de.maxhenkel.camera;

import de.maxhenkel.camera.items.ImageItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ImageData {

    private UUID id;
    private long time;
    private String owner;
    @Nullable
    private ResourceLocation biome;
    @Nullable
    private List<ResourceLocation> entities;

    private ImageData() {

    }

    public UUID getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public String getOwner() {
        return owner;
    }

    @Nullable
    public ResourceLocation getBiome() {
        return biome;
    }

    @Nullable
    public List<ResourceLocation> getEntities() {
        return entities;
    }

    @Nullable
    public static ImageData fromStack(ItemStack stack) {
        if (!(stack.getItem() instanceof ImageItem)) {
            return null;
        }
        ImageData data = new ImageData();

        UUID id = getImageID(stack);
        if (id == null) {
            return null;
        }
        data.id = id;
        data.time = getTime(stack);
        data.owner = getOwner(stack);

        if (Main.SERVER_CONFIG.advancedImageData.get()) {
            data.biome = getBiome(stack);
            data.entities = getEntities(stack);
        }

        return data;
    }

    public static ImageData create(ServerPlayerEntity player, UUID imageID) {
        ImageData data = new ImageData();
        data.id = imageID;
        data.time = System.currentTimeMillis();
        data.owner = player.getName().getUnformattedComponentText();

        if (Main.SERVER_CONFIG.advancedImageData.get()) {
            data.biome = player.world.getBiome(player.getPosition()).getRegistryName();
            data.entities = player.world.getEntitiesWithinAABB(LivingEntity.class, player.getBoundingBox().grow(128), e -> canEntityBeSeen(player, e)).stream().sorted(Comparator.comparingDouble(player::getDistance)).map(livingEntity -> livingEntity.getType().getRegistryName()).limit(Main.SERVER_CONFIG.advancedDataMaxEntities.get()).collect(Collectors.toList());
        }

        return data;
    }

    private static boolean canEntityBeSeen(ServerPlayerEntity player, Entity entity) {
        if (player == entity) {
            return false;
        }
        Vector3d playerVec = new Vector3d(player.getPosX(), player.getPosYEye(), player.getPosZ());
        Vector3d entityVec = new Vector3d(entity.getPosX(), entity.getPosYEye(), entity.getPosZ());

        Vector3d lookVecToEntity = entityVec.subtract(playerVec).normalize();
        Vector3d lookVec = player.getLookVec().normalize();

        if (angle(lookVecToEntity, lookVec) > 30D) {
            return false;
        }

        return player.world.rayTraceBlocks(new RayTraceContext(playerVec, entityVec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player)).getType() == RayTraceResult.Type.MISS;
    }

    private static double angle(Vector3d vec1, Vector3d vec2) {
        return Math.toDegrees(Math.acos(vec1.dotProduct(vec2) / (vec1.length() * vec2.length())));
    }

    public static ImageData dummy() {
        ImageData data = new ImageData();
        data.id = new UUID(0L, 0L);
        data.time = System.currentTimeMillis();
        data.owner = "Steve";
        return data;
    }

    public void addToImage(ItemStack stack) {
        if (!(stack.getItem() instanceof ImageItem)) {
            return;
        }

        setImageID(stack, id);
        setTime(stack, time);
        setOwner(stack, owner);
        if (biome != null) {
            setBiome(stack, biome);
        }
        if (entities != null) {
            setEntities(stack, entities);
        }
    }

    private static CompoundNBT getImageTag(ItemStack stack) {
        CompoundNBT compound = stack.getOrCreateTag();

        if (!compound.contains("image", Constants.NBT.TAG_COMPOUND)) {
            compound.put("image", new CompoundNBT());
        }

        return compound.getCompound("image");
    }

    private static void setImageID(ItemStack stack, UUID uuid) {
        CompoundNBT compound = getImageTag(stack);

        compound.putLong("image_id_most", uuid.getMostSignificantBits());
        compound.putLong("image_id_least", uuid.getLeastSignificantBits());
    }

    @Nullable
    public static UUID getImageID(ItemStack stack) {
        CompoundNBT compound = getImageTag(stack);

        if (!compound.contains("image_id_most", Constants.NBT.TAG_LONG) || !compound.contains("image_id_least", Constants.NBT.TAG_LONG)) {
            return null;
        }

        long most = compound.getLong("image_id_most");
        long least = compound.getLong("image_id_least");
        return new UUID(most, least);
    }

    private static void setTime(ItemStack stack, long time) {
        CompoundNBT compound = getImageTag(stack);
        compound.putLong("image_time", time);
    }

    private static long getTime(ItemStack stack) {
        CompoundNBT compound = getImageTag(stack);

        if (!compound.contains("image_time", Constants.NBT.TAG_LONG)) {
            return 0L;
        }

        return compound.getLong("image_time");
    }

    private static void setOwner(ItemStack stack, String name) {
        CompoundNBT compound = getImageTag(stack);
        compound.putString("owner", name);
    }

    private static String getOwner(ItemStack stack) {
        CompoundNBT compound = getImageTag(stack);

        if (!compound.contains("owner", Constants.NBT.TAG_STRING)) {
            return "";
        }

        return compound.getString("owner");
    }

    private static void setBiome(ItemStack stack, ResourceLocation biome) {
        CompoundNBT compound = getImageTag(stack);
        compound.putString("biome", biome.toString());
    }

    @Nullable
    private static ResourceLocation getBiome(ItemStack stack) {
        CompoundNBT compound = getImageTag(stack);
        if (!compound.contains("biome", Constants.NBT.TAG_STRING)) {
            return null;
        }
        return new ResourceLocation(compound.getString("biome"));
    }

    private static void setEntities(ItemStack stack, List<ResourceLocation> entities) {
        CompoundNBT compound = getImageTag(stack);

        ListNBT list = new ListNBT();
        for (ResourceLocation entity : entities) {
            list.add(StringNBT.valueOf(entity.toString()));
        }

        compound.put("entities", list);
    }

    @Nullable
    private static List<ResourceLocation> getEntities(ItemStack stack) {
        CompoundNBT compound = getImageTag(stack);
        if (!compound.contains("entities", Constants.NBT.TAG_LIST)) {
            return null;
        }

        ListNBT entities = compound.getList("entities", Constants.NBT.TAG_STRING);
        List<ResourceLocation> list = new ArrayList<>();
        for (INBT e : entities) {
            list.add(new ResourceLocation(e.getString()));
        }

        return list;
    }

}

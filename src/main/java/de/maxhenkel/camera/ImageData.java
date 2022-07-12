package de.maxhenkel.camera;

import de.maxhenkel.camera.items.ImageItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

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

    public static ImageData create(ServerPlayer player, UUID imageID) {
        ImageData data = new ImageData();
        data.id = imageID;
        data.time = System.currentTimeMillis();
        data.owner = player.getName().getString();

        if (Main.SERVER_CONFIG.advancedImageData.get()) {
            Biome biome = player.level.getBiome(player.blockPosition()).value();
            data.biome = ForgeRegistries.BIOMES.getKey(biome);
            data.entities = player.level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(128), e -> canEntityBeSeen(player, e)).stream().sorted(Comparator.comparingDouble(player::distanceTo)).map(ImageData::getEntityID).distinct().limit(Main.SERVER_CONFIG.advancedDataMaxEntities.get()).collect(Collectors.toList());
        }

        return data;
    }

    private static ResourceLocation getEntityID(Entity entity) {
        return ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
    }

    private static boolean canEntityBeSeen(ServerPlayer player, Entity entity) {
        if (player == entity) {
            return false;
        }
        Vec3 playerVec = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 entityVec = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());

        Vec3 lookVecToEntity = entityVec.subtract(playerVec).normalize();
        Vec3 lookVec = player.getLookAngle().normalize();

        if (angle(lookVecToEntity, lookVec) > 90D) {
            return false;
        }

        return player.level.clip(new ClipContext(playerVec, entityVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS;
    }

    private static double angle(Vec3 vec1, Vec3 vec2) {
        return Math.toDegrees(Math.acos(vec1.dot(vec2) / (vec1.length() * vec2.length())));
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

    private static CompoundTag getImageTag(ItemStack stack) {
        assert stack.getItem() instanceof ImageItem;
        CompoundTag compound = stack.getOrCreateTag();

        if (!compound.contains("image", Tag.TAG_COMPOUND)) {
            compound.put("image", new CompoundTag());
        }

        return compound.getCompound("image");
    }

    private static void setImageID(ItemStack stack, UUID uuid) {
        CompoundTag compound = getImageTag(stack);

        compound.putLong("image_id_most", uuid.getMostSignificantBits());
        compound.putLong("image_id_least", uuid.getLeastSignificantBits());
    }

    @Nullable
    public static UUID getImageID(ItemStack stack) {
        CompoundTag compound = getImageTag(stack);

        if (!compound.contains("image_id_most", Tag.TAG_LONG) || !compound.contains("image_id_least", Tag.TAG_LONG)) {
            return null;
        }

        long most = compound.getLong("image_id_most");
        long least = compound.getLong("image_id_least");
        return new UUID(most, least);
    }

    private static void setTime(ItemStack stack, long time) {
        CompoundTag compound = getImageTag(stack);
        compound.putLong("image_time", time);
    }

    private static long getTime(ItemStack stack) {
        CompoundTag compound = getImageTag(stack);

        if (!compound.contains("image_time", Tag.TAG_LONG)) {
            return 0L;
        }

        return compound.getLong("image_time");
    }

    private static void setOwner(ItemStack stack, String name) {
        CompoundTag compound = getImageTag(stack);
        compound.putString("owner", name);
    }

    private static String getOwner(ItemStack stack) {
        CompoundTag compound = getImageTag(stack);

        if (!compound.contains("owner", Tag.TAG_STRING)) {
            return "";
        }

        return compound.getString("owner");
    }

    private static void setBiome(ItemStack stack, ResourceLocation biome) {
        CompoundTag compound = getImageTag(stack);
        compound.putString("biome", biome.toString());
    }

    @Nullable
    private static ResourceLocation getBiome(ItemStack stack) {
        CompoundTag compound = getImageTag(stack);
        if (!compound.contains("biome", Tag.TAG_STRING)) {
            return null;
        }
        return new ResourceLocation(compound.getString("biome"));
    }

    private static void setEntities(ItemStack stack, List<ResourceLocation> entities) {
        CompoundTag compound = getImageTag(stack);

        ListTag list = new ListTag();
        for (ResourceLocation entity : entities) {
            list.add(StringTag.valueOf(entity.toString()));
        }

        compound.put("entities", list);
    }

    @Nullable
    private static List<ResourceLocation> getEntities(ItemStack stack) {
        CompoundTag compound = getImageTag(stack);
        if (!compound.contains("entities", Tag.TAG_LIST)) {
            return null;
        }

        ListTag entities = compound.getList("entities", Tag.TAG_STRING);
        List<ResourceLocation> list = new ArrayList<>();
        for (Tag e : entities) {
            list.add(new ResourceLocation(e.getAsString()));
        }

        return list;
    }

}

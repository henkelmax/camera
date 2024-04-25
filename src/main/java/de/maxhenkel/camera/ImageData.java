package de.maxhenkel.camera;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.maxhenkel.camera.items.ImageItem;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ImageData {

    public static final Codec<ImageData> CODEC = RecordCodecBuilder.create(i -> {
        return i.group(
                UUIDUtil.CODEC.fieldOf("id").forGetter(ImageData::getId),
                Codec.LONG.fieldOf("time").forGetter(ImageData::getTime),
                Codec.STRING.fieldOf("owner").forGetter(ImageData::getOwner),
                ResourceLocation.CODEC.optionalFieldOf("biome", null).forGetter(ImageData::getBiome),
                Codec.list(ResourceLocation.CODEC).optionalFieldOf("entities", null).forGetter(ImageData::getEntities)
        ).apply(i, ImageData::new);
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, ImageData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            ImageData::getId,
            ByteBufCodecs.VAR_LONG,
            ImageData::getTime,
            ByteBufCodecs.STRING_UTF8,
            ImageData::getOwner,
            ImageData::new
    );

    private UUID id;
    private long time;
    private String owner;
    @Nullable
    private ResourceLocation biome;
    @Nullable
    private List<ResourceLocation> entities;

    private ImageData() {

    }

    private ImageData(UUID id, long time, String owner) {
        this.id = id;
        this.time = time;
        this.owner = owner;
    }

    private ImageData(UUID id, long time, String owner, @Nullable ResourceLocation biome, @Nullable List<ResourceLocation> entities) {
        this.id = id;
        this.time = time;
        this.owner = owner;
        this.biome = biome;
        this.entities = entities;
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
        convert(stack);
        return stack.get(Main.IMAGE_DATA_COMPONENT);
    }

    public static ImageData create(ServerPlayer player, UUID imageID) {
        ImageData data = new ImageData();
        data.id = imageID;
        data.time = System.currentTimeMillis();
        data.owner = player.getName().getString();

        if (Main.SERVER_CONFIG.advancedImageData.get()) {
            Biome biome = player.level().getBiome(player.blockPosition()).value();
            data.biome = player.getServer().registryAccess().registry(Registries.BIOME).map(biomes -> biomes.getKey(biome)).orElse(null);
            data.entities = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(128), e -> canEntityBeSeen(player, e)).stream().sorted(Comparator.comparingDouble(player::distanceTo)).map(ImageData::getEntityID).distinct().limit(Main.SERVER_CONFIG.advancedDataMaxEntities.get()).collect(Collectors.toList());
        }

        return data;
    }

    private static ResourceLocation getEntityID(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
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

        return player.level().clip(new ClipContext(playerVec, entityVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS;
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
        stack.set(Main.IMAGE_DATA_COMPONENT, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImageData imageData = (ImageData) o;
        return Objects.equals(id, imageData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static void convert(ItemStack stack) {
        if (!(stack.getItem() instanceof ImageItem)) {
            return;
        }
        if (stack.has(Main.IMAGE_DATA_COMPONENT)) {
            return;
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return;
        }
        CompoundTag itemTag = customData.copyTag();
        if (!itemTag.contains("image", Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag imageTag = itemTag.getCompound("image");
        itemTag.remove("image");
        if (imageTag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(itemTag));
        }

        ImageData imageData = fromImageTag(imageTag);
        if (imageData == null) {
            return;
        }
        stack.set(Main.IMAGE_DATA_COMPONENT, imageData);
    }

    @Nullable
    public static ImageData fromImageTag(CompoundTag imageTag) {
        UUID imageID;
        if (imageTag.contains("image_id_most", Tag.TAG_LONG) && imageTag.contains("image_id_least", Tag.TAG_LONG)) {
            imageID = new UUID(imageTag.getLong("image_id_most"), imageTag.getLong("image_id_least"));
        } else {
            return null;
        }
        long time = imageTag.getLong("image_time");
        String owner = imageTag.getString("owner");
        ResourceLocation biome = null;
        if (imageTag.contains("biome", Tag.TAG_STRING)) {
            biome = new ResourceLocation(imageTag.getString("biome"));
        }
        List<ResourceLocation> entityList = null;
        if (imageTag.contains("entities", Tag.TAG_LIST)) {
            ListTag entities = imageTag.getList("entities", Tag.TAG_STRING);
            entityList = new ArrayList<>();
            for (Tag e : entities) {
                entityList.add(new ResourceLocation(e.getAsString()));
            }
        }

        ImageData data = new ImageData();
        data.id = imageID;
        data.time = time;
        data.owner = owner;
        data.biome = biome;
        data.entities = entityList;
        return data;
    }

}

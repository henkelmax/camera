package de.maxhenkel.camera;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.maxhenkel.camera.items.ImageItem;
import de.maxhenkel.corelib.codec.CodecUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
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
                ResourceLocation.CODEC.optionalFieldOf("biome").forGetter(o -> Optional.ofNullable(o.getBiome())),
                Codec.list(ResourceLocation.CODEC).optionalFieldOf("entities").forGetter(imageData -> Optional.ofNullable(imageData.getEntities())),
                ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(imageData -> Optional.ofNullable(imageData.getDimension())),
                BlockPos.CODEC.optionalFieldOf("position").forGetter(imageData -> Optional.ofNullable(imageData.getPosition()))
        ).apply(i, ImageData::new);
    });

    public static final StreamCodec<ByteBuf, Optional<ResourceLocation>> OPTIONAL_RESOURCE_LOCATION_STREAM_CODEC = CodecUtils.optionalStreamCodecByteBuf(ResourceLocation.STREAM_CODEC);
    public static final StreamCodec<ByteBuf, Optional<List<ResourceLocation>>> OPTIONAL_RESOURCE_LOCATION_LIST_STREAM_CODEC = CodecUtils.optionalStreamCodecByteBuf(CodecUtils.listStreamCodecByteBuf(ResourceLocation.STREAM_CODEC));
    public static final StreamCodec<ByteBuf, Optional<ResourceKey<Level>>> OPTIONAL_DIMENSION_STREAM_CODEC = CodecUtils.optionalStreamCodecByteBuf(ResourceKey.streamCodec(Registries.DIMENSION));
    public static final StreamCodec<ByteBuf, Optional<BlockPos>> OPTIONAL_BLOCK_POS_STREAM_CODEC = CodecUtils.optionalStreamCodecByteBuf(BlockPos.STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, ImageData> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ImageData>() {
        @Override
        public ImageData decode(RegistryFriendlyByteBuf buf) {
            return new ImageData(
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    OPTIONAL_RESOURCE_LOCATION_STREAM_CODEC.decode(buf),
                    OPTIONAL_RESOURCE_LOCATION_LIST_STREAM_CODEC.decode(buf),
                    OPTIONAL_DIMENSION_STREAM_CODEC.decode(buf),
                    OPTIONAL_BLOCK_POS_STREAM_CODEC.decode(buf)
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ImageData data) {
            UUIDUtil.STREAM_CODEC.encode(buf, data.getId());
            ByteBufCodecs.VAR_LONG.encode(buf, data.getTime());
            ByteBufCodecs.STRING_UTF8.encode(buf, data.getOwner());
            OPTIONAL_RESOURCE_LOCATION_STREAM_CODEC.encode(buf, Optional.ofNullable(data.getBiome()));
            OPTIONAL_RESOURCE_LOCATION_LIST_STREAM_CODEC.encode(buf, Optional.ofNullable(data.getEntities()));
            OPTIONAL_DIMENSION_STREAM_CODEC.encode(buf, Optional.ofNullable(data.getDimension()));
            OPTIONAL_BLOCK_POS_STREAM_CODEC.encode(buf, Optional.ofNullable(data.getPosition()));
        }
    };

    private UUID id;
    private long time;
    private String owner;
    @Nullable
    private ResourceLocation biome;
    @Nullable
    private List<ResourceLocation> entities;
    @Nullable
    private ResourceKey<Level> dimension;
    @Nullable
    private BlockPos position;

    private ImageData() {

    }

    private ImageData(UUID id, long time, String owner) {
        this.id = id;
        this.time = time;
        this.owner = owner;
    }

    private ImageData(UUID id, long time, String owner, Optional<ResourceLocation> biome, Optional<List<ResourceLocation>> entities, Optional<ResourceKey<Level>> dimension, Optional<BlockPos> position) {
        this.id = id;
        this.time = time;
        this.owner = owner;
        this.biome = biome.orElse(null);
        this.entities = entities.orElse(null);
        this.dimension = dimension.orElse(null);
        this.position = position.orElse(null);
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
    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    @Nullable
    public BlockPos getPosition() {
        return position;
    }

    @Nullable
    public static ImageData fromStack(ItemStack stack) {
        convert(stack);
        return stack.get(CameraMod.IMAGE_DATA_COMPONENT);
    }

    public static ImageData create(ServerPlayer player, UUID imageID) {
        ImageData data = new ImageData();
        data.id = imageID;
        data.time = System.currentTimeMillis();
        data.owner = player.getName().getString();

        if (CameraMod.SERVER_CONFIG.advancedImageData.get()) {
            Biome biome = player.level().getBiome(player.blockPosition()).value();
            data.biome = player.getServer().registryAccess().get(Registries.BIOME).map(Holder.Reference::value).map(biomes -> biomes.getKey(biome)).orElse(null);
            data.entities = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(128), e -> canEntityBeSeen(player, e)).stream().sorted(Comparator.comparingDouble(player::distanceTo)).map(ImageData::getEntityID).distinct().limit(CameraMod.SERVER_CONFIG.advancedDataMaxEntities.get()).collect(Collectors.toList());
            data.dimension = player.level().dimension();
            data.position = player.blockPosition();
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
        stack.set(CameraMod.IMAGE_DATA_COMPONENT, this);
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
        if (stack.has(CameraMod.IMAGE_DATA_COMPONENT)) {
            return;
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return;
        }
        CompoundTag itemTag = customData.copyTag();
        if (!itemTag.contains("image")) {
            return;
        }
        CompoundTag imageTag = itemTag.getCompoundOrEmpty("image");
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
        stack.set(CameraMod.IMAGE_DATA_COMPONENT, imageData);
    }

    @Nullable
    public static ImageData fromImageTag(CompoundTag imageTag) {
        UUID imageID;
        if (imageTag.contains("image_id_most") && imageTag.contains("image_id_least")) {
            imageID = new UUID(imageTag.getLongOr("image_id_most", 0L), imageTag.getLongOr("image_id_least", 0L));
        } else {
            return null;
        }
        long time = imageTag.getLongOr("image_time", 0L);
        String owner = imageTag.getStringOr("owner", "");
        ResourceLocation biome = null;
        if (imageTag.contains("biome")) {
            biome = ResourceLocation.tryParse(imageTag.getStringOr("biome", ""));
        }
        List<ResourceLocation> entityList = null;
        if (imageTag.contains("entities")) {
            ListTag entities = imageTag.getListOrEmpty("entities");
            entityList = new ArrayList<>();
            for (Tag e : entities) {
                Optional<String> optionalString = e.asString();
                if (optionalString.isEmpty()) {
                    continue;
                }
                ResourceLocation resourceLocation = ResourceLocation.tryParse(optionalString.get());
                if (resourceLocation != null) {
                    entityList.add(resourceLocation);
                }
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

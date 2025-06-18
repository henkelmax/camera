package de.maxhenkel.camera.entities;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public class ImageEntityRenderState extends EntityRenderState {

    public int light;
    public int frameWidth;
    public int frameHeight;
    public Direction facing;
    public ImageState imageState;
    public UUID imageEntityUUID;
    public AABB imageBoundingBox;

    public record ImageState(UUID imageId, float imageRatio, ResourceLocation resourceLocation) {
    }

}

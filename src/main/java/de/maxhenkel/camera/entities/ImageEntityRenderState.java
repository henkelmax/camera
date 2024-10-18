package de.maxhenkel.camera.entities;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.UUID;

public class ImageEntityRenderState extends EntityRenderState {

    public int light;
    public int frameWidth;
    public int frameHeight;
    public Direction facing;
    @Nullable
    public UUID imageUUID;
    public UUID imageEntityUUID;
    public AABB imageBoundingBox;

}

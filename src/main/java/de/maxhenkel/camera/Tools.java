package de.maxhenkel.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Tools {

    @OnlyIn(Dist.CLIENT)
    public static Entity getEntityLookingAt() {
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.getRenderViewEntity();
        if (entity == null) {
            return null;
        }
        if (mc.world == null) {
            return null;
        }
        double reachDistance = mc.playerController.getBlockReachDistance();

        Vector3d eyePosition = entity.getEyePosition(mc.getRenderPartialTicks());
        double reachDistanceSquared = reachDistance * reachDistance;
        Vector3d lookVec = entity.getLook(1.0F);
        Vector3d lookVecReach = eyePosition.add(lookVec.x * reachDistance, lookVec.y * reachDistance, lookVec.z * reachDistance);
        AxisAlignedBB extendedBoundingBox = entity.getBoundingBox().expand(lookVec.scale(reachDistance)).grow(1.0D, 1.0D, 1.0D);
        EntityRayTraceResult result = ProjectileHelper.rayTraceEntities(entity, eyePosition, lookVecReach, extendedBoundingBox, (entity1) -> true, reachDistanceSquared);
        if (result == null) {
            return null;
        }
        double squareDistance = eyePosition.squareDistanceTo(result.getHitVec());
        if (squareDistance > 9.0D) {
            return null;
        }
        if (squareDistance >= reachDistanceSquared) {
            return null;
        }

        return result.getEntity();
    }
}

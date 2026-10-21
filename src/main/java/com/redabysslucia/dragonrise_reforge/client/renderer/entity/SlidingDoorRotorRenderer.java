package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.entities.vehicle.SlidingDoorHelicopterEntity;
import com.redabysslucia.dragonrise_reforge.entities.vehicle.SlidingDoorHelicopterEntity.DoorSide;
import com.redabysslucia.dragonrise_reforge.entities.vehicle.SlidingDoorHelicopterEntity.DoorState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Samples the authored door clips at synchronized world time, including late tracking/load. */
@OnlyIn(Dist.CLIENT)
public class SlidingDoorRotorRenderer<T extends SlidingDoorHelicopterEntity> extends RotorVehicleRenderer<T> {
    public SlidingDoorRotorRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void transformCustomModelPart(T entity, VehicleModelInstance instance, PoseStack poseStack,
                                         float entityYaw, float partialTicks) {
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);
        var animationInstance = entity.getAnimationInstance();
        if (animationInstance == null) return;

        for (DoorSide side : DoorSide.values()) {
            if (entity.getDoorState(side) == DoorState.CLOSED) continue;
            var bone = instance.getBone(side.bone);
            var animation = animationInstance.getContext().getAnimations().get(entity.getDoorAnimation(side));
            if (bone == null || animation == null) continue;

            // These clips animate only position. Apply to this door, preserving rotor/other animations.
            var pose = animation.evaluate(entity.getDoorAnimationSeconds(side, partialTicks));
            for (var transform : pose.getBoneTransforms()) {
                if (transform.boneIndex() != bone.index()) continue;
                var translation = transform.translation();
                bone.x += translation.x();
                bone.y += translation.y();
                bone.z += translation.z();
            }
        }
    }
}

package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.client.animation.VehicleTrackAnimation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import java.util.WeakHashMap;
import java.util.Map;

/** Independent left/right running belts, with one or more closed loops on each side. */
public class AnimatedTrackedVehicleRenderer<T extends VehicleEntity> extends GeoVehicleRenderer<T> {
    private final String profileId;
    private final Map<VehicleModelInstance, VehicleTrackAnimation.Rig> rigs = new WeakHashMap<>();
    private int generation = -1;

    public AnimatedTrackedVehicleRenderer(EntityRendererProvider.Context context, String profileId) {
        super(context);
        this.profileId = profileId;
    }

    @Override
    public void transformCustomModelPart(T entity, VehicleModelInstance model, PoseStack poseStack,
                                         float entityYaw, float partialTicks) {
        super.transformCustomModelPart(entity, model, poseStack, entityYaw, partialTicks);
        if (generation != VehicleTrackAnimation.generation()) {
            rigs.clear();
            generation = VehicleTrackAnimation.generation();
        }
        VehicleTrackAnimation.Rig rig = rigs.computeIfAbsent(model, instance ->
                new VehicleTrackAnimation.Rig(instance, VehicleTrackAnimation.profile(trackProfileId(instance))));
        rig.apply(getLeftWheelRot(), getRightWheelRot());
    }

    protected String trackProfileId(VehicleModelInstance model) {
        return profileId;
    }
}

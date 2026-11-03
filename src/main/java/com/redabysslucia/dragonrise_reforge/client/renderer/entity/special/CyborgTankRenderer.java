package com.redabysslucia.dragonrise_reforge.client.renderer.entity.special;

import com.redabysslucia.dragonrise_reforge.client.renderer.entity.AnimatedTrackedVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.special.CyborgTankEntity;
import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public class CyborgTankRenderer extends AnimatedTrackedVehicleRenderer<CyborgTankEntity> {
        public CyborgTankRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, "cyborg_tank");
        }

        @Override
        public void transformCustomModelPart(CyborgTankEntity entity, VehicleModelInstance instance,
                                             PoseStack poseStack, float entityYaw, float partialTicks) {
                super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);
                var yaw = instance.getBone("passengerWeaponStationYaw");
                // This station is attached to the hull, so its yaw does not subtract the turret yaw.
                if (yaw != null) {
                        yaw.rotation.rotationY(Mth.lerp(partialTicks, entity.getGunYRotO(),
                                entity.getGunYRot()) * Mth.DEG_TO_RAD);
                }
                var pitch = instance.getBone("passengerWeaponStationPitch");
                if (pitch != null) {
                        pitch.rotation.rotationX(-entity.getCompensatedGunPitch(partialTicks) * Mth.DEG_TO_RAD);
                }
        }
}

package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BakedModelInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.entities.DarkbearEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public class DarkbearRenderer extends AnimatedTrackedVehicleRenderer<DarkbearEntity> {
        public DarkbearRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, "darkbear");
        }

        @Override
        public void transformCustomModelPart(DarkbearEntity entity, VehicleModelInstance instance,
                                             PoseStack poseStack, float entityYaw, float partialTicks) {
                super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);
                applySubcannonPitch(instance,
                        Mth.lerp(partialTicks, entity.getTurretXRotO(), entity.getTurretXRot()));
        }

        /** Match DarkbearEntity's Turret * pitch transform at the modeled turret pivot. */
        public static void applySubcannonPitch(BakedModelInstance instance, float pitchDegrees) {
                var subcannon = instance.getBone("subcannon");
                var turret = instance.getBone("turret");
                if (subcannon == null || turret == null) return;

                float angle = -pitchDegrees * Mth.DEG_TO_RAD;
                float dy = subcannon.definition().pivotY() - turret.definition().pivotY();
                float dz = subcannon.definition().pivotZ() - turret.definition().pivotZ();
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);
                // SBM rotates around the subcannon's authored pivot. Move that
                // pivot along the turret-centred arc so the whole group follows
                // the same rotation as the entity's shell spawning transform.
                subcannon.y += (dy * cos - dz * sin - dy) * 16f;
                subcannon.z += (dy * sin + dz * cos - dz) * 16f;
                subcannon.rotation.rotationX(angle);
        }
}

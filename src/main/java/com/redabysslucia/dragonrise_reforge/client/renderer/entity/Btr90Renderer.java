package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.entities.Btr90Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public class Btr90Renderer extends GeoVehicleRenderer<Btr90Entity> {
    public Btr90Renderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void transformCustomModelPart(Btr90Entity entity, VehicleModelInstance instance,
                                         PoseStack stack, float yaw, float partialTick) {
        super.transformCustomModelPart(entity, instance, stack, yaw, partialTick);
        var rack = instance.getBone("move_missileRack");
        if (rack != null) {
            rack.rotation.rotationX(entity.getMissileRackElevation(partialTick) * Mth.DEG_TO_RAD);
        }
        var gun = entity.getGunData("Missile");
        int ammo = gun == null ? 0 : gun.ammo.get();
        for (int i=1; i<=4; i++) {
            var cap = instance.getBone("move_missileCover" + i);
            if (cap != null) cap.visible = ammo >= i;
        }
    }
}

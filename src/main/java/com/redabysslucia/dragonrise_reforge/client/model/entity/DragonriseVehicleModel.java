package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.animatable.GeoAnimatable;

public abstract class DragonriseVehicleModel<T extends VehicleEntity & GeoAnimatable> extends VehicleModel<T> {

    @Override
    public ResourceLocation getModelResource(T vehicle) {
        ResourceLocation precise = getPreciseModelResource(vehicle);
        if (precise != null) {
            if ("superbwarfare".equals(precise.getNamespace())) {
                return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", precise.getPath());
            }
            return precise;
        }
        ResourceLocation original = super.getModelResource(vehicle);
        if (original != null) {
            if ("superbwarfare".equals(original.getNamespace())) {
                return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", original.getPath());
            }
            return original;
        }
        String key = EntityType.getKey(vehicle.getType()).getPath();
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "geo/" + key + ".geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(T vehicle) {
        // 优先使用显式动画映射
        ResourceLocation original = super.getAnimationResource(vehicle);
        if (original != null) {
            if ("superbwarfare".equals(original.getNamespace())) {
                return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", original.getPath());
            }
            return original;
        }
        // 无显式映射时，基于entityType动态生成路径
        String key = EntityType.getKey(vehicle.getType()).getPath();
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "animations/" + key + ".animation.json");
    }

    @Override
    public ResourceLocation getTextureResource(T vehicle) {
        ResourceLocation precise = getPreciseTextureResource(vehicle);
        if (precise != null) {
            if ("superbwarfare".equals(precise.getNamespace())) {
                return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", precise.getPath());
            }
            return precise;
        }
        ResourceLocation original = super.getTextureResource(vehicle);
        if (original != null) {
            if ("superbwarfare".equals(original.getNamespace())) {
                return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", original.getPath());
            }
            return original;
        }
        String key = EntityType.getKey(vehicle.getType()).getPath();
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/entity/" + key + ".png");
    }
}

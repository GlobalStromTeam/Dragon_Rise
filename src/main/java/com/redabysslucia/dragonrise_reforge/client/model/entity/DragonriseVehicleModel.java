package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.core.animatable.GeoAnimatable;

public abstract class DragonriseVehicleModel<T extends VehicleEntity & GeoAnimatable> extends VehicleModel<T> {

    @Override
    public ResourceLocation getModelResource(T vehicle) {
        // 优先使用显式模型映射，跳过 LOD 回退路径（回退路径总是基于entityType生成路径，忽略了显式映射）
        ResourceLocation precise = getPreciseModelResource(vehicle);
        if (precise != null) {
            if ("superbwarfare".equals(precise.getNamespace())) {
                return new ResourceLocation("dragonrise_reforge", precise.getPath());
            }
            return precise;
        }
        // 无显式映射时，回退到父类逻辑（LOD + 动态路径生成 + namespace修正）
        ResourceLocation original = super.getModelResource(vehicle);
        if (original != null && "superbwarfare".equals(original.getNamespace())) {
            return new ResourceLocation("dragonrise_reforge", original.getPath());
        }
        return original;
    }

    @Override
    public ResourceLocation getAnimationResource(T vehicle) {
        // 优先使用显式动画映射
        ResourceLocation original = super.getAnimationResource(vehicle);
        if (original != null) {
            if ("superbwarfare".equals(original.getNamespace())) {
                return new ResourceLocation("dragonrise_reforge", original.getPath());
            }
            return original;
        }
        // 无显式映射时，基于entityType动态生成路径
        String key = EntityType.getKey(vehicle.getType()).getPath();
        return new ResourceLocation("dragonrise_reforge", "animations/" + key + ".animation.json");
    }

    @Override
    public ResourceLocation getTextureResource(T vehicle) {
        // 优先使用显式纹理映射，跳过 LOD 回退路径
        ResourceLocation precise = getPreciseTextureResource(vehicle);
        if (precise != null) {
            if ("superbwarfare".equals(precise.getNamespace())) {
                return new ResourceLocation("dragonrise_reforge", precise.getPath());
            }
            return precise;
        }
        // 无显式映射时，回退到父类逻辑
        ResourceLocation original = super.getTextureResource(vehicle);
        if (original != null && "superbwarfare".equals(original.getNamespace())) {
            return new ResourceLocation("dragonrise_reforge", original.getPath());
        }
        return original;
    }
}

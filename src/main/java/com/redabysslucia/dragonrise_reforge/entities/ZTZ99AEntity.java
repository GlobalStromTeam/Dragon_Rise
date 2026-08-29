package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.utils.IVehicleBackground;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@SuppressWarnings("removal")
public class ZTZ99AEntity extends VehicleEntity implements IVehicleBackground {

    private final Float[][] PitchAdjustments = {
            {180f, 180f, 180f, 4f, -4f},
            {-180f, -180f, 180f, 4f, -4f},
    };

    public ZTZ99AEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.3f) * damage);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getBackgroundTexture() {
        var mc = net.minecraft.client.Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return null;

        var seatIndex = getSeatIndex(player);
        // 圆形炮镜跟随 JSON 中配置的主炮控制位
        if (seatIndex == getTurretControllerIndex()) {
            return ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "textures/overlay/vehicle/hud/testcroos3.png");
        }
        // 二号位保留原有矩形观察窗；若同时是主炮控制位，优先显示圆形炮镜
        else if (seatIndex == 1) {
            return ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "textures/overlay/vehicle/hud/testcroos2.png");
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderBackground() {
        var mc = net.minecraft.client.Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return false;

        var seatIndex = getSeatIndex(player);
        return seatIndex == getTurretControllerIndex() || seatIndex == 1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getBackgroundAlpha() {
        // 控制背景透明度 (0.0f - 1.0f)
        return 1.0f;
    }
}

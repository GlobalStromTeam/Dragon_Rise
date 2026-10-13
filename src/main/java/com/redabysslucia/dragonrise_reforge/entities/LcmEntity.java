package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * LCM 机械化登陆艇（铁质小艇）：EngineType=Ship，靠 Buoyancy 浮于水面。
 * 两个座位都在车体坐标系下（Transform=Vehicle + RotateWithVehicle）：一号位艇长兼艏部 .50 机枪，二号位另一侧 .50 机枪。
 * 模型已由脚本做 180° 转向，使船头对齐 SBW 约定（模型 -Z = 游戏前方）。
 */
@SuppressWarnings("removal")
public class LcmEntity extends DragonriseVehicleBase {

    public LcmEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public float getTurretMaxHealth() {
        return 60;
    }

    @Override
    public float getWheelMaxHealth() {
        return 80;
    }

    @Override
    public float getEngineMaxHealth() {
        return 90;
    }
}

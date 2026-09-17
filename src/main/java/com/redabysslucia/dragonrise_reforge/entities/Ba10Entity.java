package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * BA-10 重型装甲汽车（6×4 轮式底盘 + 37mm 炮塔，单座位：驾驶兼炮手，武器 Cannon + 同轴 DT 机枪）。
 */
@SuppressWarnings("removal")
public class Ba10Entity extends DragonriseVehicleBase {

    public Ba10Entity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public float getTurretMaxHealth() {
        return 60;
    }

    @Override
    public float getWheelMaxHealth() {
        return 50;
    }

    @Override
    public float getEngineMaxHealth() {
        return 80;
    }
}

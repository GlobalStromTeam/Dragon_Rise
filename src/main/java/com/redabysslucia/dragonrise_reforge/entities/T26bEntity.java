package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * T-26B 轻型坦克（履带底盘 + 45mm 20-K 炮，单座位：驾驶兼炮手，武器 Cannon + 同轴 DT 机枪）。
 */
@SuppressWarnings("removal")
public class T26bEntity extends DragonriseVehicleBase {

    public T26bEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public int getTrackAnimationLength() {
        return 80;
    }

    @Override
    public float getTurretMaxHealth() {
        return 70;
    }

    @Override
    public float getWheelMaxHealth() {
        return 60;
    }

    @Override
    public float getEngineMaxHealth() {
        return 90;
    }
}

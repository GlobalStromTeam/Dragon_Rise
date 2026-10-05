package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * K1A1 主战坦克（韩国）：120mm 滑膛炮 + 7.62mm 同轴机枪，车长席为 K6(.50) 遥控武器站。
 * 模型骨骼已规范化为 SBW 约定：cannon→barrel；右侧轮 wheelL8/10..16→wheelR0..7；
 * 车长机枪 M2→passengerWeaponStationPitch，并补 passengerWeaponStation / ...Yaw 形成遥控武器站链条。
 */
@SuppressWarnings("removal")
public class K1a1Entity extends DragonriseVehicleBase {

    public K1a1Entity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public int getTrackAnimationLength() {
        return 80;
    }

    @Override
    public float getTurretMaxHealth() {
        return 150;
    }

    @Override
    public float getWheelMaxHealth() {
        return 130;
    }

    @Override
    public float getEngineMaxHealth() {
        return 160;
    }
}

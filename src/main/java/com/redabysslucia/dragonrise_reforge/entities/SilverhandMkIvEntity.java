package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Silverhand Mk IV 重型坦克。
 * <p>
 * 座位1 = 驾驶员兼主炮手（Transform=Turret，105mm 主炮绑定 turret/barrel 骨骼）；
 * 座位2 = 车体副炮手（Transform=Vehicle，75mm 车体炮绑定 turret_1 骨骼，随该座位乘员视线独立转动）。
 */
@SuppressWarnings("removal")
public class SilverhandMkIvEntity extends DragonriseVehicleBase {

    public SilverhandMkIvEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public int getTrackAnimationLength() {
        return 80;
    }

    @Override
    public float getTurretMaxHealth() {
        return 140;
    }

    @Override
    public float getWheelMaxHealth() {
        return 110;
    }

    @Override
    public float getEngineMaxHealth() {
        return 140;
    }
}

package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * KV-2 重型坦克（履带底盘 + 152mm M-10 榴弹炮，单座位：驾驶兼炮手，武器 Cannon + 同轴 DT 机枪）。
 * 炮塔转速极慢（TurretTurnSpeed 0.12/0.2），主炮装填 300 tick。
 */
@SuppressWarnings("removal")
public class Kv2Entity extends DragonriseVehicleBase {

    public Kv2Entity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public int getTrackAnimationLength() {
        return 80;
    }

    @Override
    public float getTurretMaxHealth() {
        return 160;
    }

    @Override
    public float getWheelMaxHealth() {
        return 130;
    }

    @Override
    public float getEngineMaxHealth() {
        return 170;
    }
}

package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * 哥萨克-5（Kozak-5，乌克兰 4×4 轮式装甲车 / MRAP）。
 * 车顶 12.7mm 高射机枪由 `turret` + `barrel` 骨骼驱动，0 号座位控制炮塔。
 */
@SuppressWarnings("removal")
public class Kozak5Entity extends DragonriseVehicleBase {

    public Kozak5Entity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public float getTurretMaxHealth() {
        return 70;
    }

    @Override
    public float getWheelMaxHealth() {
        return 80;
    }

    @Override
    public float getEngineMaxHealth() {
        return 100;
    }
}

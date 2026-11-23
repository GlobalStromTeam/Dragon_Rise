package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * 卡玛斯 台风-K（KamAZ Typhoon-K，俄罗斯 6×6 防雷反伏击车 / 装甲输送车）。
 * <p>
 * 本车为纯运兵型，无固定武器；6 个轮子已按 SBW 约定改名：
 * {@code wheelL0Turn}/{@code wheelR0Turn}（前轴可转向）、{@code wheelL1}/{@code wheelR1}（中轴）、
 * {@code wheelL2}/{@code wheelR2}（后轴）。
 */
@SuppressWarnings("removal")
public class KamazTyphoonEntity extends DragonriseVehicleBase {

    public KamazTyphoonEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public float getTurretMaxHealth() {
        return 120;
    }

    @Override
    public float getWheelMaxHealth() {
        return 90;
    }

    @Override
    public float getEngineMaxHealth() {
        return 140;
    }
}

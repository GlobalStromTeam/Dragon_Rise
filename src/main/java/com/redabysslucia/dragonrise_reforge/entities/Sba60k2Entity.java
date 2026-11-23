package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * SBA-60K2 布拉特（Bulat，俄罗斯 6×6 轮式装甲车）。
 * <p>
 * 纯运兵型，无固定武器；8 个座位由模型里的 {@code SeatsPos1~8} 标记生成。
 * 轮子骨骼已按作者自己的 {@code WheelLeft/RightObb} 重建：
 * {@code wheelL0Turn}/{@code wheelR0Turn}（前轴可转向）、{@code wheelL1/R1}（中轴）、{@code wheelL2/R2}（后轴）。
 */
@SuppressWarnings("removal")
public class Sba60k2Entity extends DragonriseVehicleBase {

    public Sba60k2Entity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public float getTurretMaxHealth() {
        return 80;
    }

    @Override
    public float getWheelMaxHealth() {
        return 85;
    }

    @Override
    public float getEngineMaxHealth() {
        return 110;
    }
}

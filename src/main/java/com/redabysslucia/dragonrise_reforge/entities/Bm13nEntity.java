package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * BM-13N 喀秋莎（Katyusha）多管火箭炮卡车。
 * <p>
 * 武器：16 发 132mm M-13 火箭，弹匣 16；模型的静态火箭已改为 {@code dummy_0_0_1..16} 空挂点，
 * 由 SBW 渲染器按 <b>剩余弹药数</b> 在每根 dummy 上绘制实体火箭（{@code ProjectileDummyInfo} +
 * {@code ShootPos.BoundUpWithAmmoAmount}），所以每打一发就少渲染一颗。
 * <p>
 * 轮子骨骼已按作者自己的 {@code WheelLeft/RightObb} 重新归位并改名为 SBW 约定：
 * {@code wheelL0Turn}/{@code wheelR0Turn}（前轴可转向）、{@code wheelL1/L2}/{@code wheelR1/R2}（中/后轴）。
 */
@SuppressWarnings("removal")
public class Bm13nEntity extends DragonriseVehicleBase {

    public Bm13nEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public float getTurretMaxHealth() {
        return 40;
    }

    @Override
    public float getWheelMaxHealth() {
        return 45;
    }

    @Override
    public float getEngineMaxHealth() {
        return 60;
    }
}

package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * F-15E 攻击鹰：双座战斗机。
 * 继承 DragonriseVehicleBase 获得起落架（gear_up/gear_down）与加力（engine_on/engine_off）状态机。
 */
public class F15eEntity extends DragonriseVehicleBase {

    public F15eEntity(EntityType<F15eEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.25f) * damage * (getHealth() > 0.1f ? 0.4f : 0.05f));
    }

    @Override
    public double getMouseSensitivity() {
        return com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle ? 0.1 : 0.25;
    }
}

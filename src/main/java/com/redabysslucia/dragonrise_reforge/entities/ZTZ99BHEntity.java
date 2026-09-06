package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ZTZ99BHEntity extends DragonriseVehicleBase {
        public ZTZ99BHEntity(EntityType<?> pEntityType, Level pLevel) {
                super(pEntityType, pLevel);
        }
        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((entity, source, damage) -> getSourceAngle(source, 0.3f) * damage);
        }
@Override
        public int getTrackAnimationLength() {
                return 80;
        }

        @Override
        public float getTurretMaxHealth() {
                return 100;
        }

        @Override
        public float getWheelMaxHealth() {
                return 100;
        }

        @Override
        public float getEngineMaxHealth() {
                return 150;
        }
}

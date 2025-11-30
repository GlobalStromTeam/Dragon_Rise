package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class AA625EEntity extends GeoVehicleEntity {
        public AA625EEntity(EntityType<?> pEntityType, Level pLevel) {
                super(pEntityType, pLevel);
        }
        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((source, damage) -> getSourceAngle(source, 0.3f) * damage);
        }

        @Override
        public float getTurretMaxHealth() {
                return 100;
        }

        @Override
        public float getWheelMaxHealth() {
                return 25;
        }

        @Override
        public float getEngineMaxHealth() {
                return 50;
        }
}

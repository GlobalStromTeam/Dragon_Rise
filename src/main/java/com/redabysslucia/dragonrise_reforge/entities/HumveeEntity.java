package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class HumveeEntity extends GeoVehicleEntity {

        public HumveeEntity(EntityType<HumveeEntity> type, Level world) {
                super(type, world);
        }

        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((source, damage) -> getSourceAngle(source, 0.05f) * damage);
        }

}
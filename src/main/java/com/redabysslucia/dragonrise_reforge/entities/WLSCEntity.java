package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class WLSCEntity extends GeoVehicleEntity {

        public WLSCEntity(EntityType<WLSCEntity> type, Level world) {
                super(type, world);
        }

        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((entity, source, damage) -> getSourceAngle(source, 0.05f) * damage);
        }


}

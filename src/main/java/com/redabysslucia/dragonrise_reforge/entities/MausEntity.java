package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import com.redabysslucia.dragonrise_reforge.entities.utils.SyncCameraVehicle;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class MausEntity extends SyncCameraVehicle {
    public MausEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.2f) * damage);
    }

    @Override
    public int getTrackAnimationLength() {
        return 80;
    }

    @Override
    public float getTurretMaxHealth() {
        return 180;
    }

    @Override
    public float getWheelMaxHealth() {
        return 150;
    }

    @Override
    public float getEngineMaxHealth() {
        return 220;
    }
}

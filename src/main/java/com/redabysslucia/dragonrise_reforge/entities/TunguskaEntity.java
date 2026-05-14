package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.entities.utils.NightVisionVehicle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class TunguskaEntity extends NightVisionVehicle {

    public TunguskaEntity(EntityType<TunguskaEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
            .custom((source, damage) -> getSourceAngle(source, 0.25f) * damage);
    }

    @Override
    public ResourceLocation getNightVisionShader() {
        return new ResourceLocation("shaders/post/night-vision-wp.json");
    }
}

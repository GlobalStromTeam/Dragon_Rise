package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.mojang.math.Axis;
import com.redabysslucia.dragonrise_reforge.entities.utils.FireLightVisionVehicle;
import com.redabysslucia.dragonrise_reforge.entities.utils.SyncCameraVehicle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Matrix4d;
import org.joml.Vector4d;

@SuppressWarnings("removal")
public class M4A2Entity extends SyncCameraVehicle {
    public M4A2Entity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((source, damage) -> getSourceAngle(source, 0.3f) * damage);
    }

//    private PlayState cannonFirePredicate(AnimationState<PANZER4Entity> event) {
//        if (getShootAnimationTimer(0, 0) > 0) {
//            return event.setAndContinue(RawAnimation.begin().thenPlay("ztq15.fire.animation"));
//        }
//        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.ztq15.idle"));
//    }
//
//    @Override
//    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
//        data.add(new AnimationController<>(this, "cannon", 0, this::cannonFirePredicate));
//    }

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

package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.entities.utils.NightVisionVehicle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

@SuppressWarnings("removal")
public class ZTZ59AEntity extends NightVisionVehicle {
    public ZTZ59AEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((source, damage) -> getSourceAngle(source, 0.3f) * damage);
    }

//    private PlayState cannonFirePredicate(AnimationState<ZTZ59AEntity> event) {
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
//
    @Override
    public ResourceLocation getNightVisionShader() {
        return new ResourceLocation("shaders/post/night-vision-wp.json");
    }
//
//    @Override
//    public int getTrackAnimationLength() {
//        return 80;
//    }
//
//    @Override
//    public float getTurretMaxHealth() {
//        return 100;
//    }
//
//    @Override
//    public float getWheelMaxHealth() {
//        return 100;
//    }
//
//    @Override
//    public float getEngineMaxHealth() {
//        return 150;
//    }
}

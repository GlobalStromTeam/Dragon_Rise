package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.entities.utils.FireLightVisionVehicle;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;


import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;

public class SU24Entity extends FireLightVisionVehicle {

    public SU24Entity(EntityType<SU24Entity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.25f) * damage * (getHealth() > 0.1f ? 0.4f : 0.05f));
    }

    private PlayState F14A(AnimationState<SU24Entity> event) {
        if (this.onGround()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.f14.ground_idle"));
        }
        else {
        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                ("animation.f14.fly_idle"));
        }
    }

    private PlayState F14B(AnimationState<SU24Entity> event) {
        if (this.sprintInputDown()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.f14.sprinting_on"));
        }
        else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.f14.sprinting_off"));
        }
    }

    private PlayState F14C(AnimationState<SU24Entity> event) {
        if (this.getPower()>= 0.6) {//这个值是节流阀我操
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.f14.highspeed_on"));
        }
        else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.f14.highspeed_off"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "f14a", 0, this::F14A));
        data.add(new AnimationController<>(this, "f14b", 0, this::F14B));
        data.add(new AnimationController<>(this, "f14c", 0, this::F14C));
    }
    //    @Override
//    public boolean useAircraftCamera(int seatIndex) {
//        return ModKeyMappings.FREE_CAMERA.isDown() && !ClientEventHandler.zoom;
//    }

    @Override
    public double getMouseSensitivity() {
        return zoomVehicle ? 0.1 : 0.25;
    }
}

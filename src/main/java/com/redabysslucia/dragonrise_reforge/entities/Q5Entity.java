package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;

public class Q5Entity extends GeoVehicleEntity {

    public Q5Entity(EntityType<Q5Entity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((source, damage) -> getSourceAngle(source, 0.25f) * damage * (getHealth() > 0.1f ? 0.4f : 0.05f));
    }

    private PlayState Q5A(AnimationState<Q5Entity> event) {
        if (this.onGround()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.q5.ground_idle"));
        }
        else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.q5.fly_idle"));
        }
    }

    //private PlayState Q5B(AnimationState<Q5Entity> event) {
    //    if (this.sprintInputDown()) {
    //        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
    //           ("animation.q5.sb1"));
    //    }
    //   else {
    //       return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
    //                ("animation.q5.sb1"));
    //    }
    //}

    private PlayState Q5C(AnimationState<Q5Entity> event) {
        if (this.getPower()>= 0.7) {//这个值是节流阀我操
            return event.setAndContinue(RawAnimation.begin().thenLoop
                    ("animation.q5.sb1"));
        }
        else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.q5.sb1"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "q5a", 0, this::Q5A));
        //data.add(new AnimationController<>(this, "q5b", 0, this::Q5B));
        data.add(new AnimationController<>(this, "q5c", 0, this::Q5C));
    }
    //    @Override
//    public boolean useAircraftCamera(int seatIndex) {
//        return ModKeyMappings.FREE_CAMERA.isDown() && !ClientEventHandler.zoom;
//    }

    @Override
    public double getMouseSensitivity() {
        return zoomVehicle ? 0.03 : 0.07;
    }
}

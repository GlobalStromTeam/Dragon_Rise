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

public class J16Entity extends FireLightVisionVehicle {

    public J16Entity(EntityType<J16Entity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.25f) * damage * (getHealth() > 0.1f ? 0.4f : 0.05f));
    }

    private PlayState J16A(AnimationState<J16Entity> event) {
        if (this.onGround()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.j-16.qljon"));
        }
        else {
        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                ("animation.j-16.qljoff"));
        }
    }

    private PlayState J16B(AnimationState<J16Entity> event) {
        if (this.getPower()>= 0.7) {//这个值是节流阀我操
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.j-16.jiyqon"));
        }
        else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.j-16.jiyqoff"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "f14a", 0, this::J16A));
        data.add(new AnimationController<>(this, "f14c", 0, this::J16B));
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

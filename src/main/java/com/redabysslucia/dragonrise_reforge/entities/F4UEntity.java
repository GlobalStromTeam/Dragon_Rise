package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;

public class F4UEntity extends GeoVehicleEntity {

    public F4UEntity(EntityType<F4UEntity> type, Level world) {
        super(type, world);
    }

    private PlayState F4U(AnimationState<F4UEntity> event) {
        if (this.onGround()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("off"));
        }
        else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("on"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "f4u", 0, this::F4U));
    }
    @Override
    public double getMouseSensitivity() {
        return zoomVehicle ? 0.1 : 0.25;
    }
}

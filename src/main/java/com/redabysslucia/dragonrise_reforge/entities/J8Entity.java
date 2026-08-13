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

public class J8Entity extends FireLightVisionVehicle {

    public J8Entity(EntityType<J8Entity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.25f) * damage * (getHealth() > 0.1f ? 0.4f : 0.05f));
    }

    private PlayState Sb(AnimationState<J8Entity> event) {
        if (this.onGround()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.j8.qljopen"));
        }
        else {
        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                ("animation.j8.qljoff"));
        }
    }

    private PlayState Sb1(AnimationState<J8Entity> event) {
        if (this.sprintInputDown()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.j8.wyon"));
        }
        else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
                    ("animation.j8.wyoff"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "sb", 0, this::Sb));
        data.add(new AnimationController<>(this, "sb1", 0, this::Sb1));
    }



//    @Override
//    public boolean useAircraftCamera(int seatIndex) {
//        return ModKeyMappings.FREE_CAMERA.isDown() && !ClientEventHandler.zoom;
//    }


}

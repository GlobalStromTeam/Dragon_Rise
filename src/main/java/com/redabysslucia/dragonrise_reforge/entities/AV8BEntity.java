package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineType;
import com.redabysslucia.dragonrise_reforge.entities.utils.VariableEngineVehicle;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;

import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;

public class AV8BEntity extends VariableEngineVehicle {

    public AV8BEntity(EntityType<AV8BEntity> type, Level world) {
        super(type, world);
        this.setEngineTypeList(List.of(EngineType.AIRCRAFT, EngineType.HELICOPTER));
    }


//    private PlayState Sb(AnimationState<AV8BEntity> event) {
//        if (this.onGround()) {
//            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
//                    ("lay down"));
//        }
//        else {
//        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
//                ("pack up"));
//        }
//    }
//
//    private PlayState Sb1(AnimationState<AV8BEntity> event) {
//        if (this.sprintInputDown()) {
//            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
//                    ("Engineon"));
//        }
//        else {
//            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold
//                    ("EngineoFF "));
//        }
//    }
//
//    @Override
//    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
//        data.add(new AnimationController<>(this, "sb", 0, this::Sb));
//        data.add(new AnimationController<>(this, "sb1", 0, this::Sb1));
//    }



//    @Override
//    public boolean useAircraftCamera(int seatIndex) {
//        return ModKeyMappings.FREE_CAMERA.isDown() && !ClientEventHandler.zoom;
//    }

    @Override
    public double getMouseSensitivity() {
        return zoomVehicle ? 0.1 : 0.25;
    }
}

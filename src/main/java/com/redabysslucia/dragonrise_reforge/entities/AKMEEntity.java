package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class AKMEEntity extends VehicleEntity {

        public AKMEEntity(EntityType<AKMEEntity> type, Level world) {
                super(type, world);
        }


//    private PlayState radar(AnimationState<AKMEEntity> event) {
//
//        return event.setAndContinue(RawAnimation.begin().thenLoop("625.animation.radar"));
//    }
//
//    @Override
//    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
//        data.add(new AnimationController<>(this, "radar", 0, this::radar));
//    }


}

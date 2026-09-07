package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class L1A2Entity extends VehicleEntity {

        public L1A2Entity(EntityType<L1A2Entity> type, Level world) {
                super(type, world);
        }


//    private PlayState radar(AnimationState<ZSU234EEntity> event) {
//
//        return event.setAndContinue(RawAnimation.begin().thenLoop("625.animation.radar"));
//    }
//
//    @Override
//    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
//        data.add(new AnimationController<>(this, "radar", 0, this::radar));
//    }


}

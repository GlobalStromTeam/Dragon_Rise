package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

@SuppressWarnings("removal")
public class ZBD04AEntity extends GeoVehicleEntity {

        private static final EntityDataAccessor<Integer> FLAP_STATE = SynchedEntityData.defineId(ZBD04AEntity.class, EntityDataSerializers.INT);
        private static final EntityDataAccessor<Integer> FLAP_TIMER = SynchedEntityData.defineId(ZBD04AEntity.class, EntityDataSerializers.INT);
        private int lastFlapCheckTick = 0;

        public ZBD04AEntity(EntityType<ZBD04AEntity> type, Level world) {
                super(type, world);
        }

        @Override
        protected void defineSynchedData() {
                super.defineSynchedData();
                entityData.define(FLAP_STATE, 0);
                entityData.define(FLAP_TIMER, 0);
        }

        @Override
        public void tick() {
                super.tick();
                if (!level().isClientSide && tickCount - lastFlapCheckTick >= 10) {
                        lastFlapCheckTick = tickCount;
                        boolean inWater = this.isInWater();
                        int state = entityData.get(FLAP_STATE);

                        if (inWater && state == 0) {
                                entityData.set(FLAP_STATE, 1);
                                entityData.set(FLAP_TIMER, 20);
                        } else if (!inWater && (state == 1 || state == 2)) {
                                entityData.set(FLAP_STATE, 3);
                                entityData.set(FLAP_TIMER, 20);
                        }

                        int timer = entityData.get(FLAP_TIMER);
                        if (timer > 0) {
                                entityData.set(FLAP_TIMER, timer - 1);
                                if (timer == 1) {
                                        if (state == 1) {
                                                entityData.set(FLAP_STATE, 2);
                                        } else if (state == 3) {
                                                entityData.set(FLAP_STATE, 0);
                                        }
                                }
                        }
                }
        }

        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((source, damage) -> getSourceAngle(source, 0.25f) * damage);
        }

        private PlayState cannonShootPredicate(AnimationState<ZBD04AEntity> event) {

                if (getShootAnimationTimer(0, 0) > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("zbd04a.animation.subcannon.new"));
                }
                if (getShootAnimationTimer(0, 1) > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("zbd04a.animation.minecannon.new"));
                }

                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.unknown.new"));

        }

        private PlayState flapPredicate(AnimationState<ZBD04AEntity> event) {
                int state = entityData.get(FLAP_STATE);
                if (state == 1 || state == 2) {
                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("flbon"));
                }
                if (state == 3) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("flboff"));
                }
                return PlayState.CONTINUE;
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar data) {
                data.add(new AnimationController<>(this, "cannon", 0, this::cannonShootPredicate));
                data.add(new AnimationController<>(this, "flap", 0, this::flapPredicate));
        }


}

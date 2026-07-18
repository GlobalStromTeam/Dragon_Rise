package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

@SuppressWarnings("removal")
public class Zbd05Entity extends GeoVehicleEntity {

        private static final EntityDataAccessor<Integer> FLAP_STATE =
                new EntityDataAccessor<>(100, EntityDataSerializers.INT);
        private static final EntityDataAccessor<Integer> FLAP_TIMER =
                new EntityDataAccessor<>(101, EntityDataSerializers.INT);
        private int lastFlapCheckTick = 0;

        public Zbd05Entity(EntityType<Zbd05Entity> type, Level world) {
                super(type, world);
        }

        @Override
        protected void defineSynchedData() {
                super.defineSynchedData();
                // 模型默认展开，初始状态为已展开
                entityData.define(FLAP_STATE, 2);
                entityData.define(FLAP_TIMER, 0);
        }

        @Override
        public void travel() {
                super.travel();
                if (!level().isClientSide && isInFluidType()) {
                        float power = entityData.get(VehicleEntity.POWER);
                        Vec3 viewVec = getViewVector(1f).normalize();
                        Vec3 delta = getDeltaMovement();
                        // 抵消部分水中阻力
                        delta = delta.multiply(1.06, 1.0, 1.06);
                        // 额外推力
                        setDeltaMovement(delta.add(viewVec.scale(power * 0.35)));
                }
        }

        @Override
        public void tick() {
                super.tick();
                if (level().isClientSide) return;

                // 动画计时器每 tick 递减
                int state = entityData.get(FLAP_STATE);
                if (state == 1 || state == 3) {
                        int timer = entityData.get(FLAP_TIMER) - 1;
                        entityData.set(FLAP_TIMER, timer);
                        if (timer <= 0) {
                                entityData.set(FLAP_STATE, state == 1 ? 2 : 0);
                        }
                }

                // 每 10 tick 检测水域
                if (tickCount - lastFlapCheckTick < 10) return;
                lastFlapCheckTick = tickCount;

                state = entityData.get(FLAP_STATE);
                boolean inWater = this.isInWater();

                // 在水中：展开防浪板
                if (inWater && state == 0) {
                        entityData.set(FLAP_STATE, 1);
                        entityData.set(FLAP_TIMER, 40);
                }
                // 离开水：关闭防浪板
                else if (!inWater && (state == 1 || state == 2)) {
                        entityData.set(FLAP_STATE, 3);
                        entityData.set(FLAP_TIMER, 40);
                }
        }

        private PlayState flapPredicate(AnimationState<Zbd05Entity> event) {
                int state = entityData.get(FLAP_STATE);
                // 展开/保持展开
                if (state == 1 || state == 2) {
                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("flbon"));
                }
                // 关闭/保持关闭：统一使用thenPlayAndHold避免3→0切换时重启动画
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("flboff"));
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar data) {
                data.add(new AnimationController<>(this, "flap", 0, this::flapPredicate));
        }


}

package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.SyncCameraVehicle;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

@SuppressWarnings("removal")
public class Zsl10Entity extends SyncCameraVehicle {

        // 防浪板状态: 0=关闭, 1=展开中, 2=已展开, 3=关闭中
        private static final EntityDataAccessor<Integer> FLAP_STATE =
                new EntityDataAccessor<>(100, EntityDataSerializers.INT);
        // 动画计时器
        private static final EntityDataAccessor<Integer> FLAP_TIMER =
                new EntityDataAccessor<>(101, EntityDataSerializers.INT);

        private int waterCheckCooldown = 0;

        public Zsl10Entity(EntityType<Zsl10Entity> type, Level world) {
                super(type, world);
        }

        @Override
        protected void defineSynchedData() {
                super.defineSynchedData();
                this.entityData.define(FLAP_STATE, 0);
                this.entityData.define(FLAP_TIMER, 0);
        }

        @Override
        public void tick() {
                super.tick();
                if (level().isClientSide) return;

                int state = entityData.get(FLAP_STATE);

                // 动画计时器倒计时（每 tick 更新，确保动画状态切换精确）
                if (state == 1 || state == 3) {
                        int timer = entityData.get(FLAP_TIMER) - 1;
                        entityData.set(FLAP_TIMER, timer);
                        if (timer <= 0) {
                                entityData.set(FLAP_STATE, state == 1 ? 2 : 0);
                        }
                }

                // 仅在冷却归零时检测水域（每10 tick检测一次，约0.5秒）
                if (--waterCheckCooldown > 0) return;
                waterCheckCooldown = 10;

                // 重新读取状态（动画计时器可能已改变状态）
                state = entityData.get(FLAP_STATE);
                boolean inWater = this.isInWater();

                // 在水中：展开防浪板 (flbon 1.125s ≈ 23 tick)
                if (inWater && state == 0) {
                        entityData.set(FLAP_STATE, 1);
                        entityData.set(FLAP_TIMER, 23);
                }
                // 离开水：关闭防浪板 (flboff 1.25s = 25 tick)
                else if (!inWater && (state == 1 || state == 2)) {
                        entityData.set(FLAP_STATE, 3);
                        entityData.set(FLAP_TIMER, 25);
                }
        }

        private PlayState flapPredicate(AnimationState<Zsl10Entity> event) {
                int state = entityData.get(FLAP_STATE);
                // 展开/保持展开 使用同一个动画，切换时不会重启
                if (state == 1 || state == 2) {
                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("flbon"));
                }
                // 关闭/保持关闭 使用同一个动画，切换时不会重启
                if (state == 3) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("flboff"));
                }
                // 状态0（初始关闭）：不播放任何动画，避免flboff首帧110°闪烁
                return PlayState.CONTINUE;
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar data) {
                data.add(new AnimationController<>(this, "flap", 0, this::flapPredicate));
        }


}

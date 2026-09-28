package com.redabysslucia.dragonrise_reforge.entities;


import net.minecraft.network.syncher.SynchedEntityData;import com.redabysslucia.dragonrise_reforge.entities.utils.SyncCameraVehicle;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class Zsl10Entity extends SyncCameraVehicle {

        // 1.21 的 SynchedEntityData 要求 ID 连续分配，必须用 defineId；固定 ID（100/101）会让实体无法生成。
        // 1.21 SynchedEntityData requires sequential IDs, so use defineId; fixed IDs (100/101) made the entity fail to spawn.
        // 防浪板状态: 0=关闭, 1=展开中, 2=已展开, 3=关闭中
        private static final EntityDataAccessor<Integer> FLAP_STATE =
                SynchedEntityData.defineId(Zsl10Entity.class, EntityDataSerializers.INT);
        // 动画计时器
        private static final EntityDataAccessor<Integer> FLAP_TIMER =
                SynchedEntityData.defineId(Zsl10Entity.class, EntityDataSerializers.INT);

        private int waterCheckCooldown = 0;

        public Zsl10Entity(EntityType<Zsl10Entity> type, Level world) {
                super(type, world);
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {
                super.defineSynchedData(builder);
                builder.define(FLAP_STATE, 0);
                builder.define(FLAP_TIMER, 0);
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
}

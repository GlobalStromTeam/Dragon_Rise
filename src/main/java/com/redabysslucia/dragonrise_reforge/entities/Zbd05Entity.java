package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import com.redabysslucia.dragonrise_reforge.entities.utils.MissileFireGate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

@SuppressWarnings("removal")
public class Zbd05Entity extends DragonriseVehicleBase {

        /** 座位武器表里导弹的下标（zbd05.json 的 Seats[].Weapons = [Cannon, MachineGun, Missile]） */
        private static final int MISSILE_WEAPON_INDEX = 2;

        /** 客户端提示节流：20 tick（1 秒）内只提示一次 */
        private int lastShootWarningTick = 0;

        public Zbd05Entity(EntityType<Zbd05Entity> type, Level world) {
                super(type, world);
        }

        @Override
        protected boolean isSplashDefaultOpen() {
                // 模型里防浪板默认展开：陆地初始先由 splash_off 收起，入水再 splash_on 展开
                return true;
        }

        /**
         * 导弹发射的速度门槛，与 ZBL08 一致：±14 km/h 以内允许发射。
         * 主炮、机枪不受影响（直接走 super）。
         */
        @Override
        public boolean canShoot(LivingEntity living) {
                int seatIndex = getSeatIndex(living);
                if (seatIndex < 0) return super.canShoot(living);
                if (getSelectedWeapon(seatIndex) != MISSILE_WEAPON_INDEX) return super.canShoot(living);

                if (!MissileFireGate.canFireMissile(this)) {
                        warnTooFast(living);
                        return false;
                }
                return super.canShoot(living);
        }

        @Override
        public void vehicleShoot(LivingEntity living, UUID uuid, Vec3 targetPos) {
                int seatIndex = getSeatIndex(living);
                if (seatIndex < 0 || getSelectedWeapon(seatIndex) != MISSILE_WEAPON_INDEX) {
                        super.vehicleShoot(living, uuid, targetPos);
                        return;
                }

                if (!MissileFireGate.canFireMissile(this)) {
                        warnTooFast(living);
                        return;
                }

                super.vehicleShoot(living, uuid, targetPos);
        }

        /** 车速超出门槛时的提示，仅客户端、节流 20 tick。 */
        private void warnTooFast(LivingEntity living) {
                if (level().isClientSide && living instanceof Player player
                                && this.tickCount - lastShootWarningTick > 20) {
                        lastShootWarningTick = this.tickCount;
                        player.displayClientMessage(
                                        Component.translatable("message.dragonrise_reforge.zbd05_stop_to_shoot"), true);
                }
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
}

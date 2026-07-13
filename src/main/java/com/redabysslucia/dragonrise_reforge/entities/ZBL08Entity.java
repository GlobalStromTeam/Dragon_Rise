package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import java.util.UUID;
@SuppressWarnings("removal")
public class ZBL08Entity extends GeoVehicleEntity {

        private int lastShootWarningTick = 0;

        public ZBL08Entity(EntityType<ZBL08Entity> type, Level world) {
                super(type, world);
        }

        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((source, damage) -> getSourceAngle(source, 0.25f) * damage);
        }

        private boolean isMoving() {
                Vec3 motion = this.getDeltaMovement();
                return Math.abs(motion.x) > 0.01 || Math.abs(motion.z) > 0.01;
        }

        @Override
        public void vehicleShoot(LivingEntity living, UUID uuid, Vec3 targetPos) {
                int seatIndex = getSeatIndex(living);
                if (seatIndex < 0) {
                        super.vehicleShoot(living, uuid, targetPos);
                        return;
                }
                int selectedWeapon = getSelectedWeapon(seatIndex);
                if (selectedWeapon != 2) {
                        super.vehicleShoot(living, uuid, targetPos);
                        return;
                }

                if (isMoving()) {
                        if (living instanceof Player player && this.tickCount - lastShootWarningTick > 20) {
                                player.displayClientMessage(Component.translatable("message.dragonrise_reforge.zbl08_stop_to_shoot"), true);
                                lastShootWarningTick = this.tickCount;
                        }
                        return;
                }

                super.vehicleShoot(living, uuid, targetPos);
        }
}

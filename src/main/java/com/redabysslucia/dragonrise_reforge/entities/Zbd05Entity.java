package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("removal")
public class Zbd05Entity extends DragonriseVehicleBase {

        public Zbd05Entity(EntityType<Zbd05Entity> type, Level world) {
                super(type, world);
        }

        @Override
        protected boolean isSplashDefaultOpen() {
                // 模型里防浪板默认展开：陆地初始先由 splash_off 收起，入水再 splash_on 展开
                return true;
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

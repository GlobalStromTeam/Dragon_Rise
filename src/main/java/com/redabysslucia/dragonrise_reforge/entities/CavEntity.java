package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.projectile.SmokeDecoyEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModSounds;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * CAV-45「莫塔利安」。
 *
 * <p><b>全向烟雾</b>：SBW 原版 {@code VehicleWeaponUtils.releaseSmokeDecoy} 从 -78.75° 起、
 * 每 22.5° 一发给 8 发，也就是只在车头方向铺开 157.5° 的扇面。这里覆写
 * {@link VehicleEntity#releaseSmokeDecoy(Vec3)}，改成 22.5° 步长、16 发覆盖 360°，
 * 弹药 / 冷却 / 音效 / {@code decoyInputDown} 的收尾逻辑与原版保持一致。
 *
 * <p>触发链路：{@code VehicleEntity.baseTick()} 在 {@code hasSmokeDecoy()} 为真时
 * 调用虚方法 {@code this.releaseSmokeDecoy(this.getTurretVector(1f))}，所以覆写即生效。
 */
public class CavEntity extends VehicleEntity {

    /** 全向烟雾的方向数（360° / 16 = 22.5°，与原版步长一致） */
    private static final int SMOKE_DIRECTIONS = 16;

    public CavEntity(EntityType<CavEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public void releaseSmokeDecoy(Vec3 dir) {
        if (this.level().isClientSide()) {
            return;
        }
        // 与原版一致：只有按下干扰弹键且还有存量时才工作
        if (!this.decoyInputDown() || this.getDecoyCount() <= 0) {
            this.setDecoyInputDown(false);
            return;
        }

        for (int i = 0; i < SMOKE_DIRECTIONS; i++) {
            SmokeDecoyEntity smoke = new SmokeDecoyEntity(this.level());
            smoke.setPos(this.getX(), this.getY() + this.getBbHeight(), this.getZ());
            // 22.5° 步长铺满一圈
            smoke.decoyShoot(this, dir.yRot((float) (i * (Math.PI / 8.0))), 4.0f, 8.0f);
            this.level().addFreshEntity(smoke);
        }

        this.level().playSound(null, this, ModSounds.DECOY_RELEASE.get(), this.getSoundSource(), 1.0f, 1.0f);

        int count = this.getDecoyCount();
        this.setDecoyCount(count - 1);
        if (this.getDecoyCount() == 0) {
            this.setDecoyReloadCoolDown(this.getDecoyReloadTime());
        }
        this.setDecoyInputDown(false);
    }
}

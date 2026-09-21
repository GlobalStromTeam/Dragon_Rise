package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.item.gun.special.RepairToolItem;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.atsuishio.superbwarfare.tools.SeekTool;
import com.atsuishio.superbwarfare.world.phys.EntityResult;
import com.redabysslucia.dragonrise_reforge.config.server.VehicleCombatConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 维修工具（{@code superbwarfare:repair_tool}）对载具的修复规则。
 * <p>
 * 卓越前线 {@code RepairToolItem.onRayHitEntity} 的载具分支：
 * <pre>
 *   敌对（上一任驾驶员不同队）/ 潜行 → hurt(0.5)                        // 破坏
 *   非残骸                          → heal(0.5 + 0.0025 * maxHealth)   // 小幅回血、无上限
 *   残骸                            → hurt(0.5 + 0.0025 * maxHealth)    // 破坏
 * </pre>
 * 本 mixin 只在"非残骸 + 非破坏"这条维修分支上接管：
 * <ul>
 *   <li>部件（炮塔 / 左右履带（车轮）/ 主副发动机）恢复到各自上限 → 部件可完全恢复；</li>
 *   <li>整车血量最多恢复到 {@link VehicleCombatConfig#REPAIR_TOOL_BODY_CAP}（15%）：
 *       低于上限补到上限，已达上限则不做任何改变 → 不能进一步修理。</li>
 * </ul>
 * 破坏分支（敌对 / 潜行 / 残骸）不拦截，完全沿用卓越前线逻辑；
 * 维修分支需要自行补上原方法在同一分支里发的音效与命中粒子。
 * <p>
 * 之所以用 HEAD 注入而不是重定向 {@code heal} 调用：{@code heal(float)} 是原版
 * {@code LivingEntity} 的成员，在被改写的调用点里名字是 SRG 名，无法用可读字符串稳定匹配；
 * 而 {@code onRayHitEntity} 是卓越前线自有方法，注入目标稳定。
 */
@Mixin(value = RepairToolItem.class, remap = false)
public abstract class RepairToolVehicleMixin {

    @Inject(method = "onRayHitEntity", at = @At("HEAD"), cancellable = true)
    private void dragonrise$repairVehiclePartsAndCapBody(Entity shooter, ServerLevel level, GunData data,
                                                         EntityResult result, Vec3 shootPosition, Vec3 shootDirection,
                                                         CallbackInfo ci) {
        if (!(result.getEntity() instanceof VehicleEntity vehicle) || vehicle.isWreck()) {
            // 非载具或残骸：交给卓越前线原逻辑（残骸是"破坏"分支）
            return;
        }

        Entity lastDriver = EntityFindUtil.findEntity(level, vehicle.getLastDriverUUID());
        boolean sabotage = (lastDriver != null && !SeekTool.IN_SAME_TEAM.test(shooter, lastDriver)
                && lastDriver.getTeam() != null) || shooter.isShiftKeyDown();
        if (sabotage) {
            // 敌对或潜行：保留卓越前线的"破坏"分支
            return;
        }

        if (VehicleCombatConfig.REPAIR_TOOL_FULL_PART_REPAIR) {
            vehicle.setTurretHealth(vehicle.getTurretMaxHealth());
            vehicle.setLeftWheelHealth(vehicle.getWheelMaxHealth());
            vehicle.setRightWheelHealth(vehicle.getWheelMaxHealth());
            vehicle.setMainEngineHealth(vehicle.getEngineMaxHealth());
            vehicle.setSubEngineHealth(vehicle.getEngineMaxHealth());
        }

        float cap = vehicle.getMaxHealth() * VehicleCombatConfig.REPAIR_TOOL_BODY_CAP;
        if (vehicle.getHealth() < cap) {
            vehicle.setHealth(cap);
        }

        // 补齐原方法在维修分支里发出的音效与命中粒子
        RepairToolItem self = (RepairToolItem) (Object) this;
        Vec3 hitPos = result.getHitPos();
        level.playSound(null, hitPos.x, hitPos.y, hitPos.z, self.getRayHitEntitySound(data),
                SoundSource.PLAYERS, 0.7F, (float) ((2 * Math.random() - 1) * 0.05f + 1.0f));
        self.summonRayHitParticle(level, null, hitPos, shootDirection.scale(-1).normalize());

        ci.cancel();
    }
}

package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * 载具「方向抗性」：让官方（卓越前线）格式的
 * <pre>$entity.getSourceAngle(source, X) * damage</pre>
 * 真正生效。
 * <p>
 * 背景：卓越前线自己的 {@code DamageModify} 把脚本型修正（{@code $...}）的闭包存在
 * {@code @Transient} 字段里，而 {@code VehicleData.compute()} 会执行
 * {@code getDefault().copy()} —— 该 copy 是 Gson 序列化往返（{@code IDBasedData.copy()}），
 * 往返之后脚本闭包丢失、{@code modifyFunction == null}，于是所有 {@code $} 修正在
 * {@code DamageModify.compute()} 里原样返回伤害（静默失效）。
 * <p>
 * 这里在 {@link VehicleEntity#hurt} 的入口自行把 {@code $} 脚本解析出来并应用一次，
 * 语义与官方 {@code VehicleVecUtils.getDamageSourceAngle} 完全一致（正面 {@code 1-m}、
 * 侧面 {@code 1}、背面 {@code 1+m}，下限 0.5）。
 */
public final class VehicleDirectionArmor {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** 载具 id（{@code namespace:path}）→ 该车 JSON 里的 $ 脚本规则 */
    private static volatile Map<String, List<DirectionArmorRule>> rules = Map.of();

    private VehicleDirectionArmor() {
    }

    /** 由数据加载器在服务端 reload 时调用 */
    public static void setRules(Map<String, List<DirectionArmorRule>> loaded) {
        rules = loaded;
    }

    public static int vehicleCount() {
        return rules.size();
    }

    public static int ruleCount() {
        return rules.values().stream().mapToInt(List::size).sum();
    }

    /**
     * 在 {@link VehicleEntity#hurt} 入口调整伤害值。
     *
     * @return 调整后的伤害（无规则/无攻击者时原样返回）
     */
    public static float apply(VehicleEntity vehicle, DamageSource source, float amount) {
        if (amount <= 0f) {
            return amount;
        }
        Map<String, List<DirectionArmorRule>> snapshot = rules;
        if (snapshot.isEmpty()) {
            return amount;
        }

        EntityType<?> type = vehicle.getType();
        if (type == null) {
            return amount;
        }
        List<DirectionArmorRule> list = snapshot.get(EntityType.getKey(type).toString());
        if (list == null || list.isEmpty()) {
            return amount;
        }

        // 与官方 getDamageSourceAngle 相同的取源顺序
        Entity attacker = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
        if (attacker == null) {
            return amount;
        }

        Vec3 toAttacker = new Vec3(vehicle.getX(), vehicle.getY() + vehicle.getBbHeight() / 2.0, vehicle.getZ())
                .vectorTo(attacker.position());
        if (toAttacker.lengthSqr() < 1.0E-6) {
            return amount;   // 与攻击者重合，方向无意义
        }

        float dot = (float) toAttacker.normalize().dot(vehicle.getViewVector(1.0f));
        float multiplier = 1.0f;
        for (DirectionArmorRule rule : list) {
            multiplier *= rule.apply(dot, vehicle.getHealth());
        }
        if (multiplier == 1.0f) {
            return amount;
        }

        float result = amount * multiplier;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("方向抗性 {}：dot={} 倍率={} {} -> {}",
                    EntityType.getKey(type), dot, multiplier, amount, result);
        }
        return result;
    }
}

package com.redabysslucia.dragonrise_reforge.entities.utils;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析官方（卓越前线）格式的 {@code $} 伤害修正脚本，只实现方向抗性所需的部分。
 * <p>
 * 支持的形式（与官方载具 JSON 里写的一模一样）：
 * <pre>
 *   $entity.getSourceAngle(source, 0.25) * damage
 *   $entity.getSourceAngle(source, 0.3) * damage * (entity.getHealth() &gt; 0.1 ? 0.4 : 0.05)
 * </pre>
 * 即：必须含 {@code getSourceAngle(source, m)}，可选尾随一个 {@code (entity.getHealth() 比较符 T ? A : B)}，
 * 其余项（如 {@code * damage}）不影响倍率，直接忽略。
 * <p>
 * 倍率算法与官方 {@code VehicleVecUtils.getDamageSourceAngle} 完全一致：
 * {@code max(1 - m * dot, 0.5)}，其中 dot 为「载具 → 攻击者」单位向量与载具朝向向量的点积
 * （正面 +1、侧面 0、背面 -1）。
 * <p>
 * 本类不引用任何 Minecraft 类型，便于单独编译与离线测试。
 */
public final class DirectionArmorRule {

    /** getSourceAngle(source, m) */
    private static final Pattern ANGLE_PATTERN = Pattern.compile(
            "getSourceAngle\\s*\\(\\s*source\\s*,\\s*(-?\\d+(?:\\.\\d*)?)\\s*\\)");

    /** 可选的 (entity.getHealth() > T ? A : B) 尾巴 */
    private static final Pattern HEALTH_GATE_PATTERN = Pattern.compile(
            "getHealth\\s*\\(\\s*\\)\\s*([<>]=?)\\s*(-?\\d+(?:\\.\\d*)?)\\s*\\?\\s*(-?\\d+(?:\\.\\d*)?)"
                    + "\\s*:\\s*(-?\\d+(?:\\.\\d*)?)");

    /** 原始脚本串（写回日志用） */
    public final String raw;
    /** getSourceAngle 的乘数 m */
    public final float angleFactor;
    /** 是否存在 (getHealth 比较 …) 这段 */
    public final boolean hasHealthGate;
    /** 比较符：">"、"<"、">="、"<=" */
    public final String healthOperator;
    private final float healthThreshold;
    private final float healthTrue;
    private final float healthFalse;

    private DirectionArmorRule(String raw, float angleFactor, boolean hasHealthGate,
                              String healthOperator, float healthThreshold, float healthTrue, float healthFalse) {
        this.raw = raw;
        this.angleFactor = angleFactor;
        this.hasHealthGate = hasHealthGate;
        this.healthOperator = healthOperator;
        this.healthThreshold = healthThreshold;
        this.healthTrue = healthTrue;
        this.healthFalse = healthFalse;
    }

    /**
     * 解析一条 {@code $...} 脚本。
     *
     * @param script JSON 里写的原始字符串（可带/不带前导 $）
     * @return 解析结果；不认识的形式返回 null（调用方记警告并忽略）
     */
    @Nullable
    public static DirectionArmorRule parse(String script) {
        if (script == null) {
            return null;
        }
        String text = script.trim();
        if (text.startsWith("$")) {
            text = text.substring(1);
        }
        Matcher angle = ANGLE_PATTERN.matcher(text);
        if (!angle.find()) {
            return null;
        }
        float factor;
        try {
            factor = Float.parseFloat(angle.group(1));
        } catch (NumberFormatException e) {
            return null;
        }

        Matcher gate = HEALTH_GATE_PATTERN.matcher(text);
        if (gate.find()) {
            try {
                return new DirectionArmorRule(script, factor, true, gate.group(1),
                        Float.parseFloat(gate.group(2)),
                        Float.parseFloat(gate.group(3)),
                        Float.parseFloat(gate.group(4)));
            } catch (NumberFormatException e) {
                return new DirectionArmorRule(script, factor, false, "", 0f, 0f, 0f);
            }
        }
        return new DirectionArmorRule(script, factor, false, "", 0f, 0f, 0f);
    }

    /**
     * 计算伤害倍率。
     *
     * @param dot    「载具 → 攻击者」与载具朝向的点积（正面 +1 / 侧面 0 / 背面 -1）
     * @param health 载具当前血量（用于可选的 getHealth 尾巴）
     */
    public float apply(float dot, float health) {
        float result = Math.max(1f - angleFactor * dot, 0.5f);
        if (hasHealthGate) {
            result *= compare(health) ? healthTrue : healthFalse;
        }
        return result;
    }

    private boolean compare(float health) {
        switch (healthOperator) {
            case ">":
                return health > healthThreshold;
            case ">=":
                return health >= healthThreshold;
            case "<":
                return health < healthThreshold;
            case "<=":
                return health <= healthThreshold;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return "DirectionArmorRule{m=" + angleFactor
                + (hasHealthGate ? ", gate=health " + healthOperator + " " + healthThreshold
                        + " ? " + healthTrue + " : " + healthFalse : "")
                + ", raw=\"" + raw + "\"}";
    }
}

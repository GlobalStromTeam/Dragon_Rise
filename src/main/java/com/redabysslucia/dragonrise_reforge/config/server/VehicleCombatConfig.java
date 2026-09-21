package com.redabysslucia.dragonrise_reforge.config.server;

/**
 * 载具战斗行为调参。
 * <p>
 * 由 {@code mixin} 包下的车辆行为 mixin 读取，用于覆盖卓越前线（superbwarfare）的默认行为。
 * 本模组沿用 {@link com.redabysslucia.dragonrise_reforge.config.server.MiscConfig} 的常量风格，
 * 修改数值需要重新编译打包。
 */
public final class VehicleCombatConfig {

    /** 是否禁用被动回血：整车"呼吸回血"与部件每 tick 回血（维修工具不受影响）。 */
    public static final boolean DISABLE_PASSIVE_REGEN = true;

    /** 炮塔损坏（TurretHealth=0）后的转速系数；卓越前线默认为 0.2。 */
    public static final float TURRET_DAMAGED_SPEED_FACTOR = 0.05f;

    /** 维修工具对"整车血量"的恢复上限比例（0.15 = 15%）；达到该比例后维修工具不再提升整车血量。 */
    public static final float REPAIR_TOOL_BODY_CAP = 0.15f;

    /** 维修工具是否把部件血量完全恢复。 */
    public static final boolean REPAIR_TOOL_FULL_PART_REPAIR = true;

    /** 单侧履带损坏时是否只允许原地转向（true = 不能直行，只能转向）。 */
    public static final boolean DEAD_TRACK_PIVOT_ONLY = true;

    private VehicleCombatConfig() {
    }
}

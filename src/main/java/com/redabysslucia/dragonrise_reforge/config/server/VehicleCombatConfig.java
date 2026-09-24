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

    /** 单侧履带损坏时是否冻结该侧履带与负重轮（true = 该侧完全不动，车体仍可正常行驶/转向）。 */
    public static final boolean FREEZE_DEAD_TRACK_SIDE = true;

    /** 单侧履带损坏时，前进/后退是否自动叠加"朝报废侧转向"的输入（左坏=叠加 A，右坏=叠加 D）。 */
    public static final boolean DEAD_TRACK_STEER_TO_DEAD_SIDE = true;

    /**
     * 单侧履带损坏时的功率上限（1.0 为正常满功率；0.25 = 功率减少 75%）。
     * <p>在引擎计算前每 tick 限幅，保证本 tick 的位移也按限幅后的功率计算。
     */
    public static final float DEAD_TRACK_POWER_CAP = 0.25f;

    private VehicleCombatConfig() {
    }
}

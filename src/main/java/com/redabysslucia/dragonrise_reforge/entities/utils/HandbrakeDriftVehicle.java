package com.redabysslucia.dragonrise_reforge.entities.utils;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 轮式载具「按住空格 = 真漂移」的物理改造层。
 *
 * <h2>SBW 原版为什么漂不起来</h2>
 * 三处硬约束叠在一起，任何一处不改都出不来漂移：
 *
 * <ol>
 *   <li><b>drift() 是"手刹锁死"而不是"漂移"</b><br>
 *       {@code VehicleEntity.drift() = upInputDown && (leftInputDown || rightInputDown)}。
 *       在 {@code wheelEngine} 里它只做三件事：纵向与侧向摩擦 {@code f0} 从 {@code 0.54~0.79}
 *       一起拉到 {@code 0.96}（四轮抱死）、推力系数从 {@code 0.15} 砍到 {@code 0.02}、
 *       功率每 tick 乘 {@code 0.93}。车被按住原地不动，横向也不滑 —— 这是刹车，不是漂移。</li>
 *   <li><b>速度矢量被强制对齐车头</b><br>
 *       {@code deltaMovement += view * (0.05 * dot(deltaMovement, view))} 把速度方向每 tick
 *       往车头拽；{@code yRot -= 12 * speed * rudderRot} 让车头随车速成比例地转。
 *       两者一起把「车头朝向」和「速度方向」锁成一条线 —— 没有侧滑角，物理上就不是漂移。</li>
 *   <li><b>空格在不带方向时被占用</b><br>
 *       SBW 的空格同时是"手刹 / 跳跃 / 收放起落架 / 悬停切换"，
 *       玩家按住空格的第一反应是"车不动了"，而不是"车甩尾了"。</li>
 * </ol>
 *
 * <h2>本类做的事（每 tick 一次，插在 {@code super.travel()} 之后）</h2>
 * <ol>
 *   <li><b>漂移（空格 + 方向）</b>：记录侧滑角，把 superb 的"速度对齐车头"拆成沿车头 / 侧向两段，
 *       沿车头按 {@link #driftDrag()}、侧向按 {@link #driftLateralGain()} 处理，车头交给
 *       <b>侧滑角伺服</b>。目标角随车速变化：
 *       {@code lerp(driftSlipLowSpeed, driftSlipHighSpeed, min(车速 / driftSlipSpeedRef, 1))} ——
 *       16 km/h 只有 8°，60 km/h 以上才 24°；伺服速率同样按车速缩放，所以越快甩得越大越快。</li>
 *   <li><b>抵消 SBW 自己的偏航</b>：{@code wheelEngine} 每 tick 还会按
 *       {@code yRot -= 12 × 车速 × rudderRot} 转车头，只让伺服去"追"会让稳定角一直高出目标好几度。
 *       这里按 blend 精确抵掉，车头转角只由伺服决定。</li>
 *   <li><b>介入系数 blend 渐入</b>：按下空格后 blend 按
 *       {@code driftBlendIn × (0.6 + 0.8 × 车速系数)} 爬升，抓地松脱 / 偏航 / 侧推 / 补油门全部乘它。</li>
 *   <li><b>抓地连续过渡</b>：superb 漂移态摩擦固定 0.96，正常行驶是 {@code 0.54 + 0.25|cos|}（≈0.79）。
 *       直接切过去等于"摩擦突然变小" → 速度跳。这里用 travel 前的速度反推 {@code stockF0}，
 *       按 {@code lerp(stockF0, 漂移目标值, blend)} 反算补偿量。</li>
 *   <li><b>补油门</b>：superb 漂移态推力只剩 0.02 且功率每 tick 衰 7%，一按空格等于"松油"。
 *       这里按 {@code (目标车速 − 当前车速) × driftHoldGain} 补推力，目标车速 =
 *       {@code max(driftSpeedCap, 进入速度 × driftHoldDecay)}：
 *       <b>按住 W 照样能加速到车速上限</b>（不会锁死在进入速度），进入速度已高于上限时缓降。</li>
 *   <li><b>纯手刹（空格不带走方向）</b>：superb 此时用的是普通抓地 {@code stockF0}，直接再乘一个
 *       锁死系数会让速度"骤减"。这里同样按 {@link #handbrakeBlendIn()} 渐入，
 *       净保留率从 {@code stockF0} 平滑过渡到 {@link #handbrakeLockDrag()}，是"踩刹车"不是"撞墙"。</li>
 *   <li><b>松手回正</b>：方向按 {@link #recoverGrip()} 收敛回车头，
 *       同时用 travel 前速度按 {@link #recoverScrub()} 保住动量。</li>
 *   <li><b>侧滑超限</b>：超过 {@link #maxSlipAngle()} 转入失速，按 {@link #overSlipDrag()} 掉速。</li>
 * </ol>
 *
 * <h2>为什么双端都算</h2>
 * SBW 的输入位（left/right/forward/up…）都是 synched entity data，服务端 {@code processInput}
 * 写、客户端自动读，所以两端跑同一套 {@code wheelEngine} 是确定的。这里也在两端执行同一段
 * 后处理：客户端保持预测一致，服务端的结果仍然通过位置同步作为权威值。
 *
 * <h2>接入方式</h2>
 * <pre>
 * A. 子类覆写（推荐，SantanaEntity 就是这么接的）：
 *    public void travel() {
 *        this.driftBegin();    // 缓存 travel 前速度
 *        super.travel();       // superb 算油门 / 转向 / 抓地
 *        this.driftEnd();      // 改写成本 tick 的漂移速度（tickCount 去重）
 *    }
 *
 * B. 什么都不写：VehicleEntityHandbrakeDriftMixin 在 VehicleEntity.travel() 的
 *    HEAD 调 driftBeginHook()、TAIL 调 driftEndHook()。
 *
 * 两条路共用同一个 tickCount 闸门，谁先到谁算，绝不重复施加阻力。
 * </pre>
 */
public abstract class HandbrakeDriftVehicle extends DragonriseVehicleBase {

    /** SBW {@code wheelEngine} 里漂移态用的推力系数（{@code if (drift()) 0.02 else 0.15}）。 */
    private static final double SUPERB_DRIFT_THRUST = 0.02;
    /** SBW {@code wheelEngine} 里正常行驶的推力系数。 */
    private static final double SUPERB_NORMAL_THRUST = 0.15;
    /** SBW 在漂移态给整个速度矢量乘的摩擦（X/Z 同一个值）。 */
    private static final double SUPERB_DRIFT_FRICTION = 0.96;
    /** SBW 每 tick 的偏航系数：{@code yRot -= 12 × 车速 × rudderRot}。 */
    private static final double SUPERB_YAW_RATE = 12.0;

    /* ==================== 调参区（改一个就只动一件事） ==================== */

    /** 漂移最低水平速度（格/tick）。0.16 ≈ 11.5 格/秒；低于它按住空格就是纯手刹。 */
    protected double driftMinSpeed() {
        return 0.16;
    }

    /** 漂移中转向输入换算成侧向冲量的系数（前轮"咬地"那一下，主要靠侧滑角伺服）。 */
    protected double driftLateralPush() {
        return 0.02;
    }

    /** 低速时的漂移目标侧滑角（度）。刚过漂移门槛时只偏这么多：小角度带一下，不是横着走。 */
    protected float driftSlipLowSpeed() {
        return 8f;
    }

    /** 参考车速及以上的漂移目标侧滑角（度）。车速越高摆得越大，这里是上限。 */
    protected float driftSlipHighSpeed() {
        return 24f;
    }

    /**
     * 漂移角达到上限、偏航给满速率的参考车速（格/tick）。
     * 目标侧滑角、偏航速率、介入速度都按 {@code min(车速 / 本值, 1)} 缩放。
     */
    protected double driftSlipSpeedRef() {
        return 0.85;
    }

    /**
     * 偏航速率在极低速时的下限比例（0~1）。
     * 纯按车速缩放的话，刚过门槛时伺服几乎没有转向权限，侧滑建立不起来；
     * 0.4 表示最慢也有满速率的 40%，再线性爬到 1.0。
     */
    protected double driftYawFloor() {
        return 0.4;
    }

    /** 当前车速下的漂移目标侧滑角（度）：低速小角度 → 高速大角度，线性过渡。 */
    public float driftTargetSlipAt(double horizontalSpeed) {
        float f = (float) Mth.clamp(horizontalSpeed / Math.max(driftSlipSpeedRef(), 1.0E-3), 0.0, 1.0);
        return Mth.lerp(f, driftSlipLowSpeed(), driftSlipHighSpeed());
    }

    /** 侧滑角伺服的增益（度/度）。越大越快贴住目标角。 */
    protected double driftYawGain() {
        return 1.15;
    }

    /** 侧滑角伺服的偏航速率上限（度/tick），再乘车速系数与 blend。 */
    protected double driftMaxYawRate() {
        return 8.0;
    }

    /**
     * 漂移介入的基础爬升速率（每 tick 给 blend 增加多少；1.0 = 一 tick 就满）。
     * 实际会再乘 {@code 0.6 + 0.8 × 车速系数}：高速介入快（一把就甩出来），低速介入慢（慢慢揉）。
     */
    protected double driftBlendIn() {
        return 0.10;
    }

    /** 漂移中「沿车头」速度的每 tick 净保留率（blend=1 时的目标值）。越小手刹越利。 */
    protected double driftDrag() {
        return 0.99;
    }

    /**
     * 漂移中「侧向」速度的每 tick <b>净</b>保留率（必须 &lt;= 1）。
     * 越接近 1 越像冰面；大于 1 会让侧滑分量自己长出速度，车速失控上涨。
     */
    protected double driftLateralGain() {
        return 0.99;
    }

    /** 补推力的上限（每 tick 乘 targetSpeed）。superb 漂移态只给 0.02，这里最多补到这么多。 */
    protected double driftThrust() {
        return 0.05;
    }

    /** 补推力的比例增益：离目标车速越远补得越多。 */
    protected double driftHoldGain() {
        return 0.25;
    }

    /**
     * 漂移时仍能加速到的车速上限（格/tick）。按住 W 时补推力朝它推 ——
     * <b>漂移不会把速度锁死在进入瞬间</b>，该加速还是加速，只是被这个上限兜住。
     */
    protected double driftSpeedCap() {
        return 0.75;
    }

    /**
     * 进入漂移时的车速作为"最低保速"每 tick 的衰减率。
     * 0.995 ≈ 每秒掉 10%；1.0 = 一直维持进入速度。
     * 用于进入速度高于 {@link #driftSpeedCap()} 的情况（不让高速漂移瞬间掉速）。
     */
    protected double driftHoldDecay() {
        return 0.995;
    }

    /** 侧滑角上限（度）。超过转入失速：不再补推力，先掉速。 */
    protected float maxSlipAngle() {
        return 72f;
    }

    /** 侧滑超限时的速度衰减（每 tick）。 */
    protected double overSlipDrag() {
        return 0.93;
    }

    /** 松手后速度矢量回正车头的插值系数（0~1）。越大回正越快、越"抓地"。 */
    protected double recoverGrip() {
        return 0.22;
    }

    /**
     * 回正期的速度保留率（每 tick）。
     * <p>松手那一 tick superb 会按 {@code f0 = 0.54 + 0.25|cos|} 施加地面摩擦 —— 带着大侧滑时
     * 相当于每 tick 砍掉四成速度，手感像撞墙。回正期改为"按 travel 前的速度保住此比例、
     * 只把方向掰回车头"，抓地力是渐进回来的。
     */
    protected double recoverScrub() {
        return 0.96;
    }

    /** 纯手刹（空格不带走方向）的<b>净</b>纵向锁死系数。这是"踩刹车"，不是漂移。 */
    protected double handbrakeLockDrag() {
        return 0.94;
    }

    /**
     * 纯手刹的介入速率（每 tick）。与漂移一样渐入，
     * 所以按空格那一瞬间速度不会"骤减"，而是 6~7 tick 内渐进到锁死。
     */
    protected double handbrakeBlendIn() {
        return 0.15;
    }

    /* ==================== 运行时状态 ==================== */

    /** 本 tick 是否处于漂移（服务端物理 / 双端视觉都读这个） */
    private boolean drifting;
    /** 漂移介入系数 0~1：抓地松脱 / 偏航 / 侧推 / 补油门都乘它，保证进入是渐变的 */
    private float driftBlend;
    /** 纯手刹介入系数 0~1：让"按空格不带走方向"也是渐进刹车 */
    private float handbrakeBlend;
    /** 进入漂移那一刻的车速：作为保速下限（不会锁死上限，上限见 driftSpeedCap） */
    private double driftHoldSpeed;
    /** 侧滑角（度，带符号；正 = 速度在车头左侧，即车尾向右甩） */
    private float slipAngle;
    private float slipAngleO;
    /** 未平滑的原始侧滑角，供伺服使用（平滑值只给渲染） */
    private float rawSlip;
    /** travel() 之前的速度，回正期用来保住动量，并用来反推"无手刹时的抓地系数" */
    private Vec3 preTravelMotion = Vec3.ZERO;
    /** 每 tick 去重：子类覆写与 mixin 注入只算一次 */
    private int lastDriftTick = -1;
    /** 本 tick 是否已由子类在 travel 前登记 */
    private Boolean activeEntry;

    public HandbrakeDriftVehicle(EntityType<?> type, Level level) {
        super(type, level);
    }

    /* ==================== 对外查询（渲染 / HUD） ==================== */

    /** 本 tick 是否在漂移 */
    public boolean isDrifting() {
        return drifting;
    }

    /** 漂移介入系数 0~1（渐入用，渲染可据此淡入轮胎烟 / 侧倾） */
    public float getDriftBlend() {
        return driftBlend;
    }

    /** 当前车速系数 0~1：目标侧滑角、介入速度按它缩放 */
    public float getDriftSpeedFactor() {
        return (float) Mth.clamp(
                getDeltaMovement().horizontalDistance() / Math.max(driftSlipSpeedRef(), 1.0E-3), 0.0, 1.0);
    }

    /** 本 tick 的目标侧滑角（度），HUD / 调试用 */
    public float getTargetSlipAngle() {
        return driftTargetSlipAt(getDeltaMovement().horizontalDistance());
    }

    /** 插值后的侧滑角（度，带符号），渲染层取 partialTicks 用 */
    public float getSlipAngle(float partialTicks) {
        return Mth.lerp(partialTicks, slipAngleO, slipAngle);
    }

    /** 当前侧滑角（度，带符号） */
    public float getSlipAngle() {
        return slipAngle;
    }

    /** 漂移强度 0~1：侧滑角占上限的比例 × blend，用于轮胎烟 / 车身侧倾幅度 */
    public float getDriftIntensity() {
        return Mth.clamp(Math.abs(slipAngle) / Math.max(1f, maxSlipAngle()), 0f, 1f) * driftBlend;
    }

    /** 漂移时车身的视觉侧倾（度），渲染器可直接叠在 roll 上 */
    public float getDriftLean(float partialTicks) {
        return Mth.clamp(-getSlipAngle(partialTicks) * 0.12f, -8f, 8f) * driftBlend;
    }

    /**
     * 手刹是否按下 = 空格。SBW 把空格映射到 upInputDown（MOVE_SPACE）。
     * 刻意不与方向键做与运算：按住空格永远是手刹，
     * 有没有方向输入只决定「漂移」还是「直线刹车」。
     */
    public boolean handbrakeDown() {
        return upInputDown();
    }

    /** 漂移判定：手刹 + 有驾驶员 + 在地面 + 未摧毁 + 速度够 */
    protected boolean driftActive() {
        if (level() == null) return false;
        if (!handbrakeDown()) return false;
        if (getFirstPassenger() == null) return false;
        if (!onGround()) return false;
        if (isWreck()) return false;
        return getDeltaMovement().horizontalDistance() >= driftMinSpeed();
    }

    /* ==================== 物理主循环 ==================== */

    /** 子类入口（travel 之前）：缓存 travel 前速度并登记接管，实际计算在 driftEnd 里一次完成 */
    protected void driftBegin() {
        this.preTravelMotion = getDeltaMovement();
        this.activeEntry = Boolean.TRUE;
    }

    /** 子类入口（travel 之后）：执行本 tick 的漂移后处理 */
    protected void driftEnd() {
        this.activeEntry = null;
        runDriftTick();
    }

    /* ---- mixin 入口（兜底，与子类入口共用 tickCount 闸门） ---- */

    /** mixin HEAD：登记接管（与子类 driftBegin 一致地缓存 travel 前速度） */
    public void driftBeginHook() {
        if (activeEntry != null) return;
        this.preTravelMotion = getDeltaMovement();
        activeEntry = Boolean.TRUE;
    }

    /** mixin TAIL：执行本 tick 的漂移后处理 */
    public void driftEndHook() {
        activeEntry = null;
        runDriftTick();
    }

    /**
     * 每 tick 只执行一次。无论从子类 {@code driftEnd()} 还是 mixin TAIL 进来，
     * 靠 {@code tickCount} 去重，保证阻力 / 侧向冲量绝不叠加。
     */
    private void runDriftTick() {
        if (level() == null) return;
        if (lastDriftTick == tickCount) return;
        lastDriftTick = tickCount;

        Vec3 motion = getDeltaMovement();
        updateSlipAngle(motion);
        double hSpeed = motion.horizontalDistance();

        // 只有"手刹 + 打方向 + 速度够"才是漂移；只按空格不打方向走纯手刹分支。
        // （SBW 的 drift() 也要求带方向，此时它才把摩擦切成 0.96；不带方向时它用普通抓地，
        //  两条分支的基准摩擦不同，混在一起算就会出现速度骤减。）
        boolean steering = steerInput() != 0f;

        if (driftActive() && steering) {
            handbrakeBlend = 0f;
            // 进入瞬间锁定"保速下限"（上限仍是 driftSpeedCap，所以不会把车速锁死在当前值）
            if (driftBlend <= 0f) {
                driftHoldSpeed = hSpeed;
            }
            double speedFactor = Mth.clamp(hSpeed / Math.max(driftSlipSpeedRef(), 1.0E-3), 0.0, 1.0);
            driftBlend = (float) Math.min(1.0, driftBlend + driftBlendIn() * (0.6 + 0.8 * speedFactor));
            applyDriftPhysics(motion, driftBlend);
            this.drifting = true;
        } else if (handbrakeDown() && getFirstPassenger() != null && onGround()) {
            // 纯手刹（不带走方向）：渐进锁死，避免按下去速度骤减
            driftBlend = 0f;
            handbrakeBlend = (float) Math.min(1.0, handbrakeBlend + handbrakeBlendIn());
            applyHandbrake(motion);
            this.drifting = false;
        } else {
            driftBlend = 0f;
            handbrakeBlend = 0f;
            recover(motion);
            this.drifting = false;
        }
    }

    /**
     * 纯手刹：superb 在这一分支用的是普通抓地 {@code stockF0}（因为没打方向时 drift() 为假），
     * 所以这里把净保留率从 {@code stockF0} 平滑过渡到 {@link #handbrakeLockDrag()}，
     * 再换算成"还要额外乘多少"（除以 superb 已经乘过的 stockF0）。
     */
    private void applyHandbrake(Vec3 motion) {
        double stockF0 = Math.max(stockFriction(), 1.0E-3);
        double net = Mth.lerp(handbrakeBlend, stockF0, handbrakeLockDrag());
        double apply = net / stockF0;
        setDeltaMovement(motion.x * apply, motion.y, motion.z * apply);
    }

    /**
     * 漂移态：抓地松脱（按 blend 渐变）→ 侧向冲量 → 侧滑角伺服（目标角与速率都随车速）→ 补油门。
     *
     * @param blend 0~1 的介入系数
     */
    private void applyDriftPhysics(Vec3 motion, float blend) {
        Vec3 heading = horizontalHeading();
        double hSpeed = motion.horizontalDistance();

        // 速度拆成平面上的「沿车头」与「侧向」两个分量（竖直分量单独保留）
        Vec3 horiz = new Vec3(motion.x, 0.0, motion.z);
        Vec3 along = heading.scale(horiz.dot(heading));
        Vec3 lateral = horiz.subtract(along);

        // superb 在漂移态把 X/Z 统一乘 0.96。这里不去"还原成普通抓地"（那会在低 blend 时
        // 突然加上一大截阻力，速度 V 型下坠），而是从这个 0.96 平滑过渡到漂移目标保留率：
        // blend=0 → 乘 1.0（等于完全不动 superb 的结果），blend=1 → 乘 driftDrag / 0.96。
        double keepAlong = Mth.lerp(blend, 1.0, driftDrag() / SUPERB_DRIFT_FRICTION);
        double keepLateral = Mth.lerp(blend, 1.0, driftLateralGain() / SUPERB_DRIFT_FRICTION);

        Vec3 out = along.scale(keepAlong).add(lateral.scale(keepLateral));

        if (hSpeed > 1.0E-4) {
            // 目标角与偏航速率都随车速变化：速度越快摆得越大、侧滑建立得越快
            double speedRatio = Mth.clamp(hSpeed / Math.max(driftSlipSpeedRef(), 1.0E-3), 0.0, 1.0);
            double yawFactor = Mth.lerp(speedRatio, driftYawFloor(), 1.0);
            float steer = steerInput();

            if (steer != 0f) {
                // 车头左手方向 = (heading.z, 0, -heading.x)；steer>0 表示左打
                Vec3 leftward = new Vec3(heading.z, 0.0, -heading.x);
                out = out.add(leftward.scale(steer * driftLateralPush() * hSpeed * blend));

                // 先把 superb 本 tick 自己的偏航抵掉（yRot -= 12 × 车速 × rudderRot），
                // 否则它会持续把车头往外拽，稳定角永远高出目标好几度。
                double superbYaw = SUPERB_YAW_RATE * hSpeed * getRudderRot() * (getPower() > 0f ? 1.0 : -1.0);
                setYRot((float) (getYRot() + superbYaw * blend));

                // 侧滑角伺服：目标角随车速抬升（低速 8° → 高速 24°），
                // err > 0 表示侧滑还不够 → 车头再转（左打时 yRot 减小，与 SBW 同向）；
                // 侧滑超过目标 err 变号，车头自动回摆 —— 不会甩成原地陀螺。
                float targetSlip = driftTargetSlipAt(hSpeed);
                double err = steer * targetSlip - rawSlip;
                double rate = Mth.clamp(err * driftYawGain(), -driftMaxYawRate(), driftMaxYawRate())
                        * blend * yawFactor;
                setYRot((float) (getYRot() - rate));
            }

            // 补油门：superb 漂移态推力只剩 0.02 且功率每 tick 衰 7%，一按空格等于"松油"。
            // 目标车速 = max(上限, 进入速度 × 缓降)：所以按住 W 仍能加速到上限，不会锁死在进入速度；
            // 进入速度高于上限时按 driftHoldDecay 缓降。
            // 推力也从 superb 已给的 0.02 起步（blend=0 时不额外补），避免一按空格就猛地加推力。
            if (forwardInputDown()) {
                double power = Math.max(getPower(), 0.0);
                double ts = getTargetSpeed();
                double superbThrust = SUPERB_DRIFT_THRUST * ts * power;   // superb 漂移分支已加
                double want = Math.max(driftSpeedCap(), driftHoldSpeed * driftHoldDecay());
                double holdThrust = Mth.clamp((want - hSpeed) * driftHoldGain(), 0.0, driftThrust() * ts);
                double wanted = Mth.lerp(blend, superbThrust, holdThrust);
                out = out.add(heading.scale(wanted - superbThrust));
            }
        }

        // 侧滑超限：先掉速，别变原地陀螺
        if (Math.abs(rawSlip) > maxSlipAngle()) {
            out = out.scale(overSlipDrag());
        }

        setDeltaMovement(new Vec3(out.x, motion.y, out.z));
    }

    /**
     * 没按手刹时 superb 会用的地面抓地系数：{@code 0.54 + 0.25 * |normalize(deltaMovement)·view|}。
     * 用 travel 前的速度算，就能知道"这一 tick 如果没按空格，速度会被乘多少"，
     * 从而把漂移 / 手刹的摩擦接入做成连续的，避免速度突变。
     */
    private double stockFriction() {
        double speed = preTravelMotion.horizontalDistance();
        if (speed < 1.0E-4) return 0.79;
        Vec3 heading = horizontalHeading();
        double cos = Math.abs(new Vec3(preTravelMotion.x, 0.0, preTravelMotion.z).dot(heading)) / speed;
        return 0.54 + 0.25 * Math.min(cos, 1.0);
    }

    /** 平面车头方向（丢掉俯仰分量；地面载具基本等同于 getViewVector） */
    private Vec3 horizontalHeading() {
        Vec3 view = getViewVector(1f);
        Vec3 flat = new Vec3(view.x, 0.0, view.z);
        return flat.lengthSqr() < 1.0E-8 ? new Vec3(0.0, 0.0, 1.0) : flat.normalize();
    }

    /**
     * 松手 / 停车：方向掰回车头，同时保住动量。
     * <p>松手那一 tick superb 会用 {@code f0 = 0.54 + 0.25|cos|} 施加地面摩擦，带大侧滑时
     * 相当于每 tick 掉四成速度（手感像撞墙）。所以这里以 travel 前的速度为基准，
     * 只按 {@link #recoverScrub()} 温和衰减，方向按 {@link #recoverGrip()} 收敛。
     */
    private void recover(Vec3 motion) {
        double horizSpeed = motion.horizontalDistance();
        if (horizSpeed <= 1.0E-4 || Math.abs(rawSlip) <= 1.5f) return;

        Vec3 heading = horizontalHeading();
        Vec3 curDir = new Vec3(motion.x, 0.0, motion.z).normalize();

        double base = Math.max(preTravelMotion.horizontalDistance(), horizSpeed) * recoverScrub();
        Vec3 dir = curDir.lerp(heading, recoverGrip());
        if (dir.lengthSqr() < 1.0E-8) return;
        dir = dir.normalize();

        setDeltaMovement(dir.x * base, motion.y, dir.z * base);
    }

    /** 侧滑角：速度矢量相对车头的 yaw 偏角，带符号（正 = 速度在车头左侧，即车尾向右甩） */
    private void updateSlipAngle(Vec3 motion) {
        if (motion.horizontalDistanceSqr() > 1.0E-8) {
            double velYaw = Math.toDegrees(Math.atan2(-motion.x, motion.z));
            rawSlip = (float) Mth.clamp(Mth.wrapDegrees(velYaw - getYRot()), -180.0, 180.0);
        } else {
            rawSlip = 0f;
        }
        slipAngleO = slipAngle;
        slipAngle = Mth.lerp(0.5f, slipAngle, rawSlip);
    }

    /**
     * 转向输入（+1 = 左 / -1 = 右 / 0 = 直行）。
     * SBW 0.8.9.2 wheelEngine：leftInputDown → {@code deltaRot -= steeringSpeed * 0.12 * min(holdTick,10)}，
     * 再经 {@code rudderRot} 得到 {@code yRot -= 12 * speed * rudderRot} —— 也就是<b>左打方向时 yRot 减小</b>。
     * 侧向冲量与偏航伺服都以这个符号为准，方向手感与 SBW 一致。
     */
    protected float steerInput() {
        if (rightInputDown()) return -1f;
        if (leftInputDown()) return 1f;
        return 0f;
    }

    /** 兜底：外部需要手动刷新视觉状态时调用 */
    public void syncDriftVisualState() {
        if (level() == null) return;
        updateSlipAngle(getDeltaMovement());
        this.drifting = driftActive();
    }
}

# 载具损伤行为改造 —— 移植提示词

> 用途：把下列 5 项"载具损伤行为改造"以 **Mixin** 方式实现在**目标模组**中，注入对象是第三方模组
> **superbwarfare（卓越前线）**。所有行为**必须可开关**（服务端配置），任一项关闭时必须与卓越前线原生行为**完全一致**。

---

## 0. 任务概览

在目标模组里新增 5 个 Mixin + 1 个配置类，实现：

| # | 功能 | 影响的玩家体验 |
|---|---|---|
| 1 | 关闭整车"呼吸回血" | 载具血量不再自愈 |
| 2 | 关闭部件被动回血 | 炮塔/履带/发动机血量不再自愈 |
| 3 | 炮塔报废转速惩罚加重 | 炮塔被打坏后几乎转不动 |
| 4 | 履带/发动机报废表现加重 | 单侧断带只能画圈慢走；双侧断带或发动机报废彻底趴窝 |
| 5 | 维修工具规则改写 | 部件能一次修满，整车最多修到 15% |

---

## 1. 环境与依赖（必须先核对）

- Minecraft **1.20.1**、Forge **47.x**、Java **17**、SpongePowered Mixin **0.8.5+**
- 依赖：**superbwarfare 0.8.9.1-snapshot**（CurseForge maven 坐标 `curse.maven:superb-warfare-1218165:8689629`），包名 `com.atsuishio.superbwarfare.*`（Kotlin + Java 混合）
- 本文档附录 A 中的方法签名已在该 jar 上用 `javap` 核对过。**换版本必须重新用 `javap` 核对**（Kotlin 默认参数、方法改名都会让 Mixin 静默失配）
- 目标类属于**第三方模组**（不在 MC 的 SRG 映射里），因此注入点的 `remap` 规则是：

| 情况 | remap |
|---|---|
| 注入点方法是 **MC 覆写方法**（如 `VehicleEntity#baseTick`，继承自 `LivingEntity`）→ 方法名会被 SRG 改写 | **保持默认**（不要写 `remap = false`），`method = "baseTick"` |
| 注入点方法是 **superb 自有方法**（`handlePartHealth` / `trackEngine` / `turretAutoAimFromVector` / `onRayHitEntity` …）或目标类本身是 superb 工具类 | **`@Mixin(value = X.class, remap = false)`** |

> 踩坑记录：不要用 `@Redirect`/`@Inject` 去命中 `heal(float)`、`setHealth(float)` 这类**原版 `LivingEntity` 成员**——
> 在 superb 的调用点里它们已变成 SRG 名（`m_xxxxx_`），无法用可读字符串稳定匹配。**一律改为命中 superb 自有的包装方法**
> （本方案里就是 `repairAmount()`、`handlePartHealth`、`onRayHitEntity`）。

---

## 2. 配置（可开关，必须做）

用 Forge `ModConfigSpec`（推荐 `ModConfig.Type.SERVER`）建一个配置类，**每一项独立开关 + 数值可调**：

| 配置键 | 类型 | 默认 | 含义 |
|---|---|---|---|
| `disable_passive_regen` | bool | `true` | 关闭整车与部件的被动回血（功能 1+2 共用） |
| `turret_damaged_speed_factor` | double 0–1 | `0.05` | 炮塔报废后转速系数（原生 `0.2`） |
| `repair_tool_full_part_repair` | bool | `true` | 维修工具一次把部件修满 |
| `repair_tool_body_cap` | double 0–1 | `0.15` | 维修工具对**整车**血量的恢复上限比例 |
| `freeze_dead_track_side` | bool | `true` | 单侧履带报废时冻结该侧履带与负重轮 |
| `dead_track_steer_to_dead_side` | bool | `true` | 单侧履带报废时，前进/后退自动叠加"朝报废侧转向" |
| `dead_track_power_cap` | double 0–1 | `0.25` | 单侧履带报废时的功率上限 |
| `lock_on_engine_destroyed` | bool | `true` | 发动机报废 → 完全不能动（总开关） |
| `lock_track_vehicle` | bool | `true` | 上面这条是否作用于**履带车**（含双侧断带） |
| `lock_wheel_vehicle` | bool | `true` | 是否作用于**轮式车** |
| `lock_ship` | bool | `true` | 是否作用于**船** |
| `lock_helicopter` | bool | `true` | 是否作用于**直升机** |

配置类骨架：

```java
public final class VehicleCombatConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue DISABLE_PASSIVE_REGEN;
    public static final ModConfigSpec.DoubleValue TURRET_DAMAGED_SPEED_FACTOR;
    public static final ModConfigSpec.BooleanValue REPAIR_TOOL_FULL_PART_REPAIR;
    public static final ModConfigSpec.DoubleValue REPAIR_TOOL_BODY_CAP;
    public static final ModConfigSpec.BooleanValue FREEZE_DEAD_TRACK_SIDE;
    public static final ModConfigSpec.BooleanValue DEAD_TRACK_STEER_TO_DEAD_SIDE;
    public static final ModConfigSpec.DoubleValue DEAD_TRACK_POWER_CAP;
    public static final ModConfigSpec.BooleanValue LOCK_ON_ENGINE_DESTROYED;
    public static final ModConfigSpec.BooleanValue LOCK_TRACK_VEHICLE;
    public static final ModConfigSpec.BooleanValue LOCK_WHEEL_VEHICLE;
    public static final ModConfigSpec.BooleanValue LOCK_SHIP;
    public static final ModConfigSpec.BooleanValue LOCK_HELICOPTER;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("载具损伤行为改造").push("vehicle_combat");
        DISABLE_PASSIVE_REGEN = b.comment("关闭整车与部件的被动回血").define("disable_passive_regen", true);
        TURRET_DAMAGED_SPEED_FACTOR = b.comment("炮塔报废后的转速系数(原生 0.2)")
                .defineInRange("turret_damaged_speed_factor", 0.05, 0.0, 1.0);
        REPAIR_TOOL_FULL_PART_REPAIR = b.comment("维修工具一次把部件修满").define("repair_tool_full_part_repair", true);
        REPAIR_TOOL_BODY_CAP = b.comment("维修工具对整车的恢复上限比例")
                .defineInRange("repair_tool_body_cap", 0.15, 0.0, 1.0);
        FREEZE_DEAD_TRACK_SIDE = b.comment("单侧履带报废时冻结该侧履带与负重轮").define("freeze_dead_track_side", true);
        DEAD_TRACK_STEER_TO_DEAD_SIDE = b.comment("单侧履带报废时自动朝报废侧转向")
                .define("dead_track_steer_to_dead_side", true);
        DEAD_TRACK_POWER_CAP = b.comment("单侧履带报废时的功率上限")
                .defineInRange("dead_track_power_cap", 0.25, 0.0, 1.0);
        LOCK_ON_ENGINE_DESTROYED = b.comment("发动机报废后完全不能移动").define("lock_on_engine_destroyed", true);
        LOCK_TRACK_VEHICLE = b.define("lock_track_vehicle", true);
        LOCK_WHEEL_VEHICLE = b.define("lock_wheel_vehicle", true);
        LOCK_SHIP = b.define("lock_ship", true);
        LOCK_HELICOPTER = b.define("lock_helicopter", true);
        b.pop();
        SPEC = b.build();
    }
    private VehicleCombatConfig() {}
}
```

注册（mod 构造里）：

```java
ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, VehicleCombatConfig.SPEC);
```

**读取要求**：

- 每次 tick / 每次调用都直接读（`Value#get()` 是字段读，开销可忽略），这样改配置后**不需要重启游戏**（SERVER 型在 `/reload` 或重连后生效）
- **关闭开关时必须"什么都不做"**：原样返回原值 / 原样调用原方法 / 原样放行，保证与原生逐值一致

---

## 3. 逐项实现规格

### 3.1 关闭整车被动回血 —— `VehicleRegenMixin`

- **目标**：`VehicleEntity#baseTick`
- **原生行为**：`if (repairCoolDown == 0 && health > 0) heal(repairAmount())`，即载具血量会"呼吸回血"；低血量自损（selfHurt）也在这段里，**要保留**
- **做法**：`@Redirect` 命中 `repairAmount()F` 调用，开关开启时返回 `0f`

```java
@Mixin(VehicleEntity.class)   // baseTick 是 MC 覆写方法，保持默认 remap
public abstract class VehicleRegenMixin {

    @Redirect(
            method = "baseTick",
            at = @At(value = "INVOKE",
                     target = "Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;repairAmount()F")
    )
    private float yourmod$disableBodyBreathingRegen(VehicleEntity vehicle) {
        return VehicleCombatConfig.DISABLE_PASSIVE_REGEN.get() ? 0f : vehicle.repairAmount();
    }
}
```

> 为什么改 `repairAmount()` 而不改 `heal`：`repairAmount()` 是 superb 自有方法（生产环境不改名），注入目标稳定；
> `heal(float)` 是原版 `LivingEntity` 成员，在被改写的调用点里是 SRG 名。

### 3.2 关闭部件被动回血 —— `VehiclePartRegenMixin`

- **目标**：`VehicleEffectUtils#handlePartHealth`（**静态方法**，`remap = false`）
- **原生行为**：方法末尾给 5 个部件（炮塔 / 左履带(轮) / 右履带(轮) / 主发动机 / 副发动机）各回 **0.25% 上限**
- **做法**：`@Inject(HEAD)` 快照 5 个部件血量，`@Inject(RETURN)` 原样写回，"吃掉"这段回血
- **必须保留**该方法里的其它逻辑：`≤0 → Damaged 标志`、`>95% → 解除标志`、`整车血量 < 5% → 强制打坏炮塔与发动机`
- 部件血量是 `SynchedEntityData`，服务端写回后客户端同样看到"不回复"
- 用 `ThreadLocal` 快照，避免客户端/服务端线程互相污染（该方法两端都会执行）

```java
@Mixin(value = VehicleEffectUtils.class, remap = false)
public abstract class VehiclePartRegenMixin {

    private static final ThreadLocal<float[]> SNAPSHOT = ThreadLocal.withInitial(() -> new float[5]);

    @Inject(method = "handlePartHealth", at = @At("HEAD"))
    private static void yourmod$capture(VehicleEntity vehicle, CallbackInfo ci) {
        if (!VehicleCombatConfig.DISABLE_PASSIVE_REGEN.get()) return;
        float[] s = SNAPSHOT.get();
        s[0] = vehicle.getTurretHealth();
        s[1] = vehicle.getLeftWheelHealth();
        s[2] = vehicle.getRightWheelHealth();
        s[3] = vehicle.getMainEngineHealth();
        s[4] = vehicle.getSubEngineHealth();
    }

    @Inject(method = "handlePartHealth", at = @At("RETURN"))
    private static void yourmod$restore(VehicleEntity vehicle, CallbackInfo ci) {
        if (!VehicleCombatConfig.DISABLE_PASSIVE_REGEN.get()) return;
        float[] s = SNAPSHOT.get();
        vehicle.setTurretHealth(s[0]);
        vehicle.setLeftWheelHealth(s[1]);
        vehicle.setRightWheelHealth(s[2]);
        vehicle.setMainEngineHealth(s[3]);
        vehicle.setSubEngineHealth(s[4]);
    }
}
```

> 为什么不用 `@Redirect` 改 `kotlin.math.min`：避免依赖 Kotlin 内联/编译细节；快照+写回对公式变动更健壮。

### 3.3 炮塔报废转速惩罚 —— `VehicleTurretSpeedMixin`

- **目标**：`VehicleWeaponUtils#turretAutoAimFromVector`（`remap = false`）
- **原生行为**：`if (vehicle.entityData.get(VehicleEntity.TURRET_DAMAGED)) { ySpeed *= 0.2f; xSpeed *= 0.2f; }`
- **做法**：`@ModifyConstant(floatValue = 0.2F)` 替换为配置值（默认 `0.05`）
- 该方法内**只有这一处 `0.2F`**（实现前请用反编译确认；superb 更新后可能变化）
- 这是玩家手动瞄准与自动瞄准的唯一路径（`VehicleEntity#baseTick → adjustTurretAngle → turretAutoAimFromVector`）

```java
@Mixin(value = VehicleWeaponUtils.class, remap = false)
public abstract class VehicleTurretSpeedMixin {

    @ModifyConstant(method = "turretAutoAimFromVector", constant = @Constant(floatValue = 0.2F))
    private static float yourmod$turretDamagedSpeedFactor(float original) {
        return VehicleCombatConfig.TURRET_DAMAGED_SPEED_FACTOR.get().floatValue();
    }
}
```

> 关闭开关时若要"完全等同原生"，把默认值改成 `0.2` 或在该方法里判断开关返回 `original`（推荐后者，语义更清楚）。

### 3.4 履带 / 发动机报废表现 —— `VehicleEngineDamageMixin`（核心）

- **目标**：`VehicleEngineUtils#trackEngine / wheelEngine / shipEngine / helicopterEngine`，
  每个方法 `@Inject(HEAD)` + `@Inject(TAIL)`（`remap = false`，静态方法）

**原生表现（非常轻微，务必先理解再改）**：

| 情况 | 原生 |
|---|---|
| 履带车单侧履带报废 | 功率 ×0.975 + 一点偏航 |
| 履带车双侧履带报废 | 功率 ×0.93 |
| 轮式车车轮报废 | 同上 0.975 / 0.93 |
| 发动机报废 | 履带 ×0.96、轮式 ×0.875、船 ×0.875、直升机 ×0.98 |

**改造后的行为**：

1. **履带车（`trackEngine`）**
   - **单侧报废**（`freeze_dead_track_side` / `dead_track_steer_to_dead_side` / `dead_track_power_cap`）
     - ① 冻结该侧：`setLeftTrack(0f); setLeftWheelRot(0f);`（右侧同理）→ 该侧履带与负重轮完全静止
     - ② 玩家按 W/S 时自动叠加"朝报废侧转向"的输入（左坏 → `setLeftInputDown(true)`，右坏 → `setRightInputDown(true)`）
       ⇒ 按 W 等同 W+A、按 S 等同 S+A，**只能画圈走**
     - ③ 功率限幅到 `dead_track_power_cap`（默认 0.25，双向限幅），在引擎计算前限幅 → 本 tick 位移也按限幅后的功率算
   - **双侧报废** = 等同发动机失效（走下面第 2 条）
2. **发动机失效（`mainEngineDamaged`）**（`lock_on_engine_destroyed` + 按类型开关）
   - `HEAD`：清空 4 个行驶输入 + `setPower(0f)`
   - `TAIL`：把水平位移锁死（`setDeltaMovement(0, dy, 0)`，**保留竖直分量**，重力/浮力继续生效）
   - 覆盖履带/轮式/船/直升机四类；**固定翼 `aircraftEngine` 不覆盖**（保留原生）
3. **不改动**：`tomEngine`、`wheelChairEngine`、`airShipEngine`（如需覆盖，作为可选扩展另开开关）

```java
@Mixin(value = VehicleEngineUtils.class, remap = false)
public abstract class VehicleEngineDamageMixin {

    private static void lockInputs(VehicleEntity v) {
        v.setForwardInputDown(false);
        v.setBackInputDown(false);
        v.setLeftInputDown(false);
        v.setRightInputDown(false);
        v.setPower(0f);
    }

    private static void lockHorizontal(VehicleEntity v) {
        Vec3 d = v.getDeltaMovement();
        v.setDeltaMovement(0.0, d.y, 0.0);
    }

    private static void handleTrack(VehicleEntity v) {
        boolean left = v.getLeftWheelDamaged(), right = v.getRightWheelDamaged();
        boolean lockAll = VehicleCombatConfig.LOCK_ON_ENGINE_DESTROYED.get()
                && VehicleCombatConfig.LOCK_TRACK_VEHICLE.get();
        if (v.getMainEngineDamaged() || (left && right)) {
            if (lockAll) lockInputs(v);
            return;
        }
        if (left ^ right) {                       // 单侧
            if (VehicleCombatConfig.DEAD_TRACK_STEER_TO_DEAD_SIDE.get()
                    && (v.forwardInputDown() || v.backInputDown())) {
                if (left) v.setLeftInputDown(true); else v.setRightInputDown(true);
            }
            float cap = VehicleCombatConfig.DEAD_TRACK_POWER_CAP.get().floatValue();
            if (v.getPower() > cap) v.setPower(cap);
            else if (v.getPower() < -cap) v.setPower(-cap);
        }
    }

    private static void freezeDeadSide(VehicleEntity v) {
        if (!VehicleCombatConfig.FREEZE_DEAD_TRACK_SIDE.get()) return;
        if (v.getLeftWheelDamaged()) { v.setLeftTrack(0f); v.setLeftWheelRot(0f); }
        if (v.getRightWheelDamaged()) { v.setRightTrack(0f); v.setRightWheelRot(0f); }
    }

    @Inject(method = "trackEngine", at = @At("HEAD"))
    private static void onTrackHead(VehicleEntity v, EngineInfo.Track info, CallbackInfo ci) { handleTrack(v); }

    @Inject(method = "trackEngine", at = @At("TAIL"))
    private static void onTrackTail(VehicleEntity v, EngineInfo.Track info, CallbackInfo ci) {
        freezeDeadSide(v);
        boolean left = v.getLeftWheelDamaged(), right = v.getRightWheelDamaged();
        if ((v.getMainEngineDamaged() || (left && right))
                && VehicleCombatConfig.LOCK_ON_ENGINE_DESTROYED.get()
                && VehicleCombatConfig.LOCK_TRACK_VEHICLE.get()) {
            lockHorizontal(v);
        }
    }

    @Inject(method = "wheelEngine", at = @At("HEAD"))
    private static void onWheelHead(VehicleEntity v, EngineInfo.Wheel info, CallbackInfo ci) {
        if (v.getMainEngineDamaged() && VehicleCombatConfig.LOCK_ON_ENGINE_DESTROYED.get()
                && VehicleCombatConfig.LOCK_WHEEL_VEHICLE.get()) lockInputs(v);
    }

    @Inject(method = "wheelEngine", at = @At("TAIL"))
    private static void onWheelTail(VehicleEntity v, EngineInfo.Wheel info, CallbackInfo ci) {
        if (v.getMainEngineDamaged() && VehicleCombatConfig.LOCK_ON_ENGINE_DESTROYED.get()
                && VehicleCombatConfig.LOCK_WHEEL_VEHICLE.get()) lockHorizontal(v);
    }

    // shipEngine / helicopterEngine 与 wheelEngine 同构，分别用 LOCK_SHIP / LOCK_HELICOPTER 开关
}
```

> **为什么这样安全**：`清空输入 + 归零功率`正是卓越前线自己在"能量耗尽（`energy <= energyCost`）"和"无乘客"分支里的做法，
> 因此不会与它自身逻辑冲突。水平锁死放在 `TAIL`，保证本 tick 的位移不会再被引擎改回去。

### 3.5 维修工具规则 —— `RepairToolVehicleMixin`

- **目标**：`RepairToolItem#onRayHitEntity`（`remap = false`），`@Inject(HEAD, cancellable = true)`
- **原生行为**（载具分支）：

```
敌对（上一任驾驶员不同队）/ 玩家潜行 → hurt(0.5)                        // 破坏
非残骸                              → heal(0.5 + 0.0025 * maxHealth)  // 小幅回血、无上限
残骸                                → hurt(0.5 + 0.0025 * maxHealth)  // 破坏
```

- **新规则**（只接管"非残骸 + 非破坏"这条维修分支）：
  - 部件（5 个）恢复到各自上限（`repair_tool_full_part_repair`）
  - 整车血量最多恢复到 `maxHealth * repair_tool_body_cap`（默认 15%）：低于上限补到上限，**已达上限则不做任何改变**
  - **破坏分支（敌对 / 潜行 / 残骸）完全不拦截**，沿用原逻辑
  - 接管后必须**自己补上原方法在同一分支里发的音效与命中粒子**，否则玩家看不到/听不到反馈

```java
@Mixin(value = RepairToolItem.class, remap = false)
public abstract class RepairToolVehicleMixin {

    @Inject(method = "onRayHitEntity", at = @At("HEAD"), cancellable = true)
    private void yourmod$repairPartsAndCapBody(Entity shooter, ServerLevel level, GunData data,
                                               EntityResult result, Vec3 shootPos, Vec3 shootDir,
                                               CallbackInfo ci) {
        if (!(result.getEntity() instanceof VehicleEntity vehicle) || vehicle.isWreck()) return; // 交给原逻辑

        Entity lastDriver = EntityFindUtil.findEntity(level, vehicle.getLastDriverUUID());
        boolean sabotage = (lastDriver != null && !SeekTool.IN_SAME_TEAM.test(shooter, lastDriver)
                && lastDriver.getTeam() != null) || shooter.isShiftKeyDown();
        if (sabotage) return;   // 破坏分支沿用原逻辑

        if (VehicleCombatConfig.REPAIR_TOOL_FULL_PART_REPAIR.get()) {
            vehicle.setTurretHealth(vehicle.getTurretMaxHealth());
            vehicle.setLeftWheelHealth(vehicle.getLeftWheelMaxHealth());
            vehicle.setRightWheelHealth(vehicle.getRightWheelMaxHealth());
            vehicle.setMainEngineHealth(vehicle.getMainEngineMaxHealth());
            vehicle.setSubEngineHealth(vehicle.getSubEngineMaxHealth());
        }

        float cap = vehicle.getMaxHealth() * VehicleCombatConfig.REPAIR_TOOL_BODY_CAP.get().floatValue();
        if (vehicle.getHealth() < cap) vehicle.setHealth(cap);

        // 补齐原分支的音效与粒子
        RepairToolItem self = (RepairToolItem) (Object) this;
        Vec3 hit = result.getHitPos();
        level.playSound(null, hit.x, hit.y, hit.z, self.getRayHitEntitySound(data),
                SoundSource.PLAYERS, 0.7F, (float) ((2 * Math.random() - 1) * 0.05f + 1.0f));
        self.summonRayHitParticle(level, null, hit, shootDir.scale(-1).normalize());
        ci.cancel();
    }
}
```

> 注意：`getWheelMaxHealth()` / `getEngineMaxHealth()` 在**新版**卓越前线里已不再被伤害逻辑调用（拆成了
> `getLeftWheelMaxHealth/getRightWheelMaxHealth/getMainEngineMaxHealth/getSubEngineMaxHealth`，值来自 JSON 的 `PartHealth`）。
> 移植时**必须按你所用版本的实际 getter 名字**调用，用 `javap` 核对。

---

## 4. 注册

1. 目标模组的 mixin 配置 JSON（如 `yourmod.mixins.json`）的 **`"mixins"`** 数组加入：

```json
"mixins": [
  "VehicleRegenMixin",
  "VehiclePartRegenMixin",
  "VehicleTurretSpeedMixin",
  "VehicleEngineDamageMixin",
  "RepairToolVehicleMixin"
]
```

2. 确认 `mods.toml` 的 `MixinConfigs` 已包含该 JSON；`"package"` 与 mixin 实际包名一致
3. 5 个类都放在 `com.yourmod.mixin`（或你既有 mixin 包）下，注意 §1 的 `remap` 规则

---

## 5. 验收清单（游戏内逐项测）

1. **基线**：所有开关关闭 → 与卓越前线原生逐值一致（回血、断带功率 0.975/0.93、炮塔 0.2、维修工具可修满）
2. 开关 `disable_passive_regen` → 整车血量与 5 个部件都不再自愈；低血量自损仍在
3. 打断**单侧履带** → 该侧履带/负重轮静止、按 W/S 只能画圈、功率被压到 25%
4. 打断**双侧履带**或**发动机** → 原地不动（履带/轮式/船/直升机分别验证；固定翼应保持原生）
5. 打坏**炮塔** → 转向速度降到 5%
6. **维修工具**：部件一次修满、整车 15% 封顶且不再上涨；敌对/潜行/残骸仍是破坏行为（有音效与粒子）
7. 单独关掉某一项 → 只有该项恢复原生，其它项仍生效

---

## 6. 注意事项与坑

- **不要**重定向 `heal(float)` / `setHealth(float)`：原版成员，调用点是 SRG 名
- `handlePartHealth` 是**静态**方法：`@Inject` 的处理方法也必须 `static`
- `handlePartHealth` 客户端与服务端都会执行：用 `ThreadLocal` 快照，别用普通静态字段
- 部件血量/损坏标志是 `SynchedEntityData`：服务端写回后两端一致，不要在客户端做写回
- `@ModifyConstant` 依赖"常量唯一"：superb 更新后可能失配 → 每次升级用 `javap`/反编译复核
- 与其它注入同一方法的 mixin 冲突：本方案在 `trackEngine` 等方法用 `HEAD/TAIL`，较安全；但若其它模组也在
  同一方法注入并 `cancel`，要注意优先级与冲突日志
- **固定翼 `aircraftEngine` 不改**；`tomEngine` / `wheelChairEngine` / `airShipEngine` 默认也不改
- `SERVER` 型配置改动后需要 `/reload`（或重连）才生效，文档里要写清楚
- 默认值必须与原实现一致（`0.05 / 0.15 / 0.25 / true`），否则"关闭全部开关"的回归基线会对不上

---

## 附录 A：已核对的方法签名（superbwarfare 0.8.9.1-snapshot / 8689629）

```
com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity
  public float  repairAmount();
  public float  getPower();                     public void setPower(float);
  public boolean forwardInputDown();            public void setForwardInputDown(boolean);
  public boolean backInputDown();               public void setBackInputDown(boolean);
  public boolean leftInputDown();               public void setLeftInputDown(boolean);
  public boolean rightInputDown();              public void setRightInputDown(boolean);
  public float  getLeftTrack();                 public void setLeftTrack(float);
  public float  getRightTrack();                public void setRightTrack(float);
  public float  getLeftWheelRot();              public void setLeftWheelRot(float);
  public float  getRightWheelRot();             public void setRightWheelRot(float);
  public float  getTurretHealth();              public void setTurretHealth(float);
  public float  getLeftWheelHealth();           public void setLeftWheelHealth(float);
  public float  getRightWheelHealth();          public void setRightWheelHealth(float);
  public float  getMainEngineHealth();          public void setMainEngineHealth(float);
  public float  getSubEngineHealth();           public void setSubEngineHealth(float);
  public boolean getTurretDamaged();            public boolean getLeftWheelDamaged();
  public boolean getRightWheelDamaged();        public boolean getMainEngineDamaged();
  public boolean getSubEngineDamaged();
  public boolean isWreck();
  public float  getTurretMaxHealth();           public float getLeftWheelMaxHealth();
  public float  getRightWheelMaxHealth();       public float getMainEngineMaxHealth();
  public float  getSubEngineMaxHealth();
  public float  getHealth();                    public void setHealth(float);       // 整车血量
  public float  getMaxHealth();
  public java.lang.String getLastDriverUUID();  public void setLastDriverUUID(java.lang.String);
  public net.minecraft.world.entity.Entity getLastDriver();
  // getDeltaMovement() / setDeltaMovement(Vec3) 继承自原版 Entity
  // EntityDataAccessor: HEALTH / TURRET_HEALTH / L_WHEEL_HEALTH / R_WHEEL_HEALTH /
  //                     MAIN_ENGINE_HEALTH / SUB_ENGINE_HEALTH / POWER /
  //                     TURRET_DAMAGED / L_WHEEL_DAMAGED / R_WHEEL_DAMAGED /
  //                     MAIN_ENGINE_DAMAGED / SUB_ENGINE_DAMAGED

com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleEngineUtils
  public static final void trackEngine(VehicleEntity, EngineInfo$Track);
  public static final void wheelEngine(VehicleEntity, EngineInfo$Wheel);
  public static final void shipEngine(VehicleEntity, EngineInfo$Ship);
  public static final void helicopterEngine(VehicleEntity, EngineInfo$Helicopter);
  public static final void aircraftEngine(VehicleEntity, EngineInfo$Aircraft);      // 不改
  public static final void tomEngine(VehicleEntity, EngineInfo$Tom6);                // 不改
  public static final void wheelChairEngine(VehicleEntity, EngineInfo$WheelChair);   // 不改
  public static final void airShipEngine(VehicleEntity, EngineInfo$AirShip);         // 不改

com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleEffectUtils
  public static final void handlePartHealth(VehicleEntity);
  public static final void adjustTurretAngle(VehicleEntity);

com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleWeaponUtils
  public static final void turretAutoAimFromVector(VehicleEntity, net.minecraft.world.phys.Vec3);

com.atsuishio.superbwarfare.item.gun.special.RepairToolItem
  public void onRayHitEntity(Entity, ServerLevel, GunData, EntityResult, Vec3, Vec3);
  public net.minecraft.sounds.SoundEvent getRayHitEntitySound(GunData);
  public void summonRayHitParticle(ServerLevel, BlockState, Vec3, Vec3);
```

## 附录 B：原生 vs 改造后 对照（默认配置下）

| 情况 | 卓越前线原生 | 本方案（默认值） |
|---|---|---|
| 整车回血 | 自动"呼吸回血" | 不回血 |
| 部件回血 | 每 tick 0.25% 上限 | 不回血 |
| 单侧履带报废 | 功率 ×0.975 + 轻微偏航 | 冻结该侧、只能画圈、功率 ≤25% |
| 双侧履带报废 | 功率 ×0.93 | 完全不能动 |
| 发动机报废（履带/轮式/船/直升机） | ×0.96 / ×0.875 / ×0.875 / ×0.98 | 完全不能动 |
| 炮塔报废 | 转速 ×0.2 | 转速 ×0.05 |
| 维修工具·部件 | 不回部件 | 一次修满 |
| 维修工具·整车 | 每下 +0.5+0.25% 上限，可修满 | 最多 15%，封顶后不再变化 |
| 维修工具·破坏分支 | 敌对/潜行/残骸 | 不变 |

---

### 交付要求（给实现者）

1. 5 个 Mixin + 1 个配置类，全部正式注册、可编译、`runClient` 可跑
2. 每项功能都有独立开关，全部关闭时与原生逐值一致
3. 提交时附：改动的文件清单、配置项说明、以及按 §5 的实测结果（哪几项验过、怎么验的）

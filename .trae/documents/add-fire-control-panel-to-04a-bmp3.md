# 为 ZBD04A 和 BMP3 步战车添加火控面板

## 概述

将 FCP (Frontline-Combat-Pack) 模组中的曲射火控面板功能移植到 Dragon\_Rise 模组的 ZBD04A 和 BMP3 步战车上，复用 FCP 的 UI 组件代码和 API 逻辑。由于 Dragon\_Rise 不依赖 FCP，需要将相关代码复制并适配到 Dragon\_Rise 的包结构中。

**附加需求**（本次更新）：
1. **弹药类型限制**：火控面板仅在玩家选择主炮（100mm 最大口径弹药，武器索引 1）时激活；切换到自动炮（索引 0）或同轴机枪（索引 2）时自动停用并释放炮塔旋转限制。
2. **火控接管炮塔开关**：在面板内提供可切换的"火控接管炮塔"功能。启用时限制炮塔旋转（仅主炮）；禁用时即使使用主炮也允许玩家完全控制炮塔。

## 当前状态分析

### Dragon\_Rise 模组

* **Mod ID**: `dragonrise_reforge`

* **加载器**: Forge 1.20.1, Java 17

* **依赖**: SBW (Superb Warfare), GeckoLib, Curios, Kotlin for Forge

* **目标载具**:

  * [ZBD04AEntity.java](file:///home/shu/IdeaProjects/Dragon_Rise/src/main/java/com/redabysslucia/dragonrise_reforge/entities/ZBD04AEntity.java) — 继承 `GeoVehicleEntity`，有防浪板动画逻辑

  * [BMP3Entity.java](file:///home/shu/IdeaProjects/Dragon_Rise/src/main/java/com/redabysslucia/dragonrise_reforge/entities/BMP3Entity.java) — 继承 `GeoVehicleEntity`

* **网络**: [ModNetwork.java](file:///home/shu/IdeaProjects/Dragon_Rise/src/main/java/com/redabysslucia/dragonrise_reforge/network/ModNetwork.java) — 已有 `playToServer()` 辅助方法

* **主类**: [Dragonrise\_reforge.java](file:///home/shu/IdeaProjects/Dragon_Rise/src/main/java/com/redabysslucia/dragonrise_reforge/Dragonrise_reforge.java)

### FCP 火控系统组成
1. **弹道解算** (`firecontrol` 包): `IndirectFireBallistics`, `FireControlSolution`, `FireControlComputation`, `FireControlStatus`, `TrajectoryMode`
2. **载具基类**: `IndirectFireVehicleBase` (继承 `CamoVehicleBase` → `GeoVehicleEntity`)，`IndirectFireVehicle` 接口
3. **UI**: `FiringSolutionScreen` — 火控面板界面
4. **网络**: `FCPNetwork` + `SetFireControlMessage` — 客户端到服务端通信
5. **按键**: `FiringSolutionKeyHandler` — K 键打开面板
6. **Mixin**: [FireControlTurretAimMixin.java](file:///home/shu/IdeaProjects/Frontline-Combat-Pack/src/main/java/frontline/combat/fcp/mixins/FireControlTurretAimMixin.java) — 注入 `VehicleEntity.adjustTurretAngle`，火控激活时取消 SBW 的玩家视角炮塔瞄准

### 武器配置分析（ZBD04A / BMP3 通用）

从 [zbd04a.json](file:///home/shu/IdeaProjects/Dragon_Rise/src/main/resources/data/dragonrise_reforge/sbw/vehicles/zbd04a.json#L52-L58) 和 [bmp3.json](file:///home/shu/IdeaProjects/Dragon_Rise/src/main/resources/data/dragonrise_reforge/sbw/vehicles/bmp3.json#L52-L58) 的座位 0（炮塔座位）配置：
```json
"Weapons": [
    "Cannon",         // 索引 0 - 30mm 自动炮
    "100MM_Cannon",   // 索引 1 - 100mm 主炮（最大口径）
    "MainMachineGun"  // 索引 2 - 同轴机枪
]
```

**主炮索引 = 1**（`100MM_Cannon`，Damage 200，最大口径）

### SBW 关键 API（已在 Dragon_Rise 中使用）
- `getSelectedWeapon(int seatIndex)` → `int`：获取座位当前选中的武器索引（见 [M3A3Entity.java:64](file:///home/shu/IdeaProjects/Dragon_Rise/src/main/java/com/redabysslucia/dragonrise_reforge/entities/M3A3Entity.java#L64) 和 [TJGCEntity.java:166](file:///home/shu/IdeaProjects/Dragon_Rise/src/main/java/com/redabysslucia/dragonrise_reforge/entities/TJGCEntity.java#L166)）
- `getTurretControllerIndex()` → `int`：获取炮塔控制器座位索引
- `setMouseMoveSpeedX/Y(float)`：清除鼠标移动输入（FCP `IndirectFireVehicleBase.java:218-219`）
- `turretAutoAimFromVector(Vec3)`：自动瞄准指定方向（FCP `IndirectFireVehicleBase.java:223`）
- `adjustTurretAngle`：SBW 每帧根据玩家视角调整炮塔的方法（FCP 通过 Mixin 取消）

### Mixin 配置
- [dragonrise_reforge.mixins.json](file:///home/shu/IdeaProjects/Dragon_Rise/src/main/resources/dragonrise_reforge.mixins.json) — 已有 mixin 配置，包名 `com.redabysslucia.dragonrise_reforge.mixin`

### 关键设计决策

* **跳过 CamoVehicleBase**: Dragon\_Rise 载具不需要涂装切换功能，新建的 `IndirectFireVehicleBase` 直接继承 `GeoVehicleEntity`

* **移除 DelayedMortarVehicleBase 引用**: FCP 的 Screen 中引用了此迫击炮类，Dragon\_Rise 不需要，简化为默认 `TrajectoryMode.LOW`

* **复用 SBW API**: 两个模组都依赖 SBW，火控系统使用的 `VehicleEntity` 方法（`getTurretControllerIndex()`, `getShootPos()`, `turretAutoAimFromVector()` 等）在 Dragon\_Rise 中同样可用

* **保留 FiringParametersItem 交互**: SBW 的射击参数物品交互功能保留，因为 Dragon\_Rise 也依赖 SBW

* **主炮索引常量**: 定义 `MAIN_CANNON_WEAPON_INDEX = 1`，对应 JSON 配置中的 `100MM_Cannon`

* **弹药类型限制策略**: 在 `tickFireControl()` 中检测当前武器，若不是主炮则自动调用 `clearFireControl()`，并通知玩家

* **火控接管开关策略**: 新增 `FIRE_CONTROL_TAKEOVER` 同步数据；只有当 `isFireControlActive() && isFireControlTakeoverEnabled()` 同时为 true 时，才执行炮塔限制（`setMouseMoveSpeedX/Y`、`turretAutoAimFromVector` 以及 Mixin 取消 `adjustTurretAngle`）

* **Mixin 适配**: 复制 FCP 的 `FireControlTurretAimMixin`，条件改为检查火控激活且接管开启

## 实施方案

### 第 1 步: 创建 firecontrol 包（弹道解算逻辑）

将 FCP 的 `frontline.combat.fcp.firecontrol` 包复制到 `com.redabysslucia.dragonrise_reforge.firecontrol`，内容几乎不变（仅修改包名）:

| 新文件路径                                                                                        | 来源                                | 说明                                                                               |
| -------------------------------------------------------------------------------------------- | --------------------------------- | -------------------------------------------------------------------------------- |
| `src/main/java/com/redabysslucia/dragonrise_reforge/firecontrol/TrajectoryMode.java`         | FCP `TrajectoryMode.java`         | 弹道模式枚举 (LOW/HIGH)，翻译 key 前缀改为 `dragonrise_reforge`                               |
| `src/main/java/com/redabysslucia/dragonrise_reforge/firecontrol/FireControlStatus.java`      | FCP `FireControlStatus.java`      | 火控状态枚举，翻译 key 前缀改为 `dragonrise_reforge`                                          |
| `src/main/java/com/redabysslucia/dragonrise_reforge/firecontrol/FireControlSolution.java`    | FCP `FireControlSolution.java`    | 解算结果 record，无修改                                                                  |
| `src/main/java/com/redabysslucia/dragonrise_reforge/firecontrol/FireControlComputation.java` | FCP `FireControlComputation.java` | 解算结果包装类，无修改                                                                      |
| `src/main/java/com/redabysslucia/dragonrise_reforge/firecontrol/IndirectFireBallistics.java` | FCP `IndirectFireBallistics.java` | 弹道计算工具类，无修改（引用 SBW 的 `VehicleEntity`, `VehicleVecUtils`, `TrajectoryCalculator`） |

### 第 2 步: 创建载具火控基类

| 新文件路径                                                                                              | 来源                                 | 说明                                                                                                                                         |
| -------------------------------------------------------------------------------------------------- | ---------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| `src/main/java/com/redabysslucia/dragonrise_reforge/entities/vehicle/IndirectFireVehicle.java`     | FCP `IndirectFireVehicle.java`     | 火控载具接口，新增 `isFireControlTakeoverEnabled()` 和 `setFireControlTakeover(boolean)` 方法声明 |
| `src/main/java/com/redabysslucia/dragonrise_reforge/entities/vehicle/IndirectFireVehicleBase.java` | FCP `IndirectFireVehicleBase.java` | **适配**: 直接继承 `GeoVehicleEntity`（跳过 `CamoVehicleBase`），移除 `ICamoVehicle` 引用；翻译 key 前缀改为 `dragonrise_reforge`；保留 `FiringParametersItem` 交互逻辑；**新增武器检测和接管开关逻辑** |

`IndirectFireVehicleBase` 的关键改动:

* `extends CamoVehicleBase` → `extends GeoVehicleEntity`

* 移除 `implements ICamoVehicle` 相关代码

* 翻译 key: `message.fcp.fire_control.*` → `message.dragonrise_reforge.fire_control.*`

* 保留 `baseTick()` 中的 `tickFireControl()` 调用

* 保留 `vehicleShoot()` 覆写用于间接射击

* 保留 `interact()` 中的 FiringParametersItem 处理

* 保留 NBT 持久化逻辑

**新增：弹药类型限制逻辑**

添加常量和方法:
```java
// 主炮武器索引（对应 JSON 配置中的 "100MM_Cannon"）
protected static final int MAIN_CANNON_WEAPON_INDEX = 1;

// 检测炮塔控制器当前是否选择了主炮
protected boolean isMainCannonSelected() {
    int seatIndex = getTurretControllerIndex();
    if (seatIndex < 0) return false;
    return getSelectedWeapon(seatIndex) == MAIN_CANNON_WEAPON_INDEX;
}
```

修改 `tickFireControl()` 方法，在开头添加武器类型检测:
```java
private void tickFireControl() {
    if (!isFireControlActive()) {
        stationaryTicks = 0;
        setFireControlStatus(FireControlStatus.INACTIVE);
        return;
    }
    // 新增：如果当前武器不是主炮，自动停用火控
    if (!isMainCannonSelected()) {
        clearFireControl(null);
        // 通知炮塔控制器座位的玩家
        notifyWeaponChanged();
        return;
    }
    // ... 原有逻辑
}
```

**新增：火控接管开关**

添加同步数据:
```java
private static final EntityDataAccessor<Boolean> FIRE_CONTROL_TAKEOVER =
        SynchedEntityData.defineId(IndirectFireVehicleBase.class, EntityDataSerializers.BOOLEAN);

// defineSynchedData() 中
this.entityData.define(FIRE_CONTROL_TAKEOVER, false);

// 新增方法
@Override
public boolean isFireControlTakeoverEnabled() {
    return this.entityData.get(FIRE_CONTROL_TAKEOVER);
}

public void setFireControlTakeover(boolean enabled, Entity actor) {
    if (this.level().isClientSide) return;
    this.entityData.set(FIRE_CONTROL_TAKEOVER, enabled);
    if (actor instanceof Player player) {
        player.displayClientMessage(Component.translatable(
                enabled ? "message.dragonrise_reforge.fire_control.takeover_enabled"
                        : "message.dragonrise_reforge.fire_control.takeover_disabled"
        ), true);
    }
}
```

修改 `tickFireControl()` 中的炮塔限制部分，添加接管开关检查:
```java
// 原有代码（仅当接管开启时执行）
if (isFireControlTakeoverEnabled()) {
    setMouseMoveSpeedX(0.0f);
    setMouseMoveSpeedY(0.0f);
    turretAutoAimFromVector(desired);
    // ... 测量误差逻辑
} else {
    // 接管关闭：不限制炮塔，状态保持 ALIGNING 或根据其他条件
    setFireControlStatus(FireControlStatus.ALIGNING);
}
```

修改 NBT 持久化:
```java
// addAdditionalSaveData()
tag.putBoolean("FCPFireControlTakeover", isFireControlTakeoverEnabled());

// readAdditionalSaveData()
if (tag.contains("FCPFireControlTakeover")) {
    this.entityData.set(FIRE_CONTROL_TAKEOVER, tag.getBoolean("FCPFireControlTakeover"));
}
```

### 第 3 步: 创建网络消息

| 新文件路径                                                                                           | 来源                               | 说明                                                                                |
| ----------------------------------------------------------------------------------------------- | -------------------------------- | --------------------------------------------------------------------------------- |
| `src/main/java/com/redabysslucia/dragonrise_reforge/network/message/SetFireControlMessage.java` | FCP `SetFireControlMessage.java` | 适配: 引用 Dragon\_Rise 的 `IndirectFireVehicleBase`；通过 `ModNetwork.PACKET_HANDLER` 发送；**新增 `takeover` 字段** |
| `src/main/java/com/redabysslucia/dragonrise_reforge/network/message/ToggleTakeoverMessage.java` | 新建 | 用于切换火控接管炮塔开关，仅包含 `entityId` 和 `takeover` 布尔值 |

**`SetFireControlMessage` 改动**：在原有 `apply`/`clear` 消息中添加 `takeover` 字段，应用火控时同时设置接管状态:
```java
public record SetFireControlMessage(
        int entityId,
        boolean clear,
        BlockPos target,
        int radius,
        TrajectoryMode trajectoryMode,
        boolean takeover  // 新增
) { ... }
```

**`ToggleTakeoverMessage` 结构**:
```java
public record ToggleTakeoverMessage(int entityId, boolean takeover) {
    // encode: writeVarInt(entityId), writeBoolean(takeover)
    // decode: readVarInt(), readBoolean()
    // handle: 获取载具，验证玩家是炮塔控制器，调用 setFireControlTakeover(takeover, player)
}
```

### 第 4 步: 创建客户端 UI

| 新文件路径                                                                                        | 来源                                  | 说明                                                                                                                                                                          |
| -------------------------------------------------------------------------------------------- | ----------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `src/main/java/com/redabysslucia/dragonrise_reforge/client/screen/FiringSolutionScreen.java` | FCP `FiringSolutionScreen.java`     | 适配: 引用 Dragon\_Rise 的类；网络发送改为 `ModNetwork.PACKET_HANDLER.sendToServer(...)`；移除 `DelayedMortarVehicleBase` 引用，默认弹道模式改为 `TrajectoryMode.LOW`；翻译 key 前缀改为 `dragonrise_reforge`；**新增接管开关按钮和武器检测提示** |
| `src/main/java/com/redabysslucia/dragonrise_reforge/client/FireControlKeyHandler.java`       | FCP `FiringSolutionKeyHandler.java` | 适配: mod ID 改为 `dragonrise_reforge`；引用 Dragon\_Rise 的 `IndirectFireVehicleBase` 和 `FiringSolutionScreen`；按键保持 K 键；翻译 key 改为 `key.dragonrise_reforge.open_fire_control`       |

`FiringSolutionScreen` 的关键改动:

* 构造函数中 `vehicle instanceof DelayedMortarVehicleBase ? TrajectoryMode.HIGH : TrajectoryMode.LOW` → `TrajectoryMode.LOW`

* `FCPNetwork.FCP_HANDLER.sendToServer(...)` → `ModNetwork.PACKET_HANDLER.sendToServer(...)`

* 所有 `screen.fcp.fire_control.*` → `screen.dragonrise_reforge.fire_control.*`

**新增：接管开关按钮**

在 `init()` 中添加切换按钮（位于操作行上方）:
```java
private Button takeoverToggle;

// init() 中，在 apply/clear/close 按钮之前
int takeoverY = panelY + panelHeight - 42;
takeoverToggle = addRenderableWidget(Button.builder(
        Component.translatable("screen.dragonrise_reforge.fire_control.takeover"),
        button -> toggleTakeover()
).bounds(panelX + 10, takeoverY, 160, 16).build());
updateTakeoverLabel();
```

新增方法:
```java
private void toggleTakeover() {
    boolean newState = !vehicle.isFireControlTakeoverEnabled();
    ModNetwork.PACKET_HANDLER.sendToServer(new ToggleTakeoverMessage(vehicle.getId(), newState));
    updateTakeoverLabel();
}

private void updateTakeoverLabel() {
    if (takeoverToggle == null) return;
    boolean enabled = vehicle.isFireControlTakeoverEnabled();
    takeoverToggle.setMessage(Component.translatable("screen.dragonrise_reforge.fire_control.takeover")
            .append(": ")
            .append(Component.translatable(enabled
                    ? "screen.dragonrise_reforge.fire_control.takeover.on"
                    : "screen.dragonrise_reforge.fire_control.takeover.off")
                    .withStyle(enabled ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.GRAY)));
}
```

**新增：武器类型检测提示**

在 `tick()` 中检测当前武器，如果不是主炮则显示警告:
```java
@Override
public void tick() {
    super.tick();
    if (player.getVehicle() != vehicle) {
        onClose();
        return;
    }
    // 新增：检测当前武器是否为主炮
    if (vehicle instanceof IndirectFireVehicleBase base && !base.isMainCannonSelected()) {
        // 显示警告（可通过 apply 按钮禁用状态体现）
        apply.active = false;
    }
    updatePreview();
    updateTakeoverLabel();  // 同步接管开关状态
}
```

**新增：应用火控时发送接管状态**

修改 `applySolution()`:
```java
private void applySolution() {
    if (!preview.isSuccess()) return;
    try {
        BlockPos target = new BlockPos(...);
        int hitRadius = Integer.parseInt(radius.getValue());
        // 应用火控时同时启用接管
        ModNetwork.PACKET_HANDLER.sendToServer(SetFireControlMessage.apply(
                vehicle.getId(), target, hitRadius, trajectoryMode, true
        ));
    } catch (NumberFormatException ignored) { ... }
}
```

### 第 5 步: 修改目标载具实体

**ZBD04AEntity.java** — [当前代码](file:///home/shu/IdeaProjects/Dragon_Rise/src/main/java/com/redabysslucia/dragonrise_reforge/entities/ZBD04AEntity.java):

* `extends GeoVehicleEntity` → `extends IndirectFireVehicleBase`

* 添加 import: `com.redabysslucia.dragonrise_reforge.entities.vehicle.IndirectFireVehicleBase`

* 保持现有的 `defineSynchedData()`, `travel()`, `tick()`, 动画控制器等逻辑不变

* `tick()` 中已调用 `super.tick()`，火控逻辑通过 `baseTick()` → `tickFireControl()` 链自动执行

**BMP3Entity.java** — [当前代码](file:///home/shu/IdeaProjects/Dragon_Rise/src/main/java/com/redabysslucia/dragonrise_reforge/entities/BMP3Entity.java):

* `extends GeoVehicleEntity` → `extends IndirectFireVehicleBase`

* 添加 import: `com.redabysslucia.dragonrise_reforge.entities.vehicle.IndirectFireVehicleBase`

* 保持现有的 `getDamageModifier()` 和动画控制器逻辑不变

### 第 6 步: 注册网络消息

修改 [ModNetwork.java](file:///home/shu/IdeaProjects/Dragon_Rise/src/main/java/com/redabysslucia/dragonrise_reforge/network/ModNetwork.java):

* 在 `register()` 方法中添加:

```java
playToServer(SetFireControlMessage.class, SetFireControlMessage::encode, SetFireControlMessage::decode, SetFireControlMessage::handle);
playToServer(ToggleTakeoverMessage.class, ToggleTakeoverMessage::encode, ToggleTakeoverMessage::decode, ToggleTakeoverMessage::handle);
```

### 第 7 步: 创建火控炮塔 Mixin

| 新文件路径 | 来源 | 说明 |
|---|---|---|
| `src/main/java/com/redabysslucia/dragonrise_reforge/mixin/FireControlTurretAimMixin.java` | FCP [FireControlTurretAimMixin.java](file:///home/shu/IdeaProjects/Frontline-Combat-Pack/src/main/java/frontline/combat/fcp/mixins/FireControlTurretAimMixin.java) | 注入 `VehicleEntity.adjustTurretAngle`，当火控激活且接管开启时取消 SBW 的玩家视角炮塔瞄准 |

```java
@Mixin(value = VehicleEntity.class, remap = false)
public abstract class FireControlTurretAimMixin {

    @Inject(method = "adjustTurretAngle", at = @At("HEAD"), cancellable = true)
    private void dragonrise$skipLookAimWhenFireControlActive(CallbackInfo ci) {
        VehicleEntity self = (VehicleEntity) (Object) this;
        // 仅当火控激活且接管开启时取消炮塔自动瞄准
        if (self instanceof IndirectFireVehicleBase vehicle
                && vehicle.isFireControlActive()
                && vehicle.isFireControlTakeoverEnabled()) {
            ci.cancel();
        }
    }
}
```

修改 [dragonrise_reforge.mixins.json](file:///home/shu/IdeaProjects/Dragon_Rise/src/main/resources/dragonrise_reforge.mixins.json)，在 `mixins` 数组中添加:
```json
"mixins": [
    "VehicleEntityLeadMixin",
    "FireControlTurretAimMixin"
]
```

### 第 8 步: 添加语言文件条目

修改 [en\_us.json](file:///home/shu/IdeaProjects/Dragon_Rise/src/main/resources/assets/dragonrise_reforge/lang/en_us.json) 和 [zh\_cn.json](file:///home/shu/IdeaProjects/Dragon_Rise/src/main/resources/assets/dragonrise_reforge/lang/zh_cn.json)，添加火控相关的翻译条目（翻译 key 前缀从 `fcp` 改为 `dragonrise_reforge`）:

**en\_us.json 新增:**

* `key.categories.dragonrise_reforge`: "Dragon Rising"

* `key.dragonrise_reforge.open_fire_control`: "Open Indirect Fire Control"

* `message.dragonrise_reforge.fire_control.applied`: "Fire control set: %1$s / %2$s / %3$s, radius %4$s m, %5$s"

* `message.dragonrise_reforge.fire_control.cleared`: "Fire control cleared"

* `message.dragonrise_reforge.fire_control.invalid_input`: "Invalid target coordinates or hit radius"

* `message.dragonrise_reforge.fire_control.blocked`: "Cannot fire: %1$s"

* `message.dragonrise_reforge.fire_control.weapon_changed`: "Fire control deactivated: main cannon not selected"

* `message.dragonrise_reforge.fire_control.takeover_enabled`: "Fire control turret takeover enabled"

* `message.dragonrise_reforge.fire_control.takeover_disabled`: "Fire control turret takeover disabled"

* `status.dragonrise_reforge.fire_control.*`: 各状态文本

* `screen.dragonrise_reforge.fire_control.*`: 面板 UI 文本

* `screen.dragonrise_reforge.fire_control.takeover`: "TURRET TAKEOVER"

* `screen.dragonrise_reforge.fire_control.takeover.on`: "ON"

* `screen.dragonrise_reforge.fire_control.takeover.off`: "OFF"

* `screen.dragonrise_reforge.fire_control.weapon_warning`: "MAIN CANNON REQUIRED"

**zh\_cn.json 新增:**

* 对应的中文翻译，参考 FCP 的 zh\_cn.json

* `message.dragonrise_reforge.fire_control.weapon_changed`: "火控已停用：未选择主炮"

* `message.dragonrise_reforge.fire_control.takeover_enabled`: "火控接管炮塔已启用"

* `message.dragonrise_reforge.fire_control.takeover_disabled`: "火控接管炮塔已禁用"

* `screen.dragonrise_reforge.fire_control.takeover`: "火控接管炮塔"

* `screen.dragonrise_reforge.fire_control.takeover.on`: "开启"

* `screen.dragonrise_reforge.fire_control.takeover.off`: "关闭"

* `screen.dragonrise_reforge.fire_control.weapon_warning`: "需要选择主炮"

## 假设与决策

1. **复制代码而非添加 FCP 依赖**: FCP 不是 Dragon\_Rise 的依赖项，用户明确指出 FCP 源码位置以便复用代码
2. **跳过 CamoVehicleBase**: Dragon\_Rise 载具不需要涂装切换功能，直接继承 `GeoVehicleEntity` 减少不必要的耦合
3. **保留 FiringParametersItem 交互**: 两个模组都依赖 SBW，此功能可以正常工作
4. **按键使用 K 键**: 与 FCP 保持一致，用户可在控制设置中修改
5. **不修改现有载具功能**: ZBD04A 的防浪板动画、BMP3 的伤害修正等逻辑保持不变
6. **翻译 key 前缀统一为** **`dragonrise_reforge`**: 保持与模组现有翻译风格一致

## 验证步骤

1. **编译验证**: `./gradlew compileJava` 确保无编译错误
2. **运行时验证**:
   * 生成 ZBD04A 和 BMP3 载具
   * 驾驶载具时按 K 键打开火控面板
   * 输入目标坐标，验证解算结果显示（射程、方位角、仰角、飞行时间）
   * 点击"应用"按钮，验证炮塔自动转向目标
   * 验证状态显示（对准中/就绪等）
   * 点击"清除"按钮，验证火控解除
   * 验证移动时状态变为"稳定中"
3. **弹药类型限制验证**:
   * 选择主炮（武器索引 1）时，火控面板正常工作
   * 切换到自动炮（武器索引 0）时，火控自动停用，炮塔恢复自由旋转
   * 切换到同轴机枪（武器索引 2）时，火控自动停用，炮塔恢复自由旋转
   * 重新切换回主炮时，可重新激活火控
4. **火控接管开关验证**:
   * 启用接管时：炮塔根据火控参数自动转向，玩家无法手动旋转炮塔
   * 禁用接管时：即使火控已激活，玩家仍可完全手动控制炮塔旋转
   * 接管开关状态在面板中正确显示（绿色"开启"/灰色"关闭"）
   * 切换接管状态时，玩家收到提示消息
5. **兼容性验证**:
   * ZBD04A 的防浪板动画在水中仍正常工作
   * BMP3 的伤害修正功能不受影响
   * 载具的正常射击功能（非火控模式下）不受影响
   * Mixin 不影响其他载具（仅作用于 `IndirectFireVehicleBase` 实例）
6. **持久化验证**: 退出并重新进入游戏，火控状态和接管开关状态正确保存/恢复


# FCP 旧版载具数据包迁移到新版说明

## 适用范围与迁移目标

旧环境：服务器使用旧版 FCP，载具配置与 Dragonrise 一样，通常通过 `data/fcp/sbw/vehicles/<车型>.json` 覆盖。

目标环境：升级到本地已检查的 FCP 2.0.1 源码版本（Git 提交 `e4188472`）。该版本增加了单人／多人配置选择、分类目录重映射、可选装甲系统，以及独立的补给和音效机制。若实际升级的 JAR 与此版本不同，应以目标 JAR 内容为准。

目标是保留旧数据包中有意调整的数值，同时保留新版载具的结构与功能。本说明是迁移方法；尚未对服务器实际旧数据包逐车执行迁移。

## 1. 迁移原则：以新版完整文件为底稿

不要仅移动旧文件后直接覆盖新版。载具 JSON 是整份替换，不是自动合并；旧文件可能缺少新版的座位、OBB、武器、相机、弹种、音效引用等内容。

推荐对每台车进行三方对照：

1. **旧版模组原文件**：旧数据包修改前的基准。
2. **旧数据包文件**：找出服务器主动调整过哪些字段。
3. **新版模组原文件**：作为迁移后的底稿，只迁入第 2 项相对第 1 项的必要差异。

例如，旧原版 `Damage = 500`，服务器改成 `600`，新版原版变成 `700`：先确定服务器的意图是“固定为 600”，还是“原版基础上提高 20%”。前者迁为 600，后者迁为 840。不能把旧文件中所有与新版不同的值都当作服务器自定义。

若找不到旧版原文件，应先保存旧数据包，人工列出确定需要保留的数值，再逐项迁入新版；无法确认的结构和数值采用新版基准。

## 2. 旧路径迁移到新版真实路径

新版在 Superb 完成加载后，还会整理载具数据：

```text
当前选中的 crew 配置 > 分类目录中的共享配置 > vehicles 根目录平铺配置
```

- `single_crew/`：单人操纵配置。
- `multicrew/`：多人分工配置。
- 其它分类目录（包括 `shared/`、`helicopter/` 等）：两种模式共用。
- 未选中的 crew 目录会被丢弃。
- 子目录最后会被去掉，按命名空间和文件名映射到实体，例如 `fcp:single_crew/abrams/m1a2` 最终映射成 `fcp:m1a2`。

这不是普通的数据包优先级：即使旧数据包具有更高优先级，里面的平铺 `m1a2.json` 仍可能被新版分类目录中的同车型覆盖。应覆盖目标 JAR 中的**相同完整路径**。

### 已核实的迁移示例

| 旧数据包路径 | 新数据包目标路径 |
|---|---|
| `data/fcp/sbw/vehicles/m1a2.json` | `data/fcp/sbw/vehicles/single_crew/abrams/m1a2.json` 和／或 `data/fcp/sbw/vehicles/multicrew/abrams/m1a2.json` |
| `data/fcp/sbw/vehicles/bmp2.json` | `data/fcp/sbw/vehicles/single_crew/bmp/bmp2.json` 和／或 `data/fcp/sbw/vehicles/multicrew/bmp/bmp2.json` |
| `data/fcp/sbw/vehicles/uh60.json` | `data/fcp/sbw/vehicles/helicopter/uh60.json` |

其它车型必须在目标 JAR 中查找，不要按车辆名称自行猜目录。实体 ID 有改名、拆分或移除时，需要另列映射，不能只改文件名。

### 两种 crew 模式如何处理

服务器的 `config/fcp-common.toml`：

```toml
[vehicles]
multicrew_vehicles = false
```

- `false` 使用 `single_crew/`。
- `true` 使用 `multicrew/`。

只支持服务器固定模式时，迁移对应版本即可。若要支持以后切换模式，应分别以新版两份文件为底稿迁入数值，不能把同一份单人文件复制到多人目录，否则会破坏驾驶员／炮手的座位和武器分工。

不要为同一车型额外创建多个共享分类文件，例如同时放 `shared/m1a2.json` 和 `tanks/m1a2.json`。它们会映射到同一个 ID，而同层冲突没有可依赖的稳定优先级。

> 当前源码中的 `FCPConfig` 类注释仍提到旧的内置数据包机制；实际迁移依据应是 `VehicleDataReorganizer` 的目录选择与重映射逻辑。

## 3. 按类别迁移旧数值

| 旧数据包修改内容 | 新版迁移方法 |
|---|---|
| 血量、能源 | 将目标值迁入新版 `MaxHealth`、`MaxEnergy`，再测试当前状态与最大值 |
| 动力、转向、升力 | 在新版对应 `EngineType` 的 `EngineInfo` 中调整；检查新版参数基准和含义 |
| 直击／爆炸伤害、弹速、装填、射速 | 迁入新版对应武器；先核对武器键、弹种和 `AmmoType.Override` |
| 炮塔转速、射界 | 保留新版轴点和变换结构，迁入明确需要的速度或角度限制 |
| 准星与 HUD 颜色 | 迁入新版实际使用的武器配置，保留有效的准星名称 |
| 座位、相机、枪口、OBB | 默认保留新版；旧数据基于旧模型，只有核对新版模型后才迁移 |
| 防护／伤害倍率 | 按下一节选择旧伤害逻辑或新版装甲系统 |
| 携弹量／补给数量 | 同时检查新版 `fcp_armor` 的 `ammo`，不能只改武器 `Magazine` |
| 开火／引擎音效 | 核对独立客户端音效机制，不能只搬旧 `SoundInfo` |

不要只按同名武器键匹配。新版可能重命名武器、拆分弹种或改变弹种顺序；应逐项核对发射物、弹药物品和弹种用途。

## 4. 伤害与装甲：先选择兼容方向

新版装甲系统是世界设置，管理界面为 F8；本地检查版本默认关闭。实际服务器已有世界保存的设置可能不同，升级后应核对。

### 方案 A：优先保留旧数据包的伤害规则

关闭新版 FCP 装甲系统，然后把旧版主动调整的 `DamageModifiers` 等数值迁入新版载具文件。

此时符合原版路径的伤害继续使用 Superb 的伤害修正。关闭装甲系统不等于关闭所有新版代码：例如个别车型的顶部防护、实体类中的修正仍可能参与计算，需要实测。

### 方案 B：采用新版装甲系统

开启装甲系统，并以新版完整文件为底稿覆盖：

```text
data/fcp/fcp_armor/<车型>.json
```

| 字段 | 用途 |
|---|---|
| `armor` | 车体正面、侧面、后部、顶部及炮塔装甲 |
| `modifiers` | 对 kinetic、heat、hat、explosive 等伤害类型的倍率 |
| `weapons` | 本车武器的伤害类型和穿深 `pen` |
| `ammo` | 各武器／弹种的补给目标数量 |

启用后，系统覆盖的伤害会按穿深、装甲和类型倍率重新计算，不能只靠载具 JSON 的 `DamageModifiers` 调整。弹丸基础伤害仍需在载具武器配置中检查。

旧版“正面减伤百分比”和新版“装甲毫米值”不存在直接通用换算，应分别针对动能弹、破甲弹、机枪、爆炸等测试。不能把旧倍率直接填写成装甲厚度。

`fcp_armor` 本身不分单人／多人目录，同一车型共用一份。若两个模式的武器键或弹种顺序不同，需核对这份配置是否覆盖了两者的实际键。

## 5. 携弹量与补给也要迁移

新版补给和“生成时满载”会读取 `fcp_armor/<车型>.json` 的 `ammo`，这与装甲系统是否开启是两回事。

例如：

```json
"ammo": {
  "Cannon": [21, 1],
  "Cannon#1": [14, 1],
  "Coax": [1, 2000]
}
```

含义：

- 数组格式为 `[弹匣数, 每匣发数]`，两者相乘为该项目标数量。
- `Cannon` 对应第 0 种弹药；`Cannon#1` 对应第 1 种，索引从 0 开始。
- `Coax` 必须与载具配置中的武器键一致。
- 系统会考虑已装填弹药和库存；共用弹药物品的武器会合并为弹药池。

这些数值控制补给目标，不等同于武器本身的 `Magazine`。迁移旧携弹量时，应检查两处设置，并测试库存是否有足够容量；部分库存大小和物品限制在 Java 中定义。

## 6. 音效与代码行为的兼容

### 音效

新版部分车型的载具 JSON 将原生开火音效设为 `fcp:squad.silent`，实际开火由独立的 Squad 音效系统播放。引擎也可能使用独立分层播放器。

因此不要直接恢复旧版全部 `SoundInfo`，否则可能和新版播放器叠加。只迁移战斗数值时，保留新版音效配置。

如需迁移自定义音效，应另行检查客户端资源：

```text
assets/fcp/squad_weapons/
assets/fcp/squad_engines/
```

这部分应通过客户端资源包处理，不能只依靠服务器数据包下发；独立播放器的适用车型等还受代码控制。

### 代码行为

数据包迁移不会恢复旧版全部行为。新版的发动机启停、直升机旋翼怠速和停转、地面贴合、涉水判断等可能由代码接管。改 `EngineInfo` 不等于能够修改启动时长或关闭这些机制。

同一数据包里的 Dragonrise 配置保持自己的命名空间和原路径，不要为了迁移 FCP 而一并移动到 crew 目录。新版 FCP 的目录整理器会扫描载具数据表中的其它命名空间，应避免为 Dragonrise 添加会改变其 ID 的额外分类层级。

## 7. 迁移后数据包结构示例

```text
vehicle_balance/
├─ pack.mcmeta
└─ data/
   ├─ fcp/
   │  ├─ sbw/vehicles/
   │  │  ├─ single_crew/abrams/m1a2.json
   │  │  ├─ multicrew/abrams/m1a2.json
   │  │  └─ helicopter/uh60.json
   │  └─ fcp_armor/
   │     └─ m1a2.json
   └─ dragonrise_reforge/
      └─ sbw/vehicles/
         └─ m1a2sepv1.json
```

只保留实际需要覆盖的文件。已迁移的旧 FCP 平铺文件应从新版数据包副本中移除，原始旧数据包另行备份。

若仍是 Minecraft 1.20.1，`pack.mcmeta` 的 `pack_format` 仍为 15；FCP 升级本身不要求改变它。若同时升级 Minecraft，应按目标 Minecraft 版本设置。

## 8. 上线与验证步骤

1. 备份旧数据包、旧模组、世界和服务器配置，在世界副本中测试迁移。
2. 安装目标 FCP 及其兼容的 Superb 版本；先确认新版自身能正常启动。
3. 放入迁移后的数据包，停用旧包，避免两份补丁同时参与覆盖。
4. 用 `/datapack list` 核对启用状态，将迁移包设为较高优先级，再执行 `/reload`。
5. 核对 `multicrew_vehicles` 和 F8 装甲设置。切换 crew 模式会让乘员下车，应在测试时安排好。
6. 查看日志：确认没有 JSON 解析错误，并检查 `[FCP] vehicle data reorganized` 输出的模式是否正确。
7. 每个已修改车型先用新生成载具测试：生命值、动力、装填、各弹种伤害、携弹量、补给、座位控制、碰撞和声音。
8. 若支持两种 crew 模式，两种模式都测试；只测单人不能证明多人配置已迁移成功。
9. 最后再测试旧世界中已有载具。当前血量、库存、燃料和其它实体保存状态不一定随配置自动重置；迁移数据包不等于迁移这些存档状态。

若回滚模组版本，应恢复对应的世界与配置备份；仅停用数据包不能撤销新版对存档或实体状态产生的全部变化。

## 核对依据

- 配置筛选及路径重映射：`src/main/java/frontline/combat/fcp/init/VehicleDataReorganizer.java`。
- 模式配置：`src/main/java/frontline/combat/fcp/FCPConfig.java`。
- 世界装甲开关：`src/main/java/frontline/combat/fcp/FcpWorldSettings.java`。
- 装甲、弹种与携弹量读取：`src/main/java/frontline/combat/fcp/balance/VehicleArmor.java`。
- 补给及满载逻辑：`src/main/java/frontline/combat/fcp/balance/VehicleRearm.java`。
- 独立开火音效：`src/main/java/frontline/combat/fcp/entity/vehicle/engine/SquadWeaponFire.java`。

以上均指目标 FCP 项目的实际 `src/main/`，不是工程根目录遗留的 `java/`、`resources/`。

# 1.21.1 合并 1.20.1 新内容 — 进度交接

> 工作区：`E:\Dragon_Rise_1211`（git worktree，分支 `1.21.1/main`）
> 目标：把 `1.20.1/main` 的 60 个提交合并进 1.21.1，并独立成分支维护（对齐 SBW 每版本一分支）。

## ⚠️ 必读：环境坑

- **`Set-Location` 在 pwsh 工具里不生效**：必须每条命令用绝对路径，git 用 `git -C <worktree>`。
  否则会静默地在 `E:\Dragon_Rise`（1.20.1 仓库）上操作，读到完全错误的文件。
  本轮因此连续误判数次，务必注意。
- 中文编译器输出是 GBK 乱码，**用行号/位置定位，不要解析消息文本**。
- 收集"缺失符号"时，正则抓代码行附近的 CamelCase 标识符 + 按文件存在性过滤，
  比试图解析乱码消息更可靠。

## 状态：合并已完成，编译收尾中

```
worktree   E:/Dragon_Rise_1211  [1.21.1/main]   基于 origin/1.21.1/sync
           E:/Dragon_Rise       [1.20.1/main]   未受影响
合并       60 提交 / 1077 文件；MERGE_HEAD=a9756ba8
冲突       23 个全部解决（已 git add，1084 项已暂存）
提交       合并提交【尚未创建】
编译       约 100 错误 / 23 文件（从初始 100 逐步收敛，见下）
```

## 已完成的工作

### 1. 提交了一个真修复（1.20.1 侧，随合并带入）

`chonmaho5.geo.json` 在仓库里是 **0 字节** → 模型加载失败 → 阻断模组加载。
已从构建产物恢复为 288511 字节并提交（`a9756ba8`）。

### 2. 23 个冲突的解决

| 文件 | 解决方式 |
|---|---|
| `build.gradle` | 取 1.21.1 侧（ModDevGradle；1.20.1 是 ForgeGradle，不可混用） |
| `Dragonrise_reforge.java` | NeoForge 的 `(IEventBus, ModContainer)` 构造 + 采纳新注册项 |
| `ModItems.java` | 取 1.20.1 列表（66 ⊃ 46），`RegistryObject<Item>` → `DeferredHolder<Item, Item>` |
| `ModEntities.java` | 取 1.20.1 列表（186 ⊃ 163），`RegistryObject<EntityType<X>>` → `DeferredHolder<EntityType<?>, EntityType<X>>` |
| `ModEntityRenderers.java` | import 取并集，注册表取 1.20.1（186） |
| `ModSounds.java` | NeoForge `DeferredHolder` + 新条目 IED |
| `ModNetwork.java` | 重写为 NeoForge payload；只保留本分支存在的消息类 |
| `mixins.json` | 取 1.20.1 完整列表（39 个 mixin 类全部存在） |
| `ClientEventHandler.java` | `RegisterGuiLayersEvent` + 新注册；删 `CannonBallisticOverlay`（属已移除功能） |
| `HJ8Entity`/`R9M133Entity` | NeoForge `defineSynchedData(Builder)` + 补 `ROUND_STATE` |
| `M1A2SEPV2`/`ZTZ99A`/`ZBD04A` | 删两侧都无用的 import |
| `R6DroneCameraMixin` | 用新的 `R6DroneClientLookup`（见下） |
| modify/delete × 8 | 7 个接受删除（间接瞄准射击系统整体移除）；`mods.toml` 删除（1.21.1 用 `neoforge.mods.toml`） |

**`R6DroneClientLookup` 是真 bug 修复**：旧 `R6DroneEntity.findDrone` 引用
`net.minecraft.client.*`，被 Forge dist cleaner 判定为专用服务端非法引用
（`Attempted to load class ... for invalid dist DEDICATED_SERVER`）。
1.20.1 已拆成 `R6DroneClientLookup`（客户端）+ `findServerDrone`（服务端）。

### 3. 已完成的迁移（脚本都在 worktree 根目录）

| 脚本 | 作用 | 结果 |
|---|---|---|
| `fix-forge-residue.ps1` | 第一批 Forge→NeoForge import | 31 文件 |
| `fix-forge-residue2.ps1` | 第二批（event/network/items/capabilities 等） | 9 文件 |
| GeckoLib 包路径 | `core.animatable.instance` → `animatable.instance`；`core.animation` → `animation` | 19 文件 / 38 处 |
| `@Mod.EventBusSubscriber` → `@EventBusSubscriber`（`Bus.FORGE`→`Bus.GAME`） | | 5 文件 |
| `TickEvent.RenderTickEvent` → `RenderFrameEvent.Pre`（去掉 phase 判断） | | `StarShellLightManager` |
| mixin 描述符 `TickEvent$ClientTickEvent` → `ClientTickEvent$Post` | | `VehicleMainWeaponHudOverlayRemoteMixin` |
| `NetworkHooks.getEntitySpawningPacket` → `new ClientboundAddEntityPacket(this, serverEntity)` + `getAddEntityPacket(ServerEntity)` | | 3 实体 |
| Sodium 包名 `me.jellysquid.mods.sodium` → `net.caffeinemc.mods.sodium` | | 1 文件 |
| `new ResourceLocation(a,b)` → `ResourceLocation.fromNamespaceAndPath(a,b)` | | 10 文件 |
| 无参 `defineSynchedData()` → `(SynchedEntityData.Builder)` | | 3 文件 |
| `new EntityDataAccessor<>(id, ser)` → `SynchedEntityData.defineId(Cls.class, ser)` | | 4 文件 |
| 网络消息 `RemoteSeatSwitchMessage`/`RemoteSeatSyncMessage` 重写为 `CustomPacketPayload` + `StreamCodec` + `IPayloadContext` | | 2 文件 |
| 发送方改用 `PacketDistributor.sendToServer/sendToPlayer` | | 3 处 |
| 两个 SeatOneOnly mixin 的注入描述符 → `handler(IPayloadContext)V` | | 2 文件 |
| `SignalSmokeOption` 重写（`Deserializer` 已移除 → `MapCodec` + `StreamCodec`） | | 2 文件 |
| `ModEntities` import 重建（最多 197 个 import） | | `fix-imports.ps1` + 迭代补 |

### 4. 依赖补充（`build.gradle`）

- 加 Modrinth 仓库（Sodium 坐标需要）
- Sodium：`mc1.21-0.6.0-beta.2` → `mc1.21.1-0.6.13-neoforge`，且加 `compileOnly`
  （`StarShellLightManager` 引用 `SodiumWorldRenderer`，API 已验证兼容）
- 动态光源（软依赖）：`compileOnly files("local_repo/curse/maven/dynamiclights-reforged-551736/6044477/dynamiclights-reforged-551736-6044477.jar")`
  （jar 已就位；是 official 映射的 1.21.1 构建，同时含 `mods.toml` 与 `neoforge.mods.toml`）

## 剩余错误（约 100 处 / 23 文件）—— 都是同类机械迁移

### A. 护甲属性 API（17 个文件的 3 处/个，共约 50 处）

```
item\armor\ChineseDesertChest.java:59 方法不会覆盖或实现超类型的方法   @Override
item\armor\ChineseDesertChest.java:61 不兼容的类型: EquipmentSlot无法转换为ItemStack
        Multimap<Attribute, AttributeModifier> map = super.getDefaultAttributeModifiers(slot);
item\armor\ChineseDesertChest.java:66 找不到符号
        ..., AttributeModifier.Operation.ADDITION));
```

1.21.1 需要改为：
- 覆写 `getDefaultAttributeModifiers(ItemStack)`（不是 `EquipmentSlot`）
- 返回 `ItemAttributeModifiers`，用 `.withModifierAdded(Attribute, AttributeModifier, EquipmentSlotGroup)`
- `AttributeModifier(ResourceLocation, double, Operation)`，`Operation.ADDITION` → **`ADD_VALUE`**
- `EquipmentSlotGroup.bySlot(...)`

涉及文件：`ChineseDesertChest/Helmet/Pants`、`Jungle07Chest/Helmet/Pants`、
`Jungle07MedicChest/Helmet/Pants`、`RussianSoldierChest/Helmet/Pants`、
`UkrainianSoldierChest/Helmet/Pants`（共 17 个）

> 这条在前一轮 Architectury 1.21.1 工程里已经做过一次，参考
> `E:\Dragon_Rise\1.21.1\tools\fix-armor-attributes.ps1`。

### B. `ModItems.java`（34 处）

大概率又是 import 被 `fix-moditems.ps1` 替换掉了（同 `ModEntities` 的坑），
需要按同样办法重建 import 段。

### C. 其余

- `ShuangweixieEntity`（8 处）—— 可能 `getOrCreateTag()` 残留
- `SignalFlareRenderer` / `StarShellRenderer`（各 4 处）—— 新增文件，需 1.21.1 渲染器 API
- `RemoteAircraftClientHandler`（2 处）

## 后续步骤

1. 修 A（护甲属性，可参照 Architectury 工程的 `fix-armor-attributes.ps1`）→ 重编
2. 修 B（`ModItems` import 重建）→ 重编
3. 修 C（逐个看）→ 重编至 0 错误
4. `git commit` 创建合并提交
5. `:runClient` 验证进世界
6. 推远端建 `1.21.1/main` 分支

## 清理清单（完成后）

worktree 根目录的临时脚本应删除：
`fix-*.ps1`、`dump-conflicts.ps1`、`mb*.log`、本文件

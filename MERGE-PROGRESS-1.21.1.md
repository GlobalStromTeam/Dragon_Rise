# 1.21.1 合并 1.20.1 新内容 — 进度交接

> 工作区：`E:\Dragon_Rise_1211`（git worktree，分支 `1.21.1/main`）
> 目标：把 `1.20.1/main` 的 60 个提交合并进 1.21.1，并独立成分支维护（对齐 SBW 每版本一分支）。

## ⚠️ 必读：环境坑（本轮踩过，浪费大量时间）

- **`Set-Location` 在 pwsh 工具里不生效**：必须每条命令用绝对路径，git 用 `git -C <worktree>`。
  否则会在 `E:\Dragon_Rise`（1.20.1 仓库）上操作，读到完全错误的文件。
- **中文编译器输出是 GBK 乱码**：用行号/位置定位，不要解析消息文本。
  收集"缺失符号"时，正则抓代码行附近的 CamelCase 标识符 + 按文件存在性过滤，比解析乱码可靠。
- **PowerShell 5.1 对哈希表/字符串里的 `(` 解析敏感**：正则要写成独立变量或拆成多条命令。

## ✅ 已完成（核心目标已达成）

### 1. 合并提交已创建

```
9e7658c0  Merge 1.20.1/main 到 1.21.1/main：同步 89a / 无人机 / spg9 等新内容
          父提交：8ec38856（1.21.1 移植侧） + a9756ba8（1.20.1 侧）
```

验证：
- 2 个父提交（真合并）
- `git rev-list --count 1.21.1/main..1.20.1/main` = **0**（1.20.1 的内容已全部包含）
- `1.21.1/main` 领先 `1.20.1/main` 5 个提交（1.21.1 专有的移植工作）
- 0 处冲突标记残留

规模：60 提交 / 1077 文件。

### 2. 先提交了一个真修复（1.20.1 侧，随合并带入）

`chonmaho5.geo.json` 在仓库里是 **0 字节** → 模型加载失败 → 阻断模组加载。
已从构建产物恢复为 288511 字节（提交 `a9756ba8`）。

### 3. 23 个冲突全部解决

| 文件 | 解决方式 |
|---|---|
| `build.gradle` | 取 1.21.1 侧（ModDevGradle；1.20.1 是 ForgeGradle，不可混用） |
| `ModItems` / `ModEntities` / `ModEntityRenderers` | 取 1.20.1 超集（66>46、186>163），再转 `DeferredHolder` |
| `Dragonrise_reforge` | NeoForge 的 `(IEventBus, ModContainer)` 构造 + 采纳新注册项 |
| `ModSounds` | NeoForge `DeferredHolder` + 新条目 IED |
| `ModNetwork` | 重写为 payload；只保留本分支存在的消息类 |
| `mixins.json` | 取 1.20.1 完整列表（39 个 mixin 类全部存在） |
| `ClientEventHandler` | `RegisterGuiLayersEvent` + 新注册；删 `CannonBallisticOverlay` |
| `HJ8Entity`/`R9M133Entity` | `defineSynchedData(Builder)` + 补 `ROUND_STATE` |
| `R6DroneCameraMixin` | 用新的 `R6DroneClientLookup` |
| modify/delete × 8 | 7 个接受删除（间接瞄准射击系统整体移除）；`mods.toml` 删除 |

### 4. 编译已通过

```
:compileJava  → BUILD SUCCESSFUL（0 错误）
build         → BUILD SUCCESSFUL（dragonrise_reforge-1.5.1-release.jar，40.67 MB）
```

从最初的 100 处错误逐类修完。迁移脚本都在 worktree 根目录：

| 脚本 | 作用 |
|---|---|
| `fix-forge-residue.ps1` / `fix-forge-residue2.ps1` | Forge → NeoForge import（共 40 文件） |
| `fix-armor-attributes.ps1` | 护甲属性 → `ItemAttributeModifiers.withModifierAdded` + `ADD_VALUE` |
| `fix-imports.ps1` / `fix-modentities-imports.ps1` | 重建被冲突解决脚本丢掉的 import 段 |
| `fix-entity-spawn-api.ps1` | `getAddEntityPacket(ServerEntity)` + `ClientboundAddEntityPacket` |
| `fix-moditems.ps1` / `fix-modentities.ps1` / `fix-modentityrenderers.ps1` | 注册表超集 + 类型转换 |

手工迁移的其它项：
- GeckoLib 4.4 → 4.8 包路径（`core.animatable.instance` → `animatable.instance`）
- `@Mod.EventBusSubscriber` → `@EventBusSubscriber`（`Bus.FORGE` → `Bus.GAME`）
- `TickEvent.RenderTickEvent` → `RenderFrameEvent.Pre`；mixin 描述符 → `ClientTickEvent$Post`
- `ItemStack.getTag/getOrCreateTag` → `NBTTool.getTag` + `NBTTool.saveTag`（写必须回写）
- `new ResourceLocation(a,b)` → `ResourceLocation.fromNamespaceAndPath`
- `new EntityDataAccessor<>(id,ser)` → `SynchedEntityData.defineId`
- 无参 `defineSynchedData()` → `defineSynchedData(SynchedEntityData.Builder)`
- 网络消息 → `CustomPacketPayload` + `StreamCodec` + `IPayloadContext`；发送用 `PacketDistributor`
- `ForgeCapabilities.ITEM_HANDLER` → `Capabilities.ItemHandler.ENTITY`
- `CustomRecipe` → `(CraftingBookCategory)` + `assemble(CraftingInput, Provider)`
- 粒子选项 → `MapCodec` + `StreamCodec`
- `VertexConsumer` → `addVertex/setColor/setUv/setOverlay/setLight/setNormal`（无 `endVertex`）

依赖补充（`build.gradle`）：
- 加 Modrinth 仓库；Sodium `mc1.21-0.6.0-beta.2` → `mc1.21.1-0.6.13-neoforge`（+ `compileOnly`）
- 动态光源以本地文件依赖接入（CurseMaven 只提供 POM）：
  `compileOnly files("local_repo/curse/maven/dynamiclights-reforged-551736/6044477/...jar")`

## 🔄 进行中：`runClient` 的 mixin 注入复核

编译通过**不代表** mixin 注入点正确 —— 1.21.1 改了若干目标方法签名，Mixin 在运行期
APPLY 阶段才报 `Invalid descriptor`。已修复 3 个：

| mixin | 问题 | 修复 |
|---|---|---|
| `MinecraftRemoteSeatMixin` | 缺 `@Mixin` 注解 | 补 `@Mixin(Minecraft.class)` |
| `GameRendererMixin` | `renderItemInHand` 首参 `PoseStack` → `Matrix4f` | 处理器签名改 `org.joml.Matrix4f` |
| `AircraftHudMixin` | `AircraftHud#render` 去掉了 `Gui` 参数 | 两处处理器去掉 `Gui gui` |

修完这 3 个后重跑，`AircraftHudMixin` 通过、`GameRendererMixin` 仍报 —— 因为**参数顺序**
也要一致（不只是类型）。1.21.1 的目标签名是 `(Camera, float partialTick, Matrix4f)`，
而 1.20.1 的首参 `PoseStack` 被挪到末尾并换成 `Matrix4f`。改成正确顺序后：

```
rc4.log：mixin 全部通过，零 mixin 错误
         [04:39:30] Sound engine started
         [04:39:31] TextureAtlas / flywheel shader / simplebedrockmodel 加载
```

### 🔴 当前阻塞：客户端在资源重载阶段卡住（非崩溃）

`rc4.log` 到 19129 字节后 **10.5 分钟无新增**，且进程 CPU 增量仅 **0.45s / 8s**
（117 线程、3.2 GB 内存）—— 是**卡在等待**，不是"加载慢"。

最后几条日志（无任何异常/堆栈）：

```
[04:39:26] Reloading ResourceManager: vanilla, ..., mod/simplebedrockmodel, mod/dragonrise_reforge, ...
[04:39:26] DRAGON VEHICLE DEPLOYER: COMMON SETUP
[04:39:26] DragonRise:Reforge HELLO FROM COMMON SETUP     <- 模组初始化已完成
[04:39:26] DragonRise:Reforge DIRT BLOCK >> minecraft:dirt
[04:39:31] simplebedrockmodel: Loaded 0 particle definitions
[04:39:31] AnimationLoader: Loaded 0 entity animations
（之后停止）
```

**排查方向**（按可能性排序）：

1. **某个 reload listener 阻塞**。本分支的 SBM 模型由
   `VehicleModelReloadListener` / `ArmorModelReloadListener` /
   `EntityModelReloadListener` / `ItemModelReloadListener` /
   `ProjectileModelReloadListener` / `BlockModelReloadListener` /
   `VehicleLODModelReloadListener` 加载（见 `ClientEventHandler#registerReloadListeners`），
   共 225+ 个 geo 模型。1.20.1 侧用 7 个 listener，1.21.1 侧也要核对是否重复注册或路径不对。
2. **网络 payload 注册问题**：`ModNetwork.register` 在 mod 构造期跑，
   若某个 `TYPE`/`STREAM_CODEC` 为 null 可能静默卡住（本轮刚重写过这些消息类）。
3. **`SignalSmokeOption` / `ModParticleTypes`**：粒子类型 codec 是本轮新写的，
   若 `codec()` 返回 null 会在注册/序列化期出问题。
4. **动态光源软依赖**：`StarShellLightManager` 的 `RenderFrameEvent.Pre` 处理器每帧跑，
   若 `DynamicLightsCompat` 判断有误会空转（但那只影响帧率，不至于卡死加载）。

取证手段建议：
```
# 卡住时抓线程栈（最直接）
jstack <pid> > stack.txt
# 或让 JVM 自己 dump
# 在 runClient 的 JVM 参数里加 -XX:+HeapDumpOnOutOfMemoryError 不适用；
# 用 jcmd <pid> Thread.print
```
`jcmd <pid> Thread.print` 能直接看出卡在哪个 listener / 锁上。

### 下一步

1. 看 `rc3.log`，若还有 `Invalid descriptor` / `InvalidInjectionException`，按同样办法
   逐个核对注入处理器签名与 1.21.1 目标方法签名是否一致
   - 注意：`require = 0` 的注入点失败会**静默跳过**，不会报错 —— 这类要靠人工核对
2. 全部通过、能进世界后：把 mixin 修复追加提交（`git -C E:\Dragon_Rise_1211 add -A && git commit`）
3. 推远端建 `1.21.1/main` 分支：
   `git -C E:\Dragon_Rise_1211 push -u origin 1.21.1/main`
4. 清理 worktree 根目录的临时脚本与日志（`fix-*.ps1`、`mb*.log`、`rc*.log`、本文件）

## 仓库结构备忘

```
E:\Dragon_Rise        [1.20.1/main]      1.20.1 Forge 工程（仓库根 src/），未受影响
E:\Dragon_Rise_1211   [1.21.1/main]      1.21.1 NeoForge 工程（worktree，同结构 src/）
```

`E:\Dragon_Rise\1.20.1\` 与 `E:\Dragon_Rise\1.21.1\` 是更早一轮做的 Architectury 工程，
**未跟踪、与本次合并无关**，可另行处理。

# SBW（卓越前线）载具皮肤系统 · 一页说明

> 适用：`superbwarfare 0.8.9.1`（Forge 1.20.1）+ 本工程 Dragonrise ReForge 的扩展。
> 结论先行：**皮肤 = 载具实体上的一个字符串 `skinId`（同步数据 + NBT），渲染时按它去查 `data/<ns>/sbw/vehicle_skins/<载具id>.json` 里的贴图**。
> 本工程额外允许「涂装自带模型与动画」，靠同名的 `<载具id>_<涂装id>.geo.json` / `.animation.json` 配对。

---

## 0. 一张图看懂全流程

```
① 玩家手持 皮肤喷漆罐 /superbwarfare:skin_spray 右键载具
        │  （SkinSprayItem implements IVehicleInteract）
        ▼
② 服务端 → 客户端：OpenVehicleSkinScreenMessage(vehicleId)
        ▼
③ VehicleSkinScreen 打开：列 Vanilla（=空 skinId）+ 该载具 json 里的皮肤（4 个/页，实时 3D 预览）
        ▼
④ 点选 → 客户端 → 服务端：SetVehicleSkinMessage(entityId, skinId)
        ▼
⑤ 服务端校验 skinId 存在 → vehicle.skinId = skinId     （写入 SKIN_ID 同步数据）
        ▼
⑥ 所有客户端收到同步数据更新 → GeoVehicleRenderer.render 里
     VehicleSkin.getSkin(entity) → skinInfo.texture → 替换贴图
        ▼
⑦ 存档：写 NBT "SkinId"（仅非空时写），重登/重载后保留
```

---

## 1. 数据结构（`superb` 侧）

`src/main/kotlin/com/atsuishio/superbwarfare/data/vehicle_skin/SkinInfo.kt`：

```kotlin
@Serializable
data class SkinInfo(
    @SerialName("Id")          val id: String = "vanilla",
    @SerialName("Name")        val name: String = "Vanilla",
    @SerialName("Description") val description: String = "",
    @SerialName("Texture")     val texture: String = "",
    @SerialName("Priority")    val priority: Int = 0
)

@Serializable
data class VehicleSkinData(@SerialName("Skins") val skins: List<SkinInfo> = listOf())
```

| 字段 | 含义 | 备注 |
|---|---|---|
| `Id` | 皮肤标识，**大小写敏感**的裸字符串 | `""`（空）= Vanilla，由 UI 自带，不写进 json |
| `Name` | 显示名，**字面量**（不是翻译键） | 写中文就直接写中文 |
| `Description` | 描述；支持 `Component#<lang key>` 前缀走翻译 | 例：`Component#skin.superbwarfare.truck.green` |
| `Texture` | 贴图 `ResourceLocation` 字符串 | 解析失败会被静默忽略 |
| `Priority` | 只影响**列表顺序/展示**，不决定默认外观 | 默认外观永远是 `skinId` 为空 + 模型自带 `Texture` |

注意序列化名是**首字母大写**（`Id`/`Name`/`Texture`/`Priority`/`Skins`），写错 key 会被当作未设置、用默认值（不会报错）。

---

## 2. 数据加载链

```kotlin
// src/main/java/com/atsuishio/superbwarfare/data/CustomData.kt:44-46
val VEHICLE_SKINS = DataLoader.createData(
    "sbw/vehicle_skins", VehicleSkinData::class.java, true, isKtData = true
) { _ -> VehicleSkin.DATA_CACHE.invalidateAll() }
```

- 路径 = **`data/<命名空间>/sbw/vehicle_skins/<载具注册id>.json`**（datapack，服务端与客户端都要有 → 通常直接放进 mod 的 `src/main/resources/data/...`）。
- 文件名必须**等于 `EntityType` 的注册 id**（`dragonrise_reforge:m3a3` → `m3a3.json`）。`VehicleSkin.from(entityType)` 用 `EntityType.getKey(type).toString()` 查表，命名空间也要一致。
- **`assets/` 下的同名文件完全不生效**（这一条踩过坑：`assets/dragonrise_reforge/sbw/vehicles/*.json` 只有 3 个键，改它没有任何效果）。
- 重载：datapack reload 会触发上面那个回调 `VehicleSkin.DATA_CACHE.invalidateAll()`，**`/reload` 之后不需要重新生成载具**。

`VehicleSkin`（`data/vehicle_skin/VehicleSkin.kt`）：

```kotlin
fun compute(): VehicleSkinData                 // 带实例缓存，getOrElseGet(vehicleId) { VehicleSkinData() }
fun update()                                   // 清缓存
fun getSkin(skinId: String): SkinInfo?         // skinId 为空 → null；否则按 id 精确匹配
companion object {
    fun getSkin(entity: VehicleEntity): SkinInfo?      // entity.skinId 为空 → null
    fun getSkins(entityType: EntityType<*>): VehicleSkinData
    val DATA_CACHE: LoadingCache<String, VehicleSkin>  // Guava，键 = 实体 id 字符串
}
```

---

## 3. 玩家怎么改皮肤

| 途径 | 实现 | 说明 |
|---|---|---|
| 喷漆罐右键载具 | `item/misc/SkinSprayItem.kt`（`stacksTo(1)`，实现 `IVehicleInteract`） | 服务端发 `OpenVehicleSkinScreenMessage(vehicle.id)`，返回 `InteractionResult.CONSUME` |
| 皮肤选择 GUI | `client/screens/VehicleSkinScreen.kt` | 4 个/页，`BUTTON 142×94`、`IMAGE 320×200`，贴图 `textures/gui/vehicle_skin.png`、`vehicle_skin_button.png`；为每个皮肤 `vehicleType.create(level)` 造一个预览实体并设 `skinId` 就地渲染 |
| 命令 | `command/SkinCommand.kt` → `/superbwarfare:skin <vehicle> set <skinId>` / `clear` | 需要权限等级 2 |

喷漆罐本体：`ModItems.SKIN_SPRAY = registerItem("skin_spray") { SkinSprayItem() }`（`init/ModItems.kt:306`），
配方（`src/generated/resources/data/superbwarfare/recipes/skin_spray.json`）：

```
" ab"
"cdc"     a=iron_nugget  b=tripwire_hook  c=forge:plates/steel  d=forge:dyes
" c "     → superbwarfare:skin_spray
```

语言键：`item.superbwarfare.skin_spray` = "Skin Spray" / "皮肤喷漆罐"。

---

## 4. 状态、同步与存储

`entity/vehicle/base/VehicleEntity.kt`：

| 位置 | 内容 |
|---|---|
| `:5107` | `val SKIN_ID: EntityDataAccessor<String> = SynchedEntityData.defineId(VehicleEntity::class.java, EntityDataSerializers.STRING)` |
| `:1000` | `define(SKIN_ID, "")` |
| `:4864` | `open var skinId by SKIN_ID` |
| `:1600` | `readAdditionalSaveData`：`skinId = compound.getString("SkinId")` |
| `:1750-1752` | `addAdditionalSaveData`：`if (skinId.isNotBlank()) compound.putString("SkinId", skinId)` |

要点：

- 皮肤是**每实体**的（同一型号两辆车可以不同皮肤），走同步数据 → 客户端渲染即改，无需额外包。
- NBT 键是 **`SkinId`**（大小写敏感）；空值不写入 = 默认外观。
- `SetVehicleSkinMessage` 的服务端校验只做两件事：`entityId` 能解析成 `VehicleEntity`、`skinId` 为空或确实存在于该型号的皮肤表。**没有权限校验**——若服务器要限制"谁能改皮肤"，需要自己加。

---

## 5. 渲染时怎么用（superb 原生）

`client/renderer/entity/GeoVehicleRenderer.kt:100-130`：

```kotlin
val entry = getCurrentModelEntry(poseStack, entity) ?: return
val instance = entry.instance
var texture = entry.texture

// 只有"完整模型"才应用皮肤，LOD 不应用
val isLOD = entry.isLOD()
if (!isLOD) {
    val skinInfo = VehicleSkin.getSkin(entity)
    if (skinInfo != null) {
        val skinTexture = ResourceLocation.tryParse(skinInfo.texture)
        if (skinTexture != null) texture = skinTexture
    }
}

texture = if (ClientEventHandler.activeThermalImaging) ... // 热成像
          else if (entity.isWreck) ...                      // 残骸变暗
          else texture
```

由此产生两条**必知行为**：

1. **远处 LOD 不换皮肤**（`isLOD` 时跳过），所以远看会"变回原色"。
2. 皮肤只覆盖 **base texture**；残骸/热成像等后续处理仍会在此基础上再加工。

---

## 6. 本工程（Dragonrise）的扩展：涂装自带模型 + 动画

superb 原生只换贴图。本工程加了两层（都可单独关闭，缺失文件即回退原逻辑）：

### 6.1 涂装自带模型（渲染器）

`src/main/java/com/redabysslucia/dragonrise_reforge/client/renderer/entity/DragonriseVehicleRenderer.java`

```java
@Override
public VehicleModelEntry getCurrentModelEntry(PoseStack poseStack, T entity) {
    VehicleModelEntry skinEntry = getSkinModelEntry(entity);
    return skinEntry != null ? skinEntry : super.getCurrentModelEntry(poseStack, entity);
}

@Override
public ResourceLocation getEmissiveTextureLocation(PoseStack poseStack, T entity) { ... }
```

- 约定路径：`assets/dragonrise_reforge/models/bedrock/vehicle/<载具id>_<涂装id>.geo.json`。
- 每个实体一份模型实例，缓存在 `WeakHashMap<VehicleEntity, CachedSkinModel>` 里；用 `baked` 对象身份判断资源重载（F3+T）后是否要重建。
- 没有对应 geo 时返回 `null`，完全走 superb 原逻辑（只换贴图）。
- `AMX56Renderer` 另有自己的 `getCurrentModelEntry` 覆写（特殊机型），改动机型模型时注意别被它抢走。

### 6.2 涂装自带动画（动画实例）

`src/main/java/com/redabysslucia/dragonrise_reforge/entities/utils/DragonriseVehicleBase.java:129-170`

```java
protected VehicleAnimationInstance<VehicleEntity> getSkinAnimationInstance() {
    if (!level().isClientSide()) return null;
    String skinId = getSkinId();
    if (skinId == null || skinId.isBlank()) return null;
    String key = EntityType.getKey(getType()).getPath() + "_" + skinId;   // 例：tjgc_skin
    ResourceLocation modelPath = ... "models/bedrock/vehicle/" + key + ".geo.json";
    ResourceLocation animPath  = ... "animations/bedrock/vehicle/" + key + ".animation.json";
    if (VehicleModelReloadListener.INSTANCE.getModel(modelPath) == null) return null;
    if (VehicleModelReloadListener.INSTANCE.getAnimation(animPath) == null) return null;
    ...
}

@Override
public VehicleAnimationInstance<VehicleEntity> getAnimationInstance() {
    VehicleAnimationInstance<VehicleEntity> skinAnim = getSkinAnimationInstance();
    return skinAnim != null ? skinAnim : super.getAnimationInstance();
}
```

**为什么必须成对**：superb 的 `BedrockAnimation.createAnimation(file, BoneIndexProvider)` 是**按模型烘焙的骨骼索引**建动画的，
模型与动画按文件名配对。只换模型不换动画 → 关键帧打在旧骨骼索引上，表现就是**换涂装后动画全不播**。

### 6.3 当前工程里的实际分布（21 个 json / 40 条皮肤）

- 只有 **1 条**皮肤同时带模型与动画：`tjgc` 的 `skin`（`tjgc_skin.geo.json` + `tjgc_skin.animation.json`）。
- 其余全是"只换贴图"：`aav7a1`、`aavc7c1`、`amx56`、`bmp3`、`brdm2`、`leopard2a4`、`m1a2sepv1/2`、`m3a3`、`mv3*`、`t72b3`、`tjgc`、`ural4320*`、`zbd04a`、`ztq15`、`ztz96a`、`ztz99a`。
- 贴图都在 `assets/dragonrise_reforge/textures/entity/<载具id>_<涂装id>.png`。

---

## 7. datagen：自动生成皮肤条目（本工程）

`src/main/java/com/redabysslucia/dragonrise_reforge/datagen/VehicleSkinGenerator.java`（`DataProvider`）：

1. 扫 `assets/dragonrise_reforge/textures/entity/`，按文件名 `{载具id}_{涂装id}.png` 拆分。
2. 载具 id 从 `init/ModEntities.java` 用正则 `register\("([a-z0-9_]+)"` 收集，**按 id 长度从长到短**匹配前缀（否则 `m4a2` 会抢走 `m4a2_105`）。
3. 跳过 `EXCLUDED_SUFFIXES = {icon, glow, icon_item, outline}` 与任何 `_glow` 后缀（那是某涂装的发光贴图）。
4. `SKIN_NAMES`（TreeMap）给常见 id 中文名：`sand` 沙漠涂装、`green` 绿色涂装、`ttsko` / `ttsko1960` / `nato` / `desert` / `snow` / `woodland`；未命中就直接用涂装 id 当 `Name`。
5. 写四份：`src/main/resources/data/.../vehicle_skins`、`src/main/resources/assets/.../vehicle_skins`、`bin/main/data/...`、`build/resources/main/data/...`。
6. `mergeSkins` 按 `Id` 去重：**已存在条目原样保留**，只补缺的（手写的 `Priority`/`Description` 不会被覆盖，但新贴图会被自动追加一条 `Priority 0`、纯文本描述的条目）。

> 手写优先：本项目约定**不跑 `runData`**（会覆盖手调数据），所以新增涂装默认手写 json，datagen 只在明确要重新生成时用。

---

## 8. 语言键

| 类型 | 键 | 例 |
|---|---|---|
| 皮肤名/描述 | `skin.<命名空间>.<载具id>.<涂装id>` | `skin.dragonrise_reforge.ztq15.sand` = "沙漠迷彩" |
| 物品名 | `item.<ns>.skin_spray` | superb 自带 |
| 命令反馈 | `commands.superbwarfare.skin.*` | superb 自带 |

`Name` 字段**不会**走翻译（它是字面量）；只有 `Description` 以 `Component#` 开头才会 `Component.translatable(...)`。
所以想给皮肤名做多语言，只能两个语言文件里各写一条 `skin.*` 并用 `Description: "Component#skin...."`，或在 json 里直接写死一种语言。

---

## 9. 常见坑（按出现频率排序）

| 现象 | 根因 | 处理 |
|---|---|---|
| 改了 json 没反应 | 改到了 `assets/.../vehicle_skins/*.json` 或改错文件名 | 只认 `data/<ns>/sbw/vehicle_skins/<实体id>.json`；`/reload` |
| 皮肤列表里找不到某条 | 该条 `Id` 写成了 `vanilla` | GUI 会 `filter { it.id != "vanilla" }` 并自带一条"Vanilla"（=空 skinId），json 里的 `vanilla` 条目被隐藏 |
| 换了涂装，动画不播 | 只加了 `<id>_<skin>.geo.json`，没加同名 `.animation.json` | 两个都要有，见 §6.2 |
| 远处看还是原色 | LOD 分支不应用皮肤 | 正常行为；要统一只能改渲染器 |
| 皮肤选完没变化 | `Texture` 路径写错（解析失败静默忽略）/ png 不存在 | 用 `ResourceLocation.tryParse` 能过的写法，且贴图真在 `assets/<ns>/textures/...` |
| 服务端改了不生效 | 以为要重生载具 / 用的是旧 datapack | `/reload` 即可（`DATA_CACHE.invalidateAll()`），不用重生 |
| 换肤后模型错位/贴图错 | 涂装模型与主模型骨骼/UV 不一致 | 涂装模型必须与主模型同骨骼结构、同贴图尺寸 |
| 谁都能改皮肤 | `SetVehicleSkinMessage` 无权限校验 | 需要限制就在服务端包处理里自己加 |

---

## 10. 加一个新涂装（实操 5 步）

```text
1) 贴图    assets/dragonrise_reforge/textures/entity/<载具id>_<涂装id>.png
           （尺寸/UV 必须与主贴图一致）
2) 数据    data/dragonrise_reforge/sbw/vehicle_skins/<载具id>.json
           {"Skins":[{"Id":"sand","Name":"沙漠涂装","Texture":"dragonrise_reforge:textures/entity/<载具id>_sand.png","Priority":0}]}
3) 语言    assets/dragonrise_reforge/lang/zh_cn.json + en_us.json
           若 Name 直接写中文可跳过；用 Description 的话加 skin.dragonrise_reforge.<载具id>.<涂装id>
4)（可选）涂装自带模型/动画
           assets/.../models/bedrock/vehicle/<载具id>_<涂装id>.geo.json
           assets/.../animations/bedrock/vehicle/<载具id>_<涂装id>.animation.json
           两个必须成对；缺一个就当作"只换贴图"
5) 验证    python -c "import json;json.load(open(r'<json>',encoding='utf-8'))"   # 语法
           build 后从 jar 里读回该 json 与 png；游戏里 /reload 或 F3+T
```

> 纯资源改动：**F3+T** 即可看到贴图/模型变化；涉及 Java（渲染器/基类）才需要重启客户端。

---

## 11. 相关文件索引

**superb 侧**

| 文件 | 作用 |
|---|---|
| `data/vehicle_skin/SkinInfo.kt` | `SkinInfo` / `VehicleSkinData` 定义 |
| `data/vehicle_skin/VehicleSkin.kt` | 查表 + 两级缓存（`VehicleSkin` 实例缓存 + `DATA_CACHE`） |
| `data/CustomData.kt:44-46` | `VEHICLE_SKINS` 数据加载器 + 重载回调 |
| `entity/vehicle/base/VehicleEntity.kt:1000 / 1600 / 1750 / 4864 / 5107` | `SKIN_ID` 同步数据、NBT、属性委托 |
| `client/renderer/entity/GeoVehicleRenderer.kt:100-130` | 渲染时按皮肤换贴图（LOD 跳过） |
| `item/misc/SkinSprayItem.kt` | 喷漆罐：右键载具开界面 |
| `client/screens/VehicleSkinScreen.kt` | 皮肤选择 GUI + 预览 |
| `network/message/send/SetVehicleSkinMessage.kt` | 客户端 → 服务端设置皮肤（校验） |
| `network/message/receive/OpenVehicleSkinScreenMessage.kt` | 服务端 → 客户端打开界面 |
| `command/SkinCommand.kt` | `/superbwarfare:skin …` |
| `init/ModItems.kt:306` + `assets/superbwarfare/lang/*` | 物品注册与名称 |

**本工程（Dragonrise）侧**

| 文件 | 作用 |
|---|---|
| `client/renderer/entity/DragonriseVehicleRenderer.java` | 涂装自带模型（`getCurrentModelEntry` + `WeakHashMap` 缓存） |
| `entities/utils/DragonriseVehicleBase.java:129-170` | 涂装自带动画实例（模型/动画成对） |
| `datagen/VehicleSkinGenerator.java` | 按贴图命名自动生成皮肤条目 |
| `data/dragonrise_reforge/sbw/vehicle_skins/*.json` | 21 个载具、40 条皮肤（当前） |

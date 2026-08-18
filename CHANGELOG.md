# DragonRise:Reforge 更新简报

> 本次改动覆盖：载具物品栏、牵引系统移除、SBM 渲染迁移、datagen 工具链增强、载具皮肤系统。

## 一、载具物品栏

- **移除物品栏自定义 Mixin**（`DefaultVehicleDataMixin` / `VehicleContainerTypeMixin` / `SmallVehicleContainerMenuMixin`），载具物品栏恢复 SuperbWarfare 本体纯净行为（3×9、6×9、6×13、6×17 等按数据配置）
- 删除按 3×7 布局绘制的残留 GUI 贴图覆盖

## 二、牵引（栓绳/铁链）系统移除

- 删除整套牵引实现：`VehicleEntityLeadMixin`、`VehicleLeashData`、牵引 Tick/事件处理器、连接线渲染器、断开网络消息
- 清理对应的按键（`DISCONNECT_LEASH`）、语言条目与 `FireLightVisionVehicle` 中的牵引调用

## 三、全面迁移至 SBM 渲染

- **实体层**：全部载具实体由 `GeoVehicleEntity` 改为继承 SuperbWarfare `VehicleEntity`，清除全部 GeckoLib 代码（69 处 `registerControllers`、341 处 import）；`FireLightVisionVehicle`/`SyncCameraVehicle`/`VariableEngineVehicle`/`AirshipVehicle`/`IndirectFireVehicleBase` 等基类同步调整
- **渲染器层**：120+ 渲染器统一为 SuperbWarfare `GeoVehicleRenderer`（SBM v2 管线），删除本模组精简版 `DragonriseSbmVehicleRenderer` 及 `DragonriseBedrockLoader`；删除 GeckoLib 发光层（`GlowLayer`）与车辆 GeckoLib 模型类
- **资源层**：模型统一放 `models/bedrock/vehicle/`，贴图由车辆 JSON `Models` 数组指定（`textures/entity/{id}.png`）；防具等 GeckoLib 模型保留在 `geo/`（已修复该目录丢失问题）
- 抛射物/恐怖分子等非车辆实体保留 GeckoLib 渲染

## 四、datagen 工具链增强

- **GeoOBBDataProvider**
  - 支持 `models/bedrock/vehicle/` 输入目录（与 `geo/` 合并去重）
  - 生成 OBB 时读取骨骼 `rotation` 输出 `CustomRotate`（角度，Z 轴与 Position 一致取反）
  - 识别 `CollisionObb` 骨骼 → `"Part": "Collision"`
  - 单个模型损坏时跳过并提示，不再导致整个 runData 崩溃
  - 车辆配置同时写入 `assets/superbwarfare/` 与 `assets/dragonrise_reforge/` 两个目录
- **VehicleJavaGenerator**
  - 支持 `models/bedrock/vehicle/` 输入目录
  - 实体模板改为继承 `VehicleEntity`，渲染器模板改为 `GeoVehicleRenderer`，不再生成 GeckoLib 模型类
  - 修复多行注册块插入（新增 `insertAfterBlockEnd`）与 import 去重（CRLF 兼容）
- **新增 MeshModelNormalRemover**：删除模型中的 `normals` 顶点法线字段（文本级方括号配对，保留格式）
- **新增 VehicleSkinGenerator**：按 `{载具ID}_{涂装ID}.png` 命名约定自动生成/合并 `vehicle_skins` 皮肤数据（data/assets 双份并同步 IDE/Gradle 输出）
- **新增 VehicleDataCompletenessChecker**：对照 superbwarfare `DefaultVehicleData` 63 字段生成 `datagen_vehicle_report.md` 完整性报告，并为缺失核心字段的载具生成 `datagen_ai_prompts/{id}.md` AI 填充提示词（仅处理模型含 Build 骨骼的载具，只读不覆盖）
  - **分类型提示词**：通过**类型标记骨骼**判定载具类型——与 Build 骨骼机制一致，遍历模型骨骼，骨骼名（忽略大小写）为 `HELI`/`SHIP`/`AIRSHIP`/`TANK`/`PLANE`/`CAR` 即判定为对应类型（多个标记按 HELI>SHIP>AIRSHIP>TANK>PLANE>CAR 取首个，无标记默认 CAR）；按类型给出专属填写指南——`HudType`、`EngineType`、`EngineInfo` 字段键（履带 Track vs 轮式 Wheel vs 飞行器 Pitch/Yaw/Roll/Lift 参数）、特有字段（飞机 `HasGear`/诱饵、直升机旋翼、舰船浮力）、武器配置要点，并提示该类型**无需**的字段

## 五、载具皮肤（喷漆罐）系统

- 皮肤数据加载位置修正：`sbw/vehicle_skins` 由服务器加载器读取，数据文件放 **`data/`** 目录
- 已配置涂装：**aav7a1**（绿色基础 + 沙漠）、**aavc7c1**（绿色基础 + 沙漠）、**brdm2**（沙漠 / TTSKO / TTSKO 1960）

## 六、修复

- `aavc7c1.geo.json` 文件开头混入字符 `3` 导致 JSON 损坏（已修复）
- runData 因单个坏模型崩溃（生成器容错）
- `VehicleJavaGenerator` 注册块挤行/劈开已有块（`alignBlock` 丢失换行符）
- 车辆数据读取：实体 ID 需在 `assets/dragonrise_reforge/sbw/vehicles/` 下有对应 JSON（缺失的 25 辆已补齐）
- 载具 asset JSON 补齐 `Models` 数组（SBM 渲染必需）

# tools/ —— 工具索引（新会话先读这个）

> **入口约定**：任何"加护甲 / 转模型 / 出图标"的任务，先读本文件，再读
> [`docs/护甲模型规范.md`](../docs/护甲模型规范.md)。两者一起构成完整的操作规范；
> 本文件负责"用哪个工具、怎么跑"，规范文档负责"数据必须长什么样"。

---

## 一、护甲流水线（关键工具，留存备用）

5 个工具串成一条线，输入 `.bbmodel` 或别人导出的 `.geo.json`，输出可直接进游戏的护甲：

```
① 模型来源              ② 转 geo                    ③ 校验/修              ④ 图标                    ⑤ 注册
 bbmodel ──────────► bbmodel_to_armor_geo.mjs ──► armor_geo_norm.mjs ──► make_armor_icons.py ──► gen_armor_set.mjs
 别人的 geo ───────► geo_to_armor.mjs        ──┘   (+ bake_uv_rotation.py)
```

### ① `geo_to_armor.mjs` — **首选**：把 Blockbench 导出的护甲 geo 规整成 SBM 可用格式

```bash
node tools/geo_to_armor.mjs --all "<源目录>" "src/main/resources/assets/dragonrise_reforge/models/bedrock/armor"
node tools/geo_to_armor.mjs --in a.geo.json --out b.geo.json --id geometry.xxx   # 单个
```

- 枢轴全部改成规范值（`armor*` 与 `biped*` 同值，bind=0）；左右腿/靴按**几何实际所在侧**对齐；
- 额外骨骼（`group` / `group2` / `bone…`）的方块沿父链并入最近的 `armor*` 骨；
- `format_version` 统一 `1.12.0`，只保留 14 根骨；
- 方块 `origin/size/pivot/rotation/uv` 原样保留（不动几何）。
- **适用前提**：源文件的方块归属已经正确（例如你在 Blockbench 里拆好的）。归属错的话它不会帮你重分。

### ② `bbmodel_to_armor_geo.mjs` — 从 `.bbmodel` 直接生成（无现成 geo 时用）

```bash
node tools/bbmodel_to_armor_geo.mjs --in "D:\Download\xxx兵.bbmodel" \
     --geo src/.../bedrock/armor/xxx.geo.json --png src/.../textures/armor/xxx.png \
     --id geometry.xxx [--strip-rotation]
```

- 自动选**被 face 引用最多的那张贴图**（bbmodel 里常带一张 64px 副本，选错全错）；
- 按"旋转后包围盒中心"分骨：先 X 判手臂、再 Y 判头/躯干/腿（兼容抬臂姿势）；
- `--strip-rotation`：`free` 格式的 cube 旋转序与 bedrock 不一致，直接导会压成"平板"，加这个开关还原静止姿势；
- 主贴图的面保持原始 UV，只有引用"非主贴图"的面才做 UV 换算（否则 `uv_width` 不符时会整体放大）。

### ③ `armor_geo_norm.mjs` — 规范校验/自动修

```bash
node tools/armor_geo_norm.mjs --check        # 有问题退出码 1
node tools/armor_geo_norm.mjs --fix          # 自动修枢轴（备份 build/armor_geo_backup/）
node tools/armor_geo_norm.mjs --check --all  # 顺带体检 vehicles/entity（只报告）
```

Gradle 侧同名任务：`./gradlew armorGeoCheck` / `./gradlew armorGeoFix`（`check` 依赖前者，`build` 会自动跑）。

### ④ `make_armor_icons.py` — 物品图标（离线渲染，不需 Blockbench）

```bash
python tools/make_armor_icons.py [--size 512] [--fill 0.85] [--yaw 315] [--sets haom,juaw]
```

- 复用 1201 工程的 `Dragon_Rise_1201/tools/vehicle_icon_render.py`（`--renderer` 可改路径）；
- 一套 geo 三槽共用 → 按槽位**过滤骨骼**后各渲一张，所以头盔/胸甲/护腿图标各只显示对应部位；
- **必须 `near_smaller=False`**，否则深度反了会渲染出背面（像从模型内部往外看）。

### ⑤ `gen_armor_set.mjs` — 一键生成物品侧代码与数据

```bash
node tools/gen_armor_set.mjs     # 改脚本顶部 SETS 表
```

生成：物品类（`item/armor/<Cap><Slot>.java`）、`ModItems` 注册、`ModTabs` 创造栏、`zh_cn/en_us` 语言、
`models/item/*.json`、`recipe/*_assembling.json`（kevlar 5/8/7）。
**贴图共用**：按 PNG 的 SHA-256 去重，内容相同的多套模型只引用第一份贴图（Java `TEXTURE` 常量自动指向它）。

### 辅助：`bake_uv_rotation.py`

```bash
python tools/bake_uv_rotation.py <geo.json> <texture.png>
```

SBM / GeckoLib **都不读 `uv_rotation`**。180° 用负 `uv_size` 表达；90/270° 且图集里是整数像素正方形时，
就地把图块旋转进图集再清掉该字段（非正方形会跳过并报告）。`--dry` 只报告不写。

---

## 二、不可违反的数据不变量（写错必错位）

| 项 | 值 / 规则 |
|---|---|
| 骨骼名 | `bipedHead/armorHead`、`bipedBody/armorBody`、`bipedRightArm/armorRightArm`、`bipedLeftArm/armorLeftArm`、`bipedRightLeg/armorRightLeg/armorRightBoot`、`bipedLeftLeg/armorLeftLeg/armorLeftBoot` |
| 枢轴 | head/body `[0,24,0]`；右臂 `[-5,22,0]` / 左臂 `[5,22,0]`；右腿+靴 `[-2,12,0]` / 左腿+靴 `[2,12,0]`；`armor*` 必须与其 `biped*` 父骨相同 |
| 立方体坐标 | 模型空间**绝对值**（原点脚底、+y 向上、16 单位=1 格） |
| `format_version` | `1.12.0` |
| 贴图 | geo 声明尺寸必须是 PNG 的整数因子（本项目多为 2×） |
| `uv_rotation` | 必须为 0（先烘焙） |
| 一套三槽 | 头盔/胸甲/护腿共用同一份 geo，靠 SBM 槽位可见性切分 |

历史事故：8 件胸甲的 `armorBody` 写成 22（SBM 静息值是 24）→ 胸甲整体飘到头上去。

---

## 三、本项目 `tools/` 全部脚本

| 脚本 | 语言 | 作用 |
|---|---|---|
| `geo_to_armor.mjs` | Node | **首选**：规整别人导出的护甲 geo（枢轴/合并额外骨/版本） |
| `bbmodel_to_armor_geo.mjs` | Node | `.bbmodel` → 护甲 geo + 贴图 |
| `armor_geo_norm.mjs` | Node | 护甲 geo 规范校验 / 自动修（Gradle 也接了这一层） |
| `gen_armor_set.mjs` | Node | 生成物品类/注册/创造栏/语言/物品模型/配方 |
| `make_armor_icons.py` | Python | 512×512 物品图标（离线渲染，按槽位过滤骨骼） |
| `bake_uv_rotation.py` | Python | 把残留 `uv_rotation` 烘进图集 |
| `armor_geo_preview.py` | Python | 骨骼着色的正交预览（快速看分骨/UV 是否合理） |

### Gradle 任务（`build.gradle`）

| 任务 | 作用 |
|---|---|
| `armorGeoCheck` | 跑 `armor_geo_norm.mjs --check`；`check` 依赖它，`build` 因此自动校验 |
| `armorGeoFix` | 跑 `--fix`（先备份） |

> `gradlew build` 会先跑 `createMinecraftArtifacts`；**游戏正在运行时该产物被锁**，会报
> `AccessDeniedException ... build/moddev/artifacts/...`。此时用
> `gradlew compileJava processResources jar armorGeoCheck` 即可，或先关游戏。

---

## 四、可复用的外部工具（在 1201 工程里，直接引用不复制）

路径：`E:\Dragon_Rise\Dragon_Rise_1201\tools\`

| 脚本 | 用途 |
|---|---|
| `vehicle_icon_render.py` | **Bedrock 离线渲染底座**：`render_icon(geo, tex, out, size, fill, yaw, pitch, near_smaller)`；`make_armor_icons.py` 就是调它 |
| `vehicle_icon_pipeline.py` | 载具图标四步流程（solve/check/build/sync/review），配 `icon_jobs*.json` |
| `make_vehicle_icons.py` | 载具图标批处理（含 yaw/pitch 约定：车头朝 −Z 时用 yaw=225） |
| `bb_icon_render.js` | Blockbench 侧截图脚本（`__renderIcon/__renderShot`），需要 Blockbench + MCP 离屏预览 |
| `icon_jobs_armor.json` | 之前那批护甲（chinese_desert / russian_soldier / ukrainian_soldier）的图标任务模板 |
| `geo_ortho_view.py` / `geo_tex_view.py` / `geo_bone_bbox.py` / `geo_bone_rename.py` | geo 正交视图 / 贴图查看 / 骨骼包围盒 / 骨改名 |
| `载具图标渲染流程.md` | 载具图标完整流程文档（角度、取景、像素闭环） |

---

## 五、常见故障对照

| 现象 | 原因 / 处理 |
|---|---|
| 整件护甲上浮/下沉（胸甲跑到头上） | `armor*` 枢轴与 `biped*` 父骨不一致 → `armorGeoFix` |
| 左臂护甲横向错位 | `bipedLeftArm.x` 写成负数（应 +5） |
| 靴子偏离脚 | `armorBoot` 枢轴错（如 `[0,0,0]`、`[0.2,-1,0]`） |
| 贴图错位/发糊 | PNG 与声明不是整数倍，或 `uv_rotation` 未烘焙 |
| 模型被压成"平板" | `free` 格式 cube 旋转未剥离 → 加 `--strip-rotation` |
| 图标像从模型内部往外看 | 渲染时漏了 `near_smaller=False` |
| 护甲不跟随动作 | 骨骼名拼错（必须精确 `armorBody` 等） |

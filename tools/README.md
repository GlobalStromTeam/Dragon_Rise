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
| `batch_container_icons.py` | **载具集装箱图标批量重刷**（128×128，遍历 vehicles/*.json，可 `--ids` 指定） |
| `icon_preview.py` | 拼图预览 / ASCII 剪影排查（`--cols` 多列、`--ascii` 看缺口） |
| `angle_scan.py` | 视角扫描，`--ref` 时按剪影 IoU 排序找最佳角度 |
| `bb_icon_render.js` | Blockbench 侧截图脚本（`__renderIcon/__renderShot`），需要 Blockbench + MCP 离屏预览 |
| `icon_jobs_armor.json` | 之前那批护甲（chinese_desert / russian_soldier / ukrainian_soldier）的图标任务模板 |
| `geo_ortho_view.py` / `geo_tex_view.py` / `geo_bone_bbox.py` / `geo_bone_rename.py` | geo 正交视图 / 贴图查看 / 骨骼包围盒 / 骨改名 |
| `载具图标渲染流程.md` | 载具图标完整流程文档（角度、取景、像素闭环） |

### 4.1 载具集装箱图标：AI 调用契约（改渲染器前必读）

```bash
# 批量重刷某个版本的全部载具（会自动同步 build/resources/main）
py tools/batch_container_icons.py
py tools/batch_container_icons.py --ids m1128,f15e
py tools/batch_container_icons.py --root E:/Dragon_Rise/Dragon_Rise_1211    # 跨版本

# 单台精修（可调 margin / 是否压国旗）
py tools/make_vehicle_icons.py <id> [--container-only] [--root ...]

# 出图后自检
py tools/icon_preview.py <目录> --cols 6 --scale 2 --out audit.png
py tools/icon_preview.py a.png --ascii        # 看缺口/浮空件
```

**硬性参数（不要"优化"）**

| 项 | 值 | 原因 |
|---|---|---|
| 输出路径 | `assets/superbwarfare/textures/vehicle_icon/container/<id>.png` | SBW 的 `ContainerItemDecorator` 把命名空间写死为 superbwarfare |
| 尺寸 / 内容占比 | 128×128 / fill 0.85 | 与 SBW 官方图标一致 |
| yaw | 225（地面载具**和**飞机都是） | 模型机头朝 −Z；飞机不是 +Z，曾用 45 全部方向错 |
| pitch | 26.565（atan 1/2） | 2:1 等距 |
| 超采样 | 6 | 细节密的模型 3 倍偏粗 |
| 深度 | `near_smaller=False` | 反了会渲染成"从模型内部往外看" |

**渲染器里 6 个已修缺陷，改动前逐条对照**（每一条都对应一批坏图）

1. 欧拉角是"以 Y 为镜像轴的左手系"：**X/Z 取负、Y 保持原符号**（三轴全取负会让偏心 pivot 的大角度部件飞出模型）
2. `poly_mesh` 的 UV 原点在**左下**，v 必须 `1-v`（否则整个自由网格取到贴图空白区，表现为"该部件没渲染"）
3. `poly_mesh` 顶点索引三元组顺序是 **`[位置, 法线, UV]`**，UV 取 `e[2]`（取 `e[1]` 会让一个四边形横跨大半个贴图，呈现乱色块）
4. 立方体 `uv` 有两种写法：逐面字典，或 **`[u, v]` 列表（标准 box uv）**；后者要按展开图推导六面矩形，漏了会整面不画
5. **EAST 面**（face_index=5）在展开条带上的 u 与 WEST 相反，需镜像 u
6. 辅助骨骼要**沿 parent 链**判断：`Obb` 下的 `left`/`right` 等子骨骼挂着 2×2 UV 的碰撞板，只看自身名字会漏，把机舱整块盖成灰色

**验收标准**：任一旋翼/履带/轮胎不应缺失；机身不应出现整块灰白面；细天线、炮管等 1px 特征应保留（`alpha_composite` 而非 `paste`，否则 alpha 被平方抹掉细节）。


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


---

## 六、武器站（副武器 / 车顶机枪）

**先读 [`docs/武器站骨骼绑定.md`](../docs/武器站骨骼绑定.md)**，那里写了三种可用写法、SBW 的硬编码骨骼名、坐标换算与排错表。

```bash
node tools/check_weapon_station.mjs <项目根目录>          # 校验 data ↔ 模型骨骼引用
node tools/check_weapon_station.mjs <项目根目录> --strict  # 把"提示"也算错误
```

三种写法速查：

| 写法 | 模型骨骼 | 数据关键字段 | 模型跟不跟视角转 |
|---|---|---|---|
| A 绑定骨骼（银手5 / CAV MK19） | `<前缀>Yaw` → `<前缀>Pitch` | `BoundBonesYaw` / `BoundBonesPitch` + `Transform: Vehicle` | 转 |
| B 标准武器站（2s25m 等） | `passengerWeaponStationYaw` / `...Pitch`（名字写死） | `Transform: WeaponStationBarrel` + 根级 `PassengerWeaponStation*` | 转 |
| C 车体固定（M2HB、AC-130 各炮） | 无要求 | `Transform: Vehicle` + `Directions: ["Passenger"]` | 不转，只跟随开火方向 |

**硬要求**：A 方案的骨骼名必须以 `Yaw` / `Pitch` 结尾（`move_xxxa` 这种不会转）。
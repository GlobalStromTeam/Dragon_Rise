# CH-53E 导入记录（可复现）

源资产：`D:\桌面\新建文件夹 (8)\`（`ch53e_super_stallion.obj/.mtl` + `textures/`）

## 一键复现

```powershell
$py = "C:\Users\Administrator\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe"
$obj = "D:\桌面\新建文件夹 (8)\ch53e_super_stallion.obj"

# 1) OBJ -> SBM poly_mesh geo.json（UV：v 落在 -1..0，用 --uv-add 0 1；车头转 180° 对齐仓库惯例）
& $py ..\obj2polymesh.py $obj `
  --out ..\..\src\main\resources\assets\dragonrise_reforge\models\bedrock\vehicle\ch53e.geo.json `
  --tex-size 2048 2048 --scale 10.66 --split material `
  --uv-add 0 1 --yaw 180 `
  --parts ch53e_parts.json --pivots ch53e_pivots.json `
  --identifier geometry.ch53e

# 2) 贴图：basecolor(jpg) + alpha(png) 合并成 RGBA
& $py -c "from PIL import Image; b=Image.open(r'D:\桌面\新建文件夹 (8)\textures\ch53e_super_stallion.jpg').convert('RGB'); a=Image.open(r'D:\桌面\新建文件夹 (8)\textures\ch53e_super_stallion_a.png').convert('L'); Image.merge('RGBA',(*b.split(),a)).save(r'..\..\src\main\resources\assets\dragonrise_reforge\textures\entity\ch53e.png')"

# 3) 图标（容器 128 + 击杀横幅 512x128）
& $py ..\make_vehicle_icons.py ch53e --no-flag

# 4) 校验
& $py ..\check_polymesh_geo.py ..\..\src\main\resources\assets\dragonrise_reforge\models\bedrock\vehicle\ch53e.geo.json
```

载具数据/注册/语言已由 `install_assets.py` 与手改的 Java 落库（见下）。

## 分件规则

`ch53e_parts.json`：
- `move_tailPropeller`：bbox `[3.0,6.2, 5.8,11.2, -32.2,-26.8]`（尾桨在右侧、z≈-29.4）
- `move_propeller`：绕轮毂 `(x=0.08, z=4.16)` 半径 ≥8 且 y∈[3,10]（只取叶片，轮毂留给机体）
- 其余 → `body`

`ch53e_pivots.json`（OBJ 坐标）：主旋翼轮毂 `(0.08, 6.40, 4.16)`、尾桨轮毂 `(5.11, 8.62, -29.41)`。

模型单位 16 = 1 格；`--scale 10.66` 让整机约 42.6 格长（UH-60 是 27.9 格，真机长度比 30.2m/19.8m≈1.53，对得上）。

## 落库文件

| 类型 | 路径 |
| --- | --- |
| 模型 | `assets/dragonrise_reforge/models/bedrock/vehicle/ch53e.geo.json`（1.87MB / 22052 三角 / 4 骨骼） |
| 贴图 | `assets/dragonrise_reforge/textures/entity/ch53e.png`（2048 RGBA） |
| 载具数据 | `data/dragonrise_reforge/sbw/vehicles/ch53e.json`（OBB/座位按 UH-60 ×1.5） |
| 模型覆盖 | `assets/superbwarfare/sbw/vehicles/ch53e.json`、`assets/dragonrise_reforge/sbw/vehicles/ch53e.json` |
| 图标 | `assets/dragonrise_reforge/textures/vehicle_icon/ch53e_icon.png`、`assets/superbwarfare/textures/vehicle_icon/container/ch53e.png` |
| 实体/渲染器 | `entities/Ch53EEntity.java`、`client/renderer/entity/Ch53ERenderer.java` |
| 注册 | `init/ModEntities.java`、`init/ModEntityRenderers.java`、`init/ModTabs.java` |
| 语言 | `lang/zh_cn.json`、`lang/en_us.json`（`entity./info.dragonrise_reforge.ch53e`） |

## 游戏内验证

```
/summon dragonrise_reforge:ch53e ~ ~3 ~
```
或创造模式「载具」页签里的 CH-53E 集装箱物品。

## 待调

- `EngineInfo`（速度/升力）沿用 UH-60 后略微下调，实际手感按需改 `data/.../ch53e.json`。
- `OBB` 与座位位置是 ×1.5 估算，下车点/第三人称镜头可能要再挪。
- 主旋翼骨骼只含叶片（r≥8），如需连轮毂一起转，在 `ch53e_parts.json` 增加一条
  `{"bone":"move_propeller","cyl":{"x":0.08,"z":4.16,"r":[0,8],"y":[7.6,10]}}`（会带上整流罩）。

## 旋翼不转的两种「骨骼问题」（2026-11-23 追查记录）

1. **名字差一个数字**：Blockbench 里给尾桨骨骼起名 move_tailPropeller1 / move_tailPropeller2，
   而渲染器是精确匹配 getBone("move_propeller") / getBone("move_tailPropeller")。
2. **几何没挂在那根骨骼上**：move_propeller 是空骨骼，桨叶的 poly_mesh 还留在别的骨骼里
   （实测那次的文件：主旋翼 8706 三角挂在 move_tailPropeller1，尾桨 2084 三角挂在
   move_tailPropeller2，move_propeller 一点几何都没有）。

修法（自动判定 + 搬迁 + 重算桨毂枢轴 + 备份 .bak）：

`powershell
python tools/ch53e_import/fix_rotor_bones.py 模型.geo.json --dry-run   # 先看判定
python tools/ch53e_import/fix_rotor_bones.py 模型.geo.json             # 就地修
`

判定规则：X 跨距最大的 poly_mesh = 主旋翼；剔除体量<main 5% 的碎片后，|z| 最靠后的 = 尾桨。
桨毂位置用「各桨叶离质心最近顶点的均值」算，避免旋转时甩圈。

渲染器侧新增三行一次性的 [rotor] 日志，直接分辨：无旋翼骨骼 / 骨骼在但没几何 / 引擎没启动或能量为 0。

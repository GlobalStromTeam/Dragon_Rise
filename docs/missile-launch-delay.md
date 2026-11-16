# 地面导弹发射延迟

2026-11-16：为 25 个现代地面载具和 2 个架设反坦克导弹配置增加 1.5 秒发射延迟，共覆盖 29 个导弹武器/弹种选项。

## 覆盖范围

| 类别 | 配置 ID |
| --- | --- |
| 炮射导弹 | bmd4m、bmp3、m1128、vt4a1、vt4b、zbd04a、zlt11、ztd05、ztq15、ztz99a、ztz99ah、ztz99bh |
| 车载反坦克导弹 | bmpt72、btr90、cv90、humveetow、m3a3、zbd05、zbl08 |
| 地面防空导弹 | 625e、akm、flarakpz1、l1a2、tunguska、zsu234 |
| 架设反坦克导弹 | 9m133、hj8 |

CV90 的两种瞄准选项和 M3A3 的普通/顶攻 TOW 弹种均覆盖。炮射导弹仅修改对应的 `AmmoType[].Override`，普通炮弹、机炮、机枪和火箭保持原来的发射时间及声音。飞机、直升机、舰船、历史载具未改。

## 时间线与播放

使用当前 Superb Warfare 已有的 `ShootDelayTime: 30`，由前置服务端排队生成导弹，单位是游戏 tick（正常 20 TPS 时为 1.5 秒）。声音由现有 `Fire1P/Fire3P/Fire3PFar/Fire3PVeryFar` 播放链路播放，新组合音频排列为：

| 时刻 | 内容 |
| --- | --- |
| 0.0 秒 | TOW 触发 click，线性增益 1.0 |
| 0.1 秒 | TOW electric，线性增益 0.15 |
| 1.5 秒 | 原有对应视角的完整发射音频，线性增益 1.0；服务端同时生成导弹 |

触发声来自 `E:/SquadGame/Content/Vehicles/Emplaced_BGM71TOW/Sounds/Fire/Delay/Start/`：`tow_fire_start_click_02.wav`（2.417 秒）与 `tow_fire_start_electric_01.wav`（3.2 秒）。该用途素材各只有一条，未复制凑变体。完整保留两条触发素材，所以其尾部可以与出筒声叠加。

触发 click/electric 加入成员/第一人称和外部近层，中远层只把原有声音后移 1.5 秒。沿用原有各距离层及 `SoundRadius`，没有把近距离机械触发声混入远场。

新建 21 个共享声音事件、39 条 OGG，保留原有事件和文件，不改变其它载具对旧声音的使用。原声的随机变体数量、权重及播放设置均保留。第一人称双声道，外部单声道；44.1 kHz，Vorbis q10。此处从现有 OGG 再编码，使用 q10 减少二次有损编码误差；没有重做响度标定、压缩、限幅或削减尾音。播放音量系数均为 1.0。

| 对象 | 沿用的发射声 |
| --- | --- |
| BMP-3/BMD-4M、04A 及中系炮射导弹 | 本包 APS03（保持已有低压炮混音） |
| M1128 炮射导弹 | 原配置 Superb Warfare M1A2 声组 |
| BMPT72、BTR90、短号 | 本包 9M113 |
| CV90 | 原配置 Superb Warfare BMP-2 导弹声 |
| M3A3、Humvee TOW | 本包 TOW |
| ZBD05、ZBL08 | 本包红箭 73C（已有 AT-3 近似素材） |
| 红箭 8 | 本包 HJ8 成员声，9M113 外部声 |
| 六个地面防空配置 | 原配置 Superb Warfare bomb_release |

## 架设武器发射收尾

短号和红箭 8 的实体原先在调用 `super.vehicleShoot` 后立即改变筒状态、产生尾焰和伤害，红箭 8 还会抛出空筒。现在通过本包 `MissileLaunchEffects.afterLaunch`，读取同一个 `ShootDelayTime` 后执行原有收尾逻辑。三个开火重载均接入，保持原收尾内容和参数；实体移除或成为残骸后不再执行这些效果。

除声音事件注册与上述两个架设武器的收尾调度外，没有修改其它 Java 行为。载具性能、武器射速、弹速、伤害、装填配置、模型、OBB 和动画资源保持原样。

## 验证

- 严格检查 167 个载具 JSON，无重复键；目标为 27 个配置、29 个导弹选项，导弹延迟均为 30 tick。
- 配置仅修改 `ShootDelayTime` 及对应开火事件引用，验证普通炮弹与非目标武器保持原样。
- 校验新增事件注册、39 条音频的原生声道/采样率、1.5 秒源偏移、完整源长度和尾段能量。
- 对比编码前后的完整时间线，最低信噪比约 32.59 dB，末段能量比为 1.0006～1.0068，未裁剪尾音。
- 使用真实调度辅助类的独立 JVM 测试验证第 30 tick 与模拟导弹生成对齐、仅执行一次、客户端不重复执行，以及移除/残骸/空武器防护。
- JDK17 离线构建及最终 JAR 配置、音频资源一致性检查通过；未进行游戏内试听。

原始快照、素材 SHA-256、逐项组合层信息及验证报告保存在忽略目录 `build/missile_launch_delay_20261116/`。

## 编码后响度

以下是每组随机变体中的最大 100ms RMS，按 Minecraft PCM16 转换口径测量，单位 dBFS。原出筒声列不包含新触发层，组合声列包含完整触发/等待/出筒时间线；两列不代表在游戏混音中的最终响度。

| 原声音事件 | 视角字段 | 原出筒声 | 新组合声 |
| --- | --- | ---: | ---: |
| `superbwarfare:bomb_release` | Fire1P | -9.57 | -7.98 |
| `superbwarfare:bomb_release` | Fire3P | -6.57 | -6.53 |
| `dragonrise_reforge:9m113_fire_1p` | Fire1P | -4.70 | -4.64 |
| `dragonrise_reforge:9m113_fire_3p` | Fire3P | -18.13 | -6.53 |
| `dragonrise_reforge:aps03_fire_1p` | Fire1P | -4.14 | -4.08 |
| `dragonrise_reforge:aps03_fire_3p` | Fire3P | -3.76 | -3.72 |
| `dragonrise_reforge:aps03_fire_3p_far` | Fire3PFar | -6.79 | -6.75 |
| `dragonrise_reforge:aps03_fire_3p_very_far` | Fire3PVeryFar | -11.45 | -11.39 |
| `dragonrise_reforge:9m113_fire_3p_far` | Fire3PFar | -25.50 | -25.46 |
| `dragonrise_reforge:9m113_fire_3p_very_far` | Fire3PVeryFar | -31.94 | -31.92 |
| `superbwarfare:bmp_2_missile_fire_1p` | Fire1P | -11.44 | -7.98 |
| `superbwarfare:bmp_2_missile_fire_3p` | Fire3P | -3.72 | -3.67 |
| `dragonrise_reforge:hj8_fire_1p` | Fire1P | -11.42 | -7.98 |
| `dragonrise_reforge:tow_fire_1p` | Fire1P | -11.42 | -7.98 |
| `dragonrise_reforge:tow_fire_3p` | Fire3P | -3.74 | -3.70 |
| `superbwarfare:m_1a_2_fire_1p` | Fire1P | -2.36 | -2.34 |
| `superbwarfare:m_1a_2_fire_3p` | Fire3P | -1.17 | -1.16 |
| `superbwarfare:m_1a_2_far` | Fire3PFar | -5.41 | -5.36 |
| `superbwarfare:m_1a_2_veryfar` | Fire3PVeryFar | -10.65 | -10.62 |
| `dragonrise_reforge:hj73c_fire_1p` | Fire1P | -11.44 | -7.98 |
| `dragonrise_reforge:hj73c_fire_3p` | Fire3P | -3.72 | -3.71 |

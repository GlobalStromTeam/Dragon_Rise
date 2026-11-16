# 地面载具准星与 HUD 配置

2026-11-16 检查 Dragonrise ReForge 的 114 个地面配置（Track、Wheel、WheelChair、Fixed），调整其中 108 个配置的 475 个显示字段。飞机、直升机、舰船及仅提供模型/OBB 的局部配置不在本次范围。

## 颜色规则

武器准星使用 `Weapons.<武器>.CrosshairColor`，陆地 HUD 使用载具根级 `HUDColor`。两者分别设置；弹药 `Override` 中已有的颜色覆盖也要同步。

| 范围 | 准星与 HUD |
| --- | --- |
| 中国、俄罗斯/苏联坦克 | 红色 `0xFF0000` |
| ZLT11、ZTD05、2S25M 等突击炮/坦克歼击车 | 红色 `0xFF0000` |
| 步兵战车、装甲输送车及中俄其它地面车辆 | 绿色 `0x66FF00` |
| 北约国家车辆和地面武器，含其历史车辆 | 绿色 `0x66FF00` |
| CV90，覆盖上述绿色规则 | 红色 `0xFF0000` |
| 其它未指定国家、架空车型和未指定阵营的固定武器 | 保留原有颜色 |

114 个配置中，25 个使用红色规则，67 个使用绿色规则，22 个保留原有颜色。无武器车辆只处理适用的 HUD 颜色。

## 准星选型

| 武器/瞄具 | 使用资源 |
| --- | --- |
| 现代中国坦克 | 本包 `cn_tank_cannon_cross.png` |
| 现代俄罗斯坦克、天马虎 | 本包 `ru_tank_cannon_cross.png` |
| 美英德等现代坦克、梅卡瓦、Strv103 | `@VehicleUsTank` |
| 勒克莱尔及其测试配置 | 本包 `fr_tank_cannon_cross.png` |
| BMP-3、BMD-4M、BMPT72、BTR90、BRDM2 | `@VehicleRuApc` |
| ZBD04A、ZBD05、ZBL08、CAV | `@VehicleCnTank` |
| M3A3、M1296、CM34 | `@VehicleUsApc` |
| CV90 主炮与同轴机枪 | 本包 `cv90_cross.png` |
| 历史火炮、火炮阵地、无后坐力炮、火箭炮 | `@VehicleCommonCannon` |
| 高射机炮、自动榴弹发射器 | `@VehicleCommonGunDynamic` |
| 线导导弹，含 TOW 顶攻弹 | `@VehicleCommonMissile` |
| 自导导弹、便携防空导弹 | `@VehicleCommonSeekMissile` |
| 独立机枪、车顶/乘员机枪 | `@VehicleCommonGun` |

同一炮手瞄具下的同轴机枪沿用该瞄具；独立乘员机枪采用机枪准星。切换普通炮弹、榴弹、霰弹时保留同一光学准星，切换到导弹时使用对应导弹准星。架空车型按已有武器类型选择现有准星资源。

历史火炮及固定炮的开镜刻度使用显式路径 `superbwarfare:textures/overlay/vehicle/crosshair/common_cannon_zooming.png`。当前前置的 `@VehicleCommonCannonZooming` 专用分支没有传入颜色；显式纹理路径走普通着色分支，保证开镜后仍遵循红绿规则。

已修复 BMP-3 的 `@Custom` 空准星、Pak40 的透明 `non.png`、M270 的空准星，以及俄系步战车沿用美系准星的问题。CV90 启用已有专用纹理。

## 校验

- 167 个载具 JSON 均可严格解析，无重复键。
- 114 个地面配置的武器模板、弹种覆盖、开镜准星和颜色继承已校验。
- 所有变更仅限 `Crosshair`、`CrosshairZooming`、`CrosshairColor` 和根级 `HUDColor`；其它源文件和非地面配置保持原样。
- 13 项准星纹理资源已核对本包与项目固定版本的 Superb Warfare 依赖。
- Gradle 离线构建和最终 JAR 资源一致性检查通过；尚未进行游戏内逐车目视测试。

本轮临时审计与原始快照位于忽略的 `build/ground_crosshairs_20261116/`。

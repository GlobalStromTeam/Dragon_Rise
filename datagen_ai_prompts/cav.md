# AI 填充任务：载具 `cav`

该载具模型含 Build 骨骼，属于可自动生成数据的车辆。**请重点补全 `EngineInfo`（载具性能）与 `Weapons`（武器）字段**，其他字段可忽略。**已有字段保持原样，不得改动**。输出完整 JSON。

## 载具类型

`CAR`

## 本次重点关注（缺失）

```
EngineInfo
```

## 类型专属填写指南（CAR）

- `MaxHealth`：**100-300**（汽车）
- `HudType`：`@Land`
- `EngineType`：`Wheel`（轮式）；`EngineSound` 填音效 ID
- `EngineInfo` 应包含：`Buoyancy`, `EnergyCostRate`, `WheelRotSpeed`, `WheelDifferential`, `MaxForwardSpeedRate`, `MaxBackwardSpeedRate`, `Increment`, `Decrement`, `SteeringSpeed`, `EngineSoundVolume`
- 炮塔：`TurretPos`/`BarrelPos`（若模型有）、`TurretTurnSpeed`/`TurretYawRange`/`TurretPitchRange`
- 地形：`TerrainCompat`
- 武器典型：`Cannon`/`MachineGun`/`Missile`（按模型射击点骨骼）
- 无需字段：飞行器的 `PitchSpeed`/`YawSpeed`/`RollSpeed`/`LiftSpeed`/`HasGear`

## 速度参考（所有载具统一）

- `MaxEnergy`：**默认 100000**（所有载具）
- `Increment`/`Decrement`：前进/倒车**加速度**，**建议统一填 `0.01`**
- `MaxForwardSpeedRate`（最大前进速度）：填 `1` 时最快 80 km/h → 每 `0.01` = 0.8 km/h。按目标速度换算：目标km/h ÷ 0.8 × 0.01（例：60 km/h → 60 ÷ 0.8 × 0.01 = 0.75；40 km/h → 0.5）
- `MaxBackwardSpeedRate`（最大倒车速度）：填 `1` 时最快 64 km/h → 每 `0.01` = 0.64 km/h。按目标速度换算：目标km/h ÷ 0.64 × 0.01（例：32 km/h → 32 ÷ 0.64 × 0.01 = 0.5；16 km/h → 0.25）

## HudType 可选值（供选择）

- `@Land`：陆地载具（坦克/装甲车/汽车）
- `@Aircraft`：固定翼飞行器
- `@OldAircraft`：旧式/螺旋桨固定翼（如二战螺旋桨机）
- `@Helicopter`：直升机
- `@Artillery`：自行火炮/火炮类
- `@Kirov`：飞艇（原版基洛夫专用 HUD）

## Type 可选值（供选择）

- `Tank`：坦克
- `APC`：装甲运兵车/步兵战车
- `Car`：汽车/轮式车辆
- `AA`：防空
- `Artillery`：自行火炮
- `Defense`：固定防御设施
- `Airplane`：固定翼飞行器
- `Helicopter`：直升机
- `AirShip`：飞艇
- `Boat`：舰船/水上载具
- `Drone`：无人机
- `Special`：特殊载具

## EngineType 可选值（供选择）

- `Track`：履带（坦克/履带装甲车）
- `Wheel`：轮式（汽车/轮式装甲车）
- `Aircraft`：固定翼喷气/螺旋桨引擎
- `Helicopter`：直升机旋翼引擎
- `AirShip`：飞艇引擎
- `Ship`：舰船引擎
- `Empty`：无引擎（静态/防御设施）
- `Fixed`：固定引擎（其他固定类型）
- `WheelChair`/`Tom6`：特殊（一般不用）


## ContainerIcon 载具类型图标选择

根据载具的国家/阵营与类别，从以下图标中选择 `ContainerIcon`（值为完整资源路径 `dragonrise_reforge:textures/gui/vehicle/type/<文件名>`）：

**国家/阵营前缀**：`cn`=中国、`us`=美国、`ru`=俄罗斯、`uk`=英国、`jp`=日本、`fr`=法国、`gm`=德国、`se`=瑞典、`ussr`=苏联、`dr`=**二战德国**、`blue`=蓝方阵营、`red`=红方阵营
**类别后缀**：`land`=陆地载具、`aircraft`=飞行器、`water`=水上载具

**本模组可用图标**：
```
blue_aircraft.png
blue_land.png
blue_water.png
cn_aircraft.png
cn_land.png
cn_water.png
dr_aircraft.png
dr_land.png
dr_water.png
fr_aircraft.png
fr_land.png
gm_aircraft.png
gm_land.png
gm_water.png
jp_aircraft.png
jp_land.png
jp_water.png
kr_land.png
red_aircraft.png
red_land.png
red_water.png
ru_aircraft.png
ru_land.png
se_aircraft.png
se_land.png
uk_aircraft.png
uk_land.png
uk_water.png
us_aircraft.png
us_land.png
us_water.png
ussr_aircraft.png
ussr_land.png
ussr_water.png
```
**superbwarfare 通用图标**（`superbwarfare:textures/gui/vehicle/type/<名>.png`）：`land`（陆地）、`aircraft`（固定翼）、`helicopter`（直升机）、`water`（水上）、`airship`（飞艇）、`defense`（防御）、`civilian`（民用）、`otto`
- 例：中国陆地载具 → `dragonrise_reforge:textures/gui/vehicle/type/cn_land.png`；二战德国陆地 → `dragonrise_reforge:textures/gui/vehicle/type/dr_land.png`；美国飞行器 → `dragonrise_reforge:textures/gui/vehicle/type/us_aircraft.png`


- 通用：`VehicleIcon` 填 `dragonrise_reforge:textures/vehicle_icon/XXX_icon.png`；`VehicleContainerType` 按载具大小（`Empty`/`Mini`/`Small`/`Medium`/`Large`/`Huge`）


## 官方参考载具（同类型，供参照）

**官方参考：LAV-25 轮式装甲车（lav_25.json）**
```
"Type": "APC", "HudType": "@Land", "EngineType": "Wheel",
"MaxHealth": 300, "MaxEnergy": 5000000, "VehicleContainerType": "Medium",
"EngineInfo": { "Buoyancy": 0.052, "EnergyCostRate": 64, "WheelRotSpeed": 1.75, "WheelDifferential": 0.3,
  "MaxForwardSpeedRate": 0.95, "MaxBackwardSpeedRate": 0.6, "Increment": 0.027, "Decrement": 0.017,
  "SteeringSpeed": 0.065 },
"Weapons": 键为 Cannon（25mm机炮）、MachineGun（同轴机枪）、Missile
```

## 现有数据（骨架，保留不变）

```json
{
  "ID": "dragonrise_reforge:cav",
  "Type": "APC",
  "HudType": "@Land",
  "EngineType": "Wheel",
  "Weapons": {
    "Cannon": {
      "DefaultFireMode": "Auto",
      "AvailableFireModes": "Auto",
      "DefaultZoom": 3,
      "RPM": 250,
      "Velocity": 18,
      "Damage": 65,
      "ExplosionDamage": 8,
      "ExplosionRadius": 2,
      "HeatPerShoot": 8,
      "NaturalCooldown": 1,
      "RecoilTime": 31,
      "RecoilForce": 0.62,
      "ShootAnimationTime": 3,
      "Spread": 0.5,
      "ProjectileLife": 40,
      "Crosshair": "@VehicleUsApc",
      "CrosshairColor": "0x66FF00",
      "Name": "weapon.superbwarfare.25mm_cannon_ap",
      "Projectile": "superbwarfare:small_cannon_shell",
      "SoundInfo": {
        "Fire1P": "superbwarfare:bradley_cannon_fire_1p",
        "Fire3P": "superbwarfare:bradley_cannon_fire_3p",
        "Fire3PFar": "superbwarfare:bradley_cannon_far",
        "Fire3PVeryFar": "superbwarfare:bradley_cannon_veryfar",
        "Change": "superbwarfare:into_missile"
      },
      "SoundRadius": 24,
      "Icon": "superbwarfare:textures/overlay/vehicle/weapon/icons/cannon_25mm_ap.png",
      "AmmoType": [
        "superbwarfare:small_shell_ap",
        {
          "Ammo": "superbwarfare:small_shell_he",
          "Override": {
            "Icon": "superbwarfare:textures/overlay/vehicle/weapon/icons/cannon_25mm_he.png",
            "Velocity": 15,
            "Damage": 15,
            "ExplosionDamage": 18,
            "ExplosionRadius": 5,
            "Name": "weapon.superbwarfare.25mm_cannon_he"
          }
        },
        {
          "Ammo": "superbwarfare:small_shell_gs",
          "Override": {
            "Icon": "superbwarfare:textures/overlay/vehicle/weapon/icons/cannon_25mm_gs.png",
            "Spread": 3,
            "Velocity": 10,
            "Damage": 8,
            "ProjectileAmount": 16,
            "DamageReduce": {
              "Type": "Shotgun"
            },
            "ExplosionDamage": 0,
            "ExplosionRadius": 0,
            "Projectile": "superbwarfare:projectile",
            "Name": "weapon.superbwarfare.25mm_cannon_gs"
          }
        }
      ],
      "ShootPos": {
        "Transform": "Barrel",
        "Positions": [
          [0.451,-0.002,1.676]
        ],
        "Directions": [
          "Barrel"
        ]
      }
    },
    "Missile": {
      "DefaultFireMode": "Semi",
      "AvailableFireModes": "Semi",
      "DefaultZoom": 3,
      "HeatPerShoot": 0.5,
      "Spread": 0.5,
      "CrosshairColor": "0x66FF00",
      "Name": "weapon.superbwarfare.tow_missile",
      "AmmoType": "superbwarfare:medium_anti_ground_missile",
      "Projectile": "superbwarfare:wire_guide_missile",
      "ProjectileLife": 400,
      "Magazine": 2,
      "EmptyReloadTime": 200,
      "Damage": 600,
      "ExplosionDamage": 60,
      "ExplosionRadius": 8,
      "RPM": 60,
      "Velocity": 3,
      "RecoilTime": 18,
      "RecoilForce": 0.05,
      "ShootPos": {
        "Transform": "Turret",
        "Positions": [
          [-1.166,0.725,0.573]
        ],
        "Directions": [
          "Turret"
        ],
        "ViewDirection": "Barrel"
      },
      "Crosshair": "@VehicleCommonMissile",
      "SoundInfo": {
        "Fire1P": "superbwarfare:bmp_2_missile_fire_1p",
        "Fire3P": "superbwarfare:bmp_2_missile_fire_3p",
        "VehicleReload3p": "superbwarfare:medium_missile_reload",
        "VehicleReloadSoundTime": 28,
        "Change": "superbwarfare:into_missile"
      },
      "SoundRadius": 12,
      "Icon": "superbwarfare:textures/overlay/vehicle/weapon/icons/missile_tow.png"
    },
    "PassengerMachineGun": {
      "DefaultFireMode": "Auto",
      "AvailableFireModes": "Auto",
      "Projectile": "superbwarfare:gun_grenade",
      "DefaultZoom": 3,
      "RPM": 280,
      "Magazine": 20,
      "EmptyReloadTime": 100,
      "Velocity": 8,
      "Gravity": 0.05,
      "Damage": 55,
      "ExplosionDamage": 10,
      "ExplosionRadius": 5,
      "HeatPerShoot": 6,
      "NaturalCooldown": 1,
      "RecoilTime": 31,
      "RecoilForce": 1.62,
      "ShootAnimationTime": 3,
      "Spread": 1.2,
      "Crosshair": "@VehicleCommonGun",
      "CrosshairColor": "0xFFC700",
      "Name": "dragonrise_reforge:mk19",
      "SoundInfo": {
        "Fire1P": "dragonrise_reforge:zbd04a_30mm_fire_1p",
        "Fire3P": "dragonrise_reforge:zbd04a_30mm_fire_3p",
        "Fire3PFar": "dragonrise_reforge:zbd04a_30mm_fire_3p_far",
        "Fire3PVeryFar": "superbwarfare:mi_28_cannon_veryfar",
        "Change": "dragonrise_reforge:change_weapon"
      },
      "SoundRadius": 24,
      "AmmoType": "superbwarfare:grenade_40mm",
      "Icon": "dragonrise_reforge:textures/overlay/vehicle/weapon/icons/grenade.png",
      "ShootPos": {
        "Transform": "Vehicle",
        "Positions": [
          [-0.856,2.366,1.116]
        ],
        "Directions": [
          "Passenger"
        ]
      }
    }
  },
  "Seats": [
    {
      "HidePassenger": true,
      "Transform": "Turret",
      "Position": [-0.459,-0.367,0.295],
      "Weapons": [
        "Cannon",
        "Missile"
      ],
      "CanRotateHead": false,
      "MinPitch": -6,
      "MaxPitch": 20,
      "Sensitivity": [0.52,0.75,0.9],
      "HasThermalImaging": true
    },
    {
      "HidePassenger": true,
      "Transform": "Vehicle",
      "Position": [-0.627,0.892,0.738],
      "MinPitch": -20,
      "MaxPitch": 60,
      "MinYaw": -90,
      "MaxYaw": 90,
      "Weapons": [
        "PassengerMachineGun"
      ],
      "Sensitivity": [0.59,0.8,0.9],
      "HasThermalImaging": true,
      "CameraPos": {
        "UseFixedCameraPos": true,
        "Transform": "Vehicle",
        "Position": [-0.73,2.6,0.6],
        "ZoomPosition": [-0.73,2.62,1.2]
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [-0.717,1.022,-2.425],
      "MinYaw": -95,
      "MaxYaw": 95
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [0.533,1.022,-2.55],
      "MinYaw": -95,
      "MaxYaw": 95
    }
  ],
  "OBB": [
    {
      "Size": [1.223,0.606,2.919],
      "Position": [0.02,1.387,-0.668]
    },
    {
      "Size": [1.223,0.225,1.028],
      "Position": [0.02,1.542,2.683],
      "CustomRotate": [22.5,0.0,0.0]
    },
    {
      "Size": [1.223,0.373,0.621],
      "Position": [0.02,0.998,2.907],
      "CustomRotate": [-35.0,0.0,0.0]
    },
    {
      "Size": [1.263,1.039,3.46],
      "Position": [0.0,1.034,-0.037],
      "Part": "Collision"
    },
    {
      "Size": [0.626,0.445,0.76],
      "Position": [-0.6,1.609,1.064],
      "Part": "MainEngine"
    },
    {
      "Size": [0.184,0.551,0.59],
      "Position": [1.089,0.555,2.111],
      "Part": "WheelLeft"
    },
    {
      "Size": [0.184,0.551,0.59],
      "Position": [1.089,0.555,0.831],
      "Part": "WheelLeft"
    },
    {
      "Size": [0.184,0.551,0.59],
      "Position": [1.089,0.555,-0.819],
      "Part": "WheelLeft"
    },
    {
      "Size": [0.184,0.551,0.59],
      "Position": [1.089,0.555,-2.014],
      "Part": "WheelLeft"
    },
    {
      "Size": [0.184,0.551,0.59],
      "Position": [-1.089,0.555,2.111],
      "Part": "WheelRight"
    },
    {
      "Size": [0.184,0.551,0.59],
      "Position": [-1.089,0.555,0.831],
      "Part": "WheelRight"
    },
    {
      "Size": [0.184,0.551,0.59],
      "Position": [-1.089,0.555,-0.819],
      "Part": "WheelRight"
    },
    {
      "Size": [0.184,0.551,0.59],
      "Position": [-1.089,0.555,-2.17],
      "Part": "WheelRight"
    },
    {
      "Size": [0.2,0.154,1.106],
      "Position": [0.001,0.739,0.365],
      "Part": "Turret",
      "Transform": "Turret",
      "Rotation": "Turret"
    },
    {
      "Size": [0.543,0.243,0.542],
      "Position": [-0.843,0.718,-0.149],
      "Part": "Turret",
      "Transform": "Turret",
      "Rotation": "Turret"
    },
    {
      "Size": [0.592,0.12,0.608],
      "Position": [0.003,0.033,-0.003],
      "Part": "Turret",
      "Transform": "Turret",
      "Rotation": "Turret"
    }
  ],
  "TurretPos": [-0.003,2.079,-0.582],
  "BarrelPos": [-0.443,0.655,-0.023],
  "TerrainCompat": [
    [1.703,0.0,3.286],
    [-1.702,0.0,3.286],
    [1.703,0.0,1.243],
    [-1.702,0.0,1.243],
    [1.703,0.0,-1.301],
    [-1.702,0.0,-1.301],
    [1.703,0.0,-3.345],
    [-1.702,0.0,-3.345]
  ],
  "PassengerWeaponStationTurnSpeed": [10.0,10.0],
  "PassengerWeaponStationPitchRange": [-9,60],
  "VehicleIcon": "dragonrise_reforge:textures/vehicle_icon/cav_icon.png",
  "Model": {
    "Model": "dragonrise_reforge:models/bedrock/vehicle/cav.geo.json",
    "Texture": "dragonrise_reforge:textures/entity/cav.png"
  },
  "Models": [
    {
      "Model": "dragonrise_reforge:models/bedrock/vehicle/cav.geo.json",
      "Texture": "dragonrise_reforge:textures/entity/cav.png"
    }
  ],
  "PassengerWeaponStationPos": [-0.693,-0.007,1.333],
  "PassengerWeaponStationBarrelPos": [-0.034,0.31,-0.2]
}
```

## 模型信息（辅助判断武器/座位/炮塔）

```
射击点骨骼: MissilePos1, CannonPos1
座位骨骼: SeatsPos1, SeatsPos2, SeatsPos3, SeatsPos4
地形兼容骨骼: （无） : 
OBB 骨骼数: 17
```

## 填写要求（重点：EngineInfo 性能 + 武器字段）

### 1. EngineInfo（载具性能）

按上面「类型专属填写指南」中该类型的 `EngineInfo` 键清单填写，给出**合理的性能参数**（速度、转向、能量消耗等）。
参考（速度换算见下文）：`Increment`/`Decrement`（加速度）建议 `0.01`；`MaxForwardSpeedRate` 填 1 = 80 km/h、`MaxBackwardSpeedRate` 填 1 = 64 km/h。

### 2. Weapons（武器字段）

- **注意**：`Weapons` 非必填——**如果该载具不应该有武器**（纯运输车/无武装车辆/模型没有 `CannonPos`/`MachineGunPos`/`MissilePos` 射击点骨骼），**省略 `Weapons` 字段**即可，不要硬填。
- **武器键名（key）**：根据模型射击点骨骼命名（见上方模型信息），如 `CannonPos*` → `Cannon`、`MachineGunPos*` → `MachineGun`、`MissilePos*` → `Missile`；多个射击点可用 `Cannon1`/`Cannon2`。
- **每把武器的字段结构**（superbwarfare 标准）：
```json
"<武器名>": {
  "AmmoType": "<弹药ID>",
  "Projectile": "<抛射物ID>",
  "RPM": 射速,
  "Velocity": 初速,
  "Damage": 伤害,
  "ExplosionDamage": 爆炸伤害,
  "ExplosionRadius": 爆炸半径,
  "Magazine": 弹夹容量,
  "EmptyReloadTime": 装填tick,
  "Spread": 散布,
  "DefaultZoom": 默认缩放,
  "ShootPos": { "Positions": [[x,y,z]], "Transform": "Vehicle" },
  "SoundInfo": {},
  "Name": "weapon.dragonrise_reforge.<名字>"
}
```
- 只给出**武器键名与字段结构**即可，弹药/抛射物 ID 可填占位或参考 superbwarfare 原版 ID（如 `superbwarfare:xxx`）。

### 3. 武器完整配置示例（官方实测，填机枪/机炮/坦克炮/导弹/火箭参照）

**坦克炮示例（M1A2 cannon_ap，官方）**
```json
"Cannon": {
  "Icon": "superbwarfare:textures/overlay/vehicle/weapon/icons/ap_shell.png",
  "DefaultFireMode": "Semi", "AvailableFireModes": "Semi",
  "Projectile": "superbwarfare:cannon_shell", "ShellType": "AP",
  "Magazine": 1, "EmptyReloadTime": 100, "Velocity": 20, "ProjectileLife": 50,
  "Damage": 700, "ExplosionDamage": 80, "ExplosionRadius": 4, "Spread": 0.02,
  "DefaultZoom": 3, "Gravity": 0.03, "RecoilTime": 42, "ShootAnimationTime": 20,
  "AmmoType": ["superbwarfare:large_shell_ap", {"Ammo": "superbwarfare:large_shell_he", "Override": {"ShellType": "HE", "Damage": 250, "ExplosionRadius": 10}}],
  "ShootPos": { "Transform": "Barrel", "Positions": [[0,0.13,4.7]], "Directions": ["Barrel"] },
  "Name": "weapon.superbwarfare.cannon_ap"
}
```
**机炮示例（A-10 GAU-8 30mm，官方）**
```json
"Cannon": {
  "DefaultFireMode": "Auto", "AvailableFireModes": "Auto",
  "AmmoType": "superbwarfare:small_shell_ap", "Projectile": "superbwarfare:small_cannon_shell",
  "RPM": 1200, "Velocity": 24, "ProjectileLife": 60, "Damage": 35,
  "ExplosionDamage": 10, "ExplosionRadius": 3, "Spread": 0.5, "DefaultZoom": 2,
  "HeatPerShoot": 1.5, "NaturalCooldown": 0.5,
  "ShootPos": { "Transform": "Vehicle", "Positions": [[0.1,1.36,5.9]], "Directions": [[0,-0.02,1]] },
  "Name": "weapon.superbwarfare.30mm_gau_8_a"
}
```
**机枪示例（M1A2 7.62mm 同轴，官方）**
```json
"MachineGun": {
  "DefaultFireMode": "Auto", "AvailableFireModes": "Auto",
  "AmmoType": "@RifleAmmo", "Projectile": "superbwarfare:projectile",
  "RPM": 600, "Damage": 9.5, "BypassesArmor": 0.3, "Velocity": 30, "Spread": 0.5,
  "DefaultZoom": 3, "HeatPerShoot": 4, "NaturalCooldown": 1,
  "ShootPos": { "Transform": "Barrel", "Positions": [[-0.139,0.245,0.982]], "Directions": ["Barrel"] },
  "Name": "weapon.superbwarfare.7_62mm_coax"
}
```
**导弹示例（A-10 AGM-65 小牛，官方）**
```json
"Missile": {
  "DefaultFireMode": "Semi", "AvailableFireModes": "Semi",
  "Magazine": 4, "EmptyReloadTime": 200, "Projectile": "superbwarfare:agm_65",
  "Damage": 1100, "ExplosionDamage": 180, "ExplosionRadius": 12,
  "AddShooterDeltaMovement": true, "Velocity": 1.05, "ProjectileLife": 3600,
  "SeekWeaponInfo": { "SeekDirection": "ClientCamera", "SeekRange": 1024, "SeekAngle": 20, "SeekTime": 10, "MaxTargetHeight": 32, "MinTargetSize": 0.9 },
  "AmmoType": ["superbwarfare:large_anti_ground_missile"],
  "ShootPos": { "Transform": "Vehicle", "Positions": [[4.97,1.19,0]], "BoundUpWithAmmoAmount": true },
  "Crosshair": "@AirCraftMissile", "Name": "weapon.superbwarfare.agm_65_missile"
}
```
**火箭示例（A-10 70mm 火箭巢，官方）**
```json
"Rocket": {
  "AmmoType": "superbwarfare:small_rocket", "Projectile": "superbwarfare:small_rocket",
  "DefaultFireMode": "Auto", "AvailableFireModes": "Semi",
  "Magazine": 28, "EmptyReloadTime": 160, "RPM": 450, "Spread": 0.4,
  "Damage": 80, "ExplosionDamage": 40, "ExplosionRadius": 5,
  "Gravity": 0.001, "ProjectileLife": 60, "Velocity": 11,
  "ShootPos": { "Transform": "Vehicle", "Positions": [[2.95,1.2,0.1]], "Directions": [[0,-0.03,1]] },
  "Name": "weapon.superbwarfare.70mm_rocket"
}
```

### 4. Projectile 抛射物参考（ID + 特点 + 需特别填写的字段）

根据武器类型从下表选择 `Projectile` 与 `AmmoType`，并按特点补对应字段：

- **普通炮弹**：`superbwarfare:small_cannon_shell`（小口径/机炮）、`superbwarfare:cannon_shell`（中口径坦克炮/机炮）
  - 特点：直线高速弹道，`Velocity` 20~30，`Gravity` 小
  - 特别字段：`Damage`、`RPM`（射速）、`Magazine`、`Spread`、`ShootPos`（炮口位置）
- **霰弹**：`superbwarfare:grapeshot`
  - 特点：散射面杀伤
  - 特别字段：`Spread` 大、`Velocity` 较低
- **迫击炮弹**：`superbwarfare:mortar_shell`
  - 特点：高抛物线，`Gravity` 大、`Velocity` 低、`ProjectileLife` 长
  - 特别字段：`Gravity`、`ProjectileLife`、`ExplosionRadius`（火炮类可配合间接火控）
- **火箭**：`superbwarfare:small_rocket`（及 medium_rocket 等）
  - 特点：直线+重力，多发齐射
  - 特别字段：`Spread` 大、`Magazine`（火箭巢管数）、`RPM` 低、`DefaultZoom`
- **航弹（投放/自由落体）**：`superbwarfare:mk_82`/`mk_84`（美制）、`sc_50`/`sc_250`（德制）、`melon_bomb`、`bor_57`
  - 特点：自由落体投放，`Velocity` 低、`Gravity` 有值
  - **特别字段（必填）**：`AddShooterDeltaMovement: true`、`ShootPos.Directions: ["DeltaMovement"]`、`ShootDirectionForHud: "Bomb"`、`ViewDirection: "Bomb"`、`BoundUpWithAmmoAmount`（按弹数挂载）
- **制导导弹**：`superbwarfare:wire_guide_missile`（线导/TOW）、`agm_65`（小牛空地）、`fim_92_missile`（毒刺防空）、`kh_39`、`ru_3m14`/`ru_9m100`/`ru_9m336`（俄制各型）
  - 特点：需制导/寻的，`Velocity` 中速、可 `SeekMissile`/`@Missile` 武器类型
  - 特别字段：武器 key 用 `Missile`/`SeekMissile`/`DriverAAMissile`（防空）等、`DefaultZoom`、`Magazine`、`Spread` 小
- **其他**：`superbwarfare:projectile`（通用抛射物）、`swarm_drone`（蜂群无人机，`@Missile` 类）

### 5. AmmoType 弹药参考（供选择）

根据武器与 Projectile 类型选择对应 `AmmoType`：

- **机炮/炮弹**：`superbwarfare:small_shell_ap`（穿甲）、`superbwarfare:small_shell_he`（高爆）、`superbwarfare:small_shell_aa`（对空/防空弹链）；大口径用 `superbwarfare:large_shell_ap`/`large_shell_he`
- **榴弹发射器**：`superbwarfare:grenade_40mm`
- **航弹**：`superbwarfare:small_aerial_bomb` / `medium_aerial_bomb` / `large_aerial_bomb`
- **火箭**：`superbwarfare:small_rocket`、`superbwarfare:medium_rocket_ap`（穿甲火箭）
- **导弹**：`superbwarfare:medium_anti_air_missile`（防空导弹）、`superbwarfare:medium_anti_ground_missile`（反地导弹）、`superbwarfare:javelin_missile`（标枪）、`superbwarfare:taser_electrode`（电击弹）
- **迫击炮**：`superbwarfare:mortar_shell`
- **通用占位**：`@HeavyAmmo`/`@RifleAmmo`/`@HandgunAmmo`/`@ShotgunAmmo`/`@SniperAmmo`（机枪/步枪等弹药类型）
- 例：坦克主炮 → `superbwarfare:small_shell_ap` 或 `large_shell_he`；对空机炮 → `superbwarfare:small_shell_aa`；飞机航弹 → `superbwarfare:large_aerial_bomb`；防空导弹 → `superbwarfare:medium_anti_air_missile`

### 6. Velocity 初速规范化参考

按武器/弹种选择标准 `Velocity`（初速）值：

- **机炮/小口径炮弹**（small_cannon_shell）：`20~30`（高初速、直线弹道）
- **坦克炮/中口径炮弹**（cannon_shell）：`25~35`
- **大口径炮弹**（large_shell）：`15~25`
- **迫击炮弹**（mortar_shell）：`8~15`（抛物线）
- **榴弹发射器**（gun_grenade）：`4~10`
- **火箭**（small_rocket）：`15~25`
- **航弹**（aerial_bomb/bor_57/mk_82 等投放弹）：`0.8~1.5`（几乎无初速，靠重力下落）
- **制导导弹**（missile/wire_guide）：`2~6`（发射后制导加速）
- 原则：直线弹道初速高（20+），抛物线/投放初速低（<15），导弹初速中等偏低（2~6）

### 7. ProjectileLife 抛射物存活时间参考（官方包实测）

`ProjectileLife` 是抛射物存活 tick 数（超时自动销毁）。以下为 superbwarfare 官方载具 data 的实测范围，请按武器弹道类型套用：

- **高速直射武器**（机炮/坦克炮直射，Velocity ≥ 25）：`20~40`（如 cannon_shell Vel=35 → Life 20~40）
- **中高速直射**（机炮/坦克炮，Velocity 15~25）：`40~60`（如 small_cannon_shell Vel=18 → Life 40~60、Vel=23~25 → 20）
- **曲射/远程火炮**（榴弹炮/迫击炮/间射，Velocity 低）：`800`（官方 cannon_shell Vel=15~18 用 800，需长存活飞到远距离）
- 原则：直线快弹短命（20~60），曲射慢弹长命（数百~800）；**避免高机动弹超长存活卡服**

### 8. Spread 散布参考（官方包实测）

`Spread` 为武器散布值，越小越准。superbwarfare 官方载具 data 实测范围：

- **坦克/火炮主炮**（cannon_ap 高精度，如 M1A2/T-90A/ZTZ99A/榴弹炮）：`0.02~0.06`
- **机炮/中口径**（20mm/25mm/30mm、cannon_ap）：`0.25~3`
- **重机枪**（50_cal 等）：`5`
- **面杀伤/火箭/霰弹**：`5~6`
- **航弹投放**（aerial_bomb/bor_57）：`10`
- **制导导弹**（missile/线导）：`0~0.02`（高精度寻的）
- 规律：主炮/导弹精度最高（0.02~0.06），面杀伤/航弹散布最大（5~10）

输出：直接给出完整的 `cav.json` 内容（JSON 代码块），未要求填写的字段保持原样或省略。

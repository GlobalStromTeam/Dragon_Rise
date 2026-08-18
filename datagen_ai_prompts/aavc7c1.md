# AI 填充任务：载具 `aavc7c1`

该载具模型含 Build 骨骼，属于可自动生成数据的车辆。请按 superbwarfare 车辆数据格式填充缺失字段，**已有字段保持原样，不得改动**。输出完整 JSON。

## 缺失字段

```
RepairCooldown
RepairAmount
SelfHurtPercent
SelfHurtAmount
Radar
TrackDistanceMultiply
KeepChunkLoaded
MouseSensitivity
PassengerRenderScale
AllowFreeCam
SmokeDecoy
ApplyDefaultDamageModifiers
SendHitParticles
TowForceFactor
DecoyMagazineSize
DecoyReloadTime
SeekInfo
HasUpgradeSlots
LaserColor
LaserScale
HornSound
HasLowHealthWarning
ForwardTowed
Weapons
TurretYawRange
TurretControllerIndex
TurretCustomPitch
PassengerWeaponStationPos
PassengerWeaponStationBarrelPos
PassengerWeaponStationTurnSpeed
PassengerWeaponStationYawRange
PassengerWeaponStationPitchRange
PassengerWeaponStationControllerIndex
UsePassengerCreativeAmmoBox
Gravity
TerrainCompatRotateRate
```

## 现有数据（骨架，保留不变）

```json
{
  "ID": "dragonrise_reforge:aavc7c1",
  "MaxHealth": 300,
  "MaxEnergy": 5000000,
  "OBB": [
    {
      "Size": [1.192,1.075,3.727],
      "Position": [0.0,1.726,-0.648],
      "CustomRotate": [-2.5,0.0,0.0]
    },
    {
      "Size": [1.192,0.419,0.664],
      "Position": [0.0,2.333,3.412],
      "CustomRotate": [22.5,0.0,0.0]
    },
    {
      "Size": [1.192,0.325,0.445],
      "Position": [0.0,2.14,3.74]
    },
    {
      "Size": [1.192,0.325,0.977],
      "Position": [0.0,1.522,3.199],
      "CustomRotate": [-35.0,0.0,0.0]
    },
    {
      "Size": [0.536,0.419,2.414],
      "Position": [1.275,2.077,-1.789],
      "CustomRotate": [0.0,0.0,-45.0]
    },
    {
      "Size": [0.442,0.419,3.164],
      "Position": [1.531,1.585,-1.017],
      "CustomRotate": [0.0,0.0,-90.0]
    },
    {
      "Size": [0.348,0.419,0.945],
      "Position": [1.531,2.472,1.165],
      "CustomRotate": [0.0,0.0,-90.0]
    },
    {
      "Size": [0.786,0.356,0.852],
      "Position": [1.295,2.104,2.778],
      "CustomRotate": [22.5,0.0,-90.0]
    },
    {
      "Size": [0.442,0.356,0.852],
      "Position": [-1.295,1.761,2.793],
      "CustomRotate": [22.5,0.0,90.0]
    },
    {
      "Size": [0.348,0.419,0.945],
      "Position": [-1.531,2.347,1.165],
      "CustomRotate": [0.0,0.0,45.0]
    },
    {
      "Size": [0.442,0.419,3.164],
      "Position": [-1.531,1.585,-1.017],
      "CustomRotate": [0.0,0.0,90.0]
    },
    {
      "Size": [0.536,0.419,2.414],
      "Position": [-1.275,2.077,-1.789],
      "CustomRotate": [0.0,0.0,45.0]
    },
    {
      "Size": [1.192,0.325,0.977],
      "Position": [0.0,1.522,3.199],
      "CustomRotate": [-35.0,0.0,0.0]
    },
    {
      "Size": [0.442,0.356,0.852],
      "Position": [-1.103,2.26,2.692],
      "CustomRotate": [14.546,17.529,46.757]
    },
    {
      "Size": [0.348,0.419,0.945],
      "Position": [-1.162,2.541,1.157],
      "CustomRotate": [0.0,2.5,90.0]
    },
    {
      "Size": [0.348,0.481,0.508],
      "Position": [1.156,2.73,1.415],
      "CustomRotate": [0.0,0.0,-90.0]
    },
    {
      "Size": [0.411,0.356,0.383],
      "Position": [1.156,2.792,1.412],
      "CustomRotate": [0.0,0.0,-90.0]
    },
    {
      "Size": [0.442,0.231,0.289],
      "Position": [0.469,2.828,1.505],
      "CustomRotate": [0.0,-2.5,-90.0]
    },
    {
      "Size": [0.411,0.575,0.258],
      "Position": [0.894,2.798,0.538],
      "CustomRotate": [0.0,-2.5,-90.0]
    },
    {
      "Size": [1.973,1.294,4.227],
      "Position": [0.0,1.383,-0.148],
      "Part": "Collision"
    },
    {
      "Size": [0.541,0.82,1.166],
      "Position": [0.0,1.952,1.578],
      "Part": "MainEngine"
    },
    {
      "Size": [0.288,0.611,3.453],
      "Position": [-1.452,0.617,-0.678],
      "Part": "WheelLeft"
    },
    {
      "Size": [0.288,0.611,3.453],
      "Position": [1.452,0.617,-0.678],
      "Part": "WheelRight"
    },
    {
      "Size": [0.698,0.186,0.956],
      "Position": [-1.0,3.111,0.928],
      "Part": "Turret",
      "Transform": "Turret",
      "Rotation": "Turret"
    }
  ],
  "Seats": [
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.0,1.14,2.938],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel",
        "ZoomPosition": [1.0,2.75,2.938]
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [-1.0,1.703,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel",
        "ZoomPosition": [-1.0,3.313,1.375]
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel",
        "ZoomPosition": [1.125,3.188,1.375]
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    },
    {
      "HidePassenger": true,
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [1.125,1.578,1.375],
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    }
  ],
  "HUDColor": "0xFFC700",
  "EngineType": "Track",
  "EngineInfo": {
    "Buoyancy": 0.052,
    "EnergyCostRate": 96,
    "WheelRotSpeed": 1.25,
    "WheelDifferential": 0.5,
    "TrackRotSpeed": 1.9,
    "TrackDifferential": 0.8,
    "MaxForwardSpeedRate": 0.82,
    "MaxBackwardSpeedRate": 0.35,
    "Increment": 0.025,
    "Decrement": 0.015,
    "SteeringSpeed": 0.1
  },
  "TurretPos": [-1.059,2.849,1.103],
  "TurretTurnSpeed": [6.125,3.295],
  "TurretPitchRange": [-6,60],
  "BarrelPos": [0.103,0.225,0.453],
  "HasDecoy": true,
  "UpStep": 2.25,
  "RotateOffsetHeight": 0.5,
  "Mass": 22.9,
  "DamageModifiers": [
    "All - 13",
    "minecraft:lava + 13",
    "minecraft:lava * 10",
    "@minecraft:tnt * 3",
    "@minecraft:tnt_minecart * 3",
    "All * 0.2",
    "minecraft:arrow * 1.5",
    "minecraft:trident * 1.5",
    "minecraft:mob_attack * 2.5",
    "minecraft:mob_attack_no_aggro * 2",
    "minecraft:mob_projectile * 1.5",
    "minecraft:explosion * 6",
    "minecraft:player_explosion * 6",
    "superbwarfare:custom_explosion * 2",
    "superbwarfare:projectile_explosion * 2",
    "superbwarfare:mine * 0.7",
    "superbwarfare:lunge_mine * 0.9",
    "superbwarfare:projectile_hit * 1.35",
    "superbwarfare:grapeshot_hit * 0.25",
    "superbwarfare:laser * 1.25",
    "@#superbwarfare:aerial_bomb * 3",
    "@#superbwarfare:aa_missile * 0.5",
    "#superbwarfare:projectile * 0.1",
    "#superbwarfare:projectile_absolute * 0.7",
    "#superbwarfare:vehicle_strike * 13",
    "@superbwarfare:mortar_shell * 1.1",
    "@superbwarfare:gun_grenade * 1.5",
    "@superbwarfare:javelin_missile * 0.8"
  ],
  "ThirdPersonCameraPos": [0,1,3],
  "VehicleContainerType": "Medium",
  "DestroyInfo": {
    "ParticleType": "Huge",
    "ExplosionDamage": 160,
    "ExplosionRadius": 8
  },
  "VehicleIcon": "dragonrise_reforge:textures/vehicle_icon/aavc7c1_icon.png",
  "ContainerIcon": "dragonrise_reforge:textures/gui/vehicle/type/us_land.png",
  "Type": "APC",
  "CollisionLevel": {
    "Level": 3,
    "PowerLimits": [
      [
        0,
        0,
        true
      ],
      [
        0,
        0,
        true
      ],
      [
        0.12,
        0.07,
        false
      ]
    ]
  },
  "EngineSound": "superbwarfare:bmp_2_engine",
  "HudType": "@Land",
  "TerrainCompat": [
    [1.344,0.038,1.484],
    [-1.356,0.038,-2.984],
    [-1.356,0.038,1.484],
    [1.344,0.038,-3.109]
  ],
  "InertiaRotateRate": 1.75,
  "Models": [
    {
      "Model": "dragonrise_reforge:models/bedrock/vehicle/aavc7c1.geo.json",
      "Texture": "dragonrise_reforge:textures/entity/aavc7c1.png"
    }
  ]
}
```

## 模型信息（辅助判断武器/座位/炮塔）

```
射击点骨骼: CannonPos1, CannonPos2
座位骨骼: SeatsPos1, SeatsPos2, SeatsPos3
地形兼容骨骼: TerrainCompatPos4, TerrainCompatPos1, TerrainCompatPos2, TerrainCompatPos3
OBB 骨骼数: 25
```

## 填写要求

参考 superbwarfare 车辆 JSON 格式。关键字段说明：

- `Weapons`：武器映射表（如 `Cannon`/`MachineGun`/`Missile`）。每把武器包含：`AmmoType`（弹药 ID）、`Projectile`（抛射物 ID）、`RPM`（射速）、`Velocity`（初速）、`Damage`（伤害）、`ExplosionDamage`、`ExplosionRadius`、`Magazine`（弹夹）、`EmptyReloadTime`（装填 tick）、`Spread`、`DefaultZoom`、`ShootPos`（射击位，可参考上面射击点骨骼）、`SoundInfo` 等。
- `EngineType`：动力类型（如 `Tank`/`Aircraft`/`Helicopter`/`Wheeled`），`EngineInfo` 为对应参数 JSON，`EngineSound` 为音效 ID。
- `MaxHealth`/`MaxEnergy`：按车辆定位填写（坦克 300-800，装甲车 200-500，飞机 200-400）。
- `HudType`：`@Land`（陆地）/ `@Aircraft`（飞行）/ `@AirCraftCommon` 等。
- `VehicleContainerType`：`Empty`/`Mini`/`Small`/`Medium`/`Large`/`Huge`。
- `TurretPos`/`BarrelPos`/`Seats`/`TerrainCompat` 若缺失可参考模型骨骼 pivot 换算（除以 16，Z 取反）。
- `VehicleIcon`/`ContainerIcon` 填贴图路径：`dragonrise_reforge:textures/vehicle_icon/aavc7c1_icon.png` / `dragonrise_reforge:textures/gui/vehicle/type/*.png`。

输出：直接给出完整的 `aavc7c1.json` 内容（JSON 代码块）。

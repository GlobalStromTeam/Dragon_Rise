# AI 填充任务：载具 `challenger_ds`

该载具模型含 Build 骨骼，属于可自动生成数据的车辆。请按 superbwarfare 车辆数据格式填充缺失字段，**已有字段保持原样，不得改动**。输出完整 JSON。

## 载具类型

`CAR`

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
VehicleContainerType
HasUpgradeSlots
LaserColor
LaserScale
HornSound
HasLowHealthWarning
ForwardTowed
TurretYawRange
TurretControllerIndex
TurretCustomPitch
PassengerWeaponStationYawRange
PassengerWeaponStationControllerIndex
UsePassengerCreativeAmmoBox
Gravity
TerrainCompatRotateRate
```

## 类型专属填写指南（CAR）

- `HudType`：`@Land`
- `EngineType`：`Wheel`（轮式）；`EngineSound` 填音效 ID
- `EngineInfo` 应包含：`Buoyancy`, `EnergyCostRate`, `WheelRotSpeed`, `WheelDifferential`, `MaxForwardSpeedRate`, `MaxBackwardSpeedRate`, `Increment`, `Decrement`, `SteeringSpeed`, `EngineSoundVolume`
- 炮塔：`TurretPos`/`BarrelPos`（若模型有）、`TurretTurnSpeed`/`TurretYawRange`/`TurretPitchRange`
- 地形：`TerrainCompat`
- 武器典型：`Cannon`/`MachineGun`/`Missile`（按模型射击点骨骼）
- 无需字段：飞行器的 `PitchSpeed`/`YawSpeed`/`RollSpeed`/`LiftSpeed`/`HasGear`

- 通用：`VehicleIcon` 填 `dragonrise_reforge:textures/vehicle_icon/XXX_icon.png`；`ContainerIcon` 填 `dragonrise_reforge:textures/gui/vehicle/type/*.png`；`VehicleContainerType` 按载具大小（`Empty`/`Mini`/`Small`/`Medium`/`Large`/`Huge`）


## 现有数据（骨架，保留不变）

```json
{
  "ID": "dragonrise_reforge:challenger_ds",
  "MaxHealth": 620,
  "MaxEnergy": 20000000,
  "OBB": [
    {
      "Size": [1.317,0.544,2.82],
      "Position": [0.0,1.195,-0.804]
    },
    {
      "Size": [1.88,0.138,0.57],
      "Position": [0.0,1.551,3.384],
      "CustomRotate": [10.0,0.0,0.0]
    },
    {
      "Size": [1.192,0.419,0.07],
      "Position": [0.0,1.073,3.641],
      "CustomRotate": [35.0,0.0,0.0]
    },
    {
      "Size": [1.942,0.2,3.695],
      "Position": [0.0,1.73,-0.679],
      "CustomRotate": [-0.5,0.0,0.0]
    },
    {
      "Size": [1.192,0.138,0.289],
      "Position": [0.0,1.283,3.567],
      "CustomRotate": [25.0,0.0,0.0]
    },
    {
      "Size": [1.13,0.638,3.695],
      "Position": [0.0,1.293,-0.675],
      "CustomRotate": [-0.5,0.0,0.0]
    },
    {
      "Size": [1.973,0.794,4.039],
      "Position": [0.0,0.883,0.04],
      "Part": "Collision"
    },
    {
      "Size": [1.353,0.445,0.844],
      "Position": [0.0,1.639,-3.244],
      "Part": "MainEngine"
    },
    {
      "Size": [0.381,0.611,4.172],
      "Position": [1.545,0.617,0.041],
      "Part": "WheelLeft"
    },
    {
      "Size": [0.381,0.611,4.141],
      "Position": [-1.545,0.617,0.009],
      "Part": "WheelRight"
    },
    {
      "Size": [1.605,0.561,0.894],
      "Position": [0.234,0.198,-0.87],
      "Part": "Turret",
      "Transform": "Turret",
      "Rotation": "Turret"
    },
    {
      "Size": [0.855,0.467,0.738],
      "Position": [-0.298,0.105,1.099],
      "CustomRotate": [0.0,-32.5,0.0],
      "Part": "Turret",
      "Transform": "Turret",
      "Rotation": "Turret"
    },
    {
      "Size": [0.855,0.467,0.738],
      "Position": [0.734,0.105,1.099],
      "CustomRotate": [0.0,32.5,0.0],
      "Part": "Turret",
      "Transform": "Turret",
      "Rotation": "Turret"
    },
    {
      "Size": [1.605,0.467,0.644],
      "Position": [0.234,0.105,0.317],
      "Part": "Turret",
      "Transform": "Turret",
      "Rotation": "Turret"
    },
    {
      "Size": [1.605,0.53,0.894],
      "Position": [0.234,0.167,-1.933],
      "Part": "Turret",
      "Transform": "Turret",
      "Rotation": "Turret"
    }
  ],
  "Seats": [
    {
      "HidePassenger": true,
      "CameraPos": {
        "Transform": "Turret",
        "ZoomPosition": [-0.563,0.708,0.734],
        "Direction": "Barrel",
        "UseFixedCameraPos": false
      },
      "Transform": "Turret",
      "Position": [-0.563,-0.902,0.734],
      "Weapons": [
        "Cannon",
        "MachineGun"
      ],
      "CanRotateHead": false,
      "MinPitch": -10,
      "MaxPitch": 20,
      "Sensitivity": [0.52,0.75,0.9],
      "HasThermalImaging": true
    },
    {
      "HidePassenger": true,
      "Transform": "WeaponStation",
      "CameraPos": {
        "Transform": "WeaponStation",
        "ZoomPosition": [-0.458,1.496,-1.062],
        "Direction": "WeaponStationBarrel",
        "UseFixedCameraPos": false
      },
      "Position": [-0.458,-0.114,-1.062],
      "Weapons": [
        "PassengerMachineGun"
      ],
      "CanRotateHead": false,
      "MinPitch": -9,
      "MaxPitch": 65,
      "Sensitivity": [0.57,0.75,0.9],
      "HasThermalImaging": true
    },
    {
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [-0.031,0.296,2.5],
      "MinYaw": -95,
      "MaxYaw": 95,
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel",
        "ZoomPosition": [-0.031,1.906,2.5]
      }
    },
    {
      "BanHand": true,
      "Transform": "Vehicle",
      "Position": [-1.625,0.8,-5.5],
      "MinYaw": -95,
      "MaxYaw": 95,
      "CameraPos": {
        "UseFixedCameraPos": false,
        "Transform": "Turret",
        "Direction": "Barrel"
      }
    }
  ],
  "Weapons": {
    "Cannon": {
      "Icon": "superbwarfare:textures/overlay/vehicle/weapon/icons/ap_shell.png",
      "DefaultFireMode": "Semi",
      "AvailableFireModes": "Semi",
      "Projectile": "superbwarfare:cannon_shell",
      "IsArmorPiercingProjectile": true,
      "DefaultZoom": 3,
      "Magazine": 1,
      "EmptyReloadTime": 110,
      "Velocity": 83.95,
      "Gravity": 0.05,
      "Damage": 500,
      "ExplosionDamage": 80,
      "ExplosionRadius": 4,
      "RecoilTime": 42,
      "RecoilForce": 0.1,
      "ShootShake": [0.5,0.5,0.5],
      "Spread": 0.02,
      "ShootAnimationTime": 40,
      "Crosshair": "@VehicleUsTank",
      "CrosshairColor": "0x00ff66",
      "Name": "dragonrise_reforge:M829A1",
      "AmmoType": [
        "superbwarfare:large_shell_ap",
        {
          "Ammo": "superbwarfare:large_shell_he",
          "Override": {
            "Icon": "superbwarfare:textures/overlay/vehicle/weapon/icons/he_shell.png",
            "IsArmorPiercingProjectile": false,
            "IsHighExplosiveProjectile": true,
            "Velocity": 36.6,
            "Damage": 250,
            "ExplosionDamage": 120,
            "ExplosionRadius": 10,
            "Name": "dragonrise_reforge:M908"
          }
        }
      ],
      "SoundInfo": {
        "Fire1P": "dragonrise_reforge:m1a2_fire_1p",
        "Fire3P": "dragonrise_reforge:m1a2_fire_3p",
        "Fire3PFar": "dragonrise_reforge:m1a2_fire_3p_far",
        "Fire3PVeryFar": "dragonrise_reforge:m1a2_fire_3p_far",
        "VehicleReload": "dragonrise_reforge:m1a2_reload",
        "Change": "superbwarfare:into_cannon",
        "CancellableSounds": [
          "dragonrise_reforge:m1a2_reload"
        ]
      },
      "SoundRadius": 32,
      "ShootPos": {
        "Transform": "Barrel",
        "Positions": [
          [0.001,-0.004,7.329],
          [-0.19,-0.01,2.141]
        ],
        "Directions": [
          "Barrel"
        ]
      }
    },
    "MachineGun": {
      "Icon": "superbwarfare:textures/overlay/vehicle/weapon/icons/gun_7_62mm.png",
      "DefaultFireMode": "Auto",
      "AvailableFireModes": "Auto",
      "Spread": 0.25,
      "CrosshairColor": "0x00ff66",
      "Projectile": "superbwarfare:projectile",
      "DefaultZoom": 3,
      "RPM": 700,
      "Damage": 10,
      "BypassesArmor": 0.2,
      "Velocity": 25,
      "Gravity": 0.05,
      "RecoilTime": 16,
      "RecoilForce": 0.05,
      "Magazine": 500,
      "EmptyReloadTime": 160,
      "HeatPerShoot": 3.75,
      "NaturalCooldown": 1,
      "ShootAnimationTime": 1,
      "AmmoType": "@RifleAmmo",
      "ShootPos": {
        "Transform": "Barrel",
        "Positions": [
          [-0.19,-0.01,2.141]
        ],
        "Directions": [
          "Barrel"
        ]
      },
      "Crosshair": "@VehicleCommonGun",
      "Name": "dragonrise_reforge:M240",
      "SoundInfo": {
        "Fire1P": "superbwarfare:m_2_hb_fire_1p",
        "Fire3P": "superbwarfare:m_2_hb_fire_3p",
        "Fire3PFar": "superbwarfare:m_2_hb_far",
        "Fire3PVeryFar": "superbwarfare:m_2_hb_veryfar",
        "Change": "superbwarfare:into_cannon"
      },
      "SoundRadius": 16
    },
    "PassengerMachineGun": {
      "Icon": "superbwarfare:textures/overlay/vehicle/weapon/icons/gun_12_7mm.png",
      "DefaultFireMode": "Auto",
      "AvailableFireModes": "Auto",
      "Spread": 0.25,
      "CrosshairColor": "0x00ff66",
      "Projectile": "superbwarfare:projectile",
      "DefaultZoom": 3,
      "RPM": 575,
      "Damage": 40,
      "BypassesArmor": 0.2,
      "Velocity": 25,
      "Gravity": 0.05,
      "RecoilTime": 16,
      "RecoilForce": 0.05,
      "HeatPerShoot": 4,
      "NaturalCooldown": 1,
      "ShootAnimationTime": 1,
      "AmmoType": "@HeavyAmmo",
      "ShootPos": {
        "Transform": "WeaponStationBarrel",
        "Positions": [
          [-0.028,0.094,0.946]
        ],
        "Directions": [
          "WeaponStationBarrel"
        ]
      },
      "Crosshair": "@VehicleCommonGun",
      "Name": "dragonrise_reforge:M2HB",
      "SoundInfo": {
        "Fire1P": "superbwarfare:m_2_hb_fire_1p",
        "Fire3P": "superbwarfare:m_2_hb_fire_3p",
        "Fire3PFar": "superbwarfare:m_2_hb_far",
        "Fire3PVeryFar": "superbwarfare:m_2_hb_veryfar",
        "Change": "superbwarfare:into_cannon"
      },
      "SoundRadius": 16
    }
  },
  "HUDColor": "0x00ff66",
  "EngineType": "Track",
  "EngineInfo": {
    "Buoyancy": 0,
    "EnergyCostRate": 128,
    "WheelRotSpeed": 1.25,
    "WheelDifferential": 0.75,
    "TrackRotSpeed": 1.9,
    "TrackDifferential": 0.6,
    "MaxForwardSpeedRate": 0.85,
    "MaxBackwardSpeedRate": 0.5,
    "Increment": 0.02,
    "Decrement": 0.01,
    "SteeringSpeed": 0.1,
    "EngineSoundVolume": 0.6
  },
  "TurretPos": [-0.218,2.1,0.621],
  "BarrelPos": [0.218,0.16,1.568],
  "TurretTurnSpeed": [3.765,4.2],
  "TurretPitchRange": [-10,20],
  "PassengerWeaponStationPos": [-0.458,0.826,-1.062],
  "PassengerWeaponStationBarrelPos": [0.262,0.363,0.373],
  "PassengerWeaponStationTurnSpeed": [15.75,8.47],
  "PassengerWeaponStationPitchRange": [-9,65],
  "HasDecoy": true,
  "UpStep": 2.25,
  "RotateOffsetHeight": 3.5,
  "Mass": 64.6,
  "DamageModifiers": [
    "minecraft:arrow 0",
    "minecraft:trident 0",
    "minecraft:mob_attack 0",
    "minecraft:mob_attack_no_aggro 0",
    "minecraft:mob_projectile 0",
    "minecraft:player_attack 0",
    "#superbwarfare:projectile 0",
    "All - 20",
    "minecraft:lava + 20",
    "minecraft:lava * 10",
    "@minecraft:tnt * 4",
    "@minecraft:tnt_minecart * 4",
    "@#superbwarfare:aerial_bomb * 12",
    "All * 0.2",
    "superbwarfare:vehicle_strike * 2.5",
    "minecraft:explosion * 2",
    "superbwarfare:custom_explosion * 0.65",
    "superbwarfare:projectile_explosion * 0.65",
    "superbwarfare:mine * 0.5",
    "superbwarfare:lunge_mine * 0.5",
    "superbwarfare:projectile_hit * 1.3",
    "superbwarfare:grapeshot_hit * 0.1",
    "#superbwarfare:projectile_absolute * 0.15",
    "@#superbwarfare:aa_missile * 0.3",
    "@superbwarfare:small_cannon_shell * 0.25",
    "@superbwarfare:c4 * 4",
    "@#superbwarfare:at_rocket * 1.1",
    "@superbwarfare:gun_grenade * 1.25",
    "@superbwarfare:mortar_shell * 1.25",
    "@superbwarfare:tm_62 * 2.5"
  ],
  "DestroyInfo": {
    "ParticleType": "Giant",
    "ExplosionDamage": 200,
    "ExplosionRadius": 16
  },
  "ThirdPersonCameraPos": [-0.8669625,1.5,5],
  "VehicleIcon": "dragonrise_reforge:textures/vehicle_icon/challenger_ds_icon.png",
  "ContainerIcon": "dragonrise_reforge:textures/gui/vehicle/type/us_land.png",
  "Type": "Tank",
  "CollisionLevel": {
    "Level": 4,
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
        0.1,
        0.05,
        false
      ],
      [
        1,
        0.3,
        false
      ]
    ]
  },
  "EngineSound": "dragonrise_reforge:m1a2_engine",
  "HudType": "@Land",
  "InertiaRotateRate": 1.2,
  "TerrainCompat": [
    [1.656,0.038,2.984],
    [-1.669,0.038,-3.047],
    [-1.669,0.038,3.047],
    [1.656,0.038,-2.984]
  ],
  "Models": [
    {
      "Model": "dragonrise_reforge:models/bedrock/vehicle/m1a2sepv2.geo.json",
      "Texture": "dragonrise_reforge:textures/entity/m1a2sepv2.png"
    }
  ]
}
```

## 模型信息（辅助判断武器/座位/炮塔）

```
射击点骨骼: CannonPos1, CannonPos2
座位骨骼: SeatsPos1, SeatsPos2, SeatsPos3
地形兼容骨骼: TerrainCompatPos4, TerrainCompatPos1, TerrainCompatPos2, TerrainCompatPos3
OBB 骨骼数: 16
```

## 填写要求

参考 superbwarfare 车辆 JSON 格式。关键字段说明：

- `Weapons`：武器映射表（如 `Cannon`/`MachineGun`/`Missile`）。每把武器包含：`AmmoType`（弹药 ID）、`Projectile`（抛射物 ID）、`RPM`（射速）、`Velocity`（初速）、`Damage`（伤害）、`ExplosionDamage`、`ExplosionRadius`、`Magazine`（弹夹）、`EmptyReloadTime`（装填 tick）、`Spread`、`DefaultZoom`、`ShootPos`（射击位，可参考上面射击点骨骼）、`SoundInfo` 等。
- `EngineType`：动力类型（如 `Tank`/`Aircraft`/`Helicopter`/`Wheeled`），`EngineInfo` 为对应参数 JSON，`EngineSound` 为音效 ID。
- `MaxHealth`/`MaxEnergy`：按车辆定位填写（坦克 300-800，装甲车 200-500，飞机 200-400）。
- `HudType`：`@Land`（陆地）/ `@Aircraft`（飞行）/ `@AirCraftCommon` 等。
- `VehicleContainerType`：`Empty`/`Mini`/`Small`/`Medium`/`Large`/`Huge`。
- `TurretPos`/`BarrelPos`/`Seats`/`TerrainCompat` 若缺失可参考模型骨骼 pivot 换算（除以 16，Z 取反）。
- `VehicleIcon`/`ContainerIcon` 填贴图路径：`dragonrise_reforge:textures/vehicle_icon/challenger_ds_icon.png` / `dragonrise_reforge:textures/gui/vehicle/type/*.png`。

输出：直接给出完整的 `challenger_ds.json` 内容（JSON 代码块）。

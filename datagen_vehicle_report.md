# 载具数据完整性报告

生成时间：2026-08-22 08:14:51

前提：仅统计**模型含 Build 骨骼**的载具（防止误报/覆盖已完成载具）；本报告只读，不修改任何数据。

字段清单：superbwarfare `DefaultVehicleData` 共 63 个字段，其中核心必填 2 个（MaxHealth/MaxEnergy/OBB/Seats/Weapons/Engine*/HudType/ContainerType/Icon）。

## 一、缺少核心字段的载具（需要 AI/人工填充）

| 载具 | 缺失字段数 | 缺失字段 |
|---|---|---|
| aavc7c1 | 36 | RepairCooldown, RepairAmount, SelfHurtPercent, SelfHurtAmount, Radar, TrackDistanceMultiply, KeepChunkLoaded, MouseSensitivity, PassengerRenderScale, AllowFreeCam, SmokeDecoy, ApplyDefaultDamageModifiers, SendHitParticles, TowForceFactor, DecoyMagazineSize, DecoyReloadTime, SeekInfo, HasUpgradeSlots, LaserColor, LaserScale, HornSound, HasLowHealthWarning, ForwardTowed, Weapons, TurretYawRange, TurretControllerIndex, TurretCustomPitch, PassengerWeaponStationPos, PassengerWeaponStationBarrelPos, PassengerWeaponStationTurnSpeed, PassengerWeaponStationYawRange, PassengerWeaponStationPitchRange, PassengerWeaponStationControllerIndex, UsePassengerCreativeAmmoBox, Gravity, TerrainCompatRotateRate |

## 二、核心字段齐全的载具

aav7a1, ah1f, brdm2, challenger_ds, markv, ural4320, ural4320_supply, ural4320_zu23, ztd05

## 三、跳过（模型缺失或无 Build 骨骼）

2s25m, 2s38, 625e, 9m133, ac130, ah64, akm, amx56, av8b, bmd4m, bmp3, bmpt72, camel, churchill_vii, cm34, comet, csk181, cv90, cyborg_tank, darkbear, dshk, ec665, f14, f16c, f4u, fa18e, fav_a, flarakpz1, hj8, humvee, humveetow, hyr0, is2, j10, j10c, j11, j15t, j16, j20, j20vtol, j35, j8, jas39e, jf17, ka50, kv1, l1a2, leopard2a4, lvt, m10booker, m113, m1a1hc, m1a2sepv2, m2, m270, m3a3, m3stuart, m4a2, m4a2_105, maus, mk19, motuo, mv3, nh90, npds114, npds514, npds810, pak40, panzer4, pershing, project640, pzbjy, q5, qjz89, refale, refaleaa, sd905, shield, spacebag, strv103, su24m, sx1, syy651, t3476, t3485, t72b3, t80, t80b, t90mh, t90mhigh, test, test_ship, tiger, tjgc, toyota_seiki, tunguska, type100, type3, type97, type97q, uh60, vt4a1, vt4b, wlhgzu23, wlsc, z10a, z10me, z20, z9, zbd04a, zbd05, zbl08, zlt11, zsl10, zsu234, ztq15, ztz59a, ztz96a, ztz99a, ztz99ah, ztz99bh, zu23

package com.redabysslucia.dragonrise_reforge.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

/**
 * 载具数据完整性检查器 + AI 填充提示词生成器。
 * <p>
 * 前提条件：只处理<b>模型含 Build 骨骼</b>的载具（与 GeoOBB/VehicleJava 生成器一致），
 * 避免把已经手工做好的载具（模型无 Build 骨骼或数据完整）误纳入。
 * 本类<b>只读</b>车辆 JSON，只生成报告与提示词两个新文件，绝不覆盖任何已有数据。
 * <p>
 * 输出（写入项目根目录，不进游戏资源包）：
 * <ul>
 *   <li>{@code datagen_vehicle_report.md} —— 全部 Build 骨骼载具的字段缺失总览</li>
 *   <li>{@code datagen_ai_prompts/{载具ID}.md} —— 每个缺失核心字段的载具一份 AI 填充任务提示词</li>
 * </ul>
 */
public class VehicleDataCompletenessChecker implements DataProvider {

    /** DefaultVehicleData 的全部序列化字段（对照 superbwarfare 源码） */
    private static final List<String> ALL_FIELDS = List.of(
            "MaxHealth", "RepairCooldown", "RepairAmount", "SelfHurtPercent", "SelfHurtAmount", "MaxEnergy", "OBB",
            "Seats", "Radar", "UpStep", "TrackDistanceMultiply", "KeepChunkLoaded", "MouseSensitivity",
            "PassengerRenderScale", "AllowFreeCam", "HasDecoy", "SmokeDecoy", "ApplyDefaultDamageModifiers",
            "SendHitParticles", "DamageModifiers", "Mass", "TowForceFactor", "DecoyMagazineSize", "DecoyReloadTime",
            "DestroyInfo", "SeekInfo", "VehicleContainerType", "HasUpgradeSlots", "VehicleIcon", "ContainerIcon",
            "HUDColor", "LaserColor", "LaserScale", "Type", "EngineType", "EngineInfo", "EngineSound", "HornSound",
            "ThirdPersonCameraPos", "HasLowHealthWarning", "ForwardTowed", "RotateOffsetHeight", "Weapons",
            "CollisionLevel", "TurretPos", "TurretTurnSpeed", "TurretYawRange", "TurretPitchRange",
            "TurretControllerIndex", "TurretCustomPitch", "HudType", "BarrelPos", "PassengerWeaponStationPos",
            "PassengerWeaponStationBarrelPos", "PassengerWeaponStationTurnSpeed", "PassengerWeaponStationYawRange",
            "PassengerWeaponStationPitchRange", "PassengerWeaponStationControllerIndex",
            "UsePassengerCreativeAmmoBox", "Gravity", "TerrainCompat", "TerrainCompatRotateRate", "InertiaRotateRate"
    );

    /** 核心字段：只关注 EngineInfo（性能）与 Weapons（武器），其他字段不参与"缺失判定" */
    private static final List<String> CORE_FIELDS = List.of(
            "EngineInfo", "Weapons"
    );

    private final PackOutput output;
    private final ExistingFileHelper existingFileHelper;

    /** 本模组可用的载具类型图标文件名（assets/dragonrise_reforge/textures/gui/vehicle/type/） */
    private List<String> availableTypeIcons = new ArrayList<>();

    /** 各类型的官方参考载具（superbwarfare 官方包实测数据），供 AI 参照填写 */
    private static final Map<String, String> OFFICIAL_REFS = new TreeMap<>();

    /** 各类武器的官方完整配置示例（精简自官方包），供 AI 填 Weapons 时参照 */
    private static final String WEAPON_EXAMPLES = """
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
            """;

    static {
        OFFICIAL_REFS.put("TANK", """
                **官方参考：M1A2 艾布拉姆斯（m_1a_2.json）**
                ```
                "Type": "Tank", "HudType": "@Land", "EngineType": "Track",
                "MaxHealth": 500, "MaxEnergy": 10000000, "VehicleContainerType": "Medium",
                "EngineInfo": { "Buoyancy": 0, "EnergyCostRate": 128, "WheelRotSpeed": 1.25, "WheelDifferential": 0.75,
                  "TrackRotSpeed": 1.9, "TrackDifferential": 0.6, "MaxForwardSpeedRate": 0.78, "MaxBackwardSpeedRate": 0.5,
                  "Increment": 0.02, "Decrement": 0.01, "SteeringSpeed": 0.1, "EngineSoundVolume": 0.6 },
                "Weapons": 键为 Cannon（主炮）、MachineGun（同轴机枪）、PassengerMachineGun
                ```""");

        OFFICIAL_REFS.put("CAR", """
                **官方参考：LAV-25 轮式装甲车（lav_25.json）**
                ```
                "Type": "APC", "HudType": "@Land", "EngineType": "Wheel",
                "MaxHealth": 300, "MaxEnergy": 5000000, "VehicleContainerType": "Medium",
                "EngineInfo": { "Buoyancy": 0.052, "EnergyCostRate": 64, "WheelRotSpeed": 1.75, "WheelDifferential": 0.3,
                  "MaxForwardSpeedRate": 0.95, "MaxBackwardSpeedRate": 0.6, "Increment": 0.027, "Decrement": 0.017,
                  "SteeringSpeed": 0.065 },
                "Weapons": 键为 Cannon（25mm机炮）、MachineGun（同轴机枪）、Missile
                ```""");

        OFFICIAL_REFS.put("PLANE", """
                **官方参考：A-10 攻击机（a_10a.json）**
                ```
                "Type": "Airplane", "HudType": "@Aircraft", "EngineType": "Aircraft",
                "MaxHealth": 350, "MaxEnergy": 10000000, "VehicleContainerType": "Small",
                "EngineInfo": { "EnergyCostRate": 256, "Increment": 1, "Decrement": 1, "PitchSpeed": 1, "YawSpeed": 1,
                  "RollSpeed": 1, "LiftSpeed": 1, "SpeedRate": 1, "GearRotateAngle": 85, "EngineSoundVolume": 0.8 },
                "Weapons": 键为 Cannon（机炮）、Rocket、Bomb、Missile
                ```""");

        OFFICIAL_REFS.put("HELI", """
                **官方参考：米-28 武装直升机（mi_28.json）**
                ```
                "Type": "Helicopter", "HudType": "@Helicopter", "EngineType": "Helicopter",
                "MaxHealth": 350, "MaxEnergy": 10000000, "VehicleContainerType": "Small",
                "EngineInfo": { "EnergyCostRate": 320, "Increment": 0.8, "Decrement": 0.8, "PitchSpeed": 0.75,
                  "YawSpeed": 0.85, "RollSpeed": 0.6, "LiftSpeed": 1, "Speed": 0.97, "EngineSoundVolume": 2 },
                "Weapons": 键为 Cannon（机炮）、Rocket、@Missile、SeekMissile、DriverAAMissile
                ```""");

        OFFICIAL_REFS.put("SHIP", """
                **官方参考：快艇（speedboat.json）**
                ```
                "Type": "Boat", "EngineType": "Ship",
                "MaxHealth": 200, "MaxEnergy": 500000, "VehicleContainerType": "Medium",
                "EngineInfo": { "Buoyancy": 0.09, "EnergyCostRate": 48, "MaxForwardSpeedRate": 1.5,
                  "MaxBackwardSpeedRate": 1.0, "Increment": 0.035, "Decrement": 0.022, "SteeringSpeed": 0.15,
                  "BodyRollRate": 0.8 },
                "Weapons": 键为 MachineGun
                ```""");

        OFFICIAL_REFS.put("AIRSHIP", """
                **官方参考：基洛夫飞艇（kirov.json）**
                ```
                "Type": "AirShip", "HudType": "@Kirov", "EngineType": "AirShip", "Gravity": 0,
                "MaxHealth": 4000, "MaxEnergy": 40000000, "VehicleContainerType": "Huge",
                "EngineInfo": { "Buoyancy": 0.1, "EnergyCostRate": 128, "MaxForwardSpeedRate": 0.7,
                  "MaxBackwardSpeedRate": 0.6, "MaxUpSpeedRate": 0.3, "MaxDownSpeedRate": 0.3, "Increment": 0.4,
                  "Decrement": 0.3, "SteeringSpeed": 0.3, "EngineSoundVolume": 0.4, "FloatHeight": 3.5, "SprintMultiply": 3 },
                "Weapons": 键为 Bomb（航弹）
                ```""");
    }

    public VehicleDataCompletenessChecker(PackOutput output, ExistingFileHelper existingFileHelper) {
        this.output = output;
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        return CompletableFuture.runAsync(() -> {
            try {
                String workingDir = System.getProperty("user.dir").replace("\\run-data", "").replace("/run-data", "");
                Path dataDir = Path.of(workingDir).resolve("src/main/resources/data/dragonrise_reforge/sbw/vehicles");
                Path modelDir = Path.of(workingDir).resolve("src/main/resources/assets/dragonrise_reforge/models/bedrock/vehicle");
                Path reportFile = Path.of(workingDir).resolve("datagen_vehicle_report.md");
                Path promptDir = Path.of(workingDir).resolve("datagen_ai_prompts");

                if (!Files.exists(dataDir)) {
                    return;
                }

                // 扫描本模组可用的载具类型图标
                Path typeIconDir = Path.of(workingDir).resolve("src/main/resources/assets/dragonrise_reforge/textures/gui/vehicle/type");
                availableTypeIcons = new ArrayList<>();
                if (Files.exists(typeIconDir)) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(typeIconDir, "*.png")) {
                        for (Path p : stream) {
                            availableTypeIcons.add(p.getFileName().toString());
                        }
                    }
                    availableTypeIcons.sort(String::compareTo);
                }

                Map<String, List<String>> missingByVehicle = new TreeMap<>();
                List<String> completeVehicles = new ArrayList<>();
                List<String> skippedVehicles = new ArrayList<>();

                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir, "*.json")) {
                    for (Path f : stream) {
                        String id = f.getFileName().toString().replace(".json", "");
                        // 前提：模型存在且含 Build 骨骼
                        Path modelFile = modelDir.resolve(id + ".geo.json");
                        if (!Files.exists(modelFile) || !hasBuildBone(Files.readString(modelFile))) {
                            skippedVehicles.add(id);
                            continue;
                        }
                        JsonObject json;
                        try {
                            json = JsonParser.parseString(Files.readString(f)).getAsJsonObject();
                        } catch (Exception e) {
                            continue;
                        }
                        List<String> missing = new ArrayList<>();
                        for (String field : ALL_FIELDS) {
                            if (!json.has(field)) {
                                missing.add(field);
                            }
                        }
                        List<String> missingCore = new ArrayList<>();
                        for (String field : CORE_FIELDS) {
                            if (!json.has(field)) {
                                missingCore.add(field);
                            }
                        }
                        if (missingCore.isEmpty()) {
                            completeVehicles.add(id);
                        } else {
                            missingByVehicle.put(id, missing);
                        }
                    }
                }

                StringBuilder report = new StringBuilder();
                report.append("# 载具数据完整性报告\n\n");
                report.append("生成时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
                report.append("前提：仅统计**模型含 Build 骨骼**的载具（防止误报/覆盖已完成载具）；本报告只读，不修改任何数据。\n\n");
                report.append("字段清单：superbwarfare `DefaultVehicleData` 共 ").append(ALL_FIELDS.size()).append(" 个字段，其中核心必填 ")
                        .append(CORE_FIELDS.size()).append(" 个（MaxHealth/MaxEnergy/OBB/Seats/Weapons/Engine*/HudType/ContainerType/Icon）。\n\n");

                report.append("## 一、缺少核心字段的载具（需要 AI/人工填充）\n\n");
                if (missingByVehicle.isEmpty()) {
                    report.append("（无）\n\n");
                } else {
                    report.append("| 载具 | 缺失字段数 | 缺失字段 |\n|---|---|---|\n");
                    for (Map.Entry<String, List<String>> e : missingByVehicle.entrySet()) {
                        report.append("| ").append(e.getKey()).append(" | ").append(e.getValue().size())
                                .append(" | ").append(String.join(", ", e.getValue())).append(" |\n");
                    }
                    report.append("\n");
                }

                report.append("## 二、核心字段齐全的载具\n\n");
                if (completeVehicles.isEmpty()) {
                    report.append("（无）\n\n");
                } else {
                    report.append(String.join(", ", completeVehicles)).append("\n\n");
                }

                report.append("## 三、跳过（模型缺失或无 Build 骨骼）\n\n");
                if (skippedVehicles.isEmpty()) {
                    report.append("（无）\n\n");
                } else {
                    report.append(String.join(", ", skippedVehicles)).append("\n");
                }

                Files.createDirectories(reportFile.getParent());
                Files.writeString(reportFile, report.toString());
                System.out.println("VehicleDataCompletenessChecker: report -> " + reportFile.getFileName());

                // 为每个缺核心字段的载具生成 AI 提示词
                if (!missingByVehicle.isEmpty()) {
                    Files.createDirectories(promptDir);
                }
                int prompts = 0;
                for (Map.Entry<String, List<String>> e : missingByVehicle.entrySet()) {
                    String id = e.getKey();
                    Path vehicleJson = dataDir.resolve(id + ".json");
                    Path modelFile = modelDir.resolve(id + ".geo.json");
                    String prompt = buildPrompt(id, Files.readString(vehicleJson),
                            Files.exists(modelFile) ? Files.readString(modelFile) : "", e.getValue());
                    Files.writeString(promptDir.resolve(id + ".md"), prompt);
                    prompts++;
                }
                System.out.println("VehicleDataCompletenessChecker: generated " + prompts + " AI prompt(s)");
            } catch (Exception e) {
                throw new RuntimeException("Failed to check vehicle data completeness", e);
            }
        });
    }

    /** 构建单个载具的 AI 填充提示词。 */
    private String buildPrompt(String id, String vehicleJson, String modelJson, List<String> missingFields) {
        String typeCategory = detectTypeCategory(vehicleJson, modelJson);
        StringBuilder sb = new StringBuilder();
        sb.append("# AI 填充任务：载具 `").append(id).append("`\n\n");
        sb.append("该载具模型含 Build 骨骼，属于可自动生成数据的车辆。**请重点补全 `EngineInfo`（载具性能）与 `Weapons`（武器）字段**，其他字段可忽略。");
        sb.append("**已有字段保持原样，不得改动**。输出完整 JSON。\n\n");
        sb.append("## 载具类型\n\n");
        sb.append("`").append(typeCategory).append("`\n\n");

        sb.append("## 本次重点关注（缺失）\n\n");
        sb.append("```\n");
        for (String f : missingFields) {
            if (f.equals("EngineInfo") || f.equals("Weapons")) {
                sb.append(f).append("\n");
            }
        }
        sb.append("```\n\n");

        sb.append(buildTypeGuide(typeCategory)).append("\n\n");

        String officialRef = OFFICIAL_REFS.get(typeCategory);
        if (officialRef != null) {
            sb.append("## 官方参考载具（同类型，供参照）\n\n").append(officialRef).append("\n\n");
        }

        sb.append("## 现有数据（骨架，保留不变）\n\n```json\n").append(vehicleJson).append("\n```\n\n");

        sb.append("## 模型信息（辅助判断武器/座位/炮塔）\n\n```\n");
        if (modelJson != null && !modelJson.isEmpty()) {
            try {
                JsonObject geo = JsonParser.parseString(modelJson).getAsJsonObject();
                JsonArray geometries = geo.has("minecraft:geometry") ? geo.getAsJsonArray("minecraft:geometry") : null;
                if (geometries != null && geometries.size() > 0) {
                    JsonObject geometry = geometries.get(0).getAsJsonObject();
                    if (geometry.has("bones")) {
                        List<String> weaponBones = new ArrayList<>();
                        List<String> seatBones = new ArrayList<>();
                        List<String> terrainBones = new ArrayList<>();
                        int obbCount = 0;
                        for (JsonElement b : geometry.getAsJsonArray("bones")) {
                            String name = b.getAsJsonObject().has("name") ? b.getAsJsonObject().get("name").getAsString() : "";
                            if (name.matches("^(CannonPos|MachineGunPos|MissilePos).*")) weaponBones.add(name);
                            else if (name.startsWith("SeatsPos")) seatBones.add(name);
                            else if (name.startsWith("TerrainCompatPos")) terrainBones.add(name);
                            if (name.toLowerCase().contains("obb")) obbCount++;
                        }
                        sb.append("射击点骨骼: ").append(weaponBones.isEmpty() ? "（无）" : String.join(", ", weaponBones)).append("\n");
                        sb.append("座位骨骼: ").append(seatBones.isEmpty() ? "（无）" : String.join(", ", seatBones)).append("\n");
                        sb.append("地形兼容骨骼: ").append(terrainBones.isEmpty() ? "（无） : " + String.join(", ", terrainBones) : String.join(", ", terrainBones)).append("\n");
                        sb.append("OBB 骨骼数: ").append(obbCount).append("\n");
                    }
                }
            } catch (Exception ignored) {
                sb.append("（模型解析失败）\n");
            }
        }
        sb.append("```\n\n");

        sb.append("## 填写要求（重点：EngineInfo 性能 + 武器字段）\n\n");
        sb.append("### 1. EngineInfo（载具性能）\n\n");
        sb.append("按上面「类型专属填写指南」中该类型的 `EngineInfo` 键清单填写，给出**合理的性能参数**（速度、转向、能量消耗等）。\n");
        sb.append("参考（速度换算见下文）：`Increment`/`Decrement`（加速度）建议 `0.01`；`MaxForwardSpeedRate` 填 1 = 80 km/h、`MaxBackwardSpeedRate` 填 1 = 64 km/h。\n\n");
        sb.append("### 2. Weapons（武器字段）\n\n");
        sb.append("- **注意**：`Weapons` 非必填——**如果该载具不应该有武器**（纯运输车/无武装车辆/模型没有 `CannonPos`/`MachineGunPos`/`MissilePos` 射击点骨骼），**省略 `Weapons` 字段**即可，不要硬填。\n");
        sb.append("- **武器键名（key）**：根据模型射击点骨骼命名（见上方模型信息），如 `CannonPos*` → `Cannon`、`MachineGunPos*` → `MachineGun`、`MissilePos*` → `Missile`；多个射击点可用 `Cannon1`/`Cannon2`。\n");
        sb.append("- **每把武器的字段结构**（superbwarfare 标准）：\n");
        sb.append("```json\n\"<武器名>\": {\n  \"AmmoType\": \"<弹药ID>\",\n  \"Projectile\": \"<抛射物ID>\",\n  \"RPM\": 射速,\n  \"Velocity\": 初速,\n  \"Damage\": 伤害,\n  \"ExplosionDamage\": 爆炸伤害,\n  \"ExplosionRadius\": 爆炸半径,\n  \"Magazine\": 弹夹容量,\n  \"EmptyReloadTime\": 装填tick,\n  \"Spread\": 散布,\n  \"DefaultZoom\": 默认缩放,\n  \"ShootPos\": { \"Positions\": [[x,y,z]], \"Transform\": \"Vehicle\" },\n  \"SoundInfo\": {},\n  \"Name\": \"weapon.dragonrise_reforge.<名字>\"\n}\n```\n");
        sb.append("- 只给出**武器键名与字段结构**即可，弹药/抛射物 ID 可填占位或参考 superbwarfare 原版 ID（如 `superbwarfare:xxx`）。\n\n");

        sb.append("### 3. 武器完整配置示例（官方实测，填机枪/机炮/坦克炮/导弹/火箭参照）\n\n");
        sb.append(WEAPON_EXAMPLES).append("\n");

        sb.append("### 4. Projectile 抛射物参考（ID + 特点 + 需特别填写的字段）\n\n");
        sb.append("根据武器类型从下表选择 `Projectile` 与 `AmmoType`，并按特点补对应字段：\n\n");
        sb.append("- **普通炮弹**：`superbwarfare:small_cannon_shell`（小口径/机炮）、`superbwarfare:cannon_shell`（中口径坦克炮/机炮）\n");
        sb.append("  - 特点：直线高速弹道，`Velocity` 20~30，`Gravity` 小\n");
        sb.append("  - 特别字段：`Damage`、`RPM`（射速）、`Magazine`、`Spread`、`ShootPos`（炮口位置）\n");
        sb.append("- **霰弹**：`superbwarfare:grapeshot`\n");
        sb.append("  - 特点：散射面杀伤\n");
        sb.append("  - 特别字段：`Spread` 大、`Velocity` 较低\n");
        sb.append("- **迫击炮弹**：`superbwarfare:mortar_shell`\n");
        sb.append("  - 特点：高抛物线，`Gravity` 大、`Velocity` 低、`ProjectileLife` 长\n");
        sb.append("  - 特别字段：`Gravity`、`ProjectileLife`、`ExplosionRadius`（火炮类可配合间接火控）\n");
        sb.append("- **火箭**：`superbwarfare:small_rocket`（及 medium_rocket 等）\n");
        sb.append("  - 特点：直线+重力，多发齐射\n");
        sb.append("  - 特别字段：`Spread` 大、`Magazine`（火箭巢管数）、`RPM` 低、`DefaultZoom`\n");
        sb.append("- **航弹（投放/自由落体）**：`superbwarfare:mk_82`/`mk_84`（美制）、`sc_50`/`sc_250`（德制）、`melon_bomb`、`bor_57`\n");
        sb.append("  - 特点：自由落体投放，`Velocity` 低、`Gravity` 有值\n");
        sb.append("  - **特别字段（必填）**：`AddShooterDeltaMovement: true`、`ShootPos.Directions: [\"DeltaMovement\"]`、`ShootDirectionForHud: \"Bomb\"`、`ViewDirection: \"Bomb\"`、`BoundUpWithAmmoAmount`（按弹数挂载）\n");
        sb.append("- **制导导弹**：`superbwarfare:wire_guide_missile`（线导/TOW）、`agm_65`（小牛空地）、`fim_92_missile`（毒刺防空）、`kh_39`、`ru_3m14`/`ru_9m100`/`ru_9m336`（俄制各型）\n");
        sb.append("  - 特点：需制导/寻的，`Velocity` 中速、可 `SeekMissile`/`@Missile` 武器类型\n");
        sb.append("  - 特别字段：武器 key 用 `Missile`/`SeekMissile`/`DriverAAMissile`（防空）等、`DefaultZoom`、`Magazine`、`Spread` 小\n");
        sb.append("- **其他**：`superbwarfare:projectile`（通用抛射物）、`swarm_drone`（蜂群无人机，`@Missile` 类）\n\n");

        sb.append("### 5. AmmoType 弹药参考（供选择）\n\n");
        sb.append("根据武器与 Projectile 类型选择对应 `AmmoType`：\n\n");
        sb.append("- **机炮/炮弹**：`superbwarfare:small_shell_ap`（穿甲）、`superbwarfare:small_shell_he`（高爆）、`superbwarfare:small_shell_aa`（对空/防空弹链）；大口径用 `superbwarfare:large_shell_ap`/`large_shell_he`\n");
        sb.append("- **榴弹发射器**：`superbwarfare:grenade_40mm`\n");
        sb.append("- **航弹**：`superbwarfare:small_aerial_bomb` / `medium_aerial_bomb` / `large_aerial_bomb`\n");
        sb.append("- **火箭**：`superbwarfare:small_rocket`、`superbwarfare:medium_rocket_ap`（穿甲火箭）\n");
        sb.append("- **导弹**：`superbwarfare:medium_anti_air_missile`（防空导弹）、`superbwarfare:medium_anti_ground_missile`（反地导弹）、`superbwarfare:javelin_missile`（标枪）、`superbwarfare:taser_electrode`（电击弹）\n");
        sb.append("- **迫击炮**：`superbwarfare:mortar_shell`\n");
        sb.append("- **通用占位**：`@HeavyAmmo`/`@RifleAmmo`/`@HandgunAmmo`/`@ShotgunAmmo`/`@SniperAmmo`（机枪/步枪等弹药类型）\n");
        sb.append("- 例：坦克主炮 → `superbwarfare:small_shell_ap` 或 `large_shell_he`；对空机炮 → `superbwarfare:small_shell_aa`；飞机航弹 → `superbwarfare:large_aerial_bomb`；防空导弹 → `superbwarfare:medium_anti_air_missile`\n\n");

        sb.append("### 6. Velocity 初速规范化参考\n\n");
        sb.append("按武器/弹种选择标准 `Velocity`（初速）值：\n\n");
        sb.append("- **机炮/小口径炮弹**（small_cannon_shell）：`20~30`（高初速、直线弹道）\n");
        sb.append("- **坦克炮/中口径炮弹**（cannon_shell）：`25~35`\n");
        sb.append("- **大口径炮弹**（large_shell）：`15~25`\n");
        sb.append("- **迫击炮弹**（mortar_shell）：`8~15`（抛物线）\n");
        sb.append("- **榴弹发射器**（gun_grenade）：`4~10`\n");
        sb.append("- **火箭**（small_rocket）：`15~25`\n");
        sb.append("- **航弹**（aerial_bomb/bor_57/mk_82 等投放弹）：`0.8~1.5`（几乎无初速，靠重力下落）\n");
        sb.append("- **制导导弹**（missile/wire_guide）：`2~6`（发射后制导加速）\n");
        sb.append("- 原则：直线弹道初速高（20+），抛物线/投放初速低（<15），导弹初速中等偏低（2~6）\n\n");

        sb.append("### 7. ProjectileLife 抛射物存活时间参考（官方包实测）\n\n");
        sb.append("`ProjectileLife` 是抛射物存活 tick 数（超时自动销毁）。以下为 superbwarfare 官方载具 data 的实测范围，请按武器弹道类型套用：\n\n");
        sb.append("- **高速直射武器**（机炮/坦克炮直射，Velocity ≥ 25）：`20~40`（如 cannon_shell Vel=35 → Life 20~40）\n");
        sb.append("- **中高速直射**（机炮/坦克炮，Velocity 15~25）：`40~60`（如 small_cannon_shell Vel=18 → Life 40~60、Vel=23~25 → 20）\n");
        sb.append("- **曲射/远程火炮**（榴弹炮/迫击炮/间射，Velocity 低）：`800`（官方 cannon_shell Vel=15~18 用 800，需长存活飞到远距离）\n");
        sb.append("- 原则：直线快弹短命（20~60），曲射慢弹长命（数百~800）；**避免高机动弹超长存活卡服**\n\n");

        sb.append("### 8. Spread 散布参考（官方包实测）\n\n");
        sb.append("`Spread` 为武器散布值，越小越准。superbwarfare 官方载具 data 实测范围：\n\n");
        sb.append("- **坦克/火炮主炮**（cannon_ap 高精度，如 M1A2/T-90A/ZTZ99A/榴弹炮）：`0.02~0.06`\n");
        sb.append("- **机炮/中口径**（20mm/25mm/30mm、cannon_ap）：`0.25~3`\n");
        sb.append("- **重机枪**（50_cal 等）：`5`\n");
        sb.append("- **面杀伤/火箭/霰弹**：`5~6`\n");
        sb.append("- **航弹投放**（aerial_bomb/bor_57）：`10`\n");
        sb.append("- **制导导弹**（missile/线导）：`0~0.02`（高精度寻的）\n");
        sb.append("- 规律：主炮/导弹精度最高（0.02~0.06），面杀伤/航弹散布最大（5~10）\n\n");

        sb.append("输出：直接给出完整的 `" + id + ".json` 内容（JSON 代码块），未要求填写的字段保持原样或省略。\n");
        return sb.toString();
    }

    /** 判断模型几何是否含名为 Build 的骨骼（与 GeoOBBDataProvider 一致）。 */
    private boolean hasBuildBone(String content) {
        try {
            JsonObject geoJson = JsonParser.parseString(content).getAsJsonObject();
            if (!geoJson.has("minecraft:geometry")) return false;
            JsonArray geometries = geoJson.getAsJsonArray("minecraft:geometry");
            for (JsonElement geomElement : geometries) {
                JsonObject geometry = geomElement.getAsJsonObject();
                if (!geometry.has("bones")) continue;
                for (JsonElement boneElement : geometry.getAsJsonArray("bones")) {
                    JsonObject bone = boneElement.getAsJsonObject();
                    if (bone.has("name") && bone.get("name").getAsString().equals("Build")) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * 检测载具类型类别：<b>查找类型标记骨骼</b>，机制与 Build 骨骼一致。
     * <p>遍历模型骨骼，骨骼名（忽略大小写）为 HELI / SHIP / AIRSHIP / TANK / PLANE / CAR 之一即判定为对应类型；
     * 多个标记时按 HELI > SHIP > AIRSHIP > TANK > PLANE > CAR 的顺序取第一个命中；
     * 未找到任何类型标记骨骼时返回 CAR（陆地默认）。
     */
    private String detectTypeCategory(String vehicleJson, String modelJson) {
        if (modelJson == null || modelJson.isEmpty()) {
            return "CAR";
        }
        try {
            JsonObject geoJson = JsonParser.parseString(modelJson).getAsJsonObject();
            if (!geoJson.has("minecraft:geometry")) {
                return "CAR";
            }
            JsonArray geometries = geoJson.getAsJsonArray("minecraft:geometry");
            for (JsonElement geomElement : geometries) {
                JsonObject geometry = geomElement.getAsJsonObject();
                if (!geometry.has("bones")) {
                    continue;
                }
                for (JsonElement boneElement : geometry.getAsJsonArray("bones")) {
                    JsonObject bone = boneElement.getAsJsonObject();
                    if (!bone.has("name")) {
                        continue;
                    }
                    switch (bone.get("name").getAsString().toUpperCase()) {
                        case "HELI": return "HELI";
                        case "SHIP": return "SHIP";
                        case "AIRSHIP": return "AIRSHIP";
                        case "TANK": return "TANK";
                        case "PLANE": return "PLANE";
                        case "CAR": return "CAR";
                        default: break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "CAR";
    }

    /** 按类型类别返回填写指南（HudType/EngineType/EngineInfo 键/特有字段/武器要点/无需字段）。 */
    private String buildTypeGuide(String category) {
        StringBuilder g = new StringBuilder();
        g.append("## 类型专属填写指南（").append(category).append("）\n\n");

        switch (category) {
            case "TANK" -> {
                g.append("- `MaxHealth`：**300-500**（坦克）\n");
                g.append("- `HudType`：`@Land`\n");
                g.append("- `EngineType`：`Track`（履带）；`EngineSound` 填音效 ID\n");
                g.append("- `EngineInfo` 应包含：`Buoyancy`, `EnergyCostRate`, `WheelRotSpeed`, `WheelDifferential`, `TrackRotSpeed`, `TrackDifferential`, `MaxForwardSpeedRate`, `MaxBackwardSpeedRate`, `Increment`, `Decrement`, `SteeringSpeed`, `EngineSoundVolume`\n");
                g.append("- 建议补：`TrackDistanceMultiply`（履带动画速度）、`InertiaRotateRate`（转向惯性）\n");
                g.append("- 炮塔：`TurretPos`/`BarrelPos`（若模型有 turret/barrel 骨骼）、`TurretTurnSpeed`（如 `1.5 1.5`）、`TurretYawRange`（如 `-75 75`）、`TurretPitchRange`（如 `-9 20`）、`TurretControllerIndex`\n");
                g.append("- 地形：`TerrainCompat`（履带接地位置数组）\n");
                g.append("- 武器典型：`Cannon`（主炮）+ `MachineGun`/`Coax`（同轴机枪）\n");
                g.append("- 无需字段：`PitchSpeed`/`YawSpeed`/`RollSpeed`/`LiftSpeed`/`HasGear`（那是飞行器用的）\n");
            }
            case "CAR" -> {
                g.append("- `MaxHealth`：**100-300**（汽车）\n");
                g.append("- `HudType`：`@Land`\n");
                g.append("- `EngineType`：`Wheel`（轮式）；`EngineSound` 填音效 ID\n");
                g.append("- `EngineInfo` 应包含：`Buoyancy`, `EnergyCostRate`, `WheelRotSpeed`, `WheelDifferential`, `MaxForwardSpeedRate`, `MaxBackwardSpeedRate`, `Increment`, `Decrement`, `SteeringSpeed`, `EngineSoundVolume`\n");
                g.append("- 炮塔：`TurretPos`/`BarrelPos`（若模型有）、`TurretTurnSpeed`/`TurretYawRange`/`TurretPitchRange`\n");
                g.append("- 地形：`TerrainCompat`\n");
                g.append("- 武器典型：`Cannon`/`MachineGun`/`Missile`（按模型射击点骨骼）\n");
                g.append("- 无需字段：飞行器的 `PitchSpeed`/`YawSpeed`/`RollSpeed`/`LiftSpeed`/`HasGear`\n");
            }
            case "PLANE" -> {
                g.append("- `MaxHealth`：**100-300**（飞行器）\n");
                g.append("- `HudType`：`@Aircraft`\n");
                g.append("- `EngineType`：`Aircraft`；`EngineSound` 填喷气/螺旋桨音效\n");
                g.append("- `EngineInfo` 应包含：`HasGear`, `EnergyCostRate`, `Increment`, `Decrement`, `PitchSpeed`, `YawSpeed`, `RollSpeed`, `LiftSpeed`, `SpeedRate`, `GearRotateAngle`, `EngineStartSound`, `EngineSoundVolume`\n");
                g.append("- 建议补：`HasDecoy: true`（诱饵/热焰弹）、`ThirdPersonCameraPos`、`RotateOffsetHeight`\n");
                g.append("- 武器典型：`Cannon`（机炮）+ `Missile`（空空/空地）+ `Rocket` + `Bomb`（按模型 `CannonPos`/`MissilePos` 骨骼）\n");
                g.append("- 无需字段：陆地车的 `TerrainCompat` 可不填；`TrackDistanceMultiply` 不需要\n");
            }
            case "HELI" -> {
                g.append("- `MaxHealth`：**100-300**（飞行器）\n");
                g.append("- `HudType`：`@Helicopter`\n");
                g.append("- `EngineType`：`Helicopter`；`EngineSound` 填旋翼音效\n");
                g.append("- `EngineInfo` 应包含：`EnergyCostRate`, `Increment`, `Decrement`, `PitchSpeed`, `YawSpeed`, `RollSpeed`, `LiftSpeed`, `Speed`, `EngineStartSound`, `EngineSoundVolume`\n");
                g.append("- 建议补：`HasDecoy: true`、`ThirdPersonCameraPos`、`RotateOffsetHeight`\n");
                g.append("- 武器典型：`Cannon`（机炮，可旋转）+ `Rocket` + `Missile`（含 `@Missile` 等，按模型骨骼）\n");
                g.append("- 无需字段：`HasGear`/`SpeedRate`（固定翼特有）、`TrackDistanceMultiply`\n");
            }
            case "SHIP" -> {
                g.append("- `HudType`：`@Boat`（如无则 `@Land`）\n");
                g.append("- `EngineType`：`Boat`；`EngineSound` 填引擎音效\n");
                g.append("- `EngineInfo` 应包含浮力相关：`Buoyancy`, `EnergyCostRate`, `Increment`, `Decrement`, `SteeringSpeed`, `EngineSoundVolume` 等\n");
                g.append("- 建议补：`waterMask` 水面遮罩；`TerrainCompat` 可省略\n");
            }
            case "AIRSHIP" -> {
                g.append("- `MaxHealth`：**100-300**（飞行器）\n");
                g.append("- 飞艇（参考 superbwarfare 原版 kirov 基洛夫）：浮空重型平台，整体 `Gravity: 0`\n");
                g.append("- `HudType`：`@Kirov`（原版飞艇专用 HUD）\n");
                g.append("- `EngineType`：`AirShip`；`EngineSound` 填飞艇引擎音效\n");
                g.append("- `EngineInfo` 应包含（kirov 原版键）：`Buoyancy`, `EnergyCostRate`, `MaxForwardSpeedRate`, `MaxBackwardSpeedRate`, `MaxUpSpeedRate`, `MaxDownSpeedRate`, `Increment`, `Decrement`, `SteeringSpeed`, `EngineSoundVolume`, `FloatHeight`, `SprintMultiply`\n");
                g.append("- 特有字段：`FloatHeight`（悬浮高度，kirov 为 3.5）、`Buoyancy`（浮力，kirov 为 0.1）、`MaxUpSpeedRate`/`MaxDownSpeedRate`（垂直爬升/下降速度）\n");
                g.append("- 建议补：`HasDecoy: true`、`ThirdPersonCameraPos`（如 `[0, 11, 44]`）、`RotateOffsetHeight`、`VehicleContainerType: Huge`\n");
                g.append("- 武器典型：`Bomb`（航弹投放——`AddShooterDeltaMovement: true`、`ShootPos.Directions: [\"DeltaMovement\"]`、`ShootDirectionForHud: \"Bomb\"`、`Velocity: 1.0`），也可带 `Missile`\n");
                g.append("- 无需字段：陆地车的 `TerrainCompat`、`TrackDistanceMultiply` 不需要\n");
            }
            default -> {
                g.append("- 陆地载具默认：`HudType: @Land`，`EngineType` 按模型（`Track`/`Wheel`）\n");
                g.append("- `EngineInfo` 参考：`EnergyCostRate`, `MaxForwardSpeedRate`, `MaxBackwardSpeedRate`, `Increment`, `Decrement`, `SteeringSpeed`, `EngineSoundVolume`（履带再加 `TrackRotSpeed`/`TrackDifferential`，轮式加 `WheelRotSpeed`/`WheelDifferential`）\n");
                g.append("- 有炮塔补 `TurretPos`/`BarrelPos`/`TurretTurnSpeed`/`TurretYawRange`/`TurretPitchRange`\n");
            }
        }
        g.append("\n## 速度参考（所有载具统一）\n\n");
        g.append("- `MaxEnergy`：**默认 100000**（所有载具）\n");
        g.append("- `Increment`/`Decrement`：前进/倒车**加速度**，**建议统一填 `0.01`**\n");
        g.append("- `MaxForwardSpeedRate`（最大前进速度）：填 `1` 时最快 80 km/h → 每 `0.01` = 0.8 km/h。按目标速度换算：目标km/h ÷ 0.8 × 0.01（例：60 km/h → 60 ÷ 0.8 × 0.01 = 0.75；40 km/h → 0.5）\n");
        g.append("- `MaxBackwardSpeedRate`（最大倒车速度）：填 `1` 时最快 64 km/h → 每 `0.01` = 0.64 km/h。按目标速度换算：目标km/h ÷ 0.64 × 0.01（例：32 km/h → 32 ÷ 0.64 × 0.01 = 0.5；16 km/h → 0.25）\n");
        g.append("\n## HudType 可选值（供选择）\n\n");
        g.append("- `@Land`：陆地载具（坦克/装甲车/汽车）\n");
        g.append("- `@Aircraft`：固定翼飞行器\n");
        g.append("- `@OldAircraft`：旧式/螺旋桨固定翼（如二战螺旋桨机）\n");
        g.append("- `@Helicopter`：直升机\n");
        g.append("- `@Artillery`：自行火炮/火炮类\n");
        g.append("- `@Kirov`：飞艇（原版基洛夫专用 HUD）\n\n");

        g.append("## Type 可选值（供选择）\n\n");
        g.append("- `Tank`：坦克\n");
        g.append("- `APC`：装甲运兵车/步兵战车\n");
        g.append("- `Car`：汽车/轮式车辆\n");
        g.append("- `AA`：防空\n");
        g.append("- `Artillery`：自行火炮\n");
        g.append("- `Defense`：固定防御设施\n");
        g.append("- `Airplane`：固定翼飞行器\n");
        g.append("- `Helicopter`：直升机\n");
        g.append("- `AirShip`：飞艇\n");
        g.append("- `Boat`：舰船/水上载具\n");
        g.append("- `Drone`：无人机\n");
        g.append("- `Special`：特殊载具\n\n");

        g.append("## EngineType 可选值（供选择）\n\n");
        g.append("- `Track`：履带（坦克/履带装甲车）\n");
        g.append("- `Wheel`：轮式（汽车/轮式装甲车）\n");
        g.append("- `Aircraft`：固定翼喷气/螺旋桨引擎\n");
        g.append("- `Helicopter`：直升机旋翼引擎\n");
        g.append("- `AirShip`：飞艇引擎\n");
        g.append("- `Ship`：舰船引擎\n");
        g.append("- `Empty`：无引擎（静态/防御设施）\n");
        g.append("- `Fixed`：固定引擎（其他固定类型）\n");
        g.append("- `WheelChair`/`Tom6`：特殊（一般不用）\n\n");

        g.append("\n## ContainerIcon 载具类型图标选择\n\n");
        g.append("根据载具的国家/阵营与类别，从以下图标中选择 `ContainerIcon`（值为完整资源路径 `dragonrise_reforge:textures/gui/vehicle/type/<文件名>`）：\n\n");
        g.append("**国家/阵营前缀**：`cn`=中国、`us`=美国、`ru`=俄罗斯、`uk`=英国、`jp`=日本、`fr`=法国、`gm`=德国、`se`=瑞典、`ussr`=苏联、`dr`=**二战德国**、`blue`=蓝方阵营、`red`=红方阵营\n");
        g.append("**类别后缀**：`land`=陆地载具、`aircraft`=飞行器、`water`=水上载具\n\n");
        g.append("**本模组可用图标**：\n");
        if (availableTypeIcons.isEmpty()) {
            g.append("（未扫描到图标文件）\n");
        } else {
            g.append("```\n");
            for (String icon : availableTypeIcons) {
                g.append(icon).append("\n");
            }
            g.append("```\n");
        }
        g.append("**superbwarfare 通用图标**（`superbwarfare:textures/gui/vehicle/type/<名>.png`）：`land`（陆地）、`aircraft`（固定翼）、`helicopter`（直升机）、`water`（水上）、`airship`（飞艇）、`defense`（防御）、`civilian`（民用）、`otto`\n");
        g.append("- 例：中国陆地载具 → `dragonrise_reforge:textures/gui/vehicle/type/cn_land.png`；二战德国陆地 → `dragonrise_reforge:textures/gui/vehicle/type/dr_land.png`；美国飞行器 → `dragonrise_reforge:textures/gui/vehicle/type/us_aircraft.png`\n\n");
        g.append("\n- 通用：`VehicleIcon` 填 `dragonrise_reforge:textures/vehicle_icon/").append("XXX").append("_icon.png`；`VehicleContainerType` 按载具大小（`Empty`/`Mini`/`Small`/`Medium`/`Large`/`Huge`）\n");
        return g.toString();
    }

    @Override
    public String getName() {
        return "DragonRise Vehicle Data Completeness Checker";
    }
}

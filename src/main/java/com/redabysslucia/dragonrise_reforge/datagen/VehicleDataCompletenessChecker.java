package com.redabysslucia.dragonrise_reforge.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;

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

    /** 核心字段：缺失即视为"未完成"，需要 AI/人工填充 */
    private static final List<String> CORE_FIELDS = List.of(
            "MaxHealth", "MaxEnergy", "OBB", "Seats", "Weapons", "EngineType", "EngineInfo", "EngineSound",
            "HudType", "VehicleContainerType", "VehicleIcon"
    );

    private final PackOutput output;
    private final ExistingFileHelper existingFileHelper;

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
        sb.append("该载具模型含 Build 骨骼，属于可自动生成数据的车辆。请按 superbwarfare 车辆数据格式填充缺失字段，");
        sb.append("**已有字段保持原样，不得改动**。输出完整 JSON。\n\n");
        sb.append("## 载具类型\n\n");
        sb.append("`").append(typeCategory).append("`\n\n");

        sb.append("## 缺失字段\n\n");
        sb.append("```\n").append(String.join("\n", missingFields)).append("\n```\n\n");

        sb.append(buildTypeGuide(typeCategory)).append("\n\n");

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

        sb.append("## 填写要求\n\n");
        sb.append("参考 superbwarfare 车辆 JSON 格式。关键字段说明：\n\n");
        sb.append("- `Weapons`：武器映射表（如 `Cannon`/`MachineGun`/`Missile`）。每把武器包含：`AmmoType`（弹药 ID）、`Projectile`（抛射物 ID）、`RPM`（射速）、`Velocity`（初速）、`Damage`（伤害）、`ExplosionDamage`、`ExplosionRadius`、`Magazine`（弹夹）、`EmptyReloadTime`（装填 tick）、`Spread`、`DefaultZoom`、`ShootPos`（射击位，可参考上面射击点骨骼）、`SoundInfo` 等。\n");
        sb.append("- `EngineType`：动力类型（如 `Tank`/`Aircraft`/`Helicopter`/`Wheeled`），`EngineInfo` 为对应参数 JSON，`EngineSound` 为音效 ID。\n");
        sb.append("- `MaxHealth`/`MaxEnergy`：按车辆定位填写（坦克 300-800，装甲车 200-500，飞机 200-400）。\n");
        sb.append("- `HudType`：`@Land`（陆地）/ `@Aircraft`（飞行）/ `@AirCraftCommon` 等。\n");
        sb.append("- `VehicleContainerType`：`Empty`/`Mini`/`Small`/`Medium`/`Large`/`Huge`。\n");
        sb.append("- `TurretPos`/`BarrelPos`/`Seats`/`TerrainCompat` 若缺失可参考模型骨骼 pivot 换算（除以 16，Z 取反）。\n");
        sb.append("- `VehicleIcon`/`ContainerIcon` 填贴图路径：`dragonrise_reforge:textures/vehicle_icon/" + id + "_icon.png` / `dragonrise_reforge:textures/gui/vehicle/type/*.png`。\n\n");
        sb.append("输出：直接给出完整的 `" + id + ".json` 内容（JSON 代码块）。\n");
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
                g.append("- `HudType`：`@Land`\n");
                g.append("- `EngineType`：`Wheel`（轮式）；`EngineSound` 填音效 ID\n");
                g.append("- `EngineInfo` 应包含：`Buoyancy`, `EnergyCostRate`, `WheelRotSpeed`, `WheelDifferential`, `MaxForwardSpeedRate`, `MaxBackwardSpeedRate`, `Increment`, `Decrement`, `SteeringSpeed`, `EngineSoundVolume`\n");
                g.append("- 炮塔：`TurretPos`/`BarrelPos`（若模型有）、`TurretTurnSpeed`/`TurretYawRange`/`TurretPitchRange`\n");
                g.append("- 地形：`TerrainCompat`\n");
                g.append("- 武器典型：`Cannon`/`MachineGun`/`Missile`（按模型射击点骨骼）\n");
                g.append("- 无需字段：飞行器的 `PitchSpeed`/`YawSpeed`/`RollSpeed`/`LiftSpeed`/`HasGear`\n");
            }
            case "PLANE" -> {
                g.append("- `HudType`：`@Aircraft`\n");
                g.append("- `EngineType`：`Aircraft`；`EngineSound` 填喷气/螺旋桨音效\n");
                g.append("- `EngineInfo` 应包含：`HasGear`, `EnergyCostRate`, `Increment`, `Decrement`, `PitchSpeed`, `YawSpeed`, `RollSpeed`, `LiftSpeed`, `SpeedRate`, `GearRotateAngle`, `EngineStartSound`, `EngineSoundVolume`\n");
                g.append("- 建议补：`HasDecoy: true`（诱饵/热焰弹）、`ThirdPersonCameraPos`、`RotateOffsetHeight`\n");
                g.append("- 武器典型：`Cannon`（机炮）+ `Missile`（空空/空地）+ `Rocket` + `Bomb`（按模型 `CannonPos`/`MissilePos` 骨骼）\n");
                g.append("- 无需字段：陆地车的 `TerrainCompat` 可不填；`TrackDistanceMultiply` 不需要\n");
            }
            case "HELI" -> {
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
                g.append("- 飞艇：垂直起降飞行器，参考直升机与固定翼的混合\n");
                g.append("- `HudType`：`@Aircraft` 或 `@Helicopter`\n");
                g.append("- `EngineType`：`Helicopter` 或 `Aircraft`（按模型推进方式）\n");
                g.append("- `EngineInfo` 参考直升机：`EnergyCostRate`, `Increment`, `Decrement`, `PitchSpeed`, `YawSpeed`, `RollSpeed`, `LiftSpeed`, `Speed`, `EngineStartSound`, `EngineSoundVolume`\n");
                g.append("- 建议补：`HasDecoy: true`、`ThirdPersonCameraPos`、`RotateOffsetHeight`\n");
                g.append("- 无需字段：`TrackDistanceMultiply`、`TerrainCompat` 可不填\n");
            }
            default -> {
                g.append("- 陆地载具默认：`HudType: @Land`，`EngineType` 按模型（`Track`/`Wheel`）\n");
                g.append("- `EngineInfo` 参考：`EnergyCostRate`, `MaxForwardSpeedRate`, `MaxBackwardSpeedRate`, `Increment`, `Decrement`, `SteeringSpeed`, `EngineSoundVolume`（履带再加 `TrackRotSpeed`/`TrackDifferential`，轮式加 `WheelRotSpeed`/`WheelDifferential`）\n");
                g.append("- 有炮塔补 `TurretPos`/`BarrelPos`/`TurretTurnSpeed`/`TurretYawRange`/`TurretPitchRange`\n");
            }
        }
        g.append("\n- 通用：`VehicleIcon` 填 `dragonrise_reforge:textures/vehicle_icon/").append("XXX").append("_icon.png`；`ContainerIcon` 填 `dragonrise_reforge:textures/gui/vehicle/type/*.png`；`VehicleContainerType` 按载具大小（`Empty`/`Mini`/`Small`/`Medium`/`Large`/`Huge`）\n");
        return g.toString();
    }

    @Override
    public String getName() {
        return "DragonRise Vehicle Data Completeness Checker";
    }
}

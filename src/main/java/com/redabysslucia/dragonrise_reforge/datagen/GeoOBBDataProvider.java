package com.redabysslucia.dragonrise_reforge.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeoOBBDataProvider implements DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final PackOutput output;
    private final ExistingFileHelper existingFileHelper;

    public GeoOBBDataProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        this.output = output;
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        return CompletableFuture.runAsync(() -> {
            try {
                String workingDir = System.getProperty("user.dir").replace("\\run-data", "").replace("/run-data", "");
                Path geoPath = Path.of(workingDir).resolve("src/main/resources/assets/dragonrise_reforge/geo");
                Path bedrockPath = Path.of(workingDir).resolve("src/main/resources/assets/dragonrise_reforge/models/bedrock/vehicle");
                Path dragonriseOutputPath = Path.of(workingDir).resolve("src/main/resources/data/dragonrise_reforge/sbw/vehicles");
                Path superbwarfareOutputPath = Path.of(workingDir).resolve("src/main/resources/assets/superbwarfare/sbw/vehicles");
                Path dragonriseAssetOutputPath = Path.of(workingDir).resolve("src/main/resources/assets/dragonrise_reforge/sbw/vehicles");

                if (!Files.exists(geoPath) && !Files.exists(bedrockPath)) {
                    return;
                }

                // 同时支持 geo/ 与 models/bedrock/vehicle/ 两个输入目录，按文件名去重（SBM 模型优先放 models/bedrock/vehicle/）
                Set<Path> inputFiles = new LinkedHashSet<>();
                if (Files.exists(geoPath)) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(geoPath, "*.geo.json")) {
                        for (Path p : stream) {
                            inputFiles.add(p);
                        }
                    }
                }
                if (Files.exists(bedrockPath)) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(bedrockPath, "*.geo.json")) {
                        for (Path p : stream) {
                            boolean duplicate = inputFiles.stream()
                                    .anyMatch(e -> e.getFileName().equals(p.getFileName()));
                            if (!duplicate) {
                                inputFiles.add(p);
                            }
                        }
                    }
                }

                for (Path geoFile : inputFiles) {
                    processGeoFile(geoFile, dragonriseOutputPath, superbwarfareOutputPath, dragonriseAssetOutputPath);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to process geo files", e);
            }
        });
    }

    private void processGeoFile(Path geoFile, Path dragonriseOutputPath, Path superbwarfareOutputPath, Path dragonriseAssetOutputPath) {
        String content;
        try {
            content = Files.readString(geoFile);
        } catch (Exception e) {
            System.err.println("GeoOBBDataProvider: cannot read " + geoFile.getFileName() + ", skipped: " + e.getMessage());
            return;
        }

        JsonObject geoJson;
        try {
            geoJson = JsonParser.parseString(content).getAsJsonObject();
        } catch (Exception e) {
            // 单个模型损坏/非 JSON 时跳过，避免整个 runData 崩溃
            System.err.println("GeoOBBDataProvider: malformed model " + geoFile.getFileName() + " (" + e.getMessage() + "), skipped");
            return;
        }

        try {
            // 入口闸门：只有 Obb 组下存在 Build 骨骼的模型才会生成（飞机/坦克统一规则）——
            // 即 Blockbench 中把 Build 骨骼挂在 Obb 组下。无 Build 或 Build 不在 Obb 组下的一律跳过。
            if (!hasBuildUnderObb(geoJson)) {
                return;
            }

            boolean hasSeatsPos1 = checkSeatsPos1(geoJson);
            boolean isAircraft = isAircraft(geoJson);
            double turretCustomPitch = extractTurretCustomPitch(geoJson);

            JsonArray turretPos = hasSeatsPos1 ? extractTurretPos(geoJson) : null;
            double turretPivotY = turretPos != null ? turretPos.get(1).getAsDouble() * 16.0 : 0.0;
            JsonArray barrelPos = hasSeatsPos1 ? extractBarrelPos(geoJson, turretPos, turretCustomPitch) : null;
            double barrelPosY = barrelPos != null ? barrelPos.get(1).getAsDouble() : 0.0;
            double barrelPivotY = barrelPosY * 16.0;
            JsonArray obbList = extractOBBList(geoJson, turretPos, turretCustomPitch);
            Map<String, JsonArray> weaponPositions = extractWeaponPositions(geoJson, barrelPivotY, turretPivotY, turretCustomPitch);
            Map<Integer, JsonArray> seatsPositions = extractSeatsPositions(geoJson);
            Map<Integer, Double> seatsOrientations = extractSeatsOrientations(geoJson);
            Map<Integer, JsonArray> seatsCameraPositions = extractSeatsCameraPositions(geoJson);
            List<JsonArray> terrainCompatPositions = extractTerrainCompatPositions(geoJson);

            String baseName = geoFile.getFileName().toString().replace(".geo.json", "");
            Path vehicleFile = dragonriseOutputPath.resolve(baseName + ".json");

            // 更新VehicleIcon（无论是否有其他数据更新，只要vehicle文件存在就替换）
            if (Files.exists(vehicleFile)) {
                try {
                    String vehicleContent = Files.readString(vehicleFile);
                    JsonObject existingVehicleJson = JsonParser.parseString(vehicleContent).getAsJsonObject();
                    if (existingVehicleJson.has("VehicleIcon")) {
                        String iconPath = "dragonrise_reforge:textures/vehicle_icon/" + baseName + "_icon.png";
                        existingVehicleJson.addProperty("VehicleIcon", iconPath);
                        Files.writeString(vehicleFile, compactJson(GSON.toJson(existingVehicleJson)));
                    }
                } catch (Exception ignored) {
                }
            }

            // 如果没有需要提取的数据，后面只做superbwarfare配置生成
            boolean hasExtractableData = !(obbList.isEmpty() && turretPos == null && barrelPos == null && weaponPositions.isEmpty() && seatsPositions.isEmpty() && seatsCameraPositions.isEmpty() && terrainCompatPositions.isEmpty()) || isAircraft;

             if (hasExtractableData) {
             JsonObject vehicleJson;
             if (Files.exists(vehicleFile)) {
                 String vehicleContent = Files.readString(vehicleFile);
                 vehicleJson = JsonParser.parseString(vehicleContent).getAsJsonObject();
             } else {
                 vehicleJson = new JsonObject();
                 vehicleJson.addProperty("ID", "dragonrise_reforge:" + baseName);
             }

             if (!obbList.isEmpty()) {
                vehicleJson.add("OBB", obbList);
            }

            if (hasSeatsPos1 && turretPos != null) {
                vehicleJson.add("TurretPos", turretPos);
            }

            if (hasSeatsPos1 && barrelPos != null) {
                vehicleJson.add("BarrelPos", barrelPos);
            }

            if (!weaponPositions.isEmpty() && vehicleJson.has("Weapons")) {
                JsonObject weapons = vehicleJson.getAsJsonObject("Weapons");
                for (Map.Entry<String, JsonArray> entry : weaponPositions.entrySet()) {
                    String weaponName = entry.getKey();
                    JsonArray positions = entry.getValue();

                    if (weapons.has(weaponName)) {
                        JsonObject weapon = weapons.getAsJsonObject(weaponName);

                        JsonObject shootPos;
                        if (weapon.has("ShootPos")) {
                            shootPos = weapon.getAsJsonObject("ShootPos");
                        } else {
                            shootPos = new JsonObject();
                            weapon.add("ShootPos", shootPos);
                        }

                        String transform = shootPos.has("Transform") ? shootPos.get("Transform").getAsString() : "";

                        JsonArray adjustedPositions = new JsonArray();
                        for (JsonElement posElement : positions) {
                            JsonArray pos = posElement.getAsJsonArray();
                            JsonArray adjustedPos = new JsonArray();
                            adjustedPos.add(pos.get(0));
                            adjustedPos.add(pos.get(1));
                            adjustedPos.add(pos.get(2));
                            adjustedPositions.add(adjustedPos);
                        }

                        // 坦克武器按炮塔/炮管相对化；飞机武器一律读取骨骼直接坐标（绝对坐标），不做相对化
                        if (hasSeatsPos1 && !isAircraft) {
                            if ("Barrel".equals(transform) && barrelPos != null && turretPos != null) {
                                double barrelY = barrelPos.get(1).getAsDouble();
                                double turretY = turretPos.get(1).getAsDouble();
                                for (int i = 0; i < adjustedPositions.size(); i++) {
                                    JsonArray pos = adjustedPositions.get(i).getAsJsonArray();
                                    untiltAroundTurret(pos, turretPos, turretCustomPitch);
                                    double y = pos.get(1).getAsDouble();
                                    y = y - barrelY - turretY;
                                    pos.set(1, new com.google.gson.JsonPrimitive(round(y, 3)));
                                }
                            } else if ("Turret".equals(transform) && turretPos != null) {
                                double turretY = turretPos.get(1).getAsDouble();
                                for (int i = 0; i < adjustedPositions.size(); i++) {
                                    JsonArray pos = adjustedPositions.get(i).getAsJsonArray();
                                    untiltAroundTurret(pos, turretPos, turretCustomPitch);
                                    double y = pos.get(1).getAsDouble();
                                    y = y - turretY;
                                    pos.set(1, new com.google.gson.JsonPrimitive(round(y, 3)));
                                }
                            }
                        }

                        shootPos.add("Positions", adjustedPositions);
                    }
                }
            }

            // 飞机专项：挂架发射位置（dummy_<座位>_<武器>_<挂点> 骨骼，与挂架假体渲染共用）
            if (isAircraft && vehicleJson.has("Weapons")) {
                Map<String, JsonArray> dummyWeaponPositions = extractDummyWeaponPositions(geoJson, vehicleJson);
                JsonObject weapons = vehicleJson.getAsJsonObject("Weapons");
                for (Map.Entry<String, JsonArray> entry : dummyWeaponPositions.entrySet()) {
                    String weaponName = entry.getKey();
                    if (!weapons.has(weaponName)) {
                        continue;
                    }
                    JsonObject weapon = weapons.getAsJsonObject(weaponName);
                    JsonObject shootPos;
                    if (weapon.has("ShootPos")) {
                        shootPos = weapon.getAsJsonObject("ShootPos");
                    } else {
                        shootPos = new JsonObject();
                        weapon.add("ShootPos", shootPos);
                    }
                    // 飞机挂架位置为载具绝对坐标，与坦克 CannonPos 相同语义，直接写入
                    shootPos.add("Positions", entry.getValue());
                }

                // 飞机专项：瞄准吊舱视角位置（NacellePos<武器名> / NacellePos 骨骼）
                Map<String, JsonArray> nacellePositions = extractNacellePositions(geoJson);
                for (Map.Entry<String, JsonArray> entry : nacellePositions.entrySet()) {
                    List<String> targets = "__ALL__".equals(entry.getKey())
                            ? new ArrayList<>(weapons.keySet())
                            : java.util.Collections.singletonList(entry.getKey());
                    for (String target : targets) {
                        if (!weapons.has(target)) {
                            continue;
                        }
                        JsonObject weapon = weapons.getAsJsonObject(target);
                        JsonObject shootPos;
                        if (weapon.has("ShootPos")) {
                            shootPos = weapon.getAsJsonObject("ShootPos");
                        } else {
                            shootPos = new JsonObject();
                            weapon.add("ShootPos", shootPos);
                        }
                        // 吊舱视角单独定义：只为已启用 UseNacelleCamera 的武器写 ViewPosition
                        // （吊舱骨骼是权威来源：位置始终按骨骼当前坐标覆盖，骨骼移动后重新生成即更新）。
                        // 不再自动给全部武器加 UseNacelleCamera —— 默认右键放大视角为座位头部位置，
                        // 吊舱仅用于明确启用的武器（如 agm65 弹种）。
                        if (weapon.has("UseNacelleCamera") && weapon.get("UseNacelleCamera").getAsBoolean()
                                && entry.getValue().size() > 0) {
                            // 单吊舱直接写；多吊舱取第一个
                            shootPos.add("ViewPosition", entry.getValue().get(0).getAsJsonArray());
                        }
                    }
                }
            }

            if (hasSeatsPos1 && !seatsPositions.isEmpty() && vehicleJson.has("Seats")) {
                var seatsElement = vehicleJson.get("Seats");
                if (seatsElement.isJsonArray()) {
                    JsonArray seats = seatsElement.getAsJsonArray();
                    for (int i = 0; i < seats.size(); i++) {
                        JsonObject seat = seats.get(i).getAsJsonObject();
                        int index = i + 1;

                        if (seatsPositions.containsKey(index)) {
                            JsonArray pos = seatsPositions.get(index);
                            String seatTransform = seat.has("Transform") ? seat.get("Transform").getAsString() : "";
                            if (("Turret".equals(seatTransform) || "WeaponStation".equals(seatTransform)) && turretPos != null) {
                                JsonArray adjustedPos = new JsonArray();
                                adjustedPos.add(round(pos.get(0).getAsDouble() - turretPos.get(0).getAsDouble(), 3));
                                adjustedPos.add(round(pos.get(1).getAsDouble() - turretPos.get(1).getAsDouble(), 3));
                                adjustedPos.add(round(pos.get(2).getAsDouble() - turretPos.get(2).getAsDouble(), 3));
                                seat.add("Position", adjustedPos);
                            } else {
                                seat.add("Position", pos);
                            }
                            if (seatsOrientations.containsKey(index)) {
                                seat.addProperty("Orientation", round(seatsOrientations.get(index), 1));
                            }
                        }

                        if (seatsCameraPositions.containsKey(index)) {
                            applyCameraPos(seat, index, seatsPositions, seatsCameraPositions, turretPos, isAircraft);
                        }
                    }
                } else if (seatsElement.isJsonObject() && seatsPositions.containsKey(1)) {
                    JsonObject seat = seatsElement.getAsJsonObject();
                    JsonArray pos = seatsPositions.get(1);
                    String seatTransform = seat.has("Transform") ? seat.get("Transform").getAsString() : "";
                    if (("Turret".equals(seatTransform) || "WeaponStation".equals(seatTransform)) && turretPos != null) {
                        JsonArray adjustedPos = new JsonArray();
                        adjustedPos.add(round(pos.get(0).getAsDouble() - turretPos.get(0).getAsDouble(), 3));
                        adjustedPos.add(round(pos.get(1).getAsDouble() - turretPos.get(1).getAsDouble(), 3));
                        adjustedPos.add(round(pos.get(2).getAsDouble() - turretPos.get(2).getAsDouble(), 3));
                        seat.add("Position", adjustedPos);
                    } else {
                        seat.add("Position", pos);
                    }
                    if (seatsOrientations.containsKey(1)) {
                        seat.addProperty("Orientation", round(seatsOrientations.get(1), 1));
                    }

                    if (seatsCameraPositions.containsKey(1)) {
                        applyCameraPos(seat, 1, seatsPositions, seatsCameraPositions, turretPos, isAircraft);
                    }
                }
            }

            if (!terrainCompatPositions.isEmpty()) {
                JsonArray terrainCompatArray = new JsonArray();
                for (JsonArray pos : terrainCompatPositions) {
                    terrainCompatArray.add(pos);
                }
                vehicleJson.add("TerrainCompat", terrainCompatArray);
            }

            // 遥控武器站链条：PassengerWeaponStationPos = 武器站Yaw 相对炮塔（pws.pivot + pwsYaw.pivot），
            // PassengerWeaponStationBarrelPos = 武器站Pitch 相对 Yaw（pitch.pivot）
            JsonArray[] pwsChain = extractPassengerWeaponStationChain(geoJson, turretCustomPitch);
            if (pwsChain != null) {
                vehicleJson.add("PassengerWeaponStationPos", pwsChain[0]);
                vehicleJson.add("PassengerWeaponStationBarrelPos", pwsChain[1]);
            }

            // 炮塔倾斜角（模型 turret 父骨骼的 X 旋转，如 turretRot 的 2.5°）
            if (turretCustomPitch != 0) {
                vehicleJson.addProperty("TurretCustomPitch", round(turretCustomPitch, 3));
            }

                // 修正模型/贴图引用指向本模型（data 版是游戏实际加载的文件，
                // 旧文件可能是从其他载具复制来的残留）
                applyModelPaths(vehicleJson, baseName);

                Files.createDirectories(vehicleFile.getParent());
                Files.writeString(vehicleFile, compactJson(GSON.toJson(vehicleJson)));
            }

            // Generate superbwarfare vehicle config（同时写入 superbwarfare 与本模组两个 assets 目录，
            // 保证实体 ID（dragonrise_reforge:xxx）在两个 namespace 下都能查到车辆数据）
            generateSuperbwarfareVehicleConfig(superbwarfareOutputPath, baseName);
            generateSuperbwarfareVehicleConfig(dragonriseAssetOutputPath, baseName);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process: " + geoFile.getFileName(), e);
        }
    }

    /**
     * 生成判定：模型存在名为 "Build" 的骨骼，且其父骨骼链上存在名字含 "obb" 的组。
     * 即 Blockbench 中把 Build 骨骼挂在 Obb 组下（飞机/坦克统一规则）。
     */
    private static boolean hasBuildUnderObb(JsonObject geoJson) {
        if (!geoJson.has("minecraft:geometry")) {
            return false;
        }

        Map<String, JsonObject> boneMap = new HashMap<>();
        for (JsonElement geomElement : geoJson.getAsJsonArray("minecraft:geometry")) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }
            for (JsonElement boneElement : geometry.getAsJsonArray("bones")) {
                JsonObject bone = boneElement.getAsJsonObject();
                boneMap.put(bone.get("name").getAsString(), bone);
            }
        }

        for (JsonObject bone : boneMap.values()) {
            if (!bone.get("name").getAsString().equals("Build")) {
                continue;
            }
            String parent = bone.has("parent") ? bone.get("parent").getAsString() : null;
            Set<String> visited = new java.util.HashSet<>();
            while (parent != null && visited.add(parent)) {
                if (parent.toLowerCase().contains("obb")) {
                    return true;
                }
                JsonObject parentBone = boneMap.get(parent);
                if (parentBone == null) {
                    break;
                }
                parent = parentBone.has("parent") ? parentBone.get("parent").getAsString() : null;
            }
        }
        return false;
    }

    private static boolean hasBuildBone(JsonObject geoJson) {
        if (!geoJson.has("minecraft:geometry")) {
            return false;
        }

        JsonArray geometries = geoJson.getAsJsonArray("minecraft:geometry");

        for (JsonElement geomElement : geometries) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }

            JsonArray bones = geometry.getAsJsonArray("bones");
            for (JsonElement boneElement : bones) {
                JsonObject bone = boneElement.getAsJsonObject();
                if (bone.get("name").getAsString().equals("Build")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 飞机判定：模型存在名为 "Plane" 的骨骼，且模型存在 Obb 组（名字含 "obb"）。
     * 兼容两种模型结构：
     *  - 旧结构（jas39e 等）：Plane 骨骼挂在 Obb 组下（父链上含 obb）；
     *  - 新结构（fa18e 等）：Plane 与 Obb 为平级顶层骨骼。
     * 该模型按飞机流程生成（挂架 dummy 位置、吊舱 NacellePos、座位相机 Transform=Vehicle 等）。
     */
    private static boolean isAircraft(JsonObject geoJson) {
        if (!geoJson.has("minecraft:geometry")) {
            return false;
        }

        Map<String, JsonObject> boneMap = new HashMap<>();
        boolean hasPlane = false;
        boolean hasObb = false;
        for (JsonElement geomElement : geoJson.getAsJsonArray("minecraft:geometry")) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }
            for (JsonElement boneElement : geometry.getAsJsonArray("bones")) {
                JsonObject bone = boneElement.getAsJsonObject();
                String name = bone.get("name").getAsString();
                boneMap.put(name, bone);
                if (name.equalsIgnoreCase("Plane")) {
                    hasPlane = true;
                }
                if (name.toLowerCase().contains("obb")) {
                    hasObb = true;
                }
            }
        }
        if (!hasPlane || !hasObb) {
            return false;
        }

        // 旧结构：沿 Plane 的父骨骼链向上查找 Obb 组
        for (JsonObject bone : boneMap.values()) {
            if (!bone.get("name").getAsString().equalsIgnoreCase("Plane")) {
                continue;
            }
            String parent = bone.has("parent") ? bone.get("parent").getAsString() : null;
            Set<String> visited = new java.util.HashSet<>();
            while (parent != null && visited.add(parent)) {
                if (parent.toLowerCase().contains("obb")) {
                    return true;
                }
                JsonObject parentBone = boneMap.get(parent);
                if (parentBone == null) {
                    break;
                }
                parent = parentBone.has("parent") ? parentBone.get("parent").getAsString() : null;
            }
        }

        // 新结构：Plane 与 Obb 平级（如 fa18e），模型同时存在 Plane 与 Obb 即按飞机生成
        return true;
    }

    /**
     * 车辆特征判定：模型存在名字含 "obb" 的骨骼（Obb/OBB/Obb1/TurretObb* 等）。
     * 部分车辆模型没有 Build 标记骨骼（如 ztz96a），只要有 OBB 组就按车辆流程生成。
     */
    private static boolean hasObbBone(JsonObject geoJson) {
        if (!geoJson.has("minecraft:geometry")) {
            return false;
        }
        for (JsonElement geomElement : geoJson.getAsJsonArray("minecraft:geometry")) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }
            for (JsonElement boneElement : geometry.getAsJsonArray("bones")) {
                JsonObject bone = boneElement.getAsJsonObject();
                if (bone.get("name").getAsString().toLowerCase().contains("obb")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void generateSuperbwarfareVehicleConfig(Path outputPath, String baseName) throws Exception {
        Path vehicleFile = outputPath.resolve(baseName + ".json");
        JsonObject vehicleJson;

        if (Files.exists(vehicleFile)) {
            String vehicleContent = Files.readString(vehicleFile);
            vehicleJson = JsonParser.parseString(vehicleContent).getAsJsonObject();
        } else {
            vehicleJson = new JsonObject();
            vehicleJson.addProperty("ID", "dragonrise_reforge:" + baseName);
        }

        // SBM 渲染需要 models/bedrock/vehicle/ 下的模型与 Models 数组（新格式）
        applyModelPaths(vehicleJson, baseName);

        Files.createDirectories(vehicleFile.getParent());
        Files.writeString(vehicleFile, compactJson(GSON.toJson(vehicleJson)));
    }

    /**
     * 修正载具 JSON 中的模型/贴图引用，使其指向本模型（baseName）：
     *  - Model（单数，旧格式，保留兼容）：缺失时补写；
     *  - Models（复数数组，GeoVehicleRenderer 渲染必需）：缺失时新建；
     *    已存在但内容不指向本模型（旧文件从其他载具复制来的残留）时覆盖为正确路径。
     */
    private static void applyModelPaths(JsonObject vehicleJson, String baseName) {
        String modelPath = "dragonrise_reforge:models/bedrock/vehicle/" + baseName + ".geo.json";
        String texturePath = "dragonrise_reforge:textures/entity/" + baseName + ".png";

        // Model（单数，旧格式，保留兼容）
        if (!vehicleJson.has("Model")) {
            JsonObject model = new JsonObject();
            model.addProperty("Model", modelPath);
            model.addProperty("Texture", texturePath);
            vehicleJson.add("Model", model);
        } else {
            JsonObject model = vehicleJson.getAsJsonObject("Model");
            if (!model.has("Model")) {
                model.addProperty("Model", modelPath);
            }
            if (!model.has("Texture")) {
                model.addProperty("Texture", texturePath);
            }
        }

        // Models（复数数组，GeoVehicleRenderer 渲染必需）
        if (!vehicleJson.has("Models")) {
            JsonArray models = new JsonArray();
            JsonObject entry = new JsonObject();
            entry.addProperty("Model", modelPath);
            entry.addProperty("Texture", texturePath);
            models.add(entry);
            vehicleJson.add("Models", models);
        } else {
            // 旧文件可能是从其他载具复制来的（Models 指向别的模型），
            // 内容不指向本模型时覆盖为正确路径
            boolean pointsToSelf = false;
            JsonArray models = vehicleJson.getAsJsonArray("Models");
            for (JsonElement e : models) {
                JsonObject o = e.getAsJsonObject();
                if (o.has("Model") && o.get("Model").getAsString().endsWith(baseName + ".geo.json")) {
                    pointsToSelf = true;
                    break;
                }
            }
            if (!pointsToSelf) {
                JsonArray correct = new JsonArray();
                JsonObject entry = new JsonObject();
                entry.addProperty("Model", modelPath);
                entry.addProperty("Texture", texturePath);
                correct.add(entry);
                vehicleJson.add("Models", correct);
            }
        }
    }

    private static String compactJson(String json) {
        // 把跨行的纯数字数组压缩成单行, 例如 [\n  1.0,\n  2.0\n] -> [1.0, 2.0]
        Pattern pattern = Pattern.compile("\\[\\s*\\n((?:\\s*[-\\d.]+,\\s*\\n)*\\s*[-\\d.]+\\s*)\\]");
        Matcher matcher = pattern.matcher(json);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String inner = matcher.group(1).replaceAll("\\s+", "");
            matcher.appendReplacement(sb, "[" + inner + "]");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private JsonArray extractOBBList(JsonObject geoJson, JsonArray turretPos, double theta) {
        JsonArray obbList = new JsonArray();

        // 计算炮塔枢轴在Blockbench中的原始坐标（用于TurretObb子骨骼的相对位置计算）
        double turretPivotX = turretPos != null ? turretPos.get(0).getAsDouble() * 16.0 : 0.0;
        double turretPivotY = turretPos != null ? turretPos.get(1).getAsDouble() * 16.0 : 0.0;
        double turretPivotZ = turretPos != null ? -turretPos.get(2).getAsDouble() * 16.0 : 0.0;

        if (!geoJson.has("minecraft:geometry")) {
            return obbList;
        }

        JsonArray geometries = geoJson.getAsJsonArray("minecraft:geometry");
        for (JsonElement geomElement : geometries) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }

            JsonArray bones = geometry.getAsJsonArray("bones");
            for (JsonElement boneElement : bones) {
                JsonObject bone = boneElement.getAsJsonObject();
                String boneName = bone.get("name").getAsString();

                // 只处理包含"obb"关键字的骨骼
                if (!boneName.toLowerCase().contains("obb")) {
                    continue;
                }

                // 跳过基础的Obb骨骼（不生成碰撞盒）
                if (boneName.equals("Obb")) {
                    continue;
                }

                JsonObject obbEntry = new JsonObject();
                // 提取OBB尺寸
                obbEntry.add("Size", extractSize(bone));
                // 提取OBB位置（带有炮塔偏移处理）
                obbEntry.add("Position", extractOBBPosition(bone, turretPivotX, turretPivotY, turretPivotZ, boneName, theta));

                // 提取OBB角度（CustomRotate，单位度；Z 轴与 Position 一致取反）。
                // Y（水平）旋转经游戏内实测（jas39e 机翼 OBB 前后镜像）确认需取反：
                // 游戏 OBB 旋转空间与模型渲染空间的 Y 方向相反。
                if (bone.has("rotation")) {
                    JsonArray rotation = bone.getAsJsonArray("rotation");
                    if (rotation.size() >= 3) {
                        double rx = rotation.get(0).getAsDouble();
                        double ry = -rotation.get(1).getAsDouble();
                        double rz = -rotation.get(2).getAsDouble();
                        if (rx != 0 || ry != 0 || rz != 0) {
                            JsonArray customRotate = new JsonArray();
                            customRotate.add(round(rx, 3));
                            customRotate.add(round(ry, 3));
                            customRotate.add(round(rz, 3));
                            obbEntry.add("CustomRotate", customRotate);
                        }
                    }
                }

                // 根据骨骼名称设置特殊部件属性（支持带编号后缀的变体如 MainEngineObb1、WheelLeftObb2 等）
                if (boneName.startsWith("MainEngineObb")) {
                    obbEntry.addProperty("Part", "MainEngine");
                } else if (boneName.startsWith("WheelRightObb")) {
                    obbEntry.addProperty("Part", "WheelRight");
                } else if (boneName.startsWith("WheelLeftObb")) {
                    obbEntry.addProperty("Part", "WheelLeft");
                } else if (boneName.startsWith("TurretObb")) {
                    obbEntry.addProperty("Part", "Turret");
                    // 如果不是主炮塔OBB，设置为跟随炮塔旋转
                    if (!boneName.equals("TurretObb")) {
                        obbEntry.addProperty("Transform", "Turret");
                        obbEntry.addProperty("Rotation", "Turret");
                    }
                } else if (boneName.contains("CollisionObb")) {
                    // 纯碰撞盒（不参与部件伤害路由）
                    obbEntry.addProperty("Part", "Collision");
                }

                obbList.add(obbEntry);
            }
        }

        return obbList;
    }

    private JsonArray extractOBBPosition(JsonObject bone, double turretPivotX, double turretPivotY, double turretPivotZ, String boneName, double theta) {
        JsonArray position = new JsonArray();
        if (bone.has("pivot")) {
            JsonArray pivot = bone.getAsJsonArray("pivot");
            double xValue = pivot.get(0).getAsDouble();
            double yValue = pivot.get(1).getAsDouble();
            double zValue = pivot.get(2).getAsDouble();

            // 如果是炮塔相关的OBB（除了主炮塔OBB本身），需要减去炮塔枢轴进行偏移
            if (boneName.startsWith("TurretObb") && !boneName.equals("TurretObb")) {
                xValue = xValue - turretPivotX;
                yValue = yValue - turretPivotY;
                zValue = zValue - turretPivotZ;
                // 炮塔倾斜补偿：绕炮塔枢轴反向旋转 -theta
                if (theta != 0) {
                    double rad = Math.toRadians(theta);
                    double c = Math.cos(rad), s = Math.sin(rad);
                    double ny = yValue * c + zValue * s;
                    double nz = -yValue * s + zValue * c;
                    yValue = ny;
                    zValue = nz;
                }
            }

            // X轴：直接除以16转换单位
            position.add(round(xValue / 16.0, 3));
            // Y轴：根据需要处理后除以16转换单位
            position.add(round(yValue / 16.0, 3));
            // Z轴：除以16转换单位后取反（Minecraft坐标系差异）
            position.add(round(-zValue / 16.0, 3));
        }
        return position;
    }

    /**
     * 从模型提取炮塔倾斜角：优先取 turret 父骨骼（如 turretRot）的 X 旋转，
     * 其次取 turret 自身的 X 旋转；无倾斜时返回 0。
     */
    private double extractTurretCustomPitch(JsonObject geoJson) {
        if (!geoJson.has("minecraft:geometry")) {
            return 0;
        }

        Map<String, JsonObject> boneMap = new HashMap<>();
        for (JsonElement geomElement : geoJson.getAsJsonArray("minecraft:geometry")) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }
            for (JsonElement boneElement : geometry.getAsJsonArray("bones")) {
                JsonObject bone = boneElement.getAsJsonObject();
                boneMap.put(bone.get("name").getAsString(), bone);
            }
        }

        JsonObject turret = boneMap.get("turret");
        if (turret == null) {
            return 0;
        }

        JsonArray rotation = null;
        if (turret.has("parent")) {
            JsonObject tiltBone = boneMap.get(turret.get("parent").getAsString());
            if (tiltBone != null && tiltBone.has("rotation")) {
                rotation = tiltBone.getAsJsonArray("rotation");
            }
        }
        if (rotation == null && turret.has("rotation")) {
            rotation = turret.getAsJsonArray("rotation");
        }

        return rotation != null && rotation.size() >= 1 ? rotation.get(0).getAsDouble() : 0;
    }

    /**
     * 炮塔倾斜补偿：把配置坐标（块）先转回模型像素坐标，绕炮塔枢轴反向旋转 -theta，再转回配置坐标。
     * theta 为 0 时不做任何处理。
     */
    private void untiltAroundTurret(JsonArray pos, JsonArray turretPos, double theta) {
        if (theta == 0 || turretPos == null) {
            return;
        }

        double turretX = turretPos.get(0).getAsDouble() * 16.0;
        double turretY = turretPos.get(1).getAsDouble() * 16.0;
        double turretZ = -turretPos.get(2).getAsDouble() * 16.0;

        double mx = pos.get(0).getAsDouble() * 16.0;
        double my = pos.get(1).getAsDouble() * 16.0;
        double mz = -pos.get(2).getAsDouble() * 16.0;

        double ox = mx - turretX;
        double oy = my - turretY;
        double oz = mz - turretZ;

        double rad = Math.toRadians(theta);
        double c = Math.cos(rad), s = Math.sin(rad);
        double ny = oy * c + oz * s;
        double nz = -oy * s + oz * c;

        pos.set(0, new com.google.gson.JsonPrimitive(round((ox + turretX) / 16.0, 3)));
        pos.set(1, new com.google.gson.JsonPrimitive(round((ny + turretY) / 16.0, 3)));
        pos.set(2, new com.google.gson.JsonPrimitive(round(-(nz + turretZ) / 16.0, 3)));
    }

    private JsonArray extractTurretPos(JsonObject geoJson) {
        if (!geoJson.has("minecraft:geometry")) {
            return null;
        }

        JsonArray geometries = geoJson.getAsJsonArray("minecraft:geometry");
        for (JsonElement geomElement : geometries) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }

            JsonArray bones = geometry.getAsJsonArray("bones");
            for (JsonElement boneElement : bones) {
                JsonObject bone = boneElement.getAsJsonObject();
                String boneName = bone.get("name").getAsString();

                // 查找名为"turret"的骨骼
                if (boneName.equalsIgnoreCase("turret") && bone.has("pivot")) {
                    JsonArray pivot = bone.getAsJsonArray("pivot");
                    JsonArray position = new JsonArray();
                    // X轴：直接除以16转换单位
                    position.add(round(pivot.get(0).getAsDouble() / 16.0, 3));
                    // Y轴：直接除以16转换单位
                    position.add(round(pivot.get(1).getAsDouble() / 16.0, 3));
                    // Z轴：除以16转换单位后取反（Minecraft坐标系差异）
                    position.add(round(-pivot.get(2).getAsDouble() / 16.0, 3));
                    return position;
                }
            }
        }

        return null;
    }

    /**
     * 提取炮管位置（相对于炮塔，与SuperbWarfare的BarrelPos语义一致）
     * @param geoJson 模型JSON
     * @param turretPos 炮塔位置（MC坐标，extractTurretPos的返回值）
     */
    private JsonArray extractBarrelPos(JsonObject geoJson, JsonArray turretPos, double theta) {
        if (!geoJson.has("minecraft:geometry")) {
            return null;
        }

        JsonArray geometries = geoJson.getAsJsonArray("minecraft:geometry");
        for (JsonElement geomElement : geometries) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }

            JsonArray bones = geometry.getAsJsonArray("bones");
            for (JsonElement boneElement : bones) {
                JsonObject bone = boneElement.getAsJsonObject();
                String boneName = bone.get("name").getAsString();

                // 查找名为"barrel"的骨骼
                if (boneName.equalsIgnoreCase("barrel") && bone.has("pivot")) {
                    JsonArray pivot = bone.getAsJsonArray("pivot");
                    JsonArray position = new JsonArray();
                    // 计算相对于炮塔的位置（pivot是Blockbench绝对坐标，需减去turret pivot）
                    double turretX = turretPos != null ? turretPos.get(0).getAsDouble() * 16.0 : 0.0;
                    double turretY = turretPos != null ? turretPos.get(1).getAsDouble() * 16.0 : 0.0;
                    double turretZ = turretPos != null ? -turretPos.get(2).getAsDouble() * 16.0 : 0.0;
                    double ox = pivot.get(0).getAsDouble() - turretX;
                    double oy = pivot.get(1).getAsDouble() - turretY;
                    double oz = pivot.get(2).getAsDouble() - turretZ;
                    // 炮塔倾斜补偿：绕炮塔枢轴反向旋转 -theta
                    if (theta != 0) {
                        double rad = Math.toRadians(theta);
                        double c = Math.cos(rad), s = Math.sin(rad);
                        double ny = oy * c + oz * s;
                        double nz = -oy * s + oz * c;
                        oy = ny;
                        oz = nz;
                    }
                    position.add(round(ox / 16.0, 3));
                    position.add(round(oy / 16.0, 3));
                    position.add(round(-oz / 16.0, 3));
                    return position;
                }
            }
        }

        return null;
    }

    private Map<String, JsonArray> extractWeaponPositions(JsonObject geoJson, double barrelY, double turretY, double theta) {
        Map<String, JsonArray> weaponPositions = new HashMap<>();

        if (!geoJson.has("minecraft:geometry")) {
            return weaponPositions;
        }

        JsonArray geometries = geoJson.getAsJsonArray("minecraft:geometry");
        // 遥控武器站炮管枢轴（passengerWeaponStationPitch 骨骼）的直接 pivot
        double[] pitchPivot = null;
        for (JsonElement geomElement : geometries) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }
            for (JsonElement boneElement : geometry.getAsJsonArray("bones")) {
                JsonObject bone = boneElement.getAsJsonObject();
                if (bone.get("name").getAsString().equals("passengerWeaponStationPitch") && bone.has("pivot")) {
                    JsonArray pv = bone.getAsJsonArray("pivot");
                    pitchPivot = new double[]{
                            pv.get(0).getAsDouble(),
                            pv.get(1).getAsDouble(),
                            pv.get(2).getAsDouble()
                    };
                    break;
                }
            }
            if (pitchPivot != null) {
                break;
            }
        }

        for (JsonElement geomElement : geometries) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }

            JsonArray bones = geometry.getAsJsonArray("bones");
            for (JsonElement boneElement : bones) {
                JsonObject bone = boneElement.getAsJsonObject();
                String boneName = bone.get("name").getAsString();

                if (boneName.startsWith("CannonPos") && bone.has("pivot")) {
                    addWeaponPosition(weaponPositions, "Cannon", boneName, "CannonPos", bone, barrelY, turretY);
                } else if (boneName.startsWith("MachineGunPos") && bone.has("pivot")) {
                    addWeaponPosition(weaponPositions, "MachineGun", boneName, "MachineGunPos", bone, barrelY, turretY);
                } else if (boneName.startsWith("MissilePos") && bone.has("pivot")) {
                    addWeaponPosition(weaponPositions, "Missile", boneName, "MissilePos", bone, barrelY, turretY);
                } else if (boneName.startsWith("BombPos") && bone.has("pivot")) {
                    addWeaponPosition(weaponPositions, "Bomb", boneName, "BombPos", bone, barrelY, turretY);
                } else if (boneName.startsWith("RocketPos") && bone.has("pivot")) {
                    addWeaponPosition(weaponPositions, "Rocket", boneName, "RocketPos", bone, barrelY, turretY);
                } else if (boneName.startsWith("PassengerMachineGunPos") && bone.has("pivot")) {
                    if (pitchPivot != null) {
                        // 枪口直接坐标减去俯仰枢轴直接坐标，得到相对武器站炮管的位置；随炮塔倾斜时绕 X 反向旋转 -theta 补偿
                        JsonArray pivot = bone.getAsJsonArray("pivot");
                        double ox = pivot.get(0).getAsDouble() - pitchPivot[0];
                        double oy = pivot.get(1).getAsDouble() - pitchPivot[1];
                        double oz = pivot.get(2).getAsDouble() - pitchPivot[2];
                        if (theta != 0) {
                            double rad = Math.toRadians(theta);
                            double c = Math.cos(rad), s = Math.sin(rad);
                            double ny = oy * c + oz * s;
                            double nz = -oy * s + oz * c;
                            oy = ny;
                            oz = nz;
                        }
                        JsonArray pos = new JsonArray();
                        pos.add(round(ox / 16.0, 3));
                        pos.add(round(oy / 16.0, 3));
                        pos.add(round(-oz / 16.0, 3));
                        weaponPositions.computeIfAbsent("PassengerMachineGun", k -> new JsonArray()).add(pos);
                    } else {
                        JsonArray pos = new JsonArray();
                        addWeaponPosition(weaponPositions, "PassengerMachineGun", boneName, "PassengerMachineGunPos", bone, barrelY, turretY);
                    }
                }
            }
        }

        return weaponPositions;
    }

    /**
     * 飞机挂架发射位置：读取模型中的 dummy_&lt;座位&gt;_&lt;武器序号&gt;_&lt;挂点序号&gt; 骨骼，
     * 按（座位，武器序号）分组后映射到座位 Weapons 列表中的武器名，生成 ShootPos.Positions。
     * 与 superb 挂架假体渲染（GeoVehicleRenderer.renderCustomPart）共用同一组骨骼：
     * 一个骨骼同时充当挂架假体锚点与弹药发射位置。
     *
     * @param vehicleJson 现有车辆配置（用于读取 Seats 的 Weapons 列表，把武器序号映射到武器名）
     */
    private Map<String, JsonArray> extractDummyWeaponPositions(JsonObject geoJson, JsonObject vehicleJson) {
        Map<String, JsonArray> weaponPositions = new HashMap<>();

        if (!geoJson.has("minecraft:geometry")) {
            return weaponPositions;
        }

        // 座位 -> 武器名列表（座位 1 起）
        Map<Integer, List<String>> seatWeapons = new HashMap<>();
        if (vehicleJson != null && vehicleJson.has("Seats")) {
            JsonElement seatsElement = vehicleJson.get("Seats");
            if (seatsElement.isJsonArray()) {
                JsonArray seats = seatsElement.getAsJsonArray();
                for (int i = 0; i < seats.size(); i++) {
                    JsonObject seat = seats.get(i).getAsJsonObject();
                    if (seat.has("Weapons") && seat.get("Weapons").isJsonArray()) {
                        List<String> names = new ArrayList<>();
                        for (JsonElement w : seat.getAsJsonArray("Weapons")) {
                            names.add(w.getAsString());
                        }
                        seatWeapons.put(i + 1, names);
                    }
                }
            } else if (seatsElement.isJsonObject()) {
                JsonObject seat = seatsElement.getAsJsonObject();
                if (seat.has("Weapons") && seat.get("Weapons").isJsonArray()) {
                    List<String> names = new ArrayList<>();
                    for (JsonElement w : seat.getAsJsonArray("Weapons")) {
                        names.add(w.getAsString());
                    }
                    seatWeapons.put(1, names);
                }
            }
        }

        for (JsonElement geomElement : geoJson.getAsJsonArray("minecraft:geometry")) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }

            for (JsonElement boneElement : geometry.getAsJsonArray("bones")) {
                JsonObject bone = boneElement.getAsJsonObject();
                String boneName = bone.get("name").getAsString();
                if (!boneName.startsWith("dummy_") || !bone.has("pivot")) {
                    continue;
                }

                // dummy_<座位>_<武器序号>_<挂点序号>；座位序号与 superb 渲染器一致为 0 基
                String[] parts = boneName.split("_");
                if (parts.length < 4) {
                    continue;
                }
                int seatIndex;
                int weaponIndex;
                try {
                    seatIndex = Integer.parseInt(parts[1]);
                    weaponIndex = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    continue;
                }
                if (seatIndex < 0 || weaponIndex < 0) {
                    continue;
                }

                List<String> weapons = seatWeapons.get(seatIndex + 1);
                if (weapons == null || weaponIndex >= weapons.size()) {
                    continue;
                }
                String weaponName = weapons.get(weaponIndex);

                JsonArray pivot = bone.getAsJsonArray("pivot");
                JsonArray pos = new JsonArray();
                pos.add(round(pivot.get(0).getAsDouble() / 16.0, 3));
                pos.add(round(pivot.get(1).getAsDouble() / 16.0, 3));
                pos.add(round(-pivot.get(2).getAsDouble() / 16.0, 3));
                weaponPositions.computeIfAbsent(weaponName, k -> new JsonArray()).add(pos);
            }
        }

        return weaponPositions;
    }

    /**
     * 瞄准吊舱视角定位：读取模型中的 NacellePos&lt;武器名&gt; 骨骼（如 NacellePosMissile），
     * 或 NacellePos（无后缀，应用到该座位全部武器），生成武器 ShootPos.ViewPosition。
     * 游戏内配合武器 UseNacelleCamera=true，zoom 时相机从吊舱位置看出去（A10 的 AGM65 模式）。
     */
    private Map<String, JsonArray> extractNacellePositions(JsonObject geoJson) {
        Map<String, JsonArray> nacellePositions = new HashMap<>();

        if (!geoJson.has("minecraft:geometry")) {
            return nacellePositions;
        }

        JsonArray defaultPositions = new JsonArray();
        for (JsonElement geomElement : geoJson.getAsJsonArray("minecraft:geometry")) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }

            for (JsonElement boneElement : geometry.getAsJsonArray("bones")) {
                JsonObject bone = boneElement.getAsJsonObject();
                String boneName = bone.get("name").getAsString();
                if (!boneName.startsWith("NacellePos") || !bone.has("pivot")) {
                    continue;
                }

                JsonArray pivot = bone.getAsJsonArray("pivot");
                JsonArray pos = new JsonArray();
                pos.add(round(pivot.get(0).getAsDouble() / 16.0, 3));
                pos.add(round(pivot.get(1).getAsDouble() / 16.0, 3));
                pos.add(round(-pivot.get(2).getAsDouble() / 16.0, 3));

                String weaponName = boneName.substring("NacellePos".length());
                if (weaponName.isEmpty()) {
                    // 无后缀：应用到全部武器（多个同名骨骼按序号 NacellePos1/2/… 追加）
                    defaultPositions.add(pos);
                } else {
                    nacellePositions.computeIfAbsent(weaponName, k -> new JsonArray()).add(pos);
                }
            }
        }

        if (!defaultPositions.isEmpty()) {
            // 无后缀吊舱骨骼：写为一个占位条目，由写入侧展开到全部武器
            nacellePositions.put("__ALL__", defaultPositions);
        }

        return nacellePositions;
    }

    private boolean checkSeatsPos1(JsonObject geoJson) {
        if (!geoJson.has("minecraft:geometry")) {
            return false;
        }

        JsonArray geometries = geoJson.getAsJsonArray("minecraft:geometry");
        for (JsonElement geomElement : geometries) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }

            JsonArray bones = geometry.getAsJsonArray("bones");
            for (JsonElement boneElement : bones) {
                JsonObject bone = boneElement.getAsJsonObject();
                String boneName = bone.get("name").getAsString();

                if (boneName.equals("SeatsPos1")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 提取各座位的旋转（Orientation）：读取 SeatsPos<序号> 骨骼的 Y 旋转。
     * 游戏里乘员朝向 = 座位变换再绕 Y 旋转 -Orientation，因此 Orientation = -骨骼Y旋转。
     */
    private Map<Integer, Double> extractSeatsOrientations(JsonObject geoJson) {
        Map<Integer, Double> seatsOrientations = new HashMap<>();

        if (!geoJson.has("minecraft:geometry")) {
            return seatsOrientations;
        }

        JsonArray geometries = geoJson.getAsJsonArray("minecraft:geometry");
        for (JsonElement geomElement : geometries) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }

            JsonArray bones = geometry.getAsJsonArray("bones");
            for (JsonElement boneElement : bones) {
                JsonObject bone = boneElement.getAsJsonObject();
                String boneName = bone.get("name").getAsString();

                if (boneName.startsWith("SeatsPos") && bone.has("rotation")) {
                    int index = extractIndex(boneName, "SeatsPos");
                    if (index > 0) {
                        JsonArray rotation = bone.getAsJsonArray("rotation");
                        if (rotation.size() >= 2) {
                            seatsOrientations.put(index, -rotation.get(1).getAsDouble());
                        }
                    }
                }
            }
        }

        return seatsOrientations;
    }

    private Map<Integer, JsonArray> extractSeatsPositions(JsonObject geoJson) {
        Map<Integer, JsonArray> seatsPositions = new HashMap<>();

        if (!geoJson.has("minecraft:geometry")) {
            return seatsPositions;
        }

        JsonArray geometries = geoJson.getAsJsonArray("minecraft:geometry");
        for (JsonElement geomElement : geometries) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }

            JsonArray bones = geometry.getAsJsonArray("bones");
            for (JsonElement boneElement : bones) {
                JsonObject bone = boneElement.getAsJsonObject();
                String boneName = bone.get("name").getAsString();

                if (boneName.startsWith("SeatsPos") && bone.has("pivot")) {
                    int index = extractIndex(boneName, "SeatsPos");
                    if (index > 0) {
                        JsonArray pivot = bone.getAsJsonArray("pivot");
                        JsonArray position = new JsonArray();
                        // X轴：直接除以16转换单位
                        position.add(round(pivot.get(0).getAsDouble() / 16.0, 3));
                        // Y轴：除以16转换单位后，再减去1.61的偏移值（微调座位高度）
                        position.add(round(pivot.get(1).getAsDouble() / 16.0 - 1.61, 3));
                        // Z轴：除以16转换单位后取反（Minecraft坐标系差异）
                        position.add(round(-pivot.get(2).getAsDouble() / 16.0, 3));
                        seatsPositions.put(index, position);
                    }
                }
            }
        }

        return seatsPositions;
    }

    private Map<Integer, JsonArray> extractSeatsCameraPositions(JsonObject geoJson) {
        Map<Integer, JsonArray> seatsCameraPositions = new HashMap<>();

        if (!geoJson.has("minecraft:geometry")) {
            return seatsCameraPositions;
        }

        JsonArray geometries = geoJson.getAsJsonArray("minecraft:geometry");
        for (JsonElement geomElement : geometries) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }

            JsonArray bones = geometry.getAsJsonArray("bones");
            for (JsonElement boneElement : bones) {
                JsonObject bone = boneElement.getAsJsonObject();
                String boneName = bone.get("name").getAsString();

                if (boneName.startsWith("SeatsCameraPos") && bone.has("pivot")) {
                    int index = extractIndex(boneName, "SeatsCameraPos");
                    if (index > 0) {
                        JsonArray pivot = bone.getAsJsonArray("pivot");
                        JsonArray position = new JsonArray();
                        // X轴：直接除以16转换单位
                        position.add(round(pivot.get(0).getAsDouble() / 16.0, 3));
                        // Y轴：直接除以16转换单位（摄像机高度不需要减去1.61偏移）
                        position.add(round(pivot.get(1).getAsDouble() / 16.0, 3));
                        // Z轴：除以16转换单位后取反（Minecraft坐标系差异）
                        position.add(round(-pivot.get(2).getAsDouble() / 16.0, 3));
                        seatsCameraPositions.put(index, position);
                    }
                }
            }
        }

        return seatsCameraPositions;
    }

    /**
     * 根据模型中是否存在对应座位的 SeatsCameraPos<序号> 组，写入座位的 CameraPos：
     * <ul>
     *   <li>存在：UseFixedCameraPos=true，并按座位位置相同的方式（Turret/WeaponStation 座位减去炮塔偏移）
     *       填入 Position 与 ZoomPosition；</li>
     *   <li>不存在：UseFixedCameraPos=false，并填入默认朝向——主炮/其他座位为 Transform=Turret、Direction=Barrel，
     *       遥控武器站座位（Transform=WeaponStation）为 Transform=WeaponStation、Direction=WeaponStationBarrel；
     *       同时 ZoomPosition 取 SeatsPos 原始坐标（不减去 1.61 玩家身高，按炮塔相对处理）。</li>
     * </ul>
     */
    private void applyCameraPos(JsonObject seat, int seatIndex,
                                Map<Integer, JsonArray> seatsPositions,
                                Map<Integer, JsonArray> seatsCameraPositions, JsonArray turretPos,
                                boolean isAircraft) {
        JsonObject cameraPos;
        if (seat.has("CameraPos")) {
            cameraPos = seat.getAsJsonObject("CameraPos");
        } else {
            cameraPos = new JsonObject();
            seat.add("CameraPos", cameraPos);
        }
        String seatTransform = seat.has("Transform") ? seat.get("Transform").getAsString() : "";

        if (seatsCameraPositions.containsKey(seatIndex)) {
            JsonArray camPos = seatsCameraPositions.get(seatIndex);
            JsonArray adjusted = camPos;
            if (("Turret".equals(seatTransform) || "WeaponStation".equals(seatTransform)) && turretPos != null) {
                adjusted = new JsonArray();
                adjusted.add(round(camPos.get(0).getAsDouble() - turretPos.get(0).getAsDouble(), 3));
                adjusted.add(round(camPos.get(1).getAsDouble() - turretPos.get(1).getAsDouble(), 3));
                adjusted.add(round(camPos.get(2).getAsDouble() - turretPos.get(2).getAsDouble(), 3));
            }
            cameraPos.addProperty("UseFixedCameraPos", true);
            cameraPos.add("Position", adjusted);
            cameraPos.add("ZoomPosition", adjusted);
        } else {
            cameraPos.addProperty("UseFixedCameraPos", false);
            // 二号位（遥控武器站座位，Transform=WeaponStation）默认看向武器站，其余座位看向主炮；
            // 飞机没有炮塔/炮管，默认看向载具自身（Transform=Vehicle）。
            if (isAircraft) {
                cameraPos.addProperty("Transform", "Vehicle");
                // 旧文件可能是按车辆生成的，清掉炮塔/炮管残留方向
                if (cameraPos.has("Direction")) {
                    cameraPos.remove("Direction");
                }
            } else if ("WeaponStation".equals(seatTransform)) {
                cameraPos.addProperty("Transform", "WeaponStation");
                cameraPos.addProperty("Direction", "WeaponStationBarrel");
            } else {
                cameraPos.addProperty("Transform", "Turret");
                cameraPos.addProperty("Direction", "Barrel");
            }

            // 未提供额外视角位置时，ZoomPosition 使用 SeatsPos 原始坐标（不减去 1.61 玩家身高），
            // 并按座位相同的方式处理炮塔相对偏移。
            if (seatsPositions.containsKey(seatIndex)) {
                JsonArray rawPos = seatsPositions.get(seatIndex);
                double zoomX = rawPos.get(0).getAsDouble();
                double zoomY = rawPos.get(1).getAsDouble() + 1.61;
                double zoomZ = rawPos.get(2).getAsDouble();

                if (("Turret".equals(seatTransform) || "WeaponStation".equals(seatTransform)) && turretPos != null) {
                    zoomX -= turretPos.get(0).getAsDouble();
                    zoomY -= turretPos.get(1).getAsDouble();
                    zoomZ -= turretPos.get(2).getAsDouble();
                }

                JsonArray zoom = new JsonArray();
                zoom.add(round(zoomX, 3));
                zoom.add(round(zoomY, 3));
                zoom.add(round(zoomZ, 3));
                cameraPos.add("ZoomPosition", zoom);
            }
        }
    }

    private List<JsonArray> extractTerrainCompatPositions(JsonObject geoJson) {
        Map<Integer, JsonArray> tempMap = new HashMap<>();

        if (!geoJson.has("minecraft:geometry")) {
            return new ArrayList<>();
        }

        JsonArray geometries = geoJson.getAsJsonArray("minecraft:geometry");
        for (JsonElement geomElement : geometries) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }

            JsonArray bones = geometry.getAsJsonArray("bones");
            for (JsonElement boneElement : bones) {
                JsonObject bone = boneElement.getAsJsonObject();
                String boneName = bone.get("name").getAsString();

                if (boneName.startsWith("TerrainCompatPos") && bone.has("pivot")) {
                    int index = extractIndex(boneName, "TerrainCompatPos");
                    if (index > 0) {
                        JsonArray pivot = bone.getAsJsonArray("pivot");
                        JsonArray position = new JsonArray();
                        // X轴：直接除以16转换单位
                        position.add(round(pivot.get(0).getAsDouble() / 16.0, 3));
                        // Y轴：直接除以16转换单位（不需要高度偏移）
                        position.add(round(pivot.get(1).getAsDouble() / 16.0, 3));
                        // Z轴：除以16转换单位后取反（Minecraft坐标系差异）
                        position.add(round(-pivot.get(2).getAsDouble() / 16.0, 3));
                        tempMap.put(index, position);
                    }
                }
            }
        }

        List<JsonArray> terrainCompatPositions = new ArrayList<>();
        int maxIndex = tempMap.keySet().stream().max(Integer::compareTo).orElse(0);
        for (int i = 1; i <= maxIndex; i++) {
            if (tempMap.containsKey(i)) {
                terrainCompatPositions.add(tempMap.get(i));
            }
        }
        return terrainCompatPositions;
    }

    private int extractIndex(String boneName, String prefix) {
        try {
            String numberPart = boneName.substring(prefix.length());
            return Integer.parseInt(numberPart);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 从模型计算遥控武器站链条的两个配置字段（子骨骼直接 pivot 减去父骨骼直接 pivot，与 amx56 等参考车一致）：
     * [0] PassengerWeaponStationPos = passengerWeaponStationYaw.pivot − turret.pivot
     * [1] PassengerWeaponStationBarrelPos = passengerWeaponStationPitch.pivot − passengerWeaponStationYaw.pivot
     */
    private JsonArray[] extractPassengerWeaponStationChain(JsonObject geoJson, double theta) {
        if (!geoJson.has("minecraft:geometry")) {
            return null;
        }

        Map<String, JsonObject> boneMap = new HashMap<>();
        for (JsonElement geomElement : geoJson.getAsJsonArray("minecraft:geometry")) {
            JsonObject geometry = geomElement.getAsJsonObject();
            if (!geometry.has("bones")) {
                continue;
            }
            for (JsonElement boneElement : geometry.getAsJsonArray("bones")) {
                JsonObject bone = boneElement.getAsJsonObject();
                boneMap.put(bone.get("name").getAsString(), bone);
            }
        }

        JsonObject turret = boneMap.get("turret");
        JsonObject pwsYaw = boneMap.get("passengerWeaponStationYaw");
        JsonObject pitch = boneMap.get("passengerWeaponStationPitch");
        if (turret == null || pwsYaw == null || pitch == null
                || !turret.has("pivot") || !pwsYaw.has("pivot") || !pitch.has("pivot")) {
            return null;
        }

        JsonArray turretPivot = turret.getAsJsonArray("pivot");
        JsonArray yawPivot = pwsYaw.getAsJsonArray("pivot");
        JsonArray pitchPivot = pitch.getAsJsonArray("pivot");

        double ox = yawPivot.get(0).getAsDouble() - turretPivot.get(0).getAsDouble();
        double oy = yawPivot.get(1).getAsDouble() - turretPivot.get(1).getAsDouble();
        double oz = yawPivot.get(2).getAsDouble() - turretPivot.get(2).getAsDouble();
        double bx = pitchPivot.get(0).getAsDouble() - yawPivot.get(0).getAsDouble();
        double by = pitchPivot.get(1).getAsDouble() - yawPivot.get(1).getAsDouble();
        double bz = pitchPivot.get(2).getAsDouble() - yawPivot.get(2).getAsDouble();

        // 武器站随炮塔一起倾斜时，绕 X 反向旋转 -theta 补偿
        if (theta != 0) {
            double rad = Math.toRadians(theta);
            double c = Math.cos(rad), s = Math.sin(rad);
            double ny = oy * c + oz * s;
            double nz = -oy * s + oz * c;
            oy = ny;
            oz = nz;
            ny = by * c + bz * s;
            nz = -by * s + bz * c;
            by = ny;
            bz = nz;
        }

        JsonArray pos = new JsonArray();
        pos.add(round(ox / 16.0, 3));
        pos.add(round(oy / 16.0, 3));
        pos.add(round(-oz / 16.0, 3));

        JsonArray barrelPos = new JsonArray();
        barrelPos.add(round(bx / 16.0, 3));
        barrelPos.add(round(by / 16.0, 3));
        barrelPos.add(round(-bz / 16.0, 3));

        return new JsonArray[]{pos, barrelPos};
    }

    private void addWeaponPosition(Map<String, JsonArray> weaponPositions, String weaponName, String boneName, String prefix, JsonObject bone, double barrelY, double turretY) {
        JsonArray positions;
        if (weaponPositions.containsKey(weaponName)) {
            positions = weaponPositions.get(weaponName);
        } else {
            positions = new JsonArray();
            weaponPositions.put(weaponName, positions);
        }

        JsonArray pivot = bone.getAsJsonArray("pivot");
        JsonArray pos = new JsonArray();
        // X轴：直接除以16转换单位
        pos.add(round(pivot.get(0).getAsDouble() / 16.0, 3));
        // Y轴：直接除以16转换单位（具体偏移会在后面根据Transform类型处理）
        pos.add(round(pivot.get(1).getAsDouble() / 16.0, 3));
        // Z轴：除以16转换单位后取反（Minecraft坐标系差异）
        pos.add(round(-pivot.get(2).getAsDouble() / 16.0, 3));
        positions.add(pos);
    }

    private JsonArray extractSize(JsonObject bone) {
        JsonArray size = new JsonArray();
        if (bone.has("cubes") && bone.getAsJsonArray("cubes").size() > 0) {
            JsonObject cube = bone.getAsJsonArray("cubes").get(0).getAsJsonObject();
            if (cube.has("size")) {
                JsonArray originalSize = cube.getAsJsonArray("size");
                for (JsonElement element : originalSize) {
                    size.add(round(element.getAsDouble() / 32.0, 3));
                }
            }
        }
        return size;
    }

    private JsonArray extractPosition(JsonObject bone) {
        JsonArray position = new JsonArray();
        if (bone.has("pivot")) {
            JsonArray pivot = bone.getAsJsonArray("pivot");
            position.add(round(pivot.get(0).getAsDouble() / 16.0, 3));
            position.add(round(pivot.get(1).getAsDouble() / 16.0, 3));
            position.add(round(-pivot.get(2).getAsDouble() / 16.0, 3));
        }
        return position;
    }

    private double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    @Override
    public String getName() {
        return "Geo OBB Data Provider";
    }
}

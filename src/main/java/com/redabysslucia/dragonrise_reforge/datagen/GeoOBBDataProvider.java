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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
                Path inputPath = Path.of(workingDir).resolve("src/main/resources/assets/dragonrise_reforge/geo");
                Path outputPath = Path.of(workingDir).resolve("src/main/resources/data/dragonrise_reforge/sbw/vehicles");

                if (!Files.exists(inputPath)) {
                    return;
                }

                try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputPath, "*.geo.json")) {
                    for (Path geoFile : stream) {
                        processGeoFile(geoFile, outputPath);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to process geo files", e);
            }
        });
    }

    private void processGeoFile(Path geoFile, Path outputPath) {
        try {
            String content = Files.readString(geoFile);
            JsonObject geoJson = JsonParser.parseString(content).getAsJsonObject();

            boolean hasSeatsPos1 = checkSeatsPos1(geoJson);

            JsonArray turretPos = hasSeatsPos1 ? extractTurretPos(geoJson) : null;
            double turretPivotY = turretPos != null ? turretPos.get(1).getAsDouble() * 16.0 : 0.0;
            JsonArray barrelPos = hasSeatsPos1 ? extractBarrelPos(geoJson) : null;
            double barrelPosY = barrelPos != null ? barrelPos.get(1).getAsDouble() : 0.0;
            double barrelPivotY = barrelPosY * 16.0;
            JsonArray obbList = extractOBBList(geoJson, turretPivotY);
            Map<String, JsonArray> weaponPositions = extractWeaponPositions(geoJson, barrelPivotY, turretPivotY);
            Map<Integer, JsonArray> seatsPositions = extractSeatsPositions(geoJson);
            Map<Integer, JsonArray> seatsCameraPositions = extractSeatsCameraPositions(geoJson);
            List<JsonArray> terrainCompatPositions = extractTerrainCompatPositions(geoJson);

            if (obbList.isEmpty() && turretPos == null && barrelPos == null && weaponPositions.isEmpty() && seatsPositions.isEmpty() && seatsCameraPositions.isEmpty() && terrainCompatPositions.isEmpty()) {
                return;
            }

            String baseName = geoFile.getFileName().toString().replace(".geo.json", "");
            Path vehicleFile = outputPath.resolve(baseName + ".json");

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

                        if (hasSeatsPos1) {
                            if ("Barrel".equals(transform) && barrelPos != null && turretPos != null) {
                                double barrelY = barrelPos.get(1).getAsDouble();
                                double turretY = turretPos.get(1).getAsDouble();
                                for (int i = 0; i < adjustedPositions.size(); i++) {
                                    JsonArray pos = adjustedPositions.get(i).getAsJsonArray();
                                    double y = pos.get(1).getAsDouble();
                                    y = y - barrelY - turretY;
                                    pos.set(1, new com.google.gson.JsonPrimitive(round(y, 3)));
                                }
                            } else if ("Turret".equals(transform) && turretPos != null) {
                                double turretY = turretPos.get(1).getAsDouble();
                                for (int i = 0; i < adjustedPositions.size(); i++) {
                                    JsonArray pos = adjustedPositions.get(i).getAsJsonArray();
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

            if (hasSeatsPos1 && !seatsPositions.isEmpty() && vehicleJson.has("Seats")) {
                JsonArray seats = vehicleJson.getAsJsonArray("Seats");
                for (int i = 0; i < seats.size(); i++) {
                    JsonObject seat = seats.get(i).getAsJsonObject();
                    int index = i + 1;

                    if (seatsPositions.containsKey(index)) {
                        seat.add("Position", seatsPositions.get(index));
                    }

                    if (seatsCameraPositions.containsKey(index)) {
                        JsonObject cameraPos;
                        if (seat.has("CameraPos")) {
                            cameraPos = seat.getAsJsonObject("CameraPos");
                            cameraPos.add("Position", seatsCameraPositions.get(index));
                        } else {
                            cameraPos = new JsonObject();
                            cameraPos.addProperty("UseFixedCameraPos", true);
                            cameraPos.add("Position", seatsCameraPositions.get(index));
                            cameraPos.addProperty("Transform", "Turret");
                            cameraPos.add("ZoomPosition", seatsCameraPositions.get(index));
                            cameraPos.addProperty("Direction", "Barrel");
                            seat.add("CameraPos", cameraPos);
                        }
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

            if (vehicleJson.has("VehicleIcon")) {
                String iconPath = "dragonrise_reforge:textures/vehicle_icon/" + baseName + "_icon.png";
                vehicleJson.addProperty("VehicleIcon", iconPath);
            }

            Files.createDirectories(vehicleFile.getParent());
            Files.writeString(vehicleFile, GSON.toJson(vehicleJson));

        } catch (Exception e) {
            throw new RuntimeException("Failed to process: " + geoFile.getFileName(), e);
        }
    }

    private JsonArray extractOBBList(JsonObject geoJson, double turretPivotY) {
        JsonArray obbList = new JsonArray();

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

                if (!boneName.toLowerCase().contains("obb")) {
                    continue;
                }

                if (boneName.equals("Obb")) {
                    continue;
                }

                JsonObject obbEntry = new JsonObject();
                obbEntry.add("Size", extractSize(bone));
                obbEntry.add("Position", extractOBBPosition(bone, turretPivotY, boneName));

                if (boneName.equals("MainEngineObb")) {
                    obbEntry.addProperty("Part", "MainEngine");
                } else if (boneName.equals("WheelRightObb")) {
                    obbEntry.addProperty("Part", "WheelRight");
                } else if (boneName.equals("WheelLeftObb")) {
                    obbEntry.addProperty("Part", "WheelLeft");
                } else if (boneName.startsWith("TurretObb")) {
                    obbEntry.addProperty("Part", "Turret");
                    if (!boneName.equals("TurretObb")) {
                        obbEntry.addProperty("Transform", "Turret");
                        obbEntry.addProperty("Rotation", "Turret");
                    }
                }

                obbList.add(obbEntry);
            }
        }

        return obbList;
    }

    private JsonArray extractOBBPosition(JsonObject bone, double turretPivotY, String boneName) {
        JsonArray position = new JsonArray();
        if (bone.has("pivot")) {
            JsonArray pivot = bone.getAsJsonArray("pivot");
            double yValue = pivot.get(1).getAsDouble();

            if (boneName.startsWith("TurretObb") && !boneName.equals("TurretObb")) {
                yValue = yValue - turretPivotY;
            }

            position.add(round(pivot.get(0).getAsDouble() / 16.0, 3));
            position.add(round(yValue / 16.0, 3));
            position.add(round(-pivot.get(2).getAsDouble() / 16.0, 3));
        }
        return position;
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

                if (boneName.equalsIgnoreCase("turret") && bone.has("pivot")) {
                    JsonArray pivot = bone.getAsJsonArray("pivot");
                    JsonArray position = new JsonArray();
                    position.add(round(pivot.get(0).getAsDouble() / 16.0, 3));
                    position.add(round(pivot.get(1).getAsDouble() / 16.0, 3));
                    position.add(round(-pivot.get(2).getAsDouble() / 16.0, 3));
                    return position;
                }
            }
        }

        return null;
    }

    private JsonArray extractBarrelPos(JsonObject geoJson) {
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

                if (boneName.equalsIgnoreCase("barrel") && bone.has("pivot")) {
                    JsonArray pivot = bone.getAsJsonArray("pivot");
                    JsonArray position = new JsonArray();
                    position.add(round(pivot.get(0).getAsDouble() / 16.0, 3));
                    position.add(round(pivot.get(1).getAsDouble() / 16.0, 3));
                    position.add(round(-pivot.get(2).getAsDouble() / 16.0, 3));
                    return position;
                }
            }
        }

        return null;
    }

    private Map<String, JsonArray> extractWeaponPositions(JsonObject geoJson, double barrelY, double turretY) {
        Map<String, JsonArray> weaponPositions = new HashMap<>();

        if (!geoJson.has("minecraft:geometry")) {
            return weaponPositions;
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

                if (boneName.startsWith("CannonPos") && bone.has("pivot")) {
                    addWeaponPosition(weaponPositions, "Cannon", boneName, "CannonPos", bone, barrelY, turretY);
                } else if (boneName.startsWith("MachineGunPos") && bone.has("pivot")) {
                    addWeaponPosition(weaponPositions, "MachineGun", boneName, "MachineGunPos", bone, barrelY, turretY);
                } else if (boneName.startsWith("MissilePos") && bone.has("pivot")) {
                    addWeaponPosition(weaponPositions, "Missile", boneName, "MissilePos", bone, barrelY, turretY);
                }
            }
        }

        return weaponPositions;
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
                        position.add(round(pivot.get(0).getAsDouble() / 16.0, 3));
                        position.add(round(pivot.get(1).getAsDouble() / 16.0 - 1.65, 3));
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
                        position.add(round(pivot.get(0).getAsDouble() / 16.0, 3));
                        position.add(round(pivot.get(1).getAsDouble() / 16.0 - 1.65, 3));
                        position.add(round(-pivot.get(2).getAsDouble() / 16.0, 3));
                        seatsCameraPositions.put(index, position);
                    }
                }
            }
        }

        return seatsCameraPositions;
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
                        position.add(round(pivot.get(0).getAsDouble() / 16.0, 3));
                        position.add(round(pivot.get(1).getAsDouble() / 16.0, 3));
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
        pos.add(round(pivot.get(0).getAsDouble() / 16.0, 3));
        pos.add(round(pivot.get(1).getAsDouble() / 16.0, 3));
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

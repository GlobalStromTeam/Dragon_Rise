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

            JsonArray obbList = extractOBBList(geoJson);
            if (obbList.isEmpty()) {
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

            vehicleJson.add("OBB", obbList);

            Files.createDirectories(vehicleFile.getParent());
            Files.writeString(vehicleFile, GSON.toJson(vehicleJson));

        } catch (Exception e) {
            throw new RuntimeException("Failed to process: " + geoFile.getFileName(), e);
        }
    }

    private JsonArray extractOBBList(JsonObject geoJson) {
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
                obbEntry.add("Position", extractPosition(bone));

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

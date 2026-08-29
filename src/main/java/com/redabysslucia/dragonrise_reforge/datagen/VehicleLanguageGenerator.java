package com.redabysslucia.dragonrise_reforge.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class VehicleLanguageGenerator implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final PackOutput output;
    private final net.neoforged.neoforge.common.data.ExistingFileHelper existingFileHelper;

    public VehicleLanguageGenerator(PackOutput output, net.neoforged.neoforge.common.data.ExistingFileHelper existingFileHelper) {
        this.output = output;
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        return CompletableFuture.runAsync(() -> {
            try {
                String workingDir = System.getProperty("user.dir").replace("\\run-data", "").replace("/run-data", "");
                Path vehiclesDir = Path.of(workingDir).resolve("src/main/resources/data/dragonrise_reforge/sbw/vehicles");
                Path langDir = Path.of(workingDir).resolve("src/main/resources/assets/dragonrise_reforge/lang");

                if (!Files.exists(vehiclesDir)) {
                    System.out.println("Vehicles directory not found: " + vehiclesDir);
                    return;
                }

                Map<String, String> existingVehicleFiles = new HashMap<>();
                Map<String, String> allNames = new HashMap<>();
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(vehiclesDir, "*.json")) {
                    for (Path vehicleFile : stream) {
                        String fileName = vehicleFile.getFileName().toString().replace(".json", "");
                        existingVehicleFiles.put("info.dragonrise_reforge." + fileName, fileName);
                        existingVehicleFiles.put("entity.dragonrise_reforge." + fileName, fileName);
                        extractNamesFromFile(vehicleFile, allNames);
                    }
                }

                if (!allNames.isEmpty()) {
                    updateLanguageFile(langDir.resolve("zh_cn.json"), allNames, existingVehicleFiles);
                    updateLanguageFile(langDir.resolve("en_us.json"), allNames, existingVehicleFiles);
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed to generate language files", e);
            }
        });
    }

    private void extractNamesFromFile(Path vehicleFile, Map<String, String> allNames) {
        try {
            String fileName = vehicleFile.getFileName().toString().replace(".json", "");
            
            allNames.put("info.dragonrise_reforge." + fileName, "");
            allNames.put("entity.dragonrise_reforge." + fileName, "");
            
            String content = Files.readString(vehicleFile);
            JsonObject vehicleJson = GSON.fromJson(content, JsonObject.class);

            if (vehicleJson.has("Weapons")) {
                JsonObject weapons = vehicleJson.getAsJsonObject("Weapons");
                for (Map.Entry<String, JsonElement> weaponEntry : weapons.entrySet()) {
                    JsonObject weapon = weaponEntry.getValue().getAsJsonObject();
                    extractNameFromObject(weapon, allNames);

                    if (weapon.has("AmmoType")) {
                        JsonElement ammoType = weapon.get("AmmoType");
                        extractNamesFromAmmoType(ammoType, allNames);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to process file: " + vehicleFile.getFileName());
            e.printStackTrace();
        }
    }

    private void extractNamesFromAmmoType(JsonElement ammoType, Map<String, String> allNames) {
        if (ammoType.isJsonArray()) {
            for (JsonElement element : ammoType.getAsJsonArray()) {
                if (element.isJsonObject()) {
                    JsonObject ammoObj = element.getAsJsonObject();
                    if (ammoObj.has("Override")) {
                        JsonObject override = ammoObj.getAsJsonObject("Override");
                        extractNameFromObject(override, allNames);
                    }
                }
            }
        }
    }

    private void extractNameFromObject(JsonObject obj, Map<String, String> allNames) {
        if (obj.has("Name")) {
            String nameKey = obj.get("Name").getAsString();
            if (!nameKey.isEmpty()) {
                if (!allNames.containsKey(nameKey)) {
                    allNames.put(nameKey, "");
                }
            }
        }
    }

    private void updateLanguageFile(Path langFile, Map<String, String> newEntries, Map<String, String> existingVehicleFiles) {
        try {
            JsonObject langJson;
            if (Files.exists(langFile)) {
                String content = Files.readString(langFile);
                langJson = GSON.fromJson(content, JsonObject.class);
            } else {
                langJson = new JsonObject();
            }

            java.util.Iterator<Map.Entry<String, JsonElement>> iterator = langJson.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonElement> entry = iterator.next();
                String key = entry.getKey();
                boolean shouldRemove = false;
                
                if ((key.startsWith("info.dragonrise_reforge.") || key.startsWith("entity.dragonrise_reforge.")) 
                    && !existingVehicleFiles.containsKey(key)) {
                    shouldRemove = true;
                } else if (key.startsWith("dragonrise_reforge:") && !newEntries.containsKey(key)) {
                    shouldRemove = true;
                }
                
                if (shouldRemove) {
                    iterator.remove();
                    System.out.println("Removed orphaned key: " + key);
                }
            }

            for (Map.Entry<String, String> entry : newEntries.entrySet()) {
                String key = entry.getKey();
                if (!langJson.has(key)) {
                    langJson.addProperty(key, entry.getValue());
                } else {
                    String existingValue = langJson.get(key).getAsString();
                    if (existingValue.equals(key)) {
                        langJson.addProperty(key, "");
                    }
                }
            }

            TreeMap<String, Object> sortedJson = new TreeMap<>();
            for (Map.Entry<String, JsonElement> entry : langJson.entrySet()) {
                if (entry.getValue().isJsonPrimitive()) {
                    sortedJson.put(entry.getKey(), entry.getValue().getAsString());
                } else {
                    sortedJson.put(entry.getKey(), entry.getValue());
                }
            }

            Files.createDirectories(langFile.getParent());
            Files.writeString(langFile, GSON.toJson(sortedJson));
            System.out.println("Updated language file: " + langFile.getFileName());

        } catch (IOException e) {
            System.err.println("Failed to update language file: " + langFile.getFileName());
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "Vehicle Language Generator";
    }
}

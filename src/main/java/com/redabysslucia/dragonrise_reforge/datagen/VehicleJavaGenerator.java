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
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class VehicleJavaGenerator implements DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final PackOutput output;
    private final ExistingFileHelper existingFileHelper;

    public VehicleJavaGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        this.output = output;
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        return CompletableFuture.runAsync(() -> {
            try {
                String workingDir = System.getProperty("user.dir").replace("\\run-data", "").replace("/run-data", "");
                Path inputPath = Path.of(workingDir).resolve("src/main/resources/assets/dragonrise_reforge/geo");

                if (!Files.exists(inputPath)) {
                    return;
                }

                try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputPath, "*.geo.json")) {
                    for (Path geoFile : stream) {
                        processGeoFile(geoFile, workingDir);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to process geo files", e);
            }
        });
    }

    private void processGeoFile(Path geoFile, String workingDir) {
        try {
            String content = Files.readString(geoFile);
            JsonObject geoJson = JsonParser.parseString(content).getAsJsonObject();

            if (!hasBuildBone(geoJson)) {
                return;
            }

            String baseName = geoFile.getFileName().toString().replace(".geo.json", "");
            
            String entityClassName = toPascalCase(baseName) + "Entity";
            String modelClassName = toPascalCase(baseName) + "Model";
            String rendererClassName = toPascalCase(baseName) + "Renderer";
            String entityConstantName = baseName.toUpperCase();

            Path javaSourcePath = Path.of(workingDir).resolve("src/main/java/com/redabysslucia/dragonrise_reforge");
            Path entitiesPath = javaSourcePath.resolve("entities");
            Path modelPath = javaSourcePath.resolve("client/model/entity");
            Path rendererPath = javaSourcePath.resolve("client/renderer/entity");
            Path initPath = javaSourcePath.resolve("init");

            Files.createDirectories(entitiesPath);
            Files.createDirectories(modelPath);
            Files.createDirectories(rendererPath);
            Files.createDirectories(initPath);

            Path entityFile = entitiesPath.resolve(entityClassName + ".java");
            Path modelFile = modelPath.resolve(modelClassName + ".java");
            Path rendererFile = rendererPath.resolve(rendererClassName + ".java");

            if (!Files.exists(entityFile)) {
                String entityContent = generateEntityContent(baseName, entityClassName);
                Files.writeString(entityFile, entityContent);
            }

            if (!Files.exists(modelFile)) {
                String modelContent = generateModelContent(baseName, entityClassName, modelClassName);
                Files.writeString(modelFile, modelContent);
            }

            if (!Files.exists(rendererFile)) {
                String rendererContent = generateRendererContent(baseName, entityClassName, modelClassName, rendererClassName);
                Files.writeString(rendererFile, rendererContent);
            }

            updateModEntities(initPath, baseName, entityClassName, entityConstantName);
            updateModEntityRenderers(initPath, baseName, entityClassName, rendererClassName, entityConstantName);
            updateModTabs(initPath, baseName, entityConstantName);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process: " + geoFile.getFileName(), e);
        }
    }

    private boolean hasBuildBone(JsonObject geoJson) {
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

                if (boneName.equals("Build")) {
                    return true;
                }
            }
        }

        return false;
    }

    private void updateModEntities(Path initPath, String baseName, String entityClassName, String entityConstantName) throws Exception {
        Path modEntitiesFile = initPath.resolve("ModEntities.java");
        if (!Files.exists(modEntitiesFile)) {
            return;
        }

        String content = Files.readString(modEntitiesFile);
        
        if (content.contains("public static final DeferredHolder<EntityType<?>, EntityType<" + entityClassName + ">> " + entityConstantName + " = register")) {
            return;
        }

        String registrationCode = "    public static final DeferredHolder<EntityType<?>, EntityType<" + entityClassName + ">> " + entityConstantName + " = register(\"" + baseName + "\",\n" +
                "            EntityType.Builder.of(" + entityClassName + "::new, MobCategory.MISC)\n" +
                "                    .setTrackingRange(512)\n" +
                "                    .setUpdateInterval(2)\n" +
                "                    .fireImmune()\n" +
                "                    .sized(4.0f, 2.9f)\n" +
                "    );\n";

        String importStatement = "import com.redabysslucia.dragonrise_reforge.entities." + entityClassName + ";\n";
        if (!content.contains(importStatement)) {
            content = content.replace("package com.redabysslucia.dragonrise_reforge.init;", "package com.redabysslucia.dragonrise_reforge.init;\n\n" + importStatement);
        }

        int lastRegistration = content.lastIndexOf("    public static final DeferredHolder<EntityType<?>, EntityType<");
        int insertIndex = content.indexOf(");", lastRegistration);
        int endOfLine = content.indexOf("\n", insertIndex);
        content = content.substring(0, endOfLine + 1) + "\n" + registrationCode + content.substring(endOfLine + 1);

        Files.writeString(modEntitiesFile, content);
    }

    private void updateModEntityRenderers(Path initPath, String baseName, String entityClassName, String rendererClassName, String entityConstantName) throws Exception {
        Path modEntityRenderersFile = initPath.resolve("ModEntityRenderers.java");
        if (!Files.exists(modEntityRenderersFile)) {
            return;
        }

        String content = Files.readString(modEntityRenderersFile);

        if (content.contains("event.registerEntityRenderer(ModEntities." + entityConstantName + ".get(), " + rendererClassName + "::new);")) {
            return;
        }

        String registrationCode = "        event.registerEntityRenderer(ModEntities." + entityConstantName + ".get(), " + rendererClassName + "::new);";

        String rendererImport = "import com.redabysslucia.dragonrise_reforge.client.renderer.entity." + rendererClassName + ";\n";
        if (!content.contains(rendererImport)) {
            content = content.replace("package com.redabysslucia.dragonrise_reforge.init;", "package com.redabysslucia.dragonrise_reforge.init;\n\n" + rendererImport);
        }

        int lastRegistration = content.lastIndexOf("        event.registerEntityRenderer(");
        int insertIndex = content.indexOf(");", lastRegistration);
        int endOfLine = content.indexOf("\n", insertIndex);
        content = content.substring(0, endOfLine + 1) + "\n" + registrationCode + content.substring(endOfLine + 1);

        Files.writeString(modEntityRenderersFile, content);
    }

    private void updateModTabs(Path initPath, String baseName, String entityConstantName) throws Exception {
        Path modTabsFile = initPath.resolve("ModTabs.java");
        if (!Files.exists(modTabsFile)) {
            return;
        }

        String content = Files.readString(modTabsFile);

        if (content.contains("output.accept(ContainerBlockItem.createInstance(ModEntities." + entityConstantName + ".get()));")) {
            return;
        }

        String tabCode = "                        output.accept(ContainerBlockItem.createInstance(ModEntities." + entityConstantName + ".get()));";

        int lastTabEntry = content.lastIndexOf("output.accept(ContainerBlockItem.createInstance(ModEntities.");
        int insertIndex = content.indexOf(");", lastTabEntry);
        int endOfLine = content.indexOf("\n", insertIndex);
        content = content.substring(0, endOfLine + 1) + "\n" + tabCode + content.substring(endOfLine + 1);

        Files.writeString(modTabsFile, content);
    }

    private String toPascalCase(String name) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : name.toCharArray()) {
            if (c == '_' || c == '-' || c == ' ') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        return result.toString();
    }

    private String generateEntityContent(String baseName, String entityClassName) {
        String pascalName = toPascalCase(baseName);
        return """
package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class %s extends GeoVehicleEntity {

        public %s(EntityType<%s> type, Level world) {
                super(type, world);
        }


}
""".formatted(pascalName + "Entity", pascalName + "Entity", pascalName + "Entity");
    }

    private String generateModelContent(String baseName, String entityClassName, String modelClassName) {
        String pascalName = toPascalCase(baseName);
        return """
package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.%s;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class %s extends DragonriseVehicleModel<%s> {
    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<%s> collectTransform(String boneName) {
        if (boneName.equals("propeller")) {
            return (bone, vehicle, state) -> bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
        }

        if (boneName.equals("tailPropeller")) {
            return (bone, vehicle, state) -> bone.setRotX(-6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
        }

        return super.collectTransform(boneName);
    }
}
""".formatted(pascalName + "Entity", pascalName + "Model", pascalName + "Entity", pascalName + "Entity");
    }

    private String generateRendererContent(String baseName, String entityClassName, String modelClassName, String rendererClassName) {
        String pascalName = toPascalCase(baseName);
        return """
package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.%s;
import com.redabysslucia.dragonrise_reforge.client.model.entity.%s;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class %s extends VehicleRenderer<%s> {
        public %s(EntityRendererProvider.Context renderManager) {
                super(renderManager, new %s());
        }
}
""".formatted(pascalName + "Entity", pascalName + "Model", pascalName + "Renderer", pascalName + "Entity", pascalName + "Renderer", pascalName + "Model");
    }

    @Override
    public String getName() {
        return "Vehicle Java Generator";
    }
}

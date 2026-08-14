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
            String rendererClassName = toPascalCase(baseName) + "Renderer";
            String entityConstantName = baseName.toUpperCase();

            Path javaSourcePath = Path.of(workingDir).resolve("src/main/java/com/redabysslucia/dragonrise_reforge");
            Path entitiesPath = javaSourcePath.resolve("entities");
            Path rendererPath = javaSourcePath.resolve("client/renderer/entity");
            Path initPath = javaSourcePath.resolve("init");

            Files.createDirectories(entitiesPath);
            Files.createDirectories(rendererPath);
            Files.createDirectories(initPath);

            Path entityFile = entitiesPath.resolve(entityClassName + ".java");
            Path rendererFile = rendererPath.resolve(rendererClassName + ".java");

            if (!Files.exists(entityFile)) {
                String entityContent = generateEntityContent(baseName, entityClassName);
                Files.writeString(entityFile, entityContent);
            }

            if (!Files.exists(rendererFile)) {
                String rendererContent = generateRendererContent(baseName, entityClassName, rendererClassName);
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

        if (content.contains("public static final RegistryObject<EntityType<" + entityClassName + ">> " + entityConstantName + " = register")) {
            return;
        }

        String registrationCode = "    public static final RegistryObject<EntityType<" + entityClassName + ">> " + entityConstantName + " = register(\"" + baseName + "\",\n" +
                "            EntityType.Builder.of(" + entityClassName + "::new, MobCategory.MISC)\n" +
                "                    .setTrackingRange(512)\n" +
                "                    .setUpdateInterval(2)\n" +
                "                    .fireImmune()\n" +
                "                    .sized(4.0f, 2.9f)\n" +
                "    );";

        String importStatement = "import com.redabysslucia.dragonrise_reforge.entities." + entityClassName + ";\n";
        if (!content.contains(importStatement)) {
            content = content.replace("package com.redabysslucia.dragonrise_reforge.init;", "package com.redabysslucia.dragonrise_reforge.init;\n\n" + importStatement);
        }

        content = insertAfterLast(content, "public static final RegistryObject<EntityType<", registrationCode);

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

        content = insertAfterLast(content, "event.registerEntityRenderer(ModEntities.", registrationCode);

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

        content = insertAfterLast(content, "output.accept(ContainerBlockItem.createInstance(ModEntities.", tabCode);

        Files.writeString(modTabsFile, content);
    }

    /**
     * 在 content 中最后一个匹配 marker 的行之后插入 newBlock。
     * <ul>
     *   <li>新块各行的缩进自动对齐到 marker 行的缩进（块内相对缩进保持不变）；</li>
     *   <li>换行风格跟随文件（CRLF/LF），不再产生混合换行或多余空行；</li>
     *   <li>兼容文件末尾无换行的情况（直接在末尾追加），避免把新行错误地插到文件开头。</li>
     * </ul>
     */
    private String insertAfterLast(String content, String marker, String newBlock) {
        int lastIdx = content.lastIndexOf(marker);
        if (lastIdx < 0) {
            return content;
        }

        String eol = content.contains("\r\n") ? "\r\n" : "\n";
        String indent = extractIndent(content, lastIdx);

        // 把 newBlock 的缩进对齐到 marker 行的缩进
        String blockIndent = extractIndent(newBlock, 0);
        StringBuilder aligned = new StringBuilder();
        for (String line : newBlock.split("\n", -1)) {
            if (line.isEmpty()) {
                aligned.append(line);
                continue;
            }
            if (line.startsWith(blockIndent)) {
                aligned.append(indent).append(line.substring(blockIndent.length()));
            } else {
                aligned.append(line);
            }
        }

        int lineEnd = content.indexOf("\n", lastIdx);
        if (lineEnd == -1) {
            // marker 行是最后一行且没有末尾换行：在文件末尾追加
            return content + eol + aligned + eol;
        }
        // 在 marker 行的行尾换行之后插入
        int insertPoint = lineEnd + 1;
        return content.substring(0, insertPoint) + aligned + eol + content.substring(insertPoint);
    }

    /** 返回 text 中 offset 所在行的前导空格。 */
    private String extractIndent(String text, int offset) {
        int lineStart = text.lastIndexOf('\n', offset) + 1;
        int i = lineStart;
        while (i < text.length() && text.charAt(i) == ' ') {
            i++;
        }
        return text.substring(lineStart, i);
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

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class %s extends VehicleEntity {

    public %s(EntityType<%s> type, Level world) {
        super(type, world);
    }

}
""".formatted(pascalName + "Entity", pascalName + "Entity", pascalName + "Entity");
    }

    private String generateRendererContent(String baseName, String entityClassName, String rendererClassName) {
        String pascalName = toPascalCase(baseName);
        return """
package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.%s;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class %s extends GeoVehicleRenderer<%s> {
    public %s(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }
}
""".formatted(pascalName + "Entity", pascalName + "Renderer", pascalName + "Entity", pascalName + "Renderer");
    }

    @Override
    public String getName() {
        return "Vehicle Java Generator";
    }
}

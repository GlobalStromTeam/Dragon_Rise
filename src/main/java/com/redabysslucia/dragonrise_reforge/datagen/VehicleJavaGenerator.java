package com.redabysslucia.dragonrise_reforge.datagen;


import net.neoforged.neoforge.registries.DeferredHolder;import com.google.gson.Gson;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
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
                Path geoPath = Path.of(workingDir).resolve("src/main/resources/assets/dragonrise_reforge/geo");
                Path bedrockPath = Path.of(workingDir).resolve("src/main/resources/assets/dragonrise_reforge/models/bedrock/vehicle");

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
                    processGeoFile(geoFile, workingDir);
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

            // 入口闸门：只有 Obb 组下存在 Build 骨骼的模型才会生成（飞机/坦克统一规则，
            // 与 GeoOBBDataProvider 一致）——无 Build 或 Build 不在 Obb 组下的一律跳过。
            if (!hasBuildUnderObb(geoJson)) {
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

            // 实体类文件已存在说明该实体由手动维护（注册/渲染器/创造物品已手动配置），
            // 生成器不再改动它，避免破坏手动维护的 ModEntities / ModEntityRenderers / ModTabs 结构。
            boolean entityFileExisted = Files.exists(entityFile);

            if (!entityFileExisted) {
                String entityContent = generateEntityContent(baseName, entityClassName);
                Files.writeString(entityFile, entityContent);
            }

            if (!Files.exists(rendererFile)) {
                String rendererContent = generateRendererContent(baseName, entityClassName, rendererClassName);
                Files.writeString(rendererFile, rendererContent);
            }

            // 仅对由生成器新建的实体执行注册/物品自动插入（全新模型一键接入）；
            // 手动创建的实体保持完全手动管理。
            if (!entityFileExisted) {
                updateModEntities(initPath, baseName, entityClassName, entityConstantName);
                updateModEntityRenderers(initPath, baseName, entityClassName, rendererClassName, entityConstantName);
                updateModTabs(initPath, baseName, entityConstantName);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to process: " + geoFile.getFileName(), e);
        }
    }

    /**
     * 生成判定：模型存在名为 "Build" 的骨骼，且其父骨骼链上存在名字含 "obb" 的组。
     * 即 Blockbench 中把 Build 骨骼挂在 Obb 组下（飞机/坦克统一规则）。
     * 与 GeoOBBDataProvider.hasBuildUnderObb 保持一致。
     */
    private boolean hasBuildUnderObb(JsonObject geoJson) {
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

    /**
     * 飞机判定：模型存在名为 "Plane" 的骨骼，且模型存在 Obb 组（名字含 "obb"）。
     * 兼容两种结构：Plane 挂在 Obb 组下（jas39e 等）或 Plane 与 Obb 平级（fa18e 等）。
     * 与 GeoOBBDataProvider.isAircraft 保持一致。
     */
    private boolean isAircraft(JsonObject geoJson) {
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
     * 部分车辆模型没有 Build 标记骨骼（如 ztz96a），只要有 OBB 组也生成。
     */
    private boolean hasObbBone(JsonObject geoJson) {
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
                "    );";

        String importLine = "import com.redabysslucia.dragonrise_reforge.entities." + entityClassName + ";";
        if (!content.contains(importLine)) {
            content = content.replace("package com.redabysslucia.dragonrise_reforge.init;", "package com.redabysslucia.dragonrise_reforge.init;\n\n" + importLine + "\n");
        }

        content = insertAfterBlockEnd(content, "public static final DeferredHolder<EntityType<?>, EntityType<", "    );", registrationCode);

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

        String rendererImportLine = "import com.redabysslucia.dragonrise_reforge.client.renderer.entity." + rendererClassName + ";";
        if (!content.contains(rendererImportLine)) {
            content = content.replace("package com.redabysslucia.dragonrise_reforge.init;", "package com.redabysslucia.dragonrise_reforge.init;\n\n" + rendererImportLine + "\n");
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
     * 在 content 中最后一个匹配 marker 的行之后插入 newBlock（单行插入）。
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
        String aligned = alignBlock(newBlock, indent);

        int lineEnd = content.indexOf("\n", lastIdx);
        if (lineEnd == -1) {
            // marker 行是最后一行且没有末尾换行：在文件末尾追加
            return content + eol + aligned + eol;
        }
        // 在 marker 行的行尾换行之后插入
        int insertPoint = lineEnd + 1;
        return content.substring(0, insertPoint) + aligned + eol + content.substring(insertPoint);
    }

    /**
     * 在 content 中最后一个匹配 marker 的<b>多行块</b>的结束标记行之后插入 newBlock。
     * 用于 ModEntities 这类注册代码是多行块（声明行 + Builder 链 + {@code );}）的情况：
     * 插入点取块结束标记（如 {@code "    );"}）所在行的换行之后，避免把已有块劈开。
     */
    private String insertAfterBlockEnd(String content, String marker, String blockEndMarker, String newBlock) {
        int lastIdx = content.lastIndexOf(marker);
        if (lastIdx < 0) {
            return content;
        }

        String eol = content.contains("\r\n") ? "\r\n" : "\n";
        String indent = extractIndent(content, lastIdx);
        String aligned = alignBlock(newBlock, indent);

        int blockEndIdx = content.indexOf(blockEndMarker, lastIdx);
        int insertPoint;
        if (blockEndIdx >= 0) {
            int lineEnd = content.indexOf("\n", blockEndIdx);
            insertPoint = lineEnd >= 0 ? lineEnd + 1 : content.length();
        } else {
            // 找不到块结束标记：回退到 marker 行尾插入
            int lineEnd = content.indexOf("\n", lastIdx);
            insertPoint = lineEnd >= 0 ? lineEnd + 1 : content.length();
        }
        return content.substring(0, insertPoint) + aligned + eol + content.substring(insertPoint);
    }

    /** 把 newBlock 的缩进对齐到目标缩进（块内相对缩进与换行保持不变）。 */
    private String alignBlock(String newBlock, String indent) {
        String blockIndent = extractIndent(newBlock, 0);
        StringBuilder aligned = new StringBuilder();
        int start = 0;
        while (start <= newBlock.length()) {
            int nl = newBlock.indexOf('\n', start);
            String line = (nl >= 0) ? newBlock.substring(start, nl) : newBlock.substring(start);
            if (!line.isEmpty() && line.startsWith(blockIndent)) {
                aligned.append(indent).append(line.substring(blockIndent.length()));
            } else {
                aligned.append(line);
            }
            if (nl >= 0) {
                aligned.append('\n');
                start = nl + 1;
            } else {
                break;
            }
        }
        return aligned.toString();
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

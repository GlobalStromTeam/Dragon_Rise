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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 车辆皮肤生成器：根据 textures/entity/ 下的贴图命名自动生成 vehicle_skins 数据。
 * <p>
 * 约定：贴图命名为 {@code {载具ID}_{涂装ID}.png}（如 {@code brdm2_sand.png}、{@code brdm2_ttsko.png}），
 * 载具 ID 必须是 {@link com.redabysslucia.dragonrise_reforge.init.ModEntities} 中已注册的名称，
 * 运行 runData 后自动为每个有涂装贴图的载具生成/合并 vehicle_skins JSON（data/ 与 assets/ 双份，并同步 IDE/Gradle 输出目录）。
 * <p>
 * 幂等：已有皮肤条目（按 Id）保留不覆盖，只补充新涂装。
 */
public class VehicleSkinGenerator implements DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern REGISTER_PATTERN = Pattern.compile("register\\(\"([a-z0-9_]+)\"");
    /** 非涂装的贴图后缀（跳过，不生成皮肤条目） */
    private static final Set<String> EXCLUDED_SUFFIXES = Set.of("icon", "glow", "icon_item", "outline");

    /** 涂装 ID → 显示名（可扩展；未命名的直接用 ID） */
    private static final Map<String, String> SKIN_NAMES = new TreeMap<>();

    static {
        SKIN_NAMES.put("sand", "沙漠涂装");
        SKIN_NAMES.put("green", "绿色涂装");
        SKIN_NAMES.put("ttsko", "TTSKO 涂装");
        SKIN_NAMES.put("ttsko1960", "TTSKO 1960 涂装");
        SKIN_NAMES.put("nato", "北约涂装");
        SKIN_NAMES.put("desert", "沙漠涂装");
        SKIN_NAMES.put("snow", "雪地涂装");
        SKIN_NAMES.put("woodland", "林地涂装");
    }

    private final PackOutput output;
    private final ExistingFileHelper existingFileHelper;

    public VehicleSkinGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        this.output = output;
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        return CompletableFuture.runAsync(() -> {
            try {
                String workingDir = System.getProperty("user.dir").replace("\\run-data", "").replace("/run-data", "");
                Path modEntitiesFile = Path.of(workingDir).resolve("src/main/java/com/redabysslucia/dragonrise_reforge/init/ModEntities.java");
                Path texDir = Path.of(workingDir).resolve("src/main/resources/assets/dragonrise_reforge/textures/entity");
                if (!Files.exists(modEntitiesFile) || !Files.exists(texDir)) {
                    return;
                }

                Set<String> vehicleIds = readVehicleIds(modEntitiesFile);
                if (vehicleIds.isEmpty()) {
                    return;
                }

                // 收集 载具ID → 涂装ID 集合
                // 匹配规则：
                //  1) 贴图文件名去掉 .png 后若本身就是已注册实体名（如 m4a2_105）→ 是默认贴图，跳过
                //  2) 否则按 {载具ID}_{涂装ID} 拆分，载具 ID 从长到短匹配（避免 m4a2 抢走 m4a2_105 的后缀）
                List<String> idsByLengthDesc = vehicleIds.stream()
                        .sorted(Comparator.comparingInt(String::length).reversed())
                        .toList();
                Map<String, Set<String>> skinsByVehicle = new TreeMap<>();
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(texDir, "*.png")) {
                    for (Path p : stream) {
                        String name = p.getFileName().toString().replace(".png", "");
                        if (vehicleIds.contains(name)) {
                            continue; // 默认贴图（实体名本身）
                        }
                        for (String id : idsByLengthDesc) {
                            if (name.length() > id.length() + 1 && name.startsWith(id + "_")) {
                                String suffix = name.substring(id.length() + 1);
                                if (!EXCLUDED_SUFFIXES.contains(suffix)) {
                                    skinsByVehicle.computeIfAbsent(id, k -> new TreeSet<>()).add(suffix);
                                }
                                break; // 已匹配到最长 id，停止
                            }
                        }
                    }
                }

                if (skinsByVehicle.isEmpty()) {
                    System.out.println("VehicleSkinGenerator: no skin textures found");
                    return;
                }

                List<Path> dataDirs = new ArrayList<>();
                dataDirs.add(Path.of(workingDir).resolve("src/main/resources/data/dragonrise_reforge/sbw/vehicle_skins"));
                dataDirs.add(Path.of(workingDir).resolve("src/main/resources/assets/dragonrise_reforge/sbw/vehicle_skins"));
                dataDirs.add(Path.of(workingDir).resolve("bin/main/data/dragonrise_reforge/sbw/vehicle_skins"));
                dataDirs.add(Path.of(workingDir).resolve("build/resources/main/data/dragonrise_reforge/sbw/vehicle_skins"));

                int written = 0;
                for (Map.Entry<String, Set<String>> entry : skinsByVehicle.entrySet()) {
                    String id = entry.getKey();
                    for (Path dir : dataDirs) {
                        Files.createDirectories(dir);
                        Path skinFile = dir.resolve(id + ".json");
                        JsonObject json = readOrCreate(skinFile);
                        boolean changed = mergeSkins(json, id, entry.getValue());
                        if (changed || !Files.exists(skinFile)) {
                            Files.writeString(skinFile, GSON.toJson(json));
                        }
                    }
                    written++;
                    System.out.println("VehicleSkinGenerator: " + id + " -> " + String.join(", ", entry.getValue()));
                }
                System.out.println("VehicleSkinGenerator: generated/merged skins for " + written + " vehicle(s)");
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate vehicle skins", e);
            }
        });
    }

    /** 从 ModEntities.java 提取所有已注册的实体名称。 */
    private Set<String> readVehicleIds(Path modEntitiesFile) throws Exception {
        String content = Files.readString(modEntitiesFile);
        Set<String> ids = new TreeSet<>();
        Matcher m = REGISTER_PATTERN.matcher(content);
        while (m.find()) {
            ids.add(m.group(1));
        }
        return ids;
    }

    private JsonObject readOrCreate(Path skinFile) {
        try {
            if (Files.exists(skinFile)) {
                return JsonParser.parseString(Files.readString(skinFile)).getAsJsonObject();
            }
        } catch (Exception ignored) {
        }
        JsonObject json = new JsonObject();
        json.add("Skins", new JsonArray());
        return json;
    }

    /** 把新涂装合并进 Skins（按 Id 去重，已有保留）；返回是否有新增。 */
    private boolean mergeSkins(JsonObject json, String vehicleId, Set<String> skinIds) {
        JsonArray skins;
        if (json.has("Skins") && json.get("Skins").isJsonArray()) {
            skins = json.getAsJsonArray("Skins");
        } else {
            skins = new JsonArray();
            json.add("Skins", skins);
        }

        Set<String> existingIds = new LinkedHashSet<>();
        for (JsonElement e : skins) {
            if (e.isJsonObject() && e.getAsJsonObject().has("Id")) {
                existingIds.add(e.getAsJsonObject().get("Id").getAsString());
            }
        }

        boolean changed = false;
        for (String skinId : skinIds) {
            if (existingIds.contains(skinId)) {
                continue;
            }
            JsonObject skin = new JsonObject();
            skin.addProperty("Id", skinId);
            skin.addProperty("Name", SKIN_NAMES.getOrDefault(skinId, skinId));
            skin.addProperty("Description", "使用喷漆罐喷涂的 " + SKIN_NAMES.getOrDefault(skinId, skinId) + "涂装");
            skin.addProperty("Texture", "dragonrise_reforge:textures/entity/" + vehicleId + "_" + skinId + ".png");
            skin.addProperty("Priority", 0);
            skins.add(skin);
            existingIds.add(skinId);
            changed = true;
        }
        return changed;
    }

    @Override
    public String getName() {
        return "DragonRise Vehicle Skin Generator";
    }
}

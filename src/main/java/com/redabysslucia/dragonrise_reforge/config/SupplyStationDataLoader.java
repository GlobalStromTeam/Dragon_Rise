package com.redabysslucia.dragonrise_reforge.config;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Dragonrise_reforge.MODID)
public class SupplyStationDataLoader extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder()
            .setLenient()
            .create();
    private static final String DIRECTORY = "supply_station";

    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get()
            .resolve(Dragonrise_reforge.MODID)
            .resolve(DIRECTORY);

    private static volatile Map<String, SupplyStationConfig> loadedConfigs = new HashMap<>();

    public SupplyStationDataLoader() {
        super(GSON, DIRECTORY);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Map<ResourceLocation, JsonElement> map = Maps.newHashMap();

        loadFromResourceManager(pResourceManager, map);
        loadFromFilesystem(map);

        LOGGER.info("Supply station: found {} config source(s) for reload", map.size());
        return map;
    }

    private void loadFromResourceManager(ResourceManager resourceManager, Map<ResourceLocation, JsonElement> map) {
        int i = DIRECTORY.length() + 1;

        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources(DIRECTORY,
                loc -> loc.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            String path = resourceLocation.getPath();
            ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(
                    resourceLocation.getNamespace(),
                    path.substring(i, path.length() - ".json".length())
            );

            if (map.containsKey(fileId)) {
                continue;
            }

            try (Reader rawReader = entry.getValue().openAsReader()) {
                String stripped = stripJsonComments(rawReader);
                JsonElement jsonElement = JsonParser.parseString(stripped);
                if (jsonElement != null) {
                    map.put(fileId, jsonElement);
                    LOGGER.debug("Loaded from datapack: {}", fileId);
                }
            } catch (Exception e) {
                LOGGER.error("Couldn't parse supply station config from datapack: {}", fileId, e);
            }
        }
    }

    private void loadFromFilesystem(Map<ResourceLocation, JsonElement> map) {
        if (!Files.exists(CONFIG_DIR)) {
            return;
        }

        try (var files = Files.list(CONFIG_DIR)) {
            files.filter(f -> f.toString().endsWith(".json")).forEach(file -> {
                String fileName = file.getFileName().toString();
                String id = fileName.substring(0, fileName.length() - ".json".length());
                ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, id);

                try {
                    String content = Files.readString(file);
                    String stripped = stripJsonCommentsString(content);
                    JsonElement jsonElement = JsonParser.parseString(stripped);
                    if (jsonElement != null) {
                        map.put(fileId, jsonElement);
                        LOGGER.info("Loaded from config dir: {} (overrides datapack)", fileId);
                    }
                } catch (Exception e) {
                    LOGGER.error("Couldn't parse supply station config from filesystem: {}", file, e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Couldn't list supply station config dir: {}", CONFIG_DIR, e);
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, SupplyStationConfig> newConfigs = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : data.entrySet()) {
            ResourceLocation location = entry.getKey();
            String key = location.toString();
            try {
                SupplyStationConfig config = GSON.fromJson(entry.getValue(), SupplyStationConfig.class);
                newConfigs.put(key, config);
                LOGGER.debug("Applied supply station config: {}", key);
            } catch (Exception e) {
                LOGGER.error("Failed to parse supply station config: {}", key, e);
            }
        }
        loadedConfigs = newConfigs;
        LOGGER.info("Loaded {} supply station config(s)", loadedConfigs.size());
    }

    private static String stripJsonComments(Reader reader) throws java.io.IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = reader instanceof BufferedReader
                ? (BufferedReader) reader
                : new BufferedReader(reader);

        String line;
        while ((line = br.readLine()) != null) {
            String trimmed = line.strip();
            if (trimmed.startsWith("//")) {
                continue;
            }
            if (trimmed.startsWith("/*")) {
                while (line != null && !line.strip().endsWith("*/")) {
                    line = br.readLine();
                }
                continue;
            }
            sb.append(line).append('\n');
        }

        return sb.toString();
    }

    private static String stripJsonCommentsString(String content) {
        try {
            return stripJsonComments(new java.io.StringReader(content));
        } catch (java.io.IOException e) {
            return content;
        }
    }

    public static SupplyStationConfig getConfig() {
        String ownId = Dragonrise_reforge.MODID + ":default";
        SupplyStationConfig config = loadedConfigs.get(ownId);
        if (config != null) {
            return config;
        }
        if (!loadedConfigs.isEmpty()) {
            return loadedConfigs.values().iterator().next();
        }
        return new SupplyStationConfig();
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SupplyStationDataLoader());
    }
}

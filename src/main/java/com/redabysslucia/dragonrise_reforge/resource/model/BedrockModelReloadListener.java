package com.redabysslucia.dragonrise_reforge.resource.model;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.animation.BedrockAnimation;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.GsonUtil;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockAnimationFile;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import com.google.gson.Gson;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BedrockModelReloadListener<T> extends SimplePreparableReloadListener<Map<ResourceLocation, BedrockModelPOJO>> {
    protected final String modelPath;
    protected final String animPath;
    protected final Gson gson;
    protected final Map<ResourceLocation, T> models = new HashMap<>();
    protected final Map<ResourceLocation, BedrockAnimationFile> animFiles = new HashMap<>();
    protected final Map<ResourceLocation, List<BedrockAnimation>> animations = new HashMap<>();
    protected final Map<ResourceLocation, ResourceLocation> idToModelPaths = new HashMap<>();
    protected final Map<ResourceLocation, ResourceLocation> animPathToIds = new HashMap<>();

    public BedrockModelReloadListener(String modelPath) {
        this(modelPath, "");
    }

    public BedrockModelReloadListener(String modelPath, String animPath) {
        this.modelPath = modelPath;
        this.animPath = animPath;
        this.gson = GsonUtil.CLIENT_GSON;
    }

    @Override
    protected Map<ResourceLocation, BedrockModelPOJO> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, BedrockModelPOJO> map = new HashMap<>();
        FileToIdConverter modelConverter = FileToIdConverter.json(this.modelPath);

        for (Map.Entry<ResourceLocation, net.minecraft.server.packs.resources.Resource> entry : modelConverter.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation location = entry.getKey();
            ResourceLocation id = modelConverter.fileToId(location);
            id = new ResourceLocation(id.getNamespace(), id.getPath().replace(".geo", ""));
            try {
                var reader = entry.getValue().openAsReader();
                try {
                    BedrockModelPOJO pojo = GsonHelper.fromJson(this.gson, reader, BedrockModelPOJO.class);
                    var existed = map.put(location, pojo);
                    idToModelPaths.put(id, location);
                    if (existed != null) {
                        throw new IllegalStateException("Duplicate model resource " + location);
                    }
                } finally {
                    reader.close();
                }
            } catch (Exception e) {
                Dragonrise_reforge.LOGGER.error("Error while reading model {}", location, e);
            }
        }

        if (!this.animPath.isEmpty()) {
            FileToIdConverter animConverter = FileToIdConverter.json(this.animPath);

            for (Map.Entry<ResourceLocation, net.minecraft.server.packs.resources.Resource> entry : animConverter.listMatchingResources(resourceManager).entrySet()) {
                ResourceLocation location = entry.getKey();
                ResourceLocation id = animConverter.fileToId(location);
                id = new ResourceLocation(id.getNamespace(), id.getPath().replace(".animation", ""));

                try {
                    var reader = entry.getValue().openAsReader();
                    try {
                        BedrockAnimationFile file = GsonHelper.fromJson(this.gson, reader, BedrockAnimationFile.class);
                        var existed = this.animFiles.put(location, file);
                        animPathToIds.put(location, id);
                        if (existed != null) {
                            throw new IllegalStateException("Duplicate animation resource " + location);
                        }
                    } finally {
                        reader.close();
                    }
                } catch (Exception e) {
                    Dragonrise_reforge.LOGGER.error("Error while reading animation {}", location, e);
                }
            }
        }

        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, BedrockModelPOJO> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.models.clear();
        this.animations.clear();
    }

    public T getModel(ResourceLocation path) {
        return this.models.get(path);
    }

    public List<BedrockAnimation> getAnimation(ResourceLocation path) {
        return this.animations.get(path);
    }
}
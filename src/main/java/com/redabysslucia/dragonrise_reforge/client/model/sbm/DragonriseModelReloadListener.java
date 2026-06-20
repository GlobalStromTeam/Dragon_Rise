package com.redabysslucia.dragonrise_reforge.client.model.sbm;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockBone;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.GsonUtil;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import com.google.gson.Gson;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Quaternionf;

import java.util.*;
import java.util.regex.Pattern;

public class DragonriseModelReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, BedrockModelPOJO>> {

    private static final Pattern WHEEL_PATTERN = Pattern.compile("^wheel(?<direction>[LR]).*$");
    private static final Pattern SHELL_PATTERN = Pattern.compile("^shell(?<id>\\d+)$");

    private final String modelPath;
    private final String animPath;
    private final Gson gson;

    public final Map<ResourceLocation, BedrockModel> models = new HashMap<>();
    public final Map<ResourceLocation, CachedVehicleModel> cachedModels = new HashMap<>();

    public DragonriseModelReloadListener(String modelPath, String animPath) {
        this.modelPath = modelPath;
        this.animPath = animPath;
        this.gson = GsonUtil.CLIENT_GSON;
    }

    public static class BoneSnapshot {
        public final float x, y, z;
        public final Quaternionf rotation;

        public BoneSnapshot(BedrockBone bone) {
            this.x = bone.x;
            this.y = bone.y;
            this.z = bone.z;
            this.rotation = new Quaternionf(bone.rotation);
        }

        public void restore(BedrockBone bone) {
            bone.x = x;
            bone.y = y;
            bone.z = z;
            bone.rotation.set(rotation);
        }
    }

    public static class CachedVehicleModel {
        public final BedrockModel model;
        public final List<BedrockBone> leftWheels = new ArrayList<>();
        public final List<BedrockBone> rightWheels = new ArrayList<>();
        public final List<BedrockBone> leftWheelsTurn = new ArrayList<>();
        public final List<BedrockBone> rightWheelsTurn = new ArrayList<>();
        public final Map<String, BoneSnapshot> boneSnapshots = new HashMap<>();

        public CachedVehicleModel(BedrockModel model) {
            this.model = model;
            categorizeBones(model);
            snapshotBones(model);
        }

        private void categorizeBones(BedrockModel model) {
            var map = model.getBoneMap();
            for (var entry : map.entrySet()) {
                String name = entry.getKey();
                BedrockBone bone = entry.getValue();

                var wheelMatcher = WHEEL_PATTERN.matcher(name);
                if (wheelMatcher.matches()) {
                    boolean left = "L".equals(wheelMatcher.group("direction"));
                    boolean turn = name.endsWith("Turn");
                    if (left) {
                        if (turn) leftWheelsTurn.add(bone);
                        else leftWheels.add(bone);
                    } else {
                        if (turn) rightWheelsTurn.add(bone);
                        else rightWheels.add(bone);
                    }
                }
            }
        }

        private void snapshotBones(BedrockModel model) {
            for (var entry : model.getBoneMap().entrySet()) {
                boneSnapshots.put(entry.getKey(), new BoneSnapshot(entry.getValue()));
            }
        }

        public void resetBones() {
            for (var entry : model.getBoneMap().entrySet()) {
                var snapshot = boneSnapshots.get(entry.getKey());
                if (snapshot != null) {
                    snapshot.restore(entry.getValue());
                }
            }
        }

        public BedrockBone getBone(String name) {
            return model.getBoneMap().get(name);
        }
    }

    @Override
    protected Map<ResourceLocation, BedrockModelPOJO> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        var map = new HashMap<ResourceLocation, BedrockModelPOJO>();
        var modelConverter = FileToIdConverter.json(this.modelPath);

        for (var entry : modelConverter.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation location = entry.getKey();
            var resource = entry.getValue();
            var id = modelConverter.fileToId(location);
            id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replace(".geo", ""));

            try (var reader = resource.openAsReader()) {
                var pojo = GsonHelper.fromJson(this.gson, reader, BedrockModelPOJO.class);
                var existed = map.put(id, pojo);
                if (existed != null) {
                    throw new IllegalStateException("Duplicate model resource " + resource);
                }
            } catch (Exception e) {
                Dragonrise_reforge.LOGGER.error("Error while reading model {}", resource, e);
            }
        }

        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, BedrockModelPOJO> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.models.clear();
        this.cachedModels.clear();

        map.forEach((location, pojo) -> {
            var model = new BedrockModel(pojo);
            this.models.put(location, model);
            this.cachedModels.put(location, new CachedVehicleModel(model));
        });
    }

    public CachedVehicleModel getCachedModel(ResourceLocation path) {
        return this.cachedModels.get(path);
    }
}

package com.redabysslucia.dragonrise_reforge.init;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockBone;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.GsonUtil;
import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockModelEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockModelReloadListenerEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.resource.BedrockModelResourceSet;
import com.github.mcmodderanchor.simplebedrockmodel.v1.resource.RawResourceLoader;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * SBM模型加载器，仿照 SuperbWarfare 的 BedrockModelLoader 设计，
 * 使用 SBM 内置的 RegisterBedrockModelEvent 注册模型并缓存骨骼分类信息。
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DragonriseBedrockLoader {

    private static final Pattern WHEEL_PATTERN = Pattern.compile("^wheel(?<direction>[LR]).*$");
    private static final Pattern SHELL_PATTERN = Pattern.compile("^shell(?<id>\\d+)$");
    private static final Pattern TRACK_PATTERN = Pattern.compile("^track(?<type>Mov|Rot)(?<direction>[LR])(?<id>\\d+)$");
    private static final Pattern FLARE_PATTERN = Pattern.compile("^flare.*");
    private static final Pattern DOG_TAG_PATTERN = Pattern.compile("^.*_dogTag$");

    private static final List<ResourceLocation> SBM_MODEL_PATHS = new ArrayList<>();
    private static final Map<ResourceLocation, ResourceLocation> RENDER_KEY_TO_SBM = new HashMap<>();
    private static final Map<ResourceLocation, CachedVehicleModel> CACHED = new HashMap<>();

    // 渲染时使用的 key（供 Renderer 调用） → SBM 模型注册路径
    public static final ResourceLocation SU24_KEY = register("vehicle/su24", "su24");
    public static final ResourceLocation SU24M_KEY = register("vehicle/su24m", "su24m");
    public static final ResourceLocation ZTZ99A_KEY = register("vehicle/ztz99a", "ztz99a");

    private static ResourceLocation register(String sbmPath, String renderKey) {
        var sbmLoc = new ResourceLocation(Dragonrise_reforge.MODID, sbmPath + ".geo");
        var renderLoc = new ResourceLocation(Dragonrise_reforge.MODID, renderKey);
        SBM_MODEL_PATHS.add(sbmLoc);
        RENDER_KEY_TO_SBM.put(renderLoc, sbmLoc);
        return renderLoc;
    }

    private static final RawResourceLoader COMMON_LOADER = new RawResourceLoader() {
        @Override
        public <T> T load(InputStream inputStream, Class<T> clazz) {
            try (var reader = new InputStreamReader(inputStream)) {
                return GsonUtil.CLIENT_GSON.fromJson(reader, clazz);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    @SubscribeEvent
    public static void onRegisterBedrockModels(RegisterBedrockModelEvent event) {
        // 去重后注册到 SBM
        new HashSet<>(SBM_MODEL_PATHS).forEach(rl -> event.register(rl, COMMON_LOADER));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onModelLoaded(RegisterBedrockModelReloadListenerEvent event) {
        event.register(resourceSet -> {
            CACHED.clear();
            RENDER_KEY_TO_SBM.forEach((renderKey, sbmKey) -> {
                var model = resourceSet.get(sbmKey);
                if (model != null) {
                    CACHED.put(renderKey, new CachedVehicleModel(model));
                }
            });
        });
    }

    @SuppressWarnings("unused")
    public static BedrockModel getModel(ResourceLocation renderKey) {
        var sbmKey = RENDER_KEY_TO_SBM.get(renderKey);
        if (sbmKey == null) return null;
        return BedrockModelResourceSet.getInstance().getModel(sbmKey);
    }

    public static CachedVehicleModel getCachedModel(ResourceLocation renderKey) {
        return CACHED.get(renderKey);
    }

    // ==================== CachedVehicleModel ====================

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
        public final List<BedrockBone> leftTrackMove = new ArrayList<>();
        public final List<BedrockBone> leftTrackRot = new ArrayList<>();
        public final List<BedrockBone> rightTrackMove = new ArrayList<>();
        public final List<BedrockBone> rightTrackRot = new ArrayList<>();
        public final List<BedrockBone> shellBones = new ArrayList<>();
        public final List<BedrockBone> flareBones = new ArrayList<>();
        public final List<BedrockBone> dogTagBones = new ArrayList<>();
        public final Map<Integer, BedrockBone> shellMap = new HashMap<>();
        public final Map<Integer, BedrockBone> leftTrackMoveMap = new HashMap<>();
        public final Map<Integer, BedrockBone> leftTrackRotMap = new HashMap<>();
        public final Map<Integer, BedrockBone> rightTrackMoveMap = new HashMap<>();
        public final Map<Integer, BedrockBone> rightTrackRotMap = new HashMap<>();
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

                var shellMatcher = SHELL_PATTERN.matcher(name);
                if (shellMatcher.matches()) {
                    int index = Integer.parseInt(shellMatcher.group("id"));
                    shellMap.put(index, bone);
                }

                var trackMatcher = TRACK_PATTERN.matcher(name);
                if (trackMatcher.matches()) {
                    boolean isRot = "Rot".equals(trackMatcher.group("type"));
                    boolean isL = "L".equals(trackMatcher.group("direction"));
                    int index = Integer.parseInt(trackMatcher.group("id"));
                    if (isRot) {
                        if (isL) leftTrackRotMap.put(index, bone);
                        else rightTrackRotMap.put(index, bone);
                    } else {
                        if (isL) leftTrackMoveMap.put(index, bone);
                        else rightTrackMoveMap.put(index, bone);
                    }
                }

                var flareMatcher = FLARE_PATTERN.matcher(name);
                if (flareMatcher.matches()) {
                    flareBones.add(bone);
                }

                var dogTagMatcher = DOG_TAG_PATTERN.matcher(name);
                if (dogTagMatcher.matches()) {
                    dogTagBones.add(bone);
                }
            }

            shellBones.addAll(shellMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList());
            leftTrackMove.addAll(leftTrackMoveMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList());
            leftTrackRot.addAll(leftTrackRotMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList());
            rightTrackMove.addAll(rightTrackMoveMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList());
            rightTrackRot.addAll(rightTrackRotMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList());
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
}

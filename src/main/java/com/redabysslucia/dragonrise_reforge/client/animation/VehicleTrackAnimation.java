package com.redabysslucia.dragonrise_reforge.client.animation;

import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BakedModelInstance;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.google.gson.Gson;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/** Arc-length track loops in model pixels, shared by the vehicles with rebuilt running belts. */
public final class VehicleTrackAnimation {
    private static final Gson GSON = new Gson();
    private static final Map<String, Profile> PROFILES = new HashMap<>();
    private static int generation;

    private VehicleTrackAnimation() { }

    public static void reload(ResourceManager resources) {
        PROFILES.clear();
        generation++;
    }

    public static int generation() {
        return generation;
    }

    public static Profile profile(String id) {
        return PROFILES.computeIfAbsent(id, key -> {
            ResourceLocation location = new ResourceLocation(Dragonrise_reforge.MODID, "tracks/" + key + ".json");
            try (Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(location)) {
                return read(reader);
            } catch (Exception exception) {
                Dragonrise_reforge.LOGGER.error("Cannot load vehicle track path {}", location, exception);
                return new Profile(new Loop[0]);
            }
        });
    }

    public static Profile read(Reader reader) {
        Profile profile = GSON.fromJson(reader, Profile.class);
        if (profile == null || profile.loops == null) throw new IllegalArgumentException("Missing track loops");
        for (Loop loop : profile.loops) {
            if (!("L".equals(loop.side) || "R".equals(loop.side)) || loop.first < 0 || loop.links < 1
                    || !Float.isFinite(loop.length) || loop.length <= 0 || !Float.isFinite(loop.radius) || loop.radius <= 0
                    || loop.points == null || loop.points.length < 2) {
                throw new IllegalArgumentException("Invalid track loop");
            }
            for (float[] point : loop.points) {
                if (point == null || point.length != 3 || !Float.isFinite(point[0])
                        || !Float.isFinite(point[1]) || !Float.isFinite(point[2])) {
                    throw new IllegalArgumentException("Invalid track sample");
                }
            }
        }
        return profile;
    }

    public record Profile(Loop[] loops) { }
    public record Loop(String side, int first, int links, float length, float radius, float[][] points) { }

    /** Resolves bone names once per model instance, including alternate skin models. */
    public static final class Rig {
        private final LoopBones[] loops;

        public Rig(BakedModelInstance model, Profile profile) {
            loops = new LoopBones[profile.loops.length];
            for (int i = 0; i < loops.length; i++) loops[i] = new LoopBones(model, profile.loops[i]);
        }

        public void apply(float leftWheelRot, float rightWheelRot) {
            for (LoopBones bones : loops) {
                // Superb rotates its wheels by 1.5 * wheelRot radians; the top path runs towards -Z.
                float wheel = "L".equals(bones.loop.side) ? leftWheelRot : rightWheelRot;
                bones.apply(-1.5f * wheel * bones.loop.radius);
            }
        }
    }

    private static final class LoopBones {
        final Loop loop;
        final BoneState[] move;
        final BoneState[] rotate;

        LoopBones(BakedModelInstance model, Loop loop) {
            this.loop = loop;
            move = new BoneState[loop.links];
            rotate = new BoneState[loop.links];
            for (int i = 0; i < loop.links; i++) {
                move[i] = model.getBone("trackMov" + loop.side + (loop.first + i));
                rotate[i] = model.getBone("trackRot" + loop.side + (loop.first + i));
            }
        }

        void apply(float phase) {
            float pitch = loop.length / loop.links;
            for (int i = 0; i < loop.links; i++) {
                BoneState bone = move[i];
                if (bone == null || rotate[i] == null) continue; // Alternate LOD may omit the running belt.
                float distance = ((phase + pitch * i) % loop.length + loop.length) % loop.length;
                float u = distance / loop.length * (loop.points.length - 1);
                int from = Math.min((int) u, loop.points.length - 2);
                float fraction = u - from;
                float[] a = loop.points[from], b = loop.points[from + 1];
                var definition = bone.definition();
                bone.y = definition.bindY() - definition.pivotY() * 16f + Mth.lerp(fraction, a[0], b[0]);
                bone.z = definition.bindZ() - definition.pivotZ() * 16f + Mth.lerp(fraction, a[1], b[1]);
                rotate[i].rotation.rotationX(Mth.rotLerp(fraction, a[2], b[2]) * Mth.DEG_TO_RAD);
            }
        }
    }
}

package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.atsuishio.superbwarfare.data.vehicle_skin.SkinInfo;
import com.atsuishio.superbwarfare.data.vehicle_skin.VehicleSkin;
import com.atsuishio.superbwarfare.entity.vehicle.VehicleModelEntry;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.resource.model.VehicleModelReloadListener;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 载具渲染器基类：在 superb 的 {@link GeoVehicleRenderer} 之上补上「皮肤自带模型 / 皮肤自带发光贴图」。
 *
 * <p>superb 的皮肤数据（SkinInfo）只有 Texture 一个字段，换涂装时仅替换贴图，既不能换模型也没有
 * 发光贴图。本类按命名约定把这两项补齐（约定与涂装贴图 {@code {载具id}_{涂装id}.png} 保持一致）：
 * <ul>
 *   <li>皮肤模型：{@code dragonrise_reforge:models/bedrock/vehicle/{载具id}_{涂装id}.geo.json}</li>
 *   <li>发光贴图：{@code dragonrise_reforge:textures/entity/{载具id}_{涂装id}_glow.png}</li>
 * </ul>
 * 两个文件任一缺失（或该实体没换涂装）时，渲染完全走 superb 原逻辑，因此对没有这套文件的载具 /
 * 涂装零影响。皮肤模型必须与基础模型使用相同的骨骼名，动画才会照常生效——superb 的动画姿态是按
 * 骨骼名混合到"正在渲染的那个模型实例"上的（GeoVehicleRenderer 里
 * {@code instance.applyPose(BLENDER.blend(instance.bindPose, ani.getPose()))}）。
 *
 * <p>注意：换用皮肤模型后不再走 LOD 模型（皮肤模型本身即最高细节），远距离也保持该细节。
 */
public class DragonriseVehicleRenderer<T extends VehicleEntity> extends GeoVehicleRenderer<T> {

    /** 皮肤 id 后缀：发光贴图 */
    private static final String GLOW_SUFFIX = "_glow";

    /**
     * 每个实体一份皮肤模型实例：模型实例内部保存姿态，不能跨实体共享。
     * 仅在渲染线程访问，用 WeakHashMap 随实体回收。
     */
    private static final Map<VehicleEntity, CachedSkinModel> SKIN_MODEL_CACHE = new WeakHashMap<>();

    private static final class CachedSkinModel {
        final String skinId;
        /** 烘焙后的模型对象身份，用于识别资源重载 / 模型文件更新 */
        @Nullable
        final Object baked;
        @Nullable
        final VehicleModelEntry entry;

        CachedSkinModel(String skinId, @Nullable Object baked, @Nullable VehicleModelEntry entry) {
            this.skinId = skinId;
            this.baked = baked;
            this.entry = entry;
        }
    }

    public DragonriseVehicleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public VehicleModelEntry getCurrentModelEntry(PoseStack poseStack, T entity) {
        VehicleModelEntry skinEntry = getSkinModelEntry(entity);
        if (skinEntry != null) {
            return skinEntry;
        }
        return super.getCurrentModelEntry(poseStack, entity);
    }

    @Override
    public ResourceLocation getEmissiveTextureLocation(PoseStack poseStack, T entity) {
        VehicleModelEntry skinEntry = getSkinModelEntry(entity);
        if (skinEntry != null && skinEntry.getEmissiveTexture() != null) {
            return skinEntry.getEmissiveTexture();
        }
        return super.getEmissiveTextureLocation(poseStack, entity);
    }

    /**
     * 取当前涂装自带的模型条目；没有对应模型文件或没换涂装时返回 null（走 superb 原逻辑）。
     */
    @Nullable
    private VehicleModelEntry getSkinModelEntry(T entity) {
        String skinId = entity.getSkinId();
        if (skinId == null || skinId.isBlank()) {
            return null;
        }

        String vehicleId = net.minecraft.world.entity.EntityType.getKey(entity.getType()).getPath();
        ResourceLocation modelPath = new ResourceLocation(Dragonrise_reforge.MODID,
                "models/bedrock/vehicle/" + vehicleId + "_" + skinId + ".geo.json");
        var baked = VehicleModelReloadListener.INSTANCE.getModel(modelPath);

        CachedSkinModel cached = SKIN_MODEL_CACHE.get(entity);
        if (cached != null && cached.skinId.equals(skinId) && cached.baked == baked) {
            return cached.entry;   // 涂装未变、模型也未被资源重载替换
        }

        if (baked == null) {
            SKIN_MODEL_CACHE.put(entity, new CachedSkinModel(skinId, null, null));
            return null;           // 该涂装没有自带模型 -> 走 superb 原逻辑
        }

        // 皮肤贴图：优先用皮肤数据里的 Texture，取不到时按命名约定推导
        SkinInfo skinInfo = VehicleSkin.getSkin(entity);
        ResourceLocation texture = skinInfo == null ? null : ResourceLocation.tryParse(skinInfo.getTexture());
        if (texture == null) {
            texture = new ResourceLocation(Dragonrise_reforge.MODID,
                    "textures/entity/" + vehicleId + "_" + skinId + ".png");
        }

        ResourceLocation glow = new ResourceLocation(Dragonrise_reforge.MODID,
                "textures/entity/" + vehicleId + "_" + skinId + GLOW_SUFFIX + ".png");
        if (!textureExists(glow)) {
            glow = null;   // 没有发光贴图就按无发光渲染，避免紫黑格子
        }

        VehicleModelEntry entry = new VehicleModelEntry(new VehicleModelInstance(baked), texture, glow, 0);
        SKIN_MODEL_CACHE.put(entity, new CachedSkinModel(skinId, baked, entry));
        return entry;
    }

    /** 贴图是否存在于当前资源包（缺失时不要交给渲染层，否则会显示为缺失贴图） */
    private static boolean textureExists(ResourceLocation location) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null && minecraft.getResourceManager().getResource(location).isPresent();
    }
}

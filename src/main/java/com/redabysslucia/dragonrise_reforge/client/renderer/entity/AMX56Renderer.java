package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.atsuishio.superbwarfare.entity.vehicle.VehicleModelEntry;
import com.atsuishio.superbwarfare.resource.model.VehicleModelReloadListener;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.entities.AMX56Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * AMX56：北约涂装（{@code skinId = "nato"}）使用**专用模型** {@code amx56nato.geo.json}。
 * <p>
 * 卓越前线的皮肤系统只能换贴图（{@code SkinInfo} 只有 Id/Name/Description/Texture/Priority，
 * {@code VehicleModelPojo} 也只有 Model/Texture/EmissiveTexture/LODDistance），所以"带独立模型的涂装"
 * 必须在渲染端自己处理：这里覆写 {@link GeoVehicleRenderer#getCurrentModelEntry}，在 nato 皮肤时换成
 * 另一份模型（贴图同时用该模型配套的 {@code amx56nato.png}）。
 * <p>
 * 模型由卓越前线的 {@link VehicleModelReloadListener} 按资源路径加载，所以
 * {@code amx56nato.geo.json} 只要放在 {@code models/bedrock/vehicle/} 下即可，不需要额外的载具 JSON 条目
 * （旧的 {@code assets/superbwarfare/sbw/vehicles/amx56nato.json} 是换 SBM 模型之前的残留，已无用）。
 * <p>
 * 注意：这份老模型与新模型骨骼名不完全一致（老模型用 {@code leclerc/body/main2}，新模型用
 * {@code root/base/main}），所以 {@code root} 与 {@code base} 相关的处理（残骸隐藏、车体后坐晃动）对它不生效；
 * {@code turret}、{@code barrel}、车轮骨骼与 {@code passengerWeaponStation} 名字一致，炮塔、车轮、武器站仍会正常动。
 */
public class AMX56Renderer extends GeoVehicleRenderer<AMX56Entity> {

    /** 触发换模型的皮肤 id（见 data/dragonrise_reforge/sbw/vehicle_skins/amx56.json） */
    private static final String NATO_SKIN = "nato";
    /** 北约涂装专用模型 */
    private static final ResourceLocation NATO_MODEL =
            new ResourceLocation("dragonrise_reforge", "models/bedrock/vehicle/amx56nato.geo.json");
    /** 北约涂装专用贴图 */
    private static final ResourceLocation NATO_TEXTURE =
            new ResourceLocation("dragonrise_reforge", "textures/entity/amx56nato.png");

    private VehicleModelEntry natoEntry;
    private boolean natoUnavailable;

    public AMX56Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public VehicleModelEntry getCurrentModelEntry(PoseStack poseStack, AMX56Entity entity) {
        if (NATO_SKIN.equals(entity.getSkinId())) {
            VehicleModelEntry entry = natoEntry();
            if (entry != null) {
                return entry;
            }
        }
        return super.getCurrentModelEntry(poseStack, entity);
    }

    /** 惰性构建北约模型条目（模型缺失时退回默认模型，不影响渲染） */
    private VehicleModelEntry natoEntry() {
        if (natoEntry == null && !natoUnavailable) {
            var model = VehicleModelReloadListener.INSTANCE.getModel(NATO_MODEL);
            if (model == null) {
                natoUnavailable = true;
                return null;
            }
            natoEntry = new VehicleModelEntry(new VehicleModelInstance(model), NATO_TEXTURE, null, 0);
        }
        return natoEntry;
    }
}

package com.redabysslucia.dragonrise_reforge.client.renderer.armor;

import com.github.mcmodderanchor.simplebedrockmodel.v2.client.renderer.GeoArmorRendererV2;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneDefinition;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.TreeArmorModelInstance;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.tree.TreeBedrockModel;
import com.redabysslucia.dragonrise_reforge.resource.model.ArmorModelReloadListener;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * 护甲「穿在身上」的 SBM v2 渲染器，写法与 SuperbWarfare 对齐
 * （SBW 的护甲物品直接 {@code new GeoArmorRendererV2(model, slot, texture)} + {@code preparePose}）。
 *
 * <p>相比直接内联，这里多做了两件事，避免 38 份重复代码和两个真实缺陷：
 * <ul>
 *   <li><b>按 geo 缓存</b>：同一件护甲只创建一个渲染器实例；</li>
 *   <li><b>资源重载感知</b>：{@link ArmorModelReloadListener} 重载后会产出新的
 *       {@link TreeBedrockModel} 实例，此处检测到实例变化就重建渲染器，
 *       否则 F3+T 之后护甲仍渲染旧模型（SBW 的内联写法就是固定缓存，没有这层处理）。</li>
 * </ul>
 *
 * <p>模型缺失（资源未加载完 / 贴图包没提供）时 {@link #get} 返回 {@code null}，
 * 调用方回退到原版护甲模型而不是崩溃。
 *
 * <h3>关于 {@link #preparePose} 的补偿</h3>
 * SBM 的 {@code TreeArmorModelInstance.preparePose} 是按<b>硬编码静息偏移</b>做增量累加的
 * （head/body 0/24/0、手臂 ±5/22/0、腿 ±1.9/12/0），而 1.21.1 原版
 * {@code HumanoidModel.createMesh(..., 0.0F)} 里 body/head 的部件位置是 (0,0,0)、
 * 手臂是 (±5,2,0)、腿是 (±1.9,12,0)。两边静息基准不一致时，静止姿态下就会残留一个常量偏移，
 * 表现就是「胸甲飘到头上去」。
 *
 * <p>这里在 super 之后再按 <b>bind + (部件位置 − 原版静息位置)</b> 重设一次：
 * 静止时护甲严格落在 geo 作者坐标，动画时只跟随原版部件的增量。
 *
 * <p>开关：启动参数 {@code -Ddragonrise.armor.compensate=false} 可关闭补偿，方便 A/B 对比。
 */
public class SbmArmorRenderer extends GeoArmorRendererV2 {

    /**
     * 是否启用静息偏移补偿（默认关闭）。
     *
     * <p>实测结论：打开后所有护甲整体下沉 24 单位 —— 说明 SBM 默认的静息偏移
     * （head/body 24、手臂 22、腿 12）对 y 方向本来就是对的，补偿等于把这个常量抵消掉。
     * 因此默认关闭；排查"胸甲飘到头上"应从<b>枢轴/bind</b>（geo 数据）入手，
     * 而不是改静的语义。
     *
     * <p>保留开关是为了后续调试：{@code -Ddragonrise.armor.compensate=true} 打开，
     * 配合 {@code -Ddragonrise.armor.yCalib=<单位>} 微调 y。
     */
    private static final boolean COMPENSATE =
            "true".equalsIgnoreCase(System.getProperty("dragonrise.armor.compensate", "false"));

    /** y 方向微调（模型单位，正数=向下）。仅在 COMPENSATE 打开时生效。 */
    private static final float Y_CALIB = parseFloat(System.getProperty("dragonrise.armor.yCalib", "0"));

    private static float parseFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return 0.0F;
        }
    }

    private static final Map<String, SbmArmorRenderer> CACHE = new HashMap<>();

    /** 同一份 geo 可能被头盔/胸甲/护腿共用；缓存键必须带槽位，否则三件同穿会每帧重建渲染器 */
    private static String cacheKey(ResourceLocation geoPath, EquipmentSlot slot) {
        return geoPath + "|" + slot.getName();
    }

    private final ResourceLocation geoPath;
    private final TreeBedrockModel cachedModel;
    private final EquipmentSlot slot;

    private SbmArmorRenderer(ResourceLocation geoPath, TreeBedrockModel model, EquipmentSlot slot, ResourceLocation texture) {
        super(model, slot, texture);
        this.geoPath = geoPath;
        this.cachedModel = model;
        this.slot = slot;
    }

    /**
     * @param geoPath 形如 {@code dragonrise_reforge:models/bedrock/armor/cn21helmet.geo.json}
     * @param slot    该护甲部件对应的槽位
     * @param texture 形如 {@code dragonrise_reforge:textures/armor/cn21helmet.png}
     * @return 渲染器；模型尚未加载时返回 {@code null}
     */
    @Nullable
    public static SbmArmorRenderer get(ResourceLocation geoPath, EquipmentSlot slot, ResourceLocation texture) {
        TreeBedrockModel model = ArmorModelReloadListener.INSTANCE.getModel(geoPath);
        if (model == null) {
            return null;
        }

        SbmArmorRenderer cached = CACHE.get(cacheKey(geoPath, slot));
        if (cached != null && cached.cachedModel == model) {
            return cached;
        }

        SbmArmorRenderer created = new SbmArmorRenderer(geoPath, model, slot, texture);
        CACHE.put(cacheKey(geoPath, slot), created);
        return created;
    }

    @Override
    public void preparePose(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        super.preparePose(livingEntity, itemStack, equipmentSlot, original);
        if (!COMPENSATE) {
            return;
        }

        TreeArmorModelInstance instance = this.getInstance();
        if (instance == null) {
            return;
        }

        // x/z 用 SBM 自己的（镜像空间：右臂 +5、右腿 +1.9），
        // y 用 1.21.1 原版部件的静息高度（createMesh(..., 0.0F)）。
        reseat(instance.getArmorHead(), original.head, 0.0F, 0.0F, 0.0F);
        reseat(instance.getArmorBody(), original.body, 0.0F, 0.0F, 0.0F);
        reseat(instance.getArmorRightArm(), original.rightArm, 5.0F, 2.0F, 0.0F);
        reseat(instance.getArmorLeftArm(), original.leftArm, -5.0F, 2.0F, 0.0F);
        reseat(instance.getArmorRightLeg(), original.rightLeg, 1.9F, 12.0F, 0.0F);
        reseat(instance.getArmorLeftLeg(), original.leftLeg, -1.9F, 12.0F, 0.0F);
        reseat(instance.getArmorRightBoot(), original.rightLeg, 1.9F, 12.0F, 0.0F);
        reseat(instance.getArmorLeftBoot(), original.leftLeg, -1.9F, 12.0F, 0.0F);

        // 重新按槽位设置可见性（copyModelPart 里会把 visible 覆盖成部件自身的可见性）
        instance.setVisibilityBySlot(equipmentSlot);
    }

    /**
     * 把骨骼重设到「bind + 相对原版静息位置的增量」。
     * 旋转/缩放沿用 SBM 自己的处理方式（x/y 缩放取负，与烘焙端的 x/y 镜像一致）。
     */
    private static void reseat(@Nullable BoneState bone, @Nullable ModelPart part,
                               float restX, float restY, float restZ) {
        if (bone == null || part == null) {
            return;
        }
        BoneDefinition def = bone.definition();
        bone.x = def.bindX() + (part.x - restX);
        bone.y = def.bindY() + (part.y - restY);
        bone.z = def.bindZ() + (part.z - restZ);

        bone.rotation.rotationZYX(part.zRot, part.yRot, part.xRot);

        bone.xScale = -part.xScale;
        bone.yScale = -part.yScale;
        bone.zScale = part.zScale;
        bone.visible = part.visible;
    }

    public ResourceLocation getGeoPath() {
        return this.geoPath;
    }

    /**
     * SBM v2 默认是 {@code armorCutoutNoCull}，但那会把贴图里的半透明像素渲染成不透明。
     * DRR 有 15 张护甲贴图含半透明像素（chinese_desert / cnchest / desert07_* / jungle07* /
     * kr06_* / ocean07_* / russian_soldier / ukrainian_soldier），沿用原 GeckoLib 版的
     * {@code entityTranslucent} 以保持观感一致。
     */
    @Override
    public RenderType getRenderType(ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}

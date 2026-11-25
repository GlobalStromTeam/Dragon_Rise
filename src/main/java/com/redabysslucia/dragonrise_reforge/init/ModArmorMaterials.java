package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;

/**
 * 自定义护甲材质：整体对齐下界合金套，只有头盔额外 +4 护甲值。
 *
 * <table>
 *   <tr><th></th><th>护甲</th><th>耐久</th></tr>
 *   <tr><td>头盔</td><td>7（下界合金 3 + 4）</td><td>407（11×37）</td></tr>
 *   <tr><td>胸甲</td><td>8</td><td>592（16×37）</td></tr>
 *   <tr><td>护腿</td><td>6</td><td>555（15×37）</td></tr>
 *   <tr><td>靴子</td><td>3</td><td>481（13×37）</td></tr>
 * </table>
 *
 * 韧性 3.0、击退抗性 0.1、附魔能力 15、修复材料下界合金锭 —— 与 {@code ArmorMaterials.NETHERITE} 一致。
 */
public final class ModArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> REGISTRY =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, Dragonrise_reforge.MODID);

    /** 下界合金的耐久系数（原版 37） */
    public static final int NETHERITE_DURABILITY_FACTOR = 37;

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> NETHERITE_HEAVY = REGISTRY.register(
            "netherite_heavy",
            () -> new ArmorMaterial(
                    Map.of(
                            ArmorItem.Type.HELMET, 7,
                            ArmorItem.Type.CHESTPLATE, 8,
                            ArmorItem.Type.LEGGINGS, 6,
                            ArmorItem.Type.BOOTS, 3,
                            ArmorItem.Type.BODY, 8),
                    15,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(Items.NETHERITE_INGOT),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("netherite"))),
                    3.0F,
                    0.1F));

    /**
     * 重装组 A —— HAOM / ADE3 / SAWKM-1 三套共用。
     *
     * <p>韧性 5.0、击退抗性 1.0（满值）；护甲 头盔 4（物品侧再叠 +0.5 = 4.5）/ 胸甲 11 / 护腿 8。
     * 子弹抗性由各物品 {@code getDefaultAttributeModifiers} 叠加（满耐久 0.5，随损伤线性衰减）。
     */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HEAVY_A = REGISTRY.register(
            "heavy_a",
            () -> new ArmorMaterial(
                    Map.of(
                            ArmorItem.Type.HELMET, 4,
                            ArmorItem.Type.CHESTPLATE, 11,
                            ArmorItem.Type.LEGGINGS, 8,
                            ArmorItem.Type.BOOTS, 3,
                            ArmorItem.Type.BODY, 11),
                    15,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(Items.NETHERITE_INGOT),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("netherite"))),
                    5.0F,
                    1.0F));

    /**
     * 重装组 B —— JUAW 专用。
     *
     * <p>韧性 6.0、击退抗性 1.0；护甲 头盔 6 / 胸甲 13 / 护腿 10；子弹抗性满耐久 0.6。
     */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HEAVY_B = REGISTRY.register(
            "heavy_b",
            () -> new ArmorMaterial(
                    Map.of(
                            ArmorItem.Type.HELMET, 6,
                            ArmorItem.Type.CHESTPLATE, 13,
                            ArmorItem.Type.LEGGINGS, 10,
                            ArmorItem.Type.BOOTS, 4,
                            ArmorItem.Type.BODY, 13),
                    15,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(Items.NETHERITE_INGOT),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("netherite"))),
                    6.0F,
                    1.0F));

    /**
     * 重装组 C —— HONF 专用。
     *
     * <p>韧性 2.0、击退抗性 1.0；护甲 头盔 3 / 胸甲 8 / 护腿 6；子弹抗性满耐久 0.5。
     */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HEAVY_C = REGISTRY.register(
            "heavy_c",
            () -> new ArmorMaterial(
                    Map.of(
                            ArmorItem.Type.HELMET, 3,
                            ArmorItem.Type.CHESTPLATE, 8,
                            ArmorItem.Type.LEGGINGS, 6,
                            ArmorItem.Type.BOOTS, 2,
                            ArmorItem.Type.BODY, 8),
                    15,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(Items.NETHERITE_INGOT),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("netherite"))),
                    2.0F,
                    1.0F));

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

    private ModArmorMaterials() {
    }
}

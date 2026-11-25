package com.redabysslucia.dragonrise_reforge.init;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.Map;

/**
 * 自定义护甲材质（1.20.1 Forge：{@link ArmorMaterial} 是接口，直接持静态实例即可，没有独立注册表）。
 *
 * <p>三套重装材质，数值按套系区分：
 *
 * <table>
 *   <tr><th>材质</th><th>归属</th><th>护甲 头/胸/腿</th><th>韧性</th><th>击退抗性</th></tr>
 *   <tr><td>{@link #HEAVY_A}</td><td>HAOM / ADE3 / SAWKM-1</td><td>4（+0.5 头盔附加）/ 11 / 8</td><td>5.0</td><td>1.0</td></tr>
 *   <tr><td>{@link #HEAVY_B}</td><td>JUAW</td><td>6 / 13 / 10</td><td>6.0</td><td>1.0</td></tr>
 *   <tr><td>{@link #HEAVY_C}</td><td>HONF</td><td>3 / 8 / 6</td><td>2.0</td><td>1.0</td></tr>
 * </table>
 *
 * <p>耐久仍由各物品在 {@code Properties#durability} 里显式给出（×37），附魔能力 15，修复材料下界合金锭。
 * 子弹抗性（Superb Warfare 的 {@code BULLET_RESISTANCE}）不在这里，由各物品的
 * {@code getAttributeModifiers} 叠加（满耐久 0.5 / JUAW 0.6，随损伤线性衰减）。
 */
public final class ModArmorMaterials {

    /** 下界合金的耐久系数（原版 37） */
    public static final int DURABILITY_FACTOR = 37;

    /** 各部位耐久基数（下界合金同款：头 11 / 胸 16 / 腿 15 / 靴 13，×37 后为 407 / 592 / 555 / 481）。 */
    private static final Map<ArmorItem.Type, Integer> DURABILITY_BASE = Map.of(
            ArmorItem.Type.HELMET, 11,
            ArmorItem.Type.CHESTPLATE, 16,
            ArmorItem.Type.LEGGINGS, 15,
            ArmorItem.Type.BOOTS, 13);

    /** 组 A —— HAOM / ADE3 / SAWKM-1：韧性 5.0、击退抗性 1.0，护甲 4 / 11 / 8（头盔再叠 +0.5）。 */
    public static final ArmorMaterial HEAVY_A = new Set("heavy_a", 4, 11, 8, 3, 5.0F, 1.0F);

    /** 组 B —— JUAW：韧性 6.0、击退抗性 1.0，护甲 6 / 13 / 10。 */
    public static final ArmorMaterial HEAVY_B = new Set("heavy_b", 6, 13, 10, 4, 6.0F, 1.0F);

    /** 组 C —— HONF：韧性 2.0、击退抗性 1.0，护甲 3 / 8 / 6。 */
    public static final ArmorMaterial HEAVY_C = new Set("heavy_c", 3, 8, 6, 2, 2.0F, 1.0F);

    private ModArmorMaterials() {
    }

    /** {@link ArmorMaterial} 的最小实现：部位护甲值 + 韧性 + 击退抗性。 */
    private static final class Set implements ArmorMaterial {

        private final String name;
        private final Map<ArmorItem.Type, Integer> defense;
        private final float toughness;
        private final float knockbackResistance;

        private Set(String name, int helmet, int chestplate, int leggings, int boots,
                    float toughness, float knockbackResistance) {
            this.name = name;
            EnumMap<ArmorItem.Type, Integer> map = new EnumMap<>(ArmorItem.Type.class);
            map.put(ArmorItem.Type.HELMET, helmet);
            map.put(ArmorItem.Type.CHESTPLATE, chestplate);
            map.put(ArmorItem.Type.LEGGINGS, leggings);
            map.put(ArmorItem.Type.BOOTS, boots);
            this.defense = map;
            this.toughness = toughness;
            this.knockbackResistance = knockbackResistance;
        }

        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
            return DURABILITY_BASE.getOrDefault(type, 0) * DURABILITY_FACTOR;
        }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return this.defense.getOrDefault(type, 0);
        }

        @Override
        public int getEnchantmentValue() {
            return 15;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_NETHERITE;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.NETHERITE_INGOT);
        }

        /** 原版兜底渲染层名，沿用下界合金贴图（实际渲染走 SBM 自定义模型）。 */
        @Override
        public String getName() {
            return "netherite";
        }

        @Override
        public float getToughness() {
            return this.toughness;
        }

        @Override
        public float getKnockbackResistance() {
            return this.knockbackResistance;
        }
    }
}

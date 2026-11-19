/**
 * 批量生成护甲物品类 + 注册 + 创造栏 + 语言 + 物品模型 + 装配配方
 * 用法：node tools/gen_armor_set.mjs
 */
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';

const ROOT = 'E:/Dragon_Rise/Dragon_Rise_1211';
const JAVA_DIR = path.join(ROOT, 'src/main/java/com/redabysslucia/dragonrise_reforge/item/armor');
const MOD_ITEMS = path.join(ROOT, 'src/main/java/com/redabysslucia/dragonrise_reforge/init/ModItems.java');
const MOD_TABS = path.join(ROOT, 'src/main/java/com/redabysslucia/dragonrise_reforge/init/ModTabs.java');
const ASSETS = path.join(ROOT, 'src/main/resources/assets/dragonrise_reforge');
const DATA_RECIPE = path.join(ROOT, 'src/main/resources/data/dragonrise_reforge/recipe');
const ARMOR_TEX_DIR = path.join(ASSETS, 'textures/armor');

const SETS = [
  { key: 'haom', cap: 'Haom', zh: 'HAOM', en: 'HAOM' },
  { key: 'juaw', cap: 'Juaw', zh: 'JUAW', en: 'JUAW' },
  { key: 'ade3', cap: 'Ade3', zh: 'ADE3', en: 'ADE3' },
  { key: 'sawkm1', cap: 'Sawkm1', zh: 'SAWKM-1', en: 'SAWKM-1' },
  { key: 'honf', cap: 'Honf', zh: 'HONF', en: 'HONF' },
];

/** 贴图共用：内容完全相同的多个模型只引用第一份贴图文件 */
{
  const byHash = new Map();
  for (const set of SETS) {
    const p = path.join(ARMOR_TEX_DIR, `${set.key}.png`);
    if (!fs.existsSync(p)) {
      set.tex = set.key;
      continue;
    }
    const h = crypto.createHash('sha256').update(fs.readFileSync(p)).digest('hex');
    if (!byHash.has(h)) byHash.set(h, set.key);
    set.tex = byHash.get(h);
  }
}

const SLOTS = [
  { key: 'helmet', cap: 'Helmet', type: 'Type.HELMET', zh: '头盔', en: 'Helmet', kevlar: 5, defense: 7 },
  { key: 'chest', cap: 'Chest', type: 'Type.CHESTPLATE', zh: '作战服', en: 'Chestplate', kevlar: 8, defense: 8 },
  { key: 'pants', cap: 'Pants', type: 'Type.LEGGINGS', zh: '护腿', en: 'Leggings', kevlar: 7, defense: 6 },
];

const javaClass = (set, slot) => `package com.redabysslucia.dragonrise_reforge.item.armor;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.init.ModAttributes;
import com.redabysslucia.dragonrise_reforge.init.ModArmorMaterials;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;
import com.redabysslucia.dragonrise_reforge.client.renderer.armor.SbmArmorItemRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.armor.SbmArmorRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * ${set.cap} 护甲（${slot.zh}）。整套模型共用同一份 geo：{@code models/bedrock/armor/${set.key}.geo.json}，
 * 由 SBM 的按槽位可见性（armorHead / armorBody+双臂 / 双腿+boots）决定显示哪一部分。
 *
 * <p>数值对齐下界合金：护甲 ${slot.defense}、韧性 3.0、击退抗性 0.1、附魔能力 15、
 * 耐久 ${slot.type}.getDurability(37)。
 */
public class ${set.cap}${slot.cap} extends ArmorItem {
    /** SBM armor model: assets/dragonrise_reforge/models/bedrock/armor/${set.key}.geo.json */
    public static final ResourceLocation GEO = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "models/bedrock/armor/${set.key}.geo.json");
    /** SBM armor texture: assets/dragonrise_reforge/textures/armor/${set.tex}.png${set.tex !== set.key ? `（与其他模型共用贴图，见 ${set.tex}.png）` : ''} */
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/armor/${set.tex}.png");

    public ${set.cap}${slot.cap}() {
        super(ModArmorMaterials.NETHERITE_HEAVY, ${slot.type},
                new Properties().durability(${slot.type}.getDurability(ModArmorMaterials.NETHERITE_DURABILITY_FACTOR)));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                SbmArmorRenderer renderer = SbmArmorRenderer.get(GEO, equipmentSlot, TEXTURE);
                if (renderer == null) {
                    return original;
                }
                renderer.preparePose(livingEntity, itemStack, equipmentSlot, original);
                return renderer;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return SbmArmorItemRenderer.get(GEO, TEXTURE);
            }
        });
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers base = super.getDefaultAttributeModifiers(stack);
        double factor = 0.5 * Math.max(0, 1 - (double) stack.getDamageValue() / stack.getMaxDamage());
        return base.withModifierAdded(
                ModAttributes.BULLET_RESISTANCE,
                new AttributeModifier(Mod.ATTRIBUTE_MODIFIER, factor, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.bySlot(this.getEquipmentSlot()));
    }

}
`;

// 1) Java 类
for (const set of SETS) {
  for (const slot of SLOTS) {
    fs.writeFileSync(path.join(JAVA_DIR, `${set.cap}${slot.cap}.java`), javaClass(set, slot), 'utf8');
  }
}

// 2) ModItems 注册
let items = fs.readFileSync(MOD_ITEMS, 'utf8');
const anchor = 'public static final DeferredHolder<Item, Item> RUSSIAN_SOLDIER_PANTS = REGISTRY.register("russian_soldier_pants", RussianSoldierPants::new);';
if (!items.includes(anchor)) throw new Error('ModItems 锚点未找到');
if (!items.includes(`${SETS[0].key.toUpperCase()}_${SLOTS[0].key.toUpperCase()} = REGISTRY`)) {
  const lines = [];
  for (const set of SETS) {
    for (const slot of SLOTS) {
      lines.push(`    public static final DeferredHolder<Item, Item> ${set.key.toUpperCase()}_${slot.key.toUpperCase()} = REGISTRY.register("${set.key}_${slot.key}", ${set.cap}${slot.cap}::new);`);
    }
  }
  items = items.replace(anchor, anchor + '\n' + lines.join('\n'));
  fs.writeFileSync(MOD_ITEMS, items, 'utf8');
}

// 3) ModTabs
let tabs = fs.readFileSync(MOD_TABS, 'utf8');
const tabAnchor = 'output.accept(ModItems.RUSSIAN_SOLDIER_PANTS.get());';
if (!tabs.includes(tabAnchor)) throw new Error('ModTabs 锚点未找到');
if (!tabs.includes(`ModItems.${SETS[0].key.toUpperCase()}_${SLOTS[0].key.toUpperCase()}.get()`)) {
  const tabLines = [];
  for (const set of SETS) {
    for (const slot of SLOTS) {
      tabLines.push(`        output.accept(ModItems.${set.key.toUpperCase()}_${slot.key.toUpperCase()}.get());`);
    }
  }
  tabs = tabs.replace(tabAnchor, tabAnchor + '\n' + tabLines.join('\n'));
  fs.writeFileSync(MOD_TABS, tabs, 'utf8');
}

// 4) 语言
function patchLang(file, pick) {
  const p = path.join(ASSETS, 'lang', file);
  const j = JSON.parse(fs.readFileSync(p, 'utf8'));
  for (const set of SETS) {
    for (const slot of SLOTS) {
      j[`item.dragonrise_reforge.${set.key}_${slot.key}`] = pick(set, slot);
    }
  }
  fs.writeFileSync(p, JSON.stringify(j, null, 2) + '\n', 'utf8');
}
patchLang('zh_cn.json', (set, slot) => `${set.zh} 士兵${slot.zh}`);
patchLang('en_us.json', (set, slot) => `${set.en} Soldier ${slot.en}`);

// 5) 物品模型
for (const set of SETS) {
  for (const slot of SLOTS) {
    fs.writeFileSync(
      path.join(ASSETS, 'models/item', `${set.key}_${slot.key}.json`),
      JSON.stringify({ parent: 'item/generated', textures: { layer0: `dragonrise_reforge:item/${set.key}_${slot.key}` } }, null, 2) + '\n',
      'utf8'
    );
  }
}

// 6) 装配配方（kevlar）
for (const set of SETS) {
  for (const slot of SLOTS) {
    fs.writeFileSync(
      path.join(DATA_RECIPE, `${set.key}_${slot.key}_assembling.json`),
      JSON.stringify({
        type: 'superbwarfare:vehicle_assembling',
        category: 'misc',
        inputs: [`${slot.kevlar} dragonrise_reforge:kevlar`],
        result: { item: `dragonrise_reforge:${set.key}_${slot.key}` },
      }, null, 2) + '\n',
      'utf8'
    );
  }
}

console.log(`已生成：${SETS.length * 3} 个物品类 / 注册 / 创造栏 / 语言 / 物品模型 / 配方`);

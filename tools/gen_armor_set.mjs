/**
 * 批量生成护甲物品类 + 注册 + 创造栏 + 语言 + 物品模型 + 装配配方
 * 用法：node tools/gen_armor_set.mjs
 *
 * 每套可配置：
 *   key/cap/zh/en    物品 id 前缀、类名前缀、中英文名
 *   material         'netherite'（ModArmorMaterials.NETHERITE_HEAVY，头盔 7 护甲）| 'cement'（SW 胶结硬质合金，老护甲数值）
 *   perSlotGeo       true = 每槽一份 geo（xxx_helmet.geo.json…）；false = 一套共用一份 geo（xxx.geo.json）
 *   durabilityFactor 耐久系数（下界合金 37、老护甲 50）
 *   defense          各槽护甲值（只用于注释；实际值由材质决定）
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

const NETHERITE = {
  material: 'netherite',
  durabilityFactor: 37,
  defense: { helmet: 7, chest: 8, pants: 6 },
};

const SETS = [
  { key: 'haom', cap: 'Haom', zh: 'HAOM', en: 'HAOM', ...NETHERITE },
  { key: 'juaw', cap: 'Juaw', zh: 'JUAW', en: 'JUAW', ...NETHERITE },
  { key: 'ade3', cap: 'Ade3', zh: 'ADE3', en: 'ADE3', ...NETHERITE },
  { key: 'sawkm1', cap: 'Sawkm1', zh: 'SAWKM-1', en: 'SAWKM-1', ...NETHERITE },
  { key: 'honf', cap: 'Honf', zh: 'HONF', en: 'HONF', ...NETHERITE },
  {
    key: 'usa_army',
    cap: 'UsaArmy',
    zh: '美军',
    en: 'US Army',
    material: 'cement',
    perSlotGeo: true,
    durabilityFactor: 50,
    defense: { helmet: 3, chest: 8, pants: 6 },
  },
  {
    key: 'gm',
    cap: 'Gm',
    zh: 'GM',
    en: 'GM',
    material: 'cement',
    perSlotGeo: true,
    texPerSlot: true,
    durabilityFactor: 50,
    defense: { helmet: 3, chest: 8, pants: 6 },
  },
  {
    key: 'british_soldier',
    cap: 'BritishSoldier',
    zh: '英国',
    en: 'British',
    material: 'cement',
    perSlotGeo: true,
    durabilityFactor: 50,
    defense: { helmet: 3, chest: 8, pants: 6 },
  },
  {
    key: 'ukraine_soldier',
    cap: 'UkraineSoldier',
    zh: '乌克兰',
    en: 'Ukrainian',
    material: 'cement',
    perSlotGeo: true,
    durabilityFactor: 50,
    defense: { helmet: 3, chest: 8, pants: 6 },
  },
];

/** 贴图共用：内容完全相同的多套模型只引用第一份贴图文件 */
{
  const byHash = new Map();
  for (const set of SETS) {
    if (set.texPerSlot) { set.tex = null; continue; }   // 分槽贴图：每件用自己的
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
  { key: 'helmet', cap: 'Helmet', type: 'Type.HELMET', zh: '头盔', en: 'Helmet', kevlar: 5 },
  { key: 'chest', cap: 'Chest', type: 'Type.CHESTPLATE', zh: '作战服', en: 'Chestplate', kevlar: 8 },
  { key: 'pants', cap: 'Pants', type: 'Type.LEGGINGS', zh: '护腿', en: 'Leggings', kevlar: 7 },
];

function materialInfo(set) {
  if (set.material === 'cement') {
    return {
      imp: 'import com.atsuishio.superbwarfare.tiers.ModArmorMaterial;\n',
      expr: 'ModArmorMaterial.CEMENTED_CARBIDE',
      doct: `Superb Warfare 胶结硬质合金（老护甲数值）：护甲 ${set.defense.helmet}/${set.defense.chest}/${set.defense.pants}、韧性 4.0、击退抗性 0.05、附魔能力 10、耐久 ×${set.durabilityFactor}`,
    };
  }
  return {
    imp: 'import com.redabysslucia.dragonrise_reforge.init.ModArmorMaterials;\n',
    expr: 'ModArmorMaterials.NETHERITE_HEAVY',
    doct: `对齐下界合金：护甲 ${set.defense.helmet}/${set.defense.chest}/${set.defense.pants}（头盔 +4）、韧性 3.0、击退抗性 0.1、附魔能力 15、耐久 ×${set.durabilityFactor}`,
  };
}

const javaClass = (set, slot) => {
  const mat = materialInfo(set);
  const geoName = `${set.key}${set.perSlotGeo ? '_' + slot.key : ''}`;
  const texName = set.texPerSlot ? `${set.key}_${slot.key}` : set.tex;
  return `package com.redabysslucia.dragonrise_reforge.item.armor;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.init.ModAttributes;
${mat.imp}import net.minecraft.client.model.HumanoidModel;
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
 * ${set.cap} 护甲（${slot.zh}）。SBM 模型：{@code models/bedrock/armor/${geoName}.geo.json}。
 *
 * <p>${mat.doct}；耐久 = ${slot.type}.getDurability(${set.durabilityFactor})。
 */
public class ${set.cap}${slot.cap} extends ArmorItem {
    /** SBM armor model: assets/dragonrise_reforge/models/bedrock/armor/${geoName}.geo.json */
    public static final ResourceLocation GEO = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "models/bedrock/armor/${geoName}.geo.json");
    /** SBM armor texture: assets/dragonrise_reforge/textures/armor/${texName}.png${set.texPerSlot ? `（本槽专用贴图）` : (set.tex !== set.key ? `（与其他模型共用贴图，见 ${set.tex}.png）` : '')} */
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/armor/${texName}.png");

    public ${set.cap}${slot.cap}() {
        super(${mat.expr}, ${slot.type},
                new Properties().durability(${slot.type}.getDurability(${set.durabilityFactor})));
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
};

// 1) Java 类
for (const set of SETS) {
  for (const slot of SLOTS) {
    fs.writeFileSync(path.join(JAVA_DIR, `${set.cap}${slot.cap}.java`), javaClass(set, slot), 'utf8');
  }
}

// 2) ModItems 注册（逐条补，缺哪条加哪条）
{
  const anchor = 'public static final DeferredHolder<Item, Item> RUSSIAN_SOLDIER_PANTS = REGISTRY.register("russian_soldier_pants", RussianSoldierPants::new);';
  let items = fs.readFileSync(MOD_ITEMS, 'utf8');
  if (!items.includes(anchor)) throw new Error('ModItems 锚点未找到');
  const missing = [];
  for (const set of SETS) {
    for (const slot of SLOTS) {
      const field = `${set.key.toUpperCase()}_${slot.key.toUpperCase()}`;
      if (items.includes(`${field} = REGISTRY`)) continue;
      missing.push(`    public static final DeferredHolder<Item, Item> ${field} = REGISTRY.register("${set.key}_${slot.key}", ${set.cap}${slot.cap}::new);`);
    }
  }
  if (missing.length) {
    items = items.replace(anchor, anchor + '\n' + missing.join('\n'));
    fs.writeFileSync(MOD_ITEMS, items, 'utf8');
  }
}

// 3) ModTabs（同样逐条补）
{
  const tabAnchor = 'output.accept(ModItems.RUSSIAN_SOLDIER_PANTS.get());';
  let tabs = fs.readFileSync(MOD_TABS, 'utf8');
  if (!tabs.includes(tabAnchor)) throw new Error('ModTabs 锚点未找到');
  const missing = [];
  for (const set of SETS) {
    for (const slot of SLOTS) {
      const field = `${set.key.toUpperCase()}_${slot.key.toUpperCase()}`;
      if (tabs.includes(`ModItems.${field}.get()`)) continue;
      missing.push(`        output.accept(ModItems.${field}.get());`);
    }
  }
  if (missing.length) {
    tabs = tabs.replace(tabAnchor, tabAnchor + '\n' + missing.join('\n'));
    fs.writeFileSync(MOD_TABS, tabs, 'utf8');
  }
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

console.log(`已生成：${SETS.length} 套 × 3 = ${SETS.length * 3} 个物品类 / 注册 / 创造栏 / 语言 / 物品模型 / 配方`);

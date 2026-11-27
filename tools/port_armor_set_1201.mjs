/**
 * 把 1.21.1 工程里的一套护甲（分件 geo）整包移植到 1.20.1 工程：
 *   geo / 贴图 / 物品图标 → 物品类 → ModItems 注册 → ModTabs → 语言 → 物品模型 → 装配配方
 *
 * 用法：
 *   node tools/port_armor_set_1201.mjs --key ukraine_soldier --cap UkraineSoldier --zh 乌克兰 --en Ukrainian
 *   可选：--material cement|netherite   (默认 cement = Superb Warfare 胶结硬质合金，老护甲数值)
 *
 * 约定（与 1211 一致）：geo 为分件命名 `<key>_<slot>.geo.json`，贴图 `<key>.png`，图标 `<key>_<slot>.png`
 */
import fs from 'node:fs';
import path from 'node:path';

const argv = process.argv.slice(2);
const arg = (k, d) => {
  const i = argv.indexOf(k);
  return i >= 0 ? argv[i + 1] : d;
};

const KEY = arg('--key');
const CAP = arg('--cap');
const ZH = arg('--zh', CAP);
const EN = arg('--en', CAP);
const MATERIAL = arg('--material', 'cement');
const TEX_PER_SLOT = argv.includes('--tex-per-slot');
if (!KEY || !CAP) {
  console.error('用法: node tools/port_armor_set_1201.mjs --key <key> --cap <CapName> [--zh 中文名] [--en English] [--material cement|netherite]');
  process.exit(2);
}

const SRC = 'E:/Dragon_Rise/Dragon_Rise_1211/src/main/resources/assets/dragonrise_reforge';
const DST_ROOT = 'E:/Dragon_Rise/Dragon_Rise_1201';
const DST = path.join(DST_ROOT, 'src/main/resources/assets/dragonrise_reforge');
const JAVA = path.join(DST_ROOT, 'src/main/java/com/redabysslucia/dragonrise_reforge');
const ARMOR_DIR = path.join(JAVA, 'item/armor');
const MOD_ITEMS = path.join(JAVA, 'init/ModItems.java');
const MOD_TABS = path.join(JAVA, 'init/ModTabs.java');
const DATA_RECIPE = path.join(DST_ROOT, 'src/main/resources/data/dragonrise_reforge/recipe');

const SLOTS = [
  { key: 'helmet', cap: 'Helmet', type: 'Type.HELMET', slot: 'HEAD', zh: '头盔', en: 'Helmet', kevlar: 5, durability: 407 },
  { key: 'chest', cap: 'Chest', type: 'Type.CHESTPLATE', slot: 'CHEST', zh: '作战服', en: 'Chestplate', kevlar: 8, durability: 592 },
  { key: 'pants', cap: 'Pants', type: 'Type.LEGGINGS', slot: 'LEGS', zh: '护腿', en: 'Leggings', kevlar: 7, durability: 555 },
];

// 1) 资源复制
fs.mkdirSync(path.join(DST, 'models/bedrock/armor'), { recursive: true });
fs.mkdirSync(path.join(DST, 'textures/armor'), { recursive: true });
fs.mkdirSync(path.join(DST, 'textures/item'), { recursive: true });
for (const s of SLOTS) {
  const geo = `${KEY}_${s.key}.geo.json`;
  const srcGeo = path.join(SRC, 'models/bedrock/armor', geo);
  if (!fs.existsSync(srcGeo)) throw new Error(`源工程缺 geo: ${geo}`);
  fs.copyFileSync(srcGeo, path.join(DST, 'models/bedrock/armor', geo));
  fs.copyFileSync(path.join(SRC, 'textures/item', `${KEY}_${s.key}.png`), path.join(DST, 'textures/item', `${KEY}_${s.key}.png`));
  if (TEX_PER_SLOT) {
    fs.copyFileSync(path.join(SRC, 'textures/armor', `${KEY}_${s.key}.png`), path.join(DST, 'textures/armor', `${KEY}_${s.key}.png`));
  }
}
if (!TEX_PER_SLOT) {
  fs.copyFileSync(path.join(SRC, 'textures/armor', `${KEY}.png`), path.join(DST, 'textures/armor', `${KEY}.png`));
}

// 2) 物品类
const materialBlock =
  MATERIAL === 'netherite'
    ? { imp: 'import net.minecraft.world.item.ArmorMaterials;\n', expr: 'ArmorMaterials.NETHERITE', dur: (s) => `new Properties().durability(${s.durability})`, doc: '对齐下界合金（护甲 3/8/6、韧性 3.0、击退抗性 0.1、附魔 15）' }
    : { imp: 'import com.atsuishio.superbwarfare.tiers.ModArmorMaterial;\n', expr: 'ModArmorMaterial.CEMENTED_CARBIDE', dur: () => 'new Properties()', doc: '老护甲数值：Superb Warfare 胶结硬质合金（护甲 3/8/6、韧性 4.0、击退抗性 0.05、附魔 10、耐久 ×50）' };

const javaClass = (s) => `package com.redabysslucia.dragonrise_reforge.item.armor;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.init.ModAttributes;
${materialBlock.imp}import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.redabysslucia.dragonrise_reforge.client.renderer.armor.SbmArmorItemRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.armor.SbmArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * ${ZH} 护甲（${s.zh}）。SBM 模型：{@code models/bedrock/armor/${KEY}_${s.key}.geo.json}，
 * 贴图：{@code textures/armor/${TEX_PER_SLOT ? `${KEY}_${s.key}` : KEY}.png}。
 *
 * <p>${materialBlock.doc}。
 */
public class ${CAP}${s.cap} extends ArmorItem {
    /** SBM armor model: assets/dragonrise_reforge/models/bedrock/armor/${KEY}_${s.key}.geo.json */
    public static final ResourceLocation GEO = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "models/bedrock/armor/${KEY}_${s.key}.geo.json");
    /** SBM armor texture: assets/dragonrise_reforge/textures/armor/${TEX_PER_SLOT ? `${KEY}_${s.key}` : KEY}.png */
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/armor/${TEX_PER_SLOT ? `${KEY}_${s.key}` : KEY}.png");

    public ${CAP}${s.cap}() {
        super(${materialBlock.expr}, ${s.type}, ${materialBlock.dur(s)});
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
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> map = super.getDefaultAttributeModifiers(slot);
        UUID uuid = new UUID(slot.toString().hashCode(), 0);
        if (slot == EquipmentSlot.${s.slot}) {
            map = HashMultimap.create(map);
            map.put(ModAttributes.BULLET_RESISTANCE.get(), new AttributeModifier(uuid, Mod.ATTRIBUTE_MODIFIER,
                    0.5 * Math.max(0, 1 - (double) stack.getDamageValue() / stack.getMaxDamage()), AttributeModifier.Operation.ADDITION));
        }
        return map;
    }
}
`;
for (const s of SLOTS) {
  fs.writeFileSync(path.join(ARMOR_DIR, `${CAP}${s.cap}.java`), javaClass(s), 'utf8');
}

// 3) ModItems
{
  const anchorCandidates = ['HONF_PANTS', 'SAWKM1_PANTS', 'RUSSIAN_SOLDIER_PANTS'];
  let txt = fs.readFileSync(MOD_ITEMS, 'utf8');
  const anchorLine = txt
    .split(/\r?\n/)
    .filter((l) => anchorCandidates.some((a) => l.includes(`RegistryObject<Item> ${a} =`)))
    .pop();
  if (!anchorLine) throw new Error('ModItems 锚点未找到');
  const missing = SLOTS.filter((s) => !txt.includes(`${KEY.toUpperCase()}_${s.key.toUpperCase()} = REGISTRY`))
    .map((s) => `public static final RegistryObject<Item> ${KEY.toUpperCase()}_${s.key.toUpperCase()} = REGISTRY.register("${KEY}_${s.key}", () -> new ${CAP}${s.cap}());`);
  if (missing.length) {
    txt = txt.replace(anchorLine, anchorLine + '\n' + missing.join('\n'));
    fs.writeFileSync(MOD_ITEMS, txt, 'utf8');
  }
}

// 4) ModTabs
{
  let txt = fs.readFileSync(MOD_TABS, 'utf8');
  const anchor = 'output.accept(ModItems.HONF_PANTS.get());';
  if (!txt.includes(anchor)) throw new Error('ModTabs 锚点未找到');
  const missing = SLOTS.filter((s) => !txt.includes(`ModItems.${KEY.toUpperCase()}_${s.key.toUpperCase()}.get()`))
    .map((s) => `        output.accept(ModItems.${KEY.toUpperCase()}_${s.key.toUpperCase()}.get());`);
  if (missing.length) {
    txt = txt.replace(anchor, anchor + '\n' + missing.join('\n'));
    fs.writeFileSync(MOD_TABS, txt, 'utf8');
  }
}

// 5) 语言
function patchLang(file, pick) {
  const p = path.join(DST, 'lang', file);
  const j = JSON.parse(fs.readFileSync(p, 'utf8'));
  for (const s of SLOTS) j[`item.dragonrise_reforge.${KEY}_${s.key}`] = pick(s);
  fs.writeFileSync(p, JSON.stringify(j, null, 2) + '\n', 'utf8');
}
patchLang('zh_cn.json', (s) => `${ZH} 士兵${s.zh}`);
patchLang('en_us.json', (s) => `${EN} Soldier ${s.en}`);

// 6) 物品模型 + 配方
fs.mkdirSync(DATA_RECIPE, { recursive: true });
for (const s of SLOTS) {
  fs.writeFileSync(
    path.join(DST, 'models/item', `${KEY}_${s.key}.json`),
    JSON.stringify({ parent: 'item/generated', textures: { layer0: `dragonrise_reforge:item/${KEY}_${s.key}` } }, null, 2) + '\n',
    'utf8'
  );
  fs.writeFileSync(
    path.join(DATA_RECIPE, `${KEY}_${s.key}_assembling.json`),
    JSON.stringify({
      type: 'superbwarfare:vehicle_assembling',
      category: 'misc',
      inputs: [`${s.kevlar} dragonrise_reforge:kevlar`],
      result: { item: `dragonrise_reforge:${KEY}_${s.key}` },
    }, null, 2) + '\n',
    'utf8'
  );
}

console.log(`已移植 ${KEY} 到 1.20.1：3 geo / 1 贴图 / 3 图标 / 3 物品类 / 注册 / 创造栏 / 语言 / 物品模型 / 配方（材质：${MATERIAL}）`);

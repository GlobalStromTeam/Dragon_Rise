/**
 * 把 Blockbench 导出的护甲 geo 规整成 SBM 可用的护甲 geo：
 *   1) 枢轴全部改成规范值（armor 骨与其 biped 父骨一致，bind=0）
 *   2) 额外骨骼（group / group2 / bone…）的方块就近并入最近的 armor 骨
 *   3) format_version 统一 1.12.0，只保留 14 根骨
 *   4) 方块本身（origin/size/pivot/rotation/uv）原样保留，不动几何
 *
 * 用法：
 *   node tools/geo_to_armor.mjs --in <src.geo.json> --out <dst.geo.json> [--id geometry.xxx] [--report]
 *   node tools/geo_to_armor.mjs --all <srcDir> <dstDir>        # 批量（按文件名识别套名）
 */
import fs from 'node:fs';
import path from 'node:path';

const CANON = [
  ['bipedHead', null, [0, 24, 0]],
  ['armorHead', 'bipedHead', [0, 24, 0]],
  ['bipedBody', null, [0, 24, 0]],
  ['armorBody', 'bipedBody', [0, 24, 0]],
  ['bipedRightArm', null, [-5, 22, 0]],
  ['armorRightArm', 'bipedRightArm', [-5, 22, 0]],
  ['bipedLeftArm', null, [5, 22, 0]],
  ['armorLeftArm', 'bipedLeftArm', [5, 22, 0]],
  ['bipedRightLeg', null, [-2, 12, 0]],
  ['armorRightLeg', 'bipedRightLeg', [-2, 12, 0]],
  ['armorRightBoot', 'bipedRightLeg', [-2, 12, 0]],
  ['bipedLeftLeg', null, [2, 12, 0]],
  ['armorLeftLeg', 'bipedLeftLeg', [2, 12, 0]],
  ['armorLeftBoot', 'bipedLeftLeg', [2, 12, 0]],
];
const CANON_NAMES = new Set(CANON.map((c) => c[0]));

/** 旧的左右腿约定（右=-2 / 左=+2）需要按实际几何翻转时，靠这个判断 */
const ARMOR_OF_BIPED = new Map(CANON.filter((c) => c[1]).map((c) => [c[0], c[1]]));

function normalize(geoJson, id) {
  const src = geoJson['minecraft:geometry'][0];
  const byName = new Map();
  const notes = [];
  for (const b of src.bones || []) byName.set(b.name, b);

  // 1) 把非标准骨骼的方块并入最近的 armor 骨
  const extra = [];
  for (const b of src.bones || []) {
    if (CANON_NAMES.has(b.name)) continue;
    if (!(b.cubes || []).length) continue;
    let cur = b.parent;
    let target = null;
    while (cur) {
      if (CANON_NAMES.has(cur) && cur.startsWith('armor')) { target = cur; break; }
      cur = byName.get(cur)?.parent;
    }
    if (!target) target = 'armorBody';
    extra.push({ from: b.name, to: target, cubes: b.cubes.length });
    const host = byName.get(target);
    host.cubes = (host.cubes || []).concat(b.cubes);
  }

  // 2) 输出 14 根规范骨
  const bones = CANON.map(([name, parent, pivot]) => {
    const srcBone = byName.get(name);
    const bone = { name, ...(parent ? { parent } : {}), pivot: pivot.slice(), cubes: (srcBone?.cubes || []).map((c) => cloneCube(c)) };
    return bone;
  });

  // 3) 左右腿/靴的 pivot 与几何是否同侧：以几何 x 中心判定，必要时翻转 pivot 符号
  for (const bone of bones) {
    if (!/Arm|Leg|Boot$/.test(bone.name) || !bone.cubes.length) continue;
    const xs = [];
    for (const c of bone.cubes) xs.push(c.origin[0], c.origin[0] + c.size[0]);
    const cx = (Math.min(...xs) + Math.max(...xs)) / 2;
    const wantSign = cx >= 0 ? 1 : -1;
    if (Math.sign(bone.pivot[0]) !== wantSign) {
      notes.push(`${bone.name} pivot.x ${bone.pivot[0]} -> ${Math.abs(bone.pivot[0]) * wantSign}（按几何侧翻转）`);
      bone.pivot[0] = Math.abs(bone.pivot[0]) * wantSign;
      // 同步其 biped 父骨，保持 bind=0
      for (const [armor, biped] of ARMOR_OF_BIPED) {
        if (armor === bone.name) {
          const p = bones.find((x) => x.name === biped);
          if (p) p.pivot[0] = bone.pivot[0];
        }
      }
      // boot 跟随其父腿
      if (bone.name.endsWith('Boot')) {
        const leg = bones.find((x) => x.name === bone.parent);
        if (leg) leg.pivot[0] = bone.pivot[0];
      }
    }
  }

  return {
    geo: {
      format_version: '1.12.0',
      'minecraft:geometry': [
        {
          description: {
            identifier: id || src.description?.identifier || 'geometry.armor',
            texture_width: src.description?.texture_width || 64,
            texture_height: src.description?.texture_height || 64,
            visible_bounds_width: 4,
            visible_bounds_height: 4,
            visible_bounds_offset: [0, 2, 0],
          },
          bones,
        },
      ],
    },
    extra,
    notes,
    counts: Object.fromEntries(bones.map((b) => [b.name, b.cubes.length])),
  };
}

function cloneCube(c) {
  return JSON.parse(JSON.stringify(c));
}

function keyFromName(file) {
  const n = path.basename(file).toLowerCase();
  if (n.includes('haom')) return 'haom';
  if (n.includes('juaw')) return 'juaw';
  if (n.includes('ade3')) return 'ade3';
  if (n.includes('sawkm')) return 'sawkm1';
  if (n.includes('ｈｏｎｆ') || n.includes('honf')) return 'honf';
  return null;
}

const argv = process.argv.slice(2);
const get = (k, d) => {
  const i = argv.indexOf(k);
  return i >= 0 ? argv[i + 1] : d;
};

if (argv.includes('--all')) {
  const srcDir = argv[argv.indexOf('--all') + 1];
  const dstDir = argv[argv.indexOf('--all') + 2];
  fs.mkdirSync(dstDir, { recursive: true });
  for (const f of fs.readdirSync(srcDir).filter((x) => x.endsWith('.geo.json'))) {
    const key = keyFromName(f);
    if (!key) continue;
    const json = JSON.parse(fs.readFileSync(path.join(srcDir, f), 'utf8'));
    const { geo, extra, notes, counts } = normalize(json, `geometry.${key}`);
    const out = path.join(dstDir, `${key}.geo.json`);
    fs.writeFileSync(out, JSON.stringify(geo, null, '\t') + '\n', 'utf8');
    console.log(
      `${f} → ${path.basename(out)} | cubes=${Object.values(counts).reduce((a, b) => a + b, 0)} | ` +
        `head=${counts.armorHead} body=${counts.armorBody} 臂L/R=${counts.armorLeftArm}/${counts.armorRightArm} ` +
        `腿L/R=${counts.armorLeftLeg}/${counts.armorRightLeg} 靴L/R=${counts.armorLeftBoot}/${counts.armorRightBoot}` +
        (extra.length ? ` | 合并额外骨: ${extra.map((e) => `${e.from}→${e.to}(${e.cubes})`).join(', ')}` : '') +
        (notes.length ? ` | ${notes.join('; ')}` : '')
    );
  }
} else {
  const IN = get('--in');
  const OUT = get('--out');
  if (!IN || !OUT) {
    console.error('用法: node tools/geo_to_armor.mjs --in src.geo.json --out dst.geo.json [--id geometry.x]');
    process.exit(2);
  }
  const json = JSON.parse(fs.readFileSync(IN, 'utf8'));
  const { geo, extra, notes, counts } = normalize(json, get('--id', null));
  fs.mkdirSync(path.dirname(OUT), { recursive: true });
  fs.writeFileSync(OUT, JSON.stringify(geo, null, '\t') + '\n', 'utf8');
  console.log(JSON.stringify({ out: OUT, counts, extra, notes }, null, 2));
}

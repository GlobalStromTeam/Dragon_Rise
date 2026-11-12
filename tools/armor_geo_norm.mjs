#!/usr/bin/env node
/**
 * 护甲 geo 规范化 / 校验工具（SBM v2 渲染约定）
 *
 * 用法：
 *   node tools/armor_geo_norm.mjs --check           # 只报告（有违规时退出码 1）
 *   node tools/armor_geo_norm.mjs --fix             # 自动修枢轴（先备份到 build/armor_geo_backup/）
 *   node tools/armor_geo_norm.mjs --check --all     # 顺带体检其它 geojson（只报告，不修）
 *
 * 规范（硬约束，违反会导致护甲错位，例如“胸甲飞到头上”）：
 *   1. format_version 必须是 1.12.0
 *   2. armor 骨的 pivot 必须等于 SBM 的静息偏移：
 *        head/body (0,24,0)   arms (±5,22,0)   legs/boots (±2,12,0)
 *   3. biped 父骨：head/body (0,24,0)，臂 y=22 |x|=5，腿 y=12 |x|=2，z=0
 *   4. armor 骨与对应 biped 骨 x 同号（同侧），y/z 取规范值
 *   5. 骨骼名唯一，parent 必须存在
 *   6. uv_rotation 必须为 0（导入时若带旋转需先烘焙）
 *   7. 必需骨骼齐全（按族推断：头盔 / 胸甲 / 护腿）
 *
 * 立方体坐标约定：模型空间绝对值，原点在脚底、+y 向上（不要写相对 pivot 的坐标）。
 */
import fs from 'node:fs';
import path from 'node:path';

const args = process.argv.slice(2);
const FIX = args.includes('--fix');
const ALL = args.includes('--all');
const CHECK = args.includes('--check') || !FIX;
const rootArg = args.indexOf('--root');
const ROOT = rootArg >= 0 ? path.resolve(args[rootArg + 1]) : process.cwd();

const ARMOR_GLOB_DIR = path.join(ROOT, 'src/main/resources/assets');

/** 规范的 armor / biped 枢轴（几何值；左右符号按模型自身保留） */
const CANON = {
  bipedHead: [0, 24, 0],
  armorHead: [0, 24, 0],
  bipedBody: [0, 24, 0],
  armorBody: [0, 24, 0],
  bipedRightArm: [-5, 22, 0],
  bipedLeftArm: [5, 22, 0],
  armorRightArm: [-5, 22, 0],
  armorLeftArm: [5, 22, 0],
  bipedRightLeg: [-2, 12, 0],
  bipedLeftLeg: [2, 12, 0],
  armorRightLeg: [-2, 12, 0],
  armorLeftLeg: [2, 12, 0],
  armorRightBoot: [-2, 12, 0],
  armorLeftBoot: [2, 12, 0],
};

/** 需要和 biped 同号的配对 */
const PAIRS = [
  ['bipedRightArm', 'armorRightArm'],
  ['bipedLeftArm', 'armorLeftArm'],
  ['bipedRightLeg', 'armorRightLeg'],
  ['bipedLeftLeg', 'armorLeftLeg'],
];

/** 左右同号修正时，boot 跟随 leg */
const BOOT_OF = { armorLeftBoot: 'armorLeftLeg', armorRightBoot: 'armorRightLeg' };

const FAMILY = {
  helmet: ['bipedHead', 'armorHead'],
  chest: ['bipedBody', 'armorBody', 'bipedRightArm', 'bipedLeftArm', 'armorRightArm', 'armorLeftArm'],
  legs: ['bipedRightLeg', 'bipedLeftLeg', 'armorRightLeg', 'armorLeftLeg'],
  boot: ['armorRightBoot', 'armorLeftBoot'],
};

const near = (a, b) => Math.abs(a - b) < 0.001;
const fmt = (p) => (p ? `[${p.map((v) => (Math.round(v * 1000) / 1000)).join(',')}]` : 'null');

function listGeo(dir) {
  const out = [];
  if (!fs.existsSync(dir)) return out;
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, entry.name);
    if (entry.isDirectory()) out.push(...listGeo(p));
    else if (entry.name.endsWith('.geo.json')) out.push(p);
  }
  return out;
}

function pngSize(file) {
  try {
    const buf = fs.readFileSync(file);
    if (buf.length < 24 || buf.toString('ascii', 1, 4) !== 'PNG') return null;
    return { w: buf.readUInt32BE(16), h: buf.readUInt32BE(20) };
  } catch {
    return null;
  }
}

function findTexture(geoFile, assetsRoot) {
  const match = geoFile.match(/[\\/]assets[\\/]([^\\/]+)[\\/]/);
  const ns = match ? match[1] : null;
  if (!ns) return null;
  const base = path.basename(geoFile).replace(/\.geo\.json$/, '');
  const armorDir = path.join(assetsRoot, ns, 'textures/armor');
  const candidates = [
    base,
    base.replace(/_(helmet|chest|pants|leggings|boots)$/i, ''),
  ];
  for (const c of [...new Set(candidates)]) {
    const p = path.join(armorDir, c + '.png');
    if (fs.existsSync(p)) return p;
  }
  return null;
}

function familyOf(names) {
  const set = new Set(names);
  if (set.has('armorHead')) return 'helmet';
  if (set.has('armorBody')) return 'chest';
  if (set.has('armorRightLeg') || set.has('armorLeftLeg') || set.has('armorRightBoot')) return 'legs';
  return null;
}

function checkAndFix(geoFile, apply) {
  const raw = fs.readFileSync(geoFile, 'utf8');
  let json;
  try {
    json = JSON.parse(raw);
  } catch (e) {
    return { file: geoFile, violations: [`JSON 解析失败：${e.message}`], changed: false };
  }
  const geo = (json['minecraft:geometry'] || [])[0];
  if (!geo || !geo.bones) return { file: geoFile, violations: ['缺少 minecraft:geometry[0].bones'], changed: false };

  const violations = [];
  const bones = geo.bones;
  const byName = new Map();
  for (const b of bones) {
    if (byName.has(b.name)) violations.push(`重复骨骼名：${b.name}`);
    byName.set(b.name, b);
  }
  for (const b of bones) {
    if (b.parent && !byName.has(b.parent)) violations.push(`${b.name} 的 parent「${b.parent}」不存在`);
  }

  // 1. format_version
  if (String(json.format_version) !== '1.12.0') {
    violations.push(`format_version=${json.format_version}（要求 1.12.0）`);
    if (apply) json.format_version = '1.12.0';
  }

  // 7. 族完整性
  const fam = familyOf([...byName.keys()]);
  if (!fam) violations.push('无法判定族（既没有 armorHead 也没有 armorBody / armor*Leg）');
  else {
    for (const req of FAMILY[fam]) {
      if (!byName.has(req)) violations.push(`缺少必需骨骼 ${req}（${fam} 族）`);
    }
    if (fam === 'legs') {
      for (const req of FAMILY.boot) if (!byName.has(req)) violations.push(`缺少 ${req}（靴子层与护腿同一件物品时应存在）`);
    }
  }

  // 2/3/4. 枢轴
  const fixedPivot = new Map();
  for (const [name, canon] of Object.entries(CANON)) {
    const bone = byName.get(name);
    if (!bone) continue;
    const cur = bone.pivot || [0, 0, 0];
    let target;
    if (/Arm$/.test(name) || /Leg$/.test(name) || /Boot$/.test(name)) {
      const sign = Math.sign(cur[0]) || Math.sign(canon[0]);
      target = [sign * Math.abs(canon[0]), canon[1], canon[2]];
    } else {
      target = canon.slice();
    }
    // armor 骨 x 跟随对应 biped
    const pair = PAIRS.find(([, a]) => a === name);
    if (pair) {
      const parent = byName.get(pair[0]);
      if (parent && parent.pivot) target[0] = Math.sign(parent.pivot[0] || target[0]) * Math.abs(target[0]);
    }
    if (BOOT_OF[name]) {
      const leg = byName.get(BOOT_OF[name]);
      if (leg && leg.pivot) target[0] = Math.sign(leg.pivot[0] || target[0]) * Math.abs(target[0]);
    }
    const ok = cur.length === 3 && near(cur[0], target[0]) && near(cur[1], target[1]) && near(cur[2], target[2]);
    if (!ok) {
      violations.push(`${name} pivot ${fmt(cur)} → 应为 ${fmt(target)}`);
      if (apply) {
        bone.pivot = target.slice();
        fixedPivot.set(name, target);
      }
    }
  }

  // 6. uv_rotation
  let rotCount = 0;
  for (const b of bones) {
    for (const c of b.cubes || []) {
      if (c.uv_rotation && Math.abs(c.uv_rotation) > 0.001) rotCount++;
      for (const f of Object.values(c.uv || {})) {
        if (f && typeof f === 'object' && f.uv_rotation && Math.abs(f.uv_rotation) > 0.001) rotCount++;
      }
    }
  }
  if (rotCount) violations.push(`存在 ${rotCount} 处 uv_rotation≠0（需烘焙）`);

  // 5. 贴图比例（警告级）：PNG 必须是声明的整数倍（历史坑：贴图被非整数缩放会导致 UV 采样错位）
  const tex = findTexture(geoFile, ARMOR_GLOB_DIR);
  const size = tex ? pngSize(tex) : null;
  const decl = [geo.description?.texture_width, geo.description?.texture_height];
  if (!decl[0] || !decl[1]) violations.push('description 缺少 texture_width/height');
  else if (size) {
    const rx = size.w / decl[0];
    const ry = size.h / decl[1];
    const intOk = Math.abs(rx - Math.round(rx)) < 1e-6 && Math.abs(ry - Math.round(ry)) < 1e-6 && rx >= 1 && rx === ry;
    if (!intOk) {
      violations.push(
        `贴图 ${path.basename(tex)} 为 ${size.w}x${size.h}，与声明 ${decl[0]}x${decl[1]} 不是整数倍（${rx.toFixed(3)}x）——需重导出为 ${decl[0] * 2}x${decl[1] * 2} 或改声明`
      );
    }
  }

  let changed = false;
  if (apply && fixedPivot.size) {
    const next = JSON.stringify(json, null, '\t') + '\n';
    if (next !== raw) {
      const bakDir = path.join(ROOT, 'build/armor_geo_backup');
      fs.mkdirSync(bakDir, { recursive: true });
      fs.writeFileSync(path.join(bakDir, path.basename(geoFile)), raw, 'utf8');
      fs.writeFileSync(geoFile, next, 'utf8');
      changed = true;
    }
  }

  return { file: geoFile, violations, changed };
}

const armorDir = path.join(ARMOR_GLOB_DIR);
const armorFiles = listGeo(armorDir).filter((f) => f.includes(`${path.sep}bedrock${path.sep}armor${path.sep}`));
const results = armorFiles.map((f) => checkAndFix(f, FIX));

let totalViolations = 0;
let changedCount = 0;
for (const r of results) {
  const rel = path.relative(ROOT, r.file).replace(/\\/g, '/');
  if (r.changed) changedCount++;
  if (r.violations.length === 0) continue;
  totalViolations += r.violations.length;
  console.log(`\n${rel}`);
  for (const v of r.violations) console.log(`  - ${v}`);
}

console.log(
  `\n护甲 geo：${results.length} 个文件，${totalViolations} 条问题` +
    (FIX ? `，已修 ${changedCount} 个（备份 build/armor_geo_backup/）` : '')
);
// ASCII 摘要：控制台编码不是 UTF-8 时也能看清结论
console.log(`[armor-geo] files=${results.length} issues=${totalViolations}` + (FIX ? ` fixed=${changedCount}` : ''));

if (ALL) {
  const others = listGeo(armorDir).filter((f) => !armorFiles.includes(f));
  let bad = 0;
  for (const f of others) {
    const j = JSON.parse(fs.readFileSync(f, 'utf8'));
    const g = (j['minecraft:geometry'] || [])[0];
    const problems = [];
    if (!g) problems.push('无 minecraft:geometry');
    else {
      const names = new Set();
      for (const b of g.bones || []) {
        if (names.has(b.name)) problems.push(`重复骨骼名 ${b.name}`);
        names.add(b.name);
        if (b.parent && !names.has(b.parent) && !(g.bones || []).some((x) => x.name === b.parent)) {
          problems.push(`${b.name} 的 parent「${b.parent}」不存在`);
        }
        for (const c of b.cubes || []) {
          if (c.uv_rotation && Math.abs(c.uv_rotation) > 0.001) problems.push(`${b.name} 有 uv_rotation≠0`);
        }
      }
    }
    if (problems.length) {
      bad++;
      console.log(`\n[其它 geo] ${path.relative(ROOT, f).replace(/\\/g, '/')}`);
      for (const p of [...new Set(problems)]) console.log(`  - ${p}`);
    }
  }
  console.log(`\n其它 geo：${others.length} 个文件，${bad} 个有问题（只查重名/悬空 parent/uv_rotation）`);
}

process.exit(CHECK && totalViolations > 0 ? 1 : 0);

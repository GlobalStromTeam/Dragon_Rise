/**
 * .bbmodel → 护甲 geo（Bedrock geometry 1.12.0）+ 贴图导出
 *
 * 用法：
 *   node tools/bbmodel_to_armor_geo.mjs --in <x.bbmodel> --geo <out.geo.json> --png <out.png> [--id <identifier>] [--strip-rotation]
 *
 * 规则：
 *  - cube 的 from/to 即模型空间绝对坐标（原点脚底、+y 向上）
 *  - 按“旋转后的包围盒中心”把 cube 归到 armor 骨：head / body / arm(L,R) / leg(L,R) / boot(L,R)
 *  - 骨骼 pivot 用 SBM 规范值，armor 骨与 biped 父骨一致（bind 0）
 *  - per-face UV 输出为 bedrock 的 { uv, uv_size }
 *  - texture_width/height = 贴图 UV 空间尺寸（= 工程 resolution），贴图由调用方按此尺寸重采样
 */
import fs from 'node:fs';
import path from 'node:path';

const argv = process.argv.slice(2);
const get = (k, d) => {
  const i = argv.indexOf(k);
  return i >= 0 ? argv[i + 1] : d;
};
const IN = get('--in');
const GEO = get('--geo');
const PNG = get('--png');
const ID = get('--id', 'geometry.custom_armor');
const STRIP_ROT = argv.includes('--strip-rotation');

if (!IN || !GEO || !PNG) {
  console.error('用法: node tools/bbmodel_to_armor_geo.mjs --in x.bbmodel --geo out.geo.json --png out.png [--id geometry.x]');
  process.exit(2);
}

const bb = JSON.parse(fs.readFileSync(IN, 'utf8'));
const els = (bb.elements || []).filter((e) => e.type === 'cube' && e.visibility !== false);
const textures = bb.textures || [];
if (!textures.length) throw new Error('bbmodel 没有贴图');

// 选“被引用最多”的贴图作为主贴图（bbmodel 里常带一张 64px 的副本，不能直接用）
const refCount = {};
for (const el of els) {
  for (const f of Object.values(el.faces || {})) {
    if (f && f.texture !== undefined) refCount[f.texture] = (refCount[f.texture] || 0) + 1;
  }
}
const mainIdx = Object.entries(refCount).sort((a, b) => b[1] - a[1])[0]?.[0] ?? 0;
const mainTex = textures[Number(mainIdx)] || textures[0];

// ── 贴图 ─────────────────────────────────────────────────────────────────────
const b64 = String(mainTex.source).split(',')[1] || '';
const pngBuf = Buffer.from(b64, 'base64');
fs.mkdirSync(path.dirname(PNG), { recursive: true });
fs.writeFileSync(PNG, pngBuf);
const isPng = pngBuf.toString('ascii', 1, 4) === 'PNG';
const pngW = isPng ? pngBuf.readUInt32BE(16) : 0;
const pngH = isPng ? pngBuf.readUInt32BE(20) : 0;

// UV 空间 = 主贴图声明的 uv 尺寸（Blockbench 里 uv 坐标以它为准），且不小于实际用到的上界
let uvMaxMain = 0;
let uvMax = 0;
for (const el of els) {
  for (const f of Object.values(el.faces || {})) {
    if (!f || !Array.isArray(f.uv)) continue;
    uvMax = Math.max(uvMax, ...f.uv);
    if (String(f.texture) === String(mainIdx)) uvMaxMain = Math.max(uvMaxMain, ...f.uv);
  }
}
const declW = Math.max(Math.ceil(mainTex.uv_width || pngW || 64), Math.ceil(bb.resolution?.width || 0), Math.ceil(uvMaxMain));
const declH = Math.max(Math.ceil(mainTex.uv_height || pngH || 64), Math.ceil(bb.resolution?.height || 0), Math.ceil(uvMaxMain));

// ── 骨骼定义（SBM 规范）──────────────────────────────────────────────────────
const BONES = [
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
const boneMap = new Map();
const bones = BONES.map(([name, parent, pivot]) => {
  const b = { name, ...(parent ? { parent } : {}), pivot: pivot.slice(), cubes: [] };
  boneMap.set(name, b);
  return b;
});

// ── 旋转后包围盒中心 ─────────────────────────────────────────────────────────
function rotatedCenter(el) {
  const fx = [el.from[0], el.to[0]];
  const fy = [el.from[1], el.to[1]];
  const fz = [el.from[2], el.to[2]];
  const rot = el.rotation || [0, 0, 0];
  const o = el.origin || [0, 0, 0];
  const rad = rot.map((d) => (d * Math.PI) / 180);
  const pts = [];
  for (const x of fx) for (const y of fy) for (const z of fz) {
    let px = x - o[0], py = y - o[1], pz = z - o[2];
    // X 轴
    let cy = Math.cos(rad[0]), sy = Math.sin(rad[0]);
    [py, pz] = [py * cy - pz * sy, py * sy + pz * cy];
    // Y 轴
    let cz = Math.cos(rad[1]), sz = Math.sin(rad[1]);
    [px, pz] = [px * cz + pz * sz, -px * sz + pz * cz];
    // Z 轴
    let cx = Math.cos(rad[2]), sx = Math.sin(rad[2]);
    [px, py] = [px * cx - py * sx, px * sx + py * cx];
    pts.push([px + o[0], py + o[1], pz + o[2]]);
  }
  const min = [0, 1, 2].map((i) => Math.min(...pts.map((p) => p[i])));
  const max = [0, 1, 2].map((i) => Math.max(...pts.map((p) => p[i])));
  return [0, 1, 2].map((i) => (min[i] + max[i]) / 2);
}

function classify([x, y, z]) {
  // 腿/靴：y 下段；左右按 x 符号（file 空间 右=负、左=正）
  if (y < 3.2) return x >= 0 ? 'armorLeftBoot' : 'armorRightBoot';
  if (y < 11.5) return x >= 0 ? 'armorLeftLeg' : 'armorRightLeg';
  // 手臂：先按 X 判（兼容“抬臂”姿势，避免被当成头部）
  if (Math.abs(x) >= 4.6 && y <= 30.5) return x >= 0 ? 'armorLeftArm' : 'armorRightArm';
  if (y >= 23.5) return 'armorHead';
  return 'armorBody';
}

// ── 顶点 / UV ───────────────────────────────────────────────────────────────
const uvNum = (v) => Math.round(v * 1000) / 1000;

function uvScaleFor(f) {
  // 只有“引用了非主贴图”的面才需要换算到主贴图的 UV 空间；
  // 主贴图的面保持原始 UV（否则 uv_width 与真实图集尺寸不符时会被整体放大，UV 全错）
  if (String(f.texture ?? mainIdx) === String(mainIdx)) return 1;
  const t = textures[Number(f.texture ?? 0)];
  const w = t?.uv_width || declW;
  return declW / w;
}

function cubeToGeo(el) {
  const inflate = el.inflate || 0;
  const from = el.from.map((v, i) => v - inflate);
  const to = el.to.map((v, i) => v + inflate);
  const origin = [Math.min(from[0], to[0]), Math.min(from[1], to[1]), Math.min(from[2], to[2])];
  const size = [Math.abs(to[0] - from[0]), Math.abs(to[1] - from[1]), Math.abs(to[2] - from[2])];
  const uv = {};
  for (const [dir, f] of Object.entries(el.faces || {})) {
    if (!f || !Array.isArray(f.uv)) continue;
    const k = uvScaleFor(f); // 面引用的贴图 UV 空间 → 主贴图 UV 空间
    const x1 = f.uv[0] * k, y1 = f.uv[1] * k, x2 = f.uv[2] * k, y2 = f.uv[3] * k;
    let w = x2 - x1;
    let h = y2 - y1;
    // Blockbench 的 face.rotation：180 用负尺寸表达翻转；90/270 面级无法表达，保持原样
    if (f.rotation === 180) { w = -w; h = -h; }
    uv[dir] = { uv: [uvNum(Math.min(x1, x2)), uvNum(Math.min(y1, y2))], uv_size: [uvNum(w), uvNum(h)] };
  }
  return {
    origin: origin.map(uvNum),
    size: size.map(uvNum),
    pivot: (el.origin || [0, 0, 0]).map(uvNum),
    rotation: STRIP_ROT ? [0, 0, 0] : (el.rotation || [0, 0, 0]).map(uvNum),
    uv,
  };
}

const counts = {};
const volumes = {};
for (const el of els) {
  const bone = classify(rotatedCenter(el));
  const cube = cubeToGeo(el);
  boneMap.get(bone).cubes.push(cube);
  counts[bone] = (counts[bone] || 0) + 1;
  volumes[bone] = (volumes[bone] || 0) + cube.size[0] * cube.size[1] * cube.size[2];
}

const geo = {
  format_version: '1.12.0',
  'minecraft:geometry': [
    {
      description: {
        identifier: ID,
        texture_width: declW,
        texture_height: declH,
        visible_bounds_width: 4,
        visible_bounds_height: 4,
        visible_bounds_offset: [0, 2, 0],
      },
      bones: bones.map((b) => (b.cubes.length ? b : { name: b.name, ...(b.parent ? { parent: b.parent } : {}), pivot: b.pivot, cubes: [] })),
    },
  ],
};

fs.mkdirSync(path.dirname(GEO), { recursive: true });
fs.writeFileSync(GEO, JSON.stringify(geo, null, '\t') + '\n', 'utf8');

const totalVol = Object.values(volumes).reduce((a, b) => a + b, 0) || 1;
const pct = (k) => Math.round(((volumes[k] || 0) / totalVol) * 100) + '%';

console.log(
  `${path.basename(IN)} → ${path.basename(GEO)} | cubes=${els.length} | uv空间=${declW}x${declH} | png=${pngW}x${pngH}\n` +
    `  数量 head=${counts.armorHead || 0} body=${counts.armorBody || 0} 臂L=${counts.armorLeftArm || 0} 臂R=${counts.armorRightArm || 0} ` +
    `腿L=${counts.armorLeftLeg || 0} 腿R=${counts.armorRightLeg || 0} 靴L=${counts.armorLeftBoot || 0} 靴R=${counts.armorRightBoot || 0}\n` +
    `  体积 head=${pct('armorHead')} body=${pct('armorBody')} 臂L=${pct('armorLeftArm')} 臂R=${pct('armorRightArm')} ` +
    `腿L=${pct('armorLeftLeg')} 腿R=${pct('armorRightLeg')} 靴L=${pct('armorLeftBoot')} 靴R=${pct('armorRightBoot')}`
);

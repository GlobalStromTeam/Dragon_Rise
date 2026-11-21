#!/usr/bin/env node
/**
 * 武器站配置校验器 —— 检查车辆数据里指向模型骨骼的名字是否真的存在。
 *
 * 用法：
 *   node tools/check_weapon_station.mjs <项目根目录> [--strict]
 *
 * 两级结果：
 *   错误（exit 1）：一定是配置写坏了
 *     - BoundBones / BoundBonesYaw / BoundBonesPitch 引用了模型里不存在的骨骼
 *     - BoundBonesYaw / BoundBonesPitch 的骨骼名不以 Yaw / Pitch 结尾（SBW 不会旋转它）
 *     - ShootPos.Transform / Directions 指向不存在的骨骼
 *   提示（不影响 exit code）：
 *     - 用了 WeaponStation / WeaponStationBarrel，但模型没有对应的 passengerWeaponStation* 骨
 *     - 用了标准武器站，但根级缺 PassengerWeaponStationPos / PassengerWeaponStationBarrelPos
 *     （历史模型遗留，多为"枪能开火但模型不跟着转"）
 *
 * --strict：把提示也当作错误。
 *
 * 规范说明见 docs/武器站骨骼绑定.md
 */
import fs from 'fs';
import path from 'path';

const ROOT = process.argv[2];
const STRICT = process.argv.includes('--strict');
if (!ROOT) {
  console.error('用法: node tools/check_weapon_station.mjs <项目根目录> [--strict]');
  process.exit(2);
}

const VEHICLE_DIR = path.join(ROOT, 'src/main/resources/data/dragonrise_reforge/sbw/vehicles');
const RES = path.join(ROOT, 'src/main/resources');

// SBW 内置 transform 关键字 -> 需要存在的骨骼
const TRANSFORM_BONES = {
  Turret: ['turret'],
  Barrel: ['barrel'],
  WeaponStation: ['passengerWeaponStationYaw'],
  WeaponStationBarrel: ['passengerWeaponStationPitch'],
};
// Directions 里允许的非骨骼关键字
const FREE_TOKENS = new Set(['Vehicle', 'Passenger', 'DeltaMovement', 'Default', 'Look', 'Aim', 'None']);

const errors = [];
const warns = [];
let vehicles = 0;
let weapons = 0;

function readBones(geoPath) {
  const geo = JSON.parse(fs.readFileSync(geoPath, 'utf8'));
  const bones = geo['minecraft:geometry']?.[0]?.bones ?? [];
  return new Set(bones.map((b) => b.name));
}

function modelPathOf(v) {
  const model = v.Model?.Model ?? v.Models?.[0]?.Model;
  if (!model) return null;
  return path.join(RES, 'assets/dragonrise_reforge', model.split(':').pop());
}

for (const file of fs.readdirSync(VEHICLE_DIR)) {
  if (!file.endsWith('.json')) continue;
  const v = JSON.parse(fs.readFileSync(path.join(VEHICLE_DIR, file), 'utf8'));
  const geoPath = modelPathOf(v);
  if (!geoPath || !fs.existsSync(geoPath)) continue;
  const bones = readBones(geoPath);
  const id = file.replace(/\.json$/, '');
  vehicles++;

  for (const [weaponName, w] of Object.entries(v.Weapons ?? {})) {
    weapons++;
    const tag = `${id} / ${weaponName}`;

    for (const [key, suffix] of [['BoundBonesYaw', 'Yaw'], ['BoundBonesPitch', 'Pitch']]) {
      const list = w[key];
      if (!Array.isArray(list)) continue;
      for (const bone of list) {
        if (!bones.has(bone)) errors.push(`${tag}: ${key} 引用了不存在的骨骼 "${bone}"`);
        else if (!bone.endsWith(suffix)) errors.push(`${tag}: ${key} 的骨骼 "${bone}" 不以 ${suffix} 结尾，SBW 不会按该轴旋转`);
      }
    }
    if (Array.isArray(w.BoundBones)) {
      for (const bone of w.BoundBones) {
        if (!bones.has(bone)) errors.push(`${tag}: BoundBones 引用了不存在的骨骼 "${bone}"`);
      }
    }

    const sp = w.ShootPos;
    if (!sp) continue;
    const targets = [sp.Transform, ...(Array.isArray(sp.Directions) ? sp.Directions : [])].filter(Boolean);

    for (const t of targets) {
      if (FREE_TOKENS.has(t)) continue;
      if (TRANSFORM_BONES[t]) {
        for (const need of TRANSFORM_BONES[t]) {
          if (!bones.has(need)) warns.push(`${tag}: 用了 ${t}，但模型没有骨骼 "${need}"，模型不会跟随瞄准转动`);
        }
        continue;
      }
      if (/^[-\d.,\s]+$/.test(t)) continue; // 向量写法
      if (!bones.has(t)) errors.push(`${tag}: Transform/Directions 指向不存在的骨骼 "${t}"`);
    }

    const usesStation = targets.some((t) => t === 'WeaponStation' || t === 'WeaponStationBarrel');
    if (usesStation) {
      if (!v.PassengerWeaponStationPos) warns.push(`${tag}: 用了标准武器站，但根级缺少 PassengerWeaponStationPos`);
      if (!v.PassengerWeaponStationBarrelPos) warns.push(`${tag}: 用了标准武器站，但根级缺少 PassengerWeaponStationBarrelPos`);
    }
  }
}

console.log(`扫描 ${vehicles} 台载具、${weapons} 门武器`);
console.log(`错误 ${errors.length} 处，提示 ${warns.length} 处`);
if (errors.length) {
  console.log('\n[错误]');
  for (const line of errors) console.log('  - ' + line);
}
if (warns.length) {
  console.log('\n[提示]');
  for (const line of warns) console.log('  - ' + line);
}
process.exit(errors.length || (STRICT && warns.length) ? 1 : 0);

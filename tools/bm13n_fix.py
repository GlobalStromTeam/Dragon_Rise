# -*- coding: utf-8 -*-
"""BM-13N（喀秋莎）模型/数据修复脚本 —— 可重复执行（幂等）。

用途：
  1) 从 Blockbench 重新导出后，新模型常常丢掉 SBW 需要的辅助/标记骨骼
     （Obb*、CollisionObb、MainEngineObb、WheelLeft/RightObb*、TurretObb*、
      CannonPos*、SeatsPos*、Build），本脚本按基准比例把它们补回去；
  2) 按数据生成逻辑重算 TurretPos / BarrelPos / 座位坐标 / 16 个火箭射击点 / OBB 碰撞盒；
  3) 写入座位与炮塔配置（炮手=驾驶、0 号位控炮塔、方向机 ±10°、二号位纯乘客）。

用法：
    py tools/bm13n_fix.py [--scale 1.201] [--dry]
"""
import argparse
import collections
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, 'src', 'main', 'resources')
MODEL = os.path.join(RES, 'assets', 'dragonrise_reforge', 'models', 'bedrock', 'vehicle', 'bm13n.geo.json')
DATA = os.path.join(RES, 'data', 'dragonrise_reforge', 'sbw', 'vehicles', 'bm13n.json')
# 原始（未缩放、含全部标记骨骼）模型，作为标记骨骼的来源
REF = os.path.join(os.path.dirname(ROOT), 'Dragon_Rise_1201', '训练模型', 'bm13n.geo.json')

MARKERS = ['Obb', 'Obb1', 'Obb2', 'Obb3', 'Obb4', 'CollisionObb', 'MainEngineObb',
           'WheelLeftObb', 'WheelLeftObb2', 'WheelLeftObb3',
           'WheelRightObb', 'WheelRightObb2', 'WheelRightObb3',
           'TurretObb1', 'TurretObb2', 'CannonPos1', 'SeatsPos1', 'SeatsPos2', 'Build']

COMPACT = re.compile(r'\[\s*\n((?:\s*[-\d.eE+]+,\s*\n)*\s*[-\d.eE+]+\s*)\]')


def compact(text):
    prev = None
    while prev != text:
        prev = text
        text = COMPACT.sub(lambda m: '[' + re.sub(r'\s+', ' ', m.group(1)).strip() + ']', text)
    return text


def dump(geo, path):
    text = compact(json.dumps(geo, ensure_ascii=False, indent='\t'))
    with open(path, 'w', encoding='utf-8') as fp:
        fp.write(text)


def game(p):
    """模型坐标 -> 游戏坐标（x/16, y/16, -z/16）。"""
    return [p[0] / 16.0, p[1] / 16.0, -p[2] / 16.0]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--scale', type=float, default=1.201, help='相对原始模型的缩放比')
    ap.add_argument('--dry', action='store_true')
    args = ap.parse_args()

    model = json.load(open(MODEL, encoding='utf-8'))
    geo = model['minecraft:geometry'][0]
    bones = geo['bones']
    existing = {b['name'] for b in bones}

    # ---------- 1) 补回丢失的标记骨骼 ----------
    added = []
    if os.path.exists(REF):
        ref = json.load(open(REF, encoding='utf-8'))['minecraft:geometry'][0]
        refb = {b['name']: b for b in ref['bones']}
        for name in MARKERS:
            if name in existing or name not in refb:
                continue
            nb = json.loads(json.dumps(refb[name]))
            nb['pivot'] = [round(v * args.scale, 3) for v in nb['pivot']]
            for c in (nb.get('cubes') or []):
                c['origin'] = [round(v * args.scale, 3) for v in c['origin']]
                c['size'] = [round(v * args.scale, 3) for v in c['size']]
            bones.append(nb)
            added.append(name)
    print('补回标记骨骼:', added or '（已在，跳过）')

    names = {b['name'] for b in bones}
    dangling = [(b['name'], b.get('parent')) for b in bones if b.get('parent') and b['parent'] not in names]
    print('骨骼 %d | 悬空父级: %s' % (len(bones), dangling or '无'))

    if not args.dry:
        if added:
            dump(model, MODEL)
            print('模型已写入', os.path.basename(MODEL))

    # ---------- 2) 重算坐标 ----------
    B = {b['name']: b for b in bones}
    turret = game(B['turret']['pivot'])
    barrel_rel = [round(game(B['barrel']['pivot'])[i] - turret[i], 4) for i in range(3)]

    def seat_abs(name):
        p = B[name]['pivot']
        return [round(p[0] / 16.0, 4), round(p[1] / 16.0 - 1.61, 4), round(-p[2] / 16.0, 4)]

    s0, s1 = seat_abs('SeatsPos1'), seat_abs('SeatsPos2')

    dummies = sorted([b for b in bones if b['name'].startswith('dummy_0_0_')],
                     key=lambda b: int(b['name'].rsplit('_', 1)[1]))
    positions = []
    for b in dummies:
        a = game(b['pivot'])
        positions.append([round(a[i] - turret[i] - barrel_rel[i], 3) for i in range(3)])

    def obb_entry(name, part=None, turret_part=False):
        b = B.get(name)
        if not b or not b.get('cubes'):
            return None
        c = b['cubes'][0]
        e = collections.OrderedDict()
        e['Size'] = [round(c['size'][0] / 32.0, 4), round(c['size'][1] / 32.0, 4), round(c['size'][2] / 32.0, 4)]
        e['Position'] = [round(b['pivot'][0] / 16.0, 4), round(b['pivot'][1] / 16.0, 4), round(-b['pivot'][2] / 16.0, 4)]
        if turret_part:
            e['Part'] = 'Turret'
            e['Transform'] = 'Turret'
            e['Rotation'] = 'Turret'
        elif part:
            e['Part'] = part
        return e

    obbs = []
    for name in ('Obb1', 'Obb2', 'Obb3', 'Obb4'):
        e = obb_entry(name)
        if e:
            obbs.append(e)
    for name, part in (('CollisionObb', 'Collision'), ('MainEngineObb', 'MainEngine'),
                       ('WheelLeftObb', 'WheelLeft'), ('WheelLeftObb2', 'WheelLeft'), ('WheelLeftObb3', 'WheelLeft'),
                       ('WheelRightObb', 'WheelRight'), ('WheelRightObb2', 'WheelRight'), ('WheelRightObb3', 'WheelRight')):
        e = obb_entry(name, part)
        if e:
            obbs.append(e)
    for name in ('TurretObb1', 'TurretObb2'):
        e = obb_entry(name, turret_part=True)
        if e:
            obbs.append(e)

    # ---------- 3) 写数据 ----------
    data = json.load(open(DATA, encoding='utf-8'), object_pairs_hook=collections.OrderedDict)
    data['TurretPos'] = [round(v, 4) for v in turret]
    data['BarrelPos'] = barrel_rel
    data['TurretControllerIndex'] = 0
    data['TurretYawRange'] = [-10, 10]
    data['TurretPitchRange'] = [-5, 45]
    data['Weapons']['Rocket']['ShootPos']['Positions'] = positions
    data['OBB'] = obbs
    data['Seats'] = [
        # 0 号：驾驶员兼炮手，枢轴绑定车体（Vehicle），方向机 ±10°，炮塔跟随其视线
        collections.OrderedDict([
            ('IsEnclosed', False), ('BanHand', True),
            ('Transform', 'Vehicle'), ('RotateWithVehicle', True), ('Position', s0),
            ('MinPitch', -10), ('MaxPitch', 10),
            ('MinYaw', -10), ('MaxYaw', 10),
            ('Sensitivity', [0.52, 0.75, 0.9]),
            ('Weapons', ['Rocket']),
            # 不写 CameraPos：相机取乘员眼睛（= 座位点）；写了 UseFixedCameraPos 容易和座位点错位
        ]),
        # 1 号：纯乘客
        collections.OrderedDict([
            ('IsEnclosed', False), ('BanHand', True),
            ('Transform', 'Vehicle'), ('RotateWithVehicle', True), ('Position', s1),
            ('MinPitch', -60), ('MaxPitch', 60),
            ('MinYaw', -95), ('MaxYaw', 95),
        ]),
    ]

    if not args.dry:
        with open(DATA, 'w', encoding='utf-8') as fp:
            json.dump(data, fp, ensure_ascii=False, indent=2)

    print('TurretPos =', data['TurretPos'], '| BarrelPos =', data['BarrelPos'])
    print('座位0(驾驶兼炮手) =', s0, '| 座位1(乘客) =', s1)
    print('火箭射击点 %d 个 | OBB %d 条 | 炮塔控制器 = %d | 方向机 = %s'
          % (len(positions), len(obbs), data['TurretControllerIndex'], data['TurretYawRange']))
    if args.dry:
        print('（--dry：未写入）')


if __name__ == '__main__':
    main()

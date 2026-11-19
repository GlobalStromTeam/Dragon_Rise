"""生成护甲物品图标（离线渲染，不依赖 Blockbench）。

复用 1201 工程里的离线 Bedrock 渲染器 `tools/vehicle_icon_render.py`：
  render_icon(geo, tex, out, size, fill, yaw, pitch, near_smaller)

每套护甲只有一份 geo（三槽共用），这里按槽位过滤骨骼后再渲染，
所以头盔/胸甲/护腿三张图标各自只显示对应部位。

用法：
  python tools/make_armor_icons.py [--size 512] [--fill 0.85] [--yaw 315] [--sets haom,juaw]
"""
import argparse
import importlib.util
import json
import shutil
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ARMOR_GEO = ROOT / 'src/main/resources/assets/dragonrise_reforge/models/bedrock/armor'
ARMOR_TEX = ROOT / 'src/main/resources/assets/dragonrise_reforge/textures/armor'
ITEM_TEX = ROOT / 'src/main/resources/assets/dragonrise_reforge/textures/item'
DEFAULT_RENDERER = Path(r'E:\Dragon_Rise\Dragon_Rise_1201\tools\vehicle_icon_render.py')

NEW_SETS = ['haom', 'juaw', 'ade3', 'sawkm1', 'honf']

# 槽位 -> 该槽位在 SBM 里会显示的骨骼
SLOTS = {
    'helmet': ['armorHead'],
    'chest': ['armorBody', 'armorLeftArm', 'armorRightArm'],
    'pants': ['armorLeftLeg', 'armorRightLeg', 'armorLeftBoot', 'armorRightBoot'],
}


def load_renderer(path: Path):
    if not path.exists():
        raise SystemExit(f'找不到离线渲染器：{path}（用 --renderer 指定）')
    spec = importlib.util.spec_from_file_location('vehicle_icon_render', path)
    mod = importlib.util.module_from_spec(spec)
    sys.modules['vehicle_icon_render'] = mod
    spec.loader.exec_module(mod)
    return mod


def filter_geo(geo: dict, keep: list) -> dict:
    out = json.loads(json.dumps(geo))
    g = out['minecraft:geometry'][0]
    g['bones'] = [b for b in g['bones'] if b['name'] in keep]
    for b in g['bones']:
        b.setdefault('cubes', [])
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--renderer', default=str(DEFAULT_RENDERER))
    ap.add_argument('--size', type=int, default=512)
    ap.add_argument('--fill', type=float, default=0.85)
    ap.add_argument('--yaw', type=float, default=315.0)
    ap.add_argument('--pitch', type=float, default=26.565)
    ap.add_argument('--sets', default='')
    ap.add_argument('--texture', default='', help='强制所有套使用同一张贴图（与游戏内共用贴图一致）')
    args = ap.parse_args()

    renderer = load_renderer(Path(args.renderer))
    sets = [s for s in args.sets.split(',') if s] or NEW_SETS

    ITEM_TEX.mkdir(parents=True, exist_ok=True)
    tmp = Path(tempfile.mkdtemp(prefix='armor_icons_'))
    made = []
    for name in sets:
        geo_path = ARMOR_GEO / f'{name}.geo.json'
        tex_path = ARMOR_TEX / (args.texture or f'{name}.png')
        if not geo_path.exists():
            print(f'skip {name}: 缺 geo')
            continue
        if not tex_path.exists():
            print(f'skip {name}: 缺贴图 {tex_path.name}')
            continue
        geo = json.loads(geo_path.read_text(encoding='utf-8'))
        for slot, bones in SLOTS.items():
            sub = filter_geo(geo, bones)
            sub_path = tmp / f'{name}_{slot}.geo.json'
            sub_path.write_text(json.dumps(sub), encoding='utf-8')
            out = ITEM_TEX / f'{name}_{slot}.png'
            # near_smaller=False：depth 越大越近；否则会渲染出背面（看起来像从里往外看）
            renderer.render_icon(str(sub_path), str(tex_path), str(out), size=args.size, fill=args.fill,
                                 yaw=args.yaw, pitch=args.pitch, near_smaller=False)
            made.append((f'{name}_{slot}', out.stat().st_size))
    shutil.rmtree(tmp, ignore_errors=True)
    for n, size in made:
        print(f'icon {n}: {size} bytes')
    print(f'共 {len(made)} 张')


if __name__ == '__main__':
    main()

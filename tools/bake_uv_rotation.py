"""把 geo 里残留的 uv_rotation 烘焙进图集（SBM/GeckoLib 都不读 uv_rotation，必须烘）。

- 180：uv_size 取负即可（纯翻转，不动像素）
- 90/270：要求该面 UV 矩形在图集里是「整数像素的正方形」，就地旋转图块后清掉 uv_rotation
- 其它情况：跳过并报告（需要手工处理）

用法：
  python tools/bake_uv_rotation.py <geo.json> <texture.png> [--dry]
"""
import argparse
import json
from pathlib import Path

from PIL import Image


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('geo')
    ap.add_argument('texture')
    ap.add_argument('--dry', action='store_true')
    ap.add_argument('--keep-unsupported', action='store_true', help='无法就地烘焙的面保留 uv_rotation（默认直接移除）')
    args = ap.parse_args()

    geo_path = Path(args.geo)
    tex_path = Path(args.texture)
    data = json.loads(geo_path.read_text(encoding='utf-8'))
    g = data['minecraft:geometry'][0]
    decl_w = g['description']['texture_width']
    decl_h = g['description']['texture_height']
    img = Image.open(tex_path).convert('RGBA')
    sx = img.width / decl_w
    sy = img.height / decl_h

    changed = 0
    stripped = 0
    skipped = []
    for bone in g['bones']:
        for cube in bone.get('cubes') or []:
            for face_name, face in (cube.get('uv') or {}).items():
                rot = face.get('uv_rotation', 0) or 0
                if abs(rot) < 1e-6:
                    continue
                u, v = face['uv']
                w, h = face['uv_size']
                x0, x1 = sorted((u, u + w))
                y0, y1 = sorted((v, v + h))
                if int(round(rot)) % 180 == 0:
                    face['uv_size'] = [-w, -h]
                    face.pop('uv_rotation', None)
                    changed += 1
                    continue
                px0, px1 = int(round(x0 * sx)), int(round(x1 * sx))
                py0, py1 = int(round(y0 * sy)), int(round(y1 * sy))
                bw, bh = px1 - px0, py1 - py0
                if bw != bh or bw <= 0:
                    # 非正方形 / 非整数像素：无法就地旋转，直接丢掉该字段（SBM 本来也不读它），仅记录
                    skipped.append((bone['name'], face_name, (bw, bh)))
                    if not args.keep_unsupported:
                        face.pop('uv_rotation', None)
                        stripped += 1
                    continue
                piece = img.crop((px0, py0, px1, py1))
                # uv_rotation=90 表示贴图在面上顺时针转 90°；烘焙时把图块逆时针转回去
                piece = piece.rotate(90, expand=False)
                img.paste(piece, (px0, py0))
                face.pop('uv_rotation', None)
                changed += 1

    if skipped:
        print('无法就地烘焙（非正方形/非整数像素，已改为直接移除字段）:', skipped)
    print(f'烘焙 {changed} 个面，直接移除 {stripped} 个面')
    if args.dry:
        return
    if changed:
        img.save(tex_path)
        geo_path.write_text(json.dumps(data, indent='\t', ensure_ascii=False) + '\n', encoding='utf-8')
        print(f'已写回 {tex_path.name} / {geo_path.name}')


if __name__ == '__main__':
    main()

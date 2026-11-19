"""把护甲 geo 渲染成正交俯视图（用贴图采样），用于快速检查几何/UV/分骨骼是否正确。"""
import json
import re
import sys
from pathlib import Path
from PIL import Image, ImageDraw

ARMOR = Path(r'E:\Dragon_Rise\Dragon_Rise_1211\src\main\resources\assets\dragonrise_reforge\models\bedrock\armor')
TEX = Path(r'E:\Dragon_Rise\Dragon_Rise_1211\src\main\resources\assets\dragonrise_reforge\textures\armor')
OUT = Path(r'E:\Dragon_Rise\build\armor_preview')
OUT.mkdir(parents=True, exist_ok=True)

BONE_COLOR = {
    'armorHead': (255, 90, 90),
    'armorBody': (90, 200, 90),
    'armorLeftArm': (90, 140, 255),
    'armorRightArm': (140, 90, 255),
    'armorLeftLeg': (255, 190, 60),
    'armorRightLeg': (255, 130, 40),
    'armorLeftBoot': (60, 220, 220),
    'armorRightBoot': (40, 170, 170),
}

SCALE = 7


def face_rect(uv, uv_size):
    u0, v0 = uv
    w, h = uv_size
    x0, x1 = sorted((u0, u0 + w))
    y0, y1 = sorted((v0, v0 + h))
    return x0, y0, x1, y1


def render(geo_path, tex_path, scale=SCALE, pad=12, outline=True):
    geo = json.loads(Path(geo_path).read_text(encoding='utf-8'))['minecraft:geometry'][0]
    tw, th = geo['description']['texture_width'], geo['description']['texture_height']
    tex = Image.open(tex_path).convert('RGBA')
    tex = tex.resize((tw, th), Image.NEAREST)

    cubes = []
    for bone in geo['bones']:
        for c in bone.get('cubes') or []:
            fx = bone['name']
            cubes.append((c, fx))
    ys = [c['origin'][1] + c['size'][1] for c, _ in cubes] + [c['origin'][1] for c, _ in cubes]
    xs = [c['origin'][0] + c['size'][0] for c, _ in cubes] + [c['origin'][0] for c, _ in cubes]
    zs = [c['origin'][2] + c['size'][2] for c, _ in cubes] + [c['origin'][2] for c, _ in cubes]
    xmin, xmax, ymin, ymax = min(xs), max(xs), min(ys), max(ys)

    W = int((xmax - xmin) * scale) + pad * 2
    H = int((ymax - ymin) * scale) + pad * 2
    img = Image.new('RGBA', (W, H), (24, 26, 30, 255))
    draw = ImageDraw.Draw(img)

    def px(x, y):
        return (pad + (x - xmin) * scale, pad + (ymax - y) * scale)

    # 远的先画：按 z 中心升序（观察方向 -z）
    cubes.sort(key=lambda t: (t[0]['origin'][2] + t[0]['size'][2] / 2))
    for c, bone in cubes:
        x, y, z = c['origin']
        sx, sy, sz = c['size']
        px0, py0 = px(x, y + sy)
        px1, py1 = px(x + sx, y)
        w, h = max(1, int(round(px1 - px0))), max(1, int(round(py1 - py0)))
        uvmap = c.get('uv') or {}
        face = uvmap.get('north') or uvmap.get('south')
        if face:
            u0, v0, u1, v1 = face_rect(face['uv'], face['uv_size'])
            u0, v0 = max(0, u0), max(0, v0)
            u1, v1 = min(tw, u1), min(th, v1)
            if u1 > u0 and v1 > v0:
                patch = tex.crop((int(u0), int(v0), int(u1), int(v1))).resize((w, h), Image.NEAREST)
                img.alpha_composite(patch, (int(round(px0)), int(round(py0))))
                continue
        draw.rectangle([px0, py0, px1, py1], fill=(70, 70, 74, 255))
    if outline:
        for c, bone in cubes:
            x, y, z = c['origin']
            sx, sy, sz = c['size']
            px0, py0 = px(x, y + sy)
            px1, py1 = px(x + sx, y)
            col = BONE_COLOR.get(bone, (200, 200, 200))
            draw.rectangle([px0, py0, px1, py1], outline=col + (110,))
    # 脚底基准线
    draw.line([(0, px(0, ymin)[1]), (W, px(0, ymin)[1])], fill=(90, 90, 96, 255))
    return img


def main():
    names = [n for n in re.findall(r'--name=(\S+)', ' '.join(sys.argv))] or ['haom', 'juaw', 'ade3', 'sawkm1', 'honf']
    imgs = []
    for n in names:
        img = render(ARMOR / f'{n}.geo.json', TEX / f'{n}.png')
        img.save(OUT / f'{n}.png')
        imgs.append((n, img))
        print(n, img.size)
    if len(imgs) > 1:
        gap = 16
        W = sum(i.width for _, i in imgs) + gap * (len(imgs) - 1)
        H = max(i.height for _, i in imgs) + 22
        sheet = Image.new('RGBA', (W, H), (18, 19, 22, 255))
        x = 0
        d = ImageDraw.Draw(sheet)
        for name, im in imgs:
            sheet.alpha_composite(im, (x, 22))
            d.text((x + 6, 6), name, fill=(230, 230, 230, 255))
            x += im.width + gap
        sheet.save(OUT / 'all_armor_preview.png')
        print('sheet:', sheet.size)


if __name__ == '__main__':
    main()

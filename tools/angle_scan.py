#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""角度扫描：在一组 yaw/pitch 里找最合适的渲染视角。

两种用法：

1) 有参考图时（推荐）——与参考图做归一化剪影比对，按 IoU 排序。
       py tools/angle_scan.py m1128 --ref <官方图标.png>
       py tools/angle_scan.py m1128 --ref ref.png --mirror-as-well

2) 无参考图时——列出各角度的内容尺寸，图存到 --out 目录，供人工挑选。
       py tools/angle_scan.py m1128 --out C:/tmp/scan

默认可选角度：yaw 0/45/90/135/180/225/270/315 × pitch +26.565/-26.565
（pitch=26.565 即 atan(1/2)，对应 2:1 等距）

--ref 的比对方式是"内容框裁紧 → 等比缩放到同一边长 → IoU"，
只比形状不看配色，因此参考图与目标模型的尺寸差异不影响结论。
"""
import argparse
import json
import os
import sys

import numpy as np
from PIL import Image, ImageOps

TOOLS = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, TOOLS)
import vehicle_icon_render as v  # noqa: E402

ROOT = os.path.dirname(TOOLS)
DEFAULT_YAWS = (0, 45, 90, 135, 180, 225, 270, 315)
DEFAULT_PITCHES = (26.565, -26.565)
BOX = 112


def to_local(res, rl):
    ns, _, path = rl.partition(":")
    return os.path.join(res, "assets", ns, *path.split("/"))


def resolve(arg, root=None):
    """载具 id 或 geo 路径 -> (geo, tex, 名称)。root 默认为脚本所在项目。"""
    res = os.path.join(root or ROOT, "src", "main", "resources")
    tex_rl = ""
    if arg.endswith(".geo.json"):
        geo = os.path.abspath(arg)
        name = os.path.basename(geo)[:-len(".geo.json")]
    else:
        name = arg
        jf = os.path.join(res, "data", "dragonrise_reforge", "sbw", "vehicles", name + ".json")
        if not os.path.exists(jf):
            raise SystemExit("找不到载具 JSON: " + jf)
        with open(jf, encoding="utf-8-sig") as fp:
            d = json.load(fp)
        m = ((d.get("Models") or [{}])[0] if d.get("Models") else {}) or d.get("Model") or {}
        geo = to_local(res, m.get("Model", ""))
        tex_rl = m.get("Texture", "")
    if not os.path.exists(geo):
        raise SystemExit("找不到模型: " + geo)

    tex = to_local(res, tex_rl) if tex_rl else ""
    if not (tex and os.path.exists(tex)):
        # 从 .../assets/<ns>/models/bedrock/vehicle/<name>.geo.json 推出 textures/entity/<name>.png
        parts = geo.replace("\\", "/").split("/")
        base = "/".join(parts[:parts.index("models")]) if "models" in parts else os.path.dirname(geo)
        cand = os.path.join(base, "textures", "entity", name + ".png")
        if os.path.exists(cand):
            tex = cand
        else:
            ent = os.path.dirname(cand)
            alt = [f for f in os.listdir(ent) if f.startswith(name) and f.endswith(".png")] \
                if os.path.isdir(ent) else []
            if not alt:
                raise SystemExit("找不到贴图: " + cand)
            tex = os.path.join(ent, sorted(alt, key=len)[0])
    return geo, tex, name


def mask_of(path, box=BOX):
    """读图并归一化成 box×box 的二值剪影。"""
    im = Image.open(path).convert("RGBA")
    a = np.array(im)[:, :, 3]
    ys, xs = np.where(a > 8)
    if len(xs) == 0:
        return None
    c = im.crop((int(xs.min()), int(ys.min()), int(xs.max()) + 1, int(ys.max()) + 1))
    s = box / max(c.size)
    c = c.resize((max(1, round(c.size[0] * s)), max(1, round(c.size[1] * s))), Image.LANCZOS)
    canvas = Image.new("RGBA", (box, box), (0, 0, 0, 0))
    canvas.paste(c, ((box - c.size[0]) // 2, (box - c.size[1]) // 2))
    return np.array(canvas)[:, :, 3] > 8


def iou(a, b):
    return float((a & b).sum()) / max(int((a | b).sum()), 1)


def main():
    ap = argparse.ArgumentParser(description="角度扫描")
    ap.add_argument("target", help="载具 id 或 .geo.json 路径")
    ap.add_argument("--root", default="", help="项目根目录（默认脚本所在项目，可用于扫描另一版本）")
    ap.add_argument("--ref", default="", help="参考图标 png，给了就按 IoU 排序")
    ap.add_argument("--yaws", default=",".join(str(y) for y in DEFAULT_YAWS))
    ap.add_argument("--pitches", default=",".join(str(p) for p in DEFAULT_PITCHES))
    ap.add_argument("--size", type=int, default=128, help="渲染尺寸，默认 128")
    ap.add_argument("--fill", type=float, default=0.85, help="内容占比，默认 0.85")
    ap.add_argument("--ss", type=int, default=3, help="超采样倍数，默认 3")
    ap.add_argument("--near-smaller", action="store_true", help="旧深度方向（默认已是正确的是后者）")
    ap.add_argument("--out", default="", help="渲染图保存目录（不给则只算不存）")
    args = ap.parse_args()

    geo, tex, name = resolve(args.target, args.root or None)
    yaws = [float(s) for s in args.yaws.split(",") if s.strip()]
    pitches = [float(s) for s in args.pitches.split(",") if s.strip()]
    ref = mask_of(args.ref) if args.ref else None
    if args.ref and ref is None:
        raise SystemExit("参考图是空图: " + args.ref)

    if args.out:
        os.makedirs(args.out, exist_ok=True)

    rows = []
    for yaw in yaws:
        for pitch in pitches:
            tmp = os.path.join(args.out, "%s_y%s_p%s.png" % (
                name, ("%g" % yaw).replace("-", "m"), ("%g" % pitch).replace("-", "m"))) \
                if args.out else os.path.join(os.environ.get("TEMP", "."), "_scan_tmp.png")
            img = v.render_icon(geo, tex, tmp, size=args.size, fill=args.fill,
                                yaw=yaw, pitch=pitch, ss=args.ss,
                                near_smaller=not args.near_smaller)
            bb = img.getchannel("A").point(lambda a: 255 if a > 8 else 0).getbbox()
            cw, ch = (bb[2] - bb[0], bb[3] - bb[1]) if bb else (0, 0)
            rec = {"yaw": yaw, "pitch": pitch, "w": cw, "h": ch, "iou": None, "iou_m": None}
            if ref is not None and cw:
                m = mask_of(tmp)
                if m is not None:
                    rec["iou"] = iou(m, ref)
                    rec["iou_m"] = iou(np.array(ImageOps.mirror(Image.fromarray((m * 255).astype(np.uint8)))) > 8, ref)
            rows.append(rec)
            if not args.out and os.path.exists(tmp):
                os.remove(tmp)

    print("== %s  模型 %s" % (name, os.path.basename(geo)))
    if ref is not None:
        rows.sort(key=lambda r: -max(r["iou"], r["iou_m"]))
        print("yaw    pitch      原图IoU   镜像IoU   最佳     内容")
        for r in rows:
            best = "原图" if r["iou"] >= r["iou_m"] else "镜像"
            print("%5g %8.3f   %8.4f  %8.4f   %-5s  %dx%d" % (
                r["yaw"], r["pitch"], r["iou"], r["iou_m"], best, r["w"], r["h"]))
    else:
        print("yaw    pitch      内容尺寸")
        for r in rows:
            print("%5g %8.3f   %dx%d" % (r["yaw"], r["pitch"], r["w"], r["h"]))
        if not args.out:
            print("\n提示：加 --out <目录> 可保存每个角度的渲染图，便于肉眼比对。")


if __name__ == "__main__":
    main()

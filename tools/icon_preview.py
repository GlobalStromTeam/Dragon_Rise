#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""图标预览工具：拼图放大查看 / ASCII 剪影排查。

用法：
    py tools/icon_preview.py a.png b.png --out preview.png --cols 3
        拼成一张对照图（棋盘格底、按 --scale 放大、标注内容尺寸）

    py tools/icon_preview.py a.png --ascii
        在终端打印 alpha 剪影（排查浮空部件、朝向、内容框）

    py tools/icon_preview.py <目录> --scale 3
        目录下所有 png 一起拼图
"""
import argparse
import os
import sys

import numpy as np
from PIL import Image, ImageDraw

CHARS = " .+#"


def content_box(im):
    bb = im.getchannel("A").point(lambda a: 255 if a > 8 else 0).getbbox()
    return bb


def ascii_view(path, width=96, height=30):
    im = Image.open(path).convert("RGBA")
    a = np.array(im)[:, :, 3]
    ys, xs = np.where(a > 8)
    if len(xs) == 0:
        print("== %s\n   [空图]" % path)
        return
    x0, y0, x1, y1 = int(xs.min()), int(ys.min()), int(xs.max()), int(ys.max())
    print("== %s\n   %dx%d  内容 %dx%d  框 (%d,%d)-(%d,%d)" % (
        path, im.width, im.height, x1 - x0 + 1, y1 - y0 + 1, x0, y0, x1, y1))
    sub = (a[y0:y1 + 1, x0:x1 + 1] > 8).astype(float)
    h, w = sub.shape
    for r in range(height):
        r0, r1 = r * h // height, max((r + 1) * h // height, r * h // height + 1)
        line = ""
        for c in range(width):
            c0, c1 = c * w // width, max((c + 1) * w // width, c * w // width + 1)
            val = sub[r0:r1, c0:c1].mean()
            line += CHARS[0] if val < 0.08 else (
                CHARS[1] if val < 0.35 else (CHARS[2] if val < 0.7 else CHARS[3]))
        print("   |" + line + "|")


def checker(w, h, step=16):
    im = Image.new("RGB", (w, h), (246, 246, 248))
    dr = ImageDraw.Draw(im)
    for y in range(0, h, step):
        for x in range(0, w, step):
            if ((x // step) + (y // step)) % 2:
                dr.rectangle([x, y, x + step - 1, y + step - 1], fill=(228, 228, 234))
    return im


def montage(paths, out, scale=2, cols=1):
    rows = [p for p in paths if os.path.exists(p)]
    if not rows:
        raise SystemExit("没有可用的输入图片")
    tiles = []
    for p in rows:
        im = Image.open(p).convert("RGBA")
        bb = content_box(im)
        tiles.append((os.path.basename(p), im,
                      "%dx%d" % (bb[2] - bb[0], bb[3] - bb[1]) if bb else "空"))
    tw = max(t[1].width for t in tiles) * scale
    th = max(t[1].height for t in tiles) * scale
    pad, head = 10, 20
    cols = max(1, min(cols, len(tiles)))
    nrows = (len(tiles) + cols - 1) // cols
    canvas = Image.new("RGB", (pad + cols * (tw + pad), pad + nrows * (head + th + pad)),
                       (250, 250, 252))
    d = ImageDraw.Draw(canvas)
    for i, (name, im, note) in enumerate(tiles):
        r, c = divmod(i, cols)
        x = pad + c * (tw + pad)
        y = pad + r * (head + th + pad)
        d.text((x, y), "%s   内容 %s" % (name, note), fill=(45, 45, 60))
        box = checker(tw, th)
        big = im.resize((im.width * scale, im.height * scale), Image.LANCZOS)
        box.paste(big, (0, 0), big)
        canvas.paste(box, (x, y + 14))
        d.rectangle([x, y + 14, x + tw - 1, y + 14 + th - 1], outline=(185, 185, 195))
    canvas.save(out)
    print("预览图 -> %s  %dx%d  (%d 张)" % (out, canvas.width, canvas.height, len(tiles)))


def main():
    ap = argparse.ArgumentParser(description="图标预览 / ASCII 剪影")
    ap.add_argument("inputs", nargs="+", help="png 文件或目录")
    ap.add_argument("--out", default="", help="拼图输出路径，默认 icon_preview.png")
    ap.add_argument("--scale", type=int, default=2, help="放大倍数，默认 2")
    ap.add_argument("--cols", type=int, default=1, help="每行放几张，默认 1（竖排）")
    ap.add_argument("--ascii", action="store_true", help="打印 ASCII 剪影（不写文件）")
    ap.add_argument("--width", type=int, default=96, help="ASCII 宽度，默认 96")
    ap.add_argument("--height", type=int, default=30, help="ASCII 高度，默认 30")
    args = ap.parse_args()

    paths = []
    for p in args.inputs:
        if os.path.isdir(p):
            paths += [os.path.join(p, f) for f in sorted(os.listdir(p)) if f.endswith(".png")]
        else:
            paths.append(p)

    if args.ascii:
        for p in paths:
            ascii_view(p, args.width, args.height)
        return

    out = args.out or os.path.join(os.getcwd(), "icon_preview.png")
    montage(paths, out, args.scale, args.cols)


if __name__ == "__main__":
    main()

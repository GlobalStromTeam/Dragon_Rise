#!/usr/bin/env python3
"""校验 Blockbench 渲染图: 内容边界 / 是否贴边裁切 / 内容占比。"""
import sys
from PIL import Image
import numpy as np


def check(src):
    im = Image.open(src).convert("RGBA")
    w, h = im.size
    a = np.array(im)[:, :, 3]
    ys, xs = np.where(a > 8)
    if len(xs) == 0:
        return f"[EMPTY] {src}"
    x0, y0, x1, y1 = int(xs.min()), int(ys.min()), int(xs.max()), int(ys.max())
    cw, ch = x1 - x0 + 1, y1 - y0 + 1
    touch = []
    if x0 == 0: touch.append("L")
    if y0 == 0: touch.append("T")
    if x1 == w - 1: touch.append("R")
    if y1 == h - 1: touch.append("B")
    return (f"{src}\n  画布 {w}x{h}  内容 {cw}x{ch}  框 ({x0},{y0})-({x1},{y1})\n"
            f"  占画布 max {100*max(cw,ch)/w:.0f}%  贴边 [{','.join(touch) or 'none'}]")


if __name__ == "__main__":
    for p in sys.argv[1:]:
        print(check(p))

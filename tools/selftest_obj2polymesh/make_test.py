# -*- coding: utf-8 -*-
"""生成用于验证 obj2polymesh 的最小 obj + 贴图。"""
import os
import numpy as np
from PIL import Image

OUT = os.path.dirname(os.path.abspath(__file__))

FACES = [
    # name, 顶点索引(逆时针), 法线
    ("top",    (4, 5, 6, 7), (0, 1, 0)),
    ("bottom", (0, 1, 2, 3), (0, -1, 0)),
    ("north",  (0, 1, 5, 4), (0, 0, -1)),
    ("south",  (3, 2, 6, 7), (0, 0, 1)),
    ("west",   (0, 3, 7, 4), (-1, 0, 0)),
    ("east",   (1, 2, 6, 5), (1, 0, 0)),
]
# 6 个面 -> 贴图纵向 6 条色带
BAND = {name: i for i, (name, _, _) in enumerate(FACES)}


def emit_box(name, x0, x1, y0, y1, z0, z1, st):
    verts = [
        (x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1),
        (x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1),
    ]
    st["lines"].append("g %s" % name)
    for v in verts:
        st["lines"].append("v %.4f %.4f %.4f" % v)
        st["v"] += 1
    for fname, idxs, n in FACES:
        st["lines"].append("vn %.1f %.1f %.1f" % n)
        st["vn"] += 1
        i = BAND[fname]
        # obj/OpenGL 的 v 轴向上：贴图第 i 条色带（自上而下）对应 v 从 1-(i+1)/6 到 1-i/6
        vb, vt_ = 1.0 - (i + 1) / 6.0, 1.0 - i / 6.0
        uvs = [(0.05, vb), (0.95, vb), (0.95, vt_), (0.05, vt_)]
        uv_base = st["vt"]
        for uv in uvs:
            st["lines"].append("vt %.4f %.4f" % uv)
            st["vt"] += 1
        tok = []
        for k in range(4):
            tok.append("%d/%d/%d" % (st["v"] - 8 + idxs[k] + 1, uv_base + k + 1, st["vn"]))
        st["lines"].append("f " + " ".join(tok))


def main():
    st = {"lines": ["o test_vehicle"], "v": 0, "vt": 0, "vn": 0}
    emit_box("body", -8, 8, 0, 8, -16, 16, st)
    emit_box("turret", -4, 4, 8, 13, -6, 6, st)
    with open(os.path.join(OUT, "test_vehicle.obj"), "w", encoding="utf-8") as fp:
        fp.write("\n".join(st["lines"]) + "\n")

    tex = np.zeros((64, 64, 4), dtype=np.uint8)
    cols = [(200, 70, 60), (60, 110, 200), (80, 170, 90),
            (220, 190, 70), (150, 90, 190), (120, 120, 120)]
    for i, c in enumerate(cols):
        y0 = int(i * 64 / 6)
        y1 = int((i + 1) * 64 / 6)
        tex[y0:y1, :, 0:3] = c
        tex[y0:y1, :, 3] = 255
    for x in range(0, 64, 8):
        tex[:, x, 0:3] = np.minimum(tex[:, x, 0:3].astype(int) + 60, 255)
    Image.fromarray(tex, "RGBA").save(os.path.join(OUT, "test_vehicle.png"))
    print("ok verts=%d vt=%d vn=%d" % (st["v"], st["vt"], st["vn"]))


if __name__ == "__main__":
    main()

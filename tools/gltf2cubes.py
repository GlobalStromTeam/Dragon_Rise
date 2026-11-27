#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""gltf2cubes.py —— glTF/GLB -> 立方体版 bedrock geo.json（Blockbench 里可编辑的那种）。

思路对齐 Blockbench 的 "glTF to Minecraft" 插件，但按这批模型的特点做了取舍：

  * **一个 primitive 拟合成一个 cube**：这些模型本来就是 Blockbench 导出的方块，
    每个 cube 在 glTF 里就是一个 primitive；按「连通件」合并会把整块外壳并成一个大盒子，
    反而把结构做丢。
  * 逐面 UV：把落在该面上的三角面采样出来，按 Blockbench 实测的 u/v 增长方向取极值，
    得到 uv + uv_size（像素）。
  * 多材质自动拼图集（SBM/GeckoLib 只吃一张贴图）。
  * 骨骼 = glTF 节点；节点名按关键词映射成 move_propeller / move_tailPropeller / turret 等。

用法：
  python gltf2cubes.py 模型.gltf --out out/x.geo.json --target-length 8 --yaw 180 --merge-static
"""

from __future__ import annotations

import argparse
import json
import math
import os
import re
import sys
from collections import OrderedDict

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gltf2polymesh import Gltf, walk_nodes, sanitize, triangulate  # noqa: E402

FACE_DIRS = {
    "north": ((0, 0, -1), (-1, 0, 0), (0, -1, 0)),
    "south": ((0, 0, 1), (1, 0, 0), (0, -1, 0)),
    "east": ((1, 0, 0), (0, 0, -1), (0, -1, 0)),
    "west": ((-1, 0, 0), (0, 0, 1), (0, -1, 0)),
    "up": ((0, 1, 0), (1, 0, 0), (0, 0, 1)),
    "down": ((0, -1, 0), (1, 0, 0), (0, 0, -1)),
}

PRESERVE = [re.compile(p) for p in (
    r"^w_.*", r"^root$", r"^move_.*", r"^flare.*", r"^laser.*", r"^wheel[LR].*", r"^track.*",
    r"^shell.*", r"^dummy_.*", r"^waterMask$", r"^passengerWeaponStation$", r"^base$",
    r"^turret$", r"^barrel$", r"^passengerWeaponStationYaw$", r"^passengerWeaponStationPitch$",
)]

RE_TAIL = re.compile(r"(tail.?rotor|tail.?prop|尾桨)", re.I)
RE_MAIN = re.compile(r"(main.?rotor|rotor.?main|propeller|propello|rotor|桨|旋翼)", re.I)
RE_TURRET = re.compile(r"(turret|gun.?mount|炮塔)", re.I)
RE_BARREL = re.compile(r"(barrel|炮管)", re.I)
RE_TRACK = re.compile(r"(track|caterpillar|履带)", re.I)
RE_WHEEL = re.compile(r"(wheel|tire|tyre|轮)", re.I)


def bone_name_for(node_name, used):
    name = sanitize(node_name)
    if RE_TAIL.search(name):
        base = "move_tailPropeller"
    elif RE_MAIN.search(name):
        base = "move_propeller"
    elif RE_TURRET.search(name):
        base = "turret"
    elif RE_BARREL.search(name):
        base = "barrel"
    elif RE_TRACK.search(name):
        base = "track_" + name
    elif RE_WHEEL.search(name):
        base = "wheel_" + name
    else:
        base = name
    out, i = base, 2
    while out in used:
        out = "%s%d" % (base, i)
        i += 1
    used.add(out)
    return out


def is_preserved(name):
    return any(p.match(name) for p in PRESERVE)


def material_key(gltf, mats, mi):
    if mi is None or mi >= len(mats):
        return "(default)"
    mat = mats[mi]
    tex = ((mat.get("pbrMetallicRoughness") or {}).get("baseColorTexture") or {}).get("index")
    if tex is None:
        return mat.get("name") or "material%d" % mi
    src = gltf.doc["textures"][tex].get("source")
    if src is None:
        return mat.get("name") or "material%d" % mi
    img = gltf.doc["images"][src]
    return img.get("uri") or img.get("name") or "image%d" % src


def collect(gltf):
    """-> OrderedDict[bone] = {"pivot": xyz, "prims": [ [(pos3, uv3, matkey), ...], ... ]}"""
    doc = gltf.doc
    mats = doc.get("materials", [])
    used = set()
    bones = OrderedDict()
    for idx, raw_name, _parent_idx, mat in walk_nodes(gltf):
        node = doc["nodes"][idx]
        if "mesh" not in node:
            continue
        bname = bone_name_for(raw_name, used)
        b = bones.setdefault(bname, {"pivot": mat[:3, 3].copy(), "prims": []})
        rot = mat[:3, :3]
        for prim in doc["meshes"][node["mesh"]].get("primitives", []):
            if "POSITION" not in prim["attributes"]:
                continue
            tri = triangulate(gltf, prim)
            if tri is None or len(tri) == 0:
                continue
            pos = gltf.accessor(prim["attributes"]["POSITION"]) @ rot.T + mat[:3, 3]
            uv = (gltf.accessor(prim["attributes"]["TEXCOORD_0"])[:, :2]
                  if "TEXCOORD_0" in prim["attributes"] else np.zeros((len(pos), 2)))
            key = material_key(gltf, mats, prim.get("material"))
            b["prims"].append([(pos[t], uv[t], key) for t in tri])
    return bones


def build_atlas(gltf, keys, tile=512):
    from PIL import Image
    import io
    n = max(1, int(math.ceil(math.sqrt(len(keys)))))
    atlas = Image.new("RGBA", (tile * n, tile * n), (128, 128, 128, 255))
    slots = {}
    for i, key in enumerate(keys):
        col, row = i % n, i // n
        slots[key] = (col * tile, row * tile)
        for img in gltf.doc.get("images", []):
            if (img.get("uri") or img.get("name")) != key:
                continue
            try:
                if str(img.get("uri", "")).startswith("data:"):
                    import base64
                    im = Image.open(io.BytesIO(base64.b64decode(img["uri"].partition(",")[2])))
                elif img.get("bufferView") is not None:
                    view = gltf.doc["bufferViews"][img["bufferView"]]
                    buf = gltf.buffer(view["buffer"])
                    base = view.get("byteOffset", 0)
                    im = Image.open(io.BytesIO(buf[base: base + view["byteLength"]]))
                else:
                    im = Image.open(os.path.join(gltf.dir, img["uri"]))
                atlas.alpha_composite(im.convert("RGBA").resize((tile, tile), Image.LANCZOS),
                                      (col * tile, row * tile))
            except Exception:
                pass
            break
    return atlas, slots, n


def fit_cube(tris, tol_rel=1e-3):
    """一组三角面 -> (lo, size, {face: (u0,v0,u1,v1)})；uv 为归一化值。"""
    pos = np.concatenate([t[0] for t in tris])
    lo, hi = pos.min(axis=0), pos.max(axis=0)
    size = hi - lo
    span = float(max(size.max(), 1e-9))
    tol = max(span * tol_rel, 1e-6)
    center = (lo + hi) / 2.0
    half = size / 2.0
    axis_tol = np.minimum(tol, np.maximum(half * 0.5, span * 1e-9))

    samples = {k: [] for k in FACE_DIRS}
    for p, uv, _k in tris:
        n = np.cross(p[1] - p[0], p[2] - p[0])
        ln = np.linalg.norm(n)
        if ln < 1e-12:
            continue
        n = n / ln
        for fname, (fn, _u, _v) in FACE_DIRS.items():
            fn = np.asarray(fn, dtype=float)
            if float(np.dot(n, fn)) < 0.7:
                continue
            axis = int(np.argmax(np.abs(fn)))
            if not all(abs(float(np.dot(q - center, fn)) - half[axis]) < axis_tol[axis] for q in p):
                continue
            for k in range(3):
                samples[fname].append((p[k] - center, uv[k]))

    face_uv = OrderedDict()
    for fname, (_fn, du, dv) in FACE_DIRS.items():
        s = samples[fname]
        if not s:
            continue
        du = np.asarray(du, dtype=float)
        dv = np.asarray(dv, dtype=float)
        pu = [float(np.dot(sp, du)) for sp, _ in s]
        pv = [float(np.dot(sp, dv)) for sp, _ in s]
        iu0, iu1 = int(np.argmin(pu)), int(np.argmax(pu))
        iv0, iv1 = int(np.argmin(pv)), int(np.argmax(pv))
        u0, u1 = float(s[iu0][1][0]), float(s[iu1][1][0])
        v0, v1 = float(s[iv0][1][1]), float(s[iv1][1][1])
        if abs(pu[iu1] - pu[iu0]) < tol:
            u0, u1 = min(u0, u1), max(u0, u1)
        if abs(pv[iv1] - pv[iv0]) < tol:
            v0, v1 = min(v0, v1), max(v0, v1)
        face_uv[fname] = (u0, v0, u1, v1)
    if not face_uv:
        return None
    return lo, size, face_uv


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("model")
    ap.add_argument("--out", required=True)
    ap.add_argument("--scale", type=float, default=None)
    ap.add_argument("--target-length", type=float, default=8.0)
    ap.add_argument("--yaw", type=float, default=0.0)
    ap.add_argument("--tile", type=int, default=512)
    ap.add_argument("--merge-static", action="store_true")
    args = ap.parse_args(argv)

    gltf = Gltf(args.model)
    bones = collect(gltf)
    if not bones:
        raise SystemExit("glTF 里没有三角网格")

    allpos = np.concatenate([t[0] for b in bones.values() for prim in b["prims"] for t in prim])
    lo0, hi0 = allpos.min(axis=0), allpos.max(axis=0)
    longest = float((hi0 - lo0).max()) or 1.0
    scale = args.scale if args.scale else args.target_length * 16.0 / longest

    keys = []
    for b in bones.values():
        for prim in b["prims"]:
            for _p, _uv, k in prim:
                if k not in keys:
                    keys.append(k)
    atlas, slots, n = build_atlas(gltf, keys, args.tile)
    tw, th = atlas.width, atlas.height

    def xform(p):
        q = np.asarray(p, dtype=float).copy()
        q[..., 0] *= -1.0
        if args.yaw % 360:
            a = math.radians(args.yaw)
            ca, sa = math.cos(a), math.sin(a)
            x = q[..., 0] * ca + q[..., 2] * sa
            z = -q[..., 0] * sa + q[..., 2] * ca
            q[..., 0], q[..., 2] = x, z
        return q * scale

    per_bone = OrderedDict()
    total = failed = 0
    for bname, b in bones.items():
        cubes = []
        for prim in b["prims"]:
            tris = []
            for p, uv, key in prim:
                pp = xform(p)[[0, 2, 1]]          # x 取反会翻绕序，换顺序保住外法线
                uu = np.asarray(uv)[[0, 2, 1]]
                ox, oy = slots[key]
                u = (uu[:, 0] * args.tile + ox) / tw
                v = (1.0 - uu[:, 1]) * args.tile / th + (th - (oy + args.tile)) / th
                tris.append((pp, np.stack([u, v], axis=1), key))
            sol = fit_cube(tris)
            if sol is None:
                failed += 1
                continue
            lo, size, face_uv = sol
            uv_obj = OrderedDict()
            for fname, (u0, v0, u1, v1) in face_uv.items():
                x0, y0 = u0 * tw, v0 * th
                w, h = (u1 - u0) * tw, (v1 - v0) * th
                if w < 0:
                    x0 += w
                    w = -w
                if h < 0:
                    y0 += h
                    h = -h
                if w < 1e-6 or h < 1e-6:
                    continue
                uv_obj[fname] = OrderedDict([("uv", [round(x0, 2), round(y0, 2)]),
                                             ("uv_size", [round(w, 2), round(h, 2)])])
            if not uv_obj:
                failed += 1
                continue
            cube = OrderedDict()
            # lo 已经在 stored 空间（x 已取反），origin 就是角点
            cube["origin"] = [round(float(v), 3) for v in lo]
            cube["size"] = [round(float(v), 3) for v in size]
            cube["uv"] = uv_obj
            cubes.append(cube)
        if cubes:
            per_bone[bname] = {"pivot": b["pivot"], "cubes": cubes}
            total += len(cubes)

    if args.merge_static:
        merged = OrderedDict()
        body = None
        for name, b in per_bone.items():
            if is_preserved(name):
                merged[name] = b
                continue
            if body is None:
                body = {"pivot": np.zeros(3), "cubes": []}
                merged["body"] = body
            body["cubes"].extend(b["cubes"])
        per_bone = merged

    out_bones = [OrderedDict([("name", "base"), ("pivot", [0.0, 0.0, 0.0])])]
    for bname, b in per_bone.items():
        bone = OrderedDict()
        bone["name"] = bname
        bone["parent"] = "base"
        pv = xform(b["pivot"])
        bone["pivot"] = [round(float(v), 3) for v in pv]
        bone["cubes"] = b["cubes"]
        out_bones.append(bone)

    stem = re.sub(r"[^0-9A-Za-z_]", "_", os.path.splitext(os.path.basename(args.out))[0]).lower()
    desc = OrderedDict()
    desc["identifier"] = "geometry." + stem
    desc["texture_width"] = tw
    desc["texture_height"] = th
    desc["visible_bounds_width"] = round(float((hi0[0] - lo0[0]) * scale / 16 + 1), 2)
    desc["visible_bounds_height"] = round(float((hi0[1] - lo0[1]) * scale / 16 + 1), 2)
    desc["visible_bounds_offset"] = [0.0, round(float((hi0[1] - lo0[1]) * scale / 32), 2), 0.0]

    geo = OrderedDict()
    geo["description"] = desc
    geo["bones"] = out_bones
    root = OrderedDict([("format_version", "1.12.0"), ("minecraft:geometry", [geo])])

    os.makedirs(os.path.dirname(os.path.abspath(args.out)) or ".", exist_ok=True)
    with open(args.out, "w", encoding="utf-8") as fp:
        json.dump(root, fp, ensure_ascii=False, separators=(",", ":"))
    atlas.save(os.path.splitext(args.out)[0] + ".png")

    size_blocks = [round(float((hi0[i] - lo0[i]) * scale / 16), 2) for i in range(3)]
    print("%-32s cube=%-5d 骨骼=%-3d 尺寸=%-20s 缩放=%.3f 图集=%dx%d 空件=%d"
          % (os.path.basename(args.out), total, len(out_bones) - 1, size_blocks, scale, tw, th, failed))
    return 0


if __name__ == "__main__":
    sys.exit(main())

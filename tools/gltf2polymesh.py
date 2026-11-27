#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""gltf2polymesh.py —— 把 glTF / GLB 转成 SBM(SimpleBedrockModel) 能吃的 poly_mesh geo.json。

glTF 自带节点树，所以「一个节点 = 一根骨骼」，不需要像 OBJ 那样靠几何去猜：

  * 节点名按关键词自动改名成 SBW 认的骨骼名：
      主旋翼/螺旋桨 -> move_propeller        尾桨 -> move_tailPropeller
      炮塔 -> turret      炮管 -> barrel      履带 -> track_*      轮 -> wheel_*
  * 骨骼 pivot = 节点原点在世界空间的位置，几何写在骨骼自己的 poly_mesh 里，
    所以旋转它就是绕它自己的原点转。
  * 多材质模型自动做**贴图图集**：每张基色贴图缩进 N×N 图集的一格，UV 同步缩放，
    输出「一个 geo.json + 一张 png」，符合 SBM 单贴图的限制。
  * 单位未知时用 --target-length 指定最长边要多少格，自动算缩放。

用法：
  python gltf2polymesh.py 模型.gltf --out out/btr82a.geo.json --target-length 8 --yaw 180
  python gltf2polymesh.py 模型.glb  --out ... --scale 16          # 已知单位时直接给缩放
"""

from __future__ import annotations

import argparse
import base64
import io
import json
import math
import os
import re
import struct
import sys
from collections import OrderedDict

import numpy as np

COMPONENT = {5120: ("b", 1), 5121: ("B", 1), 5122: ("h", 2), 5123: ("H", 2), 5125: ("I", 4), 5126: ("f", 4)}
NCOMP = {"SCALAR": 1, "VEC2": 2, "VEC3": 3, "VEC4": 4, "MAT4": 16}


# ---------------------------------------------------------------- glTF 读取


class Gltf:
    def __init__(self, path):
        self.dir = os.path.dirname(os.path.abspath(path))
        if path.lower().endswith(".glb"):
            self.doc, self.bin_chunk = self._read_glb(path)
        else:
            with open(path, encoding="utf-8") as fp:
                self.doc = json.load(fp)
            self.bin_chunk = None

    @staticmethod
    def _read_glb(path):
        with open(path, "rb") as fp:
            data = fp.read()
        magic, _v, length = struct.unpack_from("<III", data, 0)
        if magic != 0x46546C67:
            raise SystemExit("不是 GLB 文件")
        off, doc, bin_chunk = 12, None, None
        while off < length:
            clen, ctype = struct.unpack_from("<II", data, off)
            chunk = data[off + 8: off + 8 + clen]
            if ctype == 0x4E4F534A:
                doc = json.loads(chunk.decode("utf-8"))
            elif ctype == 0x004E4942:
                bin_chunk = chunk
            off += 8 + clen + ((4 - clen % 4) % 4)
        if doc is None:
            raise SystemExit("GLB 里没有 JSON 块")
        return doc, bin_chunk

    def buffer(self, index):
        buf = self.doc["buffers"][index]
        uri = buf.get("uri")
        if uri is None:
            if self.bin_chunk is None:
                raise SystemExit("buffer %d 无 uri 且不是 GLB" % index)
            return self.bin_chunk
        if uri.startswith("data:"):
            return base64.b64decode(uri.partition(",")[2])
        with open(os.path.join(self.dir, uri), "rb") as fp:
            return fp.read()

    def accessor(self, index):
        acc = self.doc["accessors"][index]
        n, nc = acc["count"], NCOMP[acc["type"]]
        fmt, size = COMPONENT[acc["componentType"]]
        if "bufferView" not in acc:
            return np.zeros((n, nc), dtype=np.float64)
        view = self.doc["bufferViews"][acc["bufferView"]]
        data = self.buffer(view["buffer"])
        base = view.get("byteOffset", 0) + acc.get("byteOffset", 0)
        stride = view.get("byteStride") or size * nc
        dt = np.dtype("<" + fmt)
        if stride == size * nc:
            arr = np.frombuffer(data, dtype=dt, count=n * nc, offset=base).reshape(n, nc)
        else:
            arr = np.empty((n, nc), dtype=dt)
            for i in range(n):
                arr[i] = np.frombuffer(data, dtype=dt, count=nc, offset=base + i * stride)
        return arr.astype(np.float64)

    def image_path(self, index):
        uri = self.doc["images"][index].get("uri")
        return None if (not uri or uri.startswith("data:")) else os.path.join(self.dir, uri)

    def image_bytes(self, index):
        img = self.doc["images"][index]
        uri = img.get("uri")
        if uri and uri.startswith("data:"):
            return base64.b64decode(uri.partition(",")[2])
        if img.get("bufferView") is not None:
            view = self.doc["bufferViews"][img["bufferView"]]
            data = self.buffer(view["buffer"])
            base = view.get("byteOffset", 0)
            return data[base: base + view["byteLength"]]
        return None


def node_matrix(node):
    if "matrix" in node:
        return np.asarray(node["matrix"], dtype=np.float64).reshape(4, 4).T
    m = np.eye(4)
    if "translation" in node:
        m[:3, 3] = node["translation"]
    if "rotation" in node:
        x, y, z, w = node["rotation"]
        m = m @ np.array([
            [1 - 2 * (y * y + z * z), 2 * (x * y - z * w), 2 * (x * z + y * w), 0],
            [2 * (x * y + z * w), 1 - 2 * (x * x + z * z), 2 * (y * z - x * w), 0],
            [2 * (x * z - y * w), 2 * (y * z + x * w), 1 - 2 * (x * x + y * y), 0],
            [0, 0, 0, 1]])
    if "scale" in node:
        s = np.eye(4)
        s[0, 0], s[1, 1], s[2, 2] = node["scale"]
        m = m @ s
    return m


def walk_nodes(gltf):
    doc = gltf.doc
    scene = doc.get("scenes", [{}])[doc.get("scene", 0)] if doc.get("scenes") else {}
    roots = scene.get("nodes") or list(range(len(doc.get("nodes", []))))
    out = []

    def rec(idx, parent, parent_m):
        node = doc["nodes"][idx]
        m = parent_m @ node_matrix(node)
        out.append((idx, node.get("name") or ("node%d" % idx), parent, m))
        for c in node.get("children", []):
            rec(c, idx, m)

    for r in roots:
        rec(r, None, np.eye(4))
    return out


# ---------------------------------------------------------------- 命名/坐标


def _rot_y(p, deg):
    if deg % 360:
        a = math.radians(deg)
        ca, sa = math.cos(a), math.sin(a)
        x, y, z = p
        return (x * ca + z * sa, y, -x * sa + z * ca)
    return p


def sanitize(name):
    s = re.sub(r"[^0-9A-Za-z_\-.]", "_", name or "bone")
    if not s or s[0].isdigit():
        s = "b_" + s
    return s


RE_TAIL = re.compile(r"(tail.?rotor|tail.?prop|rear.?rotor|尾桨)", re.I)
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


# ---------------------------------------------------------------- 采集


def material_key(gltf, mat_index):
    doc = gltf.doc
    mats = doc.get("materials", [])
    if mat_index is None or mat_index >= len(mats):
        return "(default)", None, None
    mat = mats[mat_index]
    tex = ((mat.get("pbrMetallicRoughness") or {}).get("baseColorTexture") or {}).get("index")
    if tex is None:
        return (mat.get("name") or "material%d" % mat_index), None, None
    src = doc["textures"][tex].get("source")
    if src is None:
        return (mat.get("name") or "material%d" % mat_index), None, None
    img = doc["images"][src]
    return (img.get("uri") or img.get("name") or "image%d" % src,
            gltf.image_path(src), gltf.image_bytes(src))


def triangulate(gltf, prim):
    """把一个 primitive 统一成 (n,3) 的三角形索引表。

    glTF 的 mode 不一定都是 4（TRIANGLES）：
      4 TRIANGLES      -> 每 3 个一组
      5 TRIANGLE_STRIP -> 滚动取相邻 3 个，奇数位翻转绕序
      6 TRIANGLE_FAN   -> 以首顶点为扇心
      0/1/2/3（点、线）不产生面，返回 None
    无 indices 时按顶点顺序生成索引。
    """
    mode = prim.get("mode", 4)
    count = gltf.doc["accessors"][prim["attributes"]["POSITION"]]["count"]
    if "indices" in prim:
        idx = gltf.accessor(prim["indices"]).astype(np.int64).reshape(-1)
    else:
        idx = np.arange(count, dtype=np.int64)
    if idx.size < 3:
        return None
    if mode == 4:
        n = (idx.size // 3) * 3
        if n < 3:
            return None
        return idx[:n].reshape(-1, 3)
    if mode == 5:  # strip
        quads = np.stack([idx[:-2], idx[1:-1], idx[2:]], axis=1)
        # 奇数三角形绕序要翻，否则法线朝里
        flip = np.arange(len(quads)) % 2 == 1
        quads[flip] = quads[flip][:, [0, 2, 1]]
        return quads
    if mode == 6:  # fan
        center = np.repeat(idx[:1], idx.size - 2)
        return np.stack([center, idx[1:-1], idx[2:]], axis=1)
    return None


def collect(gltf):
    """-> (bones, materials)

    bones: OrderedDict[name] = {"pivot": (x,y,z) 原始单位, "pos": [...], "uv": [...], "poly": [...]}
    materials: OrderedDict[key] = {"path":..., "bytes":...}
    """
    doc = gltf.doc
    used = set()
    nodes = walk_nodes(gltf)
    bone_of, pivot_of = {}, {}
    for idx, raw_name, parent_idx, mat in nodes:
        bone_of[idx] = bone_name_for(raw_name, used)
        pivot_of[idx] = mat[:3, 3].copy()

    bones = OrderedDict()
    materials = OrderedDict()
    for idx, raw_name, parent_idx, mat in nodes:
        node = doc["nodes"][idx]
        if "mesh" not in node:
            continue
        for prim in doc["meshes"][node["mesh"]].get("primitives", []):
            if "POSITION" not in prim["attributes"]:
                continue
            tri = triangulate(gltf, prim)
            if tri is None or len(tri) == 0:
                continue
            rot = mat[:3, :3]
            pos = gltf.accessor(prim["attributes"]["POSITION"]) @ rot.T + mat[:3, 3]
            uv = (gltf.accessor(prim["attributes"]["TEXCOORD_0"])[:, :2]
                  if "TEXCOORD_0" in prim["attributes"] else np.zeros((len(pos), 2)))
            key, tpath, tbytes = material_key(gltf, prim.get("material"))
            materials.setdefault(key, {"path": tpath, "bytes": tbytes})
            b = bones.setdefault(bone_of[idx], {"pivot": pivot_of[idx], "pos": [], "uv": [], "poly": [],
                                                "mat": key})
            base_i = len(b["pos"])
            b["pos"].extend(pos.tolist())
            b["uv"].extend(uv.tolist())
            for t in tri:
                b["poly"].append([[int(t[0] + base_i), 0, int(t[0] + base_i)],
                                  [int(t[1] + base_i), 0, int(t[1] + base_i)],
                                  [int(t[2] + base_i), 0, int(t[2] + base_i)]])
    return bones, materials


def build_atlas(materials, tile=512):
    """所有基色贴图拼成 N×N 图集，返回 (PIL.Image, {key: (col,row)}, n)"""
    from PIL import Image

    n = max(1, int(math.ceil(math.sqrt(len(materials)))))
    atlas = Image.new("RGBA", (tile * n, tile * n), (128, 128, 128, 255))
    slots = {}
    for i, (key, info) in enumerate(materials.items()):
        col, row = i % n, i // n
        slots[key] = (col, row)
        im = None
        try:
            if info["bytes"] is not None:
                im = Image.open(io.BytesIO(info["bytes"]))
            elif info["path"] and os.path.exists(info["path"]):
                im = Image.open(info["path"])
        except Exception:
            im = None
        if im is None:
            continue
        im = im.convert("RGBA").resize((tile, tile), Image.LANCZOS)
        atlas.alpha_composite(im, (col * tile, row * tile))
    return atlas, slots, n


def apply_axis(pos, yaw):
    out = pos.copy()
    out[:, 0] *= -1.0
    if yaw % 360:
        a = math.radians(yaw)
        ca, sa = math.cos(a), math.sin(a)
        x = out[:, 0] * ca + out[:, 2] * sa
        z = -out[:, 0] * sa + out[:, 2] * ca
        out[:, 0], out[:, 2] = x, z
    return out


# 与 superbwarfare VehicleModelReloadListener.PATTERNS 对齐：只有这些名字会被烘焙成「运行时骨骼」
PRESERVE = [re.compile(p) for p in (
    r"^w_.*", r"^root$", r"^move_.*", r"^flare.*", r"^laser.*", r"^wheel[LR].*", r"^track.*",
    r"^shell.*", r"^dummy_.*", r"^waterMask$", r"^passengerWeaponStation$", r"^base$",
    r"^turret$", r"^barrel$", r"^passengerWeaponStationYaw$", r"^passengerWeaponStationPitch$",
)]


def is_preserved(name):
    return any(p.match(name) for p in PRESERVE)


def merge_static(bones):
    """把所有不会被烘焙保留的骨骼合并成一根 body，文件小很多、烘焙也快。"""
    out = OrderedDict()
    body = None
    for name, b in bones.items():
        if is_preserved(name):
            out[name] = b
            continue
        if body is None:
            body = {"pivot": np.zeros(3), "pos": [], "uv": [], "poly": [], "mat": b["mat"]}
            out["body"] = body
        off = len(body["pos"])
        body["pos"].extend(b["pos"])
        body["uv"].extend(b["uv"])
        for poly in b["poly"]:
            body["poly"].append([[e[0] + off, 0, e[0] + off] for e in poly])
    return out


def split_cluster(bones, axis):
    """按某轴上的最大空隙把模型切成两份，只保留靠前的那份（模型里常有两台车并排）。"""
    idx = "xyz".index(axis)
    allp = np.concatenate([np.asarray(b["pos"], dtype=np.float64) for b in bones.values() if b["pos"]])
    v = np.sort(allp[:, idx])
    if len(v) < 10:
        return bones
    gaps = np.diff(v)
    k = int(np.argmax(gaps))
    cut = (v[k] + v[k + 1]) / 2.0
    extent = v[-1] - v[0]
    if gaps[k] < 0.08 * extent:
        print("  (auto-split: %s 轴上没有明显空隙，未拆分)" % axis)
        return bones
    print("  auto-split: %s=%.2f 处切开（原范围 %.2f..%.2f）" % (axis, cut, v[0], v[-1]))
    out = OrderedDict()
    for name, b in bones.items():
        if not b["pos"]:
            continue
        keep_pos, keep_uv, poly = [], [], []
        P = np.asarray(b["pos"], dtype=np.float64)
        for p in b["poly"]:
            c = P[[e[0] for e in p]][:, idx].mean()
            if c > cut:
                continue
            off = len(keep_pos)
            for e in p:
                keep_pos.append(b["pos"][e[0]])
                keep_uv.append(b["uv"][e[0]])
            poly.append([[off, 0, off], [off + 1, 0, off + 1], [off + 2, 0, off + 2]])
        if poly:
            out[name] = {"pivot": b["pivot"], "pos": keep_pos, "uv": keep_uv, "poly": poly, "mat": b["mat"]}
    return out


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("model")
    ap.add_argument("--out", required=True)
    ap.add_argument("--scale", type=float, default=None, help="glTF 单位 -> 模型单位（16 = 1格）")
    ap.add_argument("--target-length", type=float, default=8.0,
                    help="自动缩放：最长边多少格（--scale 优先）")
    ap.add_argument("--yaw", type=float, default=0.0, help="整体绕 Y 旋转；车头朝 -Z 通常要 180")
    ap.add_argument("--tile", type=int, default=512, help="图集每格边长")
    ap.add_argument("--merge-static", action="store_true",
                    help="把不会被烘焙保留的骨骼合并成一根 body（文件小、烘焙快）")
    ap.add_argument("--auto-split", default=None, choices=["x", "y", "z"],
                    help="模型里并排放了多份时，按该轴最大空隙切开只保留一份")
    args = ap.parse_args(argv)

    gltf = Gltf(args.model)
    bones, materials = collect(gltf)
    if not bones:
        raise SystemExit("glTF 里没有可用的三角网格（mode=4）")
    if args.auto_split:
        bones = split_cluster(bones, args.auto_split)
    if args.merge_static:
        bones = merge_static(bones)

    raw = np.concatenate([np.asarray(b["pos"], dtype=np.float64) for b in bones.values() if b["pos"]])
    lo, hi = raw.min(axis=0), raw.max(axis=0)
    longest = float((hi - lo).max()) or 1.0
    scale = args.scale if args.scale else (args.target_length * 16.0 / longest)

    atlas, slots, n = build_atlas(materials, args.tile)

    out_bones = [OrderedDict([("name", "base"), ("pivot", [0.0, 0.0, 0.0])])]
    all_lo = np.array([1e18] * 3)
    all_hi = np.array([-1e18] * 3)
    tris = 0
    for bname, b in bones.items():
        if not b["pos"]:
            continue
        pos = apply_axis(np.asarray(b["pos"], dtype=np.float64), args.yaw) * scale
        uv = np.asarray(b["uv"], dtype=np.float64)
        col, row = slots[b["mat"]]
        uv = np.stack([(uv[:, 0] + col) / n, (uv[:, 1] + row) / n], axis=1)
        # 顶点去重：glTF 里每个三角形都带自己的 3 个顶点，索引化后体积能小一大截
        key = np.round(np.concatenate([pos, uv], axis=1), 4)
        uniq, first = np.unique(key, axis=0, return_index=True)
        lookup = {tuple(row): i for i, row in enumerate(uniq)}
        remap = [lookup[tuple(row)] for row in key]
        out_pos = pos[first]
        out_uv = uv[first]
        polys = [[[remap[e[0]], 0, remap[e[0]]] for e in poly] for poly in b["poly"]]
        all_lo = np.minimum(all_lo, out_pos.min(axis=0))
        all_hi = np.maximum(all_hi, out_pos.max(axis=0))
        mesh = OrderedDict()
        mesh["normalized_uvs"] = True
        mesh["positions"] = [[round(float(v), 3) for v in p] for p in out_pos]
        mesh["uvs"] = [[round(float(u), 5), round(float(v), 5)] for u, v in out_uv]
        mesh["polys"] = polys
        bone = OrderedDict()
        bone["name"] = bname
        bone["parent"] = "base"
        p = apply_axis(np.asarray([b["pivot"]], dtype=np.float64), args.yaw)[0] * scale
        bone["pivot"] = [round(float(p[0]), 3), round(float(p[1]), 3), round(float(p[2]), 3)]
        bone["poly_mesh"] = mesh
        out_bones.append(bone)
        tris += len(b["poly"])

    stem = re.sub(r"[^0-9A-Za-z_]", "_", os.path.splitext(os.path.basename(args.out))[0]).lower()
    desc = OrderedDict()
    desc["identifier"] = "geometry." + stem
    desc["texture_width"] = atlas.width
    desc["texture_height"] = atlas.height
    desc["visible_bounds_width"] = round(float((all_hi[0] - all_lo[0]) / 16 + 1), 2)
    desc["visible_bounds_height"] = round(float((all_hi[1] - all_lo[1]) / 16 + 1), 2)
    desc["visible_bounds_offset"] = [0.0, round(float((all_hi[1] - all_lo[1]) / 32), 2), 0.0]

    geo = OrderedDict()
    geo["description"] = desc
    geo["bones"] = out_bones
    root = OrderedDict([("format_version", "1.12.0"), ("minecraft:geometry", [geo])])

    os.makedirs(os.path.dirname(os.path.abspath(args.out)) or ".", exist_ok=True)
    with open(args.out, "w", encoding="utf-8") as fp:
        json.dump(root, fp, ensure_ascii=False, separators=(",", ":"))
    tex_out = os.path.splitext(args.out)[0] + ".png"
    atlas.save(tex_out)

    size_blocks = [round(float((all_hi[i] - all_lo[i]) / 16), 2) for i in range(3)]
    print("%-34s 骨骼=%-4d 三角=%-6d 尺寸=%-22s 缩放=%.3f 图集=%dx%d(%d格) 贴图=%s"
          % (os.path.basename(args.out), len(out_bones) - 1, tris, size_blocks, scale,
             atlas.width, atlas.height, len(materials), os.path.basename(tex_out)))
    return 0


if __name__ == "__main__":
    sys.exit(main())

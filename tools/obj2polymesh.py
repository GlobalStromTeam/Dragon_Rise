#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""obj2polymesh.py —— 把 .obj 网格转成 SBM(SimpleBedrockModel) 的 poly_mesh geo.json。

SBM 的 mesh 不是 .obj，也不是 Blockbench 的 texture_meshes，而是 bedrock 几何里
bone 级的 "poly_mesh"（BedrockPolyMesh.java 读取）：

  poly_mesh = {normalized_uvs, positions[[x,y,z]...], normals[[x,y,z]...],
               uvs[[u,v]...], polys: [[[posIdx, normIdx, uvIdx], ...], ...]}

模型单位：16 = 1 格（SBM 内部统一 /16）。SBM 会把 x 取反一次，所以这里写出
x = -obj_x * scale、y = obj_y * scale、z = obj_z * scale，与 Blockbench 导出的方块
模型保持同一套约定；整体朝向不对时加 --no-mirror-x 或 --yaw 180。

用法示例：
  python tools/obj2polymesh.py in/tank.obj \
      --out src/main/resources/assets/<ns>/models/bedrock/vehicle/tank.geo.json \
      --tex-size 512 512 --scale 16 --split group --identifier geometry.tank

参数：
  --split group|object|material|none   每个 obj 组/材质 -> 一根骨骼（默认 group）
  --scale FLOAT                        obj 单位 -> 模型单位（16=1格）；obj 以米为单位时用 16
  --tex-size W H                       贴图尺寸
  --uv-pixels                          obj 的 vt 是像素坐标（不是 0..1），自动 /W /H
  --uv-flip-v                          obj 的 v 轴方向与贴图相反时翻转
  --normals                            写出 normals（默认不写，SBM 用三角绕序算法线，与仓库现有模型一致）
  --pivot center|bottom|origin         骨骼枢轴（默认 center）
  --indent                             输出带缩进的 json（调试用）
  --merge-group FLOAT                  焊接重复顶点的距离（默认 1e-4，0=关）
  --yaw FLOAT                          整体绕 Y 旋转
"""

from __future__ import annotations

import argparse
import json
import math
import os
import re
import sys
from collections import OrderedDict


# ---------------------------------------------------------------- OBJ 解析


class ObjGroup:
    __slots__ = ("name", "polys")

    def __init__(self, name):
        self.name = name
        self.polys = []  # list[list[(pi, ni, ui)]]


def _parse_face_token(tok, n_pos, n_uv, n_norm):
    """'1/2/3' | '1//3' | '1/2' | '1' -> 0-based (pi, ni, ui)，缺失项用 -1。"""
    parts = tok.split("/")
    pi = int(parts[0])
    pi = pi - 1 if pi > 0 else n_pos + pi
    ni = -1
    ui = -1
    if len(parts) > 1 and parts[1]:
        ui = int(parts[1])
        ui = ui - 1 if ui > 0 else n_uv + ui
    if len(parts) > 2 and parts[2]:
        ni = int(parts[2])
        ni = ni - 1 if ni > 0 else n_norm + ni
    return pi, ni, ui


def parse_obj(path, split="group"):
    v, vt, vn = [], [], []
    groups = OrderedDict()
    cur_name = "mesh"
    cur_obj = None
    cur_mat = "default"

    def key_for():
        if split == "material":
            return cur_mat
        if split in ("group", "object"):
            return cur_obj or cur_name
        return "mesh"

    def bucket(name):
        if name not in groups:
            groups[name] = ObjGroup(name)
        return groups[name]

    with open(path, "r", encoding="utf-8", errors="ignore") as fp:
        for raw in fp:
            line = raw.strip()
            if not line or line[0] == "#":
                continue
            tag, _, rest = line.partition(" ")
            if tag == "v":
                v.append(tuple(float(x) for x in rest.split()[:3]))
            elif tag == "vt":
                parts = rest.split()
                vt.append((float(parts[0]), float(parts[1]) if len(parts) > 1 else 0.0))
            elif tag == "vn":
                vn.append(tuple(float(x) for x in rest.split()[:3]))
            elif tag == "g":
                cur_name = rest.strip() or cur_name
                cur_obj = None
            elif tag == "o":
                cur_obj = rest.strip() or cur_obj
            elif tag == "usemtl":
                cur_mat = rest.strip() or cur_mat
            elif tag == "f":
                poly = []
                ok = True
                for tok in rest.split():
                    try:
                        pi, ni, ui = _parse_face_token(tok, len(v), len(vt), len(vn))
                    except (ValueError, IndexError):
                        ok = False
                        break
                    if pi < 0 or pi >= len(v):
                        ok = False
                        break
                    if ui >= len(vt):
                        ui = -1
                    if ni >= len(vn):
                        ni = -1
                    poly.append((pi, ni, ui))
                if ok and len(poly) >= 3:
                    bucket(key_for()).polys.append(poly)
    return v, vt, vn, groups


# ---------------------------------------------------------------- 转换


def _sanitize(name):
    s = re.sub(r"[^0-9A-Za-z_\-.]", "_", name or "bone")
    if not s or s[0].isdigit():
        s = "b_" + s
    return s


def _rot_y(p, deg):
    if deg % 360 == 0:
        return p
    a = math.radians(deg)
    ca, sa = math.cos(a), math.sin(a)
    x, y, z = p
    return (x * ca + z * sa, y, -x * sa + z * ca)


def _xform_point(p, args, scale=None):
    """OBJ 坐标 -> 模型坐标（1 格 = 16 单位）。"""
    s = args.scale if scale is None else scale
    x, y, z = p
    if args.mirror_x:
        x = -x
    x, y, z = _rot_y((x, y, z), args.yaw)
    return [x * s, y * s, z * s]


def regroup_by_parts(v, groups, parts):
    """按 --parts 的 bbox 规则把面重新分到骨骼上（先匹配先生效，没命中的进 else）。"""
    rules = parts.get("rules") or []
    fallback = parts.get("else", "body")
    out = OrderedDict()

    def bucket(name):
        if name not in out:
            out[name] = ObjGroup(name)
        return out[name]

    for g in groups.values():
        for poly in g.polys:
            pts = [v[p[0]] for p in poly]
            c = [sum(p[i] for p in pts) / len(pts) for i in range(3)]
            hit = None
            for rule in rules:
                bb = rule.get("bbox")
                if bb and not (bb[0] <= c[0] <= bb[1] and bb[2] <= c[1] <= bb[3] and bb[4] <= c[2] <= bb[5]):
                    continue
                cyl = rule.get("cyl")
                if cyl:
                    r = math.hypot(c[0] - cyl.get("x", 0.0), c[2] - cyl.get("z", 0.0))
                    r0, r1 = cyl.get("r", [0.0, 1e9])
                    y0, y1 = cyl.get("y", [-1e9, 1e9])
                    if not (r0 <= r <= r1 and y0 <= c[1] <= y1):
                        continue
                cond = rule.get("where") or {}
                ok = True
                for axis, rng in cond.items():
                    i = "xyz".index(axis)
                    if not (rng[0] <= c[i] <= rng[1]):
                        ok = False
                        break
                if ok:
                    hit = rule["bone"]
                    break
            bucket(hit or fallback).polys.append(poly)
    return out


def convert(v, vt, vn, groups, args):
    if args.parts:
        groups = regroup_by_parts(v, groups, args.parts)
    pivots_file = args.pivots or {}
    tex_w, tex_h = args.tex_size
    scale = args.scale
    merge = args.merge_group

    pos_keys = OrderedDict()
    norm_keys = OrderedDict()
    uv_keys = OrderedDict()
    out_pos, out_norm, out_uv = [], [], []
    want_normals = args.normals

    def reset_pools():
        pos_keys.clear()
        norm_keys.clear()
        uv_keys.clear()
        del out_pos[:]
        del out_norm[:]
        del out_uv[:]

    def pos_idx(p):
        if merge > 0:
            q = (round(p[0] / merge) * merge, round(p[1] / merge) * merge, round(p[2] / merge) * merge)
        else:
            q = p
        k = (round(q[0], 5), round(q[1], 5), round(q[2], 5))
        if k not in pos_keys:
            pos_keys[k] = len(out_pos)
            out_pos.append(_xform_point(q, args))
        return pos_keys[k]

    def norm_idx(n):
        k = (round(n[0], 5), round(n[1], 5), round(n[2], 5))
        if k not in norm_keys:
            norm_keys[k] = len(out_norm)
            x, y, z = n
            if args.mirror_x:
                x = -x
            ax, ay, az = _rot_y((x, y, z), args.yaw)
            ln = math.sqrt(ax * ax + ay * ay + az * az) or 1.0
            out_norm.append([ax / ln, ay / ln, az / ln])
        return norm_keys[k]

    def uv_idx(uv):
        u, w = uv
        if args.uv_pixels and tex_w and tex_h:
            u, w = u / tex_w, w / tex_h
        u += args.uv_add[0]
        w += args.uv_add[1]
        if args.uv_flip_v:
            w = 1.0 - w
        k = (round(u, 5), round(w, 5))
        if k not in uv_keys:
            uv_keys[k] = len(out_uv)
            out_uv.append([u, w])
        return uv_keys[k]

    built = []  # [(bone_name, polys, positions, normals, uvs)]
    total_tris = 0
    all_x, all_y, all_z = [], [], []

    for gname, g in groups.items():
        reset_pools()
        polys = []
        for poly in g.polys:
            entry = []
            for (pi, ni, ui) in poly:
                ip = pos_idx(v[pi])
                inn = norm_idx(vn[ni]) if (want_normals and ni >= 0) else 0
                iu = uv_idx(vt[ui]) if ui >= 0 else 0
                entry.append([ip, inn, iu])
            polys.append(entry)
            total_tris += max(0, len(entry) - 2)
        if not polys:
            continue
        pos_snapshot = [p[:] for p in out_pos]
        norm_snapshot = [n[:] for n in out_norm] if want_normals else []
        uv_snapshot = [t[:] for t in out_uv]
        all_x.extend(p[0] for p in pos_snapshot)
        all_y.extend(p[1] for p in pos_snapshot)
        all_z.extend(p[2] for p in pos_snapshot)
        built.append((_sanitize(gname), polys, pos_snapshot, norm_snapshot, uv_snapshot))

    geo_bones = [OrderedDict([("name", "base"), ("pivot", [0.0, 0.0, 0.0])])]
    for bname, polys, pos_arr, norm_arr, uv_arr in built:
        if bname in pivots_file:
            piv = [round(c, 3) for c in _xform_point(pivots_file[bname], args)]
        elif args.pivot == "origin":
            piv = [0.0, 0.0, 0.0]
        else:
            gx = [pos_arr[e[0]][0] for poly in polys for e in poly]
            gy = [pos_arr[e[0]][1] for poly in polys for e in poly]
            gz = [pos_arr[e[0]][2] for poly in polys for e in poly]
            piv = [round((min(gx) + max(gx)) / 2.0, 3),
                   round(min(gy) if args.pivot == "bottom" else (min(gy) + max(gy)) / 2.0, 3),
                   round((min(gz) + max(gz)) / 2.0, 3)]
        mesh = OrderedDict()
        mesh["normalized_uvs"] = True
        mesh["positions"] = pos_arr
        if norm_arr:
            mesh["normals"] = norm_arr
        if uv_arr:
            mesh["uvs"] = uv_arr
        mesh["polys"] = polys
        bone = OrderedDict()
        bone["name"] = bname
        bone["parent"] = "base"
        bone["pivot"] = piv
        bone["poly_mesh"] = mesh
        geo_bones.append(bone)

    xs, ys, zs = all_x, all_y, all_z
    desc = OrderedDict()
    desc["identifier"] = args.identifier or ("geometry." + _sanitize(os.path.splitext(os.path.basename(args.obj))[0]))
    desc["texture_width"] = tex_w
    desc["texture_height"] = tex_h
    desc["visible_bounds_width"] = round((max(xs) - min(xs)) / 16.0 + 1, 2)
    desc["visible_bounds_height"] = round((max(ys) - min(ys)) / 16.0 + 1, 2)
    desc["visible_bounds_offset"] = [0.0, round((max(ys) - min(ys)) / 32.0, 2), 0.0]

    geo = OrderedDict()
    geo["description"] = desc
    geo["bones"] = geo_bones

    root = OrderedDict()
    root["format_version"] = args.format_version
    root["minecraft:geometry"] = [geo]

    stats = {
        "groups": len(built),
        "positions": sum(len(b[2]) for b in built),
        "normals": sum(len(b[3]) for b in built),
        "uvs": sum(len(b[4]) for b in built),
        "tris": total_tris,
        "size_blocks": (round((max(xs) - min(xs)) / 16.0, 2),
                        round((max(ys) - min(ys)) / 16.0, 2),
                        round((max(zs) - min(zs)) / 16.0, 2)),
    }
    return root, stats


def main(argv=None):
    ap = argparse.ArgumentParser(add_help=True)
    ap.add_argument("obj")
    ap.add_argument("--out", required=True)
    ap.add_argument("--split", default="group", choices=["group", "object", "material", "none"])
    ap.add_argument("--scale", type=float, default=16.0)
    ap.add_argument("--tex-size", nargs=2, type=int, default=[512, 512])
    ap.add_argument("--uv-pixels", action="store_true")
    ap.add_argument("--uv-flip-v", action="store_true")
    ap.add_argument("--uv-add", nargs=2, type=float, default=[0.0, 0.0],
                    help="UV 平移（v 落在 -1..0 的导出常见，用 --uv-add 0 1）")
    ap.add_argument("--normals", action="store_true")
    ap.add_argument("--pivot", default="center", choices=["center", "bottom", "origin"])
    ap.add_argument("--indent", action="store_true")
    ap.add_argument("--merge-group", type=float, default=1e-4)
    ap.add_argument("--yaw", type=float, default=0.0)
    ap.add_argument("--no-mirror-x", dest="mirror_x", action="store_false")
    ap.add_argument("--identifier", default=None)
    ap.add_argument("--format-version", default="1.12.0")
    ap.add_argument("--parts", default=None,
                    help="分件规则 JSON：{rules:[{bone,bbox:[x0,x1,y0,y1,z0,z1]}], else:'body'}")
    ap.add_argument("--pivots", default=None,
                    help="骨骼枢轴 JSON：{骨骼名:[x,y,z]}（OBJ 坐标系，单位同 OBJ）")
    args = ap.parse_args(argv)

    if args.parts:
        with open(args.parts, encoding="utf-8-sig") as fp:
            args.parts = json.load(fp)
    if args.pivots:
        with open(args.pivots, encoding="utf-8-sig") as fp:
            args.pivots = json.load(fp)

    v, vt, vn, groups = parse_obj(args.obj, args.split)
    if not groups:
        raise SystemExit("OBJ 里没有找到面（f 行）")
    root, stats = convert(v, vt, vn, groups, args)

    os.makedirs(os.path.dirname(os.path.abspath(args.out)), exist_ok=True)
    with open(args.out, "w", encoding="utf-8") as fp:
        if args.indent:
            json.dump(root, fp, ensure_ascii=False, indent=2)
        else:
            json.dump(root, fp, ensure_ascii=False, separators=(",", ":"))
    print("写出 " + args.out)
    print("统计 " + json.dumps(stats, ensure_ascii=False))


if __name__ == "__main__":
    sys.exit(main())

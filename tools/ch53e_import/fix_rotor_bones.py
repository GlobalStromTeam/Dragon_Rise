#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""fix_rotor_bones.py —— 修「骨骼分了但旋翼不转」这种 geo.json：

SBM/SBW 只认两种东西：
  * 骨骼名必须是 `move_propeller`（主旋翼，绕 Y 转）和 `move_tailPropeller`（尾桨，绕 X 转）；
  * 几何（poly_mesh）必须挂在这两根骨骼自己身上。

常见错误（Blockbench 里分骨骼时很容易这样）：
  * `move_propeller` 建了但里面是空的，桨叶还在 `body` 或别的骨骼下；
  * 尾桨骨骼叫成 `move_tailPropeller1 / move_tailPropeller2`，与渲染器要的名字差一个数字；
  * 骨骼枢轴不在桨毂上，转起来整片桨叶绕圈甩。

本脚本按「几何体量 + 空间位置」自动判定哪块是主旋翼、哪块是尾桨，然后把几何搬到
正确名字的骨骼上、把枢轴算到桨毂中心。原始文件会备份成 *.bak。

用法：
  python fix_rotor_bones.py 模型.geo.json            # 就地修（自动备份）
  python fix_rotor_bones.py 模型.geo.json --dry-run  # 只看判定结果
  python fix_rotor_bones.py 模型.geo.json --main 主旋翼骨骼名 --tail 尾桨骨骼名
"""

from __future__ import annotations

import argparse
import collections
import json
import os
import shutil
import sys

import numpy as np


def mesh_stats(mesh):
    pos = np.asarray(mesh.get("positions") or [], dtype=float)
    if pos.size == 0:
        return None
    tris = sum(max(0, len(p) - 2) for p in (mesh.get("polys") or []))
    mn, mx = pos.min(axis=0), pos.max(axis=0)
    return {
        "tris": tris,
        "min": mn,
        "max": mx,
        "size": mx - mn,
        "center": (mn + mx) / 2.0,
        "pos": pos,
    }


def hub_of(mesh, assume_flat_axis=None):
    """桨毂 = 各桨叶根部（离整体质心最近的顶点）的均值。"""
    pos = np.asarray(mesh["positions"], dtype=float)
    polys = mesh.get("polys") or []
    # 连通分量（按顶点索引的并查集）
    parent = list(range(len(pos)))

    def find(a):
        while parent[a] != a:
            parent[a] = parent[parent[a]]
            a = parent[a]
        return a

    def union(a, b):
        ra, rb = find(a), find(b)
        if ra != rb:
            parent[ra] = rb

    used = set()
    for poly in polys:
        idx = [e[0] for e in poly]
        used.update(idx)
        for k in range(1, len(idx)):
            union(idx[0], idx[k])
    comps = collections.defaultdict(list)
    for v in sorted(used):
        comps[find(v)].append(v)
    c0 = pos[list(used)].mean(axis=0) if used else pos.mean(axis=0)

    roots = []
    for _, vs in comps.items():
        if len(vs) < 8:
            continue
        pts = pos[vs]
        roots.append(pts[int(np.argmin(np.linalg.norm(pts - c0, axis=1)))])
    if not roots:
        return c0
    hub = np.asarray(roots).mean(axis=0)
    if assume_flat_axis is not None:
        hub[assume_flat_axis] = c0[assume_flat_axis]
    return hub


def classify(bones):
    """返回 (主旋翼骨骼名, 尾桨骨骼名)。判定依据：主旋翼是 X 跨距最大的网格；尾桨是最靠后且体量小的网格。"""
    meshes = []
    for b in bones:
        st = mesh_stats(b.get("poly_mesh") or {})
        if st and st["tris"] > 0:
            meshes.append((b["name"], st))
    if not meshes:
        raise SystemExit("模型里没有 poly_mesh 几何")
    meshes.sort(key=lambda x: -(x[1]["size"][0] * x[1]["size"][2]))
    main = meshes[0]
    # 尾桨：在「体量不算鸡毛蒜皮」的候选里，取 |z| 最靠后的（小零件如桨毂盖、探照灯会被 tris 门槛滤掉）
    min_tris = max(50, int(main[1]["tris"] * 0.05))
    rest = [m for m in meshes[1:] if m[1]["tris"] >= min_tris]
    tail = None
    if rest:
        rest.sort(key=lambda x: -abs(x[1]["center"][2]))
        tail = rest[0]
    return main, tail


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("geo")
    ap.add_argument("--main", default=None, help="手工指定主旋翼几何所在的骨骼名")
    ap.add_argument("--tail", default=None, help="手工指定尾桨几何所在的骨骼名")
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args(argv)

    with open(args.geo, encoding="utf-8-sig") as fp:
        data = json.load(fp)
    geo = data["minecraft:geometry"][0]
    bones = geo["bones"]
    by_name = {b["name"]: b for b in bones}

    auto_main, auto_tail = classify(bones)
    main_src = args.main or auto_main[0]
    tail_src = args.tail or (auto_tail[0] if auto_tail else None)
    print("判定：主旋翼几何在 '%s'（%d 三角，X 跨距 %.1f）；尾桨几何在 %s"
          % (main_src, auto_main[1]["tris"], auto_main[1]["size"][0],
             ("'%s'（%d 三角）" % (tail_src, auto_tail[1]["tris"])) if tail_src else "(无)"))

    if args.dry_run:
        print("--dry-run：不写文件")
        return 0

    # 1) 主旋翼几何 -> move_propeller，枢轴重算到桨毂
    if "move_propeller" not in by_name:
        by_name["move_propeller"] = {"name": "move_propeller", "parent": "base", "pivot": [0, 0, 0]}
        bones.append(by_name["move_propeller"])
    main_bone = by_name["move_propeller"]
    if main_src != "move_propeller":
        src = by_name[main_src]
        main_bone["poly_mesh"] = src.pop("poly_mesh")
        print("  几何： %s -> move_propeller" % main_src)
    hub = hub_of(main_bone["poly_mesh"], assume_flat_axis=1)
    main_bone["pivot"] = [round(float(v), 3) for v in hub]
    print("  主旋翼枢轴 -> %s" % main_bone["pivot"])

    # 2) 尾桨几何 -> move_tailPropeller
    if tail_src:
        if "move_tailPropeller" not in by_name:
            by_name["move_tailPropeller"] = {"name": "move_tailPropeller", "parent": "base",
                                             "pivot": [0, 0, 0]}
            bones.append(by_name["move_tailPropeller"])
        if tail_src != "move_tailPropeller":
            src = by_name[tail_src]
            by_name["move_tailPropeller"]["poly_mesh"] = src.pop("poly_mesh")
            print("  几何： %s -> move_tailPropeller" % tail_src)
        thub = hub_of(by_name["move_tailPropeller"]["poly_mesh"])
        by_name["move_tailPropeller"]["pivot"] = [round(float(v), 3) for v in thub]
        print("  尾桨枢轴 -> %s" % by_name["move_tailPropeller"]["pivot"])

    backup = args.geo + ".bak"
    if not os.path.exists(backup):
        shutil.copy(args.geo, backup)
        print("  备份 ->", backup)
    with open(args.geo, "w", encoding="utf-8") as fp:
        json.dump(data, fp, ensure_ascii=False, separators=(",", ":"))
    print("写出", args.geo)
    return 0


if __name__ == "__main__":
    sys.exit(main())

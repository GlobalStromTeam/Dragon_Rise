#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""check_polymesh_geo.py —— 校验 SBM(SimpleBedrockModel) 的 geo.json 结构（含 poly_mesh）。

SBM 的读取路径：BedrockModelPOJO -> BonesItem.poly_mesh -> PolyMeshItem -> BedrockPolyMesh。
本脚本按这条路径逐项检查，输出 PASS/FAIL，不做渲染。

用法：
  python tools/check_polymesh_geo.py 模型.geo.json [更多.geo.json ...]
  python tools/check_polymesh_geo.py --ref 仓库里已知能用的模型.geo.json 新模型.geo.json
"""

import json
import sys

MESH_KEYS = {"normalized_uvs", "positions", "normals", "uvs", "polys"}
BONE_KEYS = {"name", "parent", "pivot", "rotation", "cubes", "poly_mesh",
             "locators", "mirror", "inflate", "binding", "neverRender"}


def check_vertex_key(key, value):
    if not isinstance(value, (int, float)) or isinstance(value, bool):
        raise AssertionError("%s 必须是数字，实际 %r" % (key, value))


def validate_mesh(mesh, bone_name, problems, stats):
    if not isinstance(mesh, dict):
        problems.append("[%s] poly_mesh 不是对象" % bone_name)
        return
    unknown = set(mesh) - MESH_KEYS
    if unknown:
        problems.append("[%s] poly_mesh 有未知字段 %s（SBM 会忽略）" % (bone_name, sorted(unknown)))
    if not isinstance(mesh.get("normalized_uvs"), bool):
        problems.append("[%s] normalized_uvs 缺失或不是 bool" % bone_name)
    positions = mesh.get("positions")
    if not isinstance(positions, list) or not positions:
        problems.append("[%s] positions 为空" % bone_name)
        return
    for p in positions:
        if not isinstance(p, list) or len(p) != 3:
            problems.append("[%s] positions 元素必须是长度 3 的数组：%r" % (bone_name, p))
            break
        try:
            for v in p:
                check_vertex_key("positions", v)
        except AssertionError as exc:
            problems.append("[%s] %s" % (bone_name, exc))
            break
    n_uv = len(mesh.get("uvs") or [])
    n_norm = len(mesh.get("normals") or [])
    if mesh.get("normalized_uvs") and n_uv:
        flat = [c for uv in mesh["uvs"] for c in uv]
        if min(flat) < -0.001 or max(flat) > 1.001:
            problems.append("[%s] normalized_uvs=true 但 uvs 超出 0..1（min=%.3f max=%.3f）"
                            % (bone_name, min(flat), max(flat)))
    polys = mesh.get("polys")
    if not isinstance(polys, list):
        problems.append("[%s] polys 必须是多边形数组（SBM 也接受字符串 tri_list/quad_list）" % bone_name)
        return
    tri = 0
    for pi, poly in enumerate(polys):
        if not isinstance(poly, list) or len(poly) < 3:
            problems.append("[%s] polys[%d] 顶点数 < 3" % (bone_name, pi))
            continue
        for entry in poly:
            if not isinstance(entry, list) or len(entry) != 3:
                problems.append("[%s] polys[%d] 的顶点必须是 [位置,法线,UV] 三元组：%r"
                                % (bone_name, pi, entry))
                break
            p_i, n_i, u_i = entry
            if not (0 <= p_i < len(positions)):
                problems.append("[%s] polys[%d] 位置索引 %d 越界（%d 个位置）"
                                % (bone_name, pi, p_i, len(positions)))
                break
            if n_norm and not (0 <= n_i < n_norm):
                problems.append("[%s] polys[%d] 法线索引 %d 越界（%d 个法线）"
                                % (bone_name, pi, n_i, n_norm))
                break
            if n_uv and not (0 <= u_i < n_uv):
                problems.append("[%s] polys[%d] UV 索引 %d 越界（%d 个 UV）"
                                % (bone_name, pi, u_i, n_uv))
                break
        tri += max(0, len(poly) - 2)
    stats["tris"] += tri
    stats["mesh_bones"] += 1


def validate(path):
    problems = []
    stats = {"mesh_bones": 0, "cube_bones": 0, "tris": 0, "bones": 0}
    with open(path, encoding="utf-8-sig") as fp:
        data = json.load(fp)
    if not isinstance(data, dict):
        return ["根节点不是对象"], stats
    geo_list = data.get("minecraft:geometry")
    if not isinstance(geo_list, list) or not geo_list:
        return ["缺少 minecraft:geometry 数组"], stats
    fv = str(data.get("format_version"))
    if fv not in ("1.8.0", "1.10.0", "1.12.0", "1.16.0"):
        problems.append("format_version=%r 不是 Bedrock 常见版本" % fv)
    geo = geo_list[0]
    desc = geo.get("description") or {}
    for key in ("identifier", "texture_width", "texture_height"):
        if key not in desc:
            problems.append("description 缺少 %s" % key)
    bones = geo.get("bones")
    if not isinstance(bones, list) or not bones:
        return problems + ["bones 为空"], stats
    names = set()
    for bone in bones:
        if not isinstance(bone, dict):
            problems.append("bones 里有非对象元素")
            continue
        stats["bones"] += 1
        name = bone.get("name")
        if not isinstance(name, str) or not name:
            problems.append("有骨骼缺少 name")
            continue
        if name in names:
            problems.append("骨骼重名：%s" % name)
        names.add(name)
        unknown = set(bone) - BONE_KEYS
        if unknown:
            problems.append("[%s] 有未知字段 %s（SBM 会忽略）" % (name, sorted(unknown)))
        if "pivot" in bone:
            piv = bone["pivot"]
            if not isinstance(piv, list) or len(piv) != 3:
                problems.append("[%s] pivot 必须是 3 元数组" % name)
        if bone.get("cubes"):
            stats["cube_bones"] += 1
        if bone.get("poly_mesh"):
            validate_mesh(bone["poly_mesh"], name, problems, stats)
    for bone in bones:
        parent = bone.get("parent")
        if parent is not None and parent not in names:
            problems.append("[%s] parent=%r 不存在" % (bone.get("name"), parent))
    if not (stats["mesh_bones"] or stats["cube_bones"]):
        problems.append("既没有 cubes 也没有 poly_mesh，模型是空的")
    return problems, stats


def main(argv):
    ref = None
    paths = []
    it = iter(argv[1:])
    for a in it:
        if a == "--ref":
            ref = next(it, None)
        else:
            paths.append(a)
    if not paths:
        print(__doc__)
        return 2
    rc = 0
    if ref:
        rp, rs = validate(ref)
        print("参考模型 %s -> %s  统计 %s" % (ref, "PASS" if not rp else "FAIL", json.dumps(rs, ensure_ascii=False)))
        for line in rp[:10]:
            print("   ", line)
    for path in paths:
        problems, stats = validate(path)
        ok = not problems
        print("%s -> %s  统计 %s" % (path, "PASS" if ok else "FAIL", json.dumps(stats, ensure_ascii=False)))
        for line in problems[:20]:
            print("   ", line)
        if not ok:
            rc = 1
    return rc


if __name__ == "__main__":
    sys.exit(main(sys.argv))

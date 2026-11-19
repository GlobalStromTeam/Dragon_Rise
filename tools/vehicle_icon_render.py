#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Bedrock 几何离线渲染底座（tools/ 下 geo_*.py 与图标流程共用）。

提供两类能力：

1) 兼容旧脚本的接口（geo_ortho_view.py / geo_helper_bone_preview.py 依赖）：
     model = Model(geo_path)          # .bone_list / .bone_matrix(name, cache)
                                      # .texture_width / .texture_height
     for tri in cube_to_tris(cube, m, tw, th):   # tri = (pos[3][3], uv[3][2])
   uv 为归一化坐标（0..1）。

2) 等轴测整模渲染（载具图标）：
     render_icon(geo_path, texture_path, out_path, size=128, fill=0.85)

面顶点顺序取自 simplebedrockmodel 的 BedrockCube.VERTEX_ORDER（权威，和游戏一致）：
  DOWN=[0,1,5,4] UP=[7,6,2,3] NORTH=[3,2,1,0] SOUTH=[6,7,4,5] WEST=[7,3,0,4] EAST=[2,6,5,1]
（Minecraft Direction 序数：0=DOWN 1=UP 2=NORTH 3=SOUTH 4=WEST 5=EAST）
立方体 8 顶点编号（cubeVertices）：
  0=(x1,y1,z1) 1=(x2,y1,z1) 2=(x2,y2,z1) 3=(x1,y2,z1)
  4=(x1,y1,z2) 5=(x2,y1,z2) 6=(x2,y2,z2) 7=(x1,y2,z2)

UV 约定：每个面 4 个顶点按 [左上, 右上, 右下, 左下]（VERTEX_ORDER 即此序），
对应 uv 矩形角点 [(u1,v1),(u2,v1),(u2,v2),(u1,v2)]，u2=u1+w、v2=v1+h（w/h 可为负＝镜像）。
")
"""
import json
import math
import os
import sys

import numpy as np
from PIL import Image

# ---------------------------------------------------------------- 常量

# 面序号（Direction 序数）-> 立方体 8 顶点索引
VERTEX_ORDER = {
    0: (0, 1, 5, 4),   # DOWN
    1: (7, 6, 2, 3),   # UP
    2: (3, 2, 1, 0),   # NORTH
    3: (6, 7, 4, 5),   # SOUTH
    4: (7, 3, 0, 4),   # WEST
    5: (2, 6, 5, 1),   # EAST
}

FACE_NAMES = {0: "down", 1: "up", 2: "north", 3: "south", 4: "west", 5: "east"}

# 图标取景：真正的 2:1 等轴测（atan(1/2)）；角度已按官方图标（kv1/j20 等）反解标定
DEFAULT_YAW = 45.0
DEFAULT_PITCH = 26.565
SUPERSAMPLE = 3

# 面朝向明暗（仿 Minecraft 方块方向光照）：按面法线的主轴取系数
FACE_SHADE = {
    (0, 0, 1): 1.0,    # +Y 顶面
    (0, 0, -1): 0.5,   # -Y 底面
    (0, 1, 0): 0.8,    # +Z
    (0, -1, 0): 0.8,   # -Z
    (1, 0, 0): 0.62,   # +X
    (-1, 0, 0): 0.62,  # -X
}


# ---------------------------------------------------------------- 变换

def _rot_xyz(rot):
    """Blockbench 骨骼/立方体欧拉角（度，ZYX 顺序）-> 4x4 矩阵。

    旋转量的表达是"以 Y 为镜像轴的左手系"：标准右手矩阵再按 S=diag(1,-1,1)
    做一次反射共轭即可，等价于 **X/Z 取负、Y 保持原符号**。
    （早期把三轴一律取负，偏心 pivot 的大角度部件会被甩到模型外：f15e 的机翼
    变成斜插的板子、zbd04a 出现斜线噪点、uh60/z20 的旋翼炸开。）
    """
    rx, ry, rz = -math.radians(rot[0]), math.radians(rot[1]), -math.radians(rot[2])
    cx, sx = math.cos(rx), math.sin(rx)
    cy, sy = math.cos(ry), math.sin(ry)
    cz, sz = math.cos(rz), math.sin(rz)
    mx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]], dtype=float)
    my = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]], dtype=float)
    mz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]], dtype=float)
    m = np.eye(4)
    m[:3, :3] = mz @ my @ mx
    return m


def translate(x, y, z):
    m = np.eye(4)
    m[0, 3], m[1, 3], m[2, 3] = x, y, z
    return m


def _pivot_rot(pivot, rot):
    """绕 pivot 旋转：T(p) · R · T(-p)。"""
    return translate(*pivot) @ _rot_xyz(rot) @ translate(*(-np.asarray(pivot, dtype=float)))


# ---------------------------------------------------------------- 模型

class Bone:
    def __init__(self, data):
        self.name = data["name"]
        self.parent = data.get("parent")
        self.pivot = data.get("pivot", [0, 0, 0])
        self.rotation = data.get("rotation")
        self.cubes = data.get("cubes", []) or []
        self.polys = data.get("poly_mesh")


class Model:
    """解析 .geo.json，提供骨骼矩阵与立方体三角形。"""

    def __init__(self, path):
        with open(path, encoding="utf-8-sig") as fp:
            geo = json.load(fp)["minecraft:geometry"][0]
        desc = geo.get("description", {})
        self.texture_width = desc.get("texture_width", 64)
        self.texture_height = desc.get("texture_height", 64)
        self.bone_list = [Bone(b) for b in geo.get("bones", [])]
        self._by_name = {b.name: b for b in self.bone_list}

    def bone_matrix(self, name, cache):
        """返回该骨骼的累计模型矩阵（含所有祖先）。"""
        if name in cache:
            return cache[name]
        bone = self._by_name[name]
        m = np.eye(4)
        if bone.parent and bone.parent in self._by_name:
            m = self.bone_matrix(bone.parent, cache)
        if bone.rotation:
            m = m @ _pivot_rot(bone.pivot, bone.rotation)
        cache[name] = m
        return m


# ---------------------------------------------------------------- 立方体 -> 三角形

def _corners(cube):
    """归一化到 x1<x2 / y1<y2 / z1<z2 并应用 inflate，返回 8 顶点（SBM 编号）。"""
    ox, oy, oz = cube["origin"]
    sx, sy, sz = cube["size"]
    inflate = float(cube.get("inflate", 0.0) or 0.0)
    x1, x2 = sorted((ox, ox + sx))
    y1, y2 = sorted((oy, oy + sy))
    z1, z2 = sorted((oz, oz + sz))
    x1 -= inflate
    y1 -= inflate
    z1 -= inflate
    x2 += inflate
    y2 += inflate
    z2 += inflate
    return np.array([
        (x1, y1, z1), (x2, y1, z1), (x2, y2, z1), (x1, y2, z1),
        (x1, y1, z2), (x2, y1, z2), (x2, y2, z2), (x1, y2, z2),
    ], dtype=float)


def _box_uv_face(cube, face_index):
    """列表式 uv（cube["uv"] = [u, v] 全局偏移）按标准展开图推导每面矩形。

        ┌────┬────┐
        │ up │down│
   ┌────┼────┼────┬────┐
   │east│north│west│south│
   w=sizeX  h=sizeY  d=sizeZ
    """
    u, v = float(cube["uv"][0]), float(cube["uv"][1])
    sx, sy, sz = cube.get("size", [0, 0, 0])
    w, h, d = abs(float(sx)), abs(float(sy)), abs(float(sz))
    rects = {
        1: (u + d, v, w, d),                  # up
        0: (u + d + w, v, w, d),              # down
        5: (u, v + d, d, h),                  # east
        2: (u + d, v + d, w, h),              # north
        4: (u + d + w, v + d, d, h),          # west
        3: (u + d + w + d, v + d, w, h),      # south
    }
    ru, rv, rw, rh = rects[face_index]
    return {"uv": [ru, rv], "uv_size": [rw, rh]}


def _cube_uvs(cube, face_index, tw, th):
    """返回该面 4 个顶点的归一化 uv（顺序同 VERTEX_ORDER），空 UV 返回 None。

    uv 有两种写法：逐面字典 {"north": {...}}，或列表 [u, v]（标准 box uv）。
    后者以前被当成"无 UV"整面跳过，导致这类立方体完全不画——sx1 的整个车体、
    ztz99ah / m1a1hc / strv103 等 67 个模型的部分部件都会凭空消失。
    """
    uv = cube.get("uv")
    if isinstance(uv, dict) and uv:
        face = uv.get(FACE_NAMES[face_index])
    elif isinstance(uv, (list, tuple)) and len(uv) >= 2:
        face = _box_uv_face(cube, face_index)
    else:
        return None
    if not isinstance(face, dict):
        return None
    u, v = face.get("uv", [0, 0])
    w, h = face.get("uv_size", [0, 0])
    if abs(w) < 1e-9 or abs(h) < 1e-9:
        return None
    u2, v2 = u + w, v + h
    corners = [(u, v), (u2, v), (u2, v2), (u, v2)]
    if face_index == 5:
        # EAST 面在"东→北→西→南"展开条带上的 u 朝向与 WEST 相反：
        # 不镜像的话斜置部件（f15e 的垂尾）会取到另一侧的透明留白，
        # 表现为尾翼中间整块空缺、机身上的编号左右反写。
        corners = [(u2, v), (u, v), (u, v2), (u2, v2)]
    return [(cu / float(tw), cv / float(th)) for cu, cv in corners]


def cube_tris(cube, matrix, tw, th):
    """产出该立方体的三角形 [(pos[3][3], uv[3][2])]，跳过空/缺 UV 的面。"""
    verts = _corners(cube)
    if cube.get("rotation") and cube.get("pivot"):
        m = matrix @ _pivot_rot(cube["pivot"], cube["rotation"])
    else:
        m = matrix
    world = (m[:3, :3] @ verts.T).T + m[:3, 3]
    for face_index, order in VERTEX_ORDER.items():
        uvs = _cube_uvs(cube, face_index, tw, th)
        if uvs is None:
            continue
        quad = [world[i] for i in order]
        quad_uv = [uvs[i] for i in range(4)]
        yield [quad[0], quad[1], quad[2]], [quad_uv[0], quad_uv[1], quad_uv[2]]
        yield [quad[0], quad[2], quad[3]], [quad_uv[0], quad_uv[2], quad_uv[3]]


def cube_to_tris(cube, matrix, tw, th):
    """旧脚本兼容入口（geo_ortho_view.py / geo_helper_bone_preview.py）。"""
    return cube_tris(cube, matrix, tw, th)


# 碰撞/占位盒骨骼：它们是 OBB 或锚点，不属于外观。
# 这些立方体往往带 UV（贴图边缘的占位像素），不剔除会渲染成浮空小块。
_HELPER_BONE_PREFIX = ("obb", "collisionobb", "turretobb", "wheelrightobb", "wheelleftobb",
                       "mainengineobb", "cannonpos", "seatspos", "terraincompat", "build")


def _is_helper_bone(name):
    n = (name or "").lower()
    return n.startswith(_HELPER_BONE_PREFIX)


def mesh_tris(pm, matrix, tw, th):
    """poly_mesh -> 与 cube_tris 相同的 (vert3, uv3) 三角形。"""
    meshes = pm if isinstance(pm, list) else [pm]
    M = matrix[:3, :3]
    T = matrix[:3, 3]
    for mesh in meshes:
        if not isinstance(mesh, dict):
            continue
        positions = mesh.get("positions") or []
        uvs = mesh.get("uvs") or []
        normalized = bool(mesh.get("normalized_uvs"))
        world = [M @ np.asarray(p, dtype=float) + T for p in positions]

        def uv_of(i):
            if i >= len(uvs):
                return (0.0, 0.0)
            u, v = float(uvs[i][0]), float(uvs[i][1])
            if not normalized:
                u, v = u / float(tw), v / float(th)
            # poly_mesh 的 UV 原点在左下，与 box uv 的左上相反，v 必须翻转；
            # 不翻会让整个自由网格取到贴图的空白区，表现为"该部分完全没渲染出来"。
            return (u, 1.0 - v)

        for poly in (mesh.get("polys") or []):
            entries = poly
            if isinstance(entries, (list, tuple)) and entries and isinstance(entries[0], str):
                entries = entries[1]
            pts, u3 = [], []
            for e in entries:
                if isinstance(e, (list, tuple)):
                    pi = int(e[0])
                    # 顶点索引三元组的顺序是 [位置, 法线, UV]，不是 [位置, UV, 法线]：
                    # 用 e[1] 当 UV 会让一个四边形横跨大半个贴图，渲染出来是乱色块
                    # （mv3 的轮胎就是这样）。只有两项时才退化为取 e[1]。
                    ui = int(e[2]) if len(e) >= 3 else (int(e[1]) if len(e) > 1 else pi)
                else:
                    pi = ui = int(e)
                pts.append(world[pi])
                u3.append(uv_of(ui))
            if len(pts) < 3:
                continue
            for k in range(1, len(pts) - 1):
                yield [pts[0], pts[k], pts[k + 1]], [u3[0], u3[k], u3[k + 1]]


def _helper_bone_names(model):
    """需要剔除的骨骼名集合：自身命中前缀，或祖先是辅助骨骼。

    碰撞盒/锚点骨骼有时把立方体挂在子骨骼上（uh60 的 Obb 下挂着 left/right 两块
    39x35 的大板，UV 只有 2x2 像素），只看骨骼自身名字会漏掉，
    这些板子会把机舱整块盖成灰色。
    """
    by_name = {b.name: b for b in model.bone_list}
    out = set()
    for b in model.bone_list:
        cur, depth = b, 0
        while cur is not None and depth < 32:
            if _is_helper_bone(cur.name):
                out.add(b.name)
                break
            cur = by_name.get(getattr(cur, "parent", None))
            depth += 1
    return out


def model_tris(model):
    """整模三角形列表（剔除碰撞盒与锚点骨骼及其子骨骼）。"""
    cache = {}
    helpers = _helper_bone_names(model)
    out = []
    for bone in model.bone_list:
        if bone.name in helpers:
            continue
        m = model.bone_matrix(bone.name, cache)
        for cube in bone.cubes:
            out.extend(cube_tris(cube, m, model.texture_width, model.texture_height))
        if getattr(bone, "polys", None):
            out.extend(mesh_tris(bone.polys, m, model.texture_width, model.texture_height))
    return out


# ---------------------------------------------------------------- 等轴测投影与光栅化

def _view_matrix(yaw_deg, pitch_deg):
    """世界 -> 视图：先绕 Y 偏航，再绕 X 俯仰。视图空间 z 为深度。"""
    yaw = math.radians(yaw_deg)
    pitch = math.radians(pitch_deg)
    ry = np.array([[math.cos(yaw), 0, math.sin(yaw)],
                   [0, 1, 0],
                   [-math.sin(yaw), 0, math.cos(yaw)]], dtype=float)
    rx = np.array([[1, 0, 0],
                   [0, math.cos(pitch), -math.sin(pitch)],
                   [0, math.sin(pitch), math.cos(pitch)]], dtype=float)
    return rx @ ry


def _sample(tex, u, v):
    """最近邻取样，uv 为归一化坐标，越界返回 None。"""
    h, w = tex.shape[0], tex.shape[1]
    xi = np.clip((np.asarray(u) * w).astype(int), 0, w - 1)
    yi = np.clip((np.asarray(v) * h).astype(int), 0, h - 1)
    return tex[yi, xi]


def _shade_of(view_tri):
    """按三角形（视图空间）主法线方向取明暗系数。"""
    a, b, c = (np.asarray(p, dtype=float) for p in view_tri)
    n = np.cross(b - a, c - a)
    if not np.any(n):
        return 0.8
    # 视图空间主轴 -> 模型空间的近似朝向（视图矩阵只是旋转，主轴仍对应 6 个方向）
    axis = int(np.argmax(np.abs(n)))
    sign = 1.0 if n[axis] > 0 else -1.0
    key = tuple(int(round(v)) for v in np.eye(3)[axis] * sign)
    return FACE_SHADE.get(key, 0.8)


def render_icon(geo_path, texture_path, out_path, size=128, fill=0.85,
                yaw=DEFAULT_YAW, pitch=DEFAULT_PITCH, ss=SUPERSAMPLE, near_smaller=True,
                cutout=True):
    """把模型渲染成 size×size 图标：透明背景、内容占 fill、居中。

    cutout=True 时丢弃贴图里的全透明像素（还原 SBM 的镂空表现）；
    若模型有面的 UV 落在贴图透明留白上，这些面会整块消失（表现为车头/车体缺口），
    此时传 cutout=False 保留全部面。
    """
    model = Model(geo_path)
    tex = np.asarray(Image.open(texture_path).convert("RGBA"), dtype=np.uint8)
    tris = model_tris(model)
    if not tris:
        raise SystemExit("模型没有可渲染的面（UV 全为空？）")

    view = _view_matrix(yaw, pitch)
    pos = np.concatenate([np.asarray(t[0], dtype=float) for t in tris], axis=0)
    view_pos = (view @ pos.T).T
    lo, hi = view_pos.min(axis=0), view_pos.max(axis=0)
    span = np.maximum(hi[:2] - lo[:2], 1e-6)

    big = size * ss
    scale = (big * fill) / max(span[0], span[1])
    cx, cy = (lo[0] + hi[0]) / 2.0, (lo[1] + hi[1]) / 2.0

    canvas = np.zeros((big, big, 4), dtype=np.uint8)
    # 深度缓冲：逐像素保存最近视图深度，避免画家算法在互相穿插/共面的立方体上出错。
    # 相机位于视图 +Z 远处，故 near_smaller=False 表示"depth 越大越近"（默认）。
    zbuf = np.full((big, big), np.inf if near_smaller else -np.inf, dtype=np.float64)
    ys, xs = np.mgrid[0:big, 0:big]

    for tri, uv in tris:
        vp = (view @ np.asarray(tri, dtype=float).T).T
        sx = (vp[:, 0] - cx) * scale + big / 2.0
        sy = big / 2.0 - (vp[:, 1] - cy) * scale
        x0, x1 = int(math.floor(sx.min())), int(math.ceil(sx.max()))
        y0, y1 = int(math.floor(sy.min())), int(math.ceil(sy.max()))
        if x1 < 0 or y1 < 0 or x0 >= big or y0 >= big:
            continue
        x0, x1 = max(x0, 0), min(x1, big - 1)
        y0, y1 = max(y0, 0), min(y1, big - 1)
        if x1 < x0 or y1 < y0:
            continue

        sub_x = xs[y0:y1 + 1, x0:x1 + 1] + 0.5
        sub_y = ys[y0:y1 + 1, x0:x1 + 1] + 0.5
        ax, ay = sx[0], sy[0]
        bx, by = sx[1], sy[1]
        cx2, cy2 = sx[2], sy[2]
        denom = (by - cy2) * (ax - cx2) + (cx2 - bx) * (ay - cy2)
        if abs(denom) < 1e-9:
            continue
        w0 = ((by - cy2) * (sub_x - cx2) + (cx2 - bx) * (sub_y - cy2)) / denom
        w1 = ((cy2 - ay) * (sub_x - cx2) + (ax - cx2) * (sub_y - cy2)) / denom
        w2 = 1.0 - w0 - w1
        mask = (w0 >= -1e-6) & (w1 >= -1e-6) & (w2 >= -1e-6)
        if not mask.any():
            continue

        u = w0 * uv[0][0] + w1 * uv[1][0] + w2 * uv[2][0]
        v = w0 * uv[0][1] + w1 * uv[1][1] + w2 * uv[2][1]
        px = _sample(tex, u, v).astype(np.float32)
        px[..., :3] *= _shade_of(tri)
        alpha = px[..., 3]
        if (not cutout) or os.environ.get("ICON_NO_CUTOUT"):
            alpha = np.full_like(alpha, 255)
        # cutout：完全透明像素丢弃（与 SBM 空面/透明像素表现一致）
        depth = w0 * vp[0, 2] + w1 * vp[1, 2] + w2 * vp[2, 2]
        zbuf_region = zbuf[y0:y1 + 1, x0:x1 + 1]
        closer = (depth < zbuf_region) if near_smaller else (depth > zbuf_region)
        mask = mask & (alpha >= 16) & closer
        if not mask.any():
            continue

        region = canvas[y0:y1 + 1, x0:x1 + 1]
        region[mask] = px[mask].astype(np.uint8)
        zbuf_region[mask] = depth[mask]

    img = Image.fromarray(canvas, "RGBA").resize((size, size), Image.LANCZOS)
    # 按 alpha 内容边界裁紧后等比放大到 fill，保证不同尺寸模型视觉重量一致
    bbox = img.getchannel("A").point(lambda a: 255 if a > 8 else 0).getbbox()
    if bbox:
        cropped = img.crop(bbox)
        target = int(size * fill)
        ratio = min(target / cropped.width, target / cropped.height)
        new = cropped.resize((max(1, int(cropped.width * ratio)),
                              max(1, int(cropped.height * ratio))), Image.LANCZOS)
        final = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        final.paste(new, ((size - new.width) // 2, (size - new.height) // 2), new)
    else:
        final = img
    final.save(out_path)
    return final


def main():
    if len(sys.argv) < 4:
        print(__doc__)
        raise SystemExit(2)
    geo, tex, out = sys.argv[1], sys.argv[2], sys.argv[3]
    size = int(sys.argv[4]) if len(sys.argv) > 4 else 128
    fill = float(sys.argv[5]) if len(sys.argv) > 5 else 0.85
    img = render_icon(geo, tex, out, size=size, fill=fill)
    print("写出", out, img.size)


if __name__ == "__main__":
    main()

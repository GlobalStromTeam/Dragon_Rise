#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""批量生成集装箱图标（128×128，2:1 等距、车头朝右下）。

SBW 的 ContainerItemDecorator 只认这个路径：
    assets/superbwarfare/textures/vehicle_icon/container/<载具id>.png
（命名空间写死 superbwarfare，vehicleId 取实体类型路径，即载具 JSON 的文件名）

视角约定：
    全部载具统一 yaw=225（模型机头/车头朝 -Z）
    pitch=26.565 即 atan(1/2)，2:1 等距

    注：曾把飞机单独设成 yaw=45（假设机头朝 +Z），实测是错的——用现网图标做
    剪影比对（angle_scan.py --ref），j20 在 225 的 IoU 0.859 / 45 仅 0.448，
    camel 在 225 的 IoU 0.827 / 45 仅 0.505。飞机与地面载具的机头朝向一致。

用法：
    py tools/batch_container_icons.py                     # 处理本项目全部载具
    py tools/batch_container_icons.py --ids cav,zsd89a    # 只处理指定载具
    py tools/batch_container_icons.py --root <另一版本目录>  # 处理 1.21.1 等副本
    py tools/batch_container_icons.py --dry-run           # 只列出将要处理的项
"""
import argparse
import json
import os
import shutil
import sys

from PIL import Image

TOOLS = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, TOOLS)
import vehicle_icon_render as v  # noqa: E402

SIZE = 128
FILL = 0.85
SS = 6
PITCH = 26.565
YAW = 225.0


def to_local(res, rl):
    """dragonrise_reforge:textures/x.png -> <res>/assets/dragonrise_reforge/textures/x.png"""
    ns, _, path = rl.partition(":")
    return os.path.join(res, "assets", ns, *path.split("/"))


def find_tex(res, tex_rl):
    """贴图缺失时回退到 <base>_sand / <base>_green / <base>*.png。"""
    p = to_local(res, tex_rl)
    if os.path.exists(p):
        return p
    ent = os.path.dirname(p)
    base = os.path.splitext(os.path.basename(p))[0]
    for suf in ("_sand", "_green"):
        q = os.path.join(ent, base + suf + ".png")
        if os.path.exists(q):
            return q
    if os.path.isdir(ent):
        cand = sorted((f for f in os.listdir(ent)
                       if f.startswith(base) and f.endswith(".png")), key=len)
        if cand:
            return os.path.join(ent, cand[0])
    return None


def collect(root, only_ids):
    """返回 [(id, geo, tex_rl)]。"""
    res = os.path.join(root, "src", "main", "resources")
    veh_dir = os.path.join(res, "data", "dragonrise_reforge", "sbw", "vehicles")
    if not os.path.isdir(veh_dir):
        raise SystemExit("找不到载具目录: " + veh_dir)
    out = []
    for fn in sorted(os.listdir(veh_dir)):
        if not fn.endswith(".json"):
            continue
        vid = fn[:-5]
        if only_ids and vid not in only_ids:
            continue
        with open(os.path.join(veh_dir, fn), encoding="utf-8-sig") as fp:
            try:
                d = json.load(fp)
            except Exception as e:
                print("  [跳过] %-18s JSON 解析失败 %s" % (vid, e))
                continue
        # 优先 Models[0]：部分载具的 Model(单数) 还是旧占位模型
        m = ((d.get("Models") or [{}])[0] if d.get("Models") else {}) or d.get("Model") or {}
        geo_rl, tex_rl = m.get("Model"), m.get("Texture")
        if not geo_rl or not tex_rl:
            print("  [跳过] %-18s JSON 无模型/贴图" % vid)
            continue
        geo = to_local(res, geo_rl)
        if not os.path.exists(geo):
            print("  [跳过] %-18s 模型不存在 %s" % (vid, os.path.basename(geo_rl)))
            continue
        out.append((vid, geo, tex_rl))
    return out, res


def main():
    ap = argparse.ArgumentParser(description="批量生成集装箱图标")
    ap.add_argument("--root", default=os.path.dirname(TOOLS), help="项目根目录（默认脚本所在项目）")
    ap.add_argument("--ids", default="", help="逗号分隔的载具 id，留空表示全部")
    ap.add_argument("--dry-run", action="store_true", help="只列出，不写文件")
    args = ap.parse_args()

    root = os.path.abspath(args.root)
    only_ids = {s.strip() for s in args.ids.split(",") if s.strip()}
    jobs, res = collect(root, only_ids)
    out_dir = os.path.join(res, "assets", "superbwarfare", "textures", "vehicle_icon", "container")
    build_dir = os.path.join(root, "build", "resources", "main", "assets",
                             "superbwarfare", "textures", "vehicle_icon", "container")
    os.makedirs(out_dir, exist_ok=True)

    ok, skip = [], []
    for vid, geo, tex_rl in jobs:
        tex = find_tex(res, tex_rl)
        if not tex:
            skip.append((vid, "贴图缺失 " + os.path.basename(tex_rl)))
            continue
        if args.dry_run:
            print("  %-18s %s yaw=%.0f" % (vid, os.path.basename(geo), YAW))
            continue
        out = os.path.join(out_dir, vid + ".png")
        try:
            v.render_icon(geo, tex, out, size=SIZE, fill=FILL,
                          yaw=YAW, pitch=PITCH, ss=SS, near_smaller=False)
        except BaseException as e:
            skip.append((vid, "渲染失败 " + str(e)[:60]))
            continue
        if os.path.isdir(os.path.dirname(build_dir)):
            os.makedirs(build_dir, exist_ok=True)
            shutil.copyfile(out, os.path.join(build_dir, vid + ".png"))
        ok.append(vid)

    if args.dry_run:
        return
    print("完成 %d 个 -> %s" % (len(ok), out_dir))
    if skip:
        print("跳过 %d 个：" % len(skip))
        for vid, why in skip:
            print("   %-18s %s" % (vid, why))


if __name__ == "__main__":
    main()

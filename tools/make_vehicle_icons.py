#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""为某个载具生成两张图标（离线渲染，不依赖 Blockbench）：

1) 集装箱物品图标：assets/superbwarfare/textures/vehicle_icon/container/<id>.png
   128×128 RGBA、透明背景、内容占 85%（与 SBW 原版容器图标一致）。
   ⚠ 必须放 superbwarfare 命名空间：SBW 的 ContainerItemDecorator 把命名空间写死为 superbwarfare。

2) 载具 GUI/地图图标：assets/dragonrise_reforge/textures/vehicle_icon/<id>_icon.png
   512×128 RGBA 侧视图（VehicleIcon，用于战术地图标记与击杀提示）。

用法：
    py tools/make_vehicle_icons.py <载具id> [模型名] [--banner-only|--container-only] [--no-flag] [--margin N]
    --banner-only     只出 512×128 击杀横幅，不碰集装箱图标
    --container-only  只出 128×128 集装箱图标，不碰击杀横幅
    --no-flag         不在横幅右下角压阵营旗（非中国阵营载具用）
    --margin          横幅内容留白，默认 6；要与既有批量图标（内容高 125）齐平用 1.5
    --root            目标项目根目录，默认脚本所在项目（可用于操作 1.21.1 那份源码）

<载具id> 同时是实体 id 与 data/.../vehicles/<id>.json 的文件名；模型名默认与 id 相同，
模型取 assets/dragonrise_reforge/models/bedrock/vehicle/<模型名>.geo.json，
贴图取 assets/dragonrise_reforge/textures/entity/<模型名>.png。

说明：本脚本是 Blockbench 图标流程（tools/载具图标渲染流程.md）的离线替代方案，
画质略逊于 Blockbench 的抗锯齿渲染；要出精品图仍建议走原流程。
"""
import os
import sys

import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import vehicle_icon_render as v  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets")
CONTAINER_DIR = os.path.join(ASSETS, "superbwarfare", "textures", "vehicle_icon", "container")
GUI_DIR = os.path.join(ASSETS, "dragonrise_reforge", "textures", "vehicle_icon")
VEHICLE_MODELS = os.path.join(ASSETS, "dragonrise_reforge", "models", "bedrock", "vehicle")
VEHICLE_TEX = os.path.join(ASSETS, "dragonrise_reforge", "textures", "entity")


def set_root(root):
    """切换目标项目（用于在 1.20.1 的工具里操作 1.21.1 那份源码）。"""
    global ROOT, ASSETS, CONTAINER_DIR, GUI_DIR, VEHICLE_MODELS, VEHICLE_TEX
    ROOT = os.path.abspath(root)
    ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets")
    CONTAINER_DIR = os.path.join(ASSETS, "superbwarfare", "textures", "vehicle_icon", "container")
    GUI_DIR = os.path.join(ASSETS, "dragonrise_reforge", "textures", "vehicle_icon")
    VEHICLE_MODELS = os.path.join(ASSETS, "dragonrise_reforge", "models", "bedrock", "vehicle")
    VEHICLE_TEX = os.path.join(ASSETS, "dragonrise_reforge", "textures", "entity")

BANNER_SIZE = (512, 128)
BANNER_MARGIN = 6

# 集装箱图标视角：2:1 等距（atan 1/2 = 26.565°），车头朝右下、看得见车顶。
# 这批载具模型的车头朝 -Z（炮管/首甲在负 Z），故 yaw=225 才能把车头转到右下，
# 与 SBW 官方 jar 图标（M1A2/T-90A/Bradley/LAV-25）的角度一致。
CONTAINER_YAW = 225.0
CONTAINER_PITCH = 26.565
# 细节密的模型（近千个立方体）3 倍超采样偏粗，集装箱图标用 6 倍
CONTAINER_SUPERSAMPLE = 6

# 阵营小旗：官方图标把国旗压在右下角（见 j20_icon.png / kv1_icon.png）。
# 这里直接复用本模组已有图标里的旗子（同阵营载具共用一张旗），避免重画。
FLAG_SOURCE = os.path.join(GUI_DIR, "j20_icon.png")
FLAG_BOX_J20 = (377, 85, 437, 126)   # j20_icon.png 中五星红旗的像素范围


def overlay_flag(banner, flag_box=FLAG_BOX_J20, source=FLAG_SOURCE):
    """把参考图标里的阵营旗贴到横幅右下角（与官方图标同位置）。"""
    if not os.path.exists(source):
        return banner
    flag = Image.open(source).convert("RGBA").crop(flag_box)
    x0, y0, x1, y1 = flag_box
    banner.alpha_composite(flag, (x0, y0))
    return banner


def _robust_bbox(img, alpha_min=8, min_neighbors=5):
    """按 alpha 求内容边界，但忽略细长突出物（天线/空速管等 1px 线）：
    先对掩码做一次 3x3 腐蚀，再取边界；这样取景不会被细线拉大。"""
    alpha = np.asarray(img.getchannel("A"), dtype=np.int16)
    mask = (alpha > alpha_min).astype(np.int16)
    if not mask.any():
        return None
    pad = np.pad(mask, 1)
    neigh = sum(pad[i:i + mask.shape[0], j:j + mask.shape[1]]
                for i in range(3) for j in range(3))
    core = (mask > 0) & (neigh >= min_neighbors)
    if not core.any():
        core = mask > 0
    ys, xs = np.nonzero(core)
    # 核心区域外扩 2px，避免把主体边缘削掉
    return (max(int(xs.min()) - 2, 0), max(int(ys.min()) - 2, 0),
            min(int(xs.max()) + 3, img.width), min(int(ys.max()) + 3, img.height))


def make_banner(geo, tex, out_path, supersample=3, flag=True, margin=BANNER_MARGIN):
    """512×128 侧视图横幅（VehicleIcon）：车头朝右、按高度贴合、透明背景。"""
    tmp = out_path + ".tmp.png"
    # 侧视：yaw=90 时模型 +Z（车尾）投影到屏幕 +X，因此车头朝左（与官方图标一致），pitch=0 为纯侧视
    v.render_icon(geo, tex, tmp, size=512, fill=1.0, yaw=90.0, pitch=0.0,
                  ss=supersample)
    src = Image.open(tmp).convert("RGBA")
    os.remove(tmp)
    bbox = _robust_bbox(src)
    if not bbox:
        raise SystemExit("侧视图渲染为空")
    cropped = src.crop(bbox)
    limit_w = BANNER_SIZE[0] - 2 * margin
    limit_h = BANNER_SIZE[1] - 2 * margin
    ratio = min(limit_w / cropped.width, limit_h / cropped.height)
    scaled = cropped.resize((max(1, int(cropped.width * ratio)),
                             max(1, int(cropped.height * ratio))), Image.LANCZOS)
    banner = Image.new("RGBA", BANNER_SIZE, (0, 0, 0, 0))
    # alpha_composite 而非 paste(..., scaled)：后者会把图像自身当 mask，alpha 被平方，
    # 细天线等半透明边缘会消失。
    banner.alpha_composite(scaled, ((BANNER_SIZE[0] - scaled.width) // 2,
                                    (BANNER_SIZE[1] - scaled.height) // 2))
    if flag:
        banner = overlay_flag(banner)
    banner.save(out_path)
    return banner


def main():
    argv = sys.argv[1:]
    opts, args, margin, root = set(), [], BANNER_MARGIN, ""
    i = 0
    while i < len(argv):
        a = argv[i]
        if a == "--margin":
            margin = float(argv[i + 1]); i += 2; continue
        if a == "--root":
            root = argv[i + 1]; i += 2; continue
        if a.startswith("--"):
            opts.add(a)
        else:
            args.append(a)
        i += 1
    if not args:
        print(__doc__)
        raise SystemExit(2)
    if root:
        set_root(root)
    vehicle_id = args[0]
    model_name = args[1] if len(args) > 1 else vehicle_id
    geo = os.path.join(VEHICLE_MODELS, model_name + ".geo.json")
    tex = os.path.join(VEHICLE_TEX, model_name + ".png")
    for path in (geo, tex):
        if not os.path.exists(path):
            raise SystemExit("缺少文件: " + path)

    os.makedirs(CONTAINER_DIR, exist_ok=True)
    os.makedirs(GUI_DIR, exist_ok=True)

    container_path = os.path.join(CONTAINER_DIR, vehicle_id + ".png")
    gui_path = os.path.join(GUI_DIR, vehicle_id + "_icon.png")

    written = []
    if "--banner-only" not in opts:
        # 集装箱图标: 2:1 等距、车头朝右下、看得见车顶（与 SBW 原版一致）。
        # yaw=225 让车头（模型 -Z 方向）落到画面右下；
        # near_smaller=False 才是"depth 越大越近"，否则会渲染出背面（看起来像仰视图）。
        container = v.render_icon(geo, tex, container_path, size=128, fill=0.85,
                                  yaw=CONTAINER_YAW, pitch=CONTAINER_PITCH,
                                  ss=CONTAINER_SUPERSAMPLE, near_smaller=False)
        written.append(container_path)
        print("集装箱图标 ->", os.path.relpath(container_path, ROOT), container.size)

    if "--container-only" not in opts:
        banner = make_banner(geo, tex, gui_path, flag="--no-flag" not in opts, margin=margin)
        written.append(gui_path)
        print("GUI 图标   ->", os.path.relpath(gui_path, ROOT), banner.size)

    # 开发客户端从 build/resources/main 读资源，顺手同步一份
    for src in written:
        if src == gui_path:
            dst_dir = os.path.join(ROOT, "build", "resources", "main", "assets", "dragonrise_reforge", "textures", "vehicle_icon")
        else:
            dst_dir = os.path.join(ROOT, "build", "resources", "main", "assets", "superbwarfare", "textures", "vehicle_icon", "container")
        if os.path.isdir(os.path.dirname(dst_dir)):
            os.makedirs(dst_dir, exist_ok=True)
            dst = os.path.join(dst_dir, os.path.basename(src))
            Image.open(src).save(dst)
            print("同步到 build ->", os.path.relpath(dst, ROOT))


if __name__ == "__main__":
    main()

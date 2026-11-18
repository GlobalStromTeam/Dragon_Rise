# Dragonrise 发动机音效远端服务器同步

适用版本为 Minecraft 1.20.1 / Forge。同步范围包括这一轮的发动机响度与衰减距离、履带和轮胎运动声、涉水声，以及直升机近、中、远三层启停声。

## 选哪种同步方式

| 远端服务器和玩家当前的情况 | 操作 |
| --- | --- |
| 还在用移植发动机系统之前的 Dragonrise，或旧版还没有直升机分层启停状态机 | 服务端和客户端一起更新 Dragonrise JAR |
| 所有人已经装好支持当前声组格式和状态机的 Dragonrise，之后只改录音、响度、衰减距离或声组参数 | 通过服务器下发客户端资源包 |
| 服务器上已有平衡数据包，覆盖了车型的油门、机动和耗油数值 | 这些数值仍由世界数据包管理，按原命名空间和路径部署 |

发动机声组由客户端从 `assets/dragonrise_reforge/squad_engines/` 读取。世界存档的 `datapacks/` 和服务端 `/reload` 负责服务端数据。发动机启停、HUD、音效状态机和网络同步由模组代码提供。

本地这次验证过的构建是 [dragonrise_reforge-1.5.1-20261116.jar](D:/minecraft/modp/GScode/build/libs/dragonrise_reforge-1.5.1-20261116.jar)。和旧构建可能同名，建议用 `Get-FileHash -Algorithm SHA256` 核对实际文件。即便文件名一样，也要确认服务端和客户端拿的是同一构建。

## 首次部署或更新状态机

1. 停服，备份远端的旧 Dragonrise JAR、世界和配置。
2. 把新版 JAR 放进远端服务器的 `mods/`，移出旧版 Dragonrise JAR，避免同时加载两个版本。
3. 玩家在各自游戏实例的 `mods/` 里换成同一 JAR，并使用兼容的 Superb Warfare 依赖。
4. 启动服务器，玩家重新进入游戏验证。当前 JAR 已内置音频和声组，可以直接播放。

这次移植增加了发动机启停网络消息，Dragonrise 的网络协议从 1 升到了 2。资源包只更新听到的声音和关联配置，客户端播放代码及服务端状态同步需通过 JAR 更新。当前声音属于 `dragonrise_reforge` 命名空间。若目标是仅安装 FCP 的服务器，需要另做 FCP 车型与资源映射，Dragonrise 的启停状态机仍由 Dragonrise 模组提供。

## 只同步音效资源

### 打包

资源包根目录采用下面的结构。Minecraft 1.20.1 资源包格式是 `15`。

```text
dragonrise-engine-audio-r1.zip
├── pack.mcmeta
└── assets/
    └── dragonrise_reforge/
        ├── sounds.json
        ├── squad_engines/
        │   └── *.json
        └── sounds/
            ├── engine/
            └── movement/
```

当前引擎声组也引用了运动层，所以打包时一起提取两个声音目录。若只想覆盖车外响度或传播距离，可更新所需的引擎事件，声组中的其它引用继续由已安装的模组提供。

在本地项目根目录执行下面的 PowerShell 命令。它从当前 JAR 提取音频和配置，过滤出这批音效的事件，并为每个事件加上 `"replace": true`。这样资源包会用新的事件覆盖模组内置定义，避免把新录音和旧录音一并加入随机播放列表。

```powershell
@'
import json
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED

jar = Path("build/libs/dragonrise_reforge-1.5.1-20261116.jar")
out = Path("build/dragonrise-engine-audio-r1.zip")
ns = "assets/dragonrise_reforge/"
prefixes = tuple(ns + folder for folder in (
    "squad_engines/", "sounds/engine/", "sounds/movement/"
))
meta = {"pack": {"pack_format": 15, "description": "Dragonrise vehicle engine audio r1"}}

with ZipFile(jar) as mod:
    files = [name for name in mod.namelist()
             if name.startswith(prefixes) and not name.endswith("/")]
    sounds = json.loads(mod.read(ns + "sounds.json"))
    events = {
        name: {**event, "replace": True}
        for name, event in sounds.items()
        if name.startswith(("engine/", "movement/"))
    }
    assert files and events, "No matching sound assets in this JAR"
    for event in events.values():
        for sample in event["sounds"]:
            ident = sample if isinstance(sample, str) else sample["name"]
            namespace, path = ident.split(":", 1)
            assert f"assets/{namespace}/sounds/{path}.ogg" in files, ident
    with ZipFile(out, "x", compression=ZIP_DEFLATED) as pack:
        pack.writestr("pack.mcmeta", json.dumps(meta))
        pack.writestr(ns + "sounds.json", json.dumps(events, indent=2))
        for name in files:
            pack.writestr(name, mod.read(name))

print(f"Created {out} with {len(events)} sound events")
'@ | & 'C:/Users/Administrator/.dsh/dsh-runtimes/dsh-primary-runtime/dependencies/python/python.exe' -

(Get-FileHash -Algorithm SHA1 -LiteralPath 'build/dragonrise-engine-audio-r1.zip').Hash.ToLowerInvariant()
```

如果已安装常规 Python 3，可把脚本最后一行的解释器路径换成 `python`。ZIP 以新建模式写入，已有同名文件时换一个版本号，例如 `r2`。改动资源包后重新计算 ZIP 的 SHA-1。

### 上传并配置服务器

1. 把 ZIP 上传到玩家客户端可以访问的 HTTPS 地址。地址直接返回 ZIP，与网盘预览页或需要登录的分享页区分开。
2. 修改远端实例的 `server.properties`，把下例替换成真实直链和上一步输出的 SHA-1。

   ```properties
   resource-pack=https://example.com/packs/dragonrise-engine-audio-r1.zip
   resource-pack-sha1=填写ZIP的40位SHA1
   require-resource-pack=false
   ```

3. 如果远端已经通过 `resource-pack` 下发整合包，先把这次的资源合入现有 ZIP，在原有命名空间的 `sounds.json` 中合并事件键，再上传合并后的 ZIP 并计算它的 SHA-1。Minecraft 1.20.1 的这项设置填写一个 URL。
4. 重启服务器，再让玩家重新连接并接受服务器资源包。`require-resource-pack=false` 允许拒绝下载，便于先测试。需要强制全服使用时，可改为 `true` 再重启。

也可以把同一个 ZIP 交给玩家放进客户端实例的 `resourcepacks/`，在资源包菜单启用并放到其它同类覆盖包上方。

## 验证与回退

1. **先验证模组功能。**找一架接入的直升机，确认驾驶员按默认键 `I` 后有启动进度，提前松键能取消点火，启动完成后能正常飞行。
2. **再验证两种视角。**同一架直升机做一次启动和停机，驾驶员在第一与第三人称之间切换，另一位玩家在不同距离听外部声。启停声应随飞机移动，切换时保持当前进度。
3. **核对地面音效。**用 M1 或 Bradley 检查车内怠速与车外加速，再用轮式车检查行驶时的轮胎声。若测试服务器资源包，确认客户端已接受并下载本次 ZIP。
4. **排查失败。**下载失败时核对直链和 ZIP 的 SHA-1。能进服但新声组无效时，检查客户端日志、ZIP 根目录以及当前 JAR 版本。载具行为和数值有差异时，再检查现有平衡数据包是否覆盖了车型配置。

只回退声音时，恢复上一版资源包 URL 与 SHA-1 后重启。回退模组版本时，服务端和客户端一起恢复匹配的 JAR。
